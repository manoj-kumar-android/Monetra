package com.monetra.presentation.screen.pending

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.provider.Settings
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.R
import com.monetra.domain.model.PendingTransaction
import com.monetra.domain.model.TransactionType
import com.monetra.presentation.component.SwipeToDeleteContainer
import com.monetra.ui.theme.Spacing
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendingTransactionsScreen(
    onNavigateBack: () -> Unit,
    onSelectPending: (Long) -> Unit,
    viewModel: PendingTransactionsViewModel = hiltViewModel()
) {
    val pendingList by viewModel.pendingTransactions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Check if permission is granted
    var isPermissionGranted by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val packageName = context.packageName
                val listeners = Settings.Secure.getString(
                    context.contentResolver,
                    "enabled_notification_listeners"
                )
                isPermissionGranted = listeners != null && listeners.contains(packageName)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.pending_transactions_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!isPermissionGranted) {
                PermissionDisclosureCard {
                    context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }
            } else {
                Text(
                    text = stringResource(R.string.suggestions_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)
                )

                if (pendingList.isEmpty()) {
                    EmptySuggestionsView()
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(Spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(Spacing.md)
                    ) {
                        items(pendingList, key = { it.id }) { pending ->
                            SwipeToDeleteContainer(
                                onDelete = { viewModel.deletePending(pending.id) },
                                title = "Remove Suggestion?",
                                message = "This will remove this suggestion from the list."
                            ) {
                                PendingTransactionItem(
                                    pending = pending,
                                    onClick = { onSelectPending(pending.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionDisclosureCard(onGrantClick: () -> Unit) {
    Card(
        modifier = Modifier.padding(Spacing.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                alpha = 0.5f
            )
        )
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                "Automate Tracking",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                stringResource(R.string.notification_access_disclosure),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Button(onClick = onGrantClick) {
                Text(stringResource(R.string.grant_notification_access))
            }
        }
    }
}

@Composable
fun PendingTransactionItem(
    pending: PendingTransaction,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val appIcon = remember(pending.sourceApp) {
        try {
            context.packageManager.getApplicationIcon(pending.sourceApp)
        } catch (e: Exception) {
            null
        }
    }
    val dateFormatter = remember { SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault()) }
    val formattedDate = dateFormatter.format(Date(pending.timestamp))
    val isIncome = pending.type == TransactionType.INCOME

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isIncome) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    val bitmap = remember(appIcon) {
                        if (appIcon is BitmapDrawable) {
                            appIcon.bitmap.asImageBitmap()
                        } else {
                            val bmp = Bitmap.createBitmap(
                                appIcon.intrinsicWidth.coerceAtLeast(1),
                                appIcon.intrinsicHeight.coerceAtLeast(1),
                                Bitmap.Config.ARGB_8888
                            )
                            val canvas = Canvas(bmp)
                            appIcon.setBounds(0, 0, canvas.width, canvas.height)
                            appIcon.draw(canvas)
                            bmp.asImageBitmap()
                        }
                    }
                    androidx.compose.foundation.Image(
                        bitmap = bitmap,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        if (isIncome) "↓" else "↑",
                        color = if (isIncome) Color(0xFF2E7D32) else Color(0xFFC62828),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pending.senderReceiver,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isIncome) "+" else "-") + stringResource(R.string.rupee_symbol) + "%.2f".format(
                        pending.amount
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = pending.sourceApp.split(".").last(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun EmptySuggestionsView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 120.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = stringResource(R.string.no_suggestions_msg),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
