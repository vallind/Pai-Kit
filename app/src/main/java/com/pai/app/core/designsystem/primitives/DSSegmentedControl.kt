// ============================================================================
// DSSegmentedControl.kt
// 分段选择器 - 基于 M3 SingleChoiceSegmentedButtonRow 扩展
// 胶囊形容器（圆角 full），选中态使用 primary 背景 + onPrimary 文字
// 作者: design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 计算胶囊形分段项的 Shape
 * - 单项时四角全圆（胶囊）
 * - 首项左两角圆（胶囊左半）
 * - 末项右两角圆（胶囊右半）
 * - 中间项无圆角
 *
 * 使用 [DSTokens.Radius.full]（999dp 超大半径，会被自动裁剪为实际半径）
 * 确保任意高度下都呈现完整胶囊外观。
 */
private fun capsuleItemShape(index: Int, count: Int): Shape = when {
    count == 1 -> RoundedCornerShape(DSTokens.Radius.full)
    index == 0 -> RoundedCornerShape(
        topStart = DSTokens.Radius.full,
        bottomStart = DSTokens.Radius.full,
        topEnd = DSTokens.Radius.none,
        bottomEnd = DSTokens.Radius.none
    )
    index == count - 1 -> RoundedCornerShape(
        topStart = DSTokens.Radius.none,
        bottomStart = DSTokens.Radius.none,
        topEnd = DSTokens.Radius.full,
        bottomEnd = DSTokens.Radius.full
    )
    else -> RoundedCornerShape(DSTokens.Radius.none)
}

