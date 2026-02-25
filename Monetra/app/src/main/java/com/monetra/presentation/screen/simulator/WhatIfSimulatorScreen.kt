package com.monetra.presentation.screen.simulator

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.material3.*
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.domain.model.FinancialBalanceStatus
import com.monetra.domain.model.SimulationParams
import com.monetra.domain.model.SimulationResult
import com.monetra.ui.theme.Spacing
import com.monetra.presentation.components.HelpIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatIfSimulatorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    viewModel: WhatIfSimulatorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val params by viewModel.params.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "What-If Simulator",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            "Safe future planning — no real money at risk",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    HelpIconButton(onClick = onNavigateToHelp)
                    IconButton(onClick = { viewModel.reset() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reset all")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is SimulatorUiState.Loading -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        CircularProgressIndicator()
                        Text("Fetching your financial data…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                is SimulatorUiState.PremiumLocked -> PremiumLockedOverlay()
                is SimulatorUiState.Error -> {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(Spacing.xl),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        Icon(Icons.Default.Warning, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp), contentDescription = null)
                        Text(state.message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
                    }
                }
                is SimulatorUiState.Success -> {
                    SimulatorContent(
                        result = state.result,
                        params = params,
                        onSalaryChange = viewModel::updateSalaryDelta,
                        onEmiChange = viewModel::updateNewEmi,
                        onSipChange = viewModel::updateNewSip,
                        onTargetChange = viewModel::updateSavingsTargetDelta,
                        onReset = viewModel::reset
                    )
                }
            }
        }
    }
}

@Composable
private fun SimulatorContent(
    result: SimulationResult,
    params: SimulationParams,
    onSalaryChange: (Double) -> Unit,
    onEmiChange: (Double) -> Unit,
    onSipChange: (Double) -> Unit,
    onTargetChange: (Double) -> Unit,
    onReset: () -> Unit
) {
    // Edge case: flag when projections are unhealthy
    val totalProjectedCommitments = result.projectedEmis + result.projectedInvestments + result.projectedExpenses
    val isOverLoaded = totalProjectedCommitments > result.projectedIncome
    val isEmiDangerous = result.projectedEmiRatio > 40
    val isSipUnaffordable = result.projectedInvestments > (result.projectedIncome * 0.5)
    val hasAnyChange = params.salaryDelta != 0.0 || params.newEmiAmount != 0.0 || params.newSipAmount != 0.0 || params.savingsTargetDelta != 0.0

    // Safe max for EMI + SIP: 60% of projected income
    val projectedIncome = result.projectedIncome
    val safeMaxEmi = ((projectedIncome * 0.5) - result.currentEmis).coerceAtLeast(0.0)
    val safeMaxSip = ((projectedIncome * 0.4) - result.currentInvestments).coerceAtLeast(0.0)

    var isAnySliderActive by remember { mutableStateOf(false) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // While a slider thumb is active, consume the Y component so the
                // parent Column does not scroll during a horizontal drag.
                return if (isAnySliderActive && source == NestedScrollSource.UserInput) {
                    Offset(0f, available.y)  // absorb the vertical movement
                } else Offset.Zero
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // ---- 1. ANIMATED STATUS BANNER ----
        AnimatedStatusBanner(result, hasAnyChange)

        // ---- 2. BEFORE / AFTER SNAPSHOT (always rendered to avoid layout shift) ----
        BeforeAfterSnapshot(result)

        // ---- 3. WARNING CARDS ----
        if (isOverLoaded) {
            WarningCard(
                icon = Icons.Default.Warning,
                color = Color(0xFFF44336),
                title = "Budget Overloaded! ⚠️",
                message = "Your total commitments (₹${"%,.0f".format(totalProjectedCommitments)}) exceed projected income (₹${"%,.0f".format(result.projectedIncome)}). Reduce EMI or SIP to bring this under control."
            )
        } else if (isEmiDangerous) {
            WarningCard(
                icon = Icons.Default.Warning,
                color = Color(0xFFFF9800),
                title = "EMI Load is Too High",
                message = "EMI ratio ${"%,.1f".format(result.projectedEmiRatio)}% is above the safe 40% limit. Try reducing new EMI or increasing salary."
            )
        } else if (isSipUnaffordable) {
            WarningCard(
                icon = Icons.Default.Info,
                color = Color(0xFF5856D6),
                title = "SIP Amount is Very Aggressive",
                message = "Investing more than 50% of income is risky. Make sure you have at least 3 months emergency fund first."
            )
        }

        // ---- 4. SLIDERS ----
        SimulatorControls(
            result = result,
            params = params,
            safeMaxEmi = safeMaxEmi,
            safeMaxSip = safeMaxSip,
            onSalaryChange = onSalaryChange,
            onEmiChange = onEmiChange,
            onSipChange = onSipChange,
            onTargetChange = onTargetChange,
            onDragStateChange = { isAnySliderActive = it }
        )

        // ---- 5. IMPACT ANALYSIS ----
        ImpactAnalysisSection(result)

        // ---- 6. 12-MONTH GRAPH ----
        FutureProjectionGraph(result)

        // ---- 7. RESET BUTTON (AnimatedVisibility — no layout shift) ----
        androidx.compose.animation.AnimatedVisibility(visible = hasAnyChange) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text("Reset All Parameters")
            }
        }

        Spacer(modifier = Modifier.height(Spacing.xxl))
    }
}

