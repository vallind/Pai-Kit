// ============================================================================
// ExampleRepositoryImpl.kt
// 示例仓库实现（决策 P1-1：实现层 @Inject constructor + @Singleton）
// 接口在 core/domain/ExampleRepository.kt；Hilt @Binds 见 core/data/di/DataModule.kt
// 业务方参考后删除
// ============================================================================

package com.pai.app.core.data

import com.pai.app.core.base.ApiResult
import com.pai.app.core.base.map
import com.pai.app.core.database.dao.ExampleDao
import com.pai.app.core.database.entity.ExampleEntity
import com.pai.app.core.domain.ExampleRepository
import com.pai.app.core.domain.model.ExampleItem
import com.pai.app.core.network.AppApi
import com.pai.app.core.network.model.ExampleDto
import com.pai.app.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 示例 Repository 实现
 *
 * 决策 P1-1：实现层 `@Inject constructor` + `@Singleton`，由
 * [com.pai.app.core.data.di.DataModule] 通过 `@Binds` 绑定到
 * [ExampleRepository] 接口。feature 层注入接口（不感知 Impl）。
 *
 * 仅作模板演示，业务方拉取脚手架后应：
 * 1. 删除此文件 + 接口 + ExampleItem
 * 2. 在 `core/data/` 下创建真实业务 Repository Impl，由 [com.pai.app.core.data.di.DataModule] 绑定
 *
 * 演示 Repository 如何组合多个数据源：
 * - [AppApi] - 网络数据源（Retrofit 接口返回**纯 DTO**，决策 1）
 * - [ExampleDao] - 数据库缓存
 *
 * 设计要点（决策 1 + 决策 8 + P1-1）：
 * 1. Impl 是 `@Inject constructor` 具体类，由 Hilt `@Binds` 绑定到
 *    [ExampleRepository] 接口（决策 P1-1：feature 注入接口）
 * 2. 网络请求用 [safeApiCall] 包装返回 [ApiResult]（决策 1：唯一规范结果类型）
 * 3. 数据库操作返回 `Flow` 便于 UI 层订阅
 * 4. **不向外暴露 [ExampleEntity] / [ExampleDto]**：转换为 domain model [ExampleItem]
 *
 * 示例（业务方参考）：
 * ```kotlin
 * @Singleton
 * class ProductRepositoryImpl @Inject constructor(
 *     private val api: AppApi,
 *     private val productDao: ProductDao,
 * ) : ProductRepository {
 *     override suspend fun getProducts(): ApiResult<List<ProductItem>> =
 *         safeApiCall { api.getProducts() }.map { dtos -> dtos.map { it.toItem() } }
 *
 *     override fun observeProducts(): Flow<ApiResult<List<ProductItem>>> =
 *         productDao.observeAll().map { entities -> ApiResult.Success(entities.map { it.toItem() }) }
 *
 *     suspend fun syncToCache(items: List<ProductItem>) {
 *         productDao.upsertAll(items.map { it.toEntity() })
 *     }
 * }
 * ```
 */
@Singleton
class ExampleRepositoryImpl @Inject constructor(
    private val api: AppApi,
    private val exampleDao: ExampleDao,
) : ExampleRepository {

    /**
     * 从网络拉取示例数据
     *
     * 用 [safeApiCall] 包装 `api.getExamples()`，返回 [ApiResult]；
     * 内部把 DTO 列表映射为 domain model [ExampleItem] 列表。
     *
     * @return 成功返回 [ExampleItem] 列表，失败返回 [ApiResult.Error]
     */
    override suspend fun getExamples(): ApiResult<List<ExampleItem>> =
        safeApiCall { api.getExamples() }.map { dtos -> dtos.map { it.toItem() } }

    /**
     * 观察本地缓存的示例数据
     *
     * 返回 `Flow<ApiResult<List<ExampleItem>>>`，便于 UI 层直接配合 [com.pai.app.core.base.asResult]
     * 或单独订阅。Room Flow 内部把 [ExampleEntity] 映射为 [ExampleItem] 后暴露，
     * Entity 不外泄。
     */
    override fun observeExamples(): Flow<ApiResult<List<ExampleItem>>> =
        exampleDao.observeAll()
            .map { entities -> ApiResult.Success(entities.map { it.toItem() }) }

    /**
     * 将网络数据同步到本地缓存
     *
     * 演示 DTO → Entity 转换（Entity 仅供 Repository 内部使用）。
     * 本方法不属于 [ExampleRepository] 接口（接口不暴露 DTO 类型，KMP-ready），
     * 仅供 Impl 内部或测试调用。
     */
    suspend fun syncToCache(examples: List<ExampleDto>) {
        exampleDao.upsertAll(examples.map { it.toEntity() })
    }

    // ------------------------------------------------------------------------
    // 内部转换：DTO ↔ Entity ↔ domain model（不暴露给外部）
    // ------------------------------------------------------------------------

    /** DTO → domain model */
    private fun ExampleDto.toItem(): ExampleItem = ExampleItem(
        id = id,
        name = name,
        description = description,
    )

    /** Entity → domain model */
    private fun ExampleEntity.toItem(): ExampleItem = ExampleItem(
        id = id,
        name = name,
        description = description,
    )

    /** DTO → Entity */
    private fun ExampleDto.toEntity(): ExampleEntity = ExampleEntity(
        id = id,
        name = name,
        description = description,
    )
}
