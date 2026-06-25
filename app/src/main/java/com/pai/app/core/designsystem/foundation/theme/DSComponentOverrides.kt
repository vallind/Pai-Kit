// ============================================================================
// DSComponentOverrides.kt
// 组件级主题覆盖机制
// M3 卓越线补齐：实现「某个按钮单独改样式」「某个卡片单独改色」的细粒度主题控制
// ============================================================================

package com.pai.app.core.designsystem.foundation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp

/**
 * 组件级主题覆盖 - 按组件类型分组的局部覆盖配置
 *
 * 用法：在需要局部改色的子树外层包一层 [DSComponentThemeOverride]，
 * 子树内通过 [LocalDSComponentOverrides] 读取并应用覆盖。
 *
 * 使用示例：
 * ```kotlin
 * // 整个 HeroCard 子树用 tertiary 而非 primary 作为主色
 * DSComponentThemeOverride(
 *     button = DSButtonOverride(containerColor = MaterialTheme.colorScheme.tertiary)
 * ) {
 *     HeroCard(...)
 * }
 *
 * // 单个按钮去掉阴影
 * DSButton(
 *     text = "无阴影",
 *     modifier = Modifier
 *         .withButtonOverride(DSButtonOverride(elevation = 0.dp))
 * )
 * ```
 *
 * 设计原则：
 * - 默认值 = null：表示不覆盖，走 M3 默认 / 当前主题
 * - 覆盖是叠加的：父级 + 子级覆盖会合并（子级优先）
 * - 不破坏 @Composable 签名：override 通过 CompositionLocal 传递
 */
@Stable
data class DSComponentOverrides(
    val button: DSButtonOverride? = null,
    val card: DSCardOverride? = null,
    val topBar: DSTopBarOverride? = null,
    val bottomBar: DSBottomBarOverride? = null,
    val textField: DSTextFieldOverride? = null,
    val fab: DSSFabOverride? = null
) {
    /**
     * 合并其他覆盖配置（other 优先）
     */
    fun merge(other: DSComponentOverrides?): DSComponentOverrides {
        if (other == null) return this
        return DSComponentOverrides(
            button = other.button ?: this.button,
            card = other.card ?: this.card,
            topBar = other.topBar ?: this.topBar,
            bottomBar = other.bottomBar ?: this.bottomBar,
            textField = other.textField ?: this.textField,
            fab = other.fab ?: this.fab
        )
    }

    companion object {
        val Empty = DSComponentOverrides()
    }
}

/**
 * Button 组件覆盖
 */
@Stable
data class DSButtonOverride(
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val disabledContainerColor: Color? = null,
    val disabledContentColor: Color? = null,
    val elevation: Dp? = null,
    val cornerRadius: Dp? = null,
    val minWidth: Dp? = null,
    val height: Dp? = null
)

/**
 * Card 组件覆盖
 */
@Stable
data class DSCardOverride(
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val elevation: Dp? = null,
    val cornerRadius: Dp? = null,
    val borderColor: Color? = null,
    val borderWidth: Dp? = null
)

/**
 * TopBar 组件覆盖
 */
@Stable
data class DSTopBarOverride(
    val containerColor: Color? = null,
    val titleContentColor: Color? = null,
    val navigationIconColor: Color? = null,
    val actionIconColor: Color? = null,
    val height: Dp? = null
)

/**
 * BottomBar 组件覆盖
 */
@Stable
data class DSBottomBarOverride(
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val selectedItemColor: Color? = null,
    val unselectedItemColor: Color? = null,
    val height: Dp? = null
)

/**
 * TextField 组件覆盖
 */
@Stable
data class DSTextFieldOverride(
    val containerColor: Color? = null,
    val textColor: Color? = null,
    val focusedBorderColor: Color? = null,
    val unfocusedBorderColor: Color? = null,
    val errorBorderColor: Color? = null,
    val cornerRadius: Dp? = null
)

