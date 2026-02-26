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
            monthlyExpenseRepository.getTotalMonthlyExpenseAmount(),
            transactionRepository.getTotalExpense(currentMonth),
            transactionRepository.getTotalExpenseBetweenDates(today, today),
            budgetRepository.getCategoryBudgets(currentMonth),
            transactionRepository.getExpenseSumByCategory(currentMonth),
            transactionRepository.getExpenseSumByCategoryBetweenDates(today, today)
        ) { results ->
            val prefs = results[0] as com.monetra.domain.model.UserPreferences
            val totalEmi = results[1] as Double
            val totalMonthlyExpense = results[2] as Double
            val monthlySpent = results[3] as Double
            val todaySpent = results[4] as Double
            @Suppress("UNCHECKED_CAST")
            val budgets = results[5] as List<com.monetra.domain.model.CategoryBudget>
            @Suppress("UNCHECKED_CAST")
            val monthlySums = results[6] as Map<String, Double>
            @Suppress("UNCHECKED_CAST")
            val todaySums = results[7] as Map<String, Double>

            // Active Budget Guards (visual only - no longer deducted from baseline)
            val budgetedCategories = budgets.filter { it.limit > 0 }.map { it.categoryName }.toSet()

            // 1. Monthly Baseline (Total money available for flexible spending this month)
            // Flexible spending = Income - Mandatory (EMI + Fixed Bills) - Savings Goal
            // Budget limits are NOT deducted here as per user request.
            val monthlyBaseline = (prefs.monthlyIncome - prefs.monthlySavingsGoal - totalEmi - totalMonthlyExpense)
            
            // 2. Total spent this month BEFORE today
            val spentBeforeToday = monthlySpent - todaySpent
            
            // 3. Remaining allowance for the rest of the month (including today)
            val remainingMonthAllowance = monthlyBaseline - spentBeforeToday
            
            // 4. Remaining days in the month (including today)
            val remainingDays = (daysInMonth - today.dayOfMonth + 1).coerceAtLeast(1)
            
            // 5. Daily limit (available flexible money divided by remaining days)
            val dailyLimit = remainingMonthAllowance / remainingDays
            
            // 6. Remaining for TODAY
            val remainingToday = dailyLimit - todaySpent

            SafeToSpend(
                dailyLimit = dailyLimit,
                remainingToday = remainingToday,
                monthlyAllowance = remainingMonthAllowance
            )
        }.flowOn(Dispatchers.Default)
    }
}
