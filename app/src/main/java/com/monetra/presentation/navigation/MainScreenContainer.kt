package com.monetra.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monetra.presentation.screen.add_edit.AddEditExpenseScreen
import com.monetra.presentation.screen.transactions.ExpenseListScreen
import kotlinx.serialization.Serializable

sealed interface BottomNavScreen {
    @Serializable data object Dashboard    : BottomNavScreen
    @Serializable data object Transactions : BottomNavScreen
    @Serializable data object Refundable   : BottomNavScreen
    @Serializable data object Summary      : BottomNavScreen
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: BottomNavScreen
)

// ── Dimensions ───────────────────────────────────────────────────────────────
private val BottomBarHeight  = 72.dp
private val FabSize          = 58.dp
private val NotchDepth       = 32.dp
private val NotchWidth       = 88.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenContainer(
    isTopLevel: Boolean = true,
    initialTab: String? = null,
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onManageBudgetsClick: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToInvestments: () -> Unit,
    onNavigateToFixedExpenses: () -> Unit,
    onNavigateToHelp: (String) -> Unit,
    onNavigateToSimulator: () -> Unit,
    onNavigateToAddRefundable: () -> Unit,
    onNavigateToEditRefundable: (Long) -> Unit,
    onNavigateToRefundableDetails: (Long) -> Unit,
    onNavigateToSavings: () -> Unit,
    onNavigateToWelcome: () -> Unit
) {

    var selectedTabStr by rememberSaveable { mutableStateOf(initialTab ?: "Dashboard") }
    val selectedTab = remember(selectedTabStr) {
        when (selectedTabStr) {
            "Transactions" -> BottomNavScreen.Transactions
            "Refundable"   -> BottomNavScreen.Refundable
            "Summary"      -> BottomNavScreen.Summary
            else           -> BottomNavScreen.Dashboard
        }
    }

    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var editTransactionId       by remember { mutableStateOf<Long?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    fun openAddSheet() { editTransactionId = null; showAddTransactionSheet = true }
    fun openEditSheet(id: Long) { editTransactionId = id; showAddTransactionSheet = true }

    BackHandler(enabled = isTopLevel && selectedTab != BottomNavScreen.Dashboard) {
        selectedTabStr = "Dashboard"
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val leftItems = listOf(
        BottomNavItem("Dashboard",    Icons.Default.Home,               BottomNavScreen.Dashboard),
        BottomNavItem("Transactions", Icons.AutoMirrored.Filled.List,   BottomNavScreen.Transactions)
    )
    val rightItems = listOf(
        BottomNavItem("Refundable",   Icons.Default.PriceCheck,         BottomNavScreen.Refundable),
        BottomNavItem("Portfolio",    Icons.Default.Star,               BottomNavScreen.Summary)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            CutoutBottomBar(
                leftItems  = leftItems,
                rightItems = rightItems,
                selectedTab = selectedTab,
                onTabSelected = { selectedTabStr = it.route.javaClass.simpleName },
                onFabClick = { openAddSheet() }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                com.monetra.presentation.component.MonetraSnackbar(snackbarData = data)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            when (selectedTab) {
                BottomNavScreen.Dashboard -> {
                    com.monetra.presentation.screen.dashboard.DashboardScreen(
                        onNavigateToAdd          = { openAddSheet() },
                        onNavigateToEdit         = { openEditSheet(it) },
                        onNavigateToSettings     = onNavigateToSettings,
                        onManageBudgetsClick     = onManageBudgetsClick,
                        onNavigateToFixedExpenses = onNavigateToFixedExpenses,
                        onNavigateToSimulator    = onNavigateToSimulator,
                        onNavigateToHelp         = { onNavigateToHelp("DASHBOARD") },
                        onSeeAllTransactions     = { selectedTabStr = "Transactions" },
                        onNavigateToWelcome      = onNavigateToWelcome
                    )
                }
                BottomNavScreen.Transactions -> {
                    ExpenseListScreen(
                        snackbarHostState = snackbarHostState,
                        onNavigateToAdd  = { openAddSheet() },
                        onNavigateToEdit = { openEditSheet(it) },
                        onNavigateToHelp = { onNavigateToHelp("TRANSACTIONS") }
                    )
                }
                BottomNavScreen.Refundable -> {
                    com.monetra.presentation.screen.refundable.RefundableScreen(
                        onAddEntryClick        = onNavigateToAddRefundable,
                        onEntryClick           = onNavigateToRefundableDetails,
                        onNavigateToHelp       = { onNavigateToHelp("REFUNDABLE") }
                    )
                }
                BottomNavScreen.Summary -> {
                    com.monetra.presentation.screen.portfolio.PortfolioScreen(
                        onNavigateToSettings    = onNavigateToSettings,
                        onNavigateToLoans       = onNavigateToLoans,
                        onNavigateToInvestments = onNavigateToInvestments,
                        onNavigateToSavings     = onNavigateToSavings
                    )
                }
            }
        }
    }

    // ── Add / Edit Bottom Sheet ───────────────────────────────────────────────
    if (showAddTransactionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddTransactionSheet = false; editTransactionId = null },
            sheetState       = sheetState,
            containerColor   = MaterialTheme.colorScheme.surface,
            dragHandle       = null,
            sheetGesturesEnabled = !imeVisible,
            shape            = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            AddEditExpenseScreen(
                transactionId  = editTransactionId,
                isSheet        = true,
                onNavigateBack = { showAddTransactionSheet = false; editTransactionId = null }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// PREMIUM CUTOUT BOTTOM BAR
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun CutoutBottomBar(
    leftItems: List<BottomNavItem>,
    rightItems: List<BottomNavItem>,
    selectedTab: BottomNavScreen,
    onTabSelected: (BottomNavItem) -> Unit,
    onFabClick: () -> Unit
) {
    val surfaceColor   = MaterialTheme.colorScheme.surface
    val primaryColor   = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val haptic = LocalHapticFeedback.current
    val navBarBottomPadding =
        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BottomBarHeight),
            contentAlignment = Alignment.BottomCenter
        ) {

            // ─────────────────────────
            // Floating Rounded Bar
            // ─────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(surfaceColor.copy(alpha = 0.96f)),
                contentAlignment = Alignment.Center
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    leftItems.forEach { item ->
                        AnimatedNavItem(
                            item = item,
                            isSelected = selectedTab == item.route,
                            primary = primaryColor,
                            inactive = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onTabSelected(item) }
                        )
                    }

                    // Space for FAB
                    Spacer(modifier = Modifier.width(56.dp))

                    rightItems.forEach { item ->
                        AnimatedNavItem(
                            item = item,
                            isSelected = selectedTab == item.route,
                            primary = primaryColor,
                            inactive = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onTabSelected(item) }
                        )
                    }
                }
            }

            // ─────────────────────────
            // Floating Action Button
            // ─────────────────────────
            FloatingActionButton(
                onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress);onFabClick() },
                shape = CircleShape,
                containerColor = primaryColor,
                contentColor = onPrimaryColor,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 12.dp,
                    pressedElevation = 4.dp
                ),
                modifier = Modifier
                    .size(FabSize)
                    .align(Alignment.TopCenter)
                    .offset(y = (-28).dp)
                    .shadow(16.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // System navigation filler
        if (navBarBottomPadding > 0.dp) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(navBarBottomPadding)
                    .background(surfaceColor)
            )
        }
    }
}
// ── Animated nav item with pill indicator + spring icon ──────────────────────
@Composable
private fun AnimatedNavItem(
    item: BottomNavItem,
    isSelected: Boolean,
    primary: Color,
    inactive: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) primary else inactive,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "iconColor"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) primary else inactive,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "textColor"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (isSelected) 1.18f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )
    val indicatorAlpha by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(200),
        label = "indicatorAlpha"
    )
    val indicatorWidth by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessMediumLow
        ),
        label = "indicatorWidth"
    )

    Column(
        modifier = modifier
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon with scale spring
        Icon(
            imageVector        = item.icon,
            contentDescription = item.title,
            tint               = iconColor,
            modifier           = Modifier.size(24.dp).scale(iconScale)
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Label
        Text(
            text       = item.title,
            color      = textColor,
            fontSize   = 10.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            textAlign  = TextAlign.Center,
            maxLines   = 1
        )

        Spacer(modifier = Modifier.height(3.dp))

        // Animated dot indicator
        val dotTargetWidthDp = 20.dp
        Box(
            modifier = Modifier
                .height(3.dp)
                .then(
                    Modifier.drawBehind {
                        val w = dotTargetWidthDp.toPx() * indicatorWidth
                        if (w > 0f) {
                            drawRoundRect(
                                color       = primary.copy(alpha = indicatorAlpha),
                                topLeft     = Offset((size.width - w) / 2f, 0f),
                                size        = Size(w, size.height),
                                cornerRadius = CornerRadius(8f, 8f)
                            )
                        }
                    }
                )
                .fillMaxWidth(0.5f)
        )
    }
}

// ── Canvas: notched bar background shape ─────────────────────────────────────
private fun DrawScope.drawCutoutBar(
    color: Color,
    width: Float,
    notchWidth: Float,
    notchDepth: Float
) {
    val height  = size.height
    val cx      = width / 2f
    val half    = notchWidth / 2f
    val ctrl    = notchWidth * 0.38f

    val path = Path().apply {
        moveTo(0f, 0f)
        lineTo(cx - half - ctrl, 0f)
        cubicTo(
            cx - half + ctrl * 0.15f, 0f,
            cx - half * 0.45f,        notchDepth,
            cx,                        notchDepth
        )
        cubicTo(
            cx + half * 0.45f,                   notchDepth,
            cx + half - ctrl * 0.15f, 0f,
            cx + half + ctrl,          0f
        )
        lineTo(width, 0f)
        lineTo(width, height)
        lineTo(0f,   height)
        close()
    }

    drawPath(path = path, color = color)
}
