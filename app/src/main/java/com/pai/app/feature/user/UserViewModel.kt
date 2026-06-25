// ============================================================================
// UserViewModel.kt
// 个人中心 ViewModel：主题模式 + 动态颜色 + 退出登录
// 继承 BaseViewModel，演示通用模板
// ============================================================================
package com.pai.app.feature.user

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
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 个人中心 UI 状态
 *
 * M2：实现 [UiState] 标记接口（团队约定，详见 [UiState] KDoc）。
 *
 * @param themeMode 当前主题模式
 * @param dynamicColor 是否启用动态取色
 */
internal data class UserUiState(
    val themeMode: ThemeMode = ThemeMode.System,
    val dynamicColor: Boolean = false,
) : UiState

/**
 * 个人中心 ViewModel
 *
 * 继承 [BaseViewModel] 演示通用模板：
 * 1. 暴露主题相关状态（[ThemePreferences.themeMode] + [ThemePreferences.dynamicColor]）
 * 2. 提供主题修改入口（[setThemeMode] / [setDynamicColor]）
 * 3. 退出登录继承自 [BaseViewModel.logout]（委托 [UserState.logout] 清空 DataStore + 内存）
 *
 * 主题状态使用 combine 合并后转 StateFlow，与 MainActivity 共享同一份 ThemePreferences
 * 单例（Hilt @Singleton），保证修改后 MainActivityViewModel 也能立即收到更新。
 *
 * @param themePreferences 主题偏好
 * @param navigator 全局导航器（BaseViewModel 注入）
 * @param userState 全局登录状态（BaseViewModel 注入）
 */
@HiltViewModel
internal class UserViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    navigator: AppNavigator,
    userState: UserState,
) : BaseViewModel(navigator, userState) {

    /**
     * 个人中心 UI 状态
     *
     * 通过 combine 合并主题模式与动态颜色两个 Flow，
     * 使用 WhileSubscribed(5000) 保持 5 秒订阅窗口，避免配置变更时重新加载。
     */
    val uiState: StateFlow<UserUiState> = combine(
        themePreferences.themeMode,
        themePreferences.dynamicColor,
    ) { mode, dynamic ->
        UserUiState(themeMode = mode, dynamicColor = dynamic)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserUiState(),
    )

    /** 设置主题模式 */
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    /** 启用 / 禁用动态取色 */
    fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setDynamicColor(enabled)
        }
    }
}
