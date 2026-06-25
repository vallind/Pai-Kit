// ============================================================================
// DSAppScaffold.kt
// 应用通用 Scaffold - 封装 M3 Scaffold + DSTopBar + SnackbarHost
// 统一页面框架结构，减少每个 Feature 重复写 Scaffold 样板代码
// ============================================================================

package com.pai.app.core.designsystem.shell

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import com.pai.app.core.designsystem.overlays.DSSnackbar

/**
 * 应用通用 Scaffold
 *
 * 封装 M3 Scaffold + DSTopBar + SnackbarHost，统一页面框架结构。
 * Feature 层使用此组件代替直接写 Scaffold，减少样板代码。
 *
 * 使用示例（标准页面）：
 * ```kotlin
 * DSAppScaffold(
 *     title = "商品详情",
 *     onBackClick = { navController.goBack() },
 *     actions = listOf(DSTopBarAction(Icons.Default.Share, "分享") { ... }),
 * ) { padding ->
 *     DSNetWorkView(
 *         isLoading = isLoading,
 *         modifier = Modifier.padding(padding),
 *     ) { ProductContent(data) }
 * }
 * ```
 *
 * 使用示例（大标题 + 底栏）：
 * ```kotlin
 * DSAppScaffold(
 *     title = "首页",
 *     useLargeTopBar = true,
 *     bottomBar = { DSBottomBar(items = ...) },
 * ) { padding ->
 *     HomeContent(modifier = Modifier.padding(padding))
 * }
 * ```
 *
 * 使用示例（自定义 TopBar）：
 * ```kotlin
 * DSAppScaffold(
 *     topBar = { MyCustomTopBar() },
 * ) { padding ->
 *     Content(modifier = Modifier.padding(padding))
 * }
 * ```
 *
 * @param modifier 修饰符
 * @param title 顶部栏标题文字（null 则不显示标题）
 * @param topBarStyle 顶部栏风格（Small / CenterAligned / Medium），默认 Small
 * @param topBarActions 顶部栏操作按钮列表
 * @param showBackIcon 是否显示返回按钮，默认 false
 * @param onBackClick 返回按钮回调
 * @param snackbarHostState Snackbar 宿主状态（外部传入可控制显示）
 * @param backgroundColor 页面背景色，默认跟随主题 background
 * @param bottomBar 底部栏内容
 * @param floatingActionButton FAB 内容
 * @param topBar 自定义顶部栏（提供则忽略 title/topBarStyle/topBarActions）
 * @param useLargeTopBar 是否使用大标题（Medium 风格 + 滚动折叠），默认 false
 * @param content 页面主体内容（需自行应用 paddingValues）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSAppScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    topBarStyle: DSTopBarStyle = DSTopBarStyle.Small,
    topBarActions: List<DSTopBarAction> = emptyList(),
    showBackIcon: Boolean = false,
    onBackClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    topBar: (@Composable () -> Unit)? = null,
    useLargeTopBar: Boolean = false,
    content: @Composable (PaddingValues) -> Unit,
) {
    // 大标题滚动行为
    val scrollBehavior = if (useLargeTopBar) {
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    } else {
        null
    }

    val finalModifier = if (scrollBehavior != null) {
        modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    } else {
        modifier
    }

    Scaffold(
        modifier = finalModifier,
        containerColor = backgroundColor,
        topBar = {
            if (topBar != null) {
                // 自定义 TopBar 优先
                topBar()
            } else if (useLargeTopBar) {
                // 大标题模式（Medium 风格 + 滚动折叠）
                DSTopBar(
                    title = title ?: "",
                    style = DSTopBarStyle.Medium,
                    onBackClick = if (showBackIcon) onBackClick else null,
                    actions = topBarActions,
                    scrollBehavior = scrollBehavior,
                )
            } else if (title != null) {
                // 标准模式
                DSTopBar(
                    title = title,
                    style = topBarStyle,
                    onBackClick = if (showBackIcon) onBackClick else null,
                    actions = topBarActions,
                )
            }
            // title = null 且 topBar = null 且 useLargeTopBar = false：不渲染顶栏
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                DSSnackbar(snackbarData = data)
            }
        },
        bottomBar = bottomBar,
        floatingActionButton = floatingActionButton,
        content = { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundColor)
            ) {
                content(paddingValues)
            }
        },
    )
}
