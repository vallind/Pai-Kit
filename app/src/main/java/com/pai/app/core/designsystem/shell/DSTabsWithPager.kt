// ============================================================================
// DSTabsWithPager.kt
// Tab + Pager 联动组件
// 支持 Top / Bottom 两种 Tab 位置，双向同步
// 复用 DSTabItem（来自 navigation 包）
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.shell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.shell.DSTabItem
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import kotlinx.coroutines.launch

/**
 * Tab 位置枚举
 * - [Top]：Tab 显示在 Pager 上方
 * - [Bottom]：Tab 显示在 Pager 下方
 */
internal enum class DSTabPosition {
    Top,
    Bottom
}

/**
 * DSTabsWithPager - Tab + Pager 联动组件
 *
 * 使用示例：
 * ```kotlin
 * val tabs = listOf(
 *     DSTabItem("推荐"),
 *     DSTabItem("关注"),
 *     DSTabItem("热门")
 * )
 * DSTabsWithPager(
 *     tabs = tabs,
 *     tabPosition = DSTabPosition.Top
 * ) { pageIndex ->
 *     when (pageIndex) {
 *         0 -> RecommendPage()
 *         1 -> FollowPage()
 *         2 -> HotPage()
 *     }
 * }
 * ```
 *
 * 设计规范：
 * - Tab 与 Pager 双向同步：
 *   - 点击 Tab → 调用 pagerState.animateScrollToPage 切换 Pager
 *   - 滑动 Pager → pagerState.currentPage 变化 → Tab 的 selected 状态自动更新
 * - 选中指示器高度 3dp，颜色 primary
 * - 选中文字 primary + SemiBold，未选中 onSurfaceVariant + Normal
 * - Tab 容器背景使用 surface，与页面背景区分
 * - 支持 [DSTabPosition.Top] / [DSTabPosition.Bottom] 两种位置
 * - 复用 [DSTabItem]（包含 title 与可选 badgeContent）
 *
 * @param tabs Tab 列表
 * @param content 每页内容，参数为页码（0-based）
 * @param modifier 修饰符
 * @param tabPosition Tab 位置，默认 Top
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSTabsWithPager(
    tabs: List<DSTabItem>,
    content: @Composable (Int) -> Unit,
    modifier: Modifier = Modifier,
    tabPosition: DSTabPosition = DSTabPosition.Top
) {
    if (tabs.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()

    // 点击 Tab 切换 Pager
    val onTabSelected: (Int) -> Unit = { index ->
        coroutineScope.launch { pagerState.animateScrollToPage(index) }
    }
    // 滑动 Pager 时 Tab 的 selected 状态由 pagerState.currentPage 驱动，无需额外同步

    val tabRow: @Composable () -> Unit = {
        PrimaryTabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = {
                TabRowDefaults.PrimaryIndicator(
                    height = DSTokens.ComponentHeight.tabIndicator,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                val selected = pagerState.currentPage == index
                Tab(
                    selected = selected,
                    onClick = { onTabSelected(index) },
                    text = {
                        if (tab.badgeContent != null) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xxs)
                            ) {
                                TabText(tab.title, selected)
                                tab.badgeContent()
                            }
                        } else {
                            TabText(tab.title, selected)
                        }
                    }
                )
            }
        }
    }

    val pager: @Composable () -> Unit = {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            content(page)
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        when (tabPosition) {
            DSTabPosition.Top -> {
                tabRow()
                pager()
            }
            DSTabPosition.Bottom -> {
                pager()
                tabRow()
            }
        }
    }
}

/**
 * Tab 标题文字
 */
@Composable
private fun TabText(title: String, selected: Boolean) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
        color = if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }
    )
}

// ============================================================================
// Preview
// ============================================================================

@Preview(showBackground = true, name = "DSTabsWithPager - Top")
@Composable
private fun DSTabsWithPagerTopPreview() {
    DSDesignTheme {
        DSTabsWithPager(
            tabs = listOf(
                DSTabItem("推荐"),
                DSTabItem("关注"),
                DSTabItem("热门")
            ),
            tabPosition = DSTabPosition.Top,
            content = { page ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "页面 ${page + 1}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "DSTabsWithPager - Bottom")
@Composable
private fun DSTabsWithPagerBottomPreview() {
    DSDesignTheme {
        DSTabsWithPager(
            tabs = listOf(
                DSTabItem("首页"),
                DSTabItem("分类"),
                DSTabItem("我的")
            ),
            tabPosition = DSTabPosition.Bottom,
            content = { page ->
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "内容 ${page + 1}",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true, name = "DSTabsWithPager - 带徽章")
@Composable
private fun DSTabsWithPagerBadgePreview() {
    DSDesignTheme {
        DSTabsWithPager(
            tabs = listOf(
                DSTabItem("消息"),
                DSTabItem(
                    title = "通知",
                    badgeContent = {
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = androidx.compose.foundation.shape.CircleShape,
                            modifier = Modifier.padding(start = DSTokens.Spacing.xxs)
                        ) {
                            Text(
                                text = "9",
                                color = MaterialTheme.colorScheme.onError,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                ),
                DSTabItem("待办")
            ),
            tabPosition = DSTabPosition.Top,
            content = { page ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Tab ${page + 1} 内容",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        )
    }
}
