package com.pai.app.buildlogic.convention

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * Convention plugin for the Android Application module.
 *
 * Applies `com.android.application` + `org.jetbrains.kotlin.plugin.compose` and
 * configures the [ApplicationExtension] with the project's shared Android config:
 *  - namespace / applicationId / SDK versions (compileSdk=36, minSdk=24, targetSdk=36)
 *  - versionCode / versionName / HiltTestRunner
 *  - buildTypes: debug (jsonplaceholder BASE_URL) + release (minify + shrinkResources
 *    + proguardFiles + api.example.com BASE_URL)
 *  - Java 17 source/target compatibility
 *  - buildFeatures: compose + buildConfig
 *  - testOptions: includeAndroidResources + returnDefaultValues (Robolectric)
 *
 * Plugin id: `zai.android.application`.
 *
 * App-specific overrides (e.g. namespace) can still be set in the consuming
 * module's `android { }` block.
 */
class AndroidApplicationConvention : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.android.application")
                apply("org.jetbrains.kotlin.plugin.compose")
            }

            extensions.configure<ApplicationExtension> {
                namespace = "com.pai.app"
                compileSdk = 36

                defaultConfig {
                    applicationId = "com.pai.app"
                    minSdk = 24
                    targetSdk = 36
                    versionCode = 1
                    versionName = "1.0.0"
                    testInstrumentationRunner = "com.pai.app.HiltTestRunner"
                }

                buildTypes {
                    debug {
                        buildConfigField(
                            "String",
                            "BASE_URL",
                            "\"https://jsonplaceholder.typicode.com/\"",
                        )
                    }
                    release {
                        isMinifyEnabled = true
                        isShrinkResources = true
                        buildConfigField(
                            "String",
                            "BASE_URL",
                            "\"https://api.example.com/\"",
                        )
                        proguardFiles(
                            getDefaultProguardFile("proguard-android-optimize.txt"),
                            "proguard-rules.pro",
                        )
                    }
                }

                compileOptions {
                    sourceCompatibility = JavaVersion.VERSION_17
                    targetCompatibility = JavaVersion.VERSION_17
                }

                buildFeatures {
                    compose = true
                    buildConfig = true
                }

                testOptions {
                    unitTests {
                        // Robolectric 需要访问 Android resources
                        isIncludeAndroidResources = true
                        // 未 mock 的 Android API 返回默认值（避免 NPE）
                        isReturnDefaultValues = true
                    }
                }
            }
        }
    }
}
