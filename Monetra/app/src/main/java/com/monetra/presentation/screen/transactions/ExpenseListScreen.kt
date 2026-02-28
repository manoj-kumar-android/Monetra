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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.presentation.screen.transactions.components.MonthlySummaryCard
import com.monetra.presentation.screen.transactions.components.TransactionRow
import com.monetra.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import com.monetra.presentation.components.HelpIconButton
import androidx.compose.ui.res.stringResource
import com.monetra.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    snackbarHostState: SnackbarHostState,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToHelp: () -> Unit,
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()

    // Every time we enter this screen via the Tab, reset to current month
    androidx.compose.runtime.DisposableEffect(Unit) {
        viewModel.onResetMonth()
        onDispose {}
    }

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is ExpenseEvent.NavigateToEdit -> onNavigateToEdit(event.transactionId)
                ExpenseEvent.NavigateToAdd -> onNavigateToAdd()
                is ExpenseEvent.ShowUndoSnackbar -> {
                    coroutineScope.launch {
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
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.transactions_title),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    HelpIconButton(onClick = onNavigateToHelp)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_transaction_cd))
            }
        }
    ) { paddingValues ->
        val pagerState = rememberPagerState(
            initialPage = Int.MAX_VALUE / 2,
            pageCount = { Int.MAX_VALUE }
        )

        val initialMonth = remember { YearMonth.now() }
        val scope = rememberCoroutineScope()

        // Derive month from pager for instant header updates during swipe
        val visibleMonth = remember(pagerState.currentPage) {
            initialMonth.plusMonths((pagerState.currentPage - (Int.MAX_VALUE / 2)).toLong())
        }

        // Sync FROM Pager TO ViewModel only when user has fully settled on a page
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.settledPage }.collect { page ->
                val monthsOffset = page - (Int.MAX_VALUE / 2)
                val targetMonth = initialMonth.plusMonths(monthsOffset.toLong())
                viewModel.onMonthSelected(targetMonth)
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            MonthSelector(
                selectedMonth = visibleMonth,
                isCurrentMonth = visibleMonth == YearMonth.now(),
                onMonthSelected = { month ->
                    val targetOffset = java.time.temporal.ChronoUnit.MONTHS.between(initialMonth, month).toInt()
                    val targetPage = (Int.MAX_VALUE / 2) + targetOffset
                    if (pagerState.currentPage != targetPage) {
                        scope.launch {
                            pagerState.animateScrollToPage(targetPage)
                        }
                    }
                },
                onResetMonth = {
                    if (pagerState.currentPage != Int.MAX_VALUE / 2) {
                        scope.launch {
                            pagerState.animateScrollToPage(Int.MAX_VALUE / 2)
                        }
                    }
                    viewModel.onResetMonth()
                }
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top,
                beyondViewportPageCount = 1
            ) { page ->
                val pageMonth = remember(page) {
                    initialMonth.plusMonths((page - (Int.MAX_VALUE / 2)).toLong())
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (val state = uiState) {
                        is ExpenseUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                        is ExpenseUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                        is ExpenseUiState.Success -> {
                            // Only show content if this page represents the currently selected month
                            if (pageMonth == state.selectedMonth) {
                                PullToRefreshBox(
                                    isRefreshing = state.isRefreshing,
                                    onRefresh = viewModel::refresh,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    TransactionListContent(
                                        state = state,
                                        onFilterSelected = viewModel::onFilterSelected,
                                        onTransactionClick = onNavigateToEdit,
                                        onDeleteClick = viewModel::onDeleteClick
                                    )
                                }
                            } else {
                                // While swiping, show a loader for the month being loaded
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }
                        }
                    }
                }
            }
        }
    }
    }


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TransactionListContent(
    state: ExpenseUiState.Success,
    onFilterSelected: (TransactionFilter) -> Unit,
    onTransactionClick: (Long) -> Unit,
    onDeleteClick: (Long) -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(start = Spacing.lg, end = Spacing.lg, top = Spacing.sm, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs)
    ) {
        item {
            Column {
                MonthlySummaryCard(summary = state.summary)
                Spacer(modifier = Modifier.height(Spacing.lg))
                FilterRow(activeFilter = state.activeFilter, onFilterSelected = onFilterSelected)
                Spacer(modifier = Modifier.height(Spacing.md))
            }
        }

        if (state.groupedTransactions.isEmpty()) {
            item {
                EmptyState()
            }
        } else {
            state.groupedTransactions.forEach { (dateHeader, transactions) ->
                stickyHeader {
                    DateHeader(header = dateHeader)
                }

                items(transactions, key = { it.id }) { transaction ->
                    com.monetra.presentation.component.SwipeToDeleteContainer(
                        onDelete = { onDeleteClick(transaction.id) },
                        title = stringResource(R.string.delete_transaction_title),
                        message = stringResource(R.string.delete_transaction_msg)
                    ) {
                        TransactionRow(
                            item = transaction,
                            onClick = { onTransactionClick(transaction.id) },
                            onDelete = { onDeleteClick(transaction.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(Spacing.md))
                }
            }
        }
    }
}

@Composable
private fun DateHeader(header: String) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = header.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = Spacing.sm)
        )
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 100.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\uD83D\uDCB8",
            style = MaterialTheme.typography.displayLarge
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(
            text = stringResource(R.string.no_transactions_found),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = stringResource(R.string.start_tracking_instruction),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.xl)
        )
    }
}

@Composable
private fun MonthSelector(
    selectedMonth: YearMonth,
    isCurrentMonth: Boolean,
    onMonthSelected: (YearMonth) -> Unit,
    onResetMonth: () -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("MMMM yyyy")
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { onMonthSelected(selectedMonth.minusMonths(1)) }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.previous_month_cd))
                }

                // Show "Today" button only if not in current month
                androidx.compose.animation.AnimatedVisibility(
                    visible = !isCurrentMonth,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.expandHorizontally(),
                    exit = androidx.compose.animation.fadeOut() + androidx.compose.animation.shrinkHorizontally()
                ) {
                    TextButton(
                        onClick = onResetMonth,
                        contentPadding = PaddingValues(horizontal = Spacing.md),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(stringResource(R.string.today), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }
            }

            Text(
                text = selectedMonth.format(formatter),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            IconButton(onClick = { onMonthSelected(selectedMonth.plusMonths(1)) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = stringResource(R.string.next_month_cd))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterRow(activeFilter: TransactionFilter, onFilterSelected: (TransactionFilter) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        TransactionFilter.entries.forEach { filter ->
            FilterChip(
                selected = activeFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { Text(filter.name.lowercase().replaceFirstChar { it.uppercase() }) },
                shape = RoundedCornerShape(16.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                ),
                border = FilterChipDefaults.filterChipBorder(enabled = true, selected = activeFilter == filter)
            )
        }
    }
}

