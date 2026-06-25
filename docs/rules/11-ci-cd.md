# CI/CD 流程

> AI 规则文件 - CI/CD 领域
> 本文件由 CLAUDE.md 拆分而来，对应原 CLAUDE.md 第 13 章。

---

## 一、GitHub Actions 工作流

`.github/workflows/ci.yml` 定义 8 个 Job：

| Job | 作用 | 触发条件 |
|-----|------|---------|
| **build** | 编译 Debug + Release | push / PR |
| **unit-test** | 单元测试（含 Konsist 架构测试） | push / PR |
| **ktlint** | 代码风格检查 | push / PR |
| **detekt** | 静态分析 | push / PR |
| **lint** | Android Lint（`:app:lintDebug`） | push / PR |
| **coverage** | Kover 覆盖率（`:app:koverXmlReport`） | push / PR |
| **instrumented-test** | 真机/模拟器 UI 测试（`reactivecircus/android-emulator-runner`） | push / PR |
| **assemble** | 构建 APK | push / PR |

### 1.1 Job 依赖关系

```
build → unit-test
      → ktlint
      → detekt
      → lint
      → coverage         （依赖 unit-test）
      → instrumented-test
      → assemble          （needs: unit-test, ktlint, detekt, lint, coverage, instrumented-test）
```

- `build` 失败时其他 Job 不执行
- `build` 之后，6 个 Job（unit-test / ktlint / detekt / lint / coverage / instrumented-test）并行执行
- `coverage` 内部依赖 `unit-test`（先跑测试再生成覆盖率报告）
- `assemble` 严格串行在 6 个质量 Job 之后
- 任一 Job 失败 → PR 状态为 failed

### 1.2 缓存策略

- Gradle 缓存通过 `gradle/actions/setup-gradle@v4`，参数 `cache-read-only: ${{ github.ref != 'refs/heads/main' }}`（main 分支写缓存，PR 只读）
- 无显式自定义 cache-key；setup-gradle 默认按 `gradle/wrapper/gradle-wrapper.properties` + `*.gradle*` 文件 hash 失效

### 1.3 Kover 覆盖率

- 插件：`org.jetbrains.kotlinx.kover`（版本 0.9.1，在 `libs.versions.toml` + `build.gradle.kts` 通过 `alias(libs.plugins.kover)` 应用）
- 命令：`./gradlew :app:koverXmlReport` 生成 XML 报告；`./gradlew :app:koverHtmlReport` 生成 HTML 报告
- CI `coverage` Job 上传 XML 报告为 artifact

---

## 二、pre-commit hook

`scripts/pre-commit` 在 Git 提交前自动运行 KtLint + core 包单元测试。

> 脚本已加 `set -eo pipefail`，严格传播 gradlew 退出码（旧实现 `set -e` 无 `pipefail`，`| tee` 管道下 gradlew 失败被 tee 的 0 退出码掩盖，hook 静默放过坏提交）。

### 安装

```bash
ln -s ../../scripts/pre-commit .git/hooks/pre-commit
chmod +x scripts/pre-commit
```

> `scripts/create-app.sh` 派生新 App 时会自动安装 pre-commit hook（symlink 到 `.git/hooks/pre-commit`）。

### 执行内容

1. KtLint 检查暂存区 `.kt` / `.kts` 文件
2. 若 KtLint 失败 → 阻止提交
3. 若 KtLint 通过 → 运行 `./gradlew :app:testDebugUnitTest --tests "com.pai.app.core.*"`
4. 若 core 包测试失败 → 阻止提交

### 跳过 hook（紧急情况）

```bash
git commit --no-verify
```

> 业务紧急修复可临时跳过，但提交后需补跑测试与 KtLint。

---

## 三、代码质量工具

| 工具 | 配置文件 | 用途 |
|------|---------|------|
| KtLint | `.editorconfig` + `ktlint` 插件 | 代码风格 |
| Detekt | `detekt.yml` + `detekt` 插件 | 静态分析 |
| Android Lint | `app/build.gradle.kts` | Android 专属检查 |
| Kover | `libs.versions.toml` + `build.gradle.kts` | 覆盖率 |

### 3.1 KtLint

- 配置在 `.editorconfig`（项目根目录）
- 涵盖：缩进 / import 顺序 / 末尾换行 / 最大行长
- 自动修复：`./gradlew :app:ktlintFormat`
- ktlint 引擎版本由 ktlint-gradle 插件（12.1.2）默认管理（旧的 `ktlint { version.set("1.5.0") }` 已删除，避免与 `libs.versions.toml` 中 `ktlint = "1.8.0"` 冲突）

### 3.2 Detekt

- 配置在 `detekt.yml`（项目根目录）
- 涵盖：复杂度 / 代码坏味道 / 潜在 bug
- `ForbiddenImport` 规则禁止 feature 层 import M3 原生组件（含 `Scaffold$` / `Icon$` / `Text$` / `Surface$` / `SnackbarHost$` / `SnackbarHostState$` 等已锚定 `$` 防误匹配 `ButtonDefaults`）
- `MagicNumber` 规则已启用（excludes `**/test/**` / `**/androidTest/**` / `*Preview*` / `*preview*`，且 `ignorePropertyDeclaration` / `ignoreLocalVariableDeclaration` / `ignoreValueParameter` 为 true，避免 DS Token 属性与默认参数误报）
- 自定义规则：可在 `detekt.yml` 中关闭或调整阈值

### 3.3 推荐工作流

```
开发 → 写代码
  ↓
本地 → ./gradlew :app:ktlintFormat :app:detekt
  ↓
git add → git commit（自动触发 pre-commit）
  ↓
push → GitHub Actions 触发 CI（8 Job）
  ↓
PR 检查通过 → merge
```

---

## 四、Release 构建配置

`app/build.gradle.kts` 的 `release {}` 块：

- `isMinifyEnabled = true`（R8 收缩 + 混淆，旧版为 false）
- `isShrinkResources = true`（资源压缩）
- `proguard-rules.pro` 含 Hilt / Room / Retrofit / kotlinx.serialization 定向 keep 规则（旧版 `-keep class com.pai.app.** { *; }` 通配规则已删除，因为它会让 R8 失效）
- `BASE_URL` 按 buildType 区分（debug / release 各自的 `buildConfigField`）

---

## 五、CI 配置变更

> 新增 CI 步骤（如新增 release 自动化 / 新增 Play Deploy）时，在本文件追加说明（详见 `00-documentation-protocol.md`）。
