// ============================================================================
// MainActivity.kt
// 应用唯一 Activity：Hilt 入口 + Compose 宿主 + 全局导航图
// 通用脚手架入口，业务方拉取后保留此结构
// ============================================================================
package com.pai.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pai.app.core.datastore.ThemeMode
import com.pai.app.core.designsystem.foundation.motion.DSPageTransitions
import com.pai.app.core.appstate.MainActivityViewModel
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSBrandColor
import com.pai.app.core.designsystem.foundation.tokens.DSFontSizeScale
import com.pai.app.core.util.AppEvent
import com.pai.app.core.util.AppEventBus
import com.pai.app.feature.auth.AuthScreen
import com.pai.app.feature.gallery.GalleryScreen
import com.pai.app.feature.gallery.pages.GalleryButtonPage
import com.pai.app.feature.gallery.pages.GalleryContainerPage
import com.pai.app.feature.gallery.pages.GalleryFeedbackPage
import com.pai.app.feature.gallery.pages.GalleryFormPage
import com.pai.app.feature.gallery.pages.GalleryLayoutPage
import com.pai.app.feature.gallery.pages.GalleryMotionPage
import com.pai.app.feature.gallery.pages.GalleryNavigationPage
import com.pai.app.feature.gallery.pages.GalleryTextPage
import com.pai.app.feature.gallery.pages.GalleryThemePage
import com.pai.app.feature.home.HomeScreen
import com.pai.app.feature.settings.SettingsScreen
import com.pai.app.feature.user.UserScreen
import com.pai.app.navigation.AppNavigator
import com.pai.app.navigation.UserState
import com.pai.app.navigation.extension.goBack
import com.pai.app.navigation.extension.gotoAuth
import com.pai.app.navigation.extension.gotoGallery
import com.pai.app.navigation.extension.gotoGalleryPage
import com.pai.app.navigation.extension.gotoHome
import com.pai.app.navigation.extension.gotoSettings
import com.pai.app.navigation.extension.gotoUser
import com.pai.app.navigation.routes.AppRoute
import com.pai.app.navigation.routes.AuthRoute
import com.pai.app.navigation.routes.GalleryButtonRoute
import com.pai.app.navigation.routes.GalleryContainerRoute
import com.pai.app.navigation.routes.GalleryFeedbackRoute
import com.pai.app.navigation.routes.GalleryFormRoute
import com.pai.app.navigation.routes.GalleryLayoutRoute
import com.pai.app.navigation.routes.GalleryMotionRoute
import com.pai.app.navigation.routes.GalleryNavigationRoute
import com.pai.app.navigation.routes.GalleryRoute
import com.pai.app.navigation.routes.GalleryTextRoute
import com.pai.app.navigation.routes.GalleryThemeRoute
import com.pai.app.navigation.routes.HomeRoute
import com.pai.app.navigation.routes.SettingsRoute
import com.pai.app.navigation.routes.UserRoute
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * MainActivity - 应用唯一 Activity
 *
 * 职责：
 * 1. 作为 Hilt 入口（[AndroidEntryPoint]），注入全局 [AppEventBus] / [AppNavigator] / [UserState]
 * 2. 收集主题状态（[MainActivityViewModel.themeMode] / [MainActivityViewModel.dynamicColor] /
 *    [MainActivityViewModel.brandColor] / [MainActivityViewModel.fontSizeScale]）
 *    并通过 [DSDesignTheme] 注入到整棵 Compose 树
 * 3. 绑定 [AppNavigator] 到 NavController（ViewModel 通过 AppNavigator 跳转）
 * 4. 决策 6：在 setContent 中 await [UserState.isInitialized] 后再用
 *    [UserState.isLoggedIn] 决策 startDestination，避免已登录用户冷启动落到 AuthScreen
 * 5. 承载全局导航图 [AppNavGraph]，订阅 [AppEventBus] 完成跨页面导航
 *
 * 注意：登录态恢复由 [UserState.initialize] 在 [PaiApplication.onCreate] 中完成，
 * MainActivity 仅 await `isInitialized` StateFlow，不直接读 DataStore。
 *
 * 业务方拉取脚手架后保留此结构，仅在 [AppNavGraph] 内添加新路由（用 `composable<XxxRoute>`）。
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** 全局事件总线，由 Hilt 注入单例 */
    @Inject
    lateinit var eventBus: AppEventBus

    /** 全局导航器，由 Hilt 注入单例 */
    @Inject
    lateinit var appNavigator: AppNavigator

    /** 全局用户状态（登录态），由 Hilt 注入单例 */
    @Inject
    lateinit var userState: UserState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // 宿主级 ViewModel：绑定在 MainActivity scope，跨 feature 共享主题状态
            val mainViewModel: MainActivityViewModel = hiltViewModel()
            val themeMode by mainViewModel.themeMode
                .collectAsStateWithLifecycle(initialValue = ThemeMode.System)
            val dynamicColor by mainViewModel.dynamicColor
                .collectAsStateWithLifecycle(initialValue = false)
            val brandColor by mainViewModel.brandColor
                .collectAsStateWithLifecycle(initialValue = DSBrandColor.Indigo)
            val fontSizeScale by mainViewModel.fontSizeScale
                .collectAsStateWithLifecycle(initialValue = DSFontSizeScale.Normal)

            // 决策 6：await UserState.isInitialized 后再决定 startDestination，
            // 避免已登录用户冷启动期间 StateFlow 仍是初始值 false 时误落到 AuthScreen
            val isInitialized by userState.isInitialized
                .collectAsStateWithLifecycle(initialValue = false)

            // 绑定 NavController 到 AppNavigator（每次 setContent 重建时重新绑定）
            val navController = rememberNavController()
            LaunchedEffect(navController) {
                appNavigator.bind(navController, userState)
            }
            DisposableEffect(navController) {
                onDispose { appNavigator.unbind() }
            }

            DSDesignTheme(
                darkTheme = when (themeMode) {
                    ThemeMode.System -> isSystemInDarkTheme()
                    ThemeMode.Light -> false
                    ThemeMode.Dark, ThemeMode.AMOLED -> true
                },
                dynamicColor = dynamicColor,
                brandColor = brandColor,
                fontSizeScale = fontSizeScale,
                amoled = themeMode == ThemeMode.AMOLED,
            ) {
                if (!isInitialized) {
                    // 决策 6：DataStore 首帧未就绪，显示 splash 等待
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    AppNavGraph(
                        eventBus = eventBus,
                        navController = navController,
                        appNavigator = appNavigator,
                        userState = userState,
                    )
                }
            }
        }
    }
}

