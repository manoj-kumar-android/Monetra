package com.monetra.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.toRoute
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.monetra.presentation.screen.add_edit.AddEditExpenseScreen
import com.monetra.presentation.screen.settings.SettingsScreen
import com.monetra.presentation.screen.budgets.BudgetsScreen
import com.monetra.presentation.screen.simulator.WhatIfSimulatorScreen
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object TransactionList : Screen

    @Serializable
    data class AddEditTransaction(val transactionId: Long? = null) : Screen

    @Serializable
    data object MonthlyReport : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data object Budgets : Screen

    @Serializable
    data object WhatIfSimulator : Screen

    @Serializable
    data object Loans : Screen

    @Serializable
    data object Investments : Screen

    @Serializable
    data object FixedExpenses : Screen

    @Serializable
    data class Help(val screenType: String) : Screen
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonetraNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Screen = Screen.Onboarding
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(400)) + fadeIn(
                animationSpec = tween(400)
            )
        },
        exitTransition = {
            slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(400)) + fadeOut(
                animationSpec = tween(400)
            )
        },
        popEnterTransition = {
            slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(400)) + fadeIn(
                animationSpec = tween(400)
            )
        },
        popExitTransition = {
            slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(400)) + fadeOut(
                animationSpec = tween(400)
            )
        }
    ) {
        composable<Screen.TransactionList> {
            MainScreenContainer(
                onNavigateToAdd = {
                    keyboardController?.hide()
                    navController.navigate(Screen.AddEditTransaction(null))
                },
                onNavigateToEdit = { transactionId ->
                    keyboardController?.hide()
                    navController.navigate(Screen.AddEditTransaction(transactionId))
                },
                onNavigateToReport = {
                    navController.navigate(Screen.MonthlyReport)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings)
                },
                onManageBudgetsClick = {
                    navController.navigate(Screen.Budgets)
                },
                onNavigateToLoans = {
                    navController.navigate(Screen.Loans)
                },
                onNavigateToInvestments = {
                    navController.navigate(Screen.Investments)
                },
                onNavigateToSimulator = {
                    navController.navigate(Screen.WhatIfSimulator)
                },
                onNavigateToFixedExpenses = {
                    navController.navigate(Screen.FixedExpenses)
                },
                onNavigateToHelp = { screenType ->
                    navController.navigate(Screen.Help(screenType))
                }
            )
        }

        composable<Screen.Settings> {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCategories = { navController.navigate(Screen.Budgets) }
            )
        }

        composable<Screen.Budgets> {
            BudgetsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHelp = { navController.navigate(Screen.Help("BUDGETS")) }
            )
        }

        composable<Screen.MonthlyReport> {
            com.monetra.presentation.screen.report.MonthlyReportScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSimulator = {
                    navController.navigate(Screen.WhatIfSimulator)
                }
            )
        }

        composable<Screen.AddEditTransaction> {
            AddEditExpenseScreen(
                onNavigateBack = {
                    keyboardController?.hide()
                    navController.popBackStack()
                }
            )
        }

        composable<Screen.WhatIfSimulator> {
            WhatIfSimulatorScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHelp = { navController.navigate(Screen.Help("SIMULATOR")) }
            )
        }

        composable<Screen.Help> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.Help>()
            com.monetra.presentation.screen.help.HelpScreen(
                screenType = args.screenType,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.Loans> {
            com.monetra.presentation.screen.loans.LoanManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHelp = { navController.navigate(Screen.Help("LOANS")) }
            )
        }

        composable<Screen.Investments> {
            com.monetra.presentation.screen.investments.InvestmentManagementScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHelp = { navController.navigate(Screen.Help("INVESTMENTS")) }
            )
        }

        composable<Screen.FixedExpenses> {
            com.monetra.presentation.screen.monthly_expense.MonthlyExpenseScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHelp = { navController.navigate(Screen.Help("FIXED_COSTS")) }
            )
        }
        
        composable<Screen.Onboarding> {
            com.monetra.presentation.screen.onboarding.OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.TransactionList) {
                        popUpTo(Screen.Onboarding) { inclusive = true }
                    }
                }
            )
        }
    }
}

