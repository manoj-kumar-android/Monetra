package com.monetra.domain.usecase.transaction

import com.monetra.domain.model.Transaction
import com.monetra.domain.repository.TransactionRepository
import javax.inject.Inject

class GetTransactionByIdUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: Long): Transaction? {
        return repository.getTransactionById(id)
    }
}
