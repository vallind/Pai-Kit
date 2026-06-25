# 构建

> 阶段：构建（阅读路径第 3 步）  
> 内容：环境配置 / 构建命令 / 版本管理 / Termux 适配

---

## 阅读路径

```
README → INTEGRATION_GUIDE → BUILD（本文件）→ CODING → 组件 API → 容器使用 → QUALITY
```

---

## 1. 环境要求

| 工具 | 版本 |
|---|---|
| JDK | 17+ |
| Android SDK | compileSdk 36, minSdk 24 |
| Gradle | 9.5.1（项目自带 wrapper） |
| Kotlin | 2.3.21 |
| AGP | 9.2.1 |

> ⚠️ **Bleeding-Edge 2026**：以上版本为前沿预览。若 Maven Central 尚未发布，回退稳定版见 `gradle/libs.versions.toml` 顶部注释。

---

## 2. 构建命令

### 2.1 常用命令

```bash
# Debug 构建
./gradlew :app:assembleDebug

# Release 构建
./gradlew :app:assembleRelease

# 安装到设备
./gradlew :app:installDebug

# 清理
./gradlew clean
```

### 2.2 测试命令

```bash
# 单元测试
./gradlew :app:testDebugUnitTest

# Instrumented 测试（需连接设备/模拟器）
./gradlew :app:connectedAndroidTest

```

### 2.3 代码质量

```bash
# KtLint 格式检查 + 自动修复
./gradlew :app:ktlintCheck
./gradlew :app:ktlintFormat

# Detekt 静态分析
./gradlew :app:detekt

# Lint
./gradlew :app:lintDebug

# 代码覆盖率
./gradlew :app:koverReport

./gradlew :app:test --tests "*.architecture.*"
```

### 2.4 全量验证

```bash
# 一键跑全部检查（CI 等价）
./gradlew :app:assembleDebug :app:testDebugUnitTest :app:ktlintCheck :app:detekt :app:lintDebug :app:koverReport
```

---

## 3. 版本管理

### 3.1 版本目录

所有依赖版本集中在 `gradle/libs.versions.toml`：

```toml
[versions]
agp = "9.2.1"
kotlin = "2.3.21"
composeBom = "2026.05.00"
# ...

[libraries]
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
# ...

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
# ...
```

### 3.2 稳定版回退

若 Bleeding-Edge 版本不可用，回退稳定版：

```toml
# 注释掉前沿版本
# agp = "9.2.1"
# kotlin = "2.3.21"
# composeBom = "2026.05.00"

# 取消注释稳定版
agp = "8.7.3"
kotlin = "2.0.21"
ksp = "2.0.21-1.0.28"
composeBom = "2024.12.01"
kotlinxCoroutines = "1.9.0"
kotlinxSerialization = "1.7.3"
```

### 3.3 Convention Plugins

通用 Gradle 配置在 `build-logic/convention/`：

| Plugin | 用途 |
|---|---|
| `pai.android.application` | App 模块通用配置 |
| `pai.android.library` | Library 模块通用配置 |
| `pai.hilt` | Hilt + KSP 配置 |
| `pai.compose` | Compose BOM + tooling + preview |
| `pai.detekt` | Detekt 配置 |
| `pai.ktlint` | KtLint 配置 |
| `pai.kotlin.test` | 测试依赖 |
| `pai.kover` | 覆盖率配置 |

---

## 4. CI（GitHub Actions）

### 4.1 CI Jobs

`.github/workflows/ci.yml` 包含 8 个 Job：

| Job | 用途 | 触发 |
|---|---|---|
| `build` | 编译 Debug | push + PR |
| `unit-test` | 单元测试 | push + PR |
| `ktlint` | 代码格式 | push + PR |
| `detekt` | 静态分析 | push + PR |
| `lint` | Android Lint | push + PR |
| `coverage` | 代码覆盖率 | push + PR |
| `instrumented-test` | UI 测试 | PR |
| `assemble` | Release 构建 | tag |

### 4.2 pre-commit hook

`scripts/pre-commit` 在 git commit 前自动运行：
- KtLint 检查
- 单元测试（仅变更文件相关）

```bash
# 安装
ln -s ../../scripts/pre-commit .git/hooks/pre-commit
chmod +x scripts/pre-commit
```

---

## 5. Termux 环境（可选）

支持在 Android Termux 环境构建。

### 5.1 安装依赖

```bash
pkg install openjdk-17 git
```

### 5.2 激活 Termux 配置

`gradle.properties` 中取消注释 Termux 相关行：

```properties
# Termux 适配（取消注释启用）
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.builder.sdkDownload=false
```

### 5.3 构建

```bash
./gradlew :app:assembleDebug
```

> `scripts/create-app.sh` 派生新 App 时会自动清理激活的 Termux 行。

---

## 6. 常见构建问题

### Q1: 依赖解析失败

```
Could not resolve com.android.tools.build:gradle:9.2.1
```

A: Bleeding-Edge 版本未发布。回退稳定版见 [§3.2](#32-稳定版回退)。

### Q2: KSP 注解处理失败

```
ksp: error: could not find symbol
```

A: 确认 `ksp(libs.room.compiler)` / `ksp(libs.hilt.compiler)` 在 `dependencies` 块中。

### Q3: Compose Compiler 报错

A: Kotlin 2.3.x 自带 Compose Compiler，确认 `plugins` 块有 `kotlin-compose`。


---

## 阅读路径

上一篇：[INTEGRATION_GUIDE.md](INTEGRATION_GUIDE.md) · 下一篇：[CODING.md](CODING.md)
