package com.monetra.presentation.screen.welcome

import com.monetra.drivebackup.api.DriveBackupManager
import android.app.Activity
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import com.monetra.domain.repository.CloudBackupRepository
import com.monetra.domain.repository.UserPreferenceRepository
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val driveBackupManager: DriveBackupManager,
    private val cloudBackupRepository: CloudBackupRepository,
    private val userPreferenceRepository: com.monetra.domain.repository.UserPreferenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<WelcomeEvent>()
    val events = _events.receiveAsFlow()

    fun onContinueWithGoogle(activity: Activity) {
        viewModelScope.launch {
            _uiState.update { it.copy(isAuthenticating = true) }
            val success = driveBackupManager.authenticate(activity)
            if (success) {
                // Mark onboarding as completed so MainViewModel navigates to Dashboard
                val prefs = userPreferenceRepository.getUserPreferences().first()
                userPreferenceRepository.saveUserPreferences(prefs.copy(isOnboardingCompleted = true))
                
                _uiState.update { it.copy(isAuthenticating = false) }
                // In new flow, we navigate to Dashboard first, then attempt restore there.
                _events.send(WelcomeEvent.AuthSuccess)
            } else {
                _uiState.update { it.copy(isAuthenticating = false) }
                _events.send(WelcomeEvent.AuthError("Authentication failed"))
            }
        }
    }
}

data class WelcomeUiState(
    val isRestoring: Boolean = false,
    val isAuthenticating: Boolean = false
)

sealed interface WelcomeEvent {
    data object AuthSuccess : WelcomeEvent
    data class AuthError(val message: String) : WelcomeEvent
}
