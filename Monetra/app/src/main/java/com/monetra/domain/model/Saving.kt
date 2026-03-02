package com.monetra.domain.model

data class Saving(
    val id: Long = 0,
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    val bankName: String,
    val amount: Double,
    val interestRate: Double?,
    val note: String,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : Syncable
