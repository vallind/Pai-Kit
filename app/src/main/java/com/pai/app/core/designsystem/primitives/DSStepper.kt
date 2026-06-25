// ============================================================================
// DSStepper.kt
// 数字步进器 - 减号按钮 + 当前数值 + 加号按钮
// 用于数量、人数等带上下边界的整数值选择
// 作者: design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
 * DSStepper - 数字步进器
 *
 * 通过 +/- 按钮对整数值进行步进调整，常用于购物车数量、表单人数等场景。
 *
 * 使用示例：
 * ```kotlin
 * var count by remember { mutableIntStateOf(1) }
 * DSStepper(
 *     value = count,
 *     onValueChange = { count = it },
 *     range = 1..99,
 *     step = 1
 * )
 * ```
 *
 * 设计规范：
 * - 整体高度 40dp（与 DSTextField Medium 一致）
 * - 圆角 medium (12dp) 容器，outline 边框
 * - 左右两侧圆形 Icon 按钮（48dp 触控目标）
 * - 中间数字区水平居中，TitleMedium + SemiBold
 * - 边界值时对应按钮自动禁用并显示 38% alpha 不可用样式
 * - 支持 step 参数自定义步长（如以 5 为单位递增）
 *
 * @param modifier 修饰符
 * @param value 当前数值
 * @param onValueChange 数值变化回调（已做边界裁剪，调用方可直接使用）
 * @param range 允许的取值区间（闭区间，如 1..99）
 * @param step 步长，默认 1
 * @param enabled 是否启用整组控件
 */
@Composable
internal fun DSStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    range: IntRange = 0..Int.MAX_VALUE,
    step: Int = 1,
    enabled: Boolean = true
) {
    // 边界判断
    val canDecrement = enabled && value - step >= range.first
    val canIncrement = enabled && value + step <= range.last

    // 颜色：禁用按钮使用 38% alpha，激活按钮使用 primary
    val activeContentColor = MaterialTheme.colorScheme.primary
    val disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)

    Surface(
        modifier = modifier
            .height(DSTokens.ComponentHeight.buttonMedium)
            .fillMaxWidth(),
        shape = RoundedCornerShape(DSTokens.Radius.medium),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            width = DSTokens.Border.thin,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.padding(horizontal = DSTokens.Spacing.xxs)
        ) {
            // ----- 减号按钮 -----
            IconButton(
                onClick = {
                    val next = (value - step).coerceIn(range.first, range.last)
                    if (next != value) onValueChange(next)
                },
                enabled = canDecrement,
                modifier = Modifier
                    .size(DSTokens.minTouchTarget)
                    .semantics { contentDescription = "减少 $step" }
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = null,
                    tint = if (canDecrement) activeContentColor else disabledContentColor,
                    modifier = Modifier.size(DSTokens.IconSize.sm)
                )
            }

            // ----- 数值显示 -----
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = DSTokens.Spacing.sm),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = value.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1
                )
            }

            // ----- 加号按钮 -----
            IconButton(
                onClick = {
                    val next = (value + step).coerceIn(range.first, range.last)
                    if (next != value) onValueChange(next)
                },
                enabled = canIncrement,
                modifier = Modifier
                    .size(DSTokens.minTouchTarget)
                    .semantics { contentDescription = "增加 $step" }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = if (canIncrement) activeContentColor else disabledContentColor,
                    modifier = Modifier.size(DSTokens.IconSize.sm)
                )
            }
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "Stepper - Default")
@Composable
private fun DSStepperPreview() {
    DSDesignTheme {
        var value by remember { mutableIntStateOf(1) }
        DSStepper(
            value = value,
            onValueChange = { value = it },
            range = 1..10,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Stepper - At Min Boundary")
@Composable
private fun DSStepperMinBoundaryPreview() {
    DSDesignTheme {
        DSStepper(
            value = 1,
            onValueChange = {},
            range = 1..10,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Stepper - At Max Boundary")
@Composable
private fun DSStepperMaxBoundaryPreview() {
    DSDesignTheme {
        DSStepper(
            value = 99,
            onValueChange = {},
            range = 1..99,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Stepper - Disabled")
@Composable
private fun DSStepperDisabledPreview() {
    DSDesignTheme {
        DSStepper(
            value = 5,
            onValueChange = {},
            range = 0..10,
            enabled = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Stepper - Step 5")
@Composable
private fun DSStepperStep5Preview() {
    DSDesignTheme {
        var value by remember { mutableIntStateOf(20) }
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
        ) {
            DSStepper(
                value = value,
                onValueChange = { value = it },
                range = 0..100,
                step = 5
            )
            Text(
                text = "当前值：$value（步长 5）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
