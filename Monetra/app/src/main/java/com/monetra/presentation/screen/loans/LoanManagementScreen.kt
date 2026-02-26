package com.monetra.presentation.screen.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.domain.model.Loan
import com.monetra.presentation.components.HelpIconButton
import com.monetra.ui.theme.Spacing
import androidx.compose.ui.res.stringResource
import com.monetra.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanManagementScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHelp: () -> Unit,
    viewModel: LoanViewModel = hiltViewModel()
) {
    val loans by viewModel.loans.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.debt_emis_title), fontWeight = FontWeight.Bold) },
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
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_loan))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            if (loans.isEmpty()) {
                item {
                    Box(Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Text(stringResource(R.string.no_loans_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            items(loans) { loan ->
                LoanItem(loan, onDelete = { viewModel.onDeleteLoan(loan) })
            }
        }

        if (uiState.isAddSheetOpen) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.toggleAddSheet(false) },
                dragHandle = { BottomSheetDefaults.DragHandle() },
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.xl).padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    Text(stringResource(R.string.add_new_emi), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = viewModel::onNameChange,
                        label = { Text(stringResource(R.string.loan_name_placeholder)) },
                        isError = uiState.nameError != null,
                        supportingText = if (uiState.nameError != null) { { Text(uiState.nameError!!) } } else null,
                        keyboardOptions = KeyboardOptions(
                            capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.Words,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = uiState.principal,
                        onValueChange = viewModel::onPrincipalChange,
                        label = { Text(stringResource(R.string.total_principal)) },
                        prefix = { Text(stringResource(R.string.rupee_symbol)) },
                        isError = uiState.principalError != null,
                        supportingText = if (uiState.principalError != null) { { Text(uiState.principalError!!) } } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = uiState.emi,
                        onValueChange = viewModel::onEmiChange,
                        label = { Text(stringResource(R.string.monthly_emi_label)) },
                        prefix = { Text(stringResource(R.string.rupee_symbol)) },
                        isError = uiState.emiError != null,
                        supportingText = if (uiState.emiError != null) { { Text(uiState.emiError!!) } } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = androidx.compose.ui.text.input.ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    OutlinedTextField(
                        value = uiState.tenure,
                        onValueChange = viewModel::onTenureChange,
                        label = { Text(stringResource(R.string.tenure_months)) },
                        isError = uiState.tenureError != null,
                        supportingText = if (uiState.tenureError != null) { { Text(uiState.tenureError!!) } } else null,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = viewModel::onSaveLoan,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(stringResource(R.string.save_debt_plan))
                    }
                }
            }
        }
    }
}

@Composable
private fun LoanItem(loan: Loan, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = loan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = stringResource(R.string.emi_format, loan.monthlyEmi), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                Text(text = stringResource(R.string.months_left_suffix, loan.remainingTenure), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
