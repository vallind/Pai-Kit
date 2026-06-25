// ============================================================================
// ThemePreferencesTest.kt
// ThemePreferences 单元测试（Robolectric）：主题模式 + 动态取色
// ============================================================================

package com.pai.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [ThemePreferences] 单元测试（Robolectric）
 *
 * 使用真实的 Android Context + Preferences DataStore，验证：
 * - 默认 themeMode = System
 * - setThemeMode(Dark) 后 themeMode Flow 发出 Dark
 * - setThemeMode(AMOLED) 后 themeMode Flow 发出 AMOLED
 * - 默认 dynamicColor = false
 * - setDynamicColor(true) 后发出 true
 * - 默认 brandColorName = "Indigo"
 * - setBrandColor("Emerald") 后发出 "Emerald"
 * - 默认 fontSizeScaleName = "Normal"
 * - setFontSizeScale("Large") 后发出 "Large"
 *
 * 说明：
 * - DataStore 内部基于 Dispatchers.IO，使用 [runBlocking] 等待磁盘读写完成
 * - 每个用例前清空 DataStore，避免用例间状态污染
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ThemePreferencesTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val preferences = ThemePreferences(context)

    @Before
    fun setup() {
        // 清空主题 DataStore，确保每个用例从默认状态开始
        runBlocking { context.themeDataStore.edit { it.clear() } }
    }

    @Test
    fun `默认 themeMode 为 System`() = runBlocking {
        assertEquals(ThemeMode.System, preferences.themeMode.first())
    }

    @Test
    fun `setThemeMode Dark 后 themeMode 发出 Dark`() = runBlocking {
        preferences.setThemeMode(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, preferences.themeMode.first())
    }

    @Test
    fun `setThemeMode AMOLED 后 themeMode 发出 AMOLED`() = runBlocking {
        preferences.setThemeMode(ThemeMode.AMOLED)
        assertEquals(ThemeMode.AMOLED, preferences.themeMode.first())
    }

    @Test
    fun `默认 dynamicColor 为 false`() = runBlocking {
        assertFalse(preferences.dynamicColor.first())
    }

    @Test
    fun `setDynamicColor true 后发出 true`() = runBlocking {
        preferences.setDynamicColor(true)
        assertEquals(true, preferences.dynamicColor.first())
    }

    @Test
    fun `默认 brandColorName 为 Indigo`() = runBlocking {
        assertEquals("Indigo", preferences.brandColorName.first())
    }

    @Test
    fun `setBrandColor Emerald 后发出 Emerald`() = runBlocking {
        preferences.setBrandColor("Emerald")
        assertEquals("Emerald", preferences.brandColorName.first())
    }

    @Test
    fun `默认 fontSizeScaleName 为 Normal`() = runBlocking {
        assertEquals("Normal", preferences.fontSizeScaleName.first())
    }

    @Test
    fun `setFontSizeScale Large 后发出 Large`() = runBlocking {
        preferences.setFontSizeScale("Large")
        assertEquals("Large", preferences.fontSizeScaleName.first())
    }
}
