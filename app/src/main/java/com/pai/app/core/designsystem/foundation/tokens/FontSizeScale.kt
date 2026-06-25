// ============================================================================
// FontSizeScale.kt
// 字号缩放等级：4 档预设倍率，通过 CompositionLocal 注入
// 影响 DSText 等所有依赖 MaterialTheme.typography 的文本组件
// ============================================================================

package com.pai.app.core.designsystem.foundation.tokens

/**
 * 字号缩放等级
 *
 * 用于全局字号放大 / 缏小，影响所有 DSText 组件。
 * 实际缩放通过 [com.pai.app.core.designsystem.foundation.theme.LocalFontSizeScale]
 * 注入到 Composition 中，DSText 在渲染时将 `style.fontSize` 乘以 [multiplier]。
 *
 * 4 档倍率覆盖大多数无障碍场景：
 * - [Small]：0.85x，紧凑显示
 * - [Normal]：1.0x，默认标准
 * - [Large]：1.15x，轻度放大
 * - [ExtraLarge]：1.3x，重度放大（适合视力辅助）
 */
enum class DSFontSizeScale(val displayName: String, val multiplier: Float) {
    Small("小", 0.85f),
    Normal("标准", 1.0f),
    Large("大", 1.15f),
    ExtraLarge("超大", 1.3f);

    companion object {
        /**
         * 由名称字符串解析为 [DSFontSizeScale]
         *
         * 用于从 DataStore 持久化的字符串还原为枚举，无法识别时回退到 [Normal]。
         *
         * @param name 枚举常量名（如 `"Small"` / `"Normal"`）
         */
        fun fromName(name: String): DSFontSizeScale =
            entries.find { it.name == name } ?: Normal
    }
}
