# 14. 增量开发工作流

> 脚手架不是静态模板，而是**随项目成长的活体**。
> 本文件指导业务方如何从脚手架起步，在开发过程中按需扩展脚手架能力。
> AI 在被要求「添加 XXX 能力」时，按本文件的决策树判断是「业务代码」还是「脚手架扩展」。

---

## 核心理念

```
脚手架 = 独立 git 仓库，包含 core + designsystem + navigation + feature 示例 + AI 规则
应用 = clone 脚手架 → 改包名 → 开发业务 → 通用能力 backport 回脚手架
```

**工作流**：
1. `git clone` 脚手架 → `./scripts/create-app.sh` 改包名 → 开始开发
2. 开发中按模板 A/B/C/D 生成业务 Feature（不改脚手架核心）
3. 发现需要新能力 → 直接在当前 App 中创建
4. 如果通用 → `./scripts/backport.sh` 回传到脚手架仓库
5. 下一个 App clone 脚手架时自动获得

---

## 阶段 0：创建应用（2 分钟）

```bash
# 克隆脚手架
git clone https://github.com/yourorg/pai-scaffold.git my-app
cd my-app

# 改包名
./scripts/create-app.sh com.company.myapp "My App"

# 初始化 git（create-app.sh 已自动 commit）
git remote remove origin
git remote add origin https://github.com/yourorg/my-app.git
git push -u origin main
```

---

## 阶段 1：清理示例 + 接入真实 API（30 分钟）

### 1.1 改 BASE_URL

```kotlin
// app/build.gradle.kts 中的 debug / release buildType 各自的 buildConfigField
// debug { buildConfigField("String", "BASE_URL", "\"https://dev-api.yourcompany.com/v1/\"") }
// release { buildConfigField("String", "BASE_URL", "\"https://api.yourcompany.com/v1/\"") }
// 代码中读 BuildConfig.BASE_URL
```

### 1.2 删除示例模板

```bash
rm app/src/.../core/network/model/ExampleDto.kt
rm app/src/.../core/database/entity/ExampleEntity.kt
rm app/src/.../core/database/dao/ExampleDao.kt
rm app/src/.../core/data/ExampleRepository.kt
# AppApi 中删除示例方法
# AppDatabase 中删除 ExampleEntity 注册
# DatabaseModule 中删除 provideExampleDao
```

### 1.3 改品牌色

```kotlin
// core/designsystem/foundation/tokens/BrandColorPalette.kt
// 修改 DSBrandColor 枚举（5 套预设：Indigo / Emerald / Rose / Amber / Sky）
// 业务方有 2 种方式自定义品牌色：
//   1. 直接选现有预设：在设置页调用 mainViewModel.setBrandColor(DSBrandColor.Rose)
//   2. 改预设色阶：在 BrandColorPalette.kt 的 palette mapOf 中替换 50-900 色阶
// 例如把 Indigo 主色改成红色：
Indigo -> mapOf(
    50 to Color(0xFFFEF2F2), 100 to Color(0xFFFEE2E2),
    // ...
    600 to Color(0xFFE63946),  // 主色 600 阶
    700 to Color(0xFFC92A3A), 800 to Color(0xFF8B1B27), 900 to Color(0xFF5A111A),
)
```

### 1.4 替换 AuthRepository.login

```kotlin
// core/data/AuthRepository.kt
// 删除模拟登录，改为真实 API
suspend fun login(email: String, password: String): ApiResult<UserItem> {
    val result = safeApiCall { api.login(LoginRequest(email, password)) }
    if (result is ApiResult.Success) {
        // result.data 是 UserItem domain model；原 UserEntity 已封装在 Repository 内
        userState.onLoginSuccess(result.data.id, result.data.token)
    }
    return result
}
// 注意：token 会由 UserState.onLoginSuccess 内部委托给 EncryptedPrefs 存储（不进 UserPreferences 明文）
```

### 1.5 改 App 图标

```bash
# Android Studio → Image Asset → 替换图标
```

---

## 阶段 2：开发业务 Feature（每个 15 分钟）

### 决策树：用哪个模板？

```
这个页面需要网络请求吗？
├─ 是 → 请求返回单个数据？
│       ├─ 是 → 用模板 A（BaseNetWorkViewModel + DSNetWorkView）
│       └─ 否（返回列表+分页）→ 用模板 D（列表分页页）
└─ 否 → 需要表单输入+校验？
        ├─ 是 → 用模板 C（表单页，H16 例外条款：继承 BaseViewModel 手写 when(ApiResult)）
        └─ 否 → 用模板 B（纯状态页）
```

