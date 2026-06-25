// ============================================================================
// AppApiTest.kt
// 网络层 MockWebServer 集成测试：Retrofit + safeApiCall 端到端异常映射
// H6：docs/rules/10-testing.md "测试禁止真实网络请求（用 MockWebServer）" 落地
// ============================================================================

package com.pai.app.core.network

import com.pai.app.core.base.ApiResult
import com.pai.app.core.base.AppException
import com.pai.app.core.network.model.ExampleDto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException

/**
 * 网络层 MockWebServer 集成测试
 *
 * H6 落地：docs/rules/10-testing.md 要求"测试禁止真实网络请求（用 MockWebServer）"，
 * 但本项目此前无 MockWebServer 测试（仅声明依赖）。本类补齐端到端验证：
 *
 * 用真实 Retrofit + OkHttp + kotlinx.serialization ConverterFactory + MockWebServer，
 * 验证 [safeApiCall] 对真实 HTTP 响应的异常分类是否正确：
 * - HTTP 200 + 合法 JSON → [ApiResult.Success] 携带 DTO 列表
 * - HTTP 404 → [ApiResult.Error] + [AppException.HttpException]（httpCode=404）
 * - HTTP 500 → [ApiResult.Error] + [AppException.HttpException]（httpCode=500）
 * - 网络断开（SocketPolicy.DISCONNECT_AT_START）→ [ApiResult.Error] + [AppException.NetworkException]
 *
 * 注：MockWebServer + Retrofit + kotlinx.serialization 均为纯 JVM 库，
 * 本测试无需 Robolectric；放在 com.pai.app.core.network 包下访问 internal [safeApiCall]。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppApiTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var api: AppApi

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
            explicitNulls = false
        }
        val contentType = "application/json".toMediaType()
        api = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
            .create(AppApi::class.java)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `HTTP 200 合法 JSON 返回 ApiResult Success 携带 DTO 列表`() = runTest {
        // 模拟后端返回 ExampleDto 列表 JSON
        val body = """[{"id":1,"name":"Item1","description":"Desc1"},{"id":2,"name":"Item2"}]"""
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody(body),
        )

        val result = safeApiCall { api.getExamples() }

        assertTrue(result is ApiResult.Success)
        val data = (result as ApiResult.Success).data
        assertEquals(2, data.size)
        assertEquals(1L, data[0].id)
        assertEquals("Item1", data[0].name)
        assertEquals("Desc1", data[0].description)
        assertEquals(2L, data[1].id)
        assertEquals("Item2", data[1].name)
        assertEquals(null, data[1].description)
    }

    @Test
    fun `HTTP 404 返回 ApiResult Error 且为 HttpException 携带 404 状态码`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(404)
                .setBody("Not Found"),
        )

        val result = safeApiCall<List<ExampleDto>> { api.getExamples() }

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is AppException.HttpException)
        assertEquals(404, (error.exception as AppException.HttpException).httpCode)
    }

    @Test
    fun `HTTP 500 返回 ApiResult Error 且为 HttpException 携带 500 状态码`() = runTest {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(500)
                .setBody("Internal Server Error"),
        )

        val result = safeApiCall<List<ExampleDto>> { api.getExamples() }

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is AppException.HttpException)
        assertEquals(500, (error.exception as AppException.HttpException).httpCode)
    }

    @Test
    fun `网络层断开 返回 ApiResult Error 且为 NetworkException`() = runTest {
        // DISCONNECT_AT_START：服务端接受连接后立即关闭，模拟网络层断开
        // Retrofit + OkHttp 会抛 IOException（如 "Broken pipe" / "Connection reset"）
        mockWebServer.enqueue(
            MockResponse()
                .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START),
        )

        val result = safeApiCall<List<ExampleDto>> { api.getExamples() }

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue("Expected NetworkException, got ${error.exception::class.simpleName}",
            error.exception is AppException.NetworkException)
    }

    @Test
    fun `HTTP 200 但 JSON 格式错误 返回 ApiResult Error 为 SerializationException`() = runTest {
        // kotlinx.serialization 在解析非法 JSON 时抛 SerializationException
        // safeApiCall 应将其归类为 AppException.SerializationException
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("not-json-at-all"),
        )

        val result = safeApiCall<List<ExampleDto>> { api.getExamples() }

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue("Expected SerializationException, got ${error.exception::class.simpleName}",
            error.exception is AppException.SerializationException)
    }
}
