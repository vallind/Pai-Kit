// ============================================================================
// RadiusTokens.kt
// 圆角 Token（基础数值，单位 dp）
// 由 theme/Shapes.kt 引用构建 Material 3 Shapes
// ============================================================================

package com.pai.app.core.designsystem.foundation.tokens

/**
 * 圆角刻度 Token
 *
 * 5 档圆角 + 完整圆形：
 * - ExtraSmall：4dp（小标签、Chip 内部）
 * - Small：8dp（默认圆角）
 * - Medium：12dp（卡片）
 * - Large：16dp（大卡片、BottomSheet）
 * - ExtraLarge：28dp（FAB、对话框）
 * - Full：999（胶囊形 / 圆形）
 */
object ShapeScale {
    const val EXTRA_SMALL = 4
    const val SMALL = 8
    const val MEDIUM = 12
    const val LARGE = 16
    const val EXTRA_LARGE = 28
    const val FULL = 999  // 完全圆形 / 胶囊形
}
