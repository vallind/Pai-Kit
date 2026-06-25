// ============================================================================
// DSStateLayer.kt
// 状态层 Modifier - M3 标准 state layer (hover/focus/pressed)
// M3 卓越线补齐：组件交互范式完整覆盖
// 用法：任何可点击组件都可以套用本 Modifier 获得 M3 标准的状态反馈层
// ============================================================================

package com.pai.app.core.designsystem.foundation.a11y

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.unit.dp

/**
 * M3 状态层透明度
 *
 * 4 种状态对应的颜色叠加透明度，遵循 M3 设计规范：
 * - Hover: 8% (0.08)
 * - Focus: 12% (0.12)
 * - Pressed: 12% (0.12)
 * - Dragged: 16% (0.16)
 */
object DSStateLayerAlpha {
    const val Hover = 0.08f
    const val Focus = 0.12f
    const val Pressed = 0.12f
    const val Dragged = 0.16f
}

/**
 * Modifier 状态层叠加
 *
 * 在组件上叠加一层透明色作为状态指示，遵循 M3 规范。
 * 自动响应 hover / focus / pressed 三种交互状态。
 *
 * 使用示例：
 * ```kotlin
 * Surface(
 *     modifier = Modifier
 *         .size(48.dp)
 *         .dsStateLayer(
 *             color = MaterialTheme.colorScheme.primary,
 *             interactionSource = remember { MutableInteractionSource() }
 *         )
 *         .clickable { /* ... */ }
 * ) {
 *     Icon(Icons.Default.Add, contentDescription = "Add")
 * }
 * ```
 *
 * 设计规范：
 * - 颜色取自组件的 contentColor 或自定义
 * - Hover 时叠加 8% contentColor
 * - Focus 时叠加 12% contentColor
 * - Pressed 时叠加 12% contentColor
 * - 多状态叠加时取最大透明度（不累加）
 *
 * @param color 状态层颜色（通常 = 组件内容色）
 * @param interactionSource 交互源
 * @param pressedDelay 按压反馈延迟（毫秒），默认 0
 */
fun Modifier.dsStateLayer(
    color: Color,
    interactionSource: MutableInteractionSource,
    pressedDelay: Int = 0
): Modifier = composed {
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    val isPressed by interactionSource.collectIsPressedAsState()

    val stateLayerAlpha = when {
        isPressed -> DSStateLayerAlpha.Pressed
        isFocused -> DSStateLayerAlpha.Focus
        isHovered -> DSStateLayerAlpha.Hover
        else -> 0f
    }

    if (color.isSpecified && stateLayerAlpha > 0f) {
        this.drawWithContent {
            drawContent()
            drawRect(
                color = color,
                alpha = stateLayerAlpha
            )
        }
    } else {
        this
    }
}

/**
 * 简化的状态层 Modifier
 *
 * 等价于 [dsStateLayer] + 内部 remember 一个 MutableInteractionSource，
 * 适用于不需要复用 interactionSource 的简单场景。
 *
 * 使用示例：
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .size(48.dp)
 *         .dsStateLayer(MaterialTheme.colorScheme.primary)
 *         .clickable { /* ... */ }
 * )
 * ```
 *
 * ⚠️ 注意：本 Modifier 不创建交互源，仅作绘制叠加。
 * 实际 hover/focus/pressed 触发需要配合 clickable 等可交互 Modifier，
 * 且应使用相同的 interactionSource。
 *
 * @param color 状态层颜色
 */
fun Modifier.dsStateLayerSimple(
    color: Color,
    isHovered: Boolean = false,
    isFocused: Boolean = false,
    isPressed: Boolean = false
): Modifier = composed {
    val alpha = when {
        isPressed -> DSStateLayerAlpha.Pressed
        isFocused -> DSStateLayerAlpha.Focus
        isHovered -> DSStateLayerAlpha.Hover
        else -> 0f
    }
    if (color.isSpecified && alpha > 0f) {
        this.drawWithContent {
            drawContent()
            drawRect(color = color, alpha = alpha)
        }
    } else {
        this
    }
}

/**
 * M3 Ripple 配置
 *
 * 用于 clickable / combinedClickable 等可交互组件，提供 M3 标准 ripple 效果。
 * Compose Material3 1.2+ 推荐使用 `ripple()` 替代 `rememberRipple()`。
 *
 * 使用示例：
 * ```kotlin
 * val interactionSource = remember { MutableInteractionSource() }
 * Box(
 *     modifier = Modifier
 *         .clickable(
 *             interactionSource = interactionSource,
 *             indication = ripple(color = MaterialTheme.colorScheme.primary),
 *             onClick = { /* ... */ }
 *         )
 * )
 * ```
 *
 * @param color ripple 颜色（通常 = 组件内容色）
 */
@Composable
fun dsRipple(
    color: Color = Color.Unspecified
): androidx.compose.foundation.Indication {
    return if (color.isSpecified) {
        ripple(color = color)
    } else {
        ripple()
    }
}
