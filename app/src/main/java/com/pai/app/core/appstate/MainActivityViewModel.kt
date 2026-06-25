// ============================================================================
// MainActivityViewModel.kt
// 宿主级 ViewModel
// 绑定在 MainActivity 上，应用全局共享：主题模式、动态颜色、品牌色板、字号缩放
// 任何 feature 均可通过 hiltViewModel() 在 MainActivity scope 复用本实例
// ============================================================================

package com.pai.app.core.appstate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pai.app.core.datastore.ThemeMode
import com.pai.app.core.datastore.ThemePreferences
import com.pai.app.core.designsystem.foundation.tokens.DSBrandColor
import com.pai.app.core.designsystem.foundation.tokens.DSFontSizeScale
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 宿主级 ViewModel
 *
 * 绑定在 MainActivity 生命周期上，作为应用全局共享状态的唯一入口：
 * - [themeMode]：当前主题模式（System / Light / Dark / AMOLED）
 * - [dynamicColor]：是否启用动态取色
 * - [brandColor]：当前品牌色板（由 datastore 字符串转换为 [DSBrandColor] 枚举）
 * - [fontSizeScale]：当前字号缩放（由 datastore 字符串转换为 [DSFontSizeScale] 枚举）
 *
 * 决策 5：登录态单一真相源为 [com.pai.app.navigation.UserState]，
 * 本类不再暴露 `isLoggedIn` StateFlow（旧实现是次级来源，与 UserState 短暂不一致）。
 * 如需读取登录态，请直接订阅 `userState.isLoggedIn`。
 *
 * 共享方式：
 * 任何子 Composable 通过 `hiltViewModel()` 拿到的 MainActivityViewModel 实例
 * 在 MainActivity 的 ViewModelStore 范围内是单例，跨 feature 共享。
 *
 * 字符串 → 枚举转换：
 * [ThemePreferences] 为避免反向依赖 designsystem，只持久化字符串。
 * 本类负责调用 [DSBrandColor.fromName] / [DSFontSizeScale.fromName] 完成转换，
 * 再以 StateFlow 暴露枚举给 UI 层。
 *
 * 注意：
 * 本类必须保持 public 可见性，否则 Hilt 在编译期生成 Hilt_MainActivityViewModel
 * 时无法访问。类成员方法 / 属性的可见性不受此限制。
 *
 * 决策 5 (Medium #18)：所有偏好 StateFlow 与对应 setter 统一标 `internal`，
 * 避免可见性不一致。
 *
 * @param themePreferences 主题偏好（DataStore），同时用于读取与写入
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
) : ViewModel() {

    /**
     * 当前主题模式
     *
     * 由 [ThemePreferences.themeMode] 转 StateFlow，
     * 订阅超时 5 秒（WhileSubscribed(5000)），初始值 [ThemeMode.System]。
     */
    internal val themeMode: StateFlow<ThemeMode> = themePreferences.themeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.System
        )

    /**
     * 是否启用动态取色
     *
     * 由 [ThemePreferences.dynamicColor] 转 StateFlow，
     * 订阅超时 5 秒，初始值 `false`。
     */
    internal val dynamicColor: StateFlow<Boolean> = themePreferences.dynamicColor
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false
        )

    /**
     * 当前品牌色板
     *
     * 由 [ThemePreferences.brandColorName] 字符串经 [DSBrandColor.fromName] 转换为枚举，
     * 订阅超时 5 秒，初始值 [DSBrandColor.Indigo]（与 DataStore 默认值一致）。
     */
    internal val brandColor: StateFlow<DSBrandColor> = themePreferences.brandColorName
        .map { name -> DSBrandColor.fromName(name) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DSBrandColor.Indigo
        )

    /**
     * 当前字号缩放
     *
     * 由 [ThemePreferences.fontSizeScaleName] 字符串经 [DSFontSizeScale.fromName] 转换为枚举，
     * 订阅超时 5 秒，初始值 [DSFontSizeScale.Normal]（与 DataStore 默认值一致）。
     */
    internal val fontSizeScale: StateFlow<DSFontSizeScale> = themePreferences.fontSizeScaleName
        .map { name -> DSFontSizeScale.fromName(name) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = DSFontSizeScale.Normal
        )

    /**
     * 设置主题模式
     *
     * @param mode 目标主题模式
     */
    internal fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themePreferences.setThemeMode(mode)
        }
    }

    /**
     * 启用 / 禁用动态取色
     *
     * @param enabled 是否启用
     */
    internal fun setDynamicColor(enabled: Boolean) {
        viewModelScope.launch {
            themePreferences.setDynamicColor(enabled)
        }
    }

    /**
     * 设置品牌色板
     *
     * 将枚举名写入 DataStore，[brandColor] StateFlow 会随后发出新值。
     *
     * @param brand 目标品牌色板枚举
     */
    internal fun setBrandColor(brand: DSBrandColor) {
        viewModelScope.launch {
            themePreferences.setBrandColor(brand.name)
        }
    }

    /**
     * 设置字号缩放
     *
     * 将枚举名写入 DataStore，[fontSizeScale] StateFlow 会随后发出新值。
     *
     * @param scale 目标字号缩放枚举
     */
    internal fun setFontSizeScale(scale: DSFontSizeScale) {
        viewModelScope.launch {
            themePreferences.setFontSizeScale(scale.name)
        }
    }
}
