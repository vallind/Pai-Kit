// ============================================================================
// DSThemeDebugPanel.kt
// M3 卓越线补齐：开发模式主题调试面板（实时切换主题/品牌/字号/对比度）
// 用法：BuildConfig.DEBUG 模式下浮动按钮触发，发布构建自动 no-op
// ============================================================================

package com.pai.app.core.designsystem.foundation.theme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.pai.app.core.designsystem.foundation.tokens.DSBrandColor
import com.pai.app.core.designsystem.foundation.tokens.DSFontSizeScale
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 主题调试面板状态
 *
 * 由调用方持有，调试面板读写此状态。
 */
data class DSThemeDebugState(
    val darkTheme: Boolean = false,
    val dynamicColor: Boolean = false,
    val brandColor: DSBrandColor = DSBrandColor.Indigo,
    val fontSizeScale: DSFontSizeScale = DSFontSizeScale.Normal,
    val amoled: Boolean = false,
    val highContrast: Boolean = false
)

/**
 * DSThemeDebugPanel - 主题调试面板
 *
 * 在开发模式下提供浮动按钮，点击打开主题调试面板，实时切换：
 * - 深色 / 浅色模式
 * - AMOLED 纯黑模式
 * - 高对比度模式（无障碍）
 * - 5 种品牌色板
 * - 4 档字号缩放
 * - Dynamic Color 开关
 *
 * 使用示例：
 * ```kotlin
 * // 在 MainActivity 顶层
 * var debugState by remember { mutableStateOf(DSThemeDebugState()) }
 * DSDesignTheme(
 *     darkTheme = debugState.darkTheme,
 *     dynamicColor = debugState.dynamicColor,
 *     brandColor = debugState.brandColor,
 *     fontSizeScale = debugState.fontSizeScale,
 *     amoled = debugState.amoled,
 *     highContrast = debugState.highContrast
 * ) {
 *     AppContent()
 *     DSThemeDebugPanel(
 *         state = debugState,
 *         onStateChange = { debugState = it },
 *         enabled = BuildConfig.DEBUG  // 仅 Debug 构建启用
 *     )
 * }
 * ```
 *
 * @param state 当前主题状态
 * @param onStateChange 状态变化回调
 * @param modifier 修饰符
 * @param enabled 是否启用面板（生产构建传 false 自动 no-op）
 */
@Composable
fun DSThemeDebugPanel(
    state: DSThemeDebugState,
    onStateChange: (DSThemeDebugState) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    if (!enabled) return

    var showPanel by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // 浮动按钮（右下角）
        IconButton(
            onClick = { showPanel = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.BugReport,
                contentDescription = "打开主题调试面板",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        // 调试面板
        if (showPanel) {
            ThemeDebugPanelContent(
                state = state,
                onStateChange = onStateChange,
                onDismiss = { showPanel = false }
            )
        }
    }
}

@Composable
private fun ThemeDebugPanelContent(
    state: DSThemeDebugState,
    onStateChange: (DSThemeDebugState) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(DSTokens.Radius.extraLarge),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = DSTokens.Elevation.level6
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // 标题栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "主题调试面板",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "关闭"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(DSTokens.Spacing.md))

                // 深色 / 浅色
                DebugToggleRow(
                    label = "深色模式",
                    checked = state.darkTheme,
                    onCheckedChange = { onStateChange(state.copy(darkTheme = it)) }
                )

                // AMOLED
                DebugToggleRow(
                    label = "AMOLED 纯黑",
                    checked = state.amoled,
                    enabled = state.darkTheme,
                    onCheckedChange = { onStateChange(state.copy(amoled = it)) }
                )

                // 高对比度
                DebugToggleRow(
                    label = "高对比度 (a11y)",
                    checked = state.highContrast,
                    onCheckedChange = { onStateChange(state.copy(highContrast = it)) }
                )

                // Dynamic Color
                DebugToggleRow(
                    label = "Dynamic Color",
                    checked = state.dynamicColor,
                    onCheckedChange = { onStateChange(state.copy(dynamicColor = it)) }
                )

                Spacer(modifier = Modifier.height(DSTokens.Spacing.md))

                // 品牌色
                Text(
                    text = "品牌色板",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(DSTokens.Spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)
                ) {
                    DSBrandColor.entries.forEach { brand ->
                        BrandColorChip(
                            brand = brand,
                            isSelected = state.brandColor == brand,
                            onClick = { onStateChange(state.copy(brandColor = brand)) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(DSTokens.Spacing.md))

                // 字号缩放
                Text(
                    text = "字号缩放",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(DSTokens.Spacing.xs))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)
                ) {
                    DSFontSizeScale.entries.forEach { scale ->
                        FilledTonalButton(
                            onClick = { onStateChange(state.copy(fontSizeScale = scale)) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (state.fontSizeScale == scale)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (state.fontSizeScale == scale)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text(
                                text = "${scale.multiplier}x",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(DSTokens.Spacing.md))

                // 当前 ColorScheme 预览
                ColorSchemePreview()
            }
        }
    }
}

@Composable
private fun DebugToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DSTokens.Spacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (enabled) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled
        )
    }
}

@Composable
private fun BrandColorChip(
    brand: DSBrandColor,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(if (isSelected) 36.dp else 32.dp)
            .padding(2.dp),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(28.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = brand.primary,
                modifier = Modifier.size(24.dp)
            ) {}
        }
    }
}

@Composable
private fun ColorSchemePreview() {
    Text(
        text = "当前 ColorScheme",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(DSTokens.Spacing.xs))
    val colors = listOf(
        "primary" to MaterialTheme.colorScheme.primary,
        "onPrimary" to MaterialTheme.colorScheme.onPrimary,
        "primaryContainer" to MaterialTheme.colorScheme.primaryContainer,
        "secondary" to MaterialTheme.colorScheme.secondary,
        "tertiary" to MaterialTheme.colorScheme.tertiary,
        "error" to MaterialTheme.colorScheme.error,
        "surface" to MaterialTheme.colorScheme.surface,
        "surfaceVariant" to MaterialTheme.colorScheme.surfaceVariant,
        "outline" to MaterialTheme.colorScheme.outline
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        colors.forEach { (name, color) ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    color = color,
                    modifier = Modifier.size(28.dp),
                    shape = RoundedCornerShape(DSTokens.Radius.extraSmall)
                ) {}
                Text(
                    text = name,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "ThemeDebugPanel", widthDp = 360, heightDp = 640)
@Composable
private fun DSThemeDebugPanelPreview() {
    var state by remember { mutableStateOf(DSThemeDebugState()) }
    DSDesignTheme(
        darkTheme = state.darkTheme,
        dynamicColor = state.dynamicColor,
        brandColor = state.brandColor,
        fontSizeScale = state.fontSizeScale,
        amoled = state.amoled,
        highContrast = state.highContrast
    ) {
        DSThemeDebugPanel(
            state = state,
            onStateChange = { state = it },
            enabled = true
        )
    }
}
