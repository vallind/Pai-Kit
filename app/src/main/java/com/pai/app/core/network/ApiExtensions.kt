// ============================================================================
// ApiExtensions.kt
// 网络请求安全包装：将任意 suspend API 调用转换为 ApiResult
// ============================================================================

package com.pai.app.core.network

import com.pai.app.core.base.ApiResult
import com.pai.app.core.base.AppException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException

/**
 * 安全地执行一次网络 API 调用，并将其结果包装为 [ApiResult]
 *
 * 异常映射规则（与 [AppException.from] 口径一致；Medium #11 修复）：
 * - [CancellationException]：**直接 rethrow**，避免破坏协程取消语义
 * - [HttpException]：→ [AppException.HttpException]（携带 HTTP 状态码）
 * - [SerializationException]：→ [AppException.SerializationException]
 * - [IOException]：→ [AppException.NetworkException]
 * - 其他 [Exception]：→ [AppException.UnknownException]
 *
 * 使用示例：
 * ```kotlin
 * suspend fun getPosts(): ApiResult<List<PostDto>> =
 *     safeApiCall { api.getPosts() }
 * ```
 *
 * @param apiCall 实际的 suspend API 调用
 * @return 成功返回 [ApiResult.Success]，失败返回 [ApiResult.Error]
 */
internal suspend fun <T> safeApiCall(apiCall: suspend () -> T): ApiResult<T> {
    return try {
        ApiResult.Success(apiCall())
    } catch (e: CancellationException) {
        // 协程取消异常必须向上抛出，否则会破坏结构化并发
        throw e
    } catch (e: HttpException) {
        ApiResult.Error(AppException.HttpException(e.code(), e.message, e))
    } catch (e: SerializationException) {
        ApiResult.Error(AppException.SerializationException(e.message, e))
    } catch (e: IOException) {
        ApiResult.Error(AppException.NetworkException(e.message, e))
    } catch (e: Exception) {
        // 兜底：NPE / IllegalStateException / 业务自定义异常等
        // 旧实现错误地把所有非 IO/HTTP 异常归为 SerializationException（Medium #11）
        ApiResult.Error(AppException.UnknownException(e.message, e))
    }
}
