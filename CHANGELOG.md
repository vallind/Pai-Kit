# Changelog

遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 规范。
版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added
### Changed
### Deprecated
### Removed
### Fixed
### Security

## [1.1.0] - 2026-06-22

### Added
- 新增 `EncryptedPrefs`：基于 EncryptedSharedPreferences（AES256-GCM）安全存储 token
- 新增 `TokenAuthenticator`：支持 token 过期自动刷新
- 新增 `DSChip` 组件
- 新增架构测试（Konsist）：`FeatureArchitectureTest`、`RouteArchitectureTest`
- 新增单元测试：`EncryptedPrefsTest`、`AppApiTest`、`AuthRepositoryTest`、`ExampleRepositoryTest`
- 新增 `HiltComponentActivity`、`HiltTestRunner` 测试基础设施
- 新增 `CHANGELOG.md`

### Changed
- 统一网络结果类型：全部使用 `ApiResult<T>`，废弃 `NetworkResponse` / `NetResult`
- 导航结果回传：迁移至 `AppNavigator.setResult` + `observeResult`（基于 savedStateHandle）
- 同步更新 `docs/rules/` 全部 15 个规则文件

### Removed
- 删除 `NetworkResponse.kt`
- 删除 `NavigationResultKey.kt`、`popBackStackWithResult`、`resultEvents`
- 删除 `Fakes.kt`（测试辅助类）

## [1.0.0] - 2026-06-22

### Added
- Pai Scaffold Android 脚手架初始化
- 技术栈：AGP 9.2.1 / Kotlin 2.3.21 / KSP 2.3.9 / Compose BOM 2026.05.00 / Hilt 2.59.2 / Room 2.8.4 / Retrofit 2.12.0
- 完整 DS 组件库（primitives / patterns / overlays / shell 四层架构）
- 类型安全路由（`@Serializable data object` + `AppRoute` 接口）
- 包级隔离（`internal` 修饰符 + 依赖流向约束）
- ViewModel 基类（`BaseViewModel` / `BaseNetWorkViewModel`）
- Gallery 组件预览页面
