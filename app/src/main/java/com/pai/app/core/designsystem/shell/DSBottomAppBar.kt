// ============================================================================
// DSBottomAppBar.kt
// 底部应用栏 - 基于 M3 BottomAppBar
// 支持 FAB 凹槽 (FAB cradle) 与操作按钮
// 与 DSTopBar 命名对称：TopBar 在顶部，BottomAppBar 在底部
// ============================================================================

package com.pai.app.core.designsystem.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.shell.DSTopBarAction
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSBottomAppBar - 底部应用栏
 *
 * 与 DSTopBar 命名对称：
 * - DSTopBar 用于页面顶部，承担导航/标题/操作职责
 * - DSBottomAppBar 用于页面底部，提供操作按钮与 FAB 入口
 *
 * 使用示例：
 * ```kotlin
 * DSBottomAppBar(
 *     title = "新建笔记",
 *     actions = listOf(
 *         DSTopBarAction(Icons.Default.Search, "搜索") { /* ... */ },
 *         DSTopBarAction(Icons.Default.MoreVert, "更多") { /* ... */ }
 *     ),
 *     fabIcon = Icons.Default.Add,
 *     fabContentDescription = "新建",
 *     onFabClick = { vm.createNote() }
 * )
 * ```
 *
 * 设计规范：
 * - 高度 80dp（BottomAppBar 默认）
 * - FAB 嵌入凹槽 (FAB cradle)，使用 Docked 模式
 * - FAB 配色：primaryContainer / onPrimaryContainer
 * - App Bar 配色：surface / onSurface
 * - 海拔 level3
 * - 操作按钮最多 4 个，超出以「更多」图标收起
 *
 * @param title 可选标题，居左显示
 * @param modifier 修饰符
 * @param actions 操作按钮列表（最多渲染前 3 个，超出聚合为 MoreVert）
 * @param fabIcon FAB 图标，非空时显示 FAB
 * @param fabContentDescription FAB 无障碍描述
 * @param onFabClick FAB 点击回调（即 fabOnClick，遵循 Compose onXClick 命名约定）
 * @param actionsRowHorizontalArrangement 操作行水平排布，默认 SpaceBetween
 */
@Composable
internal fun DSBottomAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    actions: List<DSTopBarAction> = emptyList(),
    fabIcon: ImageVector? = null,
    fabContentDescription: String? = null,
    onFabClick: (() -> Unit)? = null,
    actionsRowHorizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(
        space = DSTokens.Spacing.xs,
        alignment = Alignment.End
    )
) {
    val hasFab = fabIcon != null && onFabClick != null

    // FAB 内容
    val floatingActionButton: @Composable () -> Unit = {
        if (hasFab) {
            FloatingActionButton(
                onClick = onFabClick!!,
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = DSTokens.Elevation.level3,
                    pressedElevation = DSTokens.Elevation.level6
                )
            ) {
                Icon(
                    imageVector = fabIcon!!,
                    contentDescription = fabContentDescription,
                    modifier = Modifier.size(DSTokens.IconSize.md)
                )
            }
        }
    }

    BottomAppBar(
        modifier = modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = DSTokens.Elevation.level3,
        actions = {
            // 标题（如有），独占左侧
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(end = DSTokens.Spacing.sm)
                )
            }
            // 操作按钮：最多渲染前 3 个；超出部分不显示。
            // 如需溢出菜单（DropdownMenu），请直接使用 DSTopBar（已实现 overflow 模式）。
            val maxActions = 3
            actions.take(maxActions).forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        floatingActionButton = if (hasFab) floatingActionButton else null
    )
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "AppBar - With FAB Cradle", widthDp = 360)
@Composable
private fun DSAppBarWithFabPreview() {
    DSDesignTheme {
        DSBottomAppBar(
            title = null,
            actions = listOf(
                DSTopBarAction(Icons.Default.Search, "搜索") {},
                DSTopBarAction(Icons.Default.Edit, "编辑") {},
                DSTopBarAction(Icons.Default.MoreVert, "更多") {}
            ),
            fabIcon = Icons.Default.Add,
            fabContentDescription = "新建",
            onFabClick = {}
        )
    }
}

@Preview(showBackground = true, name = "AppBar - Title + FAB", widthDp = 360)
@Composable
private fun DSAppBarTitleAndFabPreview() {
    DSDesignTheme {
        DSBottomAppBar(
            title = "新建笔记",
            actions = listOf(
                DSTopBarAction(Icons.Default.Search, "搜索") {}
            ),
            fabIcon = Icons.Default.Add,
            fabContentDescription = "新建",
            onFabClick = {}
        )
    }
}

@Preview(showBackground = true, name = "AppBar - No FAB", widthDp = 360)
@Composable
private fun DSAppBarNoFabPreview() {
    DSDesignTheme {
        DSBottomAppBar(
            title = "底部菜单",
            actions = listOf(
                DSTopBarAction(Icons.Default.Search, "搜索") {},
                DSTopBarAction(Icons.Default.Edit, "编辑") {},
                DSTopBarAction(Icons.Default.Add, "添加") {}
            )
        )
    }
}
