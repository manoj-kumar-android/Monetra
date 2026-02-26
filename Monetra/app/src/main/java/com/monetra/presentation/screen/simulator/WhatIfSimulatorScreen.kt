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
import androidx.compose.material.icons.automirrored.filled.*
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
import androidx.compose.ui.res.stringResource
import com.monetra.R

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
                            stringResource(R.string.simulator_title),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            stringResource(R.string.simulator_subtitle),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    HelpIconButton(onClick = onNavigateToHelp)
                    IconButton(onClick = { viewModel.reset() }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.reset_all))
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
                        Text(stringResource(R.string.fetching_data), color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    val totalProjectedCommitments = result.projectedEmis + result.projectedInvestments + result.projectedExpenses
    val isOverLoaded = totalProjectedCommitments > result.projectedIncome
    val isEmiDangerous = result.projectedEmiRatio > 40
    val isSipUnaffordable = result.projectedInvestments > (result.projectedIncome * 0.5)
    val hasAnyChange = params.salaryDelta != 0.0 || params.newEmiAmount != 0.0 || params.newSipAmount != 0.0 || params.savingsTargetDelta != 0.0

    val projectedIncome = result.projectedIncome
    val safeMaxEmi = ((projectedIncome * 0.5) - result.currentEmis).coerceAtLeast(0.0)
    val safeMaxSip = ((projectedIncome * 0.4) - result.currentInvestments).coerceAtLeast(0.0)

    var isAnySliderActive by remember { mutableStateOf(false) }
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                return if (isAnySliderActive && source == NestedScrollSource.UserInput) {
                    Offset(0f, available.y)
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
        AnimatedStatusBanner(result, hasAnyChange)

        BeforeAfterSnapshot(result)

        if (isOverLoaded) {
            WarningCard(
                icon = Icons.Default.Warning,
                color = Color(0xFFF44336),
                title = stringResource(R.string.budget_overloaded_title),
                message = stringResource(R.string.budget_overloaded_msg, totalProjectedCommitments, result.projectedIncome)
            )
        } else if (isEmiDangerous) {
            WarningCard(
                icon = Icons.Default.Warning,
                color = Color(0xFFFF9800),
                title = stringResource(R.string.emi_high_title),
                message = stringResource(R.string.emi_high_msg, result.projectedEmiRatio)
            )
        } else if (isSipUnaffordable) {
            WarningCard(
                icon = Icons.Default.Info,
                color = Color(0xFF5856D6),
                title = stringResource(R.string.sip_aggressive_title),
                message = stringResource(R.string.sip_aggressive_msg)
            )
        }

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

        ImpactAnalysisSection(result)

        FutureProjectionGraph(result)

        androidx.compose.animation.AnimatedVisibility(visible = hasAnyChange) {
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(stringResource(R.string.reset_all_params))
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
        FinancialBalanceStatus.HEALTHY -> stringResource(R.string.status_healthy)
        FinancialBalanceStatus.MODERATE -> stringResource(R.string.status_moderate)
        FinancialBalanceStatus.RISK -> stringResource(R.string.status_risk)
    }

    val statusSubtitle = when (result.projectedStatus) {
        FinancialBalanceStatus.HEALTHY -> stringResource(R.string.status_healthy_desc)
        FinancialBalanceStatus.MODERATE -> stringResource(R.string.status_moderate_desc)
        FinancialBalanceStatus.RISK -> stringResource(R.string.status_risk_desc)
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
                    stringResource(R.string.adjust_sliders_hint),
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
                    stringResource(R.string.financial_snapshot),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    if (hasChange) stringResource(R.string.current_projected) else stringResource(R.string.move_sliders_hint),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Spacing.sm))
            SnapshotRow(stringResource(R.string.income_label), result.currentIncome, result.projectedIncome)
            SnapshotRow(stringResource(R.string.fixed_expenses), result.currentExpenses, result.projectedExpenses)
            SnapshotRow(stringResource(R.string.debt_emis_title), result.currentEmis, result.projectedEmis)
            SnapshotRow(stringResource(R.string.investments_sip), result.currentInvestments, result.projectedInvestments)
            HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.xs))
            SnapshotRow(stringResource(R.string.net_savings), currentSavings, result.projectedSavings, highlight = true)
        }
    }
}


