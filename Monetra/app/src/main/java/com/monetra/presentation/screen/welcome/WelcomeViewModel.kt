package com.monetra.presentation.screen.welcome

import com.monetra.domain.backup.RestoreManager
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
    private val restoreManager: RestoreManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(WelcomeUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = Channel<WelcomeEvent>()
    val events = _events.receiveAsFlow()

    fun onRestoreFromEncryptedUri(uri: Uri, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRestoring = true) }
            val result = restoreManager.restoreFromEncryptedUri(uri, password)
            _uiState.update { it.copy(isRestoring = false) }
            if (result.isSuccess) {
                _events.send(WelcomeEvent.RestoreSuccess)
            } else {
                _events.send(WelcomeEvent.RestoreError(result.exceptionOrNull()?.message ?: "Unknown error"))
            }
        }
    }
}

data class WelcomeUiState(
    val isRestoring: Boolean = false
)

sealed interface WelcomeEvent {
    data object RestoreSuccess : WelcomeEvent
    data class RestoreError(val message: String) : WelcomeEvent
}
