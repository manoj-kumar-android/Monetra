package com.monetra.domain.repository

import com.monetra.domain.model.CategoryBudget
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth

interface BudgetRepository {
    fun getCategoryBudgets(month: YearMonth): Flow<List<CategoryBudget>>
    suspend fun saveCategoryBudget(budget: CategoryBudget)
    suspend fun deleteCategoryBudget(categoryName: String)
}
