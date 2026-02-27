package com.monetra.presentation.screen.summary

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.presentation.screen.transactions.SummaryUiModel
import com.monetra.ui.theme.Elevation
import com.monetra.ui.theme.SemanticExpense
import com.monetra.ui.theme.SemanticIncome
import com.monetra.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SummaryScreen(
    snackbarHostState: SnackbarHostState,
    viewModel: SummaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(1) } // Default to Monthly
    val tabs = listOf("Weekly", "Monthly", "Yearly")

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Summary",
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            val pagerState = rememberPagerState(initialPage = 1, pageCount = { tabs.size })
            
            // Sync FROM selectedTab TO Pager
            LaunchedEffect(selectedTab) {
                if (pagerState.currentPage != selectedTab) {
                    pagerState.animateScrollToPage(selectedTab)
                }
            }
            
            // Sync FROM Pager TO selectedTab (only when settled)
            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.settledPage }.collect { page ->
                    if (selectedTab != page) {
                        selectedTab = page
                    }
                }
            }

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(modifier = Modifier.align(androidx.compose.ui.Alignment.Center))
                }
            } else {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(modifier = Modifier.fillMaxSize().padding(Spacing.lg).verticalScroll(rememberScrollState())) {
                        when (page) {
                            0 -> uiState.weeklySummary?.let { SummaryDetailCard(it) }
                            1 -> uiState.monthlySummary?.let { SummaryDetailCard(it) }
                            2 -> uiState.yearlySummary?.let { SummaryDetailCard(it) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryDetailCard(summary: SummaryUiModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card)
    ) {
        Column(modifier = Modifier.padding(Spacing.xl)) {
            Text(text = "Net Balance", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = summary.formattedBalance, style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
            
            Spacer(modifier = Modifier.height(Spacing.lg))
            
            Text(text = "Income", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = summary.formattedIncome, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = SemanticIncome)
            
            Spacer(modifier = Modifier.height(Spacing.md))
            
            Text(text = "Expenses", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text = summary.formattedExpense, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold), color = SemanticExpense)
        }
    }
}