@Composable
private fun AnimatedStatusBanner(result: SimulationResult, hasAnyChange: Boolean) {
    val healthColor by animateColorAsState(
        targetValue = when (result.projectedStatus) {
            FinancialBalanceStatus.HEALTHY -> Color(0xFF4CAF50)
            FinancialBalanceStatus.MODERATE -> Color(0xFFFFC107)
            FinancialBalanceStatus.RISK -> Color(0xFFF44336)
        },
        animationSpec = tween(500),
        label = "healthColor"
    )

    val animatedScore by animateFloatAsState(
        targetValue = result.projectedHealthScore / 100f,
        animationSpec = tween(700),
        label = "score"
    )

    val statusLabel = when (result.projectedStatus) {
        FinancialBalanceStatus.HEALTHY -> "✅ Healthy Budget"
        FinancialBalanceStatus.MODERATE -> "⚠️ Moderate Risk"
        FinancialBalanceStatus.RISK -> "🚨 Budget at Risk!"
    }

    val statusSubtitle = when (result.projectedStatus) {
        FinancialBalanceStatus.HEALTHY -> "Your finances look safe with this scenario."
        FinancialBalanceStatus.MODERATE -> "This scenario stretches your budget a bit."
        FinancialBalanceStatus.RISK -> "This scenario could cause financial stress!"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = healthColor.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, healthColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (!hasAnyChange) {
                Text(
                    "Adjust the sliders below\nto simulate a scenario",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Spacing.md))
            }

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(140.dp)) {
                CircularProgressIndicator(
                    progress = { 1f },
                    modifier = Modifier.fillMaxSize(),
                    color = healthColor.copy(alpha = 0.15f),
                    strokeWidth = 12.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                CircularProgressIndicator(
                    progress = { animatedScore },
                    modifier = Modifier.fillMaxSize(),
                    color = healthColor,
                    strokeWidth = 12.dp,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${result.projectedHealthScore}",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "/ 100",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))
            Text(statusLabel, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = healthColor)
            Spacer(modifier = Modifier.height(2.dp))
            Text(statusSubtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun BeforeAfterSnapshot(result: SimulationResult) {
    val currentSavings = result.currentIncome - result.currentExpenses - result.currentEmis - result.currentInvestments
    val hasChange = result.projectedIncome != result.currentIncome ||
        result.projectedEmis != result.currentEmis ||
        result.projectedInvestments != result.currentInvestments

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Financial Snapshot",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    if (hasChange) "Current → Projected" else "Move sliders to simulate",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            SnapshotRow("Income", result.currentIncome, result.projectedIncome)
            SnapshotRow("Fixed Expenses", result.currentExpenses, result.projectedExpenses)
            SnapshotRow("EMIs", result.currentEmis, result.projectedEmis)
            SnapshotRow("Investments / SIP", result.currentInvestments, result.projectedInvestments)
            Divider(modifier = Modifier.padding(vertical = Spacing.xs))
            SnapshotRow("Net Savings", currentSavings, result.projectedSavings, highlight = true)
        }
    }
}


@Composable
private fun SnapshotRow(label: String, before: Double, after: Double, highlight: Boolean = false) {
    val diff = after - before
    val diffColor = when {
        label.contains("EMI") || label.contains("Expense") -> if (diff > 0) Color(0xFFF44336) else Color(0xFF4CAF50)
        else -> if (diff >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
    }
    val diffPrefix = if (diff >= 0) "+" else ""
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = if (highlight) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            Text(
                "₹${"%,.0f".format(before)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text("→", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "₹${"%,.0f".format(after)}",
                style = if (highlight) MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold) else MaterialTheme.typography.bodySmall,
                color = if (after != before) diffColor else MaterialTheme.colorScheme.onSurface
            )
            if (diff != 0.0) {
                Text(
                    "(${diffPrefix}₹${"%,.0f".format(Math.abs(diff))})",
                    style = MaterialTheme.typography.labelSmall,
                    color = diffColor
                )
            }
        }
    }
}

