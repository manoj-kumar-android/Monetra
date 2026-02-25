package com.monetra.presentation.screen.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.MonthlySummary
import com.monetra.domain.usecase.transaction.GetMonthlySummaryUseCase
import com.monetra.domain.usecase.transaction.GetWeeklySummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import androidx.compose.runtime.Immutable
import com.monetra.presentation.screen.transactions.SummaryUiModel
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

@Immutable
data class SummaryUiState(
    val monthlySummary: SummaryUiModel? = null,
    val weeklySummary: SummaryUiModel? = null,
    val yearlySummary: SummaryUiModel? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class SummaryViewModel @Inject constructor(
    private val getMonthlySummary: GetMonthlySummaryUseCase,
    private val getWeeklySummary: com.monetra.domain.usecase.transaction.GetWeeklySummaryUseCase,
    private val getYearlySummary: com.monetra.domain.usecase.transaction.GetYearlySummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    init {
        loadSummaries()
    }

    private fun loadSummaries() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val currentMonth = YearMonth.now()
                val currentDate = LocalDate.now()

                combine(
                    getMonthlySummary(currentMonth),
                    getWeeklySummary(currentDate),
                    getYearlySummary(java.time.Year.of(currentMonth.year))
                ) { monthly, weekly, yearly ->
                    SummaryUiState(
                        monthlySummary = monthly.toSummaryUiModel(),
                        weeklySummary = weekly.toSummaryUiModel(),
                        yearlySummary = yearly.toSummaryUiModel(),
                        isLoading = false,
                        errorMessage = null
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load summaries"
                )
            }
        }
    }

    private fun MonthlySummary.toSummaryUiModel() = SummaryUiModel(
        formattedBalance = "₹%,.2f".format(balance),
        formattedIncome = "₹%,.2f".format(totalIncome),
        formattedExpense = "₹%,.2f".format(totalExpense),
    )
}
