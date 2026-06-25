// ============================================================================
// ExampleEntity.kt
// 示例数据库实体（业务方参考后删除）
// ============================================================================

package com.pai.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 示例 Room Entity
 *
 * 仅作模板演示，业务方拉取脚手架后应：
 * 1. 删除此文件
 * 2. 在 `core/database/entity/` 下创建真实业务 Entity
 * 3. 所有 Entity 必须加 `@Entity` 注解
 * 4. 在 `AppDatabase` 的 `entities` 数组中注册
 *
 * 示例：
 * ```kotlin
 * @Entity(
 *     tableName = "products",
 *     indices = [Index("categoryId")],
 * )
 * data class ProductEntity(
 *     @PrimaryKey val id: Long,
 *     val name: String,
 *     val price: Double,
 *     val imageUrl: String?,
 *     val createdAt: Long = System.currentTimeMillis(),
 * )
 * ```
 *
 * AI 规则：
 * - Entity 仅用于数据库存储，**禁止**直接传给 Composable
 * - Entity 与 DTO 字段可不同，通过 Repository 转换
 * - 字段加 `@PrimaryKey` 标记主键
 * - 索引字段加 `@Index` 提升查询性能
 */
@Entity(
    tableName = "examples",
    indices = [Index("name")],
)
data class ExampleEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Long = System.currentTimeMillis(),
)
