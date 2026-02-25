package com.monetra.domain.usecase.transaction

import com.monetra.domain.model.MonthlySummary
import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.YearMonth
import javax.inject.Inject

class GetMonthlySummaryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(month: YearMonth): Flow<MonthlySummary> {
        return combine(
            repository.getTotalIncome(month),
            repository.getTotalExpense(month)
        ) { income, expense ->
            MonthlySummary(
                totalIncome = income,
                totalExpense = expense
            )
        }
    }
}
