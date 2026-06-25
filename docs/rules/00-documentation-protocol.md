# 文档自维护协议

> AI 规则文件 - 元规则领域（文档与代码同步机制）
> 本文件由 CLAUDE.md 拆分而来，是 `docs/rules/` 目录的协议入口。

---

## 一、文档与代码的映射关系

AI 在生成或修改代码后，必须按本协议检查并更新对应文档。

| 代码变更类型 | 需更新的文档文件 | 更新内容 |
|------------|----------------|---------|
| 新增 Feature | `09-feature-templates.md` | 如果模式不同于现有模板，追加新模板 |
| 新增 DS 组件 | `07-ui-components.md` | 在组件清单表中追加一行 |
| 新增路由 | `04-routing.md` | 在路由清单中追加 Route 定义 |
| 新增 Repository | `06-viewmodel.md` | 在 Repository 模式说明中追加示例 |
| 新增 savedStateHandle 结果 Key 约定 | `05-navigation-communication.md` | 在「结果回传」示例中追加 key 命名约定 |
| 修改架构规则 | 对应编号文件 | 更新规则描述 |
| 升级依赖版本 | `01-project-overview.md` + `13-troubleshooting.md` | 更新技术栈版本号 + 兼容性说明 |
| Token 安全变更 | `02-package-isolation.md` + `SETUP.md` + `13-troubleshooting.md` | 更新 token 存储说明与 Robolectric 注意事项 |
| 新增测试模式 | `10-testing.md` | 追加测试示例 |
| 新增 CI 步骤 | `11-ci-cd.md` | 追加 CI 配置说明 |
| 新增基础设施 | `14-development-workflow.md` | 在脚手架扩展场景中追加 |
| 新增页面模板 | `09-feature-templates.md` | 追加模板代码 |
| 新增工具类 | `12-commands.md` | 追加工具说明 |

---

## 二、自维护流程

AI 每次完成代码生成后，必须执行以下检查：

1. **识别变更类型**：我这次改了什么？（Feature / 组件 / 路由 / Repository / 架构 / 版本 / 测试 / CI）
2. **查找对应文档**：根据上表找到需更新的 `docs/rules/XX-xxx.md`
3. **判断是否需要更新**：
   - 新增了新模式 → 追加文档
   - 修改了现有规则 → 更新描述
   - 仅业务代码变动（不影响模式） → 跳过
4. **执行更新**：用 Edit 工具更新对应文件
5. **汇报**：在最终回复中说明「已更新文档：XX-xxx.md（追加了...）」

---

## 三、判断标准

- 需要更新：创建了新的可复用模式（如新的页面模板、新的组件用法、新的路由模式）
- 不需要更新：纯业务代码（如新增了一个商品列表页，但模式与模板 A 相同）
- 需要更新：修改了架构规则（如改了 internal 修饰符策略）
- 不需要更新：修了一个 bug（不改模式）

---

## 四、示例

### 场景 1：AI 新增了「带搜索的列表页」

- 这是新模式（不是纯模板 A，也不是模板 B）
- 需要在 `09-feature-templates.md` 追加「模板 E：带搜索的列表页」
- AI 回复中说明：「已更新 docs/rules/09-feature-templates.md，追加模板 E」

### 场景 2：AI 新增了一个 DSDatePicker 组件

- 但 DSDatePicker 已存在
- 不需要更新 `07-ui-components.md`
- AI 回复中说明：「未更新文档（组件已存在）」

### 场景 3：AI 升级了 Hilt 版本到 2.58

- 需要更新 `01-project-overview.md` 的技术栈版本号
- 需要更新 `13-troubleshooting.md` 的兼容性说明
- AI 回复中说明：「已更新 docs/rules/01-project-overview.md + 13-troubleshooting.md（Hilt 2.57→2.58）」

---

## 五、文档目录索引

