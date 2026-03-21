package com.monetra.drivebackup.api

object VipConfig {
    private val VIP_EMAILS = setOf(
        "smanoj7032@gmail.com",
        "ms5967230@gmail.com"
    )

    fun isVip(email: String?): Boolean {
        return VIP_EMAILS.contains(email?.lowercase())
    }
}
