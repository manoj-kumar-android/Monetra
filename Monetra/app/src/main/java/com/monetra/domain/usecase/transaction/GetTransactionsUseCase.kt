package com.monetra.domain.usecase.transaction

import com.monetra.domain.model.Transaction
import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(month: YearMonth): Flow<List<Transaction>> {
        return repository.getTransactions(month)
    }
}
