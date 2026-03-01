// Top-level build file — plugin declarations only, no dependencies here.
// Each plugin is declared with apply false so sub-modules opt in explicitly.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library)     apply false
    alias(libs.plugins.kotlin.compose)      apply false
    alias(libs.plugins.ksp)                 apply false   // Kotlin Symbol Processing
    alias(libs.plugins.hilt.android)        apply false   // Dagger Hilt
    alias(libs.plugins.kotlin.serialization) apply false
}
