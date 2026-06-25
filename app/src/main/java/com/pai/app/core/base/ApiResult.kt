// ============================================================================
// ApiResult.kt
// 统一的 API 结果封装，用于包装网络请求的返回值（决策 1 唯一规范结果类型）
// 三态：Success（成功）/ Error（失败）/ Loading（加载中）
// ============================================================================

package com.pai.app.core.base

sealed class ApiResult<out T> {
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val exception: AppException) : ApiResult<Nothing>()
    data object Loading : ApiResult<Nothing>()
}

inline fun <T, R> ApiResult<T>.map(transform: (T) -> R): ApiResult<R> = when (this) {
    is ApiResult.Success -> ApiResult.Success(transform(data))
    is ApiResult.Error -> this
    is ApiResult.Loading -> this
}

inline fun <T> ApiResult<T>.onSuccess(action: (T) -> Unit): ApiResult<T> {
    if (this is ApiResult.Success) action(data)
    return this
}

inline fun <T> ApiResult<T>.onError(action: (AppException) -> Unit): ApiResult<T> {
    if (this is ApiResult.Error) action(exception)
    return this
}

inline fun <T> ApiResult<T>.onLoading(action: () -> Unit): ApiResult<T> {
    if (this is ApiResult.Loading) action()
    return this
}

@Suppress("UNCHECKED_CAST")
fun <T> ApiResult<T>.getOrNull(): T? = (this as? ApiResult.Success<T>)?.data

fun <T> ApiResult<T>.getOrDefault(default: T): T = getOrNull() ?: default
