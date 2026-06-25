# 新增 Feature 流程与页面模板

> AI 规则文件 - Feature 模板领域
> 本文件由 CLAUDE.md 拆分而来，对应原 CLAUDE.md 第 11 章。
> 本文件包含 4 个页面模板（A/B/C/D），AI 严格遵循。

---

## 零、用生成器而非手抄（推荐）

> **AI-first**：本项目提供 3 个 Bash 生成器，**优先用生成器**而非手抄本文件的模板。
> 生成器输出的代码已严格遵守 ktlint / detekt / Konsist 红线（含 P1-1 接口/实现分离、
> `internal` 可见性、DS 组件、类型安全路由等），手抄易漏 import / 错放包 / 忘记 `@Binds`。

### 1. `scripts/new-feature.sh <name> [--with-repository] [--dry-run] [--force]`

生成完整 feature 骨架（ViewModel + Screen + UiState + Route + gotoXxx + AppNavGraph 注册 + 测试）。
`--with-repository` 同时调用 `new-repository.sh` 生成数据层。

```bash
./scripts/new-feature.sh product --with-repository      # 实际生成
./scripts/new-feature.sh order-detail --dry-run         # 预览文件清单
```

生成内容：
- `feature/<name>/<Name>ViewModel.kt` —— `@HiltViewModel internal class <Name>ViewModel @Inject constructor(navigator: AppNavigator, userState: UserState) : BaseViewModel(navigator, userState)`
- `feature/<name>/<Name>Screen.kt` —— `@Composable internal fun <Name>Screen(...) { DSAppScaffold(...) }`（DS 组件，禁止 M3 原生）
- `app/src/test/.../feature/<name>/<Name>ViewModelTest.kt` —— MockK + MainDispatcherRule 骨架
- 自动编辑 `AppRoutes.kt`（追加 `@Serializable data object <Name>Route : AppRoute`）
- 自动编辑 `NavExtensions.kt`（追加 `internal fun AppNavigator.goto<Name>() = navigate(<Name>Route)`）
- 自动编辑 `MainActivity.kt`（在 `AppNavGraph` 内追加 `composable<<Name>Route> { <Name>Screen(...) }`）
- 提示手动追加 `docs/rules/04-routing.md` 路由表行

### 2. `scripts/new-component.sh <DSName> <domain> [--dry-run] [--force]`

生成 DS 组件（Composable + 3 个 @Preview light/dark/AMOLED + 截图测试骨架）。
`domain ∈ {primitives, patterns, shell, overlays}`（详见 `07-ui-components.md` 第五节）。

```bash
./scripts/new-component.sh DSRatingBar primitives
./scripts/new-component.sh DSBanner patterns --dry-run
```

生成内容：
- `core/designsystem/<domain>/<DSName>.kt` —— `@Composable internal fun <DSName>(...)`（M3 substrate + DSTokens，对外不暴露 M3 类型）
- 3 个 `@Preview`：Light / Dark / AMOLED，均包裹在 `DSDesignTheme` 内
- `app/src/test/.../<DSName>ScreenshotTest.kt` —— Paparazzi 占位骨架（`@Ignore`，待 P0-2 启用）
- 提示手动追加 `docs/rules/07-ui-components.md` 映射表行

### 3. `scripts/new-repository.sh <name> [--dry-run] [--force]`

生成数据层全套（**P1-1 接口/实现分离**，KMP-ready）。

```bash
./scripts/new-repository.sh comment
./scripts/new-repository.sh product --dry-run
```

