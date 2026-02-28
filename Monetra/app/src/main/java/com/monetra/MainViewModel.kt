package com.monetra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.repository.UserPreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferenceRepo: UserPreferenceRepository
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady = _isReady.asStateFlow()

    private val _isLocked = MutableStateFlow(true)
    val isLocked = _isLocked.asStateFlow()

    private val _relockEvent = MutableSharedFlow<Unit>()
    val relockEvent = _relockEvent.asSharedFlow()

    private val _isDashboardUser = MutableStateFlow(false)
    val isDashboardUser = _isDashboardUser.asStateFlow()

    private val _pendingRefundableId = MutableStateFlow<Long?>(null)
    val pendingRefundableId = _pendingRefundableId.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferenceRepo.getUserPreferences().collectLatest { preferences ->
                _isDashboardUser.value = preferences.isOnboardingCompleted
                _isReady.value = true
            }
        }
    }

    fun requestRelock() {
        if (!_isLocked.value && _isDashboardUser.value) {
            viewModelScope.launch {
                _isLocked.value = true
                _relockEvent.emit(Unit)
            }
        }
    }

    fun setLocked(locked: Boolean) {
        _isLocked.value = locked
    }

    fun setPendingRefundableId(id: Long?) {
        _pendingRefundableId.value = id
    }

    fun consumePendingRefundableId(): Long? {
        val id = _pendingRefundableId.value
        _pendingRefundableId.value = null
        return id
    }
}
