// ============================================================================
// ApiExtensionsTest.kt
// safeApiCall 单元测试：异常映射规则与 CancellationException 透传
// 决策 1 + Medium #11：异常分类细化（新增 UnknownException / SerializationException 仅匹配 kotlinx.serialization）
// ============================================================================

package com.pai.app.core.network

import com.pai.app.core.base.ApiResult
import com.pai.app.core.base.AppException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * [safeApiCall] 单元测试
 *
 * 覆盖异常映射规则（决策 1 + Medium #11，Core agent 已细化异常分类）：
 * - 正常返回 → [ApiResult.Success]
 * - [IOException] → [ApiResult.Error] + [AppException.NetworkException]
 * - [HttpException] → [ApiResult.Error] + [AppException.HttpException]（携带状态码）
 * - [kotlinx.serialization.SerializationException] → [ApiResult.Error] + [AppException.SerializationException]
 * - 其他 [Exception]（如 [NullPointerException] / [IllegalStateException]）→ [ApiResult.Error] + [AppException.UnknownException]
 * - [CancellationException] → 直接 rethrow，不包装（保持协程取消语义）
 *
 * 注：[safeApiCall] 为 internal 顶级函数，本测试置于同包 com.pai.app.core.network 下访问。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ApiExtensionsTest {

    @Test
    fun `成功返回 ApiResult Success`() = runTest {
        val result = safeApiCall { "OK" }
        assertTrue(result is ApiResult.Success)
        assertEquals("OK", (result as ApiResult.Success).data)
    }

    @Test
    fun `IOException 转为 NetworkException`() = runTest {
        val ioException = IOException("network down")
        val result = safeApiCall<String> { throw ioException }
        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is AppException.NetworkException)
        assertSame(ioException, error.exception.cause)
    }

    @Test
    fun `HttpException 转为 AppException HttpException 并携带状态码`() = runTest {
        val response = Response.error<Any>(404, "Not Found".toResponseBody(null))
        val httpException = HttpException(response)

        val result = safeApiCall<String> { throw httpException }
        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is AppException.HttpException)
        assertEquals(404, (error.exception as AppException.HttpException).httpCode)
    }

    @Test
    fun `SerializationException 转为 AppException SerializationException`() = runTest {
        // kotlinx.serialization.SerializationException —— safeApiCall 显式 catch 此类型
        val serException = object : kotlinx.serialization.SerializationException("bad json") {}
        val result = safeApiCall<String> { throw serException }
        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is AppException.SerializationException)
        assertSame(serException, error.exception.cause)
    }

    @Test
    fun `CancellationException 直接 rethrow 不被包装`() = runTest {
        val cancellation = CancellationException("cancelled")
        var thrown: Throwable? = null
        try {
            safeApiCall<String> { throw cancellation }
        } catch (e: Throwable) {
            thrown = e
        }
        assertSame(cancellation, thrown)
    }

    @Test
    fun `其他非 IO HTTP Serialization 异常转为 UnknownException`() = runTest {
        // Medium #11：旧实现把所有非 IO/HTTP 异常错误归为 SerializationException，
        // Core agent 已修正 —— 非序列化异常（如 IllegalStateException / NPE）现归为 UnknownException
        val other = IllegalStateException("unexpected state")
        val result = safeApiCall<String> { throw other }
        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is AppException.UnknownException)
        assertSame(other, error.exception.cause)
    }

    @Test
    fun `NullPointerException 也归为 UnknownException 而非 SerializationException`() = runTest {
        val npe = NullPointerException("npe")
        val result = safeApiCall<String> { throw npe }
        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is AppException.UnknownException)
        assertSame(npe, error.exception.cause)
    }
}
