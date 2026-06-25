# 跨模块通信规范

> AI 规则文件 - 跨模块通信领域
> 本文件由 CLAUDE.md 拆分而来，对应原 CLAUDE.md 第 7 章。

---

## 总原则

feature 模块之间**禁止**直接 import 对方类。通信方式 3 选 1：

| 方式 | 适用场景 | 优点 | 缺点 |
|------|---------|------|------|
| 回调参数（Composable lambda） | 简单父子通信、登录/登出导航 | 类型安全 / 无副作用 | 嵌套深时链路长 |
| AppEventBus（仅 `TokenExpired` / `GlobalError` 2 事件） | 跨多模块广播、网络层 → UI 全局事件 | 解耦 | 难追踪数据流 |
| savedStateHandle 结果回传（`AppNavigator.setResult/observeResult`） | 跳转后回传结果（详情→列表、表单→列表） | lifecycle-safe / 跨进程死亡存活 | 仅适用页面间 |

---

## 方式 1：回调参数（首选，简单场景）

Screen 通过 lambda 参数回调到 MainActivity 的 AppNavGraph：

```kotlin
@Composable
internal fun AuthScreen(onLoginSuccess: () -> Unit) { ... }

// MainActivity.kt AppNavGraph
composable<AuthRoute> {
    AuthScreen(onLoginSuccess = { appNavigator.gotoHome() })
}
```

### 使用要点

- Screen 入参用 `on[Action]: () -> Unit` 或 `on[Action]: (Arg) -> Unit`
- 回调链路：Screen → ViewModel 触发 → 回调到 MainActivity → 调用 `appNavigator.gotoXxx()`
- 适用于：登录成功跳首页、点击列表项跳详情、返回按钮等
- **登录/登出导航走回调，不走 AppEventBus**（避免双重机制）

---

## 方式 2：AppEventBus（仅 TokenExpired / GlobalError 全局事件）

`AppEventBus` 已精简为 2 个事件（旧的 9 个事件已删除）：

```kotlin
sealed interface AppEvent {
    data object TokenExpired : AppEvent              // TokenAuthenticator 在 401 时发射
    data class GlobalError(
        val message: String,
        val throwable: Throwable? = null,
    ) : AppEvent                                      // BaseViewModel 未捕获异常时发射
}
```

### 事件来源

| 事件 | 发射点 | 订阅点 | 处理 |
|------|--------|--------|------|
| `TokenExpired` | `TokenAuthenticator.authenticate()` 在 401 时 `eventBus.emit(TokenExpired)` + `userState.logout()` | `MainActivity.AppNavGraph` | `appNavigator.gotoAuth()` |
| `GlobalError` | `BaseViewModel` 在 viewModelScope 未捕获异常时 `eventBus.emit(GlobalError(...))` | `MainActivity.AppNavGraph` | 显示 Snackbar / Toast |

### 订阅示例

```kotlin
// 仅 MainActivity.AppNavGraph 订阅
LaunchedEffect(Unit) {
    eventBus.events.collect { event ->
        when (event) {
            is AppEvent.TokenExpired -> appNavigator.gotoAuth()
            is AppEvent.GlobalError  -> showSnackbar(event.message)
        }
    }
}
```

### 强制规则

- **禁止** feature 模块订阅 `AppEventBus.events`（仅 MainActivity.AppNavGraph 订阅）
- **禁止** 用 AppEventBus 做登录/登出导航（用 Composable 回调，见方式 1）
- 新增事件类型必须修改 `AppEvent` sealed interface，并补单测
- 仅在「网络层 → UI 全局反应」「未捕获异常 → 全局错误展示」场景使用 AppEventBus

---

## 方式 3：savedStateHandle 结果回传（lifecycle-safe）

> 旧的 `NavigationResultKey` / `popBackStackWithResult` / `resultEvents` / `_resultEvents` SharedFlow 已删除（replay=0 + 异步 emit 有时序缺陷，且未真正 lifecycle-safe）。统一改用基于 `NavBackStackEntry.savedStateHandle` 的 `AppNavigator.setResult/observeResult`。

适用场景：详情页编辑后通知列表页刷新、表单页提交后回传新 ID。

### API

```kotlin
// AppNavigator 提供（基于当前/上一 NavBackStackEntry 的 savedStateHandle）
fun setResult(key: String, value: Any?)        // 写入「上一 entry」的 savedStateHandle
fun <T> observeResult(key: String): Flow<T?>   // 读「当前 entry」的 savedStateHandle StateFlow
```

### 使用流程

#### 步骤 1：源页面（A）导航前订阅结果

```kotlin
@Composable
internal fun ListScreen(viewModel: ListViewModel = hiltViewModel()) {
    val resultKey = "editResult"
    val result by viewModel.appNavigator
        .observeResult<String>(resultKey)
        .collectAsStateWithLifecycle(initialValue = null)

    LaunchedEffect(result) {
        result?.let {
            // 收到回传值，刷新列表
            viewModel.refresh()
        }
    }

    // 点击进入编辑页
    DSButton(onClick = { viewModel.gotoEdit() }) { DSText("编辑") }
}
```

#### 步骤 2：目标页面（B）返回前写结果

```kotlin
class EditViewModel @Inject constructor(
    private val appNavigator: AppNavigator,
    ...
) : BaseViewModel(appNavigator, ...) {

    fun saveAndBack(newValue: String) {
        viewModelScope.launch {
            repository.save(newValue)
            appNavigator.setResult("editResult", newValue)
            appNavigator.navigateBack()
        }
    }
}
```

### 强制规则

- **必须**用 `AppNavigator.setResult(key, value)` + `observeResult(key): Flow<Any?>`（基于 savedStateHandle，跨进程死亡存活）
- **禁止**用旧的 `popBackStackWithResult` / `resultEvents` / `NavigationResultKey`（已删除）
- key 命名约定：`[Action]Result`（如 `editResult` / `refreshResult`），字符串字面量集中在调用双方约定
- 回传值类型必须是 `Parcelable` / `Serializable` / 基本类型（savedStateHandle 限制）

---

## 三种方式选择决策树

```
是否需要回传数据？
├── 否
│   ├── 父子直接关系？ → 方式 1（回调）
│   └── 跨多模块广播 / 网络层→UI 全局反应？
│       ├── 是 TokenExpired / GlobalError？ → 方式 2（AppEventBus）
│       └── 否（需要新事件类型）→ 先在 AppEvent sealed interface 加事件，再用方式 2
└── 是（详情→列表 / 表单→列表）
    → 方式 3（savedStateHandle 结果回传）
```

---

## 何时需要 BackHandler

默认 `popBackStack` 行为已足够，**仅在以下场景需要** `BackHandler`：

- 「再按一次退出」：双击返回键退出 App
- 「返回前确认」：返回前弹出确认对话框
- 「表单未保存提示」：表单页有未保存改动时拦截返回

```kotlin
@Composable
internal fun EditScreen(...) {
    var hasUnsavedChanges by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = hasUnsavedChanges) {
        showConfirmDialog = true
    }

    if (showConfirmDialog) {
        DSDialog(
            title = "放弃改动？",
            onConfirm = { showConfirmDialog = false; onBackClick() },
            onDismiss = { showConfirmDialog = false },
        )
    }
}
```

详见 `06-viewmodel.md` 中 `AppNavigator` 章节的 `setResult/observeResult` API 说明。
