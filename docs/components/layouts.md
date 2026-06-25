# Layout + Template 页面模板

> 文档版本：v1.0 · 2026-06-25  
> 适用范围：`core/designsystem/shell/` + `core/designsystem/patterns/`  
> 定位：页面级组件，组合多个分子构成页面骨架

---

## 目录

1. [页面骨架](#1-页面骨架)
2. [顶部布局](#2-顶部布局)
3. [底部与侧边布局](#3-底部与侧边布局)
4. [内容切换](#4-内容切换)
5. [状态占位](#5-状态占位)
6. [反馈类](#6-反馈类)
7. [页面模板](#7-页面模板)
8. [速查表](#8-速查表)

---

## 1. 页面骨架

### 1.1 DSAppScaffold — 应用脚手架

```kotlin
@Composable
internal fun DSAppScaffold(
    modifier: Modifier = Modifier,
    title: String? = null,
    topBarStyle: DSTopBarStyle = DSTopBarStyle.Small,
    topBarActions: List<DSTopBarAction> = emptyList(),
    showBackIcon: Boolean = false,
    onBackClick: () -> Unit = {},
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    topBar: (@Composable () -> Unit)? = null,
    useLargeTopBar: Boolean = false,
    contentShouldConsumePadding: Boolean = false,
    content: @Composable (PaddingValues) -> Unit
)
```

**单函数支持所有场景**（无重载）：

| 场景 | 关键参数 |
|---|---|
| 标准页面（顶栏 + 内容） | `title` + `topBarActions` |
| 带返回的详情页 | `title` + `showBackIcon = true` + `onBackClick` |
| 大标题首页 | `title` + `useLargeTopBar = true` |
| 自定义 TopBar | `topBar = { MyCustomTopBar() }` |
| 无顶栏（登录页） | `title = null`（不渲染顶栏） |
| 底栏导航页 | `bottomBar = { DSBottomBar(...) }` |
| FAB 浮动 | `floatingActionButton = { DSFAB(...) }` |
| Snackbar 反馈 | 外部传入 `snackbarHostState` |

**使用示例**：

```kotlin
// 标准页面
DSAppScaffold(
    title = "商品详情",
    showBackIcon = true,
    onBackClick = { navController.popBackStack() },
    topBarActions = listOf(
        DSTopBarAction(Icons.Default.Share, "分享") { vm.share() },
        DSTopBarAction(Icons.Default.Favorite, "收藏") { vm.favorite() }
    )
) { padding ->
    ProductContent(modifier = Modifier.padding(padding))
}

// 大标题 + 底栏 + FAB
DSAppScaffold(
    title = "首页",
    useLargeTopBar = true,
    bottomBar = { DSBottomBar(items = navItems, selectedItem = "首页", onItemSelected = { }) },
    floatingActionButton = { DSFAB(onClick = { /* 新建 */ }, icon = Icons.Default.Add, contentDescription = "新建") }
) { padding ->
    HomeContent(modifier = Modifier.padding(padding))
}

// 自定义 TopBar
DSAppScaffold(
    topBar = { CustomSearchTopBar(query, onQueryChange) }
) { padding ->
    SearchResults(modifier = Modifier.padding(padding))
}

// 无顶栏（登录页）
DSAppScaffold(
    backgroundColor = MaterialTheme.colorScheme.surface
) { padding ->
    LoginForm(modifier = Modifier.padding(padding))
}
```

### 1.2 内置能力

DSAppScaffold 内置：
- **DSTopBar 渲染**：根据 `title` / `topBarStyle` / `useLargeTopBar` 自动渲染
- **SnackbarHost**：使用 `DSSnackbar` 自定义样式
- **edge-to-edge**：内容延伸到状态栏下方
- **滚动折叠**：`useLargeTopBar = true` 时自动添加 `exitUntilCollapsedScrollBehavior`
- **padding 处理**：默认 `content(padding)` 应用 Scaffold padding，`contentShouldConsumePadding = true` 时业务自处理

---

## 2. 顶部布局

### 2.1 DSTopBar — 顶部应用栏

```kotlin
enum class DSTopBarStyle { Small, CenterAligned, Medium, Large }

internal data class DSTopBarAction(
    val icon: ImageVector,
    val contentDescription: String,
    val onClick: () -> Unit,
    val destructive: Boolean = false   // 破坏性操作（图标用 error 色）
)

@Composable
internal fun DSTopBar(
    title: String,
    modifier: Modifier = Modifier,
    style: DSTopBarStyle = DSTopBarStyle.Small,
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    actions: List<DSTopBarAction> = emptyList(),
    scrollBehavior: TopAppBarScrollBehavior? = null
)
```

**4 种风格**：

| Style | 高度 | 用途 |
|---|---|---|
| `Small` | 64dp | 标准顶栏（默认） |
| `CenterAligned` | 64dp | 标题居中（如设置页） |
| `Medium` | 中等 + 大标题 | 可折叠（详情页） |
| `Large` | 大 | 可折叠到 Small（个人主页） |

**自动溢出菜单**：`actions` 超过 2 个时，前 2 个直接显示为 IconButton，剩余收进 MoreVert 菜单。`destructive = true` 的 action 在顶栏直接显示时图标用 error 色，在溢出菜单中文字用 error 色。

**使用示例**：

```kotlin
// 直接使用（一般通过 DSAppScaffold）
DSTopBar(
    title = "设置",
    style = DSTopBarStyle.CenterAligned,
    onBackClick = { navController.popBackStack() },
    actions = listOf(
        DSTopBarAction(Icons.Default.Search, "搜索") { /* ... */ },
        DSTopBarAction(Icons.Default.MoreVert, "更多") { /* ... */ },
        DSTopBarAction(Icons.Default.Delete, "清空", destructive = true) { vm.clear() }
    )
)

// 折叠式（通过 DSAppScaffold 的 useLargeTopBar = true）
DSAppScaffold(
    title = "个人主页",
    useLargeTopBar = true
) { padding ->
    // 滚动时 Large TopBar 自动折叠为 Small
    LazyColumn { /* ... */ }
}
```

### 2.2 DSBottomAppBar — 底部应用栏

```kotlin
@Composable
internal fun DSBottomAppBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    actions: List<DSTopBarAction> = emptyList(),
    fabIcon: ImageVector? = null,
    fabContentDescription: String? = null,
    onFabClick: (() -> Unit)? = null,
    actionsRowHorizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(
        space = DSTokens.Spacing.xs,
        alignment = Alignment.End
    )
)
```

**与 DSTopBar 命名对称**：TopBar 在顶部，BottomAppBar 在底部。底部应用栏适合「底部操作 + FAB 凹槽」场景（如编辑器底部工具栏）。

```kotlin
DSBottomAppBar(
    title = "新建笔记",
    actions = listOf(
        DSTopBarAction(Icons.Default.Search, "搜索") {},
        DSTopBarAction(Icons.Default.Edit, "编辑") {},
        DSTopBarAction(Icons.Default.MoreVert, "更多") {}
    ),
    fabIcon = Icons.Default.Add,
    fabContentDescription = "新建",
    onFabClick = { vm.createNote() }
)
```

---

## 3. 底部与侧边布局

### 3.1 DSBottomBar — 底部导航栏

```kotlin
@Composable
internal fun DSBottomBar(
    items: List<DSNavItem>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    animated: Boolean = false   // true 时启用 spring 指示器 + 图标缩放
)
```

**使用示例**：

```kotlin
val items = listOf(
    DSNavItem(label = "首页", icon = Icons.Default.Home),
    DSNavItem(label = "发现", icon = Icons.Default.Search, badgeText = "5"),
    DSNavItem(label = "我的", icon = Icons.Default.Person)
)
var selected by remember { mutableStateOf("首页") }

// 标准
DSBottomBar(
    items = items,
    selectedItem = selected,
    onItemSelected = { selected = it }
)

// 动画模式
DSBottomBar(
    items = items,
    selectedItem = selected,
    onItemSelected = { selected = it },
    animated = true
)
```

### 3.2 DSNavigationRail — 侧边导航栏

```kotlin
@Composable
internal fun DSNavigationRail(
    items: List<DSNavItem>,
    selectedItem: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    onMenuClick: (() -> Unit)? = null,           // 顶部菜单图标
    fabIcon: ImageVector? = null,
    fabContentDescription: String? = null,
    onFabClick: (() -> Unit)? = null,
    headerLabel: String? = null                  // 顶部品牌标签
)
```

**与 DSBottomBar 互斥使用**：

| 窗口尺寸类 | 推荐导航 |
|---|---|
| Compact（手机竖屏） | DSBottomBar |
| Medium / Expanded（平板/桌面） | DSNavigationRail |

```kotlin
val wsc = rememberDSWindowSizeClass()
Row {
    if (!wsc.useBottomBar) {
        DSNavigationRail(
            items = navItems,
            selectedItem = currentRoute,
            onItemSelected = { navController.navigate(it) },
            onMenuClick = { drawerState.open() },
            headerLabel = "Pai"
        )
    }
    Box(Modifier.weight(1f)) { AppNavGraph() }
}
if (wsc.useBottomBar) {
    DSBottomBar(items = navItems, selectedItem = currentRoute, onItemSelected = { navController.navigate(it) })
}
```

### 3.3 DSModalNavigationDrawer — 模态抽屉

```kotlin
@Composable
internal fun DSModalNavigationDrawer(
    drawerContent: @Composable ColumnScope.() -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    initialDrawerValue: DrawerValue = DrawerValue.Closed,
    drawerState: DrawerState? = null,
    content: @Composable () -> Unit
)
```

**手机端标准侧边抽屉**，从左侧滑出，覆盖主内容并带遮罩。

```kotlin
var showDrawer by remember { mutableStateOf(false) }
DSModalNavigationDrawer(
    drawerContent = {
        // 业务自定义内容
        NavigationDrawerItem(
            label = { Text("首页") },
            selected = true,
            onClick = { /* ... */ },
            icon = { Icon(Icons.Default.Home, null) }
        )
    },
    onDismiss = { showDrawer = false },
    title = "导航菜单",
    drawerState = if (showDrawer) DrawerValue.Open else DrawerValue.Closed
) {
    MainScreen(onMenuClick = { showDrawer = true })
}
```

### 3.4 DSPermanentNavigationDrawer — 常驻抽屉

```kotlin
@Composable
internal fun DSPermanentNavigationDrawer(
    items: List<DSNavItem>,
    selectedItem: String?,
    onItemClick: (DSNavItem) -> Unit,
    modifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
)
```

**平板 / 桌面端常驻抽屉**，与主内容并排，无遮罩。仅在 WindowWidthSizeClass.Expanded 使用。

```kotlin
val wsc = rememberDSWindowSizeClass()
if (wsc.isExpanded) {
    DSPermanentNavigationDrawer(
        items = navItems,
        selectedItem = currentRoute,
        onItemClick = { item -> navController.navigate(item.id) },
        header = {
            Column(Modifier.padding(16.dp)) {
                Text("我的应用", style = MaterialTheme.typography.titleLarge)
            }
        }
    ) {
        AppNavGraph()
    }
} else {
    // 手机端用 DSModalNavigationDrawer
    AppNavGraph()
}
```

---

## 4. 内容切换

### 4.1 DSTabRow — 标签栏

```kotlin
internal data class DSTabItem(
    val title: String,
    val badgeContent: (@Composable () -> Unit)? = null
)

@Composable
internal fun DSTabRow(
    tabs: List<DSTabItem>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    scrollable: Boolean = false,                // > 4 个 Tab 时启用
    containerColor: Color = MaterialTheme.colorScheme.surface
)
```

**注意**：`DSTabItem` 保留独立（与 `DSNavItem` 不合并），因为 `badgeContent` 是 Composable slot，比 DSNavItem 的 String badgeText 更灵活。

```kotlin
val tabs = listOf(
    DSTabItem("推荐"),
    DSTabItem("关注", badgeContent = { Badge { Text("5") } }),
    DSTabItem("热门")
)
var selected by remember { mutableIntStateOf(0) }
DSTabRow(
    tabs = tabs,
    selectedIndex = selected,
    onTabSelected = { selected = it }
)
```

### 4.2 DSTabsWithPager — 标签 + 滑动页面

```kotlin
enum class DSTabPosition { Top, Bottom }

@Composable
internal fun DSTabsWithPager(
    tabs: List<DSTabItem>,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    position: DSTabPosition = DSTabPosition.Top,
    pagerState: PagerState = rememberPagerState(),
    content: @Composable (Int) -> Unit
)
```

标签 + HorizontalPager 联动，滑动切换页面：

```kotlin
DSTabsWithPager(
    tabs = listOf(
        DSTabItem("全部"),
        DSTabItem("未读", badgeContent = { Badge { Text("12") } }),
        DSTabItem("已读")
    ),
    onTabSelected = { }
) { pageIndex ->
    when (pageIndex) {
        0 -> AllMessagesList()
        1 -> UnreadMessagesList()
        2 -> ReadMessagesList()
    }
}
```

### 4.3 DSSearchBar — 搜索栏

详见 [molecules.md 第 4.10 节](molecules.md#410-dssearchbar--搜索栏)。

---

## 5. 状态占位

### 5.1 DSNetWorkView — 网络状态容器

```kotlin
data class DSErrorData(val message: String, val throwable: Throwable? = null)
data class DSEmptyData(val title: String = "暂无数据", val description: String? = null)

@Composable
fun DSNetWorkView(
    isLoading: Boolean = false,
    error: DSErrorData? = null,
    empty: DSEmptyData? = null,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
    padding: PaddingValues = PaddingValues(),
    customLoading: (@Composable () -> Unit)? = null,
    customError: (@Composable () -> Unit)? = null,
    customEmpty: (@Composable () -> Unit)? = null,
    content: @Composable () -> Unit
)
```

**状态机容器**，根据 `isLoading` / `error` / `empty` 自动切换显示：

```kotlin
DSNetWorkView(
    isLoading = uiState is Loading,
    error = (uiState as? Error)?.let { DSErrorData(it.message) },
    empty = if (data.isEmpty()) DSEmptyData() else null,
    onRetry = { viewModel.retry() }
) {
    ContentList(data)
}
```

### 5.2 DSFullScreenLoading — 全屏加载

```kotlin
@Composable
fun DSFullScreenLoading(
    message: String? = null,
    withScrim: Boolean = false,
    modifier: Modifier = Modifier
)
```

**两种模式**：

| 参数组合 | 行为 | 用途 |
|---|---|---|
| `message = null, withScrim = false` | 纯居中加载，无遮罩 | 页面初次进入 |
| `message = "提交中...", withScrim = true` | 半透明遮罩 + 居中 Card | 表单提交（阻断交互） |

```kotlin
// 页面初次加载
when (val result = uiState.result) {
    is Loading -> DSFullScreenLoading()
    is Success -> Content(result.data)
    is Error -> DSFullScreenError(result.message) { retry() }
}

// 表单提交遮罩
if (isSubmitting) {
    DSFullScreenLoading(message = "提交中...", withScrim = true)
}
```

### 5.3 DSFullScreenError — 全屏错误

```kotlin
@Composable
fun DSFullScreenError(
    message: String,
    onRetry: () -> Unit
)
```

居中显示 CloudOff 图标 + 错误文案 + 重试按钮。

### 5.4 DSFullScreenEmpty — 全屏空状态

```kotlin
@Composable
fun DSFullScreenEmpty(
    title: String = "暂无数据",
    description: String? = null,
    icon: ImageVector = Icons.Default.Inbox,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
)
```

居中显示图标 + 标题 + 描述 + 可选操作按钮。

### 5.5 DSEmptyState — 空状态（基础组件）

```kotlin
@Composable
internal fun DSEmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String,
    description: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
)
```

**与 DSFullScreenEmpty 的关系**：DSFullScreenEmpty 内部用 DSEmptyState 实现，再加一层 `Box(fillMaxSize, Center)` 居中。如果业务需要在卡片内或列表内显示空状态，直接用 DSEmptyState。

### 5.6 DSBanner — 横幅提示

```kotlin
typealias DSBannerType = DSMessageType  // Info / Success / Warning / Error

@Composable
internal fun DSBanner(
    modifier: Modifier = Modifier,
    message: String,
    type: DSBannerType = DSBannerType.Info,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
)
```

**4 种类型**（基于统一 DSMessageType）：

```kotlin
DSBanner(
    message = "网络连接已断开",
    type = DSMessageType.Warning,
    actionText = "重试",
    onActionClick = { retry() },
    onDismiss = { hideBanner() }
)
```

---

## 6. 反馈类

### 6.1 DSSnackbar — 底部提示条

详见 [molecules.md 第 4.8 节](molecules.md#48-dssnackbar--反馈条)。通过 DSAppScaffold 的 snackbarHostState 触发。

### 6.2 进度指示器

详见 [atoms.md 第 5 节 反馈类](atoms.md#5-反馈类)：DSLinearProgress / DSCircularProgress / DSSkeleton。

---

## 7. 页面模板

### 7.1 模板 A：详情页（Detail Page）

适用场景：商品详情、订单详情、文章详情

```kotlin
@Composable
fun DetailPageScreen(itemId: String, viewModel: DetailViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    DSAppScaffold(
        title = "详情",
        showBackIcon = true,
        onBackClick = { navController.popBackStack() },
        topBarActions = listOf(
            DSTopBarAction(Icons.Default.Share, "分享") { vm.share() },
            DSTopBarAction(Icons.Default.MoreVert, "更多") { /* showMenu */ }
        )
    ) { padding ->
        DSNetWorkView(
            isLoading = uiState is Loading,
            error = (uiState as? Error)?.let { DSErrorData(it.message) },
            onRetry = { vm.retry() }
        ) {
            val data = (uiState as Success).data
            LazyColumn(modifier = Modifier.padding(padding)) {
                item { HeroImage(data.imageUrl) }
                item { TitleSection(data) }
                item { DescriptionSection(data.description) }
                item { ActionRow(data) }
            }
        }
    }
}
```

### 7.2 模板 B：纯状态页（State Page）

适用场景：搜索结果页、消息页

```kotlin
@Composable
fun StatePageScreen(viewModel: StateViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    DSAppScaffold(title = "消息") { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when (val state = uiState) {
                is Loading -> DSFullScreenLoading()
                is Error -> DSFullScreenError(state.message) { vm.retry() }
                is Empty -> DSFullScreenEmpty(description = "暂无消息", actionText = "刷新", onActionClick = { vm.refresh() })
                is Success -> MessageList(state.data)
            }
        }
    }
}
```

### 7.3 模板 C：表单页（Form Page）

适用场景：编辑资料、新建地址、提交反馈

```kotlin
@Composable
fun FormPageScreen(viewModel: FormViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    
    DSAppScaffold(
        title = "编辑资料",
        showBackIcon = true,
        onBackClick = { navController.popBackStack() },
        snackbarHostState = snackbarHostState
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            AppCommonCard(title = "基本信息") {
                DSTextField(value = name, onValueChange = { }, label = "姓名")
                DSTextField(value = email, onValueChange = { }, label = "邮箱")
            }
            AppCommonCard(title = "头像") {
                DSAvatar(imageUrl = avatar, size = DSAvatarSize.XL)
                DSButton(text = "更换头像", onClick = { /* pick image */ })
            }
            DSButton(
                text = "保存",
                onClick = {
                    scope.launch {
                        vm.save()
                        snackbarHostState.showSnackbar("保存成功")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        if (uiState.isSubmitting) {
            DSFullScreenLoading(message = "保存中...", withScrim = true)
        }
    }
}
```

### 7.4 模板 D：列表分页页（List + Pagination Page）

适用场景：商品列表、消息列表、订单列表

```kotlin
@Composable
fun ListPageScreen(viewModel: ListViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    DSAppScaffold(
        title = "商品列表",
        bottomBar = { DSBottomBar(items = navItems, selectedItem = "list", onItemSelected = { }) }
    ) { padding ->
        DSPullToRefresh(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { vm.refresh() },
            modifier = Modifier.padding(padding)
        ) {
            DSNetWorkView(
                isLoading = uiState.isLoading,
                error = uiState.error,
                onRetry = { vm.retry() }
            ) {
                LazyColumn {
                    items(uiState.items, key = { it.id }) { item ->
                        AppStructuredCard(
                            data = AppStructuredCardData(
                                title = item.name,
                                subtitle = item.subtitle,
                                leadingAvatarUrl = item.imageUrl,
                                trailingMeta = "¥${item.price}"
                            ),
                            onClick = { navController.navigate(DetailRoute(item.id)) }
                        )
                    }
                    item {
                        DSPagination(
                            hasMore = uiState.hasMore,
                            isLoading = uiState.isLoadingMore,
                            onLoadMore = { vm.loadMore() }
                        )
                    }
                }
            }
        }
    }
}
```

### 7.5 模板 E：设置页（Settings Page）

适用场景：用户设置、应用配置

```kotlin
@Composable
fun SettingsPageScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    DSAppScaffold(
        title = "设置",
        style = DSTopBarStyle.CenterAligned
    ) { padding ->
        Column(modifier = Modifier.padding(padding).verticalScroll(rememberScrollState())) {
            // 通用设置
            AppCommonCard(title = "通用") {
                Column {
                    AppStructuredCard(
                        data = AppStructuredCardData(
                            title = "通知",
                            subtitle = "已开启 3 项",
                            leadingIcon = Icons.Default.Notifications,
                            trailingMetaIcon = Icons.Default.ChevronRight
                        ),
                        onClick = { /* ... */ }
                    )
                    AppStructuredCard(
                        data = AppStructuredCardData(
                            title = "语言",
                            subtitle = "简体中文",
                            leadingIcon = Icons.Default.Language,
                            trailingMetaIcon = Icons.Default.ChevronRight
                        ),
                        onClick = { /* ... */ }
                    )
                }
            }
            
            // 主题
            AppCommonCard(title = "主题") {
                DSSwitch(checked = isDark, onCheckedChange = { vm.toggleDark() }, label = "深色模式")
                DSSwitch(checked = isAmoled, onCheckedChange = { vm.toggleAmoled() }, label = "AMOLED 纯黑")
            }
            
            // 退出
            AppStructuredCard(
                data = AppStructuredCardData(
                    title = "退出登录",
                    leadingIcon = Icons.Default.Logout,
                    destructive = true
                ),
                onClick = { vm.logout() }
            )
        }
    }
}
```

### 7.6 模板 F：主从页（Master-Detail）

适用场景：平板/桌面端邮件、文件管理

```kotlin
@Composable
fun MasterDetailScreen(viewModel: MainViewModel = hiltViewModel()) {
    val wsc = rememberDSWindowSizeClass()
    val selectedItem by viewModel.selectedItem.collectAsStateWithLifecycle()
    
    if (wsc.isExpanded) {
        // 平板：左右分栏
        Row(Modifier.fillMaxSize()) {
            DSPermanentNavigationDrawer(
                items = navItems,
                selectedItem = "inbox",
                onItemClick = { }
            ) {
                MasterListPane(modifier = Modifier.weight(0.4f))
            }
            DetailPane(selectedItem, modifier = Modifier.weight(0.6f))
        }
    } else {
        // 手机：单栏，点击跳转
        DSAppScaffold(
            title = "收件箱",
            bottomBar = { DSBottomBar(...) }
        ) { padding ->
            MasterListPane(modifier = Modifier.padding(padding))
        }
    }
}
```

---

## 8. 速查表

### 8.1 布局组件清单（共 16 个）

| 类别 | 组件 | 文件 |
|---|---|---|
| 骨架 | DSAppScaffold | DSAppScaffold.kt |
| 顶部 | DSTopBar / DSBottomAppBar | DSTopBar.kt / DSBottomAppBar.kt |
| 底部 | DSBottomBar | DSBottomBar.kt |
| 侧边 | DSNavigationRail / DSModalNavigationDrawer / DSPermanentNavigationDrawer | DSNavigationRail.kt / DSModalNavigationDrawer.kt / DSPermanentNavigationDrawer.kt |
| 内容切换 | DSTabRow / DSTabsWithPager / DSSearchBar | DSTabs.kt / DSTabsWithPager.kt / DSSearchBar.kt |
| 状态占位 | DSNetWorkView / DSFullScreenLoading / DSFullScreenError / DSFullScreenEmpty / DSEmptyState | DSNetWorkView.kt / DSFullScreenStates.kt / DSEmptyState.kt |
| 反馈 | DSBanner | DSBanner.kt |

### 8.2 页面模板清单（共 6 个）

| 模板 | 用途 | 关键组件 |
|---|---|---|
| A 详情页 | 商品/订单/文章详情 | DSAppScaffold + DSNetWorkView + LazyColumn |
| B 纯状态页 | 搜索/消息 | DSAppScaffold + DSFullScreen* |
| C 表单页 | 编辑/新建 | DSAppScaffold + AppCommonCard + DSTextField + DSFullScreenLoading(withScrim) |
| D 列表分页页 | 商品/消息列表 | DSAppScaffold + DSPullToRefresh + DSNetWorkView + LazyColumn + AppStructuredCard + DSPagination |
| E 设置页 | 用户设置 | DSAppScaffold + AppCommonCard + AppStructuredCard |
| F 主从页 | 平板邮件/文件 | DSPermanentNavigationDrawer + 双 Pane + DSWindowSizeClass |

### 8.3 响应式布局决策树

```
窗口尺寸类是 Compact 吗？
├─ 是 → 用 DSBottomBar + DSModalNavigationDrawer（按需）
└─ 否（Medium/Expanded）
   ├─ 是 Expanded 吗？
   │  ├─ 是 → DSPermanentNavigationDrawer 常驻 + DSNavigationRail
   │  └─ 否 → DSNavigationRail + DSModalNavigationDrawer（按需）
   └─ 内容布局：DSResponsiveLayout 自动切换 Column/Row
```

### 8.4 状态占位决策树

```
页面有几个状态？
├─ 1 个（纯加载/纯错误/纯空） → 直接用 DSFullScreenLoading / DSFullScreenError / DSFullScreenEmpty
├─ 多个（加载 + 错误 + 空 + 成功） → DSNetWorkView（自动切换）
└─ 仅卡片内空状态 → DSEmptyState（不居中，跟卡片布局走）
```

### 8.5 顶栏风格选择

```
标题长度
├─ 短（≤ 8 字） → DSTopBarStyle.Small 或 CenterAligned
├─ 中（9~16 字） → DSTopBarStyle.Medium（折叠）
└─ 长 / 大标题（如个人主页） → DSTopBarStyle.Large（折叠）

是否需要居中？
├─ 设置页 / 简单列表 → CenterAligned
└─ 其他 → Small / Medium / Large
```
