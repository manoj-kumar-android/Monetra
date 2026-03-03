package com.monetra.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.monetra.presentation.screen.transactions.TransactionUiItem
import com.monetra.ui.theme.ComponentDefaults
import com.monetra.ui.theme.Elevation
import com.monetra.ui.theme.SemanticExpense
import com.monetra.ui.theme.SemanticIncome
import com.monetra.ui.theme.Spacing

/**
 * A single transaction row.
 *
 * Design constraints enforced here:
 *
 *  • Receives [TransactionUiItem] — stable types only. Compose's compiler can
 *    skip recomposition when the same item is passed again.
 *
 *  • Receives plain lambda callbacks — no ViewModel reference, no coroutine
 *    scope, no state reads. Purely declarative.
 *
 *  • No formatting, no business logic, no conditionals beyond color selection.
 *    Everything computed in the ViewModel arrives pre-formatted as Strings.
 *
 *  • isIncome drives color as a simple Boolean — avoids importing the domain
 *    enum (TransactionType) into the presentation component layer.
 */
@Composable
fun ExpenseRow(
    item: TransactionUiItem,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth(),
        shape     = ComponentDefaults.cardShape,
        elevation = CardDefaults.cardElevation(defaultElevation = Elevation.card),
    ) {
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Spacing.listItemHorizontal,
                    vertical   = Spacing.listItemVertical,
                ),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {

            // ── Type indicator ─────────────────────────────────────────────
            // A small dot communicates income vs expense without text.
            // Color comes from a stable Boolean — no enum or LocalDate involved.
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(
                        color = if (item.isIncome) SemanticIncome else SemanticExpense,
                        shape = CircleShape,
                    ),
            )

            // ── Title + date ────────────────────────────────────────────────
            // weight(1f) lets this column take remaining horizontal space,
            // keeping the amount and delete button pinned to the end.
            Column(
                modifier            = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs),
            ) {
                Text(
                    text     = item.title,
                    style    = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    // formattedDate is a pre-computed String — no DateTimeFormatter
                    // runs inside this composable.
                    text  = item.formattedDate,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            // ── Amount ──────────────────────────────────────────────────────
            // formattedAmount is a pre-computed String like "+₹1,500.00".
            // Color is the only conditional — driven by a stable Boolean.
            Text(
                text  = item.formattedAmount,
                style = MaterialTheme.typography.titleMedium,
                color = if (item.isIncome) SemanticIncome else SemanticExpense,
            )

            // ── Delete ──────────────────────────────────────────────────────
            // Minimum touch target: 44×44dp (iOS HIG + Material minimum).
            // Uses TextButton with reduced padding rather than IconButton to
            // avoid the material-icons-extended dependency for a simple × glyph.
            TextButton(
                onClick        = onDeleteClick,
                contentPadding = PaddingValues(horizontal = Spacing.xs),
                modifier       = Modifier.defaultMinSize(
                    minWidth  = Spacing.minTouchTarget,
                    minHeight = Spacing.minTouchTarget,
                ),
            ) {
                Text(
                    text  = "✕",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
