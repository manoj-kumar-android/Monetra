package com.monetra.presentation.screen.refundable

import android.app.Application
import android.content.ContentValues
import android.net.Uri
import android.telephony.SmsManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.monetra.domain.model.Refundable
import com.monetra.domain.model.RefundableType
import com.monetra.domain.repository.RefundableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit
import android.widget.Toast
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.monetra.data.worker.RefundableReminderWorker
import javax.inject.Inject

@HiltViewModel
class AddEditRefundableViewModel @Inject constructor(
    private val repository: RefundableRepository,
    private val application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val refundableId: Long? = savedStateHandle.get<Long>("id")

    private val _uiState = MutableStateFlow<AddEditRefundableUiState>(AddEditRefundableUiState())
    val uiState: StateFlow<AddEditRefundableUiState> = _uiState.asStateFlow()

    init {
        refundableId?.let { id ->
            viewModelScope.launch {
                repository.getRefundableById(id)?.let { refundable ->
                    _uiState.value = AddEditRefundableUiState(
                        amount = refundable.amount.toString(),
                        personName = refundable.personName,
                        phoneNumber = refundable.phoneNumber,
                        givenDate = refundable.givenDate,
                        dueDate = refundable.dueDate,
                        note = refundable.note ?: "",
                        remindMe = refundable.remindMe,
                        sendSmsReminder = refundable.sendSmsReminder,
                        sendSmsImmediately = refundable.sendSmsImmediately,
                        entryType = refundable.entryType,
                        isEdit = true,
                        isPaid = refundable.isPaid
                    )
                }
            }
        }
    }

    fun onEvent(event: AddEditRefundableEvent) {
        when (event) {
            is AddEditRefundableEvent.AmountChanged -> _uiState.value = _uiState.value.copy(amount = event.amount)
            is AddEditRefundableEvent.PersonNameChanged -> _uiState.value = _uiState.value.copy(personName = event.name)
            is AddEditRefundableEvent.PhoneNumberChanged -> _uiState.value = _uiState.value.copy(phoneNumber = event.number)
            is AddEditRefundableEvent.GivenDateChanged -> _uiState.value = _uiState.value.copy(givenDate = event.date)
            is AddEditRefundableEvent.DueDateChanged -> _uiState.value = _uiState.value.copy(dueDate = event.date)
            is AddEditRefundableEvent.NoteChanged -> _uiState.value = _uiState.value.copy(note = event.note)
            is AddEditRefundableEvent.RemindMeToggled -> _uiState.value = _uiState.value.copy(remindMe = event.toggled)
            is AddEditRefundableEvent.SmsReminderToggled -> _uiState.value = _uiState.value.copy(sendSmsReminder = event.toggled)
            is AddEditRefundableEvent.SmsImmediateToggled -> _uiState.value = _uiState.value.copy(sendSmsImmediately = event.toggled)
            is AddEditRefundableEvent.EntryTypeChanged -> _uiState.value = _uiState.value.copy(entryType = event.type)
            is AddEditRefundableEvent.Save -> saveRefundable()
        }
    }

    private fun saveRefundable() {
        val state = _uiState.value
        val amountValue = state.amount.toDoubleOrNull() ?: return
        if (state.personName.isBlank() || state.phoneNumber.isBlank()) return

        // Validation: TODAY is fine — only block times that have ALREADY PASSED.
        // e.g. setting a 6 PM reminder at 12:41 PM today is allowed.
        val now = LocalDateTime.now()
        if ((state.remindMe || state.sendSmsReminder) && state.dueDate.isBefore(now)) {
            val dateLabel = if (state.dueDate.toLocalDate() == now.toLocalDate())
                "That time has already passed today — pick a later time."
            else
                "Reminder date/time is in the past — pick today or a future date."
            Toast.makeText(application, dateLabel, Toast.LENGTH_LONG).show()
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
                sendSmsReminder = state.sendSmsReminder,
                sendSmsImmediately = state.sendSmsImmediately,
                entryType = state.entryType,
                isPaid = state.isPaid
            )
            val newId = repository.upsertRefundable(refundable)

            // On edit: ALWAYS cancel the old reminder for this ID first so the
            // previous scheduled time is forgotten. Then schedule at the new time.
            val workerTag = "refundable_reminder_$newId"
            WorkManager.getInstance(application).cancelUniqueWork(workerTag)

            // Schedule reminder only if opted in AND time is in the future
            if (state.remindMe || state.sendSmsReminder) {
                val delayMillis = state.dueDate.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() - System.currentTimeMillis()
                if (delayMillis > 0) {
                    val inputData = Data.Builder()
                        .putLong("refundable_id", newId)
                        .build()

                    val workRequest = OneTimeWorkRequestBuilder<RefundableReminderWorker>()
                        .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
                        .setInputData(inputData)
                        .build()

                    WorkManager.getInstance(application).enqueueUniqueWork(
                        workerTag,
                        ExistingWorkPolicy.REPLACE,
                        workRequest
                    )
                }
            }

            if (state.sendSmsImmediately) {
                val immediateMsg = when (state.entryType) {
                    RefundableType.LENT ->
                        "Hi ${state.personName}, I've recorded that you owe me ₹${state.amount}, due on ${state.dueDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))}. - Sent via Monetra"
                    RefundableType.BORROWED ->
                        "Hi ${state.personName}, I've recorded my ₹${state.amount} payment to you, due on ${state.dueDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))}. I'll settle it on time. - Sent via Monetra"
                }
                sendSms(state.phoneNumber, immediateMsg)
            }

            _uiState.value = _uiState.value.copy(isSaved = true)
        }
    }

    private fun sendSms(phoneNumber: String, message: String) {
        try {
            val smsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                application.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val normalizedNumber = if (!phoneNumber.startsWith("+") && phoneNumber.length >= 10) {
                if (phoneNumber.startsWith("91") && phoneNumber.length == 12) "+$phoneNumber"
                else if (phoneNumber.length == 10) "+91$phoneNumber"
                else phoneNumber
            } else {
                phoneNumber
            }

            val parts = smsManager.divideMessage(message)
            if (parts.size > 1) {
                smsManager.sendMultipartTextMessage(normalizedNumber, null, parts, null, null)
            } else {
                smsManager.sendTextMessage(normalizedNumber, null, message, null, null)
            }
            saveSmsToSentBox(normalizedNumber, message)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun saveSmsToSentBox(phoneNumber: String, message: String) {
        try {
            val values = ContentValues().apply {
                put("address", phoneNumber)
                put("body", message)
                put("date", System.currentTimeMillis())
                put("read", 1)
                put("type", 2) // MESSAGE_TYPE_SENT
            }
            application.contentResolver.insert(Uri.parse("content://sms/sent"), values)
        } catch (e: Exception) {
            e.printStackTrace()
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
    val remindMe: Boolean = false,
    val sendSmsReminder: Boolean = false,
    val sendSmsImmediately: Boolean = false,
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
    data class SmsReminderToggled(val toggled: Boolean) : AddEditRefundableEvent()
    data class SmsImmediateToggled(val toggled: Boolean) : AddEditRefundableEvent()
    data class EntryTypeChanged(val type: RefundableType) : AddEditRefundableEvent()
    data object Save : AddEditRefundableEvent()
}
