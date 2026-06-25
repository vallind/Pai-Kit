// ============================================================================
// DSBadge.kt
// 徽标组件 - 数字 / 圆点 / 文本，可定位到锚点元素的四角
// 基于 M3 Badge 扩展，颜色采用 errorContainer
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 徽标类型
 * - Number: 数字徽标，count > 99 时显示 "99+"
 * - Dot: 8dp 圆点徽标，无内容
 * - Text: 文本徽标，自定义任意文字（如 "New", "Hot"）
 */
internal enum class DSBadgeType {
    Number,
    Dot,
    Text
}

/**
 * 徽标位置
 * - TopEnd: 锚点右上角（默认）
 * - BottomStart: 锚点左下角
 * - Inline: 不叠加在锚点上，作为行内元素独立渲染
 */
internal enum class DSBadgePosition {
    TopEnd,
    BottomStart,
    Inline
}

/**
 * DSBadge - 徽标组件
 *
 * 使用示例：
 * ```kotlin
 * // 行内数字徽标
 * DSBadge(count = 12, type = DSBadgeType.Number, position = DSBadgePosition.Inline)
 *
 * // 叠加在图标上的红点
 * DSBadge(
 *     type = DSBadgeType.Dot,
 *     position = DSBadgePosition.TopEnd
 * ) {
 *     Icon(Icons.Default.Notifications, contentDescription = "通知")
 * }
 *
 * // 叠加在图标上的数字（超过 99 显示 99+）
 * DSBadge(
 *     count = 120,
 *     type = DSBadgeType.Number,
 *     position = DSBadgePosition.TopEnd
 * ) {
 *     Icon(Icons.Default.Email, contentDescription = "消息")
 * }
 * ```
 *
 * 设计规范：
 * - Number：最小宽度 16dp，高度 16dp，圆角 full，字号 11sp
 * - Dot：8dp 圆点
 * - Text：内边距 horizontal 6dp / vertical 2dp，圆角 full
 * - 配色：errorContainer 背景 + onErrorContainer 文字
 * - 数字上限：count > 99 显示 "99+"，count <= 0 不渲染
 * - 叠加位置：TopEnd 偏移 (0, -4dp)，BottomStart 偏移 (-4dp, 0)
 *
 * @param modifier 修饰符
 * @param type 徽标类型，默认 Number
 * @param count 数字（仅 Number 类型生效）
 * @param text 文本（仅 Text 类型生效）
 * @param position 位置，默认 Inline
 * @param content 锚点内容；非空且 position != Inline 时叠加显示
 */
@Composable
internal fun DSBadge(
    modifier: Modifier = Modifier,
    type: DSBadgeType = DSBadgeType.Number,
    count: Int = 0,
    text: String = "",
    position: DSBadgePosition = DSBadgePosition.Inline,
    content: (@Composable () -> Unit)? = null
) {
    val containerColor = MaterialTheme.colorScheme.errorContainer
    val contentColor = MaterialTheme.colorScheme.onErrorContainer

    // 徽标内容 Composable
    val badgeContent: @Composable () -> Unit = {
        when (type) {
            DSBadgeType.Number -> {
                if (count > 0) {
                    Badge(
                        containerColor = containerColor,
                        contentColor = contentColor
                    ) {
                        Text(
                            text = if (count > 99) "99+" else count.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            DSBadgeType.Dot -> {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(containerColor)
                )
            }
            DSBadgeType.Text -> {
                if (text.isNotEmpty()) {
                    Badge(
                        containerColor = containerColor,
                        contentColor = contentColor
                    ) {
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // 行内模式：直接渲染徽标
    if (position == DSBadgePosition.Inline || content == null) {
        Box(modifier = modifier) {
            badgeContent()
        }
        return
    }

    // 叠加模式：渲染锚点内容 + 徽标定位
    val badgeAlignment = when (position) {
        DSBadgePosition.TopEnd -> Alignment.TopEnd
        DSBadgePosition.BottomStart -> Alignment.BottomStart
        DSBadgePosition.Inline -> Alignment.TopEnd
    }

    Box(modifier = modifier) {
        content()
        Box(
            modifier = Modifier
                .align(badgeAlignment)
                .then(
                    when (position) {
                        DSBadgePosition.TopEnd -> Modifier.offset(x = 4.dp, y = (-4).dp)
                        DSBadgePosition.BottomStart -> Modifier.offset(x = (-4).dp, y = 4.dp)
                        DSBadgePosition.Inline -> Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            badgeContent()
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "Badge - Inline Number")
@Composable
private fun DSBadgeInlineNumberPreview() {
    DSDesignTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSBadge(count = 1, type = DSBadgeType.Number, position = DSBadgePosition.Inline)
            DSBadge(count = 12, type = DSBadgeType.Number, position = DSBadgePosition.Inline)
            DSBadge(count = 99, type = DSBadgeType.Number, position = DSBadgePosition.Inline)
            DSBadge(count = 120, type = DSBadgeType.Number, position = DSBadgePosition.Inline)
        }
    }
}

@Preview(showBackground = true, name = "Badge - Inline Dot & Text")
@Composable
private fun DSBadgeInlineDotAndTextPreview() {
    DSDesignTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSBadge(type = DSBadgeType.Dot, position = DSBadgePosition.Inline)
            DSBadge(text = "New", type = DSBadgeType.Text, position = DSBadgePosition.Inline)
            DSBadge(text = "Hot", type = DSBadgeType.Text, position = DSBadgePosition.Inline)
        }
    }
}

@Preview(showBackground = true, name = "Badge - TopEnd on Icon")
@Composable
private fun DSBadgeTopEndOnIconPreview() {
    DSDesignTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 数字徽标叠加
            DSBadge(
                count = 5,
                type = DSBadgeType.Number,
                position = DSBadgePosition.TopEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "消息",
                    modifier = Modifier.size(DSTokens.IconSize.lg)
                )
            }
            // 红点徽标叠加
            DSBadge(
                type = DSBadgeType.Dot,
                position = DSBadgePosition.TopEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "通知",
                    modifier = Modifier.size(DSTokens.IconSize.lg)
                )
            }
            // 文本徽标叠加
            DSBadge(
                text = "New",
                type = DSBadgeType.Text,
                position = DSBadgePosition.TopEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "消息",
                    modifier = Modifier.size(DSTokens.IconSize.lg)
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Badge - BottomStart on Icon")
@Composable
private fun DSBadgeBottomStartOnIconPreview() {
    DSDesignTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSBadge(
                count = 3,
                type = DSBadgeType.Number,
                position = DSBadgePosition.BottomStart
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "消息",
                    modifier = Modifier.size(DSTokens.IconSize.lg)
                )
            }
            DSBadge(
                type = DSBadgeType.Dot,
                position = DSBadgePosition.BottomStart
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "通知",
                    modifier = Modifier.size(DSTokens.IconSize.lg)
                )
            }
        }
    }
}
