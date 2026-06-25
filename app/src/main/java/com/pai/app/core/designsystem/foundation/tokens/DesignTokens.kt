// ============================================================================
// DesignTokens.kt
// Design Token 集中管理入口
// 将所有设计变量（颜色、字号、间距、圆角、动效）整合为单一访问入口
// 业务代码通过 DSTokens.xxx 访问所有设计 Token
//
// 设计原则（重构后）：
// - 本文件位于 foundation/tokens/ 目录，与 ColorTokens / SpacingTokens 等同级
// - 不再 import theme，所有原始 Token 都在同目录定义
// - theme/* 文件反向 import 本目录的 Token 来构建 Material 3 集成
// ============================================================================

package com.pai.app.core.designsystem.foundation.tokens

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.pai.app.core.designsystem.foundation.tokens.Alpha as RootAlpha
import com.pai.app.core.designsystem.foundation.tokens.Border as RootBorder
import com.pai.app.core.designsystem.foundation.tokens.IconSize as RootIconSize
import com.pai.app.core.designsystem.foundation.tokens.ComponentHeight as RootComponentHeight
import com.pai.app.core.designsystem.foundation.tokens.minTouchTarget as rootMinTouchTarget

/**
 * Design Token 总入口
 *
 * 使用方式：
 * ```kotlin
 * val spacing = DSTokens.Spacing.lg
 * val radius = DSTokens.Radius.medium
 * val color = DSTokens.Brand.indigo600
 * ```
 *
 * 设计原则：
 * 1. 所有 Token 必须从此对象访问，禁止业务代码硬编码颜色/尺寸
 * 2. Token 命名语义化，避免出现 "blue_500" 这类色值化命名
 * 3. 新增 Token 时，需同时更新 README 文档
 */
object DSTokens {

    /**
     * 间距 Token
     * - 4dp 基准栅格，所有间距为 4 的倍数
     */
    object Spacing {
        val none: Dp = DSSpacing.none
        val xxs: Dp = DSSpacing.xxs
        val xs: Dp = DSSpacing.xs
        val sm: Dp = DSSpacing.sm
        val md: Dp = DSSpacing.md
        val lg: Dp = DSSpacing.lg
        val xl: Dp = DSSpacing.xl
        val xxl: Dp = DSSpacing.xxl
        val xxxl: Dp = DSSpacing.xxxl
        val huge: Dp = DSSpacing.huge
        val giant: Dp = DSSpacing.giant
    }

    /**
     * 圆角 Token
     * - 5 档圆角，覆盖从微圆角到胶囊形
     */
    object Radius {
        val none: Dp = 0.dp
        val extraSmall: Dp = ShapeScale.EXTRA_SMALL.dp
        val small: Dp = ShapeScale.SMALL.dp
        val medium: Dp = ShapeScale.MEDIUM.dp
        val large: Dp = ShapeScale.LARGE.dp
        val extraLarge: Dp = ShapeScale.EXTRA_LARGE.dp
        val full: Dp = ShapeScale.FULL.dp
    }

    /**
     * 高度 Token
     * - 6 档高度，决定阴影与 z 轴层级
     */
    object Elevation {
        val level0: Dp = DSElevation.level0
        val level1: Dp = DSElevation.level1
        val level2: Dp = DSElevation.level2
        val level3: Dp = DSElevation.level3
        val level4: Dp = DSElevation.level4
        val level6: Dp = DSElevation.level6
        val level8: Dp = DSElevation.level8
        val level12: Dp = DSElevation.level12
    }

    /**
     * 字号 Token
     * - 5 大类 × 3 小档 = 15 个字号阶
     */
    object FontSize {
        // Display
        val displayLarge: TextUnit = TypeScale.DISPLAY_LARGE_SIZE.sp
        val displayMedium: TextUnit = TypeScale.DISPLAY_MEDIUM_SIZE.sp
        val displaySmall: TextUnit = TypeScale.DISPLAY_SMALL_SIZE.sp

