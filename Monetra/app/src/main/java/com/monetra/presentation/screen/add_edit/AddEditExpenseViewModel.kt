package com.monetra.presentation.screen.add_edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.monetra.domain.model.Transaction
import com.monetra.domain.model.TransactionType
import com.monetra.domain.usecase.transaction.AddTransactionUseCase
import com.monetra.domain.usecase.transaction.GetTransactionByIdUseCase
import com.monetra.domain.usecase.transaction.UpdateTransactionUseCase
import com.monetra.domain.usecase.transaction.ValidateTransactionUseCase
import com.monetra.presentation.navigation.Screen
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
    savedStateHandle: SavedStateHandle,
    private val addTransaction: AddTransactionUseCase,
    private val updateTransaction: UpdateTransactionUseCase,
    private val getTransactionById: GetTransactionByIdUseCase,
    private val validateTransaction: ValidateTransactionUseCase
) : ViewModel() {

    private val transactionId: Long? = savedStateHandle.toRoute<Screen.AddEditTransaction>().transactionId

    private val _uiState = MutableStateFlow(AddEditUiState())
    val uiState: StateFlow<AddEditUiState> = _uiState.asStateFlow()

    private val _events = Channel<AddEditEvent>()
    val events = _events.receiveAsFlow()

    init {
        if (transactionId != null) {
            _uiState.update { it.copy(isLoading = true, isEditing = true) }
            viewModelScope.launch {
                val transaction = getTransactionById(transactionId)
                transaction?.let { tx ->
                    _uiState.update {
                        it.copy(
                            title = tx.title,
                            amount = tx.amount.toString(),
                            note = tx.note,
                            isIncome = tx.type == TransactionType.INCOME,
                            category = tx.category,
                            date = tx.date,
                            isLoading = false
                        )
                    }
                } ?: run {
                    _uiState.update { it.copy(isLoading = false) }
                    _events.send(AddEditEvent.ShowError("Transaction not found"))
                }
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
    }

    fun onNoteChange(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun onTypeChange(isIncome: Boolean) {
        _uiState.update { it.copy(isIncome = isIncome) }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun onDateChange(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onSaveClick() {
        val currentState = _uiState.value
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
                    note = currentState.note
                )

                if (transactionId != null) {
                    updateTransaction(transaction)
                } else {
                    addTransaction(transaction)
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
