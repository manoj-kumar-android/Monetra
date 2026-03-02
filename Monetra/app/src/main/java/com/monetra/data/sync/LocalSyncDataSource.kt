package com.monetra.data.sync

import androidx.room.withTransaction
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
            userPreferences = db.userPreferencesDao.getUnsyncedPreferences(),
            deletedEntities = db.deletedEntityDao.getAll()
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
        db.deletedEntityDao.deleteByRemoteIds(bundle.deletedEntities.map { it.remoteId })
    }

    suspend fun mergeRemoteData(remoteData: BackupData) {
        db.withTransaction {
            // Delete entities marked as deleted remotely
            remoteData.deletedEntities.forEach { deleted ->
                when (deleted.entityType) {
                    "TRANSACTION" -> db.transactionDao.getTransactionByRemoteId(deleted.remoteId)?.let { db.transactionDao.deleteTransactionById(it.id) }
                    "SAVING" -> db.savingDao.getSavingByRemoteId(deleted.remoteId)?.let { db.savingDao.deleteSaving(it) }
                    "GOAL" -> db.goalDao.getGoalByRemoteId(deleted.remoteId)?.let { db.goalDao.deleteGoal(it.id) }
                    "INVESTMENT" -> db.investmentDao.getInvestmentByRemoteId(deleted.remoteId)?.let { db.investmentDao.deleteInvestment(it.id) }
                    "LOAN" -> db.loanDao.getLoanByRemoteId(deleted.remoteId)?.let { db.loanDao.deleteLoan(it.id) }
                    "REFUNDABLE" -> db.refundableDao.getRefundableByRemoteId(deleted.remoteId)?.let { db.refundableDao.deleteRefundable(it) }
                    "MONTHLY_EXPENSE" -> db.monthlyExpenseDao.getExpenseByRemoteId(deleted.remoteId)?.let { db.monthlyExpenseDao.deleteMonthlyExpense(it) }
                    "BILL_INSTANCE" -> db.monthlyExpenseDao.getInstanceByRemoteId(deleted.remoteId)?.let { db.monthlyExpenseDao.deleteBillInstanceById(it.id) }
                    "CATEGORY_BUDGET" -> db.categoryBudgetDao.getBudgetByRemoteId(deleted.remoteId)?.let { db.categoryBudgetDao.deleteBudget(it.categoryName) }
                }
            }

            // Filtering out any items that are also in the remote OR local deleted list 
            // to ensure local "pending" deletions are never restored by a remote pull.
            val remoteDeletedIds = remoteData.deletedEntities.map { it.remoteId }.toSet()
            val localDeletedIds = db.deletedEntityDao.getAll().map { it.remoteId }.toSet()
            val allDeletes = remoteDeletedIds + localDeletedIds

            remoteData.transactions.filterNot { it.remoteId in allDeletes }.forEach { db.transactionDao.upsertSync(it) }
            remoteData.savings.filterNot { it.remoteId in allDeletes }.forEach { db.savingDao.upsertSync(it) }
            remoteData.goals.filterNot { it.remoteId in allDeletes }.forEach { db.goalDao.upsertSync(it) }
            remoteData.monthlyReports.filterNot { it.remoteId in allDeletes }.forEach { db.monthlyReportDao.upsertSync(it) }
            remoteData.categoryBudgets.filterNot { it.remoteId in allDeletes }.forEach { db.categoryBudgetDao.upsertSync(it) }
            remoteData.investments.filterNot { it.remoteId in allDeletes }.forEach { db.investmentDao.upsertSync(it) }
            remoteData.loans.filterNot { it.remoteId in allDeletes }.forEach { db.loanDao.upsertSync(it) }
            remoteData.monthlyExpenses.filterNot { it.remoteId in allDeletes }.forEach { db.monthlyExpenseDao.upsertSyncExpense(it) }
            remoteData.billInstances.filterNot { it.remoteId in allDeletes }.forEach { db.monthlyExpenseDao.upsertSyncInstance(it) }
            remoteData.refundables.filterNot { it.remoteId in allDeletes }.forEach { db.refundableDao.upsertSync(it) }
            remoteData.userPreferences.filterNot { it.remoteId in allDeletes }.forEach { db.userPreferencesDao.upsertSync(it) }
        }
    }
}
