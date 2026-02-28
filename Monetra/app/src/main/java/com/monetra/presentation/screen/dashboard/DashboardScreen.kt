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
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.res.stringResource
import com.monetra.R
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
                    HelpIconButton(onClick = onNavigateToHelp)
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title))
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
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_transaction_cd))
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
            items(state.recentTransactions) { transaction ->
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
private fun SafeToSpendCard(amount: String, limit: String, percent: Float) {
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
                        stringResource(R.string.limit_format, limit.replace(",", "").replace("₹", "").toDoubleOrNull() ?: 0.0),
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
                            if (isOverspent) stringResource(R.string.redistributing_tomorrow) else stringResource(R.string.pacing_tip_format, limit.replace(",", "").replace("₹", "").toDoubleOrNull() ?: 0.0),
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
