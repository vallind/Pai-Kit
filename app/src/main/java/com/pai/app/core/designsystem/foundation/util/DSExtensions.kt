// ============================================================================
// DSExtensions.kt
// 通用扩展函数 — 调试/绘制/动画场景常用工具
// 不属于具体组件，但被组件内部和业务侧广泛使用
// ============================================================================

package com.pai.app.core.designsystem.foundation.util

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp

// ============================================================================
// Color 扩展
// ============================================================================

/**
 * 转换为 #RRGGBBAA 或 #RRGGBB 十六进制字符串
 *
 * 使用场景：
 * - 调试日志（打印颜色值）
 * - 颜色持久化（DataStore / SharedPreferences）
 * - 与设计稿工具对接（如 Figma hex 值）
 *
 * @param includeAlpha 是否包含 alpha 通道，默认 true
 * @return 形如 "#FF4F46E5" 或 "#4F46E5"
 *
 * 示例：
 * ```kotlin
 * val c = Color(0xFF4F46E5)
 * c.toHex()                  // "#FF4F46E5"
 * c.toHex(includeAlpha = false)  // "#4F46E5"
 * ```
 */
fun Color.toHex(includeAlpha: Boolean = true): String {
    val a = (alpha * 255).toInt()
    val r = (red * 255).toInt()
    val g = (green * 255).toInt()
    val b = (blue * 255).toInt()
    return if (includeAlpha) {
        String.format("#%02X%02X%02X%02X", a, r, g, b)
    } else {
        String.format("#%02X%02X%02X", r, g, b)
    }
}

/**
 * 调整颜色透明度（不修改原 Color，返回新 Color）
 *
 * 与 `Color.copy(alpha = alpha)` 等价，但更简洁。
 *
 * 示例：
 * ```kotlin
 * val overlayColor = MaterialTheme.colorScheme.primary.withAlpha(0.4f)
 * // 等价于
 * // val overlayColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
 * ```
 */
fun Color.withAlpha(alpha: Float): Color = this.copy(alpha = alpha.coerceIn(0f, 1f))

/**
 * 颜色按比例混合（线性插值）
 *
 * @param target 目标颜色
 * @param fraction 插值比例 [0, 1]，0 = 当前色，1 = 目标色
 *
 * 示例：
 * ```kotlin
 * val midColor = Color.Red.blend(Color.Blue, 0.5f)  // 紫色
 * ```
 */
fun Color.blend(target: Color, fraction: Float): Color {
    val f = fraction.coerceIn(0f, 1f)
    return Color(
        red = red + (target.red - red) * f,
        green = green + (target.green - green) * f,
        blue = blue + (target.blue - blue) * f,
        alpha = alpha + (target.alpha - alpha) * f
    )
}

/**
 * 判断颜色是否为深色（用于自动选择文字颜色）
 *
 * 使用 WCAG 相对亮度公式，亮度 < 0.5 视为深色。
 *
 * 示例：
 * ```kotlin
 * val textColor = if (backgroundColor.isDark()) Color.White else Color.Black
 * ```
 */
fun Color.isDark(): Boolean {
    // WCAG 相对亮度
    fun linearize(c: Float): Float = if (c <= 0.03928f) c / 12.92f else Math.pow(((c + 0.055) / 1.055).toDouble(), 2.4).toFloat()
    val r = linearize(red)
    val g = linearize(green)
    val b = linearize(blue)
    val luminance = 0.2126f * r + 0.7152f * g + 0.0722f * b
    return luminance < 0.5f
}

/**
 * 计算与另一颜色的对比度比（WCAG）
 *
 * 返回值范围 [1, 21]，1 = 无对比，21 = 最大对比（黑白）。
 * - 文字与背景对比度 ≥ 4.5 → WCAG AA
 * - 文字与背景对比度 ≥ 7.0 → WCAG AAA
 *
 * 示例：
 * ```kotlin
 * val ratio = textColor.contrastRatio(backgroundColor)
 * if (ratio < 4.5f) Timber.w("对比度不足，不满足 WCAG AA")
 * ```
 */
fun Color.contrastRatio(other: Color): Float {
    fun relativeLuminance(c: Color): Float {
        fun linearize(v: Float): Float = if (v <= 0.03928f) v / 12.92f else Math.pow(((v + 0.055) / 1.055).toDouble(), 2.4).toFloat()
        return 0.2126f * linearize(c.red) + 0.7152f * linearize(c.green) + 0.0722f * linearize(c.blue)
    }
    val l1 = relativeLuminance(this)
    val l2 = relativeLuminance(other)
    val lighter = maxOf(l1, l2)
    val darker = minOf(l1, l2)
    return (lighter + 0.05f) / (darker + 0.05f)
}

// ============================================================================
// Dp 扩展
// ============================================================================

/**
 * Dp 转 Px（安全版本，处理 density 为 null 的情况）
 *
 * 与 `with(density) { dp.toPx() }` 等价，但更简洁。
 *
 * 示例：
 * ```kotlin
 * val density = LocalDensity.current
 * val widthPx = 16.dp.toPxSafe(density)
 * ```
 */
fun Dp.toPxSafe(density: Density): Float = with(density) { this@toPxSafe.toPx() }

/**
 * Dp 转 Int Px（向下取整）
 *
 * 适用于需要 Int 像素值的场景（如 Canvas.drawRect）。
 *
 * 示例：
 * ```kotlin
 * val widthPx = 16.dp.toPxInt(density)
 * canvas.drawRect(Rect(0f, 0f, widthPx.toFloat(), heightPx.toFloat()), paint)
 * ```
 */
fun Dp.toPxInt(density: Density): Int = toPxSafe(density).toInt()

// ============================================================================
// Size 扩展
// ============================================================================

/**
 * Size 转 Dp（依赖 Density）
 *
 * 示例：
 * ```kotlin
 * val sizeDp = canvasSize.toDpSize(density)
 * ```
 */
fun Size.toDpSize(density: Density): androidx.compose.ui.unit.DpSize = with(density) {
    androidx.compose.ui.unit.DpSize(width.toDp(), height.toDp())
}
