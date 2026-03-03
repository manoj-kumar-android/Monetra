package com.monetra.domain.usecase.transaction

import com.monetra.domain.model.MonthlySummary
import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetLifetimeSummaryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<MonthlySummary> {
        return combine(
            repository.getLifetimeIncome(),
            repository.getLifetimeExpense()
        ) { income, expense ->
            MonthlySummary(
                totalIncome = income,
                totalExpense = expense
            )
        }
    }
}
