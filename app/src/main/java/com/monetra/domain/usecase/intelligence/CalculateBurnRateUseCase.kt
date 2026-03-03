package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.BurnRateAnalysis
import com.monetra.domain.repository.SubscriptionRepository
import com.monetra.domain.repository.TransactionRepository
import com.monetra.domain.repository.UserPreferenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Burn Rate Prediction Engine
 * 
 * Predicts end-of-month spending based on current velocity and compares it
 * with the user's disposable income limit.
 */
class CalculateBurnRateUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(): Flow<BurnRateAnalysis?> {
        val today = LocalDate.now()
        val currentMonth = YearMonth.from(today)
        val daysInMonth = currentMonth.lengthOfMonth()
        val elapsedDays = today.dayOfMonth

        return combine(
            transactionRepository.getTotalExpense(currentMonth),
            userPreferenceRepository.getUserPreferences()
        ) { totalSpent, prefs ->
            val dailyRate = if (elapsedDays > 0) totalSpent / elapsedDays else 0.0
            val projectedSpent = dailyRate * daysInMonth
            val disposableLimit = prefs.monthlyIncome - prefs.monthlySavingsGoal

            val isOverspending = projectedSpent > disposableLimit

            val warningMessage = if (isOverspending) {
                "Warning: At your current pace, you will overspend by ₹%,.0f of your disposable income limit.".format(projectedSpent - disposableLimit)
            } else null

            BurnRateAnalysis(
                currentDay = elapsedDays,
                totalDays = daysInMonth,
                currentSpend = totalSpent,
                projectedEndMonthSpend = projectedSpent,
                isOverspending = isOverspending,
                warningMessage = warningMessage
            )
        }
    }
}
