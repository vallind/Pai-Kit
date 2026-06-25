# 故障排查与 Bleeding-Edge 注意事项

> AI 规则文件 - 故障排查领域
> 本文件由 CLAUDE.md 拆分而来，对应原 CLAUDE.md 第 15 / 16 章。

---

## 一、Bleeding-Edge 版本注意事项

### 1.1 AGP 9.2.1

- 部分构建配置变化：`buildFeatures` 部分选项移到 `androidResources`
- R8 全模式默认开启；本脚手架在 `app/build.gradle.kts` 的 `release {}` 块中**显式** `isMinifyEnabled = true` + `isShrinkResources = true`（旧版为 false）
- 资源压缩默认启用
- `proguard-rules.pro` 含 Hilt / Room / Retrofit / kotlinx.serialization 定向 keep 规则（旧版 `-keep class com.pai.app.** { *; }` 通配规则已删除，避免让 R8 失效）

### 1.2 Bleeding-Edge 2026 版本未发布

若 Maven Central 尚未发布 AGP 9.x / Kotlin 2.3.x / Compose BOM 2026.05 等版本，构建会因依赖解析失败而中断。回退到稳定版：

- 打开 `gradle/libs.versions.toml` 顶部注释中的稳定版清单
- 替换 `agp` / `kotlin` / `ksp` / `composeBom` / `kotlinxCoroutines` / `kotlinxSerialization` 为稳定版
- 同步 `gradle/wrapper/gradle-wrapper.properties` 中的 Gradle 版本（稳定版 AGP 需 Gradle 8.x）

### 1.3 Kotlin 2.3.21

- K2 编译器默认启用（编译速度提升 20-30%）
- 通过 `kotlin-compose` 插件应用 Compose Compiler
- context receivers 仍为实验性

### 1.4 KSP 2.3.9（KSP2）

- KSP2 默认启用，无需 gradle property
- Hilt 必须 2.57+ 才兼容 KSP2 + AGP 9.x
- Room 必须 2.7.1+ 才兼容 KSP2
- 构建速度比 KSP1 快 2-3 倍

### 1.5 Termux 构建

若需在 Android Termux 环境构建，见 `SETUP.md` 「Termux 环境构建」小节（脚手架默认 `gradle.properties` 中 Termux 行注释化，需手动取消注释）。

---

## 二、常见编译错误表

| 错误 | 解决 |
|------|------|
| `Unresolved reference: ksp` | 检查 `plugins { alias(libs.plugins.hilt) }` |
| `Hilt processing error` | 升级 Hilt 到 2.57+ |
| `Room schema export error` | `app/build.gradle.kts` 已配置 `ksp { arg("room.schemaLocation", ...) }`，检查 `app/schemas/` 目录可写 |
| `Compose Compiler not found` | 应用 `kotlin-compose` 插件 |
| `kspDebugKotlin 失败：Cannot find Hilt modules` | 检查 `@Module` 类是否 `public`，`@Provides` 返回类型是否 `public` |
| `K2 编译器报错：Incompatible annotations` | 检查依赖是否兼容 K2，多数情况需升级到最新版 |
| `R8 全模式剥离反射类` | 在 `proguard-rules.pro` 中添加定向 `-keep` 规则（Hilt / Room / Retrofit / Serializable） |
| `Unresolved reference: NetworkResponse` / `NetResult` | 二者已删除，改为 `ApiResult<T>`（`safeApiCall { api.xxx() }` 包装） |
| `Unresolved reference: NavigationResultKey` / `popBackStackWithResult` / `resultEvents` | 已删除，用 `AppNavigator.setResult(key, value)` + `observeResult(key): Flow<Any?>`（基于 savedStateHandle） |
| `Unresolved reference: DEBUG_MODE` | BuildConfig 字段已删除，用 `BuildConfig.DEBUG` |
| `Unresolved reference: a11ySpacing` | 重命名为 `a11yMinSize()` |
| `Hilt instrumented test failed: HiltTestRunner not found` | 检查 `app/src/androidTest/.../HiltTestRunner.kt` 是否存在；`testInstrumentationRunner` 应为 `com.pai.app.HiltTestRunner` |
| `EncryptedSharedPreferences 在 Robolectric 下不稳定` | 用 `@Ignore` 标记或跑 instrumented test（`./gradlew :app:connectedDebugAndroidTest`） |

