package com.monetra.data.worker

import android.content.Context
import com.monetra.data.local.dao.PendingDeleteDao
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
    @ApplicationContext private val context: Context
) {
    /** Mark an entity for deletion (starts the undo-safe grace period). */
    suspend fun requestDelete(entityId: Long, remoteId: String, entityType: String) {
        pendingDeleteDao.insert(
            PendingDeleteEntity(
                entityId = entityId,
                remoteId = remoteId,
                entityType = entityType
            )
        )
        // Enqueue the single garbage-collector worker
        PendingDeleteWorker.enqueue(context)
    }

    /** Cancel a pending delete (undo). */
    suspend fun cancelDelete(entityId: Long, entityType: String) {
        pendingDeleteDao.cancelDelete(entityId, entityType)
    }

    /** Observe which entity IDs are pending deletion (for UI filtering). */
    fun getPendingIds(entityType: String): Flow<List<Long>> {
        return pendingDeleteDao.getPendingIdsForType(entityType)
    }
}