@Composable
private fun WarningCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    title: String,
    message: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(modifier = Modifier.padding(Spacing.md)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp).padding(top = 2.dp))
            Spacer(modifier = Modifier.width(Spacing.sm))
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = color)
                Spacer(modifier = Modifier.height(2.dp))
                Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun SimulatorControls(
    result: SimulationResult,
    params: SimulationParams,
    safeMaxEmi: Double,
    safeMaxSip: Double,
    onSalaryChange: (Double) -> Unit,
    onEmiChange: (Double) -> Unit,
    onSipChange: (Double) -> Unit,
    onTargetChange: (Double) -> Unit,
    onDragStateChange: (Boolean) -> Unit
) {
    // Track each slider's drag state
    val salaryInteraction = remember { MutableInteractionSource() }
    val emiInteraction = remember { MutableInteractionSource() }
    val sipInteraction = remember { MutableInteractionSource() }
    val targetInteraction = remember { MutableInteractionSource() }

    val salaryDragging by salaryInteraction.collectIsDraggedAsState()
    val salaryPressed by salaryInteraction.collectIsPressedAsState()
    val emiDragging by emiInteraction.collectIsDraggedAsState()
    val emiPressed by emiInteraction.collectIsPressedAsState()
    val sipDragging by sipInteraction.collectIsDraggedAsState()
    val sipPressed by sipInteraction.collectIsPressedAsState()
    val targetDragging by targetInteraction.collectIsDraggedAsState()
    val targetPressed by targetInteraction.collectIsPressedAsState()

    val anyActive = salaryDragging || salaryPressed || emiDragging || emiPressed ||
        sipDragging || sipPressed || targetDragging || targetPressed

    LaunchedEffect(anyActive) { onDragStateChange(anyActive) }

    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text("Adjust Parameters", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        // Salary slider
        SimulatorSlider(
            label = "Salary Hike / Increase",
            emoji = "💰",
            hint = "e.g. Set to ₹10,000 if expecting a raise next month",
            value = params.salaryDelta,
            hardMax = 300000f,
            safeMax = null,
            prefix = "+₹",
            onValueChange = onSalaryChange,
            warningThreshold = null,
            interactionSource = salaryInteraction
        )

        // EMI slider — capped at safe max
        SimulatorSlider(
            label = "New Monthly EMI",
            emoji = "🏠",
            hint = "Want a car loan or home loan? Add that EMI here. Safe cap: ₹${"%,.0f".format(safeMaxEmi)}",
            value = params.newEmiAmount,
            hardMax = (result.projectedIncome * 0.6).toFloat().coerceAtLeast(50000f),
            safeMax = safeMaxEmi.toFloat(),
            prefix = "₹",
            onValueChange = onEmiChange,
            warningThreshold = safeMaxEmi,
            interactionSource = emiInteraction
        )

        // SIP slider — capped at safe max
        SimulatorSlider(
            label = "New Monthly SIP / Investment",
            emoji = "📈",
            hint = "Planning to start a new SIP? Safe max: ₹${"%,.0f".format(safeMaxSip)}",
            value = params.newSipAmount,
            hardMax = (result.projectedIncome * 0.5).toFloat().coerceAtLeast(50000f),
            safeMax = safeMaxSip.toFloat(),
            prefix = "₹",
            onValueChange = onSipChange,
            warningThreshold = safeMaxSip,
            interactionSource = sipInteraction
        )

        // Savings target
        SimulatorSlider(
            label = "Savings Target Adjustment",
            emoji = "🎯",
            hint = "Increase/decrease your monthly savings goal",
            value = params.savingsTargetDelta,
            hardMax = 100000f,
            safeMax = null,
            prefix = "₹",
            onValueChange = onTargetChange,
            warningThreshold = null,
            interactionSource = targetInteraction
        )
    }
}

