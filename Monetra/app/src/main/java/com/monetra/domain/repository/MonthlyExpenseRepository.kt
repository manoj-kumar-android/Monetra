package com.monetra.domain.repository

import com.monetra.domain.model.MonthlyExpense
import kotlinx.coroutines.flow.Flow

interface MonthlyExpenseRepository {
    fun getAllMonthlyExpenses(): Flow<List<MonthlyExpense>>
    suspend fun insertMonthlyExpense(expense: MonthlyExpense)
    suspend fun deleteMonthlyExpense(expense: MonthlyExpense)
    fun getTotalMonthlyExpenseAmount(): Flow<Double>
}
