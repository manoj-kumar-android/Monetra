package com.monetra.data.repository

import com.monetra.data.local.dao.MonthlyExpenseDao
import com.monetra.data.local.entity.toDomain
import com.monetra.data.local.entity.toEntity
import com.monetra.domain.model.MonthlyExpense
import com.monetra.domain.repository.MonthlyExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MonthlyExpenseRepositoryImpl @Inject constructor(
    private val dao: MonthlyExpenseDao
) : MonthlyExpenseRepository {
    override fun getAllMonthlyExpenses(): Flow<List<MonthlyExpense>> {
        return dao.getAllMonthlyExpenses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMonthlyExpense(expense: MonthlyExpense) {
        dao.insertMonthlyExpense(expense.toEntity())
    }

    override suspend fun deleteMonthlyExpense(expense: MonthlyExpense) {
        dao.deleteMonthlyExpense(expense.toEntity())
    }

    override fun getTotalMonthlyExpenseAmount(): Flow<Double> {
        return dao.getTotalMonthlyExpenseAmount().map { it ?: 0.0 }
    }
}
