// ============================================================================
// AppApi.kt
// 应用 API 接口（通用模板）
// 业务方在此添加业务接口，删除示例后开始开发
// ============================================================================

package com.pai.app.core.network

import com.pai.app.core.network.model.ExampleDto
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * 应用 API 接口
 *
 * 业务方拉取脚手架后，在此添加业务接口。
 *
 * 约定（决策 1）：
 * - Retrofit 接口返回**纯 DTO**（如 `List<ExampleDto>`），不返回 sealed class
 * - 异常由 [safeApiCall] 在 Repository 中统一包装为 [com.pai.app.core.base.ApiResult]
 * - 路径不含 baseUrl（baseUrl 在 `app/build.gradle.kts` 的 `BuildConfig.BASE_URL` 配置）
 * - 请求体用 `@Body request: XxxRequest`，自动序列化
 *
 * 示例（业务方参考后删除）：
 * ```kotlin
 * interface AppApi {
 *     @GET("examples")
 *     suspend fun getExamples(): List<ExampleDto>
 *
 *     @GET("examples/{id}")
 *     suspend fun getExample(@Path("id") id: String): ExampleDto
 * }
 * ```
 *
 * AI 规则：业务方添加新接口时，对应的 Repository 在 `core/data/` 创建。
 */
interface AppApi {

    /**
     * 示例接口（业务方参考后删除）
     *
     * 仅作模板演示，业务方拉取脚手架后应：
     * 1. 删除此方法
     * 2. 添加真实业务接口
     * 3. 在 `core/data/ExampleRepository.kt` 中调用
     *
     * 注意（Low #26）：当前默认 `BASE_URL` 为 jsonplaceholder.typicode.com，
     * 该域名下并不存在 `/examples` 路径，调用会 404 —— 仅用于签名演示，
     * 业务方接入真实后端前不应依赖此接口可成功返回。
     */
    @GET("examples")
    suspend fun getExamples(): List<ExampleDto>

    /**
     * 示例接口：按 ID 获取（业务方参考后删除）
     *
     * 同上，仅作签名演示。
     */
    @GET("examples/{id}")
    suspend fun getExample(@Path("id") id: String): ExampleDto
}
