// ============================================================================
// DSCarousel.kt
// 轮播图组件 - 基于 HorizontalPager + 自定义圆点指示器
// 支持自动轮播与手动滑动
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import kotlinx.coroutines.delay

/**
 * DSCarousel - 轮播图
 *
 * 使用示例：
 * ```kotlin
 * val images = listOf("banner1", "banner2", "banner3")
 * DSCarousel(
 *     items = images,
 *     autoScroll = true,
 *     autoScrollInterval = 4000,
 *     onPageChange = { page -> Log.d("Carousel", "Current page: $page") }
 * ) { item ->
 *     AsyncImage(
 *         model = item,
 *         contentDescription = null,
 *         modifier = Modifier.fillMaxSize()
 *     )
 * }
 * ```
 *
 * 设计规范：
 * - 使用 HorizontalPager（Foundation 1.7+ 稳定 API）实现水平滑动
 * - 底部圆点指示器：当前页 24dp×6dp 胶囊形 primary 色，非当前页 6dp 圆点 surfaceVariant 色
 * - 圆点颜色与宽度均使用 animateColorAsState / animateFloatAsState 平滑过渡
 * - 自动轮播通过 LaunchedEffect + delay 实现：
 *   - 用户拖动时跳过本次翻页，等用户释放后再继续
 *   - 仅当 items.size > 1 时启用，避免单页无意义循环
 * - rememberInfiniteTransition 用于活跃圆点的呼吸缩放动画（autoScroll 启用时）
 *   作为「自动播放中」的视觉提示
 *
 * @param modifier 修饰符
 * @param items 数据列表
 * @param itemContent 单项渲染函数
 * @param autoScroll 是否启用自动轮播
 * @param autoScrollInterval 自动轮播间隔（毫秒），默认 3000ms
 * @param onPageChange 页面变化回调
 * @param T 数据类型
 */
@Composable
internal fun <T> DSCarousel(
    items: List<T>,
    itemContent: @Composable (T) -> Unit,
    modifier: Modifier = Modifier,
    autoScroll: Boolean = false,
    autoScrollInterval: Int = 3000,
    onPageChange: ((page: Int) -> Unit)? = null
) {
    if (items.isEmpty()) return

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { items.size }
    )

    // 页面变化回调
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collect { page -> onPageChange?.invoke(page) }
    }

    // 自动轮播逻辑
    if (autoScroll && items.size > 1) {
        LaunchedEffect(pagerState, autoScrollInterval) {
            while (true) {
                delay(autoScrollInterval.toLong())
                // 若用户正在拖动则跳过本次翻页
                if (!pagerState.isScrollInProgress) {
                    val nextPage = (pagerState.currentPage + 1) % items.size
                    pagerState.animateScrollToPage(nextPage)
                }
            }
        }
    }

    // 自动轮播开启时活跃圆点的呼吸缩放动画
    val pulseTransition = rememberInfiniteTransition(label = "carouselPulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = DSTokens.Duration.long1,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Box(modifier = modifier) {
        // ---------------------------------------------------------------------
        // Pager 主体
        // ---------------------------------------------------------------------
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            itemContent(items[page])
        }

        // ---------------------------------------------------------------------
        // 底部圆点指示器
        // ---------------------------------------------------------------------
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = DSTokens.Spacing.md),
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(items.size) { index ->
                val isActive = pagerState.currentPage == index
                DotIndicator(
                    isActive = isActive,
                    activeScale = if (autoScroll && items.size > 1 && isActive) {
                        pulseScale
                    } else {
                        1f
                    }
                )
            }
        }
    }
}

/**
 * 单个圆点指示器
 *
 * 使用 Box + animateColorAsState 实现颜色过渡
 * 使用 animateFloatAsState 实现宽度过渡
 */
@Composable
private fun DotIndicator(
    isActive: Boolean,
    activeScale: Float
) {
    val targetColor = if (isActive) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val color by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(
            durationMillis = DSTokens.Duration.medium2,
            easing = DSTokens.Easing.standard
        ),
        label = "dotColor"
    )

    val width by animateFloatAsState(
        targetValue = if (isActive) 24f else 6f,
        animationSpec = tween(
            durationMillis = DSTokens.Duration.medium2,
            easing = DSTokens.Easing.standard
        ),
        label = "dotWidth"
    )

    Box(
        modifier = Modifier
            // 圆点高度 6dp，carousel 指示器专用尺寸（暂未抽取为独立 token）
            .size(width = width.dp, height = 6.dp)
            .graphicsLayer {
                scaleX = if (isActive) activeScale else 1f
                scaleY = if (isActive) activeScale else 1f
            }
            .background(color = color, shape = CircleShape)
    )
}

// ============================================================================
// Preview
// ============================================================================

@Preview(showBackground = true, name = "DSCarousel - 三张卡片")
@Composable
private fun DSCarouselPreview() {
    DSDesignTheme {
        val pages = listOf("第一页", "第二页", "第三页")
        DSCarousel(
            items = pages,
            itemContent = { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(DSTokens.Radius.large)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = page,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "DSCarousel - 自动轮播")
@Composable
private fun DSCarouselAutoScrollPreview() {
    DSDesignTheme {
        val pages = listOf("Banner A", "Banner B", "Banner C", "Banner D")
        DSCarousel(
            items = pages,
            autoScroll = true,
            autoScrollInterval = 3000,
            itemContent = { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            shape = RoundedCornerShape(DSTokens.Radius.large)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = page,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "DSCarousel - 单项")
@Composable
private fun DSCarouselSinglePreview() {
    DSDesignTheme {
        DSCarousel(
            items = listOf("只有一页"),
            itemContent = { page ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            shape = RoundedCornerShape(DSTokens.Radius.large)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = page,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .padding(16.dp)
        )
    }
}
