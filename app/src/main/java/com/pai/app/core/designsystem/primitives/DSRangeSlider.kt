// ============================================================================
// DSRangeSlider.kt
// 范围滑块 - 基于 M3 RangeSlider 扩展
// 用于选择一个数值范围（如价格区间、日期范围、年龄范围）
// M3 卓越线补齐：完整的选择类组件覆盖
// ============================================================================

package com.pai.app.core.designsystem.primitives

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSRangeSlider - 范围滑块
 *
 * 双滑块选择数值区间，支持步长、值显示、自定义范围。
 *
 * 使用示例：
 * ```kotlin
 * var range by remember { mutableStateOf(20f..80f) }
 * DSRangeSlider(
 *     value = range,
 *     onValueChange = { range = it },
 *     valueRange = 0f..100f,
 *     steps = 9,  // 步长 10（0/10/20/.../100）
 *     startLabel = "最低价",
 *     endLabel = "最高价",
 *     valueFormatter = { "${it.toInt()} 元" }
 * )
 * ```
 *
 * 设计规范：
 * - 滑块颜色：activeTrack 用 primary，inactiveTrack 用 surfaceVariant
 * - 拇指尺寸遵循 M3 规范（16dp 默认，按下 24dp）
 * - 步长：steps = N 表示除两端外有 N 个等分点，总步数 = N + 2
 * - 触控目标遵循 48dp 最小可点击区域
 *
 * @param value 当前选中的范围值（start..end）
 * @param onValueChange 范围变化回调（拖拽过程中持续触发）
 * @param modifier 修饰符
 * @param valueRange 允许选中的范围（如 0f..100f）
 * @param steps 步长数（0 表示连续，N 表示除两端外有 N 个等分点）
 * @param onValueChangeFinished 拖拽结束回调（用于提交最终值）
 * @param startLabel 起始端标签文字（可选）
 * @param endLabel 结束端标签文字（可选）
 * @param valueFormatter 值格式化函数（默认为 toString）
 * @param enabled 是否可用
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    startLabel: String? = null,
    endLabel: String? = null,
    valueFormatter: (Float) -> String = { it.toString() },
    enabled: Boolean = true
) {
    // Inline colors without RangeSliderDefaults (removed in M3 1.5+).
    // The RangeSlider accepts colors parameter directly.
    @Suppress("DEPRECATION")
    val colors = androidx.compose.material3.SliderDefaults.colors(
        thumbColor = MaterialTheme.colorScheme.primary,
        activeTrackColor = MaterialTheme.colorScheme.primary,
        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        activeTickColor = MaterialTheme.colorScheme.onPrimary,
        inactiveTickColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledActiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledInactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "范围滑块，当前值 ${valueFormatter(value.start)} 到 ${valueFormatter(value.endInclusive)}"
            }
    ) {
        // 顶部标签行（如果有的话）
        if (startLabel != null || endLabel != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = DSTokens.Spacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (startLabel != null) {
                    Column {
                        Text(
                            text = startLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = valueFormatter(value.start),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                if (endLabel != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = endLabel,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = valueFormatter(value.endInclusive),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        RangeSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            onValueChangeFinished = onValueChangeFinished,
            colors = colors,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================================================
// Previews
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "RangeSlider - Default")
@Composable
private fun DSRangeSliderDefaultPreview() {
    DSDesignTheme {
        var range = androidx.compose.runtime.remember { 0.3f..0.7f }
        DSRangeSlider(
            value = range,
            onValueChange = { range = it },
            valueRange = 0f..1f
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "RangeSlider - With Labels", widthDp = 360)
@Composable
private fun DSRangeSliderWithLabelsPreview() {
    DSDesignTheme {
        var range = androidx.compose.runtime.remember { 2000f..8000f }
        Column(modifier = Modifier.padding(16.dp)) {
            DSRangeSlider(
                value = range,
                onValueChange = { range = it },
                valueRange = 0f..10000f,
                steps = 9,
                startLabel = "最低价",
                endLabel = "最高价",
                valueFormatter = { "¥${it.toInt()}" }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "RangeSlider - Disabled")
@Composable
private fun DSRangeSliderDisabledPreview() {
    DSDesignTheme {
        DSRangeSlider(
            value = 0.2f..0.8f,
            onValueChange = {},
            valueRange = 0f..1f,
            enabled = false
        )
    }
}
