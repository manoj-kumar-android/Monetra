package com.monetra.domain.repository

interface CloudBackupRepository {
    suspend fun runBackup(): Result<Unit>
    suspend fun runRestore(): Result<Unit>
    suspend fun isBackupAvailable(): Boolean
    fun scheduleBackup()
}
