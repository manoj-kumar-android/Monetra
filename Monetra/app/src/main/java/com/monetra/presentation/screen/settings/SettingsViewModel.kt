package com.monetra.presentation.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.repository.UserPreferenceRepository
import com.monetra.domain.usecase.intelligence.UpdateUserPreferencesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.monetra.domain.backup.BackupManager
import com.monetra.domain.backup.RestoreManager

data class SettingsUiState(
    val ownerName: String = "",
    val monthlyIncome: String = "",
    val monthlySavingsGoal: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val nameError: String? = null,
    val incomeError: String? = null,
    val savingsError: String? = null,
    val isRestoring: Boolean = false,
    val isSyncing: Boolean = false
)

sealed interface SettingsEvent {
    data object SaveSuccess : SettingsEvent
    data object RestoreSuccess : SettingsEvent
    data class RestoreError(val message: String) : SettingsEvent
    data object SyncSuccess : SettingsEvent
    data class SyncError(val message: String) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferenceRepository,
    private val updatePreferences: UpdateUserPreferencesUseCase,
    private val backupManager: BackupManager,
    private val restoreManager: RestoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadPreferences()
    }

    fun onExportEncryptedBackup(uri: android.net.Uri, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSyncing = true) }
            val result = backupManager.exportEncryptedBackup(uri, password)
            _uiState.update { it.copy(isSyncing = false) }
            if (result.isSuccess) {
                _events.send(SettingsEvent.SyncSuccess)
            } else {
                _events.send(SettingsEvent.SyncError(result.exceptionOrNull()?.message ?: "Export failed"))
            }
        }
    }

    fun onRestoreFromEncryptedUri(uri: android.net.Uri, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true) }
            val result = restoreManager.restoreFromEncryptedUri(uri, password)
            _uiState.update { it.copy(isRestoring = false) }
            if (result.isSuccess) {
                _events.send(SettingsEvent.RestoreSuccess)
                loadPreferences()
            } else {
                _events.send(SettingsEvent.RestoreError(result.exceptionOrNull()?.message ?: "Restore failed"))
            }
        }
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val prefs = repository.getUserPreferences().first()
            _uiState.update { 
                it.copy(
                    ownerName = prefs.ownerName,
                    monthlyIncome = if (prefs.monthlyIncome > 0) prefs.monthlyIncome.toString() else "",
                    monthlySavingsGoal = if (prefs.monthlySavingsGoal > 0) prefs.monthlySavingsGoal.toString() else "",
                    isLoading = false
                )
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(ownerName = name, nameError = null, isSuccess = false) }
    }

    fun onIncomeChange(income: String) {
        val sanitized = income.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return
        
        _uiState.update { it.copy(monthlyIncome = sanitized, incomeError = null, isSuccess = false) }
    }

    fun onSavingsGoalChange(goal: String) {
        val sanitized = goal.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return

        _uiState.update { it.copy(monthlySavingsGoal = sanitized, savingsError = null, isSuccess = false) }
    }

    fun onSaveClick() {
        val currentState = _uiState.value
        
        var hasError = false
        if (currentState.ownerName.isBlank()) {
            _uiState.update { it.copy(nameError = "Name cannot be empty") }
            hasError = true
        }
        
        val income = currentState.monthlyIncome.toDoubleOrNull() ?: 0.0
        if (income <= 0) {
            _uiState.update { it.copy(incomeError = "Please enter a valid income") }
            hasError = true
        }
        
        val goal = currentState.monthlySavingsGoal.toDoubleOrNull() ?: 0.0
        if (goal >= income) {
            _uiState.update { it.copy(savingsError = "Savings goal must be less than monthly income") }
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            updatePreferences(currentState.ownerName, income, goal)
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            _events.send(SettingsEvent.SaveSuccess)
        }
    }
}
