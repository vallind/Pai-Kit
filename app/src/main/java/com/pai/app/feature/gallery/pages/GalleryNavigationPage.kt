// ============================================================================
// GalleryNavigationPage.kt
// 导航组件展示页：TopBar 3 风格 + TabRow + BottomBar + AnimatedBottomBar + AppBar
// ============================================================================
//
// M4 注：本文件含演示用硬编码 dp（如 DSAppScaffold 预览区高度 220/260/200dp）——
// 仅用于导航组件演示，业务代码请用 DSTokens.ComponentHeight.* / DSTokens.Spacing.*。
// ============================================================================
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.pai.app.feature.gallery.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.shell.DSBottomAppBar
import com.pai.app.core.designsystem.shell.DSAppScaffold
import com.pai.app.core.designsystem.shell.DSBottomBar
import com.pai.app.core.designsystem.foundation.tokens.DSNavItem
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.shell.DSTabItem
import com.pai.app.core.designsystem.shell.DSTabRow
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.shell.DSTopBar
import com.pai.app.core.designsystem.shell.DSTopBarAction
import com.pai.app.core.designsystem.shell.DSTopBarStyle
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * GalleryNavigationPage - 导航组件展示页
 *
 * 内容分八节：
 * 1. DSTopBar 三种风格：Small / CenterAligned / Medium
 * 2. DSTabRow 固定 + 可滚动
 * 3. DSBottomBar 静态底部导航
 * 4. DSBottomBar animated=true 带动效底部导航（点击可切换选中态）
 * 5. DSBottomAppBar 底部应用栏（含 FAB 凹槽）
 * 6. DSAppScaffold 标准用法：title + 返回按钮 + 操作按钮 + 内容区
 * 7. DSAppScaffold 大标题模式：useLargeTopBar = true
 * 8. DSAppScaffold 无顶栏模式：简化版（仅 Scaffold + Snackbar + 底栏）
 *
 * @param onBackClick 返回上一页回调
 */
