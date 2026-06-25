# 包级隔离强制规则

> AI 规则文件 - 架构边界领域
> 本文件由 CLAUDE.md 拆分而来，对应原 CLAUDE.md 第 4 章（4.1 / 4.2 / 4.3 / 4.4）。

---

## 一、依赖流向（强制单向）

```
feature.*  →  core.* + navigation.*
core.*     →  core.*（内部依赖）
navigation.*  →  core.*
feature.*  ✗  feature.*（禁止互相依赖）
core.*     ✗  feature.*（禁止反向）
```

依赖只能向下流动（feature → core/navigation → core），禁止同层横向依赖或反向依赖。

### 1.1 core 子包分层（决策 P1-1）

```
core/
├── domain/         Repository 接口 + domain model（KMP-ready，无 Android 依赖）
│   ├── AuthRepository.kt        # interface
│   ├── ExampleRepository.kt     # interface
│   └── model/
│       ├── UserItem.kt          # domain model（data class，纯 Kotlin）
│       └── ExampleItem.kt
├── data/           Repository 实现（@Inject）+ Entity → domain model 映射
│   ├── AuthRepositoryImpl.kt    # @Singleton class ... : AuthRepository
│   ├── ExampleRepositoryImpl.kt
│   └── di/
│       └── DataModule.kt        # @Binds abstract fun bindXxxRepository(impl): XxxRepository
├── database/       Room + DAO + Entity（internal，不外泄到 feature）
├── network/        Retrofit + OkHttp + DTO
├── datastore/      DataStore + EncryptedPrefs
├── base/           BaseViewModel + ApiResult + AppException
├── util/           工具类
└── appstate/       MainActivityViewModel
```