@Composable
private fun SimulatorSlider(
    label: String,
    emoji: String,
    hint: String,
    value: Double,
    hardMax: Float,
    safeMax: Float?,
    prefix: String,
    onValueChange: (Double) -> Unit,
    warningThreshold: Double?,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isWarning = warningThreshold != null && value > warningThreshold
    val borderColor = if (isWarning) Color(0xFFFF9800) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isWarning) Color(0xFFFF9800).copy(alpha = 0.05f) else MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(Spacing.sm))
                    Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                }
                Text(
                    "$prefix${"%,.0f".format(value)}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isWarning) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(hint, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Slider(
                value = value.toFloat().coerceIn(0f, hardMax),
                onValueChange = { onValueChange(it.toDouble()) },
                valueRange = 0f..hardMax,
                modifier = Modifier.fillMaxWidth(),
                interactionSource = interactionSource,
                colors = SliderDefaults.colors(
                    thumbColor = if (isWarning) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary,
                    activeTrackColor = if (isWarning) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary
                )
            )

            // Safe zone indicator
            if (safeMax != null && hardMax > 0) {
                val safeFraction = safeMax / hardMax
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("₹0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF4CAF50))
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "Safe: ₹${"%,.0f".format(safeMax)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    Text("₹${"%,.0f".format(hardMax)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (isWarning) {
                Spacer(modifier = Modifier.height(Spacing.xs))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFFF9800), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Above safe limit — this could strain your budget!",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}

@Composable
private fun ImpactAnalysisSection(result: SimulationResult) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text("Impact Analysis", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            ImpactCard(
                modifier = Modifier.weight(1f),
                label = "EMI Ratio",
                sublabel = "Safe limit: < 40%",
                value = "${"%,.1f".format(result.projectedEmiRatio)}%",
                isGood = result.projectedEmiRatio < 35,
                isBad = result.projectedEmiRatio > 40
            )
            ImpactCard(
                modifier = Modifier.weight(1f),
                label = "Invest. Ratio",
                sublabel = "Ideal: > 15%",
                value = "${"%,.1f".format(result.projectedInvestmentRatio)}%",
                isGood = result.projectedInvestmentRatio > 15,
                isBad = result.projectedInvestmentRatio > 50
            )
        }

        ImpactCard(
            modifier = Modifier.fillMaxWidth(),
            label = "Monthly Net Savings",
            sublabel = if (result.projectedSavings < 0) "Deficit! You'd be spending more than you earn." else "Amount left after all commitments",
            value = if (result.projectedSavings < 0) "-₹${"%,.0f".format(Math.abs(result.projectedSavings))}" else "₹${"%,.0f".format(result.projectedSavings)}",
            isGood = result.projectedSavings > 0,
            isBad = result.projectedSavings < 0
        )

        // Simple verdict row
        val verdict = when {
            result.projectedSavings < 0 -> Pair("❌ Cannot afford this scenario", Color(0xFFF44336))
            result.projectedEmiRatio > 40 -> Pair("⚠️ EMI load is too high — risky", Color(0xFFFF9800))
            result.projectedEmiRatio > 30 -> Pair("🟡 Budget is stretched — proceed carefully", Color(0xFFFFC107))
            else -> Pair("✅ This scenario looks financially safe!", Color(0xFF4CAF50))
        }
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = verdict.second.copy(alpha = 0.1f))
        ) {
            Text(
                verdict.first,
                modifier = Modifier.padding(Spacing.md),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = verdict.second
            )
        }
    }
}

