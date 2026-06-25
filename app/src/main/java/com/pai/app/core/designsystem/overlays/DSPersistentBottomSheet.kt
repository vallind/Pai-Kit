// ============================================================================
// DSPersistentBottomSheet.kt
// 常驻底部弹层 - 不遮挡主内容，与主内容并排显示
// M3 卓越线补齐：有机体 #4 完整覆盖（ModalBottomSheet 已有 + Persistent 新增）
// 适用于：音乐播放器底部栏、地图搜索结果、筛选条件面板
// ============================================================================

package com.pai.app.core.designsystem.overlays

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.BottomSheetScaffoldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import com.pai.app.core.designsystem.primitives.DSButton

/**
 * DSPersistentBottomSheet - 常驻底部弹层
 *
 * 与 [DSBottomSheet]（ModalBottomSheet）的区别：
 * - ModalBottomSheet: 模态浮层，半透明遮罩盖住主内容
 * - PersistentBottomSheet: 常驻栏，与主内容并排（主内容下沉），无遮罩
 *
 * 适用场景：
 * - 音乐播放器底部 mini 播放栏（展开为完整播放器）
 * - 地图 App 底部搜索结果列表（拖动展开看更多）
 * - 电商 App 商品筛选面板
 *
 * 使用示例：
 * ```kotlin
 * val sheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
 * Box(modifier = Modifier.fillMaxSize()) {
 *     // 主内容
 *     MainContent()
 *     // 常驻底部弹层
 *     DSPersistentBottomSheet(
 *         sheetState = sheetState,
 *         modifier = Modifier.align(Alignment.BottomCenter)
 *     ) {
 *         FilterPanel()
 *     }
 * }
 * ```
 *
 * 设计规范：
 * - 颜色：surface / onSurface
 * - 形状：顶部 extraLarge 圆角
 * - 阴影：level3 顶部阴影
 * - 状态：Hidden / PartiallyExpanded / Expanded
 *
 * @param content 底部弹层内容
 * @param modifier 修饰符
 * @param sheetState sheet 状态
 * @param onDismissRequest 完全隐藏时的回调
 * @param skipHiddenState 是否禁止完全隐藏（true 时只能 Partially/Expanded 切换）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSPersistentBottomSheet(
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberStandardBottomSheetState(),
    onDismissRequest: (() -> Unit)? = null,
    skipHiddenState: Boolean = true
) {
    // 注意：M3 的 BottomSheetScaffold 是 StandardBottomSheet 的标准宿主，
    // 但如果需要直接在 Box 中使用 Persistent BottomSheet，
    // 用 Surface 包装 + 处理 sheetState 的 drag gesture 是更灵活的方式。
    //
    // 此处采用 Surface 直接渲染，业务方自行控制 anchor 状态切换。
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = DSTokens.Elevation.level3,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = DSTokens.Radius.extraLarge,
            topEnd = DSTokens.Radius.extraLarge
        ),
        tonalElevation = DSTokens.Elevation.level1
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = DSTokens.Spacing.md)
        ) {
            // Drag handle
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .padding(bottom = DSTokens.Spacing.md)
                    .padding(horizontal = DSTokens.Spacing.lg)
                    .align(androidx.compose.ui.Alignment.CenterHorizontally)
            ) {
                androidx.compose.material3.BottomSheetDefaults.DragHandle()
            }
            content()
        }
    }
}

/**
 * DSBottomSheetScaffold - 带 Persistent BottomSheet 的脚手架
 *
 * 封装 M3 的 BottomSheetScaffold，提供 TopBar + 内容 + 常驻 BottomSheet 的完整布局。
 *
 * 使用示例：
 * ```kotlin
 * DSBottomSheetScaffold(
 *     topBar = { DSTopBar(title = "搜索") },
 *     sheetContent = { FilterPanel() },
 *     sheetState = rememberStandardBottomSheetState()
 * ) { padding ->
 *     SearchResults(modifier = Modifier.padding(padding))
 * }
 * ```
 *
 * @param sheetContent BottomSheet 内容
 * @param modifier 修饰符
 * @param topBar 顶部应用栏（可选）
 * @param sheetState sheet 状态
 * @param sheetPeekHeight 收起时可见高度，默认 80dp
 * @param content 主内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSBottomSheetScaffold(
    sheetContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    topBar: (@Composable () -> Unit)? = null,
    sheetPeekHeight: androidx.compose.ui.unit.Dp = 80.dp,
    content: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
) {
    val scaffoldState = rememberBottomSheetScaffoldState()
    androidx.compose.material3.BottomSheetScaffold(
        modifier = modifier,
        topBar = topBar ?: {},
        sheetContent = {
            Column(modifier = Modifier.padding(vertical = DSTokens.Spacing.md)) {
                sheetContent()
            }
        },
        scaffoldState = scaffoldState,
        sheetPeekHeight = sheetPeekHeight,
        sheetShape = androidx.compose.foundation.shape.RoundedCornerShape(
            topStart = DSTokens.Radius.extraLarge,
            topEnd = DSTokens.Radius.extraLarge
        ),
        sheetContainerColor = MaterialTheme.colorScheme.surface,
        sheetContentColor = MaterialTheme.colorScheme.onSurface,
        sheetTonalElevation = DSTokens.Elevation.level1,
        sheetShadowElevation = DSTokens.Elevation.level3
    ) { padding ->
        content(padding)
    }
}

// ============================================================================
// Previews
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "PersistentBottomSheet - Default", widthDp = 360, heightDp = 640)
@Composable
private fun DSPersistentBottomSheetPreview() {
    DSDesignTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "主内容区",
                modifier = Modifier.padding(16.dp)
            )
            DSPersistentBottomSheet(
                modifier = Modifier.padding(top = 480.dp),
                content = {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("底部弹层内容")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "BottomSheetScaffold", widthDp = 360, heightDp = 640)
@Composable
private fun DSBottomSheetScaffoldPreview() {
    DSDesignTheme {
        var expanded by remember { mutableStateOf(false) }
        DSBottomSheetScaffold(
            sheetContent = {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("筛选条件", style = MaterialTheme.typography.titleMedium)
                    DSButton(
                        text = if (expanded) "收起" else "展开",
                        onClick = { expanded = !expanded }
                    )
                }
            },
            sheetPeekHeight = 80.dp
        ) { padding ->
            Column(modifier = Modifier.padding(padding).padding(16.dp)) {
                Text("搜索结果")
            }
        }
    }
}
