# 质量保障

> 阶段：静态检查（阅读路径第 7 步）  
> 内容：KtLint / Detekt / Konsist / 测试 / CI / 生产就绪

---

## 阅读路径

```
README → INTEGRATION_GUIDE → BUILD → CODING → 组件 API → 容器使用 → QUALITY（本文件）
```

---

## 1. 质量门禁概览

```
代码提交
  ↓
pre-commit hook（KtLint + 单元测试）
  ↓
CI（8 Job）
  ├── build（编译）
  ├── unit-test（单元测试）
  ├── ktlint（格式）
  ├── detekt（静态分析）
  ├── lint（Android Lint）
  ├── coverage（覆盖率）
  ├── instrumented-test（UI 测试）
  └── assemble（Release 构建）
  ↓
生产就绪自检（check-production.sh）
  ↓
上线
```

---

## 2. 代码格式 KtLint

### 2.1 检查与修复

```bash
./gradlew :app:ktlintCheck      # 检查
./gradlew :app:ktlintFormat     # 自动修复
```

### 2.2 配置

`build-logic/convention/KtLintConvention.kt` 配置：
- 版本：1.8.0
- 过滤：仅检查 `src/main` 和 `src/test`
- 规则：标准 KtLint 规则集

### 2.3 pre-commit 自动运行

`scripts/pre-commit` 在 git commit 前自动运行 KtLint，失败则阻止提交。

---

## 3. 静态分析 Detekt

### 3.1 运行

```bash
./gradlew :app:detekt
```

### 3.2 配置

`build-logic/convention/DetektConvention.kt` + `config/detekt.yml`：
- 禁止 `forbidden` import（如 feature 不得 import retrofit2/room）
- 强制 `internal` 可见性
- 复杂度阈值（方法长度 / 类数 / 参数数）

### 3.3 常见违规

