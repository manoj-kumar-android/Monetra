package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_deletes")
data class PendingDeleteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val entityId: Long,
    val remoteId: String,
    val entityType: String,  // TRANSACTION, MONTHLY_EXPENSE, LOAN, etc.
    val createdAt: Long = System.currentTimeMillis()
)
