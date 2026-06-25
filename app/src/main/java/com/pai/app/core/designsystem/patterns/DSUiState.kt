// ============================================================================
// DSUiState.kt
// 统一页面状态 sealed interface — 状态机范式
// 与 DSNetWorkView（slot 范式）并存，业务自选
//
// 两种范式对比：
//   DSNetWorkView：slot 驱动（isLoading/error/empty + content slot）
//                  适合简单页面，状态分散传参
//   DSUiState：sealed 多态（Loading/Success/Empty/Error/...）
//                  适合复杂状态机，编译期保证 when 分支完整
// ============================================================================

package com.pai.app.core.designsystem.patterns

import androidx.compose.runtime.Stable

/**
 * DSUiState - 统一页面状态 sealed interface
 *
 * 状态机范式，编译期保证 `when` 分支完整性。配合 [DSPageStateLayout] 使用。
 *
 * 8 种状态：
 * - [Loading]：首次加载中
 * - [Success]：加载成功且有内容
 * - [Empty]：加载成功但无内容
 * - [Error]：加载失败
 * - [LoadingMore]：分页加载下一页（保留旧数据）
 * - [Refreshing]：下拉刷新中（保留旧数据展示）
 * - [PartialError]：部分项加载失败（保留已成功部分）
 *
 * 使用示例：
 * ```kotlin
 * // ViewModel
 * val uiState: StateFlow<DSUiState<List<Item>>> = flow.map { result ->
 *     when (result) {
 *         is ApiResult.Loading -> DSUiState.loading()
 *         is ApiResult.Success -> {
 *             if (result.data.isEmpty()) DSUiState.empty(description = "暂无数据")
 *             else DSUiState.success(result.data)
 *         }
 *         is ApiResult.Error -> DSUiState.error(message = result.message)
 *     }
 * }.stateIn(...)
 *
 * // Composable
 * val state by viewModel.uiState.collectAsStateWithLifecycle()
 * DSPageStateLayout(
 *     state = state,
 *     onRetry = { viewModel.retry() },
 *     onRefresh = { viewModel.refresh() }
 * ) { items ->
 *     LazyColumn { items(items) { ItemRow(it) } }
 * }
 * ```
 *
 * 中文默认文案仅作为业务便利默认值，业务方可覆盖。
 *
 * @param T 数据类型
 */
@Stable
sealed interface DSUiState<out T> {

    /** 首次加载中 */
    data object Loading : DSUiState<Nothing>

    /** 数据加载成功且有内容 */
    data class Success<T>(val data: T) : DSUiState<T>

    /** 加载成功但无内容 */
    data class Empty(
        val title: String = "暂无数据",
        val description: String? = null,
        val actionText: String? = null
    ) : DSUiState<Nothing>

    /** 错误状态 */
    data class Error(
        val message: String = "加载失败，请稍后重试",
        val retryText: String = "重试",
        val errorCode: String? = null
    ) : DSUiState<Nothing>

    /** 加载更多（分页加载下一页，与首次 Loading 区分，保留 previousData 展示） */
    data class LoadingMore<T>(val previousData: T) : DSUiState<T>

    /** 刷新中（下拉刷新，保留旧数据展示） */
    data class Refreshing<T>(val data: T) : DSUiState<T>

    /** 部分错误（部分项加载失败，保留已成功部分） */
    data class PartialError<T>(
        val data: T,
        val errorMessage: String = "部分内容加载失败",
        val retryText: String = "重试"
    ) : DSUiState<T>

    companion object {
        fun <T> success(data: T): DSUiState<T> = Success(data)
        fun loading(): DSUiState<Nothing> = Loading
        fun empty(
            title: String = "暂无数据",
            description: String? = null,
            actionText: String? = null
        ): DSUiState<Nothing> = Empty(title, description, actionText)

        fun error(
            message: String = "加载失败，请稍后重试",
            retryText: String = "重试",
            errorCode: String? = null
        ): DSUiState<Nothing> = Error(message, retryText, errorCode)

        fun <T> loadingMore(previousData: T): DSUiState<T> = LoadingMore(previousData)
        fun <T> refreshing(data: T): DSUiState<T> = Refreshing(data)
        fun <T> partialError(
            data: T,
            errorMessage: String = "部分内容加载失败",
            retryText: String = "重试"
        ): DSUiState<T> = PartialError(data, errorMessage, retryText)
    }
}

/**
 * DSSimpleUiState - 简化版页面状态
 *
 * 无数据承载，仅 4 种状态：Loading / Content / Empty / Error
 * 适合简单页面（如纯展示页、设置页）。
 *
 * 与 [DSUiState] 的区别：
 * - DSUiState：带泛型数据，8 种状态，复杂场景
 * - DSSimpleUiState：无数据，4 种状态，简单场景
 */
@Stable
sealed interface DSSimpleUiState {
    data object Loading : DSSimpleUiState
    data object Content : DSSimpleUiState
    data object Empty : DSSimpleUiState
    data class Error(val message: String = "加载失败") : DSSimpleUiState

    companion object {
        fun loading(): DSSimpleUiState = Loading
        fun content(): DSSimpleUiState = Content
        fun empty(): DSSimpleUiState = Empty
        fun error(message: String = "加载失败"): DSSimpleUiState = Error(message)
    }
}
