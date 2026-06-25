// ============================================================================
// AppEventBus.kt
// 全局事件总线：基于 SharedFlow 的轻量事件分发（决策 2 精简版）
// ============================================================================

package com.pai.app.core.util

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 全局应用事件（决策 2：精简到 2 个事件）
 *
 * - [TokenExpired]：401 / Token 失效，由 [com.pai.app.core.network.TokenAuthenticator]
 *   发射，触发 AppNavGraph 全局跳转 AuthRoute + 清登录态
 * - [GlobalError]：未捕获异常 / 业务错误，由 BaseViewModel 发射，触发 Snackbar 提示
 *
 * 已删除事件（决策 2）：LoginSuccess / Logout / NavigateToHome / NavigateToLogin /
 * NavigateToProfile / NavigateToDetail / ThemeChanged / Custom ——
 * 登录/登出导航继续走 Composable 回调（现有可用模式），不双轨；
 * 其他导航事件应通过类型安全路由 + RouteInterceptor 处理。
 */
internal sealed class AppEvent {

    /** Token 过期，需引导用户重新登录 */
    data object TokenExpired : AppEvent()

    /**
     * 全局错误消息（用于 Snackbar / Toast）
     *
     * @param message 错误文案
     * @param throwable 可选异常对象，便于上层打印堆栈
     */
    data class GlobalError(
        val message: String,
        val throwable: Throwable? = null,
    ) : AppEvent()
}

/**
 * 全局事件总线
 *
 * 基于 [MutableSharedFlow] 实现：
 * - `replay = 0`：不重放历史事件，新订阅者只收新事件
 * - `extraBufferCapacity = 16`：缓存最多 16 个事件，应对短时高并发
 * - `onBufferOverflow = DROP_OLDEST`：缓冲溢出时丢弃最旧事件，保证新事件优先
 *
 * 使用示例：
 * ```kotlin
 * class MainActivity : ComponentActivity() {
 *     @Inject lateinit var bus: AppEventBus
 *
 *     override fun onCreate(...) {
 *         lifecycleScope.launch {
 *             bus.events.collect { event ->
 *                 when (event) {
 *                     is AppEvent.TokenExpired -> appNavigator.gotoAuth()
 *                     is AppEvent.GlobalError -> showSnackbar(event.message)
 *                 }
 *             }
 *         }
 *     }
 * }
 * ```
 *
 * 注意（Medium #14）：[emit] 会检查 [MutableSharedFlow.tryEmit] 返回值，
 * 当 DROP_OLDEST 策略实际丢弃事件时通过 [Timber.w] 输出告警，便于排查。
 *
 * 此类必须保持 public 可见性（Hilt `@Singleton` + `@Inject constructor`），
 * 但暴露 internal 类型 [AppEvent] 的成员标记为 `internal`。
 */
@Singleton
class AppEventBus @Inject constructor() {

    private val _events = MutableSharedFlow<AppEvent>(
        replay = 0,
        extraBufferCapacity = DEFAULT_BUFFER_CAPACITY,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /** 全局事件流，外部仅可订阅不可发射 */
    internal val events: SharedFlow<AppEvent> = _events.asSharedFlow()

    /**
     * 非挂起方式发射事件
     *
     * 在缓冲未满时立即返回 `true`；缓冲满时按 [BufferOverflow.DROP_OLDEST]
     * 策略丢弃最旧事件后返回 `true`。
     *
     * Medium #14：检查 [MutableSharedFlow.tryEmit] 返回值，丢弃时通过 [Timber.w] 告警。
     *
     * @return 是否成功投递（在 DROP 策略下通常为 `true`）
     */
    internal fun emit(event: AppEvent): Boolean {
        val delivered = _events.tryEmit(event)
        if (!delivered) {
            // 理论上 DROP_OLDEST 永远返回 true；此处兜底防御性日志
            Timber.w("AppEventBus emit dropped: $event")
        }
        return delivered
    }

    /**
     * 挂起方式发射事件
     *
     * 当无订阅者且缓冲已满时挂起等待，确保事件被消费。
     */
    internal suspend fun emitSuspending(event: AppEvent) {
        _events.emit(event)
    }

    private companion object {
        const val DEFAULT_BUFFER_CAPACITY = 16
    }
}
