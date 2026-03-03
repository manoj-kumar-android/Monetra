package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "deleted_entities")
data class DeletedEntity(
    @PrimaryKey
    val remoteId: String,
    val entityType: String,
    val deletedAt: Long = System.currentTimeMillis()
)
