package com.monetra.presentation.screen.settings

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.repository.CloudBackupRepository
import com.monetra.domain.repository.SubscriptionRepository
import com.monetra.domain.repository.UserPreferenceRepository
import com.monetra.domain.usecase.BackupValidationResult
import com.monetra.domain.usecase.ValidateBackupUseCase
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
    val isPremiumUnlocked: Boolean = false,
    val isSmartSuggestionEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isNotificationListenerEnabled: Boolean = false,
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
    data class ShowAccountMismatch(val currentEmail: String, val syncedEmail: String) :
        SettingsEvent

    data class ShowBackupConfirmation(val email: String) : SettingsEvent
    data class ShowPremiumDialog(val featureName: String) : SettingsEvent
    data object ShowNotificationPermissionDialog : SettingsEvent
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: UserPreferenceRepository,
    private val cloudBackupRepository: CloudBackupRepository,
    private val driveBackupManager: DriveBackupManager,
    private val validateBackupUseCase: ValidateBackupUseCase,
    private val updatePreferences: UpdateUserPreferencesUseCase,
    private val subscriptionRepository: SubscriptionRepository,
    private val billingRepository: com.monetra.domain.repository.BillingRepository,
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
        observePremiumStatus()
        validateBackupOnOpen()
    }

    private fun validateBackupOnOpen() {
        viewModelScope.launch {
            val prefs = repository.getUserPreferences().first()
            if (prefs.isBackupEnabled && prefs.isPremiumUnlocked) {
                // Check validation in background to update sync status, 
                // but NEVER flip the isBackupEnabled switch to false automatically based on result.
                validateBackupUseCase()
            }
        }
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

    private fun observePremiumStatus() {
        viewModelScope.launch {
            subscriptionRepository.getSubscriptionStatus().collectLatest { subscription ->
                _uiState.update { it.copy(isPremiumUnlocked = subscription.isPremium) }
            }
        }
        viewModelScope.launch {
            driveBackupManager.googleUserId.collectLatest { userId ->
                _uiState.update { it.copy(isLoggedIn = !userId.isNullOrBlank()) }
            }
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

    fun onSyncClick(activity: Activity, confirmed: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = validateBackupUseCase(ignoreBackupCheck = confirmed)
            handleSyncValidationResult(result, activity, confirmed)
        }
    }

    private suspend fun handleSyncValidationResult(
        result: BackupValidationResult,
        activity: Activity,
        confirmed: Boolean
    ) {
        when (result) {
            is BackupValidationResult.Success -> {
                val syncResult = cloudBackupRepository.runSync()
                if (syncResult.isSuccess) {
                    _events.send(SettingsEvent.SyncSuccess)
                } else {
                    _events.send(
                        SettingsEvent.SyncError(
                            syncResult.exceptionOrNull()?.message ?: "Sync failed"
                        )
                    )
                }
            }

            is BackupValidationResult.NotSignedIn -> {
                val authSuccess = driveBackupManager.authenticate(activity)
                if (authSuccess) {
                    onSyncClick(activity, confirmed)
                } else {
                    _events.send(SettingsEvent.AuthError("Authentication failed"))
                }
            }

            is BackupValidationResult.PermissionMissing -> {
                // Handled via recoveryIntent observation in the Screen
            }

            is BackupValidationResult.AccountMismatch -> {
                _events.send(
                    SettingsEvent.ShowAccountMismatch(
                        result.currentEmail,
                        result.syncedEmail
                    )
                )
            }

            is BackupValidationResult.BackupExistsConfirmation -> {
                _events.send(SettingsEvent.ShowBackupConfirmation(result.email))
            }

            is BackupValidationResult.NoBackupFound -> {
                cloudBackupRepository.runSync()
                _events.send(SettingsEvent.SyncSuccess)
            }
        }
        _uiState.update { it.copy(isLoading = false) }
    }

    fun onBackupToggle(enabled: Boolean, activity: Activity, confirmed: Boolean = false) {
        if (enabled && !_uiState.value.isPremiumUnlocked) {
            viewModelScope.launch { _events.send(SettingsEvent.ShowPremiumDialog("Cloud Backup")) }
            return
        }

        toggleJob?.cancel()
        toggleJob = viewModelScope.launch {
            if (!enabled) {
                // If disabling, save immediately
                _uiState.update { it.copy(isBackupEnabled = false) }
                val currentPrefs = repository.getUserPreferences().first()
                repository.saveUserPreferences(currentPrefs.copy(isBackupEnabled = false))
                return@launch
            }

            // If enabling, show loading but DONT save to repository yet.
            // Screen handles Switch snap-back based on uiState.isBackupEnabled if needed,
            // but while isLoading=true, the switch should probably be disabled.
            _uiState.update { it.copy(isLoading = true) }

            val result = validateBackupUseCase(ignoreBackupCheck = confirmed)

            // Only save if it's a "success" or "ready to go" state
            if (result is BackupValidationResult.Success || result is BackupValidationResult.NoBackupFound) {
                handleValidationResult(result, activity, isManual = true, confirmed = confirmed)
            } else {
                // Not success (e.g. PermissionMissing, NotSignedIn, AccountMismatch)
                // We should NOT save. UI switch will snap back to OFF when isLoading becomes false
                // because it collects from repository (which is still false).
                handleValidationResult(result, activity, isManual = true, confirmed = confirmed)
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    private suspend fun handleValidationResult(
        result: BackupValidationResult,
        activity: Activity,
        isManual: Boolean,
        confirmed: Boolean = false
    ) {
        when (result) {
            is BackupValidationResult.Success -> {
                repository.saveUserPreferences(
                    repository.getUserPreferences().first().copy(isBackupEnabled = true)
                )
                val syncResult = cloudBackupRepository.runSync()
                if (syncResult.isSuccess) {
                    _events.send(SettingsEvent.SyncSuccess)
                } else {
                    _events.send(
                        SettingsEvent.SyncError(
                            syncResult.exceptionOrNull()?.message ?: "Sync failed"
                        )
                    )
                }
            }

            is BackupValidationResult.NotSignedIn -> {
                val authSuccess = driveBackupManager.authenticate(activity)
                if (!authSuccess) {
                    _events.send(SettingsEvent.AuthError("Authentication failed. Please sign in to enable backup."))
                } else {
                    // Re-validate after sign in
                    handleValidationResult(
                        validateBackupUseCase(ignoreBackupCheck = confirmed),
                        activity,
                        isManual,
                        confirmed
                    )
                }
            }

            is BackupValidationResult.PermissionMissing -> {
                // Requesting permission is handled via recoveryIntent observation in the Screen
            }

            is BackupValidationResult.AccountMismatch -> {
                _events.send(
                    SettingsEvent.ShowAccountMismatch(
                        result.currentEmail,
                        result.syncedEmail
                    )
                )
            }

            is BackupValidationResult.BackupExistsConfirmation -> {
                _events.send(SettingsEvent.ShowBackupConfirmation(result.email))
            }

            is BackupValidationResult.NoBackupFound -> {
                // First time sync, no remote backup, so just perform initial upload
                repository.saveUserPreferences(
                    repository.getUserPreferences().first().copy(isBackupEnabled = true)
                )
                cloudBackupRepository.runBackup()
                _events.send(SettingsEvent.BackupSuccess)
            }
        }
        _uiState.update { it.copy(isLoading = false) }
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
                        ownerName = prefs.ownerName,
                        monthlyIncome = if (prefs.monthlyIncome > 0) prefs.monthlyIncome.toString() else "",
                        monthlySavingsGoal = if (prefs.monthlySavingsGoal > 0) prefs.monthlySavingsGoal.toString() else "",
                        isBackupEnabled = prefs.isBackupEnabled,
                        isSmartSuggestionEnabled = prefs.isSmartSuggestionEnabled,
                        isBiometricEnabled = prefs.isBiometricEnabled,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSmartSuggestionToggle(enabled: Boolean) {
        if (!_uiState.value.isPremiumUnlocked) {
            viewModelScope.launch { _events.send(SettingsEvent.ShowPremiumDialog("Smart Suggestion")) }
            return
        }

        if (enabled && !isNotificationServiceEnabled()) {
            viewModelScope.launch { _events.send(SettingsEvent.ShowNotificationPermissionDialog) }
            return
        }

        viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences().first()
            repository.saveUserPreferences(currentPrefs.copy(isSmartSuggestionEnabled = enabled))
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val listeners = android.provider.Settings.Secure.getString(
            context.contentResolver,
            "enabled_notification_listeners"
        )
        return listeners != null && listeners.contains(context.packageName)
    }

    fun onNotificationPermissionResult(granted: Boolean) {
        if (granted) {
            onSmartSuggestionToggle(true)
        }
    }

    fun onBiometricToggle(enabled: Boolean) {
        if (!_uiState.value.isPremiumUnlocked) {
            viewModelScope.launch { _events.send(SettingsEvent.ShowPremiumDialog("Fingerprint Lock")) }
            return
        }
        viewModelScope.launch {
            val currentPrefs = repository.getUserPreferences().first()
            repository.saveUserPreferences(currentPrefs.copy(isBiometricEnabled = enabled))
        }
    }

    fun onPurchasePremiumClick(activity: Activity) {
        viewModelScope.launch {
            if (!_uiState.value.isLoggedIn) {
                val success = driveBackupManager.authenticate(activity)
                if (!success) {
                    _events.send(SettingsEvent.AuthError("Sign-in required to purchase premium"))
                    return@launch
                }
            }

            _uiState.update { it.copy(isLoading = true) }
            val result = billingRepository.startPurchase(activity)
            if (result.isFailure) {
                _events.send(
                    SettingsEvent.SyncError(
                        result.exceptionOrNull()?.message ?: "Purchase failed"
                    )
                )
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onBackupManual(activity: Activity) {
        if (!_uiState.value.isPremiumUnlocked) {
            viewModelScope.launch { _events.send(SettingsEvent.ShowPremiumDialog("Cloud Backup")) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val dbFile = context.getDatabasePath("monetra_db")
            val result = driveBackupManager.performManualBackup(dbFile)
            if (result.isSuccess) {
                _events.send(SettingsEvent.BackupSuccess)
            } else {
                _events.send(
                    SettingsEvent.BackupError(
                        result.exceptionOrNull()?.message ?: "Backup failed"
                    )
                )
            }
            _uiState.update { it.copy(isLoading = false) }
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
                monthlyIncome = sanitized, incomeError = null, isSuccess = false
            )
        }
    }

    fun onSavingsGoalChange(goal: String) {
        val sanitized = goal.filter { it.isDigit() || it == '.' }
        if (sanitized.count { it == '.' } > 1) return

        _uiState.update {
            it.copy(
                monthlySavingsGoal = sanitized, savingsError = null, isSuccess = false
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

    fun onSignOutClick() {
        viewModelScope.launch { driveBackupManager.signOut() }
    }
}
