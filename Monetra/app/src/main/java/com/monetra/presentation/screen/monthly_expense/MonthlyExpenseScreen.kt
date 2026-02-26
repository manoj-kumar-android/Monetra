package com.monetra.presentation.screen.monthly_expense

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.domain.model.MonthlyExpense
import com.monetra.presentation.components.HelpIconButton
import com.monetra.ui.theme.Spacing
import androidx.compose.ui.res.stringResource
import com.monetra.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyExpenseScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    viewModel: MonthlyExpenseViewModel = hiltViewModel()
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.fixed_monthly_costs_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    HelpIconButton(onClick = onNavigateToHelp)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.toggleAddSheet(true) },
                containerColor = MaterialTheme.colorScheme.primary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_fixed_cost))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            item {
                Text(
                    text = stringResource(R.string.fixed_costs_instruction),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )
            }
            if (expenses.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(0.7f), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_fixed_costs_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(expenses) { expense ->
                FixedCostItem(expense, onDelete = { viewModel.onDeleteExpense(expense) })
            }
        }

        if (uiState.isAddSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleAddSheet(false) },
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.xl).padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(stringResource(R.string.add_recurring_cost_title), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text(stringResource(R.string.recurring_expense_hint)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = uiState.amount,
                        onValueChange = viewModel::onAmountChange,
                        label = { Text(stringResource(R.string.amount_label)) },
                        prefix = { Text(stringResource(R.string.rupee_symbol)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = viewModel::onSaveExpense,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.save_monthly_cost))
                    }
                }
            }
        }
    }
}

@Composable
private fun FixedCostItem(expense: MonthlyExpense, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = expense.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.per_month_format, expense.amount), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
            }
        }
    }
}
