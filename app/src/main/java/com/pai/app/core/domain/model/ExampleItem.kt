// ============================================================================
// ExampleItem.kt
// 示例领域模型（domain layer，决策 P1-1 + 决策 8：Repository 不向外暴露 Entity）
// 业务方参考后删除
// ============================================================================

package com.pai.app.core.domain.model

data class ExampleItem(
    val id: Long,
    val name: String,
    val description: String?,
)
