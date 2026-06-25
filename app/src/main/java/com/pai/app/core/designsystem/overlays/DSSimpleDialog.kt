// ============================================================================
// DSSimpleDialog.kt + DSFullScreenDialog.kt
// M3 卓越线补齐：完整对话框家族（AlertDialog 已有 + SimpleDialog + FullScreenDialog）
// - DSSimpleDialog: 简单选择对话框，列表/网格选项，无操作按钮
// - DSFullScreenDialog: 全屏对话框，用户自定义内容 + TopAppBar，常用于详情/编辑场景
// ============================================================================

package com.pai.app.core.designsystem.overlays

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSButtonStyle
import com.pai.app.core.designsystem.shell.DSTopBar
import com.pai.app.core.designsystem.shell.DSTopBarAction
import com.pai.app.core.designsystem.shell.DSTopBarStyle

/**
 * DSSimpleDialog 选项数据
 *
 * @param title 选项标题（必填）
 * @param subtitle 副标题（可选）
 * @param icon 前置图标（可选）
 * @param id 用于回调的标识符，默认 = title
 */
internal data class DSSimpleDialogOption(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector? = null,
    val id: String = title
)

/**
 * DSSimpleDialog - 简单选择对话框
 *
 * 列出若干选项供用户选择，选择后立即关闭对话框并回调。
 * 与 [DSDialog]（AlertDialog）的区别：
 * - AlertDialog：标题/内容/确认取消按钮，用于"提示确认"场景
 * - SimpleDialog：列表选项，无按钮，用于"快速选择"场景
 *
 * 使用示例：
 * ```kotlin
 * var show by remember { mutableStateOf(false) }
 * if (show) {
 *     DSSimpleDialog(
 *         title = "选择语言",
 *         options = listOf(
 *             DSSimpleDialogOption("简体中文", subtitle = "zh-CN"),
 *             DSSimpleDialogOption("English", subtitle = "en-US")
 *         ),
 *         selectedOptionId = "简体中文",
 *         onOptionSelected = { id -> /* ... */ show = false }
 *     )
 * }
 * ```
 *
 * @param title 对话框标题
 * @param options 选项列表
 * @param onOptionSelected 选项被点击的回调，参数为 [DSSimpleDialogOption.id]
 * @param modifier 修饰符
 * @param selectedOptionId 当前选中项的 id（用于显示选中态）
 * @param onDismissRequest 点击外部 / 返回键关闭回调
 * @param icon 顶部图标（可选）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSSimpleDialog(
    title: String,
    options: List<DSSimpleDialogOption>,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedOptionId: String? = null,
    onDismissRequest: () -> Unit = {},
    icon: ImageVector? = null
) {
    BasicAlertDialog(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .width(360.dp)
                .padding(horizontal = DSTokens.Spacing.lg),
            shape = AlertDialogDefaults.shape,
            color = AlertDialogDefaults.containerColor,
            tonalElevation = AlertDialogDefaults.TonalElevation,
            shadowElevation = DSTokens.Elevation.level3
        ) {
            Column(
                modifier = Modifier.padding(DSTokens.Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)
            ) {
                // 标题行（图标 + 标题）
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
                ) {
                    if (icon != null) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(DSTokens.IconSize.md)
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(DSTokens.Spacing.xs))

                // 选项列表
                options.forEach { option ->
                    val isSelected = option.id == selectedOptionId
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .semantics { role = Role.RadioButton }
                            .padding(vertical = DSTokens.Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
                    ) {
                        // 选中态指示器
                        Box(modifier = Modifier.size(DSTokens.IconSize.md)) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        // 前置图标（可选）
                        if (option.icon != null) {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(DSTokens.IconSize.sm)
                            )
                        }
                        // 标题 + 副标题
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.title,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (option.subtitle != null) {
                                Text(
                                    text = option.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// DSFullScreenDialog - 全屏对话框
// ============================================================================

/**
 * DSFullScreenDialog - 全屏对话框
 *
 * 覆盖整个屏幕的对话框，常用于：
 * - 详情页（点击列表项进入详情）
 * - 编辑表单（点击「编辑」进入全屏编辑）
 * - 多步骤流程（向导）
 *
 * 与 [DSSimpleDialog] / [DSDialog] 的区别：
 * - 简单对话框：信息量少，居中浮窗
 * - 全屏对话框：信息量大，独立页面，自带 TopAppBar
 *
 * 使用示例：
 * ```kotlin
 * if (show) {
 *     DSFullScreenDialog(
 *         title = "编辑资料",
 *         onSaveClick = { vm.save(); show = false },
 *         onCloseClick = { show = false }
 *     ) {
 *         // 自定义内容
 *         EditProfileForm(...)
 *     }
 * }
 * ```
 *
 * @param title 顶部标题栏文字
 * @param onCloseClick 关闭回调（返回键 / 顶部 × 按钮）
 * @param content 用户自定义内容
 * @param modifier 修饰符
 * @param onSaveClick 可选保存按钮回调（非空时显示「保存」文字按钮）
 * @param saveText 保存按钮文案，默认"保存"
 * @param closeText 关闭按钮无障碍描述，默认"关闭"
 * @param topBarStyle 顶部栏样式，默认 Small
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DSFullScreenDialog(
    title: String,
    onCloseClick: () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onSaveClick: (() -> Unit)? = null,
    saveText: String = "保存",
    closeText: String = "关闭",
    topBarStyle: DSTopBarStyle = DSTopBarStyle.Small
) {
    androidx.compose.ui.window.Dialog(
        onDismissRequest = onCloseClick,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                DSTopBar(
                    title = title,
                    style = topBarStyle,
                    onBackClick = onCloseClick,
                    actions = buildList {
                        if (onSaveClick != null) {
                            add(DSTopBarAction(icon = Icons.Default.Check, contentDescription = saveText, onClick = onSaveClick))
                        }
                    }
                )
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                }
            }
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "SimpleDialog - Default")
@Composable
private fun DSSimpleDialogPreview() {
    DSDesignTheme {
        DSSimpleDialog(
            title = "选择语言",
            options = listOf(
                DSSimpleDialogOption("简体中文", subtitle = "zh-CN"),
                DSSimpleDialogOption("English", subtitle = "en-US"),
                DSSimpleDialogOption("日本語", subtitle = "ja-JP")
            ),
            selectedOptionId = "简体中文",
            onOptionSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "SimpleDialog - With Icon", widthDp = 360)
@Composable
private fun DSSimpleDialogWithIconPreview() {
    DSDesignTheme {
        DSSimpleDialog(
            title = "分享到",
            icon = Icons.Default.Share,
            options = listOf(
                DSSimpleDialogOption(
                    title = "微信",
                    subtitle = "发送给好友",
                    icon = Icons.Default.Chat
                ),
                DSSimpleDialogOption(
                    title = "微博",
                    icon = Icons.Default.Public
                ),
                DSSimpleDialogOption(
                    title = "复制链接",
                    icon = Icons.Default.Link
                )
            ),
            onOptionSelected = {}
        )
    }
}

@Preview(showBackground = true, name = "FullScreenDialog - Default", widthDp = 360, heightDp = 640)
@Composable
private fun DSFullScreenDialogPreview() {
    DSDesignTheme {
        DSFullScreenDialog(
            title = "编辑资料",
            onCloseClick = {},
            onSaveClick = {},
            content = {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "这里是用户自定义的编辑表单内容",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        )
    }
}
