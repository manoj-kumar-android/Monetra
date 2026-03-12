package com.monetra.presentation.navigation


import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import com.monetra.MainViewModel
import com.monetra.presentation.screen.add_edit.AddEditExpenseScreen
import com.monetra.presentation.screen.budgets.BudgetsScreen
import com.monetra.presentation.screen.settings.SettingsScreen
import com.monetra.presentation.screen.simulator.WhatIfSimulatorScreen
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Welcome : Route
    @Serializable
    data class TransactionList(val initialTab: String? = null) : Route
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
    data object SavingsList : Route
    @Serializable
    data class AddEditSavings(val id: Long? = null) : Route
}

@Composable
fun MonetraNavGraph(
    backStack: NavBackStack<NavKey>,
    mainViewModel: MainViewModel = hiltViewModel(),
    initialRefundableId: Long? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val pendingRefundableId by mainViewModel.pendingRefundableId.collectAsState()

    LaunchedEffect(pendingRefundableId) {
        if (pendingRefundableId != null) {
            val currentRoute = backStack.lastOrNull()
            if (currentRoute !is Route.RefundableDetails || currentRoute.id != pendingRefundableId) {
                val id = mainViewModel.consumePendingRefundableId()
                if (id != null) {
                    val rootRoute = Route.TransactionList(initialTab = "Refundable")
                    if (backStack.lastOrNull() != rootRoute) {
                        backStack.navigateTo(rootRoute)
                    }
                    backStack.navigateTo(Route.RefundableDetails(id))
                }
            }
        }
    }
    // TRACK DIRECTION
    var previousBackStackSize by remember { mutableIntStateOf(backStack.size) }
    val isPop = remember(backStack.size) {
        val pop = backStack.size < previousBackStackSize
        previousBackStackSize = backStack.size
        pop
    }

    NavDisplay(
        backStack = backStack,
        onBack = {
            if (backStack.size > 1) {
                backStack.safePop()
            }
        },
        /*transitionSpec = {
            if (isPop) {
                slideInHorizontally(
                    initialOffsetX = { -it },
                    animationSpec = tween(
                        durationMillis = 700,
                        easing = FastOutSlowInEasing
                    )
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = 700,
                        easing = FastOutSlowInEasing
                    )
                )
            } else {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(
                        durationMillis = 700,
                        easing = FastOutSlowInEasing
                    )
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { -it },
                    animationSpec = tween(
                        durationMillis = 700,
                        easing = FastOutSlowInEasing
                    )
                )
            }
        }*/
        entryProvider = { key ->
            when (key) {

                is Route.TransactionList -> {
                    NavEntry(key) {
                        MainScreenContainer(
                            isTopLevel = backStack.lastOrNull() is Route.TransactionList,
                            initialTab = key.initialTab,
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
                            onNavigateToSimulator = {
                                backStack.navigateTo(Route.WhatIfSimulator)
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
                            },
                            onNavigateToWelcome = {
                                backStack.clear()
                                backStack.add(Route.Welcome)
                            }
                        )
                    }
                }

                is Route.SavingsList -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.savings.SavingsListScreen(
                            onNavigateBack = { backStack.safePop() },
                            onAddSavingsClick = { backStack.navigateTo(Route.AddEditSavings(null)) },
                            onSavingsClick = { id -> backStack.navigateTo(Route.AddEditSavings(id)) }
                        )
                    }
                }

                is Route.AddEditSavings -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.savings.AddEditSavingsScreen(
                            id = (key as Route.AddEditSavings).id,
                            onNavigateBack = { backStack.safePop() }
                        )
                    }
                }

                is Route.Settings -> {
                    NavEntry(key) {
                        SettingsScreen(
                            onNavigateBack = { backStack.safePop() },
                            onNavigateToCategories = { backStack.navigateTo(Route.Budgets) },
                            onNavigateToHelp = { screenType -> backStack.navigateTo(Route.Help(screenType)) },
                            onNavigateToSimulator = { backStack.navigateTo(Route.WhatIfSimulator) }
                        )
                    }
                }

                is Route.Budgets -> {
                    NavEntry(key) {
                        BudgetsScreen(
                            onNavigateBack = { backStack.safePop() },
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
                                backStack.safePop()
                            }
                        )
                    }
                }

                is Route.WhatIfSimulator -> {
                    NavEntry(key) {
                        WhatIfSimulatorScreen(
                            onNavigateBack = { backStack.safePop() },
                            onNavigateToHelp = { backStack.navigateTo(Route.Help("SIMULATOR")) }
                        )
                    }
                }

                is Route.Help -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.help.HelpScreen(
                            screenType = key.screenType,
                            onNavigateBack = { backStack.safePop() }
                        )
                    }
                }

                is Route.Loans -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.loans.LoanManagementScreen(
                            onNavigateBack = { backStack.safePop() },
                            onNavigateToHelp = { backStack.navigateTo(Route.Help("LOANS")) }
                        )
                    }
                }

                is Route.Investments -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.investments.InvestmentManagementScreen(
                            onNavigateBack = { backStack.safePop() },
                            onNavigateToHelp = { backStack.navigateTo(Route.Help("INVESTMENTS")) }
                        )
                    }
                }

                is Route.FixedExpenses -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.monthly_expense.MonthlyExpenseScreen(
                            onNavigateBack = { backStack.safePop() },
                            onNavigateToHelp = { backStack.navigateTo(Route.Help("FIXED_COSTS")) }
                        )
                    }
                }

                is Route.AddEditRefundable -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.refundable.AddEditRefundableScreen(
                            id = key.id,
                            onNavigateBack = { backStack.safePop() }
                        )
                    }
                }

                is Route.RefundableDetails -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.refundable.RefundableDetailScreen(
                            id = key.id,
                            onNavigateBack = { backStack.safePop() },
                            onEditClick = { id ->
                                backStack.navigateTo(Route.AddEditRefundable(id))
                            }
                        )
                    }
                }

                is Route.Welcome -> {
                    NavEntry(key) {
                        com.monetra.presentation.screen.welcome.WelcomeScreen(
                            onNavigateToDashboard = {
                                backStack.clear()
                                backStack.add(Route.TransactionList())
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

/**
 * Safely pops the backstack only if there is more than one item left.
 * This prevents IndexOutOfBoundsExceptions resulting from rapid back button presses.
 */
fun NavBackStack<NavKey>.safePop() {
    if (this.size > 1) {
        this.removeAt(this.lastIndex)
    }
}
