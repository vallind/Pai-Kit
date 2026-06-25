// ============================================================================
// DSAnimatedPressScale.kt
// 按压缩放修饰符 - 为按钮/卡片等可点击元素提供统一的按压反馈动效
// ============================================================================

package com.pai.app.core.designsystem.foundation.motion

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.CancellationException

/**
 * 按压缩放修饰符
 *
 * 元素被按下时缩小到 [currentDSMotionScheme] 的 pressScale.pressed，
 * 松开后回弹到 pressScale.released。
 * 适用于按钮、卡片、图标按钮等需要按压反馈的可点击元素。
 *
 * 实现说明：
 * 使用 [pointerInput] 在 [awaitEachGesture] 中观察 down/up/cancel，
 * 通过 [PressInteraction] 发射给内部 [MutableInteractionSource]，再驱动
 * [animateFloatAsState] 缩放。**关键：不消费 pointer 事件**（不调用
 * `down.consume()` / `up.consume()`），因此可以与调用方已有的
 * `Modifier.clickable { ... }` 共存，不会拦截 onClick。
 *
 * `awaitFirstDown(requireUnchanged = false)` 默认不消费事件；
 * `awaitUpOrCancellation()` 也仅在显式调用 `change.consume()` 时才消费。
 * 因此调用方的 `clickable` / `Modifier.clickable` 仍能正常收到点击事件。
 *
 * 使用示例：
 * ```kotlin
 * DSButton(
 *     text = "提交",
 *     onClick = { /* ... */ },
 *     modifier = Modifier.pressScale()
 * )
 *
 * DSCard(
 *     onClick = { /* ... */ },
 *     modifier = Modifier.pressScale()
 * )
 * ```
 *
 * 设计规范：
 * - 缩放比例：0.95（按下时缩小 5%）
 * - 动画时长：150ms（DSPressScale.durationMillis，从 MotionScheme 读取）
 * - 缓动曲线：emphasized（M3 规范）
 *
 * @param pressedScale 按下时的缩放比例，默认 0.95
 */
internal fun Modifier.pressScale(
    pressedScale: Float? = null
): Modifier = composed {
    val motion = currentDSMotionScheme()
    val ps = motion.pressScale
    val scalePressed = pressedScale ?: ps.pressed
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scalePressed else ps.released,
        animationSpec = tween(
            durationMillis = ps.durationMillis,
            easing = ps.easing
        ),
        label = "pressScale"
    )

    this
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = { offset ->
                    val press = PressInteraction.Press(offset)
                    interactionSource.tryEmit(press)
                    try {
                        awaitRelease()
                        interactionSource.tryEmit(PressInteraction.Release(press))
                    } catch (_: CancellationException) {
                        interactionSource.tryEmit(PressInteraction.Cancel(press))
                    }
                }
            )
        }
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}

/**
 * 带自定义 InteractionSource 的按压缩放
 *
 * 当调用方已有 InteractionSource 时使用此版本，避免重复创建。
 *
 * @param interactionSource 调用方提供的交互源
 * @param pressedScale 按下时的缩放比例
 */
@Composable
internal fun Modifier.pressScaleWith(
    interactionSource: MutableInteractionSource,
    pressedScale: Float? = null
): Modifier {
    val motion = currentDSMotionScheme()
    val ps = motion.pressScale
    val scalePressed = pressedScale ?: ps.pressed
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) scalePressed else ps.released,
        animationSpec = tween(
            durationMillis = ps.durationMillis,
            easing = ps.easing
        ),
        label = "pressScaleWith"
    )

    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
}
