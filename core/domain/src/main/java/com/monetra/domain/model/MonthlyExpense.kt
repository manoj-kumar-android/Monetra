package com.monetra.domain.model

import java.time.YearMonth

data class MonthlyExpense(
    val id: Long = 0L,
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    val name: String,
    val amount: Double,
    val category: String = "Bills",
    val dueDay: Int = 1,
    override val version: Long = 1L,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : Syncable

data class BillInstance(
    val id: Long = 0L,
    override val remoteId: String = java.util.UUID.randomUUID().toString(),
    val billId: Long,
    val month: YearMonth,
    val amount: Double,
    val paidAmount: Double = 0.0,
    val status: BillStatus = BillStatus.PENDING,
    override val version: Long = 1L,
    override val updatedAt: Long = System.currentTimeMillis(),
    override val deviceId: String = "",
    override val isSynced: Boolean = false
) : Syncable {
    val remainingAmount: Double
        get() = (amount - paidAmount).coerceAtLeast(0.0)

    val isPaid: Boolean
        get() = status == BillStatus.PAID
}
