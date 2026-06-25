// ============================================================================
// UserEntity.kt
// 用户持久化实体（Room）
// ============================================================================

package com.pai.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户实体
 *
 * 对应数据库表 `users`，作为用户信息的本地缓存。
 *
 * @param id 用户 ID（主键，对应 [com.pai.app.core.network.model.UserDto.id] 的 Long 形式）
 * @param name 显示名
 * @param username 登录名
 * @param email 邮箱
 * @param phone 电话（可能为空）
 * @param avatarUrl 头像 URL（可能为空，后端目前未返回，留扩展位）
 * @param createdAt 入库时间戳（毫秒）
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val username: String,
    val email: String,
    val phone: String?,
    val avatarUrl: String?,
    val createdAt: Long = System.currentTimeMillis(),
)
