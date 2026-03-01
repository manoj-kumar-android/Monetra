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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WelcomeViewModel @Inject constructor(
    private val driveBackupManager: DriveBackupManager
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
                checkAndRestore()
            } else {
                _uiState.update { it.copy(isAuthenticating = false) }
                _events.send(WelcomeEvent.AuthError("Authentication failed"))
            }
        }
    }

    private suspend fun checkAndRestore() {
        _uiState.update { it.copy(isRestoring = true) }
        val result = driveBackupManager.restore()
        _uiState.update { it.copy(isRestoring = false, isAuthenticating = false) }
        
        result.onSuccess { file ->
            if (file != null) {
                // Here the main app should handle the restored database file
                // For now, we signal success. 
                // Actual DB injection/replacement is a separate step in the main app.
                _events.send(WelcomeEvent.RestoreSuccess(file))
            } else {
                _events.send(WelcomeEvent.NoBackupFound)
            }
        }.onFailure { error ->
            _events.send(WelcomeEvent.RestoreError(error.message ?: "Restore failed"))
        }
    }
}

data class WelcomeUiState(
    val isRestoring: Boolean = false,
    val isAuthenticating: Boolean = false
)

sealed interface WelcomeEvent {
    data class RestoreSuccess(val file: java.io.File) : WelcomeEvent
    data class RestoreError(val message: String) : WelcomeEvent
    data class AuthError(val message: String) : WelcomeEvent
    data object NoBackupFound : WelcomeEvent
}
