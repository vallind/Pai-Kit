// ============================================================================
// DSBottomSheet.kt
// 模态底部弹层
// ============================================================================

package com.pai.app.core.designsystem.overlays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextVariant

/**
 * DSBottomSheet - 模态底部弹层
 *
 * 使用示例：
 * ```kotlin
 * var showSheet by remember { mutableStateOf(false) }
 * // 外部可控的 SheetState（推荐：用于编程式展开/收起/锁定）
 * val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
 * if (showSheet) {
 *     DSBottomSheet(
 *         onDismiss = { showSheet = false },
 *         title = "选择城市",
 *         sheetState = sheetState
 *     ) {
 *         // 内容
 *     }
 * }
 * ```
 *
 * 设计规范：
 * - 顶部圆角 extraLarge (28dp)
 * - 顶部带 drag handle
 * - 背景半透明遮罩
 *
 * @param onDismiss 用户下滑/点遮罩/系统返回时的回调
 * @param modifier 修饰符
 * @param title 顶部标题（可选）
 * @param dragHandle 是否显示拖拽指示器，默认 true
 * @param sheetState 底层 [SheetState]，默认 `rememberModalBottomSheetState(skipPartiallyExpanded = true)`。
 *                   调用方可传入自己的 state 以编程式控制展开/隐藏/部分展开。
 * @param content 主体内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSBottomSheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    title: String? = null,
    dragHandle: Boolean = true,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    content: @Composable () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(
            topStart = DSTokens.Radius.extraLarge,
            topEnd = DSTokens.Radius.extraLarge
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = if (dragHandle) {
            { BottomSheetDefaults.DragHandle() }
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = DSTokens.Spacing.lg)
                .padding(bottom = DSTokens.Spacing.xxl)
        ) {
            if (title != null) {
                DSText(
                    text = title,
                    variant = DSTextVariant.TitleLarge
                )
                Spacer(Modifier.height(DSTokens.Spacing.lg))
            }
            content()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true, name = "Bottom Sheet")
@Composable
private fun DSBottomSheetPreview() {
    DSDesignTheme {
        DSBottomSheet(
            onDismiss = {},
            title = "预览标题"
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                DSText(text = "底部弹层内容", variant = DSTextVariant.BodyMedium)
                DSButton(text = "确认", onClick = {})
            }
        }
    }
}