        // Headline
        val headlineLarge: TextUnit = TypeScale.HEADLINE_LARGE_SIZE.sp
        val headlineMedium: TextUnit = TypeScale.HEADLINE_MEDIUM_SIZE.sp
        val headlineSmall: TextUnit = TypeScale.HEADLINE_SMALL_SIZE.sp

        // Title
        val titleLarge: TextUnit = TypeScale.TITLE_LARGE_SIZE.sp
        val titleMedium: TextUnit = TypeScale.TITLE_MEDIUM_SIZE.sp
        val titleSmall: TextUnit = TypeScale.TITLE_SMALL_SIZE.sp

        // Body
        val bodyLarge: TextUnit = TypeScale.BODY_LARGE_SIZE.sp
        val bodyMedium: TextUnit = TypeScale.BODY_MEDIUM_SIZE.sp
        val bodySmall: TextUnit = TypeScale.BODY_SMALL_SIZE.sp

        // Label
        val labelLarge: TextUnit = TypeScale.LABEL_LARGE_SIZE.sp
        val labelMedium: TextUnit = TypeScale.LABEL_MEDIUM_SIZE.sp
        val labelSmall: TextUnit = TypeScale.LABEL_SMALL_SIZE.sp
    }

    /**
     * 动效时长 Token
     * - 单位 毫秒
     */
    object Duration {
        const val instant = MSDuration.instant
        const val small1 = MSDuration.small1
        const val small2 = MSDuration.small2
        const val small3 = MSDuration.small3
        const val medium1 = MSDuration.medium1
        const val medium2 = MSDuration.medium2
        const val medium3 = MSDuration.medium3
        const val medium4 = MSDuration.medium4
        const val long1 = MSDuration.long1
        const val long2 = MSDuration.long2
        const val long3 = MSDuration.long3
        /** 骨架屏闪烁动画时长 */
        const val skeleton = 800
    }

    /**
     * 动效缓动 Token
     */
    object Easing {
        val emphasized = MSEasing.emphasized
        val emphasizedDecelerate = MSEasing.emphasizedDecelerate
        val emphasizedAccelerate = MSEasing.emphasizedAccelerate
        val standard = MSEasing.standard
        val standardDecelerate = MSEasing.standardDecelerate
        val standardAccelerate = MSEasing.standardAccelerate
    }

    /**
     * 品牌 Token
     * - 静态颜色 Token，不随主题切换变化
     * - 用于：Logo、品牌插画、状态色（部分场景）
     * - 实际色值定义在 [ColorTokens]，此处仅作语义化聚合
     */
    object Brand {
        // 靛蓝
        val indigo50: Color = BrandIndigo50
        val indigo100: Color = BrandIndigo100
        val indigo200: Color = BrandIndigo200
        val indigo300: Color = BrandIndigo300
        val indigo400: Color = BrandIndigo400
        val indigo500: Color = BrandIndigo500
        val indigo600: Color = BrandIndigo600  // 主色
        val indigo700: Color = BrandIndigo700
        val indigo800: Color = BrandIndigo800
        val indigo900: Color = BrandIndigo900

        // 翡翠绿
        val emerald50: Color = BrandEmerald50
        val emerald100: Color = BrandEmerald100
        val emerald200: Color = BrandEmerald200
        val emerald300: Color = BrandEmerald300
        val emerald400: Color = BrandEmerald400
        val emerald500: Color = BrandEmerald500
        val emerald600: Color = BrandEmerald600
        val emerald700: Color = BrandEmerald700
        val emerald800: Color = BrandEmerald800
        val emerald900: Color = BrandEmerald900

