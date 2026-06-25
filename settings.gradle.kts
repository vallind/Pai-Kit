// ============================================================================
// settings.gradle.kts - 单模块项目
// ============================================================================

pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }  // Chucker 等 jitpack 依赖
    }
}

// 引入 build-logic 复合构建：注册 pai.android.application / pai.hilt /
// pai.compose / pai.detekt / pai.ktlint / pai.kotlin.test / pai.kover /
// pai.android.library 等 convention plugin id。
includeBuild("build-logic")

rootProject.name = "ZaiDesignSystemSingle"
include(":app")
