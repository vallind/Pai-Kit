// ============================================================================
// GalleryContainerPage.kt
// 容器组件展示页：Card / Chip / ListItem / Accordion / Banner / Dialog /
//                 BottomSheet / Carousel / Grid / Pagination
// ============================================================================
//
// M4 注：本文件含演示用硬编码 dp（如轮播高度 160.dp）—— 仅用于容器组件演示，
// 业务代码请用 DSTokens.Spacing / DSTokens.ComponentHeight.*。
// ============================================================================
@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
package com.pai.app.feature.gallery.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.primitives.DSAccordion
import com.pai.app.core.designsystem.primitives.DSAccordionSection
import com.pai.app.core.designsystem.patterns.DSBanner
import com.pai.app.core.designsystem.patterns.DSBannerType
import com.pai.app.core.designsystem.overlays.DSBottomSheet
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.primitives.DSCarousel
import com.pai.app.core.designsystem.overlays.DSChip
import com.pai.app.core.designsystem.overlays.DSChipStyle
import com.pai.app.core.designsystem.overlays.DSDialog
import com.pai.app.core.designsystem.overlays.DSDialogType
import com.pai.app.core.designsystem.primitives.DSGrid
import com.pai.app.core.designsystem.primitives.DSListItem
import com.pai.app.core.designsystem.primitives.DSListItemVariant
import com.pai.app.core.designsystem.patterns.DSPagination
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.shell.DSAppScaffold
import com.pai.app.core.designsystem.foundation.tokens.DSTokens