/**
 * AppNavGraph - 应用全局导航图（类型安全路由，Navigation Compose 2.8+）
 *
 * 通用脚手架默认路由（均用 @Serializable data object 定义在 [com.pai.app.navigation.routes]）：
 * - [AuthRoute]：登录页
 * - [HomeRoute]：空白首页（登录后默认进入）
 * - [UserRoute]：个人中心
 * - [SettingsRoute]：设置页
 * - [GalleryRoute] + 8 个子页：组件 Gallery
 *
 * 决策 6：startDestination 由 [userState] 的当前登录态决定。
 * 由于外层 [MainActivity] 已 await [UserState.isInitialized] = true，此时
 * `userState.isLoggedIn.value` 是 DataStore 首帧真实值，不会是初始值 false。
 *
 * 决策 7：所有 UI 跳转通过 [appNavigator] 走 [AppNavigator.navigate]，
 * 使 [com.pai.app.navigation.DefaultRouteInterceptor] 对 UI 跳转也生效。
 *
 * 决策 2：订阅 [AppEventBus] 仅处理 `TokenExpired` 与 `GlobalError` 两个事件
 * （其他事件已删除）。TokenExpired → gotoAuth；GlobalError → Toast 提示。
 *
 * 业务方在此添加新路由：
 * ```kotlin
 * composable<ProductDetailRoute> { ProductDetailScreen(...) }
 * ```
 *
 * @param eventBus 全局事件总线
 * @param navController NavController（由 MainActivity 绑定到 AppNavigator）
 * @param appNavigator 全局导航器（决策 7：UI 跳转经此走，触发 RouteInterceptor）
 * @param userState 全局用户状态（用于计算 startDestination）
 */