---

## 三、版本升级检查清单

升级依赖版本时，必须检查以下兼容性：

| 升级项 | 必须同步升级 | 检查文件 |
|-------|------------|---------|
| AGP | Gradle / Kotlin / KSP | `build.gradle.kts` + `gradle/libs.versions.toml` |
| Kotlin | KSP / Compose Compiler | `gradle/libs.versions.toml` |
| KSP | Hilt / Room | `gradle/libs.versions.toml` |
| Hilt | AGP / KSP 兼容性 | 本文件 1.3 节 |
| Room | KSP | 本文件 1.3 节 |
| Compose BOM | Material3 / Activity Compose / Navigation | `gradle/libs.versions.toml` |

升级后必须：

1. 同步更新 `01-project-overview.md` 技术栈版本号
2. 同步更新本文件（如版本号变化、新增注意事项）
3. 跑一遍完整测试套件（`./gradlew :app:testDebugUnitTest`）
4. 跑一遍完整构建（`./gradlew :app:assembleDebug`）

详见 `00-documentation-protocol.md` 协议。

---

## 四、Chucker Debug 抓包

Debug 模式自动启用 Chucker：

- 所有 HTTP 请求自动拦截
- 通知栏点击可查看完整请求/响应
- Release 模式通过 `library-no-op` 自动失效

### 4.1 配置

在 `app/build.gradle.kts` 中：

```kotlin
dependencies {
    debugImplementation("com.github.chuckerteam.chucker:library:4.3.1")
    releaseImplementation("com.github.chuckerteam.chucker:library-no-op:4.3.1")
}
```

### 4.2 在 NetworkModule 中注册

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(ChuckerInterceptor.Builder(context).build())
            .addInterceptor(HeaderInterceptor())
            .build()
    }
}
```

### 4.3 使用流程

1. 运行 Debug 模式 App
2. 触发任意网络请求
3. 下拉通知栏，看到 Chucker 通知
4. 点击进入查看完整请求/响应详情
5. 支持导出 HAR 文件（用于团队共享）

### 4.4 强制规则

- **禁止**在 Release 模式启用 Chucker（用 `library-no-op` 替代）
- **禁止**手动移除 ChuckerInterceptor（Debug 抓包必备）

---

## 五、其他常见问题

### 5.1 Compose 预览失败

| 现象 | 解决 |
|------|------|
| `@Preview` 不渲染 | 检查是否用 `DSDesignTheme` 包裹 |
| 预览崩溃 | 检查是否引用了非 Compose 兼容的 API（如 `Context.getSystemService`） |
| 预览版本不匹配 | 升级 Compose BOM 到 2026.05.00+ |

### 5.2 Hilt 注入失败

| 现象 | 解决 |
|------|------|
| `HiltViewModel` 找不到 | 检查 `@HiltViewModel` + `@Inject constructor` 都加了 |
| `MissingBinding` | 检查对应 Module 的 `@Provides` 方法 |
| `Hilt processing error` | 升级 Hilt 到 2.57+（兼容 AGP 9.x） |

### 5.3 Room 编译错误

| 现象 | 解决 |
|------|------|
| `Schema export error` | `app/build.gradle.kts` 已配置 `ksp { arg("room.schemaLocation", "$projectDir/schemas") }`；`app/schemas/.gitkeep` 占位；提交 schemas 到 VCS |
| `Type mismatch` | 检查 Entity 字段类型与 DAO 方法签名 |
| `Unresolved reference: Dao` | 升级 Room 到 2.7.1+（兼容 KSP2） |
| `Entity 暴露给 Repository 调用方` | Entity 标 `internal`；Repository 把 Entity 转换为 domain model 后再返回（如 `ExampleItem`） |
| `fallbackToDestructiveMigration 被移除` | 已删除（生产不安全）；业务方按版本迁移编写 Migration |

### 5.4 Navigation 类型安全路由

| 现象 | 解决 |
|------|------|
| `Route is not @Serializable` | 给 Route 加 `@Serializable data object` |
| `Cannot create NavArgument from type` | 检查参数类型是否支持（基本类型 / String / Serializable data class） |
| `Navigator not bound` | 检查 `NavHost` 中是否注册了对应 `composable<Route>` |
