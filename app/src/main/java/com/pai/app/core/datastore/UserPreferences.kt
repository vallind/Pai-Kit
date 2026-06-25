// ============================================================================
// UserPreferences.kt
// 登录态相关偏好读写（决策 4：token 委托给 EncryptedPrefs）
// ============================================================================

package com.pai.app.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户偏好
 *
 * 决策 4：token 不再以明文存于 Preferences DataStore，改由 [EncryptedPrefs]
 * （EncryptedSharedPreferences，AES256-GCM）持久化。本类只保留 userId。
 *
 * 管理登录态信息：
 * - [loggedInUserId]：当前登录用户 ID（未登录为 null）
 * - [isLoggedIn]：是否已登录（基于 [loggedInUserId] 派生）
 * - token：通过 [EncryptedPrefs] 读写（HeaderInterceptor / TokenAuthenticator 直接注入）
 *
 * 注意：本类必须保持 public 可见性（由 Hilt 直接 @Inject constructor 构造，
 * PreferencesModule 已移除冗余 @Provides —— Low #22）。
 *
 * @param context Application Context
 * @param encryptedPrefs 加密 token 存储（由 Hilt 注入）
 */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context,
    private val encryptedPrefs: EncryptedPrefs,
) {

    private val store = context.userDataStore

    /** 当前登录用户 ID，未登录返回 null */
    internal val loggedInUserId: Flow<Long?> = store.data.map { prefs ->
        prefs[KEY_USER_ID]?.toLongOrNull()
    }

    /** 是否已登录（基于 [loggedInUserId] 是否为空派生） */
    internal val isLoggedIn: Flow<Boolean> = loggedInUserId.map { it != null }

    /**
     * 保存登录态
     *
     * - userId 写入 Preferences DataStore（非敏感）
     * - token 委托给 [EncryptedPrefs] 持久化（加密）
     *
     * @param userId 用户 ID
     * @param token 登录令牌
     */
    internal suspend fun saveLogin(userId: Long, token: String) {
        store.edit { prefs ->
            prefs[KEY_USER_ID] = userId.toString()
        }
        encryptedPrefs.saveToken(token)
    }

    /**
     * 清除登录态（退出登录时调用）
     *
     * - 移除 DataStore 中的 userId
     * - 调 [EncryptedPrefs.clearToken] 清空加密 token
     */
    internal suspend fun clearLogin() {
        store.edit { prefs ->
            prefs.remove(KEY_USER_ID)
        }
        encryptedPrefs.clearToken()
    }

    private companion object {
        private val KEY_USER_ID = stringPreferencesKey("user_id")
    }
}
