package com.monetra.presentation.screen.welcome

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.drivebackup.api.DriveBackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val driveBackupManager: DriveBackupManager,
    private val userPreferenceRepository: com.monetra.domain.repository.UserPreferenceRepository,
    private val syncRepository: com.monetra.domain.repository.SyncRepository,
    private val cloudBackupRepository: com.monetra.domain.repository.CloudBackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<WelcomeEvent>()
    val events = _events.receiveAsFlow()

    val recoveryIntent = driveBackupManager.getDrivePermissionIntent()

    fun onContinueWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true) }
            val success = driveBackupManager.authenticate(activity)
            if (success) {
                // Check Drive permissions immediately
                val hasPermission = driveBackupManager.checkDrivePermission()
                if (hasPermission) {
                    completeOnboarding()
                } else {
                    // DriveBackupManager updates recoveryIntent flow, which the UI observes
                    _uiState.update { it.copy(isAuthenticating = false, needsPermission = true) }
                }
            } else {
                _uiState.update { it.copy(isAuthenticating = false) }
                _events.send(WelcomeEvent.AuthError("Authentication failed"))
            }
        }
    }

    fun onSkipForNow() {
        viewModelScope.launch {
            completeOnboarding()
        }
    }

    fun onPermissionResult(success: Boolean) {
        viewModelScope.launch {
            if (success) {
                completeOnboarding()
            } else {
                driveBackupManager.signOut()
                _uiState.update { it.copy(needsPermission = false) }
                _events.send(WelcomeEvent.AuthError("Permission required to sync"))
            }
        }
    }

    private suspend fun completeOnboarding() {
        _uiState.update { it.copy(isAuthenticating = true) }
        
        // Mark onboarding as completed
        val prefs = userPreferenceRepository.getUserPreferences().first()
        val isGoogleUser = !driveBackupManager.googleUserId.first().isNullOrBlank()
        
        userPreferenceRepository.saveUserPreferences(
            prefs.copy(
                isOnboardingCompleted = true,
                isBackupEnabled = isGoogleUser // Auto-enable if Google logged in
            )
        )

        if (isGoogleUser) {
            cloudBackupRepository.runRestore()
        }
        
        _uiState.update { it.copy(isAuthenticating = false) }
        _events.send(WelcomeEvent.AuthSuccess)
    }
}

data class WelcomeUiState(
    val isRestoring: Boolean = false,
    val isAuthenticating: Boolean = false,
    val needsPermission: Boolean = false
)

sealed interface WelcomeEvent {
    data object AuthSuccess : WelcomeEvent
    data class AuthError(val message: String) : WelcomeEvent
}
