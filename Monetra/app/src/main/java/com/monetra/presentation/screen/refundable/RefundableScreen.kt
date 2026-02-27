package com.monetra.presentation.screen.refundable

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.R
import com.monetra.domain.model.Refundable
import com.monetra.domain.model.RefundableStatus
import com.monetra.presentation.components.HelpIconButton
import com.monetra.ui.theme.Spacing
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.launch
import com.monetra.presentation.screen.monthly_expense.SwipeToDeleteContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundableScreen(
    onAddEntryClick: () -> Unit,
    onEntryClick: (Long) -> Unit,
    onNavigateToHelp: () -> Unit,
    viewModel: RefundableViewModel = hiltViewModel()
) {
    val allRefundables by viewModel.allRefundables.collectAsStateWithLifecycle()
    val currentFilter by viewModel.filter.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(pageCount = { RefundableFilter.values().size })

    // Sync FROM ViewModel TO Pager
    LaunchedEffect(currentFilter) {
        val targetPage = RefundableFilter.values().indexOf(currentFilter)
        if (pagerState.currentPage != targetPage) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // Sync FROM Pager TO ViewModel (only when settled)
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            val targetFilter = RefundableFilter.values()[page]
            if (targetFilter != currentFilter) {
                viewModel.setFilter(targetFilter)
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.refundable_title),
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = Spacing.sm)
                    )
                },
                actions = {
                    HelpIconButton(onClick = onNavigateToHelp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddEntryClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(stringResource(R.string.give_money)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filters
            RefundableFilterBar(
                selectedFilter = currentFilter,
                onFilterSelected = viewModel::setFilter
            )

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->
                val filter = RefundableFilter.values()[page]
                val pageRefundables = allRefundables.filter {
                    when (filter) {
                        RefundableFilter.ALL -> true
                        RefundableFilter.PENDING -> it.status == RefundableStatus.PENDING
                        RefundableFilter.OVERDUE -> it.status == RefundableStatus.OVERDUE
                        RefundableFilter.PAID -> it.status == RefundableStatus.PAID
                    }
                }

                if (pageRefundables.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No entries found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(pageRefundables.size, key = { index -> pageRefundables[index].id }) { index ->
                            val item = pageRefundables[index]
                        
                        // Staggered entry animation
                        val isVisible = remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            kotlinx.coroutines.delay(index * 50L)
                            isVisible.value = true
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = isVisible.value,
                            enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
                                initialOffsetY = { it / 2 },
                                animationSpec = tween(600)
                            )
                        ) {
                            SwipeToDeleteContainer(
                                onDelete = { 
                                    viewModel.deleteRefundable(item)
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Refundable entry deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreRefundable(item)
                                        }
                                    }
                                }
                            ) {
                                RefundableItemRow(
                                    item = item,
                                    onClick = { onEntryClick(item.id) }
                                )
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}
}


@Composable
private fun RefundableFilterBar(
    selectedFilter: RefundableFilter,
    onFilterSelected: (RefundableFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.sm),
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
    ) {
        RefundableFilter.values().forEach { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterSelected(filter) },
                label = { 
                    Text(
                        text = when(filter) {
                            RefundableFilter.ALL -> stringResource(R.string.all)
                            RefundableFilter.PENDING -> stringResource(R.string.pending)
                            RefundableFilter.OVERDUE -> stringResource(R.string.overdue)
                            RefundableFilter.PAID -> stringResource(R.string.paid)
                        }
                    ) 
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Composable
private fun RefundableItemRow(
    item: Refundable,
    onClick: () -> Unit
) {
    val statusColor = when (item.status) {
        RefundableStatus.PAID -> Color(0xFF34C759)
        RefundableStatus.OVERDUE -> MaterialTheme.colorScheme.error
        RefundableStatus.PENDING -> MaterialTheme.colorScheme.primary
    }

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer {
                // Subtle lift on interactions if we were using a state-based modifier
            },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(statusColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(item.status) {
                        RefundableStatus.PAID -> Icons.Default.CheckCircle
                        RefundableStatus.OVERDUE -> Icons.Default.ErrorOutline
                        RefundableStatus.PENDING -> Icons.Default.HourglassBottom
                    },
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(Spacing.md))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.personName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.dueDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.remindMe || item.sendSmsReminder) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            Icons.Default.NotificationsActive,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹" + "%,.0f".format(item.amount),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp
                    ),
                    color = statusColor
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = item.status.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
