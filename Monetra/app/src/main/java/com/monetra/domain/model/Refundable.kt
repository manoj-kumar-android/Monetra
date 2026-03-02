package com.monetra.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/** LENT = I gave money to someone, they owe me. BORROWED = I took money from someone, I owe them. */
enum class RefundableType { LENT, BORROWED }

data class Refundable(
    val id: Long = 0,
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    val amount: Double,
    val personName: String,
    val phoneNumber: String,
    val givenDate: LocalDate,
    val dueDate: LocalDateTime,
    val note: String?,
    val isPaid: Boolean = false,
    val remindMe: Boolean = false,
    val entryType: RefundableType = RefundableType.LENT,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : Syncable {
    val status: RefundableStatus
        get() {
            return when {
                isPaid -> RefundableStatus.PAID
                dueDate.toLocalDate().isBefore(LocalDate.now()) -> RefundableStatus.OVERDUE
                else -> RefundableStatus.PENDING
            }
        }
}

enum class RefundableStatus {
    PENDING, OVERDUE, PAID
}