生成内容：
- `core/network/model/<Name>Dto.kt` —— `@Serializable internal data class <Name>Dto`
- `core/database/entity/<Name>Entity.kt` —— `@Entity internal data class <Name>Entity`
- `core/database/dao/<Name>Dao.kt` —— `@Dao internal interface <Name>Dao`
- `core/domain/model/<Name>Item.kt` —— `data class <Name>Item`（**KMP-ready 纯 Kotlin**，无 Android import）
- `core/domain/<Name>Repository.kt` —— `interface <Name>Repository`（KMP-ready，仅 import `ApiResult` + `<Name>Item` + `Flow`）
- `core/data/<Name>RepositoryImpl.kt` —— `@Singleton class <Name>RepositoryImpl @Inject constructor(api, dao) : <Name>Repository`（`safeApiCall` 包装 + Entity→Item 映射；`syncToCache` 保留为 Impl 具体方法）
- `app/src/test/.../core/data/<Name>RepositoryTest.kt` —— MockK 骨架，构造 Impl、断言接口
- 自动编辑 `AppDatabase.kt`（entities 数组追加 + version+1 + abstract dao + imports）
- 自动编辑 `DatabaseModule.kt`（追加 `@Provides fun provide<Name>Dao`）
- 自动编辑 `AppApi.kt`（追加 `@GET suspend fun get<Name>s(): List<<Name>Dto>`）
- 自动编辑 `core/data/di/DataModule.kt`（追加 `@Binds @Singleton abstract fun bind<Name>Repository(impl): <Name>Repository`）
- 提示手动追加 `AppApiTest.kt` MockWebServer 用例 + Room Migration 注册

### 通用约定

- 所有脚本 `set -eo pipefail`，支持 `--help` / `--dry-run` / `--force`
- 验证项目根（含 `app/build.gradle.kts` + `settings.gradle.kts`）
- 名称校验：feature/repository 用小写 kebab-case；DS 组件用 `DS` 前缀 PascalCase
- 颜色输出 TTY-aware（CI 非 TTY 自动关闭颜色码）
- 幂等：重复运行会跳过已存在文件（除非 `--force`）

### 何时仍需手抄

生成器输出的是骨架，业务方仍需手动：
1. 填充真实业务字段（DTO / Entity / Item / UiState）
2. 选择并应用下文模板 A/B/C/D（生成器默认是模板 B 纯状态页，需网络请求时按模板 A/C/D 调整）
3. 调整 API 路径 / SQL 查询 / 业务方法签名
4. 删除占位 `// TODO` 注释

---

## 一、通用流程（6 步）

> **推荐用 `./scripts/new-feature.sh <name>` 自动完成下列 6 步**。下列手抄步骤仅在
> 生成器无法满足特殊需求（如带参路由、自定义 startDestination 决策）时使用。

当用户说「新增一个 XXX 页面 / feature」时，AI 必须按以下 6 步：

1. 创建 `feature/[name]/` 包
2. 创建 `[Name]ViewModel.kt`（含 `[Name]UiState` + `[Name]ViewModel` 继承 `BaseViewModel` 或 `BaseNetWorkViewModel`，`internal`）
3. 创建 `[Name]Screen.kt`（`internal`）
4. 在 `navigation/routes/AppRoutes.kt` 加 `@Serializable data object [Name]Route : AppRoute`
5. 在 `navigation/extension/NavExtensions.kt` 加 `internal fun AppNavigator.goto[Name]()`（走 AppNavigator.navigate，RouteInterceptor 生效）
6. 在 `MainActivity.kt` 的 `AppNavGraph` 挂载 `composable<[Name]Route> { [Name]Screen(...) }`

---

## 二、如需数据

> **推荐用 `./scripts/new-repository.sh <name>` 自动生成下列全套**（P1-1 接口/实现分离）。

- DTO 放 `core/network/model/`（参考 `ExampleDto.kt` 模板，业务方删除示例）
- Entity 放 `core/database/entity/`（参考 `ExampleEntity.kt` 模板）
- DAO 放 `core/database/dao/`（参考 `ExampleDao.kt` 模板）
- **domain model 放 `core/domain/model/`**（决策 P1-1：KMP-ready 纯 Kotlin，如 `ExampleItem.kt`）
- **Repository 接口放 `core/domain/`**（决策 P1-1：`interface XxxRepository`，KMP-ready，无 Android/Retrofit/Room 依赖）
- **Repository 实现放 `core/data/`**（决策 P1-1：`@Singleton class XxxRepositoryImpl @Inject constructor(api, dao) : XxxRepository`，由 `core/data/di/DataModule.kt` 的 `@Binds` 绑定到接口）
- ViewModel 注入 Repository **接口**（非 Impl），**禁止**直接使用 Retrofit / Room

