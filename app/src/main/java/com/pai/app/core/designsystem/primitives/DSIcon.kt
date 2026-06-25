// ============================================================================
// DSIcon.kt
// 图标组件 - 统一图标尺寸、色调
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.theme.extendedColors
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 图标尺寸
 * - XSmall: 16dp (状态图标)
 * - Small: 20dp (按钮内图标)
 * - Medium: 24dp (默认)
 * - Large: 32dp
 * - XLarge: 48dp (空状态)
 */
internal enum class DSIconSize(val dp: androidx.compose.ui.unit.Dp) {
    XSmall(DSTokens.IconSize.xs),
    Small(DSTokens.IconSize.sm),
    Medium(DSTokens.IconSize.md),
    Large(DSTokens.IconSize.lg),
    XLarge(DSTokens.IconSize.xl)
}

/**
 * 图标色调
 * - Default: 跟随主题 onSurface
 * - Primary: 主色
 * - OnPrimary: onPrimary
 * - Secondary: 次要色
 * - Error: 错误色
 * - Success: 成功色
 * - Warning: 警告色
 * - Info: 信息色
 * - Disabled: 禁用色
 */
internal enum class DSIconTint {
    Default,
    Primary,
    OnPrimary,
    Secondary,
    Error,
    Success,
    Warning,
    Info,
    Disabled
}

/**
 * DSIcon - 统一图标组件
 *
 * 使用示例：
 * ```kotlin
 * DSIcon(
 *     imageVector = Icons.Default.Add,
 *     contentDescription = "添加",
 *     size = DSIconSize.Medium,
 *     tint = DSIconTint.Primary
 * )
 * ```
 *
 * 设计规范：
 * - 默认尺寸 24dp
 * - 默认色调 OnSurface
 * - 必须提供 contentDescription 用于无障碍
 *
 * @param imageVector 图标资源
 * @param contentDescription 无障碍描述（若图标纯装饰性可传 null）
 * @param modifier 修饰符
 * @param size 尺寸，默认 Medium
 * @param tint 色调，默认 Default
 */
@Composable
internal fun DSIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: DSIconSize = DSIconSize.Medium,
    tint: DSIconTint = DSIconTint.Default
) {
    val tintColor = when (tint) {
        DSIconTint.Default -> MaterialTheme.colorScheme.onSurface
        DSIconTint.Primary -> MaterialTheme.colorScheme.primary
        DSIconTint.OnPrimary -> MaterialTheme.colorScheme.onPrimary
        DSIconTint.Secondary -> MaterialTheme.colorScheme.secondary
        DSIconTint.Error -> MaterialTheme.colorScheme.error
        DSIconTint.Success -> MaterialTheme.extendedColors.success
        DSIconTint.Warning -> MaterialTheme.extendedColors.warning
        DSIconTint.Info -> MaterialTheme.extendedColors.info
        DSIconTint.Disabled -> MaterialTheme.colorScheme.outline
    }

    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = tintColor,
        modifier = modifier.size(size.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun DSIconPreview() {
    DSDesignTheme {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            DSIcon(
                imageVector = Icons.Filled.Add,
                contentDescription = "添加",
                size = DSIconSize.XSmall
            )
            DSIcon(
                imageVector = Icons.Filled.Add,
                contentDescription = "添加",
                size = DSIconSize.Small
            )
            DSIcon(
                imageVector = Icons.Filled.Add,
                contentDescription = "添加",
                size = DSIconSize.Medium,
                tint = DSIconTint.Primary
            )
            DSIcon(
                imageVector = Icons.Filled.Add,
                contentDescription = "添加",
                size = DSIconSize.Large,
                tint = DSIconTint.Error
            )
        }
    }
}
