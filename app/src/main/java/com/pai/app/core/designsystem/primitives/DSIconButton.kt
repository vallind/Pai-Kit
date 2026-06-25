// ============================================================================
// DSIconButton.kt
// 图标按钮 - 仅显示图标的按钮，用于工具栏、卡片操作等
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.a11y.minTouchTarget
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 图标按钮风格
 */
internal enum class DSIconButtonStyle {
    Standard,    // 透明背景
    Filled,      // 实心
    Tonal,       // 浅色填充
    Outlined     // 描边
}

/**
 * DSIconButton - 图标按钮
 *
 * 使用示例：
 * ```kotlin
 * DSIconButton(
 *     icon = Icons.Default.Favorite,
 *     contentDescription = "收藏",
 *     onClick = { onFavorite() },
 *     style = DSIconButtonStyle.Tonal
 * )
 * ```
 *
 * 设计规范：
 * - 默认尺寸 48dp × 48dp（最小触控目标）
 * - 图标尺寸 24dp
 * - 圆形 Shape
 *
 * @param icon 图标
 * @param contentDescription 无障碍描述（必填，TalkBack 朗读）
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param style 风格
 * @param enabled 是否可用
 */
@Composable
internal fun DSIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: DSIconButtonStyle = DSIconButtonStyle.Standard,
    enabled: Boolean = true
) {
    val iconSize = DSTokens.IconSize.md
    val buttonModifier = modifier.minTouchTarget()

    when (style) {
        DSIconButtonStyle.Standard -> {
            IconButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        DSIconButtonStyle.Filled -> {
            FilledIconButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = CircleShape,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
        DSIconButtonStyle.Tonal -> {
            FilledTonalIconButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = CircleShape,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
        DSIconButtonStyle.Outlined -> {
            OutlinedIconButton(
                onClick = onClick,
                modifier = buttonModifier,
                enabled = enabled,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(iconSize),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DSIconButtonPreview() {
    DSDesignTheme {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            DSIconButton(
                icon = Icons.Default.Add,
                contentDescription = "添加",
                onClick = {},
                style = DSIconButtonStyle.Standard
            )
            DSIconButton(
                icon = Icons.Default.Add,
                contentDescription = "添加",
                onClick = {},
                style = DSIconButtonStyle.Filled
            )
            DSIconButton(
                icon = Icons.Default.Add,
                contentDescription = "添加",
                onClick = {},
                style = DSIconButtonStyle.Tonal
            )
            DSIconButton(
                icon = Icons.Default.Add,
                contentDescription = "添加",
                onClick = {},
                style = DSIconButtonStyle.Outlined
            )
        }
    }
}
