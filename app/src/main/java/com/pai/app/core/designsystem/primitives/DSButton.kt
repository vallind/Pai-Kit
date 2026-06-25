// ============================================================================
// DSButton.kt
// 按钮组件 - 基于 M3 Button 扩展
// 提供 6 种风格：Filled / Tonal / Outlined / Text / Elevated / Error
// 提供 3 种尺寸：Small / Medium / Large
// M3 卓越线补齐：增加 Elevated 变体（M3 标准 5 种按钮之一，带阴影的填充按钮）
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 按钮风格
 * - Filled：主操作（CTA），高强调
 * - Tonal：次主操作，中强调（M3 Filled Tonal Button）
 * - Outlined：辅助操作，低强调
 * - Text：文本操作，最低强调
 * - Elevated：带阴影的填充按钮，用于需要突出的场景（M3 Elevated Button）
 * - Error：危险操作（删除/撤销）
 */
internal enum class DSButtonStyle {
    Filled,
    Tonal,
    Outlined,
    Text,
    Elevated,
    Error
}

/**
 * 按钮尺寸
 * - Small：32dp 高，用于紧凑空间
 * - Medium：40dp 高，默认尺寸
 * - Large：48dp 高，用于 CTA / 主操作
 */
internal enum class DSButtonSize {
    Small,
    Medium,
    Large
}

/**
 * DSButton - 通用按钮组件
 *
 * 使用示例：
 * ```kotlin
 * DSButton(
 *     text = "确认提交",
 *     onClick = { viewModel.submit() },
 *     style = DSButtonStyle.Filled,
 *     size = DSButtonSize.Large,
 *     icon = Icons.Default.Check
 * )
 * ```
 *
 * 设计规范：
 * - 默认尺寸 Medium (40dp)
 * - 圆角 medium (12dp)
 * - 字号 Label Large (14sp)
 * - 内容水平间距 8dp
 * - 图标尺寸 18dp
 * - 触控目标至少 48dp（通过 minTouchTarget 保障）
 *
 * @param text 按钮文本
 * @param onClick 点击回调
 * @param modifier 修饰符
 * @param style 风格，默认 Filled
 * @param size 尺寸，默认 Medium
 * @param icon 可选前置图标
 * @param enabled 是否可用，默认 true
 * @param loading 是否加载中（false 时不显示 loading 指示）
 */
