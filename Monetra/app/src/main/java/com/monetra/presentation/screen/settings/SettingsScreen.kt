package com.monetra.presentation.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.monetra.R
import com.monetra.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showPasswordDialog by remember { mutableStateOf(false) }
    var passwordMode by remember { mutableStateOf(PasswordDialogMode.EXPORT) } // EXPORT or IMPORT
    var pendingUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.CreateDocument("application/octet-stream"),
        onResult = { uri ->
            uri?.let { 
                pendingUri = it
                passwordMode = PasswordDialogMode.EXPORT
                showPasswordDialog = true
            }
        }
    )

    val restoreLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { 
                pendingUri = it
                passwordMode = PasswordDialogMode.IMPORT
                showPasswordDialog = true
            }
        }
    )

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.SaveSuccess -> {
                    onNavigateBack()
                }
                is SettingsEvent.RestoreSuccess -> {
                    snackbarHostState.showSnackbar("Data restored successfully!")
                }
                is SettingsEvent.RestoreError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is SettingsEvent.SyncSuccess -> {
                    snackbarHostState.showSnackbar("Encrypted backup created successfully!")
                }
                is SettingsEvent.SyncError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    if (showPasswordDialog) {
        PasswordDialog(
            mode = passwordMode,
            onDismiss = { showPasswordDialog = false },
            onConfirm = { password ->
                showPasswordDialog = false
                pendingUri?.let { uri ->
                    if (passwordMode == PasswordDialogMode.EXPORT) {
                        viewModel.onExportEncryptedBackup(uri, password)
                    } else {
                        viewModel.onRestoreFromEncryptedUri(uri, password)
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(Spacing.xl)
        ) {
            Text(
                text = stringResource(R.string.profile_financial_goals),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(Spacing.lg)) {
                     OutlinedTextField(
                        value = uiState.ownerName,
                        onValueChange = viewModel::onNameChange,
                        label = { Text(stringResource(R.string.your_name_label)) },
                        isError = uiState.nameError != null,
                        supportingText = if (uiState.nameError != null) {
                            { Text(uiState.nameError!!) }
                        } else null,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.md))

                    OutlinedTextField(
                        value = uiState.monthlyIncome,
                        onValueChange = viewModel::onIncomeChange,
                        label = { Text(stringResource(R.string.monthly_income_label_settings)) },
                        prefix = { Text(stringResource(R.string.rupee_symbol)) },
                        isError = uiState.incomeError != null,
                        supportingText = if (uiState.incomeError != null) {
                            { Text(uiState.incomeError!!) }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(Spacing.md))
                    
                    OutlinedTextField(
                        value = uiState.monthlySavingsGoal,
                        onValueChange = viewModel::onSavingsGoalChange,
                        label = { Text(stringResource(R.string.monthly_savings_goal_label)) },
                        prefix = { Text(stringResource(R.string.rupee_symbol)) },
                        isError = uiState.savingsError != null,
                        supportingText = if (uiState.savingsError != null) {
                            { Text(uiState.savingsError!!) }
                        } else null,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Button(
                onClick = viewModel::onSaveClick,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else if (uiState.isSuccess) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(stringResource(R.string.saved_successfully))
                    }
                } else {
                    Text(stringResource(R.string.save_settings))
                }
            }
            
            Text(
                text = "Use these settings to tailor Monetra's financial insights to your lifestyle.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Text(
                text = stringResource(R.string.preferences),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onNavigateToCategories),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.lg).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.manage_category_budgets),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = "Privacy & Security",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Export Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.lg).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Export Encrypted Backup",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Secure your data for migration or safety.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { exportLauncher.launch("monetra_backup_${System.currentTimeMillis()}.monetra") },
                        enabled = !uiState.isSyncing,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Export")
                    }
                }
            }

            // Import Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(Spacing.lg).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (uiState.isRestoring) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Import Encrypted Backup",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Restore from a .monetra backup file.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(
                        onClick = { restoreLauncher.launch(arrayOf("*/*")) },
                        enabled = !uiState.isRestoring,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Import")
                    }
                }
            }
        }
    }
}

enum class PasswordDialogMode {
    EXPORT, IMPORT
}

@Composable
fun PasswordDialog(
    mode: PasswordDialogMode,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                if (mode == PasswordDialogMode.EXPORT) "Set Backup Password" else "Enter Backup Password",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column {
                Text(
                    if (mode == PasswordDialogMode.EXPORT) 
                        "Choose a password to encrypt your backup. You will need this to restore the data on ANY device."
                    else 
                        "Enter the password you used when creating this backup.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null)
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (password.isNotEmpty()) onConfirm(password) },
                enabled = password.isNotEmpty(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (mode == PasswordDialogMode.EXPORT) "Export" else "Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
