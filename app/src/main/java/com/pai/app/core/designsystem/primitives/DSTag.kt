// ============================================================================
// DSTag.kt
// 标签 - 比 Chip 更轻量的纯展示型彩色标签
// 半透明背景 + 实色文字，圆角 8dp
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.theme.extendedColors
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 标签颜色
 * - Default: surfaceVariant / onSurfaceVariant（中性灰）
 * - Primary: 主色（Indigo）
 * - Success: 成功色（Emerald）
 * - Warning: 警告色（Amber）
 * - Error:   错误色（Rose）
 * - Info:    信息色（Sky）
 * - Custom:  自定义色（需提供 customColor 参数）
 */
internal enum class DSTagColor {
    Default,
    Primary,
    Success,
    Warning,
    Error,
    Info,
    Custom
}

/**
 * 标签尺寸
 * - Small:  高度 20dp，字号 11sp
 * - Medium: 高度 24dp，字号 12sp
 */
internal enum class DSTagSize(val height: androidx.compose.ui.unit.Dp, val fontSize: androidx.compose.ui.unit.TextUnit) {
    Small(20.dp, 11.sp),
    Medium(24.dp, 12.sp)
}

/**
 * DSTag - 轻量彩色标签
 *
 * 与 Chip 区别：
 * - Tag 是纯展示型，不支持点击/选中
 * - 视觉更轻量：圆角 8dp + 半透明背景
 * - 主要用于状态标记（如「最新」「热门」「已通过」）
 *
 * 使用示例：
 * ```kotlin
 * // 主色标签
 * DSTag(text = "推荐", color = DSTagColor.Primary)
 *
 * // 成功状态标签（带图标）
 * DSTag(
 *     text = "已通过",
 *     color = DSTagColor.Success,
 *     icon = Icons.Default.Check,
 *     size = DSTagSize.Medium
 * )
 *
 * // 自定义颜色标签
 * DSTag(
 *     text = "活动",
 *     color = DSTagColor.Custom,
 *     customColor = Color(0xFF8B5CF6)
 * )
 * ```
 *
 * 设计规范：
 * - 圆角 8dp（DSTokens.Radius.small）
 * - 背景：前景色 12% 透明度
 * - 文字：前景色 100%
 * - 内边距 horizontal 8dp / vertical 2dp
 * - 图标尺寸 14dp（Small）/ 16dp（Medium），与文字间距 4dp
 * - 字重 Medium
 *
 * @param text 标签文字
 * @param modifier 修饰符
 * @param color 标签颜色，默认 Default
 * @param customColor 自定义颜色，仅 color == Custom 时生效
 * @param size 尺寸，默认 Medium
 * @param icon 可选图标
 */
@Composable
internal fun DSTag(
    text: String,
    modifier: Modifier = Modifier,
    color: DSTagColor = DSTagColor.Default,
    customColor: Color? = null,
    size: DSTagSize = DSTagSize.Medium,
    icon: ImageVector? = null
) {
    // 解析前景色
    val foregroundColor = when (color) {
        DSTagColor.Default -> MaterialTheme.colorScheme.onSurfaceVariant
        DSTagColor.Primary -> MaterialTheme.colorScheme.primary
        DSTagColor.Success -> MaterialTheme.extendedColors.success
        DSTagColor.Warning -> MaterialTheme.extendedColors.warning
        DSTagColor.Error -> MaterialTheme.colorScheme.error
        DSTagColor.Info -> MaterialTheme.extendedColors.info
        DSTagColor.Custom -> customColor ?: MaterialTheme.colorScheme.primary
    }

    // 半透明背景（前景色 12%）
    val backgroundColor = foregroundColor.copy(alpha = 0.12f)

    // 图标尺寸根据标签尺寸
    val iconSize = if (size == DSTagSize.Small) 14.dp else 16.dp

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(DSTokens.Radius.small))
            .background(backgroundColor)
            .height(size.height)
            .padding(horizontal = DSTokens.Spacing.sm, vertical = DSTokens.Spacing.xxs),
        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = foregroundColor,
                modifier = Modifier.size(iconSize)
            )
        }
        Text(
            text = text,
            color = foregroundColor,
            fontSize = size.fontSize,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "Tag - All Colors")
@Composable
private fun DSTagAllColorsPreview() {
    DSDesignTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSTag(text = "默认", color = DSTagColor.Default)
            DSTag(text = "主色", color = DSTagColor.Primary)
            DSTag(text = "成功", color = DSTagColor.Success)
            DSTag(text = "警告", color = DSTagColor.Warning)
            DSTag(text = "错误", color = DSTagColor.Error)
            DSTag(text = "信息", color = DSTagColor.Info)
            DSTag(
                text = "自定义",
                color = DSTagColor.Custom,
                customColor = Color(0xFF8B5CF6)
            )
        }
    }
}

@Preview(showBackground = true, name = "Tag - With Icon")
@Composable
private fun DSTagWithIconPreview() {
    DSDesignTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSTag(
                text = "已通过",
                color = DSTagColor.Success,
                icon = Icons.Default.Check
            )
            DSTag(
                text = "热门",
                color = DSTagColor.Error,
                icon = Icons.Default.LocalFireDepartment
            )
        }
    }
}

@Preview(showBackground = true, name = "Tag - Sizes Comparison")
@Composable
private fun DSTagSizesPreview() {
    DSDesignTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSTag(text = "Small", size = DSTagSize.Small, color = DSTagColor.Primary)
            DSTag(text = "Medium", size = DSTagSize.Medium, color = DSTagColor.Primary)
        }
    }
}
