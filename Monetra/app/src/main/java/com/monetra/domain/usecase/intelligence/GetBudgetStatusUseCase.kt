package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.CategoryBudget
import com.monetra.domain.repository.BudgetRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.time.YearMonth
import javax.inject.Inject

class GetBudgetStatusUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    operator fun invoke(month: YearMonth): Flow<List<BudgetStatus>> {
        return repository.getCategoryBudgets(month).map { budgets ->
            budgets.map { budget ->
                val progress = if (budget.limit > 0) budget.currentSpent / budget.limit else 0.0
                BudgetStatus(
                    categoryName = budget.categoryName,
                    currentSpent = budget.currentSpent,
                    limit = budget.limit,
                    progress = progress,
                    status = when {
                        progress >= 1.0 -> BudgetLevel.ALERT
                        progress >= 0.8 -> BudgetLevel.WARNING
                        else -> BudgetLevel.SAFE
                    }
                )
            }
        }.flowOn(Dispatchers.Default)
    }

    data class BudgetStatus(
        val categoryName: String,
        val currentSpent: Double,
        val limit: Double,
        val progress: Double,
        val status: BudgetLevel
    )

    enum class BudgetLevel {
        SAFE, WARNING, ALERT
    }
}
