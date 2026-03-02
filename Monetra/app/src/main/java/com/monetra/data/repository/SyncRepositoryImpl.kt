package com.monetra.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monetra.domain.repository.SyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncDataStore by preferencesDataStore(name = "sync_prefs")

@Singleton
class SyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SyncRepository {

    private val isDirtyKey = booleanPreferencesKey("is_dirty")
    private val lastSyncedAtKey = longPreferencesKey("last_synced_at")
    private val deviceIdKey = androidx.datastore.preferences.core.stringPreferencesKey("device_id")

    override fun isDirty(): Flow<Boolean> = context.syncDataStore.data.map { 
        it[isDirtyKey] ?: false 
    }

    override suspend fun setDirty(dirty: Boolean) {
        context.syncDataStore.edit { it[isDirtyKey] = dirty }
    }

    override fun getLastSyncedAt(): Flow<Long> = context.syncDataStore.data.map { 
        it[lastSyncedAtKey] ?: 0L 
    }

    override suspend fun setLastSyncedAt(timestamp: Long) {
        context.syncDataStore.edit { it[lastSyncedAtKey] = timestamp }
    }

    override suspend fun getDeviceId(): String {
        val currentId = context.syncDataStore.data.first()[deviceIdKey]
        if (currentId != null) return currentId
        
        val newId = java.util.UUID.randomUUID().toString()
        context.syncDataStore.edit { it[deviceIdKey] = newId }
        return newId
    }

    override fun requestSync() {
        com.monetra.data.sync.SyncWorker.startSync(context)
    }
}
