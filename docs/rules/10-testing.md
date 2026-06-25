# 测试规范

> AI 规则文件 - 测试领域
> 本文件由 CLAUDE.md 拆分而来，对应原 CLAUDE.md 第 12 章。

---

## 一、测试文件结构

```
app/src/
├── test/java/com/pai/app/                    # 单元测试（JVM）
│   ├── testing/                              # 测试工具
│   │   └── MainDispatcherRule.kt
│   ├── architecture/                         # 架构测试
│   │   ├── DesignSystemArchitectureTest.kt
│   │   ├── FeatureArchitectureTest.kt
│   │   └── RouteArchitectureTest.kt
│   ├── core/                                 # core 层测试（含 Repository + MockWebServer）
│   └── feature/                              # ViewModel 测试
└── androidTest/java/com/pai/app/             # UI 测试（需设备）
    ├── HiltTestRunner.kt                     # 自定义 test runner
    ├── HiltComponentActivity.kt              # @HiltAndroidEntryPoint 测试 Activity
    └── feature/
```

### 目录划分

| 目录 | 用途 | 运行环境 |
|------|------|---------|
| `test/` | 纯 JVM 单元测试（JUnit 4.13.2） | 不需 Android Framework |
| `androidTest/` | UI 测试 / 需 Context 的测试（Hilt） | 需连接设备或模拟器 |
| `testing/` | 测试工具共享包（Rule） | 被两类测试共享 |


---

## 二、测试工具

- **MainDispatcherRule**：替换 `Dispatchers.Main` 为 `StandardTestDispatcher`（注意：是 `StandardTestDispatcher` 不是 `UnconfinedTestDispatcher`，需手动 `advanceUntilIdle()`）
- **Turbine**：测试 Flow（`someFlow.test { awaitItem(); ... }`）
- **Robolectric**：测试需要 Android Context 的代码（如 DataStore；注意 EncryptedSharedPreferences 在 Robolectric 下可能不稳定，见 `13-troubleshooting.md`）
- **MockWebServer**：测试 Repository 真实 HTTP 调用（参考 `NetworkModuleTest` / `AppApiTest`）
- **Hilt Android Testing**：`@HiltAndroidTest` + `HiltAndroidRule` + `HiltTestRunner` + `HiltComponentActivity`（UI 测试）

### 2.1 MainDispatcherRule（StandardTestDispatcher）

```kotlin
class MainDispatcherRule(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher(),
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }
    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

// 使用
@get:Rule val mainDispatcherRule = MainDispatcherRule()

// 注意：StandardTestDispatcher 不自动执行协程，需手动 advanceUntilIdle()
@Test
fun `xxx`() = runTest {
    viewModel.doSomething()
    advanceUntilIdle()  // 等协程执行完
    // 断言
}
```


```kotlin
class PostViewModelTest {
    private lateinit var navigator: AppNavigator
    private lateinit var viewModel: PostViewModel

    @Before
    fun setup() {
        navigator = AppNavigator(FakeUserState()).apply { /* setup */ }
        viewModel = PostViewModel(navigator, userState, repository)
    }

    @Test
    fun `加载成功后 state 更新为成功`() = runTest {
        coEvery { repository.getPosts(any(), any()) } returns ApiResult.Success(listOf(/* ... */))
        viewModel.loadFirstPage()
        advanceUntilIdle()
        assertEquals(false, viewModel.uiState.value.isLoading)
    }
}
```

### 2.3 Turbine 测试 Flow

```kotlin
@Test
fun `登录成功后 state 更新`() = runTest {
    val viewModel = AuthViewModel(/* mocks */)
    viewModel.uiState.test {
        // StateFlow replay=1，先收到当前值
        assertEquals(false, awaitItem().isLoggedIn)
        viewModel.login("user", "pass")
        advanceUntilIdle()
        // 后续状态
        assertEquals(true, awaitItem().isLoginSuccess)
    }
}
```

### 2.4 MockWebServer（Repository 真实 HTTP 测试）

```kotlin
class AppApiTest {
    private lateinit var server: MockWebServer
    private lateinit var api: AppApi

    @Before
    fun setup() {
        server = MockWebServer().apply { start() }
        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(AppApi::class.java)
    }

    @After
    fun teardown() { server.shutdown() }

    @Test
    fun `getExamples 成功返回列表`() = runTest {
        server.enqueue(MockResponse().setBody("""[{"id":1,"name":"test"}]"""))
        val result = api.getExamples()
        assertEquals(1, result.size)
    }
}
```

参考脚手架中的 `NetworkModuleTest.kt` / `AppApiTest.kt`（已存在）。

### 2.5 Hilt UI 测试设置

```kotlin
// app/src/androidTest/.../HiltTestRunner.kt
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, HiltTestApplication::class.java.name, context)
    }
}

// app/src/androidTest/.../HiltComponentActivity.kt
@HiltAndroidEntryPoint
class HiltComponentActivity : ComponentActivity()

// 测试
@HiltAndroidTest
class AuthScreenTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)

    @Before fun setup() { hiltRule.setup() }

    @Test
    fun `登录按钮可点击`() {
        val composeTestRule = createAndroidComposeRule<HiltComponentActivity>()
        composeTestRule.onNodeWithText("登录").assertIsEnabled()
    }
}
```

`app/build.gradle.kts` 中 `testInstrumentationRunner = "com.pai.app.HiltTestRunner"`（已配置）。

### 2.6 InMemoryEncryptedPrefs（KeyStore 无关的测试替身）

`EncryptedPrefs` 是接口，生产实现 `EncryptedPrefsImpl` 依赖 Android KeyStore（`EncryptedSharedPreferences`）。单元测试中应使用 `InMemoryEncryptedPrefs` 避免 Robolectric 依赖：

