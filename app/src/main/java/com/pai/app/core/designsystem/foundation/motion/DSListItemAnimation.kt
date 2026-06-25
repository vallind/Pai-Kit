// ============================================================================
// DSListItemAnimation.kt
// LazyColumn 列表项进场动画工具
// ============================================================================

package com.pai.app.core.designsystem.foundation.motion

import com.pai.app.core.designsystem.foundation.tokens.MSDuration
import com.pai.app.core.designsystem.foundation.tokens.MSEasing

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

/**
 * LazyColumn / LazyRow 列表项进场动画
 *
 * 为列表项添加淡入 + 从下方滑入的进场动画，按 index 错峰延迟。
 *
 * 使用示例：
 * ```kotlin
 * LazyColumn {
 *     itemsIndexed(posts) { index, post ->
 *         PostCard(
 *             post = post,
 *             modifier = Modifier.listItemEnterAnimation(index)
 *         )
 *     }
 * }
 * ```
 *
 * 设计规范：
 * - 首次进入列表时，每项延迟 50ms 错峰进场
 * - 滑动加载新项时不重复触发（仅 index < 20 时触发）
 * - 动画时长 300ms，emphasizedDecelerate 缓动
 *
 * 实现说明：使用 [composed] 而非 `Modifier.Node` API。
 * 这会在每次调用时分配一个 `Animatable` 实例（每个列表项一个），
 * 对于"列表项进场动画"这种生命周期短、仅触发一次的场景，该开销可以接受。
 * 若未来需要应用到极长列表（数百项同时可见），可迁移到 `Modifier.Node`
 * 以获得更好的内存复用。
 *
 * @param index 列表项索引
 * @param delayMillis 错峰延迟基数，默认 50ms × index
 */
internal fun Modifier.listItemEnterAnimation(
    index: Int,
    delayMillis: Int = 50
): Modifier = composed {
    val animatable = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // 仅前 20 项触发进场动画，避免长列表卡顿
        if (index < 20) {
            animatable.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = MSDuration.medium2,
                    delayMillis = index * delayMillis,
                    easing = MSEasing.emphasizedDecelerate
                )
            )
        } else {
            animatable.snapTo(1f)
        }
    }

    this.graphicsLayer {
        alpha = animatable.value
        // 80f 为进场滑入距离（像素），近似一个 list-item 高度的 1/3
        translationY = (1f - animatable.value) * 80f
    }
}

