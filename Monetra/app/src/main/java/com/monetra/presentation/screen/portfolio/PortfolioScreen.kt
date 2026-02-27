package com.monetra.presentation.screen.portfolio

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.domain.model.*
import com.monetra.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToInvestments: () -> Unit,
    viewModel: PortfolioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val showSavingsDialog by viewModel.showSavingsDialog.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Portfolio",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(start = Spacing.sm)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (val state = uiState) {
                is PortfolioUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is PortfolioUiState.NeedsSetup -> {
                    PortfolioSetupPrompt(onNavigateToSettings = onNavigateToSettings)
                }
                is PortfolioUiState.Error -> {
                    Text(
                        text = state.message,
                        modifier = Modifier.align(Alignment.Center).padding(Spacing.xl),
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
                is PortfolioUiState.Success -> {
                    PortfolioDashboard(
                        data = state.data,
                        onEditSavings = viewModel::openSavingsDialog,
                        onNavigateToLoans = onNavigateToLoans,
                        onNavigateToInvestments = onNavigateToInvestments
                    )
                }
            }
        }
    }

    if (showSavingsDialog) {
        EditSavingsDialog(
            onDismiss = viewModel::closeSavingsDialog,
            onConfirm = viewModel::updateCurrentSavings
        )
    }
}

@Composable
private fun PortfolioSetupPrompt(onNavigateToSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("💼", fontSize = 60.sp)
        Spacer(Modifier.height(Spacing.lg))
        Text(
            "Set Up Your Portfolio",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(Spacing.md))
        Text(
            "Enter your current financial details to calculate your real net worth and track your financial growth.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(Spacing.xl))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                SetupItem("💰 Monthly Income", "Set in Settings")
                SetupItem("🏦 Current Savings", "Your existing bank savings")
                SetupItem("📈 Investments", "Stocks, SIPs, FDs, etc.")
                SetupItem("💳 Existing Loans", "Home loan, car loan, etc.")
                SetupItem("🧾 Monthly Expenses", "Fixed bills & utilities")
            }
        }
        Spacer(Modifier.height(Spacing.xl))
        Button(
            onClick = onNavigateToSettings,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Settings, contentDescription = null)
            Spacer(Modifier.width(Spacing.sm))
            Text("Go to Settings to Start")
        }
    }
}

