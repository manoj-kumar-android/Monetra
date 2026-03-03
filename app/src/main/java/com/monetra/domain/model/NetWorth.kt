package com.monetra.domain.model

data class NetWorth(
    val totalAssets: Double,
    val totalLiabilities: Double,
    val components: NetWorthComponents
) {
    val netAmount: Double get() = totalAssets - totalLiabilities
}

data class NetWorthComponents(
    val cashAndBank: Double,
    val investments: Double,
    val properties: Double,
    val loansAndEmis: Double
)
