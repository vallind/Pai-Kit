// ============================================================================
// AppStructuredCard.kt
// 结构化业务卡 - 基于 DSCard 封装的「数据驱动业务容器」
// 与 AppCommonCard 的区别：
//   AppCommonCard 是 slot 驱动（业务自己组合内容）
//   AppStructuredCard 是数据驱动（业务传 data class，组件按契约渲染）
// 提供固定 slot 模式，强制业务方按规范填充
// ============================================================================

package com.pai.app.core.designsystem.containers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import com.pai.app.core.designsystem.primitives.DSAvatar
import com.pai.app.core.designsystem.primitives.DSAvatarSize
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.primitives.DSIcon
import com.pai.app.core.designsystem.primitives.DSIconSize
import com.pai.app.core.designsystem.primitives.DSIconTint
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant

/**
 * AppStructuredCardData - 结构化业务卡数据契约
 *
 * 业务方填充此数据类即可生成符合规范的卡片。
 *
 * @param title 主标题（必填）
 * @param subtitle 副标题（可选，如时间、状态、描述）
 * @param leadingIcon 左侧主图标（可选，用于类别标识）
 * @param leadingAvatarUrl 左侧头像 URL（可选，与 leadingIcon 互斥，优先 avatar）
 * @param trailingMeta 右侧元信息（可选，如「99+」「3 分钟前」）
 * @param trailingMetaIcon 右侧元信息图标（可选，如箭头/状态点）
 * @param badgeText 徽标文字（可选，如「NEW」「HOT」）
 * @param destructive 是否为破坏性卡片（如「删除」项，标题用 error 色）
 */
@Stable
data class AppStructuredCardData(
    val title: String,
    val subtitle: String? = null,
    val leadingIcon: ImageVector? = null,
    val leadingAvatarUrl: String? = null,
    val trailingMeta: String? = null,
    val trailingMetaIcon: ImageVector? = null,
    val badgeText: String? = null,
    val destructive: Boolean = false
)

