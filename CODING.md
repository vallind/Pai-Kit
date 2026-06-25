# 编码规范

> 阶段：编码规范（阅读路径第 4 步）  
> 内容：命名 / 包隔离 / 路由 / ViewModel / 状态管理 / AI 协作

---

## 阅读路径

```
README → INTEGRATION_GUIDE → BUILD → CODING（本文件）→ 组件 API → 容器使用 → QUALITY
```

---

## 1. 核心规则

### 1.1 必须用 DS 组件

- ✅ 用 `DSButton` / `DSText` / `DSIcon` 等 DS 前缀组件
- ❌ 禁止直接用 Material3 原生 `Button` / `Text` / `Icon`
- ❌ 禁止硬编码 `16.dp` / `Color(0xFF4F46E5)`，必须用 `DSTokens.Spacing.lg` / `DSTokens.Brand.indigo600`

### 1.2 布局用原生 + Modifier

不再有 `DSBox` / `DSColumn` / `DSRow` 包装，直接用原生 + Modifier + DSTokens：

```kotlin
// ✅ 正确
Column(
    modifier = Modifier.fillMaxWidth().padding(DSTokens.Spacing.lg),
    verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
) {
    DSText("标题", variant = DSTextVariant.TitleMedium)
    DSText("内容", variant = DSTextVariant.BodyMedium)
}

// ❌ 错误（已删除）
DSCenterColumn(modifier = Modifier.fillMaxWidth()) { ... }
```

### 1.3 业务容器壳优先

| 场景 | 用什么 |
|---|---|
| 列表项（联系人/商品/通知） | `AppStructuredCard` |
| 表单分组卡 | `AppCommonCard` |
| 信息展示卡 | `AppCommonCard` |
| 设置项 | `AppStructuredCard` |
| 完全自定义结构 | `DSCard` |

详见 [docs/components/containers.md](docs/components/containers.md)。

### 1.4 Preview 规范

每个组件至少 3 个 Preview 场景，必须用 `DSPreviewScenes` 包装：

```kotlin
@Preview(showBackground = true, name = "Button - Light")
@Composable
private fun ButtonLightPreview() {
    DSPreviewScenes.Light { DSButton("Submit", onClick = {}) }
}

@Preview(showBackground = true, name = "Button - Dark")
@Composable
private fun ButtonDarkPreview() {
    DSPreviewScenes.Dark { DSButton("Submit", onClick = {}) }
}

@Preview(showBackground = true, name = "Button - Emerald")
@Composable
private fun ButtonEmeraldPreview() {
    DSPreviewScenes.Brand(DSBrandColor.Emerald) { DSButton("Submit", onClick = {}) }
}
```

---

## 2. 命名规范

详见 [docs/rules/03-naming-conventions.md](docs/rules/03-naming-conventions.md)。

### 2.1 组件命名

| 类型 | 前缀 | 示例 |
|---|---|---|
| DS 通用组件 | `DS` | `DSButton` / `DSText` / `DSIcon` |
| 业务容器壳 | `App` | `AppCommonCard` / `AppStructuredCard` |
| 枚举 | `DS` + 组件名 + `Style/Size/Variant/Type` | `DSButtonStyle` / `DSTextVariant` |
| 统一数据类 | `DS` | `DSNavItem` / `DSMessageType` |

### 2.2 包结构

```
com.pai.app/
├── core/
│   ├── base/           # BaseViewModel 等
│   ├── data/           # Repository 实现
│   ├── database/       # Room
│   ├── datastore/      # DataStore
│   ├── designsystem/   # DS 组件（foundation/primitives/patterns/shell/overlays/containers）
│   ├── domain/         # 业务模型 + Repository 接口（KMP-ready）
│   ├── network/        # Retrofit
│   └── util/           # 工具类
├── feature/
│   ├── auth/
│   ├── home/
│   └── ...
├── navigation/
│   ├── routes/
│   └── extension/
└── MainActivity.kt
```

### 2.3 可见性

- DS 组件默认 `internal`（仅同模块可访问）
- 业务容器壳（App 前缀）`public`
- Foundation Token 通过 `DSTokens.xxx` 统一入口

---

## 3. 包隔离

详见 [docs/rules/02-package-isolation.md](docs/rules/02-package-isolation.md)。

