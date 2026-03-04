package com.monetra.presentation.screen.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.repository.CloudBackupRepository
import com.monetra.domain.repository.UserPreferenceRepository
import com.monetra.domain.usecase.intelligence.UpdateUserPreferencesUseCase
import com.monetra.drivebackup.api.DriveBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val ownerName: String = "",
    val monthlyIncome: String = "",
    val monthlySavingsGoal: String = "",
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val nameError: String? = null,
    val incomeError: String? = null,
    val savingsError: String? = null,
    val isBackupAvailable: Boolean = false,
    val isBackupEnabled: Boolean = false,
    val isRestoring: Boolean = false,
    val isAuthenticating: Boolean = false,
    val accountName: String? = null,
    val lastBackupTime: Long? = null,
    val syncStatus: com.monetra.domain.model.SyncState = com.monetra.domain.model.SyncState.Idle
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
    @param:dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context
) : ViewModel() {
    private var toggleJob: kotlinx.coroutines.Job? = null

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadPreferences()
        observeCloudStatus()
    }

    private fun observeCloudStatus() {
        viewModelScope.launch {
            cloudBackupRepository.isRestoring.collectLatest { restoring ->
                _uiState.update { it.copy(isRestoring = restoring) }
            }
        }
        viewModelScope.launch {
            cloudBackupRepository.syncState.collectLatest { state ->
                _uiState.update { it.copy(syncStatus = state) }
            }
        }
        viewModelScope.launch {
            cloudBackupRepository.accountName.collectLatest { name ->
                _uiState.update { it.copy(accountName = name) }
                if (name != null) checkBackupAvailability()
            }
        }
        viewModelScope.launch {
            cloudBackupRepository.lastBackupTime.collectLatest { time ->
                _uiState.update { it.copy(lastBackupTime = time) }
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
            val success = cloudBackupRepository.checkDrivePermission()
            if (!success) {
                // If permission is not granted, DriveBackupManager will expose recoveryIntent
                // However, the current authenticate logic handles the first sign-in.
                val authSuccess = driveBackupManager.authenticate(activity)
                if (authSuccess) {
                    val hasPerm = cloudBackupRepository.checkDrivePermission()
                    if (!hasPerm) {
                        // Handled via recoveryIntent observation in the Screen
                    } else {
                        _events.send(SettingsEvent.AuthSuccess)
                    }
                } else {
                    _events.send(SettingsEvent.AuthError("Authentication failed"))
                }
            } else {
                _events.send(SettingsEvent.AuthSuccess)
            }
            _uiState.update { it.copy(isAuthenticating = false) }
        }
    }

    fun onBackupToggle(enabled: Boolean, activity: Activity) {
        _uiState.update { it.copy(isBackupEnabled = enabled) }
        toggleJob?.cancel()
        toggleJob = viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences().first()
            if (enabled) {
                _uiState.update { it.copy(isLoading = true) }
                
                // 1. Ensure authenticated
                var account = cloudBackupRepository.accountName.first()
                if (account == null) {
                    val authSuccess = driveBackupManager.authenticate(activity)
                    if (!authSuccess) {
                        _uiState.update { it.copy(isBackupEnabled = false, isLoading = false) }
                        _events.send(SettingsEvent.AuthError("Authentication failed"))
                        return@launch
                    }
                    account = cloudBackupRepository.accountName.first()
                }

                // 2. Ensure permission
                val hasPermission = cloudBackupRepository.checkDrivePermission()
                if (!hasPermission) {
                    // This triggers recoveryIntent observation in Screen. 
                    // Note: We don't reset isBackupEnabled here because the user is about to resolve it.
                    _uiState.update { it.copy(isLoading = false) }
                    return@launch 
                }

                // 3. Save Preference and Trigger Direct Sync
                repository.saveUserPreferences(currentPrefs.copy(isBackupEnabled = true))
                val syncResult = cloudBackupRepository.runSync()
                
                _uiState.update { it.copy(isLoading = false) }
                
                if (syncResult.isSuccess) {
                    _events.send(SettingsEvent.SyncSuccess)
                } else {
                    _events.send(SettingsEvent.SyncError(syncResult.exceptionOrNull()?.message ?: "Sync failed"))
                }
            } else {
                repository.saveUserPreferences(currentPrefs.copy(isBackupEnabled = false))
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onPermissionDenied() {
        viewModelScope.launch {
            cloudBackupRepository.signOut()
            _uiState.update { it.copy(isBackupEnabled = false) }
            _events.send(SettingsEvent.AuthError("Drive permission required for backup"))
        }
    }

    val recoveryIntent = cloudBackupRepository.recoveryIntent

    private fun loadPreferences() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.getUserPreferences().collectLatest { prefs ->
                _uiState.update {
                    it.copy(
                        ownerName = it.ownerName.ifEmpty { prefs.ownerName },
                        monthlyIncome = it.monthlyIncome.ifEmpty { if (prefs.monthlyIncome > 0) prefs.monthlyIncome.toString() else "" },
                        monthlySavingsGoal = it.monthlySavingsGoal.ifEmpty { if (prefs.monthlySavingsGoal > 0) prefs.monthlySavingsGoal.toString() else "" },
                        isBackupEnabled = prefs.isBackupEnabled,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update { it.copy(ownerName = name, nameError = null, isSuccess = false) }
    }

    fun onIncomeChange(income: String) {
        val sanitized = income.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return

        _uiState.update {
            it.copy(
                monthlyIncome = sanitized,
                incomeError = null,
                isSuccess = false
            )
        }
    }

    fun onSavingsGoalChange(goal: String) {
        val sanitized = goal.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return

        _uiState.update {
            it.copy(
                monthlySavingsGoal = sanitized,
                savingsError = null,
                isSuccess = false
            )
        }
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
            updatePreferences(
                ownerName = currentState.ownerName,
                income = income,
                savingsGoal = goal,
                isBackupEnabled = currentState.isBackupEnabled
            )
            _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            _events.send(SettingsEvent.SaveSuccess)
        }
    }
}
