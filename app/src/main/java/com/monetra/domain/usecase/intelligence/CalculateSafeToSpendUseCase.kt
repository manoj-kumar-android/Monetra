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
            monthlyExpenseRepository.getTotalReservedAmountForMonth(currentMonth),
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
            val monthlyBaseline =
                (prefs.monthlyIncome - prefs.monthlySavingsGoal - totalEmi - totalMonthlyExpense)

            val spentBeforeToday = monthlySpent - todaySpent

            val remainingMonthAllowance = monthlyBaseline - spentBeforeToday

            val remainingDays = (daysInMonth - today.dayOfMonth + 1).coerceAtLeast(1)

            val dailyLimit = remainingMonthAllowance / remainingDays
            val remainingToday = dailyLimit - todaySpent

            SafeToSpend(
                dailyLimit = dailyLimit,
                remainingToday = remainingToday,
                monthlyAllowance = remainingMonthAllowance
            )
        }.flowOn(Dispatchers.Default)
    }
}
