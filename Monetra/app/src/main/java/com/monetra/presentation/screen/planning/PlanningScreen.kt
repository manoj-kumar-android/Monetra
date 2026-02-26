package com.monetra.presentation.screen.planning

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.monetra.R
import com.monetra.domain.model.*
import com.monetra.presentation.components.HelpIconButton
import com.monetra.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanningScreen(
    onNavigateToLoans: () -> Unit,
    onNavigateToInvestments: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    onNavigateToHelp: () -> Unit,
    viewModel: PlanningViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showGoalDialog by remember { mutableStateOf(false) }

    if (showGoalDialog) {
        var title by remember { mutableStateOf("") }
        var target by remember { mutableStateOf("") }
        var titleError by remember { mutableStateOf<String?>(null) }
        var targetError by remember { mutableStateOf<String?>(null) }
        
        AlertDialog(
            onDismissRequest = { showGoalDialog = false },
            title = { Text(stringResource(R.string.add_financial_goal), fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { 
                            title = it
                            if (it.isNotBlank()) titleError = null 
                        },
                        label = { Text(stringResource(R.string.goal_name_label)) },
                        isError = titleError != null,
                        supportingText = if (titleError != null) { { Text(titleError!!) } } else null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = target,
                        onValueChange = { input -> 
                            val sanitized = input.filter { it.isDigit() || it == '.' }
                            if (sanitized.count { it == '.' } <= 1) {
                                target = sanitized
                                if (sanitized.isNotBlank()) targetError = null
                            }
                        },
                        label = { Text(stringResource(R.string.target_amount_label)) },
                        prefix = { Text(stringResource(R.string.rupee_symbol)) },
                        isError = targetError != null,
                        supportingText = if (targetError != null) { { Text(targetError!!) } } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                val context = androidx.compose.ui.platform.LocalContext.current
                Button(onClick = {
                    val t = target.toDoubleOrNull()
                    var hasError = false
                    
                    if (title.isBlank()) {
                        titleError = context.getString(R.string.goal_name_required)
                        hasError = true
                    }
                    if (t == null || t <= 0) {
                        targetError = context.getString(R.string.valid_amount_required)
                        hasError = true
                    }
                    
                    if (!hasError && t != null) {
                        viewModel.addGoal(title, t, GoalCategory.SAVINGS)
                        showGoalDialog = false
                    }
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.assistant_title),
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
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is PlanningUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PlanningUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                }
                is PlanningUiState.Success -> {
                    PlanningContent(
                        overview = state.overview,
                        wealthProjection = state.wealthProjection,
                        onNavigateToLoans = onNavigateToLoans,
                        onNavigateToInvestments = onNavigateToInvestments,
                        onNavigateToSimulator = onNavigateToSimulator,
                        onAddGoalClick = { showGoalDialog = true }
                    )
                }
            }
        }
    }
}

@Composable
private fun PlanningContent(
    overview: PlanningOverview,
    wealthProjection: com.monetra.domain.model.WealthProjection,
    onNavigateToLoans: () -> Unit,
    onNavigateToInvestments: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    onAddGoalClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // CARD 1: Monthly Safety
        item {
            MonthlySafetyCard(
                analysis = overview.monthlySafety
            )
        }

        // CARD 1.5: Wealth Forecast (Moved from Dashboard)
        item {
            com.monetra.presentation.screen.dashboard.components.WealthProjectionSummaryCard(
                projection = wealthProjection,
                onClick = onNavigateToInvestments
            )
        }

        // CARD 2: Money Leakage
        item {
            MoneyLeakageCard(overview.moneyLeakage)
        }

        // CARD 3: Emergency Safety
        item {
            EmergencySafetyCard(
                analysis = overview.emergencySafety
            )
        }

        // CARD 4: Impact Checker
        item {
            ImpactChecker(
                safety = overview.monthlySafety,
                emergency = overview.emergencySafety
            )
        }
        
        // CARD 5: Daily Pocket Money
        item {
            DailyPocketMoney(overview.controlPlan)
        }

        // QUICK ACTIONS / MANAGERS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = stringResource(R.string.personal_finance_managers),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.sm)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    ManagerCard(
                        title = stringResource(R.string.emis_debt),
                        subtitle = if (overview.totalEmi > 0) stringResource(R.string.monthly_emi_format, overview.totalEmi) else stringResource(R.string.no_debt),
                        icon = Icons.Default.Build,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLoans
                    )
                    ManagerCard(
                        title = stringResource(R.string.investments_title),
                        subtitle = if (overview.totalInvestments > 0) stringResource(R.string.value_format, overview.totalInvestments) else stringResource(R.string.not_started),
                        icon = Icons.Default.Star,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToInvestments
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun ManagerCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md).fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun MonthlySafetyCard(analysis: MonthlySafetyAnalysis) {
    CoachCard(
        title = stringResource(R.string.am_i_safe_month),
        status = analysis.status
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem(stringResource(R.string.savings_gap_label), "₹%,.0f".format(analysis.savingsGap), isBad = analysis.savingsGap > 0)
                StatItem(stringResource(R.string.emi_ratio_label), "%.1f%%".format(analysis.emiRatio), isBad = analysis.emiRatio > 40)
            }
            
            if (analysis.isBurnRisk) {
                StatusAlert(
                    message = stringResource(R.string.high_burn_risk),
                    color = MaterialTheme.colorScheme.error
                )
            }

            ActionSuggestion(analysis.suggestedAction)
        }
    }
}

