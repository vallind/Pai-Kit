// ============================================================================
// DSDialog.kt
// 对话框 - 基于 M3 AlertDialog 扩展
// 提供：标准 / 错误 / 成功 / 警告 4 种语义
// ============================================================================

package com.pai.app.core.designsystem.overlays

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSButtonSize
import com.pai.app.core.designsystem.primitives.DSButtonStyle
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.theme.extendedColors
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import com.pai.app.core.designsystem.foundation.tokens.DSMessageType

/**
 * Dialog 类型别名 - 统一使用 [DSMessageType]
 */
typealias DSDialogType = DSMessageType

/**
 * DSDialog - 标准对话框
 *
 * 使用示例：
 * ```kotlin
 * var showDialog by remember { mutableStateOf(true) }
 * if (showDialog) {
 *     DSDialog(
 *         title = "确认删除",
 *         message = "确定要删除这个项目吗？此操作不可撤销。",
 *         type = DSMessageType.Warning,
 *         confirmText = "删除",
 *         onConfirm = { showDialog = false },
 *         dismissText = "取消",
 *         onDismiss = { showDialog = false }
 *     )
 * }
 *
 * // 无图标对话框（showIcon = false）
 * DSDialog(
 *     title = "提示",
 *     message = "已保存",
 *     showIcon = false,
 *     onDismiss = {}
 * )
 * ```
 *
 * 设计规范：
 * - 圆角 extraLarge (28dp)
 * - 标题 TitleLarge，正文 BodyMedium
 * - 主操作按钮右侧，次操作按钮左侧
 * - type = Error 时确认按钮使用 Error 风格
 *
 * @param onDismiss 关闭回调
 * @param title 标题
 * @param message 内容
 * @param modifier 修饰符
 * @param type 消息类型，默认 Info
 * @param showIcon 是否显示类型图标，默认 true（设为 false 类似旧 Default 类型）
 * @param confirmText 确认按钮文字
 * @param onConfirm 确认回调
 * @param dismissText 取消按钮文字（不传则不显示）
 * @param onDismissClick 取消回调
 */
@Composable
internal fun DSDialog(
    onDismiss: () -> Unit,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    type: DSDialogType = DSDialogType.Info,
    showIcon: Boolean = true,
    confirmText: String = "确定",
    onConfirm: () -> Unit = {},
    dismissText: String? = "取消",
    onDismissClick: (() -> Unit)? = onDismiss
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        ),
        shape = RoundedCornerShape(DSTokens.Radius.extraLarge),
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // 类型图标
                val (icon, iconTint) = when (type) {
                    DSDialogType.Info -> Icons.Default.Info to MaterialTheme.extendedColors.info
                    DSDialogType.Success -> Icons.Default.CheckCircle to MaterialTheme.extendedColors.success
                    DSDialogType.Warning -> Icons.Default.Warning to MaterialTheme.extendedColors.warning
                    DSDialogType.Error -> Icons.Default.Error to MaterialTheme.colorScheme.error
                }

                if (showIcon && icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(Modifier.height(DSTokens.Spacing.md))
                }

                DSText(
                    text = title,
                    variant = DSTextVariant.TitleLarge
                )
                Spacer(Modifier.height(DSTokens.Spacing.sm))
                DSText(
                    text = message,
                    variant = DSTextVariant.BodyMedium,
                    color = DSTextColor.Secondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        },
        confirmButton = {
            DSButton(
                text = confirmText,
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                style = if (type == DSDialogType.Error) DSButtonStyle.Error else DSButtonStyle.Filled,
                size = DSButtonSize.Small
            )
        },
        dismissButton = if (dismissText != null && onDismissClick != null) {
            {
                DSButton(
                    text = dismissText,
                    onClick = {
                        onDismissClick()
                        onDismiss()
                    },
                    style = DSButtonStyle.Text,
                    size = DSButtonSize.Small
                )
            }
        } else null
    )
}

@Preview(showBackground = true, name = "Dialog - Warning")
@Composable
private fun DSDialogPreview() {
    DSDesignTheme {
        DSDialog(
            onDismiss = {},
            title = "确认删除",
            message = "此操作不可撤销，确定要删除吗？",
            type = DSDialogType.Warning,
            confirmText = "删除",
            onConfirm = {},
            dismissText = "取消"
        )
    }
}
