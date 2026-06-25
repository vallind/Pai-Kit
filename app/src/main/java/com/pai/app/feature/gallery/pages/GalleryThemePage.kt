// ============================================================================
// GalleryThemePage.kt
// 主题 Token 展示页：颜色板 / 字号阶 / 圆角 / 间距 / 品牌色板选择器 /
//                     字号缩放选择器 / AMOLED 模式说明 / 主题实时预览
// ============================================================================
//
// M4 注：本文件含演示用硬编码 dp（如 1.dp 的色块边框）—— 仅用于 Token 视觉演示，
// 业务代码请用 DSTokens.Spacing / DSTokens.ComponentHeight / DSTokens.Border.*。
// ============================================================================
package com.pai.app.feature.gallery.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pai.app.core.appstate.MainActivityViewModel
import com.pai.app.core.designsystem.patterns.DSBanner
import com.pai.app.core.designsystem.patterns.DSBannerType
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSButtonSize
import com.pai.app.core.designsystem.primitives.DSButtonStyle
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.primitives.DSSegmentedControl
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.shell.DSAppScaffold
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.theme.extendedColors
import com.pai.app.core.designsystem.foundation.tokens.DSBrandColor
import com.pai.app.core.designsystem.foundation.tokens.DSFontSizeScale
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * GalleryThemePage - 主题 Token 展示页
 *
 * 内容分八节：
 * 1. 颜色板：Material3 标准色板 7 色 + 扩展语义色 3 色（success/warning/info）
 * 2. 字号阶：Display / Headline / Title / Body / Label 共 15 档预览
 * 3. 形状：extraSmall / small / medium / large / extraLarge 共 5 档圆角 Box 预览
 * 4. 间距：none / xxs / xs / sm / md / lg / xl / xxl / xxxl / huge / giant 共 11 档水平色块
 * 5. 品牌色板选择器：5 套品牌色（Indigo / Emerald / Rose / Amber / Sky）切换
 * 6. 字号缩放选择器：4 档字号（Small / Normal / Large / ExtraLarge）切换
 * 7. AMOLED 模式说明：DSBanner Info 提示纯黑模式生效条件
 * 8. 主题实时预览：DSDesignTheme 包裹的预览区域，展示当前品牌色 + 字号效果
 *
 * 品牌色板与字号缩放通过 [MainActivityViewModel.setBrandColor] /
 * [MainActivityViewModel.setFontSizeScale] 写入 DataStore，再由 MainActivity
 * 重新应用 [DSDesignTheme]，整棵 Compose 树随后以新主题重绘。
 *
 * @param onBackClick 返回上一页回调
 */
@Composable
internal fun GalleryThemePage(onBackClick: () -> Unit) {
    // 从 Activity scope 获取 MainActivityViewModel，确保与 MainActivity 共享同一份主题状态
    val context = LocalContext.current
    val mainViewModel: MainActivityViewModel = hiltViewModel(
        viewModelStoreOwner = context as ViewModelStoreOwner
    )
    val brandColor by mainViewModel.brandColor.collectAsStateWithLifecycle()
    val fontSizeScale by mainViewModel.fontSizeScale.collectAsStateWithLifecycle()

    // H15：用 DSAppScaffold 替代 material3.Scaffold
    DSAppScaffold(
        title = "主题 Theme",
        showBackIcon = true,
        onBackClick = onBackClick,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DSTokens.Spacing.lg, vertical = DSTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg)
        ) {
            ThemeSectionHeader(title = "颜色板 Color Palette")
            ColorPaletteSection()

            ThemeSectionHeader(title = "字号阶 Typography Scale")
            TypographyScaleSection()

            ThemeSectionHeader(title = "形状 Shape Scale")
            ShapeScaleSection()

            ThemeSectionHeader(title = "间距 Spacing Scale")
            SpacingScaleSection()

            ThemeSectionHeader(title = "品牌色板 Brand Color")
            BrandColorSection(
                brandColor = brandColor,
                onSelect = mainViewModel::setBrandColor
            )

            ThemeSectionHeader(title = "字号缩放 Font Size Scale")
            FontSizeScaleSection(
                fontSizeScale = fontSizeScale,
                onSelect = mainViewModel::setFontSizeScale
            )

            ThemeSectionHeader(title = "AMOLED 模式说明")
            AmoledInfoSection()

            ThemeSectionHeader(title = "主题实时预览 Live Preview")
            LivePreviewSection(brandColor = brandColor, fontSizeScale = fontSizeScale)
        }
    }
}

