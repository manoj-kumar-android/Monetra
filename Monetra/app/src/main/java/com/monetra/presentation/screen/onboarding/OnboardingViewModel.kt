package com.monetra.presentation.screen.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.UserPreferences
import com.monetra.domain.repository.UserPreferenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferenceRepo: UserPreferenceRepository
) : ViewModel() {

    private val _income = MutableStateFlow("")
    val income = _income.asStateFlow()
    
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _savingsGoal = MutableStateFlow("")
    val savingsGoal = _savingsGoal.asStateFlow()

    private val _nameError = MutableStateFlow<String?>(null)
    val nameError = _nameError.asStateFlow()

    private val _incomeError = MutableStateFlow<String?>(null)
    val incomeError = _incomeError.asStateFlow()

    private val _savingsError = MutableStateFlow<String?>(null)
    val savingsError = _savingsError.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved = _isSaved.asStateFlow()

    init {
        viewModelScope.launch {
            userPreferenceRepo.getUserPreferences().firstOrNull()?.let {
                if (it.ownerName.isNotBlank() && it.ownerName != "there") {
                    _name.value = it.ownerName
                }
                if (it.monthlyIncome > 0) {
                    _income.value = if (it.monthlyIncome % 1.0 == 0.0) it.monthlyIncome.toInt().toString() else it.monthlyIncome.toString()
                }
                if (it.monthlySavingsGoal > 0) {
                    _savingsGoal.value = if (it.monthlySavingsGoal % 1.0 == 0.0) it.monthlySavingsGoal.toInt().toString() else it.monthlySavingsGoal.toString()
                }
            }
        }
    }

    fun setIncome(value: String) {
        _income.value = value
        _incomeError.value = null
        _savingsError.value = null
    }

    fun setName(value: String) {
        _name.value = value
        _nameError.value = null
    }

    fun setSavingsGoal(value: String) {
        _savingsGoal.value = value
        _savingsError.value = null
    }

    fun savePreferences() {
        val nameVal = _name.value.trim()
        val incomeVal = _income.value.toDoubleOrNull() ?: 0.0
        val savingsVal = _savingsGoal.value.toDoubleOrNull() ?: 0.0
        
        var hasError = false

        if (nameVal.isBlank()) {
            _nameError.value = "What should we call you?"
            hasError = true
        }

        if (incomeVal <= 0) {
            _incomeError.value = "Please enter your monthly income"
            hasError = true
        }

        if (savingsVal < 0) {
            _savingsError.value = "Savings cannot be negative"
            hasError = true
        } else if (incomeVal > 0 && savingsVal >= incomeVal) {
            _savingsError.value = "Savings goal should be less than income"
            hasError = true
        }

        if (hasError) return

        viewModelScope.launch {
            val currentPref = userPreferenceRepo.getUserPreferences().firstOrNull() 
                ?: UserPreferences(ownerName = nameVal, monthlyIncome = 0.0, monthlySavingsGoal = 0.0)
            
            userPreferenceRepo.saveUserPreferences(
                currentPref.copy(
                    ownerName = nameVal,
                    monthlyIncome = incomeVal,
                    monthlySavingsGoal = savingsVal,
                    isOnboardingCompleted = true
                )
            )
            _isSaved.value = true
        }
    }
}