@Composable
internal fun GalleryNavigationPage(onBackClick: () -> Unit) {
    // H15：用 DSAppScaffold 替代 material3.Scaffold
    DSAppScaffold(
        title = "导航 Navigation",
        showBackIcon = true,
        onBackClick = onBackClick,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DSTokens.Spacing.lg, vertical = DSTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg)
        ) {
            // 1. DSTopBar 三种风格
            SectionCard(title = "DSTopBar 三种风格") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                    // H15：用 Box+background 替代 material3.Surface（surfaceVariant 背景 + medium 圆角）
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium,
                            )
                    ) {
                        Column {
                            DSText(
                                text = "Small 风格",
                                variant = DSTextVariant.LabelSmall,
                                color = DSTextColor.Secondary,
                                modifier = Modifier.padding(start = DSTokens.Spacing.lg, top = DSTokens.Spacing.xs)
                            )
                            DSTopBar(
                                title = "Small TopBar",
                                style = DSTopBarStyle.Small,
                                actions = listOf(
                                    DSTopBarAction(Icons.Default.Search, "搜索") {},
                                    DSTopBarAction(Icons.Default.Notifications, "通知") {}
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium,
                            )
                    ) {
                        Column {
                            DSText(
                                text = "CenterAligned 风格",
                                variant = DSTextVariant.LabelSmall,
                                color = DSTextColor.Secondary,
                                modifier = Modifier.padding(start = DSTokens.Spacing.lg, top = DSTokens.Spacing.xs)
                            )
                            DSTopBar(
                                title = "居中标题",
                                style = DSTopBarStyle.CenterAligned,
                                onBackClick = null,
                                actions = listOf(
                                    DSTopBarAction(Icons.Default.Search, "搜索") {}
                                )
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = MaterialTheme.shapes.medium,
                            )
                    ) {
                        Column {
                            DSText(
                                text = "Medium 风格",
                                variant = DSTextVariant.LabelSmall,
                                color = DSTextColor.Secondary,
                                modifier = Modifier.padding(start = DSTokens.Spacing.lg, top = DSTokens.Spacing.xs)
                            )
                            DSTopBar(
                                title = "Medium TopBar",
                                subtitle = "大标题 + 副标题，可滚动折叠",
                                style = DSTopBarStyle.Medium
                            )
                        }
                    }
                }
            }

            // 2. DSTabRow 固定 + 可滚动
            SectionCard(title = "DSTabRow 标签栏") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                    DSText(
                        text = "固定 Tab（≤4 项）",
                        variant = DSTextVariant.LabelMedium,
                        color = DSTextColor.Secondary
                    )
                    val fixedTabs = listOf(
                        DSTabItem("推荐"),
                        DSTabItem("关注"),
                        DSTabItem("热门"),
                        DSTabItem("视频")
                    )
                    var fixedSelected by remember { mutableIntStateOf(0) }
                    DSTabRow(
                        tabs = fixedTabs,
                        selectedIndex = fixedSelected,
                        onTabSelected = { fixedSelected = it }
                    )

                    DSText(
                        text = "可滚动 Tab（>4 项）",
                        variant = DSTextVariant.LabelMedium,
                        color = DSTextColor.Secondary
                    )
                    val scrollableTabs = listOf(
                        DSTabItem("关注"), DSTabItem("推荐"), DSTabItem("热门"),
                        DSTabItem("视频"), DSTabItem("同城"), DSTabItem("科技"),
                        DSTabItem("设计"), DSTabItem("美食")
                    )
                    var scrollableSelected by remember { mutableIntStateOf(0) }
                    DSTabRow(
                        tabs = scrollableTabs,
                        selectedIndex = scrollableSelected,
                        onTabSelected = { scrollableSelected = it },
                        scrollable = true
                    )
                }
            }

            // 3. DSBottomBar 静态底部导航
            SectionCard(title = "DSBottomBar 静态底部导航") {
                DSBottomBar(
                    items = listOf(
                        DSNavItem(label = "首页", icon = Icons.Default.Home),
                        DSNavItem(label = "发现", icon = Icons.Default.Search),
                        DSNavItem(label = "收藏", icon = Icons.Default.Favorite),
                        DSNavItem(label = "我的", icon = Icons.Default.Person)
                    ),
                    selectedItem = "首页",
                    onItemSelected = {}
                )
            }

            // 4. DSBottomBar animated=true 带动效底部导航
            SectionCard(title = "DSBottomBar animated=true 带动效") {
                var selectedIndex by remember { mutableIntStateOf(0) }
                DSText(
                    text = "点击下方按钮体验选中图标的缩放动画",
                    variant = DSTextVariant.LabelMedium,
                    color = DSTextColor.Secondary
                )
                val labels = listOf("首页", "发现", "收藏", "我的")
                val icons = listOf(
                    Icons.Default.Home,
                    Icons.Default.Search,
                    Icons.Default.Favorite,
                    Icons.Default.Person
                )
                DSBottomBar(
                    items = labels.indices.map { i ->
                        DSNavItem(label = labels[i], icon = icons[i])
                    },
                    selectedItem = labels[selectedIndex],
                    onItemSelected = { id -> selectedIndex = labels.indexOf(id) },
                    animated = true
                )
            }

            // 5. DSBottomAppBar 底部应用栏（含 FAB 凹槽）
            SectionCard(title = "DSBottomAppBar 底部应用栏（含 FAB 凹槽）") {
                DSBottomAppBar(
                    title = "新建笔记",
                    actions = listOf(
                        DSTopBarAction(Icons.Default.Search, "搜索") {},
                        DSTopBarAction(Icons.Default.Edit, "编辑") {},
                        DSTopBarAction(Icons.Default.Settings, "设置") {}
                    ),
                    fabIcon = Icons.Default.Add,
                    fabContentDescription = "新建",
                    onFabClick = {}
                )
            }

            // 6. DSAppScaffold 标准用法
            SectionCard(title = "DSAppScaffold 标准用法") {
                DSText(
                    text = "title + 返回按钮 + 操作按钮 + 内容区，封装 M3 Scaffold 样板代码",
                    variant = DSTextVariant.LabelMedium,
                    color = DSTextColor.Secondary
                )
                AppScaffoldStandardDemo()
            }

            // 7. DSAppScaffold 大标题模式
            SectionCard(title = "DSAppScaffold 大标题模式") {
                DSText(
                    text = "useLargeTopBar = true，Medium 风格 + 滚动折叠",
                    variant = DSTextVariant.LabelMedium,
                    color = DSTextColor.Secondary
                )
                AppScaffoldLargeTopBarDemo()
            }

            // 8. DSAppScaffold 无顶栏模式
            SectionCard(title = "DSAppScaffold 无顶栏模式") {
                DSText(
                    text = "简化版重载，无 title 参数；适合登录页 / 全屏内容页",
                    variant = DSTextVariant.LabelMedium,
                    color = DSTextColor.Secondary
                )
                AppScaffoldNoTopBarDemo()
            }
        }
    }
}

