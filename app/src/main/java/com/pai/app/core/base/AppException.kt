// ============================================================================
// AppException.kt
// 应用统一异常体系
// 通过 sealed class 将不同类型的异常分类，便于 UI 层做精细化错误提示
// ============================================================================

package com.pai.app.core.base

/**
 * 应用统一异常基类
 *
 * 将网络、HTTP、业务、序列化、未知等不同来源的异常抽象为统一的类型体系，
 * 便于在 [ApiResult.Error] 中传递，并允许 UI 层通过 `when` 分支精确处理。
 *
 * 使用示例：
 * ```kotlin
 * when (val e = result.exception) {
 *     is AppException.NetworkException       -> showNetworkError()
 *     is AppException.HttpException          -> showHttpError(e.httpCode)
 *     is AppException.BusinessException      -> showBusinessError(e.code, e.message ?: "")
 *     is AppException.SerializationException -> showDataError()
 *     is AppException.UnknownException       -> showUnknownError()
 * }
 * ```
 *
 * 说明：
 * - `CancellationException` 不包装：[com.pai.app.core.network.safeApiCall]
 *   中 `catch (e: CancellationException) -> throw e` 直接透传，避免破坏结构化并发
 * - `BusinessException` 保留：业务方在 Repository 解析业务码（如 -1/401）时构造
 *   （见 `AuthRepository.login` 模拟失败路径与 TODO 标注）
 * - `CancelledException` 已删除（死代码；协程取消应向上抛出而非包装）
 *
 * @param message 异常描述信息
 * @param cause 原始触发异常
 * @param code 业务错误码（可选）
 */
sealed class AppException(
    message: String?,
    cause: Throwable? = null,
    open val code: Int? = null,
) : Exception(message, cause) {

    /**
     * 网络异常：无网络连接、DNS 解析失败、Socket 超时等
     */
    class NetworkException(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppException(message ?: "网络连接异常，请检查网络设置", cause)

    /**
     * HTTP 异常：服务端返回非 2xx 状态码
     *
     * @param httpCode HTTP 状态码，例如 404、500
     */
    class HttpException(
        val httpCode: Int,
        message: String? = null,
        cause: Throwable? = null,
    ) : AppException(message ?: "HTTP 异常 ($httpCode)", cause, httpCode)

    /**
     * 业务异常：服务端返回业务错误码或业务约束失败
     *
     * 调用路径：
     * 1. [com.pai.app.core.data.AuthRepository.login] 在 `email.contains("fail")` 时构造，
     *    用于演示 Error 分支可被 UI 触达
     * 2. TODO 业务方接入真实后端后，在 Repository 中解析 `{code,message,data}` 信封：
     *    `code != 0` 时构造 `BusinessException(code, message)`，
     *    并在 [com.pai.app.core.network.safeApiCall] 之上增加一层业务码解析
     *    （或在自定义 ConverterFactory 中统一完成）
     *
     * @param code 业务错误码（默认 -1）
     */
    class BusinessException(
        message: String,
        cause: Throwable? = null,
        override val code: Int = -1,
    ) : AppException(message, cause, code)

    /**
     * 序列化异常：JSON 解析失败、字段类型不匹配等
     */
    class SerializationException(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppException(message ?: "数据解析异常", cause)

    /**
     * 未知异常：NPE / IllegalStateException 等非 IO/HTTP/Serialization 异常的兜底
     *
     * 由 [com.pai.app.core.network.safeApiCall] 末尾 `catch (e: Exception)` 捕获构造，
     * 避免错误归类失真（Medium #11：旧实现把所有异常归为 SerializationException）。
     */
    class UnknownException(
        message: String? = null,
        cause: Throwable? = null,
    ) : AppException(message ?: "未知异常", cause)

    companion object {

        /**
         * 把任意 [Throwable] 归类为具体的 [AppException]
         *
         * - [kotlinx.coroutines.CancellationException]：**直接 rethrow**（不包装），
         *   保持结构化并发语义
         * - [retrofit2.HttpException]：→ [HttpException]（携带 HTTP 状态码）
         * - [kotlinx.serialization.SerializationException]：→ [SerializationException]
         * - [java.io.IOException]：→ [NetworkException]
         * - 其他：→ [UnknownException]
         *
         * 供 `Flow.asResult()` 与 `safeApiCall` 共用，确保错误分类口径一致。
         */
        fun from(throwable: Throwable): AppException = when (throwable) {
            is kotlinx.coroutines.CancellationException -> throw throwable
            is retrofit2.HttpException -> HttpException(
                httpCode = throwable.code(),
                message = throwable.message,
                cause = throwable,
            )
            is kotlinx.serialization.SerializationException -> SerializationException(
                message = throwable.message,
                cause = throwable,
            )
            is java.io.IOException -> NetworkException(
                message = throwable.message,
                cause = throwable,
            )
            is AppException -> throwable // 已经是 AppException，原样返回
            else -> UnknownException(
                message = throwable.message,
                cause = throwable,
            )
        }
    }
}
