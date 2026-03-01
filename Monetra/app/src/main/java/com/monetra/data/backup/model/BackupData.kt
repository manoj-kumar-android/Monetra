package com.monetra.data.backup.model

import com.monetra.data.local.entity.*
import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val transactions: List<TransactionEntity> = emptyList(),
    val savings: List<SavingEntity> = emptyList(),
    val goals: List<GoalEntity> = emptyList(),
    val monthlyReports: List<MonthlyReportEntity> = emptyList(),
    val categoryBudgets: List<CategoryBudgetEntity> = emptyList(),
    val investments: List<InvestmentEntity> = emptyList(),
    val loans: List<LoanEntity> = emptyList(),
    val monthlyExpenses: List<MonthlyExpenseEntity> = emptyList(),
    val billInstances: List<BillInstanceEntity> = emptyList(),
    val refundables: List<RefundableEntity> = emptyList(),
    val userPreferences: List<UserPreferencesEntity> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
)