@Composable
internal fun DSButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: DSButtonStyle = DSButtonStyle.Filled,
    size: DSButtonSize = DSButtonSize.Medium,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    // 计算尺寸相关参数
    val height = when (size) {
        DSButtonSize.Small -> DSTokens.ComponentHeight.buttonSmall
        DSButtonSize.Medium -> DSTokens.ComponentHeight.buttonMedium
        DSButtonSize.Large -> DSTokens.ComponentHeight.buttonLarge
    }

    val horizontalPadding = when (size) {
        DSButtonSize.Small -> DSTokens.Spacing.sm
        DSButtonSize.Medium -> DSTokens.Spacing.md
        DSButtonSize.Large -> DSTokens.Spacing.lg
    }

    val iconSize = when (size) {
        DSButtonSize.Small -> DSTokens.IconSize.xs
        DSButtonSize.Medium -> DSTokens.IconSize.xs
        DSButtonSize.Large -> DSTokens.IconSize.sm
    }

    val contentPadding = PaddingValues(
        horizontal = horizontalPadding,
        vertical = DSTokens.Spacing.xs
    )

    val buttonShape = RoundedCornerShape(DSTokens.Radius.medium)
    val colors = when (style) {
        DSButtonStyle.Filled -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DSButtonStyle.Tonal -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        )
        DSButtonStyle.Outlined -> ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
        DSButtonStyle.Text -> ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.primary
        )
        DSButtonStyle.Elevated -> ButtonDefaults.elevatedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
        DSButtonStyle.Error -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError
        )
    }

    // 内容 Composable
    @Composable
    fun content() {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                // 加载中：显示圆形进度指示器
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(iconSize),
                    strokeWidth = DSTokens.Border.medium,
                    color = colors.contentColor
                )
            } else if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(iconSize)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }

    // 根据风格调用不同的 M3 Button
    when (style) {
        DSButtonStyle.Filled, DSButtonStyle.Error -> {
            Button(
                onClick = onClick,
                modifier = modifier
                    .height(height)
                    .minTouchTarget(),
                enabled = enabled,
                shape = buttonShape,
                colors = colors,
                contentPadding = contentPadding,
                content = { content() }
            )
        }
        DSButtonStyle.Tonal -> {
            FilledTonalButton(
                onClick = onClick,
                modifier = modifier
                    .height(height)
                    .minTouchTarget(),
                enabled = enabled,
                shape = buttonShape,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                contentPadding = contentPadding,
                content = { content() }
            )
        }
        DSButtonStyle.Elevated -> {
            ElevatedButton(
                onClick = onClick,
                modifier = modifier
                    .height(height)
                    .minTouchTarget(),
                enabled = enabled,
                shape = buttonShape,
                colors = colors,
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = DSTokens.Elevation.level1,
                    pressedElevation = DSTokens.Elevation.level0,
                    hoveredElevation = DSTokens.Elevation.level2,
                    focusedElevation = DSTokens.Elevation.level1,
                    disabledElevation = DSTokens.Elevation.level0
                ),
                contentPadding = contentPadding,
                content = { content() }
            )
        }
        DSButtonStyle.Outlined -> {
            OutlinedButton(
                onClick = onClick,
                modifier = modifier
                    .height(height)
                    .minTouchTarget(),
                enabled = enabled,
                shape = buttonShape,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = contentPadding,
                content = { content() }
            )
        }
        DSButtonStyle.Text -> {
            TextButton(
                onClick = onClick,
                modifier = modifier
                    .height(height)
                    .minTouchTarget(),
                enabled = enabled,
                shape = buttonShape,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = contentPadding,
                content = { content() }
            )
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "Button - Filled")
@Composable
private fun DSButtonFilledPreview() {
    DSDesignTheme {
        DSButton(
            text = "Filled",
            onClick = {},
            style = DSButtonStyle.Filled,
            icon = Icons.Default.Add
        )
    }
}

@Preview(showBackground = true, name = "Button - All Styles")
@Composable
private fun DSButtonAllStylesPreview() {
    DSDesignTheme {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DSButton(text = "Filled", onClick = {}, style = DSButtonStyle.Filled)
            DSButton(text = "Elevated", onClick = {}, style = DSButtonStyle.Elevated)
            DSButton(text = "Tonal", onClick = {}, style = DSButtonStyle.Tonal)
            DSButton(text = "Outlined", onClick = {}, style = DSButtonStyle.Outlined)
            DSButton(text = "Text", onClick = {}, style = DSButtonStyle.Text)
            DSButton(text = "Error", onClick = {}, style = DSButtonStyle.Error)
        }
    }
}

@Preview(showBackground = true, name = "Button - All Sizes", widthDp = 320)
@Composable
private fun DSButtonAllSizesPreview() {
    DSDesignTheme {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DSButton(text = "Small", onClick = {}, size = DSButtonSize.Small)
            DSButton(text = "Medium", onClick = {}, size = DSButtonSize.Medium)
            DSButton(text = "Large", onClick = {}, size = DSButtonSize.Large)
        }
    }
}

@Preview(showBackground = true, name = "Button - Disabled")
@Composable
private fun DSButtonDisabledPreview() {
    DSDesignTheme {
        DSButton(text = "Disabled", onClick = {}, enabled = false)
    }
}

@Preview(showBackground = true, name = "Button - Loading")
@Composable
private fun DSButtonLoadingPreview() {
    DSDesignTheme {
        DSButton(text = "Submitting", onClick = {}, loading = true)
    }
}
