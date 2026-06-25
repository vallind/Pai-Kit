// ============================================================================
// DSTimePicker.kt
// 时间选择对话框 - 基于 M3 TimePicker + AlertDialog 包装
// 支持时钟视图与 24 小时制切换
// 作者: design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSTimePicker - 时间选择对话框
 *
 * 以模态对话框形式展示 Material3 TimePicker，用户可通过拖动时钟指针或点击数字选择时/分。
 * 支持自动 12/24 小时制切换。
 *
 * 使用示例：
 * ```kotlin
 * var showDialog by remember { mutableStateOf(false) }
 * var selectedHour by remember { mutableIntStateOf(9) }
 * var selectedMinute by remember { mutableIntStateOf(30) }
 *
 * if (showDialog) {
 *     DSTimePicker(
 *         initialHour = selectedHour,
 *         initialMinute = selectedMinute,
 *         onTimeSelected = { hour, minute ->
 *             selectedHour = hour
 *             selectedMinute = minute
 *             showDialog = false
 *         },
 *         onDismiss = { showDialog = false },
 *         title = "设置提醒时间",
 *         is24Hour = true
 *     )
 * }
 * ```
 *
 * 设计规范：
 * - 容器使用 M3 AlertDialog 包装（圆角 extraLarge 28dp）
 * - TimePicker 使用 Material3 默认时钟样式，颜色与品牌主色对齐
 * - 顶部标题区可选，未提供则使用默认文案「选择时间」
 * - 确认按钮 primary 色，取消按钮 onSurfaceVariant 色
 *
 * @param initialHour 初始小时（0-23）
 * @param initialMinute 初始分钟（0-59）
 * @param onTimeSelected 时间选中回调，参数为 (hour, minute)
 * @param onDismiss 对话框关闭回调
 * @param modifier 修饰符
 * @param title 对话框标题（可选，默认「选择时间」）
 * @param is24Hour 是否使用 24 小时制，默认 false（12 小时制）
 * @param confirmText 确认按钮文案
 * @param dismissText 取消按钮文案
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSTimePicker(
    initialHour: Int,
    initialMinute: Int,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = "选择时间",
    is24Hour: Boolean = false,
    confirmText: String = "确定",
    dismissText: String = "取消"
) {
    // TimePicker 状态
    val state = rememberTimePickerState(
        initialHour = initialHour.coerceIn(0, 23),
        initialMinute = initialMinute.coerceIn(0, 59),
        is24Hour = is24Hour
    )

    // TimePicker 颜色配置：与品牌主色对齐
    val timePickerColors = TimePickerDefaults.colors(
        clockDialColor = MaterialTheme.colorScheme.surfaceVariant,
        selectorColor = MaterialTheme.colorScheme.primary,
        containerColor = MaterialTheme.colorScheme.surface,
        periodSelectorBorderColor = MaterialTheme.colorScheme.outline,
        clockDialSelectedContentColor = MaterialTheme.colorScheme.onPrimary,
        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onSurface,
        timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        timeSelectorSelectedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

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
        title = {
            Text(
                text = title ?: "选择时间",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(
                    state = state,
                    colors = timePickerColors,
                    modifier = Modifier.widthIn(max = DSTokens.ComponentHeight.pickerMaxWidth)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onTimeSelected(state.hour, state.minute)
                    onDismiss()
                }
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "TimePicker - Default (12h)")
@Composable
private fun DSTimePickerPreview() {
    DSDesignTheme {
        DSTimePicker(
            initialHour = 9,
            initialMinute = 30,
            onTimeSelected = { _, _ -> },
            onDismiss = {},
            title = "选择时间"
        )
    }
}

@Preview(showBackground = true, name = "TimePicker - 24 Hour Mode")
@Composable
private fun DSTimePicker24HourPreview() {
    DSDesignTheme {
        DSTimePicker(
            initialHour = 18,
            initialMinute = 45,
            onTimeSelected = { _, _ -> },
            onDismiss = {},
            title = "提醒时间",
            is24Hour = true
        )
    }
}

@Preview(showBackground = true, name = "TimePicker - No Title")
@Composable
private fun DSTimePickerNoTitlePreview() {
    DSDesignTheme {
        DSTimePicker(
            initialHour = 0,
            initialMinute = 0,
            onTimeSelected = { _, _ -> },
            onDismiss = {},
            title = null,
            is24Hour = true,
            confirmText = "确认",
            dismissText = "返回"
        )
    }
}

@Preview(showBackground = true, name = "TimePicker - Custom Button Text")
@Composable
private fun DSTimePickerCustomButtonsPreview() {
    DSDesignTheme {
        DSTimePicker(
            initialHour = 14,
            initialMinute = 0,
            onTimeSelected = { _, _ -> },
            onDismiss = {},
            title = "闹钟时间",
            is24Hour = true,
            confirmText = "保存",
            dismissText = "不保存"
        )
    }
}
