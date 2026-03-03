package com.monetra.domain.usecase.transaction

import com.monetra.domain.model.BillStatus
import com.monetra.domain.model.Transaction
import com.monetra.domain.model.TransactionType
import com.monetra.domain.repository.MonthlyExpenseRepository
import com.monetra.domain.repository.TransactionRepository
import java.time.YearMonth
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
    private val monthlyExpenseRepository: MonthlyExpenseRepository
) {
    suspend operator fun invoke(transaction: Transaction) {
        var finalTransaction = transaction
        
        if (transaction.type == TransactionType.EXPENSE) {
            val month = YearMonth.from(transaction.date)
            val rules = monthlyExpenseRepository.getMonthlyExpensesByCategory(transaction.category)
            
            // For each rule in this category, try to find an unpaid instance for THIS transaction's month
            for (rule in rules) {
                val instance = monthlyExpenseRepository.getInstanceByBillAndMonth(rule.id, month)
                if (instance != null && instance.status != BillStatus.PAID) {
                    finalTransaction = transaction.copy(linkedBillId = instance.id)
                    break // Link to the first matching unpaid bill in category
                }
            }
        }
        
        repository.insertTransaction(finalTransaction)
    }
}
