# 状态管理规范

> AI 规则文件 - 状态管理领域
> 本文件由 CLAUDE.md 拆分而来，对应原 CLAUDE.md 第 10 章。

---

## 一、UiState 规范

1. UI 状态使用 `data class [Feature]UiState`，所有字段不可变（`val`）
2. ViewModel 通过 `MutableStateFlow` 内部持有状态，对外暴露 `StateFlow`（`asStateFlow`）
3. 状态更新必须用 `_uiState.update { it.copy(...) }`，**禁止**直接 `_uiState.value = _uiState.value.copy(...)`（线程安全）

### 标准 UiState 模板

```kotlin
internal data class [Name]UiState(
    val isLoading: Boolean = false,
    val data: [Name]Data? = null,
    val error: String? = null,
    // 业务字段...
)

@HiltViewModel
internal class [Name]ViewModel @Inject constructor(
    navigator: AppNavigator,
    userState: UserState,
) : BaseViewModel(navigator, userState) {

    private val _uiState = MutableStateFlow([Name]UiState())
    val uiState: StateFlow<[Name]UiState> = _uiState.asStateFlow()

    fun onUserAction(value: String) {
        _uiState.update { it.copy(/* ... */) }
    }
}
```

---

## 二、StateFlow 使用

### 2.1 对外暴露规则

| 字段类型 | 暴露方式 | 理由 |
|---------|---------|------|
| 可读状态 | `StateFlow<T>` | Compose `collectAsStateWithLifecycle` 订阅 |
| 一次性事件 | `SharedFlow<T>` 或状态标志位 | 避免倒灌 |
| 内部可变状态 | `MutableStateFlow<T>` + `private` | 防外部修改 |

### 2.2 collectAsStateWithLifecycle

UI 层必须用 `collectAsStateWithLifecycle()`，**禁止**用 `collectAsState()`：

```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

理由：`collectAsStateWithLifecycle` 在 Compose 退到后台时自动停止订阅，省电。

---

## 三、一次性事件

一次性事件（如导航、Toast、Snackbar）有两种实现方式，任选其一：

### 方式 1：状态标志位 + reset 方法

```kotlin
internal data class AuthUiState(
    val isLoginSuccess: Boolean = false,
    // ...
)

class AuthViewModel : BaseViewModel(...) {
    fun onLoginSuccess() {
        _uiState.update { it.copy(isLoginSuccess = true) }
    }

    fun resetLoginSuccess() {
        _uiState.update { it.copy(isLoginSuccess = false) }
    }
}

// Screen
LaunchedEffect(uiState.isLoginSuccess) {
    if (uiState.isLoginSuccess) {
        onLoginSuccess()
        viewModel.resetLoginSuccess()
    }
}
```

适用：与 UI 状态紧耦合的事件。

### 方式 2：AppEventBus（仅 TokenExpired / GlobalError）

```kotlin
// ViewModel（仅用于网络层 → UI 全局反应 / 未捕获异常）
eventBus.emit(AppEvent.GlobalError("加载失败"))

