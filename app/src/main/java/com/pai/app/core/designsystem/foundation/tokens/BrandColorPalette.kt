// ============================================================================
// BrandColorPalette.kt
// 品牌色板枚举：5 套预设品牌色，每套提供 50-900 完整色阶
// 由 ColorScheme.kt 动态生成 Light/Dark ColorScheme
// ============================================================================

package com.pai.app.core.designsystem.foundation.tokens

import androidx.compose.ui.graphics.Color

/**
 * 品牌色板枚举
 *
 * 每个品牌色提供 50-900 完整色阶，用于动态生成 ColorScheme。
 * 通过 [palette] 暴露完整色阶，[primary] / [primaryContainer] / [onPrimaryContainer] /
 * [secondary] 为常用色位的语义化快捷访问。
 *
 * 使用方式：
 * ```kotlin
 * val indigo = DSBrandColor.Indigo
 * val primary = indigo.primary         // 600 阶
 * val container = indigo.primaryContainer // 100 阶
 * ```
 */
enum class DSBrandColor(val displayName: String) {
    Indigo("靛蓝"),
    Emerald("翡翠绿"),
    Rose("玫瑰红"),
    Amber("琥珀黄"),
    Sky("天蓝色");

    /** 主色（600 阶） */
    val primary: Color get() = palette[600]!!

    /** 浅色容器（100 阶） */
    val primaryContainer: Color get() = palette[100]!!

    /** 深色容器（800 阶） */
    val onPrimaryContainer: Color get() = palette[800]!!

    /** 辅助色（500 阶） */
    val secondary: Color get() = palette[500]!!

    /** 完整色阶 50-900 */
    val palette: Map<Int, Color> get() = when (this) {
        Indigo -> mapOf(
            50 to BrandIndigo50, 100 to BrandIndigo100, 200 to BrandIndigo200,
            300 to BrandIndigo300, 400 to BrandIndigo400, 500 to BrandIndigo500,
            600 to BrandIndigo600, 700 to BrandIndigo700, 800 to BrandIndigo800, 900 to BrandIndigo900,
        )

        Emerald -> mapOf(
            50 to BrandEmerald50, 100 to BrandEmerald100, 200 to BrandEmerald200,
            300 to BrandEmerald300, 400 to BrandEmerald400, 500 to BrandEmerald500,
            600 to BrandEmerald600, 700 to BrandEmerald700, 800 to BrandEmerald800, 900 to BrandEmerald900,
        )

        Rose -> mapOf(
            50 to BrandRose50, 100 to BrandRose100, 200 to BrandRose200,
            300 to BrandRose300, 400 to BrandRose400, 500 to BrandRose500,
            600 to BrandRose600, 700 to BrandRose700, 800 to BrandRose800, 900 to BrandRose900,
        )

        Amber -> mapOf(
            50 to BrandAmber50, 100 to BrandAmber100, 200 to BrandAmber200,
            300 to BrandAmber300, 400 to BrandAmber400, 500 to BrandAmber500,
            600 to BrandAmber600, 700 to BrandAmber700, 800 to BrandAmber800, 900 to BrandAmber900,
        )

        Sky -> mapOf(
            50 to BrandSky50, 100 to BrandSky100, 200 to BrandSky200,
            300 to BrandSky300, 400 to BrandSky400, 500 to BrandSky500,
            600 to BrandSky600, 700 to BrandSky700, 800 to BrandSky800, 900 to BrandSky900,
        )
    }

    companion object {
        /**
         * 由名称字符串解析为 [DSBrandColor]
         *
         * 用于从 DataStore 持久化的字符串还原为枚举，无法识别时回退到 [Indigo]。
         *
         * @param name 枚举常量名（如 `"Indigo"` / `"Emerald"`）
         */
        fun fromName(name: String): DSBrandColor =
            entries.find { it.name == name } ?: Indigo
    }
}
