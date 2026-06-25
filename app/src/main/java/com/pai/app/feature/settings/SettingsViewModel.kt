// ============================================================================
// SettingsViewModel.kt
// 设置页 ViewModel：主题模式 / 动态颜色 / 缓存 / 关于 / 退出登录
// ============================================================================
package com.pai.app.feature.settings

import androidx.lifecycle.viewModelScope
import com.pai.app.core.base.BaseViewModel
import com.pai.app.core.base.UiState
import com.pai.app.core.datastore.ThemeMode
import com.pai.app.core.datastore.ThemePreferences
import com.pai.app.navigation.AppNavigator
import com.pai.app.navigation.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置页 UI 状态
 *
 * M2：实现 [UiState] 标记接口（团队约定，详见 [UiState] KDoc）。
 *
 * - [themeMode] 当前主题模式
 * - [dynamicColor] 是否启用动态取色
 * - [showClearCacheDialog] 是否展示"清除缓存"确认弹窗
 * - [showAboutDialog] 是否展示"关于我们"弹窗
 *
 * @param themeMode 主题模式
 * @param dynamicColor 动态取色开关
 * @param showClearCacheDialog 清缓存弹窗开关
 * @param showAboutDialog 关于弹窗开关
 */
internal data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = false,
    val showClearCacheDialog: Boolean = false,
    val showAboutDialog: Boolean = false,
) : UiState

/**
 * 设置页 ViewModel
 *
 * 职责：
 * 1. 暴露主题相关状态（[ThemePreferences.themeMode] + [ThemePreferences.dynamicColor]）
 *    并叠加两个弹窗开关字段，统一合并为 [SettingsUiState]
 * 2. 提供主题修改入口（[setThemeMode] / [setDynamicColor]）
 * 3. 提供弹窗开关入口（[showClearCacheDialog] / [dismissClearCacheDialog] /
 *    [showAboutDialog] / [dismissAboutDialog]）
 * 4. 提供清除缓存占位入口 [clearCache]；退出登录继承自 [BaseViewModel.logout]
 *
 * 主题状态共享同一份 [ThemePreferences] 单例（Hilt @Singleton），
 * 修改后 [com.pai.app.core.appstate.MainActivityViewModel] 也会立即收到更新。
 *
 * 退出登录逻辑（清空 DataStore + 重置内存状态）由 [BaseViewModel.logout] 委托给
 * [UserState.logout]，子类无需重复实现。
 *
 * @param themePreferences 主题偏好
 */
@HiltViewModel
internal class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    navigator: AppNavigator,
    userState: UserState,
) : BaseViewModel(navigator, userState) {

    // 弹窗开关使用 MutableStateFlow 单独维护，再与主题 Flow 合并
    private val dialogFlags = kotlinx.coroutines.flow.MutableStateFlow(DialogFlags())

    /**
     * 设置页 UI 状态
     *
     * 通过 combine 合并主题模式、动态颜色两个 Flow 与弹窗开关 StateFlow，
     * 使用 WhileSubscribed(5000) 保持 5 秒订阅窗口，避免配置变更时重新加载。
     */
    val uiState: StateFlow<SettingsUiState> = combine(
        themePreferences.themeMode,
        themePreferences.dynamicColor,
        dialogFlags,
    ) { mode, dynamic, flags ->
        SettingsUiState(
            themeMode = mode,
            dynamicColor = dynamic,
            showClearCacheDialog = flags.showClearCacheDialog,
            showAboutDialog = flags.showAboutDialog,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState(),
    )

    /**
     * 设置主题模式
     *
     * @param mode 目标主题模式
     */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    /**
     * 启用 / 禁用动态取色
     *
     * @param enabled 是否启用
     */
    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setDynamicColor(enabled)
        }
    }

    /** 显示"清除缓存"确认弹窗 */
    fun showClearCacheDialog() {
        dialogFlags.update { it.copy(showClearCacheDialog = true) }
    }

    /** 关闭"清除缓存"确认弹窗 */
    fun dismissClearCacheDialog() {
        dialogFlags.update { it.copy(showClearCacheDialog = false) }
    }

    /** 显示"关于我们"弹窗 */
    fun showAboutDialog() {
        dialogFlags.update { it.copy(showAboutDialog = true) }
    }

    /** 关闭"关于我们"弹窗 */
    fun dismissAboutDialog() {
        dialogFlags.update { it.copy(showAboutDialog = false) }
    }

    /**
     * 清除缓存（占位实现）
     *
     * 当前为空实现，业务方拉取后可接入真实缓存清理逻辑。
     * 弹窗确认按钮会自动调用 [dismissClearCacheDialog] 关闭弹窗。
     */
    fun clearCache() {
        // 占位：业务方接入真实缓存清理逻辑，如：
        // exampleRepository.clearCache()
        // 或触发 WorkManager 清理任务
    }

    /** 弹窗开关内部承载结构 */
    private data class DialogFlags(
        val showClearCacheDialog: Boolean = false,
        val showAboutDialog: Boolean = false,
    )
}