// MainActivity.AppNavGraph（唯一订阅点）
LaunchedEffect(Unit) {
    eventBus.events.collect { event ->
        when (event) {
            is AppEvent.TokenExpired -> appNavigator.gotoAuth()
            is AppEvent.GlobalError  -> showSnackbar(event.message)
        }
    }
}
```

适用：跨模块全局事件（详见 `05-navigation-communication.md`）。**登录/登出导航不用 AppEventBus**，用 Composable 回调。

---

## 四、UserState API 表（全局共享状态）

`UserState` 是完整登录状态机，由 Hilt `@Singleton` 注入（详见 `06-viewmodel.md`）。**登录态单一真相源**：`isLoggedIn` 直接派生自 `userPreferences.isLoggedIn`，不再有独立 MutableStateFlow。

| API | 返回类型 | 用途 |
|-----|---------|------|
| `isLoggedIn` | `StateFlow<Boolean>` | 是否已登录（派生自 `userPreferences.isLoggedIn`） |
| `userId` | `StateFlow<Long?>` | 当前用户 ID |
| `isInitialized` | `StateFlow<Boolean>` | `initialize()` 完成后置 true（MainActivity await 此值后才决定 startDestination） |
| `initialize()` | `suspend` | Application.onCreate 中调用，从 DataStore 恢复登录态 |
| `onLoginSuccess(userId, token)` | `suspend` | 登录成功后调用，写 DataStore + EncryptedPrefs + 更新内存 |
| `logout()` | `suspend` | 退出登录，清 DataStore + EncryptedPrefs + 重置内存 |

> `token` 仅在 `EncryptedPrefs`（EncryptedSharedPreferences AES256-GCM）中存储，**不**通过 `UserPreferences` 暴露 `StateFlow<String?>`，避免 token 漏到 UI 层。`HeaderInterceptor` 用 `AtomicReference` 缓存 token 零阻塞注入；`TokenAuthenticator` 在 401 时 emit `AppEvent.TokenExpired` + `userState.logout()`。

### 异步登录态恢复模式

`PaiApplication.onCreate` 异步调 `userState.initialize()`；`MainActivity.setContent` 内用 `produceState` await `userState.isInitialized.first()` 完成后再决定 `startDestination`（已登录 → `HomeRoute`，未登录 → `AuthRoute`），未完成时显示 splash（`DSFullScreenLoading`）。业务方 Feature 若需在启动时根据登录态初始化，同样应在 `LaunchedEffect(userState.isInitialized)` 内 await，**不要**直接读 `isLoggedIn.value`（可能读到 stale 值）。

### 业务方访问方式

```kotlin
// 通过 BaseViewModel
class MyViewModel @Inject constructor(
    navigator: AppNavigator,
    userState: UserState,  // Hilt 自动注入
) : BaseViewModel(navigator, userState) {

    fun doSomething() {
        if (isLoggedIn) {
            // 已登录逻辑
        }
    }

    fun logout() {
        viewModelScope.launch {
            // 调用 BaseViewModel.logout()，自动委托 userState.logout()
            logout()
        }
    }
}
```

---

## 五、主题状态（MainActivityViewModel）

主题模式、动态颜色、品牌色板、字号缩放通过 `MainActivityViewModel` 获取。

> **注意**：`MainActivityViewModel.isLoggedIn` 已删除（避免与 `UserState.isLoggedIn` 形成双重真相源）。登录态查 `UserState`。

```kotlin
val mainViewModel: MainActivityViewModel = hiltViewModel()
val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
val dynamicColor by mainViewModel.dynamicColor.collectAsStateWithLifecycle()
val brandColor by mainViewModel.brandColor.collectAsStateWithLifecycle()
val fontSizeScale by mainViewModel.fontSizeScale.collectAsStateWithLifecycle()
```

### MainActivityViewModel API

| API | 返回类型 | 用途 |
|-----|---------|------|
| `themeMode` | `StateFlow<ThemeMode>` | 主题模式（System / Light / Dark / **AMOLED**）|
| `dynamicColor` | `StateFlow<Boolean>` | 是否启用动态颜色（覆盖品牌色）|
| `brandColor` | `StateFlow<DSBrandColor>` | 品牌色板（Indigo / Emerald / Rose / Amber / Sky），仅当 `dynamicColor = false` 时生效 |
| `fontSizeScale` | `StateFlow<DSFontSizeScale>` | 字号缩放等级（Small / Normal / Large / ExtraLarge）|
| `setThemeMode(mode)` | `suspend` | 切换主题模式 |
| `setDynamicColor(enabled)` | `suspend` | 切换动态颜色 |
| `setBrandColor(brand)` | `suspend` | 切换品牌色板 |
| `setFontSizeScale(scale)` | `suspend` | 切换字号缩放等级 |

业务方在设置页用上述 API 修改主题，`MainActivity` 自动响应并重建 `DSDesignTheme`。

### 多主题系统说明

`DSDesignTheme` 支持以下多主题能力（详见 `07-ui-components.md` 第八节 Theme.kt）：

```kotlin
DSDesignTheme(
    darkTheme = isSystemInDarkTheme(),
    dynamicColor = false,
    brandColor = DSBrandColor.Indigo,
    fontSizeScale = DSFontSizeScale.Normal,
    amoled = false,
) { ... }
```

- **ThemeMode**：System / Light / Dark / **AMOLED**（4 档，AMOLED 为纯黑省电变种）
- **品牌色板**：5 套预设（Indigo / Emerald / Rose / Amber / Sky），由 `BrandColorPalette.kt` 提供 50-900 完整色阶，`ColorScheme.kt` 动态生成 Light/Dark ColorScheme
- **字号缩放**：4 档（Small 0.85x / Normal 1.0x / Large 1.15x / ExtraLarge 1.3x），通过 `LocalFontSizeScale` 注入 `DSText` 等文本组件
- **动态颜色**：开启时覆盖 `brandColor`（仅 Android 12+）

---

## 六、禁止事项

- **禁止**在 UiState 中使用 `MutableState`、`LiveData`
- **禁止**在 Composable 中直接调用 Repository / Retrofit / Room
- **禁止**在 Composable 中创建 `MutableStateFlow`（必须从 ViewModel 暴露）
- **禁止**在 ViewModel 中持有 `Context` / `View` 引用（用 `AndroidViewModel` + `Application` 例外，本项目应避免）
- **禁止**用 `collectAsState()`，**必须**用 `collectAsStateWithLifecycle()`