> 模板 A 内部用 `requestApiFlow(): Flow<ApiResult<T>>`（不是 NetResult，已删除）。

### 开发流程

```bash
claude  # 或 opencode
> 添加一个商品列表页，支持分页和下拉刷新
# AI 按 docs/rules/09-feature-templates.md 模板 D 生成
```

AI 生成的内容：
- `feature/product/ProductViewModel.kt`（按模板 D）
- `feature/product/ProductScreen.kt`（按模板 D）
- `core/network/model/ProductDto.kt`
- `core/data/ProductRepository.kt`（@Inject constructor，返回 `ApiResult<List<ProductItem>>`，把 Entity/DTO 转换为 ProductItem domain model）
- `navigation/routes/AppRoutes.kt` 加 `ProductRoute`
- `navigation/extension/NavExtensions.kt` 加 `appNavigator.gotoProduct()`（走 AppNavigator.navigate，RouteInterceptor 生效）
- `MainActivity.kt` 的 AppNavGraph 挂载

**这是业务代码，不回传脚手架。**

### 启动时根据登录态初始化的 Feature

Feature 若需在启动时根据登录态初始化，应 await `UserState.isInitialized` 后再读 `isLoggedIn`：

```kotlin
@Composable
internal fun MyScreen(viewModel: MyViewModel = hiltViewModel()) {
    val userState by viewModel.userState.isInitialized.collectAsStateWithLifecycle()
    LaunchedEffect(userState) {
        if (userState) {
            // isInitialized=true 后才能可靠读 isLoggedIn
            viewModel.initBasedOnLoginState()
        }
    }
}
```

**不要**直接读 `userState.isLoggedIn.value`（可能读到 stale 值，因 `initialize()` 是异步执行）。

---

## 阶段 3：按需扩展脚手架

### 决策树：这是脚手架扩展还是业务代码？

```
这个需求是否需要「所有 Feature 都可能用到」的能力？
├─ 是 → 脚手架扩展
│       → 在当前 App 中创建
│       → 如果通用，backport 回脚手架仓库
│       → 下一个 App clone 时自动获得
└─ 否 → 业务代码
        → 只改 feature/xxx/
        → 不回传脚手架
```

### 场景 1：需要新 DS 组件

```bash
# 在当前 App 中创建
> 给 designsystem 加一个 DSRatingBar 评分组件
# AI 创建 DSRatingBar.kt（internal + @Preview + KDoc）

# 如果通用，回传脚手架
./scripts/backport.sh ~/projects/pai-scaffold \
  app/src/.../core/designsystem/primitives/DSRatingBar.kt
```

### 场景 2：需要新基础设施（如 WorkManager）

```bash
# 在当前 App 中创建
> 给脚手架加 WorkManager 后台任务能力
# AI 在 core/util/work/ 创建 WorkScheduler + Worker

# 回传脚手架
./scripts/backport.sh ~/projects/pai-scaffold \
  app/src/.../core/util/work/WorkScheduler.kt \
  app/src/.../core/util/work/SyncWorker.kt
```

### 场景 3：需要新页面模板

```bash
# 在当前 App 中写了一个搜索页
> 这个搜索页模式不错，加到 docs/rules/09-feature-templates.md
# AI 追加模板 E 到规则文件

# 回传脚手架
./scripts/backport.sh ~/projects/pai-scaffold \
  docs/rules/09-feature-templates.md
```

### 场景 4：需要新工具类

```bash
# 在当前 App 中创建
> 加一个 TimeUtils 到 core/util
# AI 创建 TimeUtils.kt

# 回传脚手架
./scripts/backport.sh ~/projects/pai-scaffold \
  app/src/.../core/util/TimeUtils.kt
```

---

## backport 脚本说明

### 用法

```bash
./scripts/backport.sh <脚手架仓库路径> <文件1> [文件2] ...
```

### 功能

1. 把指定文件从当前 App 复制到脚手架仓库
2. 自动把包名从 App 包名替换回 `com.pai.app`
3. 在脚手架仓库 `git add + commit`
4. 打印 commit hash

### 判断什么该 backport

