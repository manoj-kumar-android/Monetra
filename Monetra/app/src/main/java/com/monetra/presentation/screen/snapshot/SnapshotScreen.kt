package com.monetra.presentation.screen.snapshot

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.R
import com.monetra.domain.model.*
import com.monetra.presentation.components.HelpIconButton
import com.monetra.ui.theme.Spacing
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapshotScreen(
    onNavigateToHelp: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToInvestments: () -> Unit,
    viewModel: SnapshotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(com.monetra.R.string.snapshot_title_header),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                actions = {
                    HelpIconButton(onClick = onNavigateToHelp)
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is SnapshotUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is SnapshotUiState.Error -> {
                    Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                }
                is SnapshotUiState.Success -> {
                    PortfolioContent(
                        report = state.report, 
                        overview = state.overview,
                        wealthProjection = state.wealthProjection,
                        onNavigateToLoans = onNavigateToLoans,
                        onNavigateToInvestments = onNavigateToInvestments
                    )
                }
            }
        }
    }
}

@Composable
private fun PortfolioContent(
    report: ComprehensiveMonthlyReport, 
    overview: PlanningOverview,
    wealthProjection: com.monetra.domain.model.WealthProjection,
    onNavigateToLoans: () -> Unit,
    onNavigateToInvestments: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {
        // 1. Health Status & Month
        item {
            val monthName = report.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "$monthName, ${report.month.year}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                HealthStatusHeader(report.status)
            }
        }

        // 2. Performance Comparison (Premium Visual)
        if (report.comparison != null) {
            item {
                ComparisonSection(report.comparison)
            }
        }

        // 3. Wealth Forecast (MOVED FROM ASSISTANT)
        item {
            com.monetra.presentation.screen.dashboard.components.WealthProjectionSummaryCard(
                projection = wealthProjection,
                onClick = onNavigateToInvestments
            )
        }

        // 4. Finance Overview (Income vs Outlays)
        item {
            SnapshotSection(title = stringResource(com.monetra.R.string.monthly_overview)) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                    MetricRow(label = stringResource(com.monetra.R.string.monthly_salary_label), value = report.income, isPrimary = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.xs), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    MetricRow(label = stringResource(com.monetra.R.string.total_expenses_label), value = report.expenses)
                    MetricRow(label = stringResource(com.monetra.R.string.monthly_emi_label), value = report.emis)
                    MetricRow(label = stringResource(com.monetra.R.string.investments_label), value = report.investments)
                    
                    val outlays = report.expenses + report.emis + report.investments
                    val remaining = report.income - outlays
                    
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = Spacing.sm)
                    ) {
                        Row(modifier = Modifier.padding(Spacing.md), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Unallocated Balance", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                            Text("₹%,.0f".format(remaining), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black))
                        }
                    }
                }
            }
        }

        // 4. PERSONAL FINANCE MANAGERS (User explicitly wanted this here)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                Text(
                    text = stringResource(R.string.personal_finance_managers).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
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

        // 5. TOP SPENDING CATEGORIES
        if (report.topCategories.isNotEmpty()) {
            item {
                SnapshotSection(title = stringResource(R.string.top_spending_categories)) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                        report.topCategories.forEach { category ->
                            Column {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(category.category, style = MaterialTheme.typography.bodyMedium)
                                    Text("₹%,.0f".format(category.amount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                }
                                val max = report.topCategories.first().amount
                                LinearProgressIndicator(
                                    progress = { (category.amount / max).toFloat() },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. EMI STRESS & SUGGESTIONS
        item {
            StressAndSuggestions(report)
        }
        
        item {
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun ComparisonSection(comparison: PreviousMonthComparison) {
    SnapshotSection(title = stringResource(R.string.versus_last_month)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            ComparisonMetric(modifier = Modifier.weight(1f), label = stringResource(R.string.income), percent = comparison.incomeChangePercent)
            ComparisonMetric(modifier = Modifier.weight(1f), label = stringResource(R.string.expense), percent = comparison.expenseChangePercent, inverse = true)
            ComparisonMetric(modifier = Modifier.weight(1f), label = stringResource(R.string.actual_savings_label), percent = comparison.savingsChangePercent)
        }
    }
}

@Composable
private fun ComparisonMetric(modifier: Modifier, label: String, percent: Double, inverse: Boolean = false) {
    val isPositive = percent >= 0
    val isGood = if (inverse) !isPositive else isPositive
    val color = if (isGood) Color(0xFF34C759) else Color(0xFFFF3B30)
    
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = (if (isPositive) "+" else "") + "%.0f%%".format(percent),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
private fun StressAndSuggestions(report: ComprehensiveMonthlyReport) {
    if (report.suggestions.isNotEmpty()) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
            Text(
                text = stringResource(R.string.personalized_improvements).uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            report.suggestions.take(3).forEach { suggestion ->
                SuggestionCard(suggestion)
            }
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
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
private fun HealthStatusHeader(status: FinancialBalanceStatus) {
    val (statusLabel, statusColor) = when (status) {
        FinancialBalanceStatus.HEALTHY -> stringResource(R.string.status_excellent) to Color(0xFF34C759)
        FinancialBalanceStatus.MODERATE -> stringResource(R.string.status_stable) to Color(0xFFFF9500)
        FinancialBalanceStatus.RISK -> stringResource(R.string.status_action_required) to Color(0xFFFF3B30)
    }

    Surface(
        color = statusColor.copy(alpha = 0.1f),
        shape = CircleShape
    ) {
        Text(
            text = statusLabel,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.Black,
                letterSpacing = 1.2.sp
            ),
            color = statusColor
        )
    }
}

@Composable
private fun SnapshotSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.md)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                content()
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String, 
    value: Double, 
    isPrimary: Boolean = false,
    color: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = if (isPrimary) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = if (isPrimary) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "₹${"%,.0f".format(value)}",
            style = if (isPrimary) {
                MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
            } else {
                MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            },
            color = color
        )
    }
}

@Composable
private fun SuggestionCard(suggestion: SavingSuggestion) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = suggestion.title,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = suggestion.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )
        }
    }
}
