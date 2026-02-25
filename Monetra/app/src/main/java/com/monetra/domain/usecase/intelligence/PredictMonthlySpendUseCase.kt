package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.Transaction
import com.monetra.domain.model.TransactionType
import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class PredictMonthlySpendUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<PredictionResult> {
        val now = LocalDate.now()
        val currentMonth = YearMonth.from(now)
        val previousMonth = currentMonth.minusMonths(1)
        val daysInMonth = currentMonth.lengthOfMonth()
        val dayOfMonth = now.dayOfMonth

        return combine(
            repository.getTransactions(currentMonth),
            repository.getTotalExpense(previousMonth)
        ) { currentTransactions, previousTotalSpent ->
            val totalSpent = currentTransactions
                .filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
            
            val dailyAverage = if (dayOfMonth > 0) totalSpent / dayOfMonth else 0.0
            val projectedTotal = dailyAverage * daysInMonth
            
            PredictionResult(
                currentSpent = totalSpent,
                projectedTotal = projectedTotal,
                dailyAverage = dailyAverage,
                previousMonthTotal = previousTotalSpent,
                increaseVsLastMonth = if (previousTotalSpent > 0) 
                    ((projectedTotal - previousTotalSpent) / previousTotalSpent) * 100 
                else 0.0
            )
        }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    }

    data class PredictionResult(
        val currentSpent: Double,
        val projectedTotal: Double,
        val dailyAverage: Double,
        val previousMonthTotal: Double,
        val increaseVsLastMonth: Double
    )
}
