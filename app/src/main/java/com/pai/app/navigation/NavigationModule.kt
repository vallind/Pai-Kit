// ============================================================================
// NavigationModule.kt
// Hilt 模块 - 提供 ApplicationScope / UserState 依赖
// ============================================================================

package com.pai.app.navigation

import com.pai.app.core.datastore.UserPreferences
import com.pai.app.core.util.ApplicationScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

/**
 * 导航依赖注入模块
 *
 * 提供：
 * - [@ApplicationScope][ApplicationScope] CoroutineScope - 应用级协程作用域
 * - [UserState] - 全局用户状态单例（注入 appScope + userPreferences）
 *
 * 注意：
 * - [AppNavigator] 现为无参 `@Inject constructor`，Hilt 直接构造，本模块不再 @Provides
 * - AppNavigator 在 MainActivity 中通过 [AppNavigator.bind] 绑定 NavController
 * - UserState.initialize() 应在 Application.onCreate 中调用以恢复登录态
 * - ViewModel 注入 AppNavigator / UserState 后可直接调用对应方法
 */
@Module
@InstallIn(SingletonComponent::class)
object NavigationModule {

    /**
     * 提供应用级 CoroutineScope
     *
     * 使用 SupervisorJob：子协程异常不会导致父协程取消（避免一个 emit 失败影响全局）
     * 使用 Dispatchers.Default：适合非 UI 后台任务（DataStore 读写、Flow emit）
     */
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /**
     * 提供 [UserState] 单例
     *
     * @param scope 应用级协程作用域（用于 [UserState.initialize] 中的 DataStore 读取 + isLoggedIn/userId 的 stateIn 订阅）
     * @param userPreferences 用户偏好（DataStore，持久化登录态）
     */
    @Provides
    @Singleton
    fun provideUserState(
        @ApplicationScope scope: CoroutineScope,
        userPreferences: UserPreferences,
    ): UserState = UserState(scope, userPreferences)
}
