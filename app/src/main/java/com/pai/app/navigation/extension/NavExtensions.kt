// ============================================================================
// NavExtensions.kt
// AppNavigator 跳转扩展函数（决策 7）
// 对 AppRoute 做语义化封装，调用方无需关心具体 Route 对象细节。
//
// 决策 7：所有 gotoXxx 改为 [AppNavigator] 的扩展函数（而非 [NavController]），
// 内部统一调 [AppNavigator.navigate] / [AppNavigator.navigateAndClearStack]，
// 使 [com.pai.app.navigation.DefaultRouteInterceptor] 对 UI 跳转也生效。
// ============================================================================

package com.pai.app.navigation.extension

import com.pai.app.navigation.AppNavigator
import com.pai.app.navigation.routes.AppRoute
import com.pai.app.navigation.routes.AuthRoute
import com.pai.app.navigation.routes.GalleryButtonRoute
import com.pai.app.navigation.routes.GalleryContainerRoute
import com.pai.app.navigation.routes.GalleryFeedbackRoute
import com.pai.app.navigation.routes.GalleryFormRoute
import com.pai.app.navigation.routes.GalleryMotionRoute
import com.pai.app.navigation.routes.GalleryNavigationRoute
import com.pai.app.navigation.routes.GalleryRoute
import com.pai.app.navigation.routes.GalleryTextRoute
import com.pai.app.navigation.routes.GalleryThemeRoute
import com.pai.app.navigation.routes.HomeRoute
import com.pai.app.navigation.routes.SettingsRoute
import com.pai.app.navigation.routes.UserRoute

/**
 * 跳转到鉴权 / 登录页
 *
 * 默认会清空回退栈直达起始目的地（实现"退出登录后清栈"语义），
 * 避免返回键再次回到主页面。
 *
 * @param clearStack 是否清空回退栈，默认 true
 */
internal fun AppNavigator.gotoAuth(clearStack: Boolean = true) {
    if (clearStack) {
        navigateAndClearStack(AuthRoute)
    } else {
        navigate(AuthRoute)
    }
}

/**
 * 跳转到空白首页
 *
 * 默认会清空回退栈直达起始目的地（实现"登录后清栈"语义）。
 *
 * @param clearStack 是否清空回退栈，默认 true
 */
internal fun AppNavigator.gotoHome(clearStack: Boolean = true) {
    if (clearStack) {
        navigateAndClearStack(HomeRoute)
    } else {
        navigate(HomeRoute)
    }
}

/**
 * 跳转到个人中心
 *
 * 保留回退栈，允许返回上一页。
 */
internal fun AppNavigator.gotoUser() {
    navigate(UserRoute)
}

/**
 * 跳转到设置页
 *
 * 保留回退栈，允许返回上一页。
 */
internal fun AppNavigator.gotoSettings() {
    navigate(SettingsRoute)
}

/**
 * 跳转到组件 Gallery 主页
 *
 * 保留回退栈，允许返回上一页。
 */
internal fun AppNavigator.gotoGallery() {
    navigate(GalleryRoute)
}

/**
 * 跳转到 Gallery 子页面
 *
 * @param route Gallery 子页面路由，如 [GalleryThemeRoute] / [GalleryButtonRoute] 等
 */
internal fun AppNavigator.gotoGalleryPage(route: AppRoute) {
    navigate(route)
}

/** Gallery 主题子页路由（暴露给 GalleryScreen 用于卡片点击） */
internal val GalleryThemeRouteEntry: AppRoute = GalleryThemeRoute

/** Gallery 按钮子页路由 */
internal val GalleryButtonRouteEntry: AppRoute = GalleryButtonRoute

/** Gallery 文本子页路由 */
internal val GalleryTextRouteEntry: AppRoute = GalleryTextRoute

/** Gallery 表单子页路由 */
internal val GalleryFormRouteEntry: AppRoute = GalleryFormRoute

/** Gallery 导航子页路由 */
internal val GalleryNavigationRouteEntry: AppRoute = GalleryNavigationRoute

/** Gallery 容器子页路由 */
internal val GalleryContainerRouteEntry: AppRoute = GalleryContainerRoute

/** Gallery 反馈子页路由 */
internal val GalleryFeedbackRouteEntry: AppRoute = GalleryFeedbackRoute

/** Gallery 动效子页路由 */
internal val GalleryMotionRouteEntry: AppRoute = GalleryMotionRoute

/**
 * 返回上一页
 *
 * 等价于 [AppNavigator.navigateBack]，提供更语义化的调用方式。
 */
internal fun AppNavigator.goBack() {
    navigateBack()
}