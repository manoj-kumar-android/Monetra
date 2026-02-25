package com.monetra.presentation.screen.planning

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.*
import com.monetra.domain.repository.GoalRepository
import com.monetra.domain.repository.InvestmentRepository
import com.monetra.domain.usecase.intelligence.GetFinancialPlanningOverviewUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlanningViewModel @Inject constructor(
    private val getPlanningOverview: GetFinancialPlanningOverviewUseCase,
    private val goalRepository: GoalRepository,
    private val investmentRepository: InvestmentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlanningUiState>(PlanningUiState.Loading)
    val uiState: StateFlow<PlanningUiState> = _uiState.asStateFlow()

    init {
        observePlanningData()
    }

    private fun observePlanningData() {
        viewModelScope.launch {
            getPlanningOverview()
                .onEach { overview -> 
                    _uiState.value = PlanningUiState.Success(overview) 
                }
                .catch { throwable ->
                    _uiState.value = PlanningUiState.Error(throwable.localizedMessage ?: "Unknown error")
                }
                .collect()
        }
    }

    fun addGoal(title: String, target: Double, category: GoalCategory) {
        viewModelScope.launch {
            goalRepository.upsertGoal(
                FinancialGoal(
                    title = title,
                    targetAmount = target,
                    currentAmount = 0.0,
                    deadline = null,
                    category = category
                )
            )
        }
    }

    fun addInvestment(name: String, type: InvestmentType, amount: Double) {
        viewModelScope.launch {
            investmentRepository.upsertInvestment(
                Investment(
                    name = name,
                    type = type,
                    investedAmount = amount,
                    currentValuation = amount
                )
            )
        }
    }
}

sealed interface PlanningUiState {
    data object Loading : PlanningUiState
    data class Success(val overview: PlanningOverview) : PlanningUiState
    data class Error(val message: String) : PlanningUiState
}
