// ============================================================================
// ExampleRepository.kt
// 示例仓库接口（domain layer，决策 P1-1：KMP-ready，无 Android 依赖）
// 实现见 core/data/ExampleRepositoryImpl.kt（@Inject constructor + @Binds 绑定）
// ============================================================================

package com.pai.app.core.domain

import com.pai.app.core.base.ApiResult
import com.pai.app.core.domain.model.ExampleItem
import kotlinx.coroutines.flow.Flow

/**
 * 示例 Repository 接口（domain layer）
 *
 * 决策 P1-1：接口在 [com.pai.app.core.domain] 包（KMP-ready，**无 Android / Retrofit /
 * Room 依赖**，Konsist 守护）；实现在
 * [com.pai.app.core.data.ExampleRepositoryImpl]（`@Inject constructor` + `@Singleton`）。
 *
 * 仅作模板演示，业务方拉取脚手架后应：
 * 1. 删除本接口 + Impl + model/ExampleItem
 * 2. 在 `core/domain/` 下创建真实业务 Repository 接口
 * 3. 在 `core/data/` 下创建 Impl，由 [com.pai.app.core.data.di.DataModule] `@Binds`
 *
 * 设计要点（决策 1 + 决策 8 + P1-1）：
 * 1. 本接口只暴露 domain model [ExampleItem] —— **不**返回 Room Entity 或网络 DTO
 * 2. 网络请求返回 [ApiResult]（决策 1：唯一规范结果类型）
 * 3. 数据库观察返回 `Flow<ApiResult<...>>`，便于 UI 层订阅
 *
 * 注：DTO → Entity 的本地缓存同步（`syncToCache`）属于实现层细节，
 * 不在本接口暴露（避免 domain 接口依赖 [com.pai.app.core.network.model.ExampleDto]）。
 * 如需触发同步，由 Impl 内部在 [getExamples] 成功后自动完成。
 */
interface ExampleRepository {

    /**
     * 从网络拉取示例数据
     *
     * 用 [com.pai.app.core.network.safeApiCall] 包装网络请求，返回 [ApiResult]；
     * 内部把 DTO 列表映射为 domain model [ExampleItem] 列表（决策 8：不外泄 DTO）。
     *
     * @return 成功返回 [ExampleItem] 列表，失败返回 [ApiResult.Error]
     */
    suspend fun getExamples(): ApiResult<List<ExampleItem>>

    /**
     * 观察本地缓存的示例数据
     *
     * 返回 `Flow<ApiResult<List<ExampleItem>>>`，便于 UI 层直接配合
     * [com.pai.app.core.base.asResult] 或单独订阅。Room Flow 内部把
     * [com.pai.app.core.database.entity.ExampleEntity] 映射为 [ExampleItem] 后暴露，
     * Entity 不外泄。
     */
    fun observeExamples(): Flow<ApiResult<List<ExampleItem>>>
}
