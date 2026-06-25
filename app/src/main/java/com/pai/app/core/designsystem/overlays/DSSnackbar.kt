// ============================================================================
// DSSnackbar.kt
// Snackbar 反馈组件 - 基于 M3 Snackbar 扩展
// 支持 DSMessageType 4 种语义类型（Info/Success/Warning/Error）
// ============================================================================

package com.pai.app.core.designsystem.overlays

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pai.app.core.designsystem.foundation.tokens.DSMessageType
import com.pai.app.core.designsystem.foundation.tokens.contentColor
import com.pai.app.core.designsystem.foundation.tokens.containerColor
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSSnackbar - 自定义 Snackbar
 *
 * 使用示例：
 * ```kotlin
 * val snackbarHostState = remember { SnackbarHostState() }
 * // 在 DSAppScaffold 中：
 * SnackbarHost(hostState = snackbarHostState) { data ->
 *     DSSnackbar(data, type = DSMessageType.Success)
 * }
 *
 * // 触发：
 * scope.launch {
 *     snackbarHostState.showSnackbar(
 *         message = "保存成功",
 *         actionLabel = "查看",
 *         duration = SnackbarDuration.Short
 *     )
 * }
 * ```
 *
 * @param snackbarData Snackbar 数据
 * @param modifier 修饰符
 * @param type 消息类型，决定容器色/内容色/操作色
 */
@Composable
internal fun DSSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    type: DSMessageType = DSMessageType.Info
) {
    val containerColor = type.containerColor
    val contentColor = type.contentColor
    val actionColor = when (type) {
        DSMessageType.Info -> androidx.compose.material3.MaterialTheme.colorScheme.inversePrimary
        else -> contentColor
    }

    Snackbar(
        modifier = modifier.padding(DSTokens.Spacing.lg),
        shape = RoundedCornerShape(DSTokens.Radius.medium),
        containerColor = containerColor,
        contentColor = contentColor,
        actionColor = actionColor,
        actionContentColor = actionColor,
        dismissActionContentColor = contentColor,
        snackbarData = snackbarData
    )
}
