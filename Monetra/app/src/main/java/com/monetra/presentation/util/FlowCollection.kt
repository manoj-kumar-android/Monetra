@file:Suppress("UNUSED_VARIABLE", "UnusedReceiverParameter")

package com.monetra.presentation.util

/**
 * FlowCollection.kt
 *
 * Reference file — explains and demonstrates correct vs. incorrect Flow
 * collection inside Compose. Not used at runtime; exists for reference only.
 *
 * ─────────────────────────────────────────────────────────────────────────
 * REQUIRED DEPENDENCY (already added to build.gradle.kts)
 * ─────────────────────────────────────────────────────────────────────────
 * implementation("androidx.lifecycle:lifecycle-runtime-compose:<version>")
 *
 * This ships the collectAsStateWithLifecycle() extension.
 * The version is managed by lifecycleRuntimeKtx in libs.versions.toml.
 * ─────────────────────────────────────────────────────────────────────────
 */

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.monetra.presentation.screen.transactions.ExpenseUiState
import com.monetra.presentation.screen.transactions.TransactionListViewModel
import kotlinx.coroutines.flow.StateFlow

// ══════════════════════════════════════════════════════════════════════════════
// ❌  APPROACH 1 — collectAsState()
// ══════════════════════════════════════════════════════════════════════════════

/**
 * collectAsState() ties collection to the COMPOSITION lifetime.
 *
 * The composition is alive whenever the composable is in the composition tree —
 * which, in Navigation Compose, includes screens sitting in the back stack AND
 * screens whose Activity/Fragment is stopped (app in background, screen off).
 *
 * Problems:
 *  1. App goes to background → Activity.onStop() fires → lifecycle = STOPPED
 *     → but the composition is still alive → the flow keeps collecting.
 *  2. Room emits a DB update from a background sync job → the state updates →
 *     Compose schedules a recomposition → of a screen the user cannot see.
 *  3. If the Success → recompose path triggers analytics or logging,
 *     those fire silently in the background.
 */
@Composable
private fun WrongApproach_collectAsState(viewModel: TransactionListViewModel) {

    // ❌ Collects forever — no awareness of Android lifecycle state
    val uiState: State<ExpenseUiState> = viewModel.uiState.collectAsState()

    // Even when the app is backgrounded, if Room emits a new row,
    // uiState.value changes here and triggers unnecessary recomposition.
}

// ══════════════════════════════════════════════════════════════════════════════
// ❌  APPROACH 2 — manual LaunchedEffect + collect {}
// ══════════════════════════════════════════════════════════════════════════════

/**
 * LaunchedEffect launches a coroutine in the CompositionLocal coroutine scope.
 * That scope is cancelled when the composable LEAVES the composition —
 * but NOT when the lifecycle drops to STOPPED.
 *
 * Problems:
 *  1. Same background-collection issue as Approach 1.
 *  2. The mutableStateOf local variable is disconnected from the ViewModel's
 *     StateFlow contract — you're duplicating state, which can diverge.
 *  3. If the key (Unit) is wrong or the flow is re-subscribed, you can miss
 *     emissions or double-collect on recomposition.
 *  4. Verbose — three lines where one would do.
 */
