package com.monetra.data.backup

import com.monetra.domain.backup.RestoreManager
import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.monetra.data.local.MonetraDatabase
import com.monetra.data.local.model.BackupData
import com.monetra.data.security.EncryptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RestoreManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: MonetraDatabase,
    private val encryptionManager: EncryptionManager
) : RestoreManager {

    private val json = Json { 
        ignoreUnknownKeys = true
    }

    override suspend fun restoreFromEncryptedUri(uri: Uri, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val encryptedBytes = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.readBytes()
            } ?: return@withContext Result.failure(Exception("Could not open input stream"))

            val jsonString = encryptionManager.decrypt(encryptedBytes, password)
            val backupData = json.decodeFromString<BackupData>(jsonString)
            
            performRestore(backupData)
        } catch (e: Exception) {
            e.printStackTrace()
            val message = when {
                e is javax.crypto.AEADBadTagException -> "Incorrect password or corrupted file."
                e is kotlinx.serialization.SerializationException -> "Backup file is corrupted or incompatible."
                e.message?.contains("decryption", ignoreCase = true) == true -> "Decryption error. Incorrect password."
                else -> e.message ?: "Unknown restoration error"
            }
            Result.failure(Exception(message))
        }
    }

    private suspend fun performRestore(backupData: BackupData): Result<Unit> {
        return try {
            val hasData = backupData.transactions.isNotEmpty() || 
                         backupData.savings.isNotEmpty() ||
                         backupData.loans.isNotEmpty() ||
                         backupData.userPreferences.any { it.isOnboardingCompleted }

            if (!hasData) {
                return Result.failure(Exception("Backup file contains no meaningful data to restore."))
            }

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

            // Give Room and UI observers a moment to process the mass deletion/insertion
            kotlinx.coroutines.delay(500)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
