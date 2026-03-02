package com.monetra.domain.model

interface Syncable {
    val remoteId: String
    val version: Long
    val updatedAt: Long
    val deviceId: String
    val isSynced: Boolean
}
