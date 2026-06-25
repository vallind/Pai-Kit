// ============================================================================
// DesignSystemArchitectureTest.kt
// Konsist 架构测试 - 锁死 designsystem 5 层依赖方向 (方案 B 一级子域)
// CI 中自动运行，违反依赖方向则编译失败
// ============================================================================

package com.pai.app.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assert
import org.junit.Test

/**
 * Design System 架构测试 (方案 B：一级子域扁平结构)
 *
 * 5 层依赖方向（严格单向）：
 * foundation ← primitives ← patterns ← shell
 * foundation ← primitives ← patterns ← overlays
 * shell ↔ overlays 平级互不依赖
 *
 * 子域布局：
 * - foundation/ : tokens + theme + motion + a11y
 * - primitives/ : 全部基础原子组件（layout/visual/controls/containers/display 已扁平合并）
 * - patterns/   : 业务状态模式
 * - shell/      : 顶部/底部导航骨架
 * - overlays/   : 弹层 / 抽屉 / FAB / 搜索栏
 *
 * 四条红线：
 * 1. 禁止平级跨域调用：shell ↔ overlays 之间零引用
 * 2. 禁止底层依赖高层
 * 3. Shell 与 Patterns 隔离：patterns 不被 shell/overlays 依赖
 * 4. 业务层禁止越级：Feature 不得直接用 material3 原生组件（Detekt 拦截）
 *
 * Feature 层依赖规则（松绑）：
 * - 允许依赖 primitives/patterns/shell/overlays 四个子域
 * - 优先使用 Patterns
 * - 禁止在 designsystem 下定义新的业务组件
 */
class DesignSystemArchitectureTest {

    // --- 红线 1：禁止底层依赖高层 ---

    @Test
    fun `foundation should not depend on upper layers`() {
        Konsist
            .scopeFromPackage("com.pai.app.core.designsystem.foundation..")
            .files
            .assert { file ->
                file.imports.none { import ->
                    import.fullyQualifiedName?.contains("core.designsystem.primitives") == true ||
                    import.fullyQualifiedName?.contains("core.designsystem.patterns") == true ||
                    import.fullyQualifiedName?.contains("core.designsystem.shell") == true ||
                    import.fullyQualifiedName?.contains("core.designsystem.overlays") == true
                }
            }
    }

    @Test
    fun `primitives should not depend on patterns, shell, or overlays`() {
        Konsist
            .scopeFromPackage("com.pai.app.core.designsystem.primitives..")
            .files
            .assert { file ->
                file.imports.none { import ->
                    import.fullyQualifiedName?.contains("core.designsystem.patterns") == true ||
                    import.fullyQualifiedName?.contains("core.designsystem.shell") == true ||
                    import.fullyQualifiedName?.contains("core.designsystem.overlays") == true
                }
            }
    }

    // 注：原 `components should not depend on patterns, shell, or overlays` 测试已移除
    //     components 子域已扁平合并到 primitives/ 下，不再独立存在

    @Test
    fun `patterns should not depend on shell or overlays`() {
        Konsist
            .scopeFromPackage("com.pai.app.core.designsystem.patterns..")
            .files
            .assert { file ->
                file.imports.none { import ->
                    import.fullyQualifiedName?.contains("core.designsystem.shell") == true ||
                    import.fullyQualifiedName?.contains("core.designsystem.overlays") == true
                }
            }
    }

    // --- 红线 2：Shell ↔ Overlay 平级互不依赖 ---

    @Test
    fun `shell should not depend on overlays`() {
        Konsist
            .scopeFromPackage("com.pai.app.core.designsystem.shell..")
            .files
            .assert { file ->
                file.imports.none { import ->
                    import.fullyQualifiedName?.contains("core.designsystem.overlays") == true
                }
            }
    }

    @Test
    fun `overlays should not depend on shell`() {
        Konsist
            .scopeFromPackage("com.pai.app.core.designsystem.overlays..")
            .files
            .assert { file ->
                file.imports.none { import ->
                    import.fullyQualifiedName?.contains("core.designsystem.shell") == true
                }
            }
    }

    // --- 红线 3：designsystem 不染业务 ---

    @Test
    fun `designsystem should not depend on feature`() {
        Konsist
            .scopeFromPackage("com.pai.app.core.designsystem..")
            .files
            .assert { file ->
                file.imports.none { import ->
                    import.fullyQualifiedName?.contains("com.pai.app.feature") == true
                }
            }
    }

    @Test
    fun `designsystem should not depend on core appstate`() {
        Konsist
            .scopeFromPackage("com.pai.app.core.designsystem..")
            .files
            .assert { file ->
                file.imports.none { import ->
                    import.fullyQualifiedName?.contains("com.pai.app.core.appstate") == true
                }
            }
    }

    // --- 红线 4：patterns 不消费业务状态类 ---

    @Test
    fun `patterns should not import BaseNetWorkUiState or BaseNetWorkViewModel`() {
        Konsist
            .scopeFromPackage("com.pai.app.core.designsystem.patterns..")
            .files
            .assert { file ->
                file.imports.none { import ->
                    import.fullyQualifiedName?.contains("core.base.BaseNetWorkUiState") == true ||
                    import.fullyQualifiedName?.contains("core.base.BaseNetWorkViewModel") == true ||
                    import.fullyQualifiedName?.contains("core.base.BaseViewModel") == true
                }
            }
    }
}
