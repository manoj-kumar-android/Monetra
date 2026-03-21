package com.monetra.domain.model

enum class SubscriptionPlan {
    FREE,
    PREMIUM_ONETIME
}

data class UserSubscription(
    val plan: SubscriptionPlan = SubscriptionPlan.FREE,
    val purchaseTimestamp: Long? = null
) {
    val isPremium: Boolean get() = plan == SubscriptionPlan.PREMIUM_ONETIME
}
