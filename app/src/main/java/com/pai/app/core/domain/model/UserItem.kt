// ============================================================================
// UserItem.kt
// 用户领域模型（domain layer，决策 P1-1：KMP-ready）
// 由 AuthRepositoryImpl 从 UserEntity（Room）映射生成，不外泄 Entity
// ============================================================================

package com.pai.app.core.domain.model

/**
 * 用户领域模型
 *
 * 决策 P1-1：domain 层（[com.pai.app.core.domain]）必须 KMP-ready —— 纯 Kotlin
 * data class，**不得** import `android.*` / `androidx.*` / `retrofit2.*` /
 * `com.pai.app.core.network.*` / `com.pai.app.core.database.*`。
 * 未来 KMP 迁移时，本文件原样移到 `shared/commonMain`。
 *
 * 由 [com.pai.app.core.data.AuthRepositoryImpl] 在登录成功后从
 * [com.pai.app.core.database.entity.UserEntity]（Room Entity，`internal`）映射生成，
 * 通过 [com.pai.app.core.domain.AuthRepository] 接口暴露给 feature 层。
 *
 * 字段与 [com.pai.app.core.database.entity.UserEntity] 对齐（除 `createdAt` 仅持久化用）：
 * - [id] 用户 ID
 * - [name] 显示名
 * - [username] 登录名
 * - [email] 邮箱
 * - [phone] 电话（可空）
 * - [avatarUrl] 头像 URL（可空）
 *
 * @param id 用户 ID
 * @param name 显示名
 * @param username 登录名（如邮箱前缀）
 * @param email 邮箱
 * @param phone 电话（可能为空）
 * @param avatarUrl 头像 URL（可能为空，后端目前未返回，留扩展位）
 */
data class UserItem(
    val id: Long,
    val name: String,
    val username: String,
    val email: String,
    val phone: String?,
    val avatarUrl: String?,
)
