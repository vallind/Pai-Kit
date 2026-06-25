// ============================================================================
// UserDao.kt
// 用户数据访问对象（Room DAO）
// ============================================================================

package com.pai.app.core.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.pai.app.core.database.entity.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * 用户 DAO
 *
 * 提供用户表的增删查改能力，包含：
 * - 单条 / 批量 upsert（插入或更新）
 * - 按 ID / 邮箱查询
 * - 监听全表变化（返回 [Flow]）
 * - 计数、删除、清空
 *
 * 注意：此接口必须保持 public 可见性（由 Hilt `@Provides` 返回），
 * 但其中暴露 [UserEntity] 的方法标记为 `internal`，避免实体类型外泄（决策 8）。
 */
@Dao
interface UserDao {

    /** 插入或更新单个用户 */
    @Upsert
    suspend fun upsert(user: UserEntity)

    /** 批量插入或更新用户 */
    @Upsert
    suspend fun upsertAll(users: List<UserEntity>)

    /** 按 ID 查询用户，不存在返回 null */
    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): UserEntity?

    /** 按邮箱精确查询用户（用于登录校验），不存在返回 null */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getByEmail(email: String): UserEntity?

    /** 观察全部用户，按入库时间倒序排列 */
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<UserEntity>>

    /** 删除指定用户 */
    @Delete
    suspend fun delete(user: UserEntity)

    /** 清空用户表（慎用） */
    @Query("DELETE FROM users")
    suspend fun clearAll()
}