@Composable
private fun SnapshotRow(label: String, before: Double, after: Double, highlight: Boolean = false) {
    val diff = after - before
    val diffColor = when {
        label.contains("EMI") || label.contains("Expense") || label.contains("Debt") -> if (diff > 0) Color(0xFFF44336) else Color(0xFF4CAF50)
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
            Text(stringResource(R.string.adjust_parameters), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        SimulatorSlider(
            label = stringResource(R.string.salary_hike_label),
            emoji = "💰",
            hint = stringResource(R.string.salary_hike_hint),
            value = params.salaryDelta,
            hardMax = 300000f,
            safeMax = null,
            prefix = "+₹",
            onValueChange = onSalaryChange,
            warningThreshold = null,
            interactionSource = salaryInteraction
        )

        SimulatorSlider(
            label = stringResource(R.string.new_monthly_emi),
            emoji = "🏠",
            hint = stringResource(R.string.new_emi_hint, safeMaxEmi),
            value = params.newEmiAmount,
            hardMax = (result.projectedIncome * 0.6).toFloat().coerceAtLeast(50000f),
            safeMax = safeMaxEmi.toFloat(),
            prefix = "₹",
            onValueChange = onEmiChange,
            warningThreshold = safeMaxEmi,
            interactionSource = emiInteraction
        )

        SimulatorSlider(
            label = stringResource(R.string.new_monthly_sip),
            emoji = "📈",
            hint = stringResource(R.string.new_sip_hint, safeMaxSip),
            value = params.newSipAmount,
            hardMax = (result.projectedIncome * 0.5).toFloat().coerceAtLeast(50000f),
            safeMax = safeMaxSip.toFloat(),
            prefix = "₹",
            onValueChange = onSipChange,
            warningThreshold = safeMaxSip,
            interactionSource = sipInteraction
        )

        SimulatorSlider(
            label = stringResource(R.string.savings_target_adj),
            emoji = "🎯",
            hint = stringResource(R.string.savings_target_hint),
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

            if (safeMax != null && hardMax > 0) {
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
                            stringResource(R.string.safe_label_format, safeMax),
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
                        stringResource(R.string.above_safe_limit),
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
            Text(stringResource(R.string.impact_analysis), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.md)) {
            ImpactCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.emi_ratio_label),
                sublabel = stringResource(R.string.emi_ratio_safe_hint),
                value = "${"%,.1f".format(result.projectedEmiRatio)}%",
                isGood = result.projectedEmiRatio < 35,
                isBad = result.projectedEmiRatio > 40
            )
            ImpactCard(
                modifier = Modifier.weight(1f),
                label = stringResource(R.string.invest_ratio_label),
                sublabel = stringResource(R.string.invest_ratio_ideal_hint),
                value = "${"%,.1f".format(result.projectedInvestmentRatio)}%",
                isGood = result.projectedInvestmentRatio > 15,
                isBad = result.projectedInvestmentRatio > 50
            )
        }

        ImpactCard(
            modifier = Modifier.fillMaxWidth(),
            label = stringResource(R.string.net_savings),
            sublabel = if (result.projectedSavings < 0) stringResource(R.string.deficit_desc) else stringResource(R.string.left_after_commitments),
            value = if (result.projectedSavings < 0) "-₹${"%,.0f".format(Math.abs(result.projectedSavings))}" else "₹${"%,.0f".format(result.projectedSavings)}",
            isGood = result.projectedSavings > 0,
            isBad = result.projectedSavings < 0
        )

        val verdict = when {
            result.projectedSavings < 0 -> Pair(stringResource(R.string.verdict_cannot_afford), Color(0xFFF44336))
            result.projectedEmiRatio > 40 -> Pair(stringResource(R.string.verdict_emi_high), Color(0xFFFF9800))
            result.projectedEmiRatio > 30 -> Pair(stringResource(R.string.verdict_stretched), Color(0xFFFFC107))
            else -> Pair(stringResource(R.string.verdict_safe), Color(0xFF4CAF50))
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
            Text(stringResource(R.string.premium_feature), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold))
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                stringResource(R.string.premium_simulator_desc),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(Spacing.xl))
            Button(onClick = { /* Paywall */ }, shape = CircleShape) {
                Text(stringResource(R.string.upgrade_premium))
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
                Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(stringResource(R.string.savings_projection_12m), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
            Spacer(modifier = Modifier.height(Spacing.sm))

            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg), verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(primaryColor, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.simulated), style = MaterialTheme.typography.labelSmall)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(outlineColor, CircleShape))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.current_pace), style = MaterialTheme.typography.labelSmall)
                }
                if (hasNegative) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(10.dp).background(negativeColor, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.deficit_zone), style = MaterialTheme.typography.labelSmall)
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
                        stringResource(R.string.negative_savings_warning),
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
                drawLine(
                    color = outlineColor.copy(alpha = 0.3f),
                    start = Offset(0f, zeroY),
                    end = Offset(width, zeroY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                // Current Path
                val currentPath = Path().apply {
                    moveTo(0f, zeroY)
                    baselinePathData.forEachIndexed { i, savings ->
                        val x = i * xStep
                        val y = height - (((savings - minVal) / range) * height).toFloat()
                        lineTo(x, y)
                    }
                }
                drawPath(currentPath, outlineColor, style = Stroke(width = 2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)))

                // Simulated Path
                val simulatedPath = Path().apply {
                    moveTo(0f, zeroY)
                    projectedPathData.forEachIndexed { i, savings ->
                        val x = i * xStep
                        val y = height - (((savings - minVal) / range) * height).toFloat()
                        lineTo(x, y)
                    }
                }
                drawPath(simulatedPath, if (hasNegative) negativeColor else primaryColor, style = Stroke(width = 3.dp.toPx()))
            }
        }
    }
}
