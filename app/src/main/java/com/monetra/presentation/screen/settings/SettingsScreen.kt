package com.monetra.presentation.screen.settings

import android.app.Activity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.R
import com.monetra.ui.theme.Spacing
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCategories: () -> Unit,
    onNavigateToHelp: (String) -> Unit,
    onNavigateToWelcome: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMismatchDialog by remember { mutableStateOf<SettingsEvent.ShowAccountMismatch?>(null) }
    var showBackupConfirmationEmail by remember { mutableStateOf<String?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val activity = LocalActivity.current

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            when (event) {
                is SettingsEvent.SaveSuccess -> {
                    onNavigateBack()
                }

                is SettingsEvent.BackupSuccess -> {
                    snackbarHostState.showSnackbar("Backup completed successfully!")
                }

                is SettingsEvent.RestoreSuccess -> {
                    snackbarHostState.showSnackbar("Data restored successfully!")
                }

                is SettingsEvent.RestoreError -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is SettingsEvent.BackupError -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is SettingsEvent.AuthSuccess -> {
                    snackbarHostState.showSnackbar("Signed in successfully!")
                }

                is SettingsEvent.AuthError -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is SettingsEvent.NeedsAuthorization -> {
                    // Handled via LaunchedEffect below
                }

                is SettingsEvent.ShowAccountMismatch -> showMismatchDialog = event
                is SettingsEvent.ShowBackupConfirmation -> showBackupConfirmationEmail = event.email
                is SettingsEvent.SyncSuccess -> {
                    snackbarHostState.showSnackbar("Sync completed successfully!")
                }

                is SettingsEvent.SyncError -> {
                    snackbarHostState.showSnackbar(event.message)
                }

                is SettingsEvent.DeleteSuccess -> {
                    showDeleteConfirmation = false
                    onNavigateToWelcome()
                }
            }
        }
    }


    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            viewModel.onBackupToggle(true, activity as Activity)
        } else {
            viewModel.onPermissionDenied()
        }
    }

    LaunchedEffect(viewModel.recoveryIntent) {
        viewModel.recoveryIntent.collectLatest { intent ->
            intent?.let { permissionLauncher.launch(it) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (uiState.isRestoring) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
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

                // ── Cloud Backup Section ──────────────────────────────────────────
                Text(
                    text = "Backup & Sync",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (uiState.isBackupEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        else MaterialTheme.colorScheme.outlineVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(Spacing.lg)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            if (uiState.isBackupEnabled) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = when (uiState.syncStatus) {
                                            is com.monetra.domain.model.SyncState.Synced -> Icons.Default.CloudDone
                                            is com.monetra.domain.model.SyncState.Pending -> Icons.Default.CloudUpload
                                            is com.monetra.domain.model.SyncState.Syncing -> Icons.Default.CloudDone
                                            else -> Icons.Default.CloudUpload
                                        },
                                        contentDescription = null,
                                        tint = when (uiState.syncStatus) {
                                            is com.monetra.domain.model.SyncState.Synced -> Color(
                                                0xFF34C759
                                            )

                                            is com.monetra.domain.model.SyncState.Pending -> MaterialTheme.colorScheme.onSurfaceVariant
                                            is com.monetra.domain.model.SyncState.Syncing -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                                Spacer(modifier = Modifier.width(Spacing.md))
                                Column {
                                    Text(
                                        "Automatic Backup",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = uiState.accountName ?: "Secure your data on Drive",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = uiState.isBackupEnabled,
                                onCheckedChange = {
                                    viewModel.onBackupToggle(
                                        it,
                                        activity as Activity
                                    )
                                },
                                enabled = !uiState.isLoading
                            )
                        }

                        if (uiState.isBackupEnabled) {
                            Spacer(modifier = Modifier.height(Spacing.lg))
                            androidx.compose.foundation.Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                            ) {
                                drawRect(color = Color.LightGray.copy(alpha = 0.3f))
                            }
                            Spacer(modifier = Modifier.height(Spacing.md))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Last synced:",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = if (uiState.lastBackupTime != null) {
                                            val date = Date(uiState.lastBackupTime!!)
                                            val format =
                                                SimpleDateFormat(
                                                    "dd MMM, hh:mm a",
                                                    Locale.getDefault()
                                                )
                                            format.format(date)
                                        } else "Never",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                com.monetra.presentation.components.SyncStatusAction(
                                    state = uiState.syncStatus,
                                    onClick = { viewModel.onSyncClick(activity as Activity) }
                                )
                            }
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.preferences),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateToCategories),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(Spacing.lg)
                            .fillMaxWidth(),
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

                // ── Support & Intelligence ──────────────────────────────────────
                Text(
                    text = "Support & Intelligence",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )


                SupportCard(onClick = { onNavigateToHelp("DASHBOARD") })

                Spacer(modifier = Modifier.height(Spacing.md))

                DeleteDataCard(onClick = { showDeleteConfirmation = true })

                Spacer(modifier = Modifier.height(Spacing.xxl))
            }
        }

        showMismatchDialog?.let { mismatch ->
            com.monetra.presentation.screen.dashboard.AccountMismatchDialog(
                currentEmail = mismatch.currentEmail,
                lastSyncedEmail = mismatch.syncedEmail,
                onDismiss = {
                    showMismatchDialog = null
                    viewModel.onSignOutClick()
                }
            )
        }

        showBackupConfirmationEmail?.let { email ->
            com.monetra.presentation.screen.dashboard.BackupConfirmationDialog(
                email = email,
                onConfirm = {
                    showBackupConfirmationEmail = null
                    viewModel.onBackupToggle(true, activity as Activity, confirmed = true)
                },
                onCancel = {
                    showBackupConfirmationEmail = null
                    viewModel.onSignOutClick()
                }
            )
        }

        if (showDeleteConfirmation) {
            DeleteAllDataDialog(
                isLoading = uiState.isLoading,
                onConfirm = {
                    viewModel.onDeleteAllData()
                },
                onDismiss = { if (!uiState.isLoading) showDeleteConfirmation = false }
            )
        }
    }
}

