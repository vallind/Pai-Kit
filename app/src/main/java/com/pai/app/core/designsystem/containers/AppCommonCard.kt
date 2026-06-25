// ============================================================================
// AppCommonCard.kt
// 通用业务卡 - 基于 DSCard 封装的「业务容器壳」
// 提供业务常见的卡片结构：标题 + 副标题 + 头部操作 + 内容 slot + 底部操作行
// 与 DSCard 的区别：
//   DSCard 是纯容器（surface + elevation + 圆角），无内置结构
//   AppCommonCard 内置业务常见结构，调用方只需传内容 slot
// ============================================================================

package com.pai.app.core.designsystem.containers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant

/**
 * AppCommonCard - 通用业务卡
 *
 * 业务最常见的卡片结构，内置 5 个 slot：
 * 1. 标题（title，必填）
 * 2. 副标题（subtitle，可选）
 * 3. 头部右侧操作（headerAction，可选，如「更多」按钮）
 * 4. 内容（content，必填 slot）
 * 5. 底部操作行（footerActions，可选，如「确定/取消」按钮）
 *
 * 与 [DSCard] 的区别：
 * - DSCard：纯容器，只有 surface + elevation + 圆角，调用方自己组织内容
 * - AppCommonCard：内置「标题 + 副标题 + 操作」结构，调用方只填内容 slot
 *
 * 使用场景：
 * - 设置项分组卡（标题=分组名，内容=设置项列表）
 * - 信息展示卡（标题=字段名，内容=字段值）
 * - 表单分组卡（标题=分组名，内容=表单字段，底部=提交按钮）
 * - 列表区域卡（标题=区域名，头部=「查看全部」，内容=列表）
 *
 * 使用示例：
 * ```kotlin
 * AppCommonCard(
 *     title = "基本信息",
 *     subtitle = "用户档案核心字段",
 *     headerActionIcon = Icons.Default.Edit,
 *     onHeaderActionClick = { /* 编辑 */ }
 * ) {
 *     Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
 *         InfoRow(label = "姓名", value = user.name)
 *         InfoRow(label = "邮箱", value = user.email)
 *         InfoRow(label = "手机", value = user.phone)
 *     }
 * }
 *
 * // 表单分组卡（带底部操作）
 * AppCommonCard(
 *     title = "收货地址",
 *     footerActions = {
 *         DSButton(text = "取消", onClick = { /* ... */ }, style = DSButtonStyle.Text)
 *         DSButton(text = "保存", onClick = { /* ... */ })
 *     }
 * ) {
 *     DSTextField(value = address, onValueChange = { /* ... */ }, label = "详细地址")
 * }
 * ```
 *
 * 设计规范：
 * - 基于 DSCardStyle.Elevated（默认）或可配置
 * - 标题 TitleMedium + SemiBold + OnSurface
 * - 副标题 BodySmall + OnSurfaceVariant
 * - 内容 slot 默认 padding lg
 * - 底部操作行右对齐，水平间距 md
 * - 整卡可点击（onClick 非 null 时）
 *
 * @param title 标题（必填）
 * @param modifier 修饰符
 * @param subtitle 副标题（可选）
 * @param headerActionIcon 头部右侧操作图标（可选，如 MoreVert / Edit / Share）
 * @param headerActionContentDescription 头部操作无障碍描述
 * @param onHeaderActionClick 头部操作点击回调
 * @param onClick 整卡点击回调（可选，设为非 null 时整卡可点击）
 * @param style 卡片风格，默认 Elevated
 * @param footerActions 底部操作行 slot（可选，通常放 DSButton 行）
 * @param content 主内容 slot
 */
@Composable
fun AppCommonCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    headerActionIcon: ImageVector? = null,
    headerActionContentDescription: String? = null,
    onHeaderActionClick: (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    style: DSCardStyle = DSCardStyle.Elevated,
    footerActions: (@Composable RowScope.() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    DSCard(
        modifier = modifier.fillMaxWidth(),
        style = style,
        onClick = onClick
    ) {
        Column {
            // ----------------------------------------------------------------
            // Header: 标题 + 副标题 + 头部操作
            // ----------------------------------------------------------------
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = DSTokens.Spacing.lg,
                        end = if (headerActionIcon != null) DSTokens.Spacing.xs else DSTokens.Spacing.lg,
                        top = DSTokens.Spacing.md,
                        bottom = if (subtitle != null) DSTokens.Spacing.xs else DSTokens.Spacing.sm
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    DSText(
                        text = title,
                        variant = DSTextVariant.TitleMedium,
                        color = DSTextColor.OnSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (subtitle != null) {
                        DSText(
                            text = subtitle,
                            variant = DSTextVariant.BodySmall,
                            color = DSTextColor.Secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (headerActionIcon != null && onHeaderActionClick != null) {
                    IconButton(onClick = onHeaderActionClick) {
                        Icon(
                            imageVector = headerActionIcon,
                            contentDescription = headerActionContentDescription,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // ----------------------------------------------------------------
            // Content
            // ----------------------------------------------------------------
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = DSTokens.Spacing.lg)
            ) {
                content()
            }

            // ----------------------------------------------------------------
            // Footer Actions
            // ----------------------------------------------------------------
            if (footerActions != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            horizontal = DSTokens.Spacing.lg,
                            vertical = DSTokens.Spacing.md
                        ),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    footerActions()
                }
            } else {
                // 无 footer 时补底部 padding 保持视觉平衡
                Box(modifier = Modifier.padding(bottom = DSTokens.Spacing.md))
            }
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "AppCommonCard - 标准用法")
@Composable
private fun AppCommonCardBasicPreview() {
    DSDesignTheme {
        AppCommonCard(
            title = "基本信息",
            subtitle = "用户档案核心字段"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                DSText("姓名：张三", variant = DSTextVariant.BodyMedium)
                DSText("邮箱：zhangsan@example.com", variant = DSTextVariant.BodyMedium)
                DSText("手机：138****8888", variant = DSTextVariant.BodyMedium)
            }
        }
    }
}

@Preview(showBackground = true, name = "AppCommonCard - 带头部操作")
@Composable
private fun AppCommonCardWithHeaderActionPreview() {
    DSDesignTheme {
        AppCommonCard(
            title = "订单 #20260625001",
            subtitle = "2026-06-25 14:30",
            headerActionIcon = Icons.Default.MoreVert,
            headerActionContentDescription = "更多操作",
            onHeaderActionClick = {}
        ) {
            DSText("订单内容...", variant = DSTextVariant.BodyMedium)
        }
    }
}

@Preview(showBackground = true, name = "AppCommonCard - 带底部操作")
@Composable
private fun AppCommonCardWithFooterPreview() {
    DSDesignTheme {
        AppCommonCard(
            title = "收货地址",
            footerActions = {
                com.pai.app.core.designsystem.primitives.DSButton(
                    text = "取消",
                    onClick = {},
                    style = com.pai.app.core.designsystem.primitives.DSButtonStyle.Text
                )
                com.pai.app.core.designsystem.primitives.DSButton(
                    text = "保存",
                    onClick = {}
                )
            }
        ) {
            DSText("地址表单...", variant = DSTextVariant.BodyMedium)
        }
    }
}

@Preview(showBackground = true, name = "AppCommonCard - Outlined 风格")
@Composable
private fun AppCommonCardOutlinedPreview() {
    DSDesignTheme {
        AppCommonCard(
            title = "通知设置",
            style = DSCardStyle.Outlined
        ) {
            DSText("通知偏好设置内容", variant = DSTextVariant.BodyMedium)
        }
    }
}