| 文件 | 对应 CLAUDE.md 章节 | 领域 |
|------|-------------------|------|
| `00-documentation-protocol.md` | 17 + 18（元规则） | 文档自维护协议 + 全局禁止事项 + 项目记忆维护 |
| `01-project-overview.md` | 1 + 2 + 3 + 19 | 项目快照 / 技术栈 / 目录结构 / AI 工具兼容性 |
| `02-package-isolation.md` | 4 | 包级隔离（依赖流向 / 可见性 / 模型分层 / 数据访问） |
| `03-naming-conventions.md` | 5 | 命名规范 |
| `04-routing.md` | 6 | 类型安全路由 |
| `05-navigation-communication.md` | 7 | 跨模块通信（3 种方式：回调 / AppEventBus / savedStateHandle） |
| `06-viewmodel.md` | 8 | ViewModel 与导航（BaseViewModel / AppNavigator / ApiResult / UserState） |
| `07-ui-components.md` | 9 | UI 组件使用规范 |
| `08-state-management.md` | 10 | 状态管理规范 |
| `09-feature-templates.md` | 11 | 新增 Feature 流程与页面模板 |
| `10-testing.md` | 12 | 测试规范 |
| `11-ci-cd.md` | 13 | CI/CD 流程 |
| `12-commands.md` | 14 | 常用命令 |
| `13-troubleshooting.md` | 15 + 16 | Bleeding-Edge 注意事项 + Chucker 抓包 |
| `14-development-workflow.md` | - | 增量开发工作流 + 脚手架扩展场景 |

---

## 六、项目记忆维护规则（原 CLAUDE.md 第 18 章）

1. 每次重大架构变更后，**必须**更新对应 `docs/rules/XX-xxx.md`
2. 每次升级依赖版本后，**必须**更新 `01-project-overview.md` 技术栈章节
3. 新增 Feature 模板时，**必须**更新 `09-feature-templates.md`
4. 禁止删除以下三个核心章节（分布在对应文件中）：
   - `01-project-overview.md` 的「项目快照」「技术栈」「项目结构」三节
5. 修改 `docs/rules/` 下任何文件时，必须同步检查 `CLAUDE.md` 是否需要保留引用（如仍在使用）

---

## 七、全局禁止事项汇总（原 CLAUDE.md 第 17 章）

> 以下为跨领域禁止事项，详细规则分布在对应专题文件中。本节作为速查清单。

### 路由与导航

- 禁止用 `const val` 字符串定义路由（必须用 `@Serializable data object XxxRoute : AppRoute`）
- 禁止在 feature 内出现路径字符串字面量（用 `gotoAuth()` 或 `navigate(AuthRoute)`）
- 禁止 ViewModel 直接持有 `NavController`（必须通过 `AppNavigator`）
- 禁止用旧的 `popBackStackWithResult` / `resultEvents` / `NavigationResultKey`（已删除，必须用 `AppNavigator.setResult(key, value)` + `observeResult(key): Flow<Any?>`，基于 savedStateHandle，lifecycle-safe）

### ViewModel 与状态

- 禁止 ViewModel 不继承 `BaseViewModel`（除非有特殊理由并加注释说明）
- 禁止在 UiState 中使用 `MutableState`、`LiveData`
- 禁止 ViewModel 内直接 `import retrofit2.*` / `androidx.room.*`
- 禁止 Composable 内直接调用 Repository / Retrofit / Room

### 包结构与依赖

- 禁止 feature 之间互相 import
- 禁止 core 反向依赖 feature
- 禁止在 `core.appstate` 包内放 Composable 组件（组件归 `designsystem`，`appstate` 只放 ViewModel 与模型）
- 禁止把 `@Entity` / `@Serializable` 模型丢给 Composable
- 禁止在 feature 内重复实现全屏 Loading / Error / Empty（用 `DSFullScreenLoading` 等）

### UI 与设计系统

- 禁止 Material3 原生组件代替 DS* 组件
- 禁止硬编码 dp / color / 动画时长
- 禁止使用非标准缓动曲线（必须用 `MSEasing.*`）
- 禁止在 Composable 中直接调用 Lottie 原生 API
- 禁止在 LazyColumn 长列表为所有项加进场动画（仅前 20 项触发）
- 禁止再封装一层 `XxxAppTheme`（直接用 `DSDesignTheme`）

### 文档与代码风格

- 禁止在项目根目录创建散落的 Utils 文件
- 禁止删除文件头注释块
- 禁止使用 emoji 字符
