package com.monetra.presentation.screen.report

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import com.monetra.domain.model.*
import com.monetra.ui.theme.Spacing
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyReportScreen(
    onNavigateBack: () -> Unit,
    onNavigateToSimulator: () -> Unit,
    viewModel: MonthlyReportViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Monthly Analysis", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.generateReport() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is ReportUiState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                is ReportUiState.Error -> Text(state.message, modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.error)
                is ReportUiState.Success -> ReportContent(state.report, onNavigateToSimulator)
            }
        }
    }
}

@Composable
private fun ReportContent(report: ComprehensiveMonthlyReport, onNavigateToSimulator: () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.xl)
    ) {
        // 1. Overview Header
        OverviewHeader(report)

        // 2. Comparison Section
        if (report.comparison != null) {
            ComparisonSection(report.comparison)
        }

        // 3. Top Spending Categories
        if (report.topCategories.isNotEmpty()) {
            TopSpendingSection(report.topCategories)
        }

        // 4. What-If Simulator Entry
        SimulationEntryCard(onNavigateToSimulator)

        // 5. EMI Stress Card
        EMIStressCard(report.emiStressLevel)

        // 6. Suggestions
        if (report.suggestions.isNotEmpty()) {
            SuggestionsSection(report.suggestions)
        }

        Spacer(modifier = Modifier.height(Spacing.xxl))
    }
}

@Composable
private fun OverviewHeader(report: ComprehensiveMonthlyReport) {
    val monthName = report.month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    Column {
        Text(
            text = "$monthName, ${report.month.year}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Financial Performance",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
        )
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            ReportMetricCard(modifier = Modifier.weight(1f), label = "Total Income", value = report.income, color = Color(0xFF34C759))
            ReportMetricCard(modifier = Modifier.weight(1f), label = "Total Outlays", value = report.expenses + report.emis, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun ComparisonSection(comparison: PreviousMonthComparison) {
    ReportSection(title = "Versus Last Month") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            ComparisonRow("Monthly Income", comparison.incomeChangePercent)
            ComparisonRow("Total Expenses", comparison.expenseChangePercent, inverse = true)
            ComparisonRow("Actual Savings", comparison.savingsChangePercent)
            ComparisonRow("Investments", comparison.investmentChangePercent)
        }
    }
}

@Composable
private fun TopSpendingSection(categories: List<CategorySpending>) {
    ReportSection(title = "Top Spending Categories") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            categories.forEach { category ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(category.category, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "₹%,.0f".format(category.amount),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
                val maxAmount = categories.firstOrNull()?.amount ?: 1.0
                val progress = if (maxAmount > 0) (category.amount / maxAmount).toFloat() else 0f
                
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EMIStressCard(stressLevel: String) {
    val (color, icon, description) = when (stressLevel) {
        "CRITICAL" -> Triple(
            Color(0xFFFF3B30), Icons.Default.Warning,
            "EMIs exceed 50% of income — this is financially dangerous. Reduce debt immediately."
        )
        "HIGH" -> Triple(
            Color(0xFFFF9500), Icons.Default.Warning,
            "EMIs are between 35–50% of income — above the safe limit. Avoid new loans."
        )
        "MODERATE" -> Triple(
            Color(0xFF5856D6), Icons.Default.Info,
            "EMIs are 20–35% of income — manageable, but keep an eye on new commitments."
        )
        else -> Triple(
            Color(0xFF34C759), Icons.Default.CheckCircle,
            "EMIs are under 20% of income — excellent! You have a healthy debt load."
        )
    }

    val displayLabel = when (stressLevel) {
        "CRITICAL", "HIGH", "MODERATE" -> stressLevel
        else -> "HEALTHY"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(Spacing.lg), verticalAlignment = Alignment.Top) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(Spacing.md))
            Column {
                Text("EMI Stress Level", style = MaterialTheme.typography.labelSmall, color = color)
                Text(
                    displayLabel,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                    color = color
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    description,
                    style = MaterialTheme.typography.bodySmall,
                    color = color.copy(alpha = 0.8f)
                )
            }
        }
    }
}


@Composable
private fun SuggestionsSection(suggestions: List<SavingSuggestion>) {
    ReportSection(title = "Personalized Improvements") {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
            suggestions.take(3).forEach { suggestion ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(Spacing.md)) {
                        Text(suggestion.title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                        Text(suggestion.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}


@Composable
private fun ReportMetricCard(modifier: Modifier, label: String, value: Double, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "₹%,.0f".format(value),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = color
            )
        }
    }
}

@Composable
private fun ReportSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(letterSpacing = 1.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(Spacing.md))
        content()
    }
}

@Composable
private fun ComparisonRow(label: String, percent: Double, inverse: Boolean = false) {
    val isPositive = percent >= 0
    val isGood = if (inverse) !isPositive else isPositive
    val color = if (isGood) Color(0xFF34C759) else Color(0xFFFF3B30)
    
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Text(
            text = (if (isPositive) "+" else "") + "%.1f%%".format(percent),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = color
        )
    }
}
@Composable
private fun SimulationEntryCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("⚡", fontSize = 32.sp)
            Column(modifier = Modifier.weight(1f)) {
                Text("What-If Simulator", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                Text("Test life decisions and see their impact on your financial health score.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
