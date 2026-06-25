// ============================================================================
// Theme.kt
// 主题入口：DSDesignTheme
// 集成 Material 3 主题、Light/Dark/AMOLED 模式、动态颜色（Android 12+）、品牌色板、字号缩放
// 提供扩展语义色与 Design Token 的 CompositionLocal 注入
// ============================================================================

package com.pai.app.core.designsystem.foundation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.pai.app.core.designsystem.foundation.tokens.DSBrandColor
import com.pai.app.core.designsystem.foundation.tokens.DSFontSizeScale

/**
 * 字号缩放 CompositionLocal
 *
 * 由 [DSDesignTheme] 注入，DSText 等文本组件读取后通过
 * `style.fontSize * fontSizeScale.multiplier` 应用全局字号缩放。
 *
 * 默认值 [DSFontSizeScale.Normal]（1.0x，不缩放）。
 */
val LocalFontSizeScale = staticCompositionLocalOf { DSFontSizeScale.Normal }

/**
 * DSDesignTheme - 设计系统统一主题入口
 *
 * 使用方式：
 * ```kotlin
 * DSDesignTheme(
 *     darkTheme = isSystemInDarkTheme(),
 *     dynamicColor = false,
 *     brandColor = DSBrandColor.Indigo,
 *     fontSizeScale = DSFontSizeScale.Normal,
 *     amoled = false,
 *     highContrast = false,
 * ) {
 *     // 你的 Composable
 *     MainScreen()
 * }
 * ```
 *
 * 默认行为：
 * 1. 自动跟随系统暗色模式
 * 2. 默认关闭动态颜色，使用 [brandColor] 动态生成 ColorScheme
 * 3. 启用动态颜色（`dynamicColor = true`）时忽略 [brandColor]
 * 4. `amoled = true` 时 Dark 模式的 background / surface 使用 Color.Black（省电）
 * 5. [fontSizeScale] 通过 [LocalFontSizeScale] 注入，影响 DSText 字号
 * 6. 自动处理状态栏图标颜色（亮色主题深色图标，深色主题亮色图标）
 * 7. `highContrast = true` 启用高对比度模式（a11y），强化色对比至 WCAG AAA
 *
 * @param darkTheme 是否使用深色主题
 * @param dynamicColor 是否启用动态颜色（覆盖 [brandColor]）
 * @param brandColor 品牌色板，仅当 [dynamicColor] = false 时生效
 * @param fontSizeScale 字号缩放等级，通过 [LocalFontSizeScale] 注入
 * @param amoled 是否为 AMOLED 纯黑模式（仅 Dark 主题生效）
 * @param highContrast 是否启用高对比度模式（无障碍，与 darkTheme 正交）
 * @param content 子内容
 */
@Composable
fun DSDesignTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    brandColor: DSBrandColor = DSBrandColor.Indigo,
    fontSizeScale: DSFontSizeScale = DSFontSizeScale.Normal,
    amoled: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
) {
    // -----------------------------------------------------------------------
    // 1. 选择 ColorScheme
    //    优先级：highContrast > dynamicColor > brandColor
    //    高对比度优先于动态颜色，因为无障碍是硬性约束
    // -----------------------------------------------------------------------
    val context = LocalContext.current
    val colorScheme = when {
        // 高对比度模式（覆盖一切，无障碍硬约束）
        highContrast && darkTheme -> buildHighContrastDarkColorScheme(brandColor)
        highContrast -> buildHighContrastLightColorScheme(brandColor)
        // 动态颜色（仅 Android 12+ 支持，忽略 brandColor）
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        // 品牌色板动态生成（amoled 仅在 Dark 模式生效）
        darkTheme -> buildDarkColorScheme(brandColor, amoled = amoled)
        else -> buildLightColorScheme(brandColor)
    }

    // -----------------------------------------------------------------------
    // 2. 选择扩展语义色
    //
    // 注意：AMOLED 模式复用 DarkExtendedColors（不单独提供 AmoledExtendedColors）。
    // 当前行为是有意的：在纯黑 background 上，skeletonBase=BrandSlate800 / skeletonHighlight=BrandSlate700
    // 反而提供良好可见性；border=BrandSlate700 在 surface=BrandSlate900 上对比度尚可。
    // 若未来 AMOLED 调优需要更深的 border，可在此分支基于 `amoled` 标志位选择第三套 palette。
    // -----------------------------------------------------------------------
    val extendedColors = if (darkTheme) DarkExtendedColors else LightExtendedColors

    // -----------------------------------------------------------------------
    // 3. 同步状态栏样式（仅在 darkTheme/colorScheme 变化时执行；状态栏着色 API 在
    //    Android 15+ 已废弃，edge-to-edge 由系统强制启用，故 gating 在 SDK_INT < VANILLA_ICE_CREAM）
    // -----------------------------------------------------------------------
    val view = LocalView.current
    LaunchedEffect(darkTheme, colorScheme) {
        if (view.isInEditMode) return@LaunchedEffect
        val window = (view.context as? Activity)?.window ?: return@LaunchedEffect
        // 状态栏图标外观：亮色主题用深色图标，深色主题用浅色图标
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        // 状态栏背景色：Android 15 (API 35) 之前可手动设置；之后系统强制透明（edge-to-edge）
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM) {
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.surface.toArgb()
        }
        // 让内容延伸到状态栏下方（Android 15+ 此调用为 no-op，系统已强制 edge-to-edge）
        WindowCompat.setDecorFitsSystemWindows(window, false)
    }

    // -----------------------------------------------------------------------
    // 4. 注入扩展色 + 字号缩放 + 应用 MaterialTheme
    // -----------------------------------------------------------------------
    CompositionLocalProvider(
        LocalExtendedColors provides extendedColors,
        LocalFontSizeScale provides fontSizeScale,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = DSTypography,
            shapes = DSShapes,
            content = content
        )
    }
}

/**
 * 快捷访问扩展语义色
 * - 使用方式：val successColor = MaterialTheme.extendedColors.success
 */
internal val MaterialTheme.extendedColors: ExtendedColors
    @Composable
    @ReadOnlyComposable
    get() = LocalExtendedColors.current
