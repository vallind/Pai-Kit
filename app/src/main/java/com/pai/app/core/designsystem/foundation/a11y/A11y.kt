// ============================================================================
// A11y.kt
// 可访问性 (Accessibility) 工具集
// 提供：
// - 最小点击区域保障（48dp）
// - 语义化描述（用于 TalkBack）
// - 触控目标对齐
// ============================================================================

package com.pai.app.core.designsystem.foundation.a11y

import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layout
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp

/**
 * 最小可点击区域修饰符
 *
 * Material Design 推荐最小点击区域为 48dp × 48dp
 * iOS HIG 推荐最小 44pt × 44pt
 * Android 推荐 48dp × 48dp
 *
 * 此修饰符保证元素视觉尺寸小于 48dp 时，扩大可点击区域至 48dp，
 * 但不改变视觉尺寸。
 *
 * 使用示例：
 * ```
 * Icon(
 *     imageVector = Icons.Default.Add,
 *     contentDescription = "添加",
 *     modifier = Modifier.minTouchTarget()
 * )
 * ```
 */
internal fun Modifier.minTouchTarget(
    minSize: Int = 48
): Modifier = this.then(
    Modifier.layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)

        // 计算扩展后的尺寸（至少 48dp × 48dp）
        val width = maxOf(placeable.width, minSize.dp.toPx().toInt())
        val height = maxOf(placeable.height, minSize.dp.toPx().toInt())

        layout(width, height) {
            // 居中放置原元素
            val centerX = (width - placeable.width) / 2
            val centerY = (height - placeable.height) / 2
            placeable.placeRelative(centerX, centerY)
        }
    }
)

/**
 * 语义化描述修饰符
 * - 为 TalkBack 屏幕阅读器提供描述
 *
 * 使用示例：
 * ```
 * Icon(
 *     imageVector = Icons.Default.Add,
 *     contentDescription = null,
 *     modifier = Modifier.a11yDescription("添加新项目", "按钮")
 * )
 * ```
 */
internal fun Modifier.a11yDescription(
    description: String,
    role: Role? = null
): Modifier = this.then(
    Modifier.semantics {
        contentDescription = description
        role?.let { this.role = it }
    }
)

/**
 * 状态描述修饰符
 * - 描述组件当前状态，TalkBack 会优先朗读
 *
 * 使用示例：
 * ```
 * Switch(
 *     checked = isChecked,
 *     onCheckedChange = { ... },
 *     modifier = Modifier.a11yState(
 *         description = "通知开关",
 *         state = if (isChecked) "已开启" else "已关闭"
 *     )
 * )
 * ```
 */
internal fun Modifier.a11yState(
    description: String,
    state: String
): Modifier = this.then(
    Modifier.semantics {
        contentDescription = description
        stateDescription = state
    }
)

/**
 * 最小尺寸保障修饰符
 * - 强制元素至少 48dp × 48dp，满足 Material Design 最小触控目标要求
 * - 适用于视觉尺寸小于 48dp 的图标/按钮，扩大其触控区域
 *
 * 注意：与 [minTouchTarget] 不同，本修饰符会 *改变视觉尺寸* 至 48dp × 48dp。
 * 若需在不改变视觉尺寸的前提下扩展触控区域，请使用 [minTouchTarget]。
 *
 * 使用示例：
 * ```
 * Icon(
 *     imageVector = Icons.Default.Add,
 *     contentDescription = "添加",
 *     modifier = Modifier.a11yMinSize()
 * )
 * ```
 */
internal fun Modifier.a11yMinSize(): Modifier = this.then(
    Modifier.size(48.dp, 48.dp)
)
