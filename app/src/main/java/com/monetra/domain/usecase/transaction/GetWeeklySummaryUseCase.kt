package com.monetra.domain.usecase.transaction

import com.monetra.domain.model.MonthlySummary
import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

class GetWeeklySummaryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(dateInWeek: LocalDate): Flow<MonthlySummary> {
        val startDate = dateInWeek.with(DayOfWeek.MONDAY)
        val endDate = dateInWeek.with(DayOfWeek.SUNDAY)
        return combine(
            repository.getTotalIncomeBetweenDates(startDate, endDate),
            repository.getTotalExpenseBetweenDates(startDate, endDate)
        ) { income, expense ->
            MonthlySummary(
                totalIncome = income,
                totalExpense = expense
            )
        }
    }
}