**domain 层 KMP-ready 红线**`）：

- `core.domain.*` **不得** import `android.*` / `androidx.*` / `retrofit2.*` /
  `com.pai.app.core.network.*` / `com.pai.app.core.database.*`
- 允许的 import：`kotlin.*` / `kotlinx.*` / `com.pai.app.core.base.*`（ApiResult / AppException 等纯 Kotlin sealed class）/ `com.pai.app.core.domain.*`
- 未来 KMP 迁移（P3-1）时，`core/domain/` 原样移到 `shared/commonMain/`，
  iOS 端复用接口与 model；各端 Repository Impl 各自实现

**Repository 接口/实现分离规则**：

- feature 注入 Repository **接口**（`core.domain.*Repository`），不感知实现（`core.data.*RepositoryImpl`）
- 接口在 `core/domain/`（KMP-ready），实现在 `core/data/`（`@Inject constructor` + `@Singleton`）
- Hilt 通过 `core/data/di/DataModule.kt` 的 `@Binds` 绑定接口 → 实现
  两种方式都不破坏 feature 对接口的依赖

---

## 二、可见性修饰符

| 类型 | 修饰符 | 例外 |
|------|--------|------|
| `core.*` / `feature.*` / `navigation.*` 内部类 | `internal` | Hilt Module / @Inject 类需 public |
| `MainActivity` / `PaiApplication` | `public` | Android 入口 |
| Hilt `@Module` / `@Provides` 返回类型 | `public` | Hilt 代码生成需要 |
| `@Inject constructor` 类 | `public` | Hilt 代码生成需要 |
| `@HiltViewModel` | `internal` 可加 | 同模块内 Hilt 可见 |
| `EncryptedPrefs` | `public` | Hilt @Inject constructor + @Singleton，需被 UserPreferences / TokenAuthenticator / HeaderInterceptor 注入 |
| `TokenAuthenticator` | `public` | Hilt @Inject constructor + @Singleton，需被 NetworkModule.provideOkHttpClient 参数接收 |
| `ApiResult` / `AppException` | `public` | BaseNetWorkViewModel.requestApiFlow 是 protected abstract，签名须公开 |
| `ExampleItem` 等 domain model | `public` | Repository 跨包返回给 feature，须公开 |
| Room `@Entity` | `internal` | 不应外泄给 Repository 调用方 |
| Room `@Dao` | `public` | Hilt @Provides 需要 |

**默认 internal**：除 Hilt 代码生成需要的类外，所有业务类、ViewModel、Screen、Repository、DTO、Entity 都使用 `internal`，避免跨包泄漏实现细节。

**Repository 实现类规则**：若 `@Inject constructor` 则 `public`（Hilt 需要），否则 `internal`。Repository **必须**把 `Entity` 转换为 domain model 后再暴露给 feature。

**Repository 接口/实现分离（决策 P1-1）**：Repository **必须**拆为接口 + 实现：
- 接口在 `core.domain`（`interface XxxRepository`，KMP-ready，无 Android 依赖）
- 实现在 `core.data`（`@Singleton class XxxRepositoryImpl @Inject constructor(...) : XxxRepository`）
- `core/data/di/DataModule.kt` 用 `@Binds` 绑定接口 → 实现
- feature 注入接口类型（`private val xxxRepository: XxxRepository`），不感知 Impl

**Token 存储规则**：Token **必须**存 `EncryptedPrefs`（EncryptedSharedPreferences AES256-GCM），不进 `UserPreferences` DataStore；`UserPreferences` 仅保留 `userId` / `isLoggedIn` 等非敏感字段。

---

## 三、模型分层（防 AI 混淆）

| 模型类型 | 包路径 | 注解 | 可见性 |
|---------|--------|------|--------|
| 网络 DTO | `core.network.model` | `@Serializable` | `internal` |
| 数据库 Entity | `core.database.entity` | `@Entity` | `internal` |
| Domain model | `core.domain.model` | 纯 `data class` | `public`（Repository 接口跨包返回，KMP-ready） |
| UI 共享模型 | `core.appstate.model` | 纯 `data class` | 视需要 |
| Feature 状态 | `feature.xxx.XxxUiState` | 纯 `data class` | `internal` |

**严禁混用**：禁止把 `@Entity` 类直接丢给 Composable，禁止把 `@Serializable` DTO 直接传给 Room。

**domain model 必须在 `core.domain.model`**（决策 P1-1）：domain model 是 Repository 接口的返回类型载体，与接口同处 `core.domain` 包，纯 Kotlin data class，KMP-ready。**禁止**继续在 `core.data.model` 放 domain model（旧位置已废弃，ExampleItem 已迁出）。

模型必须按层分明：
- 网络层只产生 / 消费 `@Serializable` DTO
- 数据库层只产生 / 消费 `@Entity`
- Repository 接口（`core.domain`）只声明 / 返回 domain model
- Repository 实现（`core.data`）把 `Entity` / `DTO` 转换为 domain model（如 `ExampleItem`）后再返回
- UI 层只消费 `data class`（domain model / UiState / UI 共享模型）
- 跨层传递时必须做映射（DTO → Entity → domain model → UiModel）

---

## 四、数据访问规则

- `feature.*` 下的 ViewModel **只能**调用 `core.domain.*Repository`（**接口**，决策 P1-1）
- ViewModel **不得** import `core.data.*RepositoryImpl`（实现细节，Hilt `@Binds` 解析）
- **禁止**在 ViewModel 中直接 `import retrofit2.*` 或 `androidx.room.*`
- **禁止**在 Composable 中直接调用 Repository / Retrofit / Room
- 网络请求必须用 `safeApiCall { api.xxx() }` 包装为 `ApiResult<T>`，或用 `Flow<T>.asResult(): Flow<ApiResult<T>>` 包装 Flow
- `AppApi` 返回纯 DTO（`@Serializable`），**不返回** sealed class

### 数据访问流向示意

```
Composable (feature.xxx)
  │  仅持有 ViewModel
  ▼
ViewModel (feature.xxx)  ←─ internal
  │  仅调用 Repository 接口（core.domain.*Repository）
  ▼
Repository 接口 (core.domain)   ←─ interface（KMP-ready，无 Android 依赖）
  │  Hilt @Binds（core.data.di.DataModule）解析到 Impl
  ▼
Repository 实现 (core.data.*RepositoryImpl)   ←─ @Singleton @Inject constructor
  │  调用 AppApi (Retrofit) / Dao (Room) + 用 safeApiCall 包装为 ApiResult
  │  把 Entity / DTO 转换为 domain model（如 ExampleItem）后再返回
  ▼
AppApi / Dao (core.network / core.database)
  │  返回 DTO / Entity（Entity 标 internal）
  ▼
Repository Impl 转换为 domain model 返回给 ViewModel（经接口）
```

任何跨层短路访问都是违规。详见 `06-viewmodel.md` 中 Repository 模式说明。
