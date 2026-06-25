// ============================================================================
// GalleryMotionPage.kt
// 动效展示页：pressScale / listItemEnterAnimation / DSMotionScheme 进退场 /
//            DSLottieAnimation / DSBottomBar(animated=true) 对比
// ============================================================================
package com.pai.app.feature.gallery.pages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pai.app.core.designsystem.foundation.motion.currentDSMotionScheme
import com.pai.app.core.designsystem.foundation.motion.listItemEnterAnimation
import com.pai.app.core.designsystem.foundation.motion.pressScale
import com.pai.app.core.designsystem.foundation.motion.DSLottieAnimation
import com.pai.app.core.designsystem.foundation.tokens.DSNavItem
import com.pai.app.core.designsystem.primitives.DSButton
import com.pai.app.core.designsystem.primitives.DSButtonStyle
import com.pai.app.core.designsystem.primitives.DSCard
import com.pai.app.core.designsystem.primitives.DSCardStyle
import com.pai.app.core.designsystem.shell.DSBottomBar
import com.pai.app.core.designsystem.primitives.DSText
import com.pai.app.core.designsystem.primitives.DSTextColor
import com.pai.app.core.designsystem.primitives.DSTextVariant
import com.pai.app.core.designsystem.shell.DSAppScaffold
import com.pai.app.core.designsystem.foundation.tokens.DSTokens
import com.airbnb.lottie.compose.LottieConstants

/**
 * GalleryMotionPage - 动效展示页
 *
 * 内容分六节：
 * 1. pressScale 按压缩放（DSButton + DSCard 加 pressScale 修饰符）
 * 2. listItemEnterAnimation 列表项错峰进场（LazyColumn 10 项）
 * 3. DSMotionScheme 进退场动画切换演示（fadeSlideUp / fadeScale）
 * 4. DSLottieAnimation 加载 lottie/loading.json
 * 5. DSLottieAnimation 加载 lottie/empty.json
 * 6. 页面转场说明 + DSBottomBar(animated=true) 与标准 DSBottomBar 对比
 *
 * @param onBackClick 返回上一页回调
 */
