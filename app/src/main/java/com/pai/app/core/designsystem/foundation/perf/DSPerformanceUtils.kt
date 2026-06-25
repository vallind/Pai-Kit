// ============================================================================
// DSPerformanceUtils.kt
// M3 卓越线补齐：性能优化工具（重组跟踪 + @Stable 检测 + 防抖）
// 用法：开发模式下观察组件重组次数；生产模式自动 no-op
// ============================================================================

package com.pai.app.core.designsystem.foundation.perf

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.platform.LocalInspectionMode
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * 重组次数跟踪器
 *
 * 在开发模式下统计组件的重组次数，超过阈值时打印警告。
 * 生产模式下自动 no-op（无运行时开销）。
 *
 * 使用示例：
 * ```kotlin
 * @Composable
 * fun MyComponent(data: SomeData) {
 *     DSRecompositionTracker(tag = "MyComponent") {
 *         // 组件内容
 *     }
 * }
 * ```
 *
 * 日志输出：
 * ```
 * [DS-Perf] MyComponent recomposed 5 times (threshold=3)
 * ```
 *
 * @param tag 标签，用于日志识别
 * @param threshold 警告阈值，默认 3 次（重组超过 3 次表示可能有性能问题）
 * @param enabled 是否启用跟踪，默认仅预览/开发模式启用
 * @param content 被跟踪的内容
 */
@Composable
fun DSRecompositionTracker(
    tag: String,
    threshold: Int = 3,
    enabled: Boolean = LocalInspectionMode.current,
    content: @Composable () -> Unit
) {
    if (!enabled) {
        content()
        return
    }

    var count by remember { mutableIntStateOf(0) }
    SideEffect {
        count++
        if (count > threshold) {
            Timber.tag("DS-Perf").w("$tag recomposed $count times (threshold=$threshold)")
        }
    }
    content()
}

/**
 * 重组原因分析器（开发模式）
 *
 * 在 Debug 模式下打印重组原因（哪个参数变化导致重组）。
 * 需要开启 `composeCompilerReports` 才能获得详细报告。
 *
 * 使用示例：
 * ```kotlin
 * @Composable
 * fun MyComponent(props: MyProps) {
 *     DSRecompositionReason("MyComponent", props)
 *     // 内容
 * }
 * ```
 *
 * @param tag 标签
 * @param keys 要跟踪的参数（变化时打印日志）
 */
@Composable
fun DSRecompositionReason(
    tag: String,
    vararg keys: Any?
) {
    if (!LocalInspectionMode.current) return
    val lastKeys = remember { arrayOfNulls<Any?>(keys.size) }
    val changed = keys.indices.any { keys[it] != lastKeys[it] }
    if (changed) {
        val changedIndices = keys.indices.filter { keys[it] != lastKeys[it] }
        Timber.tag("DS-Perf").d("$tag recomposed: changed params = $changedIndices")
        keys.indices.forEach { lastKeys[it] = keys[it] }
    }
}

/**
 * Modifier 重组跟踪
 *
 * 检查 Modifier 链是否引起不必要的重组。
 *
 * 使用示例：
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .fillMaxSize()
 *         .trackModifier("MyBox")
 *         .clickable { }
 * )
 * ```
 */
fun Modifier.trackModifier(tag: String): Modifier = composed {
    if (LocalInspectionMode.current) {
        SideEffect {
            Timber.tag("DS-Perf").v("$tag Modifier recomposed")
        }
    }
    this
}

/**
 * 列表项重组跟踪
 *
 * 用于 LazyColumn / LazyRow 的 items，跟踪单项重组次数。
 * 配合 key 参数使用，定位 key 不稳定导致的全量重组问题。
 *
 * 使用示例：
 * ```kotlin
 * LazyColumn {
 *     items(items = data, key = { it.id }) { item ->
 *         DSListItemTracker(item.id) {
 *             MyListItem(item)
 *         }
 *     }
 * }
 * ```
 */
@Composable
fun DSListItemTracker(
    itemId: Any,
    threshold: Int = 2,
    content: @Composable () -> Unit
) {
    DSRecompositionTracker(
        tag = "ListItem[$itemId]",
        threshold = threshold,
        content = content
    )
}

