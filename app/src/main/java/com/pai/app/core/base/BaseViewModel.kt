// ============================================================================
// BaseViewModel.kt
// 所有 Feature ViewModel 的基类 - 借鉴 AndroidProject-Compose
// 内置：类型安全跳转 + UserState 状态机
// ============================================================================

package com.pai.app.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pai.app.navigation.AppNavigator
import com.pai.app.navigation.UserState
import com.pai.app.navigation.routes.AppRoute
import kotlinx.coroutines.launch

/**
 * 应用 ViewModel 基类
 *
 * 所有 Feature ViewModel 应继承本类，获得：
 * 1. **类型安全的路由跳转**：[navigate] / [navigateAndCloseCurrent]（接收 [AppRoute]）
 *    —— 内部统一调 [AppNavigator.navigate]，使 [com.pai.app.navigation.AppNavigator.DefaultRouteInterceptor]
 *    生效（决策 7）
 * 2. **返回控制**：[navigateBack] / [navigateBackTo]
 * 3. **全局登录状态访问**：[userState] / [isLoggedIn]
 *
 * 结果回传（决策 3）已从本类移除：
 * 业务方应直接使用 `AppNavigator.setResult(key, value)` /
 * `AppNavigator.observeResult(key): Flow<Any?>` —— 基于 NavBackStackEntry 的
 * savedStateHandle 实现，lifecycle-safe，无需 SharedFlow 中转。
 *
 * 使用示例：
 * ```kotlin
 * @HiltViewModel
 * class PostDetailViewModel @Inject constructor(
 *     navigator: AppNavigator,
 *     userState: UserState,
 *     private val postRepository: PostRepository,
 * ) : BaseViewModel(navigator, userState) {
 *
 *     fun openComment(postId: Long) = navigate(CommentRoute(postId))
 * }
 * ```
 *
 * @param navigator 全局导航器，由 Hilt 注入
 * @param userState 全局用户状态，由 Hilt 注入
 */
abstract class BaseViewModel(
    protected val navigator: AppNavigator,
    protected val userState: UserState,
) : ViewModel() {

    // ------------------------------------------------------------------------
    // 导航（统一走 AppNavigator，使 RouteInterceptor 生效）
    // ------------------------------------------------------------------------

    /**
     * 跳转到目标路由
     *
     * - 自动执行登录拦截（受保护路由未登录时由 [com.pai.app.navigation.AppNavigator.DefaultRouteInterceptor] 重定向）
     * - 运行在 viewModelScope，无需业务代码手动 launch
     *
     * @param route 目标路由（@Serializable AppRoute，如 [com.pai.app.navigation.routes.UserRoute]）
     */
    fun navigate(route: AppRoute) {
        viewModelScope.launch { navigator.navigate(route) }
    }

    /**
     * 跳转并关闭当前页
     *
     * @param route 目标路由
     * @param currentRoute 当前页路由（用于 popUpTo）
     */
    fun navigateAndCloseCurrent(route: AppRoute, currentRoute: AppRoute) {
        viewModelScope.launch { navigator.navigateAndCloseCurrent(route, currentRoute) }
    }

    // ------------------------------------------------------------------------
    // 返回
    // ------------------------------------------------------------------------

    /** 返回上一页 */
    fun navigateBack() {
        viewModelScope.launch { navigator.navigateBack() }
    }

    /**
     * 返回到指定路由
     *
     * @param route 目标路由
     * @param inclusive true 时连同目标一起关闭（默认 false）
     */
    fun navigateBackTo(route: AppRoute, inclusive: Boolean = false) {
        viewModelScope.launch { navigator.navigateBackTo(route, inclusive) }
    }

    // ------------------------------------------------------------------------
    // 登录状态（决策 5：单一真相源为 UserState）
    // ------------------------------------------------------------------------

    /**
     * 当前是否已登录
     *
     * 决策 5：UserState.isLoggedIn 是登录态的唯一真相源（直接派生自
     * `userPreferences.isLoggedIn`），本属性仅作便捷读取入口，
     * `.value` 在协程外读取可能略滞后；如需响应式，请直接订阅
     * `userState.isLoggedIn: StateFlow<Boolean>`。
     */
    val isLoggedIn: Boolean
        get() = userState.isLoggedIn.value

    /**
     * 退出登录
     *
     * 委托给 [UserState.logout]：清空 DataStore + EncryptedPrefs + 重置内存状态。
     * 子类可重写以加入清理逻辑（如清缓存、重置 StateFlow）后再调用 super.logout()。
     */
    open fun logout() {
        viewModelScope.launch { userState.logout() }
    }
}
