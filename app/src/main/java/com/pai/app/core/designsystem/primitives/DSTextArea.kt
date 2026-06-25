// ============================================================================
// DSTextArea.kt
// 多行文本输入框 - DSTextField 的多行预设
// 提供最常用的多行配置：5 行最大、120dp 最小高度、可选字数统计
// ============================================================================

package com.pai.app.core.designsystem.primitives

import androidx.compose.foundation.layout.heightIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSTextArea - 多行文本输入框
 *
 * [DSTextField] 的多行预设，提供最常用的长文本输入配置：
 * - 默认 5 行最大、120dp 最小高度
 * - 默认 Outlined 风格
 * - 集成字数统计（通过 [maxLength] 启用）
 *
 * 如需更细粒度控制（如 Filled 风格、密码、自定义图标），直接使用 [DSTextField]：
 * ```kotlin
 * DSTextField(
 *     value = text,
 *     onValueChange = { text = it },
 *     label = "评论",
 *     singleLine = false,
 *     maxLines = 8,
 *     maxLength = 500
 * )
 * ```
 *
 * 使用示例：
 * ```kotlin
 * var comment by remember { mutableStateOf("") }
 * DSTextArea(
 *     value = comment,
 *     onValueChange = { comment = it },
 *     label = "评论",
 *     placeholder = "说点什么吧...",
 *     maxLength = 200
 * )
 * ```
 *
 * @param value 当前文本
 * @param onValueChange 文本变化回调（若启用 maxLength，内部会做裁剪）
 * @param modifier 修饰符
 * @param label 标签
 * @param placeholder 占位符
 * @param isError 是否错误
 * @param errorMessage 错误信息
 * @param enabled 是否可用
 * @param maxLength 最大字符数（非 null 时启用字数统计）
 * @param minHeight 最小高度，默认 120dp
 * @param maxLines 最大行数，默认 5
 */
@Composable
internal fun DSTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    maxLength: Int? = null,
    minHeight: Dp = DSTokens.ComponentHeight.textAreaMin,
    maxLines: Int = 5
) {
    DSTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.heightIn(min = minHeight),
        label = label,
        placeholder = placeholder,
        isError = isError,
        errorMessage = errorMessage,
        enabled = enabled,
        singleLine = false,
        maxLines = maxLines,
        maxLength = maxLength,
        style = DSTextFieldStyle.Outlined
    )
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "TextArea - Default")
@Composable
private fun DSTextAreaPreview() {
    DSDesignTheme {
        var text by remember { mutableStateOf("Hello World") }
        DSTextArea(
            value = text,
            onValueChange = { text = it },
            label = "评论",
            placeholder = "说点什么吧..."
        )
    }
}

@Preview(showBackground = true, name = "TextArea - With Counter")
@Composable
private fun DSTextAreaWithCounterPreview() {
    DSDesignTheme {
        var text by remember { mutableStateOf("一段已有的评论内容") }
        DSTextArea(
            value = text,
            onValueChange = { text = it },
            label = "评论",
            maxLength = 200
        )
    }
}

@Preview(showBackground = true, name = "TextArea - Error")
@Composable
private fun DSTextAreaErrorPreview() {
    DSDesignTheme {
        DSTextArea(
            value = "",
            onValueChange = {},
            label = "评论",
            isError = true,
            errorMessage = "评论不能为空"
        )
    }
}