/**
 * DSSegmentedControl - 分段选择器
 *
 * 用于在 2 ~ 5 个互斥选项之间进行快速切换，相比 RadioButton 更紧凑、更视觉化。
 * 适用于：视图切换（列表/网格）、时间范围（日/周/月）、筛选维度等。
 *
 * 使用示例：
 * ```kotlin
 * val options = listOf("日", "周", "月")
 * var selected by remember { mutableIntStateOf(0) }
 * DSSegmentedControl(
 *     options = options,
 *     selectedIndex = selected,
 *     onSelectedChange = { selected = it }
 * )
 * ```
 *
 * 设计规范：
 * - 容器整体呈胶囊形（首/末段两端为 50% 圆角）
 * - 选中态：primary 背景 + onPrimary 文字（labelLarge + Medium）
 * - 未选中态：透明背景 + onSurfaceVariant 文字
 * - 禁用态：自动应用 12% / 38% alpha 不可用样式
 * - 推荐选项数：2 ~ 5 个；超出请使用 Dropdown
 *
 * @param modifier 修饰符
 * @param options 选项文案列表，至少 1 项
 * @param selectedIndex 当前选中项索引（0-based）
 * @param onSelectedChange 选中变化回调
 * @param enabled 是否启用整组控件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    // 边界保护：selectedIndex 落在合法区间内
    val safeSelectedIndex = selectedIndex.coerceIn(0, options.lastIndex.coerceAtLeast(0))

    // 颜色配置：选中态使用品牌主色，未选中态使用透明 + onSurfaceVariant
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.primary,
        activeContentColor = MaterialTheme.colorScheme.onPrimary,
        inactiveContainerColor = Color.Transparent,
        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledActiveContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        disabledActiveContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
        disabledInactiveContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
        disabledInactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    )

    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                selected = index == safeSelectedIndex,
                onClick = { onSelectedChange(index) },
                enabled = enabled,
                shape = capsuleItemShape(index, options.size),
                colors = colors,
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (index == safeSelectedIndex) FontWeight.SemiBold
                    else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "SegmentedControl - Default")
@Composable
private fun DSSegmentedControlPreview() {
    DSDesignTheme {
        var selected by remember { mutableIntStateOf(0) }
        DSSegmentedControl(
            options = listOf("日", "周", "月"),
            selectedIndex = selected,
            onSelectedChange = { selected = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "SegmentedControl - Two Options")
@Composable
private fun DSSegmentedControlTwoOptionsPreview() {
    DSDesignTheme {
        var selected by remember { mutableIntStateOf(1) }
        DSSegmentedControl(
            options = listOf("列表", "网格"),
            selectedIndex = selected,
            onSelectedChange = { selected = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "SegmentedControl - Disabled")
@Composable
private fun DSSegmentedControlDisabledPreview() {
    DSDesignTheme {
        DSSegmentedControl(
            options = listOf("A", "B", "C", "D"),
            selectedIndex = 2,
            onSelectedChange = {},
            enabled = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "SegmentedControl - Single Option")
@Composable
private fun DSSegmentedControlSinglePreview() {
    DSDesignTheme {
        DSSegmentedControl(
            options = listOf("Only"),
            selectedIndex = 0,
            onSelectedChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "SegmentedControl - Variants Column")
@Composable
private fun DSSegmentedControlVariantsPreview() {
    DSDesignTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
        ) {
            var a by remember { mutableIntStateOf(0) }
            DSSegmentedControl(
                options = listOf("全部", "未读", "已读"),
                selectedIndex = a,
                onSelectedChange = { a = it }
            )
            var b by remember { mutableIntStateOf(1) }
            DSSegmentedControl(
                options = listOf("收入", "支出"),
                selectedIndex = b,
                onSelectedChange = { b = it }
            )
        }
    }
}

// ============================================================================
// DSMultiSegmentedControl - 多选分段按钮
// M3 卓越线补齐：M3 MultiChoiceSegmentedButtonRow 支持
// 用于多个互不排斥的开关型选项，如「筛选条件」组合选择
// ============================================================================

/**
 * DSMultiSegmentedControl - 多选分段控制器
 *
 * 用于多个独立的开关型选项，每个分段可独立选中/取消选中。
 * 适用于：筛选条件（标签、价格、品牌等可叠加筛选）、视图模式叠加、列设置。
 *
 * 使用示例：
 * ```kotlin
 * val options = listOf("新品", "包邮", "折扣", "自营")
 * val selected = remember { mutableStateOf(setOf(0, 2)) }
 * DSMultiSegmentedControl(
 *     options = options,
 *     selectedIndices = selected.value,
 *     onSelectionChange = { selected.value = it }
 * )
 * ```
 *
 * 设计规范：
 * - 容器整体呈胶囊形（首/末段两端为 50% 圆角）
 * - 选中态：primary 背景 + onPrimary 文字 + checkmark 图标
 * - 未选中态：透明背景 + onSurfaceVariant 文字
 * - 推荐选项数：2 ~ 5 个
 *
 * @param options 选项文案列表，至少 2 项
 * @param selectedIndices 当前选中的索引集合
 * @param onSelectionChange 选中集合变化回调
 * @param modifier 修饰符
 * @param enabled 是否启用整组控件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSMultiSegmentedControl(
    options: List<String>,
    selectedIndices: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = SegmentedButtonDefaults.colors(
        activeContainerColor = MaterialTheme.colorScheme.primary,
        activeContentColor = MaterialTheme.colorScheme.onPrimary,
        inactiveContainerColor = Color.Transparent,
        inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledActiveContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
        disabledActiveContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.38f),
        disabledInactiveContainerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f),
        disabledInactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
    )

    MultiChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        options.forEachIndexed { index, option ->
            SegmentedButton(
                checked = index in selectedIndices,
                onCheckedChange = {
                    onSelectionChange(
                        if (index in selectedIndices) selectedIndices - index
                        else selectedIndices + index
                    )
                },
                enabled = enabled,
                shape = capsuleItemShape(index, options.size),
                colors = colors,
                icon = {
                    SegmentedButtonDefaults.Icon(active = index in selectedIndices)
                }
            ) {
                Text(
                    text = option,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (index in selectedIndices) FontWeight.SemiBold
                    else FontWeight.Medium,
                    maxLines = 1
                )
            }
        }
    }
}

// ============================================================================
// Multi-Choice Previews
// ============================================================================

@Preview(showBackground = true, name = "SegmentedControl - Multi Choice")
@Composable
private fun DSMultiSegmentedControlPreview() {
    DSDesignTheme {
        var selected by remember { mutableStateOf(setOf(0, 2)) }
        DSMultiSegmentedControl(
            options = listOf("新品", "包邮", "折扣", "自营"),
            selectedIndices = selected,
            onSelectionChange = { selected = it },
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "SegmentedControl - Multi Choice All Selected")
@Composable
private fun DSMultiSegmentedControlAllPreview() {
    DSDesignTheme {
        DSMultiSegmentedControl(
            options = listOf("A", "B", "C"),
            selectedIndices = setOf(0, 1, 2),
            onSelectionChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "SegmentedControl - Multi Choice Disabled")
@Composable
private fun DSMultiSegmentedControlDisabledPreview() {
    DSDesignTheme {
        DSMultiSegmentedControl(
            options = listOf("选项1", "选项2", "选项3"),
            selectedIndices = setOf(1),
            onSelectionChange = {},
            enabled = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}
