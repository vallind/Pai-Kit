# Pai Design System 文档导航

> 唯一入口 · 最后更新：2026-06-25

---

## 🚀 主线阅读路径

```
项目说明 → 集成 → 构建 → 编码规范 → 组件 API → 容器使用 → 静态检查
README    INTEG   BUILD   CODING     API         CONTAINER   QUALITY
```

| 顺序 | 文档 | 阶段 | 内容 |
|---|---|---|---|
| 1 | [README.md](../README.md) | 项目说明 | 是什么 / 技术栈 / 架构概览 |
| 2 | [INTEGRATION_GUIDE.md](../INTEGRATION_GUIDE.md) | 集成 | 业务怎么接入（clone → 第一个页面） |
| 3 | [BUILD.md](../BUILD.md) | 构建 | 环境配置 / 构建命令 / Termux |
| 4 | [CODING.md](../CODING.md) | 编码规范 | 命名 / 包隔离 / 路由 / ViewModel |
| 5 | [components/README.md](components/README.md) | 组件 API | 速查表 + 决策树 |
| 6 | [components/containers.md](components/containers.md) | 容器使用 | AppCommonCard / AppStructuredCard |
| 7 | [QUALITY.md](../QUALITY.md) | 静态检查 | KtLint / Detekt / 测试 / CI / 生产就绪 |

---

## 📦 组件 API 速查

### Foundation 基础层
→ [00-foundation.md](components/00-foundation.md)

| 主题 | 章节 |
|---|---|
| Token（颜色/间距/圆角/字号/动效） | [§2](components/00-foundation.md#2-tokens-设计令牌) |
| 主题（Light/Dark/AMOLED/HighContrast） | [§3](components/00-foundation.md#3-theme-主题系统) |
| 动效（DSMotionScheme/共享元素转场） | [§4](components/00-foundation.md#4-motion-动效系统) |
| 无障碍（minTouchTarget/状态层） | [§5](components/00-foundation.md#5-a11y-无障碍) |
| 响应式布局（WindowSizeClass） | [§6](components/00-foundation.md#6-layout-响应式布局) |
| 性能（重组跟踪/稳定性包装） | [§7](components/00-foundation.md#7-perf-性能工具) |
| Preview 规范（DSPreviewScenes） | [§8](components/00-foundation.md#8-preview-预览规范) |
| 通用扩展（Color/Dp 扩展） | [§9](components/00-foundation.md#9-util-通用扩展) |
| 状态机（DSUiState/DSPageStateLayout） | [§10](components/00-foundation.md#10-uistate-状态机范式) |

### Atom 原子组件（18 个）
→ [atoms.md](components/atoms.md)

### Molecule 分子组件（28 个）
→ [molecules.md](components/molecules.md)

### Layout 布局组件（16 个）+ 6 个页面模板
→ [layouts.md](components/layouts.md)

### Container 业务容器壳（2 个）
→ [containers.md](components/containers.md)

---

## 📐 编码规则（按领域）

→ [rules/](rules/)

| 规则 | 文档 |
|---|---|
| 文档自维护协议 | [00-documentation-protocol.md](rules/00-documentation-protocol.md) |
| 项目概述 | [01-project-overview.md](rules/01-project-overview.md) |
| 包隔离 | [02-package-isolation.md](rules/02-package-isolation.md) |
| 命名规范 | [03-naming-conventions.md](rules/03-naming-conventions.md) |
| 路由 | [04-routing.md](rules/04-routing.md) |
| 导航与通信 | [05-navigation-communication.md](rules/05-navigation-communication.md) |
| ViewModel | [06-viewmodel.md](rules/06-viewmodel.md) |
| UI 组件使用规则 | [07-ui-components.md](rules/07-ui-components.md) |
| 状态管理 | [08-state-management.md](rules/08-state-management.md) |
| Feature 模板 | [09-feature-templates.md](rules/09-feature-templates.md) |
| 测试 | [10-testing.md](rules/10-testing.md) |
| CI/CD | [11-ci-cd.md](rules/11-ci-cd.md) |
| 命令 | [12-commands.md](rules/12-commands.md) |
| 故障排查 | [13-troubleshooting.md](rules/13-troubleshooting.md) |
| 开发工作流 | [14-development-workflow.md](rules/14-development-workflow.md) |

---

## 📐 文档职责划分

```
根目录/
├── README.md              ← 项目说明（阶段 1）
├── INTEGRATION_GUIDE.md   ← 集成（阶段 2）
├── BUILD.md               ← 构建（阶段 3）
├── CODING.md              ← 编码规范（阶段 4）
├── QUALITY.md             ← 静态检查（阶段 7）
├── CLAUDE.md              ← Claude Code 主索引（工具自动加载）
├── AGENTS.md              ← OpenCode 主索引（工具自动加载）
├── CHANGELOG.md           ← 变更日志
└── ROADMAP.md             ← 演进路线图

docs/
├── INDEX.md               ← 你在这里（文档总导航）
├── components/            ← 组件 API 单一真相源（阶段 5-6）
│   ├── README.md          ← 速查表 + 决策树（阶段 5）
│   ├── 00-foundation.md   ← Foundation 层
│   ├── atoms.md           ← Atom 原子组件
│   ├── molecules.md       ← Molecule 分子组件
│   ├── layouts.md         ← Layout 布局组件 + 页面模板
│   └── containers.md      ← Container 业务容器壳（阶段 6）
├── rules/                 ← 编码规则详情（CODING.md 的展开）
└── architecture-graph.md  ← 架构依赖图（自动生成）
```

**职责切分**：
- 根目录主线文档：按阅读顺序串联（README → INTEG → BUILD → CODING → QUALITY）
- `docs/components/`：组件 API 单一真相源
- `docs/rules/`：编码规则详情（CODING.md 的展开）
