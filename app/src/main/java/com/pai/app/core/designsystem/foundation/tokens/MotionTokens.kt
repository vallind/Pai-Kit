// ============================================================================
// MotionTokens.kt
// 动效 Token - 时长与缓动曲线（原始定义）
// 由 motion/DSMotionScheme.kt 引用构建常用动画规格
// ============================================================================

package com.pai.app.core.designsystem.foundation.tokens

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing

/**
 * 动效时长 Token（毫秒）
 *
 * 设计原则：
 * - instant：几乎不可见的反馈（涟漪、状态切换）
 * - small：按钮点击、图标切换
 * - medium：列表项进场、卡片展开
 * - long：页面转场、复杂动效
 */
object MSDuration {
    const val instant = 50
    const val small1 = 100
    const val small2 = 150
    const val small3 = 200
    const val medium1 = 250
    const val medium2 = 300
    const val medium3 = 350
    const val medium4 = 400
    const val long1 = 450
    const val long2 = 500
    const val long3 = 700
}

/**
 * 动效缓动曲线 Token
 *
 * 遵循 Material 3 Motion 规范
 */
object MSEasing {
    /** 线性，仅用于进度条 */
    val linear: Easing = Easing { fraction -> fraction }

    /** Emphasized：默认自然减速，最常用 */
    val emphasized: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /** Emphasized Decelerate：元素进场 */
    val emphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)

    /** Emphasized Accelerate：元素退场 */
    val emphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)

    /** Standard：通用过渡 */
    val standard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)

    /** Standard Decelerate：进场 */
    val standardDecelerate: Easing = LinearOutSlowInEasing

    /** Standard Accelerate：退场 */
    val standardAccelerate: Easing = FastOutSlowInEasing
}
