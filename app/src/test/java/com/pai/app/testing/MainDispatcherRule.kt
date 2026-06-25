// ============================================================================
// MainDispatcherRule.kt
// JUnit Rule：在单元测试中替换主线程 Dispatcher 为 TestDispatcher
// 让 ViewModel 的 viewModelScope 协程可在测试中精确控制
// ============================================================================

package com.pai.app.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * 替换 Dispatchers.Main 为 TestDispatcher 的 JUnit Rule
 *
 * 使用示例：
 * ```kotlin
 * @OptIn(ExperimentalCoroutinesApi::class)
 * class AuthViewModelTest {
 *     @get:Rule val mainDispatcherRule = MainDispatcherRule()
 *
 *     private val testDispatcher = mainDispatcherRule.testDispatcher
 *
 *     @Test
 *     fun `login success updates state`() = runTest(testDispatcher) {
 *         // ...
 *         testDispatcher.scheduler.advanceUntilIdle()
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : TestWatcher() {

    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
