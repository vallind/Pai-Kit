// ============================================================================
// AuthScreen.kt
// 登录页：居中表单 + Logo + 错误对话框
// ============================================================================
package com.pai.app.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSButtonSize
import com.pai.app.core.designsystem.primitives.DSButtonStyle
import com.pai.app.core.designsystem.overlays.DSDialog
import com.pai.app.core.designsystem.overlays.DSDialogType
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.primitives.DSTextField
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * AuthScreen - 登录页
 *
 * 居中布局自上而下：
 * 1. Logo（圆形容器 + "Z" 字母，品牌主色背景）
 * 2. 标题 "欢迎回来"（HeadlineMedium）
 * 3. 副标题 "请登录您的账号"（BodyMedium）
 * 4. 邮箱输入框（前置 Email 图标）
 * 5. 密码输入框（isPassword，自带可见性切换）
 * 6. "登录" 按钮（Filled + Large + 全宽，loading 时显示进度）
 *
 * 交互：
 * - 字段错误直接展示在 DSTextField 下方 supportingText
 * - 登录失败弹 DSDialog（Error 类型）展示 [AuthUiState.loginError]
 * - 登录成功通过 LaunchedEffect 触发 [onLoginSuccess]，并 reset 标记避免重复触发
 *
 * @param onLoginSuccess 登录成功跳转回调
 * @param viewModel 注入的 AuthViewModel
 */
@Composable
internal fun AuthScreen(
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 登录成功一次性事件：触发跳转并重置标记，避免配置变更后再次跳转
    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            onLoginSuccess()
            viewModel.resetLoginSuccess()
        }
    }

    // 登录失败对话框：loginError 非空时展示
    if (uiState.loginError != null) {
        DSDialog(
            onDismiss = { viewModel.clearLoginError() },
            title = "登录失败",
            message = uiState.loginError ?: "",
            type = DSDialogType.Error,
            confirmText = "确定",
            onConfirm = { viewModel.clearLoginError() },
            dismissText = null,
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DSTokens.Spacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg),
        ) {
            // Logo：圆形容器 + "Z" 字母
            Box(
                modifier = Modifier
                    .size(DSTokens.ComponentHeight.topBar)
                    .clip(CircleShape)
                    .background(DSTokens.Brand.indigo600),
                contentAlignment = Alignment.Center,
            ) {
                DSText(
                    text = "Z",
                    variant = DSTextVariant.HeadlineMedium,
                    color = DSTextColor.OnPrimary,
                )
            }

            // 标题
            DSText(
                text = "欢迎回来",
                variant = DSTextVariant.HeadlineMedium,
                color = DSTextColor.Primary,
            )

            // 副标题
            DSText(
                text = "请登录您的账号",
                variant = DSTextVariant.BodyMedium,
                color = DSTextColor.Secondary,
            )

            Spacer(Modifier.height(DSTokens.Spacing.sm))

            // 邮箱输入框
            DSTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                label = "邮箱",
                placeholder = "请输入邮箱",
                isError = uiState.emailError != null,
                errorMessage = uiState.emailError,
                keyboardType = KeyboardType.Email,
                leadingIcon = Icons.Default.Email,
            )

            // 密码输入框
            DSTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                label = "密码",
                placeholder = "请输入密码",
                isError = uiState.passwordError != null,
                errorMessage = uiState.passwordError,
                isPassword = true,
                leadingIcon = Icons.Default.Lock,
            )

            Spacer(Modifier.height(DSTokens.Spacing.sm))

            // 登录按钮（Filled + Large + 全宽）
            DSButton(
                text = "登录",
                onClick = viewModel::login,
                modifier = Modifier.fillMaxWidth(),
                style = DSButtonStyle.Filled,
                size = DSButtonSize.Large,
                enabled = !uiState.isLoading,
                loading = uiState.isLoading,
            )
        }
    }
}
