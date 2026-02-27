package com.monetra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.repository.UserPreferenceRepository
import com.monetra.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferenceRepo: UserPreferenceRepository
) : ViewModel() {

    private val _startDestination: MutableStateFlow<Screen?> = MutableStateFlow(null)
    val startDestination = _startDestination.asStateFlow()

    // Emits true = go to dashboard after unlock, false = go to onboarding
    private val _relockEvent = MutableSharedFlow<Boolean>()
    val relockEvent = _relockEvent.asSharedFlow()

    // Persists whether the user has completed onboarding (set once from preferences)
    private var isDashboardUser = false

    init {
        viewModelScope.launch {
            userPreferenceRepo.getUserPreferences().collect { preferences ->
                isDashboardUser = preferences.isOnboardingCompleted
                if (_startDestination.value == null) {
                    // Always go through Lock first
                    _startDestination.value = Screen.Lock(goToDashboard = isDashboardUser)
                }
            }
        }
    }

    fun requestRelock() {
        viewModelScope.launch {
            _relockEvent.emit(isDashboardUser)
        }
    }
}
