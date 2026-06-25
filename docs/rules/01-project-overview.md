# 项目快照与技术栈

> AI 规则文件 - 项目总览领域
> 本文件由 CLAUDE.md 拆分而来，对应原 CLAUDE.md 第 1 / 2 / 3 / 19 章。

---

## 一、项目快照

- **项目类型**：Android 原生应用（单模块 + Jetpack Compose）
- **架构**：AI-first 单模块 + 包级隔离（`internal` 修饰符强制）
- **开发模式**：单人 + AI（Claude Code / OpenCode）
- **目标**：为 AI 提供清晰的包级边界与可预测的命名模式

---

## 二、技术栈（Bleeding-Edge 2026）

> ⚠️ 以下版本为前沿预览，若 Maven Central 尚未发布需回退稳定版（见 `gradle/libs.versions.toml` 顶部注释中的稳定版清单）。

```
compileSdk       : 36  (Android 16 Baklava)
minSdk           : 24
targetSdk        : 36

AGP              : 9.2.1
Kotlin           : 2.3.21  (K2 编译器默认)
KSP              : 2.3.9   (KSP2)
Gradle           : 9.5.1

Compose BOM      : 2026.05.00
Material 3       : (由 BOM 管理)
Activity Compose : 1.13.0
Navigation       : 2.9.0
Lifecycle        : 2.10.0

Hilt             : 2.59.2
Room             : 2.8.4
Retrofit         : 2.12.0
OkHttp           : 5.4.0
kotlinx-serialization : 1.11.0
kotlinx-coroutines    : 1.11.0
DataStore        : 1.2.1
androidx.security:security-crypto : 1.1.0-alpha06  (token 加密存储)
Coil             : 2.7.0
Lottie           : 6.7.1
Chucker          : 4.3.1  (Debug 抓包)
Timber           : 5.0.1

Kover            : 0.9.1  (代码覆盖率)

JVM Target       : 17
```

> 升级依赖版本后，必须同步更新本章节及 `13-troubleshooting.md` 的兼容性说明（详见 `00-documentation-protocol.md`）。

---

## 三、项目结构

```
app/src/main/java/com/pai/app/
├── core/                 # 基础设施层
│   ├── base/             # ApiResult + AppException + BaseViewModel + BaseNetWorkViewModel + UiState
│   ├── network/          # Retrofit + HeaderInterceptor + TokenAuthenticator + AppApi + DTO
│   │   └── model/        # @Serializable DTO
│   ├── database/         # Room + Entity + DAO（Entity 标 internal）
│   │   ├── entity/       # @Entity
│   │   └── dao/
│   ├── datastore/        # DataStore + EncryptedPrefs（token 加密存储）
│   ├── data/             # Repository 实现 + model/（domain model，如 ExampleItem）
│   │   └── model/        # domain model
│   ├── designsystem/     # 纯 UI 组件库（5 一级子域 + 主题 + 多品牌色板 + 字号缩放）
│   │   ├── foundation/   # 第 0 层 - 横切系统
│   │   │   ├── tokens/   # 10 文件（DSTokens 总入口 + ColorTokens + BrandColorPalette + FontSizeScale + Spacing/Radius/Elevation/Typography/Motion/Component）
│   │   │   ├── theme/    # Color/Typography/Shape/Theme（DSDesignTheme 5 参数）
│   │   │   ├── motion/   # 动效 Token + pressScale + Lottie + 转场
│   │   │   └── a11y/     # 无障碍工具（a11yMinSize）
│   │   ├── primitives/   # 第 1 层 - 28 个原子+控件+容器+展示组件（扁平）
│   │   ├── patterns/     # 第 2 层 - 业务无关 UI 模式壳（slot 驱动）
│   │   ├── shell/        # 第 3 层 - 应用骨架（DSAppScaffold + DSTopBar/DSBottomBar/DSTabs/DSTabsWithPager/DSAppBar）
│   │   └── overlays/     # 第 3 层 - 全局浮层（Dialog/Drawer/Tooltip/FAB/...）
│   ├── appstate/         # 应用层共享（仅 MainActivityViewModel + UiModels，原 core.ui）
│   │   ├── MainActivityViewModel.kt
│   │   └── model/UiModels.kt
│   └── util/             # AppEventBus（2 事件：TokenExpired/GlobalError）+ ApplicationScope + Extensions
├── navigation/           # 独立顶层导航层
│   ├── routes/           # AppRoutes.kt（@Serializable data object）
│   ├── extension/        # NavExtensions.kt（gotoXxx 走 AppNavigator.navigate，RouteInterceptor 生效）
│   ├── AppNavigator.kt   # 全局导航器 + RouteInterceptor + UserState + setResult/observeResult
│   └── NavigationModule.kt
├── feature/              # 5 个业务特性（每个 2 个 kt 文件）
│   ├── auth/             # 登录模板
│   ├── home/             # 空白首页
│   ├── user/             # 个人中心
│   ├── settings/         # 设置页
│   └── gallery/          # 组件 Gallery + 9 个分类页（Theme/Button/Text/Form/Navigation/Container/Feedback/Motion/Layout）
├── MainActivity.kt       # 唯一 Activity + AppNavGraph（await userState.isInitialized 后决定 startDestination）
└── PaiApplication.kt     # @HiltAndroidApp
```

