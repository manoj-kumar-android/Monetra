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
    private val monthlyExpenseRepository: MonthlyExpenseRepository
) {
    operator fun invoke(): Flow<SafeToSpend> {
        val today = LocalDate.now()
        val currentMonth = YearMonth.from(today)
        val daysInMonth = currentMonth.lengthOfMonth()
        val dayOfMonth = today.dayOfMonth

        return combine(
            userPreferenceRepository.getUserPreferences(),
            loanRepository.getTotalMonthlyEmi(),
            monthlyExpenseRepository.getTotalMonthlyExpenseAmount(),
            transactionRepository.getTotalExpense(currentMonth),
            transactionRepository.getTotalExpenseBetweenDates(today, today)
        ) { prefs, totalEmi, totalMonthlyExpense, monthlySpent, todaySpent ->
            
            // 1. Calculate Monthly Baseline (Income - Savings - Fixed Costs)
            val monthlyBaseline = (prefs.monthlyIncome - prefs.monthlySavingsGoal - totalEmi - totalMonthlyExpense)
            
            // 2. Calculate what was spent this month BEFORE today
            val spentBeforeToday = monthlySpent - todaySpent
            
            // 3. Remaining budget for the rest of the month
            val remainingMonthBudget = monthlyBaseline - spentBeforeToday
            
            // 4. Remaining days in the month (including today)
            val remainingDays = (daysInMonth - today.dayOfMonth + 1).coerceAtLeast(1)
            
            // 5. Today's initial daily limit
            val dailyBudget = remainingMonthBudget / remainingDays
            
            // 6. Remaining for TODAY specifically (can be negative if overspent today)
            val remainingToday = dailyBudget - todaySpent

            SafeToSpend(
                dailyLimit = dailyBudget,
                remainingToday = remainingToday,
                monthlyAllowance = remainingMonthBudget
            )
        }.flowOn(Dispatchers.Default)
    }
}
