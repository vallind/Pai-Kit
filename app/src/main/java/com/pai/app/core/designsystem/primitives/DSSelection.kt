// ============================================================================
// DSCheckbox.kt / DSSwitch.kt / DSRadioButton.kt / DSSlider.kt
// 选择类组件
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.a11y.minTouchTarget
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSCheckbox - 复选框
 *
 * 使用示例：
 * ```kotlin
 * var checked by remember { mutableStateOf(false) }
 * DSCheckbox(
 *     checked = checked,
 *     onCheckedChange = { checked = it },
 *     label = "我已阅读并同意协议"
 * )
 * ```
 *
 * @param checked 是否勾选
 * @param onCheckedChange 勾选变化回调
 * @param label 标签文字（可选）
 * @param enabled 是否可用
 * @param modifier 修饰符
 */
@Composable
internal fun DSCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
) {
    val colors = CheckboxDefaults.colors(
        checkedColor = MaterialTheme.colorScheme.primary,
        uncheckedColor = MaterialTheme.colorScheme.outline,
        checkmarkColor = MaterialTheme.colorScheme.onPrimary,
        disabledCheckedColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledUncheckedColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledIndeterminateColor = MaterialTheme.colorScheme.surfaceVariant
    )

    if (label != null) {
        Row(
            modifier = modifier
                .minTouchTarget()
                .selectable(
                    selected = checked,
                    onClick = { onCheckedChange?.invoke(!checked) },
                    role = Role.Checkbox,
                    enabled = enabled
                )
                .padding(horizontal = DSTokens.Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = colors
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    } else {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = colors,
            modifier = modifier.minTouchTarget()
        )
    }
}

/**
 * DSSwitch - 开关
 *
 * 使用示例：
 * ```kotlin
 * var checked by remember { mutableStateOf(false) }
 * DSSwitch(
 *     checked = checked,
 *     onCheckedChange = { checked = it },
 *     label = "接收推送通知"
 * )
 * ```
 *
 * @param checked 是否开启
 * @param onCheckedChange 切换回调
 * @param label 标签文字（可选）
 * @param enabled 是否可用
 * @param modifier 修饰符
 */
@Composable
internal fun DSSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
) {
    val colors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        checkedBorderColor = MaterialTheme.colorScheme.primary,
        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        uncheckedBorderColor = MaterialTheme.colorScheme.outline,
        disabledCheckedThumbColor = MaterialTheme.colorScheme.surface,
        disabledCheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledUncheckedThumbColor = MaterialTheme.colorScheme.surface,
        disabledUncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
    )

    if (label != null) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .minTouchTarget()
                .selectable(
                    selected = checked,
                    onClick = { onCheckedChange?.invoke(!checked) },
                    role = Role.Switch,
                    enabled = enabled
                )
                .padding(horizontal = DSTokens.Spacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                enabled = enabled,
                colors = colors
            )
        }
    } else {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = colors,
            modifier = modifier.minTouchTarget()
        )
    }
}

/**
 * DSRadioButton - 单选按钮
 *
 * 使用示例：
 * ```kotlin
 * val options = listOf("男", "女", "其他")
 * var selected by remember { mutableStateOf("男") }
 * options.forEach { option ->
 *     DSRadioButton(
 *         selected = (selected == option),
 *         onClick = { selected = option },
 *         label = option
 *     )
 * }
 * ```
 */
@Composable
internal fun DSRadioButton(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
) {
    val colors = RadioButtonDefaults.colors(
        selectedColor = MaterialTheme.colorScheme.primary,
        unselectedColor = MaterialTheme.colorScheme.outline,
        disabledSelectedColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledUnselectedColor = MaterialTheme.colorScheme.surfaceVariant
    )

    if (label != null) {
        Row(
            modifier = modifier
                .minTouchTarget()
                .selectable(
                    selected = selected,
                    onClick = onClick,
                    role = Role.RadioButton,
                    enabled = enabled
                )
                .padding(horizontal = DSTokens.Spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                enabled = enabled,
                colors = colors
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (enabled) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    } else {
        RadioButton(
            selected = selected,
            onClick = onClick,
            enabled = enabled,
            colors = colors,
            modifier = modifier.minTouchTarget()
        )
    }
}

/**
 * DSSlider - 滑块
 *
 * 使用示例：
 * ```kotlin
 * var value by remember { mutableFloatStateOf(0.5f) }
 * DSSlider(
 *     value = value,
 *     onValueChange = { value = it },
 *     valueRange = 0f..1f
 * )
 * ```
 */
@Composable
internal fun DSSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null
) {
    val colors = SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        activeTickColor = MaterialTheme.colorScheme.onPrimary,
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        inactiveTickColor = MaterialTheme.colorScheme.outline,
        disabledThumbColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledActiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledInactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
    )

    Slider(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        enabled = enabled,
        valueRange = valueRange,
        steps = steps,
        onValueChangeFinished = onValueChangeFinished,
        colors = colors
    )
}

@Preview(showBackground = true, name = "Selection Components")
@Composable
private fun SelectionComponentsPreview() {
    DSDesignTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            var checkboxChecked by remember { mutableStateOf(true) }
            DSCheckbox(
                checked = checkboxChecked,
                onCheckedChange = { checkboxChecked = it },
                label = "同意用户协议"
            )

            var switchChecked by remember { mutableStateOf(true) }
            DSSwitch(
                checked = switchChecked,
                onCheckedChange = { switchChecked = it },
                label = "推送通知"
            )

            var radioSelected by remember { mutableStateOf("A") }
            listOf("A", "B", "C").forEach { option ->
                DSRadioButton(
                    selected = radioSelected == option,
                    onClick = { radioSelected = option },
                    label = "选项 $option"
                )
            }

            var sliderValue by remember { mutableFloatStateOf(0.5f) }
            DSSlider(
                value = sliderValue,
                onValueChange = { sliderValue = it }
            )
        }
    }
}
