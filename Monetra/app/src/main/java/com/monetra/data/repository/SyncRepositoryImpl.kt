package com.monetra.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.monetra.domain.repository.SyncRepository
import com.monetra.data.local.MonetraDatabase
import com.monetra.data.local.entity.DeletedEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

private val Context.syncDataStore by preferencesDataStore(name = "sync_prefs")

@Singleton
class SyncRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: MonetraDatabase
) : SyncRepository {

    private val isDirtyKey = booleanPreferencesKey("is_dirty")
    private val lastSyncedAtKey = longPreferencesKey("last_synced_at")
    private val deviceIdKey = androidx.datastore.preferences.core.stringPreferencesKey("device_id")
    
    private val scope = CoroutineScope(Dispatchers.Default)
    private val syncRequests = MutableSharedFlow<Unit>(replay = 0)

    init {
        scope.launch {
            syncRequests.debounce(15000).collect {
                com.monetra.data.sync.SyncWorker.startSync(context)
            }
        }
    }

    override fun isDirty(): Flow<Boolean> = context.syncDataStore.data.map { 
        it[isDirtyKey] ?: false 
    }

    override suspend fun setDirty(dirty: Boolean) {
        context.syncDataStore.edit { it[isDirtyKey] = dirty }
        if (dirty) {
            requestSync()
        }
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
        scope.launch {
            syncRequests.emit(Unit)
        }
    }

    override suspend fun markDeleted(remoteId: String, entityType: String) {
        db.deletedEntityDao.insert(DeletedEntity(remoteId, entityType))
        setDirty(true)
    }

    override suspend fun clearTombstone(remoteId: String) {
        db.deletedEntityDao.deleteByRemoteIds(listOf(remoteId))
    }
}
