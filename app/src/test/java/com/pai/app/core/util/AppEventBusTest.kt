// ============================================================================
// AppEventBusTest.kt
// AppEventBus 单元测试：事件投递 / 顺序 / replay=0 / 挂起发射
// 决策 2 精简版：仅 TokenExpired + GlobalError 两个事件
// ============================================================================

package com.pai.app.core.util

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * [AppEventBus] 单元测试
 *
 * 决策 2 精简后 AppEvent sealed class 仅保留两个变体：
 * - [AppEvent.TokenExpired] - 401 / Token 失效，由 TokenAuthenticator 发射
 * - [AppEvent.GlobalError] - 全局错误消息 + 可选 throwable，由 BaseViewModel 发射
 *
 * 基于 Turbine 验证基于 SharedFlow 的事件总线行为：
 * - emit 后订阅者能收到事件
 * - 多个事件按发射顺序依次接收
 * - emitSuspending 等价于 emit
 * - replay=0：订阅前发射的历史事件不补发给后续订阅者
 * - GlobalError 携带 throwable 不丢失
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AppEventBusTest {

    @Test
    fun `emit TokenExpired 后 events Flow 能收到事件`() = runTest {
        val bus = AppEventBus()

        bus.events.test {
            bus.emit(AppEvent.TokenExpired)
            assertEquals(AppEvent.TokenExpired, awaitItem())
        }
    }

    @Test
    fun `emit GlobalError 后 events Flow 能收到事件`() = runTest {
        val bus = AppEventBus()

        bus.events.test {
            bus.emit(AppEvent.GlobalError("出错了"))
            val event = awaitItem()
            assertTrue(event is AppEvent.GlobalError)
            assertEquals("出错了", (event as AppEvent.GlobalError).message)
        }
    }

    @Test
    fun `多个事件按顺序接收`() = runTest {
        val bus = AppEventBus()

        bus.events.test {
            bus.emit(AppEvent.GlobalError("first"))
            bus.emit(AppEvent.TokenExpired)
            bus.emit(AppEvent.GlobalError("second"))

            val first = awaitItem()
            assertTrue(first is AppEvent.GlobalError)
            assertEquals("first", (first as AppEvent.GlobalError).message)

            assertEquals(AppEvent.TokenExpired, awaitItem())

            val third = awaitItem()
            assertTrue(third is AppEvent.GlobalError)
            assertEquals("second", (third as AppEvent.GlobalError).message)
        }
    }

    @Test
    fun `GlobalError 携带的 throwable 不丢失`() = runTest {
        val bus = AppEventBus()
        val cause = IllegalStateException("boom")

        bus.events.test {
            bus.emit(AppEvent.GlobalError("失败", cause))
            val event = awaitItem() as AppEvent.GlobalError
            assertEquals("失败", event.message)
            assertSame(cause, event.throwable)
        }
    }

    @Test
    fun `emitSuspending 等同 emit 能投递事件`() = runTest {
        val bus = AppEventBus()

        bus.events.test {
            bus.emitSuspending(AppEvent.GlobalError("出错了"))
            val event = awaitItem()
            assertTrue(event is AppEvent.GlobalError)
            assertEquals("出错了", (event as AppEvent.GlobalError).message)
        }
    }

    @Test
    fun `replay 为 0 时订阅前的历史事件不补发`() = runTest {
        val bus = AppEventBus()

        // 在订阅前发射事件，由于 replay=0，事件进入缓冲但不会补发给后续订阅者
        bus.emit(AppEvent.TokenExpired)
        bus.emit(AppEvent.GlobalError("ignored"))

        bus.events.test {
            // 订阅后不应收到上述历史事件
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}
