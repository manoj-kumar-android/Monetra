package com.monetra.presentation.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.monetra.domain.model.SyncState

@Composable
fun SyncStatusAction(
    state: SyncState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tint = when (state) {
        is SyncState.Syncing -> MaterialTheme.colorScheme.primary
        is SyncState.Synced -> Color(0xFF34C759)
        is SyncState.Success -> Color(0xFF34C759)
        is SyncState.Error -> MaterialTheme.colorScheme.error
        is SyncState.AccountMismatch -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
    }

    val icon = when (state) {
        is SyncState.Syncing -> Icons.Default.CloudSync
        is SyncState.Synced -> Icons.Default.CloudDone
        is SyncState.Success -> Icons.Default.CloudDone
        is SyncState.Pending -> Icons.Default.CloudUpload
        is SyncState.Error -> Icons.Default.SyncProblem
        is SyncState.AccountMismatch -> Icons.Default.SyncProblem
        else -> Icons.Default.CloudUpload
    }

    IconButton(onClick = onClick, modifier = modifier) {
        if (state is SyncState.Syncing) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = tint
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = "Sync Status",
                tint = tint,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
