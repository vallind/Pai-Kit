// ============================================================================
// AuthRepositoryTest.kt
// AuthRepositoryImpl 单元测试：模拟登录成功 / 失败 / UserEntity 持久化
// 决策 1 + Medium #17 + P1-1：测试构造 AuthRepositoryImpl，断言对接口 AuthRepository
// ============================================================================

package com.pai.app.core.data

import com.pai.app.core.base.ApiResult
import com.pai.app.core.base.AppException
import com.pai.app.core.database.dao.UserDao
import com.pai.app.core.database.entity.UserEntity
import com.pai.app.core.domain.AuthRepository
import com.pai.app.core.domain.model.UserItem
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * [AuthRepositoryImpl] 单元测试
 *
 * 决策 P1-1：本测试构造 [AuthRepositoryImpl]（实现），但断言类型为
 * [AuthRepository]（接口）—— 验证调用方仅依赖接口，不感知实现。
 *
 * 决策 1：[AuthRepository.login] 返回 [ApiResult]<[UserItem]>（domain model，
 * 不外泄 Room [UserEntity]）。
 * Medium #17：email 含 "fail" 时返回 [ApiResult.Error] + [AppException.BusinessException]，
 * 让 UI 的 Error 分支可被验证。
 *
 * 职责边界（重要）：
 * - [AuthRepositoryImpl.login] 成功路径仅把 [UserEntity] upsert 到 [UserDao]
 * - 登录态持久化（userId + token 写 DataStore/EncryptedPrefs）由
 *   [com.pai.app.navigation.UserState.onLoginSuccess] 负责，调用方为
 *   [com.pai.app.feature.auth.AuthViewModel]，**不在 AuthRepository 范围内**
 *   —— 故本测试只验证 userDao.upsert 调用，不验证 saveLogin
 *
 * 验证：
 * - login 普通邮箱返回 Success + [UserItem] 字段正确 + userDao.upsert 被调用
 * - login 邮箱含 "fail"（大小写不敏感）返回 Error + BusinessException
 * - login 失败路径不写库（userDao.upsert 不被调用）
 * - login 成功返回的 [UserItem] 字段与 email 派生一致
 *
 * 用 MockK mock [UserDao]；[AuthRepositoryImpl] 不直接依赖 AppApi（模拟登录）。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryTest {

    private val userDao: UserDao = mockk(relaxed = true)

    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        // 决策 P1-1：构造 Impl，赋值给接口类型 —— 验证调用方仅依赖接口
        repository = AuthRepositoryImpl(userDao)
    }

    @Test
    fun `login 普通邮箱返回 Success 且 UserItem 字段正确`() = runTest {
        val email = "tester@example.com"

        val result = repository.login(email)

        assertTrue(result is ApiResult.Success)
        val user = (result as ApiResult.Success).data
        assertEquals(1L, user.id)
        assertEquals(email, user.email)
        assertEquals("tester", user.username) // substringBefore("@")
    }

    @Test
    fun `login 成功后调用 userDao upsert 持久化 UserEntity`() = runTest {
        val email = "alice@example.com"

        repository.login(email)

        coVerify {
            userDao.upsert(match { entity ->
                entity.email == email &&
                    entity.username == "alice" &&
                    entity.id == 1L
            })
        }
    }

    @Test
    fun `login 邮箱含 fail 返回 Error 且为 BusinessException`() = runTest {
        val result = repository.login("fail@test.com")

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is AppException.BusinessException)
        assertEquals(-1, (error.exception as AppException.BusinessException).code)
    }

    @Test
    fun `login 邮箱含 FAIL 大小写不敏感同样触发失败`() = runTest {
        val result = repository.login("FAIL@test.com")

        assertTrue(result is ApiResult.Error)
        assertTrue((result as ApiResult.Error).exception is AppException.BusinessException)
    }

    @Test
    fun `login 失败路径不写库`() = runTest {
        repository.login("fail@test.com")

        // 失败时不应 upsert 任何 UserEntity
        coVerify(exactly = 0) { userDao.upsert(any()) }
    }

    @Test
    fun `login 成功路径返回的 UserItem 字段匹配 email 派生`() = runTest {
        val email = "bob.smith+tag@host.io"

        val result = repository.login(email)

        assertTrue(result is ApiResult.Success)
        val user = (result as ApiResult.Success).data
        assertEquals(email, user.email)
        // substringBefore("@")：username 派生为 "bob.smith+tag"
        assertEquals("bob.smith+tag", user.username)
        // name 默认占位（业务方替换为真实 API 返回）
        assertEquals("ZAI 用户", user.name)
        // phone / avatarUrl 默认 null
        assertEquals(null, user.phone)
        assertEquals(null, user.avatarUrl)
    }
}
