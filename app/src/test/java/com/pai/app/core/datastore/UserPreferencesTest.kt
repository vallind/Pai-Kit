// ============================================================================
// UserPreferencesTest.kt
// UserPreferences 单元测试（Robolectric）：登录态读写
// 决策 4：token 已迁移到 EncryptedPrefs（独立测试见 EncryptedPrefsTest）
// ============================================================================

package com.pai.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [UserPreferences] 单元测试（Robolectric）
 *
 * 决策 4 后职责：本类仅持久化 `userId` 到 Preferences DataStore；token 已迁移到
 * [EncryptedPrefs]（AES256-GCM 加密），由 [EncryptedPrefsTest] 单独覆盖。
 *
 * 验证登录态相关偏好的读写：
 * - 初始 loggedInUserId = null
 * - saveLogin(userId, token) 后 loggedInUserId 同步更新（userId 落 DataStore，
 *   token 委托给 EncryptedPrefs —— 本测试只断言 userId）
 * - isLoggedIn Flow：saveLogin 前 false，后 true
 * - clearLogin 后所有字段清空
 *
 * 说明：
 * - 使用 [runBlocking] 等待 DataStore 磁盘读写完成
 * - 每个用例前清空 DataStore，避免用例间状态污染
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class UserPreferencesTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val encryptedPrefs = EncryptedPrefs(context)
    private val preferences = UserPreferences(context, encryptedPrefs)

    @Before
    fun setup() {
        // 清空用户偏好 DataStore，确保每个用例从默认状态开始
        runBlocking { context.userDataStore.edit { it.clear() } }
        // 清空 EncryptedPrefs 中的 token（决策 4：token 由 EncryptedPrefs 持有）
        runBlocking { encryptedPrefs.clearToken() }
    }

    @Test
    fun `初始 loggedInUserId 为 null`() = runBlocking {
        assertNull(preferences.loggedInUserId.first())
    }

    @Test
    fun `saveLogin 后 loggedInUserId 同步更新`() = runBlocking {
        preferences.saveLogin(userId = 1L, token = "token")

        assertEquals(1L, preferences.loggedInUserId.first())
    }

    @Test
    fun `saveLogin 后 token 委托给 EncryptedPrefs 持久化`() = runBlocking {
        // 决策 4：UserPreferences.saveLogin 内部委托 token 给 EncryptedPrefs.saveToken
        preferences.saveLogin(userId = 1L, token = "encrypted-token-123")

        assertEquals("encrypted-token-123", encryptedPrefs.observeToken().first())
    }

    @Test
    fun `isLoggedIn Flow 在 saveLogin 前为 false`() = runBlocking {
        assertFalse(preferences.isLoggedIn.first())
    }

    @Test
    fun `isLoggedIn Flow 在 saveLogin 后为 true`() = runBlocking {
        preferences.saveLogin(userId = 1L, token = "token")

        assertTrue(preferences.isLoggedIn.first())
    }

    @Test
    fun `clearLogin 后所有字段清空`() = runBlocking {
        // 先写入登录态
        preferences.saveLogin(userId = 1L, token = "token")
        assertEquals(1L, preferences.loggedInUserId.first())

        // 再清除
        preferences.clearLogin()

        assertNull(preferences.loggedInUserId.first())
        // 决策 4：clearLogin 同步清 EncryptedPrefs 中的 token
        assertNull(encryptedPrefs.observeToken().first())
        assertFalse(preferences.isLoggedIn.first())
    }
}
