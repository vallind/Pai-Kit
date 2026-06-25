# Pai Scaffold Roadmap

> 本文件是 Pai Scaffold 的演进路线图。每项都标注：**为什么做 / 做什么 / 验收标准 / 优先级**。
> 路线图原则：**不拆模块**（保持单模块 + internal 隔离），所有演进在现有结构内完成。
> 优先级分 4 档：P0（立即）/ P1（1 个月）/ P2（1 季度）/ P3（远期）。

---

## 现状基线（2026-06）

### 已具备（保留并继续维护）
- ✅ AI-first 文档体系：`AGENTS.md`（AI 总索引）+ `CLAUDE.md`（指向 AGENTS.md）+ `docs/rules/` 15 文件 + 文档自维护协议
- ✅ 类型安全路由：14 个 `@Serializable data object : AppRoute` + `gotoXxx()` 扩展 + `DefaultRouteInterceptor`
- ✅ DS 组件库：48 个组件（primitives 28 + patterns 6 + shell 6 + overlays 8）+ foundation 22（tokens 10 + theme 4 + motion 7 + a11y 1）
- ✅ BaseViewModel 体系：`BaseViewModel(navigator, userState)` + `BaseNetWorkViewModel<T>` + `ApiResult<T>` + `safeApiCall`
- ✅ 架构测试：designsystem 分层 + feature 隔离 + ViewModel 继承 + 路由类型安全（6 红线）
- ✅ Token 系统：`DSTokens.Spacing/Radius/Elevation/Duration/Easing/Border/Alpha` + 5 品牌色板 × 10 步
- ✅ 安全基线：`EncryptedPrefs`（AES256-GCM token）+ `TokenAuthenticator`（401 处理）+ `networkSecurityConfig` + `allowBackup=false`
- ✅ 登录态单一真相源：`UserState.isLoggedIn` 派生自 UserPreferences + `isInitialized` await
- ✅ CI：8 Job（build / unit-test / ktlint / detekt / lint / coverage / instrumented-test / assemble）
- ✅ 测试：Turbine + Robolectric + MockWebServer + HiltTestRunner
- ✅ Termux 可选适配
- ✅ Bleeding-Edge 2026 版本 + 稳定版回退清单

### 已识别的缺口（本 Roadmap 补齐）
- ❌ Convention Plugins（规则在文档，不在代码）
- ❌ 代码生成器（新增 feature/组件/repository 靠手抄模板）
- ❌ Production Readiness Checklist（"production-ready" 无定义）
- ❌ 架构可视化（架构图手画，易漂移）
- ❌ Repository 接口/实现分离（KMP 不 ready）
- ✅ Figma Tokens sync（`scripts/sync-tokens.sh` + `design/` 目录 + Style Dictionary 模板）
- ❌ Crash reporting 模板（生产崩溃无上报）
- ❌ Signing config（release 签名无模板）
- ❌ Play Deploy（无自动化发布）
- ❌ Feature Flags（无灰度/kill switch）
- ❌ Mutation Testing（测试质量不可测）

### M3 卓越线补齐（2026-06 完成）

详见 `docs/rules/07-ui-components.md` 的「M3 卓越线达成」小节。

#### 新增组件（13 个 .kt 文件）
- ✅ **primitives/DSRangeSlider.kt** — 范围滑块（M3 RangeSlider）
- ✅ **primitives/DSSkeleton.kt** — 骨架屏（Block/Text/Circle/ListItem/Card 5 种变体）
- ✅ **primitives/DSRatingBar.kt** — 评分条（半星支持）
- ✅ **shell/DSNavigationRail.kt** — 平板侧边导航（M3 NavigationRail）
- ✅ **overlays/DSSimpleDialog.kt** — 简单选择对话框 + 全屏对话框
- ✅ **overlays/DSContextMenu.kt** — 上下文菜单（长按 + 三点触发器）
- ✅ **overlays/DSPersistentBottomSheet.kt** — 常驻底部弹层 + BottomSheetScaffold 封装

#### 修改现有组件（3 个文件）
- ✅ **primitives/DSButton.kt** — 增加 Elevated 变体（M3 标准 5 种按钮之一）
- ✅ **primitives/DSSegmentedControl.kt** — 增加多选模式（M3 MultiChoiceSegmentedButtonRow）
- ✅ **shell/DSTopBar.kt** — 增加 Large 样式（M3 LargeTopAppBar）