| 违规 | 修复 |
|---|---|
| `ForbiddenImport` | 改用 DS 组件（查 [CODING.md §8](CODING.md#8-原生--ds-映射)） |
| `TooLongFunction` | 拆分函数，单函数 ≤ 60 行 |
| `ComplexCondition` | 提取为具名 boolean |

---

## 4. 架构测试 Konsist

### 4.1 运行

```bash
./gradlew :app:test --tests "*.architecture.*"
```

### 4.2 6 条架构红线

| # | 红线 | Konsist 测试 |
|---|---|---|
| 1 | `feature/*` 之间不得互相 import | `FeatureIsolationTest` |
| 2 | `feature/*` 不得直接 import `retrofit2.*` / `androidx.room.*` | `FeatureNoDataLayerTest` |
| 3 | `core/domain` 不得 import `android.*` / `retrofit2.*` / `androidx.room.*` | `DomainPurityTest` |
| 4 | `core/designsystem/<层>` 单向依赖 | `DesignSystemLayerTest` |
| 5 | ViewModel 必须继承 BaseViewModel | `ViewModelInheritanceTest` |
| 6 | 路由必须 `@Serializable data object : AppRoute` | `RouteTypeSafetyTest` |

详见 [docs/rules/02-package-isolation.md](docs/rules/02-package-isolation.md)。

---

## 5. 测试体系

### 5.1 测试矩阵

| 类型 | 框架 | 用途 |
|---|---|---|
| 单元测试 | JUnit4 + MockK + Turbine | ViewModel / Repository / 工具类 |
| 网络测试 | MockWebServer | API 接口 |
| 数据库测试 | Room Testing | DAO |
| 协程测试 | kotlinx-coroutines-test | suspend 函数 |
| UI 测试 | Compose UI Test + Hilt Testing | Composable |
| 截图测试 | Paparazzi | 视觉回归 |
| 架构测试 | Konsist | 包隔离 |

### 5.2 测试命令

```bash
# 全部单元测试
./gradlew :app:testDebugUnitTest

# 指定测试类
./gradlew :app:testDebugUnitTest --tests "com.pai.app.feature.user.UserViewModelTest"

# UI 测试（需设备）
./gradlew :app:connectedAndroidTest

# 截图测试
./gradlew :app:recordPaparazziDebug    # 录制基线
./gradlew :app:verifyPaparazziDebug    # 对比基线
```

### 5.3 覆盖率

```bash
./gradlew :app:koverReport
```

报告输出到 `app/build/reports/kover/`，目标覆盖率 ≥ 60%。

详见 [docs/rules/10-testing.md](docs/rules/10-testing.md)。

---

## 6. CI（GitHub Actions）

### 6.1 CI Job 详情

| Job | 命令 | 失败影响 |
|---|---|---|
| `build` | `./gradlew :app:assembleDebug` | 阻止合并 |
| `unit-test` | `./gradlew :app:testDebugUnitTest` | 阻止合并 |
| `ktlint` | `./gradlew :app:ktlintCheck` | 阻止合并 |
| `detekt` | `./gradlew :app:detekt` | 阻止合并 |
| `lint` | `./gradlew :app:lintDebug` | 阻止合并 |
| `coverage` | `./gradlew :app:koverReport` | 警告（不阻止） |
| `instrumented-test` | `./gradlew :app:connectedAndroidTest` | 阻止合并 |
| `assemble` | `./gradlew :app:assembleRelease` | 阻止发布 |

### 6.2 CI 配置

详见 `.github/workflows/ci.yml` 和 [docs/rules/11-ci-cd.md](docs/rules/11-ci-cd.md)。

---

## 7. 生产就绪

### 7.1 自动检查

```bash
./scripts/check-production.sh
```

打印 ✅/❌ 报告，可程序化验证的项目自动检查。

### 7.2 生产就绪清单

#### 构建配置

- [ ] release minify + shrinkResources 启用
- [ ] ProGuard 规则覆盖 Hilt/Room/Serialization/Retrofit
- [ ] BASE_URL 按 buildType 区分
- [ ] signing config 在 CI secrets

#### 安全

- [ ] `allowBackup=false` + `dataExtractionRules` 配置
- [ ] `networkSecurityConfig` 禁用 cleartext
- [ ] token 加密存储（EncryptedPrefs）
- [ ] `HttpLoggingInterceptor redactHeader("Authorization")`
- [ ] 无硬编码 API key

#### 崩溃监控

- [ ] Crashlytics/Bugly/Sentry 三选一接入
- [ ] release 构建上传 mapping 文件

#### 发布

- [ ] 自适应图标（adaptive icon）
- [ ] Android 12+ Splash Screen 配置
- [ ] 应用图标 + 名称 + 版本号就绪
- [ ] 隐私政策 URL
- [ ] Play Console listing 资源

#### 质量

- [ ] CI 全 Job 绿
- [ ] 代码覆盖率 > 60%
- [ ] 无 Critical/High detekt 违规
- [ ] 所有 DS 组件有截图测试
- [ ] Konsist 架构测试全绿

#### 可观测

- [ ] Timber 日志在 release 关闭或脱敏
- [ ] 关键业务埋点

### 7.3 脚手架 ship 时状态

| 项 | 状态 |
|---|---|
| 12/15 项通过 | ✅ |
| 剩余 3 项 | ⬜ 业务方接入 CrashReporter / adaptive icon / 业务埋点 |

---

## 8. 常见质量问题

### Q1: KtLint 报错但看起来没问题

A: 运行 `./gradlew :app:ktlintFormat` 自动修复，多数情况是 import 顺序或尾随逗号。

### Q2: Detekt ForbiddenImport

A: 业务代码不得直接 import `retrofit2.*` / `androidx.room.*` / M3 原生组件。查 [CODING.md §8](CODING.md#8-原生--ds-映射) 找 DS 替代。

### Q3: Konsist 架构测试失败

A: 检查是否违反 6 条架构红线（见 [§4.2](#42-6-条架构红线)），通常是 feature 之间互相 import 或 feature 直接 import 数据层。

### Q4: 覆盖率不足

A: 优先补 ViewModel 和 Repository 测试，用 MockK mock 依赖，Turbine 测 Flow。

### Q5: Paparazzi 截图测试失败

A: 故意改组件视觉 → 截图 diff → 确认是预期变化 → `./gradlew :app:recordPaparazziDebug` 更新基线。

---

## 9. 质量体系文档导航

| 文档 | 内容 |
|---|---|
| [docs/rules/02-package-isolation.md](docs/rules/02-package-isolation.md) | 包隔离规则（Konsist） |
| [docs/rules/10-testing.md](docs/rules/10-testing.md) | 测试规范 |
| [docs/rules/11-ci-cd.md](docs/rules/11-ci-cd.md) | CI/CD 配置 |
| [docs/rules/13-troubleshooting.md](docs/rules/13-troubleshooting.md) | 故障排查 |

---

## 阅读路径

上一篇：[docs/components/containers.md](docs/components/containers.md) · 完整路径：[README.md](README.md)
