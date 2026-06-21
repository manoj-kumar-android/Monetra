package com.monetra.core.ui.commonsheet

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.LocalOverscrollFactory
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.window.SecureFlagPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * Animation duration spec for [BottomSheetPopup].
 *
 * @property showDuration Duration of the show animation, in milliseconds.
 * @property dismissDuration Duration of the dismiss animation, in milliseconds.
 */
@Immutable
data class BottomSheetAnimationSpec(
    val showDuration: Int = DefaultBottomSheetPopupAnimationDuration,
    val dismissDuration: Int = DefaultBottomSheetPopupAnimationDuration,
)

/**
 * User interaction behavior for [BottomSheetPopup].
 *
 * @property draggable When `true`, the sheet body can be swiped down to dismiss. The drag
 *   gesture cooperates with nested scrolling, so a scrollable child hands the gesture off
 *   once it reaches the top of its scroll range.
 * @property enableDismissByInteraction When `true`, a scrim tap or a downward swipe can
 *   dismiss the sheet. When `false`, those interactions are ignored and the sheet can only
 *   be closed via code or back press (subject to [shouldDismissOnBackPress]).
 * @property shouldDismissOnBackPress When `true`, a back press triggers `onDismissRequest`.
 * @property securePolicy `FLAG_SECURE` policy applied to the popup window.
 */
@Immutable
data class BottomSheetBehavior(
    val draggable: Boolean = true,
    val enableDismissByInteraction: Boolean = true,
    val shouldDismissOnBackPress: Boolean = true,
    val securePolicy: SecureFlagPolicy = SecureFlagPolicy.Inherit,
)

/**
 * Visual theme for [BottomSheetPopup].
 *
 * @property scrimColor Color of the dim layer rendered behind the sheet. When left as
 *   [Color.Unspecified] (default), a dark or light fallback is chosen automatically based
 *   on the system dark-mode setting.
 * @property systemUiStyle Status / navigation bar appearance applied while the sheet is shown.
 */
@Immutable
data class BottomSheetTheme(
    val scrimColor: Color = Color.Unspecified,
    val systemUiStyle: SystemUiStyle = SystemUiStyle.SystemTheme,
) {
    /**
     * Status and navigation bar appearance applied while the sheet is shown.
     */
    enum class SystemUiStyle {
        /** Follow the device's current dark / light mode. */
        SystemTheme,

        /** Force light status / navigation bar icons. */
        LightTheme,

        /** Force dark status / navigation bar icons. */
        DarkTheme,

        /** Hide the system bars (immersive). */
        ImmersiveMode,
    }
}

/**
 * Shows a bottom sheet popup that overlays the host content inside a Dialog window.
 *
 * The sheet animates in from the bottom and is dismissed by any of the following, depending
 * on [behavior]:
 * - A back press (when [BottomSheetBehavior.shouldDismissOnBackPress] is `true`).
 * - A tap on the scrim (when [BottomSheetBehavior.enableDismissByInteraction] is `true`).
 * - A downward swipe on the sheet body (when both [BottomSheetBehavior.draggable] and
 *   [BottomSheetBehavior.enableDismissByInteraction] are `true`).
 *
 * Each of these paths invokes [onDismissRequest]. The caller is responsible for stopping
 * the composition of `BottomSheetPopup` in response — typically by flipping a `Boolean`
 * state held by the host.
 *
 * Sheet content is rendered without any default surface treatment (no rounded corners,
 * no background fill). Wrap [bottomSheetContent] with the visual styling you want.
 *
 * @param animationSpec Show / dismiss animation durations.
 * @param behavior User interaction behavior (drag, dismiss-by-interaction, back press,
 *   secure flag).
 * @param theme Visual theme (scrim color, system UI style).
 * @param onDismissRequest Invoked when the sheet should be dismissed. The host is expected
 *   to react by removing this composable from composition.
 * @param bottomSheetContent The sheet body. Runs in [BottomSheetPopupScope] so it can call
 *   [BottomSheetPopupScope.dismiss] to programmatically close the sheet.
 */
