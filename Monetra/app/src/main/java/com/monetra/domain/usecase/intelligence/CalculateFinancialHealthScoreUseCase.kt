package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.TransactionType
import com.monetra.domain.repository.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.YearMonth
import javax.inject.Inject

/**
 * Calculates a Financial Health Score (0-100) based on the Product Vision:
 * 
 * 1. Budget Discipline (30 pts): Adherence to category budgets.
 * 2. Savings Rate (30 pts): Percentage of income saved.
 * 3. EMI Ratio (20 pts): Debt burden vs Income.
 * 4. Burn Rate Trend (20 pts): Stability of spending vs previous month.
 */
class CalculateFinancialHealthScoreUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val budgetRepository: BudgetRepository,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val loanRepository: LoanRepository
) {
    operator fun invoke(month: YearMonth): Flow<Int> {
        val previousMonth = month.minusMonths(1)

        return combine(
            transactionRepository.getTransactions(month),
            transactionRepository.getTotalExpense(previousMonth),
            budgetRepository.getCategoryBudgets(month),
            userPreferenceRepository.getUserPreferences(),
            loanRepository.getTotalMonthlyEmi()
        ) { transactions, prevTotal, budgets, preferences, totalEmi ->
                val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
                val currentTotalSpent = expenses.sumOf { it.amount }
                val monthlyIncome = preferences.monthlyIncome
                
                // 1. Budget Discipline (30 points)
                val adherenceScore = if (budgets.isNotEmpty()) {
                    val withinLimitCount = budgets.count { it.currentSpent <= it.limit }
                    (30.0 * withinLimitCount / budgets.size).toInt()
                } else 30

                // 2. Savings Rate (30 points)
                // Target: 20% savings = full 30 points.
                val savingsRate = if (monthlyIncome > 0) {
                    ((monthlyIncome - currentTotalSpent - totalEmi) / monthlyIncome).coerceAtLeast(0.0)
                } else 0.0
                val savingsScore = (savingsRate / 0.20 * 30.0).coerceIn(0.0, 30.0).toInt()

                // 3. EMI Ratio (20 points)
                // Target: < 30% of income = full 20 pts. Penalty for > 45%.
                val emiRatio = if (monthlyIncome > 0) totalEmi / monthlyIncome else 0.0
                val emiScore = when {
                    emiRatio <= 0.30 -> 20
                    emiRatio >= 0.50 -> 0
                    else -> (20 * (1 - (emiRatio - 0.30) / 0.20)).toInt()
                }

                // 4. Burn Rate Trend (20 points)
                // Score based on spending stability vs previous month
                val growthScore = if (prevTotal > 0) {
                    val growth = (currentTotalSpent - prevTotal) / prevTotal
                    if (growth <= 0.05) 20 // 5% buffer is fine
                    else (20 * (1 - (growth - 0.05))).coerceIn(0.0, 20.0).toInt()
                } else 20

                (adherenceScore + savingsScore + emiScore + growthScore).coerceIn(0, 100)
        }
    }
}
