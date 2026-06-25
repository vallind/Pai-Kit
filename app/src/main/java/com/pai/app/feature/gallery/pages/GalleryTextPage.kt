// ============================================================================
// GalleryTextPage.kt
// 文本组件展示页：15 字号阶 × 10 颜色语义 × 4 字重
// ============================================================================
//
// M4 注：本文件含演示用硬编码 dp（如样本行间距）—— 仅用于文本组件演示，
// 业务代码请用 DSTokens.Spacing.*。
// ============================================================================
package com.pai.app.feature.gallery.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.shell.DSAppScaffold
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * GalleryTextPage - 文本组件展示页
 *
 * 内容分三节：
 * 1. 15 个字号阶：Display / Headline / Title / Body / Label 各 Large / Medium / Small
 * 2. 10 种颜色语义：Primary / Secondary / Tertiary / OnPrimary / OnSurface / Error /
 *    Success / Warning / Info / Custom
 * 3. 4 种字重：Normal / Medium / SemiBold / Bold
 *
 * @param onBackClick 返回上一页回调
 */
@Composable
internal fun GalleryTextPage(onBackClick: () -> Unit) {
    // H15：用 DSAppScaffold 替代 material3.Scaffold
    DSAppScaffold(
        title = "文本 Text",
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
            // 1. 15 个字号阶
            SectionCard(title = "15 个字号阶 Typography") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)) {
                    TextRow("Display Large", DSTextVariant.DisplayLarge)
                    TextRow("Display Medium", DSTextVariant.DisplayMedium)
                    TextRow("Display Small", DSTextVariant.DisplaySmall)
                    TextRow("Headline Large", DSTextVariant.HeadlineLarge)
                    TextRow("Headline Medium", DSTextVariant.HeadlineMedium)
                    TextRow("Headline Small", DSTextVariant.HeadlineSmall)
                    TextRow("Title Large", DSTextVariant.TitleLarge)
                    TextRow("Title Medium", DSTextVariant.TitleMedium)
                    TextRow("Title Small", DSTextVariant.TitleSmall)
                    TextRow("Body Large", DSTextVariant.BodyLarge)
                    TextRow("Body Medium", DSTextVariant.BodyMedium)
                    TextRow("Body Small", DSTextVariant.BodySmall)
                    TextRow("Label Large", DSTextVariant.LabelLarge)
                    TextRow("Label Medium", DSTextVariant.LabelMedium)
                    TextRow("Label Small", DSTextVariant.LabelSmall)
                }
            }

            // 2. 10 种颜色语义
            SectionCard(title = "10 种颜色语义 Colors") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
                    ColorTextRow("Primary 主文字色", DSTextColor.Primary)
                    ColorTextRow("Secondary 辅助文字色", DSTextColor.Secondary)
                    ColorTextRow("Tertiary 占位文字色", DSTextColor.Tertiary)
                    ColorTextRow("OnPrimary 主色背景文字", DSTextColor.OnPrimary)
                    ColorTextRow("OnSurface 同 surface 色调", DSTextColor.OnSurface)
                    ColorTextRow("Error 错误色", DSTextColor.Error)
                    ColorTextRow("Success 成功色", DSTextColor.Success)
                    ColorTextRow("Warning 警告色", DSTextColor.Warning)
                    ColorTextRow("Info 信息色", DSTextColor.Info)
                    ColorTextRow(
                        text = "Custom 自定义色（紫罗兰）",
                        color = DSTextColor.Custom,
                        customColor = androidx.compose.ui.graphics.Color(0xFF8B5CF6)
                    )
                }
            }

            // 3. 4 种字重
            SectionCard(title = "4 种字重 FontWeight") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
                    WeightRow("Normal", FontWeight.Normal)
                    WeightRow("Medium", FontWeight.Medium)
                    WeightRow("SemiBold", FontWeight.SemiBold)
                    WeightRow("Bold", FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * 单行字号预览：样本 + 名称
 */
@Composable
private fun TextRow(name: String, variant: DSTextVariant) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
    ) {
        DSText(
            text = "Aa 你好",
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

/**
 * 单行颜色预览：按颜色语义渲染文字
 */
@Composable
private fun ColorTextRow(
    text: String,
    color: DSTextColor,
    customColor: androidx.compose.ui.graphics.Color = androidx.compose.ui.graphics.Color.Unspecified
) {
    DSText(
        text = text,
        variant = DSTextVariant.BodyLarge,
        color = color,
        customColor = customColor
    )
}

/**
 * 单行字重预览：相同字号下不同字重对比
 */
@Composable
private fun WeightRow(name: String, weight: FontWeight) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
    ) {
        DSText(
            text = "Aa 你好 World",
            variant = DSTextVariant.TitleLarge,
            color = DSTextColor.Primary,
            fontWeight = weight,
            modifier = Modifier.weight(1f)
        )
        DSText(
            text = name,
            variant = DSTextVariant.LabelSmall,
            color = DSTextColor.Secondary
        )
    }
}
