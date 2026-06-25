// ============================================================================
// UserScreen.kt
// 个人中心：DSTopBar + DSBottomBar + 资料卡 + 主题设置 + 退出登录
// ============================================================================
package com.pai.app.feature.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pai.app.core.datastore.ThemeMode
import com.pai.app.core.designsystem.primitives.DSAvatar
import com.pai.app.core.designsystem.primitives.DSAvatarSize
import com.pai.app.core.designsystem.shell.DSAppScaffold
import com.pai.app.core.designsystem.shell.DSBottomBar
import com.pai.app.core.designsystem.foundation.tokens.DSNavItem
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSButtonSize
import com.pai.app.core.designsystem.primitives.DSButtonStyle
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.primitives.DSSegmentedControl
import com.pai.app.core.designsystem.primitives.DSSwitch
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.shell.DSTopBarAction
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * UserScreen - 个人中心
 *
 * 整体结构：
 * - topBar: DSTopBar "个人中心" + 右上角 Settings 图标跳转设置页
 * - bottomBar: DSBottomBar（首页 / 我的选中）
 * - 内容（Column verticalScroll）：
 *   - 资料卡（Elevated）：DSAvatar Large "Z" + "ZAI 用户" TitleMedium + "user@pai.com" BodyMedium
 *   - 主题设置卡（Filled）：
 *     - "主题模式" LabelLarge
 *     - DSSegmentedControl ["系统","浅色","深色","纯黑"]（selectedIndex 由 ThemeMode 映射）
 *     - Row SpaceBetween："动态颜色" + DSSwitch
 *   - 退出登录按钮（Error Large 全宽）
 *
 * @param onLogoutSuccess 退出登录成功回调（UI 触发后立即跳转登录页）
 * @param onHomeClick 点击底部"首页"回调
 * @param onSettingsClick 点击右上角 Settings 图标回调，跳转设置页
 * @param viewModel 注入的 UserViewModel
 */
@Composable
internal fun UserScreen(
    onLogoutSuccess: () -> Unit,
    onHomeClick: () -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: UserViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // H15：用 DSAppScaffold 替代 material3.Scaffold，统一页面框架 + 内置 SnackbarHost
    DSAppScaffold(
        title = "个人中心",
        topBarActions = listOf(
            DSTopBarAction(
                icon = Icons.Default.Settings,
                contentDescription = "设置",
                onClick = onSettingsClick,
            ),
        ),
        bottomBar = {
            DSBottomBar(
                items = listOf(
                    DSNavItem(
                        icon = Icons.Default.Home,
                        label = "首页",
                    ),
                    DSNavItem(
                        icon = Icons.Default.Person,
                        label = "我的",
                    ),
                ),
                selectedItem = "我的",
                onItemSelected = { id ->
                    if (id == "首页") onHomeClick()
                },
                animated = true
            )
        },
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
            // 资料卡
            DSCard(
                modifier = Modifier.fillMaxWidth(),
                style = DSCardStyle.Elevated,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
                ) {
                    DSAvatar(
                        initial = "Z",
                        size = DSAvatarSize.Large56,
                        contentDescription = "用户头像",
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xxs)) {
                        DSText(
                            text = "ZAI 用户",
                            variant = DSTextVariant.TitleMedium,
                            color = DSTextColor.Primary,
                        )
                        DSText(
                            text = "user@pai.com",
                            variant = DSTextVariant.BodyMedium,
                            color = DSTextColor.Secondary,
                        )
                    }
                }
            }

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

                    // 主题模式标签
                    DSText(
                        text = "主题模式",
                        variant = DSTextVariant.LabelLarge,
                        color = DSTextColor.Secondary,
                    )

                    // 主题模式分段选择器
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

                    // 动态颜色开关行
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

            // 退出登录按钮（Error Large 全宽）
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
