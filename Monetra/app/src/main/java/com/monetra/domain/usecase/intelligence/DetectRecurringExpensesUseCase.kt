package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.RecurringExpense
import com.monetra.domain.model.TransactionType
import com.monetra.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class DetectRecurringExpensesUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    operator fun invoke(): Flow<List<RecurringExpense>> {
        return repository.getAllTransactions().map { transactions ->
            val expenses = transactions.filter { it.type == TransactionType.EXPENSE }
            
            // Grouping Strategy: Title (normalized) + Amount
            val groups = expenses.groupBy { 
                it.title.lowercase().trim() to it.amount 
            }
            
            groups.mapNotNull { (key, group) ->
                if (group.size < 2) return@mapNotNull null
                
                val sorted = group.sortedBy { it.date }
                val intervals = mutableListOf<Long>()
                for (i in 0 until sorted.size - 1) {
                    intervals.add(ChronoUnit.DAYS.between(sorted[i].date, sorted[i + 1].date))
                }
                
                // Pattern Detection: Checking for monthly intervals (27-33 days)
                val monthlyIntervals = intervals.count { it in 27..33 }
                
                if (monthlyIntervals >= 1) {
                    val lastTx = sorted.last()
                    RecurringExpense(
                        title = lastTx.title,
                        amount = lastTx.amount,
                        category = lastTx.category,
                        nextExpectedDate = lastTx.date.plusMonths(1),
                        isStabilityHigh = monthlyIntervals >= 2
                    )
                } else {
                    null
                }
            }
        }.flowOn(kotlinx.coroutines.Dispatchers.Default)
    }
}
