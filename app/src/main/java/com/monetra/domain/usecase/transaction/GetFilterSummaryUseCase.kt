package com.monetra.domain.usecase.transaction

import com.monetra.domain.model.TransactionFilters
import com.monetra.domain.model.TransactionSummary
import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFilterSummaryUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(filters: TransactionFilters): Flow<TransactionSummary> {
        return repository.getFilterSummary(filters)
    }
}
