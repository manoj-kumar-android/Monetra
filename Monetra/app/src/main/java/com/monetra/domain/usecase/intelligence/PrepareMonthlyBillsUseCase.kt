package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.BillInstance
import com.monetra.domain.model.BillStatus
import com.monetra.domain.repository.MonthlyExpenseRepository
import kotlinx.coroutines.flow.first
import java.time.YearMonth
import javax.inject.Inject

/**
 * Ensures that all recurring bill rules have an active instance for the specified [month].
 */
class PrepareMonthlyBillsUseCase @Inject constructor(
    private val repository: MonthlyExpenseRepository
) {
    suspend operator fun invoke(month: YearMonth = YearMonth.now()) {
        val rules = repository.getAllMonthlyExpenses().first()
        
        rules.forEach { rule ->
            val hasInstance = repository.hasInstanceForMonth(rule.id, month)
            if (!hasInstance) {
                repository.insertBillInstance(
                    BillInstance(
                        billId = rule.id,
                        month = month,
                        amount = rule.amount,
                        paidAmount = 0.0,
                        status = BillStatus.PENDING
                    )
                )
            }
        }
    }
}
