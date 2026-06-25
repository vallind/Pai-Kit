// ============================================================================
// ExampleDto.kt
// 示例网络数据模型（业务方参考后删除）
// ============================================================================

package com.pai.app.core.network.model

import kotlinx.serialization.Serializable

/**
 * 示例 DTO（Data Transfer Object）
 *
 * 仅作模板演示，业务方拉取脚手架后应：
 * 1. 删除此文件
 * 2. 在 `core/network/model/` 下创建真实业务 DTO
 * 3. 所有 DTO 必须加 `@Serializable` 注解
 *
 * 示例：
 * ```kotlin
 * @Serializable
 * data class ProductDto(
 *     val id: Long,
 *     val name: String,
 *     val price: Double,
 *     val imageUrl: String? = null,
 * )
 * ```
 *
 * AI 规则：
 * - DTO 仅用于网络传输，**禁止**直接传给 Composable
 * - UI 层用 `core.appstate.model` 下的纯 data class 或 feature 内的 UiState
 * - DTO 与 Entity（Room）字段可不同，通过 Repository 转换
 */
@Serializable
data class ExampleDto(
    val id: Long = 0,
    val name: String = "",
    val description: String? = null,
)
