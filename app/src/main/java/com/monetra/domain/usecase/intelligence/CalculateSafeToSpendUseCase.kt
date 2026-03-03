package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.SafeToSpend
import com.monetra.domain.repository.LoanRepository
import com.monetra.domain.repository.MonthlyExpenseRepository
import com.monetra.domain.repository.TransactionRepository
import com.monetra.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Safe-To-Spend Engine
 * 
 * Calculates how much a user can afford to spend today while still meeting
 * their savings goals and mandatory EMI obligations.
 */
class CalculateSafeToSpendUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val loanRepository: LoanRepository,
    private val monthlyExpenseRepository: MonthlyExpenseRepository,
    private val budgetRepository: com.monetra.domain.repository.BudgetRepository
) {
    operator fun invoke(): Flow<SafeToSpend> {
        val today = LocalDate.now()
        val currentMonth = YearMonth.from(today)
        val daysInMonth = currentMonth.lengthOfMonth()

        return combine(
            userPreferenceRepository.getUserPreferences(),
            loanRepository.getTotalMonthlyEmi(),
            monthlyExpenseRepository.getAllMonthlyExpenses(),
            transactionRepository.getExpenseSumByCategory(currentMonth),
            transactionRepository.getExpenseSumByCategoryBetweenDates(today, today)
        ) { results ->
            val prefs = results[0] as com.monetra.domain.model.UserPreferences
            val totalEmi = results[1] as Double
            val monthlyBills = results[2] as List<com.monetra.domain.model.MonthlyExpense>
            val monthlySpentByCategory = results[3] as Map<String, Double>
            val todaySpentByCategory = results[4] as Map<String, Double>

            // 1. Calculate the Discretionary Pool
            // We subtract the TOTAL bill limits upfront, effectively "reserving" that money.
            val totalBillLimits = monthlyBills.sumOf { it.amount }
            val monthlyDiscretionaryPool = (prefs.monthlyIncome - prefs.monthlySavingsGoal - totalEmi - totalBillLimits)

            // 2. Calculate Effective Discretionary Spending
            // For Bill categories: only count spending that EXCEEDS the limit.
            // For Normal categories: count everything.
            val billCategories = monthlyBills.map { it.category }.toSet()
            
            // Calculate effective spent before today
            val totalMonthlySpent = monthlySpentByCategory.values.sum()
            val totalTodaySpent = todaySpentByCategory.values.sum()
            
            fun calculateEffectiveSpent(categoryMap: Map<String, Double>): Double {
                var effective = 0.0
                categoryMap.forEach { (cat, spent) ->
                    val limit = monthlyBills.find { it.category == cat }?.amount ?: 0.0
                    if (billCategories.contains(cat)) {
                        effective += (spent - limit).coerceAtLeast(0.0)
                    } else {
                        effective += spent
                    }
                }
                return effective
            }

            val effectiveSpentInMonth = calculateEffectiveSpent(monthlySpentByCategory)
            val effectiveSpentToday = calculateEffectiveSpent(todaySpentByCategory)
            
            val effectiveSpentBeforeToday = effectiveSpentInMonth - effectiveSpentToday

            // 3. Final Calculations
            val remainingAllowance = (monthlyDiscretionaryPool - effectiveSpentBeforeToday).coerceAtLeast(0.0)
            val remainingDays = (daysInMonth - today.dayOfMonth + 1).coerceAtLeast(1)

            val dailyLimit = remainingAllowance / remainingDays
            val remainingToday = dailyLimit - effectiveSpentToday

            SafeToSpend(
                dailyLimit = dailyLimit,
                remainingToday = remainingToday,
                monthlyAllowance = remainingAllowance
            )
        }.flowOn(Dispatchers.Default)
    }
}
