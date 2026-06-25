// ============================================================================
// EncryptedPrefs.kt
// 安全 Token 存储（决策 4）
// 用 EncryptedSharedPreferences 加密持久化 Bearer token，
// 替代旧 UserPreferences.token 明文存储（High #6）
// ============================================================================

package com.pai.app.core.datastore

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 安全 Token 存储
 *
 * 决策 4：用 [EncryptedSharedPreferences]（AES256-GCM 主密钥 + AES256-SIV key 加密 +
 * AES256-GCM value 加密）持久化 Bearer token，避免明文落盘到 `user_prefs.preferences_pb`
 * 被设备 root 或备份提取。
 *
 * 配套：
 * - AndroidManifest `allowBackup=false`（Config agent 处理）
 * - HttpLoggingInterceptor `redactHeader("Authorization")`（NetworkModule 处理）
 * - [com.pai.app.core.network.TokenAuthenticator] 在 401 时调 [clearToken] + emit TokenExpired
 *
 * 注意：
 * - EncryptedSharedPreferences 首次创建涉及密钥派生（轻量 IO），故所有读写均切到 [Dispatchers.IO]
 * - [observeToken] 是内存 StateFlow 暴露，写入时同步更新，供 HeaderInterceptor / TokenAuthenticator
 *   无阻塞读取（决策 5）
 *
 * 可见性：本类需 public（Hilt 在 [UserPreferences] 的 @Inject constructor 中注入本类，
 * Hilt 要求 @Inject constructor 是 public，故构造参数类型必须 ≥ public）。
 *
 * @param context Application Context（由 Hilt 注入）
 */
@Singleton
class EncryptedPrefs @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
    )

    /** 内存态 token，初始为已持久化值；写入时同步更新 */
    private val _tokenFlow = MutableStateFlow(prefs.getString(KEY_TOKEN, null))

    /**
     * 观察当前 token（lifecycle-safe，可被 HeaderInterceptor / TokenAuthenticator 收集）
     *
     * 首帧即返回当前持久化值（无延迟），后续写入时自动 emit 新值。
     */
    fun observeToken(): Flow<String?> = _tokenFlow.asStateFlow()

    /**
     * 保存 token（写入加密盘 + 更新内存 StateFlow）
     *
     * @param token 新 token；传 null 等价于 [clearToken]
     */
    suspend fun saveToken(token: String?) {
        withContext(Dispatchers.IO) {
            prefs.edit().apply {
                if (token == null) {
                    remove(KEY_TOKEN)
                } else {
                    putString(KEY_TOKEN, token)
                }
            }.apply()
            _tokenFlow.value = token
        }
    }

    /**
     * 清空 token（退出登录时由 [com.pai.app.navigation.UserState.logout] 调用）
     */
    suspend fun clearToken() {
        saveToken(null)
    }

    /**
     * 同步读取当前 token（仅用于 HeaderInterceptor 启动期 warmup）
     *
     * 注意：EncryptedSharedPreferences 在首次访问后会缓存解密后的 SharedPreferences
     * 对象，后续同步读取是内存操作，开销可忽略；首次访问涉及解密，故推荐走 [observeToken]。
     */
    internal fun tokenSync(): String? = prefs.getString(KEY_TOKEN, null)

    private companion object {
        const val FILE_NAME = "encrypted_prefs"
        const val KEY_TOKEN = "auth_token"
    }
}
