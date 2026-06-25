// ============================================================================
// DSFullScreenStates.kt
// 全屏状态占位组件：Loading / Error / Empty
// 在 ApiResult Loading / Error / Empty 时由页面直接渲染
// ============================================================================

package com.pai.app.core.designsystem.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import com.pai.app.core.designsystem.primitives.DSCircularProgress
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant

/**
 * 全屏加载占位
 *
 * 居中显示一个不确定进度的 [DSCircularProgress]，可选文字与遮罩。
 *
 * 使用示例：
 * ```kotlin
 * // 纯居中加载（无遮罩）
 * when (val result = uiState.result) {
 *     is ApiResult.Loading -> DSFullScreenLoading()
 *     is ApiResult.Success -> Content(result.data)
 *     is ApiResult.Error -> DSFullScreenError(result.message) { retry() }
 * }
 *
 * // 带文字与遮罩（弹窗式加载）
 * if (isSubmitting) {
 *     DSFullScreenLoading(
 *         message = "提交中...",
 *         withScrim = true
 *     )
 * }
 * ```
 *
 * @param message 可选加载文字（如 "提交中..."），为 null 时不显示
 * @param withScrim 是否显示半透明遮罩背景，默认 false
 *     - false：纯居中加载，背景透明，不阻断下层交互
 *     - true：半透明遮罩，居中 Card 含进度+文字，阻断下层交互（用于表单提交等）
 * @param modifier 修饰符
 */
@Composable
fun DSFullScreenLoading(
    message: String? = null,
    withScrim: Boolean = false,
    modifier: Modifier = Modifier
) {
    val baseModifier = if (withScrim) {
        modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f))
    } else {
        modifier.fillMaxSize()
    }

    Box(
        modifier = baseModifier,
        contentAlignment = Alignment.Center
    ) {
        if (withScrim) {
            // 遮罩模式：用 surface 容器包裹，提高可见性
            Column(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(DSTokens.Radius.large)
                    )
                    .padding(DSTokens.Spacing.xl),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
            ) {
                DSCircularProgress(
                    size = 40.dp,
                    strokeWidth = DSTokens.Border.medium,
                    color = MaterialTheme.colorScheme.primary
                )
                if (message != null) {
                    DSText(
                        text = message,
                        variant = DSTextVariant.BodyMedium,
                        color = DSTextColor.OnSurface
                    )
                }
            }
        } else {
            // 纯居中模式
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
            ) {
                DSCircularProgress()
                if (message != null) {
                    DSText(
                        text = message,
                        variant = DSTextVariant.BodyMedium,
                        color = DSTextColor.Secondary
                    )
                }
            }
        }
    }
}

/**
 * 全屏错误占位
 *
 * 居中显示 [DSEmptyState]（带 CloudOff 图标与错误文案），并提供"重试"按钮。
 *
 * @param message 错误描述文案
 * @param onRetry 重试回调
 */
@Composable
fun DSFullScreenError(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        DSEmptyState(
            icon = Icons.Default.CloudOff,
            title = "加载失败",
            description = message,
            actionText = "重试",
            onActionClick = onRetry
        )
    }
}

/**
 * 全屏空数据占位
 *
 * 居中显示 [DSEmptyState]（默认 Inbox 图标），无操作按钮。
 * 适用于列表为空、搜索无结果等场景。
 *
 * @param title 标题文案，默认"暂无数据"
 * @param description 可选描述文案
 * @param icon 可选图标，默认 Inbox
 * @param actionText 可选操作按钮文案
 * @param onActionClick 操作按钮点击回调
 */
@Composable
fun DSFullScreenEmpty(
    title: String = "暂无数据",
    description: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Inbox,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        DSEmptyState(
            icon = icon,
            title = title,
            description = description,
            actionText = actionText,
            onActionClick = onActionClick
        )
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "FullScreen Loading - Plain")
@Composable
private fun DSFullScreenLoadingPlainPreview() {
    DSDesignTheme {
        DSFullScreenLoading()
    }
}

@Preview(showBackground = true, name = "FullScreen Loading - With Scrim + Message")
@Composable
private fun DSFullScreenLoadingScrimPreview() {
    DSDesignTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            DSText("背景内容", variant = DSTextVariant.BodyLarge)
            DSFullScreenLoading(message = "加载中...", withScrim = true)
        }
    }
}

@Preview(showBackground = true, name = "FullScreen Error")
@Composable
private fun DSFullScreenErrorPreview() {
    DSDesignTheme {
        DSFullScreenError(message = "网络连接失败，请检查网络", onRetry = {})
    }
}

@Preview(showBackground = true, name = "FullScreen Empty")
@Composable
private fun DSFullScreenEmptyPreview() {
    DSDesignTheme {
        DSFullScreenEmpty(description = "下拉刷新试试")
    }
}
