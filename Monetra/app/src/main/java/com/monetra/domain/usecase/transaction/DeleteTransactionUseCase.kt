package com.monetra.domain.usecase.transaction

import com.monetra.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteTransaction(id)
    }
}
