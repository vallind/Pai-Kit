// ============================================================================
// DSText.kt
// 文本组件 - 基于 M3 Text 扩展
// 提供 4 种语义变体：Display / Title / Body / Label
// 统一封装颜色、字号、字重，避免业务代码硬编码
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.theme.LocalFontSizeScale
import com.pai.app.core.designsystem.foundation.theme.extendedColors

/**
 * 文本语义变体
 * - Display：超大展示文字（数字、空状态）
 * - Headline：页面主标题
 * - Title：模块/卡片标题
 * - Body：正文
 * - Label：按钮、Tag、Caption
 */
internal enum class DSTextVariant {
    DisplayLarge,
    DisplayMedium,
    DisplaySmall,

    HeadlineLarge,
    HeadlineMedium,
    HeadlineSmall,

    TitleLarge,
    TitleMedium,
    TitleSmall,

    BodyLarge,
    BodyMedium,
    BodySmall,

    LabelLarge,
    LabelMedium,
    LabelSmall
}

/**
 * 文本颜色语义
 * - Primary：主文字色（高对比度）
 * - Secondary：辅助文字色（中对比度）
 * - Tertiary：占位文字色（低对比度）
 * - OnPrimary：背景为 primary 时的文字色
 * - OnSurface：与 surface 同色调（默认）
 * - Error：错误色
 * - Success：成功色
 * - Warning：警告色
 * - Info：信息色
 * - Custom：自定义颜色
 */
internal enum class DSTextColor {
    Primary,
    Secondary,
    Tertiary,
    OnPrimary,
    OnSurface,
    Error,
    Success,
    Warning,
    Info,
    Custom
}

/**
 * DSText - 统一文本组件
 *
 * 使用示例：
 * ```kotlin
 * DSText(
 *     text = "用户名",
 *     variant = DSTextVariant.TitleMedium,
 *     color = DSTextColor.Primary
 * )
 * ```
 *
 * 设计规范：
 * - 默认颜色 Primary（onSurface）
 * - 默认溢出 Ellipsis（...）
 * - 默认最大行数 null（不限制）
 * - 自动跟随主题色
 *
 * @param text 文本内容
 * @param variant 文本变体，默认 BodyMedium
 * @param color 颜色语义，默认 OnSurface
 * @param modifier 修饰符
 * @param maxLines 最大行数
 * @param overflow 溢出处理
 * @param textAlign 对齐方式
 * @param fontWeight 字重（可选，覆盖默认）
 * @param textDecoration 文本装饰（下划线/删除线）
 * @param customColor 自定义颜色（color = Custom 时生效）
 * @param customStyle 自定义 TextStyle（非 null 时完全覆盖 variant 提供的 style，
 *        仍会应用 fontSizeScale 和 fontWeight 覆盖）
 */
