# 类型安全路由规范

> AI 规则文件 - 路由领域
> 本文件由 CLAUDE.md 拆分而来，对应原 CLAUDE.md 第 6 章。

---

## 一、路由定义

所有路由在 `navigation/routes/AppRoutes.kt` 中用 `@Serializable data object` 定义：

```kotlin
@Serializable data object AuthRoute : AppRoute
@Serializable data object HomeRoute : AppRoute

// 带参路由（业务方按需添加）
@Serializable data class ProductDetailRoute(val productId: Long) : AppRoute
```

### 1.1 AppRoute 接口

- 所有路由必须实现 `AppRoute` 接口（密封标记接口）
- 必须添加 `@Serializable` 注解（Navigation 2.9+ 类型安全路由依赖 kotlinx-serialization）
- 无参路由用 `data object`，有参路由用 `data class`

### 1.2 路由清单（项目当前）

| Route | 类型 | 用途 |
|-------|------|------|
| `AuthRoute` | data object | 登录页 |
| `HomeRoute` | data object | 首页 |
| `UserRoute` | data object | 个人中心（受保护） |
| `SettingsRoute` | data object | 设置页（受保护） |
| `GalleryRoute` | data object | 组件 Gallery 主页 |
| `GalleryThemeRoute` | data object | Gallery 子页：主题 |
| `GalleryButtonRoute` | data object | Gallery 子页：按钮 |
| `GalleryTextRoute` | data object | Gallery 子页：文本 |
| `GalleryFormRoute` | data object | Gallery 子页：表单 |
| `GalleryNavigationRoute` | data object | Gallery 子页：导航 |
| `GalleryContainerRoute` | data object | Gallery 子页：容器 |
| `GalleryFeedbackRoute` | data object | Gallery 子页：反馈 |
| `GalleryMotionRoute` | data object | Gallery 子页：动效（含 Lottie 演示） |
| `GalleryLayoutRoute` | data object | Gallery 子页：布局 |

共 **14 个** `data object`（4 个顶层业务 + GalleryRoute + 9 个 Gallery 子页），带参路由另用 `data class`。

> Gallery 主页卡片数与子页路由数解耦：卡片可业务调整（如合并 Lottie 与 Motion），但路由表稳定为 14 个。

> 新增路由时，在此表追加一行（详见 `00-documentation-protocol.md`）。

---

## 二、路由注册

NavHost 中用 `composable<Route>` 类型安全注册：

```kotlin
NavHost(startDestination = startRoute) {
    composable<AuthRoute> { AuthScreen(...) }
    composable<HomeRoute> { HomeScreen(...) }
    composable<ProductDetailRoute> { backStackEntry ->
        val args = backStackEntry.toRoute<ProductDetailRoute>()
        ProductDetailScreen(productId = args.productId)
    }
}
```

> `startRoute` 动态决定：`MainActivity` 在 `setContent` 内用 `produceState` await `userState.isInitialized.first()` 完成后，根据 `userState.isLoggedIn.value` 选 `HomeRoute`（已登录）或 `AuthRoute`（未登录）；未完成时显示 splash（`DSFullScreenLoading`）。不再硬编码 `StartDestination = AuthRoute`。

### 2.1 注册要点

- `composable<Route>` 必须用泛型形式，禁止 `composable("auth")` 字符串形式
- 带参路由用 `backStackEntry.toRoute<Route>()` 解析参数
- 参数类型必须是 `@Serializable` 支持的类型（基本类型 / String / 自定义 Serializable data class）

---

## 三、路由跳转

### 方式 1：AppNavigator 扩展（推荐，语义化，走 RouteInterceptor）

```kotlin
appNavigator.gotoAuth()
appNavigator.gotoHome()
```

- 扩展函数定义在 `navigation/extension/NavExtensions.kt`
- 命名模式：`goto[Feature]`
- 内部调 `AppNavigator.navigate(route)`，**不再直接调** `NavController.navigate()`
- 必须用 `internal` 修饰（仅在 app 模块内可见）
- 由于走 `AppNavigator.navigate`，`DefaultRouteInterceptor` 现对 UI 跳转也生效（受保护路由未登录重定向 AuthRoute，已登录访问 AuthRoute 重定向 HomeRoute）

### 方式 2：BaseViewModel.navigate（ViewModel 内部）

```kotlin
navigate(AuthRoute)
navigate(ProductDetailRoute(productId = 123))
```

- 通过 `BaseViewModel` 注入的 `AppNavigator` 间接调用
- 自动应用 `RouteInterceptor` 登录拦截

---

## 四、强制规则

1. **禁止**用 `const val` 字符串定义路由
2. **禁止**在 feature 模块内出现 `"auth"` 等路径字符串字面量
3. 所有路由必须实现 `AppRoute` 接口 + `@Serializable` 注解
4. 新增 Feature 时在 `AppRoutes.kt` 加 `[Feature]Route` + 在 `NavExtensions.kt` 加 `goto[Feature]()`

---

## 五、受保护路由

`RouteInterceptor` 自动处理登录拦截（现在对 UI 跳转也生效，因为 `gotoXxx` 扩展走 `AppNavigator.navigate`）：

- 未登录用户访问受保护路由 → 自动跳转 `AuthRoute`
- 已登录用户访问 `AuthRoute` → 自动跳转 `HomeRoute`（防循环：重定向目标不再过 interceptor）
- 当前受保护路由：USER / SETTINGS（定义在 `DefaultRouteInterceptor.protectedRoutes`）
- 401 Token 过期由 `TokenAuthenticator` 拦截 → 发射 `AppEvent.TokenExpired` → MainActivity.AppNavGraph 收到后 `gotoAuth()` + `userState.logout()`
- 业务方新增受保护路由时，在 `RouteInterceptor` 中注册（详见 `06-viewmodel.md` 的 AppNavigator 章节）

---

## 六、新增路由流程

1. 在 `navigation/routes/AppRoutes.kt` 追加：
   ```kotlin
   @Serializable data object [Name]Route : AppRoute
   ```
2. 在 `navigation/extension/NavExtensions.kt` 追加：
   ```kotlin
   internal fun AppNavigator.goto[Name]() {
       navigate([Name]Route)
   }
   ```
3. 在 `MainActivity.kt` 的 `AppNavGraph` 挂载：
   ```kotlin
   composable<[Name]Route> { [Name]Screen(...) }
   ```
4. 如需登录保护，在 `RouteInterceptor` 中注册
5. 完成后在 `04-routing.md` 路由清单表中追加一行（详见 `00-documentation-protocol.md`）
