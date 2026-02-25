package com.monetra.presentation.screen.investments

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.domain.model.*
import com.monetra.presentation.components.HelpIconButton
import com.monetra.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    viewModel: InvestmentViewModel = hiltViewModel()
) {
    val investments by viewModel.investments.collectAsStateWithLifecycle()
    val intelligence by viewModel.wealthIntelligence.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text("Wealth Intelligence", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    HelpIconButton(onClick = onNavigateToHelp)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleAddSheet(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Investment")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            intelligence?.let { intel ->
                // 1. Hero Financial Safety Card
                item {
                    HeroSurvivalCard(intel)
                }

                // New: Liquidity Hierarchy Visualization
                item {
                    LiquidityBreakdownSection(intel)
                }

                // 2. Asset Allocation Overview
                item {
                    AllocationSection(intel.assetAllocation)
                }

                // 3. Smart Assistant Section
                if (intel.insights.isNotEmpty()) {
                    item {
                        WealthInsightsSection(intel.insights)
                    }
                }

                // 4. Wealth Projection
                item {
                    ProjectionCard(intel.wealthProjection)
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = Spacing.sm), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                item {
                    Text("Portfolio Breakdown", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }

                // 6. Asset List
                itemsIndexed(investments) { _, inv ->
                    InvestmentCard(inv = inv, onDelete = { viewModel.onDeleteInvestment(inv) })
                }
            } ?: item {
                EmptyPortfolioPlaceholder()
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (uiState.isAddSheetOpen) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        AddInvestmentSheet(
            uiState = uiState,
            sheetState = sheetState,
            onNameChange = viewModel::onNameChange,
            onCurrentValueChange = viewModel::onCurrentValueChange,
            onInvestedChange = viewModel::onInvestedChange,
            onMonthlyAmountChange = viewModel::onMonthlyAmountChange,
            onTypeChange = viewModel::onTypeChange,
            onToggleMonthly = viewModel::onToggleMonthly,
            onSave = viewModel::onSaveInvestment,
            onDismiss = { viewModel.toggleAddSheet(false) }
        )
    }
}

@Composable
private fun HeroSurvivalCard(intel: com.monetra.domain.model.WealthIntelligence) {
    val statusColor = when(intel.safetyStatus) {
        com.monetra.domain.model.SafetyStatus.GREEN -> Color(0xFF34C759)
        com.monetra.domain.model.SafetyStatus.YELLOW -> Color(0xFFFFCC00)
        com.monetra.domain.model.SafetyStatus.RED -> Color(0xFFFF3B30)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            statusColor.copy(alpha = 0.15f),
                            statusColor.copy(alpha = 0.05f)
                        )
                    )
                )
                .fillMaxWidth()
                .padding(Spacing.xl)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Financial Survival Status",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(Spacing.xs))
                        Text(
                            "₹%,.0f".format(intel.totalNetWorth),
                            style = MaterialTheme.typography.headlineLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-1).sp
                            )
                        )
                    }
                    Surface(
                        color = statusColor.copy(alpha = 0.2f),
                        contentColor = statusColor,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = intel.safetyStatus.name,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                        )
                    }
                }
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                SurvivalStat(
                    label = "Survival Buffer", 
                    value = "${"%.1f".format(intel.emergencyRunwayMonths)} mo",
                    icon = Icons.Default.Shield,
                    color = statusColor,
                    modifier = Modifier.weight(1f)
                )
                SurvivalStat(
                    label = "Liquid Wealth", 
                    value = "₹%,.0f".format(intel.liquidNetWorth),
                    icon = Icons.Default.WaterDrop,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = intel.safetyMessage,
                    modifier = Modifier.padding(Spacing.md),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}


}
@Composable
fun InvestmentTypeGrid(
    selectedType: InvestmentType,
    onTypeSelected: (InvestmentType) -> Unit
) {
    val allTypes = InvestmentType.entries
    val half = (allTypes.size + 1) / 2
    val firstRow = allTypes.subList(0, half)
    val secondRow = allTypes.subList(half, allTypes.size)

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        listOf(firstRow, secondRow).forEach { row ->
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                contentPadding = PaddingValues(horizontal = 2.dp)
            ) {
                items(row.size) { i ->
                    val type = row[i]
                    val isSelected = type == selectedType
                    val color = Color(type.colorHex)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onTypeSelected(type) },
                        label = {
                            Text(
                                "${type.emoji} ${type.displayName}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = color.copy(alpha = 0.15f),
                            selectedLabelColor = color
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            selectedBorderColor = color.copy(alpha = 0.5f),
                            selectedBorderWidth = 1.5.dp
                        )
                    )
                }
            }
        }
    }
}


@Composable fun LiquidityIndicator(label: String, amount: Double, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            Text("₹%,.0f".format(amount), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
        }
    }
}