@Composable
internal fun GalleryMotionPage(onBackClick: () -> Unit) {
    DSAppScaffold(
        title = "动效 Motion",
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
            // 1. pressScale 按压缩放
            SectionCard(title = "pressScale 按压缩放") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                    DSText(
                        text = "按下元素时缩小到 0.95，松开后回弹到 1.0，150ms emphasized 缓动",
                        variant = DSTextVariant.LabelMedium,
                        color = DSTextColor.Secondary
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DSButton(
                            text = "按下我",
                            onClick = {},
                            icon = Icons.Default.Add,
                            modifier = Modifier.pressScale()
                        )
                        DSButton(
                            text = "Tonal",
                            onClick = {},
                            style = DSButtonStyle.Tonal,
                            modifier = Modifier.pressScale()
                        )
                    }

                    DSCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pressScale(),
                        style = DSCardStyle.Elevated,
                        onClick = {}
                    ) {
                        DSText(
                            text = "点击卡片体验缩放反馈",
                            variant = DSTextVariant.BodyMedium,
                            color = DSTextColor.Primary
                        )
                    }
                }
            }

            // 2. listItemEnterAnimation 列表项错峰进场
            SectionCard(title = "listItemEnterAnimation 列表项错峰进场") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)) {
                    DSText(
                        text = "首次进入列表时，每项延迟 50ms 错峰进场，300ms emphasizedDecelerate 缓动",
                        variant = DSTextVariant.LabelMedium,
                        color = DSTextColor.Secondary
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(360.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm)
                        ) {
                            itemsIndexed(List(10) { "列表项 ${it + 1}" }) { index, item ->
                                DSCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .listItemEnterAnimation(index),
                                    style = DSCardStyle.Outlined
                                ) {
                                    DSText(
                                        text = item,
                                        variant = DSTextVariant.BodyLarge,
                                        color = DSTextColor.Primary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. DSMotionScheme 进退场动画切换演示
            SectionCard(title = "DSMotionScheme enter/exit 动画") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                    var slideVisible by remember { mutableStateOf(true) }
                    var scaleVisible by remember { mutableStateOf(true) }
                    val motion = currentDSMotionScheme()

                    DSButton(
                        text = if (slideVisible) "隐藏 fadeSlideUp" else "显示 fadeSlideUp",
                        onClick = { slideVisible = !slideVisible },
                        modifier = Modifier.fillMaxWidth()
                    )
                    AnimatedVisibility(
                        visible = slideVisible,
                        enter = motion.enter().fadeSlideUp(),
                        exit = motion.exit().fadeSlideDown()
                    ) {
                        DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Filled) {
                            DSText(
                                text = "淡入淡出 + 上下滑入滑出",
                                variant = DSTextVariant.BodyMedium,
                                color = DSTextColor.Primary
                            )
                        }
                    }

                    DSButton(
                        text = if (scaleVisible) "隐藏 fadeScale" else "显示 fadeScale",
                        onClick = { scaleVisible = !scaleVisible },
                        modifier = Modifier.fillMaxWidth(),
                        style = DSButtonStyle.Tonal
                    )
                    AnimatedVisibility(
                        visible = scaleVisible,
                        enter = motion.enter().fadeScale(),
                        exit = motion.exit().fadeScale()
                    ) {
                        DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Elevated) {
                            DSText(
                                text = "淡入淡出 + 缩放进场（弹出感）",
                                variant = DSTextVariant.BodyMedium,
                                color = DSTextColor.Primary
                            )
                        }
                    }
                }
            }

            // 4. DSLottieAnimation 加载 loading.json
            SectionCard(title = "DSLottieAnimation - loading.json") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DSLottieAnimation(
                        assetName = "lottie/loading.json",
                        size = 120.dp,
                        iterations = LottieConstants.IterateForever
                    )
                    DSText(
                        text = "loading.json 无限循环加载动画",
                        variant = DSTextVariant.LabelMedium,
                        color = DSTextColor.Secondary
                    )
                }
            }

            // 5. DSLottieAnimation 加载 empty.json
            SectionCard(title = "DSLottieAnimation - empty.json") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.sm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    DSLottieAnimation(
                        assetName = "lottie/empty.json",
                        size = 120.dp,
                        iterations = LottieConstants.IterateForever
                    )
                    DSText(
                        text = "empty.json 空状态动画",
                        variant = DSTextVariant.LabelMedium,
                        color = DSTextColor.Secondary
                    )
                }
            }

            // 6. 页面转场说明 + DSBottomBar animated 对比
            SectionCard(title = "页面转场 + DSBottomBar animated 对比") {
                Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.md)) {
                    // 页面转场说明
                    DSCard(modifier = Modifier.fillMaxWidth(), style = DSCardStyle.Filled) {
                        Column(verticalArrangement = Arrangement.spacedBy(DSTokens.Spacing.xs)) {
                            DSText(
                                text = "页面转场（已在 NavHost 全局应用）",
                                variant = DSTextVariant.TitleSmall,
                                color = DSTextColor.Primary
                            )
                            DSText(
                                text = "前向导航：从右侧滑入\n后向导航：从左侧滑入\n基于 DSMotionScheme.pageTransitions，emphasized 缓动 350ms",
                                variant = DSTextVariant.BodySmall,
                                color = DSTextColor.Secondary
                            )
                        }
                    }

                    // 标准 DSBottomBar
                    DSText(
                        text = "标准 DSBottomBar（animated = false）",
                        variant = DSTextVariant.LabelMedium,
                        color = DSTextColor.Secondary
                    )
                    DSBottomBar(
                        items = listOf(
                            DSNavItem(label = "首页", icon = Icons.Default.Home),
                            DSNavItem(label = "发现", icon = Icons.Default.Search),
                            DSNavItem(label = "收藏", icon = Icons.Default.Favorite),
                            DSNavItem(label = "我的", icon = Icons.Default.Person)
                        ),
                        selectedItem = "首页",
                        onItemSelected = {}
                    )

                    // DSBottomBar animated=true
                    DSText(
                        text = "DSBottomBar（animated = true，点击切换选中项）",
                        variant = DSTextVariant.LabelMedium,
                        color = DSTextColor.Secondary
                    )
                    var animatedIndex by remember { mutableIntStateOf(0) }
                    val labels = listOf("首页", "发现", "收藏", "我的")
                    val icons = listOf(
                        Icons.Default.Home,
                        Icons.Default.Search,
                        Icons.Default.Favorite,
                        Icons.Default.Person
                    )
                    DSBottomBar(
                        items = labels.indices.map { i ->
                            DSNavItem(label = labels[i], icon = icons[i])
                        },
                        selectedItem = labels[animatedIndex],
                        onItemSelected = { id -> animatedIndex = labels.indexOf(id) },
                        animated = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(DSTokens.Spacing.xxl))
        }
    }
}
