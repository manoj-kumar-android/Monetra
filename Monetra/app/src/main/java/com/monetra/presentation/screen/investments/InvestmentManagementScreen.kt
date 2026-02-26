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
                    ProjectionCard(
                        intel = intel,
                        onRateChange = viewModel::onSimulationRateChange,
                        onYearsChange = viewModel::onSimulationYearsChange
                    )
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = Spacing.sm), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = Spacing.sm), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }

                val monthlyInvestments = investments.filter { it.frequency == ContributionFrequency.MONTHLY }
                val oneTimeInvestments = investments.filter { it.frequency == ContributionFrequency.ONE_TIME }

                if (monthlyInvestments.isNotEmpty()) {
                    item {
                        Text("Monthly Investments (SIP/RD)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    items(monthlyInvestments) { inv ->
                        InvestmentCard(inv = inv, onDelete = { viewModel.onDeleteInvestment(inv) })
                    }
                    item { Spacer(modifier = Modifier.height(Spacing.sm)) }
                }

                if (oneTimeInvestments.isNotEmpty()) {
                    item {
                        Text("One-time Assets (FD/Stocks/Gold)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    items(oneTimeInvestments) { inv ->
                        InvestmentCard(inv = inv, onDelete = { viewModel.onDeleteInvestment(inv) })
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
            Text("Wealth Projection 🚀", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold))
            Text("Simulate your future wealth growth", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(Spacing.xl))
            
            // Visual Progress Bar
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
                    Text("Invested: ₹%,.0f".format(projection.totalInvested), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    Text("Returns: ₹%,.0f".format(projection.totalReturns), style = MaterialTheme.typography.labelSmall, color = Color(0xFF34C759))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Simulation Controls
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                SimulationSlider(
                    label = "Expected Return Rate",
                    value = projection.interestRate.toFloat(),
                    range = 1f..30f,
                    displayValue = "${projection.interestRate.toInt()}%",
                    onValueChange = { onRateChange(it.toDouble()) }
                )
                
                SimulationSlider(
                    label = "Projection Horizon",
                    value = projection.projectionYears.toFloat(),
                    range = 1f..30f,
                    displayValue = "${projection.projectionYears} Years",
                    onValueChange = { onYearsChange(it.toInt()) }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Breakdown
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(Spacing.lg), verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    BreakdownRow("Total Invested", projection.totalInvested)
                    BreakdownRow("Estimated Returns", projection.totalReturns)
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Final Wealth", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text("₹%,.0f".format(projection.finalWealth), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary))
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // Milestones
            Text("Yearly Milestones", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(Spacing.md))
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                projection.yearlyMilestones.keys.sorted().filter { it in listOf(1, 5, 10, 20, 30) }.forEach { year ->
                    MilestoneRow(year, projection.yearlyMilestones[year] ?: 0.0)
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
fun MilestoneRow(year: Int, amount: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .padding(horizontal = Spacing.md, vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = CircleShape,
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text("Y$year", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(modifier = Modifier.width(Spacing.md))
        Text("Projected Wealth", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.weight(1f))
        Text("₹%,.0f".format(amount), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Black))
    }
}

// ---- Investment Card ----
@Composable
fun InvestmentCard(inv: Investment, onDelete: () -> Unit) {
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
                    Text("Invested capital", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹%,.0f".format(invested), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Current value", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("₹%,.0f".format(currentVal), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary))
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(returnColor.copy(alpha = 0.05f)).padding(Spacing.sm), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown, contentDescription = null, tint = returnColor, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text("${if (isPositive) "+" else ""}₹%,.0f".format(returns), style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = returnColor)
                }
                Text("${if (returnPercent >= 0) "+" else ""}${"%.2f".format(returnPercent)}%", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Black), color = returnColor)
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
                DetailItem("Started", inv.startDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMM yy")), modifier = Modifier.weight(1f))
                if (inv.interestRate > 0) {
                    DetailItem("Yield", "${inv.interestRate}%", modifier = Modifier.weight(1f))
                }
                if (inv.frequency == ContributionFrequency.MONTHLY) {
                    DetailItem("Frequency", "Monthly", modifier = Modifier.weight(1f))
                } else {
                    DetailItem("Frequency", "One-time", modifier = Modifier.weight(1f))
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
    onAmountChange: (String) -> Unit,
    onMonthlyAmountChange: (String) -> Unit,
    onTypeChange: (com.monetra.domain.model.InvestmentType) -> Unit,
    onInterestRateChange: (String) -> Unit,
    onStartDateChange: (java.time.LocalDate) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli(),
            selectableDates = object : androidx.compose.material3.SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    return utcTimeMillis <= System.currentTimeMillis()
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return year <= java.time.LocalDate.now().year
                }
            }
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val date = java.time.Instant.ofEpochMilli(it)
                            .atZone(java.time.ZoneId.systemDefault())
                            .toLocalDate()
                        onStartDateChange(date)
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = Spacing.xl)
                .padding(bottom = 48.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text("Add Investment", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))

            // 1. Asset Type Grid
            Text("Investment Type", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
            InvestmentTypeGrid(selectedType = uiState.type, onTypeSelected = { onTypeChange(it) })

            // Display Frequency Badge
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            ) {
                Text(
                    text = if (uiState.frequency == ContributionFrequency.MONTHLY) "🔄 Monthly Investment" else "💰 One-time Investment",
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // 2. Core Details
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
                // AMOUNT: Only one shown based on frequency
                if (uiState.frequency == ContributionFrequency.MONTHLY) {
                    OutlinedTextField(
                        value = uiState.monthlyAmount,
                        onValueChange = onMonthlyAmountChange,
                        label = { Text("Monthly SIP") },
                        prefix = { Text("₹") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.monthlyAmountError != null
                    )
                } else {
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = onAmountChange,
                        label = { Text("Lump Sum Amount") },
                        prefix = { Text("₹") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.amountError != null
                    )
                }

                // INTEREST RATE: Only for logic-based (FD, RD, PPF, Insurance etc)
                val needsInterest = uiState.type !in listOf(
                    InvestmentType.STOCK, InvestmentType.CRYPTO, InvestmentType.GOLD, 
                    InvestmentType.MUTUAL_FUND, InvestmentType.OTHER, InvestmentType.REAL_ESTATE,
                    InvestmentType.CASH
                )
                
                if (needsInterest) {
                    OutlinedTextField(
                        value = uiState.interestRate,
                        onValueChange = onInterestRateChange,
                        label = { Text("Returns %") },
                        suffix = { Text("%") },
                        modifier = Modifier.weight(0.8f),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }

            // 3. Date Selection
            OutlinedTextField(
                value = uiState.startDate.format(java.time.format.DateTimeFormatter.ofPattern("dd MMMM yyyy")),
                onValueChange = {},
                readOnly = true,
                label = { Text("Start Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null)
                    }
                },
                modifier = Modifier.fillMaxWidth().clickable { showDatePicker = true },
                shape = RoundedCornerShape(12.dp)
            )

            // 4. Market-based value (if applicable)
            val isMarketBased = uiState.type in listOf(
                InvestmentType.STOCK, InvestmentType.CRYPTO, InvestmentType.GOLD, 
                InvestmentType.MUTUAL_FUND, InvestmentType.OTHER, InvestmentType.REAL_ESTATE
            )

            if (isMarketBased) {
                OutlinedTextField(
                    value = uiState.currentValue,
                    onValueChange = onCurrentValueChange,
                    label = { Text("Current Market Value") },
                    prefix = { Text("₹") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { Text("Leave blank if same as amount", style = MaterialTheme.typography.labelSmall) }
                )
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Save Investment", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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