# 业务接入指南

> 阶段：集成（阅读路径第 2 步）  
> 适合：第一次用 Pai 开发业务 App 的开发者  
> 预计：30 分钟上手

---

## 阅读路径

```
README → INTEGRATION_GUIDE（本文件）→ BUILD → CODING → 组件 API → 容器使用 → QUALITY
```

---

## 1. 5 分钟接入

### 1.1 克隆脚手架

```bash
git clone https://github.com/yourorg/pai-scaffold.git my-app
cd my-app
./scripts/create-app.sh com.company.myapp "My App"
```

`create-app.sh` 自动完成：
- 改包名 `com.pai.app` → `com.company.myapp`
- 改应用名 `Pai Scaffold` → `My App`
- 清理 Termux 相关注释
- 重置 git 历史（可选）

### 1.2 第一个页面

新建 `app/src/main/java/com/company/myapp/feature/user/UserScreen.kt`：

```kotlin
package com.company.myapp.feature.user

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.primitives.*
import com.pai.app.core.designsystem.shell.DSAppScaffold
import com.pai.app.core.designsystem.shell.DSTopBarAction
import com.pai.app.core.designsystem.containers.AppCommonCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit

@Composable
fun UserScreen(
    onBackClick: () -> Unit,
    onEditClick: () -> Unit
) {
    DSAppScaffold(
        title = "用户详情",
        showBackIcon = true,
        onBackClick = onBackClick,
        topBarActions = listOf(
            DSTopBarAction(Icons.Default.Edit, "编辑") { onEditClick() }
        )
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppCommonCard(
                title = "张三",
                subtitle = "产品经理"
            ) {
                DSText("邮箱：zhangsan@example.com", variant = DSTextVariant.BodyMedium)
            }

            DSButton(
                text = "编辑资料",
                onClick = onEditClick,
                modifier = Modifier.fillMaxWidth(),
                size = DSButtonSize.Large
            )
        }
    }
}
```

### 1.3 接入路由

在 `navigation/routes/AppRoutes.kt` 添加路由：

```kotlin
@Serializable
data object UserRoute : AppRoute
```

在 `navigation/extension/NavExtensions.kt` 添加扩展：

```kotlin
fun AppNavigator.gotoUser() = navigate(UserRoute)
```

在 `MainActivity.kt` 的 `AppNavGraph` 注册：

```kotlin
composable<UserRoute> { UserScreen(onBackClick = { navController.goBack() }, onEditClick = { }) }
```

### 1.4 加 Preview

```kotlin
@Preview(showBackground = true, name = "User Screen - Light")
@Composable
private fun UserScreenLightPreview() {
    DSPreviewScenes.Light {
        UserScreen(onBackClick = {}, onEditClick = {})
    }
}

@Preview(showBackground = true, name = "User Screen - Dark")
@Composable
private fun UserScreenDarkPreview() {
    DSPreviewScenes.Dark {
        UserScreen(onBackClick = {}, onEditClick = {})
    }
}
```

---

## 2. 接入 ViewModel

### 2.1 创建 ViewModel

```kotlin
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,
    navigator: AppNavigator,
    userState: UserState
) : BaseViewModel(navigator, userState) {

    private val _uiState = MutableStateFlow<UserUiState>(UserUiState.Loading)
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    init { loadUser() }

    fun loadUser() {
        launch {
            _uiState.value = UserUiState.Loading
            userRepository.getUser()
                .collect { result ->
                    _uiState.value = when (result) {
                        is ApiResult.Success -> UserUiState.Success(result.data)
                        is ApiResult.Error -> UserUiState.Error(result.message)
                        is ApiResult.Loading -> UserUiState.Loading
                    }
                }
        }
    }
}

sealed interface UserUiState {
    data object Loading : UserUiState
    data class Success(val user: User) : UserUiState
    data class Error(val message: String) : UserUiState
}
```

### 2.2 在 Composable 中消费

```kotlin
@Composable
fun UserScreen(
    viewModel: UserViewModel = hiltViewModel(),
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DSAppScaffold(title = "用户详情", showBackIcon = true, onBackClick = onBackClick) { padding ->
        when (val state = uiState) {
            is UserUiState.Loading -> DSFullScreenLoading(modifier = Modifier.padding(padding))
            is UserUiState.Error -> DSFullScreenError(state.message, onRetry = { viewModel.loadUser() })
            is UserUiState.Success -> UserContent(state.user, Modifier.padding(padding))
        }
    }
}
```

