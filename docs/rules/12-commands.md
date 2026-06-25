# 常用命令速查

> AI 规则文件 - 命令速查领域
> 本文件由 CLAUDE.md 拆分而来，对应原 CLAUDE.md 第 14 章。

---

## 一、编译检查

```bash
# 编译 Debug Kotlin
./gradlew :app:compileDebugKotlin

# KSP 代码生成
./gradlew :app:kspDebugKotlin

# 完整构建 Debug APK
./gradlew :app:assembleDebug

# 完整构建 Release APK
./gradlew :app:assembleRelease
```

---

## 二、测试

```bash
# 单元测试（全部）
./gradlew :app:testDebugUnitTest

# 仅 core 包测试
./gradlew :app:testDebugUnitTest --tests "com.pai.app.core.*"

# 仅 feature 包测试
./gradlew :app:testDebugUnitTest --tests "com.pai.app.feature.*"

# 单个测试类
./gradlew :app:testDebugUnitTest --tests "com.pai.app.feature.auth.AuthViewModelTest"

# 单个测试方法
./gradlew :app:testDebugUnitTest --tests "com.pai.app.feature.auth.AuthViewModelTest.登录成功后 state 更新"

# Konsist 架构测试（designsystem 5 层 + feature 隔离 + 跨层短路 + ViewModel 继承 + 路由类型安全）
./gradlew :app:testDebugUnitTest --tests "com.pai.app.architecture.*"

# UI 测试（需连接设备或模拟器，需 HiltTestRunner）
./gradlew :app:connectedDebugAndroidTest
```

---

## 三、代码质量

```bash
# KtLint 检查
./gradlew :app:ktlintCheck

# KtLint 自动修复格式
./gradlew :app:ktlintFormat

# Detekt 静态分析
./gradlew :app:detekt

# Android Lint（CI lint Job 跑此）
./gradlew :app:lintDebug

# 全部质量检查
./gradlew :app:ktlintCheck :app:detekt :app:lintDebug
```

---

## 四、覆盖率（Kover）

```bash
# 生成 XML 覆盖率报告（CI coverage Job 跑此）
./gradlew :app:koverXmlReport

# 生成 HTML 覆盖率报告（本地查看）
./gradlew :app:koverHtmlReport
```

---

## 五、清理

```bash
# 清理构建产物
./gradlew clean

# 清理并重新构建
./gradlew clean :app:assembleDebug
```

---

## 六、依赖与版本

```bash
# 查看依赖树
./gradlew :app:dependencies

# 查看特定配置的依赖
./gradlew :app:dependencies --configuration debugRuntimeClasspath

# 检查依赖更新（需 ben-manes/gradle-versions-plugin）
./gradlew :app:dependencyUpdates
```

---

## 七、签名与发布

```bash
# 生成 Debug 签名 APK（默认 debug.keystore）
./gradlew :app:assembleDebug

# 生成 Release 签名 APK（需配置 keystore）
./gradlew :app:assembleRelease

# 生成 App Bundle（AAB，用于 Play Store 上传）
./gradlew :app:bundleRelease
```

---

## 八、Hilt 与 KSP

```bash
# 仅运行 KSP 代码生成（验证 Hilt / Room 代码生成）
./gradlew :app:kspDebugKotlin

# 查看 KSP 生成的源码
ls app/build/generated/ksp/debug/java/com/pai/app/
```

---

## 九、常用组合命令

```bash
# 一键本地检查（提交前）
./gradlew :app:ktlintFormat :app:detekt :app:lintDebug :app:testDebugUnitTest

# 一键完整构建（含测试）
./gradlew clean :app:assembleDebug :app:testDebugUnitTest :app:ktlintCheck :app:detekt :app:lintDebug

# 仅编译检查（快速反馈）
./gradlew :app:compileDebugKotlin
```

---

## 十、Chucker Debug 抓包

详见 `13-troubleshooting.md` 的 Chucker 抓包说明。

```bash
# Debug 模式自动启用 Chucker
# 在通知栏点击查看完整请求/响应
# Release 模式通过 library-no-op 自动失效
./gradlew :app:assembleDebug
```
