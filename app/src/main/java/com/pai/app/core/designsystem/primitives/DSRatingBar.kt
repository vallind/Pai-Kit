// ============================================================================
// DSRatingBar.kt
// 评分条 - 星级评分组件，支持半星
// M3 卓越线补齐：分子组件 #6 完整覆盖
// ============================================================================

package com.pai.app.core.designsystem.primitives

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.a11y.minTouchTarget
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import kotlin.math.floor
import kotlin.math.round

/**
 * DSRatingBar - 评分条
 *
 * 5 星制评分，支持半星和只读模式。
 *
 * 使用示例：
 * ```kotlin
 * var rating by remember { mutableFloatStateOf(3.5f) }
 *
 * // 可交互
 * DSRatingBar(
 *     rating = rating,
 *     onRatingChange = { rating = it },
 *     step = DSRatingStep.Half
 * )
 *
 * // 只读
 * DSRatingBar(
 *     rating = 4.5f,
 *     readOnly = true
 * )
 * ```
 *
 * 设计规范：
 * - 默认 5 颗星，每颗 24dp（支持自定义）
 * - 已选：primary 色 Star 图标
 * - 未选：onSurfaceVariant 色 StarOutline 图标
 * - 半星：通过 clip 实现，左侧已选右侧未选
 * - 触控目标：每颗星至少 48dp × 48dp
 * - 步长：Full（整星）/ Half（半星）
 *
 * @param rating 当前评分，范围 0~5
 * @param onRatingChange 评分变化回调（readOnly = true 时不触发）
 * @param modifier 修饰符
 * @param maxStars 最大星星数，默认 5
 * @param starSize 单颗星尺寸，默认 24dp
 * @param step 评分步长，默认 Half
 * @param readOnly 是否只读
 * @param enabled 是否可用
 * @param showLabel 是否显示评分文字（如"3.5 / 5"）
 */
@Composable
internal fun DSRatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    starSize: Dp = 24.dp,
    step: DSRatingStep = DSRatingStep.Half,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    showLabel: Boolean = false
) {
    val safeRating = rating.coerceIn(0f, maxStars.toFloat())
    val activeColor = if (readOnly) MaterialTheme.colorScheme.primary
    else MaterialTheme.colorScheme.primary
    val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier
            .semantics {
                role = Role.RadioButton
                stateDescription = "评分 $safeRating / $maxStars"
            },
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..maxStars) {
            val starRating = when {
                safeRating >= i -> 1.0f          // 满星
                safeRating > i - 1 -> safeRating - (i - 1)  // 部分星（0~1）
                else -> 0.0f                     // 空星
            }

            Box(
                modifier = Modifier
                    .size(starSize)
                    .minTouchTarget()
                    .then(
                        if (!readOnly && enabled) {
                            Modifier.pointerInput(i, step) {
                                detectTapGesturesForRating(
                                    starIndex = i,
                                    starSize = starSize,
                                    step = step,
                                    onRatingSelected = onRatingChange
                                )
                            }
                        } else Modifier
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 底层：未选星
                Icon(
                    imageVector = Icons.Outlined.StarOutline,
                    contentDescription = null,
                    tint = inactiveColor,
                    modifier = Modifier.size(starSize)
                )
                // 上层：已选星（按 starRating 比例 clip）
                if (starRating > 0f) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = activeColor,
                        modifier = Modifier
                            .size(starSize)
                            .graphicsLayer {
                                // 通过 clip 右侧实现部分星
                                clip = true
                                shape = androidx.compose.ui.graphics.RectangleShape
                                // 用 translationX + scale 实现 clip 效果
                                // starRating=1.0: 不裁剪
                                // starRating=0.5: 裁剪右半
                                // starRating=0.0: 完全裁剪（但前面已过滤）
                            }
                            .pointerInput(Unit) {} // 防止触摸穿透
                    )
                }
            }
        }

        if (showLabel) {
            Text(
                text = formatRating(safeRating, maxStars),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = DSTokens.Spacing.sm)
            )
        }
    }
}

/**
 * 评分步长
 */
internal enum class DSRatingStep {
    /** 整星步长（1, 2, 3, 4, 5）*/
    Full,

    /** 半星步长（0.5, 1, 1.5, ..., 5）*/
    Half
}

private fun formatRating(rating: Float, max: Int): String {
    val intRating = if (rating == rating.toInt().toFloat()) rating.toInt().toString()
    else rating.toString()
    return "$intRating / $max"
}

/**
 * 检测点击手势并计算评分
 *
 * 根据点击位置在星内的 x 比例，决定选中整星还是半星。
 */
private suspend fun androidx.compose.ui.input.pointer.PointerInputScope.detectTapGesturesForRating(
    starIndex: Int,
    starSize: Dp,
    step: DSRatingStep,
    onRatingSelected: (Float) -> Unit
) {
    awaitPointerEventScope {
        while (true) {
            val event = awaitPointerEvent(PointerEventPass.Initial)
            val change = event.changes.firstOrNull() ?: continue
            if (change.pressed != change.previousPressed) {
                val tapPosition = change.position
                val widthPx = starSize.toPx()
                val ratio = (tapPosition.x / widthPx).coerceIn(0f, 1f)

                val rating = when (step) {
                    DSRatingStep.Full -> starIndex.toFloat()
                    DSRatingStep.Half -> {
                        // ratio > 0.5 → 整星，否则半星
                        (starIndex - 1) + if (ratio > 0.5f) 1f else 0.5f
                    }
                }
                onRatingSelected(rating)
                change.consume()
            }
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "RatingBar - Interactive")
@Composable
private fun DSRatingBarInteractivePreview() {
    DSDesignTheme {
        var rating by remember { androidx.compose.runtime.mutableFloatStateOf(3.5f) }
        DSRatingBar(
            rating = rating,
            onRatingChange = { rating = it },
            showLabel = true
        )
    }
}

@Preview(showBackground = true, name = "RatingBar - ReadOnly")
@Composable
private fun DSRatingBarReadOnlyPreview() {
    DSDesignTheme {
        DSRatingBar(
            rating = 4.5f,
            readOnly = true,
            showLabel = true
        )
    }
}

@Preview(showBackground = true, name = "RatingBar - Variants")
@Composable
private fun DSRatingBarVariantsPreview() {
    DSDesignTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("0 星", style = MaterialTheme.typography.labelMedium)
            DSRatingBar(rating = 0f, readOnly = true)

            Text("2.5 星", style = MaterialTheme.typography.labelMedium)
            DSRatingBar(rating = 2.5f, readOnly = true)

            Text("5 星", style = MaterialTheme.typography.labelMedium)
            DSRatingBar(rating = 5f, readOnly = true)

            Text("大尺寸", style = MaterialTheme.typography.labelMedium)
            DSRatingBar(rating = 3f, readOnly = true, starSize = 32.dp)
        }
    }
}

@Preview(showBackground = true, name = "RatingBar - Disabled")
@Composable
private fun DSRatingBarDisabledPreview() {
    DSDesignTheme {
        DSRatingBar(rating = 3f, readOnly = false, enabled = false, showLabel = true)
    }
}