### 3.1 架构红线（Konsist 自动检查）

1. **`feature/*` 之间不得互相 import**（隔离）
2. **`feature/*` 不得直接 import `retrofit2.*` / `androidx.room.*`**（必须经 `core/data` 或 `core/domain`）
3. **`core/domain` 不得 import `android.*` / `retrofit2.*` / `androidx.room.*`**（KMP-ready 纯 Kotlin）
4. **`core/designsystem/<层>` 之间单向依赖**：`foundation ← primitives ← patterns`、`foundation ← shell`、`foundation + primitives ← overlays`

### 3.2 Feature 层依赖规则

- ✅ 允许依赖 primitives/patterns/shell/overlays/containers
- ✅ 优先使用 Patterns（如 DSNetWorkView）和 Containers（如 AppCommonCard）
- ❌ 禁止在 designsystem 下定义新的业务组件
- ❌ 禁止直接 import M3 原生组件

---

## 4. 路由

详见 [docs/rules/04-routing.md](docs/rules/04-routing.md)。

### 4.1 定义路由

```kotlin
// navigation/routes/AppRoutes.kt
@Serializable
data object HomeRoute : AppRoute

@Serializable
data class UserDetailRoute(val userId: String) : AppRoute
```

### 4.2 跳转扩展

```kotlin
// navigation/extension/NavExtensions.kt
fun AppNavigator.gotoHome() = navigate(HomeRoute)
fun AppNavigator.gotoUserDetail(userId: String) = navigate(UserDetailRoute(userId))
```

### 4.3 注册路由

```kotlin
// MainActivity.kt
NavHost(
    navController = navController,
    startDestination = HomeRoute,
    enterTransition = { currentDSMotionScheme().pageTransitions.enterTransition() },
    exitTransition = { currentDSMotionScheme().pageTransitions.exitTransition() }
) {
    composable<HomeRoute> { HomeScreen() }
    composable<UserDetailRoute> { entry ->
        UserDetailScreen(userId = entry.toRoute<UserDetailRoute>().userId)
    }
}
```

### 4.4 结果回传

```kotlin
// 设置结果
AppNavigator.setResult(key = "selectedItem", value = item)

// 观察结果
AppNavigator.observeResult(key = "selectedItem") { item ->
    // 处理结果
}
```

---

## 5. ViewModel

详见 [docs/rules/06-viewmodel.md](docs/rules/06-viewmodel.md)。

### 5.1 BaseViewModel

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    navigator: AppNavigator,
    userState: UserState
) : BaseViewModel(navigator, userState) {

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    fun loadUser() = launch {
        userRepository.getUser().collect { result ->
            // 处理 ApiResult
        }
    }
}
```

### 5.2 BaseNetWorkViewModel

带网络请求的 ViewModel 继承 `BaseNetWorkViewModel<T>`：

```kotlin
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    navigator: AppNavigator,
    userState: UserState
) : BaseNetWorkViewModel<Product>(navigator, userState) {

    fun loadProduct(id: String) = launchWithResult(
        apiCall = { productRepository.getProduct(id) },
        onSuccess = { /* 更新 uiState */ },
        onError = { /* 处理错误 */ }
    )
}
```

### 5.3 状态管理

详见 [docs/rules/08-state-management.md](docs/rules/08-state-management.md)。

**两种状态范式**：

| 范式 | 入口 | 适用 |
|---|---|---|
| slot 驱动 | `DSNetWorkView(isLoading, error, empty, content)` | 简单页面（4 态） |
| 状态机驱动 | `DSPageStateLayout(state: DSUiState, content)` | 复杂状态机（8 态） |

详见 [docs/components/00-foundation.md 第 10 节](docs/components/00-foundation.md#10-uistate-状态机范式)。

---

## 6. AI 协作

### 6.1 AI 工具配置

| 工具 | 主索引 | 自动加载 |
|---|---|---|
| Claude Code | `CLAUDE.md` | ✅ |
| OpenCode | `AGENTS.md` | ✅ |

### 6.2 AI 规则按领域拆分

详细规则在 `docs/rules/`：

| 规则 | 文档 |
|---|---|
| 文档自维护协议 | [00-documentation-protocol.md](docs/rules/00-documentation-protocol.md) |
| 项目概述 | [01-project-overview.md](docs/rules/01-project-overview.md) |
| 包隔离 | [02-package-isolation.md](docs/rules/02-package-isolation.md) |
| 命名规范 | [03-naming-conventions.md](docs/rules/03-naming-conventions.md) |
| 路由 | [04-routing.md](docs/rules/04-routing.md) |
| 导航与通信 | [05-navigation-communication.md](docs/rules/05-navigation-communication.md) |
| ViewModel | [06-viewmodel.md](docs/rules/06-viewmodel.md) |
| UI 组件使用规则 | [07-ui-components.md](docs/rules/07-ui-components.md) |
| 状态管理 | [08-state-management.md](docs/rules/08-state-management.md) |
| Feature 模板 | [09-feature-templates.md](docs/rules/09-feature-templates.md) |
| 测试 | [10-testing.md](docs/rules/10-testing.md) |
| CI/CD | [11-ci-cd.md](docs/rules/11-ci-cd.md) |
| 命令 | [12-commands.md](docs/rules/12-commands.md) |
| 故障排查 | [13-troubleshooting.md](docs/rules/13-troubleshooting.md) |
| 开发工作流 | [14-development-workflow.md](docs/rules/14-development-workflow.md) |

### 6.3 Prompt 模板

**新增 Feature**：
```
添加一个商品列表页，支持下拉刷新和分页，用 AppStructuredCard 渲染列表项
```

**新增 DS 组件**：
```
给 designsystem 加一个 DSRatingBar 评分组件，支持半星和只读
```

**修 Bug**：
```
ProductScreen 列表滚动时 FAB 没有收起，参考 DSFAB 的 collapsed 参数
```

AI 会按 `docs/rules/09-feature-templates.md` 模板自动生成代码。

---

## 7. 动效规范

必须用 `DSMotionScheme` 统一动效入口：

```kotlin
val motion = currentDSMotionScheme()

