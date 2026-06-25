// ============================================================================
// DSNavItem.kt
// 统一导航项数据类 - 5 个导航组件共用（BottomBar / NavigationRail / ModalDrawer / PermanentDrawer / Tabs / AnimatedBottomBar）
// 替代旧的 DSBottomNavItem / DSRailItem / DSDrawerItem / DSTabItem / DSAnimatedBottomNavItem 5 个相似但不互通的类
// ============================================================================

package com.pai.app.core.designsystem.foundation.tokens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * DSNavItem - 统一导航项数据
 *
 * 用于 [DSBottomBar] / [DSNavigationRail] / [DSModalNavigationDrawer] /
 * [DSPermanentNavigationDrawer] / [DSTabRow] / [DSTabsWithPager] 等所有导航组件。
 *
 * 替代旧的 5 个相似类：
 * - DSBottomNavItem(icon, selectedIcon, label, badgeText)
 * - DSRailItem(icon, selectedIcon, label, badgeText)
 * - DSDrawerItem(icon, label, badge)
 * - DSTabItem(title, icon, badge)
 * - DSAnimatedBottomNavItem(icon, label)
 *
 * 使用示例：
 * ```kotlin
 * val items = listOf(
 *     DSNavItem(label = "首页", icon = Icons.Default.Home),
 *     DSNavItem(label = "发现", icon = Icons.Default.Search, badgeText = "5"),
 *     DSNavItem(label = "我的", icon = Icons.Default.Person, selectedIcon = Icons.Default.Person)
 * )
 * var selected by remember { mutableStateOf("首页") }
 * DSBottomBar(items = items, selectedItem = selected, onItemSelected = { selected = it })
 * ```
 *
 * @param label 标签文案（同时作为 selectedItem 的匹配键）
 * @param icon 未选中态图标
 * @param selectedIcon 选中态图标（可选，未提供时复用 icon）
 * @param badgeText 可选徽标文案（如未读数 "5"），为 null 时不显示
 * @param id 用于 selectedItem 匹配的标识符，默认 = label
 */
@Stable
data class DSNavItem(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector? = null,
    val badgeText: String? = null,
    val id: String = label
)
