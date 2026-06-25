// ============================================================================
// RouteArchitectureTest.kt
// Konsist 架构测试 - 类型安全路由规则（H7 扩展，决策 11）
// 锁死 navigation/routes 下所有路由必须 @Serializable + 实现 AppRoute
// ============================================================================

package com.pai.app.architecture

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assert
import org.junit.Test

/**
 * 路由架构测试
 *
 * 类型安全路由（Navigation Compose 2.8+）要求每个路由：
 * 1. 标注 `@Serializable`（kotlinx.serialization）—— Navigation Compose 用 KSerializer 推断
 * 2. 实现 [com.pai.app.navigation.routes.AppRoute] 接口 —— 强类型约束，避免任意类型混入
 *
 * 业务方拉取脚手架后追加 `@Serializable data object XxxRoute : AppRoute` 即可，
 * 由本测试在 CI 中自动守护两条不变量。
 *
 * Konsist 0.17.3 API：`Konsist.scopeFromPackage("..").objects` 返回所有
 * `object` / `data object` / `companion object` 声明；用 `hasAnnotation` /
 * `hasSuperclass` 检查注解与父类型。若 API 在更高版本变化，可改用
 * `obj.annotations.any { it.name == "Serializable" }` 等价形式。
 */
class RouteArchitectureTest {

    /**
     * routes 包下所有 data object 必须有 @Serializable 注解
     *
     * Navigation Compose 2.8+ 的类型安全 API（`composable<Route>` /
     * `navigate(Route)`）依赖 KSerializer 推断路由序列化，
     * 缺少 @Serializable 会在运行时崩溃。
     */
    @Test
    fun `routes 包下所有 data object 必须有 @Serializable 注解`() {
        Konsist
            .scopeFromPackage("com.pai.app.navigation.routes..")
            .objects
            .filter { obj -> obj.name.endsWith("Route") }
            .assert { obj -> obj.hasAnnotation("kotlinx.serialization.Serializable") }
    }

    /**
     * routes 包下所有 data object 必须实现 AppRoute 接口
     *
     * AppRoute 是路由标记接口，强制所有路由共享同一类型，便于：
     * - RouteInterceptor 统一拦截 `AppRoute` 实例
     * - AppNavigator.navigate(route: AppRoute) 类型安全
     * - StartDestination: AppRoute 类型约束
     *
     * 漏标 AppRoute 会导致 RouteInterceptor / navigate(AppRoute) 编译失败，
     * 本测试在 CI 中提前拦截。
     *
     * Konsist 0.17.3：data object 通过 `: AppRoute` 实现接口 —— 用
     * `hasSuperInterface` 检查实现的接口 FQN；若 API 在更高版本变化，
     * 可改用 `obj.superInterfaces.any { it.name == "AppRoute" }` 等价形式。
     */
    @Test
    fun `routes 包下所有 data object 必须实现 AppRoute 接口`() {
        Konsist
            .scopeFromPackage("com.pai.app.navigation.routes..")
            .objects
            .filter { obj -> obj.name.endsWith("Route") }
            .assert { obj ->
                obj.hasSuperInterface("com.pai.app.navigation.routes.AppRoute")
            }
    }
}
