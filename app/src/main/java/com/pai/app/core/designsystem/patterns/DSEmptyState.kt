// ============================================================================
// DSEmptyState.kt
// 空状态占位 - 居中布局：图标 + 标题 + 描述 + 可选按钮
// 适用于列表空、搜索无结果、网络错误等场景
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.patterns

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSButtonStyle

/**
 * DSEmptyState - 空状态占位
 *
 * 使用示例：
 * ```kotlin
 * // 列表空
 * DSEmptyState(
 *     icon = Icons.Default.Inbox,
 *     title = "暂无数据",
 *     description = "下拉刷新或点击按钮重新加载",
 *     actionText = "重新加载",
 *     onActionClick = { refresh() }
 * )
 *
 * // 搜索无结果（无按钮）
 * DSEmptyState(
 *     icon = Icons.Default.SearchOff,
 *     title = "未找到相关结果",
 *     description = "尝试更换关键词"
 * )
 * ```
 *
 * 设计规范：
 * - 居中垂直布局，子元素水平居中
 * - 图标尺寸 64dp，颜色 onSurfaceVariant（次要色），可省略
 * - 标题 titleMedium + SemiBold + onSurface
 * - 描述 bodyMedium + onSurfaceVariant（次要色），可省略
 * - 操作按钮使用 DSButton（filled），仅 actionText 与 onActionClick 同时非空时显示
 * - 元素间距 md (16dp)
 * - 文案超长时居中对齐换行（TextAlign.Center）
 *
 * @param modifier 修饰符
 * @param icon 可选图标（64dp），为 null 时不显示
 * @param title 标题文案
 * @param description 可选描述文案
 * @param actionText 可选操作按钮文案
 * @param onActionClick 操作按钮点击回调；actionText 与本参数同时非空时才显示按钮
 */
@Composable
internal fun DSEmptyState(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    title: String,
    description: String? = null,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.padding(DSTokens.Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
    ) {
        // --------------------------------------------------------------------
        // 图标（64dp）
        // --------------------------------------------------------------------
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(64.dp)
            )
        }

        // --------------------------------------------------------------------
        // 标题
        // --------------------------------------------------------------------
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        // --------------------------------------------------------------------
        // 描述
        // --------------------------------------------------------------------
        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        // --------------------------------------------------------------------
        // 操作按钮
        // --------------------------------------------------------------------
        if (actionText != null && onActionClick != null) {
            DSButton(
                text = actionText,
                onClick = onActionClick,
                style = DSButtonStyle.Filled
            )
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "DSEmptyState - 列表为空")
@Composable
private fun DSEmptyStateListEmptyPreview() {
    DSDesignTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DSEmptyState(
                icon = Icons.Default.Inbox,
                title = "暂无数据",
                description = "点击下方按钮刷新数据",
                actionText = "重新加载",
                onActionClick = {}
            )
        }
    }
}

@Preview(showBackground = true, name = "DSEmptyState - 搜索无结果")
@Composable
private fun DSEmptyStateSearchNoResultPreview() {
    DSDesignTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DSEmptyState(
                icon = Icons.Default.SearchOff,
                title = "未找到相关结果",
                description = "尝试更换关键词或调整筛选条件"
            )
        }
    }
}

@Preview(showBackground = true, name = "DSEmptyState - 网络错误")
@Composable
private fun DSEmptyStateNetworkErrorPreview() {
    DSDesignTheme {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            DSEmptyState(
                icon = Icons.Default.WifiOff,
                title = "网络连接失败",
                description = "请检查网络设置后重试",
                actionText = "重试",
                onActionClick = {}
            )
        }
    }
}
