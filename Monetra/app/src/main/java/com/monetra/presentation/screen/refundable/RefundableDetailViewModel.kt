package com.monetra.presentation.screen.refundable

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.monetra.domain.model.Refundable
import com.monetra.domain.repository.RefundableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RefundableDetailViewModel @Inject constructor(
    private val repository: RefundableRepository,
    private val application: Application,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val refundableId: Long = savedStateHandle.get<Long>("id") ?: -1L

    val refundable: StateFlow<Refundable?> = repository.observeRefundableById(refundableId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun markAsPaid(isPaid: Boolean) {
        viewModelScope.launch {
            repository.updatePaidStatus(refundableId, isPaid)
            if (isPaid) {
                cancelReminder()
            }
        }
    }

    fun deleteRefundable() {
        viewModelScope.launch {
            refundable.value?.let { repository.deleteRefundable(it) }
            cancelReminder()
            _isDeleted.value = true
        }
    }

    private fun cancelReminder() {
        if (refundableId != -1L) {
            WorkManager.getInstance(application).cancelUniqueWork("refundable_reminder_$refundableId")
        }
    }

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()
}
