package com.monetra.presentation.screen.transactions

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.CompareArrows
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.outlined.CompareArrows
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.monetra.R
import com.monetra.domain.model.TransactionType
import com.monetra.presentation.components.HelpIconButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.ZoneId
import com.monetra.presentation.screen.transactions.components.MonthlySummaryCard
import com.monetra.presentation.screen.transactions.components.TransactionRow
import com.monetra.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val monthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ExpenseListScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToHelp: () -> Unit,
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val pagingItems = viewModel.pagedTransactions.collectAsLazyPagingItems()
    val summary by viewModel.filterSummary.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val activeFilters by viewModel.activeFilters.collectAsStateWithLifecycle()
    val availableCategories by viewModel.availableCategories.collectAsStateWithLifecycle()
    val amountRange by viewModel.databaseAmountRange.collectAsStateWithLifecycle()
    
    val coroutineScope = rememberCoroutineScope()
    var isFilterSheetOpen by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is ExpenseEvent.NavigateToEdit -> onNavigateToEdit(event.transactionId)
                ExpenseEvent.NavigateToAdd -> onNavigateToAdd()
                is ExpenseEvent.ShowUndoSnackbar -> {
                    coroutineScope.launch {
                        snackbarHostState.currentSnackbarData?.dismiss()
                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = "Undo"
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            viewModel.undoDelete()
                        }
                    }
                }
                else -> {}
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
                TopAppBar(
                    title = {
                        Text(
                            text = stringResource(R.string.transactions_title),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    },
                    actions = {
                        IconButton(onClick = { isFilterSheetOpen = true }) {
                            Icon(Icons.Default.FilterAlt, contentDescription = "Filter")
                        }
                        HelpIconButton(onClick = onNavigateToHelp)
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
                
                // Search Bar
                TextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.md, vertical = Spacing.xs),
                    placeholder = { Text("Search title, notes, category...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        disabledIndicatorColor = androidx.compose.ui.graphics.Color.Transparent
                    ),
                    singleLine = true
                )
            }
        },
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Filter Summary
            MonthlySummaryCard(
                summary = summary,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm)
            )

            // Active Filter Chips
            ActiveFiltersRow(
                filters = activeFilters,
                onRemoveType = { viewModel.onFilterTypeChanged(null) },
                onRemoveCategory = viewModel::removeCategoryFilter,
                onRemoveDate = viewModel::clearDateFilter,
                onRemoveAmount = { viewModel.onAmountRangeChanged(null, null) }
            )

            // Continuous Timeline List
            TransactionTimeline(
                pagingItems = pagingItems,
                onTransactionClick = onNavigateToEdit,
                onDeleteClick = viewModel::onDeleteClick
            )
        }
    }

    if (isFilterSheetOpen) {
        FilterBottomSheet(
            activeFilters = activeFilters,
            availableCategories = availableCategories,
            databaseAmountRange = amountRange,
            onDismiss = { isFilterSheetOpen = false },
            onTypeSelected = viewModel::onFilterTypeChanged,
            onCategorySelected = viewModel::onCategorySelected,
            onDateRangeSelected = viewModel::onDateRangeSelected,
            onAmountRangeChanged = viewModel::onAmountRangeChanged,
            onClearAll = viewModel::clearAllFilters
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TransactionTimeline(
    pagingItems: androidx.paging.compose.LazyPagingItems<TransactionHistoryItem>,
    onTransactionClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = Spacing.md, end = Spacing.md, top = Spacing.sm, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = Modifier.fillMaxSize()
    ) {
        items(
            count = pagingItems.itemCount,
            key = { index -> 
                when (val item = pagingItems[index]) {
                    is TransactionHistoryItem.Transaction -> "tx-${item.uiItem.id}"
                    is TransactionHistoryItem.MonthHeader -> "header-${item.monthName}"
                    null -> "placeholder-$index"
                }
            },
            contentType = { index ->
                when (pagingItems[index]) {
                    is TransactionHistoryItem.Transaction -> "transaction"
                    is TransactionHistoryItem.MonthHeader -> "header"
                    null -> "placeholder"
                }
            }
        ) { index ->
            when (val item = pagingItems[index]) {
                is TransactionHistoryItem.Transaction -> {
                    com.monetra.presentation.component.SwipeToDeleteContainer(
                        onDelete = { onDeleteClick(item.uiItem.id) },
                        title = stringResource(R.string.delete_transaction_title),
                        message = stringResource(R.string.delete_transaction_msg)
                    ) {
                        TransactionRow(
                            item = item.uiItem,
                            onClick = { onTransactionClick(item.uiItem.id) },
                            onDelete = { onDeleteClick(item.uiItem.id) }
                        )
                    }
                }
                is TransactionHistoryItem.MonthHeader -> {
                    MonthStickyHeader(monthName = item.monthName)
                }
                null -> {
                    // Placeholder row
                    Box(modifier = Modifier.fillMaxWidth().height(72.dp).background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp)))
                }
            }
        }
    }
}

