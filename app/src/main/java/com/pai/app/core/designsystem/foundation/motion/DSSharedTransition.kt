// ============================================================================
// DSSharedTransition.kt
// 共享元素转场 - 基于 Compose 1.7+ 的 SharedTransitionLayout
// M3 卓越线补齐：实现列表→详情的 hero element transition
// ============================================================================

package com.pai.app.core.designsystem.foundation.motion

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.BoundsTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.tokens.MSDuration
import com.pai.app.core.designsystem.foundation.tokens.MSEasing
import timber.log.Timber

/**
 * 共享元素转场 CompositionLocal
 *
 * 由 [DSSharedTransitionProvider] 提供当前作用域，所有子组件通过
 * [Modifier.sharedElement] / [Modifier.sharedBounds] 接入转场。
 *
 * 默认值 = null：未在 Provider 包裹时，访问会抛异常，强制调用方正确包裹。
 */
val LocalSharedTransitionScope = staticCompositionLocalOf<SharedTransitionScope?> { null }

/**
 * 共享元素转场内容访问接口
 *
 * 把 SharedTransitionScope + AnimatedVisibilityScope 两个参数打包，
 * 让业务组件不需要直接依赖 Compose 内部 API。
 */
class DSSharedTransitionScope(
    val sharedScope: SharedTransitionScope,
    val animatedVisibilityScope: AnimatedVisibilityScope
)

/**
 * DSSharedTransitionProvider - 共享元素转场宿主
 *
 * 必须包裹在 NavHost（或 AnimatedContent）外层，让所有路由页面共享同一个
 * SharedTransitionScope。
 *
 * 使用示例：
 * ```kotlin
 * DSSharedTransitionProvider {
 *     NavHost(
 *         navController = navController,
 *         startDestination = HomeRoute,
 *         enterTransition = { DSPageTransitions.enterTransition() },
 *         // ...
 *     ) {
 *         composable<HomeRoute> { HomeScreen() }
 *         composable<DetailRoute> { DetailScreen() }
 *     }
 * }
 * ```
 *
 * 在子页面中：
 * ```kotlin
 * // 列表页
 * Image(
 *     painter = ...,
 *     modifier = Modifier
 *         .sharedElement(
 *             rememberSharedContentState(key = "image-${item.id}"),
 *             animatedVisibilityScope = LocalAnimatedVisibilityScope.current
 *         )
 * )
 *
 * // 详情页
 * Image(
 *     painter = ...,
 *     modifier = Modifier
 *         .sharedElement(
 *             rememberSharedContentState(key = "image-${item.id}"),
 *             animatedVisibilityScope = LocalAnimatedVisibilityScope.current
 *         )
 * )
 * ```
 *
 * @param content 子内容
 */
@Composable
fun DSSharedTransitionProvider(
    content: @Composable (SharedTransitionScope, Modifier) -> Unit
) {
    SharedTransitionScope(
        content = content
    )
}

/**
 * 默认 BoundsTransform - 遵循 M3 emphasized 缓动曲线
 *
 * 用于 [Modifier.sharedBounds] 的 boundsTransform 参数。
 * 控制 shared element 从源位置到目标位置的过渡曲线。
 */
val DSDefaultBoundsTransform: BoundsTransform = { _: Rect, _: Rect ->
    tween(
        durationMillis = MSDuration.medium4,
        easing = MSEasing.emphasized
    )
}

/**
 * 共享元素进退场动画（用于 sharedBounds 的 enter/exit 参数）
 *
 * - enter: 淡入 + 轻微缩放（从 0.85 到 1.0）
 * - exit: 淡出 + 轻微缩放（从 1.0 到 1.15）
 */
val DSSharedEnterTransition: EnterTransition
    get() = fadeIn(
        animationSpec = tween(MSDuration.medium3, easing = MSEasing.emphasizedDecelerate)
    ) + scaleIn(
        animationSpec = tween(MSDuration.medium3, easing = MSEasing.emphasizedDecelerate),
        initialScale = 0.85f
    )

val DSSharedExitTransition: ExitTransition
    get() = fadeOut(
        animationSpec = tween(MSDuration.medium3, easing = MSEasing.emphasizedAccelerate)
    ) + scaleOut(
        animationSpec = tween(MSDuration.medium3, easing = MSEasing.emphasizedAccelerate),
        targetScale = 1.15f
    )

/**
 * 共享元素 Resize 行为 - 控制非共享内容如何随共享元素改变
 *
 * - RemeasureToBounds: 立即按目标 bounds 重新测量（默认推荐）
 * - Scale: 缩放过渡（保留视觉尺寸连续性）
 */
val DSDefaultContentScale: ContentScale = ContentScale.Crop

/**
 * Modifier 扩展：默认参数的 sharedElement 便捷调用
 *
 * 等价于：
 * ```kotlin
 * Modifier.sharedElement(
 *     state = rememberSharedContentState(key = key),
 *     animatedVisibilityScope = animatedVisibilityScope,
 *     boundsTransform = DSDefaultBoundsTransform,
 *     enter = DSSharedEnterTransition,
 *     exit = DSSharedExitTransition
 * )
 * ```
 *
 * @param key 共享元素 key，源页面和目标页面使用相同 key 才能配对
 * @param sharedScope 当前 SharedTransitionScope（来自 [LocalSharedTransitionScope]）
 * @param animatedVisibilityScope 当前 AnimatedVisibilityScope（来自 NavHost 的 composable 块）
 * @param modifier 链上的其他修饰符
 */
@Composable
fun Modifier.dsSharedElement(
    key: String,
    sharedScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier = Modifier
): Modifier {
    if (sharedScope == null || animatedVisibilityScope == null) {
        // Debug 警告：调用方未包裹 DSSharedTransitionProvider
        // 生产环境无开销（Timber 在 release 自动 no-op）
        Timber.tag("DS-Motion").w(
            "dsSharedElement(key=$key) called outside DSSharedTransitionProvider — no-op. " +
                "Wrap your NavHost with DSSharedTransitionProvider { ... }."
        )
        return modifier
    }
    return with(sharedScope) {
        modifier.sharedElement(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = DSDefaultBoundsTransform
        )
    }
}

/**
 * Modifier 扩展：默认参数的 sharedBounds 便捷调用
 *
 * 与 sharedElement 区别：
 * - sharedElement: 共享 *视觉内容*（如同一个图片），适合图像
 * - sharedBounds: 共享 *容器边界*，但内容不同（如列表项标题 vs 详情页大标题）
 */
@Composable
fun Modifier.dsSharedBounds(
    key: String,
    sharedScope: SharedTransitionScope?,
    animatedVisibilityScope: AnimatedVisibilityScope?,
    modifier: Modifier = Modifier
): Modifier {
    if (sharedScope == null || animatedVisibilityScope == null) {
        Timber.tag("DS-Motion").w(
            "dsSharedBounds(key=$key) called outside DSSharedTransitionProvider — no-op. " +
                "Wrap your NavHost with DSSharedTransitionProvider { ... }."
        )
        return modifier
    }
    return with(sharedScope) {
        modifier.sharedBounds(
            sharedContentState = rememberSharedContentState(key = key),
            animatedVisibilityScope = animatedVisibilityScope,
            boundsTransform = DSDefaultBoundsTransform,
            enter = DSSharedEnterTransition,
            exit = DSSharedExitTransition,
        )
    }
}