// ============================================================================
// 通用 Section 标题
// ============================================================================

/**
 * 一节的标题文本
 */
@Composable
private fun ThemeSectionHeader(title: String) {
    DSText(
        text = title,
        variant = DSTextVariant.TitleMedium,
        color = DSTextColor.Primary
    )
}

// ============================================================================
// 颜色板
// ============================================================================

/**
 * 颜色板展示：M3 标准色板 + 扩展语义色
 */
@Composable
private fun ColorPaletteSection() {
    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Filled) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
            // Material3 标准色板
            ColorRow(label = "primary", color = MaterialTheme.colorScheme.primary)
            ColorRow(label = "onPrimary", color = MaterialTheme.colorScheme.onPrimary)
            ColorRow(label = "secondary", color = MaterialTheme.colorScheme.secondary)
            ColorRow(label = "tertiary", color = MaterialTheme.colorScheme.tertiary)
            ColorRow(label = "error", color = MaterialTheme.colorScheme.error)
            ColorRow(label = "surface", color = MaterialTheme.colorScheme.surface)
            ColorRow(label = "outline", color = MaterialTheme.colorScheme.outline)

            // 扩展语义色
            ColorRow(label = "success", color = MaterialTheme.extendedColors.success)
            ColorRow(label = "warning", color = MaterialTheme.extendedColors.warning)
            ColorRow(label = "info", color = MaterialTheme.extendedColors.info)
        }
    }
}

/**
 * 单行颜色：色块 + 名称 + 色值描述
 */
@Composable
private fun ColorRow(label: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(DSTokens.IconSize.lg)
                .background(color = color, shape = RoundedCornerShape(DSTokens.Radius.small)),
            contentAlignment = Alignment.Center
        ) {
            // 表面色为白时显示边框
            if (color == Color.White) {
                Box(
                    modifier = Modifier
                        .size(DSTokens.IconSize.lg)
                        .background(
                            color = MaterialTheme.colorScheme.outlineVariant,
                            shape = RoundedCornerShape(DSTokens.Radius.small)
                        )
                )
                Box(
                    modifier = Modifier
                        .size(DSTokens.IconSize.lg - 1.dp)
                        .background(
                            color = color,
                            shape = RoundedCornerShape(DSTokens.Radius.small)
                        )
                )
            }
        }
        DSText(
            text = label,
            variant = DSTextVariant.BodyMedium,
            color = DSTextColor.Secondary
        )
    }
}

// ============================================================================
// 字号阶
// ============================================================================

/**
 * 15 档字号阶展示：Display / Headline / Title / Body / Label
 */
@Composable
private fun TypographyScaleSection() {
    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Filled) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
            TypographyRow("Display Large", DSTextVariant.DisplayLarge)
            TypographyRow("Display Medium", DSTextVariant.DisplayMedium)
            TypographyRow("Display Small", DSTextVariant.DisplaySmall)
            TypographyRow("Headline Large", DSTextVariant.HeadlineLarge)
            TypographyRow("Headline Medium", DSTextVariant.HeadlineMedium)
            TypographyRow("Headline Small", DSTextVariant.HeadlineSmall)
            TypographyRow("Title Large", DSTextVariant.TitleLarge)
            TypographyRow("Title Medium", DSTextVariant.TitleMedium)
            TypographyRow("Title Small", DSTextVariant.TitleSmall)
            TypographyRow("Body Large", DSTextVariant.BodyLarge)
            TypographyRow("Body Medium", DSTextVariant.BodyMedium)
            TypographyRow("Body Small", DSTextVariant.BodySmall)
            TypographyRow("Label Large", DSTextVariant.LabelLarge)
            TypographyRow("Label Medium", DSTextVariant.LabelMedium)
            TypographyRow("Label Small", DSTextVariant.LabelSmall)
        }
    }
}

/**
 * 单行字号：样本文字（按对应字号渲染）+ 名称
 */
@Composable
private fun TypographyRow(name: String, variant: DSTextVariant) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
    ) {
        DSText(
            text = "Aa",
            variant = variant,
            color = DSTextColor.Primary,
            modifier = Modifier.weight(1f)
        )
        DSText(
            text = name,
            variant = DSTextVariant.LabelSmall,
            color = DSTextColor.Secondary
        )
    }
}

