package com.monetra.presentation.screen.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.MonthlySummary
import com.monetra.domain.usecase.transaction.GetYearlySummaryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.Immutable
import com.monetra.presentation.screen.transactions.SummaryUiModel
import java.time.Year
import javax.inject.Inject

@Immutable
data class YearlySummaryUiState(
    val yearlySummary: SummaryUiModel? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

@HiltViewModel
class YearlySummaryViewModel @Inject constructor(
    private val getYearlySummary: GetYearlySummaryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(YearlySummaryUiState())
    val uiState: StateFlow<YearlySummaryUiState> = _uiState.asStateFlow()

    init {
        loadSummary()
    }

    private fun loadSummary() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val currentYear = Year.now()

                getYearlySummary(currentYear).collect { summary ->
                    _uiState.value = YearlySummaryUiState(
                        yearlySummary = summary.toSummaryUiModel(),
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load yearly summary"
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