@Composable
fun BottomSheetPopup(
    animationSpec: BottomSheetAnimationSpec = remember { BottomSheetAnimationSpec() },
    behavior: BottomSheetBehavior = remember { BottomSheetBehavior() },
    theme: BottomSheetTheme = remember { BottomSheetTheme() },
    onDismissRequest: () -> Unit,
    bottomSheetContent: @Composable BottomSheetPopupScope.() -> Unit = {},
) {
    val resolvedScrimColor = theme.scrimColor.takeOrElse {
        if (isSystemInDarkTheme()) darkBottomSheetScrimColor else lightBottomSheetScrimColor
    }
    val internalProperties = remember(behavior, theme) {
        BottomSheetProperties(
            securePolicy = behavior.securePolicy,
            systemUiStyle = theme.systemUiStyle,
            shouldDismissOnBackPress = behavior.shouldDismissOnBackPress,
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val bottomSheetScope = remember {
        BottomSheetPopupScope(
            coroutineScope = coroutineScope,
            dismissAnimationDuration = animationSpec.dismissDuration,
            dismissRequest = onDismissRequest
        )
    }

    SideEffect {
        bottomSheetScope.enableDismissByInteraction = behavior.enableDismissByInteraction
    }
    BottomSheetContainerDialog(
        properties = internalProperties,
        onDismissRequest = { bottomSheetScope.dismiss() }
    ) {
        BottomSheetBackScrim(
            modifier = Modifier
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = {
                        if (bottomSheetScope.enableDismissByInteraction) bottomSheetScope.dismiss()
                    }
                ),
            color = resolvedScrimColor,
            bottomSheetScope = bottomSheetScope,
            showAnimationDuration = animationSpec.showDuration,
            dismissAnimationDuration = animationSpec.dismissDuration
        )
        Column(
            Modifier
                .safeDrawingPadding()
                .fillMaxHeight()
        ) {
            Spacer(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
            AnimatedVisibility(
                visibleState = bottomSheetScope.popupVisible,
                enter = slideInVertically(
                    animationSpec = tween(animationSpec.showDuration),
                    initialOffsetY = { it }
                ) + fadeIn(animationSpec = tween(animationSpec.showDuration)),
                exit = slideOutVertically(
                    animationSpec = tween(animationSpec.dismissDuration),
                    targetOffsetY = { it }
                ) + fadeOut(animationSpec = tween(animationSpec.dismissDuration))
            ) {
                if (behavior.draggable) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        CompositionLocalProvider(LocalOverscrollFactory provides null) {
                            Box(modifier = with(bottomSheetScope) {
                                Modifier.bottomSheetPopupDraggable(
                                    true
                                )
                            }) {
                                BottomSheetStyleContainer {
                                    bottomSheetScope.bottomSheetContent()
                                }
                            }
                        }
                    } else {
                        Box(modifier = with(bottomSheetScope) {
                            Modifier.bottomSheetPopupDraggable(
                                true
                            )
                        }) {
                            BottomSheetStyleContainer {
                                bottomSheetScope.bottomSheetContent()
                            }
                        }
                    }
                } else {
                    BottomSheetStyleContainer {
                        bottomSheetScope.bottomSheetContent()
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomSheetStyleContainer(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        content()
    }
}

/**
 * Receiver scope for [BottomSheetPopup] content.
 *
 * Use [dismiss] to programmatically close the sheet from inside its content lambda.
 */
class BottomSheetPopupScope(
    private val coroutineScope: CoroutineScope,
    private val dismissAnimationDuration: Int,
    private val dismissRequest: () -> Unit
) {
    internal val popupVisible = MutableTransitionState(false).apply { targetState = true }
    internal var enableDismissByInteraction: Boolean = true

    /**
     * Dismisses the sheet and invokes the host's `onDismissRequest`.
     *
     * @param withAnimation When `true` (default), plays the dismiss animation before
     *   invoking `onDismissRequest`. When `false`, dismisses immediately without animation.
     */
    fun dismiss(withAnimation: Boolean = true) {
        coroutineScope.launch {
            popupVisible.targetState = false
            if (withAnimation) {
                delay(dismissAnimationDuration + 50L)
            }
            dismissRequest()
        }
    }

    internal fun Modifier.bottomSheetPopupDraggable(
        nestedScrollEnabled: Boolean = true,
        animationDuration: Int = DefaultBottomSheetPopupAnimationDuration
    ) = swipeDownClosable(
        nestedScrollEnabled = nestedScrollEnabled,
        animationDuration = animationDuration,
        isEnableClose = {
            enableDismissByInteraction
        },
        closeAction = {
            dismiss(withAnimation = true)
        }
    )
}

@Composable
private fun BottomSheetBackScrim(
    bottomSheetScope: BottomSheetPopupScope,
    showAnimationDuration: Int,
    dismissAnimationDuration: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        modifier = modifier,
        visibleState = bottomSheetScope.popupVisible,
        enter = fadeIn(tween(showAnimationDuration)),
        exit = fadeOut(tween(dismissAnimationDuration))
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(color = color)
        )
    }
}


fun Modifier.swipeDownClosable(
    nestedScrollEnabled: Boolean = false,
    animationDuration: Int = 250,
    closeThreshold: Float = 0.2f,
    isEnableClose: () -> Boolean = { true },
    closeAction: () -> Unit = {}
) = composed {
    var maxHeight by remember { mutableFloatStateOf(0f) }
    var offset by remember { mutableFloatStateOf(0f) }

    fun performDrag(available: Float): Float {
        val consumed = if (available > 0) {
            available
        } else {
            if (offset > -available) available else -offset
        }
        offset = max(0f, offset + available)
        return consumed
    }

    suspend fun performFling(velocity: Float) {
        // Dismiss when the sheet has been dragged down past closeThreshold (default 20%)
        // of its max height AND the fling velocity points downward.
        val isCloseCondition =
            isEnableClose() && ((offset > maxHeight * closeThreshold) && (velocity > 0f))
        Animatable(offset).animateTo(
            targetValue = if (isCloseCondition) maxHeight else 0f,
            animationSpec = tween(animationDuration, easing = LinearOutSlowInEasing)
        ) {
            offset = this.value
        }
        if (isCloseCondition) closeAction()
    }

    val nestedScrollConnection = remember(nestedScrollEnabled) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (nestedScrollEnabled && available.y < 0 && source == NestedScrollSource.UserInput) {
                    Offset(0f, performDrag(available.y))
                } else {
                    super.onPreScroll(available, source)
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                return if (nestedScrollEnabled && source == NestedScrollSource.UserInput) {
                    Offset(0f, performDrag(available.y))
                } else {
                    super.onPostScroll(consumed, available, source)
                }
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (offset <= 0f) return super.onPreFling(available)
                performFling(available.y)
                return available
            }
        }
    }

    return@composed this
        .clipToBounds()
        .onGloballyPositioned {
            maxHeight = it.size.height.toFloat()
        }
        .offset {
            IntOffset(
                0,
                offset
                    .toInt()
                    .coerceAtLeast(0)
            )
        }
        .nestedScroll(nestedScrollConnection)
        .draggable(
            state = rememberDraggableState(
                onDelta = {
                    performDrag(it)
                }
            ),
            orientation = Orientation.Vertical,
            onDragStopped = { velocity ->
                performFling(velocity)
            }
        )
    /*
    .swipeDownDraggable(
        onDelta = { delta ->
            performDrag(delta)
        },
        onDragStopped = { velocity ->
            performFling(velocity)
        }
    )*/
}

// Compose 1.8.0 bug workaround.
// When ComposeFoundationFlags.DragGesturePickUpEnabled is true, scrolling a child scrollable
// in a direction different from its own orientation makes draggable fire a short
// onDragStarted - onDelta - onDragStopped sequence: onDelta(0f) is called once and the
// gesture is canceled. This worked correctly before 1.8.0.
// https://issuetracker.google.com/issues/416832576
/*
private fun Modifier.swipeDownDraggable(
    onDelta: (delta: Float) -> Unit,
    onDragStopped: suspend (velocity: Float) -> Unit
): Modifier = composed {
    var draggableCount by remember { mutableIntStateOf(0) }
    var latestDraggableValue by remember { mutableFloatStateOf(0f) }

    fun initDraggableValues() {
        draggableCount = 0
        latestDraggableValue = 0f
    }

    return@composed draggable(
        state = rememberDraggableState(
            onDelta = {
                onDelta(it)
                latestDraggableValue = it
                draggableCount++
            }
        ),
        orientation = Orientation.Vertical,
        onDragStarted = {
            initDraggableValues()
        },
        onDragStopped = { velocity ->
            if (latestDraggableValue != 0f && draggableCount > 1 && velocity != 0f) {
                onDragStopped(velocity)
            }
        }
    )
}
*/