# 命名规范

> AI 规则文件 - 命名约定领域
> 本文件由 CLAUDE.md 拆分而来，对应原 CLAUDE.md 第 5 章。

---

## 一、命名总表（约定大于配置）

| 类型 | 模式 | 示例 |
|------|------|------|
| UI 状态类 | `[Feature]UiState` | `AuthUiState`, `MainUiState` |
| ViewModel | `[Feature]ViewModel` | `AuthViewModel`, `MainViewModel` |
| Screen Composable | `[Feature]Screen` | `AuthScreen`, `MainScreen` |
| **路由定义** | `[Feature]Route` | `AuthRoute`, `HomeRoute`, `ProductDetailRoute(id)` |
| 导航扩展 | `goto[Feature]` | `appNavigator.gotoAuth()`（走 AppNavigator.navigate，RouteInterceptor 生效） |
| Hilt Module | `[Domain]Module` | `NetworkModule`, `DatabaseModule` |
| Repository | `[Entity]Repository` | `PostRepository` |
| 网络 DTO | `[Entity]Dto` | `PostDto` |
| Room Entity | `[Entity]Entity` | `PostEntity` |
| Domain model | `[Entity]Item` 或 `[Entity]` | `ExampleItem`（Repository 跨包返回的 domain model） |
| 全局共享 ViewModel | `MainActivityViewModel` | 固定名 |

---

## 二、命名要点

### 2.1 Feature 命名

- Feature 名使用小写单词，单数（`auth` / `home` / `user` / `settings` / `gallery`）
- 多词 Feature 用小驼峰（`productDetail`），但目录名建议用单单词

### 2.2 类名前缀

- UI 组件：必须以 `DS` 前缀（`DSButton` / `DSText` / `DSTopBar`）—— 见 `07-ui-components.md`
- 数据层组件：以领域或实体为前缀（`PostRepository` / `UserEntity` / `PostDto`）
- Hilt Module：以领域为前缀（`NetworkModule` / `DatabaseModule` / `PreferencesModule`）

### 2.3 包路径命名

- 全小写，不使用下划线或连字符
- 包名按语义选择：
  - `routes/`（复数，含多个路由）
  - `extension/`（单数，导航扩展集合）
  - `entity/`（单数，实体集合）
  - `dao/`（单数，DAO 集合）
  - `model/`（单数，DTO 或 domain model 集合）
  - 业务 feature 用单数名词（`auth` / `user` / `theme`）
- 保留当前实际命名（与脚手架代码一致）

### 2.4 方法命名

- ViewModel 暴露的方法以动词开头：`login()` / `logout()` / `submit()` / `refresh()` / `loadMore()` / `retry()`
- 事件回调 lambda 以 `on` 开头：`onLoginSuccess` / `onBackClick` / `onItemClick`
- StateFlow 字段名直接用业务名：`uiState` / `themeMode` / `isLoggedIn`

### 2.5 文件命名

- 一个文件一个顶层类（ViewModel + UiState 可共存于同一文件）
- 文件名 = 类名（`AuthViewModel.kt` 包含 `AuthViewModel` + `AuthUiState`）
