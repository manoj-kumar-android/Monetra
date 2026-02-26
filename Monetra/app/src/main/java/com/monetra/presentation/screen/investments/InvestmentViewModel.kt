package com.monetra.presentation.screen.investments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.Investment
import com.monetra.domain.model.InvestmentType
import com.monetra.domain.usecase.intelligence.GetWealthIntelligenceUseCase
import com.monetra.domain.usecase.investment.AddInvestmentUseCase
import com.monetra.domain.usecase.investment.DeleteInvestmentUseCase
import com.monetra.domain.usecase.investment.GetInvestmentsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val getInvestments: GetInvestmentsUseCase,
    private val getWealthIntelligence: GetWealthIntelligenceUseCase,
    private val addInvestment: AddInvestmentUseCase,
    private val deleteInvestment: DeleteInvestmentUseCase,
    private val userPreferenceRepository: com.monetra.domain.repository.UserPreferenceRepository
) : ViewModel() {

    fun onSimulationRateChange(rate: Double) {
        viewModelScope.launch {
            val current = userPreferenceRepository.getUserPreferences().first()
            userPreferenceRepository.saveUserPreferences(current.copy(projectionRate = rate))
        }
    }

    fun onSimulationYearsChange(years: Int) {
        viewModelScope.launch {
            val current = userPreferenceRepository.getUserPreferences().first()
            userPreferenceRepository.saveUserPreferences(current.copy(projectionYears = years))
        }
    }

    private val _investments = getInvestments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val investments: StateFlow<List<Investment>> = _investments

    val wealthIntelligence = getWealthIntelligence()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(InvestmentUiState())
    val uiState: StateFlow<InvestmentUiState> = _uiState.asStateFlow()

    fun onNameChange(name: String) { 
        if (name.length <= 50) {
            _uiState.update { it.copy(name = name, nameError = null) } 
        }
    }
    fun onAmountChange(v: String) { 
        val sanitized = v.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return
        _uiState.update { it.copy(amount = sanitized, amountError = null) } 
    }
    fun onMonthlyAmountChange(v: String) { 
        val sanitized = v.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return
        _uiState.update { it.copy(monthlyAmount = sanitized, monthlyAmountError = null) } 
    }
    fun onInterestRateChange(v: String) {
        val sanitized = v.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return
        val rate = sanitized.toDoubleOrNull() ?: 0.0
        if (rate > 30.0) return // Limit as requested
        _uiState.update { it.copy(interestRate = sanitized, interestRateError = null) }
    }
    fun onCurrentValueChange(v: String) {
        val sanitized = v.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return
        _uiState.update { it.copy(currentValue = sanitized, currentValueError = null) }
    }
    fun onStartDateChange(date: java.time.LocalDate) {
        if (date.isAfter(java.time.LocalDate.now())) return
        _uiState.update { it.copy(startDate = date) }
    }
    fun onTypeChange(type: InvestmentType) {
        _uiState.update { it.copy(type = type, frequency = type.defaultFrequency) }
    }
    fun onDeleteInvestment(investment: Investment) {
        viewModelScope.launch { deleteInvestment(investment) }
    }
    fun toggleAddSheet(isOpen: Boolean) {
        _uiState.update {
            if (isOpen) it.copy(isAddSheetOpen = true)
            else InvestmentUiState()
        }
    }

    fun onSaveInvestment() {
        val state = _uiState.value
        var hasError = false

        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Name is required") }
            hasError = true
        }

        val amount = state.amount.toDoubleOrNull() ?: 0.0
        val monthlyAmount = state.monthlyAmount.toDoubleOrNull() ?: 0.0
        val interestRate = state.interestRate.toDoubleOrNull() ?: 0.0
        val currentValueInput = state.currentValue.toDoubleOrNull() ?: 0.0

        if (state.frequency == com.monetra.domain.model.ContributionFrequency.MONTHLY) {
            if (monthlyAmount <= 0) {
                _uiState.update { it.copy(monthlyAmountError = "Required") }
                hasError = true
            }
        } else {
            if (amount <= 0) {
                _uiState.update { it.copy(amountError = "Required") }
                hasError = true
            }
        }

        if (hasError) return

        viewModelScope.launch {
            addInvestment(
                Investment(
                    name = state.name,
                    type = state.type,
                    startDate = state.startDate,
                    amount = if (state.frequency == com.monetra.domain.model.ContributionFrequency.ONE_TIME) amount else 0.0,
                    monthlyAmount = if (state.frequency == com.monetra.domain.model.ContributionFrequency.MONTHLY) monthlyAmount else 0.0,
                    interestRate = interestRate,
                    currentValue = if (currentValueInput > 0) currentValueInput else if (state.frequency == com.monetra.domain.model.ContributionFrequency.ONE_TIME) amount else 0.0,
                    frequency = state.frequency
                )
            )
            _uiState.update { InvestmentUiState() }
        }
    }
}

data class InvestmentUiState(
    val name: String = "",
    val type: InvestmentType = InvestmentType.SIP,
    val startDate: java.time.LocalDate = java.time.LocalDate.now(),
    val amount: String = "",
    val monthlyAmount: String = "",
    val interestRate: String = "0",
    val currentValue: String = "",
    val frequency: com.monetra.domain.model.ContributionFrequency = com.monetra.domain.model.ContributionFrequency.MONTHLY,
    val isAddSheetOpen: Boolean = false,
    val nameError: String? = null,
    val amountError: String? = null,
    val monthlyAmountError: String? = null,
    val interestRateError: String? = null,
    val currentValueError: String? = null
)
