// ============================================================================
// build-logic/settings.gradle.kts
// build-logic 是一个独立构建的 Gradle 复合构建（included build）。
// 它本身是一个 Kotlin Gradle Plugin 项目，用于生成 convention plugins。
// 通过根 settings.gradle.kts 的 includeBuild("build-logic") 引入。
// ============================================================================

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}

rootProject.name = "build-logic"

include(":convention")
