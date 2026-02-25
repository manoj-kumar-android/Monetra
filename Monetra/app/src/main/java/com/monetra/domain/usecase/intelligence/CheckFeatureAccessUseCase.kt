package com.monetra.domain.usecase.intelligence

import com.monetra.domain.model.SubscriptionPlan
import com.monetra.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

enum class MonetraFeature {
    SAVING_SUGGESTIONS,
    INVESTMENT_RECOMMENDATIONS,
    WHAT_IF_SIMULATOR,
    FINANCIAL_HEALTH_SCORE,
    ADVANCED_ANALYTICS,
    BASIC_TRACKING
}

class CheckFeatureAccessUseCase @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) {
    operator fun invoke(feature: MonetraFeature): Flow<Boolean> {
        return subscriptionRepository.getSubscriptionStatus().map { subscription ->
            when (feature) {
                MonetraFeature.BASIC_TRACKING -> true
                else -> subscription.plan == SubscriptionPlan.PREMIUM
            }
        }
    }
}
