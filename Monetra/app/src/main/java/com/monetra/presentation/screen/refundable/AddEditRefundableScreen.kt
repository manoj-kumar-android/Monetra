package com.monetra.presentation.screen.refundable

import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.R
import com.monetra.domain.model.RefundableType
import com.monetra.ui.theme.Spacing
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditRefundableScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditRefundableViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri ->
        uri?.let { contactUri ->
            val projection = arrayOf(
                ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME
            )
            context.contentResolver.query(contactUri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                    val name = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                    viewModel.onEvent(AddEditRefundableEvent.PersonNameChanged(name))
                    
                    val phoneProjection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    context.contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        phoneProjection,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )?.use { pc ->
                        if (pc.moveToFirst()) {
                            val number = pc.getString(pc.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                            viewModel.onEvent(AddEditRefundableEvent.PhoneNumberChanged(number))
                        }
                    }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEdit) stringResource(R.string.edit_entry) else stringResource(R.string.add_entry)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.onEvent(AddEditRefundableEvent.Save) }) {
                        Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // Entry Type Selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        RefundableType.LENT to "💰  I Lent Money",
                        RefundableType.BORROWED to "💳  I Borrowed"
                    ).forEach { (type, label) ->
                        val selected = uiState.entryType == type
                        Button(
                            onClick = { viewModel.onEvent(AddEditRefundableEvent.EntryTypeChanged(type)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Text(label, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            // Amount
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { viewModel.onEvent(AddEditRefundableEvent.AmountChanged(it)) },
                label = { Text(stringResource(R.string.amount)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                shape = RoundedCornerShape(12.dp)
            )

            // Person Name
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    contactPickerLauncher.launch(null)
                }
            }

            OutlinedTextField(
                value = uiState.personName,
                onValueChange = { viewModel.onEvent(AddEditRefundableEvent.PersonNameChanged(it)) },
                label = { Text(stringResource(R.string.person_name)) },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { 
                        permissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
                    }) {
                        Icon(Icons.Default.Contacts, contentDescription = null)
                    }
                },
                shape = RoundedCornerShape(12.dp)
            )

            // Phone Number
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = { viewModel.onEvent(AddEditRefundableEvent.PhoneNumberChanged(it)) },
                    label = { Text(stringResource(R.string.phone_number)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("+", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    supportingText = {
                        Text(
                            text = stringResource(R.string.phone_instruction),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    isError = uiState.phoneNumber.isNotEmpty() && uiState.phoneNumber.length < 10
                )
            }

            // Dates & Time
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                DatePickerButton(
                    modifier = Modifier.weight(1f),
                    label = stringResource(R.string.due_date),
                    date = uiState.dueDate.toLocalDate(),
                    onDateSelected = { 
                        viewModel.onEvent(AddEditRefundableEvent.DueDateChanged(uiState.dueDate.with(it))) 
                    }
                )
                TimePickerButton(
                    modifier = Modifier.weight(1f),
                    label = "Time",
                    time = uiState.dueDate.toLocalTime(),
                    onTimeSelected = { 
                        viewModel.onEvent(AddEditRefundableEvent.DueDateChanged(uiState.dueDate.with(it))) 
                    }
                )
            }

            // Note
            OutlinedTextField(
                value = uiState.note,
                onValueChange = { viewModel.onEvent(AddEditRefundableEvent.NoteChanged(it)) },
                label = { Text(stringResource(R.string.note)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )

            // Toggles
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                ToggleRow(
                    label = stringResource(R.string.remind_me),
                    checked = uiState.remindMe,
                    onCheckedChange = { viewModel.onEvent(AddEditRefundableEvent.RemindMeToggled(it)) }
                )
                ToggleRow(
                    label = stringResource(R.string.send_sms_reminder),
                    checked = uiState.sendSmsReminder,
                    onCheckedChange = { viewModel.onEvent(AddEditRefundableEvent.SmsReminderToggled(it)) }
                )
                ToggleRow(
                    label = stringResource(R.string.send_sms_immediately),
                    checked = uiState.sendSmsImmediately,
                    onCheckedChange = { viewModel.onEvent(AddEditRefundableEvent.SmsImmediateToggled(it)) }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerButton(
    modifier: Modifier,
    label: String,
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = date.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    )

    OutlinedCard(
        onClick = { showDialog = true },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = date.format(DateTimeFormatter.ofPattern("dd MMM yyyy")), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerButton(
    modifier: Modifier,
    label: String,
    time: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val timePickerState = rememberTimePickerState(
        initialHour = time.hour,
        initialMinute = time.minute
    )

    OutlinedCard(
        onClick = { showDialog = true },
        modifier = modifier,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = time.format(DateTimeFormatter.ofPattern("hh:mm a")), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onTimeSelected(LocalTime.of(timePickerState.hour, timePickerState.minute))
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
private fun ToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked, 
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                uncheckedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
            )
        )
    }
}
