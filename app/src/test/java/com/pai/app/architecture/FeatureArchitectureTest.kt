// ============================================================================
// FeatureArchitectureTest.kt
// Konsist 架构测试 - feature 层隔离规则（H7 扩展，决策 11）
// 补齐 DesignSystemArchitectureTest 未覆盖的 feature 层架构红线
// ============================================================================

package com.pai.app.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.KoModifier
import com.lemonappdev.konsist.api.verify.assert
import org.junit.Test

/**
 * Feature 层架构测试
 *
 * 补齐 H7 中 DesignSystemArchitectureTest 未覆盖的 3 条 feature 层红线
 * （docs/rules/02-package-isolation.md 已规定但无测试守护）：
 *
 * 1. **Feature 子包隔离**：`feature.<X>.*` 不得 import `feature.<Y>.*`
 *    （X != Y）。各 feature 子包之间零跨包引用，强制通过父级 Composable / 共享 core 通信。
 * 2. **Layer isolation**：`feature.*` 不得直接 import `retrofit2.*` 或 `androidx.room.*`，
 *    网络与数据库访问必须经 `core.data` Repository 中转（决策 8）。
 * 3. **ViewModel 继承**：`feature.*` 中所有以 `ViewModel` 结尾的类必须继承
 *    `com.pai.app.core.base.BaseViewModel`，统一获得导航 / 登录态 / 错误处理能力。
 * 4. **ViewModel 可见性**：`feature.*` ViewModel 类必须为 `internal`，避免泄漏到模块外
 *    （L6：Hilt 允许 internal + @HiltViewModel 同模块可见）。
 *
 * Konsist 0.17.3 API：使用 `Konsist.scopeFromPackage("..")` + `.files` / `.classes`
 * + `.assert { ... }`。若 API 在更高版本变化，可改用 `klass.hasModifier(KoModifier.INTERNAL)`
 * 等价形式。
 */
class FeatureArchitectureTest {

    // ------------------------------------------------------------------------
    // 红线 1：Feature 子包隔离
    // ------------------------------------------------------------------------

    /**
     * 各 feature 子包之间零跨包导入
     *
     * 遍历每个 feature 子包（auth/user/home/settings/gallery），
     * 断言该子包内任何文件都不 import 其他 feature 子包的代码。
     *
     * `gallery.pages` 属于 gallery 子包的一部分，允许互相引用；
     * 但 `gallery.*` 不得 import `auth.*` / `user.*` 等。
     */
    @Test
    fun `feature 子包之间零跨包导入`() {
        val featureSubs = listOf("auth", "user", "home", "settings", "gallery")

        featureSubs.forEach { current ->
            val otherSubs = featureSubs.filter { it != current }
            Konsist
                .scopeFromPackage("com.pai.app.feature.$current..")
                .files
                .assert { file ->
                    file.imports.none { imp ->
                        val fqn = imp.fullyQualifiedName ?: return@none false
                        // 不允许 import 同级 feature.<other>.*
                        otherSubs.any { other -> fqn.startsWith("com.pai.app.feature.$other.") }
                    }
                }
        }
    }

    // ------------------------------------------------------------------------
    // 红线 2：Layer isolation（feature 不直接 import retrofit2 / androidx.room）
    // ------------------------------------------------------------------------

    /**
     * feature 不得直接 import retrofit2 或 androidx room
     *
     * 网络与数据库访问必须经 `core.data` Repository 中转（决策 8：Repository
     * 是数据出口，不向外暴露 Entity / DTO）。
     *
     * 该规则 detekt.yml ForbiddenImport 目前仅覆盖 M3 组件，
     * retrofit2 / androidx.room 由本 Konsist 测试守护。
     */
    @Test
    fun `feature 不得直接 import retrofit2 或 androidx room`() {
        Konsist
            .scopeFromPackage("com.pai.app.feature..")
            .files
            .assert { file ->
                file.imports.none { imp ->
                    val fqn = imp.fullyQualifiedName ?: return@none false
                    fqn.startsWith("retrofit2.") || fqn.startsWith("androidx.room.")
                }
            }
    }

    // ------------------------------------------------------------------------
    // 红线 3：ViewModel 继承 BaseViewModel
    // ------------------------------------------------------------------------

