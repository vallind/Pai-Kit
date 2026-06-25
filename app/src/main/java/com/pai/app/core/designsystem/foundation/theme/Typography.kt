// ============================================================================
// Typography.kt
// Material 3 Typography 实例
// 用 TypographyTokens（TypeScale + RobotoFontFamily）构建
// ============================================================================

package com.pai.app.core.designsystem.foundation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.pai.app.core.designsystem.foundation.tokens.RobotoFontFamily
import com.pai.app.core.designsystem.foundation.tokens.TypeScale

/**
 * Material 3 Typography 实例
 * 行高与字重遵循 M3 设计规范
 */
internal val DSTypography: Typography = Typography(
    // --- Display ---
    displayLarge = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = TypeScale.DISPLAY_LARGE_SIZE.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = TypeScale.DISPLAY_MEDIUM_SIZE.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp
    ),
    displaySmall = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = TypeScale.DISPLAY_SMALL_SIZE.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),

    // --- Headline ---
    headlineLarge = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = TypeScale.HEADLINE_LARGE_SIZE.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = TypeScale.HEADLINE_MEDIUM_SIZE.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = TypeScale.HEADLINE_SMALL_SIZE.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    // --- Title ---
    titleLarge = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = TypeScale.TITLE_LARGE_SIZE.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = TypeScale.TITLE_MEDIUM_SIZE.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = TypeScale.TITLE_SMALL_SIZE.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    // --- Body ---
    bodyLarge = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = TypeScale.BODY_LARGE_SIZE.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = TypeScale.BODY_MEDIUM_SIZE.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = TypeScale.BODY_SMALL_SIZE.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),

    // --- Label ---
    labelLarge = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = TypeScale.LABEL_LARGE_SIZE.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = TypeScale.LABEL_MEDIUM_SIZE.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = RobotoFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = TypeScale.LABEL_SMALL_SIZE.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