@Composable
fun ProjectionCard(projection: com.monetra.domain.model.WealthProjection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text("Wealth Projection (8% return)", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(Spacing.md))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("1 Year Forecast", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹%,.0f".format(projection.expectedValue1Year), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("5 Year Forecast", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹%,.0f".format(projection.expectedValue5Years), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// ---- Investment Card ----
@Composable
fun InvestmentCard(inv: Investment, onDelete: () -> Unit) {
    val returns = inv.currentValuation - inv.investedAmount
    val returnPercent = if (inv.investedAmount > 0) (returns / inv.investedAmount) * 100.0 else 0.0
    val isPositive = returns >= 0
    val returnColor = if (isPositive) Color(0xFF34C759) else Color(0xFFFF3B30)
    val typeColor = Color(inv.type.colorHex)

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Remove Investment") },
            text = { Text("Remove \"${inv.name}\" from your portfolio?") },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text("Remove", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, typeColor.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(inv.type.emoji, fontSize = 22.sp)
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        inv.name,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.weight(1f)
                    )
                    if (inv.isMonthly) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                "₹${"%,.0f".format(inv.monthlyAmount)}/mo",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                Text(
                    inv.type.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = typeColor
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "₹${"%,.0f".format(inv.currentValuation)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            "Invested: ₹${"%,.0f".format(inv.investedAmount)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${if (isPositive) "+" else ""}₹${"%,.0f".format(returns)}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = returnColor
                        )
                        Text(
                            "${if (returnPercent >= 0) "+" else ""}${"%.1f".format(returnPercent)}%",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = returnColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))
            IconButton(onClick = { showDeleteConfirm = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

// ---- Empty State ----
@Composable
fun EmptyPortfolioPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.md)
    ) {
        Text("📊", fontSize = 64.sp)
        Text(
            "No investments yet",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            "Tap the button below to add your first SIP, FD, stocks or any investment to track your wealth.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// ---- Add Investment Bottom Sheet ----
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestmentSheet(
    uiState: InvestmentUiState,
    sheetState: SheetState,
    onNameChange: (String) -> Unit,
    onCurrentValueChange: (String) -> Unit,
    onInvestedChange: (String) -> Unit,
    onMonthlyAmountChange: (String) -> Unit,
    onTypeChange: (com.monetra.domain.model.InvestmentType) -> Unit,
    onToggleMonthly: (Boolean) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl)
                .padding(bottom = 48.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("Add Asset", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))

            // Asset Type Grid - Icon Selection
            Text("Select Asset Type", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            InvestmentTypeGrid(selectedType = uiState.type, onTypeSelected = onTypeChange)

            // Basic Info
            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text("Name (e.g. HDFC NIFTY 50)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                // Monthly vs One-time toggle
                Card(
                    modifier = Modifier.weight(1f).height(56.dp).clickable { onToggleMonthly(!uiState.isMonthly) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = if (uiState.isMonthly) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                    border = if (uiState.isMonthly) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(if (uiState.isMonthly) "🔄 Monthly SIP" else "💰 One-time", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                    }
                }

                OutlinedTextField(
                    value = if (uiState.isMonthly) uiState.monthlyAmount else uiState.investedLumpSum,
                    onValueChange = if (uiState.isMonthly) onMonthlyAmountChange else onInvestedChange,
                    label = { Text("Amount") },
                    prefix = { Text("₹") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = if (uiState.isMonthly) uiState.monthlyAmountError != null else uiState.investedLumpSumError != null
                )
            }

            // Advanced Options (Current Value)
            var showAdvanced by remember { mutableStateOf(false) }
            TextButton(onClick = { showAdvanced = !showAdvanced }) {
                Text(if (showAdvanced) "Hide Advanced" else "Add Current Value (Optional)", style = MaterialTheme.typography.labelMedium)
            }

            if (showAdvanced) {
                OutlinedTextField(
                    value = uiState.currentValue,
                    onValueChange = onCurrentValueChange,
                    label = { Text("Current Market Value") },
                    prefix = { Text("₹") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Confirm Asset Addition", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SurvivalStat(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun AllocationSection(allocation: List<com.monetra.domain.model.AssetAllocationItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text("Asset Allocation", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Row(
            modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp))
        ) {
            allocation.forEach { item ->
                Box(
                    modifier = Modifier
                        .weight(item.percentage.toFloat().coerceAtLeast(0.01f))
                        .fillMaxHeight()
                        .background(Color(item.type.colorHex))
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            allocation.take(4).forEach { item ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Color(item.type.colorHex)))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${item.type.displayName} ${item.percentage.toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
fun WealthInsightsSection(insights: List<com.monetra.domain.model.WealthInsight>) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text("Smart Assistant", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        insights.forEach { insight ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(modifier = Modifier.padding(Spacing.md), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column {
                        Text(insight.title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        Text(insight.message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun LiquidityBreakdownSection(intel: WealthIntelligence) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text("Liquidity Hierarchy", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            LiquidityIndicator(
                label = "Liquid",
                amount = intel.liquidNetWorth,
                color = Color(0xFF34C759),
                modifier = Modifier.weight(1f)
            )
            LiquidityIndicator(
                label = "Semi-liquid",
                amount = intel.semiLiquidAdjustedValue,
                color = Color(0xFFFFCC00),
                modifier = Modifier.weight(1f)
            )
            LiquidityIndicator(
                label = "Locked",
                amount = intel.lockedAssetsValue,
                color = Color(0xFF8E8E93),
                modifier = Modifier.weight(1f)
            )
        }
    }
}