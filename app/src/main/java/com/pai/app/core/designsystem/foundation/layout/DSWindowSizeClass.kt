// ============================================================================
// DSWindowSizeClass.kt
// 窗口尺寸类 - 基于 androidx.window 的 WindowSizeClass
// M3 卓越线补齐：响应式布局支持（Compact/Medium/Expanded 三档）
// ============================================================================

package com.pai.app.core.designsystem.foundation.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DS 窗口尺寸类
 *
 * 三档宽度 × 三档高度 = 9 种组合，业务代码根据 widthSizeClass 决定布局策略。
 *
 * | WidthSizeClass | 断点 | 设备示例 | 推荐导航 |
 * |---|---|---|---|
 * | Compact | < 600dp | 手机竖屏 | NavigationBar（底部导航）|
 * | Medium | 600~840dp | 手机横屏 / 小平板 | NavigationRail（侧边导航）|
 * | Expanded | > 840dp | 平板 / 折叠屏 / 桌面 | NavigationRail + PermanentDrawer |
 *
 * | HeightSizeClass | 断点 | 场景 |
 * |---|---|---|
 * | Compact | < 480dp | 横屏模式 / 折叠屏外屏 |
 * | Medium | 480~900dp | 手机竖屏 |
 * | Expanded | > 900dp | 平板竖屏 |
 */
@Stable
enum class DSWidthSizeClass {
    Compact,
    Medium,
    Expanded
}

@Stable
enum class DSHeightSizeClass {
    Compact,
    Medium,
    Expanded
}

/**
 * 当前窗口尺寸类信息
 */
@Stable
data class DSWindowSizeClass(
    val widthSizeClass: DSWidthSizeClass,
    val heightSizeClass: DSHeightSizeClass,
    val widthDp: Dp,
    val heightDp: Dp
) {
    /** 是否为手机竖屏（Compact width）*/
    val isCompact: Boolean get() = widthSizeClass == DSWidthSizeClass.Compact

    /** 是否为平板/折叠屏（Medium 或 Expanded）*/
    val isTablet: Boolean get() = widthSizeClass != DSWidthSizeClass.Compact

    /** 是否为大屏（Expanded）*/
    val isExpanded: Boolean get() = widthSizeClass == DSWidthSizeClass.Expanded

    /** 是否为横屏（高度小于宽度，常见于手机横屏模式）*/
    val isLandscape: Boolean get() = heightDp < widthDp

    /**
     * 根据窗口尺寸类选择不同的值
     *
     * 使用示例：
     * ```kotlin
     * val columns = windowSizeClass.select(
     *     compact = 1,    // 手机竖屏：单列
     *     medium = 2,     // 小平板：双列
     *     expanded = 3    // 大屏：三列
     * )
     * ```
     */
    fun <T> select(
        compact: T,
        medium: T,
        expanded: T
    ): T = when (widthSizeClass) {
        DSWidthSizeClass.Compact -> compact
        DSWidthSizeClass.Medium -> medium
        DSWidthSizeClass.Expanded -> expanded
    }

    /** 推荐的网格列数 */
    val recommendedGridColumns: Int
        get() = select(compact = 1, medium = 2, expanded = 3)

    /** 推荐的内容最大宽度（避免大屏一行过长难读）*/
    val recommendedContentMaxWidth: Dp
        get() = select(
            compact = Dp.Infinity,
            medium = 600.dp,
            expanded = 840.dp
        )

    /** 推荐导航组件：true = BottomBar, false = NavigationRail */
    val useBottomBar: Boolean
        get() = widthSizeClass == DSWidthSizeClass.Compact
}

/**
 * 当前窗口尺寸类 CompositionLocal
 *
 * 由 [rememberDSWindowSizeClass] 计算并通过 [provideDSWindowSizeClass] 注入。
 */
val LocalDSWindowSizeClass = compositionLocalOf {
    DSWindowSizeClass(
        widthSizeClass = DSWidthSizeClass.Compact,
        heightSizeClass = DSHeightSizeClass.Medium,
        widthDp = 360.dp,
        heightDp = 640.dp
    )
}

/**
 * 计算当前窗口尺寸类
 *
 * 基于 [LocalConfiguration] 的 screenWidthDp / screenHeightDp 计算尺寸类。
 * 在 Activity 配置变更（旋转、折叠）时会自动 recompose。
 *
 * 使用示例：
 * ```kotlin
 * val windowSizeClass = rememberDSWindowSizeClass()
 * if (windowSizeClass.useBottomBar) {
 *     DSBottomBar(...)
 * } else {
 *     DSNavigationRail(...)
 * }
 * ```
 */
