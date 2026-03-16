package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.monetra.domain.model.Note

@Entity(
    tableName = "notes",
    indices = [
        Index(value = ["remoteId"], unique = true)
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    override val remoteId: String,
    val title: String,
    val content: String,
    override val version: Long,
    override val updatedAt: Long,
    override val deviceId: String,
    override val isSynced: Boolean
) : SyncableEntity

fun NoteEntity.toDomainModel(): Note {
    return Note(
        id = id,
        remoteId = remoteId,
        title = title,
        content = content,
        version = version,
        updatedAt = updatedAt,
        deviceId = deviceId,
        isSynced = isSynced
    )
}

fun Note.toEntity(): NoteEntity {
    return NoteEntity(
        id = id,
        remoteId = remoteId,
        title = title,
        content = content,
        version = version,
        updatedAt = updatedAt,
        deviceId = deviceId,
        isSynced = isSynced
    )
}
