package com.monetra.domain.model

sealed interface SyncState {
    data object Idle : SyncState
    data object Synced : SyncState
    data object Pending : SyncState
    data class Syncing(val message: String = "", val current: Int = 0, val total: Int = 0) : SyncState
    data object Success : SyncState
    data class Error(val message: String) : SyncState
    data class AccountMismatch(val currentEmail: String, val lastSyncedEmail: String) : SyncState
}
