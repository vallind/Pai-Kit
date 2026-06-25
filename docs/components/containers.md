# Container 业务容器壳

> 文档版本：v1.0 · 2026-06-25  
> 适用范围：`core/designsystem/containers/`  
> 定位：基于 DS 组件封装的「业务容器壳」，提供业务常见的卡片结构，强制规范

---

## 目录

1. [设计哲学](#1-设计哲学)
2. [AppCommonCard 通用业务卡](#2-appcommoncard-通用业务卡)
3. [AppStructuredCard 结构化业务卡](#3-appstructuredcard-结构化业务卡)
4. [全场景使用规范](#4-全场景使用规范)
5. [vs DSCard 对比](#5-vs-dscard-对比)
6. [最佳实践](#6-最佳实践)

---

## 1. 设计哲学

### 1.1 为什么需要 Container 层？

**问题**：DSCard 是纯容器（surface + elevation + 圆角），无内置结构。业务每次用 DSCard 都要自己写「标题 + 副标题 + 操作 + 内容」的布局，导致：

- 同类页面卡片结构不一致（间距、字号、对齐方式各异）
- 重复代码（每个 feature 都写一遍 header Row + content Box + footer Row）
- 难以统一升级（如要改所有卡片的标题字号，要改 N 处）

**解决**：AppCommonCard / AppStructuredCard 封装业务常见结构，提供固定 slot 模式，业务只填数据/slot，结构由容器保证。

### 1.2 Container 层与 DS 通用组件的区别

| 维度 | DS 通用组件（DSCard 等） | Container 业务容器（AppCommonCard 等） |
|---|---|---|
| 命名前缀 | `DS` | `App` |
| 通用性 | 跨业务复用 | 当前 App 业务复用 |
| 抽象层级 | 纯 UI（无业务语义） | 业务语义（标题/副标题/操作） |
| Slot | 完全自由 | 固定 slot 模式 |
| 修改影响 | 所有业务 | 当前 App |
| 位置 | `core/designsystem/primitives/` | `core/designsystem/containers/` |

### 1.3 两个容器的关系

```
┌─────────────────────────────────────────────┐
│ AppStructuredCard  数据驱动（强约束）         │
│ - 接受 data class 契约                       │
│ - 固定结构：leading + title/subtitle +       │
│   trailing + 可选扩展                        │
│ - 适合：列表项 / 设置项 / 通知项              │
├─────────────────────────────────────────────┤
│ AppCommonCard  slot 驱动（中等约束）          │
│ - 接受 content slot                          │
│ - 结构：title + subtitle + headerAction +    │
│   content + footerActions                    │
│ - 适合：分组卡 / 表单卡 / 信息卡              │
├─────────────────────────────────────────────┤
│ DSCard  纯容器（无约束）                      │
│ - 只有 surface + elevation + 圆角            │
│ - 完全自由的内容 slot                        │
│ - 适合：自定义结构                            │
└─────────────────────────────────────────────┘
```

**选择原则**：能用 AppStructuredCard 就用 AppStructuredCard；不满足用 AppCommonCard；还不满足用 DSCard。

---

## 2. AppCommonCard 通用业务卡

> 文件：`core/designsystem/containers/AppCommonCard.kt`

### 2.1 API

```kotlin
@Composable
fun AppCommonCard(
    title: String,                                          // 必填
    modifier: Modifier = Modifier,
    subtitle: String? = null,                               // 可选副标题
    headerActionIcon: ImageVector? = null,                  // 头部右侧操作图标
    headerActionContentDescription: String? = null,
    onHeaderActionClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,                          // 整卡点击
    style: DSCardStyle = DSCardStyle.Elevated,              // 卡片风格
    footerActions: (@Composable RowScope.() -> Unit)? = null, // 底部操作行
    content: @Composable () -> Unit                         // 主内容 slot
)
```

### 2.2 内置结构

```
┌─────────────────────────────────────────────┐
│ [Title]                          [Action]   │  ← header
│ [Subtitle]                                  │
├─────────────────────────────────────────────┤
│                                             │
│  content slot                               │  ← 主内容
│                                             │
├─────────────────────────────────────────────┤
│                       [Cancel] [Confirm]    │  ← footerActions（可选）
└─────────────────────────────────────────────┘
```

### 2.3 5 个 Slot 说明

| Slot | 必填 | 类型 | 说明 |
|---|---|---|---|
| `title` | ✅ | String | 标题（TitleMedium + SemiBold + OnSurface） |
| `subtitle` | ❌ | String? | 副标题（BodySmall + OnSurfaceVariant） |
| `headerActionIcon` + `onHeaderActionClick` | ❌ | ImageVector + callback | 头部右侧 IconButton（如 MoreVert / Edit） |
| `content` | ✅ | @Composable slot | 主内容，padding horizontal = lg |
| `footerActions` | ❌ | RowScope slot | 底部操作行，右对齐，水平间距 md |

### 2.4 使用示例

#### 场景 1：信息展示卡（标题 + 副标题 + 内容）

```kotlin
AppCommonCard(
    title = "基本信息",
    subtitle = "用户档案核心字段"
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        InfoRow(label = "姓名", value = user.name)
        InfoRow(label = "邮箱", value = user.email)
        InfoRow(label = "手机", value = user.phone)
    }
}
```

#### 场景 2：列表区域卡（标题 + 头部「查看全部」+ 内容）

```kotlin
AppCommonCard(
    title = "最近订单",
    headerActionIcon = Icons.Default.ChevronRight,
    headerActionContentDescription = "查看全部",
    onHeaderActionClick = { navController.navigate(AllOrdersRoute) }
) {
    OrderList(orders.take(3))
}
```

#### 场景 3：表单分组卡（标题 + 内容 + 底部操作）

```kotlin
AppCommonCard(
    title = "收货地址",
    footerActions = {
        DSButton(text = "取消", onClick = { /* ... */ }, style = DSButtonStyle.Text)
        DSButton(text = "保存", onClick = { vm.save() })
    }
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DSTextField(value = name, onValueChange = { }, label = "收件人")
        DSTextField(value = phone, onValueChange = { }, label = "手机号")
        DSTextArea(value = address, onValueChange = { }, label = "详细地址", maxLength = 200)
    }
}
```

#### 场景 4：可点击卡（整卡点击 + 头部操作）

```kotlin
AppCommonCard(
    title = "订单 #20260625001",
    subtitle = "2026-06-25 14:30",
    headerActionIcon = Icons.Default.MoreVert,
    headerActionContentDescription = "更多操作",
    onHeaderActionClick = { showOrderMenu = true },
    onClick = { navController.navigate(OrderDetailRoute(orderId)) }
) {
    OrderSummary(order)
}
```

#### 场景 5：设置分组卡（Outlined 风格）

```kotlin
AppCommonCard(
    title = "通知设置",
    style = DSCardStyle.Outlined
) {
    Column {
        DSSwitch(checked = pushEnabled, onCheckedChange = { }, label = "推送通知")
        DSSwitch(checked = emailEnabled, onCheckedChange = { }, label = "邮件通知")
    }
}
```

#### 场景 6：统计卡（Filled 风格 + 大数字内容）

```kotlin
AppCommonCard(
    title = "本月销售",
    subtitle = "2026 年 6 月",
    style = DSCardStyle.Filled
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DSText("¥ 128,500", variant = DSTextVariant.HeadlineMedium, color = DSTextColor.Primary)
        DSText("较上月增长 12.5%", variant = DSTextVariant.BodySmall, color = DSTextColor.Success)
    }
}
```

---

## 3. AppStructuredCard 结构化业务卡

> 文件：`core/designsystem/containers/AppStructuredCard.kt`

### 3.1 数据契约

```kotlin
@Stable
data class AppStructuredCardData(
    val title: String,                       // 必填
    val subtitle: String? = null,
    val leadingIcon: ImageVector? = null,    // 左侧主图标
    val leadingAvatarUrl: String? = null,    // 左侧头像（与 icon 互斥，优先 avatar）
    val trailingMeta: String? = null,        // 右侧元信息（如「99+」「3 分钟前」）
    val trailingMetaIcon: ImageVector? = null, // 右侧元信息图标（如箭头）
    val badgeText: String? = null,           // 徽标文字（如「NEW」「HOT」）
    val destructive: Boolean = false         // 破坏性卡片（标题用 error 色）
)
```

### 3.2 API

```kotlin
@Composable
fun AppStructuredCard(
    data: AppStructuredCardData,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    style: DSCardStyle = DSCardStyle.Filled,
    content: (@Composable () -> Unit)? = null   // 可选扩展 slot
)
```

### 3.3 内置结构

```
┌─────────────────────────────────────────────┐
│ [Avatar/Icon] [Title]            [Meta] [⌄] │  ← 主行
│                [Subtitle]          [Badge]  │
├─────────────────────────────────────────────┤
│                                             │
│  扩展内容 slot（可选）                       │  ← content
│                                             │
└─────────────────────────────────────────────┘
```

### 3.4 使用示例

#### 场景 1：联系人列表项

```kotlin
AppStructuredCard(
    data = AppStructuredCardData(
        title = "张三",
        subtitle = "产品经理 · 字节跳动",
        leadingAvatarUrl = user.avatarUrl,
        trailingMeta = "138****8888",
        badgeText = "NEW"
    ),
    onClick = { navController.navigate(UserDetailRoute(user.id)) }
)
```

#### 场景 2：设置项

```kotlin
AppStructuredCard(
    data = AppStructuredCardData(
        title = "通知",
        subtitle = "已开启 3 项",
        leadingIcon = Icons.Default.Notifications,
        trailingMetaIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight
    ),
    onClick = { navController.navigate(NotificationSettingsRoute) }
)
```

#### 场景 3：通知项（带头像 + 时间 + 未读 badge）

```kotlin
AppStructuredCard(
    data = AppStructuredCardData(
        title = "李四 关注了你",
        subtitle = "2 分钟前",
        leadingAvatarUrl = notification.actorAvatar,
        trailingMeta = "刚刚",
        badgeText = "1"
    ),
    onClick = { markAsRead(notification.id) }
)
```

#### 场景 4：统计卡（带扩展内容）

```kotlin
AppStructuredCard(
    data = AppStructuredCardData(
        title = "本月销售额",
        subtitle = "2026 年 6 月",
        leadingIcon = Icons.Default.TrendingUp,
        trailingMeta = "+12.5%"
    )
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        DSText("¥ 128,500", variant = DSTextVariant.HeadlineMedium, color = DSTextColor.Primary)
        DSText("较上月增长 12.5%", variant = DSTextVariant.BodySmall, color = DSTextColor.Success)
        SalesTrendChart(data = salesData)  // 自定义图表
    }
}
```

#### 场景 5：破坏性卡片（退出登录 / 删除）

```kotlin
AppStructuredCard(
    data = AppStructuredCardData(
        title = "退出登录",
        subtitle = "将清除本地缓存",
        leadingIcon = Icons.Default.Logout,
        destructive = true
    ),
    onClick = { showLogoutConfirm = true }
)
```

#### 场景 6：商品列表项

```kotlin
AppStructuredCard(
    data = AppStructuredCardData(
        title = product.name,
        subtitle = product.category,
        leadingAvatarUrl = product.imageUrl,
        trailingMeta = "¥${product.price}",
        badgeText = if (product.isNew) "NEW" else null
    ),
    onClick = { navController.navigate(ProductDetailRoute(product.id)) }
)
```

---

## 4. 全场景使用规范

### 4.1 场景对照表

| 业务场景 | 推荐容器 | 关键参数 |
|---|---|---|
| 联系人/商品/通知列表项 | AppStructuredCard | `leadingAvatarUrl` + `title/subtitle` + `trailingMeta` + `badgeText` |
| 设置项 | AppStructuredCard | `leadingIcon` + `title/subtitle` + `trailingMetaIcon = ChevronRight` |
| 退出登录 / 删除账户 | AppStructuredCard | `destructive = true` + `leadingIcon` |
| 统计卡（带数字 + 趋势） | AppStructuredCard + 扩展 slot | `leadingIcon = TrendingUp` + `trailingMeta = "+12.5%"` + `content = { DSText("¥128,500", HeadlineMedium) }` |
| 表单分组卡 | AppCommonCard | `title` + `content = { TextFields }` + `footerActions = { 取消/保存 }` |
| 信息展示卡 | AppCommonCard | `title` + `subtitle` + `content = { InfoRows }` |
| 列表区域卡 | AppCommonCard | `title` + `headerActionIcon = ChevronRight` + `content = { 列表 }` |
| 订单卡（可点击 + 操作） | AppCommonCard | `title` + `subtitle` + `headerActionIcon = MoreVert` + `onClick` |
| 设置分组卡 | AppCommonCard | `title` + `style = Outlined` + `content = { Switches }` |
| 完全自定义结构 | DSCard | 直接传 content slot |

### 4.2 风格选择规范

| 风格 | 视觉 | 适用场景 |
|---|---|---|
| `Elevated`（默认） | 默认阴影 | AppCommonCard 默认；首页/详情页主卡片 |
| `Filled` | 背景色填充无阴影 | AppStructuredCard 默认；列表项卡（与背景区分） |
| `Outlined` | 描边无阴影 | 设置项卡；强调边界 |

### 4.3 点击行为规范

| 组件 | 点击模式 |
|---|---|
| AppCommonCard | `onClick` 非 null 时整卡可点击 |
| AppStructuredCard | `onClick` 非 null 时整卡可点击 |
| 头部操作 | 独立点击区域，不影响整卡点击 |

**注意**：整卡可点击时，内部不要放其他可点击组件（如按钮），否则点击事件冲突。

### 4.4 内容布局规范

#### AppCommonCard 内容 slot

- 默认水平 padding = `DSTokens.Spacing.lg`（16dp）
- 内容用 `Column(verticalArrangement = Arrangement.spacedBy(12.dp))` 组织
- 多个 TextField 用 spacedBy，不要手动 padding
- 多行 InfoRow 用 spacedBy

#### AppStructuredCard 扩展 slot

- 默认水平 padding = `DSTokens.Spacing.md`（12dp）
- 与主行底部对齐，padding = `DSTokens.Spacing.md`
- 扩展内容适合放图表、明细列表、操作按钮行

### 4.5 数据契约扩展

如果 `AppStructuredCardData` 字段不够用：

1. **优先**：检查是否能用 `content` 扩展 slot 实现
2. **次选**：在业务层继承或包装（创建业务 data class，转换为 AppStructuredCardData）
3. **最后**：提交 issue 讨论是否给 AppStructuredCardData 加字段

```kotlin
// 业务层数据类
data class OrderItem(
    val id: String,
    val name: String,
    val status: String,
    val price: Double,
    val imageUrl: String
)

// 转换为 AppStructuredCardData
fun OrderItem.toCardData() = AppStructuredCardData(
    title = name,
    subtitle = status,
    leadingAvatarUrl = imageUrl,
    trailingMeta = "¥$price"
)

// 使用
AppStructuredCard(data = order.toCardData(), onClick = { })
```

---

## 5. vs DSCard 对比

### 5.1 何时用 DSCard

- 完全自定义结构（如 Hero 卡片、特殊布局）
- 不需要标题/副标题/操作的标准结构
- 卡片内嵌套卡片（外层 DSCard，内层 AppCommonCard）
- 性能敏感场景（DSCard 更轻量）

### 5.2 何时用 AppCommonCard

- 需要「标题 + 副标题 + 内容 + 可选操作」结构
- 表单分组、信息展示、列表区域
- 需要底部操作行（取消/保存）
- 业务侧想保证卡片结构一致性

### 5.3 何时用 AppStructuredCard

- 列表项（联系人/商品/通知/设置项）
- 数据驱动渲染（业务传 data class）
- 需要 leading icon/avatar + trailing meta 的固定结构
- 强制业务按规范填充

### 5.4 性能对比

| 组件 | 重组次数 | 内存分配 | 适用列表 |
|---|---|---|---|
| DSCard | 最低 | 最低 | 长列表（100+ 项） |
| AppCommonCard | 中 | 中 | 中等列表（10~50 项） |
| AppStructuredCard | 中 | 中（含 data class） | 中等列表（10~50 项） |

长列表（如商品列表 100+ 项）建议直接用 DSCard + 自定义 Row 优化性能。

---

## 6. 最佳实践

### 6.1 DO ✅

```kotlin
// ✅ 用 AppCommonCard 组织表单
AppCommonCard(title = "基本信息") {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        DSTextField(value = name, onValueChange = { }, label = "姓名")
        DSTextField(value = email, onValueChange = { }, label = "邮箱")
    }
}

// ✅ 用 AppStructuredCard 渲染列表项
LazyColumn {
    items(users, key = { it.id }) { user ->
        AppStructuredCard(
            data = AppStructuredCardData(
                title = user.name,
                subtitle = user.role,
                leadingAvatarUrl = user.avatar
            ),
            onClick = { navController.navigate(UserDetailRoute(user.id)) }
        )
    }
}

// ✅ 用 destructive 标记破坏性操作
AppStructuredCard(
    data = AppStructuredCardData(
        title = "退出登录",
        leadingIcon = Icons.Default.Logout,
        destructive = true
    ),
    onClick = { showLogoutConfirm = true }
)

// ✅ 长列表用 DSCard + 自定义 Row 优化
LazyColumn {
    items(products, key = { it.id }) { product ->
        DSCard(onClick = { /* ... */ }) {
            Row(modifier = Modifier.padding(12.dp)) {
                DSAvatar(imageUrl = product.imageUrl)
                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                    DSText(product.name, variant = DSTextVariant.TitleMedium)
                    DSText("¥${product.price}", variant = DSTextVariant.BodyMedium)
                }
            }
        }
    }
}
```

### 6.2 DON'T ❌

```kotlin
// ❌ 不要在 AppCommonCard 内部再放可点击组件（与整卡点击冲突）
AppCommonCard(
    title = "订单",
    onClick = { /* 整卡点击 */ }
) {
    DSButton(text = "查看", onClick = { /* 冲突！ */ })
}

// ❌ 不要用 DSCard 重复造结构（应该用 AppCommonCard）
DSCard {
    Column {
        Row {
            DSText("标题", variant = DSTextVariant.TitleMedium)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = { }) { Icon(Icons.Default.MoreVert, null) }
        }
        DSText("内容")
    }
}
// ✅ 应该用
AppCommonCard(title = "标题", headerActionIcon = Icons.Default.MoreVert, onHeaderActionClick = { }) {
    DSText("内容")
}

// ❌ 不要在 AppStructuredCard 的 content slot 里放标题（重复）
AppStructuredCard(
    data = AppStructuredCardData(title = "销售额")
) {
    DSText("销售额", variant = DSTextVariant.HeadlineMedium)  // 重复！
    DSText("¥ 128,500")
}
// ✅ 应该用 content slot 放数值和趋势
AppStructuredCard(
    data = AppStructuredCardData(title = "销售额", subtitle = "本月")
) {
    DSText("¥ 128,500", variant = DSTextVariant.HeadlineMedium)
    DSText("+12.5%", variant = DSTextVariant.BodySmall, color = DSTextColor.Success)
}

// ❌ 不要硬编码 padding（用 DSTokens）
AppCommonCard(title = "标题") {
    Box(modifier = Modifier.padding(16.dp)) { /* ❌ */ }
}
// ✅ 应该用 DSTokens
AppCommonCard(title = "标题") {
    Box(modifier = Modifier.padding(DSTokens.Spacing.lg)) { /* ✅ */ }
}
```

### 6.3 与 DSAppScaffold 配合

```kotlin
DSAppScaffold(title = "个人中心") { padding ->
    Column(
        modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 用 AppCommonCard 分组
        AppCommonCard(title = "基本信息", subtitle = "用户档案") {
            UserInfoForm()
        }
        AppCommonCard(title = "通知设置", style = DSCardStyle.Outlined) {
            NotificationToggles()
        }
        // 用 AppStructuredCard 渲染设置项列表
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
```

---

## 附录：Container 文件清单

```
core/designsystem/containers/
├── AppCommonCard.kt          # AppCommonCard + 5 个 slot
└── AppStructuredCard.kt      # AppStructuredCard + AppStructuredCardData 数据契约
```

**未来扩展**：当业务出现新的高频容器结构时，可在此目录新增：

- `AppListItemCard.kt` — 标准列表项卡（如联系人/商品统一形态）
- `AppStatCard.kt` — 统计卡（KPI 数值 + 趋势）
- `AppFormCard.kt` — 表单卡（标题 + 表单字段 + 提交按钮）
- `AppMediaCard.kt` — 媒体卡（图片 + 标题 + 描述）

但**避免过度抽象**：只有当某个结构在 3+ 个页面重复出现时才提取为 Container。
