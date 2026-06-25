// ============================================================================
// DSProgress.kt
// 进度指示器 - 线性 + 圆形
// 基于 M3 LinearProgressIndicator / CircularProgressIndicator 扩展
// ============================================================================

package com.pai.app.core.designsystem.primitives

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSLinearProgress - 线性进度条
 *
 * 使用示例：
 * ```kotlin
 * DSLinearProgress(progress = 0.5f)  // 确定进度
 * DSLinearProgress()                  // 不确定进度（加载动画）
 * ```
 *
 * @param progress 进度（0~1），为 null 表示不确定
 * @param modifier 修饰符
 * @param color 自定义颜色，默认 primary
 */
@Composable
internal fun DSLinearProgress(
    progress: Float? = null,
    modifier: Modifier = Modifier.fillMaxWidth(),
    color: Color = MaterialTheme.colorScheme.primary
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val shapeModifier = modifier
        .height(DSTokens.Border.medium)
        .clip(RoundedCornerShape(DSTokens.Border.thin))

    if (progress != null) {
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = shapeModifier,
            color = color,
            trackColor = trackColor,
            gapSize = DSTokens.Spacing.none
        )
    } else {
        LinearProgressIndicator(
            modifier = shapeModifier,
            color = color,
            trackColor = trackColor
        )
    }
}

/**
 * DSCircularProgress - 圆形进度指示器
 *
 * 使用示例：
 * ```kotlin
 * DSCircularProgress(progress = 0.5f, size = 32.dp)
 * DSCircularProgress()  // 不确定进度
 * ```
 *
 * @param progress 进度（0~1），为 null 表示不确定
 * @param modifier 修饰符
 * @param size 尺寸，默认 32dp
 * @param strokeWidth 描边宽度，默认 3dp
 * @param color 颜色，默认 primary
 */
@Composable
internal fun DSCircularProgress(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    strokeWidth: Dp = 3.dp,
    color: Color = MaterialTheme.colorScheme.primary
) {
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val sizeModifier = modifier.size(size)

    if (progress != null) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = sizeModifier,
            color = color,
            strokeWidth = strokeWidth,
            trackColor = trackColor
        )
    } else {
        CircularProgressIndicator(
            modifier = sizeModifier,
            color = color,
            strokeWidth = strokeWidth,
            trackColor = trackColor
        )
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "Linear Progress")
@Composable
private fun DSLinearProgressPreview() {
    DSDesignTheme {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
        ) {
            DSLinearProgress(progress = 0.7f)
            DSLinearProgress(progress = null)
        }
    }
}

@Preview(showBackground = true, name = "Circular Progress")
@Composable
private fun DSCircularProgressPreview() {
    DSDesignTheme {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            DSCircularProgress(progress = 0.5f, size = 32.dp)
            DSCircularProgress(progress = null, size = 32.dp)
            DSCircularProgress(progress = null, size = 48.dp, strokeWidth = 4.dp)
        }
    }
}
