// ============================================================================
// SpacingTokens.kt
// 间距系统：基于 4dp 基准栅格
// 提供 8 档间距 Token，从 0 到 80dp，覆盖从微间距到模块间距
// ============================================================================

package com.pai.app.core.designsystem.foundation.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 间距系统 Token
 *
 * 设计原则：
 * - 基准 4dp，所有间距为 4 的倍数
 * - 命名采用语义化前缀（xxs/xs/sm/md/lg/xl/xxl/xxxl），便于阅读
 *
 * 使用示例：
 * ```
 * Spacer(modifier = Modifier.height(DSSpacing.md))
 * Column(modifier = Modifier.padding(all = DSSpacing.lg))
 * ```
 */
object DSSpacing {

    /** 0dp：无间距 */
    val none: Dp = 0.dp

    /** 2dp：极小间距（图标与文字内部间距） */
    val xxs: Dp = 2.dp

    /** 4dp：小间距（图标内部、Tag 间距） */
    val xs: Dp = 4.dp

    /** 8dp：默认紧凑间距（组件内元素间距） */
    val sm: Dp = 8.dp

    /** 12dp：中间距（组件内分组间距） */
    val md: Dp = 12.dp

    /** 16dp：默认间距（卡片内边距、列表项间距） */
    val lg: Dp = 16.dp

    /** 24dp：大间距（卡片之间、模块内部） */
    val xl: Dp = 24.dp

    /** 32dp：超大间距（模块之间、页面分组） */
    val xxl: Dp = 32.dp

    /** 48dp：超大间距（页面顶部留白、重要分隔） */
    val xxxl: Dp = 48.dp

    /** 64dp：超大间距（页面级分隔） */
    val huge: Dp = 64.dp

    /** 80dp：超大间距（首屏顶部、空状态） */
    val giant: Dp = 80.dp
}
