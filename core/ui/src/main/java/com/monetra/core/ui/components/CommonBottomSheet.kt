package com.monetra.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.monetra.core.ui.commonsheet.BottomSheetPopup
import com.monetra.core.ui.commonsheet.BottomSheetPopupScope

/**
 * Thin wrapper around [BottomSheetPopup].
 *
 * Opens a bottom sheet in its own Dialog window — sits above the keyboard
 * without any delays, focus clearing, or inset hacks.
 *
 * Content runs in [BottomSheetPopupScope]; call [BottomSheetPopupScope.dismiss]
 * to close with the slide-down animation.
 */
@Composable
fun CommonBottomSheet(
    isOpen: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BottomSheetPopupScope.() -> Unit
) {
    if (isOpen) {
        BottomSheetPopup(
            onDismissRequest = onDismissRequest,
            bottomSheetContent = content
        )
    }
}
