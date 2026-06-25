// ============================================================================
// TokenAuthenticator.kt
// OkHttp Authenticator：401 时 emit TokenExpired + 清登录态（决策 2 + 决策 4）
// ============================================================================

package com.pai.app.core.network

import com.pai.app.core.datastore.EncryptedPrefs
import com.pai.app.core.util.AppEvent
import com.pai.app.core.util.AppEventBus
import com.pai.app.navigation.UserState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Token 失效处理器
 *
 * 决策 2 + 决策 4：服务端返回 401（Token 过期/无效）时触发：
 * 1. 若已重试 ≥ 2 次，放弃（避免无限循环）
 * 2. 读取当前 token；若为空说明已登出，直接放弃
 * 3. emit [AppEvent.TokenExpired]，由 MainActivity AppNavGraph 统一跳转 AuthRoute
 * 4. 调 [UserState.logout] 清空内存登录态 + EncryptedPrefs token + DataStore userId
 * 5. 返回 null（不重试），让请求以 401 失败上抛，由业务 Repository 包装为 ApiResult.Error
 *
 * 注意：[Authenticator.authenticate] 是 OkHttp 同步回调，内部只能用 [runBlocking]
 * 读一次性 token / 触发登出。Authenticator 仅在 401 时触发（非每请求），开销可接受。
 *
 * @param encryptedPrefs 加密 token 存储
 * @param userState 全局用户状态（用于 logout）
 * @param appEventBus 全局事件总线（用于 emit TokenExpired）
 */
@Singleton
class TokenAuthenticator @Inject constructor(
    private val encryptedPrefs: EncryptedPrefs,
    private val userState: UserState,
    private val appEventBus: AppEventBus,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 1. 防止无限重试：响应链上 401 累计 ≥ 2 次时放弃
        if (response.responseCount() >= MAX_RETRY) {
            Timber.w("TokenAuthenticator: 401 retry exhausted, give up")
            return null
        }

        // 2. 当前 token 为空说明已登出，无需重试 / emit
        val currentToken = runBlocking { encryptedPrefs.observeToken().first() }
        if (currentToken.isNullOrBlank()) {
            Timber.w("TokenAuthenticator: 401 with no cached token, skip")
            return null
        }

        // 3. emit TokenExpired → MainActivity 跳 AuthRoute
        appEventBus.emit(AppEvent.TokenExpired)

        // 4. 清登录态（内存 + EncryptedPrefs + DataStore）
        runBlocking { userState.logout() }

        // 5. 不重试：让请求以 401 失败上抛，避免在登出过程中再发请求
        return null
    }

    private companion object {
        const val MAX_RETRY = 2
    }

    /**
     * 计算 Response 链路上的 401 次数
     *
     * OkHttp 在 Authenticator 返回非 null 时会用新 Request 重试，并把 priorResponse
     * 链接进来；通过递归 priorResponse 计数即可知道当前是第几次 401。
     */
    private fun Response.responseCount(): Int {
        var count = 1
        var prior = priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
