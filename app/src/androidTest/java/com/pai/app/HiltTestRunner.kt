// ============================================================================
// HiltTestRunner.kt
// 自定义 AndroidJUnitRunner —— 为 instrumented 测试注入 HiltTestApplication
// ============================================================================
//
// 在 app/build.gradle.kts 中通过
//   testInstrumentationRunner = "com.pai.app.HiltTestRunner"
// 启用，使所有 androidTest 用例在 Hilt 依赖图下运行。
// 业务方在 androidTest 中写 @HiltAndroidTest + @get:Rule HiltAndroidRule(this)
// 即可注入真实 Hilt 提供的 Repository / ViewModel / Preferences 单例。
// ============================================================================

package com.pai.app

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import dagger.hilt.android.testing.HiltTestApplication

/**
 * HiltTestRunner - 自定义 AndroidJUnitRunner，为 instrumented 测试注入 HiltTestApplication
 *
 * 与默认 [AndroidJUnitRunner] 的唯一区别：用 [HiltTestApplication] 替换
 * 真实 [Application]，使 Hilt 测试组件能注入测试用依赖图
 * （由 `@HiltAndroidTest` + `HiltAndroidRule` 触发）。
 *
 * 业务方在 androidTest 中：
 * ```kotlin
 * @HiltAndroidTest
 * @RunWith(AndroidJUnit4::class)
 * class MyScreenTest {
 *     @get:Rule val hiltRule = HiltAndroidRule(this)
 *
 *     @Before fun setUp() { hiltRule.inject() }
 *
 *     @get:Rule val composeRule = createAndroidComposeRule<HiltComponentActivity>()
 * }
 * ```
 */
class HiltTestRunner : AndroidJUnitRunner() {
    override fun newApplication(
        cl: ClassLoader?,
        name: String?,
        context: Context?,
    ): Application = super.newApplication(cl, HiltTestApplication::class.java.name, context)
}
