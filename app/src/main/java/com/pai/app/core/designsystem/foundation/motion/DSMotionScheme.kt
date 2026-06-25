// ============================================================================
// DSMotionScheme.kt
// 完整 Motion Token 体系（M3 Motion Tokens 全集）
// 将 MSDuration + MSEasing + spring + page transitions 整合为可注入的 MotionScheme
// 类似 ColorScheme/Typography，支持运行时切换动效方案（如减少动效模式）
// ============================================================================

package com.pai.app.core.designsystem.foundation.motion

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.tokens.MSDuration
import com.pai.app.core.designsystem.foundation.tokens.MSEasing

/**
 * MotionScheme - 动效方案
 *
 * 类似 [androidx.compose.material3.ColorScheme] 和 [Typography]，但用于动效规格。
 * 可被主题注入，支持运行时切换动效方案（如减少动效模式）。
 *
 * 包含：
 * - [duration] 时长集合
 * - [easing] 缓动曲线集合
 * - [enter] / [exit] 进退场动画工厂
 * - [fadeContentTransform] / [slideContentTransform] / [scaleContentTransform] 内容转换器
 * - [DSSpring] spring 物理动画工厂
 * - [pageTransitions] NavHost 转场规格
 * - [pressScale] 按压反馈参数
 *
 * 使用示例：
 * ```kotlin
 * val motion = currentDSMotionScheme()
 * AnimatedVisibility(
 *     visible = isVisible,
 *     enter = motion.enter().fadeSlideUp(),
 *     exit = motion.exit().fadeSlideDown()
 * )
 * ```
 */
@Stable
class DSMotionScheme(
    val duration: DSMotionDuration = DSMotionDuration(),
    val easing: DSMotionEasing = DSMotionEasing(),
    val pressScale: DSPressScale = DSPressScale(),
    val pageTransitions: DSPageTransitions = DSPageTransitions()
) {
    /** 进场动画工厂 */
    fun enter(): DSEnterMotion = DSEnterMotion(duration, easing)

    /** 退场动画工厂 */
    fun exit(): DSExitMotion = DSExitMotion(duration, easing)

    /** Fade 进退场组合（用于 Tab 切换、Overlay 显隐） */
    fun fadeContentTransform(): ContentTransform = fadeIn(
        animationSpec = tween(duration.medium2, easing = easing.standard)
    ) togetherWith fadeOut(
        animationSpec = tween(duration.medium2, easing = easing.standard)
    )

    /** Slide 进退场组合（用于详情页进退） */
    fun slideContentTransform(): ContentTransform =
        enter().fadeSlideLeft() togetherWith exit().fadeSlideRight()

    /** Scale 进退场组合（用于 Dialog / BottomSheet） */
    fun scaleContentTransform(): ContentTransform =
        enter().fadeScale() togetherWith exit().fadeScale()
}

/**
 * Motion 时长集合
 */
@Stable
class DSMotionDuration(
    val instant: Int = MSDuration.instant,
    val small1: Int = MSDuration.small1,
    val small2: Int = MSDuration.small2,
    val small3: Int = MSDuration.small3,
    val medium1: Int = MSDuration.medium1,
    val medium2: Int = MSDuration.medium2,
    val medium3: Int = MSDuration.medium3,
    val medium4: Int = MSDuration.medium4,
    val long1: Int = MSDuration.long1,
    val long2: Int = MSDuration.long2,
    val long3: Int = MSDuration.long3
)

/**
 * Motion 缓动曲线集合
 */
@Stable
class DSMotionEasing(
    val emphasized: androidx.compose.animation.core.Easing = MSEasing.emphasized,
    val emphasizedDecelerate: androidx.compose.animation.core.Easing = MSEasing.emphasizedDecelerate,
    val emphasizedAccelerate: androidx.compose.animation.core.Easing = MSEasing.emphasizedAccelerate,
    val standard: androidx.compose.animation.core.Easing = MSEasing.standard,
    val standardDecelerate: androidx.compose.animation.core.Easing = MSEasing.standardDecelerate,
    val standardAccelerate: androidx.compose.animation.core.Easing = MSEasing.standardAccelerate,
    val linear: androidx.compose.animation.core.Easing = MSEasing.linear
)

