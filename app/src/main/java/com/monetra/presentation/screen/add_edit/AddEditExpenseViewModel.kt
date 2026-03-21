package com.monetra.presentation.screen.add_edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.Transaction
import com.monetra.domain.model.TransactionType
import com.monetra.domain.repository.PendingTransactionRepository
import com.monetra.domain.usecase.transaction.AddTransactionUseCase
import com.monetra.domain.usecase.transaction.GetAccountsUseCase
import com.monetra.domain.usecase.transaction.AddAccountUseCase
import com.monetra.domain.usecase.transaction.GetLastBalanceUseCase
import com.monetra.domain.usecase.transaction.GetTransactionByIdUseCase
import com.monetra.domain.usecase.transaction.UpdateTransactionUseCase
import com.monetra.domain.usecase.transaction.ValidateTransactionUseCase
import com.monetra.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.runtime.Immutable
import javax.inject.Inject

@Immutable
data class AddEditUiState(
    val title: String = "",
    val amount: String = "",
    val note: String = "",
    val isIncome: Boolean = false,
    val category: String = "General",
    val date: LocalDate = LocalDate.now(),
    val titleError: String? = null,
    val amountError: String? = null,
    val isLoading: Boolean = false,
    val isEditing: Boolean = false,
    val accountName: String = "Cash",
    val balanceAfter: String = "",
    val availableAccounts: List<String> = emptyList()
) {
    val formattedDate: String
        get() = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
}

sealed interface AddEditEvent {
    data object SaveSuccess : AddEditEvent
    data class ShowError(val message: String) : AddEditEvent
}

