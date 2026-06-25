// ============================================================================
// PaiApplication.kt
// 应用入口：@HiltAndroidApp + Timber 日志初始化 + UserState 恢复登录态
// ============================================================================
package com.pai.app

import android.app.Application
import com.pai.app.navigation.UserState
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

/**
 * PaiApplication - 应用入口
 *
 * 职责：
 * 1. 通过 [HiltAndroidApp] 触发 Hilt 代码生成，生成全局 SingletonComponent
 *    并完成所有 @Module（NetworkModule / DatabaseModule / PreferencesModule /
 *    NavigationModule）的依赖装配
 * 2. 在 DEBUG 构建下植入 [Timber.DebugTree]，统一日志输出到 Logcat
 * 3. 调用 [UserState.initialize] 异步从 DataStore 恢复登录态到内存
 *    （使 RouteInterceptor 在首次导航前能正确判断登录状态）
 *
 * 决策 6（StartDestination 竞态修复）：
 * `userState.initialize()` 内部用 `CompletableDeferred` 标记完成（Feature agent 实现
 * `val isInitialized: StateFlow<Boolean>`），MainActivity setContent 内通过
 * `produceState` await `userState.isInitialized.first()` 完成后再决定 startDestination
 * （`if (userState.isLoggedIn.value) HomeRoute else AuthRoute`）。在完成前显示
 * splash（DSFullScreenLoading 或简单 Box），避免已登录用户冷启动落到 AuthScreen。
 *
 * 可见性说明：
 * 本类必须保持 public（@HiltAndroidApp 生成的 Hilt_PaiApplication 需要可见性）。
 */
@HiltAndroidApp
class PaiApplication : Application() {

    /** 全局用户状态，由 Hilt 注入单例 */
    @Inject
    lateinit var userState: UserState

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
        Timber.i("PaiApplication initialized")

        // 异步从 DataStore 恢复登录态到内存；
        // MainActivity 会 await userState.isInitialized 后再决定 startDestination（决策 6）
        userState.initialize()
    }
}
