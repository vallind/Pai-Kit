// ============================================================================
// ExampleDao.kt
// 示例数据访问对象（业务方参考后删除）
// ============================================================================

package com.pai.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pai.app.core.database.entity.ExampleEntity
import kotlinx.coroutines.flow.Flow

/**
 * 示例 Room DAO
 *
 * 仅作模板演示，业务方拉取脚手架后应：
 * 1. 删除此文件
 * 2. 在 `core/database/dao/` 下创建真实业务 DAO
 * 3. 在 `DatabaseModule` 中 `@Provides` 提供 DAO
 *
 * 示例：
 * ```kotlin
 * @Dao
 * interface ProductDao {
 *     @Insert(onConflict = OnConflictStrategy.REPLACE)
 *     suspend fun upsertAll(products: List<ProductEntity>)
 *
 *     @Query("SELECT * FROM products ORDER BY id DESC")
 *     fun observeAll(): Flow<List<ProductEntity>>
 *
 *     @Query("SELECT * FROM products WHERE id = :id")
 *     suspend fun getById(id: Long): ProductEntity?
 *
 *     @Query("DELETE FROM products")
 *     suspend fun clearAll()
 * }
 * ```
 *
 * AI 规则：
 * - DAO 方法用 `suspend` 或返回 `Flow`
 * - 禁止用 `Single<ListenableFuture>` 等旧式 API
 * - SQL 语句加注释说明意图
 */
@Dao
interface ExampleDao {

    /** 插入或更新（主键冲突时替换） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ExampleEntity>)

    /** 观察全部，按创建时间倒序 */
    @Query("SELECT * FROM examples ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<ExampleEntity>>

    /** 按 ID 查询 */
    @Query("SELECT * FROM examples WHERE id = :id")
    suspend fun getById(id: Long): ExampleEntity?

    /** 清空表 */
    @Query("DELETE FROM examples")
    suspend fun clearAll()
}
