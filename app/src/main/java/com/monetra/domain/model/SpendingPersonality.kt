package com.monetra.domain.model

data class SpendingPersonality(
    val type: PersonalityType,
    val description: String,
    val keyStrengths: List<String>,
    val areasOfImprovement: List<String>
)

enum class PersonalityType {
    IMPULSIVE_SPENDER,
    WEEKEND_SPENDER,
    SUBSCRIPTION_HEAVY,
    CONTROLLED_PLANNER,
    BALANCED_OVRALL
}
