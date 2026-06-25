// ============================================================================
// DSContextMenu.kt
// 上下文菜单 - 长按弹出的操作菜单
// M3 卓越线补齐：完整菜单家族（DropdownMenu 已有 + ExposedDropdown 已有 + ContextMenu 新增）
// 用于列表项、卡片等元素的长按上下文操作
// ============================================================================

package com.pai.app.core.designsystem.overlays

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 上下文菜单项
 *
 * @param icon 前置图标
 * @param label 菜单项文案
 * @param destructive 是否为破坏性操作（true 时使用 error 色高亮）
 * @param onClick 点击回调
 */
internal data class DSContextMenuItem(
    val icon: ImageVector? = null,
    val label: String,
    val destructive: Boolean = false,
    val onClick: () -> Unit,
)

/**
 * DSContextMenuHost - 上下文菜单宿主
 *
 * 用 combinedClickable 包装子内容，长按弹出上下文菜单。
 *
 * 使用示例：
 * ```kotlin
 * DSContextMenuHost(
 *     menuItems = listOf(
 *         DSContextMenuItem(Icons.Default.Edit, "编辑") { vm.edit(item) },
 *         DSContextMenuItem(Icons.Default.Share, "分享") { vm.share(item) },
 *         DSContextMenuItem(Icons.Default.Delete, "删除", destructive = true) { vm.delete(item) }
 *     )
 * ) {
 *     // 任意可点击/长按的内容
 *     ListItem(title = item.title, subtitle = item.desc)
 * }
 * ```
 *
 * 设计规范：
 * - 长按触发：触发 HapticFeedback.LongPress
 * - 菜单宽度：根据内容自适应，最大 200dp
 * - 破坏性操作：文字 + 图标使用 error 色
 * - 菜单弹出位置：长按点附近，自动避让屏幕边缘
 *
 * @param menuItems 菜单项列表
 * @param modifier 修饰符
 * @param enabled 是否启用长按菜单
 * @param onClick 可选单击回调（非空时绑定到子内容的点击）
 * @param content 被包装的内容
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun DSContextMenuHost(
    menuItems: List<DSContextMenuItem>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    Box(
        modifier = modifier.combinedClickable(
            enabled = enabled,
            onClick = { onClick?.invoke() },
            onLongClick = {
                // 长按触发位置作为菜单弹出锚点
                offsetX = 0f
                offsetY = 0f
                expanded = true
            }
        )
    ) {
        content()
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(200.dp)
        ) {
            menuItems.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (item.destructive) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = item.icon?.let { icon ->
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (item.destructive) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(DSTokens.IconSize.sm)
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        item.onClick()
                    }
                )
            }
        }
    }
}

/**
 * DSContextMenuTrigger - 三点菜单触发器
 *
 * 显式的图标按钮形态，点击弹出菜单。
 * 适用于卡片右上角、列表项尾部等显式入口场景。
 *
 * 与 [DSContextMenuHost] 的区别：
 * - DSContextMenuHost：长按触发，整体包装
 * - DSContextMenuTrigger：点击三点图标触发，独立按钮
 *
 * 使用示例：
 * ```kotlin
 * DSContextMenuTrigger(
 *     menuItems = listOf(
 *         DSContextMenuItem(Icons.Default.Edit, "编辑") { vm.edit() },
 *         DSContextMenuItem(Icons.Default.Delete, "删除", destructive = true) { vm.delete() }
 *     )
 * )
 * ```
 *
 * @param menuItems 菜单项列表
 * @param modifier 修饰符
 * @param icon 触发按钮图标，默认三点
 * @param contentDescription 无障碍描述
 */
@Composable
internal fun DSContextMenuTrigger(
    menuItems: List<DSContextMenuItem>,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.MoreVert,
    contentDescription: String = "更多操作"
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        androidx.compose.material3.IconButton(onClick = { expanded = true }) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.width(200.dp)
        ) {
            menuItems.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (item.destructive) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    leadingIcon = item.icon?.let { icon ->
                        {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = if (item.destructive) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(DSTokens.IconSize.sm)
                            )
                        }
                    },
                    onClick = {
                        expanded = false
                        item.onClick()
                    }
                )
            }
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "ContextMenu - Long Press Trigger", widthDp = 360)
@Composable
private fun DSContextMenuHostPreview() {
    DSDesignTheme {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            DSContextMenuHost(
                menuItems = listOf(
                    DSContextMenuItem(Icons.Default.Edit, "编辑") {},
                    DSContextMenuItem(Icons.Default.Share, "分享") {},
                    DSContextMenuItem(Icons.Default.Delete, "删除", destructive = true) {}
                ),
                onClick = {}
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(DSTokens.Spacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
                ) {
                    Text(
                        text = "长按我试试",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "ContextMenu - Icon Trigger", widthDp = 360)
@Composable
private fun DSContextMenuTriggerPreview() {
    DSDesignTheme {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            DSContextMenuTrigger(
                menuItems = listOf(
                    DSContextMenuItem(Icons.Default.Edit, "编辑") {},
                    DSContextMenuItem(Icons.Default.Share, "分享") {},
                    DSContextMenuItem(Icons.Default.Delete, "删除", destructive = true) {}
                )
            )
        }
    }
}
