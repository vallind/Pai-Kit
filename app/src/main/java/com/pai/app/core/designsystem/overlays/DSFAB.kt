// ============================================================================
// DSFAB.kt
// 悬浮按钮 (Floating Action Button) - 基于 M3 FloatingActionButton / ExtendedFloatingActionButton
// 提供 3 种风格：Small / Large / Extended
// Extended 风格支持 collapsed 参数控制展开/收起（适合列表滚动时收起节省空间）
// ============================================================================

package com.pai.app.core.designsystem.overlays

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.a11y.minTouchTarget
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * FAB 风格
 * - Small: 40dp × 40dp，紧凑型，用于次要操作
 * - Large: 56dp × 56dp，标准尺寸，默认推荐
 * - Extended: 文字 + 图标横向排列，支持 [collapsed] 收起为仅图标
 */
internal enum class DSFABStyle {
    Small,
    Large,
    Extended
}

/**
 * DSFAB - 悬浮按钮
 *
 * 使用示例：
 * ```kotlin
 * // 标准 Large FAB
 * DSFAB(
 *     onClick = { viewModel.createNew() },
 *     icon = Icons.Default.Add,
 *     contentDescription = "新建",
 *     style = DSFABStyle.Large
 * )
 *
 * // 扩展型（带文字）
 * DSFAB(
 *     onClick = { /* ... */ },
 *     icon = Icons.Default.Edit,
 *     contentDescription = "撰写",
 *     style = DSFABStyle.Extended,
 *     text = "撰写"
 * )
 *
 * // 可收起扩展型（配合 LazyColumn 滚动状态切换）
 * val listState = rememberLazyListState()
 * val collapsed by remember {
 *     derivedStateOf { listState.firstVisibleItemIndex > 0 }
 * }
 * DSFAB(
 *     onClick = { /* ... */ },
 *     icon = Icons.Default.Edit,
 *     contentDescription = "撰写",
 *     style = DSFABStyle.Extended,
 *     text = "撰写",
 *     collapsed = collapsed
 * )
 * ```
 *
 * 设计规范：
 * - Small：40dp × 40dp，圆形，图标 24dp
 * - Large：56dp × 56dp，圆形，图标 24dp
 * - Extended：高度 56dp，圆角 16dp，图标 24dp + 间距 8dp + 文字
 *   - collapsed = false（默认）：显示图标 + 文字
 *   - collapsed = true：仅显示图标，视觉宽度收缩为 56dp 圆角矩形
 *   - 切换动画：300ms emphasized 缓动
 * - 配色：primaryContainer 背景 + onPrimaryContainer 内容
 * - 海拔：默认 level3，按下 level6
 * - 必须包含 contentDescription 用于 TalkBack 朗读
 *
 * @param onClick 点击回调
 * @param icon 图标
 * @param contentDescription 无障碍描述（必填，TalkBack 朗读）
 * @param modifier 修饰符
 * @param style 风格，默认 Large
 * @param text 仅在 Extended 风格下生效的文本
 * @param collapsed 仅在 Extended 风格下生效，true 时仅显示图标（用于滚动时收起）
 */
@Composable
internal fun DSFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    style: DSFABStyle = DSFABStyle.Large,
    text: String? = null,
    collapsed: Boolean = false
) {
    val containerColor = MaterialTheme.colorScheme.primaryContainer
    val contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    val elevation = FloatingActionButtonDefaults.elevation(
        defaultElevation = DSTokens.Elevation.level3,
        pressedElevation = DSTokens.Elevation.level6
    )
    val iconSize = DSTokens.IconSize.md

    when (style) {
        DSFABStyle.Small -> FloatingActionButton(
            onClick = onClick,
            modifier = modifier.minTouchTarget(),
            shape = CircleShape,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = elevation
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
        }

        DSFABStyle.Large -> FloatingActionButton(
            onClick = onClick,
            modifier = modifier.minTouchTarget(),
            shape = CircleShape,
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = elevation
        ) {
            Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
        }

        DSFABStyle.Extended -> ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier.minTouchTarget(),
            shape = RoundedCornerShape(DSTokens.Radius.large),
            containerColor = containerColor,
            contentColor = contentColor,
            elevation = elevation,
            icon = {
                Icon(imageVector = icon, contentDescription = contentDescription, modifier = Modifier.size(iconSize))
            },
            text = {
                AnimatedVisibility(
                    visible = !collapsed && text != null,
                    enter = expandHorizontally() + fadeIn(),
                    exit = shrinkHorizontally() + fadeOut()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        text?.let {
                            Text(text = it, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        )
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "FAB - Small")
@Composable
private fun DSFABSmallPreview() {
    DSDesignTheme {
        DSFAB(onClick = {}, icon = Icons.Default.Add, contentDescription = "新建", style = DSFABStyle.Small)
    }
}

@Preview(showBackground = true, name = "FAB - Large")
@Composable
private fun DSFABLargePreview() {
    DSDesignTheme {
        DSFAB(onClick = {}, icon = Icons.Default.Add, contentDescription = "新建", style = DSFABStyle.Large)
    }
}

@Preview(showBackground = true, name = "FAB - Extended Expanded")
@Composable
private fun DSFABExtendedPreview() {
    DSDesignTheme {
        DSFAB(
            onClick = {},
            icon = Icons.Default.Add,
            contentDescription = "新建",
            style = DSFABStyle.Extended,
            text = "新建"
        )
    }
}

@Preview(showBackground = true, name = "FAB - Extended Collapsed")
@Composable
private fun DSFABExtendedCollapsedPreview() {
    DSDesignTheme {
        DSFAB(
            onClick = {},
            icon = Icons.Default.Add,
            contentDescription = "新建",
            style = DSFABStyle.Extended,
            text = "新建",
            collapsed = true
        )
    }
}

@Preview(showBackground = true, name = "FAB - All Styles", widthDp = 360)
@Composable
private fun DSFABAllStylesPreview() {
    DSDesignTheme {
        Row(
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSFAB(onClick = {}, icon = Icons.Default.Add, contentDescription = "新建", style = DSFABStyle.Small)
            DSFAB(onClick = {}, icon = Icons.Default.Add, contentDescription = "新建", style = DSFABStyle.Large)
            DSFAB(
                onClick = {},
                icon = Icons.Default.Add,
                contentDescription = "新建",
                style = DSFABStyle.Extended,
                text = "新建"
            )
        }
    }
}
