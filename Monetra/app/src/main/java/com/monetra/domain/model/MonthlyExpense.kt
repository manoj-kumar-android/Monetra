package com.monetra.domain.model

import java.time.LocalDate
import java.time.YearMonth

/**
 * Represents a recurring bill rule configured by the user.
 * e.g., "WiFi" - ₹1,000 on the 10th of every month.
 */
data class MonthlyExpense(
    val id: Long = 0L,
    override   val remoteId: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val category: String = "Bills",
    val dueDay: Int = 1, // 1 to 31
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : Syncable

/**
 * Represents a specific instance of a [MonthlyExpense] for a given month.
 * This is what tracks the actual payment progress for that month.
 */
data class BillInstance(
    val id: Long = 0L,
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    val billId: Long,
    val month: YearMonth,
    val amount: Double, // Snapshot of the bill's amount at creation
    val paidAmount: Double = 0.0,
    val status: BillStatus = BillStatus.PENDING,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : Syncable {
    val remainingAmount: Double
        get() = (amount - paidAmount).coerceAtLeast(0.0)
        
    val isPaid: Boolean
        get() = status == BillStatus.PAID
}
