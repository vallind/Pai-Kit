# UI 组件使用规范

> AI 规则文件 - UI 组件与设计系统领域  
> 本文件由 CLAUDE.md 拆分而来，对应原 CLAUDE.md 第 9 章。  
> **本文件只讲「使用规则」，组件 API 详见 [components/](../components/)**

---

## 一、5 层包结构

```
core/designsystem/
├── foundation/                     第 0 层 - 横切系统（最底层）
│   ├── tokens/                     设计 Token
│   ├── theme/                      主题系统
│   ├── motion/                     动效系统
│   ├── a11y/                       无障碍
│   ├── layout/                     响应式布局
│   ├── perf/                       性能工具
│   ├── preview/                    Preview 规范
│   └── util/                       通用扩展
├── primitives/                     第 1 层 - 原子层
├── patterns/                       第 2 层 - 业务无关 UI 模式
├── shell/                          第 3 层 - 应用骨架
├── overlays/                       第 3 层 - 全局浮层
└── containers/                     业务容器壳（AppCommonCard / AppStructuredCard）
```

> **组件 API 详见**：[components/](../components/) 5 个文档（foundation / atoms / molecules / layouts / containers）

### 依赖方向（严格单向 + 平级隔离）

```
Feature 层（允许依赖 primitives/patterns/shell/overlays/containers）
     ↓
┌──────────────────────────────────────────────┐
│  第 3 层 Overlays     第 3 层 Shell          │  ← 平级，互不依赖
│        ↓                    ↓                │
│  第 2 层 Patterns（不消费业务状态类，slot 驱动）│  ← 不依赖 Shell/Overlay
│        ↓                                     │
│  第 1 层 Primitives（原子+控件+容器+展示）    │
│        ↓                                     │
│  第 0 层 Foundation                          │
└──────────────────────────────────────────────┘

Container（业务容器壳）依赖 Primitives + Patterns
```

### 四条架构红线（Konsist + Detekt 自动检查）

1. **禁止平级跨域调用**：shell ↔ overlays 之间零引用
2. **禁止底层依赖高层**：primitives 不能引用 patterns/shell/overlays
3. **Shell 与 Patterns 隔离**：patterns 不被 shell/overlays 依赖
4. **业务层禁止越级**：Feature 不得直接用 M3 原生组件（Detekt ForbiddenImport）

### Feature 层依赖规则

- ✅ 允许依赖 primitives/patterns/shell/overlays/containers 五个子域
- ✅ 优先使用 Patterns（如 DSNetWorkView）和 Containers（如 AppCommonCard）
- ✅ 直接用 DSButton/DSText/DSIcon 是合理的
- ❌ 禁止在 designsystem 下定义新的业务组件
- ❌ 禁止直接 import M3 原生组件

### Patterns 层纯净性规则

- ❌ patterns **不消费** `BaseNetWorkUiState<T>` 等业务 sealed class
- ✅ patterns 只暴露 `isLoading: Boolean` / `error: DSErrorData?` / `empty: DSEmptyData?` 通用参数 + slot lambda
- ✅ Feature 层负责把业务状态映射为通用参数

---

## 二、强制规则

### 2.1 必须用 DS 组件

1. **必须**使用 `DS*` 前缀组件，**禁止**直接使用 Material3 原生组件
2. **主题入口**：直接用 `DSDesignTheme`，**禁止**再封装
3. **布局容器**：用原生 `Box/Column/Row` + `Modifier` + `DSTokens`（不再有 DSBox/DSColumn/DSRow 包装）
4. **业务容器壳**：优先用 `AppCommonCard` / `AppStructuredCard`，其次 `DSCard`
5. **Preview 规范**：必须用 `DSPreviewScenes` 包装，至少 Light/Dark/多品牌 3 个场景

### 2.2 命名规范

