// ============================================================================
// AppRoutes.kt
// 类型安全路由表 - 借鉴 AndroidProject-Compose
// 用 @Serializable data object / data class 定义所有 NavDestination
// 替代旧的 const val 字符串路由（Navigation Compose 2.8+ 类型安全路由）
// ============================================================================

package com.pai.app.navigation.routes

import kotlinx.serialization.Serializable

/**
 * 路由标记接口
 *
 * 所有应用路由必须实现本接口并标注 `@Serializable`，
 * 才能被 Navigation Compose 2.8+ 的类型安全 API（`composable<Route>` / `navigate(Route)`）接受。
 *
 * 业务方拉取脚手架后，在此文件内追加 `@Serializable data object XxxRoute : AppRoute` 即可。
 *
 * 带参路由示例（业务方按需添加）：
 * ```kotlin
 * @Serializable data class ProductDetailRoute(val productId: Long) : AppRoute
 * ```
 */
interface AppRoute

// ============================================================================
// 顶层路由
// ============================================================================

/** 鉴权 / 登录页（应用起始目的地） */
@Serializable
data object AuthRoute : AppRoute

/** 空白首页（登录后默认进入） */
@Serializable
data object HomeRoute : AppRoute

/** 个人中心 */
@Serializable
data object UserRoute : AppRoute

/** 设置页 */
@Serializable
data object SettingsRoute : AppRoute

// ============================================================================
// 组件 Gallery 路由（开发期组件预览，业务方可保留或删除）
// ============================================================================

/** Gallery 主页 */
@Serializable
data object GalleryRoute : AppRoute

/** Gallery 子页：主题 */
@Serializable
data object GalleryThemeRoute : AppRoute

/** Gallery 子页：按钮 */
@Serializable
data object GalleryButtonRoute : AppRoute

/** Gallery 子页：文本 */
@Serializable
data object GalleryTextRoute : AppRoute

/** Gallery 子页：表单 */
@Serializable
data object GalleryFormRoute : AppRoute

/** Gallery 子页：导航 */
@Serializable
data object GalleryNavigationRoute : AppRoute

/** Gallery 子页：容器 */
@Serializable
data object GalleryContainerRoute : AppRoute

/** Gallery 子页：反馈 */
@Serializable
data object GalleryFeedbackRoute : AppRoute

/** Gallery 子页：动效 */
@Serializable
data object GalleryMotionRoute : AppRoute

/** Gallery 子页：布局 */
@Serializable
data object GalleryLayoutRoute : AppRoute

// ============================================================================
// NavHost 起始目的地
// ============================================================================

/**
 * NavHost 起始目的地（默认 AuthRoute）
 *
 * 决策 6：MainActivity 不再直接使用本常量作 startDestination，
 * 而是在 await [com.pai.app.navigation.UserState.isInitialized] 后用
 * `if (userState.isLoggedIn.value) HomeRoute else AuthRoute` 计算，
 * 避免已登录用户冷启动期间 StateFlow 仍是初始值 false 时误落到 AuthScreen。
 *
 * 本常量保留作为业务方参考：若不需要登录态感知的 startDestination 决策，
 * 可直接用本常量；否则参考 MainActivity 的 await 模式。
 *
 * 使用方式（类型安全）：
 * ```kotlin
 * NavHost(
 *     navController = navController,
 *     startDestination = StartDestination,
 * ) {
 *     composable<AuthRoute> { AuthScreen(...) }
 *     composable<HomeRoute> { HomeScreen(...) }
 *     // ...
 * }
 * ```
 *
 * 注意：Navigation Compose 2.8+ 的 `startDestination` 接受 `Any`（KSerializer 可推断），
 * 直接传 `StartDestination`（即 `AuthRoute` 对象）即可。
 */
val StartDestination: AppRoute = AuthRoute