@Composable
@ReadOnlyComposable
fun rememberDSWindowSizeClass(): DSWindowSizeClass {
    val configuration = LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.dp
    val heightDp = configuration.screenHeightDp.dp

    val widthSizeClass = when {
        widthDp < 600.dp -> DSWidthSizeClass.Compact
        widthDp < 840.dp -> DSWidthSizeClass.Medium
        else -> DSWidthSizeClass.Expanded
    }
    val heightSizeClass = when {
        heightDp < 480.dp -> DSHeightSizeClass.Compact
        heightDp < 900.dp -> DSHeightSizeClass.Medium
        else -> DSHeightSizeClass.Expanded
    }

    return DSWindowSizeClass(
        widthSizeClass = widthSizeClass,
        heightSizeClass = heightSizeClass,
        widthDp = widthDp,
        heightDp = heightDp
    )
}

/**
 * 提供窗口尺寸类给子组件
 *
 * 在 Activity/Fragment 顶层调用一次，子树通过 [LocalDSWindowSizeClass] 读取。
 *
 * ```kotlin
 * provideDSWindowSizeClass {
 *     AppContent()
 * }
 * ```
 */
@Composable
fun provideDSWindowSizeClass(
    content: @Composable () -> Unit
) {
    val windowSizeClass = rememberDSWindowSizeClass()
    CompositionLocalProvider(LocalDSWindowSizeClass provides windowSizeClass) {
        content()
    }
}

/**
 * 响应式布局容器
 *
 * 根据 [DSWindowSizeClass] 自动切换布局方向：
 * - Compact width: Column（垂直堆叠，手机竖屏）
 * - Medium/Expanded: Row（水平排列，平板/桌面）
 *
 * 使用示例：
 * ```kotlin
 * DSResponsiveLayout(
 *     modifier = Modifier.fillMaxWidth(),
 *     horizontalArrangement = Arrangement.spacedBy(16.dp),
 *     verticalArrangement = Arrangement.spacedBy(16.dp)
 * ) {
 *     // 子组件
 *     LeftPane()
 *     RightPane()
 * }
 * ```
 *
 * @param modifier 修饰符
 * @param horizontalArrangement 水平排列方式（Medium/Expanded 时生效）
 * @param verticalArrangement 垂直排列方式（Compact 时生效）
 * @param content 子内容
 */
@Composable
fun DSResponsiveLayout(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(DSTokens.Spacing.md),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(DSTokens.Spacing.md),
    content: @Composable () -> Unit
) {
    val windowSizeClass = rememberDSWindowSizeClass()
    if (windowSizeClass.isCompact) {
        Column(
            modifier = modifier,
            verticalArrangement = verticalArrangement
        ) {
            content()
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = horizontalArrangement
        ) {
            content()
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "WindowSizeClass - Compact (Phone Portrait)", widthDp = 360, heightDp = 640)
@Composable
private fun DSWindowSizeClassCompactPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            val wsc = rememberDSWindowSizeClass()
            Text("Width: ${wsc.widthSizeClass}", style = MaterialTheme.typography.bodyLarge)
            Text("Height: ${wsc.heightSizeClass}", style = MaterialTheme.typography.bodyLarge)
            Text("isCompact: ${wsc.isCompact}", style = MaterialTheme.typography.bodyMedium)
            Text("useBottomBar: ${wsc.useBottomBar}", style = MaterialTheme.typography.bodyMedium)
            Text("Grid Columns: ${wsc.recommendedGridColumns}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true, name = "WindowSizeClass - Medium (Small Tablet)", widthDp = 720, heightDp = 1024)
@Composable
private fun DSWindowSizeClassMediumPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            val wsc = rememberDSWindowSizeClass()
            Text("Width: ${wsc.widthSizeClass}", style = MaterialTheme.typography.bodyLarge)
            Text("isTablet: ${wsc.isTablet}", style = MaterialTheme.typography.bodyMedium)
            Text("useBottomBar: ${wsc.useBottomBar}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true, name = "WindowSizeClass - Expanded (Tablet)", widthDp = 1200, heightDp = 900)
@Composable
private fun DSWindowSizeClassExpandedPreview() {
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            val wsc = rememberDSWindowSizeClass()
            Text("Width: ${wsc.widthSizeClass}", style = MaterialTheme.typography.bodyLarge)
            Text("isExpanded: ${wsc.isExpanded}", style = MaterialTheme.typography.bodyMedium)
            Text("Recommended Max Width: ${wsc.recommendedContentMaxWidth}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true, name = "ResponsiveLayout - Compact", widthDp = 360)
@Composable
private fun DSResponsiveLayoutCompactPreview() {
    MaterialTheme {
        DSResponsiveLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Single Column", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
