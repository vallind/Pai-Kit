// ============================================================================
// DSSearchBar.kt
// 搜索栏 - 基于 M3 SearchBar 扩展
// 支持返回按钮、清除按钮、占位提示文字
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.overlays

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSSearchBar - 搜索栏
 *
 * 使用示例：
 * ```kotlin
 * var query by remember { mutableStateOf("") }
 * var active by remember { mutableStateOf(false) }
 *
 * DSSearchBar(
 *     query = query,
 *     onQueryChange = { query = it },
 *     onSearch = { keyword -> viewModel.search(keyword) },
 *     active = active,
 *     onActiveChange = { active = it },
 *     onBackClick = { navController.popBackStack() },
 *     placeholder = "搜索笔记、标签或文件夹"
 * )
 * ```
 *
 * 设计规范：
 * - 高度 56dp（M3 SearchBar 默认 inputField 高度）
 * - 圆角 full（胶囊形）
 * - 配色：surface 背景 + onSurface 文字，搜索时 primary 容器
 * - 返回按钮（leadingIcon）：ArrowBack，调用 onBackClick
 * - 清除按钮（trailingIcon）：Close，仅在 query 非空时显示
 * - 默认 icon（Search）在不传 onBackClick 时作为 leadingIcon 显示
 * - active 状态由调用方 hoisted，便于编程式展开/收起
 *
 * @param query 当前搜索词
 * @param onQueryChange 搜索词变更回调
 * @param onSearch 提交搜索回调（用户按下 IME 搜索键时触发，仅在 active 状态生效）
 * @param modifier 修饰符
 * @param active 是否处于展开（搜索结果面板）状态。传 null（默认）时使用内部 remember 状态。
 * @param onActiveChange active 状态变化回调，与 [active] 配合以提升状态。传 null 时由内部状态管理。
 * @param onBackClick 返回按钮回调，非空时显示返回箭头；为空时显示搜索图标
 * @param placeholder 占位提示文字
 * @param enabled 是否可用
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean? = null,
    onActiveChange: ((Boolean) -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    placeholder: String = "搜索",
    enabled: Boolean = true
) {
    // 当调用方未提供 active/onActiveChange 时使用内部 remember 状态；
    // 提供时则由调用方 hoisted 状态驱动，便于编程式展开/收起。
    var internalActive by remember { mutableStateOf(false) }
    val isActive = active ?: internalActive
    val updateActive: (Boolean) -> Unit = { expanded ->
        if (onActiveChange != null) onActiveChange(expanded) else internalActive = expanded
    }

    SearchBar(
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = { newQuery ->
                    updateActive(false)
                    onSearch(newQuery)
                },
                expanded = isActive,
                onExpandedChange = { expanded -> updateActive(expanded) },
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    if (onBackClick != null) {
                        IconButton(
                            onClick = {
                                updateActive(false)
                                onBackClick()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "返回",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(
                            onClick = { onQueryChange("") },
                            modifier = Modifier.semantics { contentDescription = "清除" }
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "清除",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        },
        expanded = isActive,
        onExpandedChange = { expanded -> updateActive(expanded) },
        modifier = modifier.fillMaxWidth(),
        shape = SearchBarDefaults.inputFieldShape,
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            dividerColor = MaterialTheme.colorScheme.outlineVariant
        ),
        tonalElevation = DSTokens.Elevation.level0,
        shadowElevation = DSTokens.Elevation.level0,
        content = {
            // 默认占位内容（active 模式下展示）
            // 业务方可在此处通过子组件叠加自定义建议词
            Box(modifier = Modifier.padding(DSTokens.Spacing.md))
        }
    )
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "SearchBar - Default (with back)")
@Composable
private fun DSSearchBarDefaultPreview() {
    var query by remember { mutableStateOf("") }
    DSDesignTheme {
        DSSearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = {},
            onBackClick = {},
            placeholder = "搜索笔记"
        )
    }
}

@Preview(showBackground = true, name = "SearchBar - With Query")
@Composable
private fun DSSearchBarWithQueryPreview() {
    var query by remember { mutableStateOf("Jetpack Compose") }
    DSDesignTheme {
        DSSearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = {},
            onBackClick = {},
            placeholder = "搜索笔记"
        )
    }
}

@Preview(showBackground = true, name = "SearchBar - No Back (leading = search icon)")
@Composable
private fun DSSearchBarNoBackPreview() {
    var query by remember { mutableStateOf("") }
    DSDesignTheme {
        DSSearchBar(
            query = query,
            onQueryChange = { query = it },
            onSearch = {},
            placeholder = "搜索"
        )
    }
}
