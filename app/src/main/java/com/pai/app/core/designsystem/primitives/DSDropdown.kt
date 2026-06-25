// ============================================================================
// DSDropdown.kt
// 下拉选择器 - 基于 M3 ExposedDropdownMenuBox 扩展
// 适合 4 个以上互斥选项场景，相比 SegmentedControl 更节省纵向空间
// 作者: design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSDropdown - 下拉选择器
 *
 * 适用于选项较多（≥4）的互斥单选场景，点击文本框展开菜单列表。
 *
 * 使用示例：
 * ```kotlin
 * val cities = listOf("北京", "上海", "广州", "深圳", "杭州", "成都")
 * var selected by remember { mutableIntStateOf(0) }
 * DSDropdown(
 *     label = "所在城市",
 *     options = cities,
 *     selectedIndex = selected,
 *     onSelectedChange = { selected = it },
 *     leadingIcon = Icons.Default.LocationOn
 * )
 * ```
 *
 * 设计规范：
 * - 文本框为 Outlined 风格，圆角 medium (12dp)
 * - 右侧 trailingIcon 使用 ArrowDropDown（不可编辑模式）
 * - 选中项在菜单中显示 check 图标（M3 默认行为）
 * - 禁用态：边框 outlineVariant，文字 50% alpha
 * - 触控目标符合 a11y 标准
 *
 * @param modifier 修饰符
 * @param label 标签（始终显示，浮动）
 * @param options 选项文案列表
 * @param selectedIndex 当前选中项索引（0-based，-1 表示未选中）
 * @param onSelectedChange 选中变化回调
 * @param enabled 是否启用
 * @param leadingIcon 前置图标（可选）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSDropdown(
    label: String,
    options: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null
) {
    // 展开/收起状态
    var expanded by remember { mutableStateOf(false) }

    // 安全索引处理（-1 表示未选中，显示占位符）
    val safeSelectedIndex = selectedIndex.coerceIn(-1, options.lastIndex.coerceAtLeast(-1))
    val selectedText = options.getOrNull(safeSelectedIndex)

    // 颜色配置
    val colors: TextFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = Color.Transparent,
        unfocusedContainerColor = Color.Transparent,
        disabledContainerColor = Color.Transparent,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
        disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
        unfocusedIndicatorColor = MaterialTheme.colorScheme.outline,
        disabledIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
        focusedLabelColor = MaterialTheme.colorScheme.primary,
        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        focusedTrailingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledTrailingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
        unfocusedLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        disabledLeadingIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        cursorColor = MaterialTheme.colorScheme.primary
    )

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = enabled && it },
        modifier = modifier.fillMaxWidth()
    ) {
        // 当 enabled 从 true 变为 false 时强制收起菜单，避免禁用后菜单仍展开
        LaunchedEffect(enabled) {
            if (!enabled) expanded = false
        }
        OutlinedTextField(
            value = selectedText ?: "",
            onValueChange = { /* 只读模式，不处理输入 */ },
            readOnly = true,
            enabled = enabled,
            label = {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            placeholder = if (selectedText == null) {
                { Text("请选择", style = MaterialTheme.typography.bodyMedium) }
            } else null,
            leadingIcon = leadingIcon?.let {
                {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        modifier = Modifier.size(DSTokens.IconSize.sm)
                    )
                }
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = colors,
            shape = RoundedCornerShape(DSTokens.Radius.medium),
            textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            // 使用新 API：MenuAnchorType.PrimaryNotEditable 表示不可编辑的下拉锚点
            modifier = Modifier
                .menuAnchor(
                    type = MenuAnchorType.PrimaryNotEditable,
                    enabled = enabled
                )
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEachIndexed { index, option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (index == safeSelectedIndex) FontWeight.SemiBold
                            else FontWeight.Normal,
                            color = if (index == safeSelectedIndex) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        onSelectedChange(index)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "Dropdown - Default")
@Composable
private fun DSDropdownPreview() {
    DSDesignTheme {
        var selected by remember { mutableStateOf(1) }
        DSDropdown(
            label = "所在城市",
            options = listOf("北京", "上海", "广州", "深圳", "杭州", "成都"),
            selectedIndex = selected,
            onSelectedChange = { selected = it },
            leadingIcon = Icons.Default.LocationOn,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Dropdown - No Selection")
@Composable
private fun DSDropdownNoSelectionPreview() {
    DSDesignTheme {
        DSDropdown(
            label = "职业",
            options = listOf("学生", "工程师", "设计师", "产品经理", "其他"),
            selectedIndex = -1,
            onSelectedChange = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Dropdown - Disabled")
@Composable
private fun DSDropdownDisabledPreview() {
    DSDesignTheme {
        DSDropdown(
            label = "国家",
            options = listOf("中国", "美国", "日本", "韩国"),
            selectedIndex = 0,
            onSelectedChange = {},
            enabled = false,
            leadingIcon = Icons.Default.LocationOn,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "Dropdown - Multiple")
@Composable
private fun DSDropdownMultiplePreview() {
    DSDesignTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
        ) {
            var city by remember { mutableStateOf(0) }
            var gender by remember { mutableStateOf(-1) }
            var education by remember { mutableStateOf(2) }

            DSDropdown(
                label = "城市",
                options = listOf("北京", "上海", "广州"),
                selectedIndex = city,
                onSelectedChange = { city = it }
            )
            DSDropdown(
                label = "性别",
                options = listOf("男", "女"),
                selectedIndex = gender,
                onSelectedChange = { gender = it }
            )
            DSDropdown(
                label = "学历",
                options = listOf("高中", "本科", "硕士", "博士"),
                selectedIndex = education,
                onSelectedChange = { education = it }
            )
        }
    }
}
