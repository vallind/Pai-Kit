# Pai 组件速查表

> 77 个 DS 组件 + 2 个业务容器壳 · 4 主层 + 2 横切层  
> 完整 API 请点击对应组件链接进入详细文档

---

## 🔍 我要用...

### 按钮
- 主操作 CTA → [DSButton Filled](atoms.md#11-dsbutton--通用按钮)
- 次主操作 → [DSButton Tonal](atoms.md#11-dsbutton--通用按钮)
- 危险操作（删除） → [DSButton Error](atoms.md#11-dsbutton--通用按钮)
- 工具栏图标按钮 → [DSIconButton](atoms.md#11-dsiconbutton--图标按钮)
- 浮动新建按钮 → [DSFAB](molecules.md#49-dsfab--悬浮按钮)
- 列表项 + FAB 凹槽 → [DSBottomAppBar](layouts.md#22-dsbottomappbar--底部应用栏)

### 输入
- 单行文本 → [DSTextField](atoms.md#21-dstextfield--文本输入框)
- 多行文本（评论/描述） → [DSTextArea](atoms.md#22-dstextarea--多行文本输入)
- 密码 → [DSTextField isPassword=true](atoms.md#21-dstextfield--文本输入框)
- 下拉选择 → [DSDropdown](atoms.md#23-dsdropdown--下拉选择器)
- 数值步进 → [DSStepper](molecules.md#23-dsstepper--数值步进器)
- 评分 → [DSRatingBar](molecules.md#24-dsratingbar--评分条)
- 日期/时间 → [DSDatePicker](molecules.md#25-dsdatepicker--日期选择器) / [DSTimePicker](molecules.md#26-dstimepicker--时间选择器)
- 范围选择（价格区间） → [DSRangeSlider](molecules.md#22-dsrangeslider--范围滑块)
- 单选分段 → [DSSegmentedControl](molecules.md#21-dssegmentedcontrol--分段选择器单选)
- 多选分段 → [DSMultiSegmentedControl](molecules.md#21-dssegmentedcontrol--分段选择器单选)

### 选择
- 多选 → [DSCheckbox](atoms.md#61-dscheckbox--复选框)
- 二态开关 → [DSSwitch](atoms.md#62-dsswitch--开关)
- 互斥单选 → [DSRadioButton](atoms.md#63-dsradiobutton--单选框)
- 连续值（音量） → [DSSlider](atoms.md#64-dsslider--滑块)

### 反馈
- 加载中（线性） → [DSLinearProgress](atoms.md#51-dslinearprogress--线性进度条)
- 加载中（圆形） → [DSCircularProgress](atoms.md#52-dscircularprogress--圆形进度)
- 骨架屏 → [DSSkeleton](atoms.md#53-dsskeleton--骨架屏)
- 全屏加载 → [DSFullScreenLoading](layouts.md#52-dsfullscreendloading--全屏加载)
- Snackbar 提示 → [DSSnackbar](molecules.md#48-dssnackbar--反馈条)
- 横幅提示 → [DSBanner](layouts.md#56-dsbanner--横幅提示)
- Tooltip → [DSTooltip](molecules.md#47-dstooltip--提示)

### 容器
- 纯卡片 → [DSCard](molecules.md#11-dscard--卡片)
- 通用业务卡（标题+内容） → [AppCommonCard](containers.md#2-appcommoncard-通用业务卡)
- 列表项卡（数据驱动） → [AppStructuredCard](containers.md#3-appstructuredcard-结构化业务卡)
- 列表项 → [DSListItem](molecules.md#12-dslistitem--列表项)
- 网格 → [DSGrid](molecules.md#13-dsgrid--网格)
- 折叠面板 → [DSAccordion](molecules.md#32-dsaccordion--折叠面板)
- 轮播 → [DSCarousel](molecules.md#33-dscarousel--轮播)

### 导航
- 顶部栏 → [DSTopBar](layouts.md#21-dstopbar--顶部应用栏) (Small/CenterAligned/Medium/Large)
- 底部导航 → [DSBottomBar](layouts.md#31-dsbottombar--底部导航栏) (含 animated 模式)
- 平板侧边导航 → [DSNavigationRail](layouts.md#32-dsnavigationrail--侧边导航栏)
- 模态抽屉（手机） → [DSModalNavigationDrawer](layouts.md#33-dsmodalnavigationdrawer--模态抽屉)
- 常驻抽屉（平板） → [DSPermanentNavigationDrawer](layouts.md#34-dspermanentnavigationdrawer--常驻抽屉)
- Tab 标签 → [DSTabRow](layouts.md#41-dstabrow--标签栏) / [DSTabsWithPager](layouts.md#42-dstabswithpager--标签--滑动页面)
- 搜索栏 → [DSSearchBar](molecules.md#410-dssearchbar--搜索栏)

### 弹层
- 确认对话框 → [DSDialog](molecules.md#41-dsdialog--对话框)
- 简单选择对话框 → [DSSimpleDialog](molecules.md#42-dssimpledialog--简单选择对话框)
- 全屏对话框（编辑） → [DSFullScreenDialog](molecules.md#43-dsfullscreendialog--全屏对话框)
- 模态底部弹层 → [DSBottomSheet](molecules.md#44-dsbottomsheet--模态底部弹层)
- 常驻底部弹层 → [DSPersistentBottomSheet](molecules.md#45-dspersistentbottomsheet--常驻底部弹层)
- 长按菜单 → [DSContextMenu](molecules.md#46-dscontextmenu--上下文菜单)

### 状态占位
- 加载/错误/空统一容器 → [DSNetWorkView](layouts.md#51-dsnetWorkview--网络状态容器)
- 状态机范式 → [DSPageStateLayout + DSUiState](00-foundation.md#10-uistate-状态机范式)
- 全屏加载 → [DSFullScreenLoading](layouts.md#52-dsfullscreendloading--全屏加载)
- 全屏错误 → [DSFullScreenError](layouts.md#53-dsfullscreenerror--全屏错误)
- 全屏空状态 → [DSFullScreenEmpty](layouts.md#54-dsfullscreenempty--全屏空状态)
- 卡片内空状态 → [DSEmptyState](layouts.md#55-dsemptystate--空状态基础组件)

### 装饰
- 图标 → [DSIcon](atoms.md#41-dsicon--图标)
- 头像 → [DSAvatar](atoms.md#45-dsavatar--头像)
- 标签 Chip → [DSChip](molecules.md#31-dschap--标签-chip)（Assist/Filter/Input/Suggestion）
- 小标签 → [DSTag](atoms.md#44-dstag--标签)
- 徽标 → [DSBadge](atoms.md#43-dsbadge--徽标)
- 分割线 → [DSDivider](atoms.md#42-dsdivider--分割线)

---

## 🌳 决策树

### 选哪个 Button？

```
是主操作 CTA？
├─ 是 → DSButton(Filled, size=Large)
├─ 否 → 是危险操作（删除/撤销）？
│       ├─ 是 → DSButton(Error)
│       └─ 否 → 是次主操作？
│               ├─ 是 → DSButton(Tonal)
│               └─ 否 → 是辅助操作？
│                       ├─ 是 → DSButton(Outlined)
│                       └─ 否 → 是三级操作？
│                               ├─ 是 → DSButton(Text)
│                               └─ 否 → 是工具栏图标？ → DSIconButton
└─ 否 → 是浮动新建？ → DSFAB
```

### 选哪个 Card？

```
业务场景
├─ 列表项（联系人/商品/通知） → AppStructuredCard（数据驱动）
├─ 表单分组（标题+字段+操作） → AppCommonCard（slot 驱动）
├─ 信息展示（标题+内容） → AppCommonCard
├─ 设置项（icon+标题+箭头） → AppStructuredCard
└─ 完全自定义结构 → DSCard
```

### 选哪个状态组件？

```
页面有几个状态？
├─ 1 个（纯加载/纯错误/纯空） → DSFullScreenLoading / DSFullScreenError / DSFullScreenEmpty
├─ 多个（加载+错误+空+成功）
│   ├─ 简单（4 态） → DSNetWorkView（slot 驱动）
│   └─ 复杂（含 LoadingMore/Refreshing/PartialError） → DSPageStateLayout + DSUiState（状态机）
└─ 仅卡片内空状态 → DSEmptyState（不居中，跟卡片布局走）
```

### 选哪个导航？

```
窗口尺寸类
├─ Compact（手机竖屏） → DSBottomBar + DSModalNavigationDrawer（按需）
├─ Medium（小平板） → DSNavigationRail + DSModalNavigationDrawer（按需）
└─ Expanded（平板/桌面） → DSPermanentNavigationDrawer 常驻 + DSNavigationRail
```

### 选哪个 TopBar？

```
标题长度
├─ 短（≤ 8 字） → DSTopBarStyle.Small 或 CenterAligned
├─ 中（9~16 字） → DSTopBarStyle.Medium（折叠）
└─ 长 / 大标题 → DSTopBarStyle.Large（折叠）

是否需要居中？
├─ 设置页 / 简单列表 → CenterAligned
└─ 其他 → Small / Medium / Large
```

---

## 📋 完整组件清单

### 按层级

| 层 | 文档 | 组件数 |
|---|---|---|
| Foundation | [00-foundation.md](00-foundation.md) | Token/Theme/Motion/A11y/Layout/Perf/Preview/Util/UiState |
| Atom 原子 | [atoms.md](atoms.md) | 18 |
| Molecule 分子 | [molecules.md](molecules.md) | 28 |
| Layout 布局 | [layouts.md](layouts.md) | 16 + 6 个页面模板 |
| Container 容器 | [containers.md](containers.md) | 2 |

### 按字母

<details>
<summary>展开字母索引（77 个组件）</summary>

A: AppCommonCard / AppStructuredCard / [DSAccordion](molecules.md#32-dsaccordion--折叠面板) / [DSAvatar](atoms.md#45-dsavatar--头像)

B: [DSBadge](atoms.md#43-dsbadge--徽标) / [DSBanner](layouts.md#56-dsbanner--横幅提示) / [DSBottomAppBar](layouts.md#22-dsbottomappbar--底部应用栏) / [DSBottomBar](layouts.md#31-dsbottombar--底部导航栏) / [DSBottomSheet](molecules.md#44-dsbottomsheet--模态底部弹层) / [DSButton](atoms.md#11-dsbutton--通用按钮)

C: [DSCard](molecules.md#11-dscard--卡片) / [DSCarousel](molecules.md#33-dscarousel--轮播) / [DSCheckbox](atoms.md#61-dscheckbox--复选框) / [DSChip](molecules.md#31-dschap--标签-chip) / [DSContextMenu](molecules.md#46-dscontextmenu--上下文菜单) / [DSCircularProgress](atoms.md#52-dscircularprogress--圆形进度)

D: [DSDatePicker](molecules.md#25-dsdatepicker--日期选择器) / [DSDialog](molecules.md#41-dsdialog--对话框) / [DSDivider](atoms.md#42-dsdivider--分割线) / [DSDropdown](atoms.md#23-dsdropdown--下拉选择器)

E: [DSEmptyState](layouts.md#55-dsemptystate--空状态基础组件)

F: [DSFAB](molecules.md#49-dsfab--悬浮按钮) / [DSFullScreenDialog](molecules.md#43-dsfullscreendialog--全屏对话框) / [DSFullScreenEmpty](layouts.md#54-dsfullscreenempty--全屏空状态) / [DSFullScreenError](layouts.md#53-dsfullscreenerror--全屏错误) / [DSFullScreenLoading](layouts.md#52-dsfullscreendloading--全屏加载)

G: [DSGrid](molecules.md#13-dsgrid--网格)

I: [DSIcon](atoms.md#41-dsicon--图标) / [DSIconButton](atoms.md#11-dsiconbutton--图标按钮)

L: [DSLinearProgress](atoms.md#51-dslinearprogress--线性进度条) / [DSListItem](molecules.md#12-dslistitem--列表项)

M: [DSModalNavigationDrawer](layouts.md#33-dsmodalnavigationdrawer--模态抽屉) / [DSMultiSegmentedControl](molecules.md#21-dssegmentedcontrol--分段选择器单选)

N: [DSNetWorkView](layouts.md#51-dsnetWorkview--网络状态容器) / [DSNavigationRail](layouts.md#32-dsnavigationrail--侧边导航栏)

P: [DSPageStateLayout](00-foundation.md#10-uistate-状态机范式) / [DSPagination](molecules.md#35-dspagination--分页) / [DSPermanentNavigationDrawer](layouts.md#34-dspermanentnavigationdrawer--常驻抽屉) / [DSPersistentBottomSheet](molecules.md#45-dspersistentbottomsheet--常驻底部弹层) / [DSPullToRefresh](molecules.md#34-dspulltorefresh--下拉刷新)

R: [DSRadioButton](atoms.md#63-dsradiobutton--单选框) / [DSRangeSlider](molecules.md#22-dsrangeslider--范围滑块) / [DSRatingBar](molecules.md#24-dsratingbar--评分条)

S: [DSSearchBar](molecules.md#410-dssearchbar--搜索栏) / [DSSegmentedControl](molecules.md#21-dssegmentedcontrol--分段选择器单选) / [DSSimpleDialog](molecules.md#42-dssimpledialog--简单选择对话框) / [DSSkeleton](atoms.md#53-dsskeleton--骨架屏) / [DSSlider](atoms.md#64-dsslider--滑块) / [DSSnackbar](molecules.md#48-dssnackbar--反馈条) / [DSStepper](molecules.md#23-dsstepper--数值步进器) / [DSSwitch](atoms.md#62-dsswitch--开关)

T: [DSTabRow](layouts.md#41-dstabrow--标签栏) / [DSTabsWithPager](layouts.md#42-dstabswithpager--标签--滑动页面) / [DSTag](atoms.md#44-dstag--标签) / [DSText](atoms.md#3-文本类) / [DSTextArea](atoms.md#22-dstextarea--多行文本输入) / [DSTextField](atoms.md#21-dstextfield--文本输入框) / [DSTimePicker](molecules.md#26-dstimepicker--时间选择器) / [DSTooltip](molecules.md#47-dstooltip--提示) / [DSTopBar](layouts.md#21-dstopbar--顶部应用栏)

U: [DSUiState](00-foundation.md#10-uistate-状态机范式)

</details>

---

## 📐 文档结构

```
components/
├── README.md         ← 你在这里（速查表 + 决策树）
├── 00-foundation.md  ← Foundation 层（Token/Theme/Motion/A11y/Layout/Perf/Preview/Util/UiState）
├── atoms.md          ← Atom 原子组件（18 个）
├── molecules.md      ← Molecule 分子组件（28 个）
├── layouts.md        ← Layout 布局组件（16 个）+ 6 个页面模板
└── containers.md     ← Container 业务容器壳（2 个）
```

**单一真相源原则**：每个组件的完整 API 只在一个文档定义，本文件只放链接和决策树。
