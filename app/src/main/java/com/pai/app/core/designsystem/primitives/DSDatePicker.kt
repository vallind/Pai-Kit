// ============================================================================
// DSDatePicker.kt
// 日期选择对话框 - 基于 M3 DatePicker + DatePickerDialog + rememberDatePickerState
// 强制中文 locale 显示，标题与按钮文案本地化为中文
// 作者: design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.primitives
import android.content.res.Configuration
import java.util.Locale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSDatePicker - 日期选择对话框
 *
 * 以模态对话框形式展示 Material3 DatePicker，用户可在日历视图与输入视图之间切换。
 * 对话框关闭时通过 [onDateSelected] 回调返回所选时间戳（UTC 毫秒，可能为 null 表示清空）。
 *
 * 使用示例：
 * ```kotlin
 * var selected by remember { mutableStateOf<Long?>(System.currentTimeMillis()) }
 * var showDialog by remember { mutableStateOf(false) }
 *
 * if (showDialog) {
 *     DSDatePicker(
 *         selectedDateMillis = selected,
 *         onDateSelected = { millis ->
 *             selected = millis
 *             showDialog = false
 *         },
 *         onDismiss = { showDialog = false },
 *         title = "选择生日"
 *     )
 * }
 * ```
 *
 * 设计规范：
 * - 使用 Material3 DatePickerDialog 作为容器（圆角 extraLarge 28dp）
 * - 强制中文 locale 显示（年/月/日 顺序、星期文案、月份名称均本地化）
 * - 默认确认/取消按钮文案为中文「确定」/「取消」
 * - 标题参数 [title] 为可选；M3 DatePickerDialog 在 1.3.0 中未单独暴露 title slot，
 *   DatePicker 内部已含本地化标题（如「选择日期」），[title] 用于应用层语义标记，
 *   若需自定义顶部标题，建议外层自行包裹 Text。
 *
 * @param selectedDateMillis 当前已选日期（UTC 毫秒），null 表示未选择
 * @param onDateSelected 日期选中回调，参数为所选时间戳（可能为 null）
 * @param onDismiss 对话框关闭回调（点击外部、返回键、取消按钮均触发）
 * @param modifier 修饰符（应用于对话框根容器）
 * @param title 自定义标题（应用层语义；DatePicker 自身含本地化标题）
 * @param initialSelectedDateMillis DatePicker 初始化选中日期，默认回退到 selectedDateMillis
 * @param confirmText 确认按钮文案
 * @param dismissText 取消按钮文案
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSDatePicker(
    selectedDateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    initialSelectedDateMillis: Long? = selectedDateMillis,
    confirmText: String = "确定",
    dismissText: String = "取消"
) {
    // DatePicker 状态：以 initialSelectedDateMillis 初始化
    val state = rememberDatePickerState(
        initialSelectedDateMillis = initialSelectedDateMillis
    )

    // ----- 强制中文 locale -----
    // DatePicker 内部依赖 DateTimeFormatter 与系统 Configuration 渲染日期文案，
    // 通过覆盖 LocalContext 注入简体中文配置（LocalConfiguration 由 LocalContext 派生，
    // 无需重复 provide — Low #24 修复）。
    val context = LocalContext.current
    val localizedContext = remember(context) {
        val chineseConfig = Configuration(context.resources.configuration).apply {
            setLocale(Locale.SIMPLIFIED_CHINESE)
        }
        context.createConfigurationContext(chineseConfig)
    }

    CompositionLocalProvider(LocalContext provides localizedContext) {
        DatePickerDialog(
            modifier = modifier,
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        onDateSelected(state.selectedDateMillis)
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
            },
            shape = RoundedCornerShape(DSTokens.Radius.extraLarge),
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                headlineContentColor = MaterialTheme.colorScheme.onSurface,
                weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                yearContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                currentYearContentColor = MaterialTheme.colorScheme.primary,
                selectedYearContentColor = MaterialTheme.colorScheme.onPrimary,
                selectedYearContainerColor = MaterialTheme.colorScheme.primary,
                dayContentColor = MaterialTheme.colorScheme.onSurface,
                disabledDayContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
                disabledSelectedDayContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
                selectedDayContainerColor = MaterialTheme.colorScheme.primary,
                disabledSelectedDayContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                todayContentColor = MaterialTheme.colorScheme.primary,
                todayDateBorderColor = MaterialTheme.colorScheme.primary,
                dayInSelectionRangeContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            DatePicker(state = state)
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "DatePicker - Default")
@Composable
private fun DSDatePickerPreview() {
    DSDesignTheme {
        DSDatePicker(
            selectedDateMillis = System.currentTimeMillis(),
            onDateSelected = {},
            onDismiss = {},
            title = "选择日期"
        )
    }
}

@Preview(showBackground = true, name = "DatePicker - No Initial Date")
@Composable
private fun DSDatePickerNoInitialPreview() {
    DSDesignTheme {
        DSDatePicker(
            selectedDateMillis = null,
            onDateSelected = {},
            onDismiss = {}
        )
    }
}

@Preview(showBackground = true, name = "DatePicker - With Title")
@Composable
private fun DSDatePickerWithTitlePreview() {
    DSDesignTheme {
        DSDatePicker(
            selectedDateMillis = System.currentTimeMillis(),
            onDateSelected = {},
            onDismiss = {},
            title = "选择生日",
            confirmText = "确认",
            dismissText = "返回"
        )
    }
}
