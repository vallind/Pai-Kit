// ============================================================================
// Shapes.kt
// Material 3 Shapes 实例 + 常用快捷 Shape
// 用 RadiusTokens（ShapeScale）构建
// ============================================================================

package com.pai.app.core.designsystem.foundation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.tokens.ShapeScale

/**
 * Material 3 Shapes 实例
 */
internal val DSShapes: Shapes = Shapes(
    extraSmall = RoundedCornerShape(ShapeScale.EXTRA_SMALL.dp),
    small = RoundedCornerShape(ShapeScale.SMALL.dp),
    medium = RoundedCornerShape(ShapeScale.MEDIUM.dp),
    large = RoundedCornerShape(ShapeScale.LARGE.dp),
    extraLarge = RoundedCornerShape(ShapeScale.EXTRA_LARGE.dp)
)

// ---------------------------------------------------------------------------
// 常用 Shape 快捷构造（业务直接引用）
// 命名统一为 DSShape 前缀，与其他 DS 资产保持一致
// ---------------------------------------------------------------------------

/** 圆形 / 胶囊形（如 Chip、Avatar、FAB） */
internal val DSShapePill = RoundedCornerShape(ShapeScale.FULL.dp)

/** 顶部圆角（BottomSheet、Card 顶部） */
internal val DSShapeTopMedium = RoundedCornerShape(
    topStart = ShapeScale.MEDIUM.dp,
    topEnd = ShapeScale.MEDIUM.dp
)

/** 顶部大圆角（BottomSheet 弹出） */
internal val DSShapeTopExtraLarge = RoundedCornerShape(
    topStart = ShapeScale.EXTRA_LARGE.dp,
    topEnd = ShapeScale.EXTRA_LARGE.dp
)
