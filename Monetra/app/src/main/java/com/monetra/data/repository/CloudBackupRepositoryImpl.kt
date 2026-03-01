package com.monetra.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.monetra.data.backup.model.BackupData
import com.monetra.data.local.MonetraDatabase
import com.monetra.domain.repository.CloudBackupRepository
import com.monetra.drivebackup.api.DriveBackupManager
import com.monetra.drivebackup.internal.security.EncryptionManager
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.monetra.data.worker.FullBackupWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudBackupRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: MonetraDatabase,
    private val driveBackupManager: DriveBackupManager,
    private val encryptionManager: EncryptionManager
) : CloudBackupRepository {

    private val json = Json { 
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun runBackup(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val googleUserId = driveBackupManager.googleUserId.first()
            if (googleUserId.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Not authenticated with Google"))
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
            
            result
        } catch (e: Exception) {
            android.util.Log.e("CloudBackup", "Backup failed", e)
            Result.failure(e)
        }
    }

    override suspend fun runRestore(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val googleUserId = driveBackupManager.googleUserId.first()
            if (googleUserId.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Not authenticated with Google"))
            }

            val tempFile = File(context.cacheDir, "restore_temp.enc")
            val downloadResult = driveBackupManager.downloadRawFile(tempFile)
            
            if (downloadResult.isFailure || !downloadResult.getOrDefault(false)) {
                tempFile.delete()
                return@withContext Result.failure(Exception("No backup found on Google Drive"))
            }

            val encryptedBytes = tempFile.readBytes()
            tempFile.delete()

            val decryptedBytes = encryptionManager.decrypt(googleUserId, encryptedBytes)
            val jsonString = String(decryptedBytes)
            val backupData = json.decodeFromString<BackupData>(jsonString)

            performRestore(backupData)
            Result.success(Unit)
        } catch (e: Exception) {
            android.util.Log.e("CloudBackup", "Restore failed", e)
            Result.failure(e)
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

            // Restore from backup
            db.transactionDao.insertAllTransactions(backupData.transactions)
            db.savingDao.insertAllSavings(backupData.savings)
            db.goalDao.insertAllGoals(backupData.goals)
            db.monthlyReportDao.insertAllMonthlyReports(backupData.monthlyReports)
            db.categoryBudgetDao.insertAllCategoryBudgets(backupData.categoryBudgets)
            db.investmentDao.insertAllInvestments(backupData.investments)
            db.loanDao.insertAllLoans(backupData.loans)
            db.monthlyExpenseDao.insertAllMonthlyExpenses(backupData.monthlyExpenses)
            db.monthlyExpenseDao.insertAllBillInstances(backupData.billInstances)
            db.refundableDao.insertAllRefundables(backupData.refundables)
            db.userPreferencesDao.insertAllUserPreferences(backupData.userPreferences)
        }
    }

    override suspend fun isBackupAvailable(): Boolean {
        return driveBackupManager.isBackupAvailable()
    }

    override fun scheduleBackup() {
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
