// ============================================================================
// DSAccordion.kt
// 折叠面板 - 基于 M3 Card + AnimatedVisibility 实现
// 单选模式：同时只允许展开一个 section
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 折叠面板 Section 数据类
 *
 * @param title 标题文本
 * @param content 展开后显示的内容（Composable）
 */
internal data class DSAccordionSection(
    val title: String,
    val content: @Composable () -> Unit
)

/**
 * DSAccordion - 折叠面板
 *
 * 使用示例：
 * ```kotlin
 * val sections = listOf(
 *     DSAccordionSection(title = "个人信息") {
 *         Column {
 *             Text("姓名：张三")
 *             Text("邮箱：zhangsan@example.com")
 *         }
 *     },
 *     DSAccordionSection(title = "订单记录") {
 *         Text("订单号：12345")
 *     }
 * )
 * DSAccordion(
 *     sections = sections,
 *     initiallyExpandedIndex = 0
 * ) { index, isExpanded ->
 *     Log.d("Accordion", "section $index expanded=$isExpanded")
 * }
 * ```
 *
 * 设计规范：
 * - 单选模式：同一时间只展开一个 section，再次点击已展开项可收起
 * - 使用 Material3 Card 包裹，圆角 large (16dp)，elevation level1
 * - 标题高度 ≥ 48dp（垂直内边距 16dp），保证最小触控目标
 * - 展开箭头使用 KeyboardArrowDown，展开时旋转 180°
 * - 展开/收起动画使用 emphasized 缓动，时长 medium2 (250ms)
 * - 展开区域使用 surfaceVariant 半透明背景，与标题区分
 *
 * @param modifier 修饰符
 * @param sections section 列表
 * @param initiallyExpandedIndex 初始展开的 index，null 表示全部收起
 * @param onExpandChange 展开/收起状态变化回调
 *   - index: 当前展开的 section 下标，全部收起时为 null
 *   - isExpanded: 当前是否有 section 处于展开状态
 */
@Composable
internal fun DSAccordion(
    modifier: Modifier = Modifier,
    sections: List<DSAccordionSection>,
    initiallyExpandedIndex: Int? = null,
    onExpandChange: ((index: Int?, isExpanded: Boolean) -> Unit)? = null
) {
    var expandedIndex by remember {
        mutableIntStateOf(initiallyExpandedIndex ?: -1)
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
    ) {
        sections.forEachIndexed { index, section ->
            val isExpanded = expandedIndex == index
            AccordionItem(
                section = section,
                isExpanded = isExpanded,
                onClick = {
                    val newIndex = if (isExpanded) -1 else index
                    expandedIndex = newIndex
                    onExpandChange?.invoke(
                        newIndex.takeIf { it >= 0 },
                        newIndex >= 0
                    )
                }
            )
        }
    }
}

/**
 * 单个折叠项的内部实现
 */
@Composable
private fun AccordionItem(
    section: DSAccordionSection,
    isExpanded: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(DSTokens.Radius.large),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = DSTokens.Elevation.level1
        )
    ) {
        Column {
            // ----------------------------------------------------------------
            // 标题行
            // ----------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        role = Role.Button,
                        onClick = onClick
                    )
                    .padding(
                        horizontal = DSTokens.Spacing.lg,
                        vertical = DSTokens.Spacing.md
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = section.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )

                // 箭头旋转动画
                val rotation by animateFloatAsState(
                    targetValue = if (isExpanded) 180f else 0f,
                    animationSpec = tween(
                        durationMillis = DSTokens.Duration.medium2,
                        easing = DSTokens.Easing.emphasized
                    ),
                    label = "arrowRotation"
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(DSTokens.IconSize.md)
                        .graphicsLayer { rotationZ = rotation }
                )
            }

            // ----------------------------------------------------------------
            // 可展开内容区
            // ----------------------------------------------------------------
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = tween(
                        durationMillis = DSTokens.Duration.medium2,
                        easing = DSTokens.Easing.emphasized
                    )
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = DSTokens.Duration.medium2,
                        easing = DSTokens.Easing.standard
                    )
                ),
                exit = shrinkVertically(
                    animationSpec = tween(
                        durationMillis = DSTokens.Duration.medium2,
                        easing = DSTokens.Easing.emphasizedAccelerate
                    )
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = DSTokens.Duration.medium2,
                        easing = DSTokens.Easing.standardAccelerate
                    )
                )
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    shape = RoundedCornerShape(
                        bottomStart = DSTokens.Radius.large,
                        bottomEnd = DSTokens.Radius.large
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = DSTokens.Spacing.lg,
                                vertical = DSTokens.Spacing.md
                            )
                    ) {
                        section.content()
                    }
                }
            }
        }
    }
}

// ============================================================================
// Preview
// ============================================================================

@Preview(showBackground = true, name = "DSAccordion - 默认展开第一项")
@Composable
private fun DSAccordionPreview() {
    DSDesignTheme {
        val sections = listOf(
            DSAccordionSection(title = "个人信息") {
                Column {
                    Text(
                        text = "姓名：张三",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "邮箱：zhangsan@example.com",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "手机：13800138000",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            DSAccordionSection(title = "订单记录") {
                Text(
                    text = "订单号：12345 / 总价：¥199.00",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            DSAccordionSection(title = "售后服务") {
                Text(
                    text = "7 天无理由退换货 / 30 天质保",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        )
        DSAccordion(
            sections = sections,
            initiallyExpandedIndex = 0,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(showBackground = true, name = "DSAccordion - 全部收起")
@Composable
private fun DSAccordionCollapsedPreview() {
    DSDesignTheme {
        DSAccordion(
            sections = listOf(
                DSAccordionSection(title = "标题一") {
                    Text("内容一")
                },
                DSAccordionSection(title = "标题二") {
                    Text("内容二")
                },
                DSAccordionSection(title = "标题三") {
                    Text("内容三")
                }
            ),
            modifier = Modifier.padding(16.dp)
        )
    }
}
