# ViewModel 与导航规范

> AI 规则文件 - ViewModel 与导航领域
> 本文件由 CLAUDE.md 拆分而来，对应原 CLAUDE.md 第 8 章。

---

## 一、ViewModel 必须继承 BaseViewModel

所有 Feature ViewModel **必须**继承 `core.base.BaseViewModel`：

- 注入 `AppNavigator` + `UserState`（Hilt 自动注入）
- 获得 `navigate(route)` / `navigateAndCloseCurrent(route, currentRoute)` / `navigateBack()` / `navigateBackTo(route)` / `isLoggedIn`
- 结果回传通过 `appNavigator.setResult(key, value)` + `appNavigator.observeResult(key): Flow<Any?>`（详见 `05-navigation-communication.md`）
- **禁止**直接继承 `ViewModel()` 而不继承 `BaseViewModel`
- **禁止**用旧的 `popBackStackWithResult` / `resultEvents` / `NavigationResultKey`（已删除）

```kotlin
@HiltViewModel
internal class PostDetailViewModel @Inject constructor(
    navigator: AppNavigator,
    userState: UserState,
    private val postRepository: PostRepository
) : BaseViewModel(navigator, userState) {

    fun openComment(postId: Long) = navigate(ProductDetailRoute(productId = postId))

    fun goBackWithRefresh(newCommentCount: Long) {
        appNavigator.setResult("refreshResult", newCommentCount)
        appNavigator.navigateBack()
    }
}
```

> 路由参数必须是 `@Serializable` 支持的类型（基本类型 / String / 自定义 Serializable data class）。

### BaseViewModel 提供的 API

| API | 用途 |
|-----|------|
| `navigate(route: AppRoute)` | 跳转到指定路由（接收 @Serializable `AppRoute` 实例，走 `AppNavigator.navigate`，RouteInterceptor 生效）|
| `navigateAndCloseCurrent(route, currentRoute)` | 跳转并关闭当前页（如登录后跳首页） |
| `navigateBack()` | 返回上一页 |
| `navigateBackTo(route)` | 返回到栈中指定页 |
| `appNavigator.setResult(key, value)` | 返回上一页并携带结果（详见 `05-navigation-communication.md`） |
| `appNavigator.observeResult(key): Flow<Any?>` | 监听其他页面回传的结果（lifecycle-safe，基于 savedStateHandle） |
| `isLoggedIn: Boolean` | 当前是否已登录（读 `userState.isLoggedIn.value`，单一真相源） |
| `logout()` | 退出登录（委托 `UserState.logout()`） |

---

## 二、路由跳转必须通过 AppNavigator

- ViewModel **禁止**直接持有 `NavController`（避免内存泄漏）
- 通过注入 `AppNavigator` 跳转（自动登录拦截）
- `AppNavigator` 在 `MainActivity` 中自动绑定 `NavController`
- `gotoXxx` 扩展现在也走 `AppNavigator.navigate(route)`，因此 `RouteInterceptor` 对 UI 跳转也生效
- 全局登录拦截由 `RouteInterceptor` 自动处理
- 受保护路由：USER / SETTINGS（未登录自动跳 AUTH），定义在 `DefaultRouteInterceptor.protectedRoutes`
- 已登录访问 AuthRoute 会被重定向到 HomeRoute（防循环：重定向目标不再过 interceptor）

### AppNavigator 设计

```
AppNavigator (Singleton, @Inject)
  │  持有 NavController 引用（弱引用 / LateInit）
  │  提供 navigate / navigateBack / setResult / observeResult 等 API
  ▼
RouteInterceptor
  │  在 navigate 调用前检查目标路由
  │  若为受保护路由且未登录 → 改写目标为 AuthRoute
  │  若为 AuthRoute 且已登录 → 改写目标为 HomeRoute（防循环）
  ▼
NavController
```

业务方无需关心登录拦截，所有 `navigate(ProtectedRoute)` 调用都会被自动重定向。

---

## 三、ApiResult 是唯一规范结果类型

