package com.monetra.domain.usecase.transaction

import com.monetra.domain.model.MonthlySummary
import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.LocalDate
import java.time.Month
import java.time.Year
import javax.inject.Inject

class GetYearlySummaryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(year: Year): Flow<MonthlySummary> {
        val startDate = LocalDate.of(year.value, Month.JANUARY, 1)
        val endDate = LocalDate.of(year.value, Month.DECEMBER, 31)
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
