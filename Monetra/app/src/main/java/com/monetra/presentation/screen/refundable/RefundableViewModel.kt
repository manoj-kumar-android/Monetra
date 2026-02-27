package com.monetra.presentation.screen.refundable

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.monetra.domain.model.Refundable
import com.monetra.domain.model.RefundableStatus
import com.monetra.domain.repository.RefundableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RefundableViewModel @Inject constructor(
    private val repository: RefundableRepository,
    private val application: Application
) : ViewModel() {

    private val _filter = MutableStateFlow<RefundableFilter>(RefundableFilter.ALL)
    val filter: StateFlow<RefundableFilter> = _filter.asStateFlow()

    val allRefundables: StateFlow<List<Refundable>> = repository.getAllRefundables()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val refundables: StateFlow<List<Refundable>> = combine(
        repository.getAllRefundables(),
        _filter
    ) { list, filter ->
        when (filter) {
            RefundableFilter.ALL -> list
            RefundableFilter.PENDING -> list.filter { it.status == RefundableStatus.PENDING }
            RefundableFilter.OVERDUE -> list.filter { it.status == RefundableStatus.OVERDUE }
            RefundableFilter.PAID -> list.filter { it.status == RefundableStatus.PAID }
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
            repository.deleteRefundable(refundable)
            WorkManager.getInstance(application).cancelUniqueWork("refundable_reminder_${refundable.id}")
        }
    }

    fun restoreRefundable(refundable: Refundable) {
        viewModelScope.launch {
            repository.upsertRefundable(refundable)
            // Note: Reminders are not automatically re-scheduled upon simple undo
            // to prevent notification spam logic, though it could be added if needed.
        }
    }
}

enum class RefundableFilter {
    ALL, PENDING, OVERDUE, PAID
}
