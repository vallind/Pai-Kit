// ============================================================================
// InMemoryEncryptedPrefs.kt
// 内存 Token 存储（测试实现）
// 不依赖 Android KeyStore / EncryptedSharedPreferences，纯内存 StateFlow
// ============================================================================

package com.pai.app.core.datastore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 内存 Token 存储（测试实现）
 *
 * 不依赖 Android KeyStore / EncryptedSharedPreferences，使用纯内存 [MutableStateFlow]
 * 存储 token。用于依赖 [EncryptedPrefs] 的单元测试，避免 Robolectric / 真机依赖。
 *
 * 用法（单元测试）：
 * ```kotlin
 * class HeaderInterceptorTest {
 *     private val encryptedPrefs = InMemoryEncryptedPrefs()
 *     private val interceptor = HeaderInterceptor(encryptedPrefs, ...)
 *
 *     @Test
 *     fun `登录后 Authorization 头注入`() = runTest {
 *         encryptedPrefs.saveToken("my-token")
 *         // assert header 携带 Bearer my-token
 *     }
 * }
 * ```
 *
 * 用法（Hilt UI 测试替换）：
 * ```kotlin
 * @UninstallModules(EncryptedPrefsModule::class)
 * @TestInstallIn(
 *     components = [SingletonComponent::class],
 *     replaces = [EncryptedPrefsModule::class],
 * )
 * @Module
 * object TestEncryptedPrefsModule {
 *     @Provides @Singleton
 *     fun provideEncryptedPrefs(): EncryptedPrefs = InMemoryEncryptedPrefs()
 * }
 * ```
 *
 * 注意：
 * - token 仅存于内存，进程重启后丢失（与生产实现 EncryptedSharedPreferences 不同）
 * - 适合 ViewModel / Interceptor / Authenticator 单元测试
 * - 不适合测试「跨进程持久化」场景（请用 [EncryptedPrefsImpl] + Robolectric）
 */
class InMemoryEncryptedPrefs : EncryptedPrefs {

    private val _token = MutableStateFlow<String?>(null)

    /**
     * 观察当前 token（内存 StateFlow，首帧为 null 或最近写入值）
     */
    override fun observeToken(): Flow<String?> = _token.asStateFlow()

    /**
     * 保存 token（更新内存 StateFlow）
     *
     * @param token 新 token；传 null 等价于 [clearToken]
     */
    override suspend fun saveToken(token: String?) {
        _token.value = token
    }

    /**
     * 清空 token
     */
    override suspend fun clearToken() {
        _token.value = null
    }
}
