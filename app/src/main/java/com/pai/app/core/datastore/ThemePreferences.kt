// ============================================================================
// ThemePreferences.kt
// 主题相关偏好读写
// ============================================================================

package com.pai.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 主题偏好
 *
 * 管理应用级主题配置：
 * - [themeMode]：主题模式（System / Light / Dark / AMOLED）
 * - [dynamicColor]：是否启用 Android 12+ 动态取色
 * - [brandColorName]：品牌色板名称（"Indigo" / "Emerald" / "Rose" / "Amber" / "Sky"）
 * - [fontSizeScaleName]：字号缩放名称（"Small" / "Normal" / "Large" / "ExtraLarge"）
 *
 * 包级隔离约定：
 * 为避免 datastore 反向依赖 designsystem，本类只持久化 / 暴露字符串值，
 * 由上层（[com.pai.app.core.appstate.MainActivityViewModel]）负责转换为
 * [com.pai.app.core.designsystem.foundation.tokens.DSBrandColor] /
 * [com.pai.app.core.designsystem.foundation.tokens.DSFontSizeScale] 枚举。
 *
 * 注意：此类必须保持 public 可见性（由 Hilt `@Provides` 返回），
 * 但对外暴露 [ThemeMode]（internal）的成员标记为 `internal`。
 */
@Singleton
class ThemePreferences @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val store = context.themeDataStore

    /**
     * 观察当前主题模式，默认 [ThemeMode.System]
     *
     * 若存储的值无法解析为 [ThemeMode]，回退到 [ThemeMode.System]。
     */
    internal val themeMode: Flow<ThemeMode> = store.data.map { prefs ->
        prefs[KEY_THEME_MODE]
            ?.let { name -> runCatching { ThemeMode.valueOf(name) }.getOrNull() }
            ?: ThemeMode.System
    }

    /** 观察是否启用动态取色，默认 `false` */
    internal val dynamicColor: Flow<Boolean> = store.data.map { prefs ->
        prefs[KEY_DYNAMIC_COLOR] ?: false
    }

    /**
     * 观察当前品牌色板名称，默认 `"Indigo"`
     *
     * 上层通过 [com.pai.app.core.designsystem.foundation.tokens.DSBrandColor.fromName]
     * 将字符串转换为枚举。无法识别时枚举侧也会回退到 `Indigo`。
     */
    internal val brandColorName: Flow<String> = store.data.map { prefs ->
        prefs[KEY_BRAND_COLOR] ?: DEFAULT_BRAND_COLOR
    }

    /**
     * 观察当前字号缩放名称，默认 `"Normal"`
     *
     * 上层通过 [com.pai.app.core.designsystem.foundation.tokens.DSFontSizeScale.fromName]
     * 将字符串转换为枚举。无法识别时枚举侧也会回退到 `Normal`。
     */
    internal val fontSizeScaleName: Flow<String> = store.data.map { prefs ->
        prefs[KEY_FONT_SIZE_SCALE] ?: DEFAULT_FONT_SIZE_SCALE
    }

    /** 设置主题模式 */
    internal suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[KEY_THEME_MODE] = mode.name }
    }

    /** 启用 / 禁用动态取色 */
    internal suspend fun setDynamicColor(enabled: Boolean) {
        store.edit { it[KEY_DYNAMIC_COLOR] = enabled }
    }

    /**
     * 设置品牌色板
     *
     * @param brandColorName 品牌色枚举名（如 `"Indigo"` / `"Emerald"`），
     *   通常由 `DSBrandColor.name` 提供
     */
    internal suspend fun setBrandColor(brandColorName: String) {
        store.edit { it[KEY_BRAND_COLOR] = brandColorName }
    }

    /**
     * 设置字号缩放
     *
     * @param fontSizeScaleName 字号缩放枚举名（如 `"Small"` / `"Normal"`），
     *   通常由 `DSFontSizeScale.name` 提供
     */
    internal suspend fun setFontSizeScale(fontSizeScaleName: String) {
        store.edit { it[KEY_FONT_SIZE_SCALE] = fontSizeScaleName }
    }

    private companion object {
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        private val KEY_BRAND_COLOR = stringPreferencesKey("brand_color")
        private val KEY_FONT_SIZE_SCALE = stringPreferencesKey("font_size_scale")

        private const val DEFAULT_BRAND_COLOR = "Indigo"
        private const val DEFAULT_FONT_SIZE_SCALE = "Normal"
    }
}