新代码**必须**用 `core.base.ApiResult<T>`（`NetworkResponse` / `NetResult` 已删除）：

```kotlin
// Service 层（AppApi）：返回纯 DTO
@GET("posts")
suspend fun getPosts(): List<PostDto>

// Repository 层：用 safeApiCall 包装为 ApiResult
suspend fun getPosts(): ApiResult<List<PostItem>> = safeApiCall {
    api.getPosts().map { it.toItem() }
}

// 或返回 Flow<ApiResult<T>>
fun observePosts(): Flow<ApiResult<List<PostItem>>> =
    postDao.observeAll().map { entities -> entities.map { it.toItem() } }.asResult()
```

### ApiResult 三态

| 状态 | 字段 | 用途 |
|------|------|------|
| `Success<T>` | `data: T` | 成功，携带数据 |
| `Error` | `exception: AppException` | 失败，携带异常（统一分类：Http / Serialization / Network / Business / Unknown） |
| `Loading` | - | 加载中 |

### safeApiCall 包装

`AppApi` 返回纯 DTO 的接口，Repository 用 `safeApiCall` 包装为 `ApiResult`，异常由 `AppException.from(throwable)` 统一分类（CancellationException 透传、HttpException → HttpException、JsonDecodingException → SerializationException、IOException → NetworkException、其他 → UnknownException）：

```kotlin
suspend fun getPost(id: Long): ApiResult<PostItem> = safeApiCall {
    api.getPost(id).toItem()
}
```

### Flow.asResult

普通 `Flow<T>` 用 `.asResult(): Flow<ApiResult<T>>` 包装（自动 emit `Loading` + 捕获异常 emit `Error`）。

---

## 四、BaseNetWorkViewModel + Flow.asResult + ResultHandler

对于「详情页 / 列表页」这类典型网络请求页面，推荐用 `BaseNetWorkViewModel<T>`：

```kotlin
@HiltViewModel
internal class PostDetailViewModel @Inject constructor(
    navigator: AppNavigator,
    userState: UserState,
    private val repository: PostRepository,
) : BaseNetWorkViewModel<PostDetailData>(navigator, userState) {

    override fun requestApiFlow(): Flow<ApiResult<PostDetailData>> {
        return repository.getPostDetail().asResult()
    }

    init { executeRequest() }

    fun retryRequest() = executeRequest()
}
```

### 关键点

- ViewModel 继承 `BaseNetWorkViewModel<T>`，只需实现 `requestApiFlow(): Flow<ApiResult<T>>`
- 用 `.asResult()` 把任意 `Flow<T>` 包装为 `Flow<ApiResult<T>>`（自动捕获异常转 Error）
- UI 用 `DSNetWorkView` 一行搞定三态切换（详见 `07-ui-components.md`）
- **禁止**手动写 `when(result) { Loading → ...; Success → ...; Error → ... }`，用 `DSNetWorkView` 替代

### 表单页例外（H16）

含表单字段 / 复合状态的页面（如登录、反馈表单）**允许**继承 `BaseViewModel` 手写 `when(ApiResult)` 处理——因 `BaseNetWorkViewModel` 的纯三态抽象不适用（还需管理表单字段、校验状态等）。需在 KDoc 注明例外理由。

参考 `feature/auth/AuthViewModel.kt`：继承 `BaseViewModel`，`login()` 内 `when (val result = authRepository.login(...))` 处理 Success / Error，Loading 分支不可达（`safeApiCall` 同步返回，不 emit Loading）——在 KDoc 中已注明。

---

## 五、全局共享状态（UserState 状态机）

`UserState` 是完整登录状态机，由 Hilt `@Singleton` 注入，**登录态单一真相源**（`isLoggedIn` 直接派生自 `userPreferences.isLoggedIn`，不再有独立 MutableStateFlow）：

