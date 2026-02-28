package com.monetra.presentation.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PriceCheck
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.monetra.presentation.screen.snapshot.SnapshotScreen
import com.monetra.presentation.screen.transactions.ExpenseListScreen
import kotlinx.serialization.Serializable

sealed interface BottomNavScreen {
    @Serializable
    data object Dashboard : BottomNavScreen

    @Serializable
    data object Transactions : BottomNavScreen

    @Serializable
    data object Refundable : BottomNavScreen

    @Serializable
    data object Summary : BottomNavScreen
}

data class BottomNavItem(
    val title: String,
    val icon: ImageVector,
    val route: BottomNavScreen
)

@Composable
fun MainScreenContainer(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onManageBudgetsClick: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onNavigateToInvestments: () -> Unit,
    onNavigateToFixedExpenses: () -> Unit,
    onNavigateToHelp: (String) -> Unit,
    onNavigateToAddRefundable: () -> Unit,
    onNavigateToEditRefundable: (Long) -> Unit,
    onNavigateToRefundableDetails: (Long) -> Unit
) {
    var selectedTabStr by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf("Dashboard") }
    var selectedTab = remember(selectedTabStr) {
        when(selectedTabStr) {
            "Transactions" -> BottomNavScreen.Transactions
            "Refundable" -> BottomNavScreen.Refundable
            "Summary" -> BottomNavScreen.Summary
            else -> BottomNavScreen.Dashboard
        }
    }

    BackHandler(enabled = selectedTab != BottomNavScreen.Dashboard) {
        selectedTabStr = "Dashboard"
    }

    val snackbarHostState = remember { SnackbarHostState() }

    val navItems = listOf(
        BottomNavItem("Dashboard", Icons.Default.Home, BottomNavScreen.Dashboard),
        BottomNavItem("Transactions", Icons.AutoMirrored.Filled.List, BottomNavScreen.Transactions),
        BottomNavItem("Refundable", Icons.Default.PriceCheck, BottomNavScreen.Refundable),
        BottomNavItem("Portfolio", Icons.Default.Star, BottomNavScreen.Summary)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.navigationBars,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                navItems.forEach { item ->
                    val isSelected = selectedTab == item.route

                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = { selectedTabStr = item.route.javaClass.simpleName },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                com.monetra.presentation.component.MonetraSnackbar(snackbarData = data)
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            when (selectedTab) {
                BottomNavScreen.Dashboard -> {
                    com.monetra.presentation.screen.dashboard.DashboardScreen(
                        onNavigateToAdd = onNavigateToAdd,
                        onNavigateToEdit = onNavigateToEdit,
                        onNavigateToSettings = onNavigateToSettings,
                        onManageBudgetsClick = onManageBudgetsClick,
                        onNavigateToFixedExpenses = onNavigateToFixedExpenses,
                        onNavigateToHelp = { onNavigateToHelp("DASHBOARD") },
                        onSeeAllTransactions = { selectedTabStr = "Transactions" }
                    )
                }
                BottomNavScreen.Transactions -> {
                    ExpenseListScreen(
                        snackbarHostState = snackbarHostState,
                        onNavigateToAdd = onNavigateToAdd,
                        onNavigateToEdit = onNavigateToEdit,
                        onNavigateToHelp = { onNavigateToHelp("TRANSACTIONS") }
                    )
                }
                BottomNavScreen.Refundable -> {
                    com.monetra.presentation.screen.refundable.RefundableScreen(
                        onAddEntryClick = onNavigateToAddRefundable,
                        onEntryClick = onNavigateToRefundableDetails,
                        onNavigateToHelp = { onNavigateToHelp("REFUNDABLE") }
                    )
                }
                BottomNavScreen.Summary -> {
                    com.monetra.presentation.screen.portfolio.PortfolioScreen(
                        onNavigateToSettings = onNavigateToSettings,
                        onNavigateToLoans = onNavigateToLoans,
                        onNavigateToInvestments = onNavigateToInvestments
                    )
                }
            }
        }
    }
}
