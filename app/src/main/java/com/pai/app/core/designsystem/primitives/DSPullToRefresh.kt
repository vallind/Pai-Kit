// ============================================================================
// DSPullToRefresh.kt
// 下拉刷新容器 - 基于 Material3 1.3.0 PullToRefreshBox
// 自定义 indicator 颜色为 primary
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSPullToRefresh - 下拉刷新容器
 *
 * 使用示例：
 * ```kotlin
 * var isRefreshing by remember { mutableStateOf(false) }
 *
 * DSPullToRefresh(
 *     isRefreshing = isRefreshing,
 *     onRefresh = {
 *         isRefreshing = true
 *         viewModel.refresh {
 *             isRefreshing = false  // 数据加载完成后置 false
 *         }
 *     }
 * ) {
 *     LazyColumn { ... }
 * }
 * ```
 *
 * 设计规范：
 * - 基于 Material3 1.3.0 [PullToRefreshBox]（统一容器 API，替代旧的 PullRefreshIndicator 组合）
 * - indicator 颜色定制为 primary（旋转圆点 primary / 容器 surface）
 * - 满足 Material 推荐的 64dp 拉动阈值（PullToRefreshDefaults.PositionalThreshold）
 * - 内容应可垂直滚动（如 LazyColumn / LazyVerticalGrid / verticalScroll Column），
 *   下拉手势才会触发刷新
 * - isRefreshing = true 时 indicator 显示为加载态（持续旋转）
 *
 * @param modifier 修饰符
 * @param isRefreshing 是否正在刷新
 * @param onRefresh 用户触发刷新时的回调（业务层需自行将 isRefreshing 置为 true，
 *   数据加载完成后置为 false）
 * @param content 子内容（应是可滚动组件）
 */
@Composable
internal fun DSPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val state = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier,
        state = state,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = isRefreshing,
                state = state,
                containerColor = MaterialTheme.colorScheme.surface,
                color = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        content()
    }
}

// ============================================================================
// Preview
// ============================================================================

@Preview(showBackground = true, name = "DSPullToRefresh - 列表")
@Composable
private fun DSPullToRefreshPreview() {
    DSDesignTheme {
        var isRefreshing by remember { mutableStateOf(false) }
        val items = remember { (1..30).map { "Item $it" } }

        DSPullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = { isRefreshing = !isRefreshing }
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    horizontal = DSTokens.Spacing.lg,
                    vertical = DSTokens.Spacing.md
                )
            ) {
                items(items) { item ->
                    Text(
                        text = item,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = DSTokens.Spacing.md),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "DSPullToRefresh - 刷新中")
@Composable
private fun DSPullToRefreshRefreshingPreview() {
    DSDesignTheme {
        DSPullToRefresh(
            isRefreshing = true,
            onRefresh = {}
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "正在刷新...",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "DSPullToRefresh - 空内容")
@Composable
private fun DSPullToRefreshEmptyPreview() {
    DSDesignTheme {
        DSPullToRefresh(
            isRefreshing = false,
            onRefresh = {}
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "下拉刷新",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
