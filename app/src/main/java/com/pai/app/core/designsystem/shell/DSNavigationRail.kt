// ============================================================================
// DSNavigationRail.kt
// 侧边导航栏 - 基于 M3 NavigationRail 扩展
// 用于平板/桌面端（Compact 之外的窗口尺寸类），左侧常驻 3~7 个导航项
// M3 卓越线补齐：完整导航组件覆盖（NavigationBar 已有 + NavigationRail 新增 + Drawer 已有）
// 与 DSBottomBar 互斥：Compact 用 BottomBar，Medium/Expanded 用 NavigationRail
// ============================================================================

package com.pai.app.core.designsystem.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSNavItem
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSNavigationRail - 侧边导航栏
 *
 * 与 [DSBottomBar] 互斥使用：
 * - Compact 窗口尺寸类（手机竖屏 < 600dp）→ 用 [DSBottomBar]
 * - Medium / Expanded 窗口尺寸类（平板/折叠屏/桌面）→ 用 [DSNavigationRail]
 *
 * 使用示例：
 * ```kotlin
 * val items = listOf(
 *     DSNavItem(label = "首页", icon = Icons.Default.Home),
 *     DSNavItem(label = "搜索", icon = Icons.Default.Search),
 *     DSNavItem(label = "我的", icon = Icons.Default.Person, badgeText = "3")
 * )
 * var selected by remember { mutableStateOf("首页") }
 * DSNavigationRail(
 *     items = items,
 *     selectedItem = selected,
 *     onItemSelected = { selected = it },
 *     onMenuClick = { drawerState.open() },
 *     headerLabel = "Pai"
 * )
 * ```
 *
 * 设计规范：
 * - 宽度 80dp（M3 标准）
 * - 选中态：primary 容器 + onPrimaryContainer 内容 + 指示器 pill
 * - 未选中态：surfaceVariant 容器 + onSurfaceVariant 内容
 * - 顶部可放菜单图标（汉堡）+ 可选 FAB
 * - 推荐项数 3~7 个
 *
 * @param items 导航项列表
 * @param selectedItem 当前选中项的 label
 * @param onItemSelected 选中项变化回调，参数为 label
 * @param modifier 修饰符
 * @param onMenuClick 顶部菜单图标点击回调（通常用于打开 ModalDrawer）
 * @param fabIcon 可选 FAB 图标，非空时显示在顶部
 * @param fabContentDescription FAB 无障碍描述
 * @param onFabClick FAB 点击回调
 * @param headerLabel 可选头部标签（如品牌名），显示在菜单图标下方
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSNavigationRail(
    items: List<DSNavItem>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null,
    fabIcon: ImageVector? = null,
    fabContentDescription: String? = null,
    onFabClick: (() -> Unit)? = null,
    headerLabel: String? = null
) {
    val colors = NavigationRailItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
        selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    NavigationRail(
        modifier = modifier.fillMaxHeight(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        header = {
            // 顶部区域：菜单图标 + 可选品牌标签 + 可选 FAB
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
            ) {
                if (onMenuClick != null) {
                    IconButton(onClick = onMenuClick) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "打开导航抽屉",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (headerLabel != null) {
                    Text(
                        text = headerLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (fabIcon != null && onFabClick != null) {
                    FloatingActionButton(
                        onClick = onFabClick,
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(DSTokens.Radius.medium),
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        elevation = FloatingActionButtonDefaults.elevation(
                            defaultElevation = DSTokens.Elevation.level1,
                            pressedElevation = DSTokens.Elevation.level0
                        )
                    ) {
                        Icon(
                            imageVector = fabIcon,
                            contentDescription = fabContentDescription,
                            modifier = Modifier.size(DSTokens.IconSize.md)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(DSTokens.Spacing.sm))
            }
        }
    ) {
        items.forEach { item ->
            NavigationRailItem(
                selected = item.label == selectedItem,
                onClick = { onItemSelected(item.label) },
                icon = {
                    if (item.badgeText != null) {
                        BadgedBox(badge = { Badge { Text(item.badgeText) } }) {
                            Icon(
                                imageVector = if (item.label == selectedItem) item.selectedIcon ?: item.icon
                                else item.icon,
                                contentDescription = item.label
                            )
                        }
                    } else {
                        Icon(
                            imageVector = if (item.label == selectedItem) item.selectedIcon ?: item.icon
                            else item.icon,
                            contentDescription = item.label
                        )
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                colors = colors,
                alwaysShowLabel = true
            )
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "NavigationRail - Default", widthDp = 120, heightDp = 640)
@Composable
private fun DSNavigationRailDefaultPreview() {
    DSDesignTheme {
        val items = listOf(
            DSNavItem(icon = Icons.Default.Home, label = "首页"),
            DSNavItem(icon = Icons.Default.Person, label = "我的", badgeText = "3"),
            DSNavItem(icon = Icons.Default.Settings, label = "设置")
        )
        var selected by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf("首页") }
        DSNavigationRail(
            items = items,
            selectedItem = selected,
            onItemSelected = { selected = it },
            onMenuClick = {},
            headerLabel = "Pai"
        )
    }
}

@Preview(showBackground = true, name = "NavigationRail - With FAB", widthDp = 120, heightDp = 640)
@Composable
private fun DSNavigationRailWithFabPreview() {
    DSDesignTheme {
        val items = listOf(
            DSNavItem(icon = Icons.Default.Home, label = "首页"),
            DSNavItem(icon = Icons.Default.Person, label = "我的"),
            DSNavItem(icon = Icons.Default.Settings, label = "设置")
        )
        DSNavigationRail(
            items = items,
            selectedItem = "首页",
            onItemSelected = {},
            onMenuClick = {},
            fabIcon = Icons.Default.Add,
            fabContentDescription = "新建",
            onFabClick = {}
        )
    }
}

@Preview(showBackground = true, name = "NavigationRail - Compact", widthDp = 100, heightDp = 480)
@Composable
private fun DSNavigationRailCompactPreview() {
    DSDesignTheme {
        val items = listOf(
            DSNavItem(icon = Icons.Default.Home, label = "首页"),
            DSNavItem(icon = Icons.Default.Settings, label = "设置")
        )
        DSNavigationRail(
            items = items,
            selectedItem = "首页",
            onItemSelected = {}
        )
    }
}