#### 新增 Foundation（6 个 .kt 文件）
- ✅ **foundation/theme/ColorScheme.kt** — 新增高对比度 Light/Dark 配色（WCAG AAA）
- ✅ **foundation/theme/Theme.kt** — 新增 `highContrast` 参数
- ✅ **foundation/theme/DSComponentOverrides.kt** — 组件级主题覆盖机制
- ✅ **foundation/theme/DSThemeDebugPanel.kt** — 开发模式主题调试面板
- ✅ **foundation/motion/DSSharedTransition.kt** — 共享元素转场（SharedTransitionLayout）
- ✅ **foundation/motion/DSMotionScheme.kt** — 完整 Motion Token 体系封装
- ✅ **foundation/layout/DSWindowSizeClass.kt** — 响应式布局（Compact/Medium/Expanded）
- ✅ **foundation/a11y/DSStateLayer.kt** — M3 状态层（hover/focus/pressed）
- ✅ **foundation/perf/DSPerformanceUtils.kt** — 重组跟踪 + 防抖工具

#### 工程能力（4 项）
- ✅ DSButtonScreenshotTest 示例覆盖（Light/Dark/AMOLED/HighContrast × 5 品牌色）
- ✅ Figma Tokens sync 脚本（`scripts/sync-tokens.sh`）

#### 卓越线达成度
| 类别 | 项目数 | 达成 |
|---|---|---|
| 合格线（7 项必备） | 7 | ✅ 100% |
| 优秀线（8 项加分） | 8 | ✅ 100% |
| 卓越线（8 项顶级） | 8 | ✅ 100% |

---

## P0：立即做（1-2 周）

> 目标：补齐"脚手架该有但没有"的基础设施，让"production-ready"和"AI-first"两个承诺真正落地。

### P0-1. Convention Plugins（规则代码化）

**为什么**：当前 15 个 `docs/rules/*.md` 是文档建议，PR 误改规则无人拦。Convention plugin 把规则变成 Kotlin 代码，编译器强制。

**做什么**：
```
build-logic/
├── convention/
│   ├── src/main/kotlin/
│   │   ├── AndroidApplicationConvention.kt   # :app 用：applicationId/minSdk/Java17/Compose/buildConfig
│   │   ├── AndroidLibraryConvention.kt       # 通用 library 配置
│   │   ├── HiltConvention.kt                 # Hilt + KSP 配置
│   │   ├── ComposeConvention.kt              # Compose BOM + tooling + preview
│   │   ├── DetektConvention.kt               # detekt.yml 路径 + 报告
│   │   ├── KtLintConvention.kt              # ktlint 版本 + filter
│   │   ├── KotlinTestConvention.kt          # JUnit4 + Turbine + Robolectric + MockWebServer
│   │   └── KoverConvention.kt               # 覆盖率配置
│   └── build.gradle.kts
└── settings.gradle.kts
```
- `app/build.gradle.kts` 从 183 行降到 ~30 行（只剩 `plugins { id("android.application"); id("hilt"); ... }` + namespace + dependencies 的非约定部分）
- `docs/rules/11-ci-cd.md` + `12-commands.md` 更新说明 convention plugin 用法

**验收**：
- [ ] `app/build.gradle.kts` < 40 行
- [ ] 删除 detekt {} / ktlint {} / testOptions {} 等重复配置（移到 convention）
- [ ] 新建一个空 library module 用 `id("android.library")` 一行配齐（即使不拆模块，验证 convention 可用）
- [ ] `./gradlew :app:assembleDebug` 通过
- [ ] `docs/rules/02-package-isolation.md` 补"规则代码化"说明

---


**为什么**：48 个 DS 组件 + 70+ @Preview，但改一个组件可能破坏 N 处视觉，无保护。截图测试是 DS 能"放心改"的底线。

**做什么**：
- 每个 DS 组件 + 每个 @Preview 生成截图测试：
  ```
  app/src/test/java/com/pai/app/core/designsystem/
  ├── primitives/
  │   ├── DSButtonScreenshotTest.kt       # 全 style × 全 size × 全 state × 全 brand
  │   ├── DSTextFieldScreenshotTest.kt    # filled/outlined × enabled/disabled × error/normal
  │   ├── DSCardScreenshotTest.kt         # elevated/outlined/filled × light/dark/AMOLED
  │   └── ... (28 primitives)
  ├── patterns/  (6)
  ├── shell/     (6)
  └── overlays/  (8)
  ```
- 覆盖矩阵：每个组件 × {light, dark, AMOLED} × {5 brand colors} × {4 font scales} = 60 变体（按组件挑关键组合，不必全跑）
- PR 自动上传 diff 报告

**验收**：
- [ ] 48 个 DS 组件 100% 覆盖截图测试
- [ ] CI `screenshot-test` Job 绿
- [ ] 故意改一个组件颜色 → CI 截图 diff 报红
- [ ] `docs/rules/10-testing.md` 增"截图测试"章节

---

### P0-3. 代码生成器（AI-first 真正落地）

**为什么**：当前"模板 A/B/C/D"是文档，用户手抄。AI-first 应该是"AI 按模板生成全套代码 + 测试 + 文档"。

