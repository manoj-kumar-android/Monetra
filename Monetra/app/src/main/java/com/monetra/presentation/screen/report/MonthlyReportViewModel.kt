package com.monetra.presentation.screen.report

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.usecase.intelligence.MonthlyReportGeneratorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class MonthlyReportViewModel @Inject constructor(
    private val reportGenerator: MonthlyReportGeneratorUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ReportUiState>(ReportUiState.Loading)
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    init {
        generateReport()
    }

    fun generateReport() {
        viewModelScope.launch {
            _uiState.value = ReportUiState.Loading
            try {
                val report = reportGenerator.generate(YearMonth.now())
                _uiState.value = ReportUiState.Success(report)
            } catch (e: Exception) {
                _uiState.value = ReportUiState.Error(e.localizedMessage ?: "Failed to generate report")
            }
        }
    }
}

sealed interface ReportUiState {
    data object Loading : ReportUiState
    data class Success(
        val report: com.monetra.domain.model.ComprehensiveMonthlyReport
    ) : ReportUiState
    data class Error(val message: String) : ReportUiState
}