        // 玫瑰红
        val rose50: Color = BrandRose50
        val rose100: Color = BrandRose100
        val rose200: Color = BrandRose200
        val rose300: Color = BrandRose300
        val rose400: Color = BrandRose400
        val rose500: Color = BrandRose500
        val rose600: Color = BrandRose600
        val rose700: Color = BrandRose700
        val rose800: Color = BrandRose800
        val rose900: Color = BrandRose900

        // 琥珀黄
        val amber50: Color = BrandAmber50
        val amber100: Color = BrandAmber100
        val amber200: Color = BrandAmber200
        val amber300: Color = BrandAmber300
        val amber400: Color = BrandAmber400
        val amber500: Color = BrandAmber500
        val amber600: Color = BrandAmber600
        val amber700: Color = BrandAmber700
        val amber800: Color = BrandAmber800
        val amber900: Color = BrandAmber900

        // 天蓝
        val sky50: Color = BrandSky50
        val sky100: Color = BrandSky100
        val sky200: Color = BrandSky200
        val sky300: Color = BrandSky300
        val sky400: Color = BrandSky400
        val sky500: Color = BrandSky500
        val sky600: Color = BrandSky600
        val sky700: Color = BrandSky700
        val sky800: Color = BrandSky800
        val sky900: Color = BrandSky900

        // 中性色
        val slate50: Color = BrandSlate50
        val slate100: Color = BrandSlate100
        val slate200: Color = BrandSlate200
        val slate300: Color = BrandSlate300
        val slate400: Color = BrandSlate400
        val slate500: Color = BrandSlate500
        val slate600: Color = BrandSlate600
        val slate700: Color = BrandSlate700
        val slate800: Color = BrandSlate800
        val slate900: Color = BrandSlate900
        val slate950: Color = BrandSlate950
    }

    /**
     * 最小可点击区域
     * - Material 推荐最小 48dp
     * - iOS 推荐最小 44pt
     * - 我们采用 48dp
     */
    val minTouchTarget: Dp = rootMinTouchTarget

    /**
     * 描边宽度 Token
     */
    object Border {
        val hairline: Dp = RootBorder.hairline
        val thin: Dp = RootBorder.thin
        val medium: Dp = RootBorder.medium
        val thick: Dp = RootBorder.thick
    }

    /**
     * 透明度 Token
     */
    object Alpha {
        const val overlay: Float = RootAlpha.overlay
        const val disabledContainer: Float = RootAlpha.disabledContainer
        const val disabledContent: Float = RootAlpha.disabledContent
        const val loadingScrim: Float = RootAlpha.loadingScrim
    }

    /**
     * 常用图标尺寸
     */
    object IconSize {
        val xs: Dp = RootIconSize.xs
        val sm: Dp = RootIconSize.sm
        val md: Dp = RootIconSize.md
        val lg: Dp = RootIconSize.lg
        val xl: Dp = RootIconSize.xl
    }

    /**
     * 组件高度
     */
    object ComponentHeight {
        val buttonSmall: Dp = RootComponentHeight.buttonSmall
        val buttonMedium: Dp = RootComponentHeight.buttonMedium
        val buttonLarge: Dp = RootComponentHeight.buttonLarge
        val textField: Dp = RootComponentHeight.textField
        val textAreaMin: Dp = RootComponentHeight.textAreaMin
        val listItem: Dp = RootComponentHeight.listItem
        val listItemCompact: Dp = RootComponentHeight.listItemCompact
        val topBar: Dp = RootComponentHeight.topBar
        val bottomBar: Dp = RootComponentHeight.bottomBar
        val pickerMaxWidth: Dp = RootComponentHeight.pickerMaxWidth
        val tabIndicator: Dp = RootComponentHeight.tabIndicator
        val avatarSmall: Dp = RootComponentHeight.avatarSmall
        val avatarMedium: Dp = RootComponentHeight.avatarMedium
        val avatarLarge: Dp = RootComponentHeight.avatarLarge
        val avatarXLarge: Dp = RootComponentHeight.avatarXLarge
    }
}
