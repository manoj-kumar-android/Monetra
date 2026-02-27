package com.monetra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalDateTime

@Entity(tableName = "refundable")
data class RefundableEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val personName: String,
    val phoneNumber: String,
    val givenDate: LocalDate,
    val dueDate: LocalDateTime,
    val note: String?,
    val isPaid: Boolean = false,
    val remindMe: Boolean = false,
    val sendSmsReminder: Boolean = false,
    val sendSmsImmediately: Boolean = false,
    val entryType: String = "LENT"
)
