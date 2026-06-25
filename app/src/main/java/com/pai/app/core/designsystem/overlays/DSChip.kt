// ============================================================================
// DSChip.kt
// Chip 标签（Assist / Filter / Input / Suggestion）
// ============================================================================

package com.pai.app.core.designsystem.overlays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * Chip 风格
 */
internal enum class DSChipStyle {
    Assist,    // 辅助 Chip（可点击）
    Filter,    // 筛选 Chip（可选）
    Input,     // 输入 Chip（可删除）
    Suggestion // 建议 Chip（可点击）
}

/**
 * DSChip - 标签 Chip
 *
 * 使用示例：
 * ```kotlin
 * var selected by remember { mutableStateOf(false) }
 * DSChip(
 *     text = "热门",
 *     style = DSChipStyle.Filter,
 *     selected = selected,
 *     onClick = { selected = !selected }
 * )
 * ```
 *
 * @param text Chip 文案
 * @param modifier 修饰符
 * @param style Chip 类型，默认 [DSChipStyle.Assist]
 * @param selected 是否选中（仅 Filter / Input 生效）
 * @param enabled 是否启用
 * @param onClick 点击回调（Input 可空，仅用于删除以外的交互）
 * @param onDismiss 删除回调（仅 Input 生效，显示 trailing 关闭图标）
 * @param leadingIcon 前置图标（可选）
 */
@Composable
internal fun DSChip(
    text: String,
    modifier: Modifier = Modifier,
    style: DSChipStyle = DSChipStyle.Assist,
    selected: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    leadingIcon: ImageVector? = null
) {
    val shape = RoundedCornerShape(DSTokens.Radius.small)

    when (style) {
        DSChipStyle.Assist -> {
            AssistChip(
                onClick = onClick ?: {},
                label = {
                    Text(text = text, style = MaterialTheme.typography.labelLarge)
                },
                leadingIcon = leadingIcon?.let {
                    { Icon(it, contentDescription = null, modifier = Modifier.size(18.dp)) }
                },
                shape = shape,
                colors = AssistChipDefaults.assistChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = AssistChipDefaults.assistChipBorder(
                    enabled = enabled,
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                enabled = enabled,
                modifier = modifier
            )
        }
        DSChipStyle.Filter -> {
            FilterChip(
                selected = selected,
                onClick = onClick ?: {},
                label = {
                    Text(text = text, style = MaterialTheme.typography.labelLarge)
                },
                leadingIcon = if (selected) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                } else null,
                shape = shape,
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = enabled,
                    selected = selected,
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                enabled = enabled,
                modifier = modifier
            )
        }
        DSChipStyle.Input -> {
            InputChip(
                selected = selected,
                onClick = onClick ?: {},
                label = {
                    Text(text = text, style = MaterialTheme.typography.labelLarge)
                },
                trailingIcon = onDismiss?.let {
                    {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = "删除",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                shape = shape,
                colors = InputChipDefaults.inputChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                enabled = enabled,
                modifier = modifier
            )
        }
        DSChipStyle.Suggestion -> {
            SuggestionChip(
                onClick = onClick ?: {},
                label = {
                    Text(text = text, style = MaterialTheme.typography.labelLarge)
                },
                shape = shape,
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                border = SuggestionChipDefaults.suggestionChipBorder(
                    enabled = enabled,
                    borderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                enabled = enabled,
                modifier = modifier
            )
        }
    }
}

@Preview(showBackground = true, name = "Chips")
@Composable
private fun DSChipPreview() {
    DSDesignTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DSChip(text = "Assist", style = DSChipStyle.Assist, onClick = {})
                DSChip(text = "Suggestion", style = DSChipStyle.Suggestion, onClick = {})
            }
            var filterSelected by remember { mutableStateOf(true) }
            DSChip(
                text = "Filter",
                style = DSChipStyle.Filter,
                selected = filterSelected,
                onClick = { filterSelected = !filterSelected }
            )
            DSChip(text = "Input", style = DSChipStyle.Input, onDismiss = {})
        }
    }
}