@Composable
private fun MonthStickyHeader(monthName: String) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm)
    ) {
        Text(
            text = monthName.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.5.sp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = Spacing.xs)
        )
    }
}

@Composable
private fun ActiveFiltersRow(
    filters: com.monetra.domain.model.TransactionFilters,
    onRemoveType: () -> Unit,
    onRemoveCategory: (String) -> Unit,
    onRemoveDate: () -> Unit,
    onRemoveAmount: () -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        // Type filter
        filters.type?.let { type ->
            item {
                AssistChip(
                    onClick = onRemoveType,
                    label = { Text(type.name) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                )
            }
        }

        // Category filters
        filters.categories?.forEach { category ->
            item {
                AssistChip(
                    onClick = { onRemoveCategory(category) },
                    label = { Text(category) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                )
            }
        }

        // Date range filter
        val start = filters.startDate
        val end = filters.endDate
        if (start != null || end != null) {
            item {
                val dateText = when {
                    start != null && end != null -> 
                        "${start.format(DateTimeFormatter.ofPattern("dd MMM"))} - ${end.format(DateTimeFormatter.ofPattern("dd MMM"))}"
                    start != null -> "From ${start.format(DateTimeFormatter.ofPattern("dd MMM"))}"
                    end != null -> "Until ${end.format(DateTimeFormatter.ofPattern("dd MMM"))}"
                    else -> ""
                }
                AssistChip(
                    onClick = onRemoveDate,
                    label = { Text(dateText) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }
        }

        // Amount filter
        val minAmt = filters.minAmount
        val maxAmt = filters.maxAmount
        if (minAmt != null || maxAmt != null) {
            item {
                val amountText = when {
                    minAmt != null && maxAmt != null -> "₹${minAmt.toInt()} - ₹${maxAmt.toInt()}"
                    minAmt != null -> "Min ₹${minAmt.toInt()}"
                    maxAmt != null -> "Max ₹${maxAmt.toInt()}"
                    else -> ""
                }
                AssistChip(
                    onClick = onRemoveAmount,
                    label = { Text(amountText) },
                    trailingIcon = { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterBottomSheet(
    activeFilters: com.monetra.domain.model.TransactionFilters,
    availableCategories: List<String>,
    databaseAmountRange: Pair<Double, Double>,
    onDismiss: () -> Unit,
    onTypeSelected: (TransactionType?) -> Unit,
    onCategorySelected: (String) -> Unit,
    onDateRangeSelected: (LocalDate?, LocalDate?) -> Unit,
    onAmountRangeChanged: (Double?, Double?) -> Unit,
    onClearAll: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val startPickerState = rememberDatePickerState(
        initialSelectedDateMillis = activeFilters.startDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )
    val endPickerState = rememberDatePickerState(
        initialSelectedDateMillis = activeFilters.endDate?.atStartOfDay(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    )

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val date = startPickerState.selectedDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    onDateRangeSelected(date, activeFilters.endDate)
                    showStartDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = startPickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val date = endPickerState.selectedDateMillis?.let {
                        Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    onDateRangeSelected(activeFilters.startDate, date)
                    showEndDatePicker = false
                }) { Text("OK") }
            }
        ) {
            DatePicker(state = endPickerState)
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = Spacing.md)
                    .size(width = 32.dp, height = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = CircleShape
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Filter Transactions",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                androidx.compose.material3.TextButton(
                    onClick = onClearAll
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.primary)
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Transaction Type Section
            FilterSectionHeader(title = "Transaction Type", icon = Icons.AutoMirrored.Outlined.CompareArrows)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                TransactionType.entries.forEach { type ->
                    val isSelected = activeFilters.type == type
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTypeSelected(if (isSelected) null else type) },
                        label = { Text(type.name, modifier = Modifier.padding(horizontal = 4.dp)) },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        shape = RoundedCornerShape(12.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (type == TransactionType.INCOME) 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.15f) 
                                else MaterialTheme.colorScheme.error.copy(alpha = 0.15f),
                            selectedLabelColor = if (type == TransactionType.INCOME)
                                MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Categories Section
            FilterSectionHeader(title = "Categories", icon = Icons.Outlined.Category)
            androidx.compose.foundation.layout.FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                availableCategories.forEach { category ->
                    val isSelected = activeFilters.categories?.contains(category) == true
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(category) },
                        label = { Text(category) },
                        shape = RoundedCornerShape(12.dp)
                    )
                }
                if (availableCategories.isEmpty()) {
                    Text(
                        "No categories used in this month",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Date Range Section
            FilterSectionHeader(title = "Time Period", icon = Icons.Outlined.DateRange)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                QuickDateChip(
                    label = "Last 7 Days",
                    isSelected = activeFilters.startDate == LocalDate.now().minusDays(7),
                    onClick = { onDateRangeSelected(LocalDate.now().minusDays(7), LocalDate.now()) }
                )
                QuickDateChip(
                    label = "This Month",
                    isSelected = activeFilters.startDate == LocalDate.now().withDayOfMonth(1),
                    onClick = { onDateRangeSelected(LocalDate.now().withDayOfMonth(1), LocalDate.now()) }
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.sm))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                DatePickerButton(
                    label = activeFilters.startDate?.format(DateTimeFormatter.ofPattern("dd MMM")) ?: "Start Date",
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.weight(1f)
                )
                DatePickerButton(
                    label = activeFilters.endDate?.format(DateTimeFormatter.ofPattern("dd MMM")) ?: "End Date",
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // Amount Range Section
            FilterSectionHeader(
                title = "Amount Range", 
                icon = Icons.Outlined.Payments,
                trailingValue = "₹${activeFilters.minAmount?.toInt() ?: databaseAmountRange.first.toInt()} - ₹${activeFilters.maxAmount?.toInt() ?: databaseAmountRange.second.toInt()}"
            )
            
            val minSlider = databaseAmountRange.first.toFloat()
            val maxSlider = databaseAmountRange.second.toFloat().coerceAtLeast(minSlider + 1f)
            
            RangeSlider(
                value = (activeFilters.minAmount?.toFloat() ?: minSlider)..(activeFilters.maxAmount?.toFloat() ?: maxSlider),
                onValueChange = { range ->
                    onAmountRangeChanged(range.start.toDouble(), range.endInclusive.toDouble())
                },
                valueRange = minSlider..maxSlider,
                colors = SliderDefaults.colors(
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    thumbColor = MaterialTheme.colorScheme.primary
                )
            )
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            androidx.compose.material3.Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
            ) {
                Text("Show Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun FilterSectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    trailingValue: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (trailingValue != null) {
            Spacer(modifier = Modifier.weight(1f))
            Text(trailingValue, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun QuickDateChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.labelLarge,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DatePickerButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    androidx.compose.material3.Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(contentAlignment = Alignment.CenterStart, modifier = Modifier.padding(horizontal = Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

