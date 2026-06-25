package com.pai.app.buildlogic.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Convention plugin for Hilt dependency injection.
 *
 * Applies `com.google.dagger.hilt.android` + `com.google.devtools.ksp` and
 * adds the Hilt runtime + KSP compiler + Hilt Navigation Compose dependencies.
 *
 * Plugin id: `zai.hilt`.
 *
 * 注意：KSP 在本插件中一并 apply，因此依赖 Hilt 的其他 convention
 * （如 KotlinTestConvention 用 kspAndroidTest）可在其后使用。
 */
class HiltConvention : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.dagger.hilt.android")
                apply("com.google.devtools.ksp")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("implementation", libs.findLibrary("hilt-android").get())
                add("ksp", libs.findLibrary("hilt-compiler").get())
                add("implementation", libs.findLibrary("hilt-navigation-compose").get())
            }
        }
    }
}
