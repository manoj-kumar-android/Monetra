package com.monetra.domain.repository

import kotlinx.coroutines.flow.Flow

interface CloudBackupRepository {
    suspend fun runBackup(): Result<Unit>
    suspend fun runSync(): Result<Unit>
    suspend fun runRestore(): Result<Unit>
    suspend fun isBackupAvailable(): Boolean
    fun scheduleBackup()
    suspend fun checkDrivePermission(): Boolean
    suspend fun signOut()
    
    val events: Flow<BackupEvent>
    val isRestoring: Flow<Boolean>
    val syncState: Flow<com.monetra.domain.model.SyncState>
    val accountName: Flow<String?>
    val lastBackupTime: Flow<Long?>
    val recoveryIntent: Flow<android.content.Intent?>
}

sealed interface BackupEvent {
    data object AuthError : BackupEvent
    data class Error(val message: String) : BackupEvent
}
