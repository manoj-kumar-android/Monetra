package com.monetra.presentation.screen.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.Saving
import com.monetra.domain.repository.SavingRepository
import com.monetra.data.worker.PendingDeleteManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SavingsViewModel @Inject constructor(
    private val repository: SavingRepository,
    private val pendingDeleteManager: PendingDeleteManager
) : ViewModel() {

    private val _pendingDeleteIds = pendingDeleteManager.getPendingIds("SAVING")

    val savingsList: StateFlow<List<Saving>> = combine(
        repository.getAllSaving(),
        _pendingDeleteIds
    ) { list, pendingIds ->
        list.filter { it.id !in pendingIds }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSavings: StateFlow<Double> = combine(
        repository.getAllSaving(),
        _pendingDeleteIds
    ) { list, pendingIds ->
        list.filter { it.id !in pendingIds }.sumOf { it.amount }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun deleteSavings(saving: Saving) {
        viewModelScope.launch {
            pendingDeleteManager.requestDelete(saving.id, saving.remoteId, "SAVING")
        }
    }

    fun restoreSaving(id: Long) {
        viewModelScope.launch {
            pendingDeleteManager.cancelDelete(id, "SAVING")
        }
    }
}
