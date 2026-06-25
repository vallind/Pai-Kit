// ============================================================================
// AuthViewModel.kt
// 登录页 ViewModel：邮箱密码表单 + 字段校验 + 模拟登录
// 继承 BaseViewModel，演示通用登录模板
// ============================================================================
package com.pai.app.feature.auth

import androidx.lifecycle.viewModelScope
import com.pai.app.core.base.ApiResult
import com.pai.app.core.base.BaseViewModel
import com.pai.app.core.base.UiState
import com.pai.app.core.domain.AuthRepository
import com.pai.app.navigation.AppNavigator
import com.pai.app.navigation.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 登录页 UI 状态
 *
 * M2：实现 [UiState] 标记接口（团队约定，详见 [UiState] KDoc）。
 *
 * - [email] / [password]：表单输入
 * - [emailError] / [passwordError]：字段级错误（为 null 表示无错误，由 DSTextField 直接展示）
 * - [loginError]：登录失败错误（为 null 表示无错误，由 DSDialog 弹窗展示）
 * - [isLoginSuccess]：登录成功一次性标记，UI 消费后通过 [AuthViewModel.resetLoginSuccess] 清空
 */
internal data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginError: String? = null,
    val isLoginSuccess: Boolean = false,
) : UiState

/**
 * 登录页 ViewModel
 *
 * 继承 [BaseViewModel] 演示通用登录模板：
 * 1. 维护邮箱 / 密码表单状态与字段级校验
 * 2. 调用 [AuthRepository.login] 完成模拟登录（返回 [com.pai.app.core.domain.model.UserItem]，不持久化）
 * 3. 登录成功后通过 [UserState.onLoginSuccess] 同步登录态（DataStore + 内存），
 *    并通知 UI 跳转
 *
 * 业务方拉取脚手架后：
 * - 修改 [login] 调用真实后端登录 API
 * - 添加注册 / 忘记密码等方法
 *
 * @param authRepository 鉴权仓库
 * @param navigator 全局导航器（BaseViewModel 注入）
 * @param userState 全局登录状态（BaseViewModel 注入）
 */
@HiltViewModel
internal class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    navigator: AppNavigator,
    userState: UserState,
) : BaseViewModel(navigator, userState) {

    private val _uiState = MutableStateFlow(AuthUiState())

    /** 登录页 UI 状态流 */
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    /** 邮箱正则：用户名允许字母 / 数字 / + _ . -；域名允许字母 / 数字 / . - */
    private val emailRegex = Regex(EMAIL_PATTERN)

    /** 邮箱输入变化回调 */
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, emailError = null) }
    }

    /** 密码输入变化回调 */
    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, passwordError = null) }
    }

    /**
     * 触发登录
     *
     * 流程：
     * 1. 字段级校验（邮箱非空 + 格式、密码非空），不通过则更新对应错误并立即返回
     * 2. 进入 loading 态，禁用登录按钮
     * 3. 调用 [AuthRepository.login]（模拟登录，仅写 Room，不持久化登录态）
     * 4. 成功：[UserState.onLoginSuccess] 持久化登录态 + 内存同步 + isLoginSuccess = true
     * 5. 失败：将异常 message 写入 loginError，由 UI 弹 DSDialog 展示
     *
     * H16 例外说明：本方法手写 `when(result){ Loading/Success/Error }` 属于表单页例外
     * （Auth 含表单字段 + 登录态持久化等自定义流程，不适合 BaseNetWorkViewModel 的纯三态抽象，
     * 故不采用 DSNetWorkView 自动三态渲染）。参考 docs/rules/06-viewmodel.md 对表单页的例外说明。
     * `ApiResult.Loading` 分支不可达（safeApiCall 不返回 Loading），仅占位满足 sealed when。
     */
    fun login() {
        val current = _uiState.value

        // 字段校验
        val emailError: String? = when {
            current.email.isBlank() -> "请输入邮箱"
            !emailRegex.matches(current.email) -> "邮箱格式不正确"
            else -> null
        }
        val passwordError: String? = if (current.password.isBlank()) "请输入密码" else null

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError)
            }
            return
        }

        _uiState.update { it.copy(isLoading = true, loginError = null) }

        viewModelScope.launch {
            when (val result = authRepository.login(current.email)) {
                is ApiResult.Success -> {
                    // 持久化登录态到 DataStore + 同步内存状态（RouteInterceptor 自动感知）
                    userState.onLoginSuccess(
                        userId = result.data.id,
                        token = "fake-token-${System.currentTimeMillis()}",
                    )
                    _uiState.update {
                        it.copy(isLoading = false, isLoginSuccess = true)
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            loginError = result.exception.message ?: "登录失败",
                        )
                    }
                }
                is ApiResult.Loading -> {
                    // H16：safeApiCall 不会返回 Loading，此处仅占位以满足 sealed when 分支
                }
            }
        }
    }

    /** 重置登录成功标记 */
    fun resetLoginSuccess() {
        _uiState.update { it.copy(isLoginSuccess = false) }
    }

    /** 清空登录失败提示 */
    fun clearLoginError() {
        _uiState.update { it.copy(loginError = null) }
    }

    private companion object {
        /** 邮箱正则：参考 RFC 5322 简化版 */
        private const val EMAIL_PATTERN = """^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"""
    }
}
