// ============================================================================
// GalleryLayoutPage.kt
// 布局与 Token 演示页：Spacing / Radius / Elevation / IconSize / Border / Alpha
// 直接使用原生 Box/Column/Row + Modifier + DSTokens，演示 DS 布局哲学
// ============================================================================

package com.pai.app.feature.gallery.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.shell.DSAppScaffold

/**
 * GalleryLayoutPage - 布局与 Token 演示页
 *
 * 演示 DS 布局哲学：使用原生 Box/Column/Row + Modifier 链 + DSTokens。
 * 不再提供 DSBox/DSColumn/DSRow 等包装组件，避免 API 爆炸。
 *
 * 内容分六节：
 * 1. Spacing 间距体系（11 档）
 * 2. Radius 圆角体系（7 档）
 * 3. Elevation 海拔体系（8 级）
 * 4. IconSize 图标尺寸（5 档）
 * 5. Border 描边宽度（4 档）
 * 6. Alpha 透明度（4 档）
 *
 * @param onBackClick 返回上一页回调
 */
@Composable
internal fun GalleryLayoutPage(onBackClick: () -> Unit) {
    DSAppScaffold(
        title = "布局与 Token",
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
            // 1. Spacing 间距体系
            LayoutPageSectionCard(title = "Spacing 间距体系（11 档）") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
                    SpacingRow("xxs", DSTokens.Spacing.xxs)
                    SpacingRow("xs", DSTokens.Spacing.xs)
                    SpacingRow("sm", DSTokens.Spacing.sm)
                    SpacingRow("md", DSTokens.Spacing.md)
                    SpacingRow("lg", DSTokens.Spacing.lg)
                    SpacingRow("xl", DSTokens.Spacing.xl)
                    SpacingRow("xxl", DSTokens.Spacing.xxl)
                    SpacingRow("xxxl", DSTokens.Spacing.xxxl)
                }
            }

            // 2. Radius 圆角体系
            LayoutPageSectionCard(title = "Radius 圆角体系（7 档）") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadiusBox("none", DSTokens.Radius.none)
                    RadiusBox("xs", DSTokens.Radius.extraSmall)
                    RadiusBox("sm", DSTokens.Radius.small)
                    RadiusBox("md", DSTokens.Radius.medium)
                    RadiusBox("lg", DSTokens.Radius.large)
                    RadiusBox("xl", DSTokens.Radius.extraLarge)
                    RadiusBox("full", DSTokens.Radius.full)
                }
            }

            // 3. Elevation 海拔体系
            LayoutPageSectionCard(title = "Elevation 海拔体系（8 级）") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ElevationBox("L0", DSTokens.Elevation.level0)
                    ElevationBox("L1", DSTokens.Elevation.level1)
                    ElevationBox("L2", DSTokens.Elevation.level2)
                    ElevationBox("L3", DSTokens.Elevation.level3)
                    ElevationBox("L4", DSTokens.Elevation.level4)
                    ElevationBox("L6", DSTokens.Elevation.level6)
                }
            }

            // 4. IconSize 图标尺寸
            LayoutPageSectionCard(title = "IconSize 图标尺寸（5 档）") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SizeBox("xs", DSTokens.IconSize.xs)
                    SizeBox("sm", DSTokens.IconSize.sm)
                    SizeBox("md", DSTokens.IconSize.md)
                    SizeBox("lg", DSTokens.IconSize.lg)
                    SizeBox("xl", DSTokens.IconSize.xl)
                }
            }

            // 5. Border 描边宽度
            LayoutPageSectionCard(title = "Border 描边宽度（4 档）") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
                    BorderRow("hairline", DSTokens.Border.hairline)
                    BorderRow("thin", DSTokens.Border.thin)
                    BorderRow("medium", DSTokens.Border.medium)
                    BorderRow("thick", DSTokens.Border.thick)
                }
            }

            // 6. Alpha 透明度
            LayoutPageSectionCard(title = "Alpha 透明度（4 档）") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
                ) {
                    AlphaBox("overlay", DSTokens.Alpha.overlay)
                    AlphaBox("disabled\nContainer", DSTokens.Alpha.disabledContainer)
                    AlphaBox("disabled\nContent", DSTokens.Alpha.disabledContent)
                    AlphaBox("loading\nScrim", DSTokens.Alpha.loadingScrim)
                }
            }

            Spacer(modifier = Modifier.height(DSTokens.Spacing.xxl))
        }
    }
}

@Composable
private fun LayoutPageSectionCard(title: String, content: @Composable () -> Unit) {
    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Elevated) {
        Column(modifier = Modifier.padding(DSTokens.Spacing.lg)) {
            DSText(
                text = title,
                variant = DSTextVariant.TitleMedium,
                color = DSTextColor.Primary
            )
            Spacer(modifier = Modifier.height(DSTokens.Spacing.md))
            content()
        }
    }
}

@Composable
private fun SpacingRow(name: String, dp: androidx.compose.ui.unit.Dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
    ) {
        Box(
            modifier = Modifier
                .width(dp)
                .height(20.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
        DSText(
            text = "$name = ${dp.value}dp",
            variant = DSTextVariant.BodyMedium,
            color = DSTextColor.Secondary
        )
    }
}

@Composable
private fun RadiusBox(name: String, dp: androidx.compose.ui.unit.Dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(dp))
        )
        DSText(
            text = name,
            variant = DSTextVariant.LabelSmall,
            color = DSTextColor.Secondary
        )
    }
}

@Composable
private fun ElevationBox(name: String, dp: androidx.compose.ui.unit.Dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .shadow(dp, RoundedCornerShape(DSTokens.Radius.medium))
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(DSTokens.Radius.medium))
        )
        DSText(
            text = name,
            variant = DSTextVariant.LabelSmall,
            color = DSTextColor.Secondary
        )
    }
}

@Composable
private fun SizeBox(name: String, dp: androidx.compose.ui.unit.Dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
        )
        DSText(
            text = "$name=${dp.value}dp",
            variant = DSTextVariant.LabelSmall,
            color = DSTextColor.Secondary
        )
    }
}

@Composable
private fun BorderRow(name: String, dp: androidx.compose.ui.unit.Dp) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .border(dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(DSTokens.Radius.small))
        )
        DSText(
            text = "$name = ${dp.value}dp",
            variant = DSTextVariant.BodyMedium,
            color = DSTextColor.Secondary
        )
    }
}

@Composable
private fun AlphaBox(name: String, alpha: Float) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
        )
        DSText(
            text = name,
            variant = DSTextVariant.LabelSmall,
            color = DSTextColor.Secondary
        )
    }
}
