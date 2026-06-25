# Atom 原子组件

> 文档版本：v1.0 · 2026-06-25  
> 适用范围：`core/designsystem/primitives/` 中的原子组件 + `overlays/DSFAB`  
> 定位：不可再分的最小 UI 单元，单一职责

---

## 目录

1. [按钮类](#1-按钮类)
2. [输入类](#2-输入类)
3. [文本类](#3-文本类)
4. [装饰类](#4-装饰类)
5. [反馈类](#5-反馈类)
6. [选择类](#6-选择类)
7. [速查表](#7-速查表)

---

## 1. 按钮类

### 1.1 DSButton — 通用按钮

```kotlin
enum class DSButtonStyle { Filled, Elevated, Tonal, Outlined, Text, Error }
enum class DSButtonSize { Small, Medium, Large }

@Composable
internal fun DSButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: DSButtonStyle = DSButtonStyle.Filled,
    size: DSButtonSize = DSButtonSize.Medium,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    loading: Boolean = false
)
```

**6 种风格优先级**（高 → 低）：

| Style | 用途 | 视觉 |
|---|---|---|
| `Filled` | 主操作（CTA） | primary 背景 + onPrimary 文字 |
| `Elevated` | 需突出的次操作 | surface 背景 + primary 文字 + 阴影 |
| `Tonal` | 次主操作 | secondaryContainer 背景 |
| `Outlined` | 辅助操作 | 透明背景 + primary 文字 + 描边 |
| `Text` | 三级操作 | 透明背景 + primary 文字 |
| `Error` | 危险操作（删除/撤销） | error 背景 + onError 文字 |

**3 种尺寸**：

| Size | 高度 | 用途 |
|---|---|---|
| `Small` | 32dp | 紧凑空间（如卡片底部操作） |
| `Medium` | 40dp | 默认 |
| `Large` | 48dp | CTA / 主操作 |

**使用示例**：

```kotlin
// 主 CTA
DSButton(text = "提交", onClick = { vm.submit() }, size = DSButtonSize.Large)

// 加载态
DSButton(text = "提交中", onClick = {}, loading = true, enabled = false)

// 带图标
DSButton(text = "新建", onClick = { vm.create() }, icon = Icons.Default.Add)

// 危险操作
DSButton(text = "删除", onClick = { vm.delete() }, style = DSButtonStyle.Error)
```

### 1.2 DSIconButton — 图标按钮

```kotlin
enum class DSIconButtonStyle { Standard, Filled, Tonal, Outlined }

@Composable
internal fun DSIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: DSIconButtonStyle = DSIconButtonStyle.Standard,
    enabled: Boolean = true
)
```

**4 种风格**：

| Style | 用途 |
|---|---|
| `Standard` | 工具栏默认（透明背景） |
| `Filled` | 突出操作（如「筛选」按钮） |
| `Tonal` | 次突出（柔和填充） |
| `Outlined` | 辅助操作 |

**使用示例**：

```kotlin
DSIconButton(
    icon = Icons.Default.Search,
    contentDescription = "搜索",
    onClick = { navController.navigate(SearchRoute) }
)

// 突出操作
DSIconButton(
    icon = Icons.Default.Filter,
    contentDescription = "筛选",
    onClick = { showFilterSheet() },
    style = DSIconButtonStyle.Tonal
)
```

### 1.3 DSFAB — 悬浮按钮

详见 [molecules.md 第 8 节 浮层类](molecules.md#8-浮层类)。

---

## 2. 输入类

### 2.1 DSTextField — 文本输入框

```kotlin
enum class DSTextFieldStyle { Filled, Outlined }

@Composable
internal fun DSTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    maxLength: Int? = null,             // 非 null 时启用字数统计
    style: DSTextFieldStyle = DSTextFieldStyle.Outlined,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,        // 显示密码切换按钮
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null
)
```

**使用示例**：

```kotlin
// 基础
var name by remember { mutableStateOf("") }
DSTextField(
    value = name,
    onValueChange = { name = it },
    label = "用户名",
    placeholder = "请输入用户名"
)

// 密码
var pwd by remember { mutableStateOf("") }
DSTextField(
    value = pwd,
    onValueChange = { pwd = it },
    label = "密码",
    isPassword = true
)

// 错误态
DSTextField(
    value = email,
    onValueChange = { email = it },
    label = "邮箱",
    isError = !isValidEmail(email),
    errorMessage = "邮箱格式不正确"
)

// 字数统计
DSTextField(
    value = bio,
    onValueChange = { bio = it },
    label = "个人简介",
    singleLine = false,
    maxLines = 5,
    maxLength = 200
)
```

### 2.2 DSTextArea — 多行文本输入

DSTextField 的多行预设，提供最常用的长文本配置：

```kotlin
@Composable
internal fun DSTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    maxLength: Int? = null,
    minHeight: Dp = DSTokens.ComponentHeight.textAreaMin,  // 120dp
    maxLines: Int = 5
)
```

**与 DSTextField 的关系**：DSTextArea 是 DSTextField 的薄包装：

```kotlin
// 等价于
DSTextField(
    value = value,
    onValueChange = onValueChange,
    label = label,
    singleLine = false,
    maxLines = maxLines,
    maxLength = maxLength,
    style = DSTextFieldStyle.Outlined
)
```

需要更细粒度控制（如 Filled 风格、密码、自定义图标）时直接用 DSTextField。

### 2.3 DSDropdown — 下拉选择器

```kotlin
@Composable
internal fun DSDropdown(
    items: List<String>,
    selected: String,
    onSelectedChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
)
```

基于 M3 `ExposedDropdownMenuBox`，点击展开下拉菜单。

```kotlin
val languages = listOf("简体中文", "English", "日本語")
var selected by remember { mutableStateOf("简体中文") }

DSDropdown(
    items = languages,
    selected = selected,
    onSelectedChange = { selected = it },
    label = "语言"
)
```

---

## 3. 文本类

### 3.1 DSText — 文本

```kotlin
enum class DSTextVariant {
    DisplayLarge, DisplayMedium, DisplaySmall,
    HeadlineLarge, HeadlineMedium, HeadlineSmall,
    TitleLarge, TitleMedium, TitleSmall,
    BodyLarge, BodyMedium, BodySmall,
    LabelLarge, LabelMedium, LabelSmall
}

enum class DSTextColor {
    Primary, Secondary, Tertiary,
    OnPrimary, OnSurface, OnSurfaceVariant,
    Error, Success, Warning, Info, Custom
}

@Composable
internal fun DSText(
    text: String,
    modifier: Modifier = Modifier,
    variant: DSTextVariant = DSTextVariant.BodyMedium,
    color: DSTextColor = DSTextColor.OnSurface,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    textAlign: TextAlign? = null,
    fontWeight: FontWeight? = null,
    textDecoration: TextDecoration? = null,
    customColor: Color = Color.Unspecified,    // color = Custom 时生效
    customStyle: TextStyle? = null              // 非 null 时完全覆盖 variant
)
```

**15 种 variant 选择指南**：

| 类别 | 用途 | 推荐场景 |
|---|---|---|
| Display | 超大标题 | 数字仪表盘 / 启动页 |
| Headline | 页面标题 | 顶部主标题 |
| Title | 区块标题 | 卡片标题 / 区段标题 |
| Body | 正文 | 文章 / 描述 / 列表项 |
| Label | 标签文字 | 按钮 / Chip / Tab |

**使用示例**：

```kotlin
// 标准
DSText("Hello World")

// 标题
DSText("个人中心", variant = DSTextVariant.HeadlineSmall, color = DSTextColor.Primary)

// 错误提示
DSText("邮箱格式不正确", variant = DSTextVariant.BodySmall, color = DSTextColor.Error)

// 字数统计
DSText("${text.length} / 200", variant = DSTextVariant.LabelSmall, color = DSTextColor.Secondary)

// 自定义颜色
DSText("特别提示", color = DSTextColor.Custom, customColor = Color(0xFFFF6B35))

// 自定义样式（完全覆盖）
DSText(
    text = "自定义",
    customStyle = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp
    )
)
```

**字号缩放**：DSText 自动跟随 `DSDesignTheme(fontSizeScale = DSFontSizeScale.Large)` 注入的缩放比例，无需手动处理。

---

## 4. 装饰类

### 4.1 DSIcon — 图标

```kotlin
enum class DSIconSize(val dp: Dp) { XS, SM, MD, LG, XL }  // 12 / 16 / 24 / 32 / 48 dp
enum class DSIconTint { Primary, Secondary, OnSurface, OnSurfaceVariant, Error, Success, Custom }

@Composable
internal fun DSIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: DSIconSize = DSIconSize.MD,
    tint: DSIconTint = DSIconTint.OnSurfaceVariant,
    customTint: Color = Color.Unspecified,
    contentDescription: String? = null
)
```

### 4.2 DSDivider — 分割线

```kotlin
enum class DSDividerThickness(val dp: Dp) { Hairline, Thin, Medium, Thick }  // 0.5 / 1 / 2 / 4 dp

@Composable
internal fun DSHorizontalDivider(modifier: Modifier = Modifier, thickness: DSDividerThickness = DSDividerThickness.Thin)
@Composable
internal fun DSVerticalDivider(modifier: Modifier = Modifier, thickness: DSDividerThickness = DSDividerThickness.Thin, height: Dp = 24.dp)
```

### 4.3 DSBadge — 徽标

```kotlin
enum class DSBadgeType { Number, Dot, Text }
enum class DSBadgePosition { TopEnd, TopStart, BottomEnd, BottomStart }

@Composable
internal fun DSBadge(
    modifier: Modifier = Modifier,
    type: DSBadgeType = DSBadgeType.Number,
    text: String? = null,
    number: Int? = null,
    maxNumber: Int = 99
)
```

### 4.4 DSTag — 标签

```kotlin
enum class DSTagColor { Primary, Secondary, Tertiary, Error, Success, Warning, Info }
enum class DSTagSize { Small, Medium, Large }

@Composable
internal fun DSTag(
    text: String,
    modifier: Modifier = Modifier,
    color: DSTagColor = DSTagColor.Primary,
    size: DSTagSize = DSTagSize.Medium,
    icon: ImageVector? = null,
    onClick: (() -> Unit)? = null
)
```

### 4.5 DSAvatar — 头像

```kotlin
enum class DSAvatarSize(val dp: Dp) { XS, SM, MD, LG, XL }  // 24 / 32 / 40 / 56 / 72 dp
enum class DSAvatarShape { Circle, RoundedSquare }

@Composable
internal fun DSAvatar(
    imageUrl: String? = null,
    modifier: Modifier = Modifier,
    size: DSAvatarSize = DSAvatarSize.MD,
    shape: DSAvatarShape = DSAvatarShape.Circle,
    placeholderIcon: ImageVector = Icons.Default.Person,
    border: Boolean = false
)
```

---

## 5. 反馈类

### 5.1 DSLinearProgress — 线性进度条

```kotlin
@Composable
internal fun DSLinearProgress(
    progress: Float? = null,    // null = 不确定进度（加载动画）
    modifier: Modifier = Modifier.fillMaxWidth(),
    color: Color = MaterialTheme.colorScheme.primary
)
```

### 5.2 DSCircularProgress — 圆形进度

```kotlin
@Composable
internal fun DSCircularProgress(
    progress: Float? = null,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    strokeWidth: Dp = 3.dp,
    color: Color = MaterialTheme.colorScheme.primary
)
```

### 5.3 DSSkeleton — 骨架屏

```kotlin
@Composable internal fun DSSkeletonBlock(modifier, shape = RoundedCornerShape(4dp))
@Composable internal fun DSSkeletonText(modifier, lineCount = 2, lineHeight = 16.dp, lineSpacing = 8.dp)
@Composable internal fun DSSkeletonCircle(modifier, size = 40.dp)
@Composable internal fun DSSkeletonListItem(modifier, avatarSize = 40.dp)
@Composable internal fun DSSkeletonCard(modifier, imageHeight = 120.dp)
```

**5 种变体**：

| 组件 | 用途 |
|---|---|
| `DSSkeletonBlock` | 基础色块（自定义 shape） |
| `DSSkeletonText` | 多行文本占位（每行宽度递减） |
| `DSSkeletonCircle` | 头像 / 图标占位 |
| `DSSkeletonListItem` | 列表项占位（avatar + 两行文本） |
| `DSSkeletonCard` | 卡片占位（图片 + 标题 + 副标题 + 操作行） |

**使用示例**：

```kotlin
if (uiState.isLoading) {
    DSSkeletonCard(modifier = Modifier.padding(16.dp))
} else {
    ContentCard(data = uiState.data)
}

// 列表骨架
LazyColumn {
    items(5) {
        DSSkeletonListItem()
        DSHorizontalDivider()
    }
}
```

**配色**：`extendedColors.skeletonBase` + `skeletonHighlight`，闪烁周期 800ms。

---

## 6. 选择类

### 6.1 DSCheckbox — 复选框

```kotlin
@Composable
internal fun DSCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
)
```

### 6.2 DSSwitch — 开关

```kotlin
@Composable
internal fun DSSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
)
```

### 6.3 DSRadioButton — 单选框

```kotlin
@Composable
internal fun DSRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    label: String? = null,
    enabled: Boolean = true
)
```

### 6.4 DSSlider — 滑块

```kotlin
@Composable
internal fun DSSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    steps: Int = 0,
    onValueChangeFinished: (() -> Unit)? = null,
    label: String? = null,
    enabled: Boolean = true
)
```

**4 种选择类对比**：

| 组件 | 语义 | 使用场景 |
|---|---|---|
| `DSCheckbox` | 多选 | 同意协议 / 多个筛选条件 |
| `DSSwitch` | 二态开关 | 设置项开关 |
| `DSRadioButton` | 单选（互斥） | 性别 / 主题模式 |
| `DSSlider` | 连续值 | 音量 / 透明度 |

---

## 7. 速查表

### 7.1 原子组件清单（共 18 个）

| 类别 | 组件 | 文件 |
|---|---|---|
| 按钮 | DSButton / DSIconButton | DSButton.kt / DSIconButton.kt |
| 输入 | DSTextField / DSTextArea / DSDropdown | DSTextField.kt / DSTextArea.kt / DSDropdown.kt |
| 文本 | DSText | DSText.kt |
| 装饰 | DSIcon / DSDivider / DSBadge / DSTag / DSAvatar | DSIcon.kt / DSDivider.kt / DSBadge.kt / DSTag.kt / DSAvatar.kt |
| 反馈 | DSLinearProgress / DSCircularProgress / DSSkeleton | DSProgress.kt / DSSkeleton.kt |
| 选择 | DSCheckbox / DSSwitch / DSRadioButton / DSSlider | DSSelection.kt |

### 7.2 命名规范

- 所有原子组件以 `DS` 前缀命名（DSButton / DSText / DSIcon）
- 枚举以 `DS` 前缀 + 组件名 + `Style/Size/Variant/Type`（DSButtonStyle / DSTextVariant）
- `internal` 可见性，仅设计系统内部 + 同模块业务可访问

### 7.3 设计规范

- **触控目标**：所有可点击原子通过 `Modifier.minTouchTarget()` 保证至少 48dp × 48dp
- **状态完整**：每个原子组件至少有正常 / 禁用 2 态，部分有按下 / 聚焦 / 悬停
- **配色**：通过 `MaterialTheme.colorScheme.*` 读取，禁止硬编码颜色
- **尺寸**：通过 `DSTokens.Spacing / IconSize / ComponentHeight` 读取，禁止硬编码

### 7.4 与 Compose 原生对照

| Pai 原子 | Compose 原生 | 差异 |
|---|---|---|
| `DSButton` | `Button` / `OutlinedButton` / `TextButton` | 统一入口 + 6 种风格 + 3 种尺寸 + loading 态 |
| `DSIconButton` | `IconButton` / `FilledIconButton` | 统一入口 + 4 种风格 |
| `DSTextField` | `TextField` / `OutlinedTextField` | 统一入口 + 2 种风格 + 字数统计 + 密码切换 |
| `DSText` | `Text` | 限定 15 种 variant + 10 种 color，强制规范 |
| `DSIcon` | `Icon` | 限定 5 种 size + 6 种 tint，强制规范 |
| `DSCheckbox` | `Checkbox` | + label 参数 |
| `DSSwitch` | `Switch` | + label 参数 |
| `DSRadioButton` | `RadioButton` | + label 参数 |
| `DSSlider` | `Slider` | + label 参数 |
| `DSDivider` | `HorizontalDivider` / `VerticalDivider` | + thickness 枚举 |
| `DSLinearProgress` | `LinearProgressIndicator` | + 可选 progress |
| `DSCircularProgress` | `CircularProgressIndicator` | + 可选 progress |