// ============================================================================
// 形状
// ============================================================================

/**
 * 5 档圆角 Box 预览
 */
@Composable
private fun ShapeScaleSection() {
    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Filled) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShapeItem(label = "extraSmall", radius = DSTokens.Radius.extraSmall)
            ShapeItem(label = "small", radius = DSTokens.Radius.small)
            ShapeItem(label = "medium", radius = DSTokens.Radius.medium)
            ShapeItem(label = "large", radius = DSTokens.Radius.large)
            ShapeItem(label = "extraLarge", radius = DSTokens.Radius.extraLarge)
        }
    }
}

/**
 * 单个圆角预览：色块 + 名称
 */
@Composable
private fun ShapeItem(label: String, radius: Dp) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)
    ) {
        Box(
            modifier = Modifier
                .size(DSTokens.IconSize.xl)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(radius)
                )
        )
        DSText(
            text = label,
            variant = DSTextVariant.LabelSmall,
            color = DSTextColor.Secondary
        )
    }
}

// ============================================================================
// 间距
// ============================================================================

/**
 * 11 档间距水平色块预览
 */
@Composable
private fun SpacingScaleSection() {
    val spacings = listOf(
        "none" to DSTokens.Spacing.none,
        "xxs" to DSTokens.Spacing.xxs,
        "xs" to DSTokens.Spacing.xs,
        "sm" to DSTokens.Spacing.sm,
        "md" to DSTokens.Spacing.md,
        "lg" to DSTokens.Spacing.lg,
        "xl" to DSTokens.Spacing.xl,
        "xxl" to DSTokens.Spacing.xxl,
        "xxxl" to DSTokens.Spacing.xxxl,
        "huge" to DSTokens.Spacing.huge,
        "giant" to DSTokens.Spacing.giant
    )

    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Filled) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
            spacings.forEach { (name, dp) ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
                ) {
                    // 色块宽度反映间距大小（最小 4dp 保证可见）
                    Box(
                        modifier = Modifier
                            .height(DSTokens.Spacing.lg)
                            .width(dp.coerceAtLeast(DSTokens.Spacing.xs))
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(DSTokens.Radius.extraSmall)
                            )
                    )
                    DSText(
                        text = "$name (${dp.value.toInt()}dp)",
                        variant = DSTextVariant.BodySmall,
                        color = DSTextColor.Secondary
                    )
                }
            }
        }
    }
}

// ============================================================================
// 品牌色板选择器
// ============================================================================

/**
 * 5 套品牌色板选择器
 *
 * 用 DSSegmentedControl 展示 Indigo / Emerald / Rose / Amber / Sky，
 * 选中后调用 [MainActivityViewModel.setBrandColor] 写入 DataStore，
 * MainActivity 重新应用 [DSDesignTheme] 后整棵 UI 树以新品牌色重绘。
 *
 * @param brandColor 当前品牌色枚举
 * @param onSelect 选中品牌色回调
 */
@Composable
private fun BrandColorSection(
    brandColor: DSBrandColor,
    onSelect: (DSBrandColor) -> Unit
) {
    // 5 套品牌色枚举按顺序展示，选项文案取 displayName
    val brands = DSBrandColor.entries
    val options = brands.map { it.displayName }
    val selectedIndex = brands.indexOf(brandColor).coerceAtLeast(0)

    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Filled) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
            DSText(
                text = "点击切换品牌色板，整页 primary 色会实时变化",
                variant = DSTextVariant.LabelMedium,
                color = DSTextColor.Secondary
            )
            DSSegmentedControl(
                options = options,
                selectedIndex = selectedIndex,
                onSelectedChange = { index -> onSelect(brands[index]) }
            )

            // 当前品牌色预览条：5 个色块展示选中品牌色的完整色阶（50-900）
            BrandColorPaletteRow(brandColor = brandColor)
        }
    }
}

/**
 * 单个品牌色的完整色阶预览条
 *
 * 横向展示 50 / 100 / 300 / 500 / 700 / 900 六档代表性色位，
 * 直观反映品牌色的明暗层次。
 *
 * @param brandColor 品牌色枚举
 */
@Composable
private fun BrandColorPaletteRow(brandColor: DSBrandColor) {
    val palette = brandColor.palette
    // 取 6 档代表性色位（50 / 100 / 300 / 500 / 700 / 900）
    val sampleKeys = listOf(50, 100, 300, 500, 700, 900)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)
    ) {
        sampleKeys.forEach { key ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(DSTokens.Spacing.xxl)
                    .background(
                        color = palette[key] ?: MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(DSTokens.Radius.extraSmall)
                    )
            )
        }
    }
}