| 改动 | backport？ | 理由 |
|------|-----------|------|
| 新 DS 组件 | ✅ 是 | 其他 App 可能需要 |
| 新动效工具 | ✅ 是 | 通用能力 |
| 新页面模板 | ✅ 是 | 其他 App 可能用相同模式 |
| 新基础设施 | ✅ 是 | 通用能力 |
| 新工具类 | ✅ 是 | 通用能力 |
| 业务 Feature | ❌ 否 | 仅此 App 需要 |
| 业务 API 接口 | ❌ 否 | 仅此 App 需要 |
| 品牌色定制 | ❌ 否 | 每个 App 不同 |

---

## 脚手架扩展的规则

### 可以扩展的位置

| 位置 | 扩展内容 | 需更新文档 |
|------|---------|-----------|
| `core/designsystem/primitives/` | 新 DS 组件 | `07-ui-components.md` |
| `core/designsystem/foundation/motion/` | 新动效工具 | `07-ui-components.md` |
| `core/designsystem/foundation/theme/` | 新主题能力 | `01-project-overview.md` |
| `core/base/` | 新基类 | `06-viewmodel.md` |
| `core/data/` | 新 Repository 模式 | `06-viewmodel.md` |
| `core/network/` | 新网络能力 | `01-project-overview.md` |
| `core/util/` | 新工具类 | `12-commands.md` |
| `navigation/` | 新导航能力 | `04-routing.md` |
| `docs/rules/` | 新规则文件 | `00-documentation-protocol.md` |

### 扩展时的强制规则

1. **先在当前 App 中创建**：不要等，直接写
2. **通用则 backport**：用 `./scripts/backport.sh` 回传脚手架
3. **新组件加 `internal`**：单模块下 internal 可见
4. **新组件加 `@Preview`**：用 `DSDesignTheme` 包裹
5. **新基类加 KDoc + 模板**：在 `09-feature-templates.md` 追加使用模板
6. **Gallery 中展示**：新 DS 组件应在 Gallery 中追加展示页

---

## 多应用管理

```
~/projects/
├── pai-scaffold/          ← 脚手架仓库（上游）
├── app-a/                 ← App A（clone 改包名，独立 git）
├── app-b/                 ← App B（clone 改包名，独立 git）
└── app-c/                 ← App C（clone 改包名，独立 git）
```

每个 App 独立 git 仓库，脚手架是上游。
App 中的通用改动通过 `backport.sh` 回传脚手架。
下一个 App clone 脚手架时自动获得。

---

## 开发节奏建议

### 每周节奏（单人 + AI）

```
周一：需求拆解 + AI 生成骨架（5-6 个 Feature）
周二-周三：填充业务逻辑 + 联调
周四：脚手架扩展（如果有新需求）+ backport
周五：测试 + CI + 文档更新
```

### 里程碑检查

| 里程碑 | 检查项 |
|--------|--------|
| 第 1 周末 | 示例清理完成 + 真实 API 接入 + 登录跑通 |
| 第 2 周末 | 核心业务页面完成（3-5 个 Feature） |
| 第 4 周末 | 所有业务页面完成 + 脚手架扩展稳定 |
| 第 6 周末 | 测试覆盖 + CI 通过 + 文档完整 |

### 何时考虑拆分多模块

当出现以下信号时，可考虑把脚手架拆为 Library 模块：
- 单次冷启动编译 > 30 秒
- AI 频繁产生幻觉
- 代码量超过 5 万行
- 多个 App 需要同时引用脚手架（而非各自 clone）

---

## AI 指令模板：扩展脚手架

### 模板 1：加 DS 组件

```
core/designsystem/primitives 缺少 [组件名] 组件，帮我加一个：
- 功能：[描述]
- 参数：[列出]
- 风格：[与现有 DS 组件一致]

约束：
- 加 internal
- 加 @Preview（用 DSDesignTheme 包裹）
- 加 KDoc（含使用示例）
- 更新 docs/rules/07-ui-components.md 组件清单
- 在 Gallery 中追加展示
```

### 模板 2：加基础设施

```
脚手架需要加 [能力名] 基础设施：
- 用途：[描述]
- 依赖：[列出第三方库]

约束：
- 放在 core/[合适位置]
- 加 Hilt Module 注入
- 更新 docs/rules/01-project-overview.md 技术栈
- 更新 PaiApplication（如需初始化）
```

### 模板 3：加新页面模板

```
我刚写了一个 [页面类型] 页面，模式不错，加到脚手架模板：
- 与现有模板 A/B/C/D 的区别：[描述]
- 完整代码在 feature/[name]/

约束：
- 在 docs/rules/09-feature-templates.md 追加「模板 X：[名称]」
- 提取通用模式（去掉业务细节）
- 含完整 ViewModel + Screen 代码模板
```
