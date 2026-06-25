// ============================================================================
// GalleryButtonPage.kt
// 按钮组件展示页：5 风格 × 3 尺寸 + 图标 / 禁用 / 加载 + IconButton + FAB + ExtendedFAB
// ============================================================================
//
// M4 注：本文件含演示用硬编码 dp（如按钮间距示例）—— 仅用于按钮组件演示，
// 业务代码请用 DSTokens.Spacing.*。
// ============================================================================
package com.pai.app.feature.gallery.pages

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSButtonSize
import com.pai.app.core.designsystem.primitives.DSButtonStyle
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.overlays.DSFAB
import com.pai.app.core.designsystem.overlays.DSFABStyle
import com.pai.app.core.designsystem.primitives.DSIconButton
import com.pai.app.core.designsystem.primitives.DSIconButtonStyle
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.shell.DSAppScaffold
import com.pai.app.core.designsystem.foundation.motion.pressScale
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * GalleryButtonPage - 按钮组件展示页
 *
 * 内容分七节：
 * 1. 5 种风格：Filled / Tonal / Outlined / Text / Error
 * 2. 3 种尺寸：Small / Medium / Large
 * 3. 带图标按钮：Add / Share / Favorite
 * 4. 禁用态与加载态
 * 5. DSIconButton 4 风格
 * 6. DSFAB Small / Large / Extended
 * 7. DSFAB Extended 可收起扩展按钮 + pressScale 按压演示
 *
 * @param onBackClick 返回上一页回调
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GalleryButtonPage(onBackClick: () -> Unit) {
    // H15：用 DSAppScaffold 替代 material3.Scaffold
    DSAppScaffold(
        title = "按钮 Button",
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
            // 1. 5 种风格
            SectionCard(title = "5 种风格 Styles") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
                ) {
                    DSButton(text = "Filled", onClick = {}, style = DSButtonStyle.Filled)
                    DSButton(text = "Tonal", onClick = {}, style = DSButtonStyle.Tonal)
                    DSButton(text = "Outlined", onClick = {}, style = DSButtonStyle.Outlined)
                    DSButton(text = "Text", onClick = {}, style = DSButtonStyle.Text)
                    DSButton(text = "Error", onClick = {}, style = DSButtonStyle.Error)
                }
            }

            // 2. 3 种尺寸
            SectionCard(title = "3 种尺寸 Sizes") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
                ) {
                    DSButton(text = "Small", onClick = {}, size = DSButtonSize.Small)
                    DSButton(text = "Medium", onClick = {}, size = DSButtonSize.Medium)
                    DSButton(text = "Large", onClick = {}, size = DSButtonSize.Large)
                }
            }

            // 3. 带图标按钮
            SectionCard(title = "带图标按钮 Icon Button") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
                ) {
                    DSButton(text = "添加", onClick = {}, icon = Icons.Default.Add)
                    DSButton(text = "分享", onClick = {}, icon = Icons.Default.Share)
                    DSButton(text = "收藏", onClick = {}, icon = Icons.Default.Favorite)
                }
            }

            // 4. 禁用态与加载态
            SectionCard(title = "禁用态与加载态 States") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
                ) {
                    DSButton(text = "禁用", onClick = {}, enabled = false)
                    DSButton(text = "提交中", onClick = {}, loading = true)
                    DSButton(
                        text = "处理中",
                        onClick = {},
                        loading = true,
                        style = DSButtonStyle.Tonal
                    )
                }
            }

            // 5. DSIconButton 4 风格
            SectionCard(title = "DSIconButton 4 风格") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DSIconButton(
                        icon = Icons.Default.Star,
                        contentDescription = "Standard",
                        onClick = {},
                        style = DSIconButtonStyle.Standard
                    )
                    DSIconButton(
                        icon = Icons.Default.Star,
                        contentDescription = "Filled",
                        onClick = {},
                        style = DSIconButtonStyle.Filled
                    )
                    DSIconButton(
                        icon = Icons.Default.Star,
                        contentDescription = "Tonal",
                        onClick = {},
                        style = DSIconButtonStyle.Tonal
                    )
                    DSIconButton(
                        icon = Icons.Default.Star,
                        contentDescription = "Outlined",
                        onClick = {},
                        style = DSIconButtonStyle.Outlined
                    )
                }
            }

            // 6. DSFAB Small / Large / Extended
            SectionCard(title = "DSFAB 三种风格") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
                ) {
                    DSFAB(
                        onClick = {},
                        icon = Icons.Default.Add,
                        contentDescription = "Small",
                        style = DSFABStyle.Small
                    )
                    DSFAB(
                        onClick = {},
                        icon = Icons.Default.Add,
                        contentDescription = "Large",
                        style = DSFABStyle.Large
                    )
                    DSFAB(
                        onClick = {},
                        icon = Icons.Default.Add,
                        contentDescription = "Extended",
                        style = DSFABStyle.Extended,
                        text = "新建"
                    )
                }
            }

            // 7. DSFAB Extended 可收起 + pressScale 演示
            SectionCard(title = "DSFAB Extended + pressScale 按压动效") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                    // 可收起扩展 FAB
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DSFAB(
                            onClick = {},
                            icon = Icons.Default.Edit,
                            contentDescription = "撰写",
                            style = DSFABStyle.Extended,
                            text = "撰写",
                            collapsed = false
                        )
                        DSFAB(
                            onClick = {},
                            icon = Icons.Default.Edit,
                            contentDescription = "撰写",
                            style = DSFABStyle.Extended,
                            text = "撰写",
                            collapsed = true
                        )
                    }

                    // pressScale 按压动效
                    DSText(
                        text = "按下下方按钮 / 卡片体验缩放反馈",
                        variant = DSTextVariant.LabelMedium,
                        color = DSTextColor.Secondary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DSButton(
                            text = "按压",
                            onClick = {},
                            icon = Icons.Default.Refresh,
                            modifier = Modifier.pressScale()
                        )
                        DSButton(
                            text = "Tonal 按压",
                            onClick = {},
                            style = DSButtonStyle.Tonal,
                            modifier = Modifier.pressScale()
                        )
                    }

                    // pressScale 应用到 DSCard
                    DSCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressScale(),
                        style = DSCardStyle.Elevated,
                        onClick = {}
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
                        ) {
                            DSIconButton(
                                icon = Icons.Default.Email,
                                contentDescription = "Mail",
                                onClick = {},
                                style = DSIconButtonStyle.Tonal
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                DSText(
                                    text = "按压卡片有缩放反馈",
                                    variant = DSTextVariant.BodyMedium,
                                    color = DSTextColor.Primary
                                )
                                DSText(
                                    text = "Modifier.pressScale() 0.95 缩放 + 150ms standard 缓动",
                                    variant = DSTextVariant.LabelSmall,
                                    color = DSTextColor.Secondary
                                )
                            }
                        }
                    }
                }
            }

            // 底部留白
            Spacer(modifier = Modifier.height(DSTokens.Spacing.xxl))
        }
    }
}

// ============================================================================
// 通用 Section 卡片
// ============================================================================

/**
 * 通用一节卡片：标题 + 内容
 *
 * @param title 节标题
 * @param content 节内容
 */
@Composable
internal fun SectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Filled) {
        Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
            DSText(
                text = title,
                variant = DSTextVariant.TitleMedium,
                color = DSTextColor.Primary
            )
            content()
        }
    }
}

/**
 * 通用 Section 标题（不放在卡片内时使用）
 *
 * @param title 标题
 */
@Composable
internal fun SectionHeader(title: String) {
    DSText(
        text = title,
        variant = DSTextVariant.TitleMedium,
        color = DSTextColor.Primary
    )
}
