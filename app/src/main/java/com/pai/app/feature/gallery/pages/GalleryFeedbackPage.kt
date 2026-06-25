// ============================================================================
// GalleryFeedbackPage.kt
// 反馈组件展示页：Progress / LoadingOverlay / Skeleton / EmptyState /
//                 Badge / Avatar / Tag / Snackbar
// ============================================================================
//
// M4 注：本文件含演示用硬编码 dp（如进度圈 32/48/64dp、骨架屏 120/80dp）——
// 仅用于反馈组件演示，业务代码请用 DSTokens.ComponentHeight.* / DSTokens.Spacing.*。
// ============================================================================
package com.pai.app.feature.gallery.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.primitives.DSAvatar
import com.pai.app.core.designsystem.primitives.DSAvatarShape
import com.pai.app.core.designsystem.primitives.DSAvatarSize
import com.pai.app.core.designsystem.primitives.DSBadge
import com.pai.app.core.designsystem.primitives.DSBadgePosition
import com.pai.app.core.designsystem.primitives.DSBadgeType
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.primitives.DSIcon
import com.pai.app.core.designsystem.primitives.DSIconSize
import com.pai.app.core.designsystem.primitives.DSIconTint
import com.pai.app.core.designsystem.primitives.DSCircularProgress
import com.pai.app.core.designsystem.patterns.DSEmptyState
import com.pai.app.core.designsystem.primitives.DSLinearProgress
import com.pai.app.core.designsystem.patterns.DSFullScreenLoading
import com.pai.app.core.designsystem.primitives.DSSkeletonBlock
import com.pai.app.core.designsystem.overlays.DSSnackbar
import com.pai.app.core.designsystem.primitives.DSTag
import com.pai.app.core.designsystem.primitives.DSTagColor
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.shell.DSAppScaffold
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * GalleryFeedbackPage - 反馈组件展示页
 *
 * 内容分九节：
 * 1. DSLinearProgress 确定进度 0.7 + 不确定进度
 * 2. DSCircularProgress 确定 + 不确定（3 种尺寸）
 * 3. DSFullScreenLoading(withScrim=true) 触发按钮（点击显示遮罩 3 秒）
 * 4. DSSkeletonBlock 列表骨架屏（3 行）
 * 5. DSEmptyState 含图标 + 标题 + 描述 + 操作按钮
 * 6. DSBadge 三种类型：Number=99+ / Dot / Text
 * 7. DSAvatar 5 种尺寸 + 两种形状
 * 8. DSTag 7 种颜色
 * 9. DSSnackbar 触发按钮（使用 SnackbarHostState）
 *
 * @param onBackClick 返回上一页回调
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GalleryFeedbackPage(onBackClick: () -> Unit) {
    // 加载遮罩状态
    var showOverlay by remember { mutableStateOf(false) }
    // Snackbar 状态：DSAppScaffold 接收该实例用于内部 SnackbarHost 渲染
    // （SnackbarHostState 是状态持有者，非视觉组件，detekt 允许 import）
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // 显示遮罩 3 秒后自动消失
    LaunchedEffect(showOverlay) {
        if (showOverlay) {
            delay(3000L)
            showOverlay = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // H15：用 DSAppScaffold 替代 material3.Scaffold，snackbarHost 交由 DSAppScaffold 内部渲染
        DSAppScaffold(
            title = "反馈 Feedback",
            showBackIcon = true,
            onBackClick = onBackClick,
            snackbarHostState = snackbarHostState,
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = DSTokens.Spacing.lg, vertical = DSTokens.Spacing.md),
                verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg)
            ) {
                // 1. DSLinearProgress
                SectionCard(title = "DSLinearProgress 线性进度") {
                    Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                        DSText(
                            text = "确定进度 0.7",
                            variant = DSTextVariant.LabelMedium,
                            color = DSTextColor.Secondary
                        )
                        DSLinearProgress(progress = 0.7f)

                        DSText(
                            text = "不确定进度",
                            variant = DSTextVariant.LabelMedium,
                            color = DSTextColor.Secondary
                        )
                        DSLinearProgress(progress = null)
                    }
                }

                // 2. DSCircularProgress 确定 + 不确定（3 种尺寸）
                SectionCard(title = "DSCircularProgress 圆形进度") {
                    Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DSCircularProgress(progress = 0.7f, size = 32.dp)
                            DSCircularProgress(progress = 0.7f, size = 48.dp, strokeWidth = 4.dp)
                            DSCircularProgress(progress = 0.7f, size = 64.dp, strokeWidth = 6.dp)
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DSCircularProgress(progress = null, size = 32.dp)
                            DSCircularProgress(progress = null, size = 48.dp, strokeWidth = 4.dp)
                            DSCircularProgress(progress = null, size = 64.dp, strokeWidth = 6.dp)
                        }
                    }
                }

                // 3. DSFullScreenLoading(withScrim=true) 触发按钮
                SectionCard(title = "DSFullScreenLoading 加载遮罩") {
                    DSButton(
                        text = "显示加载遮罩 3 秒",
                        onClick = { showOverlay = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 4. DSSkeletonBlock 列表骨架屏（3 行）
                SectionCard(title = "DSSkeletonBlock 列表骨架屏") {
                    Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                        repeat(3) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)
                            ) {
                                DSSkeletonBlock(
                                    modifier = Modifier.size(DSTokens.ComponentHeight.buttonLarge),
                                    shape = CircleShape
                                )
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)
                                ) {
                                    DSSkeletonBlock(
                                        modifier = Modifier
                                            .width(120.dp)
                                            .height(16.dp)
                                    )
                                    DSSkeletonBlock(
                                        modifier = Modifier
                                            .width(80.dp)
                                            .height(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 5. DSEmptyState
                SectionCard(title = "DSEmptyState 空状态") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(DSTokens.Radius.medium)
                            )
                            .padding(vertical = DSTokens.Spacing.lg),
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

                // 6. DSBadge 三种类型
                SectionCard(title = "DSBadge 三种类型") {
                    Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DSBadge(
                                count = 99,
                                type = DSBadgeType.Number,
                                position = DSBadgePosition.TopEnd
                            ) {
                                DSCircularProgress(progress = null, size = DSTokens.IconSize.lg)
                            }
                            DSBadge(
                                count = 120,
                                type = DSBadgeType.Number,
                                position = DSBadgePosition.TopEnd
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(DSTokens.IconSize.lg)
                                        .background(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(DSTokens.Radius.small)
                                        )
                                )
                            }
                            DSBadge(
                                type = DSBadgeType.Dot,
                                position = DSBadgePosition.TopEnd
                            ) {
                                // H15：用 DSIcon 替代全限定名 material3.Icon
                                DSIcon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "通知",
                                    size = DSIconSize.Large,
                                    tint = DSIconTint.Default,
                                )
                            }
                            DSBadge(
                                text = "New",
                                type = DSBadgeType.Text,
                                position = DSBadgePosition.TopEnd
                            ) {
                                // H15：用 DSIcon 替代全限定名 material3.Icon
                                DSIcon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "邮件",
                                    size = DSIconSize.Large,
                                    tint = DSIconTint.Default,
                                )
                            }
                        }
                    }
                }

                // 7. DSAvatar 5 种尺寸 + 两种形状
                SectionCard(title = "DSAvatar 5 种尺寸 + 两种形状") {
                    Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                        // 圆形 5 种尺寸
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DSAvatar(initial = "A", size = DSAvatarSize.XSmall24, shape = DSAvatarShape.Circle)
                            DSAvatar(initial = "Bo", size = DSAvatarSize.Small32, shape = DSAvatarShape.Circle)
                            DSAvatar(initial = "Z", size = DSAvatarSize.Medium40, shape = DSAvatarShape.Circle)
                            DSAvatar(initial = "W", size = DSAvatarSize.Large56, shape = DSAvatarShape.Circle)
                            DSAvatar(initial = "OK", size = DSAvatarSize.XLarge72, shape = DSAvatarShape.Circle)
                        }
                        // 圆角矩形 5 种尺寸
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            DSAvatar(initial = "A", size = DSAvatarSize.XSmall24, shape = DSAvatarShape.Rounded)
                            DSAvatar(initial = "Bo", size = DSAvatarSize.Small32, shape = DSAvatarShape.Rounded)
                            DSAvatar(initial = "Z", size = DSAvatarSize.Medium40, shape = DSAvatarShape.Rounded)
                            DSAvatar(initial = "W", size = DSAvatarSize.Large56, shape = DSAvatarShape.Rounded)
                            DSAvatar(initial = "OK", size = DSAvatarSize.XLarge72, shape = DSAvatarShape.Rounded)
                        }
                    }
                }

                // 8. DSTag 7 种颜色
                SectionCard(title = "DSTag 7 种颜色") {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
                        verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
                    ) {
                        DSTag(text = "默认", color = DSTagColor.Default)
                        DSTag(text = "主色", color = DSTagColor.Primary)
                        DSTag(text = "成功", color = DSTagColor.Success, icon = Icons.Default.Check)
                        DSTag(text = "警告", color = DSTagColor.Warning)
                        DSTag(text = "错误", color = DSTagColor.Error, icon = Icons.Default.LocalFireDepartment)
                        DSTag(text = "信息", color = DSTagColor.Info)
                        DSTag(text = "自定义", color = DSTagColor.Custom, customColor = Color(0xFF8B5CF6))
                    }
                }

                // 9. DSSnackbar 触发按钮
                SectionCard(title = "DSSnackbar 通知条") {
                    Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
                        DSButton(
                            text = "显示普通 Snackbar",
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "这是一条普通提示",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            }
                        )
                        DSButton(
                            text = "显示带操作按钮 Snackbar",
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "保存成功",
                                        actionLabel = "查看",
                                        duration = SnackbarDuration.Long
                                    )
                                }
                            },
                            style = com.pai.app.core.designsystem.primitives.DSButtonStyle.Tonal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(DSTokens.Spacing.xxl))
            }
        }

        // 加载遮罩层：覆盖整个 Scaffold 区域
        if (showOverlay) {
            DSFullScreenLoading(message = "加载中...", withScrim = true)
        }
    }
}
