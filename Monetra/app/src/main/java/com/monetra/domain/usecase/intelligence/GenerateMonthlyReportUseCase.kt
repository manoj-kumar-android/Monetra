package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.MonthlyReport
import com.monetra.domain.model.TransactionType
import com.monetra.domain.repository.BudgetRepository
import com.monetra.domain.repository.TransactionRepository
import com.monetra.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import java.time.YearMonth
import javax.inject.Inject

class GenerateMonthlyReportUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val calculateHealthScore: CalculateFinancialHealthScoreUseCase,
    private val analyzeSpendingBehavior: AnalyzeSpendingBehaviorUseCase
) {
    operator fun invoke(month: YearMonth): Flow<MonthlyReport> {
        val previousMonth = month.minusMonths(1)
        
        return combine(
            combine(
                transactionRepository.getTransactions(month),
                transactionRepository.getTotalExpense(previousMonth),
                budgetRepository.getCategoryBudgets(month)
            ) { t, pt, b -> Triple(t, pt, b) },
            combine(
                userPreferenceRepository.getUserPreferences(),
                calculateHealthScore(month),
                analyzeSpendingBehavior(month)
            ) { p, s, b -> Triple(p, s, b) }
        ) { (transactions, prevTotal, budgets), (preferences, score, personality) ->
            val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
            val totalSpent = expenses.sumOf { it.amount }
            val totalIncome = preferences.monthlyIncome
            
            val categoryGroups = expenses.groupBy { it.category }
            val topCategory = categoryGroups.maxByOrNull { it.value.sumOf { tx -> tx.amount } }
            
            val largestExpense = expenses.maxByOrNull { it.amount }
            
            val savingsRate = if (totalIncome > 0) {
                ((totalIncome - totalSpent) / totalIncome * 100).coerceAtLeast(0.0)
            } else 0.0
            
            MonthlyReport(
                month = month,
                totalSpent = totalSpent,
                totalIncome = totalIncome,
                topCategory = topCategory?.key ?: "N/A",
                topCategoryAmount = topCategory?.value?.sumOf { it.amount } ?: 0.0,
                largestExpenseTitle = largestExpense?.title ?: "N/A",
                largestExpenseAmount = largestExpense?.amount ?: 0.0,
                previousMonthTotal = prevTotal,
                savingsRate = savingsRate,
                budgetDisciplineScore = score,
                budgetAdherenceCount = budgets.count { !it.isAlert },
                totalBudgetsCount = budgets.size,
                personality = personality
            )
        }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    }
}
