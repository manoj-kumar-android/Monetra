package com.monetra.presentation.screen.add_edit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.monetra.R
import com.monetra.ui.theme.Elevation
import com.monetra.ui.theme.Spacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddEditExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is AddEditEvent.SaveSuccess -> onNavigateBack()
                is AddEditEvent.ShowError -> { /* Handle Error UI */ }
            }
        }
    }

    AddEditExpenseContent(
        onNavigateBack = onNavigateBack,
        title = uiState.title,
        onTitleChange = viewModel::onTitleChange,
        amount = uiState.amount,
        onAmountChange = viewModel::onAmountChange,
        note = uiState.note,
        onNoteChange = viewModel::onNoteChange,
        formattedDate = uiState.formattedDate,
        onDateClick = { showDatePicker = true },
        isIncome = uiState.isIncome,
        onTypeChange = viewModel::onTypeChange,
        category = uiState.category,
        onCategoryChange = viewModel::onCategoryChange,
        titleError = uiState.titleError,
        amountError = uiState.amountError,
        onSaveClick = viewModel::onSaveClick
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        viewModel.onDateChange(date)
                    }
                    showDatePicker = false
                }) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditExpenseContent(
    onNavigateBack: () -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    amount: String,
    onAmountChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    formattedDate: String,
    onDateClick: () -> Unit,
    isIncome: Boolean,
    onTypeChange: (Boolean) -> Unit,
    category: String,
    onCategoryChange: (String) -> Unit,
    titleError: String?,
    amountError: String?,
    onSaveClick: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize().imePadding(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(if (title.isBlank()) R.string.add_transaction_title else R.string.edit_transaction_title),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md)
            ) {
                Button(
                    onClick = onSaveClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.lg),
        ) {
            Spacer(modifier = Modifier.height(Spacing.md))
            
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !isIncome,
                    onClick = { onTypeChange(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text(stringResource(R.string.expense))
                }
                SegmentedButton(
                    selected = isIncome,
                    onClick = { onTypeChange(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text(stringResource(R.string.income))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))
            
            // Suggestions (localized suggestions would be better, but for now we keep the strings or skip if not critical)
            val suggestions = if (!isIncome) {
                listOf("Groceries", "Coffee", "Dinner", "Fuel", "Rent", "WiFi")
            } else {
                listOf("Salary", "Gift", "Refund", "Freelance")
            }
            
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                items(suggestions) { suggestion ->
                    SuggestionChip(
                        onClick = { onTitleChange(suggestion) },
                        label = { Text(suggestion, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            Text(
                text = stringResource(R.string.details_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.sm, bottom = Spacing.xs)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column {
                    GroupedTextField(
                        value = amount,
                        onValueChange = onAmountChange,
                        placeholder = stringResource(R.string.amount_placeholder),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        textStyle = MaterialTheme.typography.headlineMedium,
                        isError = amountError != null
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = Spacing.lg))
                    GroupedTextField(
                        value = title,
                        onValueChange = onTitleChange,
                        placeholder = stringResource(R.string.title_placeholder),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        isError = titleError != null
                    )
                    HorizontalDivider(modifier = Modifier.padding(start = Spacing.lg))
                    GroupedTextField(
                        value = note,
                        onValueChange = onNoteChange,
                        placeholder = stringResource(R.string.notes_placeholder),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = stringResource(R.string.category_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.sm, bottom = Spacing.xs)
            )
            
            EmojiCategoryGrid(category, onCategoryChange)
            
            if (amountError != null) ErrorText(amountError)
            if (titleError != null) ErrorText(titleError)

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                text = stringResource(R.string.date_label),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = Spacing.sm, bottom = Spacing.xs)
            )

            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onDateClick),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(stringResource(R.string.date_label), modifier = Modifier.weight(1f))
                    Text(formattedDate, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(Spacing.xxxl))
        }
    }
}

@Composable
private fun GroupedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardOptions: KeyboardOptions,
    textStyle: androidx.compose.ui.text.TextStyle,
    isError: Boolean = false
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = textStyle) },
        modifier = Modifier.fillMaxWidth(),
        textStyle = textStyle,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        keyboardOptions = keyboardOptions,
        isError = isError,
        singleLine = true
    )
}

@Composable
private fun EmojiCategoryGrid(selected: String, onSelect: (String) -> Unit) {
    val categories = listOf(
        "General" to (R.string.cat_general to "💰"),
        "Food" to (R.string.cat_food to "🍔"),
        "Transport" to (R.string.cat_transport to "🚗"),
        "Shopping" to (R.string.cat_shopping to "🛍️"),
        "Fun" to (R.string.cat_fun to "🎭"),
        "Bills" to (R.string.cat_bills to "💡"),
        "Health" to (R.string.cat_health to "🏥"),
        "Salary" to (R.string.cat_salary to "💸")
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxWidth().height(180.dp),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        items(categories) { (id, data) ->
            val (resId, emoji) = data
            val isSelected = id == selected
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onSelect(id) },
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                ),
                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
            ) {
                Column(modifier = Modifier.padding(Spacing.md), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(emoji, fontSize = 24.sp)
                    Text(stringResource(resId), style = MaterialTheme.typography.labelSmall, maxLines = 1)
                }
            }
        }
    }
}

@Composable
private fun ErrorText(error: String) {
    Row(modifier = Modifier.padding(start = Spacing.sm, top = Spacing.xs), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.size(4.dp))
        Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
}
