package com.monetra.presentation.screen.monthly_expense

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import com.monetra.domain.model.BillStatus
import com.monetra.domain.model.MonthlyExpense
import com.monetra.presentation.components.HelpIconButton
import com.monetra.presentation.component.SwipeToDeleteContainer
import com.monetra.ui.theme.Spacing
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyExpenseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    viewModel: MonthlyExpenseViewModel = hiltViewModel()
) {
    val billModels by viewModel.billModels.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Show undo snackbar whenever a delete is pending
    LaunchedEffect(uiState.pendingDeleteExpense) {
        val item = uiState.pendingDeleteExpense ?: return@LaunchedEffect
        snackbarHostState.currentSnackbarData?.dismiss()
        val result = snackbarHostState.showSnackbar(
            message = "\"${item.name}\" deleted",
            actionLabel = "UNDO",
            duration = SnackbarDuration.Long
        )
        if (result == SnackbarResult.ActionPerformed) {
            viewModel.undoDelete()
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
                title = { Text("Recurring Fixed Bills", fontWeight = FontWeight.Bold) },
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
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_fixed_cost))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Configure your monthly bills once. Monetra will automatically track them every month and reserve the amount from your available balance.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(Spacing.md)
                    )
                }
            }
            if (billModels.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxWidth()
                            .fillParentMaxHeight(0.7f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🗓️", fontSize = 48.sp)
                            Spacer(Modifier.height(Spacing.md))
                            Text(
                                "No recurring bills set up yet.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            items(billModels, key = { it.rule.id }, contentType = { "bill_model" }) { model ->
                SwipeToDeleteContainer(
                    onDelete = {
                        viewModel.requestDelete(model.rule)
                        scope.launch {
                            snackbarHostState.currentSnackbarData?.dismiss()
                        }
                    },
                    title = "Delete Bill?",
                    message = "Are you sure you want to stop tracking this recurring bill?"
                ) {
                    BillItem(model, onClick = { viewModel.onEditExpense(model.rule) })
                }
            }
        }
    }

    if (uiState.isAddSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleAddSheet(false) },
            sheetState = sheetState,
            dragHandle = { BottomSheetDefaults.DragHandle() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.xl)
                    .padding(bottom = 48.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Text(
                    if (uiState.editingId != null) stringResource(R.string.edit_recurring_cost_title) else stringResource(R.string.add_recurring_cost_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                // Name field
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = viewModel::onNameChange,
                    label = { Text("Bill Description (e.g. Room Rent)") },
                    isError = uiState.nameError != null,
                    supportingText = if (uiState.nameError != null) {
                        { Text(uiState.nameError!!, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
                // Amount field
                OutlinedTextField(
                    value = uiState.amount,
                    onValueChange = viewModel::onAmountChange,
                    label = { Text("Monthly Amount") },
                    prefix = { Text("₹ ") },
                    isError = uiState.amountError != null,
                    supportingText = if (uiState.amountError != null) {
                        { Text(uiState.amountError!!, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
                // Due Day Selection
                Column {
                    Text("Due on day of month: ${uiState.dueDay}", style = MaterialTheme.typography.labelMedium)
                    Slider(
                        value = uiState.dueDay.toFloat(),
                        onValueChange = { viewModel.onDueDayChange(it.toInt()) },
                        valueRange = 1f..31f,
                        steps = 30
                    )
                }
                // Category Selection
                Text("Auto-link Category", style = MaterialTheme.typography.labelSmall)
                val categories = listOf(
                    "General" to (R.string.cat_general to "💰"),
                    "Food" to (R.string.cat_food to "🍔"),
                    "Transport" to (R.string.cat_transport to "🚗"),
                    "Shopping" to (R.string.cat_shopping to "🛍️"),
                    "Groceries" to (R.string.cat_groceries to "🛒"),
                    "Bills" to (R.string.cat_bills to "💡"),
                    "Rent" to (R.string.cat_rent to "🏠"),
                    "Subscription" to (R.string.cat_subscription to "🔄"),
                    "Fun" to (R.string.cat_fun to "🎭"),
                    "Health" to (R.string.cat_health to "🏥"),
                    "Mobile Recharge" to (R.string.cat_mobile_recharge to "📱")
                )
                androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                    columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth().height(240.dp),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                    userScrollEnabled = false
                ) {
                    items(categories, key = { it.first }, contentType = { "category" }) { (id, data) ->
                        val (resId, emoji) = data
                        val isSelected = id == uiState.category
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { viewModel.onCategoryChange(id) },
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
                Button(
                    onClick = viewModel::onSaveExpense,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(if (uiState.editingId != null) stringResource(R.string.save_changes) else stringResource(R.string.save_recurring_cost))
                }
                Spacer(Modifier.height(Spacing.sm))
            }
        }
    }
}

@Composable
fun BillItem(model: BillUiModel, onClick: () -> Unit) {
    val status = model.instance?.status ?: BillStatus.PENDING
    val statusColor = when (status) {
        BillStatus.PAID -> Color(0xFF34C759)
        BillStatus.PARTIAL -> Color(0xFFFF9500)
        BillStatus.PENDING -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(statusColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (status == BillStatus.PAID) "✅" else "🧾", fontSize = 20.sp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = model.rule.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Due on the ${model.rule.dueDay}${getDaySuffix(model.rule.dueDay)} of every month",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "₹%,.0f".format(model.rule.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black
                    )
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = status.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            if (status != BillStatus.PAID) {
                val paidAmount = model.instance?.paidAmount ?: 0.0
                val totalAmount = model.rule.amount
                
                Spacer(modifier = Modifier.height(Spacing.md))
                LinearProgressIndicator(
                    progress = { (paidAmount / totalAmount).toFloat().coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.1f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Paid: ₹%,.0f".format(paidAmount),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Reserved: ₹%,.0f".format((totalAmount - paidAmount).coerceAtLeast(0.0)),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

fun getDaySuffix(day: Int): String {
    if (day in 11..13) return "th"
    return when (day % 10) {
        1 -> "st"
        2 -> "nd"
        3 -> "rd"
        else -> "th"
    }
}
