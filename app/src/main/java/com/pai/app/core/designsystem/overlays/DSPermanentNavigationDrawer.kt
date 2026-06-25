// ============================================================================
// DSPermanentNavigationDrawer.kt
// 常驻抽屉 - 基于 M3 PermanentNavigationDrawer + PermanentDrawerSheet
// 适用于平板 / 桌面端：抽屉常驻显示，不遮挡主内容
// ============================================================================

package com.pai.app.core.designsystem.overlays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSNavItem
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSPermanentNavigationDrawer - 常驻抽屉（桌面 / 平板）
 *
 * 使用示例：
 * ```kotlin
 * val items = listOf(
 *     DSNavItem(label = "首页", icon = Icons.Default.Home),
 *     DSNavItem(label = "收件箱", icon = Icons.Default.Inbox, badgeText = "12"),
 *     DSNavItem(label = "设置", icon = Icons.Default.Settings)
 * )
 * var selected by remember { mutableStateOf("首页") }
 *
 * DSPermanentNavigationDrawer(
 *     items = items,
 *     selectedItem = selected,
 *     onItemClick = { item -> selected = item.label },
 *     header = {
 *         Column(Modifier.padding(16.dp)) {
 *             Text("我的应用", style = MaterialTheme.typography.titleLarge)
 *         }
 *     }
 * ) {
 *     MainScreen()
 * }
 * ```
 *
 * 设计规范：
 * - 基于 M3 PermanentNavigationDrawer + PermanentDrawerSheet，抽屉常驻左侧
 * - 抽屉宽度跟随 M3 默认（360dp），适用于 WindowWidthSizeClass.Expanded
 * - 每项使用 M3 NavigationDrawerItem，选中态 secondaryContainer 指示器
 * - 选中匹配依据：item.label == selectedItem
 * - header slot 由业务自定义（如应用 Logo / 用户信息），为 null 时不渲染
 * - 仅在展开宽度（平板 / 桌面）使用；手机端请改用 DSModalNavigationDrawer
 *
 * @param items 抽屉项列表
 * @param selectedItem 当前选中项的 label，null 表示无选中
 * @param onItemClick 点击回调，传入被点击的 DSNavItem
 * @param modifier 修饰符
 * @param header 可选 header 插槽（应用 Logo / 用户信息等），渲染于抽屉顶部
 * @param content 主屏幕内容，由 PermanentNavigationDrawer 包装
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSPermanentNavigationDrawer(
    items: List<DSNavItem>,
    selectedItem: String?,
    onItemClick: (DSNavItem) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
) {
    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            PermanentDrawerSheet {
                // ----------------------------------------------------------------
                // header 插槽
                // ----------------------------------------------------------------
                if (header != null) {
                    header()
                }

                // ----------------------------------------------------------------
                // 导航项列表
                // ----------------------------------------------------------------
                Column(
                    modifier = Modifier.padding(
                        horizontal = DSTokens.Spacing.md,
                        vertical = DSTokens.Spacing.sm
                    ),
                    verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)
                ) {
                    items.forEach { item ->
                        val isSelected = item.label == selectedItem
                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = item.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.SemiBold
                                                 else FontWeight.Normal
                                )
                            },
                            selected = isSelected,
                            onClick = { onItemClick(item) },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null
                                )
                            },
                            badge = item.badgeText?.let { badgeText ->
                                {
                                    Badge { Text(text = badgeText) }
                                }
                            },
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }
        },
        content = content
    )
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, widthDp = 840, name = "DSPermanentNavigationDrawer - 平板布局")
@Composable
private fun DSPermanentNavigationDrawerPreview() {
    DSDesignTheme {
        val items = listOf(
            DSNavItem(
                icon = Icons.Default.Home,
                label = "首页"
            ),
            DSNavItem(
                icon = Icons.Default.Inbox,
                label = "收件箱",
                badgeText = "12"
            ),
            DSNavItem(
                icon = Icons.Default.Email,
                label = "已发送",
                badgeText = "3"
            ),
            DSNavItem(
                icon = Icons.Default.Settings,
                label = "设置"
            )
        )
        DSPermanentNavigationDrawer(
            items = items,
            selectedItem = "收件箱",
            onItemClick = {},
            header = {
                Column(
                    modifier = Modifier.padding(
                        horizontal = DSTokens.Spacing.lg,
                        vertical = DSTokens.Spacing.md
                    )
                ) {
                    Text(
                        text = "我的应用",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "v1.0.0",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "主内容区域",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 840, name = "DSPermanentNavigationDrawer - 无 header")
@Composable
private fun DSPermanentNavigationDrawerNoHeaderPreview() {
    DSDesignTheme {
        DSPermanentNavigationDrawer(
            items = listOf(
                DSNavItem(icon = Icons.Default.Home, label = "首页"),
                DSNavItem(icon = Icons.Default.Settings, label = "设置")
            ),
            selectedItem = "首页",
            onItemClick = {}
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "主内容",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
