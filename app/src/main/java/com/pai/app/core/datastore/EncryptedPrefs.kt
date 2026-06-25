// ============================================================================
// EncryptedPrefs.kt
// 安全 Token 存储接口（决策 4）
// 将加密 token 持久化与内存态 StateFlow 抽象为接口，便于测试替换
// ============================================================================

package com.pai.app.core.datastore

import kotlinx.coroutines.flow.Flow

/**
 * 加密 Token 存储接口
 *
 * 决策 4：将 Bearer token 加密持久化，替代旧 UserPreferences.token 明文存储（High #6）。
 *
 * 接口定义：
 * - [saveToken] - 持久化 token（加密写入）
 * - [observeToken] - 观察 token 变化（Flow，首帧即当前值）
 * - [clearToken] - 清空 token
 *
 * 生产实现：[EncryptedPrefsImpl]（EncryptedSharedPreferences + AES256-GCM）
 * 测试实现：[com.pai.app.core.datastore.InMemoryEncryptedPrefs]（内存 StateFlow，无 KeyStore 依赖）
 *
 * 被 HeaderInterceptor / TokenAuthenticator / UserPreferences 注入使用。
 */
interface EncryptedPrefs {

    /**
     * 观察当前 token（lifecycle-safe）
     *
     * 首帧即返回当前持久化值（无延迟），后续写入时自动 emit 新值。
     */
    fun observeToken(): Flow<String?>

    /**
     * 保存 token（加密持久化 + 更新内存 StateFlow）
     *
     * @param token 新 token；传 null 等价于 [clearToken]
     */
    suspend fun saveToken(token: String?)

    /**
     * 清空 token（退出登录时调用）
     */
    suspend fun clearToken()
}