- 组件命名：`DS` 前缀（如 `DSButton`）
- 业务容器壳命名：`App` 前缀（如 `AppCommonCard`）
- 枚举命名：`DS` 前缀 + 组件名 + `Style/Size/Variant/Type`（如 `DSButtonStyle`）
- 统一数据类：`DS` 前缀（如 `DSNavItem` / `DSMessageType`）

### 2.3 可见性

- DS 组件默认 `internal` 可见性（仅同模块可访问）
- 业务容器壳（App 前缀）`public` 可见性
- Foundation Token 通过 `DSTokens.xxx` 统一入口访问

---

## 三、原生 → DS 映射表

Detekt 报错时查此表，把原生组件替换为 DS 组件：

| 原生 | DS 替代 | 文档 |
|---|---|---|
| `Button` / `OutlinedButton` / `TextButton` | `DSButton` | [atoms.md §1.1](../components/atoms.md#11-dsbutton--通用按钮) |
| `IconButton` / `FilledIconButton` | `DSIconButton` | [atoms.md §1.2](../components/atoms.md#11-dsiconbutton--图标按钮) |
| `TextField` / `OutlinedTextField` | `DSTextField` | [atoms.md §2.1](../components/atoms.md#21-dstextfield--文本输入框) |
| `Text` | `DSText` | [atoms.md §3.1](../components/atoms.md#3-文本类) |
| `Icon` | `DSIcon` | [atoms.md §4.1](../components/atoms.md#41-dsicon--图标) |
| `HorizontalDivider` / `VerticalDivider` | `DSHorizontalDivider` / `DSVerticalDivider` | [atoms.md §4.2](../components/atoms.md#42-dsdivider--分割线) |
| `Checkbox` | `DSCheckbox` | [atoms.md §6.1](../components/atoms.md#61-dscheckbox--复选框) |
| `Switch` | `DSSwitch` | [atoms.md §6.2](../components/atoms.md#62-dsswitch--开关) |
| `RadioButton` | `DSRadioButton` | [atoms.md §6.3](../components/atoms.md#63-dsradiobutton--单选框) |
| `Slider` | `DSSlider` | [atoms.md §6.4](../components/atoms.md#64-dsslider--滑块) |
| `LinearProgressIndicator` | `DSLinearProgress` | [atoms.md §5.1](../components/atoms.md#51-dslinearprogress--线性进度条) |
| `CircularProgressIndicator` | `DSCircularProgress` | [atoms.md §5.2](../components/atoms.md#52-dscircularprogress--圆形进度) |
| `Card` / `ElevatedCard` / `OutlinedCard` | `DSCard` 或 `AppCommonCard` / `AppStructuredCard` | [molecules.md §1.1](../components/molecules.md#11-dscard--卡片) / [containers.md](../components/containers.md) |
| `Scaffold` | `DSAppScaffold` | [layouts.md §1.1](../components/layouts.md#11-dsappscaffold--应用脚手架) |
| `TopAppBar` / `CenterAlignedTopAppBar` / `MediumTopAppBar` / `LargeTopAppBar` | `DSTopBar` | [layouts.md §2.1](../components/layouts.md#21-dstopbar--顶部应用栏) |
| `BottomAppBar` | `DSBottomAppBar` | [layouts.md §2.2](../components/layouts.md#22-dsbottomappbar--底部应用栏) |
| `NavigationBar` | `DSBottomBar` | [layouts.md §3.1](../components/layouts.md#31-dsbottombar--底部导航栏) |
| `NavigationRail` | `DSNavigationRail` | [layouts.md §3.2](../components/layouts.md#32-dsnavigationrail--侧边导航栏) |
| `ModalNavigationDrawer` | `DSModalNavigationDrawer` | [layouts.md §3.3](../components/layouts.md#33-dsmodalnavigationdrawer--模态抽屉) |
| `PermanentNavigationDrawer` | `DSPermanentNavigationDrawer` | [layouts.md §3.4](../components/layouts.md#34-dspermanentnavigationdrawer--常驻抽屉) |
| `TabRow` / `ScrollableTabRow` | `DSTabRow` | [layouts.md §4.1](../components/layouts.md#41-dstabrow--标签栏) |
| `AlertDialog` | `DSDialog` | [molecules.md §4.1](../components/molecules.md#41-dsdialog--对话框) |
| `ModalBottomSheet` | `DSBottomSheet` | [molecules.md §4.4](../components/molecules.md#44-dsbottomsheet--模态底部弹层) |
| `BottomSheetScaffold` | `DSBottomSheetScaffold` | [molecules.md §4.5](../components/molecules.md#45-dspersistentbottomsheet--常驻底部弹层) |
| `FloatingActionButton` / `ExtendedFloatingActionButton` | `DSFAB` | [molecules.md §4.9](../components/molecules.md#49-dsfab--悬浮按钮) |
| `SearchBar` | `DSSearchBar` | [molecules.md §4.10](../components/molecules.md#410-dssearchbar--搜索栏) |
| `SegmentedButton` / `SingleChoiceSegmentedButtonRow` | `DSSegmentedControl` | [molecules.md §2.1](../components/molecules.md#21-dssegmentedcontrol--分段选择器单选) |
| `RangeSlider` | `DSRangeSlider` | [molecules.md §2.2](../components/molecules.md#22-dsrangeslider--范围滑块) |
| `AssistChip` / `FilterChip` / `InputChip` / `SuggestionChip` | `DSChip` | [molecules.md §3.1](../components/molecules.md#31-dschap--标签-chip) |
| `DatePicker` | `DSDatePicker` | [molecules.md §2.5](../components/molecules.md#25-dsdatepicker--日期选择器) |
| `TimePicker` | `DSTimePicker` | [molecules.md §2.6](../components/molecules.md#26-dstimepicker--时间选择器) |
| `Badge` / `BadgedBox` | `DSBadge` | [atoms.md §4.3](../components/atoms.md#43-dsbadge--徽标) |

---

## 四、组件放置决策树

```
新组件应该放哪？
├─ 是 Foundation 层（Token/Theme/Motion/A11y）？
│   └─ foundation/{对应子目录}/
├─ 是原子组件（单一职责，不可再分）？
│   └─ primitives/
├─ 是分子组件（原子组合）？
│   ├─ 是浮层（Dialog/Sheet/Tooltip/FAB/Snackbar）？ → overlays/
│   └─ 其他 → primitives/
├─ 是页面骨架（组合多个分子）？
│   └─ shell/
├─ 是业务容器壳（封装业务结构）？
│   └─ containers/
└─ 是业务模式（如 NetWorkView 状态容器）？
    └─ patterns/
```

---

## 五、DSTokens 总入口

所有设计 Token 通过 `DSTokens` 统一访问，**禁止硬编码** `16.dp` / `Color(0xFF4F46E5)`：

```kotlin
object DSTokens {
    object Spacing      // none/xxs/xs/sm/md/lg/xl/xxl/xxxl/huge/giant
    object Radius       // none/extraSmall/small/medium/large/extraLarge/full
    object Elevation    // level0/1/2/3/4/6/8/12
    object FontSize     // 15 种 M3 Typography
    object Duration     // instant/small1-3/medium1-4/long1-3
    object Easing       // emphasized/emphasizedDecelerate/emphasizedAccelerate/standard/...
    object Brand        // 5 套品牌色板 × 10 步
    object Border       // hairline/thin/medium/thick
    object Alpha        // overlay/disabledContainer/disabledContent/loadingScrim
    object IconSize     // xs/sm/md/lg/xl
    object ComponentHeight  // 16 种组件高度
    val minTouchTarget: Dp  // 48dp
}
```

> 完整 Token 文档：[00-foundation.md §2](../components/00-foundation.md#2-tokens-设计令牌)

---

## 六、统一数据类

避免重复定义，使用统一数据类：

| 数据类 | 用途 | 文档 |
|---|---|---|
| `DSNavItem(label, icon, selectedIcon, badgeText, id)` | 统一导航项（5 个导航组件共用） | [00-foundation.md §2.9](../components/00-foundation.md#29-统一数据类) |
| `DSMessageType { Info, Success, Warning, Error }` | 统一消息类型（Snackbar/Banner/Dialog 共用） | [00-foundation.md §2.9](../components/00-foundation.md#29-统一数据类) |
| `DSBrandColor` enum | 5 套品牌色板 | [00-foundation.md §2.6](../components/00-foundation.md#26-品牌色-brand) |
| `DSFontSizeScale` enum | 4 档字号缩放 | [00-foundation.md §3.1](../components/00-foundation.md#31-dsdesigntheme--主题入口) |
| `AppStructuredCardData` | 结构化业务卡数据契约 | [containers.md §3.1](../components/containers.md#31-数据契约) |
| `DSUiState<T>` sealed | 状态机范式（8 种状态） | [00-foundation.md §10](../components/00-foundation.md#10-uistate-状态机范式) |
| `DSStableList<T>` / `DSStableMap<K,V>` | 稳定性包装类 | [00-foundation.md §7.4](../components/00-foundation.md#74-稳定性包装类) |

---

## 七、Preview 规范

**所有 DS 组件 Preview 必须用 `DSPreviewScenes` 包装**，至少 3 个场景：

```kotlin
@Preview(showBackground = true, name = "Button - Light")
@Composable
private fun ButtonLightPreview() {
    DSPreviewScenes.Light {
        DSButton("Submit", onClick = {})
    }
}

@Preview(showBackground = true, name = "Button - Dark")
@Composable
private fun ButtonDarkPreview() {
    DSPreviewScenes.Dark {
        DSButton("Submit", onClick = {})
    }
}

@Preview(showBackground = true, name = "Button - Emerald Brand")
@Composable
private fun ButtonEmeraldPreview() {
    DSPreviewScenes.Brand(DSBrandColor.Emerald) {
        DSButton("Submit", onClick = {})
    }
}
```

> 完整规范：[00-foundation.md §8](../components/00-foundation.md#8-preview-预览规范)

---

## 八、动效规范

**必须用 `DSMotionScheme` 统一动效入口**，禁止直接写 `tween(300)`：

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
Modifier.pressScale()  // 内部自动读 motion.pressScale

// 减少动效（无障碍）
provideDSMotionScheme(scheme = reducedMotionScheme()) { AppContent() }
```

> 完整动效文档：[00-foundation.md §4](../components/00-foundation.md#4-motion-动效系统)

---

## 九、状态管理范式选择

| 范式 | 入口 | 适用场景 |
|---|---|---|
| **slot 驱动** | `DSNetWorkView(isLoading, error, empty, content)` | 简单页面，4 态 |
| **状态机驱动** | `DSPageStateLayout(state: DSUiState, content)` | 复杂状态机，8 态（含 LoadingMore/Refreshing/PartialError） |

> 完整对比：[00-foundation.md §10.6](../components/00-foundation.md#106-与-dsnetWorkview-的选择)

---

## 十、完整文档导航

| 想了解 | 看哪 |
|---|---|
| 文档总入口 | [INDEX.md](../INDEX.md) |
| 新人入门 | [getting-started.md](../guides/getting-started.md) |
| 组件速查表 + 决策树 | [components/README.md](../components/README.md) |
| Foundation 层完整 API | [00-foundation.md](../components/00-foundation.md) |
| Atom 原子组件完整 API | [atoms.md](../components/atoms.md) |
| Molecule 分子组件完整 API | [molecules.md](../components/molecules.md) |
| Layout 布局组件 + 页面模板 | [layouts.md](../components/layouts.md) |
| Container 业务容器壳规范 | [containers.md](../components/containers.md) |
