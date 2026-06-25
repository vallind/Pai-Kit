// ============================================================================
// SettingsScreen.kt
// 设置页：主题设置 / 通用 / 账户三组卡片 + 清缓存与关于弹窗
// ============================================================================
package com.pai.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pai.app.core.datastore.ThemeMode
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSButtonSize
import com.pai.app.core.designsystem.primitives.DSButtonStyle
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.overlays.DSDialog
import com.pai.app.core.designsystem.foundation.tokens.DSMessageType
import com.pai.app.core.designsystem.primitives.DSListItem
import com.pai.app.core.designsystem.shell.DSAppScaffold
import com.pai.app.core.designsystem.primitives.DSSegmentedControl
import com.pai.app.core.designsystem.primitives.DSSwitch
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * SettingsScreen - 设置页
 *
 * 整体结构：
 * - topBar: DSTopBar "设置" + 返回按钮
 * - 内容（Column verticalScroll）：
 *   - 主题设置卡（Filled）：主题模式分段选择器 + 动态颜色开关
 *   - 通用卡（Filled）：清除缓存 / 关于我们 两个 DSListItem
 *   - 账户卡（Filled）：退出登录按钮（Error Large 全宽）
 * - 弹窗：
 *   - [SettingsUiState.showClearCacheDialog] → DSDialog Warning "确认清除缓存？"
 *   - [SettingsUiState.showAboutDialog] → DSDialog Info 仅"知道了"按钮
 *
 * @param onLogoutSuccess 退出登录成功回调（UI 触发后立即跳转登录页）
 * @param onBackClick 返回回调
 * @param viewModel 注入的 SettingsViewModel
 */
@Composable
internal fun SettingsScreen(
    onLogoutSuccess: () -> Unit,
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // H15：用 DSAppScaffold 替代 material3.Scaffold
    DSAppScaffold(
        title = "设置",
        showBackIcon = true,
        onBackClick = onBackClick,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(
                    horizontal = DSTokens.Spacing.lg,
                    vertical = DSTokens.Spacing.md,
                ),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg),
        ) {
            // 主题设置卡
            DSCard(
                modifier = Modifier.fillMaxWidth(),
                style = DSCardStyle.Filled,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                    DSText(
                        text = "主题设置",
                        variant = DSTextVariant.TitleMedium,
                        color = DSTextColor.Primary,
                    )

                    DSText(
                        text = "主题模式",
                        variant = DSTextVariant.LabelLarge,
                        color = DSTextColor.Secondary,
                    )

                    DSSegmentedControl(
                        options = listOf("系统", "浅色", "深色", "纯黑"),
                        selectedIndex = when (uiState.themeMode) {
                            ThemeMode.System -> 0
                            ThemeMode.Light -> 1
                            ThemeMode.Dark -> 2
                            ThemeMode.AMOLED -> 3
                        },
                        onSelectedChange = { index ->
                            val mode = when (index) {
                                0 -> ThemeMode.System
                                1 -> ThemeMode.Light
                                2 -> ThemeMode.Dark
                                else -> ThemeMode.AMOLED
                            }
                            viewModel.setThemeMode(mode)
                        },
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DSText(
                            text = "动态颜色",
                            variant = DSTextVariant.BodyMedium,
                            color = DSTextColor.Primary,
                        )
                        DSSwitch(
                            checked = uiState.dynamicColor,
                            onCheckedChange = viewModel::setDynamicColor,
                        )
                    }
                }
            }

            // 通用卡
            DSCard(
                modifier = Modifier.fillMaxWidth(),
                style = DSCardStyle.Filled,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)) {
                    DSText(
                        text = "通用",
                        variant = DSTextVariant.TitleMedium,
                        color = DSTextColor.Primary,
                    )
                    DSListItem(
                        title = "清除缓存",
                        leadingIcon = Icons.Default.Cached,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = viewModel::showClearCacheDialog,
                    )
                    DSListItem(
                        title = "关于我们",
                        leadingIcon = Icons.Default.Info,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = viewModel::showAboutDialog,
                    )
                }
            }

            // 账户卡
            DSCard(
                modifier = Modifier.fillMaxWidth(),
                style = DSCardStyle.Filled,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                    DSText(
                        text = "账户",
                        variant = DSTextVariant.TitleMedium,
                        color = DSTextColor.Primary,
                    )
                    DSButton(
                        text = "退出登录",
                        onClick = {
                            viewModel.logout()
                            onLogoutSuccess()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        style = DSButtonStyle.Error,
                        size = DSButtonSize.Large,
                    )
                }
            }
        }
    }

    // 清除缓存确认弹窗
    if (uiState.showClearCacheDialog) {
        DSDialog(
            onDismiss = viewModel::dismissClearCacheDialog,
            title = "确认清除缓存？",
            message = "清除缓存不会影响您的收藏与浏览历史。",
            type = DSMessageType.Warning,
            confirmText = "清除",
            onConfirm = viewModel::clearCache,
            dismissText = "取消",
            onDismissClick = viewModel::dismissClearCacheDialog,
        )
    }

    // 关于我们弹窗（仅"知道了"按钮）
    if (uiState.showAboutDialog) {
        DSDialog(
            onDismiss = viewModel::dismissAboutDialog,
            title = "关于我们",
            message = "Pai Design System v1.0.0\nAndroid 原生单模块脚手架",
            type = DSMessageType.Info,
            confirmText = "知道了",
            onConfirm = {},
            dismissText = null,
            onDismissClick = null,
        )
    }
}
