// ============================================================================
// NetworkModule.kt
// Hilt 网络模块：提供 Json / OkHttp / Retrofit / AppApi 单例
// ============================================================================

package com.pai.app.core.network

import com.pai.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 网络依赖注入模块
 *
 * 注册到 [SingletonComponent]，提供以下单例：
 * 1. [Json] - kotlinx.serialization JSON 解析器
 * 2. [OkHttpClient] - 含 [HeaderInterceptor] / [TokenAuthenticator] / 日志拦截器 / 30s 超时
 * 3. [Retrofit] - 通过 kotlinx-serialization ConverterFactory 解析 JSON
 * 4. [AppApi] - 由 Retrofit 创建的 API 接口实现
 *
 * 注意：Hilt 要求 `@Module` 与 `@Provides` 方法均为 public 可见性。
 *
 * [HeaderInterceptor] / [TokenAuthenticator] 均带 `@Inject constructor` + `@Singleton`，
 * Hilt 自动构造，无需在本模块 `@Provides`。
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /** 30 秒超时阈值（兼顾弱网与中等大小响应体） */
    private const val TIMEOUT_SECONDS = 30L

    /**
     * 提供 JSON 解析器
     *
     * 配置说明：
     * - `ignoreUnknownKeys`：忽略后端返回的多余字段，提升兼容性
     * - `isLenient`：宽松模式，允许部分非标准 JSON
     * - `encodeDefaults`：序列化时输出默认值
     * - `explicitNulls`：禁用显式 null 输出，避免给后端带来歧义
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        explicitNulls = false
    }

    /**
     * 提供 OkHttp 客户端
     *
     * 拦截器链：
     * 1. [HeaderInterceptor] - 统一注入 Content-Type / Accept / Authorization（决策 5：零阻塞缓存）
     * 2. [HttpLoggingInterceptor] - Debug 构建打印请求/响应日志，Release 关闭；
     *    `redactHeader("Authorization")` 防止 token 进 logcat（决策 4 / High #10）
     * 3. [ChuckerInterceptor] - Debug 构建弹通知抓包，Release 自动 noop
     *
     * Authenticator：
     * - [TokenAuthenticator] - 401 时 emit TokenExpired + 清登录态（决策 2 + 决策 4）
     *
     * Chucker 使用方式：
     * - Debug 模式自动拦截所有 HTTP 请求
     * - 在通知栏点击可查看完整请求/响应
     * - 或在 App 内启动 Chucker Activity 查看历史
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: android.content.Context,
        headerInterceptor: HeaderInterceptor,
        tokenAuthenticator: TokenAuthenticator,
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            // 决策 4：脱敏 Authorization 头，避免 Bearer token 进 logcat
            redactHeader(HEADER_AUTHORIZATION)
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        val builder = OkHttpClient.Builder()
            .addInterceptor(headerInterceptor)
            .addInterceptor(loggingInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

        // Chucker 仅 Debug 构建启用，Release 通过 library-noop 自动失效
        if (BuildConfig.DEBUG) {
            val chuckerInterceptor = com.chuckerteam.chucker.api.ChuckerInterceptor.Builder(context)
                .build()
            builder.addInterceptor(chuckerInterceptor)
        }

        return builder.build()
    }

    /**
     * 提供 Retrofit 实例
     *
     * - baseUrl 取自 BuildConfig.BASE_URL（在 app/build.gradle.kts 中配置）
     * - 使用 kotlinx-serialization ConverterFactory 解析 JSON
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        json: Json,
    ): Retrofit {
        val contentType = "application/json".toMediaType()
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()
    }

    /**
     * 提供 [AppApi] 接口实现
     */
    @Provides
    @Singleton
    fun provideAppApi(retrofit: Retrofit): AppApi =
        retrofit.create(AppApi::class.java)

    private const val HEADER_AUTHORIZATION = "Authorization"
}