@Composable
internal fun AppNavGraph(
    eventBus: AppEventBus,
    navController: NavHostController,
    appNavigator: AppNavigator,
    userState: UserState,
) {
    // 决策 6：startDestination 基于已就绪的 isLoggedIn 计算（外层已 await isInitialized）
    val isLoggedIn by userState.isLoggedIn.collectAsStateWithLifecycle()
    val startDestination: AppRoute = if (isLoggedIn) HomeRoute else AuthRoute

    // 决策 2：订阅全局事件 —— 仅 TokenExpired / GlobalError 两个分支
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        eventBus.events.collect { event ->
            when (event) {
                is AppEvent.TokenExpired -> appNavigator.gotoAuth()
                is AppEvent.GlobalError -> {
                    // 全局错误：用 Toast 提示（避免 DSAppScaffold 的 Snackbar 嵌套问题）
                    Toast.makeText(context, event.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        // 全局页面转场动画：前向导航左滑 + 后向导航右滑
        enterTransition = DSPageTransitions().enterTransition(),
        exitTransition = DSPageTransitions().exitTransition(),
        popEnterTransition = DSPageTransitions().popEnterTransition(),
        popExitTransition = DSPageTransitions().popExitTransition(),
    ) {
        // 登录页（未登录用户的起始目的地）
        composable<AuthRoute> {
            AuthScreen(onLoginSuccess = { appNavigator.gotoHome() })
        }

        // 空白首页（登录后默认进入）
        composable<HomeRoute> {
            HomeScreen(
                onOpenGallery = { appNavigator.gotoGallery() },
                onOpenSettings = { appNavigator.gotoSettings() },
                onOpenProfile = { appNavigator.gotoUser() },
            )
        }

        // 个人中心
        composable<UserRoute> {
            UserScreen(
                onLogoutSuccess = { appNavigator.gotoAuth() },
                onHomeClick = { appNavigator.gotoHome() },
                onSettingsClick = { appNavigator.gotoSettings() },
            )
        }

        // 设置页
        composable<SettingsRoute> {
            SettingsScreen(
                onLogoutSuccess = { appNavigator.gotoAuth() },
                onBackClick = { appNavigator.goBack() },
            )
        }

        // -------------------------------------------------------------------
        // 组件 Gallery 路由（主页 + 8 个分类子页）
        // 业务方保留，作为开发期组件预览
        // -------------------------------------------------------------------
        composable<GalleryRoute> {
            GalleryScreen(
                onBackClick = { appNavigator.goBack() },
                onNavigateToPage = { route -> appNavigator.gotoGalleryPage(route) },
            )
        }
        composable<GalleryThemeRoute> {
            GalleryThemePage(onBackClick = { appNavigator.goBack() })
        }
        composable<GalleryButtonRoute> {
            GalleryButtonPage(onBackClick = { appNavigator.goBack() })
        }
        composable<GalleryTextRoute> {
            GalleryTextPage(onBackClick = { appNavigator.goBack() })
        }
        composable<GalleryFormRoute> {
            GalleryFormPage(onBackClick = { appNavigator.goBack() })
        }
        composable<GalleryNavigationRoute> {
            GalleryNavigationPage(onBackClick = { appNavigator.goBack() })
        }
        composable<GalleryContainerRoute> {
            GalleryContainerPage(onBackClick = { appNavigator.goBack() })
        }
        composable<GalleryFeedbackRoute> {
            GalleryFeedbackPage(onBackClick = { appNavigator.goBack() })
        }
        composable<GalleryMotionRoute> {
            GalleryMotionPage(onBackClick = { appNavigator.goBack() })
        }
        composable<GalleryLayoutRoute> {
            GalleryLayoutPage(onBackClick = { appNavigator.goBack() })
        }
    }
}