---

## 三、模板选择决策树

```
是否纯状态页面（无网络请求）？
├── 是 → 模板 B（纯状态页）
└── 否
    ├── 单次网络请求 + 详情页 → 模板 A（BaseNetWorkViewModel + DSNetWorkView）
    ├── 表单提交 + 字段校验 → 模板 C（表单页）
    └── 分页列表 + 下拉刷新 + 上拉加载 → 模板 D（列表分页页）
```

---

## 四、模板 A：网络请求详情页（推荐用 BaseNetWorkViewModel）

AI 生成详情页时**必须**按以下模板：

```kotlin
// === [Name]ViewModel.kt ===
@HiltViewModel
internal class [Name]ViewModel @Inject constructor(
    navigator: AppNavigator,
    userState: UserState,
    private val repository: [Name]Repository,
) : BaseNetWorkViewModel<[Name]Data>(navigator, userState) {

    override fun requestApiFlow(): Flow<ApiResult<[Name]Data>> {
        return repository.get[Name]().asResult()
    }

    init { executeRequest() }
}

// === [Name]Screen.kt ===
@Composable
internal fun [Name]Screen(
    onBackClick: () -> Unit,
    viewModel: [Name]ViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DSAppScaffold(
        title = "[Name]",
        showBackIcon = true,
        onBackClick = onBackClick,
    ) { padding ->
        DSNetWorkView(
            isLoading = uiState is BaseNetWorkUiState.Loading,
            error = (uiState as? BaseNetWorkUiState.Error)?.let { DSErrorData(it.message) },
            empty = if (uiState is BaseNetWorkUiState.Empty) DSEmptyData() else null,
            onRetry = viewModel::retryRequest,
            modifier = Modifier.padding(padding),
        ) {
            val data = (uiState as BaseNetWorkUiState.Success).data
            [Name]Content(data)
        }
    }
}
```

### 关键点

- ViewModel 继承 `BaseNetWorkViewModel<T>`，只需实现 `requestApiFlow(): Flow<ApiResult<T>>`
- 用 `.asResult()` 包装 Flow（返回 `Flow<ApiResult<T>>`）
- UI 用 `DSNetWorkView` 一行搞定三态切换
- **禁止**手动写 `when(result) { Loading → ...; Success → ...; Error → ... }`

---

## 五、模板 B：纯状态页（无网络请求，如设置页）

```kotlin
// === [Name]ViewModel.kt ===
internal data class [Name]UiState(
    val field1: String = "",
    val field2: Boolean = false,
)

@HiltViewModel
internal class [Name]ViewModel @Inject constructor(
    navigator: AppNavigator,
    userState: UserState,
) : BaseViewModel(navigator, userState) {

    private val _uiState = MutableStateFlow([Name]UiState())
    val uiState: StateFlow<[Name]UiState> = _uiState.asStateFlow()
}

// === [Name]Screen.kt ===
@Composable
internal fun [Name]Screen(
    onBackClick: () -> Unit,
    viewModel: [Name]ViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    DSAppScaffold(
        title = "[Name]",
        showBackIcon = true,
        onBackClick = onBackClick,
    ) { padding ->
        // 直接渲染 uiState
    }
}
```

适用场景：设置页、关于页、纯展示页面（无网络请求）。

---

## 六、模板 C：表单页（带校验）

> **表单页例外（H16）**：含表单字段 / 复合状态的页面**允许**继承 `BaseViewModel` 手写 `when(ApiResult)` 处理——因 `BaseNetWorkViewModel` 的纯三态抽象不适用。需在 KDoc 注明例外理由。

