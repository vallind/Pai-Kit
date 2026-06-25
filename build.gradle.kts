// ============================================================================
// Root build.gradle.kts - 单模块项目（Bleeding-Edge 2026）
// AGP 9.2.1 / Kotlin 2.3.21 / KSP 2.3.9
// ============================================================================

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt.android) apply false
    alias(libs.plugins.ksp) apply false
}