// ============================================================================
// DSAppScaffold 演示
// ============================================================================

/**
 * DSAppScaffold 标准用法演示
 *
 * 通过 title / showBackIcon / topBarActions 配置顶栏，
 * 通过 content lambda 接收 PaddingValues 并渲染页面内容。
 * 由于嵌入卡片内，外层用固定高度 220dp 限制预览区域。
 */
@Composable
private fun AppScaffoldStandardDemo() {
    // H15：用 Box+background 替代 material3.Surface（限定预览区域高度 + 背景色）
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
            )
    ) {
        DSAppScaffold(
            title = "标准 Scaffold",
            showBackIcon = true,
            onBackClick = {},
            topBarActions = listOf(
                DSTopBarAction(Icons.Default.Search, "搜索") {},
                DSTopBarAction(Icons.Default.MoreVert, "更多") {}
            )
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(DSTokens.Spacing.lg),
                contentAlignment = Alignment.Center
            ) {
                DSText(
                    text = "内容区域（PaddingValues 由 DSAppScaffold 自动注入）",
                    variant = DSTextVariant.BodyMedium,
                    color = DSTextColor.Secondary
                )
            }
        }
    }
}

/**
 * DSAppScaffold 大标题模式演示
 *
 * useLargeTopBar = true 时，DSAppScaffold 内部使用 Medium 风格 TopBar +
 * exitUntilCollapsedScrollBehavior，内容向上滚动时大标题会折叠为小标题。
 * 由于嵌入卡片内，外层用固定高度 260dp 限制预览区域。
 */
@Composable
private fun AppScaffoldLargeTopBarDemo() {
    // H15：用 Box+background 替代 material3.Surface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
            )
    ) {
        DSAppScaffold(
            title = "大标题模式 Large TopBar",
            useLargeTopBar = true,
            showBackIcon = true,
            onBackClick = {},
            topBarActions = listOf(
                DSTopBarAction(Icons.Default.Edit, "编辑") {}
            )
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = DSTokens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
            ) {
                // 滚动内容触发大标题折叠
                repeat(6) { index ->
                    DSText(
                        text = "列表项 ${index + 1}：向上滚动观察标题折叠",
                        variant = DSTextVariant.BodyMedium,
                        color = DSTextColor.Primary
                    )
                }
            }
        }
    }
}

/**
 * DSAppScaffold 无顶栏模式演示
 *
 * 调用简化版重载（无 title / topBarActions / showBackIcon 参数），
 * 仅含 Scaffold + SnackbarHost + 可选 bottomBar，适合全屏内容页 / 登录页。
 * 由于嵌入卡片内，外层用固定高度 200dp 限制预览区域。
 */
@Composable
private fun AppScaffoldNoTopBarDemo() {
    // H15：用 Box+background 替代 material3.Surface
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium,
            )
    ) {
        DSAppScaffold(
            bottomBar = {
                DSBottomBar(
                    items = listOf(
                        DSNavItem(label = "首页", icon = Icons.Default.Home),
                        DSNavItem(label = "发现", icon = Icons.Default.Search),
                        DSNavItem(label = "我的", icon = Icons.Default.Person)
                    ),
                    selectedItem = "首页",
                    onItemSelected = {}
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                DSText(
                    text = "无顶栏内容区域（仅底栏）",
                    variant = DSTextVariant.BodyMedium,
                    color = DSTextColor.Secondary
                )
            }
        }
    }
}
