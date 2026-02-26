package com.monetra

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.repository.UserPreferenceRepository
import com.monetra.presentation.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userPreferenceRepo: UserPreferenceRepository
) : ViewModel() {

    private val _startDestination: MutableStateFlow<Screen?> = MutableStateFlow(null)
    val startDestination = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferenceRepo.getUserPreferences().collect { preferences ->
                if (_startDestination.value == null) {
                    if (preferences.isOnboardingCompleted) {
                        _startDestination.value = Screen.TransactionList
                    } else {
                        _startDestination.value = Screen.Welcome
                    }
                }
            }
        }
    }
}
