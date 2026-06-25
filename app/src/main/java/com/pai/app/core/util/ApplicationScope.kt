// ============================================================================
// ApplicationScope.kt
// Hilt 限定符 - 标记应用级 CoroutineScope
// 借鉴 AndroidProject-Compose：用于在 Singleton 中安全启动协程
// （如 AppNavigator 的结果回传 emit、UserState 的 DataStore 初始化）
// ============================================================================

package com.pai.app.core.util

import javax.inject.Qualifier

/**
 * 标记应用级 [kotlinx.coroutines.CoroutineScope] 的 Hilt 限定符
 *
 * 该 Scope 由 [com.pai.app.navigation.NavigationModule] 提供，
 * 内部使用 `SupervisorJob() + Dispatchers.Default`，
 * 生命周期与应用进程一致，子协程异常不会导致父协程取消。
 *
 * 使用场景：
 * - [com.pai.app.navigation.UserState] 在 [initialize] 中从 DataStore
 *   恢复登录态（Application.onCreate 调用，无 ViewModel scope 可用）
 * - [com.pai.app.core.network.TokenAuthenticator] 在 401 时 emit
 *   `AppEvent.TokenExpired`（OkHttp 同步回调中，需应用级 scope）
 * - [com.pai.app.core.network.HeaderInterceptor] 在 init 中后台订阅
 *   `EncryptedPrefs.observeToken()` 缓存到 AtomicReference
 *
 * 使用示例：
 * ```kotlin
 * @Inject
 * class MySingleton(
 *     @ApplicationScope private val appScope: CoroutineScope,
 * ) {
 *     fun doSomething() {
 *         appScope.launch { /* ... */ }
 *     }
 * }
 * ```
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
