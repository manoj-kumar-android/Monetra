package com.monetra.presentation.screen.dashboard

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.NativeCanvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import com.monetra.presentation.screen.dashboard.components.*
import com.monetra.presentation.screen.transactions.*
import com.monetra.presentation.screen.transactions.components.*
import com.monetra.presentation.components.HelpIconButton
import com.monetra.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToReport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onManageBudgetsClick: () -> Unit,
    onNavigateToFixedExpenses: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onSeeAllTransactions: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Dashboard",
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
                    HelpIconButton(onClick = onNavigateToHelp)
                    IconButton(onClick = onNavigateToReport) {
                        Icon(Icons.Default.DateRange, contentDescription = "Monthly Report")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToAdd,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = Spacing.md)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Transaction")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is DashboardUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is DashboardUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                }
                is DashboardUiState.NoSalarySet -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(Spacing.lg),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("👋", style = MaterialTheme.typography.displayMedium)
                        Spacer(modifier = Modifier.height(Spacing.md))
                        Text(
                            "Welcome to Monetra!",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(Spacing.sm))
                        Text(
                            "Set your monthly salary in Settings to unlock your personalised financial dashboard.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(Spacing.xl))
                        Button(
                            onClick = onNavigateToSettings,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                        ) {
                            Text("⚙️  Go to Settings")
                        }
                    }
                }
                is DashboardUiState.Success -> {
                    DashboardContent(
                        state = state,
                        onTransactionClick = onNavigateToEdit,
                        onManageBudgetsClick = onManageBudgetsClick,
                        onFixedCostsClick = onNavigateToFixedExpenses,
                        onSeeAllTransactions = onSeeAllTransactions
                    )
                }
            }
        }
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
                val hour = java.time.LocalTime.now().hour
                val greeting = when {
                    hour < 12 -> "Good Morning,"
                    hour < 17 -> "Good Afternoon,"
                    else -> "Good Evening,"
                }
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = state.summary.ownerName.ifBlank { "there" },
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
                percent = state.stsPercent
            )
        }

        // INSIGHTS & MONTHLY SUMMARY
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                // Projection / Intelligence integrated here instead of a separate card
                InsightBadgeCard(
                    status = state.intelligence.burnRateStatus,
                    message = state.intelligence.comparisonText,
                    modifier = Modifier.weight(1f)
                )
                
                MetricCard(
                    title = "Fixed Bills",
                    amount = state.fixedCosts,
                    subtitle = "Monthly commit",
                    icon = Icons.Default.Notifications,
                    modifier = Modifier.weight(1f).clickable(onClick = onFixedCostsClick),
                    color = MaterialTheme.colorScheme.tertiaryContainer
                )
            }
        }

        // FINANCIAL RUNWAY / SUMMARY
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = "Monthly Health",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = Spacing.xs)
                )
                MonthlySummaryCard(summary = state.summary)
            }
        }



        // BUDGET BREAKDOWN (Plan)
        item {
            FinancialWaterfall(
                income = state.income,
                savings = state.savingsGoal,
                emis = state.totalEmi,
                fixed = state.fixedCosts.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0
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

        // RECENT TRANSACTIONS HEADER
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.md),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onSeeAllTransactions) {
                    Text("See All", style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        // TRANSACTIONS
        items(state.recentTransactions) { transaction ->
            TransactionRow(
                item = transaction,
                onClick = { onTransactionClick(transaction.id) },
                onDelete = {}
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(80.dp)) // FAB padding
        }
    }
}

@Composable
private fun SafeToSpendCard(amount: String, limit: String, percent: Float) {
    val isOverspent = amount.startsWith("−") || amount.startsWith("-")
    val animatedPercent by animateFloatAsState(targetValue = percent, label = "STSProgress")
    
    val baseColor = if (isOverspent) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    val containerColor = if (isOverspent) baseColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)

    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, baseColor.copy(alpha = 0.1f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Background Liquid Effect
            Box(
                modifier = Modifier
                    .fillMaxWidth(if (isOverspent) 1f else animatedPercent)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                baseColor.copy(alpha = 0.15f),
                                baseColor.copy(alpha = 0.05f)
                            )
                        )
                    )
            )

            Column(
                modifier = Modifier.padding(Spacing.xl).fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "Safe to Spend Today",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (isOverspent) "Redistributing from tomorrow" else "Daily Budget: ₹$limit",
                            style = MaterialTheme.typography.labelMedium,
                            color = baseColor.copy(alpha = 0.7f)
                        )
                    }
                    
                    Surface(
                        shape = CircleShape,
                        color = baseColor.copy(alpha = 0.1f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            if (isOverspent) Icons.Default.Info else Icons.Default.Notifications,
                            contentDescription = null,
                            tint = baseColor,
                            modifier = Modifier.padding(Spacing.sm)
                        )
                    }
                }

                Column {
                    Text(
                        text = amount,
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = (-1.5).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    
                    // Simple progress bar for a cleaner look
                    LinearProgressIndicator(
                        progress = { if (isOverspent) 1f else animatedPercent },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                        color = baseColor,
                        trackColor = baseColor.copy(alpha = 0.1f),
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }
    }
}