@Composable
private fun SetupItem(emoji: String, desc: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Text(emoji, fontSize = 20.sp)
        Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PortfolioDashboard(
    data: PortfolioData,
    onEditSavings: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToInvestments: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        Spacer(Modifier.height(Spacing.xs))

        // NET WORTH HERO CARD
        NetWorthCard(data = data)

        Column(
            modifier = Modifier.padding(horizontal = Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            // FREE MONEY
            FreeMoneyCard(freeMoney = data.freeMoney, income = data.monthlyIncome)

            // FINANCIAL BREAKDOWN (3-col grid)
            FinancialBreakdownRow(
                savings = data.currentSavings,
                investments = data.totalInvestmentValue,
                loanRemaining = data.totalLoanRemaining,
                onEditSavings = onEditSavings,
                onNavigateToInvestments = onNavigateToInvestments,
                onNavigateToLoans = onNavigateToLoans
            )

            // FINANCIAL SCORE
            FinancialScoreCard(score = data.financialScore, monthlyInvestment = data.totalMonthlyInvestment, income = data.monthlyIncome)

            // WEALTH PROJECTION
            WealthProjectionCard(projection = data.wealthProjection, onNavigateToInvestments = onNavigateToInvestments)


            Spacer(Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun NetWorthCard(data: PortfolioData) {
    val netWorthColor = if (data.netWorth >= 0) Color(0xFF34C759) else MaterialTheme.colorScheme.error
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.matchParentSize()) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .offset(x = 180.dp, y = (-50).dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                )
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .offset(x = (-50).dp, y = 100.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f))
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally, 
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xxl)
            ) {
                Text(
                    "TOTAL NET WORTH",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 2.sp, fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(Modifier.height(Spacing.sm))
                AnimatedContent(targetState = data.netWorth, label = "netWorthAnim") { worth ->
                    Text(
                        text = "₹%,.0f".format(worth),
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(Spacing.sm))
                Surface(
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (data.netWorth >= 0) "Your total wealth position" else "You owe more than you own",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FreeMoneyCard(freeMoney: Double, income: Double) {
    val isPositive = freeMoney >= 0
    val color = when {
        freeMoney > 0 -> Color(0xFF34C759)
        freeMoney == 0.0 -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.error
    }
    val message = when {
        freeMoney > 0 -> "Extra money available after all obligations"
        freeMoney == 0.0 -> "Perfectly balanced — no spare cash"
        else -> "You're overspending by ₹%,.0f/month".format(-freeMoney)
    }

    PortfolioCard(
        title = "FREE MONEY",
        icon = Icons.Default.AccountBalance,
        accentColor = color
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "₹%,.0f".format(freeMoney),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = color
                )
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (income > 0) {
                val pct = (freeMoney / income * 100).toInt()
                Surface(color = color.copy(alpha = 0.12f), shape = CircleShape) {
                    Text(
                        "$pct%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                        color = color
                    )
                }
            }
        }
    }
}

@Composable
private fun FinancialBreakdownRow(
    savings: Double,
    investments: Double,
    loanRemaining: Double,
    onEditSavings: () -> Unit,
    onNavigateToInvestments: () -> Unit,
    onNavigateToLoans: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        MiniStatCard(
            modifier = Modifier.weight(1f),
            label = "Savings",
            value = savings,
            emoji = "🏦",
            color = Color(0xFF34C759),
            editable = true,
            onClick = onEditSavings
        )
        MiniStatCard(
            modifier = Modifier.weight(1f),
            label = "Investments",
            value = investments,
            emoji = "📈",
            color = Color(0xFF5856D6),
            onClick = onNavigateToInvestments
        )
        MiniStatCard(
            modifier = Modifier.weight(1f),
            label = "Loan Debt",
            value = loanRemaining,
            emoji = "💳",
            color = if (loanRemaining > 0) MaterialTheme.colorScheme.error else Color(0xFF34C759),
            onClick = onNavigateToLoans
        )
    }
}

@Composable
private fun MiniStatCard(
    modifier: Modifier,
    label: String,
    value: Double,
    emoji: String,
    color: Color,
    editable: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(120.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.2f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .offset(x = 60.dp, y = (-20).dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.05f))
            )

            Column(
                modifier = Modifier.fillMaxSize().padding(Spacing.md),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween, 
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(emoji, fontSize = 18.sp)
                    }
                    if (editable) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
                    }
                }
                Column {
                    Text(
                        "₹%,.0f".format(value),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                        color = color,
                        maxLines = 1
                    )
                    Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun FinancialScoreCard(score: FinancialScore, monthlyInvestment: Double, income: Double) {
    val scoreColor = when (score) {
        FinancialScore.POOR -> MaterialTheme.colorScheme.error
        FinancialScore.AVERAGE -> Color(0xFFFF9500)
        FinancialScore.GOOD -> Color(0xFF34C759)
        FinancialScore.EXCELLENT -> Color(0xFF5856D6)
    }
    PortfolioCard(title = "FINANCIAL SCORE", icon = Icons.Default.Star, accentColor = scoreColor) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(
                    "${score.emoji} ${score.label}",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = scoreColor
                )
                Text(
                    if (income > 0) "Saving ${"%,.0f".format(monthlyInvestment / income * 100)}% of income monthly"
                    else "Add income in Settings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(Spacing.md))
        val progress = when (score) {
            FinancialScore.POOR -> 0.1f
            FinancialScore.AVERAGE -> 0.35f
            FinancialScore.GOOD -> 0.65f
            FinancialScore.EXCELLENT -> 1f
        }
        val animProgress by animateFloatAsState(targetValue = progress, animationSpec = tween(1000), label = "score")
        LinearProgressIndicator(
            progress = { animProgress },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
            color = scoreColor,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(Modifier.height(Spacing.sm))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(FinancialScore.POOR, FinancialScore.AVERAGE, FinancialScore.GOOD, FinancialScore.EXCELLENT).forEach { s ->
                Text(
                    s.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (s == score) scoreColor else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    fontWeight = if (s == score) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun WealthProjectionCard(projection: PortfolioProjection, onNavigateToInvestments: () -> Unit) {
    PortfolioCard(
        modifier = Modifier.fillMaxWidth().clickable { onNavigateToInvestments() },
        title = "WEALTH PROJECTION", 
        icon = Icons.AutoMirrored.Filled.TrendingUp, 
        accentColor = Color(0xFF5856D6)
    ) {
        if (projection.monthlyContribution <= 0) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("📈", fontSize = 32.sp)
                Spacer(Modifier.height(Spacing.sm))
                Text("No monthly investments yet", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                Spacer(Modifier.height(Spacing.md))
                TextButton(onClick = onNavigateToInvestments) {
                    Text("Add Investments →")
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(
                        "₹%,.0f".format(projection.projectedValue),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                        color = Color(0xFF5856D6)
                    )
                    Surface(color = Color(0xFF5856D6).copy(alpha = 0.12f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            "in ${projection.years} yrs @ ${projection.annualRatePercent.toInt()}%",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF5856D6)
                        )
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    ProjectionMetric("Invested", projection.totalInvested, Color(0xFF8E8E93))
                    ProjectionMetric("Returns", projection.totalReturns, Color(0xFF34C759))
                    ProjectionMetric("Monthly", projection.monthlyContribution, Color(0xFF5856D6))
                }
            }
        }
    }
}

@Composable
private fun ProjectionMetric(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("₹%,.0f".format(amount), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}



@Composable
private fun PortfolioCard(
    modifier: Modifier = Modifier.fillMaxWidth(),
    title: String,
    icon: ImageVector,
    accentColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Spacing.xl)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
                }
                Text(
                    title,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Spacing.lg))
            content()
        }
    }
}

@Composable
private fun EditSavingsDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var text by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Current Savings") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    "Enter your current total savings / bank balance.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Savings Amount") },
                    prefix = { Text("₹ ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    text.toDoubleOrNull()?.let { onConfirm(it) }
                },
                enabled = text.toDoubleOrNull() != null
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
