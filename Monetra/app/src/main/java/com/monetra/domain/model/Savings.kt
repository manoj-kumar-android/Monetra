package com.monetra.domain.model

data class Savings(
    val id: Long = 0,
    val bankName: String,
    val amount: Double,
    val interestRate: Double?,
    val note: String
)
