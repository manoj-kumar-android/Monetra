package com.monetra.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.monetra.MainViewModel
import com.monetra.presentation.screen.add_edit.AddEditExpenseScreen
import com.monetra.presentation.screen.budgets.BudgetsScreen
import com.monetra.presentation.screen.lock.LockScreen
import com.monetra.presentation.screen.settings.SettingsScreen
import com.monetra.presentation.screen.simulator.WhatIfSimulatorScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Welcome : Route
    @Serializable
    data object Onboarding : Route
    @Serializable
    data object TransactionList : Route
    @Serializable
    data class AddEditTransaction(val transactionId: Long? = null) : Route
    @Serializable
    data object Settings : Route
    @Serializable
    data object Budgets : Route
    @Serializable
    data object WhatIfSimulator : Route
    @Serializable
    data object Loans : Route
    @Serializable
    data object Investments : Route
    @Serializable
    data object FixedExpenses : Route
    @Serializable
    data class Help(val screenType: String) : Route
    @Serializable
    data class AddEditRefundable(val id: Long? = null) : Route
    @Serializable
    data class RefundableDetails(val id: Long) : Route
    @Serializable
    data object Lock : Route
    @Serializable
    data object SavingsList : Route
    @Serializable
    data class AddEditSavings(val id: Long? = null) : Route
}

