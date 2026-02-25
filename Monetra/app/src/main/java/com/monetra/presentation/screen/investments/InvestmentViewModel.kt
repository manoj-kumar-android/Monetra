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
import javax.inject.Inject

@HiltViewModel
class InvestmentViewModel @Inject constructor(
    private val getInvestments: GetInvestmentsUseCase,
    private val getWealthIntelligence: GetWealthIntelligenceUseCase,
    private val addInvestment: AddInvestmentUseCase,
    private val deleteInvestment: DeleteInvestmentUseCase
) : ViewModel() {

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
    fun onCurrentValueChange(v: String) { 
        val sanitized = v.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return
        _uiState.update { it.copy(currentValue = sanitized, currentValueError = null) } 
    }
    fun onInvestedChange(v: String) { 
        val sanitized = v.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return
        _uiState.update { it.copy(investedLumpSum = sanitized, investedLumpSumError = null) } 
    }
    fun onMonthlyAmountChange(v: String) { 
        val sanitized = v.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return
        _uiState.update { it.copy(monthlyAmount = sanitized, monthlyAmountError = null) } 
    }
    fun onTypeChange(type: InvestmentType) {
        _uiState.update { it.copy(type = type, isMonthly = type.defaultMonthly) }
    }
    fun onToggleMonthly(isMonthly: Boolean) { _uiState.update { it.copy(isMonthly = isMonthly) } }
    fun onDeleteInvestment(investment: Investment) {
        viewModelScope.launch { deleteInvestment(investment) }
    }
    fun toggleAddSheet(isOpen: Boolean) {
        _uiState.update {
            if (isOpen) it.copy(isAddSheetOpen = true)
            else InvestmentUiState()  // full reset on close
        }
    }

    fun onSaveInvestment() {
        val state = _uiState.value
        
        var hasError = false
        if (state.name.isBlank()) {
            _uiState.update { it.copy(nameError = "Investment name is required") }
            hasError = true
        }

        val monthlyAmount = state.monthlyAmount.toDoubleOrNull() ?: 0.0
        val currentValuation = state.currentValue.toDoubleOrNull() ?: 0.0
        val investedTotal = state.investedLumpSum.toDoubleOrNull() ?: 0.0

        if (state.isMonthly) {
            if (monthlyAmount <= 0) {
                _uiState.update { it.copy(monthlyAmountError = "Enter a valid monthly amount") }
                hasError = true
            }
        } else {
            if (investedTotal <= 0) {
                _uiState.update { it.copy(investedLumpSumError = "Enter invested amount") }
                hasError = true
            }
        }

        if (hasError) return

        // For SIPs: if they don't specify total invested, assume it's the first installment
        val finalInvested = if (state.isMonthly && investedTotal == 0.0) monthlyAmount else investedTotal
        // If current value is not provided, use invested amount
        val finalValuation = if (currentValuation == 0.0) finalInvested else currentValuation

        viewModelScope.launch {
            addInvestment(
                Investment(
                    name = state.name,
                    type = state.type,
                    currentValuation = finalValuation,
                    investedAmount = finalInvested,
                    monthlyAmount = if (state.isMonthly) monthlyAmount else 0.0,
                    isMonthly = state.isMonthly
                )
            )
            _uiState.update { InvestmentUiState() }
        }
    }
}

data class InvestmentUiState(
    val name: String = "",
    val currentValue: String = "",
    val investedLumpSum: String = "",
    val monthlyAmount: String = "",
    val type: InvestmentType = InvestmentType.SIP,
    val isMonthly: Boolean = true,
    val isAddSheetOpen: Boolean = false,
    val nameError: String? = null,
    val currentValueError: String? = null,
    val investedLumpSumError: String? = null,
    val monthlyAmountError: String? = null
)
