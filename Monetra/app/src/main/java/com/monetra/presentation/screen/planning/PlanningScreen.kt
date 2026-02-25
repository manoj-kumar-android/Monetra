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
            title = { Text("Add Financial Goal", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { 
                            title = it
                            if (it.isNotBlank()) titleError = null 
                        },
                        label = { Text("Goal Name (e.g., Vacation)") },
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
                        label = { Text("Target Amount") },
                        prefix = { Text("₹") },
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
                Button(onClick = {
                    val t = target.toDoubleOrNull()
                    var hasError = false
                    
                    if (title.isBlank()) {
                        titleError = "Goal name is required"
                        hasError = true
                    }
                    if (t == null || t <= 0) {
                        targetError = "Enter a valid amount"
                        hasError = true
                    }
                    
                    if (!hasError && t != null) {
                        viewModel.addGoal(title, t, GoalCategory.SAVINGS)
                        showGoalDialog = false
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showGoalDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Assistant",
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

        // CARD 4: Can I Afford This?
        item {
            CanIAffordThisCard(overview.monthlySafety)
        }

        // CARD 5: 7-Day Control Plan
        item {
            WeeklyControlPlanCard(overview.controlPlan)
        }

        // QUICK ACTIONS / MANAGERS
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = "Personal Finance Managers",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = Spacing.xs, vertical = Spacing.sm)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    ManagerCard(
                        title = "EMIs & Debt",
                        subtitle = if (overview.totalEmi > 0) "₹%,.0f/mo".format(overview.totalEmi) else "No debt",
                        icon = Icons.Default.Build,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToLoans
                    )
                    ManagerCard(
                        title = "Investments",
                        subtitle = if (overview.totalInvestments > 0) "Value: ₹%,.0f".format(overview.totalInvestments) else "Not started",
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
        title = "Am I Safe This Month?",
        status = analysis.status
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                StatItem("Savings Gap", "₹%,.0f".format(analysis.savingsGap), isBad = analysis.savingsGap > 0)
                StatItem("EMI Ratio", "%.1f%%".format(analysis.emiRatio), isBad = analysis.emiRatio > 40)
            }
            
            if (analysis.isBurnRisk) {
                StatusAlert(
                    message = "High Burn Risk: You are spending faster than your budget allows.",
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
        title = "Where Is My Money Leaking?",
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
        title = "Emergency Safety (Runway)",
        status = analysis.status
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            Text(
                "You can survive %.1f months without income.".format(analysis.monthsCovered),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text("Survival target: 6 months", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            analysis.suggestion?.let {
                ActionSuggestion(it)
            }
        }
    }
}

@Composable
private fun CanIAffordThisCard(safety: MonthlySafetyAnalysis) {
    var amount by remember { mutableStateOf("") }
    val expense = amount.toDoubleOrNull() ?: 0.0
    
    CoachCard(
        title = "Can I Afford This?",
        status = SafetyStatus.GREEN 
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() } },
                label = { Text("Enter expense amount") },
                prefix = { Text("₹") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            
            if (expense > 0) {
                val impact = safety.savingsGap + expense
                StatusAlert(
                    message = "This will increase your savings gap to ₹%,.0f.".format(impact),
                    color = if (impact > 0) MaterialTheme.colorScheme.error else Color(0xFF34C759)
                )
            }
        }
    }
}

@Composable
private fun WeeklyControlPlanCard(plan: WeeklyControlPlan) {
    CoachCard(
        title = "7-Day Control Plan",
        status = SafetyStatus.GREEN
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(
                "Max spend next 7 days:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "₹%,.0f".format(plan.weeklyLimit),
                style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.primary
            )
            
            plan.highRiskCategory?.let {
                ActionSuggestion("Watch out for $it - your highest spend category.")
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
            text = "Coach: $suggestion",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.padding(Spacing.md),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}





