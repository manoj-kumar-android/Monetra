package com.monetra.presentation.screen.dashboard

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.R
import com.monetra.presentation.components.HelpIconButton
import com.monetra.presentation.screen.dashboard.components.FinancialWaterfall
import com.monetra.presentation.screen.transactions.components.BudgetSection
import com.monetra.presentation.screen.transactions.components.TransactionRow
import com.monetra.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onManageBudgetsClick: () -> Unit,
    onNavigateToFixedExpenses: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    onNavigateToWelcome: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRestoring by viewModel.isRestoring.collectAsStateWithLifecycle()
    val events by viewModel.events.collectAsStateWithLifecycle(initialValue = null)
    var showExitSheet by remember { mutableStateOf(false) }

    androidx.compose.runtime.LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            if (event is DashboardEvent.NavigateToWelcome) {
                onNavigateToWelcome()
            }
        }
    }

    val activity = LocalActivity.current

    BackHandler(enabled = !showExitSheet) {
        showExitSheet = true
    }
    
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.dashboard_title),
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(start = Spacing.sm)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = onNavigateToSimulator) {
                        Text("🔮", fontSize = 20.sp)
                    }
                    HelpIconButton(onClick = onNavigateToHelp)
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            val renderState = when {
                isRestoring -> DashboardUiState.Loading
                else -> uiState
            }
                when (renderState) {
                    is DashboardUiState.Loading ->  CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is DashboardUiState.Error -> {
                        Text(renderState.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                    }
                is DashboardUiState.NoSalarySet -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("👋", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            stringResource(R.string.welcome_to_monetra),
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            stringResource(R.string.set_salary_instruction),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Spacing.xl))
                        Button(
                            onClick = onNavigateToSettings,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                        ) {
                            Text(stringResource(R.string.go_to_settings))
                        }
                    }
                }
                is DashboardUiState.Success -> {
                    DashboardContent(
                        state = renderState,
                        onTransactionClick = onNavigateToEdit,
                        onManageBudgetsClick = onManageBudgetsClick,
                        onFixedCostsClick = onNavigateToFixedExpenses,
                        onSeeAllTransactions = onSeeAllTransactions
                    )
                }
            }

        }
    }

    if (showExitSheet) {
        ExitConfirmationSheet(
            onDismiss = { showExitSheet = false },
            onConfirmExit = {
                activity?.finishAffinity()
            }
        )
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Success,
    onTransactionClick: (Long) -> Unit,
    onManageBudgetsClick: () -> Unit,
    onFixedCostsClick: () -> Unit,
    onSeeAllTransactions: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // GREETING SECTION
        item {
            Column(modifier = Modifier.padding(bottom = Spacing.sm)) {
                val hour = remember { java.time.LocalTime.now().hour }
                val greeting = when {
                    hour < 12 -> stringResource(R.string.good_morning)
                    hour < 17 -> stringResource(R.string.good_afternoon)
                    else -> stringResource(R.string.good_evening)
                }
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = state.summary.ownerName.ifBlank { stringResource(R.string.there) },
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // HERO CARD: TODAY'S LIMIT
        item {
            SafeToSpendCard(
                amount = state.dailySafeToSpend, 
                limit = state.dailyLimit,
                rawLimit = state.rawDailyLimit,
                percent = state.stsPercent
            )
        }

        // INSIGHTS & FIXED COSTS
        item {
            PremiumFixedBillsCard(
                amount = state.fixedCosts,
                onClick = onFixedCostsClick
            )
        }

        // BUDGET BREAKDOWN (Plan)
        item {
            FinancialWaterfall(
                income = state.income,
                savings = state.savingsGoal,
                emis = state.totalEmi,
                fixed = state.rawFixedCosts
            )
        }

        // BUDGET PROGRESS
        if (state.budgets.isNotEmpty()) {
            item {
                BudgetSection(
                    budgets = state.budgets,
                    onManageClick = onManageBudgetsClick
                )
            }
        }

        if (state.recentTransactions.isNotEmpty()) {
            // RECENT TRANSACTIONS HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.recent_activity),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    if (state.recentTransactions.size > 3) {
                        TextButton(onClick = onSeeAllTransactions) {
                            Text(stringResource(R.string.see_all), style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
    
            // TRANSACTIONS
            items(state.recentTransactions, key = { it.id }, contentType = { "transaction" }) { transaction ->
                TransactionRow(
                    item = transaction,
                    onClick = { onTransactionClick(transaction.id) },
                    onDelete = {}
                )
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // FAB padding
        }
    }
}

@Composable
private fun SafeToSpendCard(amount: String, limit: String, rawLimit: Double, percent: Float) {
    val isOverspent = amount.startsWith("−") || amount.startsWith("-")
    val progress = (1f - percent).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "STSProgress")
    
    val statusColor = when {
        isOverspent -> MaterialTheme.colorScheme.error
        progress > 0.9f -> MaterialTheme.colorScheme.error
        progress > 0.7f -> Color(0xFFFF9500)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, statusColor.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Spacer(modifier = Modifier.width(Spacing.md))
                Text(
                    text = stringResource(R.string.safe_to_spend_today),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(stringResource(R.string.todays_allowance), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            amount,
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                            color = statusColor
                        )
                    }
                    Text(
                        stringResource(R.string.limit_format, rawLimit),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    LinearProgressIndicator(
                        progress = { if (isOverspent) 1f else animatedProgress },
                        modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                        color = statusColor,
                        trackColor = statusColor.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.spent_today_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(stringResource(R.string.used_percent_format, if (isOverspent) 100 else (progress * 100).toInt()), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor)
                    }
                }
                
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.sm).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            if (isOverspent) stringResource(R.string.redistributing_tomorrow) else stringResource(R.string.pacing_tip_format, rawLimit),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumFixedBillsCard(amount: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Fancy abstract shapes
            Box(Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .offset(x = 220.dp, y = (-30).dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                )
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .offset(x = (-30).dp, y = 60.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f))
                )
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = Spacing.lg, vertical = Spacing.md)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.tertiary),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧾", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.fixed_bills),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        text = stringResource(R.string.monthly_commit),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
                Text(
                    text = amount,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExitConfirmationSheet(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val imeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        sheetGesturesEnabled = !imeVisible,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedAvatar()

            Spacer(modifier = Modifier.height(Spacing.xl))

            Text(
                "Leaving so soon?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(Spacing.sm))

            Text(
                "Your finances will miss you! Are you sure you want to exit the app?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(Spacing.xxl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                OutlinedButton(
                    onClick = onConfirmExit,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
                ) {
                    Text("Exit App", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Stay", fontWeight = FontWeight.Bold)
                }
            }
            
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun AnimatedAvatar() {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "avatarPulse")
    
    val scale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse1"
    )

    val scale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(2000, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "pulse2"
    )

    val alpha1 by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1500, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "alpha1"
    )
    
    val alpha2 by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(2000, easing = androidx.compose.animation.core.LinearOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "alpha2"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(120.dp)
    ) {
        // Outer ripple
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(scale2)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha2))
        )
        // Inner ripple
        Box(
            modifier = Modifier
                .size(72.dp)
                .scale(scale1)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha1))
        )
        // Main Avatar Base
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text("🥺", fontSize = 40.sp, modifier = Modifier.offset(y = (-2).dp))
        }
    }
}
