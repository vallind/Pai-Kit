// ============================================================================
// ComponentTokens.kt
// 组件级 Token：图标尺寸 / 组件高度 / 最小触控目标
// 由各组件直接引用，避免硬编码 dp 值
// ============================================================================

package com.pai.app.core.designsystem.foundation.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 最小可点击区域
 * - Material 推荐最小 48dp
 * - iOS 推荐最小 44pt
 * - 我们采用 48dp
 */
val minTouchTarget: Dp = 48.dp

/**
 * 常用图标尺寸
 */
object IconSize {
    val xs: Dp = 16.dp   // 状态图标
    val sm: Dp = 20.dp   // 按钮内图标
    val md: Dp = 24.dp   // 默认图标
    val lg: Dp = 32.dp   // 大图标
    val xl: Dp = 48.dp   // 空状态图标
}

/**
 * 组件高度
 */
object ComponentHeight {
    val buttonSmall: Dp = 32.dp
    val buttonMedium: Dp = 40.dp
    val buttonLarge: Dp = 48.dp
    val textField: Dp = 56.dp
    val textAreaMin: Dp = 120.dp      // 多行输入框最小高度
    val listItem: Dp = 56.dp
    val listItemCompact: Dp = 48.dp
    val topBar: Dp = 64.dp
    val bottomBar: Dp = 80.dp
    val pickerMaxWidth: Dp = 360.dp   // 日期/时间选择器最大宽度
    val tabIndicator: Dp = 3.dp      // Tab 选中指示器厚度
    val avatarSmall: Dp = 32.dp
    val avatarMedium: Dp = 40.dp
    val avatarLarge: Dp = 56.dp
    val avatarXLarge: Dp = 72.dp
}

/**
 * 描边宽度 Token
 */
object Border {
    val hairline: Dp = 0.5.dp
    val thin: Dp = 1.dp
    val medium: Dp = 2.dp
    val thick: Dp = 8.dp
}

/**
 * 透明度 Token
 */
object Alpha {
    /** 遮罩/蒙层背景透明度 */
    const val overlay: Float = 0.12f
    /** 禁用态容器透明度 */
    const val disabledContainer: Float = 0.12f
    /** 禁用态内容透明度 */
    const val disabledContent: Float = 0.38f
    /** 加载遮罩背景透明度 */
    const val loadingScrim: Float = 0.4f
}