/**
 * 进场动画集合
 */
class DSEnterMotion(
    private val duration: DSMotionDuration,
    private val easing: DSMotionEasing
) {
    /** 淡入 + 上滑（列表项进场） */
    fun fadeSlideUp(d: Int = duration.medium2) = fadeIn(tween(d, easing = easing.emphasizedDecelerate)) +
        slideInVertically(tween(d, easing = easing.emphasizedDecelerate), initialOffsetY = { it / 4 })

    /** 淡入 + 左滑（详情页进场） */
    fun fadeSlideLeft(d: Int = duration.medium3) = fadeIn(tween(d, easing = easing.emphasizedDecelerate)) +
        slideInHorizontally(tween(d, easing = easing.emphasizedDecelerate), initialOffsetX = { it / 3 })

    /** 淡入 + 缩放（对话框进场） */
    fun fadeScale(d: Int = duration.medium2) = fadeIn(tween(d, easing = easing.emphasizedDecelerate)) +
        scaleIn(tween(d, easing = easing.emphasizedDecelerate), initialScale = 0.85f)

    /** 仅淡入 */
    fun fade(d: Int = duration.small3) = fadeIn(tween(d, easing = easing.standard))

    /** 从底部滑入（BottomSheet 进场） */
    fun slideUp(d: Int = duration.medium3) =
        slideInVertically(tween(d, easing = easing.emphasizedDecelerate), initialOffsetY = { it })

    /** 缩放进场（FAB 展开） */
    fun scale(d: Int = duration.medium2) =
        scaleIn(tween(d, easing = easing.emphasizedDecelerate), initialScale = 0.5f)
}

/**
 * 退场动画集合
 */
class DSExitMotion(
    private val duration: DSMotionDuration,
    private val easing: DSMotionEasing
) {
    /** 淡出 + 下滑 */
    fun fadeSlideDown(d: Int = duration.medium2) = fadeOut(tween(d, easing = easing.emphasizedAccelerate)) +
        slideOutVertically(tween(d, easing = easing.emphasizedAccelerate), targetOffsetY = { it / 4 })

    /** 淡出 + 右滑 */
    fun fadeSlideRight(d: Int = duration.medium3) = fadeOut(tween(d, easing = easing.emphasizedAccelerate)) +
        slideOutHorizontally(tween(d, easing = easing.emphasizedAccelerate), targetOffsetX = { it / 3 })

    /** 淡出 + 缩放 */
    fun fadeScale(d: Int = duration.medium2) = fadeOut(tween(d, easing = easing.emphasizedAccelerate)) +
        scaleOut(tween(d, easing = easing.emphasizedAccelerate), targetScale = 0.85f)

    /** 仅淡出 */
    fun fade(d: Int = duration.small3) = fadeOut(tween(d, easing = easing.standard))

    /** 向底部滑出 */
    fun slideDown(d: Int = duration.medium3) =
        slideOutVertically(tween(d, easing = easing.emphasizedAccelerate), targetOffsetY = { it })

    /** 缩放退场 */
    fun scale(d: Int = duration.medium2) =
        scaleOut(tween(d, easing = easing.emphasizedAccelerate), targetScale = 0.5f)
}

/**
 * Spring 物理动画工厂
 *
 * M3 推荐的 spring 动画参数（用于按压反馈、SharedElement 转场）。
 */
object DSSpring {
    /** 中等弹性，按压回弹用 */
    fun <T> medium() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /** 低弹性，平滑过渡 */
    fun <T> low() = spring<T>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )

    /** 无弹性，自然过渡 */
    fun <T> noBouncy() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )

    /** 高弹性，弹簧效果（玩味场景） */
    fun <T> high() = spring<T>(
        dampingRatio = Spring.DampingRatioHighBouncy,
        stiffness = Spring.StiffnessMedium
    )
}

/**
 * 按压反馈参数
 *
 * 用于按钮、卡片等可点击元素的按压缩放反馈。
 */
@Stable
class DSPressScale(
    val pressed: Float = 0.95f,
    val released: Float = 1.0f,
    val durationMillis: Int = MSDuration.small2,
    val easing: androidx.compose.animation.core.Easing = MSEasing.emphasized
)

