package com.monetra.domain.model

enum class SubscriptionPlan {
    FREE,
    PREMIUM
}

data class UserSubscription(
    val plan: SubscriptionPlan,
    val expiryTimestamp: Long? = null,
    val isAutoRenewEnabled: Boolean = false
) {
    val isPremium: Boolean get() = plan == SubscriptionPlan.PREMIUM
}