@Composable
private fun SupportCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                alpha = 0.3f
            )
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🤔", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Help & Support",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Documentation and guides",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun DeleteDataCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(
                alpha = 0.2f
            )
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text("⚠️", fontSize = 22.sp)
            }
            Spacer(modifier = Modifier.width(Spacing.md))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Danger Zone",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    "Permanently delete all data",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
private fun DeleteAllDataDialog(
    isLoading: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var deleteInput by remember { mutableStateOf("") }
    val isConfirmEnabled = deleteInput == "DELETE" && !isLoading

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CloudDone, // Or another relevant icon
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = "Permanent Data Wiping",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.05f))
                        .padding(Spacing.md)
                ) {
                    Text(
                        text = "This action is irreversible and complies with Google Play Data Safety requirements. Continuing will permanently erase:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    val items = listOf(
                        "All local transactions and accounts history",
                        "Encrypted backups stored on Google Drive AppData",
                        "Personal configurations and app session"
                    )
                    items.forEach { item ->
                        Row(verticalAlignment = Alignment.Top) {
                            Text("• ", fontWeight = FontWeight.Bold)
                            Text(item, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                Text(
                    text = "To confirm this final action, please type 'DELETE' in all caps below:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                )

                OutlinedTextField(
                    value = deleteInput,
                    onValueChange = { deleteInput = it },
                    placeholder = { Text("DELETE") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !isLoading,
                    isError = deleteInput.isNotEmpty() && !"DELETE".startsWith(deleteInput),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = isConfirmEnabled,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    disabledContainerColor = MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onError
                    )
                } else {
                    Text("Confirm Wipe")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel")
            }
        }
    )
}
