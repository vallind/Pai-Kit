// ============================================================================
// DSGrid.kt
// 网格布局 - 基于 Column + Row 简化实现（便于嵌套滚动）
// 提供 DSL（item / span 跨列）
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSGridScope - 网格 DSL 作用域
 *
 * 在 [DSGrid] 的 content lambda 中通过 [item] 添加网格项，
 * 支持 span 参数控制跨列数。
 *
 * 使用示例：
 * ```kotlin
 * DSGrid(columns = 3) {
 *     item { Card1() }              // 占 1 列
 *     item { Card2() }              // 占 1 列
 *     item(span = 2) { WideCard() } // 跨 2 列
 * }
 * ```
 */
internal class DSGridScope {

    @PublishedApi
    internal val items = mutableListOf<GridItem>()

    /**
     * 添加一个网格项
     *
     * @param span 跨列数（默认 1，最大不超过 [DSGrid] 的 columns）
     * @param content 该项的内容
     */
    fun item(span: Int = 1, content: @Composable () -> Unit) {
        require(span >= 1) { "span 必须大于等于 1" }
        items.add(GridItem(span, content))
    }

    /**
     * 内部使用的网格项数据
     */
    internal class GridItem(val span: Int, val content: @Composable () -> Unit)
}

/**
 * DSGrid - 网格布局
 *
 * 使用示例：
 * ```kotlin
 * DSGrid(columns = 3, spacing = 12.dp) {
 *     item { Card1() }
 *     item { Card2() }
 *     item(span = 2) { WideCard() }  // 跨 2 列
 *     item { Card3() }
 *     item(span = 3) { BannerCard() } // 跨满 3 列
 * }
 * ```
 *
 * 设计规范：
 * - 使用 Column + Row 简化实现，便于嵌套滚动（与 LazyVerticalGrid 不同，
 *   本组件不会自身滚动，可作为 Column / LazyColumn 子项）
 * - 每行的 weight 总和为 columns，保证每列宽度严格相等
 * - 当某行剩余空间不足以放下当前 item 时，自动换行
 * - 单个 item 的 span 超过 columns 时自动截断为 columns
 * - 行未填满时自动在末尾补齐空白 Spacer，保证列宽稳定
 *
 * @param modifier 修饰符
 * @param columns 列数（默认 2）
 * @param spacing 间距（横向与纵向统一）
 * @param content DSL 内容
 */
@Composable
internal fun DSGrid(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    spacing: Dp = DSTokens.Spacing.lg,
    content: @Composable DSGridScope.() -> Unit
) {
    require(columns >= 1) { "columns 必须大于等于 1" }

    // 使用 remember 保持 scope 实例稳定，避免每次重组都新建对象
    val scope = remember { DSGridScope() }
    scope.items.clear()
    scope.content()

    // 使用 derivedStateOf 包裹 buildRows，避免每次重组都重新计算行布局。
    // scope.items 是同一 MutableList 引用，size 或 span 变化时才会触发 buildRows 重算。
    val rows = remember(columns) {
        derivedStateOf { buildRows(scope.items.toList(), columns) }
    }.value

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        rows.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                row.forEach { gridItem ->
                    Box(modifier = Modifier.weight(gridItem.span.toFloat())) {
                        gridItem.content()
                    }
                }
                // 行未填满时补齐空白，保证列宽稳定
                val usedSpan = row.sumOf { it.span }
                if (usedSpan < columns) {
                    Spacer(modifier = Modifier.weight((columns - usedSpan).toFloat()))
                }
            }
        }
    }
}

/**
 * 根据每个 item 的 span 计算行布局
 * - 跨列超过剩余空间则换行
 * - 单 item span 超过 columns 时截断
 */
private fun buildRows(
    items: List<DSGridScope.GridItem>,
    columns: Int
): List<List<DSGridScope.GridItem>> {
    val rows = mutableListOf<MutableList<DSGridScope.GridItem>>()
    var currentRow = mutableListOf<DSGridScope.GridItem>()
    var usedSpan = 0

    for (item in items) {
        val effectiveSpan = item.span.coerceAtMost(columns)
        // 当前行剩余空间不足时换行
        if (usedSpan + effectiveSpan > columns && currentRow.isNotEmpty()) {
            rows.add(currentRow)
            currentRow = mutableListOf()
            usedSpan = 0
        }
        currentRow.add(DSGridScope.GridItem(effectiveSpan, item.content))
        usedSpan += effectiveSpan
    }
    if (currentRow.isNotEmpty()) {
        rows.add(currentRow)
    }
    return rows
}

// ============================================================================
// Preview
// ============================================================================

@Preview(showBackground = true, name = "DSGrid - 2 列")
@Composable
private fun DSGridPreview() {
    DSDesignTheme {
        DSGrid(
            columns = 2,
            spacing = DSTokens.Spacing.md,
            modifier = Modifier.padding(16.dp)
        ) {
            repeat(6) { index ->
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.medium,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "卡片 ${index + 1}",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "DSGrid - 3 列跨列")
@Composable
private fun DSGridSpanPreview() {
    DSDesignTheme {
        DSGrid(
            columns = 3,
            spacing = DSTokens.Spacing.md,
            modifier = Modifier.padding(16.dp)
        ) {
            item { GridPreviewCell("1") }
            item { GridPreviewCell("2") }
            item(span = 2) { GridPreviewCell("3 (span=2)") }
            item { GridPreviewCell("4") }
            item(span = 3) { GridPreviewCell("5 (span=3)") }
        }
    }
}

@Preview(showBackground = true, name = "DSGrid - 4 列")
@Composable
private fun DSGridFourColumnsPreview() {
    DSDesignTheme {
        DSGrid(
            columns = 4,
            spacing = DSTokens.Spacing.sm,
            modifier = Modifier.padding(16.dp)
        ) {
            repeat(8) { index ->
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "${index + 1}",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GridPreviewCell(label: String) {
    Surface(
        color = MaterialTheme.colorScheme.tertiaryContainer,
        shape = MaterialTheme.shapes.medium,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onTertiaryContainer,
            style = MaterialTheme.typography.titleMedium
        )
    }
}
