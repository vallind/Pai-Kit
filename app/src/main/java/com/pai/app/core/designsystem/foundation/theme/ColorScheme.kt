// ============================================================================
// ColorScheme.kt
// Material 3 ColorScheme（Light / Dark）+ 扩展语义色 + 品牌色动态生成
// 用 ColorTokens 构建，由 Theme.kt 注入 MaterialTheme
// ============================================================================

package com.pai.app.core.designsystem.foundation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.pai.app.core.designsystem.foundation.tokens.BrandAmber400
import com.pai.app.core.designsystem.foundation.tokens.BrandAmber500
import com.pai.app.core.designsystem.foundation.tokens.BrandEmerald100
import com.pai.app.core.designsystem.foundation.tokens.BrandEmerald300
import com.pai.app.core.designsystem.foundation.tokens.BrandEmerald400
import com.pai.app.core.designsystem.foundation.tokens.BrandEmerald50
import com.pai.app.core.designsystem.foundation.tokens.BrandEmerald500
import com.pai.app.core.designsystem.foundation.tokens.BrandEmerald700
import com.pai.app.core.designsystem.foundation.tokens.BrandEmerald800
import com.pai.app.core.designsystem.foundation.tokens.BrandEmerald900
import com.pai.app.core.designsystem.foundation.tokens.BrandRose100
import com.pai.app.core.designsystem.foundation.tokens.BrandRose300
import com.pai.app.core.designsystem.foundation.tokens.BrandRose400
import com.pai.app.core.designsystem.foundation.tokens.BrandRose50
import com.pai.app.core.designsystem.foundation.tokens.BrandRose600
import com.pai.app.core.designsystem.foundation.tokens.BrandRose700
import com.pai.app.core.designsystem.foundation.tokens.BrandRose800
import com.pai.app.core.designsystem.foundation.tokens.BrandRose900
import com.pai.app.core.designsystem.foundation.tokens.BrandSky400
import com.pai.app.core.designsystem.foundation.tokens.BrandSky500
import com.pai.app.core.designsystem.foundation.tokens.BrandSlate100
import com.pai.app.core.designsystem.foundation.tokens.BrandSlate200
import com.pai.app.core.designsystem.foundation.tokens.BrandSlate300
import com.pai.app.core.designsystem.foundation.tokens.BrandSlate400
import com.pai.app.core.designsystem.foundation.tokens.BrandSlate500
import com.pai.app.core.designsystem.foundation.tokens.BrandSlate600
import com.pai.app.core.designsystem.foundation.tokens.BrandSlate700
import com.pai.app.core.designsystem.foundation.tokens.BrandSlate800
import com.pai.app.core.designsystem.foundation.tokens.BrandSlate50
import com.pai.app.core.designsystem.foundation.tokens.BrandSlate900
import com.pai.app.core.designsystem.foundation.tokens.BrandSlate950
import com.pai.app.core.designsystem.foundation.tokens.DSBrandColor

// ---------------------------------------------------------------------------
// 动态 ColorScheme 生成：根据 [DSBrandColor] 实时构建 Light/Dark ColorScheme
// ---------------------------------------------------------------------------

/**
 * 根据品牌色动态生成 Light ColorScheme
 *
 * 设计原则：
 * - primary / secondary / primaryContainer 等品牌相关色位由 [brand] 提供
 * - tertiary（Emerald）/ error（Rose）固定，不随品牌色变化（语义色保持一致）
 * - 中性色（Slate）固定，避免随品牌色调意外漂移
 *
 * @param brand 品牌色板枚举
 */
