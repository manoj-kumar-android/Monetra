package com.monetra.presentation.screen.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.Saving
import com.monetra.domain.repository.SavingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditSavingsViewModel @Inject constructor(
    private val repository: SavingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddEditSavingsUiState())
    val uiState: StateFlow<AddEditSavingsUiState> = _uiState.asStateFlow()

    private var currentSavingId: Long? = null

    fun loadSavings(id: Long?) {
        if (id == null || id == 0L) {
            _uiState.value = AddEditSavingsUiState()
            currentSavingId = null
            return
        }
        currentSavingId = id
        viewModelScope.launch {
            repository.getSavingById(id)?.let { saving ->
                _uiState.value = AddEditSavingsUiState(
                    bankName = saving.bankName,
                    amount = saving.amount.toString(),
                    interestRate = saving.interestRate?.toString() ?: "",
                    note = saving.note,
                    isEdit = true,
                    isSaved = false
                )
            } ?: run {
                _uiState.value = AddEditSavingsUiState(isSaved = false)
            }
        }
    }

    fun onSaveConsumed() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }

    fun onBankNameChange(name: String) {
        _uiState.value = _uiState.value.copy(bankName = name)
    }

    fun onAmountChange(amount: String) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun onInterestRateChange(rate: String) {
        _uiState.value = _uiState.value.copy(interestRate = rate)
    }

    fun onNoteChange(note: String) {
        _uiState.value = _uiState.value.copy(note = note)
    }

    fun saveSavings() {
        val state = _uiState.value
        val amount = state.amount.toDoubleOrNull() ?: return
        if (state.bankName.isBlank()) return

        viewModelScope.launch {
            val saving = Saving(
                id = currentSavingId ?: 0L,
                bankName = state.bankName,
                amount = amount,
                interestRate = state.interestRate.toDoubleOrNull(),
                note = state.note
            )
            repository.insertSaving(saving)
            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }
}

data class AddEditSavingsUiState(
    val bankName: String = "",
    val amount: String = "",
    val interestRate: String = "",
    val note: String = "",
    val isEdit: Boolean = false,
    val isSaved: Boolean = false
)
