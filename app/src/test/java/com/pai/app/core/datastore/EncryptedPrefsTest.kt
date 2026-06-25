// ============================================================================
// EncryptedPrefsTest.kt
// EncryptedPrefsImpl 单元测试（Robolectric）：AES256-GCM 加密 token 持久化
// 决策 4：token 安全存储
// ============================================================================

package com.pai.app.core.datastore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * [EncryptedPrefsImpl] 单元测试（Robolectric）
 *
 * 决策 4：用 EncryptedSharedPreferences（AES256-GCM）持久化 Bearer token，
 * 替代旧 UserPreferences.token 明文存储（High #6）。
 *
 * 验证：
 * - 初始 observeToken = null（无历史 token）
 * - saveToken 后 observeToken 反映新值
 * - clearToken 后 observeToken 回到 null
 * - saveToken(null) 等价于 clearToken
 * - 首帧（订阅即得）即返回当前持久化值，无延迟
 *
 * Robolectric 4.16 支持模拟 Android KeyStore（API 23+），故可在 JVM 跑。
 * 若 CI 环境对 EncryptedSharedPreferences 出现兼容性问题，可改 @Ignore 并改在
 * 真机 instrumented 测试覆盖；当前 Robolectric 4.16.1 + SDK 33 验证通过。
 *
 * 说明：
 * - 使用 [runBlocking] 等待加密盘读写完成
 * - 每个用例前 clearToken，避免用例间状态污染
 * - 直接测试 [EncryptedPrefsImpl]（生产实现），[InMemoryEncryptedPrefs] 的测试见
 *   [InMemoryEncryptedPrefsTest]
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class EncryptedPrefsTest {

    private val context: Context = ApplicationProvider.getApplicationContext()
    private val prefs = EncryptedPrefsImpl(context)

    @Before
    fun setup() {
        // 清空加密 prefs，确保每个用例从默认状态开始
        runBlocking { prefs.clearToken() }
    }

    @Test
    fun `初始 observeToken 为 null`() = runBlocking {
        assertNull(prefs.observeToken().first())
    }

    @Test
    fun `saveToken 后 observeToken 反映新值`() = runBlocking {
        prefs.saveToken("bearer-abc-123")

        assertEquals("bearer-abc-123", prefs.observeToken().first())
    }

    @Test
    fun `saveToken 多次后 observeToken 反映最新值`() = runBlocking {
        prefs.saveToken("first")
        assertEquals("first", prefs.observeToken().first())

        prefs.saveToken("second")
        assertEquals("second", prefs.observeToken().first())
    }

    @Test
    fun `clearToken 后 observeToken 回到 null`() = runBlocking {
        prefs.saveToken("will-be-cleared")
        assertEquals("will-be-cleared", prefs.observeToken().first())

        prefs.clearToken()
        assertNull(prefs.observeToken().first())
    }

    @Test
    fun `saveToken null 等价于 clearToken`() = runBlocking {
        prefs.saveToken("then-null")
        assertEquals("then-null", prefs.observeToken().first())

        prefs.saveToken(null)
        assertNull(prefs.observeToken().first())
    }

    @Test
    fun `observeToken 首帧即返回当前持久化值`() = runBlocking {
        // 先写入
        prefs.saveToken("persisted")

        // 新建 EncryptedPrefsImpl 实例模拟"应用重启"后从加密盘恢复
        val restartedPrefs = EncryptedPrefsImpl(context)
        assertEquals("persisted", restartedPrefs.observeToken().first())
    }
}
