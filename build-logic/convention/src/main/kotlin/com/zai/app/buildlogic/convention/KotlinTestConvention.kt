package com.zai.app.buildlogic.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Convention plugin for Kotlin / Android 测试依赖。
 *
 * 单元测试 (testImplementation)：
 *  - JUnit4 / MockK / Turbine / kotlinx-coroutines-test
 *  - OkHttp MockWebServer / Robolectric / Konsist
 *  - AndroidX Test Core（Robolectric 中 ApplicationProvider 用）
 *
 * Instrumented 测试 (androidTestImplementation)：
 *  - AndroidX JUnit / Espresso / MockK-Android / Turbine / coroutines-test
 *  - Room testing / Hilt Android Testing
 *  - Compose UI test junit4（含 Compose BOM platform）
 *
 * KSP：
 *  - kspAndroidTest(hilt-compiler) —— Hilt 测试组件生成
 *
 * Debug：
 *  - debugImplementation(compose-ui-test-manifest) —— 启动 HiltComponentActivity
 *
 * Plugin id: `zai.kotlin.test`。
 *
 * 注意：`kspAndroidTest` 配置由 KSP plugin 注册，因此本插件应在 `zai.hilt`
 * 之后应用（HiltConvention 会 apply KSP）。
 */
class KotlinTestConvention : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                // --- Unit test (testImplementation) ---
                add("testImplementation", libs.findLibrary("junit").get())
                add("testImplementation", libs.findLibrary("mockk").get())
                add("testImplementation", libs.findLibrary("turbine").get())
                add("testImplementation", libs.findLibrary("kotlinx.coroutines.test").get())
                add("testImplementation", libs.findLibrary("okhttp.mockwebserver").get())
                add("testImplementation", libs.findLibrary("robolectric").get())
                add("testImplementation", libs.findLibrary("konsist.test").get())
                // Robolectric 单元测试中获取 Application Context 所需（ApplicationProvider）
                add("testImplementation", libs.findLibrary("androidx.test.core").get())

                // --- Instrumented test (androidTestImplementation) ---
                add("androidTestImplementation", libs.findLibrary("androidx.junit").get())
                add("androidTestImplementation", libs.findLibrary("androidx.espresso.core").get())
                add("androidTestImplementation", libs.findLibrary("mockk.android").get())
                add("androidTestImplementation", libs.findLibrary("turbine").get())
                add("androidTestImplementation", libs.findLibrary("kotlinx.coroutines.test").get())
                add("androidTestImplementation", libs.findLibrary("room.testing").get())
                add("androidTestImplementation", libs.findLibrary("hilt.android.testing").get())
                add("kspAndroidTest", libs.findLibrary("hilt.compiler").get())
                add("androidTestImplementation", platform(libs.findLibrary("androidx.compose.bom").get()))
                add("androidTestImplementation", libs.findLibrary("androidx.compose.ui.test.junit4").get())

                // --- Debug ---
                add("debugImplementation", libs.findLibrary("androidx.compose.ui.test.manifest").get())
            }
        }
    }
}
