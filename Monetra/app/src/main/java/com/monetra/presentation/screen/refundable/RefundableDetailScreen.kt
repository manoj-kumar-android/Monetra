package com.monetra.presentation.screen.refundable

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.R
import com.monetra.domain.model.RefundableStatus
import com.monetra.ui.theme.Spacing
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt
import androidx.core.net.toUri

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RefundableDetailScreen(
    id: Long,
    onNavigateBack: () -> Unit,
    onEditClick: (Long) -> Unit,
    viewModel: RefundableDetailViewModel = hiltViewModel()
) {
    LaunchedEffect(id) {
        viewModel.loadRefundable(id)
    }
    val refundable by viewModel.refundable.collectAsStateWithLifecycle()
    val isDeleted by viewModel.isDeleted.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(isDeleted) {
        if (isDeleted) {
            onNavigateBack()
        }
    }


    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Entry?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this entry? This action can be undone from the main screen.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteRefundable()
                    }
                ) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.entry_details)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = { refundable?.let { onEditClick(it.id) } }) {
                        Icon(Icons.Default.Edit, contentDescription = null)
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        refundable?.let { item ->
            val statusColor = when (item.status) {
                RefundableStatus.PAID -> Color(0xFF34C759)
                RefundableStatus.OVERDUE -> MaterialTheme.colorScheme.error
                RefundableStatus.PENDING -> MaterialTheme.colorScheme.primary
            }

            var isVisible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { isVisible = true }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Spacing.lg, vertical = Spacing.xl),
                verticalArrangement = Arrangement.spacedBy(Spacing.xl)
            ) {
                // Header Card: Amount & Status with Gradient
                androidx.compose.animation.AnimatedVisibility(
                    visible = isVisible,
                    enter = androidx.compose.animation.fadeIn() + androidx.compose.animation.slideInVertically()
                ) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        color = Color.Transparent, // Using Brush instead
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            statusColor.copy(alpha = 0.15f),
                                            statusColor.copy(alpha = 0.05f)
                                        )
                                    )
                                )
                                .border(1.dp, statusColor.copy(alpha = 0.2f), RoundedCornerShape(32.dp))
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = stringResource(R.string.amount).uppercase(),
                                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp),
                                    color = statusColor.copy(alpha = 0.6f)
                                )
                                Spacer(modifier = Modifier.height(Spacing.xs))
                                Text(
                                    text = "₹" + "%,.0f".format(item.amount),
                                    style = MaterialTheme.typography.displayLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 52.sp
                                    ),
                                    color = statusColor
                                )
                                Spacer(modifier = Modifier.height(Spacing.md))
                                Surface(
                                    shape = CircleShape,
                                    color = statusColor,
                                    shadowElevation = 4.dp
                                ) {
                                    Text(
                                        text = item.status.name,
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Actions
                if (!item.isPaid) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        QuickActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Call,
                            label = "Call",
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL,
                                    "tel:${item.phoneNumber}".toUri())
                                context.startActivity(intent)
                            }
                        )
                        QuickActionButton(
                            modifier = Modifier.weight(1f),
                            icon = Icons.Default.Sms,
                            label = "SMS",
                            onClick = {
                                val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                    data = Uri.parse("smsto:${item.phoneNumber}")
                                    putExtra("sms_body", "Hi ${item.personName}, a friendly reminder about the ₹${item.amount} due. - Sent via Monetra")
                                }
                                context.startActivity(smsIntent)
                            }
                        )
                    }
                }

                // Details Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        DetailItem(icon = Icons.Default.Person, label = stringResource(R.string.person_name), value = item.personName)
                        DetailItem(icon = Icons.Default.Phone, label = stringResource(R.string.phone_number), value = item.phoneNumber)
                        
                        Divider(
                            modifier = Modifier.alpha(0.3f),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        
                        DetailItem(icon = Icons.Default.CalendarToday, label = stringResource(R.string.given_date), value = item.givenDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                        DetailItem(icon = Icons.Default.Event, label = stringResource(R.string.due_date), value = item.dueDate.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")))
                        
                        if (!item.note.isNullOrBlank()) {
                            DetailItem(icon = Icons.Default.StickyNote2, label = stringResource(R.string.note), value = item.note)
                        }
                    }
                }

                // Main Completion Button
                SwipeToPaidButton(
                    isPaid = item.isPaid,
                    onConfirm = { viewModel.markAsPaid(true) },
                    onReopen = { viewModel.markAsPaid(false) },
                    statusColor = statusColor,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(Spacing.xl))
            }
        }
    }
}

