// ============================================================================
// ExampleItem.kt
// 示例领域模型（决策 8：Repository 不向外暴露 Entity）
// 业务方参考后删除
// ============================================================================

package com.pai.app.core.data.model

/**
 * 示例领域模型
 *
 * 决策 8：Repository 是唯一的数据出口，禁止把 Room Entity（[com.pai.app.core.database.entity.ExampleEntity]）
 * 或网络 DTO（[com.pai.app.core.network.model.ExampleDto]）直接暴露给 feature/UI 层。
 * 本 data class 是 ExampleRepository 对外的领域模型。
 *
 * 业务方拉取脚手架后应：
 * 1. 删除此文件
 * 2. 在 `core/data/model/` 下创建真实业务 domain model
 * 3. 在 Repository 内部完成 Entity ↔ domain model ↔ DTO 的转换
 *
 * @param id 主键
 * @param name 名称
 * @param description 描述（可能为空）
 */
data class ExampleItem(
    val id: Long,
    val name: String,
    val description: String?,
)
