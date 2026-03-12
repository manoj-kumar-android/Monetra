package com.monetra.domain.usecase.transaction

import com.monetra.domain.model.Transaction
import com.monetra.domain.model.TransactionFilters
import com.monetra.domain.repository.TransactionRepository
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPagedTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(filters: TransactionFilters): Flow<PagingData<Transaction>> {
        return repository.getTransactionsPaged(filters)
    }
}
