package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.MonthlyExpense
import com.monetra.domain.repository.MonthlyExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMonthlyExpensesUseCase @Inject constructor(
    private val repository: MonthlyExpenseRepository
) {
    operator fun invoke(): Flow<List<MonthlyExpense>> = repository.getAllMonthlyExpenses()
}

class AddMonthlyExpenseUseCase @Inject constructor(
    private val repository: MonthlyExpenseRepository
) {
    suspend operator fun invoke(expense: MonthlyExpense) = repository.insertMonthlyExpense(expense)
}

class DeleteMonthlyExpenseUseCase @Inject constructor(
    private val repository: MonthlyExpenseRepository
) {
    suspend operator fun invoke(expense: MonthlyExpense) = repository.deleteMonthlyExpense(expense)
}