**做什么**：
- `scripts/new-feature.sh <name>` —— 生成完整 feature：
  ```
  feature/<name>/
  ├── <Name>ViewModel.kt        # 继承 BaseViewModel，含 UiState
  ├── <Name>Screen.kt           # 用 DSAppScaffold + DS 组件
  └── <Name>UiState.kt          # 实现 UiState 接口
  navigation/routes/AppRoutes.kt        # 追加 @Serializable data object <Name>Route
  navigation/extension/NavExtensions.kt # 追加 goto<Name>()
  MainActivity.kt                       # AppNavGraph 追加 composable<<Name>Route>
  app/src/test/.../feature/<name>/<Name>ViewModelTest.kt
  docs/rules/04-routing.md              # 追加路由表行
  ```
- `scripts/new-component.sh <DSName> <domain>` —— 生成 DS 组件：
  ```
  core/designsystem/<domain>/<DSName>.kt     # Composable + KDoc + 3 @Preview (light/dark/AMOLED)
  docs/rules/07-ui-components.md             # 追加"原生→DS 映射"行
  ```
  domain ∈ {primitives, patterns, shell, overlays}
- `scripts/new-repository.sh <name>` —— 生成数据层全套：
  ```
  core/network/model/<Name>Dto.kt           # @Serializable
  core/database/entity/<Name>Entity.kt      # @Entity, internal
  core/database/dao/<Name>Dao.kt            # @Dao, suspend + Flow
  core/database/AppDatabase.kt              # version +1, 注册 entity
  core/database/DatabaseModule.kt           # @Provides dao
  core/data/model/<Name>Item.kt             # domain model
  core/data/<Name>Repository.kt             # @Inject, safeApiCall + Entity→Item 映射
  core/network/AppApi.kt                    # 追加 suspend fun get<Xxx>()
  app/schemas/<version>.json                # Room schema 导出
  app/src/test/.../core/data/<Name>RepositoryTest.kt
  app/src/test/.../core/network/AppApiTest.kt  # MockWebServer 追加用例
  ```
- 每个脚本支持 `--ai` 标志：调用 LLM 根据自然语言描述生成业务逻辑骨架（如 `new-feature.sh product --ai "商品列表页，支持下拉刷新和分页"`）

**验收**：
- [ ] 3 个脚本可执行，dry-run 模式预览文件清单
- [ ] `./scripts/new-feature.sh feedback` 生成完整 feature，编译通过
- [ ] `./scripts/new-component.sh DSRatingBar primitives` 生成组件 + 截图测试，ktlint/detekt 通过
- [ ] `./scripts/new-repository.sh comment` 生成数据层，Room schema 版本+1 正确
- [ ] `docs/rules/09-feature-templates.md` 改为引用脚本，删除手抄模板
- [ ] `AGENTS.md` 新增"用生成器而非手写"指引

---

### P0-4. Production Readiness Checklist + Auto-Checker

**为什么**：README 宣传 "production-ready" 但无定义。应该有可验证的 checklist + 自动检查脚本。

**做什么**：
- 新建 `PRODUCTION_CHECKLIST.md`：
  ```markdown
  ## 构建配置
  - [ ] release minify + shrinkResources 启用
  - [ ] ProGuard 规则覆盖 Hilt/Room/Serialization/Retrofit
  - [ ] BASE_URL 按 buildType 区分（debug/dev, release/prod）
  - [ ] signing config 在 CI secrets，不在 repo
  
  ## 安全
  - [ ] allowBackup=false + dataExtractionRules 配置
  - [ ] networkSecurityConfig 禁用 cleartext
  - [ ] token 加密存储（EncryptedPrefs）
  - [ ] HttpLoggingInterceptor redactHeader("Authorization")
  - [ ] 无硬编码 API key / secret
  
  ## 崩溃监控
  - [ ] Crashlytics/Bugly/Sentry 三选一接入
  - [ ] release 构建上传 mapping 文件（deobfuscation）
  
  ## 发布
  - [ ] 自适应图标（adaptive icon）
  - [ ] Android 12+ Splash Screen 配置
  - [ ] 应用图标 + 名称 + 版本号就绪
  - [ ] 隐私政策 URL 配置
  - [ ] Play Console listing 资源（截图/描述/分级）
  
  ## 质量
  - [ ] CI 全 Job 绿（build/test/ktlint/detekt/lint/coverage/instrumented/screenshot）
  - [ ] 代码覆盖率 > 60%（Kover 报告）
  - [ ] 无 Critical/High detekt 违规
  - [ ] 所有 DS 组件有截图测试
  
  ## 可观测
  - [ ] Timber 日志在 release 关闭或脱敏
  - [ ] 关键业务埋点（可选）
  ```
