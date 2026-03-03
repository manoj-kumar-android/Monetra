package com.monetra.presentation.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.monetra.ui.theme.Spacing

@Composable
fun MonetraSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier
) {
    val isError = snackbarData.visuals.message.contains("Error", ignoreCase = true) || 
                  snackbarData.visuals.message.contains("Failed", ignoreCase = true)
    
    val icon = when {
        isError -> Icons.Default.Warning
        snackbarData.visuals.actionLabel != null -> Icons.Default.Info
        else -> Icons.Default.CheckCircle
    }

    // Adjusting colors to match the theme brightness (Dark in Dark, Light in Light)
    // and adding a subtle border for extra "premium" feel.
    val containerColor = if (isError) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isError) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    val borderColor = if (isError) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
    }

    Surface(
        modifier = modifier
            .padding(horizontal = 12.dp, vertical = 4.dp) // Reduced outer padding
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), // Slightly smaller corner radius
        color = containerColor,
        tonalElevation = 4.dp, // Lowered elevation for slimmer look
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp), // Reduced inner padding
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp) // Reduced icon size
            )
 
            Text(
                text = snackbarData.visuals.message,
                style = MaterialTheme.typography.bodySmall.copy( // Smaller typography
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.1.sp,
                    lineHeight = 16.sp
                ),
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
 
            snackbarData.visuals.actionLabel?.let { actionLabel ->
                TextButton(
                    onClick = { snackbarData.performAction() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = actionLabel.uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
            
            // Close Button
            IconButton(
                onClick = { snackbarData.dismiss() },
                modifier = Modifier
                    .size(28.dp) // Reduced size
                    .clip(CircleShape)
                    .background(contentColor.copy(alpha = 0.05f))
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = contentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(14.dp) // Reduced icon size
                )
            }
        }
    }
}
