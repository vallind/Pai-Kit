// ============================================================================
// DSSkeleton.kt
// 骨架屏组件 - 加载占位
// M3 卓越线补齐：原子组件 #9 完整覆盖
// ============================================================================

package com.pai.app.core.designsystem.primitives

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.theme.extendedColors
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSSkeleton - 骨架屏基础块
 *
 * 用闪烁动画的色块占位内容，常用于列表、卡片、详情页的加载态。
 *
 * 使用示例：
 * ```kotlin
 * if (uiState.isLoading) {
 *     DSSkeletonBlock(
 *         modifier = Modifier.fillMaxWidth().height(120.dp),
 *         shape = RoundedCornerShape(12.dp)
 *     )
 * } else {
 *     ContentCard(data = uiState.data)
 * }
 * ```
 *
 * 设计规范：
 * - 基础色：extendedColors.skeletonBase
 * - 高亮色：extendedColors.skeletonHighlight
 * - 闪烁周期：800ms，LinearEasing，RepeatMode.Reverse
 * - 透明度从 1.0 到 0.6 振荡
 *
 * @param modifier 修饰符
 * @param shape 块形状，默认 RoundedCornerShape(4dp)
 */
@Composable
internal fun DSSkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(DSTokens.Radius.extraSmall)
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(DSTokens.Duration.skeleton, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "skeletonAlpha"
    )
    val skeletonColor = MaterialTheme.extendedColors.skeletonBase
    Box(
        modifier = modifier
            .clip(shape)
            .background(skeletonColor)
            .graphicsLayer { this.alpha = alpha }
    )
}

/**
 * DSSkeletonText - 文本骨架
 *
 * 模拟多行文本的占位。每行宽度递减，符合真实文本视觉。
 *
 * @param modifier 修饰符
 * @param lineCount 行数
 * @param lineHeight 行高
 * @param lineSpacing 行间距
 */
@Composable
internal fun DSSkeletonText(
    modifier: Modifier = Modifier,
    lineCount: Int = 2,
    lineHeight: Dp = 16.dp,
    lineSpacing: Dp = 8.dp
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(lineSpacing)
    ) {
        repeat(lineCount) { index ->
            val isLastLine = index == lineCount - 1
            val widthFraction = if (isLastLine) 0.6f else 1.0f
            DSSkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(widthFraction)
                    .height(lineHeight),
                shape = RoundedCornerShape(DSTokens.Radius.extraSmall)
            )
        }
    }
}

/**
 * DSSkeletonCircle - 圆形骨架
 *
 * 模拟头像、图标占位。
 *
 * @param modifier 修饰符
 * @param size 直径
 */
@Composable
internal fun DSSkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    DSSkeletonBlock(
        modifier = modifier.size(size),
        shape = CircleShape
    )
}

/**
 * DSSkeletonListItem - 列表项骨架
 *
 * 完整列表项占位：左侧圆形头像 + 右侧两行文本。
 * 用于 LazyColumn 加载态。
 *
 * @param modifier 修饰符
 * @param avatarSize 头像尺寸，默认 40dp
 */
@Composable
internal fun DSSkeletonListItem(
    modifier: Modifier = Modifier,
    avatarSize: Dp = 40.dp
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(DSTokens.Spacing.md),
        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        DSSkeletonCircle(size = avatarSize)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)
        ) {
            DSSkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(16.dp)
            )
            DSSkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(12.dp)
            )
        }
    }
}

/**
 * DSSkeletonCard - 卡片骨架
 *
 * 完整卡片占位：图片块 + 标题 + 副标题 + 操作行。
 *
 * @param modifier 修饰符
 * @param imageHeight 顶部图片块高度，默认 120dp
 */
@Composable
internal fun DSSkeletonCard(
    modifier: Modifier = Modifier,
    imageHeight: Dp = 120.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(DSTokens.Spacing.md)
    ) {
        // 顶部图片块
        DSSkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(imageHeight),
            shape = RoundedCornerShape(DSTokens.Radius.medium)
        )
        Spacer(modifier = Modifier.height(DSTokens.Spacing.md))
        // 标题
        DSSkeletonBlock(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(20.dp)
        )
        Spacer(modifier = Modifier.height(DSTokens.Spacing.xs))
        // 副标题
        DSSkeletonText(lineCount = 2, lineHeight = 12.dp)
        Spacer(modifier = Modifier.height(DSTokens.Spacing.md))
        // 底部操作行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
        ) {
            DSSkeletonBlock(
                modifier = Modifier
                    .width(80.dp)
                    .height(32.dp),
                shape = RoundedCornerShape(DSTokens.Radius.small)
            )
            DSSkeletonBlock(
                modifier = Modifier
                    .width(80.dp)
                    .height(32.dp),
                shape = RoundedCornerShape(DSTokens.Radius.small)
            )
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "Skeleton - Block")
@Composable
private fun DSSkeletonBlockPreview() {
    DSDesignTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DSSkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Skeleton - Text")
@Composable
private fun DSSkeletonTextPreview() {
    DSDesignTheme {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            DSSkeletonText(lineCount = 3)
        }
    }
}

@Preview(showBackground = true, name = "Skeleton - Circle")
@Composable
private fun DSSkeletonCirclePreview() {
    DSDesignTheme {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DSSkeletonCircle(size = 24.dp)
            DSSkeletonCircle(size = 40.dp)
            DSSkeletonCircle(size = 64.dp)
        }
    }
}

@Preview(showBackground = true, name = "Skeleton - ListItem", widthDp = 360)
@Composable
private fun DSSkeletonListItemPreview() {
    DSDesignTheme {
        Column(modifier = Modifier.fillMaxWidth()) {
            repeat(5) { DSSkeletonListItem() }
        }
    }
}

@Preview(showBackground = true, name = "Skeleton - Card", widthDp = 360)
@Composable
private fun DSSkeletonCardPreview() {
    DSDesignTheme {
        DSSkeletonCard(modifier = Modifier.padding(16.dp))
    }
}
