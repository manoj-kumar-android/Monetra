package com.monetra.presentation.screen.refundable

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.monetra.domain.model.Refundable
import com.monetra.domain.model.RefundableStatus
import com.monetra.domain.repository.RefundableRepository
import com.monetra.data.worker.PendingDeleteManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RefundableViewModel @Inject constructor(
    private val repository: RefundableRepository,
    private val application: Application,
    private val pendingDeleteManager: PendingDeleteManager
) : ViewModel() {

    private val _filter = MutableStateFlow<RefundableFilter>(RefundableFilter.ALL)
    val filter: StateFlow<RefundableFilter> = _filter.asStateFlow()

    private val _pendingDeleteIds = pendingDeleteManager.getPendingIds("REFUNDABLE")

    val refundables: StateFlow<List<Refundable>> = combine(
        repository.getAllRefundables(),
        _filter,
        _pendingDeleteIds
    ) { list, filter, pendingIds ->
        val filteredList = list.filter { it.id !in pendingIds }
        when (filter) {
            RefundableFilter.ALL -> filteredList
            RefundableFilter.PENDING -> filteredList.filter { it.status == RefundableStatus.PENDING }
            RefundableFilter.OVERDUE -> filteredList.filter { it.status == RefundableStatus.OVERDUE }
            RefundableFilter.PAID -> filteredList.filter { it.status == RefundableStatus.PAID }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setFilter(filter: RefundableFilter) {
        _filter.value = filter
    }

    fun markAsPaid(id: Long, isPaid: Boolean) {
        viewModelScope.launch {
            repository.updatePaidStatus(id, isPaid)
            if (isPaid) {
                WorkManager.getInstance(application).cancelUniqueWork("refundable_reminder_$id")
            }
        }
    }

    fun deleteRefundable(refundable: Refundable) {
        viewModelScope.launch {
            pendingDeleteManager.requestDelete(refundable.id, refundable.remoteId, "REFUNDABLE")
            // Note: Reminders will be canceled when the garbage collector actually deletes the item
            // or we could cancel them here if preferred. But let's cancel them here for immediate effect
            WorkManager.getInstance(application).cancelUniqueWork("refundable_reminder_${refundable.id}")
        }
    }

    fun restoreRefundable(id: Long) {
        viewModelScope.launch {
            pendingDeleteManager.cancelDelete(id, "REFUNDABLE")
            // Reminders should ideally be re-added, but that requires more logic
        }
    }
}

enum class RefundableFilter {
    ALL, PENDING, OVERDUE, PAID
}
