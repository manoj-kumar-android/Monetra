package com.monetra.data.backup

import com.monetra.domain.backup.BackupManager
import android.content.Context
import android.net.Uri
import com.monetra.data.local.MonetraDatabase
import com.monetra.data.local.model.BackupData
import com.monetra.data.security.EncryptionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: MonetraDatabase,
    private val encryptionManager: EncryptionManager
) : BackupManager {

    private val json = Json { 
        ignoreUnknownKeys = true
    }

    override suspend fun exportEncryptedBackup(uri: Uri, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val transactions = db.transactionDao.getAllTransactionsList()
            val savings = db.savingDao.getAllSavingsList()
            val goals = db.goalDao.getAllGoals()
            val monthlyReports = db.monthlyReportDao.getAllMonthlyReportsList()
            val categoryBudgets = db.categoryBudgetDao.getAllCategoryBudgets()
            val investments = db.investmentDao.getAllInvestments()
            val loans = db.loanDao.getAllLoansForBackUp()
            val monthlyExpenses = db.monthlyExpenseDao.getAllMonthlyExpensesList()
            val billInstances = db.monthlyExpenseDao.getAllBillInstances()
            val refundables = db.refundableDao.getAllRefundablesList()
            val userPreferences = db.userPreferencesDao.getAllUserPreferences()

            val backupData = BackupData(
                transactions = transactions,
                savings = savings,
                goals = goals,
                monthlyReports = monthlyReports,
                categoryBudgets = categoryBudgets,
                investments = investments,
                loans = loans,
                monthlyExpenses = monthlyExpenses,
                billInstances = billInstances,
                refundables = refundables,
                userPreferences = userPreferences,
                createdAt = System.currentTimeMillis()
            )

            val jsonString = json.encodeToString(backupData)
            val encryptedBytes = encryptionManager.encrypt(jsonString, password)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(encryptedBytes)
            } ?: return@withContext Result.failure(Exception("Could not open output stream"))

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