/**
 * FAB 组件覆盖
 */
@Stable
data class DSSFabOverride(
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val size: Dp? = null,
    val shape: Shape? = null,
    val elevation: Dp? = null
)

/**
 * 组件级覆盖的 CompositionLocal
 *
 * 默认 = [DSComponentOverrides.Empty]，表示不覆盖。
 */
val LocalDSComponentOverrides = staticCompositionLocalOf { DSComponentOverrides.Empty }

/**
 * 应用组件级主题覆盖
 *
 * 在子树外层包裹，子树内通过 [LocalDSComponentOverrides] 读取覆盖配置。
 *
 * @param overrides 覆盖配置（与父级合并）
 * @param content 子内容
 */
@Composable
fun DSComponentThemeOverride(
    overrides: DSComponentOverrides,
    content: @Composable () -> Unit
) {
    val parent = LocalDSComponentOverrides.current
    val merged = remember(overrides, parent) { parent.merge(overrides) }
    CompositionLocalProvider(LocalDSComponentOverrides provides merged) {
        content()
    }
}

/**
 * 应用单个 Button 覆盖（便捷方法）
 *
 * 等价于：
 * ```kotlin
 * DSComponentThemeOverride(
 *     overrides = DSComponentOverrides(button = override)
 * ) { ... }
 * ```
 */
@Composable
fun DSButtonThemeOverride(
    override: DSButtonOverride,
    content: @Composable () -> Unit
) {
    DSComponentThemeOverride(
        overrides = DSComponentOverrides(button = override),
        content = content
    )
}

/**
 * 应用单个 Card 覆盖
 */
@Composable
fun DSCardThemeOverride(
    override: DSCardOverride,
    content: @Composable () -> Unit
) {
    DSComponentThemeOverride(
        overrides = DSComponentOverrides(card = override),
        content = content
    )
}

/**
 * 应用单个 TopBar 覆盖
 */
@Composable
fun DSTopBarThemeOverride(
    override: DSTopBarOverride,
    content: @Composable () -> Unit
) {
    DSComponentThemeOverride(
        overrides = DSComponentOverrides(topBar = override),
        content = content
    )
}

/**
 * 读取当前 Button 覆盖（如果存在）
 *
 * 在 DSButton 内部使用：
 * ```kotlin
 * val override = currentButtonOverride()
 * val containerColor = override?.containerColor ?: MaterialTheme.colorScheme.primary
 * ```
 */
@Composable
@ReadOnlyComposable
fun currentButtonOverride(): DSButtonOverride? {
    return LocalDSComponentOverrides.current.button
}

/**
 * 读取当前 Card 覆盖
 */
@Composable
@ReadOnlyComposable
fun currentCardOverride(): DSCardOverride? {
    return LocalDSComponentOverrides.current.card
}

/**
 * 读取当前 TopBar 覆盖
 */
@Composable
@ReadOnlyComposable
fun currentTopBarOverride(): DSTopBarOverride? {
    return LocalDSComponentOverrides.current.topBar
}

/**
 * 读取当前 BottomBar 覆盖
 */
@Composable
@ReadOnlyComposable
fun currentBottomBarOverride(): DSBottomBarOverride? {
    return LocalDSComponentOverrides.current.bottomBar
}

/**
 * 读取当前 TextField 覆盖
 */
@Composable
@ReadOnlyComposable
fun currentTextFieldOverride(): DSTextFieldOverride? {
    return LocalDSComponentOverrides.current.textField
}

/**
 * 读取当前 FAB 覆盖
 */
@Composable
@ReadOnlyComposable
fun currentFabOverride(): DSSFabOverride? {
    return LocalDSComponentOverrides.current.fab
}

// 提供 remember 函数（避免每次重组创建新实例）
@Composable
private fun <T> remember(key1: Any?, key2: Any?, calculation: () -> T): T =
    androidx.compose.runtime.remember(key1, key2, calculation)
