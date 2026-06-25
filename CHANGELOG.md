# Changelog

遵循 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.1.0/) 规范。
版本号遵循 [Semantic Versioning](https://semver.org/lang/zh-CN/)。

## [Unreleased]

## [1.3.0] - 2026-06-25

### Removed
- 移除 Paparazzi：`PaparazziConvention.kt`、截图测试、CI Job、libs.versions.toml 条目
- 移除 MockK：全部 5 个测试文件、版本依赖、convention plugin 引用
- 移除 Konsist：全部 3 个架构测试文件、版本依赖、convention plugin 引用
- 移除 `DSButtonScreenshotTest.kt`（依赖 Paparazzi）
- 清理项目中所有 Paparazzi/MockK/Konsist 文案引用（13 个 .md 文件 + 5 个 .sh 脚本）

### Fixed
- 修复 4 个 Lint Error：`LocalContextConfigurationRead`、`ModifierFactoryUnreferencedReceiver`、`StateFlowValueCalledInComposition`

## [1.2.0] - 2026-06-25

### Fixed
- 编译修复：Kotlin 2.3.21 trailing lambda 行为变更 — `DSContextMenuItem` / `DSTopBarAction` 参数重排序
- 编译修复：Compose M3 1.9+ API 迁移（`SegmentedButton`/`RangeSliderDefaults`/`ElevatedButtonDefaults`/`BottomSheetScaffold`/`AlertDialogDefaults` 等）
- 编译修复：`isDebugInspectorInfoEnabled` → `LocalInspectionMode`
- 编译修复：`DSDuration` → `DSTokens.Duration`、`DSIcon(icon=)` → `imageVector=`、`DSIconSize.MD` → `Medium`、`DSAvatarSize.Medium` → `Medium40`
- 编译修复：`DSDialogType` → `DSMessageType`、`DSBadgeType.Error` → `Text`、`DSTextColor.OnSurfaceVariant` → `Secondary`
- 编译修复：16 处缺少的 import（`BrandRose100/300/800`、`BrandEmerald100/300/800`、`DSIconTint`、Material Icons 等）
- 编译修复：`SectionCard` 多文件重名冲突 — `GalleryLayoutPage` 重命名为 `LayoutPageSectionCard`
- UI 修复：`DSAppScaffold` 双层 padding 导致页面顶部大块空白
- UI 修复：`DSPagination` 首末页重复（页码 1 和 20 出现两次）
- UI 修复：`DSPagination` 自适应居中布局，按屏幕宽度动态调整兄弟页数
- UI 修复：`loading.json` 旋转动画不可见 — 改用 Trim Paths 缺口圆环加载器
- API 修复：`DSSharedTransition` 适配新的 `SharedTransitionScope` lambda 签名
- API 修复：`DSLottieAnimation` 恢复原始 `animateLottieCompositionAsState` 实现
- 依赖修复：`DSTextArea` 移除不存在的 `counter` 参数

### Changed
- `DSCardStyle` 可见性从 `internal` 改为 `public`（被 `AppCommonCard`/`AppStructuredCard` 使用）
- 优化性能诊断工具 `DSPerformanceUtils` 不兼容 Compose 1.9 API 的引用
- 移除 `RangeSliderDefaults` 和 `ElevatedButtonDefaults` 等已废弃 M3 API 引用

## [1.1.0] - 2026-06-22

### Added
- 新增 `EncryptedPrefs`：基于 EncryptedSharedPreferences（AES256-GCM）安全存储 token
- 新增 `TokenAuthenticator`：支持 token 过期自动刷新
- 新增 `DSChip` 组件
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
