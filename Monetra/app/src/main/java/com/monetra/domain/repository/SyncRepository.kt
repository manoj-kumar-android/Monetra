package com.monetra.domain.repository

import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun isDirty(): Flow<Boolean>
    suspend fun setDirty(dirty: Boolean)
    fun getLastSyncedAt(): Flow<Long>
    suspend fun setLastSyncedAt(timestamp: Long)
    suspend fun getDeviceId(): String
    fun requestSync()
}
