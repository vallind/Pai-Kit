// ============================================================================
// DSTopBar.kt
// 顶部应用栏 - 基于 M3 TopAppBar 扩展
// 支持：返回 / 标题 / 副标题 / 操作按钮 / 溢出菜单
// ============================================================================

package com.pai.app.core.designsystem.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 顶栏样式
 * - Small: 标准高度 64dp
 * - CenterAligned: 标题居中
 * - Medium: 中等高度 + 大标题（可折叠）
 * - Large: 大高度（M3 LargeTopAppBar，可折叠到 Small）
 */
internal enum class DSTopBarStyle {
    Small,
    CenterAligned,
    Medium,
    Large
}

/**
 * 顶栏操作项
 *
 * @param icon 图标
 * @param contentDescription 无障碍描述
 * @param destructive 是否为破坏性操作（true 时图标用 error 色），默认 false
 * @param onClick 点击回调
 */
internal data class DSTopBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val destructive: Boolean = false,
    val onClick: () -> Unit,
)

/**
 * DSTopBar - 顶部应用栏
 *
 * 使用示例：
 * ```kotlin
 * DSTopBar(
 *     title = "首页",
 *     onBackClick = { onBackPressed() },
 *     actions = listOf(
 *         DSTopBarAction(Icons.Default.Search, "搜索") { /* ... */ },
 *         DSTopBarAction(Icons.Default.MoreVert, "更多") { /* ... */ }
 *     )
 * )
 * ```
 *
 * 设计规范：
 * - 高度 64dp（Small）
 * - 标题最大 1 行，溢出省略
 * - 背景色 surface，内容色 onSurface
 * - 状态栏区域自动着色
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSTopBar(
    title: String,
    modifier: Modifier = Modifier,
    style: DSTopBarStyle = DSTopBarStyle.Small,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: List<DSTopBarAction> = emptyList(),
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    val colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurface
    )

    val navigationIcon: @Composable () -> Unit = {
        if (onBackClick != null) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }
        }
    }

    val actionsContent: @Composable RowScope.() -> Unit = {
        Row(horizontalArrangement = Arrangement.End) {
            // 前 2 个 action 直接显示为图标按钮
            actions.take(2).forEach { action ->
                IconButton(onClick = action.onClick) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription,
                        tint = if (action.destructive) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            // 超过 2 个 action 时，剩余的收进溢出菜单
            if (actions.size > 2) {
                var showOverflow by remember { mutableStateOf(false) }
                Box {
                    IconButton(onClick = { showOverflow = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "更多"
                        )
                    }
                    DropdownMenu(
                        expanded = showOverflow,
                        onDismissRequest = { showOverflow = false }
                    ) {
                        actions.drop(2).forEach { action ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = action.contentDescription,
                                        color = if (action.destructive) MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    showOverflow = false
                                    action.onClick()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    val titleContent: @Composable () -> Unit = {
        if (subtitle != null) {
            androidx.compose.foundation.layout.Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        } else {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }

    when (style) {
        DSTopBarStyle.Small -> {
            TopAppBar(
                title = titleContent,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actionsContent,
                colors = colors,
                scrollBehavior = scrollBehavior
            )
        }
        DSTopBarStyle.CenterAligned -> {
            CenterAlignedTopAppBar(
                title = titleContent,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actionsContent,
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        }
        DSTopBarStyle.Medium -> {
            MediumTopAppBar(
                title = titleContent,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actionsContent,
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        }
        DSTopBarStyle.Large -> {
            LargeTopAppBar(
                title = titleContent,
                modifier = modifier,
                navigationIcon = navigationIcon,
                actions = actionsContent,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                scrollBehavior = scrollBehavior
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "TopBar - Small")
@Composable
private fun DSTopBarSmallPreview() {
    DSDesignTheme {
        DSTopBar(
            title = "首页",
            onBackClick = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "TopBar - With Actions")
@Composable
private fun DSTopBarWithActionsPreview() {
    DSDesignTheme {
        DSTopBar(
            title = "设置",
            onBackClick = {},
            actions = listOf(
                DSTopBarAction(androidx.compose.material.icons.Icons.Filled.Search, "搜索") {},
                DSTopBarAction(Icons.Default.MoreVert, "更多") {}
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "TopBar - Large")
@Composable
private fun DSTopBarLargePreview() {
    DSDesignTheme {
        DSTopBar(
            title = "个人主页",
            style = DSTopBarStyle.Large,
            onBackClick = {},
            actions = listOf(
                DSTopBarAction(androidx.compose.material.icons.Icons.Filled.Search, "搜索") {},
                DSTopBarAction(Icons.Default.MoreVert, "更多") {}
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "TopBar - Medium")
@Composable
private fun DSTopBarMediumPreview() {
    DSDesignTheme {
        DSTopBar(
            title = "订单详情",
            style = DSTopBarStyle.Medium,
            onBackClick = {}
        )
    }
}
