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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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
    val nestedNavController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }

    val navItems = listOf(
        BottomNavItem("Dashboard", Icons.Default.Home, BottomNavScreen.Dashboard),
        BottomNavItem("Transactions", Icons.AutoMirrored.Filled.List, BottomNavScreen.Transactions),
        BottomNavItem("Refundable", Icons.Default.PriceCheck, BottomNavScreen.Refundable),
        BottomNavItem("Portfolio", Icons.Default.Star, BottomNavScreen.Summary)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.navigationBars, // Only pad for navigation bars at the bottom
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                val navBackStackEntry by nestedNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                navItems.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { 
                        // Simplified route matching logic relying on class names.
                        it.route?.contains(item.route::class.simpleName ?: "") == true 
                    } == true
                    
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.title) },
                        label = { Text(item.title) },
                        selected = isSelected,
                        onClick = {
                            nestedNavController.navigate(item.route) {
                                popUpTo(nestedNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
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
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        NavHost(
            navController = nestedNavController,
            startDestination = BottomNavScreen.Dashboard,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding)
        ) {
            composable<BottomNavScreen.Dashboard> {
                com.monetra.presentation.screen.dashboard.DashboardScreen(
                    onNavigateToAdd = onNavigateToAdd,
                    onNavigateToEdit = onNavigateToEdit,
                    onNavigateToSettings = onNavigateToSettings,
                    onManageBudgetsClick = onManageBudgetsClick,
                    onNavigateToFixedExpenses = onNavigateToFixedExpenses,
                    onNavigateToHelp = { onNavigateToHelp("DASHBOARD") },
                    onSeeAllTransactions = {
                        nestedNavController.navigate(BottomNavScreen.Transactions) {
                            popUpTo(nestedNavController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable<BottomNavScreen.Transactions> {
                ExpenseListScreen(
                    snackbarHostState = snackbarHostState,
                    onNavigateToAdd = onNavigateToAdd,
                    onNavigateToEdit = onNavigateToEdit,
                    onNavigateToHelp = { onNavigateToHelp("TRANSACTIONS") }
                )
            }
            composable<BottomNavScreen.Refundable> {
                com.monetra.presentation.screen.refundable.RefundableScreen(
                    onAddEntryClick = onNavigateToAddRefundable,
                    onEntryClick = onNavigateToRefundableDetails,
                    onNavigateToHelp = { onNavigateToHelp("REFUNDABLE") }
                )
            }
            composable<BottomNavScreen.Summary> {
                com.monetra.presentation.screen.portfolio.PortfolioScreen(
                    onNavigateToSettings = onNavigateToSettings,
                    onNavigateToLoans = onNavigateToLoans,
                    onNavigateToInvestments = onNavigateToInvestments
                )
            }
        }
    }
}
