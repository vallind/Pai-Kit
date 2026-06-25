// ============================================================================
// DSBottomBar.kt
// 底部导航栏 - 基于 M3 NavigationBar
// 合并了旧 DSAnimatedBottomBar 的动画能力（通过 animated 参数控制）
// ============================================================================

package com.pai.app.core.designsystem.shell

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.tokens.DSNavItem
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSBottomBar - 底部导航栏
 *
 * 使用示例：
 * ```kotlin
 * val items = listOf(
 *     DSNavItem(label = "首页", icon = Icons.Default.Home),
 *     DSNavItem(label = "发现", icon = Icons.Default.Search, badgeText = "5"),
 *     DSNavItem(label = "我的", icon = Icons.Default.Person)
 * )
 * var selected by remember { mutableStateOf("首页") }
 *
 * // 标准模式
 * DSBottomBar(
 *     items = items,
 *     selectedItem = selected,
 *     onItemSelected = { selected = it }
 * )
 *
 * // 动画模式（选中项指示器带 spring 动画 + 图标缩放）
 * DSBottomBar(
 *     items = items,
 *     selectedItem = selected,
 *     onItemSelected = { selected = it },
 *     animated = true
 * )
 * ```
 *
 * 设计规范：
 * - 高度 80dp（M3 NavigationBar 默认）
 * - 选中态：onSecondaryContainer 图标 + secondaryContainer 指示器
 * - 未选中：onSurfaceVariant 色
 * - 推荐项数 3~5 个
 * - animated = true 时：选中项图标 1.2x 放大 + spring 指示器过渡
 *
 * @param items 导航项列表
 * @param selectedItem 当前选中项的 id（默认 = label）
 * @param onItemSelected 选中项变化回调，参数为 [DSNavItem.id]
 * @param modifier 修饰符
 * @param animated 是否启用动画（spring 指示器 + 图标缩放），默认 false
 */
@Composable
internal fun DSBottomBar(
    items: List<DSNavItem>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    animated: Boolean = false
) {
    NavigationBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = DSTokens.Elevation.level3
    ) {
        items.forEach { item ->
            val isSelected = item.id == selectedItem

            // 动画模式：图标缩放
            val iconScale by animateFloatAsState(
                targetValue = if (isSelected && animated) 1.2f else 1.0f,
                animationSpec = if (animated) spring() else spring(dampingRatio = 1f, stiffness = 1f),
                label = "bottomBarIconScale"
            )

            NavigationBarItem(
                selected = isSelected,
                onClick = { onItemSelected(item.id) },
                icon = {
                    BadgedBox(badge = {
                        item.badgeText?.let {
                            Badge { Text(it) }
                        }
                    }) {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon ?: item.icon else item.icon,
                            contentDescription = item.label,
                            modifier = if (animated) Modifier.graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            } else Modifier
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "Bottom Bar - Standard")
@Composable
private fun DSBottomBarStandardPreview() {
    DSDesignTheme {
        DSBottomBar(
            items = listOf(
                DSNavItem(label = "首页", icon = Icons.Default.Home),
                DSNavItem(label = "发现", icon = Icons.Default.Search, badgeText = "5"),
                DSNavItem(label = "我的", icon = Icons.Default.Person)
            ),
            selectedItem = "首页",
            onItemSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "Bottom Bar - Animated")
@Composable
private fun DSBottomBarAnimatedPreview() {
    DSDesignTheme {
        DSBottomBar(
            items = listOf(
                DSNavItem(label = "首页", icon = Icons.Default.Home),
                DSNavItem(label = "发现", icon = Icons.Default.Search),
                DSNavItem(label = "我的", icon = Icons.Default.Person)
            ),
            selectedItem = "发现",
            onItemSelected = {},
            animated = true
        )
    }
}
