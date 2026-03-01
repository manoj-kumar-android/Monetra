package com.monetra.presentation.screen.loans

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.R
import com.monetra.domain.model.Loan
import com.monetra.presentation.component.SwipeToDeleteContainer
import com.monetra.presentation.components.HelpIconButton
import com.monetra.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    viewModel: LoanViewModel = hiltViewModel()
) {
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show undo snackbar whenever a delete is pending
    LaunchedEffect(uiState.pendingDeleteLoan) {
        val item = uiState.pendingDeleteLoan ?: return@LaunchedEffect
        snackbarHostState.currentSnackbarData?.dismiss()
        val result = snackbarHostState.showSnackbar(
            message = "\"${item.name}\" EMI deleted",
            actionLabel = "UNDO",
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoDeleteLoan()
        }
    }

    val hapticAddClick = com.monetra.presentation.components.rememberHapticClick { 
        viewModel.toggleAddSheet(true) 
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                com.monetra.presentation.component.MonetraSnackbar(snackbarData = data)
            }
        },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.debt_emis_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = { HelpIconButton(onClick = onNavigateToHelp) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = hapticAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_loan))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            if (loans.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .fillParentMaxHeight(0.85f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(Spacing.md)
                        ) {
                            Text("💳", fontSize = 48.sp)
                            Text(
                                stringResource(R.string.no_loans_empty),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            items(loans, key = { it.id }, contentType = { "loan" }) { loan ->
                SwipeToDeleteContainer(
                    onDelete = {
                        viewModel.requestDeleteLoan(loan)
                        scope.launch { snackbarHostState.currentSnackbarData?.dismiss() }
                    }
                ) {
                    LoanItem(loan, onClick = { viewModel.onEditLoan(loan) })
                }
            }
        }

        if (uiState.isAddSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleAddSheet(false) },
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surface,
                sheetMaxWidth = 640.dp
            ) {
                AddLoanSheet(uiState = uiState, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddLoanSheet(uiState: LoanUiState, viewModel: LoanViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = Spacing.xl)
            .padding(top = Spacing.md)
            .padding(bottom = 64.dp)
            .navigationBarsPadding()
            .imePadding(),
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text(
            if (uiState.editingId != null) stringResource(R.string.edit_loan) else stringResource(R.string.add_new_emi),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            "We'll calculate your monthly EMI automatically.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(Spacing.xs))

        // ── Loan Name ──────────────────────────────────────────────────────
        OutlinedTextField(
            value = uiState.name,
            onValueChange = viewModel::onNameChange,
            label = { Text(stringResource(R.string.loan_name_placeholder)) },
            placeholder = { Text("e.g. Home Loan, Car Loan…") },
            isError = uiState.nameError != null,
            supportingText = if (uiState.nameError != null) {
                { Text(uiState.nameError!!, color = MaterialTheme.colorScheme.error) }
            } else null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // ── Loan Amount ──────────────────────────────────────────────────────
        OutlinedTextField(
            value = uiState.principal,
            onValueChange = viewModel::onPrincipalChange,
            label = { Text(stringResource(R.string.total_principal)) },
            placeholder = { Text("e.g. 500000") },
            prefix = { Text("₹ ") },
            isError = uiState.principalError != null,
            supportingText = if (uiState.principalError != null) {
                { Text(uiState.principalError!!, color = MaterialTheme.colorScheme.error) }
            } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // ── Interest Rate ──────────────────────────────────────────────────
        OutlinedTextField(
            value = uiState.interestRate,
            onValueChange = viewModel::onInterestRateChange,
            label = { Text("Annual Interest Rate") },
            placeholder = { Text("e.g. 8.5") },
            suffix = { Text("% p.a.") },
            isError = uiState.interestRateError != null,
            supportingText = if (uiState.interestRateError != null) {
                { Text(uiState.interestRateError!!, color = MaterialTheme.colorScheme.error) }
            } else {
                { Text("Enter 0 for an interest-free loan", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Next
            ),
            leadingIcon = { Icon(Icons.Default.Percent, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // ── Tenure ──────────────────────────────────────────────────────────
        OutlinedTextField(
            value = uiState.tenure,
            onValueChange = viewModel::onTenureChange,
            label = { Text(stringResource(R.string.tenure_months)) },
            placeholder = { Text("e.g. 60") },
            suffix = { Text("months") },
            isError = uiState.tenureError != null,
            supportingText = if (uiState.tenureError != null) {
                { Text(uiState.tenureError!!, color = MaterialTheme.colorScheme.error) }
            } else {
                val years = uiState.tenure.toIntOrNull()?.let { it / 12.0 }
                if (years != null && years > 0)
                    { { Text("= %.1f years".format(years), color = MaterialTheme.colorScheme.onSurfaceVariant) } }
                else null
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        // ── Start Date ──────────────────────────────────────────────────────
        OutlinedCard(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Loan Start Date",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        uiState.startDate.format(dateFormatter),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
                // Show months elapsed badge if start date is in the past
                val elapsed = java.time.temporal.ChronoUnit.MONTHS.between(uiState.startDate, LocalDate.now()).toInt().coerceAtLeast(0)
                if (elapsed > 0) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "$elapsed months elapsed",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        // ── Live EMI Preview ──────────────────────────────────────────────
        AnimatedVisibility(visible = uiState.calculatedEmi > 0) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Calculate,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                "MONTHLY EMI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        AnimatedContent(
                            targetState = uiState.calculatedEmi,
                            transitionSpec = { androidx.compose.animation.fadeIn() togetherWith androidx.compose.animation.fadeOut() },
                            label = "emi_anim"
                        ) { emi ->
                            Text(
                                "₹%,.0f / month".format(emi),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    // Show remaining tenure
                    val tenure = uiState.tenure.toIntOrNull() ?: 0
                    if (tenure > 0) {
                        val elapsed = java.time.temporal.ChronoUnit.MONTHS
                            .between(uiState.startDate, LocalDate.now()).toInt().coerceAtLeast(0)
                        val remaining = (tenure - elapsed).coerceAtLeast(0)
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "$remaining",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "months left",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }

                // Total interest to be paid
                val principal = uiState.principal.toDoubleOrNull() ?: 0.0
                val tenure = uiState.tenure.toIntOrNull() ?: 0
                if (principal > 0 && tenure > 0 && uiState.calculatedEmi > 0) {
                    val totalPayment = uiState.calculatedEmi * tenure
                    val totalInterest = totalPayment - principal
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = Spacing.lg),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                    Row(
                        modifier = Modifier.padding(Spacing.lg),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xl)
                    ) {
                        Column {
                            Text("Total Payment", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text("₹%,.0f".format(totalPayment), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        Column {
                            Text("Total Interest", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                            Text("₹%,.0f".format(totalInterest), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        // ── Save Button ──────────────────────────────────────────────────
        Button(
            onClick = viewModel::onSaveLoan,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            enabled = uiState.calculatedEmi > 0 || (uiState.principal.isNotBlank() && uiState.tenure.isNotBlank())
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(Spacing.sm))
            Text(
                if (uiState.editingId != null) stringResource(R.string.save_changes) else stringResource(R.string.save_debt_plan),
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(Modifier.height(Spacing.sm))
    }

    // ── Date Picker Dialog ──────────────────────────────────────────────
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.startDate.atStartOfDay()
                .atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = java.time.Instant.ofEpochMilli(millis)
                            .atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        viewModel.onStartDateChange(date)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun LoanItem(loan: Loan, onClick: () -> Unit) {
    val progressColor = when {
        loan.progress > 0.8f -> Color(0xFF34C759)
        loan.progress > 0.5f -> Color(0xFFFF9500)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("💳", fontSize = 20.sp)
                }
                Spacer(Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = loan.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${loan.category} • ${loan.annualInterestRate}% p.a.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Swipe hint instead of icon button
                Text(
                    "← swipe",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }

            Spacer(Modifier.height(Spacing.md))

            // Progress bar
            val animProgress by androidx.compose.animation.core.animateFloatAsState(
                targetValue = loan.progress,
                animationSpec = androidx.compose.animation.core.tween(800),
                label = "progress"
            )
            LinearProgressIndicator(
                progress = { animProgress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = progressColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(Spacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LoanMetric(
                    label = "Monthly EMI",
                    value = "₹%,.0f".format(loan.monthlyEmi),
                    color = MaterialTheme.colorScheme.primary
                )
                LoanMetric(
                    label = "Remaining",
                    value = "${loan.remainingTenure} months",
                    color = MaterialTheme.colorScheme.onSurface
                )
                LoanMetric(
                    label = "Balance",
                    value = "₹%,.0f".format(loan.remainingBalance),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun LoanMetric(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
