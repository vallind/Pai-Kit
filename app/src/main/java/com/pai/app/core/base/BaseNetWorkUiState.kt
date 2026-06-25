// ============================================================================
// BaseNetWorkUiState.kt
// 网络请求三态状态模型 - 借鉴 AndroidProject-Compose
// Loading / Success / Error / Empty 四态 sealed class
// ============================================================================

package com.pai.app.core.base

/**
 * 网络请求 UI 状态（不分页）
 *
 * 四态 sealed class，配合 [BaseNetWorkViewModel] 自动管理：
 * - [Loading]：请求中（首次加载或重试）
 * - [Success]：请求成功，携带数据
 * - [Error]：请求失败，携带错误消息与异常
 * - [Empty]：请求成功但无数据
 *
 * 使用示例：
 * ```kotlin
 * val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 * when (uiState) {
 *     is BaseNetWorkUiState.Loading -> DSFullScreenLoading()
 *     is BaseNetWorkUiState.Success -> Content(uiState.data)
 *     is BaseNetWorkUiState.Error -> DSFullScreenError(uiState.message) { viewModel.retry() }
 *     is BaseNetWorkUiState.Empty -> DSFullScreenEmpty()
 * }
 * ```
 *
 * 或用 [DSNetWorkView] 一行搞定三态切换。
 */
sealed class BaseNetWorkUiState<out T> {

    /** 加载中 */
    data object Loading : BaseNetWorkUiState<Nothing>()

    /** 成功，携带数据 */
    data class Success<T>(val data: T) : BaseNetWorkUiState<T>()

    /** 失败 */
    data class Error(
        val message: String,
        val throwable: Throwable? = null,
    ) : BaseNetWorkUiState<Nothing>()

    /** 空数据（请求成功但返回空列表等） */
    data object Empty : BaseNetWorkUiState<Nothing>()

    /** 是否加载中 */
    val isLoading: Boolean get() = this is Loading

    /** 是否成功 */
    val isSuccess: Boolean get() = this is Success

    /** 是否失败 */
    val isError: Boolean get() = this is Error

    /** 是否空数据 */
    val isEmpty: Boolean get() = this is Empty

    /**
     * 获取数据（仅 [Success] 时有值）
     *
     * 安全性说明：本类对 `T` 协变（`out T`），所以 `this as? Success<T>`
     * 触发 unchecked cast。但实际仅读取 `data: T` 字段，且只有
     * [Success] 子类持有该字段（[Error]/[Loading]/[Empty] 都用 `Nothing`），
     * 运行时类型与静态类型一致，cast 安全。
     */
    @Suppress("UNCHECKED_CAST")
    fun getOrNull(): T? = (this as? Success)?.data
}
