// ============================================================================
// AuthScreenTest.kt
// Compose UI 测试 - 登录页交互验证（Hilt 接入版，决策 H2/H17）
// ============================================================================

package com.pai.app.feature.auth

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.pai.app.HiltComponentActivity
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * AuthScreen Compose UI 测试（Hilt 接入版）
 *
 * 验证登录页的关键交互：
 * - 邮箱输入框显示并能输入
 * - 密码输入框显示并能输入
 * - 登录按钮显示
 * - 空表单时点击登录触发校验（显示错误提示）
 *
 * Hilt 接入（H2/H17）：
 * - [HiltTestRunner]（app/build.gradle.kts `testInstrumentationRunner`）已替换默认 Runner
 * - [HiltAndroidRule] + [HiltAndroidTest] 注入测试依赖图
 * - [HiltComponentActivity] 标 `@AndroidEntryPoint`，让 `hiltViewModel()` 在
 *   Composable 内能正确解析 [AuthViewModel]（@HiltViewModel）
 *
 * 业务逻辑由 AuthViewModelTest 单元测试覆盖，本类只验证纯 UI 交互。
 */
@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class AuthScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<HiltComponentActivity>()

    @Before
    fun setUp() {
        // 触发 Hilt 测试依赖图注入；在 setContent 前调用，确保
        // hiltViewModel() 解析 AuthViewModel 时依赖图已就绪
        hiltRule.inject()
    }

    @Test
    fun `登录页显示标题与登录按钮`() {
        composeRule.setContent {
            DSDesignTheme {
                AuthScreen(onLoginSuccess = {})
            }
        }

        composeRule.onNodeWithText("欢迎回来").assertIsDisplayed()
        composeRule.onNodeWithText("登录").assertIsDisplayed()
    }

    @Test
    fun `输入邮箱与密码后表单状态更新`() {
        composeRule.setContent {
            DSDesignTheme {
                AuthScreen(onLoginSuccess = {})
            }
        }

        // 输入邮箱（输入框 placeholder 为"请输入邮箱"）
        composeRule.onNodeWithText("邮箱").assertIsDisplayed()
        composeRule.onNodeWithText("邮箱").performTextInput("test@example.com")

        // 输入密码
        composeRule.onNodeWithText("密码").assertIsDisplayed()
        composeRule.onNodeWithText("密码").performTextInput("password123")
    }

    @Test
    fun `点击登录按钮空表单触发校验`() {
        composeRule.setContent {
            DSDesignTheme {
                AuthScreen(onLoginSuccess = {})
            }
        }

        // 点击登录按钮（空表单应触发校验）
        composeRule.onNodeWithText("登录").performClick()

        // 校验后应显示"请输入邮箱"错误提示
        composeRule.onNodeWithText("请输入邮箱").assertIsDisplayed()
    }
}