```kotlin
// === [Name]ViewModel.kt ===
internal data class [Name]UiState(
    val field1: String = "",
    val field1Error: String? = null,
    val isLoading: Boolean = false,
)

@HiltViewModel
internal class [Name]ViewModel @Inject constructor(
    navigator: AppNavigator,
    userState: UserState,
    private val repository: [Name]Repository,
) : BaseViewModel(navigator, userState) {

    private val _uiState = MutableStateFlow([Name]UiState())
    val uiState: StateFlow<[Name]UiState> = _uiState.asStateFlow()

    fun onField1Change(value: String) {
        _uiState.update { it.copy(field1 = value, field1Error = null) }
    }

    fun submit() {
        // 校验
        if (_uiState.value.field1.isBlank()) {
            _uiState.update { it.copy(field1Error = "不能为空") }
            return
        }
        // 提交
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = repository.submit(_uiState.value.field1)) {
                is ApiResult.Loading -> {} // safeApiCall 同步返回，不会触发
                is ApiResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    appNavigator.setResult("[name]Result", result.data.id)
                    appNavigator.navigateBack()
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, field1Error = result.exception.message) }
                }
            }
        }
    }
}
```

适用场景：登录页、注册页、编辑页（带字段校验与提交）。参考 `feature/auth/AuthViewModel.kt`。

---

## 七、模板 D：列表分页页（新增）

适用场景：商品列表、文章列表、消息列表（分页加载 + 下拉刷新 + 上拉加载更多 + 错误重试 + 空数据）。

```kotlin
// === [Name]ViewModel.kt ===
internal data class [Name]UiState(
    val items: List<[Name]Item> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val hasMore: Boolean = true,
    val currentPage: Int = 1,
)

@HiltViewModel
internal class [Name]ViewModel @Inject constructor(
    navigator: AppNavigator,
    userState: UserState,
    private val repository: [Name]Repository,
) : BaseViewModel(navigator, userState) {

    private val _uiState = MutableStateFlow([Name]UiState())
    val uiState: StateFlow<[Name]UiState> = _uiState.asStateFlow()

    private val pageSize = 20

    init { loadFirstPage() }

    fun loadFirstPage() {
        _uiState.update { it.copy(isLoading = true, error = null, currentPage = 1) }
        viewModelScope.launch {
            when (val result = repository.getItems(page = 1, size = pageSize)) {
                is ApiResult.Success -> {
                    val items = result.data
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            items = items,
                            hasMore = items.size >= pageSize,
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            when (val result = repository.getItems(page = 1, size = pageSize)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isRefreshing = false,
                            items = result.data,
                            currentPage = 1,
                            hasMore = result.data.size >= pageSize,
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isRefreshing = false) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
        _uiState.update { it.copy(isLoadingMore = true) }
        viewModelScope.launch {
            val nextPage = _uiState.value.currentPage + 1
            when (val result = repository.getItems(page = nextPage, size = pageSize)) {
                is ApiResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoadingMore = false,
                            items = it.items + result.data,
                            currentPage = nextPage,
                            hasMore = result.data.size >= pageSize,
                        )
                    }
                }
                is ApiResult.Error -> {
                    _uiState.update { it.copy(isLoadingMore = false, hasMore = false) }
                }
                is ApiResult.Loading -> {}
            }
        }
    }

    fun retry() = loadFirstPage()
}

// === [Name]Screen.kt ===
@Composable
internal fun [Name]Screen(
    onItemClick: (Long) -> Unit,
    onBackClick: () -> Unit,
    viewModel: [Name]ViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // 上拉加载更多检测
    LaunchedEffect(listState, uiState.items.size) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible >= uiState.items.size - 3
        }.collect { shouldLoad ->
            if (shouldLoad && uiState.hasMore && !uiState.isLoadingMore && !uiState.isLoading) {
                viewModel.loadMore()
            }
        }
    }

    DSAppScaffold(
        title = "[Name]",
        showBackIcon = true,
        onBackClick = onBackClick,
    ) { padding ->
        when {
            uiState.isLoading -> DSFullScreenLoading(modifier = Modifier.padding(padding))
            uiState.error != null -> DSFullScreenError(
                message = uiState.error,
                onRetry = viewModel::retry,
                modifier = Modifier.padding(padding),
            )
            uiState.items.isEmpty() -> DSFullScreenEmpty(
                title = "暂无数据",
                modifier = Modifier.padding(padding),
            )
            else -> DSPullToRefresh(
                isRefreshing = uiState.isRefreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.padding(padding),
            ) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(
                        horizontal = DSTokens.Spacing.lg,
                        vertical = DSTokens.Spacing.md,
                    ),
                    verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
                ) {
                    itemsIndexed(uiState.items, key = { _, item -> item.id }) { index, item ->
                        [Name]Card(
                            item = item,
                            onClick = { onItemClick(item.id) },
                            modifier = Modifier.listItemEnterAnimation(index),
                        )
                    }
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(DSTokens.Spacing.lg),
                                contentAlignment = Alignment.Center,
                            ) {
                                DSCircularProgress(size = DSTokens.IconSize.sm)
                            }
                        }
                    }
                }
            }
        }
    }
}
```

