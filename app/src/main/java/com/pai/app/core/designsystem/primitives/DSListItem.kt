// ============================================================================
// DSListItem.kt
// 列表项 - 基于 M3 ListItem 扩展
// 支持 SingleLine / TwoLine / ThreeLine 三种变体
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 列表项变体
 * - SingleLine: 仅标题，1 行
 * - TwoLine: 标题 + 副标题（supportingContent），2 行
 * - ThreeLine: overline + 标题 + 副标题，3 行
 */
internal enum class DSListItemVariant {
    SingleLine,
    TwoLine,
    ThreeLine
}

/**
 * DSListItem - 列表项
 *
 * 使用示例：
 * ```kotlin
 * // 单行带图标
 * DSListItem(
 *     title = "设置",
 *     leadingIcon = Icons.Default.Settings,
 *     trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
 *     onClick = { appNavigator.gotoSettings() }
 * )
 *
 * // 双行（标题 + 副标题）
 * DSListItem(
 *     title = "张三",
 *     subtitle = "zhangsan@example.com",
 *     leadingIcon = Icons.Default.AccountCircle,
 *     variant = DSListItemVariant.TwoLine
 * )
 *
 * // 三行（overline + 标题 + 副标题）
 * DSListItem(
 *     title = "Jetpack Compose 1.3.0",
 *     subtitle = "新增 ListItem、SearchBar、Slider 等 API",
 *     overline = "新版本发布",
 *     variant = DSListItemVariant.ThreeLine
 * )
 * ```
 *
 * 设计规范：
 * - 高度：SingleLine 56dp / TwoLine 72dp / ThreeLine 88dp（M3 默认）
 * - 标题：bodyLarge / onSurface
 * - 副标题：bodyMedium / onSurfaceVariant
 * - overline：labelMedium / onSurfaceVariant
 * - 图标尺寸：24dp
 * - 点击态：surfaceVariant 涟漪背景
 * - leadingIcon 与 leadingContent 同时存在时，leadingContent 优先
 * - trailingIcon 与 trailingContent 同时存在时，trailingContent 优先
 *
 * @param title 主标题（必填）
 * @param modifier 修饰符
 * @param subtitle 副标题（仅 TwoLine / ThreeLine 生效）
 * @param overline 顶行小标题（仅 ThreeLine 生效）
 * @param variant 列表项变体，默认 SingleLine
 * @param leadingIcon 前导图标（便捷参数）
 * @param leadingContent 前导自定义内容（覆盖 leadingIcon）
 * @param trailingIcon 尾部图标（便捷参数）
 * @param trailingContent 尾部自定义内容（覆盖 trailingIcon）
 * @param onClick 点击回调，非空时整行可点击
 * @param containerColor 容器背景色，默认透明（用于融入背景）
 */
@Composable
internal fun DSListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    overline: String? = null,
    variant: DSListItemVariant = DSListItemVariant.SingleLine,
    leadingIcon: ImageVector? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    trailingIcon: ImageVector? = null,
    trailingContent: (@Composable () -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    containerColor: Color = Color.Transparent
) {
    // 前导内容：优先使用 leadingContent，其次 leadingIcon
    val resolvedLeading: (@Composable () -> Unit)? = when {
        leadingContent != null -> leadingContent
        leadingIcon != null -> {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(DSTokens.IconSize.md),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> null
    }

    // 尾部内容：优先使用 trailingContent，其次 trailingIcon
    val resolvedTrailing: (@Composable () -> Unit)? = when {
        trailingContent != null -> trailingContent
        trailingIcon != null -> {
            {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    modifier = Modifier.size(DSTokens.IconSize.md),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        else -> null
    }

    // 标题内容
    val headline: @Composable () -> Unit = {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = if (variant == DSListItemVariant.ThreeLine) 2 else 1,
            overflow = TextOverflow.Ellipsis
        )
    }

    // 副标题内容（仅 TwoLine / ThreeLine）
    val supporting: (@Composable () -> Unit)? = if (variant != DSListItemVariant.SingleLine && subtitle != null) {
        {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = if (variant == DSListItemVariant.ThreeLine) 2 else 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    } else null

    // 顶行小标题（仅 ThreeLine）
    val overlineContent: (@Composable () -> Unit)? = if (variant == DSListItemVariant.ThreeLine && overline != null) {
        {
            Text(
                text = overline,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    } else null

    val clickModifier = if (onClick != null) {
        Modifier.clickable(
            role = Role.Button,
            onClick = onClick
        )
    } else {
        Modifier
    }

    ListItem(
        headlineContent = headline,
        modifier = modifier.then(clickModifier),
        leadingContent = resolvedLeading,
        overlineContent = overlineContent,
        supportingContent = supporting,
        trailingContent = resolvedTrailing,
        colors = ListItemDefaults.colors(
            containerColor = containerColor
        )
    )
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "ListItem - Single Line")
@Composable
private fun DSListItemSingleLinePreview() {
    DSDesignTheme {
        Column {
            DSListItem(
                title = "设置",
                leadingIcon = Icons.Default.Settings,
                trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                onClick = {}
            )
            DSListItem(
                title = "通知",
                leadingIcon = Icons.Default.Notifications,
                trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "ListItem - Two Line")
@Composable
private fun DSListItemTwoLinePreview() {
    DSDesignTheme {
        Column {
            DSListItem(
                title = "张三",
                subtitle = "zhangsan@example.com",
                leadingIcon = Icons.Default.AccountCircle,
                variant = DSListItemVariant.TwoLine,
                onClick = {}
            )
            DSListItem(
                title = "李四",
                subtitle = "lisi@example.com",
                leadingIcon = Icons.Default.AccountCircle,
                variant = DSListItemVariant.TwoLine,
                onClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "ListItem - Three Line")
@Composable
private fun DSListItemThreeLinePreview() {
    DSDesignTheme {
        Column {
            DSListItem(
                title = "Jetpack Compose 1.3.0",
                subtitle = "新增 ListItem、SearchBar 稳定 API，建议升级",
                overline = "新版本发布",
                variant = DSListItemVariant.ThreeLine,
                trailingContent = {
                    Text(
                        text = "3天前",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            DSListItem(
                title = "Android Studio Koala",
                subtitle = "智能编辑器性能改进，新增设备镜像",
                overline = "工具更新",
                variant = DSListItemVariant.ThreeLine,
                trailingContent = {
                    Text(
                        text = "1周前",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true, name = "ListItem - Custom Leading Content")
@Composable
private fun DSListItemCustomLeadingPreview() {
    DSDesignTheme {
        DSListItem(
            title = "未读消息",
            subtitle = "5 条新消息",
            variant = DSListItemVariant.TwoLine,
            leadingIcon = Icons.Default.Email,
            trailingContent = {
                Text(
                    text = "查看",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        )
    }
}
