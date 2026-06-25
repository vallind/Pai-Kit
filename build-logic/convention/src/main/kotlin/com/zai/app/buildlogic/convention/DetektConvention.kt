package com.zai.app.buildlogic.convention

import io.gitlab.arturbosch.detekt.Detekt
import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.withType

/**
 * Convention plugin for Detekt static analysis.
 *
 * Applies `io.gitlab.arturbosch.detekt` and configures the extension to:
 *  - use the root `detekt.yml` config
 *  - buildUponDefaultConfig = true
 *  - parallel = true
 *  - allRules = false, autoCorrect = false
 *  - ignoreFailures = false (CI 发现 issue 即失败)
 *
 * Configures Detekt tasks to emit HTML + XML reports (txt/sarif 关闭) with
 * JVM target 17.
 *
 * Plugin id: `zai.detekt`.
 */
class DetektConvention : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("io.gitlab.arturbosch.detekt")

            val detektConfigFile = rootProject.file("detekt.yml")

            extensions.configure<DetektExtension> {
                config.setFrom(detektConfigFile)
                buildUponDefaultConfig = true
                parallel = true
                allRules = false
                autoCorrect = false
                ignoreFailures = false // CI 中发现 issue 即失败
            }

            tasks.withType<Detekt>().configureEach {
                reports {
                    html.required.set(true)
                    xml.required.set(true)
                    txt.required.set(false)
                    sarif.required.set(false)
                }
                jvmTarget = "17"
            }
        }
    }
}