### 关键点

- `UiState` 包含 7 个字段：`items` / `isLoading`（首屏）/ `isRefreshing`（下拉）/ `isLoadingMore`（上拉）/ `error` / `hasMore` / `currentPage`
- `loadFirstPage()`：首屏加载，显示全屏 Loading
- `refresh()`：下拉刷新，仅显示刷新指示器，不显示全屏 Loading
- `loadMore()`：上拉加载，自动防重入（`isLoadingMore || !hasMore` 短路）
- UI 三态切换：`isLoading` → `DSFullScreenLoading`；`error` → `DSFullScreenError`；`items.isEmpty()` → `DSFullScreenEmpty`
- 列表项点击通过 `onItemClick: (Long) -> Unit` 回调到 `AppNavGraph` 跳转详情
- 上拉加载更多用 `snapshotFlow` 监听 `LazyListState` 可见项最后一个 index，距底部 ≤ 3 项时触发
- 列表项进场动画用 `Modifier.listItemEnterAnimation(index)`（仅前 20 项触发，详见 `07-ui-components.md`）
- `key = { _, item -> item.id }` 保证分页加载时复用 Composable，避免闪烁

### 与其他模板的关系

- 模板 D 与模板 A 的区别：A 是单次详情请求；D 是分页列表请求
- 模板 D 不用 `BaseNetWorkViewModel`（因为状态字段更多，且需手动控制加载更多节奏）
- 模板 D 用 `ApiResult` 三态分支处理（见 `06-viewmodel.md`）

---

## 八、业务方拉取脚手架后的清理步骤

> 推荐：直接用 `./scripts/new-repository.sh <name>` 生成真实业务 Repository，
> 然后删除下列 Example* 示例文件即可。

1. 删除示例文件：`ExampleDto.kt` / `ExampleEntity.kt` / `ExampleDao.kt` /
   `core/domain/ExampleRepository.kt`（接口）/ `core/data/ExampleRepositoryImpl.kt`（实现）/
   `core/domain/model/ExampleItem.kt`（domain model）
2. 删除 `AppApi` 中的示例方法，添加真实业务接口
3. 删除 `AppDatabase` 中的 `ExampleEntity` 注册，添加真实 Entity（version +1）
4. 删除 `DatabaseModule` 中的 `provideExampleDao`，添加真实 DAO 的 `@Provides`
5. 删除 `DataModule` 中的 `bindExampleRepository`，添加真实 Repository 的 `@Binds`
6. 替换 `AuthRepositoryImpl.login`（`core/data/AuthRepositoryImpl.kt`）为真实后端登录 API
7. 替换 `feature/home/HomeScreen.kt` 为真实业务首页

---

## 九、新增模板的规则

如果业务场景不属于现有 4 个模板（A/B/C/D），AI 应：

1. 实现业务代码
2. 在本文件追加「模板 E：[场景描述]」
3. 在最终回复中说明：「已更新 docs/rules/09-feature-templates.md，追加模板 E」

详见 `00-documentation-protocol.md`。
