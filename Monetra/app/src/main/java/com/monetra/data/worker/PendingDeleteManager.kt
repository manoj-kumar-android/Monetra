package com.monetra.data.worker

import android.content.Context
import com.monetra.data.local.dao.DeletedEntityDao
import com.monetra.data.local.dao.PendingDeleteDao
import com.monetra.data.local.entity.DeletedEntity
import com.monetra.data.local.entity.PendingDeleteEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central manager for delayed (undo-safe) deletions.
 *
 * Usage from ViewModel:
 *   requestDelete(entityId, remoteId, "TRANSACTION") → hides item + schedules worker
 *   cancelDelete(entityId, "TRANSACTION")            → undo — removes from queue
 *   getPendingIds("TRANSACTION")                     → Flow of IDs to filter from UI
 */
@Singleton
class PendingDeleteManager @Inject constructor(
    private val pendingDeleteDao: PendingDeleteDao,
    private val deletedEntityDao: DeletedEntityDao,
    @ApplicationContext private val context: Context
) {
    suspend fun requestDelete(entityId: Long, remoteId: String, entityType: String) {
        pendingDeleteDao.insert(
            PendingDeleteEntity(
                entityId = entityId,
                remoteId = remoteId,
                entityType = entityType
            )
        )
        // Immediate tombstone to prevent sync resurrection during grace period
        deletedEntityDao.insert(DeletedEntity(remoteId, entityType))
        
        // Enqueue the single garbage-collector worker
        PendingDeleteWorker.enqueue(context)
    }

    /** Cancel a pending delete (undo). */
    suspend fun cancelDelete(entityId: Long, entityType: String) {
        val entry = pendingDeleteDao.getPendingEntry(entityId, entityType)
        pendingDeleteDao.cancelDelete(entityId, entityType)
        
        // Remove tombstone if we are undoing
        entry?.let { 
            deletedEntityDao.deleteByRemoteIds(listOf(it.remoteId))
        }
    }

    /** Observe which entity IDs are pending deletion (for UI filtering). */
    fun getPendingIds(entityType: String): Flow<List<Long>> {
        return pendingDeleteDao.getPendingIdsForType(entityType)
    }
}
