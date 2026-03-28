package com.monetra.presentation.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.monetra.ui.theme.Spacing
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    title: String = "Delete?",
    message: String = "This action cannot be undone.",
    content: @Composable () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // Manual offset management to allow "staying" at the release position
    val offset = remember { Animatable(0f) }
    var itemWidth by remember { mutableFloatStateOf(0f) }

    if (showDialog) {
        MonetraDeleteDialog(
            title = title,
            message = message,
            onConfirm = {
                showDialog = false
                scope.launch {
                    // Animate to full dismiss before deleting
                    offset.animateTo(-itemWidth, tween(300))
                    onDelete()
                }
            },
            onDismiss = {
                showDialog = false
                scope.launch {
                    // Animate back to original state
                    offset.animateTo(0f, tween(300))
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { itemWidth = it.size.width.toFloat() }
    ) {
        // Background - revealed as we swipe
        val progress = if (itemWidth > 0) abs(offset.value) / itemWidth else 0f
        val isPastThreshold = progress > 0.35f

        val color by animateColorAsState(
            targetValue = if (isPastThreshold) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            label = "swipe_bg"
        )
        val iconScale by animateFloatAsState(
            targetValue = if (isPastThreshold) 1.2f else 0.8f,
            label = "icon_scale"
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(24.dp))
                .background(color),
            contentAlignment = Alignment.CenterEnd
        ) {
            Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier
                    .padding(end = Spacing.xl)
                    .scale(iconScale)
            )
        }

        // Foreground content - the draggable part
        Box(
            modifier = Modifier
                .offset { IntOffset(offset.value.roundToInt(), 0) }
                .fillMaxWidth()
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        // Only allow swiping from right (negative delta)
                        val newOffset = (offset.value + delta).coerceIn(-itemWidth, 0f)
                        scope.launch { offset.snapTo(newOffset) }
                    },
                    onDragStopped = { _ ->
                        if (progress > 0.35f) {
                            // Dialog shown, item stays at current release position
                            showDialog = true
                        } else {
                            // Snap back if threshold not reached
                            scope.launch {
                                offset.animateTo(0f, tween(300))
                            }
                        }
                    }
                )
        ) {
            content()
        }
    }
}

@Composable
fun MonetraDeleteDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(Spacing.xxl),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Cool Icon Header
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.error),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xl))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        letterSpacing = (-0.5).sp
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.sm))

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(Spacing.xxl))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Text(
                            "Cancel",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            "Delete",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
