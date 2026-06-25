// ============================================================================
// DSTextField.kt
// 文本输入框 - 基于 M3 TextField 扩展
// 提供 2 种风格：Filled / Outlined
// 集成 label / placeholder / error / icon / trailing
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 输入框风格
 */
internal enum class DSTextFieldStyle {
    Filled,    // 填充式（有底色）
    Outlined   // 描边式
}

/**
 * DSTextField - 文本输入框
 *
 * 使用示例：
 * ```kotlin
 * var text by remember { mutableStateOf("") }
 * DSTextField(
 *     value = text,
 *     onValueChange = { text = it },
 *     label = "用户名",
 *     placeholder = "请输入用户名",
 *     isError = false
 * )
 * ```
 *
 * 设计规范：
 * - 默认高度 56dp
 * - 默认圆角 medium (12dp)
 * - 错误状态：边框/底边变红，显示错误文字
 * - 禁用状态：透明度降低
 *
 * @param value 当前文本
 * @param onValueChange 文本变化回调
 * @param modifier 修饰符
 * @param label 标签（始终显示，浮动）
 * @param placeholder 占位符（仅 value 为空时显示）
 * @param isError 是否错误
 * @param errorMessage 错误信息（isError = true 时显示）
 * @param enabled 是否可用
 * @param singleLine 是否单行
 * @param maxLines 最大行数（仅在 singleLine = false 时生效），默认 1
 * @param maxLength 最大字符数（非 null 时启用字数统计）
 * @param style 输入框风格
 * @param keyboardType 键盘类型
 * @param isPassword 是否密码（显示切换按钮）
 * @param leadingIcon 前置图标
 * @param trailingIcon 后置图标
 * @param onTrailingIconClick 后置图标点击回调
 */
