package com.monetra.presentation.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import com.monetra.MainViewModel
import com.monetra.presentation.screen.add_edit.AddEditExpenseScreen
import com.monetra.presentation.screen.budgets.BudgetsScreen
import com.monetra.presentation.screen.lock.LockScreen
import com.monetra.presentation.screen.settings.SettingsScreen
import com.monetra.presentation.screen.simulator.WhatIfSimulatorScreen
import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Welcome : Screen

    @Serializable
    data object Onboarding : Screen

    @Serializable
    data object TransactionList : Screen

    @Serializable
    data class AddEditTransaction(val transactionId: Long? = null) : Screen

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

    @Serializable
    data class AddEditRefundable(val id: Long? = null) : Screen

    @Serializable
    data class RefundableDetails(val id: Long) : Screen

    @Serializable
    data class Lock(val goToDashboard: Boolean = true) : Screen
}

@Composable
fun MonetraNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: Screen = Screen.Onboarding,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    // Listen for relock events from MainActivity (app returned from background)
    LaunchedEffect(Unit) {
        mainViewModel.relockEvent.collect { goToDashboard ->
            navController.navigate(Screen.Lock(goToDashboard = goToDashboard)) {
                popUpTo(0) { inclusive = false }
            }
        }
    }

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
        composable<Screen.Lock> { backStackEntry ->
            val args = backStackEntry.toRoute<Screen.Lock>()
            LockScreen(
                onAuthenticated = {
                    val destination = if (args.goToDashboard) Screen.TransactionList else Screen.Welcome
                    navController.navigate(destination) {
                        popUpTo(Screen.Lock(args.goToDashboard)) { inclusive = true }
                    }
                }
            )
        }

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
                onNavigateToFixedExpenses = {
                    navController.navigate(Screen.FixedExpenses)
                },
                onNavigateToHelp = { screenType ->
                    navController.navigate(Screen.Help(screenType))
                },
                onNavigateToAddRefundable = {
                    navController.navigate(Screen.AddEditRefundable(null))
                },
                onNavigateToEditRefundable = { id ->
                    navController.navigate(Screen.AddEditRefundable(id))
                },
                onNavigateToRefundableDetails = { id ->
                    navController.navigate(Screen.RefundableDetails(id))
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

        composable<Screen.AddEditRefundable> {
            com.monetra.presentation.screen.refundable.AddEditRefundableScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable<Screen.RefundableDetails>(
            deepLinks = listOf(
                navDeepLink<Screen.RefundableDetails>(basePath = "monetra://refundable")
            )
        ) {
            com.monetra.presentation.screen.refundable.RefundableDetailScreen(
                onNavigateBack = { navController.popBackStack() },
                onEditClick = { id -> 
                    navController.navigate(Screen.AddEditRefundable(id))
                }
            )
        }
        
        composable<Screen.Welcome> {
            com.monetra.presentation.screen.welcome.WelcomeScreen(
                onNavigateToOnboarding = {
                    navController.navigate(Screen.Onboarding) {
                        popUpTo(Screen.Welcome) { inclusive = true }
                    }
                }
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