internal fun buildLightColorScheme(brand: DSBrandColor): ColorScheme = lightColorScheme(
    primary = brand.primary,
    onPrimary = Color.White,
    primaryContainer = brand.primaryContainer,
    onPrimaryContainer = brand.onPrimaryContainer,
    inversePrimary = brand.palette[400]!!,
    secondary = brand.secondary,
    onSecondary = Color.White,
    secondaryContainer = brand.palette[50]!!,
    onSecondaryContainer = brand.palette[800]!!,
    // tertiary 用 Emerald（固定，不随品牌色变化）
    tertiary = BrandEmerald500,
    onTertiary = Color.White,
    tertiaryContainer = BrandEmerald50,
    onTertiaryContainer = BrandEmerald700,
    // error 用 Rose（固定）
    error = BrandRose600,
    onError = Color.White,
    errorContainer = BrandRose50,
    onErrorContainer = BrandRose700,
    // 中性色用 Slate（固定）
    background = Color.White,
    onBackground = BrandSlate900,
    surface = Color.White,
    onSurface = BrandSlate900,
    surfaceVariant = BrandSlate100,
    onSurfaceVariant = BrandSlate700,
    surfaceTint = brand.primary,
    inverseSurface = BrandSlate800,
    inverseOnSurface = BrandSlate50,
    outline = BrandSlate400,
    outlineVariant = BrandSlate200,
    scrim = Color.Black,
)

/**
 * 根据品牌色动态生成 Dark ColorScheme
 *
 * @param brand 品牌色板枚举
 * @param amoled 是否为 AMOLED 纯黑模式（true 时 background / surface 使用 Color.Black）
 */
internal fun buildDarkColorScheme(brand: DSBrandColor, amoled: Boolean = false): ColorScheme = darkColorScheme(
    primary = brand.palette[400]!!,
    onPrimary = brand.palette[900]!!,
    primaryContainer = brand.palette[700]!!,
    onPrimaryContainer = brand.palette[50]!!,
    inversePrimary = brand.primary,
    secondary = brand.palette[300]!!,
    onSecondary = brand.palette[900]!!,
    secondaryContainer = brand.palette[800]!!,
    onSecondaryContainer = brand.palette[100]!!,
    tertiary = BrandEmerald400,
    onTertiary = BrandEmerald900,
    tertiaryContainer = BrandEmerald700,
    onTertiaryContainer = BrandEmerald50,
    error = BrandRose400,
    onError = BrandRose900,
    errorContainer = BrandRose700,
    onErrorContainer = BrandRose50,
    background = if (amoled) Color.Black else BrandSlate950,
    onBackground = BrandSlate50,
    // AMOLED 模式下 surface 使用 BrandSlate900（深近黑而非纯黑），
    // 保证 Elevated Card / Dialog / BottomSheet 在纯黑背景上仍可见，
    // 避免 surface 与 background 同为 Color.Black 导致层级塌陷。
    surface = BrandSlate900,
    onSurface = BrandSlate50,
    surfaceVariant = BrandSlate800,
    onSurfaceVariant = BrandSlate300,
    surfaceTint = brand.palette[400]!!,
    inverseSurface = BrandSlate100,
    inverseOnSurface = BrandSlate900,
    outline = BrandSlate500,
    outlineVariant = BrandSlate700,
    scrim = Color.Black,
)

// ---------------------------------------------------------------------------
// 高对比度配色（High Contrast）- WCAG AAA 7:1 标准
// 用于无障碍场景：弱视用户、强光环境、辅助技术配套
// 设计原则：
// - 文字与背景对比度 ≥ 7:1（WCAG AAA 标准）
// - 状态色（success/warning/error）使用最饱和的色阶
// - 中性色使用极差（纯黑/纯白 vs 纯灰），最大化层级感
// - outline 提升为更深色阶，确保描边可见
// ---------------------------------------------------------------------------

/**
 * 高对比度 Light 配色
 * - 文字用纯黑（Color.Black）保证与白底对比度 = 21:1
 * - outline 用 BrandSlate900（深黑）替代 BrandSlate400（浅灰）
 * - primary 使用 brand.palette[800] 加深，确保 onPrimary=White 对比度 > 7:1
 */