@Composable
internal fun DSTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    maxLength: Int? = null,
    style: DSTextFieldStyle = DSTextFieldStyle.Outlined,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null
) {
    // 密码可见性状态
    var passwordVisible by remember { mutableStateOf(false) }

    // 计算实际 maxLines（singleLine = true 时强制 1）
    val actualMaxLines = if (singleLine) 1 else maxLines

    // supportingText：错误信息或字数统计
    val supportingText: (@Composable () -> Unit)? = when {
        errorMessage != null -> { { Text(errorMessage, color = MaterialTheme.colorScheme.error) } }
        maxLength != null -> {
            { Text("${value.length} / $maxLength", style = MaterialTheme.typography.bodySmall) }
        }
        else -> null
    }

    // 实际值：超过 maxLength 时截断
    val actualValue = if (maxLength != null && value.length > maxLength) value.take(maxLength) else value
    val actualOnValueChange: (String) -> Unit = if (maxLength != null) {
        { newValue -> if (newValue.length <= maxLength) onValueChange(newValue) }
    } else onValueChange

    val actualPasswordVisible = isPassword && passwordVisible

    // 计算视觉转换
    val visualTransformation: VisualTransformation =
        if (isPassword && !passwordVisible) PasswordVisualTransformation()
        else VisualTransformation.None

    // 键盘类型修正
    val actualKeyboardType = if (isPassword) KeyboardType.Password else keyboardType

    // 颜色配置
    val colors = TextFieldDefaults.colors(
        focusedContainerColor = if (style == DSTextFieldStyle.Filled)
            MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        unfocusedContainerColor = if (style == DSTextFieldStyle.Filled)
            MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        disabledContainerColor = if (style == DSTextFieldStyle.Filled)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent,
        errorContainerColor = if (style == DSTextFieldStyle.Filled)
            MaterialTheme.colorScheme.errorContainer else Color.Transparent,

        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
        disabledIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
        errorIndicatorColor = MaterialTheme.colorScheme.error,

        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        errorTextColor = MaterialTheme.colorScheme.onSurface,

        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        errorLabelColor = MaterialTheme.colorScheme.error,

        focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        errorPlaceholderColor = MaterialTheme.colorScheme.error,

        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),

        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),

        cursorColor = MaterialTheme.colorScheme.primary,
    )

    // 后置图标内容
    @Composable
    fun trailingIconContent() {
        when {
            isPassword -> {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff
                        else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "隐藏密码" else "显示密码",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            trailingIcon != null && onTrailingIconClick != null -> {
                IconButton(onClick = onTrailingIconClick) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            trailingIcon != null -> {
                Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // 前置图标
    @Composable
    fun leadingIconContent() {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (style == DSTextFieldStyle.Outlined) {
        OutlinedTextField(
            value = actualValue,
            onValueChange = actualOnValueChange,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = actualMaxLines,
            label = label?.let { { Text(it, style = MaterialTheme.typography.bodyMedium) } },
            placeholder = placeholder?.let {
                { Text(it, style = MaterialTheme.typography.bodyMedium) }
            },
            leadingIcon = if (leadingIcon != null) { { leadingIconContent() } } else null,
            trailingIcon = if (isPassword || trailingIcon != null) { { trailingIconContent() } } else null,
            isError = isError,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = actualKeyboardType),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(DSTokens.Radius.medium),
            colors = colors
        )
    } else {
        TextField(
            value = actualValue,
            onValueChange = actualOnValueChange,
            modifier = modifier.fillMaxWidth(),
            enabled = enabled,
            readOnly = readOnly,
            singleLine = singleLine,
            maxLines = actualMaxLines,
            label = label?.let { { Text(it, style = MaterialTheme.typography.bodyMedium) } },
            placeholder = placeholder?.let {
                { Text(it, style = MaterialTheme.typography.bodyMedium) }
            },
            leadingIcon = if (leadingIcon != null) { { leadingIconContent() } } else null,
            trailingIcon = if (isPassword || trailingIcon != null) { { trailingIconContent() } } else null,
            isError = isError,
            supportingText = supportingText,
            visualTransformation = visualTransformation,
            keyboardOptions = KeyboardOptions(keyboardType = actualKeyboardType),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(
                topStart = DSTokens.Radius.medium,
                topEnd = DSTokens.Radius.medium
            ),
            colors = colors
        )
    }
}

@Preview(showBackground = true, name = "TextField - Default")
@Composable
private fun DSTextFieldPreview() {
    DSDesignTheme {
        var text by remember { mutableStateOf("Hello World") }
        DSTextField(
            value = text,
            onValueChange = { text = it },
            label = "用户名",
            placeholder = "请输入用户名"
        )
    }
}

@Preview(showBackground = true, name = "TextField - Password")
@Composable
private fun DSTextFieldPasswordPreview() {
    DSDesignTheme {
        var text by remember { mutableStateOf("password123") }
        DSTextField(
            value = text,
            onValueChange = { text = it },
            label = "密码",
            isPassword = true
        )
    }
}

@Preview(showBackground = true, name = "TextField - Error")
@Composable
private fun DSTextFieldErrorPreview() {
    DSDesignTheme {
        var text by remember { mutableStateOf("invalid") }
        DSTextField(
            value = text,
            onValueChange = { text = it },
            label = "邮箱",
            isError = true,
            errorMessage = "邮箱格式不正确"
        )
    }
}

@Preview(showBackground = true, name = "TextField - Multi-line + Counter")
@Composable
private fun DSTextFieldMultiLineCounterPreview() {
    DSDesignTheme {
        var text by remember { mutableStateOf("这是一段多行文本") }
        DSTextField(
            value = text,
            onValueChange = { text = it },
            label = "描述",
            singleLine = false,
            maxLines = 5,
            maxLength = 200
        )
    }
}

@Preview(showBackground = true, name = "TextField - Filled")
@Composable
private fun DSTextFieldFilledPreview() {
    DSDesignTheme {
        var text by remember { mutableStateOf("Hello") }
        DSTextField(
            value = text,
            onValueChange = { text = it },
            label = "姓名",
            style = DSTextFieldStyle.Filled
        )
    }
}
