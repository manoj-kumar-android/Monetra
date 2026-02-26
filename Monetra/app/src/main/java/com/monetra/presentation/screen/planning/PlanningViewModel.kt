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
    private val getWealthIntelligence: com.monetra.domain.usecase.intelligence.GetWealthIntelligenceUseCase,
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
            combine(
                getPlanningOverview(),
                getWealthIntelligence()
            ) { overview, wealth ->
                PlanningUiState.Success(overview, wealth.wealthProjection)
            }
            .catch { throwable ->
                _uiState.value = PlanningUiState.Error(throwable.localizedMessage ?: "Unknown error")
            }
            .collect { state ->
                _uiState.value = state
            }
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
                    startDate = java.time.LocalDate.now(),
                    amount = amount,
                    monthlyAmount = 0.0,
                    interestRate = 0.0,
                    currentValue = amount,
                    frequency = ContributionFrequency.ONE_TIME
                )
            )
        }
    }
}

sealed interface PlanningUiState {
    data object Loading : PlanningUiState
    data class Success(
        val overview: PlanningOverview,
        val wealthProjection: com.monetra.domain.model.WealthProjection
    ) : PlanningUiState
    data class Error(val message: String) : PlanningUiState
}