@Composable
private fun WrongApproach_LaunchedEffect(viewModel: TransactionListViewModel) {

    // ❌ Local mutable state — a duplicate of the ViewModel's own state
    var uiState by remember { mutableStateOf<ExpenseUiState>(ExpenseUiState.Loading) }

    LaunchedEffect(Unit) {
        // ❌ Coroutine scope tied to composition, not Android lifecycle
        viewModel.uiState.collect { newState ->
            uiState = newState  // collects even in background
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// ✅  APPROACH 3 — collectAsStateWithLifecycle()   ← always use this
// ══════════════════════════════════════════════════════════════════════════════

/**
 * collectAsStateWithLifecycle() does two things that the others don't:
 *
 *  1. It reads the lifecycle from [LocalLifecycleOwner] (the Activity or
 *     Fragment that hosts this composable).
 *  2. It automatically PAUSES the upstream collection whenever the lifecycle
 *     drops below [minActiveState] (default = Lifecycle.State.STARTED), and
 *     RESUMES it when the lifecycle comes back up.
 *
 * This means:
 *  - App in background (STOPPED)     → collection paused   ✓
 *  - Screen off / lock screen (STOPPED) → collection paused ✓
 *  - Screen returns to foreground (STARTED) → collection resumes ✓
 *
 * No wasted emissions, no invisible recompositions, no battery drain.
 * StateFlow always re-emits its latest value on resumption, so the UI
 * is always consistent when it becomes visible again.
 */
@Composable
fun CorrectApproach_collectAsStateWithLifecycle(viewModel: TransactionListViewModel) {

    // ✅ Pauses when lifecycle < STARTED. Resumes when lifecycle ≥ STARTED.
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // uiState here is an immutable snapshot — same as approach 1, but safe.
}

// ══════════════════════════════════════════════════════════════════════════════
// ✅  minActiveState — fine-tuning when collection pauses
// ══════════════════════════════════════════════════════════════════════════════

/**
 * The default minimum active state is STARTED, which covers the most common
 * need. You can tighten it to RESUMED to pause even when the screen is
 * partially obscured (e.g., a dialog from another app is on top).
 *
 * For Monetra, STARTED (default) is the right choice — we want the data
 * ready the moment the screen becomes visible, not just when it's on top.
 */
@Composable
private fun CollectAsStateWithLifecycle_CustomMinState(flow: StateFlow<ExpenseUiState>) {

    // Pause at CREATED and below (stricter — only collects at STARTED+)
    val stateAtStarted by flow.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.STARTED   // ← default, explicit here for clarity
    )

    // Pause at STARTED and below (stricter still — only collects at RESUMED)
    val stateAtResumed by flow.collectAsStateWithLifecycle(
        minActiveState = Lifecycle.State.RESUMED   // use only if truly needed
    )
}

// ══════════════════════════════════════════════════════════════════════════════
// ✅  One-shot events — Channel + LaunchedEffect (not collectAsStateWithLifecycle)
// ══════════════════════════════════════════════════════════════════════════════

/**
 * collectAsStateWithLifecycle() is for STATE — values that represent the
 * current condition and that the UI should always show.
 *
 * One-shot events (navigate, show snackbar) must NOT go through state.
 * They use a Channel exposed as a Flow, collected via LaunchedEffect.
 *
 * LaunchedEffect IS correct here because:
 *  - Channel.receiveAsFlow() suspends when empty — it is not a hot producer.
 *  - The coroutine only works when an event arrives, so no background waste.
 *  - If the screen is off when the event fires, the Channel buffers it and
 *    delivers it the moment the composable recomposes on return.
 *
 * Key: use viewModel.events (the Flow reference) as the LaunchedEffect key
 * so the coroutine restarts only if the Flow object itself changes (never).
 */
@Composable
private fun EventCollection_LaunchedEffect(viewModel: TransactionListViewModel) {

    LaunchedEffect(viewModel.events) {          // key = stable Flow reference
        viewModel.events.collect { event ->     // suspends between events — no waste
            // handle navigation, snackbar, etc.
        }
    }
}

// ══════════════════════════════════════════════════════════════════════════════
// SUMMARY TABLE
// ══════════════════════════════════════════════════════════════════════════════
//
//  Approach                           Lifecycle-aware?  Correct for state?
//  ─────────────────────────────────────────────────────────────────────────
//  collectAsState()                        ❌                  ❌
//  LaunchedEffect + collect {}             ❌                  ❌
//  collectAsStateWithLifecycle()           ✅                  ✅   ← use this
//  LaunchedEffect + collect {} (events)    ❌ (safe anyway)    N/A  ← correct for Channel
//  ─────────────────────────────────────────────────────────────────────────
