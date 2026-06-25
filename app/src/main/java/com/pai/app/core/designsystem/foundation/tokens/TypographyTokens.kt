// ============================================================================
// TypographyTokens.kt
// 字号阶 Token（M3 spec 数值常量）
// 由 theme/Typography.kt 引用构建 Material 3 Typography
// ============================================================================

package com.pai.app.core.designsystem.foundation.tokens

import androidx.compose.ui.text.font.FontFamily

// ---------------------------------------------------------------------------
// 字体家族
// Android 系统默认即 Roboto，无需额外下载字体
// 通过 FontFamily.Default 引用，可保证编译不依赖任何外部资源
// ---------------------------------------------------------------------------

val RobotoFontFamily: FontFamily = FontFamily.Default

// ---------------------------------------------------------------------------
// 字号阶常量（M3 spec）
// 单位 sp，供组件直接引用
// ---------------------------------------------------------------------------

object TypeScale {
    // Display：超大展示文字，用于空状态、欢迎页大标题
    const val DISPLAY_LARGE_SIZE = 57
    const val DISPLAY_MEDIUM_SIZE = 45
    const val DISPLAY_SMALL_SIZE = 36

    // Headline：页面主标题
    const val HEADLINE_LARGE_SIZE = 32
    const val HEADLINE_MEDIUM_SIZE = 28
    const val HEADLINE_SMALL_SIZE = 24

    // Title：模块标题、卡片标题
    const val TITLE_LARGE_SIZE = 22
    const val TITLE_MEDIUM_SIZE = 16
    const val TITLE_SMALL_SIZE = 14

    // Body：正文
    const val BODY_LARGE_SIZE = 16
    const val BODY_MEDIUM_SIZE = 14
    const val BODY_SMALL_SIZE = 12

    // Label：按钮、Tag、Caption
    const val LABEL_LARGE_SIZE = 14
    const val LABEL_MEDIUM_SIZE = 12
    const val LABEL_SMALL_SIZE = 11
}
