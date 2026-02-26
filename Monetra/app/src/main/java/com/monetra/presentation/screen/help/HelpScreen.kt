package com.monetra.presentation.screen.help

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monetra.R
import com.monetra.presentation.components.HelpSection
import com.monetra.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    screenType: String,
    onNavigateBack: () -> Unit
) {
    val (titleResId, sections) = getHelpContent(screenType)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleResId), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.lg)
        ) {
            sections.forEachIndexed { index, section ->
                HelpCard(index + 1, section)
            }
            
            Spacer(modifier = Modifier.height(Spacing.xxl))
        }
    }
}

@Composable
private fun HelpCard(index: Int, section: HelpSection) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = index.toString(),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(Spacing.md))
                Text(
                    text = stringResource(section.titleResId),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = stringResource(section.descriptionResId),
                style = MaterialTheme.typography.bodyLarge.copy(lineHeight = 26.sp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getHelpContent(screenType: String): Pair<Int, List<HelpSection>> {
    return when (screenType) {
        "DASHBOARD" -> Pair(
            R.string.help_dashboard_guide_title,
            listOf(
                HelpSection(
                    titleResId = R.string.help_dashboard_step1_title,
                    descriptionResId = R.string.help_dashboard_step1_desc
                ),
                HelpSection(
                    titleResId = R.string.help_dashboard_step2_title,
                    descriptionResId = R.string.help_dashboard_step2_desc
                ),
                HelpSection(
                    titleResId = R.string.help_dashboard_step3_title,
                    descriptionResId = R.string.help_dashboard_step3_desc
                )
            )
        )
        "ASSISTANT" -> Pair(
            R.string.help_assistant_guide_title,
            listOf(
                HelpSection(
                    titleResId = R.string.help_assistant_step1_title,
                    descriptionResId = R.string.help_assistant_step1_desc
                ),
                HelpSection(
                    titleResId = R.string.help_assistant_step2_title,
                    descriptionResId = R.string.help_assistant_step2_desc
                ),
                HelpSection(
                    titleResId = R.string.help_assistant_step3_title,
                    descriptionResId = R.string.help_assistant_step3_desc
                ),
                HelpSection(
                    titleResId = R.string.help_assistant_step4_title,
                    descriptionResId = R.string.help_assistant_step4_desc
                )
            )
        )
        "TRANSACTIONS" -> Pair(
            R.string.help_transactions_guide_title,
            listOf(
                HelpSection(
                    titleResId = R.string.help_transactions_step1_title,
                    descriptionResId = R.string.help_transactions_step1_desc
                ),
                HelpSection(
                    titleResId = R.string.help_transactions_step2_title,
                    descriptionResId = R.string.help_transactions_step2_desc
                )
            )
        )
        "SIMULATOR" -> Pair(
            R.string.help_simulator_guide_title,
            listOf(
                HelpSection(
                    titleResId = R.string.help_simulator_step1_title,
                    descriptionResId = R.string.help_simulator_step1_desc
                ),
                HelpSection(
                    titleResId = R.string.help_simulator_step2_title,
                    descriptionResId = R.string.help_simulator_step2_desc
                )
            )
        )
        "LOANS" -> Pair(
            R.string.help_loans_guide_title,
            listOf(
                HelpSection(
                    titleResId = R.string.help_loans_step1_title,
                    descriptionResId = R.string.help_loans_step1_desc
                )
            )
        )
        "INVESTMENTS" -> Pair(
            R.string.wealth_intelligence_title,
            listOf(
                HelpSection(
                    titleResId = R.string.help_investments_step1_title,
                    descriptionResId = R.string.help_investments_step1_desc
                ),
                HelpSection(
                    titleResId = R.string.help_investments_step2_title,
                    descriptionResId = R.string.help_investments_step2_desc
                ),
                HelpSection(
                    titleResId = R.string.help_investments_step3_title,
                    descriptionResId = R.string.help_investments_step3_desc
                ),
                HelpSection(
                    titleResId = R.string.help_investments_step4_title,
                    descriptionResId = R.string.help_investments_step4_desc
                ),
                HelpSection(
                    titleResId = R.string.help_investments_step5_title,
                    descriptionResId = R.string.help_investments_step5_desc
                )
            )
        )
        "FIXED_COSTS" -> Pair(
            R.string.help_fixed_costs_guide_title,
            listOf(
                HelpSection(
                    titleResId = R.string.help_fixed_costs_step1_title,
                    descriptionResId = R.string.help_fixed_costs_step1_desc
                )
            )
        )
        else -> Pair(R.string.help_center, listOf(HelpSection(R.string.help_general_title, R.string.help_general_desc)))
    }
}