// ============================================================================
// 字号缩放选择器
// ============================================================================

/**
 * 4 档字号缩放选择器
 *
 * 用 DSSegmentedControl 展示 Small / Normal / Large / ExtraLarge，
 * 选中后调用 [MainActivityViewModel.setFontSizeScale] 写入 DataStore，
 * [DSDesignTheme] 通过 LocalFontSizeScale 注入到所有 DSText，字号实时变化。
 *
 * @param fontSizeScale 当前字号缩放枚举
 * @param onSelect 选中字号缩放回调
 */
@Composable
private fun FontSizeScaleSection(
    fontSizeScale: DSFontSizeScale,
    onSelect: (DSFontSizeScale) -> Unit
) {
    val scales = DSFontSizeScale.entries
    val options = scales.map { it.displayName }
    val selectedIndex = scales.indexOf(fontSizeScale).coerceAtLeast(0)

    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Filled) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
            DSText(
                text = "点击切换字号缩放，下方文本会按 ${fontSizeScale.multiplier}x 倍率渲染",
                variant = DSTextVariant.LabelMedium,
                color = DSTextColor.Secondary
            )
            DSSegmentedControl(
                options = options,
                selectedIndex = selectedIndex,
                onSelectedChange = { index -> onSelect(scales[index]) }
            )

            // 当前倍率下的样本文本预览
            DSText(
                text = "示例文本 The quick brown fox 1234567890",
                variant = DSTextVariant.BodyLarge,
                color = DSTextColor.Primary
            )
            DSText(
                text = "倍率：${fontSizeScale.multiplier}x（${fontSizeScale.displayName}）",
                variant = DSTextVariant.LabelSmall,
                color = DSTextColor.Secondary
            )
        }
    }
}

// ============================================================================
// AMOLED 模式说明
// ============================================================================

/**
 * AMOLED 模式说明
 *
 * 用 DSBanner Info 提示 AMOLED 模式仅在深色主题下生效，
 * background / surface 替换为 Color.Black，省电并提升对比度。
 */
@Composable
private fun AmoledInfoSection() {
    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Filled) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
            DSBanner(
                message = "AMOLED 模式在深色主题下生效，background / surface 为纯黑",
                type = DSBannerType.Info
            )
            DSText(
                text = "提示：前往 个人中心 → 主题模式 切换为「纯黑」即可启用 AMOLED 模式",
                variant = DSTextVariant.LabelSmall,
                color = DSTextColor.Secondary
            )
        }
    }
}

// ============================================================================
// 主题实时预览
// ============================================================================

/**
 * 主题实时预览
 *
 * 显式用 [DSDesignTheme] 包裹预览区域，展示当前品牌色 + 字号缩放的组合效果，
 * 包含标题 / 副标题 / 按钮三个代表性元素，便于直观验证主题切换结果。
 *
 * @param brandColor 当前品牌色
 * @param fontSizeScale 当前字号缩放
 */
@Composable
private fun LivePreviewSection(
    brandColor: DSBrandColor,
    fontSizeScale: DSFontSizeScale
) {
    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Filled) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
            DSText(
                text = "品牌色：${brandColor.displayName}    字号：${fontSizeScale.displayName}（${fontSizeScale.multiplier}x）",
                variant = DSTextVariant.LabelMedium,
                color = DSTextColor.Secondary
            )

            // 预览区域：DSDesignTheme 包裹，确保内部 primary 色与字号缩放与全局一致
            DSDesignTheme(
                brandColor = brandColor,
                fontSizeScale = fontSizeScale
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(DSTokens.Radius.medium)
                        )
                        .padding(DSTokens.Spacing.lg)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
                        DSText(
                            text = "标题 Title Large",
                            variant = DSTextVariant.TitleLarge,
                            color = DSTextColor.Primary
                        )
                        DSText(
                            text = "副标题 Body Medium，受字号缩放影响",
                            variant = DSTextVariant.BodyMedium,
                            color = DSTextColor.Secondary
                        )
                        DSButton(
                            text = "主操作按钮",
                            style = DSButtonStyle.Filled,
                            size = DSButtonSize.Medium,
                            onClick = {}
                        )
                    }
                }
            }
        }
    }
}