/**
 * NavHost 页面转场规格
 *
 * 使用示例：
 * ```kotlin
 * NavHost(
 *     navController = navController,
 *     startDestination = HomeRoute,
 *     enterTransition = { currentDSMotionScheme().pageTransitions.enterTransition() },
 *     exitTransition = { currentDSMotionScheme().pageTransitions.exitTransition() },
 *     popEnterTransition = { currentDSMotionScheme().pageTransitions.popEnterTransition() },
 *     popExitTransition = { currentDSMotionScheme().pageTransitions.popExitTransition() }
 * ) { ... }
 */
@Stable
class DSPageTransitions(
    private val duration: DSMotionDuration = DSMotionDuration(),
    private val easing: DSMotionEasing = DSMotionEasing(),
    val slideHorizontalOffset: Dp = 48.dp,
    val slideVerticalOffset: Dp = 48.dp
) {
    /** 进入新页面（前向导航） */
    fun enterTransition(): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(duration.medium3, easing = easing.emphasizedDecelerate)
        ) + fadeIn(tween(duration.medium3, easing = easing.emphasizedDecelerate))
    }

    /** 离开当前页面（前向导航） */
    fun exitTransition(): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Left,
            animationSpec = tween(duration.medium3, easing = easing.emphasizedAccelerate)
        ) + fadeOut(tween(duration.medium3, easing = easing.emphasizedAccelerate))
    }

    /** 返回上一页（后向导航，新页面进入） */
    fun popEnterTransition(): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        slideIntoContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(duration.medium3, easing = easing.emphasizedDecelerate)
        ) + fadeIn(tween(duration.medium3, easing = easing.emphasizedDecelerate))
    }

    /** 返回上一页（后向导航，当前页面退出） */
    fun popExitTransition(): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        slideOutOfContainer(
            towards = AnimatedContentTransitionScope.SlideDirection.Right,
            animationSpec = tween(duration.medium3, easing = easing.emphasizedAccelerate)
        ) + fadeOut(tween(duration.medium3, easing = easing.emphasizedAccelerate))
    }

    /** 淡入淡出转场（轻量级，用于标签切换） */
    fun fadeEnterTransition(): AnimatedContentTransitionScope<*>.() -> EnterTransition = {
        fadeIn(tween(duration.medium2, easing = easing.standard))
    }

    /** 淡出转场 */
    fun fadeExitTransition(): AnimatedContentTransitionScope<*>.() -> ExitTransition = {
        fadeOut(tween(duration.medium2, easing = easing.standard))
    }
}

/**
 * 减少动效方案（无障碍）
 *
 * 用于"减少动效"系统设置开启时，所有动画时长 = 0。
 * 仍然保留可见性切换，但去除过渡动画。
 */
fun reducedMotionScheme(): DSMotionScheme = DSMotionScheme(
    duration = DSMotionDuration(
        instant = 0, small1 = 0, small2 = 0, small3 = 0,
        medium1 = 0, medium2 = 0, medium3 = 0, medium4 = 0,
        long1 = 0, long2 = 0, long3 = 0
    ),
    easing = DSMotionEasing()
)

/**
 * MotionScheme CompositionLocal
 *
 * 默认 = 标准 M3 Motion。
 * 由 [provideDSMotionScheme] 注入，子组件通过 [currentDSMotionScheme] 读取。
 */
val LocalDSMotionScheme = compositionLocalOf { DSMotionScheme() }

/**
 * 提供 MotionScheme 给子树
 *
 * 使用示例：
 * ```kotlin
 * // 检测系统"减少动效"设置
 * val reduceMotion = LocalAccessibilityManager.current.isReduceMotionEnabled
 * provideDSMotionScheme(
 *     scheme = if (reduceMotion) reducedMotionScheme() else DSMotionScheme()
 * ) {
 *     AppContent()
 * }
 * ```
 */
@Composable
fun provideDSMotionScheme(
    scheme: DSMotionScheme,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalDSMotionScheme provides scheme) {
        content()
    }
}

/**
 * 读取当前 MotionScheme
 */
@Composable
@ReadOnlyComposable
fun currentDSMotionScheme(): DSMotionScheme = LocalDSMotionScheme.current
