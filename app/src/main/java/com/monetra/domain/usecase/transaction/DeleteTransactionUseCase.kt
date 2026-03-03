package com.monetra.domain.usecase.transaction

import com.monetra.domain.model.BillStatus
import com.monetra.domain.repository.MonthlyExpenseRepository
import com.monetra.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val monthlyExpenseRepository: MonthlyExpenseRepository
) {
    suspend operator fun invoke(id: Long) {
        repository.deleteTransaction(id)
    }
}
