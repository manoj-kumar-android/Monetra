package com.monetra.data.local.entity

interface SyncableEntity {
    val remoteId: String
    val version: Long
    val updatedAt: Long
    val deviceId: String
    val isSynced: Boolean
}
