// ============================================================================
// DSPagination.kt
// 分页器 - 上一页 / 页码 / 下一页
// 总页数 > 7 时使用 "..." 省略中间页，当前页使用 primary 背景
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.patterns

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 分页 token：页码或省略号
 */
private sealed class PageToken {
    data class Page(val number: Int) : PageToken()
    data object Ellipsis : PageToken()
}

/**
 * DSPagination - 分页器
 *
 * 使用示例：
 * ```kotlin
 * var page by remember { mutableStateOf(1) }
 *
 * DSPagination(
 *     currentPage = page,
 *     totalPages = 20,
 *     onPageChange = { page = it },
 *     siblingCount = 1
 * )
 * ```
 *
 * 设计规范：
 * - 布局：[上一页] [页码 ...] [下一页]，水平排列
 * - 总页数 <= 7：展示全部页码
 * - 总页数 > 7：始终展示首页与末页，当前页左右各 siblingCount 个，其余用 "..." 省略
 * - 当前页：primary 背景 + onPrimary 文字 + SemiBold 字重，不可重复点击
 * - 非当前页：透明背景 + onSurface 文字
 * - 页码按钮视觉尺寸 40dp（ComponentHeight.buttonMedium），触控区域 48dp（minTouchTarget）
 * - 上一页 / 下一页使用 IconButton（48dp 触控目标），到达边界时禁用
 *
 * @param currentPage 当前页码（1-based）
 * @param totalPages 总页数
 * @param onPageChange 翻页回调，传入目标页码
 * @param modifier 修饰符
 * @param siblingCount 当前页左右各显示的兄弟页数，默认 1
 */
@Composable
internal fun DSPagination(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    siblingCount: Int = 1
) {
    // 每个 page token 的预估宽度（touch target 48dp + 间距 2dp）
    val itemWidth: Dp = DSTokens.minTouchTarget + DSTokens.Spacing.xxs
    // 首尾箭头按钮宽度
    val navWidth: Dp = 48.dp
    // page 之外的最小宽度（prev + next + 2 × spacing）
    val fixedWidth: Dp = navWidth * 2 + DSTokens.Spacing.xxs * 2

    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        // 可容纳的最大 page token 数
        val maxItems = ((maxWidth - fixedWidth) / itemWidth).toInt().coerceAtLeast(3)
        // 预留给首、末、当前各 1 格，剩余平分给左右邻居
        val maxSiblings = ((maxItems - 3) / 2).coerceAtLeast(0)

        val actualSiblingCount = siblingCount.coerceAtMost(maxSiblings)
        val pages = remember(currentPage, totalPages, actualSiblingCount) {
            buildPageList(currentPage, totalPages, actualSiblingCount)
        }

        Row(
            modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally),
            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 上一页
            IconButton(
                onClick = { onPageChange(currentPage - 1) },
                enabled = currentPage > 1
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "上一页"
                )
            }

            // 页码列表
            pages.forEach { token ->
                when (token) {
                    is PageToken.Ellipsis -> {
                        Box(
                            modifier = Modifier.size(DSTokens.minTouchTarget),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is PageToken.Page -> {
                        val isSelected = token.number == currentPage
                        PageButton(
                            number = token.number,
                            isSelected = isSelected,
                            onClick = { onPageChange(token.number) }
                        )
                    }
                }
            }

            // 下一页
            IconButton(
                onClick = { onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "下一页"
                )
            }
        }
    }
}

/**
 * 单个页码按钮
 * - 外层 48dp 触控区域（DSTokens.minTouchTarget + 圆形裁剪 + clickable）
 * - 内层 40dp 视觉圆（背景色），居中显示页码文案
 */
@Composable
private fun PageButton(
    number: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(DSTokens.minTouchTarget)
            .clip(CircleShape)
            .clickable(
                enabled = !isSelected,
                role = Role.Tab,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(DSTokens.ComponentHeight.buttonMedium)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number.toString(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 计算需要展示的页码 token 列表
 * - 总页数 <= 7：展示全部
 * - 总页数 > 7：始终展示首页与末页，当前页左右各 siblingCount 个，其余用省略号
 */
private fun buildPageList(
    currentPage: Int,
    totalPages: Int,
    siblingCount: Int
): List<PageToken> {
    if (totalPages <= 7) {
        return (1..totalPages).map { PageToken.Page(it) }
    }

    val leftSibling = (currentPage - siblingCount).coerceAtLeast(1)
    val rightSibling = (currentPage + siblingCount).coerceAtMost(totalPages)

    val showLeftEllipsis = leftSibling > 2
    val showRightEllipsis = rightSibling < totalPages - 1

    val list = mutableListOf<PageToken>()
    list.add(PageToken.Page(1))
    if (showLeftEllipsis) list.add(PageToken.Ellipsis)
    // 跳过首末页避免重复（首页和末页已单独添加）
    val rangeStart = if (leftSibling <= 1) 2 else leftSibling
    val rangeEnd = if (rightSibling >= totalPages) totalPages - 1 else rightSibling
    for (i in rangeStart..rangeEnd) {
        list.add(PageToken.Page(i))
    }
    if (showRightEllipsis) list.add(PageToken.Ellipsis)
    list.add(PageToken.Page(totalPages))
    return list
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "DSPagination - 少量页（全展示）")
@Composable
private fun DSPaginationFewPreview() {
    DSDesignTheme {
        Column(
            modifier = Modifier.padding(DSTokens.Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
        ) {
            // 5 页，当前第 1 页
            DSPagination(
                currentPage = 1,
                totalPages = 5,
                onPageChange = {}
            )
            // 5 页，当前第 3 页
            DSPagination(
                currentPage = 3,
                totalPages = 5,
                onPageChange = {}
            )
            // 5 页，当前第 5 页（末页）
            DSPagination(
                currentPage = 5,
                totalPages = 5,
                onPageChange = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "DSPagination - 大量页（含省略号）")
@Composable
private fun DSPaginationManyPreview() {
    DSDesignTheme {
        Column(
            modifier = Modifier.padding(DSTokens.Spacing.xl),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
        ) {
            // 20 页，当前第 1 页
            DSPagination(
                currentPage = 1,
                totalPages = 20,
                onPageChange = {}
            )
            // 20 页，当前第 5 页
            DSPagination(
                currentPage = 5,
                totalPages = 20,
                onPageChange = {}
            )
            // 20 页，当前第 10 页（中间）
            DSPagination(
                currentPage = 10,
                totalPages = 20,
                onPageChange = {}
            )
            // 20 页，当前第 20 页（末页）
            DSPagination(
                currentPage = 20,
                totalPages = 20,
                onPageChange = {}
            )
        }
    }
}
