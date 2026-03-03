package com.monetra.domain.repository

import kotlinx.coroutines.flow.Flow

interface CloudBackupRepository {
    suspend fun runBackup(): Result<Unit>
    suspend fun runRestore(): Result<Unit>
    suspend fun isBackupAvailable(): Boolean
    fun scheduleBackup()
    val events: Flow<BackupEvent>
    val isRestoring: Flow<Boolean>
}

sealed interface BackupEvent {
    data object AuthError : BackupEvent
}
