package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.CategoryBudget
import com.monetra.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import java.time.YearMonth
import javax.inject.Inject

class GetCategoryBudgetsUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    operator fun invoke(month: YearMonth): Flow<List<CategoryBudget>> {
        return repository.getCategoryBudgets(month)
    }
}

class UpdateCategoryBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(categoryName: String, limit: Double) {
        repository.saveCategoryBudget(CategoryBudget(categoryName = categoryName, limit = limit))
    }
}
