package com.pai.app.buildlogic.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Convention plugin for Jetpack Compose.
 *
 * Applies `org.jetbrains.kotlin.plugin.compose` and adds the Compose BOM plus
 * the standard set of Compose UI / Foundation / Material3 / Material Icons
 * Extended dependencies, with `ui-tooling` as a debugImplementation.
 *
 * Plugin id: `zai.compose`.
 *
 * 注意：本插件依赖项目已应用 Android plugin（com.android.application 或
 * com.android.library），因为 `implementation` / `debugImplementation` 配置
 * 由 Android plugin 注册。建议使用顺序：`id("zai.android.application")` →
 * `id("zai.compose")`。
 */
class ComposeConvention : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("implementation", platform(libs.findLibrary("androidx.compose.bom").get()))
                add("implementation", libs.findLibrary("androidx.compose.ui").get())
                add("implementation", libs.findLibrary("androidx.compose.ui.graphics").get())
                add("implementation", libs.findLibrary("androidx.compose.ui.tooling.preview").get())
                add("implementation", libs.findLibrary("androidx.compose.foundation").get())
                add("implementation", libs.findLibrary("androidx.compose.material3").get())
                add("implementation", libs.findLibrary("androidx.compose.material.icons.extended").get())
                add("debugImplementation", libs.findLibrary("androidx.compose.ui.tooling").get())
            }
        }
    }
}