### 3.1 designsystem 与 appstate 包的职责划分

| 包 | 职责 | 依赖 |
|----|------|------|
| `core.designsystem` | **纯 UI 组件库**：5 一级子域（foundation/primitives/patterns/shell/overlays）+ DSDesignTheme（5 参数）+ 5 套品牌色板 + 4 档字号缩放 + 动效 + 无障碍 + 全屏状态占位 | 仅 Compose/Material3 |
| `core.appstate` | **应用层共享**：MainActivityViewModel（主题模式 + 动态颜色 + 品牌色 + 字号缩放 + 登录态）+ UiModels（全局 UI 模型） | designsystem + datastore |

**关键规则**：

- `DSDesignTheme` 是 designsystem 库的对外主题入口，**public** 可见
- 业务方直接用 `DSDesignTheme(darkTheme, dynamicColor, brandColor, fontSizeScale, amoled) { ... }`，无需额外封装
- `DSFullScreenLoading` / `DSFullScreenError` / `DSFullScreenEmpty` 在 `designsystem/patterns`，业务方直接用
- `core.appstate` **不含**任何 Composable 组件，只放 ViewModel 与数据模型

### 3.2 每个 Feature 只允许 2 个文件

```
feature/auth/
├── AuthViewModel.kt   # AuthUiState + AuthViewModel（继承 BaseViewModel，internal）
└── AuthScreen.kt      # AuthScreen（internal）
```

---

## 四、与其他 AI 工具的兼容性（原 CLAUDE.md 第 19 章）

| 文件 | 工具 | 用途 |
|------|------|------|
| `AGENTS.md`（项目根） | Claude Code + OpenCode + Codex | **AI 总索引**（自动加载），主线阅读路径 + 规则索引 + Prompt 模板 |
| `CLAUDE.md` | Claude Code | 指向 AGENTS.md（工具强制要求文件名存在） |
| `.claude/settings.json` | Claude Code | 工具配置（权限 / 环境 / 必读文件） |
| `opencode.json` | OpenCode | 工具配置（模型 / Agent / 权限） |
| `docs/rules/` | 所有 AI | 分领域细化的规则子文件（按需读取） |

**AI 总索引在 `AGENTS.md`，详细规则在 `docs/rules/`**，`CLAUDE.md` 仅指向 AGENTS.md。

> 规则细化的最新版本在 `docs/rules/` 下，`CLAUDE.md` 作为索引指向各专题文件。
