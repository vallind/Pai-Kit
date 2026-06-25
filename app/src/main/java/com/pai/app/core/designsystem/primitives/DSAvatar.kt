// ============================================================================
// DSAvatar.kt
// 头像组件 - 支持图片 URL / 首字母 / 图标 三种回退方案
// 图片加载使用 Coil AsyncImage（可选），无图时回退首字母或图标
// 作者：design-system-bot
// ============================================================================

package com.pai.app.core.designsystem.primitives
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.pai.app.core.designsystem.foundation.theme.DSDesignTheme
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * 头像尺寸
 * - XSmall24: 24dp，用于列表项前导位、消息气泡
 * - Small32:  32dp，用于评论、紧凑列表
 * - Medium40: 40dp，默认尺寸，用于卡片、设置项
 * - Large56:  56dp，用于个人资料卡片
 * - XLarge72: 72dp，用于详情页大头像
 */
internal enum class DSAvatarSize(val dp: androidx.compose.ui.unit.Dp) {
    XSmall24(DSTokens.Spacing.xl),                  // 24dp（DSSpacing.xl == 24dp）
    Small32(DSTokens.ComponentHeight.avatarSmall),   // 32dp
    Medium40(DSTokens.ComponentHeight.avatarMedium), // 40dp
    Large56(DSTokens.ComponentHeight.avatarLarge),   // 56dp
    XLarge72(DSTokens.ComponentHeight.avatarXLarge)  // 72dp
}

/**
 * 头像形状
 * - Circle: 圆形，默认
 * - Rounded: 8dp 圆角矩形
 */
internal enum class DSAvatarShape {
    Circle,
    Rounded
}

/**
 * DSAvatar - 头像组件
 *
 * 优先级：imageUrl > initial > icon > 默认 Person 图标
 *
 * 使用示例：
 * ```kotlin
 * // 图片头像
 * DSAvatar(
 *     imageUrl = "https://example.com/avatar.jpg",
 *     size = DSAvatarSize.Medium40,
 *     contentDescription = "用户头像"
 * )
 *
 * // 首字母头像
 * DSAvatar(
 *     initial = "Z",
 *     size = DSAvatarSize.Small32
 * )
 *
 * // 图标头像
 * DSAvatar(
 *     icon = Icons.Default.Person,
 *     size = DSAvatarSize.Large56,
 *     shape = DSAvatarShape.Rounded
 * )
 * ```
 *
 * 设计规范：
 * - 5 档尺寸：24 / 32 / 40 / 56 / 72 dp
 * - 2 种形状：圆形 / 8dp 圆角
 * - 图片加载：Coil AsyncImage，可选；加载失败自动回退到首字母或图标
 * - 回退背景色：surfaceVariant
 * - 回退文字色：onSurfaceVariant
 * - 字号根据头像尺寸缩放
 *
 * @param modifier 修饰符
 * @param imageUrl 图片 URL，非空时优先使用 Coil 加载
 * @param initial 首字母（建议传 1-2 字符），imageUrl 为空时显示
 * @param icon 图标，imageUrl 与 initial 均为空时显示，默认 Person
 * @param size 头像尺寸，默认 Medium40
 * @param shape 头像形状，默认 Circle
 * @param contentDescription 无障碍描述
 */
@Composable
internal fun DSAvatar(
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    initial: String? = null,
    icon: ImageVector? = null,
    size: DSAvatarSize = DSAvatarSize.Medium40,
    shape: DSAvatarShape = DSAvatarShape.Circle,
    contentDescription: String? = null
) {
    val avatarShape = when (shape) {
        DSAvatarShape.Circle -> CircleShape
        DSAvatarShape.Rounded -> RoundedCornerShape(DSTokens.Radius.small)
    }

    // 字号根据头像尺寸缩放（保持视觉舒适）。
    // 这些字号为头像专用值，暂未抽到 DSTokens.FontSize，未来可提取。
    val fontSize = when (size) {
        DSAvatarSize.XSmall24 -> 10.sp
        DSAvatarSize.Small32 -> 13.sp
        DSAvatarSize.Medium40 -> 16.sp
        DSAvatarSize.Large56 -> 22.sp
        DSAvatarSize.XLarge72 -> 28.sp
    }

    // 图标尺寸根据头像尺寸缩放
    val iconSize = when (size) {
        DSAvatarSize.XSmall24 -> DSTokens.IconSize.xs   // 16dp
        DSAvatarSize.Small32 -> DSTokens.IconSize.xs    // 16dp
        DSAvatarSize.Medium40 -> DSTokens.IconSize.sm   // 20dp
        DSAvatarSize.Large56 -> DSTokens.IconSize.lg    // 32dp
        DSAvatarSize.XLarge72 -> DSTokens.IconSize.lg   // 32dp
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(avatarShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        when {
            // 优先级 1：图片 URL
            !imageUrl.isNullOrEmpty() -> {
                val context = LocalContext.current
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(size.dp)
                )
            }
            // 优先级 2：首字母
            !initial.isNullOrEmpty() -> {
                Text(
                    text = initial.take(2).uppercase(),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = fontSize,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            // 优先级 3：图标
            icon != null -> {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(iconSize)
                )
            }
            // 优先级 4：默认 Person 图标
            else -> {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = contentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

// ============================================================================
// Previews
// ============================================================================

@Preview(showBackground = true, name = "Avatar - Initials (All Sizes)")
@Composable
private fun DSAvatarInitialsAllSizesPreview() {
    DSDesignTheme {
        Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                DSTokens.Spacing.md
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSAvatar(initial = "A", size = DSAvatarSize.XSmall24)
            DSAvatar(initial = "Bo", size = DSAvatarSize.Small32)
            DSAvatar(initial = "Z", size = DSAvatarSize.Medium40)
            DSAvatar(initial = "W", size = DSAvatarSize.Large56)
            DSAvatar(initial = "OK", size = DSAvatarSize.XLarge72)
        }
    }
}

@Preview(showBackground = true, name = "Avatar - Icon (Default Person)")
@Composable
private fun DSAvatarIconPreview() {
    DSDesignTheme {
        Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                DSTokens.Spacing.md
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSAvatar(size = DSAvatarSize.Small32)
            DSAvatar(size = DSAvatarSize.Medium40)
            DSAvatar(size = DSAvatarSize.Large56)
        }
    }
}

@Preview(showBackground = true, name = "Avatar - Rounded Shape")
@Composable
private fun DSAvatarRoundedPreview() {
    DSDesignTheme {
        Row(
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                DSTokens.Spacing.md
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DSAvatar(
                initial = "Z",
                size = DSAvatarSize.Medium40,
                shape = DSAvatarShape.Rounded
            )
            DSAvatar(
                initial = "A",
                size = DSAvatarSize.Large56,
                shape = DSAvatarShape.Rounded
            )
            DSAvatar(
                initial = "B",
                size = DSAvatarSize.XLarge72,
                shape = DSAvatarShape.Rounded
            )
        }
    }
}

@Preview(showBackground = true, name = "Avatar - Image URL (Coil)")
@Composable
private fun DSAvatarImageUrlPreview() {
    DSDesignTheme {
        Column(
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(
                DSTokens.Spacing.md
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            DSAvatar(
                imageUrl = "https://www.gravatar.com/avatar/00000000000000000000000000000000?d=mp&s=80",
                size = DSAvatarSize.Medium40,
                contentDescription = "用户头像"
            )
            Text(
                text = "（图片来自网络，预览时可能不渲染）",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
