package com.monetra.data.local.entity

interface SyncableEntity {
    val remoteId: String
    val updatedAt: Long
    val deviceId: String
    val isSynced: Boolean
}
