package com.monetra.presentation.screen.snapshot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.MonthlyFinancialReport
import com.monetra.domain.model.SavingSuggestion
import com.monetra.domain.usecase.intelligence.GenerateMonthlyFinancialReportUseCase
import com.monetra.domain.usecase.intelligence.GenerateSavingSuggestionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class SnapshotViewModel @Inject constructor(
    private val generateReport: GenerateMonthlyFinancialReportUseCase,
    private val generateSuggestions: GenerateSavingSuggestionsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<SnapshotUiState>(SnapshotUiState.Loading)
    val uiState: StateFlow<SnapshotUiState> = _uiState.asStateFlow()

    init {
        observeSnapshotData()
    }

    private fun observeSnapshotData() {
        val currentMonth = YearMonth.now()
        
        viewModelScope.launch {
            combine(
                generateReport(currentMonth),
                generateSuggestions(currentMonth)
            ) { report, suggestions ->
                SnapshotUiState.Success(
                    report = report,
                    suggestions = suggestions
                )
            }.catch { throwable ->
                _uiState.value = SnapshotUiState.Error(throwable.localizedMessage ?: "Unknown error")
            }.collect {
                _uiState.value = it
            }
        }
    }
}

sealed interface SnapshotUiState {
    data object Loading : SnapshotUiState
    data class Success(
        val report: MonthlyFinancialReport,
        val suggestions: List<SavingSuggestion>
    ) : SnapshotUiState
    data class Error(val message: String) : SnapshotUiState
}