---

## 3. 接入数据层

### 3.1 创建 Repository

```kotlin
// core/data/UserRepository.kt
@Singleton
class UserRepository @Inject constructor(
    private val api: AppApi,
    private val userDao: UserDao
) {
    fun getUser(): Flow<ApiResult<User>> = api.getUser().asResult(
        onSuccess = { it.toDomain() },
        onError = { ApiResult.Error(it.message ?: "未知错误") }
    )
}
```

### 3.2 创建 API

```kotlin
// core/network/AppApi.kt
interface AppApi {
    @GET("user/profile")
    suspend fun getUser(): UserDto
}
```

### 3.3 创建 Room Entity + DAO

```kotlin
// core/database/entity/UserEntity.kt
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String
)

// core/database/dao/UserDao.kt
@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE id = :id")
    fun observeUser(id: String): Flow<UserEntity?>

    @Upsert
    suspend fun upsertUser(user: UserEntity)
}
```

记得在 `AppDatabase.kt` 注册 entity 并 `version + 1`，在 `DatabaseModule.kt` 提供 DAO。

---

## 4. 主题配置

### 4.1 在 MainActivity 接入主题

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeState by viewModel.themeState.collectAsStateWithLifecycle()

            DSDesignTheme(
                darkTheme = themeState.darkTheme,
                dynamicColor = themeState.dynamicColor,
                brandColor = themeState.brandColor,
                fontSizeScale = themeState.fontSizeScale,
                amoled = themeState.amoled,
                highContrast = themeState.highContrast
            ) {
                provideDSWindowSizeClass {
                    AppNavGraph()
                }
            }
        }
    }
}
```

### 4.2 主题调试面板（开发期）

```kotlin
DSDesignTheme(...) {
    provideDSWindowSizeClass {
        AppNavGraph()
        DSThemeDebugPanel(
            state = debugState,
            onStateChange = { debugState = it },
            enabled = BuildConfig.DEBUG  // 仅 Debug 构建显示
        )
    }
}
```

---

## 5. 常见接入场景

### 5.1 表单页

详见 [docs/components/layouts.md 第 7.3 节](docs/components/layouts.md#73-模板-c表单页form-page)。

### 5.2 列表分页页

详见 [docs/components/layouts.md 第 7.4 节](docs/components/layouts.md#74-模板-d列表分页页list--pagination-page)。

### 5.3 设置页

详见 [docs/components/layouts.md 第 7.5 节](docs/components/layouts.md#75-模板-e设置页settings-page)。

### 5.4 详情页

详见 [docs/components/layouts.md 第 7.1 节](docs/components/layouts.md#71-模板-a详情页detail-page)。

---

## 6. 下一步

| 完成 | 去哪 |
|---|---|
| 接入成功，能跑起来 | → [BUILD.md](BUILD.md) 了解构建命令 |
| 想写代码 | → [CODING.md](CODING.md) 看编码规范 |
| 想用组件 | → [docs/components/README.md](docs/components/README.md) 速查表 |
| 想过质量门禁 | → [QUALITY.md](QUALITY.md) 静态检查 |

---

## 7. 常见问题

### Q1: create-app.sh 改包名后编译失败？

A: 检查 `gradle/libs.versions.toml` 的 `applicationId` 是否也改了，运行 `./gradlew clean` 后重试。

### Q2: Hilt 注入失败？

A: 确认 `PaiApplication` 上有 `@HiltAndroidApp`，且 ViewModel 有 `@HiltViewModel` + `@Inject constructor`。

### Q3: 路由跳转无反应？

A: 确认 `AppNavGraph` 中注册了 `composable<XxxRoute>`，且 `gotoXxx()` 扩展走 `AppNavigator`。

### Q4: 主题不生效？

A: 确认 `DSDesignTheme` 包裹了所有内容，且 `provideDSWindowSizeClass` 在其内部。

---

## 阅读路径

上一篇：[README.md](README.md) · 下一篇：[BUILD.md](BUILD.md)
