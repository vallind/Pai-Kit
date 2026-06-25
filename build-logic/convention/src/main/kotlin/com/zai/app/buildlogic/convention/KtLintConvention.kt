package com.zai.app.buildlogic.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jlleitschuh.gradle.ktlint.KtlintExtension

/**
 * Convention plugin for KtLint code style checks.
 *
 * Applies `org.jlleitschuh.gradle.ktlint` and configures the extension to:
 *  - debug = false
 *  - android = true（启用 Android 风格规则）
 *  - ignoreFailures = false（CI 发现违规即失败）
 *  - filter：排除 build/，包含 *.kt 与 *.kts
 *
 * 不显式指定 ktlint 版本：让 ktlint-gradle 插件 12.1.2 选择兼容默认版本。
 *
 * Plugin id: `zai.ktlint`.
 */
class KtLintConvention : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jlleitschuh.gradle.ktlint")

            extensions.configure<KtlintExtension> {
                debug.set(false)
                android.set(true) // 启用 Android 风格规则
                ignoreFailures.set(false)
                filter {
                    exclude("**/build/**")
                    include("**/*.kt")
                    include("**/*.kts")
                }
            }
        }
    }
}
