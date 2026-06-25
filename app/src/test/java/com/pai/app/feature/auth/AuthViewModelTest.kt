// ============================================================================
// AuthViewModelTest.kt
// AuthViewModel 单元测试：表单校验 + 模拟登录成功 / 失败 + 状态重置
// 适配 BaseViewModel + AuthRepository + UserState 架构
// ============================================================================

package com.pai.app.feature.auth

import app.cash.turbine.test
import com.pai.app.core.base.ApiResult
import com.pai.app.core.base.AppException
import com.pai.app.core.datastore.UserPreferences
import com.pai.app.core.domain.AuthRepository
import com.pai.app.core.domain.model.UserItem
import com.pai.app.navigation.AppNavigator
import com.pai.app.navigation.UserState
import com.pai.app.testing.MainDispatcherRule
import io.mockk.any
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.eq
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * [AuthViewModel] 单元测试
 *
 * 适配新架构：AuthViewModel 继承 BaseViewModel，注入 AuthRepository + AppNavigator + UserState。
 * UserState 现在依赖 (@ApplicationScope CoroutineScope, UserPreferences)，
 * 测试中用 [TestScope] 作为应用作用域、mockk UserPreferences。
 *
 * 决策 5（单一真相源）：[UserState.isLoggedIn] 在新设计中直接派生自
 * `userPreferences.isLoggedIn`，故此处额外 mock `userPreferences.isLoggedIn` 为
 * MutableStateFlow 并在 `saveLogin` / `clearLogin` 时切换其值，
 * 以兼容 OLD（UserState 持有独立 _isLoggedIn）与 NEW（派生自 UserPreferences）两种实现。
 *
 * 验证以下行为：
 * - 初始 UI 状态（email/password 空、isLoading/isLoginSuccess 为 false）
 * - onEmailChange / onPasswordChange 更新字段并清空对应错误
 * - login() 字段校验（空邮箱 / 邮箱格式不正确 / 空密码）
 * - login() 成功：isLoginSuccess = true、UserState.isLoggedIn = true、UserPreferences.saveLogin 被调用
 * - login() 失败：loginError 非空、isLoading false
 * - login() 业务失败路径（email 含 "fail"）触发 BusinessException（Medium #17）
 * - resetLoginSuccess / clearLoginError 重置一次性标记
 *
 * Turbine 时序修复（M2）：StateFlow 在订阅后会重放当前值作为首帧，
 * 故 `viewModel.uiState.test { ... }` 中第一个 `awaitItem()` 是当前状态，
 * `viewModel.login()` 必须放在 `awaitItem()` 拿到初始状态之后调用，
 * 否则会把初始状态误判为 loading 帧。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val appNavigator: AppNavigator = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)

    /** 应用级协程作用域，用于构造 UserState（替代 @ApplicationScope 注入） */
    private val appScope = TestScope(mainDispatcherRule.testDispatcher)

    private val userState: UserState = UserState(appScope, userPreferences)

    /** 登录态 Flow，由 mock 的 saveLogin / clearLogin 同步更新（兼容决策 5 单一真相源） */
    private val isLoggedInFlow = MutableStateFlow(false)

    /** 测试用 UserItem（domain model），模拟登录返回的用户 */
    private val userItem = UserItem(
        id = 1L,
        name = "测试用户",
        username = "tester",
        email = "test@example.com",
        phone = null,
        avatarUrl = null,
    )

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        // 决策 5：UserState.isLoggedIn 在新设计中派生自 userPreferences.isLoggedIn
        // —— mock 为 MutableStateFlow 并在 saveLogin / clearLogin 时切换，使
        // `userState.isLoggedIn.value` 在两种实现下都正确反映登录态
        every { userPreferences.isLoggedIn } returns isLoggedInFlow
        coEvery { userPreferences.saveLogin(any(), any()) } answers {
            isLoggedInFlow.value = true
        }
        coEvery { userPreferences.clearLogin() } answers {
            isLoggedInFlow.value = false
        }

        viewModel = AuthViewModel(authRepository, appNavigator, userState)
    }

    @Test
    fun `初始 uiState 字段均为默认值`() = runTest(mainDispatcherRule.testDispatcher) {
        val state = viewModel.uiState.value
        assertEquals("", state.email)
        assertEquals("", state.password)
        assertFalse(state.isLoading)
        assertFalse(state.isLoginSuccess)
        assertNull(state.emailError)
        assertNull(state.passwordError)
        assertNull(state.loginError)
    }

    @Test
    fun `onEmailChange 更新 email 并清空 emailError`() = runTest(mainDispatcherRule.testDispatcher) {
        // 制造 emailError
        viewModel.onEmailChange("invalid-format")
        viewModel.login()
        assertNotNull(viewModel.uiState.value.emailError)

        // 重新输入应清空错误
        viewModel.onEmailChange("new@example.com")
        assertEquals("new@example.com", viewModel.uiState.value.email)
        assertNull(viewModel.uiState.value.emailError)
    }

    @Test
    fun `onPasswordChange 更新 password 并清空 passwordError`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // 制造 passwordError（email 合法但密码为空）
            viewModel.onEmailChange("test@example.com")
            viewModel.login()
            assertNotNull(viewModel.uiState.value.passwordError)

            // 输入密码应清空错误
            viewModel.onPasswordChange("123456")
            assertEquals("123456", viewModel.uiState.value.password)
            assertNull(viewModel.uiState.value.passwordError)
        }

    @Test
    fun `login 空邮箱时设置 emailError 且不发起请求`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.login()

        assertNotNull(viewModel.uiState.value.emailError)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 0) { authRepository.login(any()) }
    }

    @Test
    fun `login 邮箱格式不正确时 emailError 为 邮箱格式不正确`() =
        runTest(mainDispatcherRule.testDispatcher) {
            viewModel.onEmailChange("invalid-format")
            viewModel.onPasswordChange("123456")
            viewModel.login()

            assertEquals("邮箱格式不正确", viewModel.uiState.value.emailError)
            assertFalse(viewModel.uiState.value.isLoading)
            coVerify(exactly = 0) { authRepository.login(any()) }
        }

    @Test
    fun `login 空密码时设置 passwordError 且不发起请求`() = runTest(mainDispatcherRule.testDispatcher) {
        viewModel.onEmailChange("test@example.com")
        viewModel.login()

        assertNotNull(viewModel.uiState.value.passwordError)
        assertFalse(viewModel.uiState.value.isLoading)
        coVerify(exactly = 0) { authRepository.login(any()) }
    }

    @Test
    fun `login 成功后 isLoginSuccess 为 true 且 UserState 同步为已登录`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { authRepository.login(any()) } returns ApiResult.Success(userItem)

            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("123456")

            // M2 修复：StateFlow 订阅后先收到当前值（初始状态），调用 login() 后再
            // awaitItem 才能拿到 loading 帧；最后 advanceUntilIdle 让协程跑完拿到 final 帧
            viewModel.uiState.test {
                // 1. 先消费初始状态（isLoading=false）
                val initial = awaitItem()
                assertFalse(initial.isLoading)
                assertFalse(initial.isLoginSuccess)

                // 2. 触发 login —— 同步设置 isLoading=true
                viewModel.login()

                // 3. 等待 loading 帧
                val loading = awaitItem()
                assertTrue(loading.isLoading)
                assertFalse(loading.isLoginSuccess)

                // 4. 让协程跑完（authRepository.login + userState.onLoginSuccess）
                advanceUntilIdle()

                // 5. 最终状态：isLoading=false、isLoginSuccess=true、loginError=null
                val final = awaitItem()
                assertFalse(final.isLoading)
                assertTrue(final.isLoginSuccess)
                assertNull(final.loginError)
            }

            // 验证 UserState 已同步为已登录（UserState.onLoginSuccess 已写内存 / 派生 Flow）
            assertTrue(userState.isLoggedIn.value)
            // 验证 UserState.onLoginSuccess 调用了 UserPreferences.saveLogin 持久化
            coVerify { userPreferences.saveLogin(eq(userItem.id), any()) }
        }

    @Test
    fun `login 失败时 loginError 非空且 isLoading 为 false`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val errorMsg = "网络连接失败"
            coEvery { authRepository.login(any()) } returns ApiResult.Error(
                AppException.NetworkException(message = errorMsg),
            )

            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("123456")
            viewModel.login()

            // login 同步设置 isLoading = true，协程未跑前先验证
            assertTrue(viewModel.uiState.value.isLoading)

            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertNotNull(state.loginError)
            assertEquals(errorMsg, state.loginError)
            assertFalse(state.isLoading)
            assertFalse(state.isLoginSuccess)
            // 失败不应同步登录状态
            assertFalse(userState.isLoggedIn.value)
        }

    @Test
    fun `login email 含 fail 时返回 BusinessException 并写 loginError`() =
        runTest(mainDispatcherRule.testDispatcher) {
            // Medium #17：AuthRepository 在 email.contains("fail") 时返回 BusinessException
            // 这里直接 mock repository 返回该异常，验证 AuthViewModel Error 分支可触达
            val errorMsg = "模拟登录失败"
            coEvery { authRepository.login(any()) } returns ApiResult.Error(
                AppException.BusinessException(message = errorMsg, code = -1),
            )

            viewModel.onEmailChange("fail@test.com")
            viewModel.onPasswordChange("123456")

            // M2 时序修复：先消费初始帧，再 login()，再拿 loading 帧
            viewModel.uiState.test {
                val initial = awaitItem()
                assertFalse(initial.isLoading)

                viewModel.login()

                val loading = awaitItem()
                assertTrue(loading.isLoading)
                assertFalse(loading.isLoginSuccess)

                advanceUntilIdle()

                val final = awaitItem()
                assertFalse(final.isLoading)
                assertFalse(final.isLoginSuccess)
                assertNotNull(final.loginError)
                assertEquals(errorMsg, final.loginError)
            }

            // 失败不应同步登录状态、不应调用 saveLogin
            assertFalse(userState.isLoggedIn.value)
            coVerify(exactly = 0) { userPreferences.saveLogin(any(), any()) }
        }

    @Test
    fun `resetLoginSuccess 将 isLoginSuccess 置为 false`() =
        runTest(mainDispatcherRule.testDispatcher) {
            coEvery { authRepository.login(any()) } returns ApiResult.Success(userItem)
            viewModel.onEmailChange("test@example.com")
            viewModel.onPasswordChange("123456")
            viewModel.login()
            advanceUntilIdle()
            assertTrue(viewModel.uiState.value.isLoginSuccess)

            viewModel.resetLoginSuccess()

            assertFalse(viewModel.uiState.value.isLoginSuccess)
        }

    @Test
    fun `clearLoginError 将 loginError 置为 null`() = runTest(mainDispatcherRule.testDispatcher) {
        coEvery { authRepository.login(any()) } returns ApiResult.Error(
            AppException.NetworkException(message = "登录失败"),
        )
        viewModel.onEmailChange("test@example.com")
        viewModel.onPasswordChange("123456")
        viewModel.login()
        advanceUntilIdle()
        assertNotNull(viewModel.uiState.value.loginError)

        viewModel.clearLoginError()

        assertNull(viewModel.uiState.value.loginError)
    }
}
