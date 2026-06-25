// ============================================================================
// AuthRepositoryImpl.kt
// 鉴权仓库实现（决策 P1-1：实现层 @Inject constructor + @Singleton）
// 接口在 core/domain/AuthRepository.kt；Hilt @Binds 见 core/data/di/DataModule.kt
// ============================================================================

package com.pai.app.core.data

import com.pai.app.core.base.ApiResult
import com.pai.app.core.base.AppException
import com.pai.app.core.database.dao.UserDao
import com.pai.app.core.database.entity.UserEntity
import com.pai.app.core.domain.AuthRepository
import com.pai.app.core.domain.model.UserItem
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 鉴权仓库实现
 *
 * 决策 P1-1：实现层 `@Inject constructor` + `@Singleton`，由
 * [com.pai.app.core.data.di.DataModule] 通过 `@Binds` 绑定到
 * [AuthRepository] 接口。feature 层注入接口（不感知 Impl）。
 *
 * 架构说明（与 UserState 协作）：
 * - 本类只负责「凭证校验 + 用户数据持久化（Room）」，不再负责登录态持久化
 * - 登录态持久化（userId + token）由
 *   [com.pai.app.navigation.UserState.onLoginSuccess] 负责
 *   （userId 写入 DataStore；token 由 UserPreferences 委托给 EncryptedPrefs，决策 4）
 * - 退出登录清理由 [com.pai.app.navigation.UserState.logout] 负责
 *
 * 决策 8：本 Impl 内部把 [UserEntity]（Room，`internal`）映射为
 * [UserItem]（domain model，`public`）后再通过接口返回，Entity 不外泄。
 *
 * 当前实现：模拟登录（无真实后端）
 * - email 含 "fail" 时返回 [ApiResult.Error]（业务异常），让 UI 的 Error 分支可被验证（Medium #17）
 * - 否则构造虚拟 [UserEntity] 写入 [UserDao] 持久化用户基本信息，
 *   再映射为 [UserItem] 返回成功
 * - 不再持久化登录态（token / userId）—— 该职责已迁移到
 *   [com.pai.app.navigation.UserState.onLoginSuccess]
 *
 * 业务方拉取脚手架后：
 * 1. 修改 [login] 方法为真实后端登录接口（用
 *    [com.pai.app.core.network.safeApiCall] 包装 `api.login(...)`）
 * 2. 添加 `register` / `refreshToken` 等方法到 [AuthRepository] 接口 + 本 Impl 同步实现
 * 3. 删除「模拟登录」逻辑（包括下面的 `email.contains("fail")` 模拟失败路径）
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
) : AuthRepository {

    /**
     * 模拟登录（业务方替换为真实 API）
     *
     * 当前实现：
     * - 不发起网络请求
     * - email 含 "fail" 时返回 [ApiResult.Error]（业务异常，演示 Error 分支可触达）
     * - 否则构造虚拟 [UserEntity] 写入 [UserDao] 持久化用户信息，
     *   再映射为 [UserItem] 返回成功
     * - 不写 DataStore / EncryptedPrefs（由调用方
     *   [com.pai.app.navigation.UserState.onLoginSuccess] 处理）
     *
     * 业务方改造示例：
     * ```kotlin
     * override suspend fun login(email: String, password: String): ApiResult<UserItem> {
     *     val result = safeApiCall { api.login(LoginRequest(email, password)) }
     *     if (result is ApiResult.Success) {
     *         val entity = result.data.toEntity()
     *         userDao.upsert(entity)
     *         return ApiResult.Success(entity.toItem())
     *     }
     *     return result
     * }
     * ```
     *
     * @param email 邮箱（含 "fail" 时模拟登录失败，便于 UI 错误流验证）
     * @return 成功返回虚拟用户（[UserItem]，domain model），失败返回 [ApiResult.Error]
     */
    override suspend fun login(email: String): ApiResult<UserItem> {
        // 模拟网络延迟
        delay(500)

        // Medium #17：模拟一次失败路径，让 AuthViewModel.Error 分支可被验证
        // 业务方接入真实后端后删除此 if 块
        if (email.contains("fail", ignoreCase = true)) {
            return ApiResult.Error(
                AppException.BusinessException(
                    message = "模拟登录失败",
                    code = -1,
                ),
            )
        }

        // 构造虚拟用户（业务方替换为真实 API 返回）
        val user = UserEntity(
            id = 1L,
            name = "ZAI 用户",
            username = email.substringBefore("@"),
            email = email,
            phone = null,
            avatarUrl = null,
        )

        // 写入数据库
        userDao.upsert(user)

        // 注意：登录态（userId + token）持久化已迁移到
        // UserState.onLoginSuccess，由 AuthViewModel 在收到 Success 后调用
        return ApiResult.Success(user.toItem())
    }

    // ------------------------------------------------------------------------
    // 内部转换：Entity → domain model（不暴露给外部）
    // ------------------------------------------------------------------------

    /** Entity → domain model（决策 8：不外泄 Entity） */
    private fun UserEntity.toItem(): UserItem = UserItem(
        id = id,
        name = name,
        username = username,
        email = email,
        phone = phone,
        avatarUrl = avatarUrl,
    )
}