    /**
     * feature 中所有 ViewModel 类必须继承 BaseViewModel
     *
     * BaseViewModel 提供统一的：
     * - 类型安全跳转（navigate / navigateAndCloseCurrent / navigateBack / navigateBackTo）
     * - 登录态访问（isLoggedIn）
     * - 退出登录模板（logout 委托 UserState）
     *
     * 所有以 `ViewModel` 结尾的 feature 类必须继承之，避免业务 ViewModel 自行
     * 重新实现导航 / 登录态。
     */
    @Test
    fun `feature 中所有 ViewModel 类必须继承 BaseViewModel`() {
        Konsist
            .scopeFromPackage("com.pai.app.feature..")
            .classes
            .filter { klass -> klass.name.endsWith("ViewModel") }
            .assert { klass ->
                // Konsist 0.17.3：hasSuperclass 检查父类与实现的接口的 FQN
                klass.hasSuperclass("com.pai.app.core.base.BaseViewModel")
            }
    }

    // ------------------------------------------------------------------------
    // 红线 4：ViewModel 可见性必须为 internal
    // ------------------------------------------------------------------------

    /**
     * feature 中所有 ViewModel 类必须为 internal 可见性
     *
     * L6：`@HiltViewModel | internal 可加 | 同模块内 Hilt 可见`。
     * Feature ViewModel 不应被模块外引用，强制 `internal` 防止意外泄漏。
     *
     * 注：Hilt 允许 `internal @HiltViewModel class`，同模块内的 Hilt 编译器
     * 生成的代码可见，不需要 public。
     *
     * Konsist 0.17.3：用 `hasModifier(KoModifier.INTERNAL)` 检查 internal 关键字
     * （跨版本稳定 API）；等价于 `klass.hasInternalModifier` 扩展属性。
     */
    @Test
    fun `feature 中所有 ViewModel 类必须为 internal 可见性`() {
        Konsist
            .scopeFromPackage("com.pai.app.feature..")
            .classes
            .filter { klass -> klass.name.endsWith("ViewModel") }
            .assert { klass -> klass.hasModifier(KoModifier.INTERNAL) }
    }

    // ------------------------------------------------------------------------
    // 红线 5：core.domain KMP-ready（无 Android / Retrofit / Room / network / database 依赖）
    // ------------------------------------------------------------------------

    /**
     * core.domain 不得 import android / androidx / retrofit2 / core.network / core.database
     *
     * 决策 P1-1：domain 层（Repository 接口 + domain model）必须 KMP-ready，
     * 未来 KMP 迁移时原样移到 `shared/commonMain`。本规则守护 domain 层不引入
     * Android / Retrofit / Room / 项目内 network / database 包的任何 import。
     *
     * 允许的 import：
     * - `kotlin.*` / `kotlinx.*`（纯 Kotlin）
     * - `com.pai.app.core.base.*`（[com.pai.app.core.base.ApiResult] / [com.pai.app.core.base.AppException]
     *   等纯 Kotlin sealed class，是 domain 接口的返回类型载体）
     * - `com.pai.app.core.domain.*` / `com.pai.app.core.domain.model.*`（同层内部引用）
     *
     * 注：[com.pai.app.core.base.AppException.from] 内部用了 retrofit2 / java.io，
     * 但那是 base 层实现细节；domain 层只 import 类型签名（[com.pai.app.core.base.ApiResult] /
     * [com.pai.app.core.base.AppException]），不感知这些依赖。P3-1 KMP 迁移时
     * 会进一步把 core.base 拆为 commonMain + androidMain，本规则保持不变。
     *
     * Konsist 0.17.3：`Konsist.scopeFromPackage("...domain..").files.assert { ... }`
     * 遍历每个文件的 imports，断言无禁用前缀。
     */
    @Test
    fun `core domain 不依赖 Android 或 Retrofit 或 Room 或 network 或 database`() {
        Konsist
            .scopeFromPackage("com.pai.app.core.domain..")
            .files
            .assert { file ->
                file.imports.none { imp ->
                    val fqn = imp.fullyQualifiedName ?: return@none false
                    fqn.startsWith("android.") ||
                        fqn.startsWith("androidx.") ||
                        fqn.startsWith("retrofit2.") ||
                        fqn.startsWith("com.pai.app.core.network.") ||
                        fqn.startsWith("com.pai.app.core.database.")
                }
            }
    }
}
