// ============================================================================
// AppNavigator.kt
// 统一 NavController 封装 - 借鉴 AndroidProject-Compose
// 提供 ViewModel 友好的导航 API + 全局登录拦截 + savedStateHandle 结果回传
// 关键升级（决策 5/6/7/3）：
// 1. navigate 接收 AppRoute（@Serializable 类型安全路由）而非 String
// 2. 结果回传改为基于 NavBackStackEntry.savedStateHandle（lifecycle-safe）
// 3. UserState 改为单一真相源：isLoggedIn 直接派生自 userPreferences.isLoggedIn
// 4. UserState 新增 isInitialized StateFlow，供 MainActivity await 后再决定 startDestination
// ============================================================================

package com.pai.app.navigation

import androidx.navigation.NavHostController
import com.pai.app.core.datastore.UserPreferences
import com.pai.app.core.util.ApplicationScope
import com.pai.app.navigation.routes.AppRoute
import com.pai.app.navigation.routes.AuthRoute
import com.pai.app.navigation.routes.HomeRoute
import com.pai.app.navigation.routes.SettingsRoute
import com.pai.app.navigation.routes.UserRoute
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.reflect.KClass

/**
 * 全局用户状态 - 应用级登录状态机（决策 5：单一真相源）
 *
 * 关键设计：
 * 1. **单一真相源**：[isLoggedIn] / [userId] 直接派生自 [UserPreferences] 的 Flow，
 *    不再维护独立的内存 MutableStateFlow。所有登录态读写都经 DataStore，
 *    消除"内存态 vs 持久态"分歧风险。
 * 2. **持久化恢复**：[initialize] 在 Application.onCreate 中调用一次，
 *    读一次 DataStore 用于 warm up Flow 的首帧发射（避免 DataStore 异步加载期间
 *    StateFlow 用初始值 false 误判），完成后置 [isInitialized] = true。
 * 3. **登录成功同步**：[onLoginSuccess] 仅调 [UserPreferences.saveLogin]，
 *    DataStore 写入后 Flow 自动 emit 新值，[isLoggedIn] StateFlow 自动更新。
 * 4. **退出登录清理**：[logout] 仅调 [UserPreferences.clearLogin]，
 *    同样通过 Flow 自动 propagate。
 *
 * 持有 [ApplicationScope] 用于在 Application.onCreate 上下文中安全启动协程读取 DataStore。
 *
 * 决策 6：[isInitialized] 让 MainActivity 可以 await 初始化完成再决定 startDestination，
 * 避免已登录用户冷启动落到 AuthScreen 的竞态。
 *
 * @param appScope 应用级协程作用域（[@ApplicationScope][com.pai.app.core.util.ApplicationScope] 注入）
 * @param userPreferences 用户偏好（DataStore，持久化登录态 + token 委托 EncryptedPrefs）
 */
@Singleton
class UserState @Inject constructor(
    @ApplicationScope private val appScope: CoroutineScope,
    private val userPreferences: UserPreferences,
) {

    /**
     * 是否已登录（决策 5：单一真相源）
     *
     * 直接派生自 [UserPreferences.isLoggedIn] Flow，Eagerly 订阅保证首帧立即可用。
     * 任何调用 [UserPreferences.saveLogin] / [UserPreferences.clearLogin] 的写入
     * 都会自动 propagate 到本 StateFlow。
     */
    val isLoggedIn: StateFlow<Boolean> = userPreferences.isLoggedIn
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = false,
        )

    /**
     * 当前登录用户 ID（未登录为 null）
     *
     * 同样直接派生自 [UserPreferences.loggedInUserId]。
     */
    val userId: StateFlow<Long?> = userPreferences.loggedInUserId
        .stateIn(
            scope = appScope,
            started = SharingStarted.Eagerly,
            initialValue = null,
        )

    /**
     * UserState 是否已完成首帧 DataStore 恢复（决策 6）
     *
     * - `false`：[initialize] 尚未调用或尚未完成（StateFlow 仍用初始值）
     * - `true`：[initialize] 已读完一次 DataStore，[isLoggedIn] / [userId] 的首帧真实值已就绪
     *
     * MainActivity 应 await 本 StateFlow 变为 `true` 后再用 [isLoggedIn] 决策 startDestination，
     * 否则可能在 DataStore 异步加载期间误判为未登录。
     */
    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized.asStateFlow()

    /**
     * 从 DataStore 恢复登录状态
     *
     * 应在 `Application.onCreate()` 中调用一次。异步执行（不阻塞 Application 启动）：
     * 1. 读一次 [UserPreferences.isLoggedIn] 的首帧值（触发 DataStore 加载）
     * 2. 标记 [isInitialized] = true，使 MainActivity 可以决策 startDestination
     *
     * 由于 [isLoggedIn] / [userId] 已是 Eagerly 订阅的 StateFlow，本方法主要作用是
     * "确保 DataStore 完成首次加载后再标记 isInitialized"，避免 MainActivity 在
     * StateFlow 仍是初始值 false 时就误判为未登录。
     */
    fun initialize() {
        appScope.launch {
            // 等待 isLoggedIn Flow 的首帧真实值（DataStore 已加载完成）
            userPreferences.isLoggedIn.first()
            _isInitialized.value = true
        }
    }

    /**
     * 登录成功后调用
     *
     * 仅调 [UserPreferences.saveLogin] 写入 userId（DataStore）+ token（EncryptedPrefs）。
     * 写入完成后 DataStore Flow 自动 emit 新值，[isLoggedIn] StateFlow 自动更新为 true。
     *
     * @param userId 用户 ID
     * @param token 登录令牌（由 [UserPreferences] 委托 [com.pai.app.core.datastore.EncryptedPrefs] 加密存储）
     */
    suspend fun onLoginSuccess(userId: Long, token: String) {
        userPreferences.saveLogin(userId, token)
    }

    /**
     * 退出登录
     *
     * 仅调 [UserPreferences.clearLogin] 清除 userId（DataStore）+ token（EncryptedPrefs）。
     * 清除后 DataStore Flow 自动 emit false / null，[isLoggedIn] / [userId] 自动更新。
     */
    suspend fun logout() {
        userPreferences.clearLogin()
    }
}

