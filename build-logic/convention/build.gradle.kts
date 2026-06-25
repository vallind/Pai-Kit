// ============================================================================
// build-logic/convention/build.gradle.kts
// 用 kotlin-dsl 插件构建 convention plugins。
// 通过 gradlePlugin {} 注册 9 个 convention plugin id，供根项目 / app 项目使用。
// ============================================================================

plugins {
    `kotlin-dsl`
}

group = "com.zai.app.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// convention plugin 运行时需要访问 AGP / Kotlin / KSP / Hilt / Detekt /
// KtLint / Kover 等 Gradle 插件的 API 与 DSL 类型。compose-compiler-gradle-plugin
// 用于让 AndroidApplicationConvention / ComposeConvention 通过
// pluginManager.apply("org.jetbrains.kotlin.plugin.compose") 应用 Compose 插件。
dependencies {
    implementation(libs.agp)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.compose.compiler.gradle.plugin)
    implementation(libs.ksp.gradle.plugin)
    implementation(libs.hilt.gradle.plugin)
    implementation(libs.detekt.gradle.plugin)
    implementation(libs.ktlint.gradle.plugin)
    implementation(libs.kover.gradle.plugin)
    implementation(libs.paparazzi.gradle.plugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "pai.android.application"
            implementationClass = "com.zai.app.buildlogic.convention.AndroidApplicationConvention"
        }
        register("androidLibrary") {
            id = "pai.android.library"
            implementationClass = "com.zai.app.buildlogic.convention.AndroidLibraryConvention"
        }
        register("hilt") {
            id = "pai.hilt"
            implementationClass = "com.zai.app.buildlogic.convention.HiltConvention"
        }
        register("compose") {
            id = "pai.compose"
            implementationClass = "com.zai.app.buildlogic.convention.ComposeConvention"
        }
        register("detekt") {
            id = "pai.detekt"
            implementationClass = "com.zai.app.buildlogic.convention.DetektConvention"
        }
        register("ktlint") {
            id = "pai.ktlint"
            implementationClass = "com.zai.app.buildlogic.convention.KtLintConvention"
        }
        register("kotlinTest") {
            id = "pai.kotlin.test"
            implementationClass = "com.zai.app.buildlogic.convention.KotlinTestConvention"
        }
        register("kover") {
            id = "pai.kover"
            implementationClass = "com.zai.app.buildlogic.convention.KoverConvention"
        }
        register("paparazzi") {
            id = "pai.paparazzi"
            implementationClass = "com.zai.app.buildlogic.convention.PaparazziConvention"
        }
    }
}
