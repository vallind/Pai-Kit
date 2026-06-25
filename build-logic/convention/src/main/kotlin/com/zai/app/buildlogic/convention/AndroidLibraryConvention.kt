package com.zai.app.buildlogic.convention

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention plugin for future Android Library modules.
 *
 * Applies `com.android.library` + `org.jetbrains.kotlin.plugin.compose` and
 * configures the [LibraryExtension] with the project's shared Android config:
 *  - compileSdk=36, minSdk=24
 *  - Java 17 source/target compatibility
 *  - buildFeatures.compose = true
 *  - testOptions: includeAndroidResources + returnDefaultValues (Robolectric)
 *
 * Plugin id: `zai.android.library`.
 *
 * 当前单模块脚手架暂无 :library 模块，但提前 ship 此 plugin，未来拆出 core/lib
 * 模块时只需 `id("zai.android.library")` 一行即可复用所有通用配置。
 *
 * 注意：Compose 相关依赖由 `zai.compose` convention 提供，本插件仅开启
 * `buildFeatures.compose`。建议使用顺序：`id("zai.android.library")` →
 * `id("zai.compose")` → `id("zai.hilt")` 等。
 */
class AndroidLibraryConvention : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.library")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<LibraryExtension> {
                compileSdk = 36

                defaultConfig {
                    minSdk = 24
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                buildFeatures {
                    compose = true
                }

                testOptions {
                    unitTests {
                        isIncludeAndroidResources = true
                        isReturnDefaultValues = true
                    }
                }
            }
        }
    }
}
