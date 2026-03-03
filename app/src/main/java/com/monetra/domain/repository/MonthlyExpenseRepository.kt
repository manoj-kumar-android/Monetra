package com.monetra.domain.repository

import com.monetra.domain.model.BillInstance
import com.monetra.domain.model.MonthlyExpense
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface MonthlyExpenseRepository {
    // Rules
    fun getAllMonthlyExpenses(): Flow<List<MonthlyExpense>>
    suspend fun insertMonthlyExpense(expense: MonthlyExpense): Long
    suspend fun deleteMonthlyExpense(expense: MonthlyExpense)
    suspend fun getMonthlyExpensesByCategory(category: String): List<MonthlyExpense>
    suspend fun getMonthlyExpenseById(id: Long): MonthlyExpense?
    fun getTotalMonthlyExpenseAmount(): Flow<Double>

    // Instances
    fun getInstancesForMonth(month: YearMonth): Flow<List<BillInstance>>
    suspend fun getInstanceByBillAndMonth(billId: Long, month: YearMonth): BillInstance?
    suspend fun insertBillInstance(instance: BillInstance)
    suspend fun getInstanceById(id: Long?): BillInstance?
    fun getTotalReservedAmountForMonth(month: YearMonth): Flow<Double>
    suspend fun hasInstanceForMonth(billId: Long, month: YearMonth): Boolean
}
