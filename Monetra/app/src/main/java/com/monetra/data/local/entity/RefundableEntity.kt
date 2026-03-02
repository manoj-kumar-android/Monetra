package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

import kotlinx.serialization.Serializable
import com.monetra.data.local.util.LocalDateSerializer
import com.monetra.data.local.util.LocalDateTimeSerializer

@Serializable
@Entity(tableName = "refundable")
data class RefundableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    val amount: Double,
    val personName: String,
    val phoneNumber: String,
    @Serializable(with = LocalDateSerializer::class)
    val givenDate: LocalDate,
    @Serializable(with = LocalDateTimeSerializer::class)
    val dueDate: LocalDateTime,
    val note: String?,
    val isPaid: Boolean = false,
    val remindMe: Boolean = false,
    val entryType: String = "LENT",
    override val version: Long = 1L,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : SyncableEntity
