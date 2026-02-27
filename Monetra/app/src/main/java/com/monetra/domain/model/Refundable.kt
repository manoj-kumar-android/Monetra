package com.monetra.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/** LENT = I gave money to someone, they owe me. BORROWED = I took money from someone, I owe them. */
enum class RefundableType { LENT, BORROWED }

data class Refundable(
    val id: Long = 0,
    val amount: Double,
    val personName: String,
    val phoneNumber: String,
    val givenDate: LocalDate,
    val dueDate: LocalDateTime,
    val note: String?,
    val isPaid: Boolean = false,
    val remindMe: Boolean = false,
    val entryType: RefundableType = RefundableType.LENT
) {
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