/**
 * 路由拦截器 - 判断目标路由是否需要登录 / 重定向
 *
 * 自定义拦截逻辑示例：
 * ```kotlin
 * class MyRouteInterceptor(
 *     private val userState: UserState,
 * ) : RouteInterceptor {
 *     override fun intercept(route: AppRoute): AppRoute? {
 *         if (route is UserRoute && !userState.isLoggedIn.value) return AuthRoute
 *         return null
 *     }
 * }
 * ```
 */
interface RouteInterceptor {
    /**
     * 拦截目标路由
     *
     * @param route 原始目标路由（@Serializable AppRoute）
     * @return null 表示放行；非 null 表示重定向到该 AppRoute
     */
    fun intercept(route: AppRoute): AppRoute?
}

/**
 * 默认拦截器实现（决策 7）
 *
 * 默认规则（防循环）：
 * - 已登录访问 [AuthRoute]，重定向到 [HomeRoute]（避免重复登录）
 *   → 重定向目标 HomeRoute 不在 protectedRoutes 中且非 AuthRoute，再次拦截时返回 null，无循环
 * - 未登录访问受保护路由（[UserRoute] / [SettingsRoute]），重定向到 [AuthRoute]
 *   → 重定向目标 AuthRoute 不在 protectedRoutes 中，且未登录时不会触发"已登录访问 AuthRoute"分支，无循环
 *
 * @param userState 全局登录状态
 */
class DefaultRouteInterceptor(
    private val userState: UserState,
) : RouteInterceptor {

    /** 受保护路由列表（需登录才能访问） */
    private val protectedRoutes: Set<KClass<out AppRoute>> = setOf(
        UserRoute::class,
        SettingsRoute::class,
    )

    override fun intercept(route: AppRoute): AppRoute? {
        // 已登录访问 AUTH，自动跳到 HOME（避免重复登录）
        if (route is AuthRoute && userState.isLoggedIn.value) {
            return HomeRoute
        }
        // 未登录访问受保护路由，重定向到 AUTH
        if (route::class in protectedRoutes && !userState.isLoggedIn.value) {
            return AuthRoute
        }
        return null
    }
}

/**
 * 全局导航器 - 封装 NavController，对 ViewModel 友好
 *
 * 核心能力：
 * 1. **类型安全跳转**：[navigate] / [navigateAndCloseCurrent] 接收 [AppRoute]
 * 2. **全局登录拦截**：[RouteInterceptor]（默认 [DefaultRouteInterceptor]）
 *    —— [navigate] 内部会调用 [RouteInterceptor.intercept]，受保护路由未登录时自动重定向
 * 3. **结果回传（决策 3）**：基于 NavBackStackEntry.savedStateHandle 的 [setResult] /
 *    [observeResult]，lifecycle-safe，替代旧的 SharedFlow 方案
 * 4. **返回控制**：[navigateBack] / [navigateBackTo]
 *
 * 使用方式：
 * ```kotlin
 * @HiltViewModel
 * class MyViewModel @Inject constructor(
 *     navigator: AppNavigator,
 *     userState: UserState,
 * ) : BaseViewModel(navigator, userState) {
 *     fun openUser() = navigate(UserRoute)
 * }
 * ```
 *
 * 注意：本类为单例，NavController 实例由 MainActivity 持有并通过 [bind] 注入。
 * ViewModel 不直接持有 NavController，避免内存泄漏。
 */
@Singleton
class AppNavigator @Inject constructor() {

    /** 当前绑定的 NavController（由 MainActivity 在 setContent 时绑定） */
    @Volatile
    private var navController: NavHostController? = null

    /** 路由拦截器，默认使用 [DefaultRouteInterceptor] */
    @Volatile
    private var interceptor: RouteInterceptor? = null

