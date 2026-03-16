package com.monetra.presentation.screen.pending

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.PendingTransaction
import com.monetra.domain.repository.PendingTransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PendingTransactionsViewModel @Inject constructor(
    private val repository: PendingTransactionRepository
) : ViewModel() {

    val pendingTransactions: StateFlow<List<PendingTransaction>> = repository.getAllPending()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deletePending(id: Long) {
        viewModelScope.launch {
            repository.deletePending(id)
        }
    }
}
