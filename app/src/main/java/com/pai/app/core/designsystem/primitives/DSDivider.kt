// ============================================================================
// DSDivider.kt
// 分割线组件 - 水平 / 垂直分割线
// 原 DSSpacer 已拆分到 primitives/DSSpacer.kt
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 分割线粗细
 */
internal enum class DSDividerThickness(val dp: Dp) {
    Hairline(DSTokens.Border.hairline),    // 极细分割线
    Thin(DSTokens.Border.thin),            // 标准分割线
    Thick(DSTokens.Border.medium),         // 加粗分割线
    Section(DSTokens.Border.thick)         // 区块分隔
}

/**
 * DSHorizontalDivider - 水平分割线
 *
 * 使用示例：
 * ```kotlin
 * DSHorizontalDivider()
 * DSHorizontalDivider(startIndent = 16.dp)
 * ```
 *
 * @param modifier 修饰符
 * @param thickness 粗细档位，默认 Thin（1dp）
 * @param color 颜色，默认 MaterialTheme.colorScheme.outlineVariant
 * @param startIndent 起始缩进，默认 0dp
 */
@Composable
internal fun DSHorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: DSDividerThickness = DSDividerThickness.Thin,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    startIndent: Dp = 0.dp
) {
    HorizontalDivider(
        modifier = modifier.padding(start = startIndent),
        thickness = thickness.dp,
        color = color
    )
}

/**
 * DSVerticalDivider - 垂直分割线
 *
 * 使用示例：
 * ```kotlin
 * Row {
 *     Text("左")
 *     DSVerticalDivider(modifier = Modifier.height(20.dp).padding(horizontal = 8.dp))
 *     Text("右")
 * }
 * ```
 *
 * @param modifier 修饰符
 * @param thickness 粗细档位，默认 Thin（1dp）
 * @param color 颜色，默认 MaterialTheme.colorScheme.outlineVariant
 */
@Composable
internal fun DSVerticalDivider(
    modifier: Modifier = Modifier,
    thickness: DSDividerThickness = DSDividerThickness.Thin,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    VerticalDivider(
        modifier = modifier,
        thickness = thickness.dp,
        color = color
    )
}

@Preview(showBackground = true)
@Composable
private fun DSDividerPreview() {
    DSDesignTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DSHorizontalDivider()
            DSHorizontalDivider(thickness = DSDividerThickness.Thick)
            DSHorizontalDivider(startIndent = 16.dp)
        }
    }
}
