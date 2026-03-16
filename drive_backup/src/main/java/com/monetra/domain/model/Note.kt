package com.monetra.domain.model

data class Note(
    val id: Long = 0L,
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    override val version: Long = 1L,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : Syncable
