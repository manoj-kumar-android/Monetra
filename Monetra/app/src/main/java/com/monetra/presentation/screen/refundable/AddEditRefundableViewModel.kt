package com.monetra.presentation.screen.refundable

import android.app.NotificationManager
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.monetra.data.worker.RefundableReminderWorker
import com.monetra.domain.model.Refundable
import com.monetra.domain.model.RefundableType
import com.monetra.domain.repository.RefundableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class AddEditRefundableViewModel @Inject constructor(
    private val repository: RefundableRepository,
    private val application: android.app.Application,
    private val savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private var refundableId: Long? = null

    private val _uiState = MutableStateFlow<AddEditRefundableUiState>(
        // Initial state doesn't use SavedStateHandle directly anymore to avoid pre-filling wrong data
        // before loadRefundable checks the ID.
        AddEditRefundableUiState()
    )
    val uiState: StateFlow<AddEditRefundableUiState> = _uiState.asStateFlow()

    fun loadRefundable(id: Long?) {
        this.refundableId = id

        // Check if we should clear SavedStateHandle because the ID has changed
        val lastSavedId = savedStateHandle.get<Long>("last_id")
        if (savedStateHandle.contains("last_id") && lastSavedId != (id ?: -1L)) {
            clearSessionData()
        }
        savedStateHandle["last_id"] = id ?: -1L
        
        if (id == null) {
            this.refundableId = null
            // Restore from SavedStateHandle if available (for current session)
            _uiState.value = AddEditRefundableUiState(
                amount = savedStateHandle.get<String>("amount") ?: "",
                personName = savedStateHandle.get<String>("personName") ?: "",
                phoneNumber = savedStateHandle.get<String>("phoneNumber") ?: "",
                givenDate = savedStateHandle.get<String>("givenDate")?.let { LocalDate.parse(it) } ?: LocalDate.now(),
                dueDate = savedStateHandle.get<String>("dueDate")?.let { LocalDateTime.parse(it) } ?: LocalDateTime.now().plusWeeks(1).withHour(10).withMinute(0),
                note = savedStateHandle.get<String>("note") ?: "",
                remindMe = savedStateHandle.get<Boolean>("remindMe") ?: true,
                entryType = savedStateHandle.get<RefundableType>("entryType") ?: RefundableType.LENT
            )
            return
        }
        
        this.refundableId = id
        viewModelScope.launch {
            repository.getRefundableById(id)?.let { refundable ->
                // Only load if state is still default (not restored from SavedStateHandle with user changes)
                // Actually, for EDIT, we should probably prefer the restored state if any.
                // But for the FIRST load, we need to populate from DB.
                _uiState.value = AddEditRefundableUiState(
                    amount = savedStateHandle.get<String>("amount") ?: refundable.amount.toString(),
                    personName = savedStateHandle.get<String>("personName") ?: refundable.personName,
                    phoneNumber = savedStateHandle.get<String>("phoneNumber") ?: refundable.phoneNumber,
                    givenDate = savedStateHandle.get<String>("givenDate")?.let { LocalDate.parse(it) } ?: refundable.givenDate,
                    dueDate = savedStateHandle.get<String>("dueDate")?.let { LocalDateTime.parse(it) } ?: refundable.dueDate,
                    note = savedStateHandle.get<String>("note") ?: (refundable.note ?: ""),
                    remindMe = savedStateHandle.get<Boolean>("remindMe") ?: refundable.remindMe,
                    entryType = savedStateHandle.get<RefundableType>("entryType") ?: refundable.entryType,
                    isEdit = true,
                    isPaid = refundable.isPaid,
                    isSaved = false
                )
            } ?: run {
                _uiState.value = _uiState.value.copy(isSaved = false)
            }
        }
    }
    
    fun onSaveConsumed() {
        _uiState.value = _uiState.value.copy(isSaved = false)
    }

    fun onExit() {
        clearSessionData()
    }

    private fun clearSessionData() {
        listOf("amount", "personName", "phoneNumber", "givenDate", "dueDate", "note", "remindMe", "entryType")
            .forEach { savedStateHandle.remove<Any>(it) }
        savedStateHandle.remove<Long>("last_id")
    }

    fun onEvent(event: AddEditRefundableEvent) {
        when (event) {
            is AddEditRefundableEvent.AmountChanged -> {
                _uiState.value = _uiState.value.copy(amount = event.amount)
                savedStateHandle["amount"] = event.amount
            }
            is AddEditRefundableEvent.PersonNameChanged -> {
                _uiState.value = _uiState.value.copy(personName = event.name)
                savedStateHandle["personName"] = event.name
            }
            is AddEditRefundableEvent.PhoneNumberChanged -> {
                // Keep only digits and limit to 10
                val filtered = event.number.filter { it.isDigit() }.take(12)
                _uiState.value = _uiState.value.copy(phoneNumber = filtered)
                savedStateHandle["phoneNumber"] = filtered
            }
            is AddEditRefundableEvent.GivenDateChanged -> {
                _uiState.value = _uiState.value.copy(givenDate = event.date)
                savedStateHandle["givenDate"] = event.date.toString()
            }
            is AddEditRefundableEvent.DueDateChanged -> {
                _uiState.value = _uiState.value.copy(dueDate = event.date)
                savedStateHandle["dueDate"] = event.date.toString()
            }
            is AddEditRefundableEvent.NoteChanged -> {
                _uiState.value = _uiState.value.copy(note = event.note)
                savedStateHandle["note"] = event.note
            }
            is AddEditRefundableEvent.RemindMeToggled -> {
                _uiState.value = _uiState.value.copy(remindMe = event.toggled)
                savedStateHandle["remindMe"] = event.toggled
            }
            is AddEditRefundableEvent.EntryTypeChanged -> {
                _uiState.value = _uiState.value.copy(entryType = event.type)
                savedStateHandle["entryType"] = event.type
            }
            is AddEditRefundableEvent.Save -> saveRefundable()
        }
    }

    private fun saveRefundable() {
        val state = _uiState.value
        val amountValue = state.amount.toDoubleOrNull() ?: return
        
        // Prevent save if name is empty or phone number is provided but isn't 10 digits.
        if (state.personName.isBlank() || (state.phoneNumber.isNotEmpty() && state.phoneNumber.length != 10)) return

        val now = LocalDateTime.now()
        if (state.remindMe && state.dueDate.isBefore(now)) {
            val msg = if (state.dueDate.toLocalDate() == now.toLocalDate())
                "That time has already passed today — pick a later time."
            else
                "Reminder date/time is in the past — pick today or a future date."
            android.widget.Toast.makeText(application, msg, android.widget.Toast.LENGTH_LONG).show()
            return
        }

        viewModelScope.launch {
            val refundable = Refundable(
                id = refundableId ?: 0,
                amount = amountValue,
                personName = state.personName,
                phoneNumber = state.phoneNumber,
                givenDate = state.givenDate,
                dueDate = state.dueDate,
                note = state.note.ifBlank { null },
                remindMe = state.remindMe,
                entryType = state.entryType,
                isPaid = state.isPaid
            )

            // Upsert: returns new rowId for INSERT, -1 for UPDATE.
            val upsertResult = repository.upsertRefundable(refundable)

            // ── Determine the CORRECT entity ID ────────────────────────────────────
            // For EDIT: refundableId is non-null and is the real ID.
            // For INSERT: upsertResult is the auto-generated rowId.
            val effectiveId: Long = refundableId ?: upsertResult

            // Dismiss any currently shown notification for this entry
            val notificationManager = application.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(effectiveId.toInt())

            // ── Always cancel the existing work for this specific entry ─────────────
            // This covers all 3 cases:
            //   1. User updates time BEFORE notification fires → cancels the old schedule
            //   2. User updates time AFTER notification fires  → worker already ran (no-op cancel)
            //   3. User updates multiple times               → only the last enqueue survives
            val workerTag = "refundable_reminder_$effectiveId"
            WorkManager.getInstance(application).cancelUniqueWork(workerTag)

            // ── Re-schedule only if remindMe is ON ────────────────────────────────
            if (state.remindMe) {
                val targetEpoch = state.dueDate
                    .atZone(java.time.ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                val delayMillis = (targetEpoch - System.currentTimeMillis()).coerceAtLeast(0L)

                val inputData = Data.Builder()
                    .putLong("refundable_id", effectiveId)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<RefundableReminderWorker>()
                    .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                    .setInputData(inputData)
                    .build()

                // REPLACE ensures any residual duplicate work is wiped and replaced
                WorkManager.getInstance(application).enqueueUniqueWork(
                    workerTag,
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
            }

            _uiState.value = _uiState.value.copy(isSaved = true)
            // Clear SavedStateHandle background data but keep in-memory UI state for transition
            clearSessionData()
        }
    }
}

data class AddEditRefundableUiState(
    val amount: String = "",
    val personName: String = "",
    val phoneNumber: String = "",
    val givenDate: LocalDate = LocalDate.now(),
    val dueDate: LocalDateTime = LocalDateTime.now().plusWeeks(1).withHour(10).withMinute(0),
    val note: String = "",
    val remindMe: Boolean = true,
    val entryType: RefundableType = RefundableType.LENT,
    val isEdit: Boolean = false,
    val isPaid: Boolean = false,
    val isSaved: Boolean = false
)

sealed class AddEditRefundableEvent {
    data class AmountChanged(val amount: String) : AddEditRefundableEvent()
    data class PersonNameChanged(val name: String) : AddEditRefundableEvent()
    data class PhoneNumberChanged(val number: String) : AddEditRefundableEvent()
    data class GivenDateChanged(val date: LocalDate) : AddEditRefundableEvent()
    data class DueDateChanged(val date: LocalDateTime) : AddEditRefundableEvent()
    data class NoteChanged(val note: String) : AddEditRefundableEvent()
    data class RemindMeToggled(val toggled: Boolean) : AddEditRefundableEvent()
    data class EntryTypeChanged(val type: RefundableType) : AddEditRefundableEvent()
    data object Save : AddEditRefundableEvent()
}
