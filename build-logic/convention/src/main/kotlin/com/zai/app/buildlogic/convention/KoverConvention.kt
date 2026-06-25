package com.zai.app.buildlogic.convention

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Convention plugin for Kover code coverage.
 *
 * Applies `org.jetbrains.kotlinx.kover`。Kover 插件会自动注册
 * `koverXmlReport` / `koverHtmlReport` 等任务，CI 中执行
 * `./gradlew :app:koverXmlReport` 即可生成 XML 报告
 * （位于 app/build/reports/kover/）。
 *
 * 如需自定义阈值，业务方可在自身 build script 按 Kover 0.9.x DSL 扩展
 * （参见 https://kotlinlang.org/docs/kover-gradle-plugin/）。
 *
 * Plugin id: `zai.kover`.
 */
class KoverConvention : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlinx.kover")
        }
    }
}
