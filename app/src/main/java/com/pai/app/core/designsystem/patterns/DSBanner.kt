// ============================================================================
// DSBanner.kt
// 顶部横幅提示 - 自定义实现（M3 1.3.0 暂无独立 Banner 组件）
// 支持 Info / Warning / Error / Success 四种类型
// 布局：左侧图标 + 中间文案 + 右侧可选操作 + 关闭按钮
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.patterns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.a11y.minTouchTarget
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.theme.extendedColors
import com.pai.app.core.designsystem.foundation.tokens.DSMessageType
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * Banner 类型别名 - 统一使用 [DSMessageType]
 *
 * - Info: 信息提示（天蓝色）
 * - Warning: 警告提示（琥珀色）
 * - Error: 错误提示（玫瑰红）
 * - Success: 成功提示（翡翠绿）
 */
typealias DSBannerType = DSMessageType

/**
 * DSBanner - 顶部横幅提示
 *
 * 使用示例：
 * ```kotlin
 * DSBanner(
 *     message = "网络连接已断开，请检查网络设置",
 *     type = DSMessageType.Warning,
 *     actionText = "重试",
 *     onActionClick = { retry() },
 *     onDismiss = { hideBanner() }
 * )
 * ```
 *
 * 设计规范：
 * - 圆角 small (8dp)，背景为类型色 12% alpha 半透明
 * - 左侧实色图标（IconSize.sm = 20dp），颜色为类型主色
 * - 中间文案 bodyMedium + Medium，颜色 onSurface
 * - 右侧可选操作按钮（TextButton，labelLarge + SemiBold，颜色为类型主色）
 * - 最右侧可选关闭按钮（IconButton，IconSize.sm，颜色 onSurfaceVariant）
 * - 类型色映射：
 *   - Info -> extendedColors.info (Sky)
 *   - Warning -> extendedColors.warning (Amber)
 *   - Error -> colorScheme.error (Rose)
 *   - Success -> extendedColors.success (Emerald)
 *
 * @param modifier 修饰符
 * @param message 提示文案
 * @param type Banner 类型，默认 Info
 * @param actionText 可选操作按钮文案；为 null 时不显示操作按钮
 * @param onActionClick 操作按钮点击回调；actionText 与本参数同时非空时才显示按钮
 * @param onDismiss 关闭按钮点击回调；为 null 时不显示关闭按钮
 */
@Composable
internal fun DSBanner(
    modifier: Modifier = Modifier,
    message: String,
    type: DSBannerType = DSBannerType.Info,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    // 根据类型解析主色与图标
    val accentColor: Color
    val icon: ImageVector
    when (type) {
        DSBannerType.Info -> {
            accentColor = MaterialTheme.extendedColors.info
            icon = Icons.Default.Info
        }
        DSBannerType.Warning -> {
            accentColor = MaterialTheme.extendedColors.warning
            icon = Icons.Default.Warning
        }
        DSBannerType.Error -> {
            accentColor = MaterialTheme.colorScheme.error
            icon = Icons.Default.Error
        }
        DSBannerType.Success -> {
            accentColor = MaterialTheme.extendedColors.success
            icon = Icons.Default.CheckCircle
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DSTokens.Radius.small),
        // 半透明背景（类型色 overlay alpha，来自 DSTokens.Alpha.overlay）
        color = accentColor.copy(alpha = DSTokens.Alpha.overlay),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = DSTokens.Spacing.md,
                vertical = DSTokens.Spacing.sm
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
        ) {
            // 左侧类型图标
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(DSTokens.IconSize.sm)
            )

            // 中间文案
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            // 右侧操作按钮
            if (actionText != null && onActionClick != null) {
                TextButton(
                    onClick = onActionClick,
                    contentPadding = PaddingValues(
                        horizontal = DSTokens.Spacing.sm,
                        vertical = 0.dp
                    )
                ) {
                    Text(
                        text = actionText,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }
            }

            // 关闭按钮
            if (onDismiss != null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.minTouchTarget()
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(DSTokens.IconSize.sm)
                    )
                }
            }
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "DSBanner - 四种类型")
@Composable
private fun DSBannerAllTypesPreview() {
    DSDesignTheme {
        Column(
            modifier = Modifier.padding(DSTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
        ) {
            DSBanner(
                message = "新版本已发布，请更新以获取最新功能",
                type = DSBannerType.Info,
                actionText = "更新",
                onActionClick = {},
                onDismiss = {}
            )
            DSBanner(
                message = "存储空间不足，请清理缓存",
                type = DSBannerType.Warning,
                actionText = "清理",
                onActionClick = {},
                onDismiss = {}
            )
            DSBanner(
                message = "加载失败，请稍后重试",
                type = DSBannerType.Error,
                actionText = "重试",
                onActionClick = {},
                onDismiss = {}
            )
            DSBanner(
                message = "保存成功",
                type = DSBannerType.Success,
                onDismiss = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "DSBanner - 纯文案")
@Composable
private fun DSBannerPlainPreview() {
    DSDesignTheme {
        Column(
            modifier = Modifier.padding(DSTokens.Spacing.lg),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
        ) {
            DSBanner(
                message = "这是一条普通信息提示，无操作按钮也无关闭按钮",
                type = DSBannerType.Info
            )
            Spacer(modifier = Modifier.height(DSTokens.Spacing.xxs))
            DSBanner(
                message = "仅含关闭按钮的警告",
                type = DSBannerType.Warning,
                onDismiss = {}
            )
        }
    }
}