    /**
     * 绑定 NavController（由 MainActivity 调用）
     *
     * @param controller MainActivity 持有的 NavHostController
     * @param userState 全局用户状态，用于初始化默认拦截器
     */
    fun bind(controller: NavHostController, userState: UserState) {
        navController = controller
        if (interceptor == null) {
            interceptor = DefaultRouteInterceptor(userState)
        }
    }

    /** 解绑 NavController（MainActivity onDestroy 时调用） */
    fun unbind() {
        navController = null
    }

    /** 设置自定义路由拦截器 */
    fun setInterceptor(interceptor: RouteInterceptor) {
        this.interceptor = interceptor
    }

    // ------------------------------------------------------------------------
    // 跳转
    // ------------------------------------------------------------------------

    /**
     * 跳转到目标路由
     *
     * - 自动执行登录拦截（[RouteInterceptor.intercept]）
     * - 路由必须是 @Serializable 的 [AppRoute] 子类（data object / data class）
     *
     * @param route 目标路由
     * @return true 表示跳转成功，false 表示 NavController 未绑定
     */
    fun navigate(route: AppRoute): Boolean {
        val controller = navController ?: return false
        val target = interceptor?.intercept(route) ?: route
        controller.navigate(target)
        return true
    }

    /**
     * 跳转并关闭当前页（类似 navigate + popUpTo(current, inclusive=true)）
     *
     * @param route 目标路由
     * @param currentRoute 当前页路由（用于 popUpTo）
     */
    fun navigateAndCloseCurrent(route: AppRoute, currentRoute: AppRoute): Boolean {
        val controller = navController ?: return false
        val target = interceptor?.intercept(route) ?: route
        controller.navigate(target) {
            popUpTo(currentRoute::class) { inclusive = true }
        }
        return true
    }

    /**
     * 跳转到目标路由并清空回退栈直达起始目的地
     *
     * 用于"登录后清栈" / "退出登录后清栈"语义：跳转到 [route] 的同时把
     * startDestination 及其上所有页面全部 pop，避免返回键再次回到旧页面。
     *
     * @param route 目标路由
     * @return true 表示跳转成功，false 表示 NavController 未绑定
     */
    fun navigateAndClearStack(route: AppRoute): Boolean {
        val controller = navController ?: return false
        val target = interceptor?.intercept(route) ?: route
        controller.navigate(target) {
            popUpTo(controller.graph.startDestinationId) { inclusive = true }
        }
        return true
    }

    // ------------------------------------------------------------------------
    // 返回
    // ------------------------------------------------------------------------

    /** 返回上一页 */
    fun navigateBack(): Boolean {
        val controller = navController ?: return false
        return controller.popBackStack()
    }

    /**
     * 返回到指定路由
     *
     * @param route 目标路由
     * @param inclusive true 时连同目标路由一起关闭（默认 false）
     */
    fun navigateBackTo(route: AppRoute, inclusive: Boolean = false): Boolean {
        val controller = navController ?: return false
        return controller.popBackStack(route::class, inclusive)
    }

    // ------------------------------------------------------------------------
    // 结果回传（决策 3：基于 NavBackStackEntry.savedStateHandle）
    // ------------------------------------------------------------------------

    /**
     * 由子页面调用：把结果写入"将要返回到的"页面（previousBackStackEntry）的 savedStateHandle
     *
     * 调用时机：子页面 B 在 popBackStack 之前调用，结果会写入 A 的 savedStateHandle；
     * A 重新成为 currentBackStackEntry 后，通过 [observeResult] 即可读到该值。
     *
     * lifecycle-safe：savedStateHandle 与 NavBackStackEntry 绑定，随进程死亡/重建保留。
     *
     * @param key 结果 Key（建议用 `companion object { const val KEY = "..." }` 定义）
     * @param value 结果值（任意可序列化对象，savedStateHandle 支持）
     */
    fun setResult(key: String, value: Any?) {
        navController?.previousBackStackEntry?.savedStateHandle?.set(key, value)
    }

    /**
     * 由源页面调用：观察自己 currentBackStackEntry 的 savedStateHandle 上的结果
     *
     * 调用时机：源页面 A 在进入子页面 B 之前（或在 Composable 顶层）订阅本 Flow；
     * 当 B 通过 [setResult] 写入值并 popBackStack 回到 A 时，A 的 currentBackStackEntry
     * 的 savedStateHandle 拿到值，本 Flow emit。
     *
     * 注意：每次 A 重新成为 currentBackStackEntry（包括配置变更后）都会拿到当前 savedStateHandle
     * 中的最新值。如果需要"一次性消费"，订阅方在收到值后调用 `savedStateHandle.remove(key)`。
     *
     * @param key 结果 Key
     * @return Flow<T?> 发射保存的值；若 NavController 未绑定或 entry 不存在返回 emptyFlow
     */
    fun <T> observeResult(key: String): kotlinx.coroutines.flow.Flow<T?> {
        val entry = navController?.currentBackStackEntry
            ?: return kotlinx.coroutines.flow.emptyFlow()
        return entry.savedStateHandle.getStateFlow<T?>(key, null)
    }
}