@HiltViewModel
class AddEditExpenseViewModel @Inject constructor(
    private val addTransaction: AddTransactionUseCase,
    private val updateTransaction: UpdateTransactionUseCase,
    private val getTransactionById: GetTransactionByIdUseCase,
    private val validateTransaction: ValidateTransactionUseCase,
    private val pendingRepository: PendingTransactionRepository,
    private val getAccounts: GetAccountsUseCase,
    private val addAccount: AddAccountUseCase,
    private val getLastBalance: GetLastBalanceUseCase
) : ViewModel() {

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            getAccounts().collect { accounts ->
                _uiState.update { it.copy(availableAccounts = accounts) }
            }
        }
    }

    private var transactionId: Long? = null
    private var pendingId: Long? = null
    private var pendingTimestamp: Long? = null

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<AddEditEvent>()
    val events = _events.receiveAsFlow()

    fun loadTransaction(id: Long?, pendingId: Long? = null) {
        _uiState.update { it.copy(titleError = null, amountError = null) }

        if (id == null) {
            this.transactionId = null
            if (pendingId != null) {
                loadFromPending(pendingId)
            } else {
                _uiState.update { AddEditUiState(availableAccounts = it.availableAccounts) }
            }
            return
        }
        
        this.transactionId = id
        
        _uiState.update { it.copy(isLoading = true, isEditing = true) }
        viewModelScope.launch {
            val transaction = getTransactionById(id)
            transaction?.let { tx ->
                _uiState.update {
                    it.copy(
                        title = tx.title,
                        amount = tx.amount.toString(),
                        note = tx.note,
                        isIncome = tx.type == TransactionType.INCOME,
                        category = tx.category,
                        date = tx.date,
                        accountName = tx.accountName,
                        balanceAfter = if (tx.balanceAfter > 0) tx.balanceAfter.toString() else "",
                        isLoading = false,
                        isEditing = true
                    )
                }
            } ?: run {
                _uiState.update { it.copy(isLoading = false, isEditing = false) }
                _events.send(AddEditEvent.ShowError("Transaction not found"))
            }
        }
    }

    private fun loadFromPending(id: Long) {
        this.pendingId = id
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            pendingRepository.getPendingById(id)?.let { pending ->
                this@AddEditExpenseViewModel.pendingTimestamp = pending.timestamp
                val txDate = java.time.Instant.ofEpochMilli(pending.timestamp)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()

                _uiState.update {
                    it.copy(
                        title = pending.senderReceiver,
                        amount = pending.amount.toString(),
                        isIncome = pending.type == TransactionType.INCOME,
                        note = "Ref: ${pending.referenceId ?: "N/A"}",
                        accountName = pending.accountPart ?: "Cash",
                        balanceAfter = if ((pending.balanceAfter ?: 0.0) > 0.0) pending.balanceAfter.toString() else "",
                        date = txDate,
                        isLoading = false
                    )
                }
            } ?: run {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun onTitleChange(title: String) {
        if (title.length <= 50) {
            _uiState.update { it.copy(title = title, titleError = null) }
        }
    }

    fun onAmountChange(amount: String) {
        // Sanitize: allow only digits and one decimal point
        var sanitized = amount.filter { it.isDigit() || it == '.' }
        
        // Only one decimal point
        if (sanitized.count { it == '.' } > 1) {
            val firstDotIndex = sanitized.indexOf('.')
            sanitized = sanitized.substring(0, firstDotIndex + 1) + 
                        sanitized.substring(firstDotIndex + 1).replace(".", "")
        }

        // Limit to 2 decimal places
        if (sanitized.contains('.')) {
            val parts = sanitized.split('.')
            if (parts.size > 1 && parts[1].length > 2) {
                sanitized = parts[0] + "." + parts[1].take(2)
            }
        }

        _uiState.update { it.copy(amount = sanitized, amountError = null) }
        calculateBalance()
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onTypeChange(isIncome: Boolean) {
        _uiState.update { it.copy(isIncome = isIncome) }
        calculateBalance()
    }

    fun onAccountChange(accountName: String) {
        _uiState.update { it.copy(accountName = accountName) }
        viewModelScope.launch {
            if (!_uiState.value.availableAccounts.contains(accountName)) {
                addAccount(accountName)
            }
            calculateBalance()
        }
    }

    fun onBalanceChange(balance: String) {
        val sanitized = balance.filter { it.isDigit() || it == '.' || it == '-' }
        _uiState.update { it.copy(balanceAfter = sanitized) }
    }

    private fun calculateBalance() {
        if (_uiState.value.isEditing) return
        
        viewModelScope.launch {
            val lastBalance = getLastBalance(_uiState.value.accountName) ?: 0.0
            val amount = _uiState.value.amount.toDoubleOrNull() ?: 0.0
            val isIncome = _uiState.value.isIncome
            
            val suggestedBalance = if (isIncome) {
                lastBalance + amount
            } else {
                // User Rule: If it was already 0, don't go negative. If it was >0, it can go negative.
                if (lastBalance <= 0.0) {
                    0.0
                } else {
                    lastBalance - amount
                }
            }
            _uiState.update { 
                it.copy(balanceAfter = if (suggestedBalance > 0) suggestedBalance.toString() else "") 
            }
        }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onSaveClick() {
        val currentState = _uiState.value
        if (currentState.isLoading) return
        
        val validationResult = validateTransaction(
            title = currentState.title,
            amount = currentState.amount,
            date = currentState.date
        )

        if (!validationResult.isValid) {
            _uiState.update {
                it.copy(
                    titleError = validationResult.titleError,
                    amountError = validationResult.amountError
                )
            }
            if (validationResult.genericError != null) {
                viewModelScope.launch {
                    _events.send(AddEditEvent.ShowError(validationResult.genericError))
                }
            }
            return
        }

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                val transaction = Transaction(
                    id = transactionId ?: 0L,
                    title = currentState.title,
                    amount = currentState.amount.toDouble(),
                    type = if (currentState.isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                    category = currentState.category,
                    date = currentState.date,
                    note = currentState.note,
                    accountName = currentState.accountName,
                    balanceAfter = currentState.balanceAfter.toDoubleOrNull() ?: 0.0,
                    updatedAt = pendingTimestamp ?: System.currentTimeMillis()
                )

                if (transactionId != null) {
                    updateTransaction(transaction)
                } else {
                    addTransaction(transaction)
                    // If this was from a suggestion, clear it
                    pendingId?.let { pendingRepository.deletePending(it) }
                }
                _events.send(AddEditEvent.SaveSuccess)
            } catch (e: Exception) {
                _events.send(AddEditEvent.ShowError("Failed to save transaction: ${e.message}"))
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
