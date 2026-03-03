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
        val transaction = repository.getTransactionById(id)
        if (transaction?.linkedBillId != null) {
            val instance = monthlyExpenseRepository.getInstanceById(transaction.linkedBillId)
            if (instance != null) {
                val newPaidAmount = (instance.paidAmount - transaction.amount).coerceAtLeast(0.0)
                val newStatus = when {
                    newPaidAmount >= instance.amount -> BillStatus.PAID
                    newPaidAmount > 0 -> BillStatus.PARTIAL
                    else -> BillStatus.PENDING
                }
                
                monthlyExpenseRepository.insertBillInstance(
                    instance.copy(
                        paidAmount = newPaidAmount,
                        status = newStatus
                    )
                )
            }
        }
        repository.deleteTransaction(id)
    }
}
