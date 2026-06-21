package com.monetra.feature.onboarding.presentation

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.feature.onboarding.presentation.component.BillsStepContent
import com.monetra.feature.onboarding.presentation.component.IncomeStepContent
import com.monetra.feature.onboarding.presentation.component.OnboardingHeader
import com.monetra.feature.onboarding.presentation.component.SavingsStepContent
import com.monetra.feature.onboarding.presentation.component.WelcomeStepContent

@Composable
fun OnboardingScreen(
    onNavigateToDashboard: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current

    androidx.activity.compose.BackHandler(enabled = uiState.currentStep != OnboardingStep.WELCOME) {
        viewModel.onIntent(OnboardingIntent.PreviousStep)
    }

    LaunchedEffect(uiState.currentStep) {
        if (uiState.currentStep == OnboardingStep.WELCOME) {
            keyboardController?.hide()
        }
    }

    // Confetti logic: trigger confetti when user completes a step and moves forward
    var previousStep by remember { mutableStateOf(uiState.currentStep) }
    var confettiTrigger by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(uiState.currentStep) {
        if (uiState.currentStep.ordinal > previousStep.ordinal) {
            confettiTrigger = (confettiTrigger ?: 0) + 1
        }
        previousStep = uiState.currentStep
    }

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is OnboardingEffect.NavigationToDashboard -> {
                    keyboardController?.hide()
                    onNavigateToDashboard()
                }

                is OnboardingEffect.ShowToast -> {
                    Toast.makeText(context, effect.message.asString(context), Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header is ALWAYS present with a fixed height to avoid layout shift jitter
                OnboardingHeader(
                    currentStep = uiState.currentStep,
                    onBackClick = { viewModel.onIntent(OnboardingIntent.PreviousStep) }
                )

                AnimatedContent(
                    targetState = uiState.currentStep,
                    transitionSpec = {
                        val springSpec = spring<androidx.compose.ui.unit.IntOffset>(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                        val fadeSpec = spring<Float>(
                            stiffness = Spring.StiffnessMediumLow
                        )
                        if (targetState.ordinal > initialState.ordinal) {
                            (slideInHorizontally(animationSpec = springSpec) { width -> width } + fadeIn(
                                animationSpec = fadeSpec
                            )).togetherWith(
                                slideOutHorizontally(animationSpec = springSpec) { width -> -width } + fadeOut(
                                    animationSpec = fadeSpec
                                )
                            )
                        } else {
                            (slideInHorizontally(animationSpec = springSpec) { width -> -width } + fadeIn(
                                animationSpec = fadeSpec
                            )).togetherWith(
                                slideOutHorizontally(animationSpec = springSpec) { width -> width } + fadeOut(
                                    animationSpec = fadeSpec
                                )
                            )
                        }.using(
                            SizeTransform(clip = false) { _, _ -> snap() }
                        )
                    },
                    label = "onboardingStepTransition",
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) { step ->
                    when (step) {
                        OnboardingStep.WELCOME -> {
                            WelcomeStepContent(
                                onGetStarted = { viewModel.onIntent(OnboardingIntent.NextStep) }
                            )
                        }

                        OnboardingStep.INCOME -> {
                            IncomeStepContent(
                                incomeValue = uiState.monthlyIncome,
                                errorMsg = uiState.error,
                                onIncomeChange = {
                                    viewModel.onIntent(
                                        OnboardingIntent.UpdateIncome(
                                            it
                                        )
                                    )
                                },
                                onNext = { viewModel.onIntent(OnboardingIntent.NextStep) }
                            )
                        }

                        OnboardingStep.SAVINGS -> {
                            SavingsStepContent(
                                savingsValue = uiState.monthlySavingsGoal,
                                errorMsg = uiState.error,
                                onSavingsChange = {
                                    viewModel.onIntent(
                                        OnboardingIntent.UpdateSavingsGoal(
                                            it
                                        )
                                    )
                                },
                                onNext = { viewModel.onIntent(OnboardingIntent.NextStep) }
                            )
                        }

                        OnboardingStep.BILLS -> {
                            BillsStepContent(
                                billName = uiState.billName,
                                billAmount = uiState.billAmount,
                                billCategory = uiState.billCategory,
                                billDueDay = uiState.billDueDay,
                                billsList = uiState.bills,
                                onNameChange = {
                                    viewModel.onIntent(
                                        OnboardingIntent.UpdateBillName(
                                            it
                                        )
                                    )
                                },
                                onAmountChange = {
                                    viewModel.onIntent(
                                        OnboardingIntent.UpdateBillAmount(
                                            it
                                        )
                                    )
                                },
                                onCategoryChange = {
                                    viewModel.onIntent(
                                        OnboardingIntent.UpdateBillCategory(
                                            it
                                        )
                                    )
                                },
                                onDueDayChange = {
                                    viewModel.onIntent(
                                        OnboardingIntent.UpdateBillDueDay(
                                            it
                                        )
                                    )
                                },
                                onAddBill = { viewModel.onIntent(OnboardingIntent.AddBill) },
                                onDeleteBill = { viewModel.onIntent(OnboardingIntent.DeleteBill(it)) },
                                onNext = { viewModel.onIntent(OnboardingIntent.NextStep) }
                            )
                        }
                    }
                }
            }
        }
    }
}

