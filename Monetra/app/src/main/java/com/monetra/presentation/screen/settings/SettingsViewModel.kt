package com.monetra.presentation.screen.settings

import android.app.Activity
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
import com.monetra.drivebackup.api.DriveBackupManager
import com.monetra.domain.repository.CloudBackupRepository
import kotlinx.coroutines.flow.collectLatest
import javax.inject.Inject
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException

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
    val isSyncing: Boolean = false,
    val isAuthenticating: Boolean = false,
    val isBackupAvailable: Boolean = false,
    val accountName: String? = null,
    val lastBackupTime: Long? = null,
    val recoveryIntent: android.content.Intent? = null
)

sealed interface SettingsEvent {
    data object SaveSuccess : SettingsEvent
    data object RestoreSuccess : SettingsEvent
    data class RestoreError(val message: String) : SettingsEvent
    data object SyncSuccess : SettingsEvent
    data class SyncError(val message: String) : SettingsEvent
    data object BackupSuccess : SettingsEvent
    data class BackupError(val message: String) : SettingsEvent
    data object AuthSuccess : SettingsEvent
    data class AuthError(val message: String) : SettingsEvent
    data class NeedsAuthorization(val intent: android.content.Intent) : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferenceRepository,
    private val updatePreferences: UpdateUserPreferencesUseCase,
    private val driveBackupManager: DriveBackupManager,
    private val cloudBackupRepository: CloudBackupRepository,
    private val syncUseCase: com.monetra.domain.usecase.SyncUseCase,
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadPreferences()
        observeBackupStatus()
        checkBackupAvailability()
        observeRestorationStatus()
    }

    private fun observeRestorationStatus() {
        viewModelScope.launch {
            cloudBackupRepository.isRestoring.collectLatest { restoring ->
                _uiState.update { it.copy(isRestoring = restoring) }
            }
        }
    }

    private fun observeBackupStatus() {
        viewModelScope.launch {
            driveBackupManager.lastBackupTime.collectLatest { time ->
                _uiState.update { it.copy(lastBackupTime = time) }
            }
        }
        viewModelScope.launch {
            driveBackupManager.accountName.collectLatest { name ->
                _uiState.update { it.copy(accountName = name) }
                if (name != null) {
                    checkBackupAvailability()
                }
            }
        }
    }

    private fun checkBackupAvailability() {
        viewModelScope.launch {
            val available = cloudBackupRepository.isBackupAvailable()
            _uiState.update { it.copy(isBackupAvailable = available) }
        }
    }

    fun onAuthenticateClick(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true) }
            val success = driveBackupManager.authenticate(activity)
            _uiState.update { it.copy(isAuthenticating = false) }
            if (success) {
                _events.send(SettingsEvent.AuthSuccess)
            } else {
                _events.send(SettingsEvent.AuthError("Authentication failed"))
            }
        }
    }

    fun onRestoreClick() {
        viewModelScope.launch {
            val result = cloudBackupRepository.runRestore()
            
            result.onSuccess {
                loadPreferences() // Reload UI with restored data
                _events.send(SettingsEvent.RestoreSuccess)
            }.onFailure { error ->
                _events.send(SettingsEvent.RestoreError(error.message ?: "Restore failed"))
            }
        }
    }

    fun onManualBackupClick() {
        _uiState.update { it.copy(isSyncing = true) }
        syncUseCase()
        // We'll observe sync status via the StateFlow in the UI or let the foreground service handle it
        viewModelScope.launch {
            syncUseCase.syncState.collectLatest { state ->
                when (state) {
                    is com.monetra.domain.model.SyncState.Success -> {
                        _uiState.update { it.copy(isSyncing = false) }
                        _events.send(SettingsEvent.SyncSuccess)
                    }
                    is com.monetra.domain.model.SyncState.Error -> {
                        _uiState.update { it.copy(isSyncing = false) }
                        _events.send(SettingsEvent.SyncError(state.message))
                    }
                    else -> {}
                }
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