@Composable
private fun ImpactCard(modifier: Modifier, label: String, sublabel: String, value: String, isGood: Boolean, isBad: Boolean) {
    val color = when {
        isBad -> Color(0xFFF44336)
        isGood -> Color(0xFF4CAF50)
        else -> Color(0xFFFFC107)
    }
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Text(label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black), color = color)
            Text(sublabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PremiumLockedOverlay() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(Spacing.xxl)) {
            Text("💎 Premium Feature", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                "The What-If Simulator helps you plan life decisions by projecting financial impacts before you make them.",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(Spacing.xl))
            Button(onClick = { /* Paywall */ }, shape = CircleShape) {
                Text("Upgrade to Monetra Premium")
            }
        }
    }
}

@Composable
private fun FutureProjectionGraph(result: SimulationResult) {
    val months = 12
    val currentNetSavings = result.currentIncome - result.currentExpenses - result.currentEmis - result.currentInvestments
    val baseSavings = currentNetSavings.coerceAtLeast(0.0)

    val projectedPathData = List(months) { i -> (i + 1) * result.projectedSavings }
    val baselinePathData = List(months) { i -> (i + 1) * baseSavings }

    val hasNegative = result.projectedSavings < 0
    val maxVal = maxOf(
        projectedPathData.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0,
        baselinePathData.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
    ) * 1.2
    val minVal = projectedPathData.minOrNull()?.coerceAtMost(0.0) ?: 0.0

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val negativeColor = Color(0xFFF44336)
    val surfaceColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor)
    ) {
        Column(modifier = Modifier.padding(Spacing.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ShowChart, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text("12-Month Savings Projection", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(Spacing.sm))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(primaryColor, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Simulated", style = MaterialTheme.typography.labelSmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(outlineColor, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Current pace", style = MaterialTheme.typography.labelSmall)
                }
                if (hasNegative) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(negativeColor, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Deficit zone", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            if (hasNegative) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(negativeColor.copy(alpha = 0.08f), RoundedCornerShape(8.dp)).padding(Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, tint = negativeColor, modifier = Modifier.size(14.dp), contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Negative savings — deficit grows each month!",
                        style = MaterialTheme.typography.labelSmall,
                        color = negativeColor
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.sm))
            }

            Canvas(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                val width = size.width
                val height = size.height
                val xStep = width / (months - 1)
                val range = (maxVal - minVal).coerceAtLeast(1.0)
                val zeroY = height - ((-minVal / range) * height).toFloat()

                // Zero line
                if (hasNegative) {
                    drawLine(
                        color = negativeColor.copy(alpha = 0.4f),
                        start = Offset(0f, zeroY),
                        end = Offset(width, zeroY),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                    )
                }

                // Baseline
                val basePath = Path()
                baselinePathData.forEachIndexed { index, value ->
                    val x = index * xStep
                    val y = height - (((value - minVal) / range) * height).toFloat()
                    if (index == 0) basePath.moveTo(x, y) else basePath.lineTo(x, y)
                }
                drawPath(basePath, outlineColor, style = Stroke(3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))))

                // Projected
                val projPath = Path()
                val projColor = if (hasNegative) negativeColor else primaryColor
                projectedPathData.forEachIndexed { index, value ->
                    val x = index * xStep
                    val y = height - (((value - minVal) / range) * height).toFloat()
                    if (index == 0) projPath.moveTo(x, y) else projPath.lineTo(x, y)
                }
                drawPath(projPath, projColor, style = Stroke(5f))

                val finalY = height - (((projectedPathData.last() - minVal) / range) * height).toFloat()
                drawCircle(color = projColor, radius = 8f, center = Offset(width, finalY))
            }

            Spacer(modifier = Modifier.height(Spacing.sm))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Month 1", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val finalVal = projectedPathData.last()
                    Text(
                        "Year end: ${if (finalVal < 0) "-" else ""}₹${"%,.0f".format(Math.abs(finalVal))}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (finalVal < 0) Color(0xFFF44336) else MaterialTheme.colorScheme.primary
                    )
                }
                Text("Month 12", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