| API | 返回类型 | 用途 |
|-----|---------|------|
| `isLoggedIn` | `StateFlow<Boolean>` | 是否已登录（派生自 `userPreferences.isLoggedIn`，`stateIn(appScope, SharingStarted.Eagerly, false)`） |
| `userId` | `StateFlow<Long?>` | 当前用户 ID |
| `isInitialized` | `StateFlow<Boolean>` | `initialize()` 完成后置 true（MainActivity await 此值后才决定 startDestination） |
| `initialize()` | `suspend` | Application.onCreate 中调用，从 DataStore 恢复登录态 |
| `suspend onLoginSuccess(userId, token)` | `suspend` | 登录成功后调用，写 DataStore + EncryptedPrefs + 更新内存 |
| `suspend logout()` | `suspend` | 退出登录，清 DataStore + EncryptedPrefs + 重置内存 |

业务方 ViewModel 通过 BaseViewModel 访问：

```kotlin
val isLoggedIn: Boolean get() = userState.isLoggedIn.value
// 继承 BaseViewModel 的 logout() 自动委托 userState.logout()
```

> `token` 仅在 `EncryptedPrefs`（EncryptedSharedPreferences AES256-GCM）中存储，**不**通过 `UserPreferences` 暴露 StateFlow，避免 token 漏到 UI 层；`HeaderInterceptor` 用 `AtomicReference` 缓存 token 零阻塞注入；`TokenAuthenticator` 在 401 时读 token + emit `TokenExpired` + `userState.logout()`。

> `MainActivityViewModel.isLoggedIn` 已删除（避免双重真相源）；主题相关状态仍在 `MainActivityViewModel`。

---

## 六、主题模式与动态颜色

通过 `MainActivityViewModel` 获取（不要在业务 ViewModel 中直接访问 DataStore）：

```kotlin
val mainViewModel: MainActivityViewModel = hiltViewModel()
val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
```

`MainActivityViewModel` 在 `core.appstate` 中：

- 读取 `ThemePreferences`（DataStore）暴露 `themeMode: StateFlow<ThemeMode>`
- 读取 `dynamicColor: StateFlow<Boolean>`
- 读取 `brandColor: StateFlow<DSBrandColor>` / `fontSizeScale: StateFlow<DSFontSizeScale>`
- **不再**暴露 `isLoggedIn`（单一真相源是 `UserState`，避免双重真相源）
- 业务方通过 `hiltViewModel()` 获取

---

## 七、Repository 模式

### 7.1 Repository 写法（直接 @Inject constructor，无需 Module）

```kotlin
// core/data/PostRepository.kt
class PostRepository @Inject constructor(
    private val api: AppApi,
    private val postDao: PostDao,
) {
    suspend fun getPosts(page: Int, size: Int): ApiResult<List<PostItem>> =
        safeApiCall { api.getPosts(page, size).map { it.toItem() } }

    fun observePosts(): Flow<ApiResult<List<PostItem>>> =
        postDao.observeAll().map { entities -> entities.map { it.toItem() } }.asResult()

    suspend fun getPost(id: Long): Flow<PostDetailData> {
        // 优先从数据库缓存，再从网络刷新
        return flow {
            postDao.findById(id)?.let { emit(it.toModel()) }
            val remote = api.getPost(id)
            postDao.insert(remote.toEntity())
            emit(remote.toModel())
        }
    }
}
```

### 7.2 Repository 强制规则

- 必须放在 `core/data/` 下
- 类名 `[Entity]Repository`
- 用 `@Inject constructor`，**无需**写 Hilt Module
- 内部可同时调用 `AppApi`（Retrofit）和 `XxxDao`（Room），但**对外只暴露 domain model**（DTO / Entity 转换为 domain model 如 `ExampleItem` 后返回）
- ViewModel 注入 Repository，**禁止**直接使用 Retrofit / Room
- 网络请求用 `safeApiCall { api.xxx() }` 包装为 `ApiResult<T>`；Flow 用 `.asResult()` 包装为 `Flow<ApiResult<T>>`

> 新增 Repository 模式时，在本节追加示例（详见 `00-documentation-protocol.md`）。
