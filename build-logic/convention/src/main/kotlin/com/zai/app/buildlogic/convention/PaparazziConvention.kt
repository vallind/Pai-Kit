package com.pai.app.buildlogic.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

/**
 * Convention plugin for Paparazzi 截图测试（M3 卓越线补齐）
 *
 * 接入 app.cash.paparazzi —— 在 JVM 上无设备渲染 Compose UI 并对比基线图。
 * 用法：
 *   ./gradlew :app:recordPaparazziDebug     # 录制基线截图
 *   ./gradlew :app:verifyPaparazziDebug     # 对比基线，差异 → CI 失败
 *
 * Plugin id: `zai.paparazzi`（与 `zai.kotlin.test` 互补，专注视觉回归）。
 *
 * 应用方需自行在 dependencies 中 testImplementation(paparazzi) —— 本插件只注册插件本身。
 */
class PaparazziConvention : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")
            pluginManager.apply(libs.findPlugin("paparazzi").get().get().pluginId)
        }
    }
}