@Composable
private fun MoneyLeakageCard(analysis: MoneyLeakageAnalysis) {
    CoachCard(
        title = stringResource(R.string.leakage_title),
        status = if (analysis.topCategories.isNotEmpty()) SafetyStatus.YELLOW else SafetyStatus.GREEN
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            analysis.topCategories.forEach { category ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(category.name, style = MaterialTheme.typography.bodyLarge)
                    Text("₹%,.0f".format(category.amount), fontWeight = FontWeight.Bold)
                }
            }
            
            ActionSuggestion(analysis.leakageMessage)
        }
    }
}

@Composable
private fun EmergencySafetyCard(analysis: EmergencySafetyAnalysis) {
    CoachCard(
        title = stringResource(R.string.emergency_safety_runway),
        status = analysis.status
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text(
                stringResource(R.string.survival_months_format, analysis.monthsCovered),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(stringResource(R.string.survival_target), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            analysis.suggestion?.let {
                ActionSuggestion(it)
            }
        }
    }
}

@Composable
private fun ImpactChecker(safety: MonthlySafetyAnalysis, emergency: EmergencySafetyAnalysis) {
    var amountText by remember { mutableStateOf("") }
    val amount = amountText.toDoubleOrNull() ?: 0.0
    
    CoachCard(
        title = stringResource(R.string.impact_checker_title),
        status = if (amount == 0.0) SafetyStatus.GREEN else {
            when {
                amount > (safety.savingsGap * -1).coerceAtLeast(0.0) && safety.savingsGap > 0 -> SafetyStatus.RED
                amount > (safety.dailyBurnRate * 3) -> SafetyStatus.YELLOW
                else -> SafetyStatus.GREEN
            }
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text(
                stringResource(R.string.impact_checker_instruction),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { c -> c.isDigit() } },
                label = { Text(stringResource(R.string.how_much_is_it)) },
                prefix = { Text(stringResource(R.string.rupee_symbol)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            
            if (amount > 0) {
                val impactOnSavings = (safety.savingsGap + amount)
                val daysToRecover = if (safety.dailyBurnRate > 0) amount / safety.dailyBurnRate else 0.0
                
                val (verdict, color, advice) = when {
                    amount > (safety.dailyBurnRate * 15) -> Triple(stringResource(R.string.verdict_big_purchase), MaterialTheme.colorScheme.error, stringResource(R.string.impact_checker_big_purchase_advice, daysToRecover))
                    impactOnSavings > 0 -> Triple(stringResource(R.string.verdict_savings_risk), Color(0xFFFF9500), stringResource(R.string.impact_checker_savings_risk_advice))
                    else -> Triple(stringResource(R.string.verdict_safe_to_buy), Color(0xFF34C759), stringResource(R.string.impact_checker_safe_advice))
                }

                Surface(
                    color = color.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (color == MaterialTheme.colorScheme.error) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = color,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(Spacing.sm))
                            Text(verdict, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = color)
                        }
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(advice, style = MaterialTheme.typography.bodySmall, color = color.copy(alpha = 0.9f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyPocketMoney(plan: WeeklyControlPlan) {
    val progress = if (plan.dailyLimit > 0) 
        ((plan.dailyLimit - plan.remainingToday) / plan.dailyLimit).toFloat().coerceIn(0f, 1f) 
        else 0f
    
    val statusColor = when {
        progress > 0.9f -> MaterialTheme.colorScheme.error
        progress > 0.7f -> Color(0xFFFF9500)
        else -> MaterialTheme.colorScheme.primary
    }

    CoachCard(
        title = stringResource(R.string.daily_pocket_money),
        status = if (progress > 0.9f) SafetyStatus.RED else SafetyStatus.GREEN
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(stringResource(R.string.todays_allowance), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        "₹%,.0f".format(plan.remainingToday),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                        color = statusColor
                    )
                }
                Text(
                    stringResource(R.string.limit_format, plan.dailyLimit),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                    color = statusColor,
                    trackColor = statusColor.copy(alpha = 0.1f),
                    strokeCap = StrokeCap.Round
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.spent_today_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(stringResource(R.string.used_percent_format, (progress * 100).toInt()), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor)
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
                        stringResource(R.string.pacing_tip_format, plan.dailyLimit),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CoachCard(
    title: String,
    status: SafetyStatus,
    content: @Composable () -> Unit
) {
    val statusColor = when (status) {
        SafetyStatus.GREEN -> Color(0xFF34C759)
        SafetyStatus.YELLOW -> Color(0xFFFF9500)
        SafetyStatus.RED -> Color(0xFFFF3B30)
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
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.lg))
            content()
        }
    }
}

@Composable
private fun StatItem(label: String, value: String, isBad: Boolean) {
    Column {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = if (isBad) Color(0xFFFF3B30) else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun StatusAlert(message: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(message, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActionSuggestion(suggestion: String) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.coach_prefix, suggestion),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(Spacing.md),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}