- `scripts/check-production.sh`：自动检查前 N 项（minify/allowBackup/encryptedPrefs/cert pinning 模板/coverage 阈值等），打印 ✅/❌ 报告
- README "production-ready" 宣传改为"ship 时 checklist 100% 打勾，业务方上线前自行复核"

**验收**：
- [ ] `PRODUCTION_CHECKLIST.md` 存在，覆盖 6 大类 20+ 项
- [ ] `./scripts/check-production.sh` 可执行，输出报告
- [ ] 当前 Pai 跑 check-production 至少 80% 通过（剩余项是业务方责任）
- [ ] README + SETUP 引用 checklist

---

## P1：近期做（1 个月）

> 目标：从"静态脚手架"向"活系统"演进，KMP 预备 + 设计-研发协同 + 架构可观测。

### P1-1. Repository 接口/实现分离（KMP 预备）

**为什么**：当前 `core/data/AuthRepository` 是 `@Inject constructor` 具体类。未来 KMP 时 domain 层要移到 `commonMain`，接口必须无 Android 依赖。

**做什么**（不拆模块，只分包）：
```
core/domain/                          ← 新建包（KMP-ready，无 Android import）
  AuthRepository.kt                   # interface（suspend fun login(email): ApiResult<UserItem>）
  ExampleRepository.kt                # interface
  model/
    UserItem.kt                       # 从 core/data/model 移来
    ExampleItem.kt
core/data/                            ← 实现层
  AuthRepositoryImpl.kt               # @Inject constructor, @Binds 到接口
  ExampleRepositoryImpl.kt
  di/
    DataModule.kt                     # @Binds abstract fun bindAuthRepository(impl): AuthRepository
```
- Hilt `@Binds` 绑定接口→实现
- feature 注入 `AuthRepository`（接口），不感知 `AuthRepositoryImpl`
- 加红线：`core.domain` 不得 import `android.*` / `retrofit2.*` / `androidx.room.*`

**验收**：
- [ ] `core/domain/` 包存在，含 2 个 interface + 2 个 model
- [ ] `core/domain/` 无任何 Android/Retrofit/Room import
- [ ] feature ViewModel 注入 Repository 接口（非 Impl）
- [ ] Hilt @Binds 绑定正确，编译通过
- [ ] `docs/rules/02-package-isolation.md` 更新 domain/data 分层说明

---

### P1-2. 架构可视化（代码生成依赖图）

**为什么**：`ARCHITECTURE.md` 的依赖图手画，与代码会漂移（审查时发现路由数 13 vs 14 不一致）。图应该是生成的。