@Composable
fun MonetraNavGraph(
    backStack: NavBackStack<NavKey>,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val isDashboardUser by mainViewModel.isDashboardUser.collectAsState()

    // Listen for relock events from MainActivity
    LaunchedEffect(Unit) {
        mainViewModel.relockEvent.collect {
            backStack.navigateTo(Route.Lock)
        }
    }

    NavDisplay(
        backStack = backStack,
        onBack = {
            val lastRoute = backStack.lastOrNull()
            if (lastRoute is Route.Lock) {
                // Security: Don't allow backing out of the lock screen.
                // If there's only one item (Lock), or if we were relocked on top of something,
                // the safest thing to do is let the activity handle it (which usually finishes/minimizes).
                // In Navigation3, if we don't handle it, it bubbles up.
                // However, we want to ensure we don't pop the Lock screen to reveal what's under it.
                // So we do nothing here, which prevents the backStack.removeAt below.
            } else if (backStack.size > 1) {
                backStack.removeAt(backStack.lastIndex)
            }
        },
        // Use the decorator that provides ViewModelStore support
/*
        transitionSpec = { direction ->
            val (enter, exit) = if (direction == NavDisplay.Direction.Forward) {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                ) + fadeIn(animationSpec = tween(200)) to
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> -fullWidth / 4 },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                        )
            } else {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> -fullWidth / 4 },
                    animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                ) to
                        slideOutHorizontally(
                            targetOffsetX = { fullWidth -> fullWidth },
                            animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy, stiffness = Spring.StiffnessMedium)
                        ) + fadeOut(animationSpec = tween(200))
            }
            NavTransition(enter, exit)
        } as AnimatedContentTransitionScope<Scene<NavKey>>.() -> ContentTransform,
*/
        entryProvider = { key ->
            when (key) {
                is Route.Lock -> {
                    NavEntry(key) {
                        LockScreen(
                            onAuthenticated = {
                                // If we were relocked (pushed Lock on top of a screen), just pop back
                                if (backStack.size > 1) {
                                    backStack.removeAt(backStack.lastIndex)
                                } else {
                                    // Cold start: clear lock and go to destination
                                    backStack.clear()
                                    backStack.add(if (isDashboardUser) Route.TransactionList else Route.Welcome)
                                }
                                mainViewModel.setLocked(false)
                            }
                        )
                    }
                }

                is Route.TransactionList -> {
                    NavEntry(key) {
                        MainScreenContainer(
                            isTopLevel = backStack.lastOrNull() is Route.TransactionList,
                            onNavigateToAdd = {
                                keyboardController?.hide()
                                backStack.navigateTo(Route.AddEditTransaction(null))
                            },
                            onNavigateToEdit = { transactionId ->
                                keyboardController?.hide()
                                backStack.navigateTo(Route.AddEditTransaction(transactionId))
                            },
                            onNavigateToSettings = {
                                backStack.navigateTo(Route.Settings)
                            },
                            onManageBudgetsClick = {
                                backStack.navigateTo(Route.Budgets)
                            },
                            onNavigateToLoans = {
                                backStack.navigateTo(Route.Loans)
                            },
                            onNavigateToInvestments = {
                                backStack.navigateTo(Route.Investments)
                            },
                            onNavigateToFixedExpenses = {
                                backStack.navigateTo(Route.FixedExpenses)
                            },
                            onNavigateToHelp = { screenType ->
                                backStack.navigateTo(Route.Help(screenType))
                            },
                            onNavigateToAddRefundable = {
                                backStack.navigateTo(Route.AddEditRefundable(null))
                            },
                            onNavigateToEditRefundable = { id ->
                                backStack.navigateTo(Route.AddEditRefundable(id))
                            },
                            onNavigateToRefundableDetails = { id ->
                                backStack.navigateTo(Route.RefundableDetails(id))
                            },
                            onNavigateToSavings = {
                                backStack.navigateTo(Route.SavingsList)
                            }
                        )
                    }
                }

                is Route.SavingsList -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.savings.SavingsListScreen(
                            onNavigateBack = { backStack.removeAt(backStack.lastIndex) },
                            onAddSavingsClick = { backStack.navigateTo(Route.AddEditSavings(null)) },
                            onSavingsClick = { id -> backStack.navigateTo(Route.AddEditSavings(id)) }
                        )
                    }
                }

                is Route.AddEditSavings -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.savings.AddEditSavingsScreen(
                            id = (key as Route.AddEditSavings).id,
                            onNavigateBack = { backStack.removeAt(backStack.lastIndex) }
                        )
                    }
                }

                is Route.Settings -> {
                    NavEntry(key) {
                        SettingsScreen(
                            onNavigateBack = { backStack.removeAt(backStack.lastIndex) },
                            onNavigateToCategories = { backStack.navigateTo(Route.Budgets) }
                        )
                    }
                }

                is Route.Budgets -> {
                    NavEntry(key) {
                        BudgetsScreen(
                            onNavigateBack = { backStack.removeAt(backStack.lastIndex) },
                            onNavigateToHelp = { backStack.navigateTo(Route.Help("BUDGETS")) }
                        )
                    }
                }

                is Route.AddEditTransaction -> {
                    NavEntry(key) {
                        AddEditExpenseScreen(
                            transactionId = key.transactionId,
                            onNavigateBack = {
                                keyboardController?.hide()
                                backStack.removeAt(backStack.lastIndex)
                            }
                        )
                    }
                }

                is Route.WhatIfSimulator -> {
                    NavEntry(key) {
                        WhatIfSimulatorScreen(
                            onNavigateBack = { backStack.removeAt(backStack.lastIndex) },
                            onNavigateToHelp = { backStack.navigateTo(Route.Help("SIMULATOR")) }
                        )
                    }
                }

                is Route.Help -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.help.HelpScreen(
                            screenType = key.screenType,
                            onNavigateBack = { backStack.removeAt(backStack.lastIndex) }
                        )
                    }
                }

                is Route.Loans -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.loans.LoanManagementScreen(
                            onNavigateBack = { backStack.removeAt(backStack.lastIndex) },
                            onNavigateToHelp = { backStack.navigateTo(Route.Help("LOANS")) }
                        )
                    }
                }

                is Route.Investments -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.investments.InvestmentManagementScreen(
                            onNavigateBack = { backStack.removeAt(backStack.lastIndex) },
                            onNavigateToHelp = { backStack.navigateTo(Route.Help("INVESTMENTS")) }
                        )
                    }
                }

                is Route.FixedExpenses -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.monthly_expense.MonthlyExpenseScreen(
                            onNavigateBack = { backStack.removeAt(backStack.lastIndex) },
                            onNavigateToHelp = { backStack.navigateTo(Route.Help("FIXED_COSTS")) }
                        )
                    }
                }

                is Route.AddEditRefundable -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.refundable.AddEditRefundableScreen(
                            id = (key as Route.AddEditRefundable).id,
                            onNavigateBack = { backStack.removeAt(backStack.lastIndex) }
                        )
                    }
                }

                is Route.RefundableDetails -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.refundable.RefundableDetailScreen(
                            id = (key as Route.RefundableDetails).id,
                            onNavigateBack = { backStack.removeAt(backStack.lastIndex) },
                            onEditClick = { id ->
                                backStack.navigateTo(Route.AddEditRefundable(id))
                            }
                        )
                    }
                }

                is Route.Welcome -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.welcome.WelcomeScreen(
                            onNavigateToOnboarding = {
                                backStack.clear()
                                backStack.add(Route.Onboarding)
                            }
                        )
                    }
                }

                is Route.Onboarding -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.onboarding.OnboardingScreen(
                            onComplete = {
                                backStack.clear()
                                backStack.add(Route.TransactionList)
                            }
                        )
                    }
                }
                else -> NavEntry(key) { }
            }
        }
    )
}

/**
 * Extension to handle "single-top" navigation style. If the route (of the same type)
 * is already at the top, we don't add it again.
 */
fun NavBackStack<NavKey>.navigateTo(route: Route) {
    if (this.lastOrNull() != route) {
        this.add(route)
    }
}