/**
 * AppStructuredCard - 结构化业务卡
 *
 * 数据驱动版本的业务卡，业务方填充 [AppStructuredCardData] + 可选的扩展内容 slot，
 * 组件按固定契约渲染。
 *
 * 与 [AppCommonCard] 的区别：
 * - AppCommonCard：slot 驱动，业务自由组合内容
 * - AppStructuredCard：数据驱动，固定结构（leading + title/subtitle + trailing + 可选扩展）
 *
 * 适用场景：
 * - 列表项卡（联系人/商品/文章列表项）
 * - 设置项卡（icon + 名称 + 当前值 + 箭头）
 * - 通知项卡（avatar + 标题 + 时间 + 未读 badge）
 * - 统计卡（icon + 指标名 + 数值 + 趋势箭头）
 *
 * 使用示例：
 * ```kotlin
 * // 联系人列表项
 * AppStructuredCard(
 *     data = AppStructuredCardData(
 *         title = "张三",
 *         subtitle = "产品经理",
 *         leadingAvatarUrl = user.avatarUrl,
 *         trailingMeta = "138****8888",
 *         badgeText = "NEW"
 *     ),
 *     onClick = { /* 查看详情 */ }
 * )
 *
 * // 设置项
 * AppStructuredCard(
 *     data = AppStructuredCardData(
 *         title = "通知",
 *         subtitle = "已开启 3 项",
 *         leadingIcon = Icons.Default.Notifications,
 *         trailingMetaIcon = Icons.Default.ChevronRight
 *     ),
 *     onClick = { navController.navigate(NotificationSettingsRoute) }
 * )
 *
 * // 带扩展内容（统计卡）
 * AppStructuredCard(
 *     data = AppStructuredCardData(
 *         title = "本月销售额",
 *         subtitle = "2026 年 6 月",
 *         leadingIcon = Icons.Default.TrendingUp,
 *         trailingMeta = "+12.5%"
 *     )
 * ) {
 *     // 扩展内容 slot：图表 / 明细
 *     SalesChart(data = salesData)
 * }
 * ```
 *
 * 设计规范：
 * - 默认 DSCardStyle.Filled（与列表背景区分），可配置
 * - 主行：[leadingAvatarUrl / leadingIcon] + [title + subtitle] + [trailingMeta + trailingMetaIcon]
 * - 左侧 avatar 40dp / icon 24dp
 * - 标题 TitleMedium + SemiBold，副标题 BodySmall + OnSurfaceVariant
 * - 右侧元信息 BodyMedium + Secondary
 * - destructive = true 时标题用 error 色
 * - 扩展 slot 在主行下方，padding 对齐
 *
 * @param data 数据契约
 * @param modifier 修饰符
 * @param onClick 整卡点击回调（可选）
 * @param style 卡片风格，默认 Filled
 * @param content 扩展内容 slot（可选，渲染在主行下方）
 */
@Composable
fun AppStructuredCard(
    data: AppStructuredCardData,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    style: DSCardStyle = DSCardStyle.Filled,
    content: (@Composable () -> Unit)? = null
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        style = style,
        onClick = onClick
    ) {
        Column {
            // ----------------------------------------------------------------
            // 主行：leading + title/subtitle + trailing
            // ----------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(DSTokens.Spacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
            ) {
                // Leading：avatar 优先于 icon
                when {
                    data.leadingAvatarUrl != null -> {
                        DSAvatar(
                            imageUrl = data.leadingAvatarUrl,
                            size = DSAvatarSize.Medium40
                        )
                    }
                    data.leadingIcon != null -> {
                        Box(
                            modifier = Modifier.size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            DSIcon(
                                imageVector = data.leadingIcon,
                                contentDescription = null,
                                size = DSIconSize.Medium,
                                tint = if (data.destructive) DSIconTint.Error
                                else DSIconTint.Primary
                            )
                        }
                    }
                }

                // Title + Subtitle（weight 1f 占据剩余空间）
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    DSText(
                        text = data.title,
                        variant = DSTextVariant.TitleMedium,
                        color = if (data.destructive) DSTextColor.Error else DSTextColor.OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (data.subtitle != null) {
                        DSText(
                            text = data.subtitle,
                            variant = DSTextVariant.BodySmall,
                            color = DSTextColor.Secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Trailing：meta + icon
                if (data.trailingMeta != null || data.trailingMetaIcon != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)
                    ) {
                        if (data.trailingMeta != null) {
                            DSText(
                                text = data.trailingMeta,
                                variant = DSTextVariant.BodyMedium,
                                color = DSTextColor.Secondary,
                                maxLines = 1
                            )
                        }
                        if (data.trailingMetaIcon != null) {
                            Icon(
                                imageVector = data.trailingMetaIcon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(DSTokens.IconSize.sm)
                            )
                        }
                    }
                }

                // Badge（最右）
                if (data.badgeText != null) {
                    com.pai.app.core.designsystem.primitives.DSBadge(
                        text = data.badgeText,
                        type = com.pai.app.core.designsystem.primitives.DSBadgeType.Text
                    )
                }
            }

            // ----------------------------------------------------------------
            // 扩展内容 slot
            // ----------------------------------------------------------------
            if (content != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = DSTokens.Spacing.md,
                            end = DSTokens.Spacing.md,
                            bottom = DSTokens.Spacing.md
                        )
                ) {
                    content()
                }
            }
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "AppStructuredCard - 联系人项")
@Composable
private fun AppStructuredCardContactPreview() {
    DSDesignTheme {
        AppStructuredCard(
            data = AppStructuredCardData(
                title = "张三",
                subtitle = "产品经理 · 字节跳动",
                leadingAvatarUrl = null,  // Preview 无网络图，用占位
                trailingMeta = "138****8888",
                badgeText = "NEW"
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "AppStructuredCard - 设置项")
@Composable
private fun AppStructuredCardSettingPreview() {
    DSDesignTheme {
        AppStructuredCard(
            data = AppStructuredCardData(
                title = "通知",
                subtitle = "已开启 3 项",
                leadingIcon = Icons.Default.Notifications,
                trailingMetaIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true, name = "AppStructuredCard - 统计卡（带扩展）")
@Composable
private fun AppStructuredCardStatsPreview() {
    DSDesignTheme {
        AppStructuredCard(
            data = AppStructuredCardData(
                title = "本月销售额",
                subtitle = "2026 年 6 月",
                leadingIcon = Icons.Default.TrendingUp,
                trailingMeta = "+12.5%"
            )
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                DSText(
                    text = "¥ 128,500",
                    variant = DSTextVariant.HeadlineMedium,
                    color = DSTextColor.Primary
                )
                DSText(
                    text = "较上月增长 12.5%",
                    variant = DSTextVariant.BodySmall,
                    color = DSTextColor.Success
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "AppStructuredCard - 破坏性项")
@Composable
private fun AppStructuredCardDestructivePreview() {
    DSDesignTheme {
        AppStructuredCard(
            data = AppStructuredCardData(
                title = "退出登录",
                subtitle = "将清除本地缓存",
                leadingIcon = Icons.Default.Logout,
                destructive = true
            ),
            onClick = {}
        )
    }
}