**做什么**：
- `scripts/arch-graph.sh`：扫描 `app/src/main/java/com/pai/app/**` 的 import，生成 Mermaid 依赖图
  - 节点：core/* 子包 + feature/* + navigation + appstate
  - 边：A import B 的任何符号 → A→B
  - 输出 `docs/architecture-graph.md`（Mermaid）+ PNG（用 mermaid-cli 渲染）
- CI `arch-graph` Job：跑脚本，对比 `docs/architecture-graph.md` 是否有 diff。有 diff → PR 评论"架构图过期，请运行 `./scripts/arch-graph.sh` 更新"
- 红线反向验证：图中不应有 feature→feature 边、feature→retrofit2/room 边
- README 顶部嵌入架构图（mermaid 渲染）

**验收**：
- [ ] `scripts/arch-graph.sh` 生成 Mermaid 图，节点 ≥ 15（core 子包 + 5 feature + navigation + appstate）
- [ ] `docs/architecture-graph.md` 自动生成，手改会被 CI 拦
- [ ] 故意加 `feature.auth → feature.user` import → CI 报红 + 图中高亮违规边
- [ ] README 嵌入图，PR 改架构 → 图自动更新

---

### P1-3. Figma Tokens Sync（设计-研发协同）

**为什么**：`ColorTokens.kt` 5 品牌色板 × 10 步 + Spacing/Radius 等 token 手维护，与设计脱节。设计师改 Figma → 研发手动抄。

**做什么**：
- 接入 [Figma Tokens Studio](https://figmatokens.com/) + [Style Dictionary](https://amzn.github.io/style-dictionary/)
- 流程：
  1. 设计师在 Figma 用 Tokens Studio 插件定义 token（color.brand.indigo.600 = #4F46E5 等）
  2. 插件导出 `tokens.json` 提交到 `design/tokens.json`
  3. CI 跑 `scripts/sync-tokens.sh` → Style Dictionary 用模板生成 `ColorTokens.kt` + `SpacingTokens.kt` + ...
  4. PR 自动包含生成的 token 文件 diff
- 模板：`design/templates/ColorTokens.kt.template`（Style Dictionary handlebars）
- `DSTokens` 保持门面不变，底层 val 自动生成
- designsystem 截图测试验证 token 变更的视觉影响

**验收**：
- [ ] `design/tokens.json` 存在，含 5 品牌色板 × 10 步 + Spacing/Radius/Elevation
- [ ] `scripts/sync-tokens.sh` 生成 `ColorTokens.kt` 等文件（与当前手写一致）
- [ ] CI `sync-tokens` Job：`tokens.json` 变更 → 自动生成 .kt → PR
- [ ] `docs/rules/07-ui-components.md` 新增"token 来源 = Figma"说明

---

### P1-4. Crash Reporting 模板（三选一）

**为什么**：生产崩溃无上报，"production-ready" 是空话。

**做什么**：
- `core/util/crash/CrashReporter.kt`：接口（`fun initialize()` / `fun report(throwable, extras)` / `fun setUserId(id)` / `fun leaveBreadcrumb(msg)`）
- 3 个实现模板（默认 NoOpCrashReporter，业务方按需启用其一）：
  - `FirebaseCrashlyticsImpl`（`core/util/crash/crashlytics/`）+ build.gradle 可选依赖
  - `BuglyImpl`（`core/util/crash/bugly/`）
  - `SentryImpl`（`core/util/crash/sentry/`）
- Hilt `@Binds` 默认 NoOp，业务方在 release build flavor 切换实现
- `PaiApplication.onCreate` 调 `crashReporter.initialize()`
- Timber Tree 适配：`CrashReportingTree` 把 E 级日志转发给 CrashReporter
- `PRODUCTION_CHECKLIST.md` 勾"crash reporting 接入"

**验收**：
- [ ] `CrashReporter` 接口 + NoOp 实现默认启用，编译通过
- [ ] 3 个实现模板可选（用 build flavor 或 product flavor 切换）
- [ ] `PaiApplication` 调用 `initialize()`
- [ ] Timber W/E 日志转发到 CrashReporter
- [ ] `docs/rules/13-troubleshooting.md` 新增"接入 Crashlytics/Bugly/Sentry"指引

---

### P1-5. Signing Config 模板

**为什么**：release 签名无模板，业务方各显神通，常把 keystore 提交到 repo（安全雷）。

**做什么**：
- `app/build.gradle.kts` signingConfigs：
  ```kotlin
  val keystoreProperties = Properties().apply {
      val file = rootProject.file("keystore.properties")
      if (file.exists()) load(file.inputStream())
  }
  signingConfigs {
      create("release") {
          keyAlias = keystoreProperties["keyAlias"] as String?
          keyPassword = keystoreProperties["keyPassword"] as String?
          storeFile = keystoreProperties["storeFile"]?.let { file(it as String) }
          storePassword = keystoreProperties["storePassword"] as String?
          enableV1Signing = true
          enableV2Signing = true
          enableV3Signing = true
      }
  }
  buildTypes {
      release {
          signingConfig = signingConfigs.getByName("release")
          // ...
      }
  }
  ```
- `.gitignore` 加 `keystore.properties` + `*.keystore` + `*.jks`
- `keystore.properties.example` 模板提交
- CI secrets 配置（GitHub Actions 注入 keystore base64 → 解码 → 签名）
- `docs/rules/11-ci-cd.md` 新增"release signing"章节

**验收**：
- [ ] `keystore.properties.example` 存在
- [ ] `.gitignore` 排除 keystore 文件
- [ ] `app/build.gradle.kts` signingConfigs 读取 keystore.properties
- [ ] CI release 构建从 secrets 注入 keystore，签名 APK 产出
- [ ] `PRODUCTION_CHECKLIST.md` 签名项可勾

---

## P2：中期做（1 季度）

> 目标：从"人守规则"升级为"AI 端到端交付 + 测试质量可测 + 灰度发布"。

### P2-1. 内置 AI Agent（自然语言 → PR）

**为什么**：当前 AI 是"读规则的客人"（外部 Claude Code 读 CLAUDE.md）。下一代应该是"写代码的主人"（脚手架自带 agent）。

**做什么**：
- `tools/ai-agent/`：基于项目 convention 的 AI agent
  - 输入：自然语言需求（"加一个商品列表页，支持下拉刷新和分页"）
  - 输出：完整 PR（route + ViewModel + Screen + Repository + DTO + Entity + DAO + 测试 + 文档 断言）
- 集成 z-ai-web-dev-sdk（或等价 LLM SDK）做代码生成
- agent 自检清单（生成后自跑）：
  - [ ] `./gradlew :app:compileDebugKotlin` 通过
  - [ ] `./gradlew :app:ktlintCheck :app:detekt` 通过
  - [ ] 新代码用 DS* 而非 M3 原生
  - [ ] 新路由在 AppNavGraph 注册
- 失败重试：自检不过 → agent 读错误信息 → 修复 → 再自检（最多 3 轮）
- `scripts/ai-feature.sh "<需求>"`：一行命令提 PR

**验收**：
- [ ] `tools/ai-agent/` 存在，可执行
- [ ] "加商品列表页"需求 → agent 生成 7+ 文件 → 编译通过 → 自检全绿 → 提 PR
- [ ] agent 生成的代码 100% 用 DS 组件（无 M3 原生）
- [ ] agent 生成的 ViewModel 100% 继承 BaseViewModel
- [ ] `AGENTS.md` 新增"用 agent 而非手写"指引

---

### P2-2. Mutation Testing（测试质量可测）

**为什么**：当前覆盖率只测"代码有没有被测过"，不测"测试能不能抓 bug"。Mutation testing 验证测试质量。

**做什么**：
- 接入 [PITest](https://pitest.org/) + [kotlin-mutation-testing](https://github.com/square/mutation-testing) 或 ARcmutase
- 配置：仅对 `core/base/` + `core/data/` + `core/network/` 跑（DS 组件和 feature UI 不适合 mutation）
- CI `mutation-test` Job（可选/手动触发，因耗时）：
  - 跑 `./gradlew :app:pitest`
  - 上传 HTML 报告
  - mutation score < 60% → 标黄（建议改进，不阻断）
- `docs/rules/10-testing.md` 新增"mutation testing"章节

**验收**：
- [ ] PITest 配置完成，可对 core/* 跑 mutation
- [ ] `core/base/ApiExtensions.kt`（safeApiCall）mutation score > 80%
- [ ] CI 可手动触发 mutation-test Job
- [ ] 报告上传 artifact

---

### P2-3. Feature Flags + Kill Switch

**为什么**：生产环境需要灰度发布 / 紧急关闭功能 / A/B 测试。脚手架应该内建机制。

**做什么**：
- `core/util/featureflag/FeatureFlag.kt`：接口（`suspend fun isEnabled(key: String): Boolean` / `fun observe(key): Flow<Boolean>`）
- 3 个实现：
  - `LocalFeatureFlag`（DataStore，开发期用）
  - `RemoteFeatureFlag`（Firebase Remote Config / GrowthBook / 自建后端，生产用）
  - `NoOpFeatureFlag`（全 true，默认）
- `@Composable fun rememberFeatureFlag(key): Boolean`：UI 层便捷消费
- `FeatureFlagKey` 常量集中管理（`core/util/featureflag/FeatureFlagKeys.kt`）
- Hilt `@Binds` 默认 NoOp，业务方按需切换
- 示例：feature/gallery 某页用 `if (rememberFeatureFlag("gallery_motion_enabled")) { ... }`

**验收**：
- [ ] `FeatureFlag` 接口 + 3 实现 + Composable helper 存在
- [ ] gallery 用 FeatureFlag 控制某页显隐
- [ ] `docs/rules/08-state-management.md` 新增"feature flag"章节
- [ ] `PRODUCTION_CHECKLIST.md` 新增"feature flag 接入"项

---

### P2-4. Play Deploy 自动化（CI/CD L4）

**为什么**：当前 CI 到 assembleDebug 为止。L4 应该能自动发布到 Play Internal 测试轨道。

**做什么**：
- 接入 [gradle-play-publisher](https://github.com/Triple-T/gradle-play-publisher)
- `app/build.gradle.kts` 配置 play publisher：
  ```kotlin
  plugins { id("com.github.triplet.play") }
  play {
      serviceAccountCredentials.set(file("play-service-account.json"))
      track.set("internal")
      releaseStatus.set(com.github.triplet.gradle.play.tasks.PublishRelease.Status.COMPLETED)
  }
  ```
- CI `deploy-internal` Job（仅 main 分支 push 触发）：
  1. assembleRelease + bundleRelease（签名）
  2. `./gradlew :app:publishBundle`（上传 Play Internal）
  3. 通知 Slack/飞书
- `play-service-account.json` 在 CI secrets，不入 repo
- Staged rollout：先 10% → 观察 24h → 50% → 100%（用 Play Publisher API 或手动）
- `docs/rules/11-ci-cd.md` 新增"L4 Play Deploy"章节

**验收**：
- [ ] main 分支 push → CI 自动发布到 Play Internal
- [ ] release notes 自动生成（从 git log）
- [ ] `.gitignore` 排除 `play-service-account.json`
- [ ] `docs/rules/11-ci-cd.md` 标注当前 CI 等级（L4）

---

## P3：远期做（3+ 个月）

> 目标：向 KMP 迁移 + 生态化，保持脚手架长期生命力。

### P3-1. KMP 迁移路径（domain → shared module）

**为什么**：iOS 复用 domain 层（Repository 接口 + model + UseCase）。

**做什么**（P1-1 的延续）：
- `core/domain/` 包结构已 KMP-ready（P1-1 做了）
- 创建 `shared/` Gradle module（KMP），把 `core/domain/` 内容移入 `commonMain`
- Android `:app` 依赖 `shared`，iOS 依赖 `shared` framework
- Repository 实现层仍在 Android `:core:data`（iOS 各自实现）
- UseCase 层（`core/domain/usecase/`）放 `commonMain`，纯 Kotlin，两端复用

**验收**：
- [ ] `shared/` module 存在，含 `commonMain` + `androidMain` + `iosMain`
- [ ] Repository 接口 + model + UseCase 在 `commonMain`
- [ ] Android `:app` 依赖 `shared`，编译通过
- [ ] iOS demo 调用 `shared` framework 成功
- [ ] `ARCHITECTURE.md` 更新含 KMP 层

---

### P3-2. Scaffold Upgrade 机制（活系统）

**为什么**：当前 clone 一次永远分叉，无法拉取脚手架更新。这次做的增量补丁是创可贴。

**做什么**：
- 文件 frontmatter 标注所有权：
  ```kotlin
  // @scaffold-owned
  package com.pai.app.core.designsystem.primitives
  // DSButton.kt - 脚手架拥有，upgrade 会覆盖
  ```
  ```kotlin
  // @user-owned
  package com.pai.app.feature.product
  // ProductViewModel.kt - 用户拥有，upgrade 永不触碰
  ```
  ```kotlin
  // @template
  package com.pai.app
  // MainActivity.kt - 脚手架提供模板，用户首次 copy 后拥有
  ```
- `scripts/scaffold-upgrade.sh`：
  1. fetch 脚手架上游（git remote add scaffold <url>; git fetch scaffold）
  2. 三方 merge：
     - `@scaffold-owned`：直接覆盖（用户改动报冲突）
     - `@user-owned`：永不触碰
     - `@template`：rename-aware 三方合并
  3. 跑 migration scripts（如有）
- Migration scripts：`migrations/001_rename_usertoken_to_encryptedprefs.sh` 等
- `docs/rules/14-development-workflow.md` 新增"scaffold upgrade"章节

**验收**：
- [ ] 关键文件 frontmatter 标注所有权
- [ ] `scaffold-upgrade.sh` 可拉取上游更新，@user-owned 不动
- [ ] 至少 1 个 migration script 示例
- [ ] 文档说明 upgrade 流程

---

### P3-3. CI/CD 成熟度阶梯文档化

**为什么**：当前 CI 是 L2 但未说明。应该有 L0-L5 阶梯，用户知道自己在哪级。

**做什么**：
- `docs/rules/11-ci-cd.md` 新增成熟度阶梯表：

  | 级别 | 内容 | 触发条件 |
  |------|------|---------|
  | L0 | compile + unit test | 个人玩具 |
  | L1 | + ktlint + detekt + lint | 小团队 |
  | L2 | + screenshot + coverage | **当前 Pai** |
  | L3 | + mutation testing + dep scan + SAST | 商业产品 |
  | L4 | + Play Internal auto-deploy + staged rollout | 正式发布 |
  | L5 | + feature flags + kill switches + progressive deployment | 大规模 |

- 每级说明"如何升级到下一级"
- L3 升级指南：dependency scan（ Dependabot / Renovate）+ SAST（ Semgrep / detekt 的 security rules）
- L5 升级指南：feature flags 接入（P2-3）+ progressive deployment（Play Publisher staged rollout API）

**验收**：
- [ ] `docs/rules/11-ci-cd.md` 含 L0-L5 表
- [ ] 当前级别标 L2（含 P0/P1/P2 完成后升级到 L3/L4）
- [ ] 每级有升级路径说明

---

### P3-4. 遥测（匿名统计反哺演进）

**为什么**：脚手架不知道哪些组件/规则被用、哪些被忽略。遥测反哺演进方向。

**做什么**：
- `core/util/telemetry/Telemetry.kt`：匿名统计接口
  - 组件使用率（哪些 DS 组件被 import）
  - 编译时长分布
- 默认关闭，业务方 opt-in
- 数据上报到自建 endpoint（或 PostHog / Mixpanel）
- 脚手架维护者看 dashboard 决定下个版本优化方向

**验收**：
- [ ] `Telemetry` 接口 + NoOp 实现
- [ ] opt-in 机制（DataStore 配置）
- [ ] 至少统计 3 类数据（组件使用 / 规则违反 / 编译时长）
- [ ] `docs/rules/13-troubleshooting.md` 说明遥测内容 + opt-in 方式

---

## 优先级总览

| 优先级 | 项目 | 工作量 | 依赖 |
|--------|------|--------|------|
| **P0-1** | Convention Plugins | 2-3 天 | 无 |
| **P0-3** | 代码生成器 | 3-5 天 | P0-1（convention 引用） |
| **P0-4** | Production Checklist | 1 天 | 无 |
| **P1-1** | Repository 接口分离 | 1-2 天 | 无 |
| **P1-2** | 架构可视化 | 2-3 天 | 无 |
| **P1-3** | Figma Tokens Sync | 3-5 天 | P0-2（截图验证 token 变更） |
| **P1-4** | Crash Reporting 模板 | 2-3 天 | 无 |
| **P1-5** | Signing Config | 1 天 | 无 |
| **P2-1** | 内置 AI Agent | 2-3 周 | P0-3（生成器基础） |
| **P2-2** | Mutation Testing | 3-5 天 | 无 |
| **P2-3** | Feature Flags | 2-3 天 | 无 |
| **P2-4** | Play Deploy 自动化 | 3-5 天 | P1-5（signing） |
| **P3-1** | KMP 迁移 | 2-4 周 | P1-1（domain 分离） |
| **P3-2** | Scaffold Upgrade 机制 | 2-3 周 | 无 |
| **P3-3** | CI 成熟度阶梯文档 | 1 天 | P2-4 |
| **P3-4** | 遥测 | 1 周 | 无 |

---

## 不做清单（明确排除）

> 以下事项**故意不在 Roadmap 内**，避免范围蔓延。

- ❌ **拆分 Gradle 模块**：保持单模块 + internal 隔离 守护。多模块对 7k 行项目是负优化（编译变慢、AI 准确率下降、配置膨胀）。触发拆分信号见 `docs/decisions/001-keep-single-module.md`（待补）。
- ❌ **迁移到 Compose Multiplatform**：当前定位 Android 原生。KMP 只迁移 domain 层（P3-1），不做 UI 跨平台。
- ❌ **自建状态管理框架**：用 StateFlow + collectAsStateWithLifecycle，不引入 MVI/MvRx 等框架。
- ❌ **自建网络层**：用 Retrofit + OkHttp，不引入 Ktor。
- ❌ **自建 DI 框架**：用 Hilt，不引入 Anvil/Kotlin-Inject（除非未来脱离 Hilt）。
- ❌ **支持非 Compose UI**：仅 Compose，不支持 View/XML。
- ❌ **支持非 Kotlin 语言**：仅 Kotlin。

---

## 决策原则（Roadmap 优先级排序依据）

1. **解决真实问题 > 看起来工程化**：每项都要回答"解决了什么痛点"。不为"看起来更专业"加东西。
2. **AI-first 优先**：让 AI 端到端交付的能力（P0-3 生成器、P2-1 agent）优先级高于纯人类工具。
3. **守底线 > 锦上添花**：截图测试（P0-2）守 DS 视觉底线、Production Checklist（P0-4）守发布底线，优先于 Figma sync（P1-3）等协同工具。
4. **零成本预备 > 临时改造**：Repository 接口分离（P1-1）现在做是零成本，未来 KMP 时省大改。
5. **可验证 > 可宣传**：每项有明确的验收标准（checkbox），"production-ready" 必须可 check。
6. **不破坏单模块**：所有演进在现有结构内完成，不引入模块拆分开销。

---

## 进度追踪

每个 P 项完成后，在对应 checkbox 打勾 + 在 `docs/changelog/` 追加变更记录（按 `docs/rules/00-documentation-protocol.md` 协议）。

| 项目 | 状态 | 完成日期 | PR |
|------|------|---------|-----|
| P0-1 Convention Plugins | ⬜ 待开始 | - | - |
| P0-2 截图测试 | ⬜ 待开始 | - | - |
| P0-3 代码生成器 | ⬜ 待开始 | - | - |
| P0-4 Production Checklist | ⬜ 待开始 | - | - |
| P1-1 Repository 接口分离 | ⬜ 待开始 | - | - |
| P1-2 架构可视化 | ⬜ 待开始 | - | - |
| P1-3 Figma Tokens Sync | ⬜ 待开始 | - | - |
| P1-4 Crash Reporting | ⬜ 待开始 | - | - |
| P1-5 Signing Config | ⬜ 待开始 | - | - |
| P2-1 内置 AI Agent | ⬜ 待开始 | - | - |
| P2-2 Mutation Testing | ⬜ 待开始 | - | - |
| P2-3 Feature Flags | ⬜ 待开始 | - | - |
| P2-4 Play Deploy | ⬜ 待开始 | - | - |
| P3-1 KMP 迁移 | ⬜ 待开始 | - | - |
| P3-2 Scaffold Upgrade | ⬜ 待开始 | - | - |
| P3-3 CI 成熟度阶梯 | ⬜ 待开始 | - | - |
| P3-4 遥测 | ⬜ 待开始 | - | - |

---

> 本 Roadmap 是活文档。每完成一个 P 项或方向调整时更新，按 `docs/rules/00-documentation-protocol.md` 协议同步相关文档。