```kotlin
// app/src/test/.../core/network/HeaderInterceptorTest.kt
import com.pai.app.core.datastore.InMemoryEncryptedPrefs

class HeaderInterceptorTest {
    private val encryptedPrefs = InMemoryEncryptedPrefs()

    @Test
    fun `登录后 Authorization 头注入`() = runTest {
        encryptedPrefs.saveToken("my-token")
        // 构造 interceptor 并断言 header
    }
}
```

Hilt UI 测试中替换：
```kotlin
@UninstallModules(EncryptedPrefsModule::class)
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [EncryptedPrefsModule::class],
)
@Module
object TestEncryptedPrefsModule {
    @Provides @Singleton
    fun provideEncryptedPrefs(): EncryptedPrefs = InMemoryEncryptedPrefs()
}
```

注意：`InMemoryEncryptedPrefs` token 仅存于内存，适合 ViewModel / Interceptor / Authenticator 单元测试；跨进程持久化测试请用 `EncryptedPrefsImpl` + Robolectric。

---

## 三、测试命名规范

- 方法名用反引号包中文：`@Test fun `登录成功后 state 更新`() {}`
- 测试类名：`[ClassName]Test`
- 测试包路径与被测类路径对应（`feature/auth/AuthViewModel` → `test/feature/auth/AuthViewModelTest`）

### 命名风格示例

```kotlin
class AuthViewModelTest {
    @Test
    fun `登录成功后 state 更新为已登录`() { ... }

    @Test
    fun `密码错误时显示错误提示`() { ... }

    @Test
    fun `token 失效时自动退出登录`() { ... }
}
```

---

## 四、测试覆盖要求

- 所有 ViewModel **必须**有单元测试
- Repository **必须**有单元测试（脚手架含 `ExampleRepositoryTest` / `AuthRepositoryTest` 示例）
- Compose Screen 鼓励有 UI 测试（脚手架含 `AuthScreenTest` Hilt UI 测试示例）
- 测试**禁止**真实网络请求（用 MockWebServer；脚手架含 `NetworkModuleTest` / `AppApiTest` 示例）
- 覆盖率由 Kover 收集（CI 中 `coverage` Job 跑 `:app:koverXmlReport`）

### 4.1 ViewModel 测试覆盖点

- 初始 state 正确
- 用户操作触发 state 更新
- 网络成功 → state 更新
- 网络失败 → error 字段更新
- 一次性事件正确触发与重置

### 4.2 Repository 测试覆盖点

- 调用 API 正确参数
- API 成功 → 返回 `ApiResult.Success`
- API 失败 → 返回 `ApiResult.Error`（异常分类正确：Http / Serialization / Network / Unknown）
- 缓存逻辑（如有）：先返回缓存，再返回远程
- Entity → domain model 转换正确

### 4.3 Compose UI 测试覆盖点

- 关键文案渲染
- 点击事件触发 ViewModel 调用
- 状态切换 UI 正确（Loading / Success / Error）

---

## 五、MockWebServer 用法

见 §2.4 示例。脚手架中 `app/build.gradle.kts` 已声明 `testImplementation(libs.okhttp.mockwebserver)`，`NetworkModuleTest.kt` / `AppApiTest.kt` 是真实使用示例。

---

## 六、新增测试模式

> 新增测试模式（如新增 Robolectric 配置 / 新增 Hilt 测试 / 新增 Compose UI 测试模式）时，在本文件追加示例（详见 `00-documentation-protocol.md`）。

---



### 7.1 测试覆盖的架构红线

| 红线 | 测试文件 | 说明 |
|------|---------|------|
| designsystem 5 层依赖方向 | `DesignSystemArchitectureTest.kt` | foundation ↛ primitives/patterns/shell/overlays；primitives ↛ patterns/shell/overlays；patterns ↛ shell/overlays；shell ↔ overlays 互不依赖；designsystem ↛ feature/appstate；patterns ↛ BaseNetWorkUiState/ViewModel |
| feature→feature 隔离 | `FeatureArchitectureTest.kt` | `feature.*` 不得 `import com.pai.app.feature.<other>.*` |
| feature 不 import retrofit2/room | `FeatureArchitectureTest.kt` | `feature.*` 不得 `import retrofit2.*` / `androidx.room.*`（跨层短路禁令） |
| ViewModel 必须继承 BaseViewModel | `FeatureArchitectureTest.kt` | 所有 `feature.*.*ViewModel` 必须继承 `core.base.BaseViewModel` 或 `BaseNetWorkViewModel` |
| 路由类型安全 | `RouteArchitectureTest.kt` | 所有路由必须是 `@Serializable data object` / `data class` 实现 `AppRoute` 接口；`composable<XxxRoute>` 注册必须用泛型形式 |

### 7.2 运行命令

```bash
# 单独跑架构测试
./gradlew :app:testDebugUnitTest --tests "com.pai.app.architecture.*"

# 跑全部单元测试（含架构测试）
./gradlew :app:testDebugUnitTest
```

### 7.3 新增架构约束时的更新流程

1. 在对应 `*ArchitectureTest.kt` 追加 `@Test fun `xxx should not depend on yyy`() { ... }`
3. 在本文件 7.1 表格中追加一行说明
4. 在回复中汇报：「已更新 docs/rules/10-testing.md，追加 测试 xxx」

> 详见 `14-development-workflow.md` 的脚手架扩展场景。

---

## 八、JUnit 版本说明

本项目使用 **JUnit 4.13.2**（不是 JUnit 5）。所有测试用 `import org.junit.Test`（JUnit 4 API），`@RunWith(AndroidJUnit4::class)` 用于 androidTest。

> 旧文档中「JUnit5」标注是错误，实际是 JUnit 4.13.2。
