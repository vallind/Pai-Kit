# AGENTS.md

> **AI 协作总索引** · 所有 AI 工具（Claude Code / OpenCode / Codex）的统一入口  
> 工具自动加载本文件后，按需读取 `docs/rules/` 下详细规则。

---

## 主线阅读路径

```
README → INTEGRATION_GUIDE → BUILD → CODING → 组件 API → 容器使用 → QUALITY
```

| 阶段 | 文档 | 何时读 |
|---|---|---|
| 项目说明 | [README.md](README.md) | 第一次接触项目 |
| 集成 | [INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md) | 新建 App / 接入业务 |
| 构建 | [BUILD.md](BUILD.md) | 编译 / 构建 / Termux |
| 编码规范 | [CODING.md](CODING.md) | 写代码前必读 |
| 组件 API | [docs/components/README.md](docs/components/README.md) | 查组件速查表 + 决策树 |
| 容器使用 | [docs/components/containers.md](docs/components/containers.md) | AppCommonCard / AppStructuredCard |
| 静态检查 | [QUALITY.md](QUALITY.md) | 提交前 / 上线前 |

---

## 必读文件

1. **[CODING.md](CODING.md)** - 编码规范总索引（命名 / 包隔离 / 路由 / ViewModel / 动效）
2. **[docs/rules/00-documentation-protocol.md](docs/rules/00-documentation-protocol.md)** - 文档自维护协议（每次代码变更后必读）
3. **[docs/rules/09-feature-templates.md](docs/rules/09-feature-templates.md)** - 页面模板 A/B/C/D/E/F（新增 Feature 时必读）
4. **[docs/rules/14-development-workflow.md](docs/rules/14-development-workflow.md)** - 增量开发工作流（开始开发前必读）
5. `app/src/main/java/com/pai/app/navigation/routes/AppRoutes.kt` - 路由总表

---

## 规则索引

完整规则按领域拆分在 `docs/rules/`（15 个子文件），按需读取：

| 文件 | 领域 |
|---|---|
| [00-documentation-protocol.md](docs/rules/00-documentation-protocol.md) | 文档自维护协议 |
| [01-project-overview.md](docs/rules/01-project-overview.md) | 项目快照 + 技术栈 + 目录结构 |
| [02-package-isolation.md](docs/rules/02-package-isolation.md) | 包级隔离 + 可见性 + 依赖流向 |
| [03-naming-conventions.md](docs/rules/03-naming-conventions.md) | 命名规范 |
| [04-routing.md](docs/rules/04-routing.md) | 类型安全路由 |
| [05-navigation-communication.md](docs/rules/05-navigation-communication.md) | 跨模块通信（AppEventBus / Composable 回调 / savedStateHandle） |
| [06-viewmodel.md](docs/rules/06-viewmodel.md) | ViewModel + BaseViewModel + BaseNetWorkViewModel + ApiResult + UserState |
| [07-ui-components.md](docs/rules/07-ui-components.md) | DS 组件使用规则 + 原生→DS 映射 |
| [08-state-management.md](docs/rules/08-state-management.md) | 状态管理 + UserState + DSUiState |
| [09-feature-templates.md](docs/rules/09-feature-templates.md) | 页面模板 + 新增 Feature 流程 |
| [10-testing.md](docs/rules/10-testing.md) | 测试规范 |
| [11-ci-cd.md](docs/rules/11-ci-cd.md) | CI/CD |
| [12-commands.md](docs/rules/12-commands.md) | 命令速查 |
| [13-troubleshooting.md](docs/rules/13-troubleshooting.md) | 版本注意事项 + 常见错误 |
| [14-development-workflow.md](docs/rules/14-development-workflow.md) | 增量开发工作流 |

---

## AI 工作规则

1. **生成代码前**：先读 [CODING.md](CODING.md) + 对应 `docs/rules/XX-xxx.md`
2. **新增 Feature**：严格按 [docs/rules/09-feature-templates.md](docs/rules/09-feature-templates.md) 的 6 步流程 + 模板 A/B/C/D/E/F
3. **ViewModel**：必须继承 `core.base.BaseViewModel` 或 `BaseNetWorkViewModel`（后者消费 `Flow<ApiResult<T>>`）
4. **路由跳转**：通过 `AppNavigator`，禁止直接持有 `NavController`（`gotoXxx` 扩展也走 `AppNavigator.navigate()`，RouteInterceptor 对 UI 跳转生效）
5. **UI 组件**：必须用 `DS*` 组件，禁止 Material3 原生组件（查 [CODING.md §8](CODING.md#8-原生--ds-映射) 映射表）
6. **网络层**：新代码用 `ApiResult<T>`；跨模块通信用 `AppEventBus`（仅 `TokenExpired` / `GlobalError`）/ Composable 回调 / `AppNavigator.setResult` + `observeResult`
7. **Token 安全**：通过 `EncryptedPrefs` 存储，不进 `UserPreferences`；登录态单一真相源是 `UserState`
8. **完成代码后**：按 [docs/rules/00-documentation-protocol.md](docs/rules/00-documentation-protocol.md) 检查并更新文档
9. **验证编译**：跑 `./gradlew :app:compileDebugKotlin`
10. **提交前**：跑 `./gradlew :app:ktlintCheck :app:detekt :app:testDebugUnitTest`（或依赖 pre-commit hook）

---

## AI Prompt 模板

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

## AI 工具兼容性

| 文件 | 工具 | 用途 |
|---|---|---|
| `AGENTS.md`（本文件） | Claude Code + OpenCode + Codex | **AI 总索引**（自动加载） |
| `CLAUDE.md` | Claude Code | 指向 AGENTS.md（工具强制要求文件名存在） |
| `docs/rules/` | 所有 AI 工具 | 规则单一事实来源（15 个子文件） |
| `.claude/settings.json` | Claude Code | 工具配置 |
| `opencode.json` | OpenCode | 工具配置 |
