package com.monetra.domain.usecase.transaction

import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAmountRangeUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<Pair<Double, Double>> {
        return repository.getAmountRange()
    }
}
