package com.monetra.presentation.screen.transactions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monetra.presentation.screen.transactions.*
import com.monetra.ui.theme.Elevation
import com.monetra.ui.theme.SemanticExpense
import com.monetra.ui.theme.SemanticIncome
import com.monetra.ui.theme.Spacing

@Composable
fun TransactionRow(
    item: TransactionUiItem,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.xs),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.none)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (item.isIncome) SemanticIncome.copy(alpha = 0.1f) else SemanticExpense.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.categoryEmoji, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.size(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (item.note.isNotBlank()) {
                    Text(
                        text = item.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.size(Spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.formattedDate,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    Text(
                        text = " • ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    )
                    Text(
                        text = item.formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
            Text(
                text = item.formattedAmount,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = if (item.isIncome) SemanticIncome else SemanticExpense
            )
        }
    }
}

@Composable
fun IntelligenceCard(intelligence: IntelligenceUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.xl)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Daily Safe to Spend",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = intelligence.dailySafeToSpend,
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            when(intelligence.burnRateStatus) {
                                "Critical" -> MaterialTheme.colorScheme.error
                                "Warning" -> Color(0xFFFFA500)
                                else -> Color(0xFF4CAF50)
                            }.copy(alpha = 0.2f)
                        )
                        .padding(horizontal = Spacing.sm, vertical = Spacing.xs)
                ) {
                    Text(
                        text = intelligence.burnRateStatus,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = when(intelligence.burnRateStatus) {
                            "Critical" -> MaterialTheme.colorScheme.error
                            "Warning" -> Color(0xFFFFA500)
                            else -> Color(0xFF4CAF50)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f))
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(
                        text = "Projected Month End",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = intelligence.projectedMonthEnd,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = intelligence.comparisonText,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (intelligence.burnRateStatus == "Stable") Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                    )
                }
            }
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = "Daily Avg: ${intelligence.dailyAverage}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun BudgetSection(budgets: List<CategoryBudgetUiModel>, onManageClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.lg)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Budget Guard", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                TextButton(onClick = onManageClick) { Text("Manage", style = MaterialTheme.typography.labelMedium) }
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                budgets.forEach { budget -> BudgetProgressRow(budget = budget) }
            }
        }
    }
}

@Composable
fun BudgetProgressRow(budget: CategoryBudgetUiModel) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Column {
                Text(text = budget.categoryName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Remaining: ${budget.remaining}", style = MaterialTheme.typography.labelSmall, color = if (budget.status == "Alert") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = budget.spent, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color(budget.progressColor))
                Text(text = " / ${budget.limit}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(modifier = Modifier.height(Spacing.xs))
        LinearProgressIndicator(
            progress = { budget.progress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = Color(budget.progressColor),
            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun RecurringSection(total: String, items: List<RecurringExpenseUiModel>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.lg)) {
            Column {
                Text(text = "Recurring Subscriptions", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                Text(text = "Monthly Total: $total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                items.take(3).forEach { item -> RecurringExpenseRow(item = item) }
            }
        }
    }
}

@Composable
fun RecurringExpenseRow(item: RecurringExpenseUiModel) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = item.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            Text(text = "Next: ${item.nextDate}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(text = item.amount, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun MonthlySummaryCard(summary: SummaryUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Spacing.xl)) {
            Text(
                text = "Available to Spend", 
                style = MaterialTheme.typography.labelLarge, 
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = summary.formattedBalance, 
                style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Black), 
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                SummaryColumn(
                    label = "Income", 
                    amount = summary.formattedIncome, 
                    isPositive = true,
                    modifier = Modifier.weight(1f)
                )
                SummaryColumn(
                    label = "Expenses", 
                    amount = summary.formattedExpense, 
                    isPositive = false,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryColumn(label: String, amount: String, isPositive: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isPositive) Color(0xFF34C759).copy(alpha = 0.05f) else Color(0xFFFF3B30).copy(alpha = 0.05f))
            .padding(Spacing.md)
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = amount, 
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), 
            color = if (isPositive) Color(0xFF34C759) else Color(0xFFFF3B30)
        )
    }
}

