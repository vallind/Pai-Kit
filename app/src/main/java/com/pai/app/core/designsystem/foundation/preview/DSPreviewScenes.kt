// ============================================================================
// DSPreviewScenes.kt
// Preview 统一工具 — 强制规范：每个组件至少 Light / Dark / 多品牌 3 个 Preview 场景
//
// 使用方式：
//   @Preview(showBackground = true, name = "Button - Light")
//   @Composable
//   private fun ButtonLightPreview() {
//       DSPreviewScenes.Light { DSButton("Submit", onClick = {}) }
//   }
//
//   @Preview(showBackground = true, name = "Button - Dark")
//   @Composable
//   private fun ButtonDarkPreview() {
//       DSPreviewScenes.Dark { DSButton("Submit", onClick = {}) }
//   }
//
//   @Preview(showBackground = true, name = "Button - Emerald Brand")
//   @Composable
//   private fun ButtonEmeraldPreview() {
//       DSPreviewScenes.Brand(DSBrandColor.Emerald) { DSButton("Submit", onClick = {}) }
//   }
//
// 强制规范：所有 DS 组件 Preview 必须用本工具包装，禁止裸 @Preview 不带主题
// 旧组件逐步迁移到本规范
// ============================================================================

package com.pai.app.core.designsystem.foundation.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSBrandColor
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * DSPreviewScenes - Preview 统一工具
 *
 * 强制规范要求：每个 DS 组件至少 3 个 Preview 场景
 * - [Light]：亮色模式 + 默认品牌
 * - [Dark]：暗色模式 + 默认品牌
 * - [Brand]：亮色 + 指定品牌（覆盖 5 套品牌色）
 *
 * 高级场景：
 * - [Amoled]：AMOLED 纯黑深色
 * - [HighContrast]：高对比度无障碍
 * - [AllBrands]：一次渲染 5 套品牌对比（跨品牌视觉回归）
 * - [DynamicColor]：动态配色（仅 Android 12+）
 */
object DSPreviewScenes {

