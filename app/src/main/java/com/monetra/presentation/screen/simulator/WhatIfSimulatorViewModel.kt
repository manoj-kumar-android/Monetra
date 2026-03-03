package com.monetra.presentation.screen.simulator

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.SimulationParams
import com.monetra.domain.model.SimulationResult
import com.monetra.domain.usecase.intelligence.SimulateFinancialScenarioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

@HiltViewModel
class WhatIfSimulatorViewModel @Inject constructor(
    private val simulateUseCase: SimulateFinancialScenarioUseCase
) : ViewModel() {

    private val _params = MutableStateFlow(SimulationParams())
    val params: StateFlow<SimulationParams> = _params.asStateFlow()

    private val _uiState = MutableStateFlow<SimulatorUiState>(SimulatorUiState.Loading)
    val uiState: StateFlow<SimulatorUiState> = _uiState.asStateFlow()

    init {
        observeSimulation()
    }

    private fun observeSimulation() {
        _params.flatMapLatest { currentParams ->
            simulateUseCase(YearMonth.now(), currentParams)
        }
        .onEach { result ->
            if (result == null) {
                _uiState.value = SimulatorUiState.PremiumLocked
            } else {
                _uiState.value = SimulatorUiState.Success(result)
            }
        }
        .catch { e ->
            _uiState.value = SimulatorUiState.Error(e.message ?: "Unknown Error")
        }
        .launchIn(viewModelScope)
    }

    fun updateSalaryDelta(delta: Double) {
        _params.value = _params.value.copy(salaryDelta = delta)
    }

    fun updateNewEmi(amount: Double) {
        _params.value = _params.value.copy(newEmiAmount = amount)
    }

    fun updateNewSip(amount: Double) {
        _params.value = _params.value.copy(newSipAmount = amount)
    }

    fun updateSavingsTargetDelta(delta: Double) {
        _params.value = _params.value.copy(savingsTargetDelta = delta)
    }
    
    fun reset() {
        _params.value = SimulationParams()
    }
}

sealed interface SimulatorUiState {
    data object Loading : SimulatorUiState
    data class Success(val result: SimulationResult) : SimulatorUiState
    data object PremiumLocked : SimulatorUiState
    data class Error(val message: String) : SimulatorUiState
}