// 进退场动画
AnimatedVisibility(
    visible = isVisible,
    enter = motion.enter().fadeSlideUp(),
    exit = motion.exit().fadeSlideDown()
)

// NavHost 转场
NavHost(
    enterTransition = { motion.pageTransitions.enterTransition() },
    exitTransition = { motion.pageTransitions.exitTransition() }
)

// 按压反馈
Modifier.pressScale()

// 减少动效（无障碍）
provideDSMotionScheme(scheme = reducedMotionScheme()) { AppContent() }
```

详见 [docs/components/00-foundation.md 第 4 节](docs/components/00-foundation.md#4-motion-动效系统)。

---

## 8. 原生 → DS 映射

Detekt 报错时查此表：

| 原生 | DS 替代 | 文档 |
|---|---|---|
| `Button` | `DSButton` | [atoms.md §1.1](docs/components/atoms.md#11-dsbutton--通用按钮) |
| `IconButton` | `DSIconButton` | [atoms.md §1.2](docs/components/atoms.md#11-dsiconbutton--图标按钮) |
| `TextField` | `DSTextField` | [atoms.md §2.1](docs/components/atoms.md#21-dstextfield--文本输入框) |
| `Text` | `DSText` | [atoms.md §3.1](docs/components/atoms.md#3-文本类) |
| `Card` | `DSCard` / `AppCommonCard` / `AppStructuredCard` | [molecules.md §1.1](docs/components/molecules.md#11-dscard--卡片) |
| `Scaffold` | `DSAppScaffold` | [layouts.md §1.1](docs/components/layouts.md#11-dsappscaffold--应用脚手架) |
| `TopAppBar` | `DSTopBar` | [layouts.md §2.1](docs/components/layouts.md#21-dstopbar--顶部应用栏) |
| `NavigationBar` | `DSBottomBar` | [layouts.md §3.1](docs/components/layouts.md#31-dsbottombar--底部导航栏) |
| `AlertDialog` | `DSDialog` | [molecules.md §4.1](docs/components/molecules.md#41-dsdialog--对话框) |
| `FloatingActionButton` | `DSFAB` | [molecules.md §4.9](docs/components/molecules.md#49-dsfab--悬浮按钮) |

完整映射表见 [docs/rules/07-ui-components.md 第 3 节](docs/rules/07-ui-components.md)。

---

## 阅读路径

上一篇：[BUILD.md](BUILD.md) · 下一篇：[docs/components/README.md](docs/components/README.md)
