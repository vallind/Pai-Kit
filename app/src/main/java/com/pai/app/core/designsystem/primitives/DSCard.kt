// ============================================================================
// DSCard.kt
// 卡片组件 - 基于 M3 Card 扩展
// 提供：Filled / Outlined / Elevated 三种风格
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 卡片风格
 * - Filled: 填充式（默认）
 * - Outlined: 描边式（无阴影）
 * - Elevated: 高度式（带阴影）
 */
enum class DSCardStyle {
    Filled,
    Outlined,
    Elevated
}

/**
 * DSCard - 通用卡片
 *
 * 使用示例：
 * ```kotlin
 * DSCard(
 *     style = DSCardStyle.Elevated,
 *     onClick = { /* ... */ }
 * ) {
 *     Column(modifier = Modifier.padding(16.dp)) {
 *         DSText("标题", variant = DSTextVariant.TitleMedium)
 *         DSText("内容描述", variant = DSTextVariant.BodyMedium)
 *     }
 * }
 * ```
 *
 * 设计规范：
 * - 默认圆角 large (16dp)
 * - 默认内边距 16dp
 * - Elevated 默认 elevation level1
 *
 * @param modifier 修饰符
 * @param style 风格
 * @param onClick 点击回调（不传则不可点击）
 * @param containerColor 自定义背景色
 * @param contentPadding 内边距
 * @param elevation 高度
 * @param content 卡片内容
 */
@Composable
internal fun DSCard(
    modifier: Modifier = Modifier,
    style: DSCardStyle = DSCardStyle.Filled,
    onClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    contentPadding: PaddingValues = PaddingValues(DSTokens.Spacing.lg),
    elevation: androidx.compose.ui.unit.Dp = DSTokens.Elevation.level1,
    content: @Composable () -> Unit
) {
    val shape = androidx.compose.foundation.shape.RoundedCornerShape(DSTokens.Radius.large)
    val defaultContainerColor = when (style) {
        DSCardStyle.Filled -> MaterialTheme.colorScheme.surfaceVariant
        DSCardStyle.Outlined -> MaterialTheme.colorScheme.surface
        DSCardStyle.Elevated -> MaterialTheme.colorScheme.surface
    }

    val cardColors = CardDefaults.cardColors(
        containerColor = containerColor ?: defaultContainerColor,
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    val cardElevation: androidx.compose.material3.CardElevation = when (style) {
        DSCardStyle.Filled -> CardDefaults.cardElevation(defaultElevation = DSTokens.Elevation.level0)
        DSCardStyle.Outlined -> CardDefaults.cardElevation(defaultElevation = DSTokens.Elevation.level0)
        DSCardStyle.Elevated -> CardDefaults.cardElevation(defaultElevation = elevation)
    }

    if (onClick != null) {
        if (style == DSCardStyle.Outlined) {
            OutlinedCard(
                onClick = onClick,
                modifier = modifier,
                shape = shape,
                colors = cardColors,
                elevation = cardElevation,
                border = BorderStroke(DSTokens.Border.thin, MaterialTheme.colorScheme.outlineVariant),
                content = { androidx.compose.foundation.layout.Box(Modifier.padding(contentPadding)) { content() } }
            )
        } else {
            Card(
                onClick = onClick,
                modifier = modifier,
                shape = shape,
                colors = cardColors,
                elevation = cardElevation,
                content = { androidx.compose.foundation.layout.Box(Modifier.padding(contentPadding)) { content() } }
            )
        }
    } else {
        if (style == DSCardStyle.Outlined) {
            OutlinedCard(
                modifier = modifier,
                shape = shape,
                colors = cardColors,
                elevation = cardElevation,
                border = BorderStroke(DSTokens.Border.thin, MaterialTheme.colorScheme.outlineVariant),
                content = { androidx.compose.foundation.layout.Box(Modifier.padding(contentPadding)) { content() } }
            )
        } else {
            Card(
                modifier = modifier,
                shape = shape,
                colors = cardColors,
                elevation = cardElevation,
                content = { androidx.compose.foundation.layout.Box(Modifier.padding(contentPadding)) { content() } }
            )
        }
    }
}

@Preview(showBackground = true, name = "Card Styles")
@Composable
private fun DSCardPreview() {
    DSDesignTheme {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            DSCard(style = DSCardStyle.Filled) {
                com.pai.app.core.designsystem.primitives.DSText("Filled Card", variant = com.pai.app.core.designsystem.primitives.DSTextVariant.TitleMedium)
            }
            DSCard(style = DSCardStyle.Outlined) {
                com.pai.app.core.designsystem.primitives.DSText("Outlined Card", variant = com.pai.app.core.designsystem.primitives.DSTextVariant.TitleMedium)
            }
            DSCard(style = DSCardStyle.Elevated) {
                com.pai.app.core.designsystem.primitives.DSText("Elevated Card", variant = com.pai.app.core.designsystem.primitives.DSTextVariant.TitleMedium)
            }
        }
    }
}
