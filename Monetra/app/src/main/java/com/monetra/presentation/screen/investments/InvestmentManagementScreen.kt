package com.monetra.presentation.screen.investments

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.res.stringResource
import com.monetra.R

private val shortDateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yy")
private val fullDateFormatter = java.time.format.DateTimeFormatter.ofPattern("dd MMM yyyy")

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

    val hapticAddClick = com.monetra.presentation.components.rememberHapticClick { 
        viewModel.toggleAddSheet(true) 
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(R.string.wealth_intelligence_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
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
                onClick = hapticAddClick,
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_investment))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            intelligence?.let { intel ->
                item {
                    HeroSurvivalCard(intel)
                }

                item {
                    LiquidityBreakdownSection(intel)
                }

                item {
                    AllocationSection(intel.assetAllocation)
                }



                item {
                    ProjectionCard(
                        intel = intel,
                        onRateChange = viewModel::onSimulationRateChange,
                        onYearsChange = viewModel::onSimulationYearsChange
                    )
                }

                item {
                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.sm), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                val monthlyInvestments = investments.filter { it.frequency == ContributionFrequency.MONTHLY }
                val oneTimeInvestments = investments.filter { it.frequency == ContributionFrequency.ONE_TIME }

                if (monthlyInvestments.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.monthly_investments_header), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    items(monthlyInvestments, key = { it.id }, contentType = { "investment" }) { inv ->
                        InvestmentCard(
                            inv = inv, 
                            onDelete = { viewModel.onDeleteInvestment(inv) },
                            onEdit = { viewModel.onEditInvestment(inv) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(Spacing.sm)) }
                }

                if (oneTimeInvestments.isNotEmpty()) {
                    item {
                        Text(stringResource(R.string.onetime_investments_header), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    items(oneTimeInvestments, key = { it.id }, contentType = { "investment" }) { inv ->
                        InvestmentCard(
                            inv = inv, 
                            onDelete = { viewModel.onDeleteInvestment(inv) },
                            onEdit = { viewModel.onEditInvestment(inv) }
                        )
                    }
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
            onAmountChange = viewModel::onAmountChange,
            onMonthlyAmountChange = viewModel::onMonthlyAmountChange,
            onTypeChange = viewModel::onTypeChange,
            onInterestRateChange = viewModel::onInterestRateChange,
            onStartDateChange = viewModel::onStartDateChange,
            onEndDateChange = viewModel::onEndDateChange,
            onAddStepChange = viewModel::onAddStepChange,
            onRemoveStepChange = viewModel::onRemoveStepChange,
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
                            stringResource(R.string.financial_survival_status),
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
                    label = stringResource(R.string.survival_buffer), 
                    value = "${"%.1f".format(intel.emergencyRunwayMonths)} mo",
                    icon = Icons.Default.Shield,
                    color = statusColor,
                    modifier = Modifier.weight(1f)
                )
                SurvivalStat(
                    label = stringResource(R.string.liquid_wealth), 
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
        Text(stringResource(R.string.asset_allocation_label), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
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
fun LiquidityBreakdownSection(intel: WealthIntelligence) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        Text(stringResource(R.string.liquidity_hierarchy_label), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            LiquidityIndicator(
                label = stringResource(R.string.liquid_label),
                amount = intel.liquidNetWorth,
                color = Color(0xFF34C759),
                modifier = Modifier.weight(1f)
            )
            LiquidityIndicator(
                label = stringResource(R.string.semi_liquid_label),
                amount = intel.semiLiquidAdjustedValue,
                color = Color(0xFFFFCC00),
                modifier = Modifier.weight(1f)
            )
            LiquidityIndicator(
                label = stringResource(R.string.locked_label),
                amount = intel.lockedAssetsValue,
                color = Color(0xFF8E8E93),
                modifier = Modifier.weight(1f)
            )
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
fun ProjectionCard(
    intel: WealthIntelligence,
    onRateChange: (Double) -> Unit,
    onYearsChange: (Int) -> Unit
) {
    val projection = intel.wealthProjection
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(Spacing.xl)) {
            Text(stringResource(R.string.wealth_projection_title), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
            Text(stringResource(R.string.wealth_projection_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            val investedRatio = if (projection.finalWealth > 0) (projection.totalInvested / projection.finalWealth).toFloat() else 0f
            Column(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight(investedRatio.coerceAtLeast(0.01f))
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .weight((1f - investedRatio).coerceAtLeast(0.01f))
                                .background(Color(0xFF34C759).copy(alpha = 0.6f))
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.invested_label_format, projection.totalInvested), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text(stringResource(R.string.returns_label_format, projection.totalReturns), style = MaterialTheme.typography.labelSmall, color = Color(0xFF34C759))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                SimulationSlider(
                    label = stringResource(R.string.expected_return_rate),
                    value = projection.interestRate.toFloat(),
                    range = 1f..30f,
                    displayValue = "${projection.interestRate.toInt()}%",
                    onValueChange = { onRateChange(it.toDouble()) }
                )
                
                SimulationSlider(
                    label = stringResource(R.string.projection_horizon),
                    value = projection.projectionYears.toFloat(),
                    range = 1f..30f,
                    displayValue = "${projection.projectionYears} Years",
                    onValueChange = { onYearsChange(it.toInt()) }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    BreakdownRow(stringResource(R.string.total_invested_label), projection.totalInvested)
                    BreakdownRow(stringResource(R.string.estimated_returns_label), projection.totalReturns)
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.final_wealth_label), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("₹%,.0f".format(projection.finalWealth), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary))
                    }
                }
            }
        }
    }
}

@Composable
fun SimulationSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    displayValue: String,
    onValueChange: (Float) -> Unit
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(displayValue, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = range,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun BreakdownRow(label: String, amount: Double) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("₹%,.0f".format(amount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun InvestmentCard(inv: Investment, onDelete: () -> Unit, onEdit: () -> Unit) {
    val today = java.time.LocalDate.now()
    val invested = inv.calculateTotalInvested(today)
    val currentVal = inv.calculateCurrentValue(today)
    val returns = inv.calculateTotalReturns(today)
    val returnPercent = inv.calculateReturnPercentage(today)
    val isPositive = returns >= 0
    val returnColor = if (isPositive) Color(0xFF34C759) else Color(0xFFFF3B30)
    val typeColor = Color(inv.type.colorHex)

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.remove_investment_title)) },
            text = { Text(stringResource(R.string.remove_investment_msg, inv.name)) },
            confirmButton = {
                TextButton(onClick = { onDelete(); showDeleteConfirm = false }) {
                    Text(stringResource(R.string.remove), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, typeColor.copy(alpha = 0.1f))
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(typeColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(inv.type.emoji, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(Spacing.md))
                Column(modifier = Modifier.weight(1f)) {
                    Text(inv.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text(inv.type.displayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(stringResource(R.string.invested_capital_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹%,.0f".format(invested), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(stringResource(R.string.current_value_label), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹%,.0f".format(currentVal), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(returnColor.copy(alpha = 0.05f)).padding(Spacing.sm), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown, contentDescription = null, tint = returnColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text("${if (isPositive) "+" else ""}₹%,.0f".format(returns), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = returnColor)
                }
                Text("${if (returnPercent >= 0) "+" else ""}${"%.2f".format(returnPercent)}%", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black), color = returnColor)
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                DetailItem(stringResource(R.string.started), inv.startDate.format(shortDateFormatter), modifier = Modifier.weight(1f))
                if (inv.interestRate > 0) {
                    DetailItem(stringResource(R.string.yield_label), "${inv.interestRate}%", modifier = Modifier.weight(1f))
                }
                if (inv.frequency == ContributionFrequency.MONTHLY) {
                    DetailItem(stringResource(R.string.frequency_label), stringResource(R.string.monthly_label), modifier = Modifier.weight(1f))
                } else {
                    DetailItem(stringResource(R.string.frequency_label), stringResource(R.string.onetime_label), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
    }
}

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
            stringResource(R.string.no_investments_title),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            stringResource(R.string.no_investments_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddInvestmentSheet(
    uiState: InvestmentUiState,
    sheetState: SheetState,
    onNameChange: (String) -> Unit,
    onCurrentValueChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onMonthlyAmountChange: (String) -> Unit,
    onTypeChange: (com.monetra.domain.model.InvestmentType) -> Unit,
    onInterestRateChange: (String) -> Unit,
    onStartDateChange: (java.time.LocalDate) -> Unit,
    onEndDateChange: (java.time.LocalDate?) -> Unit,
    onAddStepChange: (Double, java.time.LocalDate) -> Unit,
    onRemoveStepChange: (com.monetra.domain.model.StepChange) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showStepDialog by remember { mutableStateOf(false) }
    
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onStartDateChange(java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate())
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.endDate?.atStartOfDay(java.time.ZoneId.systemDefault())?.toInstant()?.toEpochMilli() ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        onEndDateChange(java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate())
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) { DatePicker(state = datePickerState) }
    }

    if (showStepDialog) {
        var stepAmount by remember { mutableStateOf("") }
        var stepDate by remember { mutableStateOf(java.time.LocalDate.now()) }
        var showStepDatePicker by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showStepDialog = false },
            title = { Text("Add Step-Up/Down") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                    OutlinedTextField(
                        value = stepAmount,
                        onValueChange = { stepAmount = it.filter { c -> c.isDigit() || c == '.' } },
                        label = { Text("New Monthly Amount") },
                        prefix = { Text("₹") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = stepDate.format(fullDateFormatter),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Effective Date") },
                        trailingIcon = {
                            IconButton(onClick = { showStepDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().clickable { showStepDatePicker = true }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val amt = stepAmount.toDoubleOrNull() ?: 0.0
                    if (amt > 0) {
                        onAddStepChange(amt, stepDate)
                        showStepDialog = false
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = { showStepDialog = false }) { Text("Cancel") }
            }
        )

        if (showStepDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = stepDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showStepDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            stepDate = java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        }
                        showStepDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showStepDatePicker = false }) { Text("Cancel") }
                }
            ) { DatePicker(state = datePickerState) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl)
                .padding(bottom = 48.dp)
                .navigationBarsPadding()
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                if (uiState.editingId != null) stringResource(R.string.edit_investment) else stringResource(R.string.add_investment),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )

            Text(stringResource(R.string.investment_type_label), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            InvestmentTypeGrid(selectedType = uiState.type, onTypeSelected = { onTypeChange(it) })

            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Text(
                    text = if (uiState.frequency == ContributionFrequency.MONTHLY) stringResource(R.string.monthly_investment_tag) else stringResource(R.string.onetime_investment_tag),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            OutlinedTextField(
                value = uiState.name,
                onValueChange = onNameChange,
                label = { Text(stringResource(R.string.investment_name_placeholder)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = uiState.nameError != null,
                supportingText = uiState.nameError?.let { { Text(it) } }
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                if (uiState.frequency == ContributionFrequency.MONTHLY) {
                    OutlinedTextField(
                        value = uiState.monthlyAmount,
                        onValueChange = onMonthlyAmountChange,
                        label = { Text(stringResource(R.string.monthly_sip_label)) },
                        prefix = { Text(stringResource(R.string.rupee_symbol)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.monthlyAmountError != null
                    )
                } else {
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = onAmountChange,
                        label = { Text(stringResource(R.string.lump_sum_label)) },
                        prefix = { Text(stringResource(R.string.rupee_symbol)) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.amountError != null
                    )
                }

                val needsInterest = uiState.type !in listOf(
                    InvestmentType.STOCK, InvestmentType.CRYPTO, InvestmentType.GOLD,
                    InvestmentType.MUTUAL_FUND, InvestmentType.OTHER, InvestmentType.REAL_ESTATE,
                    InvestmentType.CASH
                )

                if (needsInterest) {
                    OutlinedTextField(
                        value = uiState.interestRate,
                        onValueChange = onInterestRateChange,
                        label = { Text(stringResource(R.string.returns_percent_label)) },
                        suffix = { Text("%") },
                        modifier = Modifier.weight(0.8f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
                OutlinedTextField(
                    value = uiState.startDate.format(fullDateFormatter),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Start Date") },
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null)
                        }
                    },
                    modifier = Modifier.weight(1f).clickable { showStartDatePicker = true },
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = uiState.endDate?.format(fullDateFormatter) ?: "Ongoing",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("End Date (Optional)") },
                    trailingIcon = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(if (uiState.endDate == null) Icons.Default.CalendarToday else Icons.Default.Close, contentDescription = null)
                        }
                    },
                    modifier = Modifier.weight(1f).clickable { if (uiState.endDate != null) onEndDateChange(null) else showEndDatePicker = true },
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (uiState.frequency == ContributionFrequency.MONTHLY) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text("Step-Up/Down History", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))

                uiState.stepChanges.forEach { step ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("₹%,.0f from ${step.effectiveDate.format(shortDateFormatter)}".format(step.amount), style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = { onRemoveStepChange(step) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }

                TextButton(onClick = { showStepDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Step Change")
                }
            }

            val isMarketBased = uiState.type in listOf(
                InvestmentType.STOCK, InvestmentType.CRYPTO, InvestmentType.GOLD,
                InvestmentType.MUTUAL_FUND, InvestmentType.OTHER, InvestmentType.REAL_ESTATE
            )

            if (isMarketBased) {
                OutlinedTextField(
                    value = uiState.currentValue,
                    onValueChange = onCurrentValueChange,
                    label = { Text(stringResource(R.string.current_market_value_label)) },
                    prefix = { Text(stringResource(R.string.rupee_symbol)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Leave blank for auto-calculation based on returns profile", style = MaterialTheme.typography.labelSmall) }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.md)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.total_invested_label), style = MaterialTheme.typography.labelMedium)
                        Text(stringResource(R.string.expected_wealth_label), style = MaterialTheme.typography.labelMedium)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("₹%,.0f".format(uiState.previewInvested), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("₹%,.0f".format(uiState.previewWealth), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary))
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
                ) {
                Text(
                    if (uiState.editingId != null) stringResource(R.string.save_changes) else stringResource(R.string.save_investment),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            Spacer(Modifier.height(Spacing.sm))
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