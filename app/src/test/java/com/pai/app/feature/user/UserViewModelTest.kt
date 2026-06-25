// ============================================================================
// UserViewModelTest.kt
// UserViewModel 单元测试：主题模式 + 动态颜色 + 退出登录
// ============================================================================

package com.pai.app.feature.user

import com.pai.app.core.datastore.ThemeMode
import com.pai.app.core.datastore.ThemePreferences
import com.pai.app.core.datastore.UserPreferences
import com.pai.app.navigation.AppNavigator
import com.pai.app.navigation.UserState
import com.pai.app.testing.MainDispatcherRule
import io.mockk.any
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * [UserViewModel] 单元测试
 *
 * 适配新架构：UserViewModel 构造参数为 (ThemePreferences, AppNavigator, UserState)，
 * 退出登录继承自 [com.pai.app.core.base.BaseViewModel.logout]，委托 [UserState.logout]
 * 清空 DataStore + 重置内存状态。
 *
 * 测试策略：[ThemePreferences] / [UserPreferences] 均为具体 `@Inject constructor` 类
 * （非接口），无法用 Fake 类替代 —— 直接用 MockK mock 真实类，并通过 `answers`
 * 将 `setThemeMode` / `setDynamicColor` 桥接到内部 MutableStateFlow，
 * 模拟真实 DataStore 的"写入即更新"行为。
 *
 * 决策 5（单一真相源）：[UserState.isLoggedIn] 在新设计中直接派生自
 * `userPreferences.isLoggedIn`，故此处额外 mock `userPreferences.isLoggedIn` 为
 * MutableStateFlow 并在 `saveLogin` / `clearLogin` 时切换其值，
 * 以兼容 OLD（UserState 持有独立 _isLoggedIn）与 NEW（派生自 UserPreferences）两种实现。
 *
 * 验证：
 * - 初始 uiState：themeMode System、dynamicColor false
 * - setThemeMode(Dark) 后 themeMode 变为 Dark
 * - setDynamicColor(true) 后 dynamicColor 变为 true
 * - logout() 通过 BaseViewModel.logout 委托 UserState.logout 调用 userPreferences.clearLogin()
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UserViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val themePreferences: ThemePreferences = mockk(relaxed = true)
    private val userPreferences: UserPreferences = mockk(relaxed = true)
    private val appNavigator: AppNavigator = mockk(relaxed = true)

    /** 应用级协程作用域，用于构造 UserState */
    private val appScope = TestScope(mainDispatcherRule.testDispatcher)

    private val userState: UserState = UserState(appScope, userPreferences)

    /** 主题模式 Flow，由 mock 的 setThemeMode 同步更新 */
    private val themeModeFlow = MutableStateFlow(ThemeMode.System)

    /** 动态颜色 Flow，由 mock 的 setDynamicColor 同步更新 */
    private val dynamicColorFlow = MutableStateFlow(false)

    /** 登录态 Flow，由 mock 的 saveLogin / clearLogin 同步更新（兼容决策 5 单一真相源） */
    private val isLoggedInFlow = MutableStateFlow(false)

    @Before
    fun setUp() {
        // 桥接 mock 属性到可控 Flow
        every { themePreferences.themeMode } returns themeModeFlow
        every { themePreferences.dynamicColor } returns dynamicColorFlow
        // mock 的 setter 直接更新 Flow，模拟真实 DataStore 行为
        coEvery { themePreferences.setThemeMode(any()) } answers {
            themeModeFlow.value = firstArg()
        }
        coEvery { themePreferences.setDynamicColor(any()) } answers {
            dynamicColorFlow.value = firstArg()
        }

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
    }

    @Test
    fun `初始 uiState themeMode 为 System 且 dynamicColor 为 false`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = UserViewModel(themePreferences, appNavigator, userState)

            // 订阅以触发 stateIn 上游收集（WhileSubscribed 需有订阅者才收集）
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(ThemeMode.System, state.themeMode)
            assertFalse(state.dynamicColor)

            job.cancel()
        }

    @Test
    fun `setThemeMode Dark 后 themeMode 变为 Dark`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = UserViewModel(themePreferences, appNavigator, userState)
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            assertEquals(ThemeMode.System, viewModel.uiState.value.themeMode)

            viewModel.setThemeMode(ThemeMode.Dark)
            advanceUntilIdle()

            assertEquals(ThemeMode.Dark, viewModel.uiState.value.themeMode)
            coVerify { themePreferences.setThemeMode(ThemeMode.Dark) }
            job.cancel()
        }

    @Test
    fun `setThemeMode AMOLED 后 themeMode 变为 AMOLED`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = UserViewModel(themePreferences, appNavigator, userState)
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            assertEquals(ThemeMode.System, viewModel.uiState.value.themeMode)

            viewModel.setThemeMode(ThemeMode.AMOLED)
            advanceUntilIdle()

            assertEquals(ThemeMode.AMOLED, viewModel.uiState.value.themeMode)
            coVerify { themePreferences.setThemeMode(ThemeMode.AMOLED) }
            job.cancel()
        }

    @Test
    fun `setDynamicColor true 后 dynamicColor 变为 true`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = UserViewModel(themePreferences, appNavigator, userState)
            val job = launch { viewModel.uiState.collect { } }
            advanceUntilIdle()
            assertFalse(viewModel.uiState.value.dynamicColor)

            viewModel.setDynamicColor(true)
            advanceUntilIdle()

            assertTrue(viewModel.uiState.value.dynamicColor)
            coVerify { themePreferences.setDynamicColor(true) }
            job.cancel()
        }

    @Test
    fun `logout 通过 BaseViewModel 委托 UserState logout 调用 clearLogin`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = UserViewModel(themePreferences, appNavigator, userState)

            viewModel.logout()
            advanceUntilIdle()

            coVerify { userPreferences.clearLogin() }
        }
}
