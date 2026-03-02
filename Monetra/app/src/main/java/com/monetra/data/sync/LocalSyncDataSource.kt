package com.monetra.data.sync

import com.monetra.data.backup.model.BackupData
import com.monetra.data.local.MonetraDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalSyncDataSource @Inject constructor(
    private val db: MonetraDatabase
) {
    suspend fun getDirtyRecords(): BackupData {
        return BackupData(
            transactions = db.transactionDao.getUnsyncedTransactions(),
            savings = db.savingDao.getUnsyncedSavings(),
            goals = db.goalDao.getUnsyncedGoals(),
            monthlyReports = db.monthlyReportDao.getUnsyncedReports(),
            categoryBudgets = db.categoryBudgetDao.getUnsyncedBudgets(),
            investments = db.investmentDao.getUnsyncedInvestments(),
            loans = db.loanDao.getUnsyncedLoans(),
            monthlyExpenses = db.monthlyExpenseDao.getUnsyncedExpenses(),
            billInstances = db.monthlyExpenseDao.getUnsyncedInstances(),
            refundables = db.refundableDao.getUnsyncedRefundables(),
            userPreferences = db.userPreferencesDao.getUnsyncedPreferences()
        )
    }

    suspend fun markAsSynced(bundle: BackupData) {
        db.transactionDao.markAsSynced(bundle.transactions.map { it.remoteId })
        db.savingDao.markAsSynced(bundle.savings.map { it.remoteId })
        db.goalDao.markAsSynced(bundle.goals.map { it.remoteId })
        db.monthlyReportDao.markAsSynced(bundle.monthlyReports.map { it.remoteId })
        db.categoryBudgetDao.markAsSynced(bundle.categoryBudgets.map { it.remoteId })
        db.investmentDao.markAsSynced(bundle.investments.map { it.remoteId })
        db.loanDao.markAsSynced(bundle.loans.map { it.remoteId })
        db.monthlyExpenseDao.markAsSyncedExpenses(bundle.monthlyExpenses.map { it.remoteId })
        db.monthlyExpenseDao.markAsSyncedInstances(bundle.billInstances.map { it.remoteId })
        db.refundableDao.markAsSynced(bundle.refundables.map { it.remoteId })
        db.userPreferencesDao.markAsSynced()
    }

    suspend fun mergeRemoteData(remoteData: BackupData) {
        db.runInTransaction {
            kotlinx.coroutines.runBlocking {
                remoteData.transactions.forEach { db.transactionDao.upsertSync(it) }
                remoteData.savings.forEach { db.savingDao.upsertSync(it) }
                remoteData.goals.forEach { db.goalDao.upsertSync(it) }
                remoteData.monthlyReports.forEach { db.monthlyReportDao.upsertSync(it) }
                remoteData.categoryBudgets.forEach { db.categoryBudgetDao.upsertSync(it) }
                remoteData.investments.forEach { db.investmentDao.upsertSync(it) }
                remoteData.loans.forEach { db.loanDao.upsertSync(it) }
                remoteData.monthlyExpenses.forEach { db.monthlyExpenseDao.upsertSyncExpense(it) }
                remoteData.billInstances.forEach { db.monthlyExpenseDao.upsertSyncInstance(it) }
                remoteData.refundables.forEach { db.refundableDao.upsertSync(it) }
                remoteData.userPreferences.forEach { db.userPreferencesDao.upsertSync(it) }
            }
        }
    }
}
