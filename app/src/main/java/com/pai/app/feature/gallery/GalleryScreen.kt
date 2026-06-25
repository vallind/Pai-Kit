// ============================================================================
// GalleryScreen.kt
// 组件 Gallery 主页：以 8 张 DSCard 罗列 8 大组件分类入口
// 点击卡片通过 onNavigateToPage 回调跳转到对应子页面
// ============================================================================
//
// M4 注：本文件含演示用硬编码 dp（如卡片图标块尺寸）—— 仅用于 Gallery 演示，
// 业务代码请用 DSTokens.ComponentHeight.* / DSTokens.Spacing.*。
// ============================================================================
package com.pai.app.feature.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Animation
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.primitives.DSIcon
import com.pai.app.core.designsystem.primitives.DSIconSize
import com.pai.app.core.designsystem.primitives.DSIconTint
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.shell.DSAppScaffold
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import com.pai.app.navigation.routes.AppRoute
import com.pai.app.navigation.routes.GalleryButtonRoute
import com.pai.app.navigation.routes.GalleryContainerRoute
import com.pai.app.navigation.routes.GalleryFeedbackRoute
import com.pai.app.navigation.routes.GalleryFormRoute
import com.pai.app.navigation.routes.GalleryLayoutRoute
import com.pai.app.navigation.routes.GalleryMotionRoute
import com.pai.app.navigation.routes.GalleryNavigationRoute
import com.pai.app.navigation.routes.GalleryTextRoute
import com.pai.app.navigation.routes.GalleryThemeRoute

/**
 * 单个 Gallery 入口卡片的数据模型
 *
 * @param icon 卡片左侧图标
 * @param title 卡片标题
 * @param description 卡片描述
 * @param route 跳转路由（@Serializable AppRoute）
 */
private data class GalleryEntry(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val route: AppRoute,
)

/**
 * GalleryScreen - 组件 Gallery 主页
 *
 * 整体结构：
 * - topBar: DSTopBar "组件 Gallery" + 返回按钮
 * - 内容：LazyColumn 渲染 8 张 DSCard 入口
 *   - 主题 / 按钮 / 文本 / 表单 / 导航 / 容器 / 反馈 / 动效 / 布局
 *   - 每张卡片含图标 + 标题 + 描述 + 右侧箭头
 *   - 点击通过 [onNavigateToPage] 触发跳转到对应 GalleryXxxRoute
 *
 * 注意（H14 修复）：原第 9 张"Lottie 动画"卡复用了 GalleryMotionRoute（重复路由），
 * 已删除该卡 —— Motion 页内本身已含 Lottie 演示，无需重复入口。
 *
 * @param onBackClick 返回上一页回调
 * @param onNavigateToPage 子页面跳转回调，参数为对应 GalleryXxxRoute
 */
@Composable
internal fun GalleryScreen(
    onBackClick: () -> Unit,
    onNavigateToPage: (AppRoute) -> Unit,
) {
    // 8 个 Gallery 入口配置（H14：原 Lottie 卡已合并到 Motion 页）
    val entries = listOf(
        GalleryEntry(
            icon = Icons.Default.Palette,
            title = "主题 Theme",
            description = "颜色板 / 字号阶 / 圆角 / 间距 Token 一览",
            route = GalleryThemeRoute,
        ),
        GalleryEntry(
            icon = Icons.Default.TouchApp,
            title = "按钮 Button",
            description = "5 种风格 × 3 种尺寸 + 图标 / 加载 / 禁用 + IconButton / FAB",
            route = GalleryButtonRoute,
        ),
        GalleryEntry(
            icon = Icons.Default.TextFields,
            title = "文本 Text",
            description = "15 个字号阶 × 10 种颜色语义 × 4 种字重",
            route = GalleryTextRoute,
        ),
        GalleryEntry(
            icon = Icons.Default.Input,
            title = "表单 Form",
            description = "TextField / TextArea / Segmented / Stepper / Dropdown / 选择器 / Slider",
            route = GalleryFormRoute,
        ),
        GalleryEntry(
            icon = Icons.Default.Dashboard,
            title = "导航 Navigation",
            description = "TopBar / TabRow / BottomBar / AnimatedBottomBar / AppBar",
            route = GalleryNavigationRoute,
        ),
        GalleryEntry(
            icon = Icons.Default.Category,
            title = "容器 Container",
            description = "Card / Chip / ListItem / Accordion / Banner / Dialog / BottomSheet / Carousel / Grid / Pagination",
            route = GalleryContainerRoute,
        ),
        GalleryEntry(
            icon = Icons.Default.BubbleChart,
            title = "反馈 Feedback",
            description = "Progress / LoadingOverlay / Skeleton / EmptyState / Badge / Avatar / Tag / Snackbar",
            route = GalleryFeedbackRoute,
        ),
        GalleryEntry(
            icon = Icons.Default.Animation,
            title = "动效 Motion",
            description = "pressScale / listItemEnter / AnimatedVisibility / Lottie / 底部栏对比",
            route = GalleryMotionRoute,
        ),
        GalleryEntry(
            icon = Icons.Default.ViewQuilt,
            title = "布局 Layout",
            description = "Box / Column / Row / LazyList / Scroll / Spacer",
            route = GalleryLayoutRoute,
        ),
    )

    DSAppScaffold(
        title = "组件 Gallery",
        showBackIcon = true,
        onBackClick = onBackClick,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = DSTokens.Spacing.lg, vertical = DSTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
        ) {
            items(items = entries, key = { it.route::class.qualifiedName.orEmpty() + it.title }) { entry ->
                GalleryEntryCard(entry = entry, onClick = { onNavigateToPage(entry.route) })
            }
        }
    }
}

/**
 * 单个 Gallery 入口卡片
 *
 * @param entry 入口数据
 * @param onClick 点击回调
 */
@Composable
private fun GalleryEntryCard(
    entry: GalleryEntry,
    onClick: () -> Unit
) {
    DSCard(
        modifier = Modifier.fillMaxWidth(),
        style = DSCardStyle.Elevated,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
        ) {
            // 左侧图标块：primaryContainer 背景 + onPrimaryContainer 图标
            Box(
                modifier = Modifier
                    .size(DSTokens.ComponentHeight.buttonLarge)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(DSTokens.Radius.medium)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // H15：用 DSIcon 替代 material3.Icon；onPrimaryContainer 无直接 DSIconTint 枚举，
                // 用 Default（onSurface）作近似，视觉差异可接受（gallery 演示页）
                DSIcon(
                    imageVector = entry.icon,
                    contentDescription = null,
                    size = DSIconSize.Medium,
                    tint = DSIconTint.Default,
                )
            }

            // 中部文本：标题 + 描述
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)
            ) {
                DSText(
                    text = entry.title,
                    variant = DSTextVariant.TitleMedium,
                    color = DSTextColor.Primary
                )
                DSText(
                    text = entry.description,
                    variant = DSTextVariant.BodySmall,
                    color = DSTextColor.Secondary,
                    maxLines = 2
                )
            }

            // 右侧箭头（H15：用 DSIcon 替代 material3.Icon）
            DSIcon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                size = DSIconSize.Medium,
                tint = DSIconTint.Secondary,
            )
        }
    }
}
