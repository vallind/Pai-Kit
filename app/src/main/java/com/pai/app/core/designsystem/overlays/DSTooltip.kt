// ============================================================================
// DSTooltip.kt
// 提示框 - 基于 M3 PlainTooltipBox + rememberTooltipState
// 长按触发，自动消失；适用于图标 / 按钮的辅助说明
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.overlays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import kotlinx.coroutines.delay

/**
 * DSTooltip - 长按提示框
 *
 * 使用示例：
 * ```kotlin
 * DSTooltip(text = "点击收藏", duration = 2000) {
 *     Icon(
 *         imageVector = Icons.Default.Favorite,
 *         contentDescription = "收藏",
 *         modifier = Modifier.size(24.dp)
 *     )
 * }
 * ```
 *
 * 设计规范：
 * - 基于 M3 PlainTooltipBox，长按锚点元素触发，自动消失
 * - 提示容器使用 primaryContainer 背景 + onPrimaryContainer 文字（M3 默认）
 * - duration 控制提示显示时长（毫秒），默认 2000ms；<= 0 时不自动消失
 * - M3 1.3.0 已移除 TooltipState.durationMillis 属性，
 *   故通过 LaunchedEffect 监听 isShown 自行控制自动消失
 * - 提示文案使用 labelLarge + Medium 字重
 *
 * @param text 提示文案
 * @param modifier 修饰符
 * @param duration 提示显示时长（毫秒），默认 2000；<= 0 表示不自动消失
 * @param content 锚点内容（图标 / 按钮等），长按该内容触发提示
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSTooltip(
    text: String,
    modifier: Modifier = Modifier,
    duration: Int = 2000,
    content: @Composable () -> Unit
) {
    val state = rememberTooltipState()

    // 监听提示显示状态：显示后等待 duration 毫秒自动消失
    LaunchedEffect(state.isVisible) {
        if (state.isVisible && duration > 0) {
            delay(duration.toLong())
            state.dismiss()
        }
    }

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
            )
        },
        modifier = modifier,
        state = state,
    ) {
        content()
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "DSTooltip - 长按触发")
@Composable
private fun DSTooltipPreview() {
    DSDesignTheme {
        Row(
            modifier = Modifier.padding(DSTokens.Spacing.xxl),
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xl),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSTooltip(text = "点击收藏") {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "收藏",
                    modifier = Modifier.size(DSTokens.IconSize.md)
                )
            }
            DSTooltip(text = "发送消息", duration = 3000) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "发送",
                    modifier = Modifier.size(DSTokens.IconSize.md)
                )
            }
        }
    }
}