    /**
     * 亮色场景（默认 Indigo 品牌）
     *
     * @param brand 品牌色板，默认 Indigo
     * @param content Preview 内容
     */
    @Composable
    fun Light(
        brand: DSBrandColor = DSBrandColor.Indigo,
        content: @Composable () -> Unit
    ) {
        DSDesignTheme(
            darkTheme = false,
            dynamicColor = false,
            brandColor = brand
        ) {
            Surface(modifier = Modifier.padding(DSTokens.Spacing.lg)) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }

    /**
     * 暗色场景（默认 Indigo 品牌）
     */
    @Composable
    fun Dark(
        brand: DSBrandColor = DSBrandColor.Indigo,
        content: @Composable () -> Unit
    ) {
        DSDesignTheme(
            darkTheme = true,
            dynamicColor = false,
            brandColor = brand
        ) {
            Surface(modifier = Modifier.padding(DSTokens.Spacing.lg)) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }

    /**
     * 多品牌场景（亮色 + 指定品牌）
     */
    @Composable
    fun Brand(
        brand: DSBrandColor,
        content: @Composable () -> Unit
    ) {
        DSDesignTheme(
            darkTheme = false,
            dynamicColor = false,
            brandColor = brand
        ) {
            Surface(modifier = Modifier.padding(DSTokens.Spacing.lg)) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }

    /**
     * AMOLED 纯黑场景（暗色 + 纯黑背景）
     */
    @Composable
    fun Amoled(content: @Composable () -> Unit) {
        DSDesignTheme(
            darkTheme = true,
            dynamicColor = false,
            amoled = true
        ) {
            Surface(modifier = Modifier.padding(DSTokens.Spacing.lg)) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }

    /**
     * 高对比度场景（无障碍 WCAG AAA）
     */
    @Composable
    fun HighContrast(
        darkTheme: Boolean = false,
        content: @Composable () -> Unit
    ) {
        DSDesignTheme(
            darkTheme = darkTheme,
            dynamicColor = false,
            highContrast = true
        ) {
            Surface(modifier = Modifier.padding(DSTokens.Spacing.lg)) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }

    /**
     * 动态配色场景（仅 Android 12+ 生效，Preview 中模拟）
     */
    @Composable
    fun DynamicColor(
        darkTheme: Boolean = false,
        content: @Composable () -> Unit
    ) {
        DSDesignTheme(
            darkTheme = darkTheme,
            dynamicColor = true
        ) {
            Surface(modifier = Modifier.padding(DSTokens.Spacing.lg)) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        }
    }

    /**
     * 一次渲染 5 套品牌对比（跨品牌视觉回归）
     *
     * 用于验证组件在所有品牌色板下的视觉一致性。
     */
    @Composable
    fun AllBrands(content: @Composable () -> Unit) {
        Column {
            Brand(DSBrandColor.Indigo) { content() }
            Spacer(modifier = Modifier.height(DSTokens.Spacing.sm))
            Brand(DSBrandColor.Emerald) { content() }
            Spacer(modifier = Modifier.height(DSTokens.Spacing.sm))
            Brand(DSBrandColor.Rose) { content() }
            Spacer(modifier = Modifier.height(DSTokens.Spacing.sm))
            Brand(DSBrandColor.Amber) { content() }
            Spacer(modifier = Modifier.height(DSTokens.Spacing.sm))
            Brand(DSBrandColor.Sky) { content() }
        }
    }

    /**
     * 一次渲染 Light + Dark + AMOLED + HighContrast 4 种主题对比
     *
     * 用于验证组件在所有主题模式下的视觉一致性。
     */
    @Composable
    fun AllThemes(content: @Composable () -> Unit) {
        Column {
            Light { content() }
            Spacer(modifier = Modifier.height(DSTokens.Spacing.sm))
            Dark { content() }
            Spacer(modifier = Modifier.height(DSTokens.Spacing.sm))
            Amoled { content() }
            Spacer(modifier = Modifier.height(DSTokens.Spacing.sm))
            HighContrast { content() }
        }
    }
}

/**
 * DSPreviewScenes - 标准三件套快捷函数
 *
 * 一次调用生成 Light / Dark / Brand 3 个标准 Preview 场景，
 * 减少每个组件的 Preview 模板代码。
 *
 * 使用示例：
 * ```kotlin
 * // 在文件底部
 * DSPreviewScenes.standardTriple(
 *     name = "Button",
 *     content = { DSButton("Submit", onClick = {}) }
 * )
 * ```
 *
 * ⚠️ 注意：由于 @Preview 注解必须在顶层函数上，本函数不能直接替代 @Preview。
 * 仅作为内容生成工具，业务方仍需手动写 3 个 @Preview 函数。
 * 推荐方式见 [standardTriplePreviewTemplate]。
 */
object DSPreviewScenesStandard {

    /**
     * 标准 3 场景 Preview 模板代码生成（仅参考，需手动复制到文件底部）
     *
     * 复制以下代码到 .kt 文件底部，替换 `ComponentName` 和 `content`：
     *
     * ```kotlin
     * @Preview(showBackground = true, name = "ComponentName - Light")
     * @Composable private fun ComponentNameLightPreview() =
     *     DSPreviewScenes.Light { /* content */ }
     *
     * @Preview(showBackground = true, name = "ComponentName - Dark")
     * @Composable private fun ComponentNameDarkPreview() =
     *     DSPreviewScenes.Dark { /* content */ }
     *
     * @Preview(showBackground = true, name = "ComponentName - Emerald Brand")
     * @Composable private fun ComponentNameEmeraldPreview() =
     *     DSPreviewScenes.Brand(DSBrandColor.Emerald) { /* content */ }
     * ```
     */
    const val standardTriplePreviewTemplate = """
        // 复制以下代码到 .kt 文件底部，替换 ComponentName 和 content
        @Preview(showBackground = true, name = "ComponentName - Light")
        @Composable
        private fun ComponentNameLightPreview() =
            DSPreviewScenes.Light { /* content */ }

        @Preview(showBackground = true, name = "ComponentName - Dark")
        @Composable
        private fun ComponentNameDarkPreview() =
            DSPreviewScenes.Dark { /* content */ }

        @Preview(showBackground = true, name = "ComponentName - Emerald Brand")
        @Composable
        private fun ComponentNameEmeraldPreview() =
            DSPreviewScenes.Brand(DSBrandColor.Emerald) { /* content */ }
    """
}
