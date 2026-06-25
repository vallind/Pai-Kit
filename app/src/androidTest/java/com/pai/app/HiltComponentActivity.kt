// ============================================================================
// HiltComponentActivity.kt
// Compose UI 测试用 ComponentActivity（标注 @AndroidEntryPoint 接入 Hilt 图）
// ============================================================================
//
// 用途：androidTest 中 `createAndroidComposeRule<HiltComponentActivity>()`
// 让被测 Composable 内的 `hiltViewModel()` 能在 Hilt 测试图下解析 ViewModel。
// 普通 ComponentActivity 不接入 Hilt，hiltViewModel() 会抛 IllegalStateException。
// ============================================================================

package com.pai.app

import androidx.activity.ComponentActivity
import dagger.hilt.android.AndroidEntryPoint

/**
 * Compose UI 测试用 [ComponentActivity]，接入 Hilt 依赖图
 *
 * 用法：
 * ```kotlin
 * @HiltAndroidTest
 * @RunWith(AndroidJUnit4::class)
 * class MyScreenTest {
 *     @get:Rule val hiltRule = HiltAndroidRule(this)
 *     @get:Rule val composeRule = createAndroidComposeRule<HiltComponentActivity>()
 *
 *     @Before fun setUp() { hiltRule.inject() }
 * }
 * ```
 *
 * 标注 [AndroidEntryPoint] 后，Hilt 会为本 Activity 生成注入入口，
 * 使其 ViewModelStoreOwner + HiltViewModelFactory 可用 —— `hiltViewModel()`
 * 在 Composable 内即可正常构造被 @HiltViewModel 标注的 ViewModel。
 */
@AndroidEntryPoint
class HiltComponentActivity : ComponentActivity()