/**
 * 防抖工具
 *
 * 高频触发的回调（如滑动、输入）用防抖避免过度重组。
 *
 * 使用示例：
 * ```kotlin
 * val debouncedSearch = rememberDebounced<String>(delayMs = 300) { query ->
 *     viewModel.search(query)
 * }
 * TextField(
 *     value = query,
 *     onValueChange = debouncedSearch
 * )
 * ```
 *
 * @param delayMs 防抖延迟（毫秒）
 * @param block 实际执行的回调（延迟后触发）
 */
@Composable
fun <T> rememberDebounced(
    delayMs: Long = 300L,
    block: (T) -> Unit
): (T) -> Unit {
    val scope = rememberCoroutineScope()
    var lastJob by remember { mutableStateOf<Job?>(null) }

    return remember(block, delayMs) {
        { value: T ->
            lastJob?.cancel()
            lastJob = scope.launch {
                delay(delayMs)
                block(value)
            }
            Unit
        }
    }
}

/**
 * Stable 标记助手 - 显式标注不可变状态类
 *
 * 对于 Compose 编译器无法推断稳定性的类（如含 List / Map 字段的 data class），
 * 显式标注 @Stable 可避免不必要的重组。
 *
 * 注意：只有当 List 内容确实不可变（如来自 Room Flow / StateFlow）时才可标注 @Stable，
 * 否则会引入 bug。
 *
 * 用法：
 * ```kotlin
 * @Stable
 * data class MyState(
 *     val items: List<Item>,  // List 字段默认不稳定
 *     val name: String
 * )
 * ```
 */
@Stable
interface DSStableMarker

// ============================================================================
// 稳定性包装类 — 解决 Compose 对 List/Map 稳定性判断问题
// Compose 在判断 List/Map 字段稳定性时会认为不稳定，用这些包装强制稳定
// ============================================================================

/**
 * DSStableList - 稳定的 List 包装
 *
 * Compose 在判断含 `List<T>` 字段的 data class 时会认为不稳定（因为 List 是接口），
 * 导致不必要的重组。用 [DSStableList] 包装可强制稳定。
 *
 * 使用示例：
 * ```kotlin
 * @Immutable
 * data class MyUiState(
 *     val items: DSStableList<Item> = DSStableList(emptyList()),  // ✅ 稳定
 *     // val items: List<Item> = emptyList()  // ❌ 不稳定
 * )
 * ```
 *
 * @param items 实际 List 数据
 */
@Immutable
data class DSStableList<T>(val items: List<T>) : Iterable<T> by items {
    val size: Int get() = items.size
    fun isEmpty(): Boolean = items.isEmpty()
    fun isNotEmpty(): Boolean = items.isNotEmpty()
    operator fun get(index: Int): T = items[index]
    fun contains(element: T): Boolean = items.contains(element)
    fun indexOf(element: T): Int = items.indexOf(element)
}

/**
 * DSStableMap - 稳定的 Map 包装
 *
 * 与 [DSStableList] 同理，用于 Map 字段。
 *
 * @param entries 实际 Map 数据
 */
@Immutable
data class DSStableMap<K, V>(val entries: Map<K, V>) {
    val size: Int get() = entries.size
    fun isEmpty(): Boolean = entries.isEmpty()
    fun isNotEmpty(): Boolean = entries.isNotEmpty()
    operator fun get(key: K): V? = entries[key]
    fun containsKey(key: K): Boolean = entries.containsKey(key)
    fun containsValue(value: V): Boolean = entries.containsValue(value)
    val keys: Set<K> get() = entries.keys
    val values: Collection<V> get() = entries.values
}

/**
 * DSStableText - 稳定的文本包装
 *
 * 用于含动态文本字段的状态类，避免文本变化导致父级重组。
 *
 * @param text 实际文本
 */
@Stable
data class DSStableText(val text: String) {
    val length: Int get() = text.length
    fun isEmpty(): Boolean = text.isEmpty()
    fun isNotEmpty(): Boolean = text.isNotEmpty()
    override fun toString(): String = text
}

/**
 * DSStableUiState - 稳定的 UI 状态接口
 *
 * 业务侧公开的 UiState 数据类可实现此接口，强制 @Immutable 稳定性契约。
 *
 * 使用示例：
 * ```kotlin
 * @Immutable
 * data class MyScreenUiState(
 *     override val data: List<Item>? = null,
 *     override val isLoading: Boolean = false,
 *     override val error: String? = null
 * ) : DSStableUiState<List<Item>>
 * ```
 */
@Immutable
interface DSStableUiState<out T> {
    val data: T?
    val isLoading: Boolean
    val error: String?
}
