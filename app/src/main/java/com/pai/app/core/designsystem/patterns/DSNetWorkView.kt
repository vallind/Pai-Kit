// ============================================================================
// DSNetWorkView.kt
// 网络三态 UI 容器（纯净版 - 不消费业务状态类）
// 只接收通用参数 + slot lambda，由 Feature 层负责状态映射
// ============================================================================

package com.pai.app.core.designsystem.patterns

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * 通用错误数据（不依赖业务 sealed class）
 */
data class DSErrorData(
    val message: String,
    val throwable: Throwable? = null,
)

/**
 * 通用空数据（不依赖业务 sealed class）
 */
data class DSEmptyData(
    val title: String = "暂无数据",
    val description: String? = null,
)

/**
 * 网络请求三态 UI 容器（纯净版）
 *
 * **不接收** `BaseNetWorkUiState<T>` 等业务状态类，
 * 只接收 `isLoading` / `error` / `empty` 通用参数 + `content` slot。
 * 由 Feature 层负责把业务状态映射为这些参数。
 *
 * 使用示例（Feature 层映射）：
 * ```kotlin
 * @Composable
 * fun ProductDetailScreen(viewModel: ProductDetailViewModel = hiltViewModel()) {
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *
 *     DSNetWorkView(
 *         isLoading = uiState is BaseNetWorkUiState.Loading,
 *         error = (uiState as? BaseNetWorkUiState.Error)?.let { DSErrorData(it.message) },
 *         empty = if (uiState is BaseNetWorkUiState.Empty) DSEmptyData() else null,
 *         onRetry = viewModel::retryRequest,
 *     ) {
 *         val data = (uiState as BaseNetWorkUiState.Success).data
 *         ProductContent(data)
 *     }
 * }
 * ```
 *
 * @param isLoading 是否加载中
 * @param error 错误数据（非 null 时显示错误态）
 * @param empty 空数据（非 null 时显示空态）
 * @param onRetry 重试回调
 * @param modifier 修饰符
 * @param padding 内边距
 * @param customLoading 自定义加载态
 * @param customError 自定义错误态
 * @param customEmpty 自定义空态
 * @param content 成功态内容
 */
@Composable
fun DSNetWorkView(
    isLoading: Boolean = false,
    error: DSErrorData? = null,
    empty: DSEmptyData? = null,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(),
    customLoading: (@Composable () -> Unit)? = null,
    customError: (@Composable () -> Unit)? = null,
    customEmpty: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(padding),
    ) {
        when {
            isLoading -> {
                customLoading?.invoke() ?: DSFullScreenLoading()
            }

            error != null -> {
                customError?.invoke() ?: DSFullScreenError(
                    message = error.message,
                    onRetry = onRetry,
                )
            }

            empty != null -> {
                customEmpty?.invoke() ?: DSFullScreenEmpty(
                    title = empty.title,
                    description = empty.description,
                )
            }

            else -> {
                content()
            }
        }
    }
}
