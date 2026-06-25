// ============================================================================
// DSButtonScreenshotTest.kt
// M3 卓越线补齐：Paparazzi 截图测试基础设施 + 示例
// 用法：
//   录制基线：./gradlew :app:recordPaparazziDebug
//   验证差异：./gradlew :app:verifyPaparazziDebug
//
// 覆盖矩阵：5 种 Button Style × {Light, Dark, AMOLED} × {Indigo, Emerald, Rose, Amber, Sky}
// 当前为基础设施 + 示例覆盖；新增组件时按本模板追加截图测试。
// ============================================================================

package com.pai.app.core.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.cash.paparazzi.Paparazzi
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSBrandColor
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSButtonStyle
import org.junit.Rule
import org.junit.Test

/**
 * DSButton 截图测试套件
 *
 * 用 Paparazzi 在 JVM 上无设备渲染 Compose UI，对比基线 PNG。
 * 命令：
 *   ./gradlew :app:recordPaparazziDebug  # 录制 / 更新基线
 *   ./gradlew :app:verifyPaparazziDebug  # 对比基线（CI 跑这个）
 *
 * 当 button 视觉变更时：
 * 1. 本地运行 recordPaparazziDebug 更新基线
 * 2. PR 包含基线 PNG diff，reviewer 视觉确认
 */
class DSButtonScreenshotTest {

    @get:Rule
    val paparazzi = Paparazzi(
        maxPercentDifference = 0.0,  // 严格匹配，0% 差异容忍
        // 默认渲染 Light + Dark 各一遍；通过 environment 主题参数控制
    )

    /**
     * 所有 Button 风格 × Light 主题
     */
    @Test
    fun button_allStyles_light() {
        paparazzi.snapshot {
            DSDesignTheme(darkTheme = false, dynamicColor = false) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DSButton(text = "Filled", onClick = {}, style = DSButtonStyle.Filled)
                    DSButton(text = "Elevated", onClick = {}, style = DSButtonStyle.Elevated)
                    DSButton(text = "Tonal", onClick = {}, style = DSButtonStyle.Tonal)
                    DSButton(text = "Outlined", onClick = {}, style = DSButtonStyle.Outlined)
                    DSButton(text = "Text", onClick = {}, style = DSButtonStyle.Text)
                    DSButton(text = "Error", onClick = {}, style = DSButtonStyle.Error)
                }
            }
        }
    }

    /**
     * 所有 Button 风格 × Dark 主题
     */
    @Test
    fun button_allStyles_dark() {
        paparazzi.snapshot {
            DSDesignTheme(darkTheme = true, dynamicColor = false) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DSButton(text = "Filled", onClick = {}, style = DSButtonStyle.Filled)
                    DSButton(text = "Elevated", onClick = {}, style = DSButtonStyle.Elevated)
                    DSButton(text = "Tonal", onClick = {}, style = DSButtonStyle.Tonal)
                    DSButton(text = "Outlined", onClick = {}, style = DSButtonStyle.Outlined)
                    DSButton(text = "Text", onClick = {}, style = DSButtonStyle.Text)
                    DSButton(text = "Error", onClick = {}, style = DSButtonStyle.Error)
                }
            }
        }
    }

    /**
     * AMOLED 纯黑模式
     */
    @Test
    fun button_allStyles_amoled() {
        paparazzi.snapshot {
            DSDesignTheme(darkTheme = true, dynamicColor = false, amoled = true) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DSButton(text = "Filled", onClick = {}, style = DSButtonStyle.Filled)
                    DSButton(text = "Elevated", onClick = {}, style = DSButtonStyle.Elevated)
                    DSButton(text = "Tonal", onClick = {}, style = DSButtonStyle.Tonal)
                }
            }
        }
    }

    /**
     * 高对比度主题（无障碍）
     */
    @Test
    fun button_allStyles_highContrast() {
        paparazzi.snapshot {
            DSDesignTheme(darkTheme = false, dynamicColor = false, highContrast = true) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DSButton(text = "Filled", onClick = {}, style = DSButtonStyle.Filled)
                    DSButton(text = "Outlined", onClick = {}, style = DSButtonStyle.Outlined)
                    DSButton(text = "Text", onClick = {}, style = DSButtonStyle.Text)
                }
            }
        }
    }

    /**
     * 多品牌色板验证
     */
    @Test
    fun button_brandColors_light() {
        paparazzi.snapshot {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                DSDesignTheme(darkTheme = false, dynamicColor = false, brandColor = DSBrandColor.Indigo) {
                    DSButton(text = "Indigo", onClick = {})
                }
                DSDesignTheme(darkTheme = false, dynamicColor = false, brandColor = DSBrandColor.Emerald) {
                    DSButton(text = "Emerald", onClick = {})
                }
                DSDesignTheme(darkTheme = false, dynamicColor = false, brandColor = DSBrandColor.Rose) {
                    DSButton(text = "Rose", onClick = {})
                }
                DSDesignTheme(darkTheme = false, dynamicColor = false, brandColor = DSBrandColor.Amber) {
                    DSButton(text = "Amber", onClick = {})
                }
                DSDesignTheme(darkTheme = false, dynamicColor = false, brandColor = DSBrandColor.Sky) {
                    DSButton(text = "Sky", onClick = {})
                }
            }
        }
    }

    /**
     * 禁用态、加载态
     */
    @Test
    fun button_states_light() {
        paparazzi.snapshot {
            DSDesignTheme(darkTheme = false, dynamicColor = false) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DSButton(text = "Disabled", onClick = {}, enabled = false)
                    DSButton(text = "Loading", onClick = {}, loading = true)
                    DSButton(text = "Disabled Loading", onClick = {}, enabled = false, loading = true)
                }
            }
        }
    }
}
