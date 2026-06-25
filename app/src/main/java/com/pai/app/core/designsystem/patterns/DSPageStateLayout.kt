// ============================================================================
// DSPageStateLayout.kt
// 页面状态布局 - 配合 DSUiState sealed interface 使用
// 根据 DSUiState 自动渲染对应的全屏状态组件（Loading/Error/Empty）或内容
// ============================================================================

package com.pai.app.core.designsystem.patterns

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme

/**
 * DSPageStateLayout - 页面状态布局
 *
 * 配合 [DSUiState] sealed interface 使用，根据状态自动渲染：
 * - [DSUiState.Loading] → [DSFullScreenLoading]
 * - [DSUiState.Success] → content(data)
 * - [DSUiState.Empty] → [DSFullScreenEmpty]
 * - [DSUiState.Error] → [DSFullScreenError]
 * - [DSUiState.LoadingMore] → content(previousData) + 底部加载指示（业务自处理）
 * - [DSUiState.Refreshing] → content(data)（业务可在内容顶部显示刷新指示）
 * - [DSUiState.PartialError] → content(data) + 错误提示条（业务自处理）
 *
 * 与 [DSNetWorkView] 的区别：
 * - DSNetWorkView：slot 驱动（isLoading/error/empty + content slot），状态分散传参
 * - DSPageStateLayout：sealed 多态（DSUiState），编译期保证 when 分支完整
 *
 * 使用示例：
 * ```kotlin
 * val state by viewModel.uiState.collectAsStateWithLifecycle()
 * DSPageStateLayout(
 *     state = state,
 *     onRetry = { viewModel.retry() },
 *     modifier = Modifier.padding(padding)
 * ) { items ->
 *     LazyColumn { items(items) { ItemRow(it) } }
 * }
 * ```
 *
 * @param state 页面状态
 * @param onRetry 错误重试回调
 * @param onEmptyAction 空状态操作按钮回调（可选）
 * @param modifier 修饰符
 * @param content 成功态内容 slot，参数为 Success.data
 */
@Composable
fun <T> DSPageStateLayout(
    state: DSUiState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onEmptyAction: (() -> Unit)? = null,
    content: @Composable (T) -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (state) {
            is DSUiState.Loading -> {
                DSFullScreenLoading()
            }

            is DSUiState.Success<*> -> {
                @Suppress("UNCHECKED_CAST")
                content(state.data as T)
            }

            is DSUiState.Empty -> {
                DSFullScreenEmpty(
                    title = state.title,
                    description = state.description,
                    actionText = state.actionText,
                    onActionClick = onEmptyAction
                )
            }

            is DSUiState.Error -> {
                DSFullScreenError(
                    message = state.message,
                    onRetry = onRetry
                )
            }

            is DSUiState.LoadingMore<*> -> {
                // 加载更多：展示旧数据，业务可在列表底部自行添加加载指示
                @Suppress("UNCHECKED_CAST")
                content(state.previousData as T)
            }

            is DSUiState.Refreshing<*> -> {
                // 刷新中：展示旧数据，业务可在顶部自行添加下拉刷新指示
                @Suppress("UNCHECKED_CAST")
                content(state.data as T)
            }

            is DSUiState.PartialError<*> -> {
                // 部分错误：展示已成功数据，业务可自行添加错误提示条
                @Suppress("UNCHECKED_CAST")
                content(state.data as T)
            }
        }
    }
}

/**
 * DSPageStateLayout - 简化版（配合 DSSimpleUiState）
 *
 * 无数据承载，适合简单页面。
 */
@Composable
fun DSPageStateLayout(
    state: DSSimpleUiState,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (state) {
            is DSSimpleUiState.Loading -> DSFullScreenLoading()
            is DSSimpleUiState.Content -> content()
            is DSSimpleUiState.Empty -> DSFullScreenEmpty()
            is DSSimpleUiState.Error -> DSFullScreenError(state.message, onRetry)
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "PageStateLayout - Loading")
@Composable
private fun DSPageStateLayoutLoadingPreview() {
    DSDesignTheme {
        DSPageStateLayout<String>(
            state = DSUiState.loading(),
            onRetry = {},
            content = { /* 不会渲染 */ }
        )
    }
}

@Preview(showBackground = true, name = "PageStateLayout - Success")
@Composable
private fun DSPageStateLayoutSuccessPreview() {
    DSDesignTheme {
        DSPageStateLayout(
            state = DSUiState.success(listOf("Item 1", "Item 2")),
            onRetry = {}
        ) { items ->
            androidx.compose.foundation.layout.Column {
                items.forEach { com.pai.app.core.designsystem.primitives.DSText(it) }
            }
        }
    }
}

@Preview(showBackground = true, name = "PageStateLayout - Empty")
@Composable
private fun DSPageStateLayoutEmptyPreview() {
    DSDesignTheme {
        DSPageStateLayout<String>(
            state = DSUiState.empty(description = "下拉刷新试试"),
            onRetry = {},
            onEmptyAction = {}
        ) { /* 不会渲染 */ }
    }
}

@Preview(showBackground = true, name = "PageStateLayout - Error")
@Composable
private fun DSPageStateLayoutErrorPreview() {
    DSDesignTheme {
        DSPageStateLayout<String>(
            state = DSUiState.error(message = "网络连接失败"),
            onRetry = {}
        ) { /* 不会渲染 */ }
    }
}
