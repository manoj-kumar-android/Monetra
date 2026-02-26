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
                            Text("Back")
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
                        Text(if (currentStep == 3) "Finish" else "Next")
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
        Text("Welcome to Monetra 🚀", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Spacing.md))
        Text("Let's set up your financial baseline to calculate your safe-to-spend allowance.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(Spacing.xxl))

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Your Name") },
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
            label = { Text("Monthly Salary (Income)") },
            prefix = { Text("₹ ") },
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
            label = { Text("Monthly Savings Target") },
            prefix = { Text("₹ ") },
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
        Text("Step 1: Commitments", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text("What are your mandatory monthly utility bills and subscriptions?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(Spacing.lg))

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Bill Name (e.g., Rent, Internet)") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    prefix = { Text("₹ ") },
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
                    Text("Add Fixed Cost")
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
        
        fixedCosts.forEach {
            ListItem(
                headlineContent = { Text(it.name) },
                trailingContent = { Text("₹${it.amount}", fontWeight = FontWeight.Bold) }
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
        Text("Step 2: Loans & EMIs", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Spacing.sm))
        Text("Do you have any ongoing loan EMIs or debts?", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(Spacing.lg))

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f))) {
            Column(modifier = Modifier.padding(Spacing.md)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Loan Name (e.g., Car, Home)") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monthly EMI Amount") },
                    prefix = { Text("₹ ") },
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
                    Text("Add EMI")
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.lg))
        
        loans.forEach {
            ListItem(
                headlineContent = { Text(it.name) },
                trailingContent = { Text("₹${it.monthlyEmi}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error) }
            )
        }
    }
}

@Composable
private fun CalculationStep(income: Double, savings: Double, totalFixed: Double, totalEmi: Double) {
    val allowance = (income - savings - totalFixed - totalEmi).coerceAtLeast(0.0)

    Column(modifier = Modifier.padding(Spacing.xl).fillMaxSize(), verticalArrangement = Arrangement.Center) {
        Text("The Monetra Magic ✨", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(Spacing.md))
        Text("Here is how we calculate your true Safe-to-Spend limit.", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        
        Spacer(modifier = Modifier.height(Spacing.xxl))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(Spacing.lg)) {
                CalcRow("Monthly Salary", income, true)
                CalcRow("Savings Target", savings, false)
                CalcRow("Fixed Costs", totalFixed, false)
                CalcRow("Loan EMIs", totalEmi, false)
                HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.md))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Safe-to-Spend", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("₹%,.0f".format(allowance), style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary))
                }
            }
        }
        
        Spacer(modifier = Modifier.height(Spacing.lg))
        Text("Spend freely from this allowance! Monetra will guide you daily to keep you on track.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Text("$symbol ₹%,.0f".format(amount), color = color, fontWeight = FontWeight.Medium)
    }
}