internal fun buildHighContrastLightColorScheme(brand: DSBrandColor): ColorScheme = lightColorScheme(
    primary = brand.palette[800]!!,
    onPrimary = Color.White,
    primaryContainer = brand.palette[200]!!,
    onPrimaryContainer = brand.palette[900]!!,
    inversePrimary = brand.palette[300]!!,
    secondary = brand.palette[700]!!,
    onSecondary = Color.White,
    secondaryContainer = brand.palette[100]!!,
    onSecondaryContainer = brand.palette[900]!!,
    tertiary = BrandEmerald700,
    onTertiary = Color.White,
    tertiaryContainer = BrandEmerald100,
    onTertiaryContainer = BrandEmerald900,
    error = BrandRose800,
    onError = Color.White,
    errorContainer = BrandRose100,
    onErrorContainer = BrandRose900,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = BrandSlate200,
    onSurfaceVariant = BrandSlate900,  // 比标准 Light 更深
    surfaceTint = brand.palette[800]!!,
    inverseSurface = BrandSlate900,
    inverseOnSurface = Color.White,
    outline = BrandSlate900,           // 比标准 Light 更深
    outlineVariant = BrandSlate700,    // 比标准 Light 更深
    scrim = Color.Black,
)

/**
 * 高对比度 Dark 配色
 * - 文字用纯白（Color.White）保证与黑底对比度 = 21:1
 * - outline 用 BrandSlate100（浅白）替代 BrandSlate500
 * - primary 使用 brand.palette[200] 提亮，确保 onPrimary=Black 对比度 > 7:1
 */
internal fun buildHighContrastDarkColorScheme(brand: DSBrandColor): ColorScheme = darkColorScheme(
    primary = brand.palette[200]!!,
    onPrimary = Color.Black,
    primaryContainer = brand.palette[600]!!,
    onPrimaryContainer = Color.White,
    inversePrimary = brand.palette[800]!!,
    secondary = brand.palette[200]!!,
    onSecondary = Color.Black,
    secondaryContainer = brand.palette[700]!!,
    onSecondaryContainer = Color.White,
    tertiary = BrandEmerald300,
    onTertiary = Color.Black,
    tertiaryContainer = BrandEmerald800,
    onTertiaryContainer = Color.White,
    error = BrandRose300,
    onError = Color.Black,
    errorContainer = BrandRose800,
    onErrorContainer = Color.White,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    surfaceVariant = BrandSlate900,
    onSurfaceVariant = Color.White,    // 比标准 Dark 更亮
    surfaceTint = brand.palette[200]!!,
    inverseSurface = Color.White,
    inverseOnSurface = Color.Black,
    outline = BrandSlate100,           // 比标准 Dark 更亮
    outlineVariant = BrandSlate300,    // 比标准 Dark 更亮
    scrim = Color.Black,
)

// ---------------------------------------------------------------------------
// 扩展语义色（通过 LocalExtendedColors 注入，跨 Light/Dark）
// ---------------------------------------------------------------------------

internal data class ExtendedColors(
    val success: Color = BrandEmerald500,
    val onSuccess: Color = Color.White,
    val warning: Color = BrandAmber500,
    val onWarning: Color = Color.White,
    val info: Color = BrandSky500,
    val onInfo: Color = Color.White,
    val border: Color = BrandSlate200,
    val borderDark: Color = BrandSlate700,
    val skeletonBase: Color = BrandSlate200,
    val skeletonHighlight: Color = BrandSlate100
)

internal val LightExtendedColors = ExtendedColors(
    success = BrandEmerald500,
    onSuccess = Color.White,
    warning = BrandAmber500,
    onWarning = Color.White,
    info = BrandSky500,
    onInfo = Color.White,
    border = BrandSlate200,
    borderDark = BrandSlate700,
    skeletonBase = BrandSlate200,
    skeletonHighlight = BrandSlate100
)

internal val DarkExtendedColors = ExtendedColors(
    success = BrandEmerald400,
    onSuccess = BrandSlate900,
    warning = BrandAmber400,
    onWarning = BrandSlate900,
    info = BrandSky400,
    onInfo = BrandSlate900,
    border = BrandSlate700,
    borderDark = BrandSlate600,
    skeletonBase = BrandSlate800,
    skeletonHighlight = BrandSlate700
)

/**
 * 扩展语义色 CompositionLocal
 * - 跨 Light/Dark 主题，用于 success/warning/info 等非 M3 标准色
 */
internal val LocalExtendedColors = staticCompositionLocalOf { LightExtendedColors }