@Composable
private fun SwipeToPaidButton(
    isPaid: Boolean,
    onConfirm: () -> Unit,
    onReopen: () -> Unit,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    var buttonWidth by remember { mutableFloatStateOf(0f) }
    val thumbSize = 56.dp
    val thumbSizePx = with(androidx.compose.ui.platform.LocalDensity.current) { thumbSize.toPx() }
    val swipeOffset = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    var isSuccessAnimating by remember { mutableStateOf(false) }

    LaunchedEffect(isPaid) {
        if (!isPaid) {
            swipeOffset.snapTo(0f)
            isSuccessAnimating = false
        }
    }

    AnimatedContent(
        targetState = isPaid to statusColor,
        transitionSpec = {
            (fadeIn(animationSpec = tween(500)) + slideInVertically()).togetherWith(fadeOut(animationSpec = tween(300)))
        }, label = "buttonState"
    ) { (paid, animColor) ->
        if (paid) {
            Button(
                onClick = onReopen,
                modifier = modifier.height(64.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(Icons.Default.Undo, contentDescription = null)
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text("Reopen Entry", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        } else {
            Box(
                modifier = modifier
                    .height(64.dp)
                    .fillMaxWidth()
                    .onSizeChanged { buttonWidth = it.width.toFloat() }
                    .clip(RoundedCornerShape(20.dp))
                    .background(animColor.copy(alpha = 0.1f))
                    .border(1.dp, animColor.copy(alpha = 0.2f), RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.CenterStart
            ) {
                // Background fill with Gradient and Rounded Corners (Pill shape)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(with(androidx.compose.ui.platform.LocalDensity.current) { (swipeOffset.value + thumbSizePx).toDp() })
                        .padding(2.dp) // Slight padding to see the parent border
                        .clip(RoundedCornerShape(18.dp)) // Refined rounded edge
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    animColor,
                                    animColor.copy(alpha = 0.85f)
                                )
                            )
                        )
                )

                // Hint Text with subtle pulse/shimmer
                val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "shimmer")
                val pulseAlpha by infiniteTransition.animateFloat(
                    initialValue = 0.6f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "pulse"
                )

                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    val triggerTooltip = (buttonWidth - thumbSizePx) * 0.9f
                    val isNearEndTool = swipeOffset.value >= triggerTooltip
                    
                    Text(
                        text = if (isSuccessAnimating) "Paid Successfully!" else if (isNearEndTool) "Release to Pay!" else "Swipe to Mark Paid",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = if (isNearEndTool || isSuccessAnimating) 0.5.sp else 0.sp
                        ),
                        color = if (isSuccessAnimating) Color.White else if (swipeOffset.value > (buttonWidth / 2)) Color.White else animColor,
                        modifier = Modifier.graphicsLayer {
                            scaleX = if (isSuccessAnimating) 1.2f else if (isNearEndTool) 1.05f else 1f
                            scaleY = if (isSuccessAnimating) 1.2f else if (isNearEndTool) 1.05f else 1f
                            alpha = if (isSuccessAnimating) 1f else ((1f - (swipeOffset.value / (buttonWidth * 0.7f))).coerceIn(0f, 1f)) * pulseAlpha
                        }
                    )
                }

                // The Thumb (Draggable Part)
                val isNearEndThumb = swipeOffset.value >= (buttonWidth - thumbSizePx) * 0.8f
                Surface(
                    modifier = Modifier
                        .padding(4.dp)
                        .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                        .size(thumbSize)
                        .pointerInput(buttonWidth) {
                            if (buttonWidth == 0f) return@pointerInput
                            val maxOffset = buttonWidth - thumbSizePx - 8.dp.toPx()
                            detectHorizontalDragGestures(
                                onHorizontalDrag = { change, dragAmount ->
                                    change.consume()
                                    coroutineScope.launch {
                                        val newOffset = (swipeOffset.value + dragAmount).coerceIn(0f, maxOffset)
                                        swipeOffset.snapTo(newOffset)
                                    }
                                },
                                onDragEnd = {
                                    coroutineScope.launch {
                                        if (swipeOffset.value >= maxOffset * 0.9f) {
                                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                            isSuccessAnimating = true
                                            swipeOffset.animateTo(maxOffset, spring(
                                                dampingRatio = Spring.DampingRatioNoBouncy,
                                                stiffness = Spring.StiffnessMedium
                                            ))
                                            kotlinx.coroutines.delay(600)
                                            onConfirm()
                                            isSuccessAnimating = false
                                        } else {
                                            swipeOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMediumLow))
                                        }
                                    }
                                }
                            )
                        },
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(2.dp, animColor.copy(alpha = 0.8f)), // Blue border from all sides
                    shadowElevation = if (isNearEndThumb) 8.dp else 2.dp,
                    tonalElevation = if (isNearEndThumb) 4.dp else 0.dp
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = animColor,
                            modifier = Modifier.graphicsLayer {
                                val maxOff = maxOf(1f, buttonWidth - thumbSizePx)
                                val progress = (swipeOffset.value / maxOff)
                                alpha = 1f - progress
                                rotationZ = progress * 90f // Rotate while swiping
                                scaleX = 1f - progress * 0.5f
                                scaleY = 1f - progress * 0.5f
                            }
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = animColor,
                            modifier = Modifier.graphicsLayer {
                                val trigger = (buttonWidth - thumbSizePx) * 0.9f
                                val active = swipeOffset.value >= trigger
                                alpha = if (active) 1f else 0f
                                scaleX = if (active) 1.3f else 0.7f
                                scaleY = if (active) 1.3f else 0.7f
                                rotationZ = if (active) 0f else -45f
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun QuickActionButton(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(58.dp),
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(Spacing.md))
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
    }
}

@Composable
private fun DetailItem(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(42.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(Spacing.lg))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    letterSpacing = 0.5.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 17.sp
                ),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


