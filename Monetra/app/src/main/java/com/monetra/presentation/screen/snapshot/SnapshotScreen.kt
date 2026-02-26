package com.monetra.presentation.screen.snapshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.domain.model.FinancialBalanceStatus
import com.monetra.domain.model.MonthlyFinancialReport
import com.monetra.domain.model.SavingSuggestion
import com.monetra.presentation.components.HelpIconButton
import com.monetra.ui.theme.Spacing
import androidx.compose.ui.res.stringResource
import com.monetra.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapshotScreen(
    onNavigateToHelp: () -> Unit,
    viewModel: SnapshotViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.snapshot_title_header),
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
                    SnapshotContent(state.report, state.suggestions)
                }
            }
        }
    }
}

@Composable
private fun SnapshotContent(report: MonthlyFinancialReport, suggestions: List<SavingSuggestion>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {
        // Health Status Header
        item {
            HealthStatusHeader(report.status)
        }

        // Primary Metrics
        item {
            SnapshotSection(title = stringResource(R.string.monthly_overview)) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                    MetricRow(label = stringResource(R.string.monthly_salary_label), value = report.income, isPrimary = true)
                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.xs), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    MetricRow(label = stringResource(R.string.total_expenses_label), value = report.totalExpenses)
                    MetricRow(label = stringResource(R.string.monthly_emi_label), value = report.totalEmis)
                    MetricRow(label = stringResource(R.string.investments_label), value = report.totalInvestments)
                }
            }
        }

        // Savings Analysis
        item {
            SnapshotSection(title = stringResource(R.string.savings_analysis)) {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                    MetricRow(label = stringResource(R.string.target_savings_label), value = report.targetSavings)
                    MetricRow(label = stringResource(R.string.actual_savings_label), value = report.actualSavings, color = if (report.actualSavings >= report.targetSavings) Color(0xFF34C759) else MaterialTheme.colorScheme.primary)
                    
                    if (report.savingsGap > 0) {
                        MetricRow(
                            label = stringResource(R.string.savings_gap_label), 
                            value = report.savingsGap, 
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        // Suggested Improvements
        if (suggestions.isNotEmpty()) {
            item {
                Text(
                    text = stringResource(R.string.suggested_improvements),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.sm)
                )
            }
            
            items(suggestions) { suggestion ->
                SuggestionCard(suggestion)
            }
        }
        
        item {
            Spacer(modifier = Modifier.height(Spacing.xxl))
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

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
        
        Spacer(modifier = Modifier.height(Spacing.md))
        
        Text(
            text = when(status) {
                FinancialBalanceStatus.HEALTHY -> stringResource(R.string.health_healthy_desc)
                FinancialBalanceStatus.MODERATE -> stringResource(R.string.health_moderate_desc)
                FinancialBalanceStatus.RISK -> stringResource(R.string.health_risk_desc)
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SnapshotSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            modifier = Modifier.padding(start = Spacing.xs, bottom = Spacing.md)
        )
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            // Minimal border for Cupertino look
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
