package com.monetra.data.repository

import com.monetra.data.local.dao.NoteDao
import com.monetra.data.local.entity.toDomainModel
import com.monetra.data.local.entity.toEntity
import com.monetra.domain.model.Note
import com.monetra.domain.repository.NoteRepository
import com.monetra.domain.repository.SyncRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NoteRepositoryImpl @Inject constructor(
    private val dao: NoteDao,
    private val syncRepository: SyncRepository
) : NoteRepository {

    override fun getNotes(): Flow<List<Note>> {
        return dao.getAllNotes().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getNoteById(id: Long): Note? {
        return dao.getNoteById(id)?.toDomainModel()
    }

    override suspend fun insertNote(note: Note) {
        val existing = if (note.id != 0L) {
            dao.getNoteById(note.id)
        } else {
            dao.getNoteByRemoteId(note.remoteId)
        }

        // Only update if there's a real change (to avoid sync loop/pointless noise)
        if (existing != null &&
            existing.title == note.title &&
            existing.content == note.content
        ) {
            return
        }

        val deviceId = syncRepository.getDeviceId()
        val syncNote = note.copy(
            id = existing?.id ?: note.id,
            remoteId = existing?.remoteId ?: note.remoteId,
            version = if (existing == null) 1L else existing.version + 1L,
            updatedAt = System.currentTimeMillis(),
            deviceId = deviceId,
            isSynced = false
        )
        dao.insertNote(syncNote.toEntity())
        syncRepository.clearTombstone(syncNote.remoteId)
        syncRepository.setDirty(true)
    }

    override suspend fun updateNote(note: Note) {
        insertNote(note)
    }

    override suspend fun deleteNote(id: Long) {
        dao.getNoteById(id)?.let { entity ->
            syncRepository.markDeleted(entity.remoteId, "NOTE")
            dao.deleteNoteById(id)
        }
    }

    override fun searchNotes(query: String): Flow<List<Note>> {
        return dao.searchNotes(query).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
}
