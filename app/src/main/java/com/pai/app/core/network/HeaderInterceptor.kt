// ============================================================================
// HeaderInterceptor.kt
// OkHttp 拦截器：统一注入 Content-Type / Accept / Authorization 头
// 决策 5：消除每请求 runBlocking，token 通过 AtomicReference 缓存
// ============================================================================

package com.pai.app.core.network

import com.pai.app.core.datastore.EncryptedPrefs
import com.pai.app.core.util.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 请求头拦截器
 *
 * 为所有出站请求统一添加：
 * - `Content-Type: application/json`
 * - `Accept: application/json`
 * - `Authorization: Bearer {token}` （若已登录）
 *
 * Token 来源：[EncryptedPrefs.observeToken]（决策 4：加密存储）。
 *
 * 决策 5（High #7 修复）：旧实现每次请求都 `runBlocking { userPreferences.token.first() }`，
 * 阻塞 OkHttp dispatcher 线程并放大首请求延迟。改为：
 * 1. `init {}` 中 `runBlocking` 一次性 warmup 缓存（仅构造时一次，可接受）
 * 2. `init {}` 中通过 [applicationScope] 启动后台协程订阅 [EncryptedPrefs.observeToken]，
 *    token 变化时自动同步到 [tokenRef]
 * 3. [intercept] 中只读 [tokenRef]（无锁 AtomicReference.get），零阻塞
 *
 * @param encryptedPrefs 加密 token 存储
 * @param applicationScope 应用级协程作用域（用于后台订阅 token 变化）
 */
@Singleton
class HeaderInterceptor @Inject constructor(
    encryptedPrefs: EncryptedPrefs,
    @ApplicationScope applicationScope: CoroutineScope,
) : Interceptor {

    /** 内存态 token 缓存，由后台协程维护 */
    private val tokenRef = AtomicReference<String?>(null)

    init {
        // Warmup：DI 图创建时一次性同步 token（仅构造一次，可接受 runBlocking）
        // 否则首请求可能拿到 null token，导致鉴权失败需走 TokenAuthenticator 重试
        tokenRef.set(runBlocking { encryptedPrefs.observeToken().first() })

        // 后台订阅 token 变化，登录/登出后自动更新缓存
        applicationScope.launch {
            encryptedPrefs.observeToken().collect { newToken ->
                tokenRef.set(newToken)
            }
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = tokenRef.get()

        val requestBuilder = chain.request().newBuilder()
            .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
            .header(HEADER_ACCEPT, CONTENT_TYPE_JSON)

        // 注入 Authorization 头（仅当 token 非空）
        token?.takeIf { it.isNotBlank() }?.let { tk ->
            requestBuilder.header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$tk")
        }

        return chain.proceed(requestBuilder.build())
    }

    private companion object {
        const val HEADER_CONTENT_TYPE = "Content-Type"
        const val HEADER_ACCEPT = "Accept"
        const val HEADER_AUTHORIZATION = "Authorization"
        const val CONTENT_TYPE_JSON = "application/json"
        const val BEARER_PREFIX = "Bearer "
    }
}
