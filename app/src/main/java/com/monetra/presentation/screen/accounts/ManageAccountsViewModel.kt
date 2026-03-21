package com.monetra.presentation.screen.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.usecase.transaction.AddAccountUseCase
import com.monetra.domain.usecase.transaction.DeleteAccountUseCase
import com.monetra.domain.usecase.transaction.GetAccountsUseCase
import com.monetra.domain.usecase.transaction.UpdateAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ManageAccountsUiState(
    val accounts: List<String> = emptyList(),
    val inputText: String = "",
    val editingAccountName: String? = null,
    val isLoading: Boolean = false,
    val deletedAccountName: String? = null, // For undo
    val inputError: String? = null
)

@HiltViewModel
class ManageAccountsViewModel @Inject constructor(
    private val getAccounts: GetAccountsUseCase,
    private val addAccount: AddAccountUseCase,
    private val updateAccount: UpdateAccountUseCase,
    private val deleteAccount: DeleteAccountUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ManageAccountsUiState())
    val uiState: StateFlow<ManageAccountsUiState> = _uiState.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            getAccounts().collect { accountList ->
                val allAccounts = (listOf("CASH", "OTHER") + accountList).distinct()
                _uiState.update { it.copy(accounts = allAccounts) }
            }
        }
    }

    fun onInputTextChange(text: String) {
        _uiState.update { it.copy(inputText = text.uppercase(), inputError = null) }
    }

    fun onSelectAccountForEdit(name: String) {
        _uiState.update { it.copy(editingAccountName = name, inputText = name) }
    }

    fun onSaveClicked() {
        val state = _uiState.value
        val newName = state.inputText.trim().uppercase()

        if (newName.isBlank()) return

        // Prevent saving duplicate names unless it's the exact same name we're editing
        if (state.accounts.contains(newName)) {
            if (state.editingAccountName == null || state.editingAccountName != newName) {
                _uiState.update { it.copy(inputError = "Account '$newName' already exists") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, inputError = null) }
            try {
                if (state.editingAccountName != null) {
                    // Update existing
                    updateAccount(state.editingAccountName, newName)
                } else {
                    // Create new
                    addAccount(newName)
                }
            } finally {
                _uiState.update {
                    it.copy(
                        inputText = "",
                        editingAccountName = null,
                        isLoading = false,
                        inputError = null
                    )
                }
            }
        }
    }

    fun onDeleteAccount(name: String) {
        // Can't delete "CASH" or "OTHER" if we treat them specially, but for now we trust the user.
        // Usually, default base accounts aren't in the DB anyway, they are merged in UI.
        viewModelScope.launch {
            deleteAccount(name)
            _uiState.update { it.copy(deletedAccountName = name) }
        }
    }

    fun onRestoreDeletedAccount() {
        val accountToRestore = _uiState.value.deletedAccountName
        if (accountToRestore != null) {
            viewModelScope.launch {
                addAccount(accountToRestore)
                _uiState.update { it.copy(deletedAccountName = null) }
            }
        }
    }

    fun onSnackbarDismissed() {
        _uiState.update { it.copy(deletedAccountName = null) }
    }
}
