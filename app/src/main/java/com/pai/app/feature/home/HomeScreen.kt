// ============================================================================
// HomeScreen.kt
// 通用空白首页 - 脚手架默认入口
// 业务方拉取后替换为真实业务页面
// ============================================================================

package com.pai.app.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.primitives.DSIcon
import com.pai.app.core.designsystem.primitives.DSIconSize
import com.pai.app.core.designsystem.primitives.DSIconTint
import com.pai.app.core.designsystem.shell.DSAppScaffold
import com.pai.app.core.designsystem.shell.DSTopBarAction
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 空白首页
 *
 * 脚手架默认入口，提供：
 * - 项目介绍卡片
 * - 快速导航卡片（Gallery / 设置 / 个人中心）
 * - 「开始开发」提示
 *
 * 业务方拉取脚手架后，应：
 * 1. 替换 [HomeViewModel] 中的逻辑为真实业务
 * 2. 替换本页面内容为真实业务 UI
 * 3. 删除「开始开发」提示
 *
 * @param onOpenGallery 打开组件 Gallery
 * @param onOpenSettings 打开设置页
 * @param onOpenProfile 打开个人中心
 */
@Composable
internal fun HomeScreen(
    onOpenGallery: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    // H15：用 DSAppScaffold 替代 material3.Scaffold，统一页面框架 + 内置 SnackbarHost
    DSAppScaffold(
        title = "Pai App",
        topBarActions = listOf(
            DSTopBarAction(
                icon = Icons.Default.Palette,
                contentDescription = "组件 Gallery",
                onClick = onOpenGallery,
            ),
            DSTopBarAction(
                icon = Icons.Default.Settings,
                contentDescription = "设置",
                onClick = onOpenSettings,
            ),
            DSTopBarAction(
                icon = Icons.Default.Person,
                contentDescription = "个人中心",
                onClick = onOpenProfile,
            ),
        ),
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = DSTokens.Spacing.lg, vertical = DSTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg),
        ) {
            // 项目介绍卡片
            DSCard(
                style = DSCardStyle.Elevated,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
                    DSText(
                        text = "Pai Design System",
                        variant = DSTextVariant.HeadlineMedium,
                        color = DSTextColor.Custom,
                        customColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    DSText(
                        text = "Android 原生单模块脚手架 · 单人 + AI 开发模式",
                        variant = DSTextVariant.BodyMedium,
                        color = DSTextColor.Custom,
                        customColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    )
                }
            }

            // 开始开发提示卡片
            DSCard(
                style = DSCardStyle.Filled,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
                    ) {
                        DSIcon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            size = DSIconSize.Medium,
                            tint = DSIconTint.Primary,
                        )
                        DSText(
                            text = "开始开发",
                            variant = DSTextVariant.TitleMedium,
                        )
                    }
                    DSText(
                        text = "1. 用 Claude Code 或 OpenCode 打开项目\n" +
                            "2. 让 AI 「添加一个 XXX 页面」\n" +
                            "3. AI 按 6 步流程自动生成 ViewModel + Screen + 路由\n" +
                            "4. 跑 ./gradlew :app:compileDebugKotlin 验证",
                        variant = DSTextVariant.BodyMedium,
                        color = DSTextColor.Secondary,
                    )
                }
            }

            // 快速导航卡片
            DSText(
                text = "快速导航",
                variant = DSTextVariant.TitleMedium,
            )

            NavigationCard(
                icon = Icons.Default.Palette,
                title = "组件 Gallery",
                description = "浏览 41 个 DS 组件 + 动效演示",
                onClick = onOpenGallery,
            )

            NavigationCard(
                icon = Icons.Default.Settings,
                title = "设置",
                description = "主题切换 / 缓存清理 / 关于",
                onClick = onOpenSettings,
            )

            NavigationCard(
                icon = Icons.Default.Person,
                title = "个人中心",
                description = "用户资料 / 主题设置 / 退出登录",
                onClick = onOpenProfile,
            )

            Spacer(modifier = Modifier.height(DSTokens.Spacing.lg))
        }
    }
}

@Composable
private fun NavigationCard(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
) {
    DSCard(
        style = DSCardStyle.Outlined,
        onClick = onClick,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(DSTokens.IconSize.xl)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(DSTokens.IconSize.xl)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    DSIcon(
                        imageVector = icon,
                        contentDescription = null,
                        size = DSIconSize.Medium,
                        tint = DSIconTint.Primary,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                DSText(text = title, variant = DSTextVariant.TitleMedium)
                DSText(
                    text = description,
                    variant = DSTextVariant.BodySmall,
                    color = DSTextColor.Secondary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    DSDesignTheme {
        HomeScreen(
            onOpenGallery = {},
            onOpenSettings = {},
            onOpenProfile = {},
        )
    }
}
