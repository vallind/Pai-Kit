// ============================================================================
// ElevationTokens.kt
// 高度系统：基于 Material 3 Elevation
// 6 档高度 Token，单位 dp，决定阴影与 z 轴层级
// ============================================================================

package com.pai.app.core.designsystem.foundation.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 高度系统 Token
 *
 * 设计原则：
 * - 0dp：无阴影（平面元素）
 * - 1-3dp：低高度（Card、MenuItem、Snackbar）
 * - 4-6dp：中高度（Dialog、BottomSheet）
 * - 8dp+：高高度（FAB、模态弹窗）
 *
 * 注：在 M3 中，高度更多用于视觉层级表达，阴影被弱化
 */
object DSElevation {

    /** 0dp：无高度 */
    val level0: Dp = 0.dp

    /** 1dp：极低高度（Card 默认、悬浮态） */
    val level1: Dp = 1.dp

    /** 2dp：低高度（Card 悬停、Snackbar） */
    val level2: Dp = 2.dp

    /** 3dp：中低高度（NavigationDrawer、Menu） */
    val level3: Dp = 3.dp

    /** 4dp：中高度（BottomBar、Dialog） */
    val level4: Dp = 4.dp

    /** 6dp：中高高度（BottomSheet、DatePicker） */
    val level6: Dp = 6.dp

    /** 8dp：高高度（FAB） */
    val level8: Dp = 8.dp

    /** 12dp：极高高度（FAB 悬浮态、模态弹窗） */
    val level12: Dp = 12.dp
}
