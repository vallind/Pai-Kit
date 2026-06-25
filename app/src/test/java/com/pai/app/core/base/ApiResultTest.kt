// ============================================================================
// ApiResultTest.kt
// ApiResult sealed class 单元测试：三态判定 / 数据读取 / map / 回调触发
// ============================================================================

package com.pai.app.core.base

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [ApiResult] sealed class 单元测试
 *
 * 覆盖以下行为：
 * - 三态判定（[isSuccess] / [isError] / [isLoading]）
 * - [getOrNull] 仅在 Success 时返回数据
 * - [map] 转换 Success 数据，Error / Loading 原样透传
 * - [onSuccess] / [onError] / [onLoading] 回调触发条件
 *
 * 注：[isSuccess] / [isError] / [isLoading] / [getOrNull] 为测试用辅助扩展，
 * 以属性风格封装三态判定，便于断言阅读；其语义与对外期望保持一致。
 */
class ApiResultTest {

    // ------------------------------------------------------------------------
    // 测试用辅助扩展：镜像期望的对外 API
    // ------------------------------------------------------------------------

    private val <T> ApiResult<T>.isSuccess: Boolean
        get() = this is ApiResult.Success

    private val <T> ApiResult<T>.isError: Boolean
        get() = this is ApiResult.Error

    private val <T> ApiResult<T>.isLoading: Boolean
        get() = this is ApiResult.Loading

    private fun <T> ApiResult<T>.getOrNull(): T? = when (this) {
        is ApiResult.Success -> data
        is ApiResult.Error -> null
        is ApiResult.Loading -> null
    }

    // ------------------------------------------------------------------------
    // 三态判定
    // ------------------------------------------------------------------------

    @Test
    fun `Success isSuccess 返回 true`() {
        val result: ApiResult<String> = ApiResult.Success("hello")
        assertTrue(result.isSuccess)
        assertFalse(result.isError)
        assertFalse(result.isLoading)
    }

    @Test
    fun `Error isError 返回 true`() {
        val result: ApiResult<String> = ApiResult.Error(AppException.NetworkException())
        assertTrue(result.isError)
        assertFalse(result.isSuccess)
        assertFalse(result.isLoading)
    }

    @Test
    fun `Loading isLoading 返回 true`() {
        val result: ApiResult<String> = ApiResult.Loading
        assertTrue(result.isLoading)
        assertFalse(result.isSuccess)
        assertFalse(result.isError)
    }

    // ------------------------------------------------------------------------
    // getOrNull
    // ------------------------------------------------------------------------

    @Test
    fun `getOrNull 仅在 Success 时返回数据`() {
        assertEquals("hello", ApiResult.Success("hello").getOrNull())
        assertNull(ApiResult.Error(AppException.NetworkException()).getOrNull())
        assertNull(ApiResult.Loading.getOrNull())
    }

    // ------------------------------------------------------------------------
    // map
    // ------------------------------------------------------------------------

    @Test
    fun `map 转换 Success 数据`() {
        val result: ApiResult<Int> = ApiResult.Success(41)
        val mapped = result.map { it + 1 }
        assertTrue(mapped is ApiResult.Success)
        assertEquals(42, (mapped as ApiResult.Success).data)
    }

    @Test
    fun `map 对 Error 原样透传`() {
        val exception = AppException.BusinessException("失败")
        val result: ApiResult<Int> = ApiResult.Error(exception)
        val mapped = result.map { it + 1 }
        assertTrue(mapped is ApiResult.Error)
        assertSame(exception, (mapped as ApiResult.Error).exception)
    }

    @Test
    fun `map 对 Loading 原样透传`() {
        val result: ApiResult<Int> = ApiResult.Loading
        val mapped = result.map { it + 1 }
        assertTrue(mapped is ApiResult.Loading)
    }

    // ------------------------------------------------------------------------
    // onSuccess / onError / onLoading
    // ------------------------------------------------------------------------

    @Test
    fun `onSuccess 仅在 Success 时触发`() {
        var consumed: String? = null

        ApiResult.Success("ok").onSuccess { consumed = it }
        assertEquals("ok", consumed)

        consumed = null
        ApiResult.Error(AppException.NetworkException()).onSuccess { consumed = it }
        assertNull(consumed)

        ApiResult.Loading.onSuccess { consumed = "should not call" }
        assertNull(consumed)
    }

    @Test
    fun `onError 仅在 Error 时触发`() {
        var consumed: AppException? = null
        val exception = AppException.HttpException(404)

        ApiResult.Error(exception).onError { consumed = it }
        assertSame(exception, consumed)

        consumed = null
        ApiResult.Success("ok").onError { consumed = it }
        assertNull(consumed)

        ApiResult.Loading.onError { consumed = AppException.NetworkException() }
        assertNull(consumed)
    }

    @Test
    fun `onLoading 仅在 Loading 时触发`() {
        var triggered = false

        ApiResult.Loading.onLoading { triggered = true }
        assertTrue(triggered)

        triggered = false
        ApiResult.Success("ok").onLoading { triggered = true }
        assertFalse(triggered)

        ApiResult.Error(AppException.NetworkException()).onLoading { triggered = true }
        assertFalse(triggered)
    }

    @Test
    fun `onSuccess 链式调用返回原始结果`() {
        val result: ApiResult<String> = ApiResult.Success("ok")
        val returned = result.onSuccess { /* no-op */ }
        assertSame(result, returned)
    }
}
