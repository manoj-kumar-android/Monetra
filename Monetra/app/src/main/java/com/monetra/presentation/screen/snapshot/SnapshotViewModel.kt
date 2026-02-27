package com.monetra.presentation.screen.snapshot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.*
import com.monetra.domain.usecase.intelligence.GenerateSavingSuggestionsUseCase
import com.monetra.domain.usecase.intelligence.GetFinancialPlanningOverviewUseCase
import com.monetra.domain.usecase.intelligence.MonthlyReportGeneratorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class SnapshotViewModel @Inject constructor(
    private val reportGenerator: MonthlyReportGeneratorUseCase,
    private val getPlanningOverview: GetFinancialPlanningOverviewUseCase,
    private val getWealthIntelligence: com.monetra.domain.usecase.intelligence.GetWealthIntelligenceUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SnapshotUiState>(SnapshotUiState.Loading)
    val uiState: StateFlow<SnapshotUiState> = _uiState.asStateFlow()

    init {
        loadPortfolioData()
    }

    private fun loadPortfolioData() {
        val currentMonth = YearMonth.now()
        
        viewModelScope.launch {
            _uiState.value = SnapshotUiState.Loading
            try {
                combine(
                    flow { emit(reportGenerator.generate(currentMonth)) },
                    getPlanningOverview(),
                    getWealthIntelligence()
                ) { report, overview, wealth ->
                    SnapshotUiState.Success(
                        report = report,
                        overview = overview,
                        wealthProjection = wealth.wealthProjection
                    )
                }.catch { throwable ->
                    _uiState.value = SnapshotUiState.Error(throwable.localizedMessage ?: "Unknown error")
                }.collect {
                    _uiState.value = it
                }
            } catch (e: Exception) {
                _uiState.value = SnapshotUiState.Error(e.localizedMessage ?: "Failed to load consolidated data")
            }
        }
    }
}

sealed interface SnapshotUiState {
    data object Loading : SnapshotUiState
    data class Success(
        val report: ComprehensiveMonthlyReport,
        val overview: PlanningOverview,
        val wealthProjection: com.monetra.domain.model.WealthProjection
    ) : SnapshotUiState
    data class Error(val message: String) : SnapshotUiState
}
