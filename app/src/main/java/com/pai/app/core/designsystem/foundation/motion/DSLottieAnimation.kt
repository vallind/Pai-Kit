// ============================================================================
// DSLottieAnimation.kt
// Lottie 动画封装组件
// 支持从 assets 加载 .json 动画文件
// ============================================================================

package com.pai.app.core.designsystem.foundation.motion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme

/**
 * Lottie 动画组件
 *
 * 从 `app/src/main/assets/lottie/` 目录加载 `.json` 动画文件。
 *
 * 使用示例：
 * ```kotlin
 * DSLottieAnimation(
 *     assetName = "lottie/loading.json",
 *     size = 120.dp,
 *     iterations = LottieConstants.IterateForever
 * )
 * ```
 *
 * 设计规范：
 * - 默认尺寸 80dp（适合作为加载指示器）
 * - 默认无限循环
 * - 加载失败时静默（Lottie 内部处理）
 *
 * @param assetName assets 内的相对路径，如 "lottie/loading.json"
 * @param modifier 修饰符
 * @param size 动画尺寸（宽高相等），默认 80dp
 * @param iterations 循环次数，默认 [LottieConstants.IterateForever] 无限循环
 * @param speed 播放速度，默认 1.0
 * @param contentAlignment 内容对齐方式
 */
@Composable
internal fun DSLottieAnimation(
    assetName: String,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    iterations: Int = LottieConstants.IterateForever,
    speed: Float = 1.0f,
    contentAlignment: Alignment = Alignment.Center
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.Asset(assetName)
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        speed = speed
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = contentAlignment
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress }
        )
    }
}

/**
 * Lottie 加载状态组件
 *
 * 专为加载场景设计的 Lottie 包装，居中显示无限循环动画。
 *
 * 使用示例：
 * ```kotlin
 * if (isLoading) {
 *     DSLottieLoading(assetName = "lottie/loading.json")
 * }
 * ```
 *
 * @param assetName Lottie 资源路径
 * @param size 动画尺寸，默认 120dp
 * @param modifier 修饰符
 */
@Composable
internal fun DSLottieLoading(
    assetName: String = "lottie/loading.json",
    size: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        DSLottieAnimation(
            assetName = assetName,
            size = size,
            iterations = LottieConstants.IterateForever
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DSLottieAnimationPreview() {
    DSDesignTheme {
        // 注：Preview 中 Lottie 可能无法加载，仅展示组件结构
        DSLottieAnimation(
            assetName = "lottie/loading.json",
            size = 80.dp
        )
    }
}
