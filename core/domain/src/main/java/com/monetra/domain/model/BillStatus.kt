package com.monetra.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class BillStatus {
    PENDING, PARTIAL, PAID
}