@Composable
internal fun DSText(
    text: String,
    modifier: Modifier = Modifier,
    variant: DSTextVariant = DSTextVariant.BodyMedium,
    color: DSTextColor = DSTextColor.OnSurface,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
    textDecoration: TextDecoration? = null,
    customColor: Color = Color.Unspecified,
    customStyle: TextStyle? = null
) {
    val style = customStyle ?: when (variant) {
        DSTextVariant.DisplayLarge -> MaterialTheme.typography.displayLarge
        DSTextVariant.DisplayMedium -> MaterialTheme.typography.displayMedium
        DSTextVariant.DisplaySmall -> MaterialTheme.typography.displaySmall
        DSTextVariant.HeadlineLarge -> MaterialTheme.typography.headlineLarge
        DSTextVariant.HeadlineMedium -> MaterialTheme.typography.headlineMedium
        DSTextVariant.HeadlineSmall -> MaterialTheme.typography.headlineSmall
        DSTextVariant.TitleLarge -> MaterialTheme.typography.titleLarge
        DSTextVariant.TitleMedium -> MaterialTheme.typography.titleMedium
        DSTextVariant.TitleSmall -> MaterialTheme.typography.titleSmall
        DSTextVariant.BodyLarge -> MaterialTheme.typography.bodyLarge
        DSTextVariant.BodyMedium -> MaterialTheme.typography.bodyMedium
        DSTextVariant.BodySmall -> MaterialTheme.typography.bodySmall
        DSTextVariant.LabelLarge -> MaterialTheme.typography.labelLarge
        DSTextVariant.LabelMedium -> MaterialTheme.typography.labelMedium
        DSTextVariant.LabelSmall -> MaterialTheme.typography.labelSmall
    }

    val resolvedColor = when (color) {
        DSTextColor.Primary -> MaterialTheme.colorScheme.onSurface
        DSTextColor.Secondary -> MaterialTheme.colorScheme.onSurfaceVariant
        DSTextColor.Tertiary -> MaterialTheme.colorScheme.outline
        DSTextColor.OnPrimary -> MaterialTheme.colorScheme.onPrimary
        DSTextColor.OnSurface -> MaterialTheme.colorScheme.onSurface
        DSTextColor.Error -> MaterialTheme.colorScheme.error
        DSTextColor.Success -> MaterialTheme.extendedColors.success
        DSTextColor.Warning -> MaterialTheme.extendedColors.warning
        DSTextColor.Info -> MaterialTheme.extendedColors.info
        DSTextColor.Custom -> customColor
    }

    // 读取全局字号缩放（由 DSDesignTheme 通过 LocalFontSizeScale 注入），
    // 对 style.fontSize 应用 multiplier 后再叠加可选的 fontWeight 覆盖。
    val fontSizeScale = LocalFontSizeScale.current
    val scaledStyle = style.copy(
        fontSize = style.fontSize * fontSizeScale.multiplier,
        fontWeight = fontWeight ?: style.fontWeight,
    )

    Text(
        text = text,
        modifier = modifier,
        color = resolvedColor,
        style = scaledStyle,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines,
        textDecoration = textDecoration
    )
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "Text - All Variants")
@Composable
private fun DSTextAllVariantsPreview() {
    DSDesignTheme {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
        ) {
            DSText("Display Large", variant = DSTextVariant.DisplayLarge)
            DSText("Display Medium", variant = DSTextVariant.DisplayMedium)
            DSText("Display Small", variant = DSTextVariant.DisplaySmall)
            DSText("Headline Large", variant = DSTextVariant.HeadlineLarge)
            DSText("Headline Medium", variant = DSTextVariant.HeadlineMedium)
            DSText("Headline Small", variant = DSTextVariant.HeadlineSmall)
            DSText("Title Large", variant = DSTextVariant.TitleLarge)
            DSText("Title Medium", variant = DSTextVariant.TitleMedium)
            DSText("Title Small", variant = DSTextVariant.TitleSmall)
            DSText("Body Large", variant = DSTextVariant.BodyLarge)
            DSText("Body Medium", variant = DSTextVariant.BodyMedium)
            DSText("Body Small", variant = DSTextVariant.BodySmall)
            DSText("Label Large", variant = DSTextVariant.LabelLarge)
            DSText("Label Medium", variant = DSTextVariant.LabelMedium)
            DSText("Label Small", variant = DSTextVariant.LabelSmall)
        }
    }
}

@Preview(showBackground = true, name = "Text - Colors")
@Composable
private fun DSTextColorsPreview() {
    DSDesignTheme {
        androidx.compose.foundation.layout.Column(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(4.dp)
        ) {
            DSText("Primary 颜色", color = DSTextColor.Primary)
            DSText("Secondary 颜色", color = DSTextColor.Secondary)
            DSText("Tertiary 颜色", color = DSTextColor.Tertiary)
            DSText("Error 颜色", color = DSTextColor.Error)
            DSText("Success 颜色", color = DSTextColor.Success)
            DSText("Warning 颜色", color = DSTextColor.Warning)
            DSText("Info 颜色", color = DSTextColor.Info)
        }
    }
}
