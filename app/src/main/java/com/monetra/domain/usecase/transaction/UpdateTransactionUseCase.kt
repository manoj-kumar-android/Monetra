package com.monetra.domain.usecase.transaction

import com.monetra.domain.model.BillStatus
import com.monetra.domain.model.Transaction
import com.monetra.domain.model.TransactionType
import com.monetra.domain.repository.MonthlyExpenseRepository
import com.monetra.domain.repository.TransactionRepository
import java.time.YearMonth
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val monthlyExpenseRepository: MonthlyExpenseRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        var finalTransaction = transaction.copy(linkedBillId = null)

        // Find and apply a link if it's an expense
        if (transaction.type == TransactionType.EXPENSE) {
            val month = YearMonth.from(transaction.date)
            val rules = monthlyExpenseRepository.getMonthlyExpensesByCategory(transaction.category)
            
            for (rule in rules) {
                val instance = monthlyExpenseRepository.getInstanceByBillAndMonth(rule.id, month)
                if (instance != null && instance.status != BillStatus.PAID) {
                    finalTransaction = transaction.copy(linkedBillId = instance.id)
                    break
                }
            }
        }

        repository.updateTransaction(finalTransaction)
    }
}
