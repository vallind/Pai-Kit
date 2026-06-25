// ============================================================================
// DSTabs.kt
// 标签页 - 基于 M3 TabRow 扩展
// 提供：固定 Tab / 可滚动 Tab / 分段控件
// ============================================================================

package com.pai.app.core.designsystem.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * Tab 项
 *
 * @param title 标题
 * @param badgeContent 可选徽章内容
 */
internal data class DSTabItem(
    val title: String,
    val badgeContent: (@Composable () -> Unit)? = null
)

/**
 * DSTabRow - 标签栏
 *
 * 使用示例：
 * ```kotlin
 * val tabs = listOf(DSTabItem("推荐"), DSTabItem("关注"), DSTabItem("热门"))
 * var selectedIndex by remember { mutableIntStateOf(0) }
 * DSTabRow(
 *     tabs = tabs,
 *     selectedIndex = selectedIndex,
 *     onTabSelected = { selectedIndex = it }
 * )
 * ```
 *
 * 设计规范：
 * - 固定 Tab：≤ 4 个时使用
 * - 可滚动 Tab：> 4 个时使用
 * - 选中指示器高度 3dp，颜色 primary
 * - 选中文字字重 SemiBold
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSTabRow(
    tabs: List<DSTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    if (scrollable) {
        SecondaryScrollableTabRow(
            selectedTabIndex = selectedIndex,
            modifier = modifier,
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.primary,
            // edgePadding=0：SecondaryScrollableTabRow 在 scrollable 模式下首个 tab 左边无额外缩进
            edgePadding = 0.dp,
            divider = {},
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onTabSelected(index) },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = if (selectedIndex == index)
                                FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                )
            }
        }
    } else {
        PrimaryTabRow(
            selectedTabIndex = selectedIndex,
            modifier = modifier,
            containerColor = containerColor,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    height = DSTokens.ComponentHeight.tabIndicator,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedIndex == index,
                    onClick = { onTabSelected(index) },
                    text = {
                        if (tab.badgeContent != null) {
                            androidx.compose.foundation.layout.Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)
                            ) {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = if (selectedIndex == index)
                                        FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selectedIndex == index)
                                        MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                tab.badgeContent()
                            }
                        } else {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedIndex == index)
                                    FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selectedIndex == index)
                                    MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Tab Row")
@Composable
private fun DSTabRowPreview() {
    DSDesignTheme {
        val tabs = listOf(
            DSTabItem("推荐"),
            DSTabItem("关注"),
            DSTabItem("热门"),
            DSTabItem("视频")
        )
        var selected by remember { mutableIntStateOf(0) }
        DSTabRow(
            tabs = tabs,
            selectedIndex = selected,
            onTabSelected = { selected = it }
        )
    }
}
