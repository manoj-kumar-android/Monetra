package com.monetra.presentation.screen.refundable

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.R
import com.monetra.domain.model.Refundable
import com.monetra.domain.model.RefundableStatus
import com.monetra.presentation.components.HelpIconButton
import com.monetra.presentation.component.SwipeToDeleteContainer
import com.monetra.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

private val fullDateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")

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

    val pagerState = rememberPagerState(pageCount = { RefundableFilter.entries.size })

    // Sync FROM ViewModel TO Pager
    LaunchedEffect(currentFilter) {
        val targetPage = RefundableFilter.entries.indexOf(currentFilter)
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
    val hapticAddClick = com.monetra.presentation.components.rememberHapticClick(onClick = onAddEntryClick)

    Scaffold(
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                com.monetra.presentation.component.MonetraSnackbar(snackbarData = data)
            }
        },
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
                onClick = hapticAddClick,
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
                val filter = RefundableFilter.entries[page]
                val pageRefundables = allRefundables.filter {
                    when (filter) {
                        RefundableFilter.ALL -> true
                        RefundableFilter.PENDING -> it.status == RefundableStatus.PENDING
                        RefundableFilter.OVERDUE -> it.status == RefundableStatus.OVERDUE
                        RefundableFilter.PAID -> it.status == RefundableStatus.PAID
                    }
                }

                if (pageRefundables.isEmpty()) {
                    RefundableEmptyState(
                        filter = filter,
                        onAddClick = onAddEntryClick
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(pageRefundables.size, key = { index -> pageRefundables[index].id }, contentType = { "refundable" }) { index ->
                            val item = pageRefundables[index]
                        
                        // Staggered entry animation
                        val isVisible = remember { mutableStateOf(false) }
                        LaunchedEffect(Unit) {
                            isVisible.value = true
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = isVisible.value,
                            enter = fadeIn(animationSpec = tween(600))
                        ) {
                            SwipeToDeleteContainer(
                                onDelete = { 
                                    viewModel.deleteRefundable(item)
                                    scope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Refundable entry deleted",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreRefundable(item)
                                        }
                                    }
                                },
                                title = "Delete Entry?",
                                message = "Are you sure you want to remove this refundable entry?"
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
        RefundableFilter.entries.forEach { filter ->
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
            .fillMaxWidth(),
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
                        text = item.dueDate.format(fullDateFormatter),
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (item.remindMe) {
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

@Composable
private fun RefundableEmptyState(
    filter: RefundableFilter,
    onAddClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_state_anim")
    
    // Floating animation for the emoji
    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_anim"
    )

    // Breathing animation for the background glow
    val scaleAnim by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale_anim"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(contentAlignment = Alignment.Center) {
            val tint = if (filter == RefundableFilter.PENDING) Color(0xFFFFCC00) else MaterialTheme.colorScheme.primary
            
            // Animated background glow
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer {
                        scaleX = scaleAnim
                        scaleY = scaleAnim
                        alpha = 0.15f
                    }
                    .background(tint, CircleShape)
            )
            
            // Animated main icon
            val iconSize = 84.dp
            Box(
                modifier = Modifier
                    .graphicsLayer { translationY = floatAnim }
                    .size(iconSize),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when(filter) {
                        RefundableFilter.ALL -> Icons.Default.ReceiptLong
                        RefundableFilter.PENDING -> Icons.Default.Handshake
                        RefundableFilter.OVERDUE -> Icons.Default.Alarm
                        RefundableFilter.PAID -> Icons.Default.TaskAlt
                    },
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    tint = tint
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xl))

        Text(
            text = when(filter) {
                RefundableFilter.ALL -> "No Money Lent or Borrowed"
                RefundableFilter.PENDING -> "All Settled Up!"
                RefundableFilter.OVERDUE -> "No Overdue Payments"
                RefundableFilter.PAID -> "No Paid Records"
            },
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 0.5.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(Spacing.xs))

        Text(
            text = when(filter) {
                RefundableFilter.ALL -> "You haven't lent or borrowed any money yet. Track them here."
                RefundableFilter.PENDING -> "You have no pending dues to receive or pay back."
                RefundableFilter.OVERDUE -> "Great! No one is running late on their payments."
                RefundableFilter.PAID -> "Settled entries will appear here for your records."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = Spacing.lg)
        )

        if (filter == RefundableFilter.ALL || filter == RefundableFilter.PENDING) {
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.alpha(0.7f)
            ) {
                Text(
                    text = "Tap ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp).background(MaterialTheme.colorScheme.primary, CircleShape).padding(2.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = " below to start",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
