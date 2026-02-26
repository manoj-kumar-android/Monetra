package com.monetra.presentation.screen.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.ui.theme.Spacing
import androidx.compose.ui.res.stringResource
import com.monetra.R

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
    val income by viewModel.income.collectAsStateWithLifecycle()
    val name by viewModel.name.collectAsStateWithLifecycle()
    val savings by viewModel.savingsGoal.collectAsStateWithLifecycle()
    val fixedCosts by viewModel.fixedCosts.collectAsStateWithLifecycle()
    val loans by viewModel.loans.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            BottomAppBar(containerColor = MaterialTheme.colorScheme.background) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentStep > 0) {
                        TextButton(onClick = viewModel::previousStep) {
                            Text(stringResource(R.string.back))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(60.dp))
                    }

                    Row {
                        repeat(4) { i ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        if (i == currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                        CircleShape
                                    )
                                    .padding(horizontal = 4.dp)
                            )
                        }
                    }

                    Button(onClick = {
                        if (currentStep < 3) {
                            viewModel.nextStep()
                        } else {
                            viewModel.savePreferences()
                            onComplete()
                        }
                    }) {
                        Text(if (currentStep == 3) stringResource(R.string.finish) else stringResource(R.string.next))
                        Icon(if (currentStep == 3) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().verticalScroll(rememberScrollState())) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally(initialOffsetX = { it }) + fadeIn() togetherWith
                            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                }, label = "OnboardingTransition"
            ) { step ->
                when (step) {
                    0 -> SalarySetupStep(name, viewModel::setName, income, viewModel::setIncome, savings, viewModel::setSavingsGoal)
                    1 -> FixedCostsStep(fixedCosts, viewModel::addQuickFixedCost)
                    2 -> EMIsStep(loans, viewModel::addQuickEmi)
                    3 -> CalculationStep(
                        income = income.toDoubleOrNull() ?: 0.0,
                        savings = savings.toDoubleOrNull() ?: 0.0,
                        totalFixed = fixedCosts.sumOf { it.amount },
                        totalEmi = loans.sumOf { it.monthlyEmi }
                    )
                }
            }
        }
    }
}

@Composable
private fun SalarySetupStep(
    name: String,
    onNameChange: (String) -> Unit,
    income: String,
    onIncomeChange: (String) -> Unit,
    savings: String,
    onSavingsChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(Spacing.xl).fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.onboarding_welcome_title), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(stringResource(R.string.onboarding_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(Spacing.xxl))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.your_name_label)) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(Spacing.lg))

        OutlinedTextField(
            value = income,
            onValueChange = onIncomeChange,
            label = { Text(stringResource(R.string.monthly_income_label_settings)) },
            prefix = { Text("${stringResource(R.string.rupee_symbol)} ") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        
        OutlinedTextField(
            value = savings,
            onValueChange = onSavingsChange,
            label = { Text(stringResource(R.string.monthly_savings_goal_label)) },
            prefix = { Text("${stringResource(R.string.rupee_symbol)} ") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun FixedCostsStep(
    fixedCosts: List<com.monetra.domain.model.MonthlyExpense>,
    onAddCost: (String, Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(Spacing.xl).fillMaxSize()) {
        Text(stringResource(R.string.onboarding_step1_title), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(stringResource(R.string.onboarding_step1_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(Spacing.lg))

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.bill_name_hint)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.amount_label)) },
                    prefix = { Text("${stringResource(R.string.rupee_symbol)} ") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Button(
                    onClick = {
                        if (title.isNotBlank() && amount.toDoubleOrNull() != null) {
                            onAddCost(title, amount.toDouble())
                            title = ""
                            amount = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank() && amount.toDoubleOrNull() != null
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.add_fixed_cost))
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
        
        fixedCosts.forEach {
            ListItem(
                headlineContent = { Text(it.name) },
                trailingContent = { Text("${stringResource(R.string.rupee_symbol)}${it.amount}", fontWeight = FontWeight.Bold) }
            )
        }
    }
}

@Composable
private fun EMIsStep(
    loans: List<com.monetra.domain.model.Loan>,
    onAddEmi: (String, Double) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(Spacing.xl).fillMaxSize()) {
        Text(stringResource(R.string.onboarding_step2_title), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text(stringResource(R.string.onboarding_step2_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(Spacing.lg))

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.loan_name_hint)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text(stringResource(R.string.monthly_emi_label_onboarding)) },
                    prefix = { Text("${stringResource(R.string.rupee_symbol)} ") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Button(
                    onClick = {
                        if (title.isNotBlank() && amount.toDoubleOrNull() != null) {
                            onAddEmi(title, amount.toDouble())
                            title = ""
                            amount = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank() && amount.toDoubleOrNull() != null
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.add_emi_onboarding))
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
        
        loans.forEach {
            ListItem(
                headlineContent = { Text(it.name) },
                trailingContent = { Text("${stringResource(R.string.rupee_symbol)}${it.monthlyEmi}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) }
            )
        }
    }
}

@Composable
private fun CalculationStep(income: Double, savings: Double, totalFixed: Double, totalEmi: Double) {
    val allowance = (income - savings - totalFixed - totalEmi).coerceAtLeast(0.0)

    Column(modifier = Modifier.padding(Spacing.xl).fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text(stringResource(R.string.onboarding_step3_title), style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Spacing.md))
        Text(stringResource(R.string.onboarding_step3_subtitle), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(Spacing.xxl))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                CalcRow(stringResource(R.string.monthly_income_label), income, true)
                CalcRow(stringResource(R.string.monthly_savings_goal_label), savings, false)
                CalcRow(stringResource(R.string.fixed_bills), totalFixed, false)
                CalcRow(stringResource(R.string.emis_debt), totalEmi, false)
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.md))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(stringResource(R.string.safe_to_spend_label), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("${stringResource(R.string.rupee_symbol)}${"%,.0f".format(allowance)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text(stringResource(R.string.spend_freely_instruction), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CalcRow(label: String, amount: Double, isAddition: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        val symbol = if (isAddition) "+" else "-"
        val color = if (isAddition) Color(0xFF4CAF50) else Color(0xFFF44336)
        Text("$symbol ${stringResource(R.string.rupee_symbol)}${"%,.0f".format(amount)}", color = color, fontWeight = FontWeight.Medium)
    }
}
