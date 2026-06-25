# Molecule 分子组件

> 文档版本：v1.0 · 2026-06-25  
> 适用范围：`core/designsystem/primitives/` 中的分子组件 + `overlays/DSChip`  
> 定位：多个原子组件组合，有明确单一职责

---

## 目录

1. [容器型](#1-容器型)
2. [输入型](#2-输入型)
3. [装饰型](#3-装饰型)
4. [浮层类](#4-浮层类-overlay)
5. [速查表](#5-速查表)

---

## 1. 容器型

### 1.1 DSCard — 卡片

```kotlin
enum class DSCardStyle { Elevated, Filled, Outlined }

@Composable
internal fun DSCard(
    modifier: Modifier = Modifier,
    style: DSCardStyle = DSCardStyle.Elevated,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
)
```

**3 种风格**：

| Style | 视觉 | 用途 |
|---|---|---|
| `Elevated` | 默认阴影 | 普通卡片（默认） |
| `Filled` | 背景色填充，无阴影 | 列表项卡 / 区分内容 |
| `Outlined` | 描边，无阴影 | 设置项卡 / 强调边界 |

**使用示例**：

```kotlin
// 静态卡片
DSCard(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp)) {
        DSText("标题", variant = DSTextVariant.TitleMedium)
        DSText("内容描述", variant = DSTextVariant.BodyMedium)
    }
}

// 可点击卡片
DSCard(
    modifier = Modifier.fillMaxWidth(),
    style = DSCardStyle.Elevated,
    onClick = { navController.navigate(DetailRoute(item.id)) }
) {
    // 内容
}

// 列表项卡（无阴影）
DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Filled) {
    Row(modifier = Modifier.padding(16.dp)) {
        DSAvatar(imageUrl = user.avatar)
        Spacer(modifier = Modifier.width(12.dp))
        Column { /* ... */ }
    }
}
```

**业务容器壳**：需要更结构化的卡片时，使用 [AppCommonCard / AppStructuredCard](containers.md)。

### 1.2 DSListItem — 列表项

```kotlin
enum class DSListItemVariant { OneLine, TwoLine, ThreeLine }

@Composable
internal fun DSListItem(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    variant: DSListItemVariant = DSListItemVariant.OneLine,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true
)
```

**3 种变体**：

| Variant | 用途 |
|---|---|
| `OneLine` | 简单项（仅标题） |
| `TwoLine` | 标题 + 副标题（默认） |
| `ThreeLine` | 标题 + 长副标题（多行） |

**使用示例**：

```kotlin
DSListItem(
    title = "张三",
    subtitle = "产品经理",
    variant = DSListItemVariant.TwoLine,
    leadingIcon = Icons.Default.Person,
    trailingIcon = Icons.Default.ChevronRight,
    onClick = { /* ... */ }
)
```

### 1.3 DSGrid — 网格

```kotlin
internal class DSGridScope {
    fun item(span: Int = 1, content: @Composable () -> Unit)
    fun items(count: Int, span: (Int) -> Int = { 1 }, content: @Composable (Int) -> Unit)
}

@Composable
internal fun DSGrid(
    columns: Int,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(DSTokens.Spacing.md),
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(DSTokens.Spacing.md),
    content: @Composable DSGridScope.() -> Unit
)
```

**使用示例**：

```kotlin
// 简单网格
DSGrid(columns = 3) {
    items(9) { index ->
        DSCard { DSText("Item $index") }
    }
}

// 不规则跨列
DSGrid(columns = 4) {
    item(span = 2) { BigCard() }     // 占 2 列
    item(span = 1) { SmallCard() }
    item(span = 1) { SmallCard() }
}

// 响应式列数
val wsc = rememberDSWindowSizeClass()
DSGrid(columns = wsc.recommendedGridColumns) {
    items(products) { product ->
        ProductCard(product)
    }
}
```

---

## 2. 输入型

### 2.1 DSSegmentedControl — 分段选择器（单选）

```kotlin
@Composable
internal fun DSSegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelectedChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)

// 多选版本
@Composable
internal fun DSMultiSegmentedControl(
    options: List<String>,
    selectedIndices: Set<Int>,
    onSelectionChange: (Set<Int>) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
)
```

**单选 vs 多选**：

| 组件 | 语义 | 场景 |
|---|---|---|
| `DSSegmentedControl` | 互斥单选 | 视图切换（日/周/月） |
| `DSMultiSegmentedControl` | 独立多选 | 筛选条件叠加（包邮 / 折扣 / 新品） |

**使用示例**：

```kotlin
// 单选
val periods = listOf("日", "周", "月")
var selected by remember { mutableIntStateOf(0) }
DSSegmentedControl(
    options = periods,
    selectedIndex = selected,
    onSelectedChange = { selected = it }
)

// 多选
val filters = listOf("新品", "包邮", "折扣", "自营")
var selected by remember { mutableStateOf(setOf(0, 2)) }
DSMultiSegmentedControl(
    options = filters,
    selectedIndices = selected,
    onSelectionChange = { selected = it }
)
```

### 2.2 DSRangeSlider — 范围滑块

```kotlin
@Composable
internal fun DSRangeSlider(
    value: ClosedFloatingPointRange<Float>,
    onValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    startLabel: String? = null,
    endLabel: String? = null,
    valueFormatter: (Float) -> String = { it.toString() },
    enabled: Boolean = true
)
```

**使用示例**：

```kotlin
var priceRange by remember { mutableStateOf(2000f..8000f) }
DSRangeSlider(
    value = priceRange,
    onValueChange = { priceRange = it },
    valueRange = 0f..10000f,
    steps = 9,  // 步长 1000
    startLabel = "最低价",
    endLabel = "最高价",
    valueFormatter = { "¥${it.toInt()}" }
)
```

### 2.3 DSStepper — 数值步进器

```kotlin
@Composable
internal fun DSStepper(
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    min: Int = Int.MIN_VALUE,
    max: Int = Int.MAX_VALUE,
    step: Int = 1,
    enabled: Boolean = true
)
```

**使用示例**：

```kotlin
var count by remember { mutableIntStateOf(1) }
DSStepper(
    value = count,
    onValueChange = { count = it },
    min = 1,
    max = 99,
    step = 1
)
```

### 2.4 DSRatingBar — 评分条

```kotlin
enum class DSRatingStep { Full, Half }

@Composable
internal fun DSRatingBar(
    rating: Float,
    onRatingChange: (Float) -> Unit = {},
    modifier: Modifier = Modifier,
    maxStars: Int = 5,
    starSize: Dp = 24.dp,
    step: DSRatingStep = DSRatingStep.Half,
    readOnly: Boolean = false,
    enabled: Boolean = true,
    showLabel: Boolean = false
)
```

**使用示例**：

```kotlin
// 可交互
var rating by remember { mutableFloatStateOf(3.5f) }
DSRatingBar(
    rating = rating,
    onRatingChange = { rating = it },
    showLabel = true
)

// 只读
DSRatingBar(rating = 4.5f, readOnly = true, showLabel = true)
```

### 2.5 DSDatePicker — 日期选择器

```kotlin
@Composable
internal fun DSDatePicker(
    state: DatePickerState,
    onDateChange: (Long?) -> Unit,
    modifier: Modifier = Modifier,
    // ... 其他配置
)
```

### 2.6 DSTimePicker — 时间选择器

```kotlin
@Composable
internal fun DSTimePicker(
    state: TimePickerState,
    onTimeChange: (HourMinute) -> Unit,
    modifier: Modifier = Modifier,
    // ... 其他配置
)
```

---

## 3. 装饰型

### 3.1 DSChip — 标签 Chip

```kotlin
enum class DSChipStyle { Assist, Filter, Input, Suggestion }

@Composable
internal fun DSChip(
    text: String,
    modifier: Modifier = Modifier,
    style: DSChipStyle = DSChipStyle.Assist,
    selected: Boolean = false,
    onClick: (() -> Unit)? = null,
    onRemoveClick: (() -> Unit)? = null,
    icon: ImageVector? = null,
    enabled: Boolean = true
)
```

**4 种风格**（M3 标准）：

| Style | 用途 | 示例 |
|---|---|---|
| `Assist` | 辅助操作 Chip | 「添加标签」按钮 |
| `Filter` | 筛选 Chip（带选中态） | 商品筛选 |
| `Input` | 输入 Chip（可删除） | 已选标签 |
| `Suggestion` | 建议 Chip | 搜索联想词 |

**使用示例**：

```kotlin
// 筛选
var selected by remember { mutableStateOf(false) }
DSChip(
    text = "热门",
    style = DSChipStyle.Filter,
    selected = selected,
    onClick = { selected = !selected }
)

// 输入 Chip（可删除）
DSChip(
    text = "Kotlin",
    style = DSChipStyle.Input,
    onRemoveClick = { removeTag("Kotlin") }
)
```

### 3.2 DSAccordion — 折叠面板

```kotlin
internal data class DSAccordionSection(
    val title: String,
    val subtitle: String? = null,
    val content: @Composable () -> Unit,
    val initiallyExpanded: Boolean = false
)

@Composable
internal fun DSAccordion(
    sections: List<DSAccordionSection>,
    modifier: Modifier = Modifier,
    singleExpand: Boolean = false
)
```

**使用示例**：

```kotlin
DSAccordion(
    sections = listOf(
        DSAccordionSection(
            title = "个人信息",
            content = { PersonalInfoForm() }
        ),
        DSAccordionSection(
            title = "工作经历",
            subtitle = "最近 3 段",
            content = { WorkExperienceList() }
        )
    ),
    singleExpand = true  // 同时只展开一个
)
```

### 3.3 DSCarousel — 轮播

```kotlin
@Composable
internal fun <T> DSCarousel(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable (T) -> Unit,
    autoScroll: Boolean = false,
    autoScrollInterval: Long = 3000L,
    showIndicators: Boolean = true,
    onPageChange: ((Int) -> Unit)? = null
)
```

### 3.4 DSPullToRefresh — 下拉刷新

```kotlin
@Composable
internal fun DSPullToRefresh(
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
)
```

**使用示例**：

```kotlin
DSPullToRefresh(
    isRefreshing = uiState.isRefreshing,
    onRefresh = { viewModel.refresh() }
) {
    LazyColumn {
        items(uiState.items) { item ->
            DSListItem(title = item.title)
        }
    }
}
```

### 3.5 DSPagination — 分页

```kotlin
@Composable
internal fun DSPagination(
    hasMore: Boolean,
    onLoadMore: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: (() -> Unit)? = null
)
```

通常放在 `LazyColumn` 末尾：

```kotlin
LazyColumn {
    items(uiState.items) { item -> ItemRow(item) }
    item {
        DSPagination(
            hasMore = uiState.hasMore,
            isLoading = uiState.isLoadingMore,
            onLoadMore = { viewModel.loadMore() }
        )
    }
}
```

---

## 4. 浮层类 (Overlay)

> 浮层组件是「在主内容之上的临时 UI」，跨多层但归到分子层文档说明，详见 [layouts.md](layouts.md) 的浮层章节。

### 4.1 DSDialog — 对话框

```kotlin
typealias DSDialogType = DSMessageType  // Info / Success / Warning / Error

@Composable
internal fun DSDialog(
    onDismiss: () -> Unit,
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    type: DSDialogType = DSDialogType.Info,
    showIcon: Boolean = true,
    confirmText: String = "确定",
    onConfirm: () -> Unit = {},
    dismissText: String? = "取消",
    onDismissClick: (() -> Unit)? = onDismiss
)
```

**使用示例**：

```kotlin
var showDialog by remember { mutableStateOf(true) }
if (showDialog) {
    DSDialog(
        onDismiss = { showDialog = false },
        title = "确认删除",
        message = "此操作不可撤销，确定要删除吗？",
        type = DSMessageType.Warning,
        confirmText = "删除",
        onConfirm = { vm.delete(); showDialog = false }
    )
}

// 无图标对话框（type = Info 但不显示图标）
DSDialog(
    onDismiss = { },
    title = "提示",
    message = "已保存",
    showIcon = false
)
```

### 4.2 DSSimpleDialog — 简单选择对话框

```kotlin
internal data class DSSimpleDialogOption(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val id: String = title
)

@Composable
internal fun DSSimpleDialog(
    title: String,
    options: List<DSSimpleDialogOption>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedOptionId: String? = null,
    onDismissRequest: () -> Unit = {},
    icon: ImageVector? = null
)
```

### 4.3 DSFullScreenDialog — 全屏对话框

```kotlin
@Composable
internal fun DSFullScreenDialog(
    title: String,
    onCloseClick: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onSaveClick: (() -> Unit)? = null,
    saveText: String = "保存",
    closeText: String = "关闭",
    topBarStyle: DSTopBarStyle = DSTopBarStyle.Small
)
```

### 4.4 DSBottomSheet — 模态底部弹层

```kotlin
@Composable
internal fun DSBottomSheet(
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
)
```

### 4.5 DSPersistentBottomSheet — 常驻底部弹层

```kotlin
@Composable
internal fun DSPersistentBottomSheet(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberStandardBottomSheetState(),
    onDismissRequest: (() -> Unit)? = null,
    skipHiddenState: Boolean = true
)

// 脚手架版本
@Composable
internal fun DSBottomSheetScaffold(
    sheetContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    sheetState: SheetState,
    sheetPeekHeight: Dp = 80.dp,
    content: @Composable (PaddingValues) -> Unit
)
```

### 4.6 DSContextMenu — 上下文菜单

```kotlin
internal data class DSContextMenuItem(
    val icon: ImageVector? = null,
    val label: String,
    val onClick: () -> Unit,
    val destructive: Boolean = false
)

// 长按触发
@Composable
internal fun DSContextMenuHost(
    menuItems: List<DSContextMenuItem>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
)

// 三点触发
@Composable
internal fun DSContextMenuTrigger(
    menuItems: List<DSContextMenuItem>,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Default.MoreVert,
    contentDescription: String = "更多操作"
)
```

### 4.7 DSTooltip — 提示

```kotlin
@Composable
internal fun DSTooltip(
    text: String,
    modifier: Modifier = Modifier,
    // ... 触发与定位配置
)
```

### 4.8 DSSnackbar — 反馈条

```kotlin
typealias DSSnackbarType = DSMessageType

@Composable
internal fun DSSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    type: DSMessageType = DSMessageType.Info
)
```

**通过 DSAppScaffold 的 SnackbarHost 触发**：

```kotlin
val snackbarHostState = remember { SnackbarHostState() }
val scope = rememberCoroutineScope()

DSAppScaffold(
    snackbarHostState = snackbarHostState,
    // ...
) {
    Button(onClick = {
        scope.launch {
            snackbarHostState.showSnackbar(
                message = "保存成功",
                actionLabel = "查看",
                duration = SnackbarDuration.Short
            )
        }
    }) { Text("保存") }
}
```

### 4.9 DSFAB — 悬浮按钮

```kotlin
enum class DSFABStyle { Small, Large, Extended }

@Composable
internal fun DSFAB(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    style: DSFABStyle = DSFABStyle.Large,
    text: String? = null,                 // Extended 风格生效
    collapsed: Boolean = false            // Extended 风格生效，true 时仅显示图标
)
```

**使用示例**：

```kotlin
// Large FAB
DSFAB(
    onClick = { navController.navigate(NewItemRoute) },
    icon = Icons.Default.Add,
    contentDescription = "新建"
)

// Extended FAB（带文字）
DSFAB(
    onClick = { /* ... */ },
    icon = Icons.Default.Edit,
    contentDescription = "撰写",
    style = DSFABStyle.Extended,
    text = "撰写"
)

// 可收起 Extended FAB（配合 LazyColumn 滚动状态）
val listState = rememberLazyListState()
val collapsed by remember {
    derivedStateOf { listState.firstVisibleItemIndex > 0 }
}
DSFAB(
    onClick = { /* ... */ },
    icon = Icons.Default.Edit,
    contentDescription = "撰写",
    style = DSFABStyle.Extended,
    text = "撰写",
    collapsed = collapsed
)
```

### 4.10 DSSearchBar — 搜索栏

```kotlin
@Composable
internal fun DSSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "搜索",
    active: Boolean = false,
    onActiveChange: (Boolean) -> Unit = {}
)
```

---

## 5. 速查表

### 5.1 分子组件清单（共 28 个）

| 类别 | 组件 | 文件 |
|---|---|---|
| 容器 | DSCard / DSListItem / DSGrid | DSCard.kt / DSListItem.kt / DSGrid.kt |
| 输入 | DSSegmentedControl / DSMultiSegmentedControl / DSRangeSlider / DSStepper / DSRatingBar / DSDatePicker / DSTimePicker | DSSegmentedControl.kt / DSRangeSlider.kt / DSStepper.kt / DSRatingBar.kt / DSDatePicker.kt / DSTimePicker.kt |
| 装饰 | DSChip / DSAccordion / DSCarousel / DSPullToRefresh / DSPagination | DSChip.kt / DSAccordion.kt / DSCarousel.kt / DSPullToRefresh.kt / DSPagination.kt |
| 浮层 | DSDialog / DSSimpleDialog / DSFullScreenDialog / DSBottomSheet / DSPersistentBottomSheet / DSBottomSheetScaffold / DSContextMenu / DSTooltip / DSSnackbar / DSFAB / DSSearchBar | DSDialog.kt / DSSimpleDialog.kt / DSBottomSheet.kt / DSPersistentBottomSheet.kt / DSContextMenu.kt / DSTooltip.kt / DSSnackbar.kt / DSFAB.kt / DSSearchBar.kt |

### 5.2 分子 vs 原子区别

| 维度 | Atom 原子 | Molecule 分子 |
|---|---|---|
| 组成 | 不可再分 | 由原子组合 |
| 职责 | 单一 | 单一但更复杂 |
| 例 | DSButton | DSCard（含 DSButton + DSText + DSIcon） |

### 5.3 何时自定义新分子

不要在业务层重复造分子。如果业务需要新的复合组件：

1. **优先**：用 `AppCommonCard` / `AppStructuredCard`（见 [containers.md](containers.md)）封装
2. **次选**：在 `feature/xxx/components/` 内自定义业务组件
3. **最后**：若通用性强，提交到 `core/designsystem/` 作为新分子

### 5.4 浮层组件触发方式

| 组件 | 触发方式 |
|---|---|
| `DSDialog` | `if (showDialog) DSDialog(...)` |
| `DSSimpleDialog` | 同上 |
| `DSFullScreenDialog` | 同上 |
| `DSBottomSheet` | `if (showSheet) DSBottomSheet(...)` |
| `DSPersistentBottomSheet` | 始终渲染，通过 `sheetState` 控制 |
| `DSContextMenuHost` | 包装内容，长按触发 |
| `DSContextMenuTrigger` | 独立 IconButton，点击触发 |
| `DSTooltip` | 长按或悬停触发 |
| `DSSnackbar` | 通过 `SnackbarHostState.showSnackbar()` |
| `DSFAB` | 始终浮在内容上，通过 `DSAppScaffold(floatingActionButton = { DSFAB(...) })` |
