package com.monetra.presentation.screen.refundable

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.monetra.domain.model.Refundable
import com.monetra.domain.repository.RefundableRepository
import com.monetra.data.worker.PendingDeleteManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RefundableDetailViewModel @Inject constructor(
    private val repository: RefundableRepository,
    private val application: Application,
    private val pendingDeleteManager: PendingDeleteManager
) : ViewModel() {

    private val _refundableId = MutableStateFlow(-1L)
    val refundableId: StateFlow<Long> = _refundableId.asStateFlow()

    fun loadRefundable(id: Long) {
        _refundableId.value = id
        _isDeleted.value = false
    }

    private val _pendingDeleteIds = pendingDeleteManager.getPendingIds("REFUNDABLE")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val refundable: StateFlow<Refundable?> = combine(_refundableId, _pendingDeleteIds) { id, pendingIds ->
        if (id == -1L || id in pendingIds) null
        else id
    }.flatMapLatest { id ->
        if (id == null) flowOf(null)
        else repository.observeRefundableById(id)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    fun markAsPaid(isPaid: Boolean) {
        val id = _refundableId.value
        if (id == -1L) return
        viewModelScope.launch {
            repository.updatePaidStatus(id, isPaid)
            if (isPaid) {
                cancelReminder()
            }
        }
    }

    fun deleteRefundable() {
        val id = _refundableId.value
        if (id == -1L) return
        val item = refundable.value ?: return
        
        viewModelScope.launch {
            pendingDeleteManager.requestDelete(id, item.remoteId, "REFUNDABLE")
            cancelReminder()
            _isDeleted.value = true
        }
    }

    private fun cancelReminder() {
        val id = _refundableId.value
        if (id != -1L) {
            WorkManager.getInstance(application).cancelUniqueWork("refundable_reminder_$id")
        }
    }

    private val _isDeleted = MutableStateFlow(false)
    val isDeleted: StateFlow<Boolean> = _isDeleted.asStateFlow()
}
