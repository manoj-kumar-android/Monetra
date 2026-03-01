package com.monetra.presentation.components

import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
fun rememberHapticClick(
    onClick: () -> Unit
): () -> Unit {
    val haptic = LocalHapticFeedback.current
    
    return remember(onClick) {
        {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onClick()
        }
    }
}