/**
 * GalleryContainerPage - 容器组件展示页
 *
 * 内容分十节：
 * 1. DSCard 三种风格：Filled / Outlined / Elevated
 * 2. DSChip 四种风格：Assist / Filter / Input / Suggestion
 * 3. DSListItem 1/2/3 行
 * 4. DSAccordion 折叠面板
 * 5. DSBanner 四种类型：Info / Warning / Error / Success
 * 6. DSDialog 触发按钮（弹出 Error/Warning/Success 三种）
 * 7. DSBottomSheet 触发按钮
 * 8. DSCarousel 轮播图（3 张占位卡）
 * 9. DSGrid 2 列网格
 * 10. DSPagination 分页器
 *
 * @param onBackClick 返回上一页回调
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun GalleryContainerPage(onBackClick: () -> Unit) {
    // 弹窗与底部弹层状态
    var showErrorDialog by remember { mutableStateOf(false) }
    var showWarningDialog by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }

    // 分页器状态
    var currentPage by remember { mutableIntStateOf(1) }

    // H15：用 DSAppScaffold 替代 material3.Scaffold
    DSAppScaffold(
        title = "容器 Container",
        showBackIcon = true,
        onBackClick = onBackClick,
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = DSTokens.Spacing.lg, vertical = DSTokens.Spacing.md),
            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.lg)
        ) {
            // 1. DSCard 三种风格
            SectionCard(title = "DSCard 三种风格") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Filled) {
                        DSText("Filled Card", variant = DSTextVariant.TitleMedium, color = DSTextColor.Primary)
                    }
                    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Outlined) {
                        DSText("Outlined Card", variant = DSTextVariant.TitleMedium, color = DSTextColor.Primary)
                    }
                    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Elevated) {
                        DSText("Elevated Card", variant = DSTextVariant.TitleMedium, color = DSTextColor.Primary)
                    }
                }
            }

            // 2. DSChip 四种风格
            SectionCard(title = "DSChip 四种风格") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
                    verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
                ) {
                    var filterSelected by remember { mutableStateOf(true) }
                    DSChip(
                        text = "Assist",
                        style = DSChipStyle.Assist,
                        leadingIcon = Icons.Default.FilterList,
                        onClick = {}
                    )
                    DSChip(
                        text = "Filter",
                        style = DSChipStyle.Filter,
                        selected = filterSelected,
                        onClick = { filterSelected = !filterSelected }
                    )
                    DSChip(
                        text = "Input",
                        style = DSChipStyle.Input,
                        onDismiss = {}
                    )
                    DSChip(
                        text = "Suggestion",
                        style = DSChipStyle.Suggestion,
                        leadingIcon = Icons.Default.LocalOffer,
                        onClick = {}
                    )
                }
            }

            // 3. DSListItem 1/2/3 行
            SectionCard(title = "DSListItem 1/2/3 行") {
                Column {
                    DSListItem(
                        title = "单行标题",
                        leadingIcon = Icons.Default.Notifications,
                        trailingIcon = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = {}
                    )
                    DSListItem(
                        title = "张三",
                        subtitle = "zhangsan@example.com",
                        leadingIcon = Icons.Default.AccountCircle,
                        variant = DSListItemVariant.TwoLine,
                        onClick = {}
                    )
                    DSListItem(
                        title = "Jetpack Compose 1.3.0",
                        subtitle = "新增 ListItem、SearchBar 稳定 API，建议升级",
                        overline = "新版本发布",
                        variant = DSListItemVariant.ThreeLine
                    )
                }
            }

            // 4. DSAccordion 折叠面板
            SectionCard(title = "DSAccordion 折叠面板") {
                DSAccordion(
                    sections = listOf(
                        DSAccordionSection(title = "个人信息") {
                            Column {
                                DSText("姓名：张三", variant = DSTextVariant.BodyMedium, color = DSTextColor.Primary)
                                DSText("邮箱：zhangsan@example.com", variant = DSTextVariant.BodyMedium, color = DSTextColor.Primary)
                                DSText("手机：13800138000", variant = DSTextVariant.BodyMedium, color = DSTextColor.Primary)
                            }
                        },
                        DSAccordionSection(title = "订单记录") {
                            DSText("订单号：12345 / 总价：¥199.00", variant = DSTextVariant.BodyMedium, color = DSTextColor.Primary)
                        },
                        DSAccordionSection(title = "售后服务") {
                            DSText("7 天无理由退换货 / 30 天质保", variant = DSTextVariant.BodyMedium, color = DSTextColor.Primary)
                        }
                    ),
                    initiallyExpandedIndex = 0
                )
            }

            // 5. DSBanner 四种类型
            SectionCard(title = "DSBanner 四种类型") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
                    DSBanner(
                        message = "新版本已发布，请更新以获取最新功能",
                        type = DSBannerType.Info,
                        actionText = "更新",
                        onActionClick = {},
                        onDismiss = {}
                    )
                    DSBanner(
                        message = "存储空间不足，请清理缓存",
                        type = DSBannerType.Warning,
                        actionText = "清理",
                        onActionClick = {},
                        onDismiss = {}
                    )
                    DSBanner(
                        message = "加载失败，请稍后重试",
                        type = DSBannerType.Error,
                        actionText = "重试",
                        onActionClick = {},
                        onDismiss = {}
                    )
                    DSBanner(
                        message = "保存成功",
                        type = DSBannerType.Success,
                        onDismiss = {}
                    )
                }
            }

            // 6. DSDialog 触发按钮
            SectionCard(title = "DSDialog 三种语义对话框") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DSButton(text = "Error 对话框", onClick = { showErrorDialog = true })
                    DSButton(text = "Warning 对话框", onClick = { showWarningDialog = true }, style = com.pai.app.core.designsystem.primitives.DSButtonStyle.Tonal)
                    DSButton(text = "Success 对话框", onClick = { showSuccessDialog = true }, style = com.pai.app.core.designsystem.primitives.DSButtonStyle.Outlined)
                }
            }

            // 7. DSBottomSheet 触发按钮
            SectionCard(title = "DSBottomSheet 底部弹层") {
                DSButton(text = "打开 BottomSheet", onClick = { showBottomSheet = true }, modifier = Modifier.fillMaxWidth())
            }

            // 8. DSCarousel 轮播图
            SectionCard(title = "DSCarousel 轮播图") {
                val pages = listOf("第一页", "第二页", "第三页")
                DSCarousel(
                    items = pages,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    itemContent = { page ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(DSTokens.Radius.large)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            DSText(
                                text = page,
                                variant = DSTextVariant.HeadlineSmall,
                                color = DSTextColor.OnPrimary
                            )
                        }
                    }
                )
            }

            // 9. DSGrid 2 列网格
            SectionCard(title = "DSGrid 2 列网格") {
                DSGrid(
                    columns = 2,
                    spacing = DSTokens.Spacing.md
                ) {
                    repeat(6) { index ->
                        DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Elevated) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)
                            ) {
                                DSText(
                                    text = "0${index + 1}",
                                    variant = DSTextVariant.HeadlineMedium,
                                    color = DSTextColor.Primary,
                                    textAlign = TextAlign.Center
                                )
                                DSText(
                                    text = "卡片 ${index + 1}",
                                    variant = DSTextVariant.LabelMedium,
                                    color = DSTextColor.Secondary
                                )
                            }
                        }
                    }
                }
            }

            // 10. DSPagination 分页器
            SectionCard(title = "DSPagination 分页器") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                    DSText(
                        text = "当前页：$currentPage",
                        variant = DSTextVariant.BodySmall,
                        color = DSTextColor.Secondary
                    )
                    DSPagination(
                        currentPage = currentPage,
                        totalPages = 20,
                        onPageChange = { currentPage = it }
                    )
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // 弹窗：三种语义
    // ------------------------------------------------------------------------
    if (showErrorDialog) {
        DSDialog(
            onDismiss = { showErrorDialog = false },
            title = "操作失败",
            message = "网络异常，请稍后重试。",
            type = DSDialogType.Error,
            confirmText = "重试",
            onConfirm = {},
            dismissText = "取消"
        )
    }
    if (showWarningDialog) {
        DSDialog(
            onDismiss = { showWarningDialog = false },
            title = "确认删除",
            message = "此操作不可撤销，确定要删除吗？",
            type = DSDialogType.Warning,
            confirmText = "删除",
            onConfirm = {},
            dismissText = "取消"
        )
    }
    if (showSuccessDialog) {
        DSDialog(
            onDismiss = { showSuccessDialog = false },
            title = "提交成功",
            message = "您的申请已成功提交，请等待审核。",
            type = DSDialogType.Success,
            confirmText = "知道了",
            onConfirm = {},
            dismissText = null
        )
    }

    // ------------------------------------------------------------------------
    // 底部弹层
    // ------------------------------------------------------------------------
    if (showBottomSheet) {
        DSBottomSheet(
            onDismiss = { showBottomSheet = false },
            title = "选择操作"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
                DSListItem(
                    title = "分享到微信",
                    leadingIcon = Icons.Default.Email,
                    onClick = { showBottomSheet = false }
                )
                DSListItem(
                    title = "复制链接",
                    leadingIcon = Icons.Default.Add,
                    onClick = { showBottomSheet = false }
                )
                DSListItem(
                    title = "搜索相似",
                    leadingIcon = Icons.Default.Search,
                    onClick = { showBottomSheet = false }
                )
                DSListItem(
                    title = "已读标记",
                    leadingIcon = Icons.Default.Check,
                    onClick = { showBottomSheet = false }
                )
            }
        }
    }
}
