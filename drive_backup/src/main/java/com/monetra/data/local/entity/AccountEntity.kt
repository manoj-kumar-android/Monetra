package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

import kotlinx.serialization.Serializable

@Serializable
@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["name"], unique = true),
        Index(value = ["remoteId"], unique = true)
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    override val version: Long = 1L,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : SyncableEntity
