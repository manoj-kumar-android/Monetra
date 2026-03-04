package com.monetra.data.repository

import android.content.Context
import androidx.room.withTransaction
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.monetra.data.backup.model.BackupData
import com.monetra.data.local.MonetraDatabase
import com.monetra.data.worker.FullBackupWorker
import com.monetra.domain.repository.BackupEvent
import com.monetra.domain.repository.CloudBackupRepository
import com.monetra.domain.repository.SyncRepository
import com.monetra.drivebackup.api.DriveBackupManager
import com.monetra.drivebackup.internal.security.EncryptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudBackupRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val db: MonetraDatabase,
    private val driveBackupManager: DriveBackupManager,
    private val encryptionManager: EncryptionManager,
    private val syncRepository: SyncRepository,
    private val syncManager: com.monetra.data.sync.SyncManager
) : CloudBackupRepository {

    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val _events = kotlinx.coroutines.flow.MutableSharedFlow<BackupEvent>()
    override val events = _events.asSharedFlow()

    private val _isRestoring = MutableStateFlow(false)
    override val isRestoring: Flow<Boolean> = _isRestoring.asStateFlow()

    override val syncState = syncManager.syncState
    override val accountName = driveBackupManager.accountName
    override val lastBackupTime = driveBackupManager.lastBackupTime
    override val recoveryIntent = driveBackupManager.getDrivePermissionIntent()

    override suspend fun runBackup(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val googleUserId = driveBackupManager.googleUserId.first()
            if (googleUserId.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Not authenticated with Google"))
            }

            val prefs = db.userPreferencesDao.getAllUserPreferences().firstOrNull()
            if (prefs?.isBackupEnabled != true) {
                return@withContext Result.failure(Exception("Backup is disabled in settings"))
            }

            val backupData = BackupData(
                transactions = db.transactionDao.getAllTransactionsList(),
                savings = db.savingDao.getAllSavingsList(),
                goals = db.goalDao.getAllGoals(),
                monthlyReports = db.monthlyReportDao.getAllMonthlyReportsList(),
                categoryBudgets = db.categoryBudgetDao.getAllCategoryBudgets(),
                investments = db.investmentDao.getAllInvestments(),
                loans = db.loanDao.getAllLoansForBackUp(),
                monthlyExpenses = db.monthlyExpenseDao.getAllMonthlyExpensesList(),
                billInstances = db.monthlyExpenseDao.getAllBillInstances(),
                refundables = db.refundableDao.getAllRefundablesList(),
                userPreferences = db.userPreferencesDao.getAllUserPreferences(),
                createdAt = System.currentTimeMillis()
            )

            val jsonString = json.encodeToString(backupData)
            val encryptedBytes = encryptionManager.encrypt(googleUserId, jsonString.toByteArray())

            val tempFile = File(context.cacheDir, "full_backup.enc")
            tempFile.writeBytes(encryptedBytes)

            val result = driveBackupManager.uploadRawFile(tempFile)
            tempFile.delete()
            
            if (result.isSuccess) {
                syncRepository.setDirty(false)
                syncRepository.setLastSyncedAt(System.currentTimeMillis())
            } else {
                handleError(result.exceptionOrNull())
            }
            
            result
        } catch (e: Exception) {
            android.util.Log.e("CloudBackup", "Backup failed", e)
            handleError(e)
            Result.failure(e)
        }
    }

    override suspend fun runSync(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            syncManager.runSync()
            Result.success(Unit)
        } catch (e: Exception) {
            handleError(e)
            Result.failure(e)
        }
    }

    override suspend fun runRestore(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _isRestoring.value = true
            // Safety Check: Is it safe to restore?
            val isDirty = syncRepository.isDirty().first()
            val isDbEmpty = db.transactionDao.getAllTransactionsList().isEmpty()
            
            if (isDirty && !isDbEmpty) {
                android.util.Log.w("CloudBackup", "Restore skipped: Local data is newer (dirty flag set)")
                return@withContext Result.failure(Exception("Unsynced local data found. Restore skipped to prevent data loss."))
            }

            val googleUserId = driveBackupManager.googleUserId.first()
            if (googleUserId.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Not authenticated with Google"))
            }

            val tempFile = File(context.cacheDir, "restore_temp.enc")
            val downloadResult = driveBackupManager.downloadRawFile(tempFile)
            
            if (downloadResult.isFailure || !downloadResult.getOrDefault(false)) {
                tempFile.delete()
                if (downloadResult.isFailure) handleError(downloadResult.exceptionOrNull())
                return@withContext Result.failure(Exception("No backup found on Google Drive"))
            }

            val encryptedBytes = tempFile.readBytes()
            tempFile.delete()

            val decryptedBytes = encryptionManager.decrypt(googleUserId, encryptedBytes)
            val jsonString = String(decryptedBytes)
            val backupData = json.decodeFromString<BackupData>(jsonString)

            performRestore(backupData)
            
            // After successful restore, it's synced
            syncRepository.setDirty(false)
            syncRepository.setLastSyncedAt(System.currentTimeMillis())
            
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("CloudBackup", "Restore failed", e)
            handleError(e)
            Result.failure(e)
        } finally {
            _isRestoring.value = false
        }
    }

    private suspend fun handleError(e: Throwable?) {
        if (e == null) return
        val message = e.message?.lowercase() ?: ""
        val isAuthError = message.contains("401") || 
                         message.contains("unauthorized") || 
                         message.contains("authenticated") ||
                         message.contains("permission") ||
                         message.contains("403") ||
                         message.contains("invalid_grant")
        
        if (isAuthError) {
            android.util.Log.e("CloudBackup", "Critical Auth Error detected: ${e.message}")
            driveBackupManager.signOut()
            _events.emit(BackupEvent.AuthError)
        }
    }

    private suspend fun performRestore(backupData: BackupData) {
        db.withTransaction {
            // Clear existing data
            db.transactionDao.deleteAllTransactions()
            db.savingDao.deleteAllSavings()
            db.goalDao.deleteAllGoals()
            db.monthlyReportDao.deleteAllMonthlyReports()
            db.categoryBudgetDao.deleteAllCategoryBudgets()
            db.investmentDao.deleteAllInvestments()
            db.loanDao.deleteAllLoans()
            db.monthlyExpenseDao.deleteAllMonthlyExpenses()
            db.monthlyExpenseDao.deleteAllBillInstances()
            db.refundableDao.deleteAllRefundables()
            db.userPreferencesDao.deleteAllUserPreferences()

            // Restore from backup (Ensuring all are marked as synced)
            db.transactionDao.insertAllTransactions(backupData.transactions.map { it.copy(isSynced = true) })
            db.savingDao.insertAllSavings(backupData.savings.map { it.copy(isSynced = true) })
            db.goalDao.insertAllGoals(backupData.goals.map { it.copy(isSynced = true) })
            db.monthlyReportDao.insertAllMonthlyReports(backupData.monthlyReports.map { it.copy(isSynced = true) })
            db.categoryBudgetDao.insertAllCategoryBudgets(backupData.categoryBudgets.map { it.copy(isSynced = true) })
            db.investmentDao.insertAllInvestments(backupData.investments.map { it.copy(isSynced = true) })
            db.loanDao.insertAllLoans(backupData.loans.map { it.copy(isSynced = true) })
            db.monthlyExpenseDao.insertAllMonthlyExpenses(backupData.monthlyExpenses.map { it.copy(isSynced = true) })
            db.monthlyExpenseDao.insertAllBillInstances(backupData.billInstances.map { it.copy(isSynced = true) })
            db.refundableDao.insertAllRefundables(backupData.refundables.map { it.copy(isSynced = true) })
            
            val prefs = if (backupData.userPreferences.isNotEmpty()) {
                backupData.userPreferences.first().copy(id = 0, isSynced = true, isBackupEnabled = true)
            } else {
                com.monetra.data.local.entity.UserPreferencesEntity(id = 0, isSynced = true, isBackupEnabled = true)
            }
            db.userPreferencesDao.upsertUserPreferences(prefs)
        }
    }

    override suspend fun isBackupAvailable(): Boolean {
        return driveBackupManager.isBackupAvailable()
    }

    override fun scheduleBackup() {
        // We use a CoroutineScope to check the database since scheduleBackup is not suspend
        kotlinx.coroutines.MainScope().launch(Dispatchers.IO) {
            val prefs = db.userPreferencesDao.getAllUserPreferences().firstOrNull()
            if (prefs?.isBackupEnabled == true) {
                val constraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<FullBackupWorker>()
                    .setConstraints(constraints)
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "full_drive_backup",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }
        }
    }

    override suspend fun checkDrivePermission(): Boolean {
        return driveBackupManager.checkDrivePermission()
    }

    override suspend fun signOut() {
        driveBackupManager.signOut()
    }
}
