// ============================================================================
// NetWorkResultExt.kt
// Flow → ApiResult 统一转换 + ResultHandler（决策 1）
// 旧的 NetResult sealed class 已删除，统一使用 ApiResult
// ============================================================================

package com.pai.app.core.base

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

/**
 * 将任意 Flow 转换为 [ApiResult] 三态 Flow
 *
 * - Flow 开始时发射 [ApiResult.Loading]
 * - 正常数据包装为 [ApiResult.Success]
 * - 异常通过 [AppException.from] 归类后包装为 [ApiResult.Error]
 *
 * 使用示例：
 * ```kotlin
 * val resultFlow = repository.getProducts().asResult()
 * // resultFlow: Flow<ApiResult<List<Product>>>
 * ```
 */
fun <T> Flow<T>.asResult(): Flow<ApiResult<T>> = this
    .map<T, ApiResult<T>> { ApiResult.Success(it) }
    .catch { e ->
        emit(ApiResult.Error(AppException.from(e)))
    }
    .onStart { emit(ApiResult.Loading) }

/**
 * Result 统一处理器
 *
 * 封装 `Flow<ApiResult<T>>` 的收集与回调分发，
 * 让 ViewModel 只关注数据本身，不写 try/catch 和 when 分支。
 *
 * 使用示例：
 * ```kotlin
 * ResultHandler.handle(
 *     scope = viewModelScope,
 *     flow = repository.getProducts().asResult(),
 *     onLoading = { _uiState.value = BaseNetWorkUiState.Loading },
 *     onSuccess = { data -> _uiState.value = BaseNetWorkUiState.Success(data) },
 *     onError = { msg, _ -> _uiState.value = BaseNetWorkUiState.Error(msg) },
 * )
 * ```
 */
object ResultHandler {

    /**
     * 统一处理 `Flow<ApiResult<T>>`
     *
     * @param scope 协程作用域（通常传 viewModelScope）
     * @param flow 已通过 asResult 注入状态的 Flow
     * @param onLoading 请求开始时回调
     * @param onSuccess 请求成功时回调，参数为数据
     * @param onError 请求失败时回调，参数为错误消息与异常
     * @param onFinally Flow 结束后必定回调（可选）
     */
    fun <T> handle(
        scope: kotlinx.coroutines.CoroutineScope,
        flow: Flow<ApiResult<T>>,
        onLoading: () -> Unit = {},
        onSuccess: (T) -> Unit = {},
        onError: (String, Throwable?) -> Unit = { _, _ -> },
        onFinally: () -> Unit = {},
    ) {
        scope.launch {
            try {
                flow.collect { result ->
                    when (result) {
                        is ApiResult.Loading -> onLoading()
                        is ApiResult.Success -> onSuccess(result.data)
                        is ApiResult.Error -> onError(
                            result.exception.message ?: "未知错误",
                            result.exception,
                        )
                    }
                }
            } finally {
                onFinally()
            }
        }
    }

    /**
     * 简化版：只需成功数据
     *
     * 自动将 Loading/Error 映射到 BaseNetWorkUiState，
     * 业务方只需提供 onData 回调。
     *
     * 使用示例：
     * ```kotlin
     * ResultHandler.handleWithData(
     *     scope = viewModelScope,
     *     flow = repository.getProductDetail(id).asResult(),
     *     onLoading = { _uiState.value = BaseNetWorkUiState.Loading },
     *     onData = { product -> _uiState.value = BaseNetWorkUiState.Success(product) },
     *     onError = { msg, _ -> _uiState.value = BaseNetWorkUiState.Error(msg) },
     * )
     * ```
     */
    fun <T> handleWithData(
        scope: kotlinx.coroutines.CoroutineScope,
        flow: Flow<ApiResult<T>>,
        onLoading: () -> Unit = {},
        onData: (T) -> Unit = {},
        onError: (String, Throwable?) -> Unit = { _, _ -> },
        onFinally: () -> Unit = {},
    ) {
        handle(
            scope = scope,
            flow = flow,
            onLoading = onLoading,
            onSuccess = onData,
            onError = onError,
            onFinally = onFinally,
        )
    }
}
