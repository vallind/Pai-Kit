// ============================================================================
// AuthRepository.kt
// 鉴权仓库接口（domain layer，决策 P1-1：KMP-ready，无 Android 依赖）
// 实现见 core/data/AuthRepositoryImpl.kt（@Inject constructor + @Binds 绑定）
// ============================================================================

package com.pai.app.core.domain

import com.pai.app.core.base.ApiResult
import com.pai.app.core.domain.model.UserItem

/**
 * 鉴权仓库接口（domain layer）
 *
 * 决策 P1-1：接口在 [com.pai.app.core.domain] 包（KMP-ready，**无 Android / Retrofit /
 * Room 依赖**，Konsist 守护）；实现在
 * [com.pai.app.core.data.AuthRepositoryImpl]（`@Inject constructor` + `@Singleton`）。
 *
 * feature 层（如 [com.pai.app.feature.auth.AuthViewModel]）注入本接口而非实现，
 * 由 Hilt `@Binds`（见 [com.pai.app.core.data.di.DataModule]）解析到 Impl。
 * 这样实现的替换（如 KMP 时 iOS 各自实现、测试时 mockk）对调用方零感知。
 *
 * 决策 8：本接口**不**返回 Room `UserEntity`（`internal`，不外泄），改返回
 * domain model [UserItem]（[com.pai.app.core.data.AuthRepositoryImpl] 内部完成映射）。
 *
 * 业务方拉取脚手架后：
 * 1. 修改 [login] 方法为真实后端登录接口（在 Impl 中用
 *    [com.pai.app.core.network.safeApiCall] 包装）
 * 2. 添加 `register` / `refreshToken` 等方法到本接口，Impl 同步实现
 * 3. 删除 Impl 中的「模拟登录」逻辑（包括 `email.contains("fail")` 模拟失败路径）
 *
 * 注意：登录态持久化（userId + token）的职责不在本接口 —— 由
 * [com.pai.app.navigation.UserState.onLoginSuccess] 负责，feature 在收到
 * [ApiResult.Success] 后调用之。
 */
interface AuthRepository {

    /**
     * 登录
     *
     * 当前模拟实现（[com.pai.app.core.data.AuthRepositoryImpl]）：
     * - email 含 "fail"（大小写不敏感）时返回 [ApiResult.Error]
     *   （[com.pai.app.core.base.AppException.BusinessException]，code = -1），
     *   让 UI 的 Error 分支可被验证（Medium #17）
     * - 否则构造虚拟 [UserItem] 返回成功，并把对应 [com.pai.app.core.database.entity.UserEntity]
     *   upsert 到 Room（仅缓存用户信息，不持久化登录态）
     *
     * @param email 邮箱（含 "fail" 触发模拟失败路径，演示 Error 分支）
     * @return 成功返回 [UserItem]，失败返回 [ApiResult.Error]
     */
    suspend fun login(email: String): ApiResult<UserItem>
}
