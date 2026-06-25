// ============================================================================
// DSModalNavigationDrawer.kt
// 模态抽屉 - 基于 M3 ModalNavigationDrawer + ModalDrawerSheet
// 适用于移动端侧边导航：从屏幕边缘滑出，覆盖主内容并带遮罩
// 内置默认 header（含 title 与关闭按钮），业务内容通过 drawerContent 注入
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.overlays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

/**
 * DSModalNavigationDrawer - 模态抽屉
 *
 * 使用示例：
 * ```kotlin
 * var showDrawer by remember { mutableStateOf(false) }
 *
 * DSModalNavigationDrawer(
 *     drawerContent = {
 *         // 业务自定义内容（已处于 ColumnScope 中）
 *         NavigationDrawerItem(
 *             label = { Text("首页") },
 *             selected = true,
 *             onClick = { /* ... */ },
 *             icon = { Icon(Icons.Default.Home, null) }
 *         )
 *     },
 *     onDismiss = { showDrawer = false },
 *     title = "导航菜单",
 *     drawerState = if (showDrawer) DrawerValue.Open else DrawerValue.Closed
 * ) {
 *     // 主屏幕内容
 *     MainScreen(onMenuClick = { showDrawer = true })
 * }
 * ```
 *
 * 设计规范：
 * - 基于 M3 ModalNavigationDrawer + ModalDrawerSheet，移动端标准侧边抽屉
 * - 抽屉宽度跟随 M3 默认（360dp），从左侧滑出
 * - 内置 header：title（titleLarge + SemiBold）+ 关闭按钮（IconButton），title 为 null 时不渲染 header
 * - drawerState 参数作为初始值传入，内部用 rememberDrawerState 创建状态。若调用方需编程式控制
 *   （例如在外部触发 `state.open()` / `state.close()`），可传入 [drawerState] 参数提供 hoisted 状态。
 *   典型用法：业务通过条件渲染控制 DSModalNavigationDrawer 的挂载/卸载，配合 onDismiss 收起
 * - onDismiss 触发时机：抽屉从 Open 转为 Closed（点击关闭按钮 / 点击遮罩 / 边缘滑动关闭）
 * - 关闭按钮调用 state.close()，由 snapshotFlow 监听状态变化统一触发 onDismiss
 *
 * @param drawerContent 抽屉主体内容，处于 ColumnScope 中，业务可填充 NavigationDrawerItem 等
 * @param onDismiss 抽屉关闭回调（Open -> Closed 时触发）
 * @param modifier 修饰符
 * @param title 抽屉标题，非 null 时渲染内置 header（含标题与关闭按钮）
 * @param initialDrawerValue 抽屉初始状态，默认 Closed；设为 Open 则挂载即展开。
 *        仅在 [drawerState] 为 null 时生效。
 * @param drawerState 可选的底层 [DrawerState]。传入 null（默认）时内部 `rememberDrawerState(initialDrawerValue)`；
 *        传入非 null 时使用调用方 hoisted 的状态，便于编程式控制。
 * @param content 主屏幕内容，由 ModalNavigationDrawer 包装
 */
@Composable
internal fun DSModalNavigationDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    initialDrawerValue: DrawerValue = DrawerValue.Closed,
    drawerState: DrawerState? = null,
    content: @Composable () -> Unit
) {
    val state = drawerState ?: rememberDrawerState(initialValue = initialDrawerValue)
    val scope = rememberCoroutineScope()

    // 监听抽屉状态：当从 Open 转为 Closed 时触发 onDismiss
    // drop(1) 跳过初始值发射，避免挂载时误触发
    LaunchedEffect(state) {
        snapshotFlow { state.currentValue }
            .drop(1)
            .filter { it == DrawerValue.Closed }
            .collect { onDismiss() }
    }

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = state,
        drawerContent = {
            ModalDrawerSheet {
                // ----------------------------------------------------------------
                // 默认 header：标题 + 关闭按钮
                // ----------------------------------------------------------------
                if (title != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = DSTokens.Spacing.lg,
                                vertical = DSTokens.Spacing.md
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = {
                            scope.launch { state.close() }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭抽屉",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ----------------------------------------------------------------
                // 业务自定义内容
                // ----------------------------------------------------------------
                drawerContent()
            }
        },
        content = content
    )
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "DSModalNavigationDrawer - 打开状态（含标题）")
@Composable
private fun DSModalDrawerOpenPreview() {
    DSDesignTheme {
        DSModalNavigationDrawer(
            drawerContent = {
                Column(modifier = Modifier.padding(horizontal = DSTokens.Spacing.lg)) {
                    Text(
                        text = "首页",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(DSTokens.Spacing.md))
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(DSTokens.Spacing.md))
                    Text(
                        text = "关于",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            onDismiss = {},
            title = "导航菜单",
            initialDrawerValue = DrawerValue.Open
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

@Preview(showBackground = true, name = "DSModalNavigationDrawer - 无标题")
@Composable
private fun DSModalDrawerNoTitlePreview() {
    DSDesignTheme {
        DSModalNavigationDrawer(
            drawerContent = {
                Column(modifier = Modifier.padding(DSTokens.Spacing.lg)) {
                    Text(
                        text = "项目一",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(DSTokens.Spacing.sm))
                    Text(
                        text = "项目二",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            onDismiss = {},
            initialDrawerValue = DrawerValue.Open
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
