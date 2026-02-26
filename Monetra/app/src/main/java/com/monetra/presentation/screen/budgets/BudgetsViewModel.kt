package com.monetra.presentation.screen.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.CategoryBudget
import com.monetra.domain.usecase.intelligence.GetCategoryBudgetsUseCase
import com.monetra.domain.usecase.intelligence.UpdateCategoryBudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

data class BudgetsUiState(
    val budgets: List<CategoryBudget> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class BudgetsViewModel @Inject constructor(
    private val getCategoryBudgets: GetCategoryBudgetsUseCase,
    private val updateCategoryBudget: UpdateCategoryBudgetUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(BudgetsUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadBudgets()
    }

    private fun loadBudgets() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val defaultCategories = listOf(
                "General", "Food", "Transport", "Shopping", 
                "Entertainment", "Utilities", "Gift", "Health"
            )
            
            getCategoryBudgets(YearMonth.now()).collectLatest { existingBudgets ->
                val existingMap = existingBudgets.associateBy { it.categoryName }
                
                val allBudgets = defaultCategories.map { category ->
                    existingMap[category] ?: CategoryBudget(
                        categoryName = category,
                        limit = 0.0,
                        currentSpent = 0.0
                    )
                }
                
                _uiState.update { it.copy(budgets = allBudgets, isLoading = false) }
            }
        }
    }

    fun onUpdateBudget(categoryName: String, limit: Double) {
        viewModelScope.launch {
            updateCategoryBudget(categoryName, limit)
        }
    }
}
