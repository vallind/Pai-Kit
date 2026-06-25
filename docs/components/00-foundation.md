# Foundation 基础层

> 文档版本：v1.0 · 2026-06-25  
> 适用范围：`core/designsystem/foundation/**`  
> 定位：构建组件的「素材」，不是组件本身。业务一般不直接用，由 DS 组件内部消费。

---

## 目录

1. [设计哲学](#1-设计哲学)
2. [Tokens 设计令牌](#2-tokens-设计令牌)
3. [Theme 主题系统](#3-theme-主题系统)
4. [Motion 动效系统](#4-motion-动效系统)
5. [A11y 无障碍](#5-a11y-无障碍)
6. [Layout 响应式布局](#6-layout-响应式布局)
7. [Perf 性能工具](#7-perf-性能工具)
8. [Preview 预览规范](#8-preview-预览规范)
9. [Util 通用扩展](#9-util-通用扩展)
10. [UiState 状态机范式](#10-uistate-状态机范式)

---

## 1. 设计哲学

Foundation 层是 DS 的「地基」，与业务完全解耦。所有上层组件（Atom/Molecule/Layout/Container）都基于 Foundation 构建，业务代码一般不直接 import Foundation（除非需要读取主题状态或自定义动效）。

**核心原则**：

| 原则 | 说明 |
|---|---|
| **单一真相源** | 所有颜色/间距/圆角/字号都从 Token 读取，禁止业务硬编码 `16.dp` / `Color(0xFF4F46E5)` |
| **可注入** | Theme / Motion 通过 CompositionLocal 注入，支持运行时切换（深色 / 高对比度 / 减少动效） |
| **零业务依赖** | Foundation 不 import 业务层，业务可独立替换 Foundation |
| **可测试** | 所有 Token 都是纯数据，Theme/Motion 都是数据类，可单元测试 |

---

## 2. Tokens 设计令牌

> 文件位置：`foundation/tokens/`

### 2.1 总入口 `DSTokens`

```kotlin
object DSTokens {
    object Spacing      // 间距（11 档）
    object Radius       // 圆角（7 档）
    object Elevation    // 高度（8 级）
    object FontSize     // 字号（15 种 M3 Typography）
    object Duration     // 动效时长（11 档）
    object Easing       // 缓动曲线（7 种）
    object Brand        // 品牌色（5 套 × 10 步 = 50 色）
    object Border       // 描边宽度（4 档）
    object Alpha        // 透明度（4 档）
    object IconSize     // 图标尺寸（5 档）
    object ComponentHeight  // 组件高度（16 种）
    val minTouchTarget: Dp  // 48dp 最小触控目标
}
```

### 2.2 间距 Spacing

```kotlin
DSTokens.Spacing.none     // 0dp
DSTokens.Spacing.xxs      // 2dp
DSTokens.Spacing.xs       // 4dp
DSTokens.Spacing.sm       // 8dp
DSTokens.Spacing.md       // 12dp
DSTokens.Spacing.lg       // 16dp
DSTokens.Spacing.xl       // 24dp
DSTokens.Spacing.xxl      // 32dp
DSTokens.Spacing.xxxl     // 48dp
DSTokens.Spacing.huge     // 64dp
DSTokens.Spacing.giant    // 96dp
```

**使用规范**：所有 padding / spacedBy 都从 Spacing 读取，禁止 `padding(16.dp)` 这类硬编码。

### 2.3 圆角 Radius

```kotlin
DSTokens.Radius.none         // 0dp
DSTokens.Radius.extraSmall   // 4dp（小元素：Badge / Tag）
DSTokens.Radius.small        // 8dp（按钮 / Chip）
DSTokens.Radius.medium       // 12dp（Card / TextField）
DSTokens.Radius.large        // 16dp（FAB / 大卡片）
DSTokens.Radius.extraLarge   // 28dp（Dialog / BottomSheet）
DSTokens.Radius.full         // 999dp（胶囊 / 圆形）
```

### 2.4 高度 Elevation

```kotlin
DSTokens.Elevation.level0    // 0dp（平面）
DSTokens.Elevation.level1    // 1dp（Card 默认）
DSTokens.Elevation.level2    // 2dp（Card 悬停）
DSTokens.Elevation.level3    // 3dp（NavigationBar / Menu）
DSTokens.Elevation.level4    // 4dp（BottomBar / Dialog）
DSTokens.Elevation.level6    // 6dp（BottomSheet / DatePicker）
DSTokens.Elevation.level8    // 8dp（FAB）
DSTokens.Elevation.level12   // 12dp（FAB 悬停 / 模态弹窗）
```

### 2.5 字号 FontSize

15 种 M3 Typography：Display / Headline / Title / Body / Label × Large / Medium / Small

```kotlin
DSTokens.FontSize.displayLarge    // 57sp
DSTokens.FontSize.headlineSmall   // 24sp
DSTokens.FontSize.titleMedium     // 16sp
DSTokens.FontSize.bodyMedium      // 14sp
DSTokens.FontSize.labelSmall      // 11sp
// ... 共 15 种
```

### 2.6 品牌色 Brand

5 套品牌色板 × 10 步色阶 = 50 个色值：

```kotlin
DSTokens.Brand.indigo600      // 主色 #4F46E5
DSTokens.Brand.emerald500     // 成功色
DSTokens.Brand.rose600        // 错误色
DSTokens.Brand.amber500       // 警告色
DSTokens.Brand.sky500         // 信息色
DSTokens.Brand.slate900       // 中性深色
// ...
```

**枚举选择**：`DSBrandColor { Indigo, Emerald, Rose, Amber, Sky }`

### 2.7 动效 Duration + Easing

```kotlin
DSTokens.Duration.small2      // 150ms（按压反馈）
DSTokens.Duration.medium2     // 300ms（标准过渡）
DSTokens.Duration.medium3     // 350ms（页面转场）

DSTokens.Easing.emphasized                    // 默认自然减速
DSTokens.Easing.emphasizedDecelerate          // 进场
DSTokens.Easing.emphasizedAccelerate          // 退场
```

### 2.8 组件尺寸 ComponentHeight / IconSize / Border / Alpha

```kotlin
DSTokens.ComponentHeight.buttonSmall     // 32dp
DSTokens.ComponentHeight.buttonMedium    // 40dp
DSTokens.ComponentHeight.buttonLarge     // 48dp
DSTokens.ComponentHeight.topBar          // 64dp
DSTokens.ComponentHeight.bottomBar       // 80dp

DSTokens.IconSize.xs / sm / md / lg / xl    // 12 / 16 / 24 / 32 / 48 dp

DSTokens.Border.hairline / thin / medium / thick    // 0.5 / 1 / 2 / 4 dp

DSTokens.Alpha.overlay              // 0.4（遮罩）
DSTokens.Alpha.disabledContainer    // 0.12
DSTokens.Alpha.disabledContent      // 0.38
DSTokens.Alpha.loadingScrim         // 0.4
```

### 2.9 统一数据类

| API | 文件 | 说明 |
|---|---|---|
| `DSNavItem(label, icon, selectedIcon, badgeText, id)` | DSNavItem.kt | 统一导航项数据（5 个导航组件共用） |
| `DSMessageType { Info, Success, Warning, Error }` | DSMessageType.kt | 统一消息类型（Snackbar/Banner/Dialog 共用）+ 扩展属性 `icon` / `containerColor` / `contentColor` / `containerColorLight` / `contentColorOnLight` |
| `DSBrandColor` enum | BrandColorPalette.kt | 5 套品牌色板 |
| `DSFontSizeScale` enum | FontSizeScale.kt | 4 档字号缩放（0.85 / 1.0 / 1.15 / 1.3） |
| `ShapeScale` object | RadiusTokens.kt | EXTRA_SMALL / SMALL / MEDIUM / LARGE / EXTRA_LARGE / FULL 常量 |

---

## 3. Theme 主题系统

> 文件位置：`foundation/theme/`

### 3.1 DSDesignTheme — 主题入口

```kotlin
@Composable
fun DSDesignTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    brandColor: DSBrandColor = DSBrandColor.Indigo,
    fontSizeScale: DSFontSizeScale = DSFontSizeScale.Normal,
    amoled: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit
)
```

**4 档主题模式**：

| 模式 | 触发方式 | 用途 |
|---|---|---|
| Light | `darkTheme = false` | 默认浅色 |
| Dark | `darkTheme = true` | 标准深色 |
| AMOLED | `darkTheme = true, amoled = true` | 纯黑（省电） |
| HighContrast | `highContrast = true` | WCAG AAA 无障碍 |

**优先级**：`highContrast` > `dynamicColor` > `brandColor`

### 3.2 ColorScheme 工厂

| API | 说明 |
|---|---|
| `buildLightColorScheme(brand)` | 标准浅色 |
| `buildDarkColorScheme(brand, amoled)` | 标准深色 / AMOLED |
| `buildHighContrastLightColorScheme(brand)` | 高对比度浅色（WCAG AAA 7:1） |
| `buildHighContrastDarkColorScheme(brand)` | 高对比度深色 |

### 3.3 扩展语义色 ExtendedColors

M3 ColorScheme 没有 success / warning / info / skeleton 等业务常用色，通过 ExtendedColors 扩展：

```kotlin
data class ExtendedColors(
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val info: Color,
    val onInfo: Color,
    val border: Color,
    val borderDark: Color,
    val skeletonBase: Color,
    val skeletonHighlight: Color
)

// 读取方式
val successColor = MaterialTheme.extendedColors.success
```

### 3.4 Typography + Shapes

```kotlin
internal val DSTypography: Typography  // 15 种 M3 文字样式
internal val DSShapes: Shapes           // 5 级 M3 形状

// 常用快捷 Shape
internal val DSShapePill            // 胶囊形（Chip / Avatar / FAB）
internal val DSShapeTopMedium       // 顶部 medium 圆角（BottomSheet / Card 顶部）
internal val DSShapeTopExtraLarge   // 顶部 extraLarge 圆角（BottomSheet 弹出）
```

### 3.5 组件级主题覆盖 DSComponentOverrides

允许局部子树覆盖组件样式（如「让 HeroCard 内的 Button 用 tertiary 色而不是 primary」）：

```kotlin
// 6 种组件覆盖
data class DSButtonOverride(containerColor, contentColor, elevation, cornerRadius, ...)
data class DSCardOverride(containerColor, contentColor, elevation, cornerRadius, borderColor, borderWidth)
data class DSTopBarOverride(containerColor, titleContentColor, navigationIconColor, actionIconColor, height)
data class DSBottomBarOverride(containerColor, contentColor, selectedItemColor, unselectedItemColor, height)
data class DSTextFieldOverride(containerColor, textColor, focusedBorderColor, unfocusedBorderColor, errorBorderColor, cornerRadius)
data class DSSFabOverride(containerColor, contentColor, size, shape, elevation)

// 入口
@Composable fun DSComponentThemeOverride(overrides: DSComponentOverrides, content)
@Composable fun DSButtonThemeOverride(override: DSButtonOverride, content)
@Composable fun DSCardThemeOverride(override: DSCardOverride, content)
@Composable fun DSTopBarThemeOverride(override: DSTopBarOverride, content)

// 读取
@Composable fun currentButtonOverride(): DSButtonOverride?
@Composable fun currentCardOverride(): DSCardOverride?
// ... 共 6 个
```

### 3.6 主题调试面板 DSThemeDebugPanel

开发模式下实时切换主题：

```kotlin
data class DSThemeDebugState(
    val darkTheme: Boolean = false,
    val dynamicColor: Boolean = false,
    val brandColor: DSBrandColor = DSBrandColor.Indigo,
    val fontSizeScale: DSFontSizeScale = DSFontSizeScale.Normal,
    val amoled: Boolean = false,
    val highContrast: Boolean = false
)

@Composable
fun DSThemeDebugPanel(
    state: DSThemeDebugState,
    onStateChange: (DSThemeDebugState) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true   // 生产构建传 false
)
```

### 3.7 CompositionLocal 速查

| Local | 默认值 | 注入位置 |
|---|---|---|
| `LocalFontSizeScale` | `DSFontSizeScale.Normal` | DSDesignTheme |
| `LocalExtendedColors` | `LightExtendedColors` | DSDesignTheme |
| `LocalDSComponentOverrides` | `DSComponentOverrides.Empty` | DSComponentThemeOverride |

---

## 4. Motion 动效系统

> 文件位置：`foundation/motion/`

### 4.1 DSMotionScheme — 统一动效方案

```kotlin
@Stable
class DSMotionScheme(
    val duration: DSMotionDuration,        // 11 档时长
    val easing: DSMotionEasing,            // 7 种缓动
    val pressScale: DSPressScale,          // 按压反馈参数
    val pageTransitions: DSPageTransitions // NavHost 转场
) {
    fun enter(): DSEnterMotion        // 6 种进场动画工厂
    fun exit(): DSExitMotion          // 6 种退场动画工厂
    fun fadeContentTransform()        // Fade 内容转换
    fun slideContentTransform()       // Slide 内容转换
    fun scaleContentTransform()       // Scale 内容转换
}

// 读取
@Composable fun currentDSMotionScheme(): DSMotionScheme

// 注入
@Composable fun provideDSMotionScheme(scheme: DSMotionScheme, content)

// 无障碍减少动效
fun reducedMotionScheme(): DSMotionScheme
```

### 4.2 使用示例

```kotlin
// 进退场动画
val motion = currentDSMotionScheme()
AnimatedVisibility(
    visible = isVisible,
    enter = motion.enter().fadeSlideUp(),
    exit = motion.exit().fadeSlideDown()
)

// NavHost 转场
NavHost(
    enterTransition = { motion.pageTransitions.enterTransition() },
    exitTransition = { motion.pageTransitions.exitTransition() }
)

// 内容转换
AnimatedContent(targetState = state, transitionSpec = { motion.fadeContentTransform() })

// 按压反馈
Modifier.pressScale()  // 内部自动读 motion.pressScale

// 减少动效模式（无障碍）
provideDSMotionScheme(scheme = reducedMotionScheme()) {
    AppContent()
}
```

### 4.3 Spring 物理动画

```kotlin
object DSSpring {
    fun <T> medium()    // 中等弹性（按压回弹）
    fun <T> low()       // 低弹性（平滑过渡）
    fun <T> noBouncy()  // 无弹性（自然过渡）
    fun <T> high()      // 高弹性（玩味场景）
}
```

### 4.4 共享元素转场 DSSharedTransition

```kotlin
val LocalSharedTransitionScope: CompositionLocalOf<SharedTransitionScope?>

@Composable
fun DSSharedTransitionProvider(content: @Composable SharedTransitionScope.() -> Unit)

// Modifier 扩展
fun Modifier.dsSharedElement(key, sharedScope, animatedVisibilityScope, modifier)
fun Modifier.dsSharedBounds(key, sharedScope, animatedVisibilityScope, modifier)

// 配套常量
val DSDefaultBoundsTransform: BoundsTransform
val DSSharedEnterTransition: EnterTransition
val DSSharedExitTransition: ExitTransition
```

⚠️ 调用方未包裹 `DSSharedTransitionProvider` 时，`dsSharedElement` / `dsSharedBounds` 会通过 Timber 打印警告并 no-op。

### 4.5 辅助动画工具

| API | 文件 | 说明 |
|---|---|---|
| `Modifier.pressScale(pressedScale)` | DSAnimatedPressScale.kt | 按压缩放反馈 |
| `Modifier.pressScaleWith(interactionSource, pressedScale)` | DSAnimatedPressScale.kt | 复用 InteractionSource 版本 |
| `Modifier.listItemEnterAnimation(index, delayMillis)` | DSListItemAnimation.kt | LazyColumn 列表项错峰进场 |
| `DSLottieAnimation(assetName, size, iterations)` | DSLottieAnimation.kt | Lottie 动画播放器 |
| `DSLottieLoading(assetName)` | DSLottieAnimation.kt | Lottie 加载占位 |

---

## 5. A11y 无障碍

> 文件位置：`foundation/a11y/`

### 5.1 触控与语义

```kotlin
// 最小触控目标（视觉尺寸不变，扩大点击区域到 48dp × 48dp）
internal fun Modifier.minTouchTarget(minSize: Int = 48): Modifier

// 语义描述（TalkBack 朗读）
internal fun Modifier.a11yDescription(description: String, role: Role? = null): Modifier

// 状态描述（TalkBack 优先朗读）
internal fun Modifier.a11yState(description: String, state: String): Modifier

// 强制最小视觉尺寸 48dp × 48dp
internal fun Modifier.a11yMinSize(): Modifier
```

### 5.2 M3 状态层 DSStateLayer

M3 标准 hover/focus/pressed 状态层（透明色叠加）：

```kotlin
object DSStateLayerAlpha {
    const val Hover: Float    // 0.08
    const val Focus: Float    // 0.12
    const val Pressed: Float  // 0.12
    const val Dragged: Float  // 0.16
}

// 完整版：自动响应 interactionSource 的 hover/focus/pressed
fun Modifier.dsStateLayer(color, interactionSource, pressedDelay = 0): Modifier

// 简化版：手动传状态
fun Modifier.dsStateLayerSimple(color, isHovered, isFocused, isPressed): Modifier

// M3 ripple
@Composable fun dsRipple(color: Color = Color.Unspecified): Indication
```

---

## 6. Layout 响应式布局

> 文件位置：`foundation/layout/`

### 6.1 DSWindowSizeClass

```kotlin
enum class DSWidthSizeClass { Compact, Medium, Expanded }
enum class DSHeightSizeClass { Compact, Medium, Expanded }

@Stable
data class DSWindowSizeClass(
    val widthSizeClass: DSWidthSizeClass,
    val heightSizeClass: DSHeightSizeClass,
    val widthDp: Dp,
    val heightDp: Dp
) {
    val isCompact: Boolean              // 手机竖屏
    val isTablet: Boolean               // 平板/折叠屏
    val isExpanded: Boolean             // 大屏
    val isLandscape: Boolean            // 横屏
    val recommendedGridColumns: Int     // 1 / 2 / 3
    val recommendedContentMaxWidth: Dp  // 大屏内容最大宽度
    val useBottomBar: Boolean           // Compact → true，Medium+ → false
    
    fun <T> select(compact: T, medium: T, expanded: T): T
}

@Composable fun rememberDSWindowSizeClass(): DSWindowSizeClass
@Composable fun provideDSWindowSizeClass(content)
val LocalDSWindowSizeClass: CompositionLocalOf<DSWindowSizeClass>
```

### 6.2 响应式布局容器

```kotlin
@Composable
fun DSResponsiveLayout(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal,
    verticalArrangement: Arrangement.Vertical,
    content: @Composable () -> Unit
)
// Compact → Column，Medium/Expanded → Row
```

### 6.3 断点规范

| WidthSizeClass | 断点 | 设备示例 | 推荐导航 |
|---|---|---|---|
| Compact | < 600dp | 手机竖屏 | DSBottomBar |
| Medium | 600~840dp | 手机横屏 / 小平板 | DSNavigationRail |
| Expanded | > 840dp | 平板 / 折叠屏 / 桌面 | DSNavigationRail + DSPermanentNavigationDrawer |

---

## 7. Perf 性能工具

> 文件位置：`foundation/perf/`

### 7.1 重组跟踪

```kotlin
// 跟踪组件重组次数（超阈值告警）
@Composable
fun DSRecompositionTracker(tag: String, threshold: Int = 3, content)

// 跟踪参数变化导致重组
@Composable
fun DSRecompositionReason(tag: String, vararg keys: Any?)

// 列表项重组跟踪
@Composable
fun DSListItemTracker(itemId: Any, threshold: Int = 2, content)

// Modifier 重组跟踪
fun Modifier.trackModifier(tag: String): Modifier
```

### 7.2 防抖

```kotlin
@Composable
fun <T> rememberDebounced(delayMs: Long = 300L, block: (T) -> Unit): (T) -> Unit
```

### 7.3 Stable 标记

```kotlin
@Stable
interface DSStableMarker  // data class 实现此接口强制标注 Stable
```

### 7.4 稳定性包装类

Compose 在判断含 `List<T>` / `Map<K, V>` 字段的 data class 时会认为不稳定（因为 List/Map 是接口），导致不必要的重组。用以下包装类强制稳定：

```kotlin
@Immutable
data class DSStableList<T>(val items: List<T>) : Iterable<T> by items {
    val size: Int get() = items.size
    fun isEmpty() / isNotEmpty() / get(index) / contains(element) / indexOf(element)
}

@Immutable
data class DSStableMap<K, V>(val entries: Map<K, V>) {
    val size / keys / values
    fun isEmpty() / isNotEmpty() / get(key) / containsKey(key) / containsValue(value)
}

@Stable
data class DSStableText(val text: String) {
    val length / fun isEmpty() / isNotEmpty()
    override fun toString(): String = text
}

@Immutable
interface DSStableUiState<out T> {
    val data: T?
    val isLoading: Boolean
    val error: String?
}
```

**使用示例**：

```kotlin
@Immutable
data class MyUiState(
    val items: DSStableList<Item> = DSStableList(emptyList()),  // ✅ 稳定
    // val items: List<Item> = emptyList()  // ❌ 不稳定
    val count: Int = 0
) : DSStableUiState<List<Item>> {
    override val data: List<Item>? get() = items.items
    override val isLoading: Boolean = false
    override val error: String? = null
}
```

---

## 8. Preview 预览规范

> 文件位置：`foundation/preview/DSPreviewScenes.kt`

### 8.1 强制规范

**所有 DS 组件 Preview 必须用 `DSPreviewScenes` 工具包装，禁止裸 `@Preview` 不带主题。**

每个组件至少 3 个 Preview 场景：
- **Light** — 亮色模式 + 默认品牌
- **Dark** — 暗色模式 + 默认品牌
- **Brand** — 亮色 + 指定品牌（覆盖 5 套品牌色之一）

### 8.2 DSPreviewScenes API

```kotlin
object DSPreviewScenes {
    // 标准三件套
    @Composable fun Light(brand: DSBrandColor = DSBrandColor.Indigo, content)
    @Composable fun Dark(brand: DSBrandColor = DSBrandColor.Indigo, content)
    @Composable fun Brand(brand: DSBrandColor, content)
    
    // 高级场景
    @Composable fun Amoled(content)                         // AMOLED 纯黑
    @Composable fun HighContrast(darkTheme: Boolean, content) // 高对比度无障碍
    @Composable fun DynamicColor(darkTheme: Boolean, content) // 动态配色
    
    // 批量对比
    @Composable fun AllBrands(content)    // 5 套品牌对比
    @Composable fun AllThemes(content)    // Light + Dark + AMOLED + HighContrast 对比
}
```

### 8.3 使用示例

```kotlin
// ✅ 正确：用 DSPreviewScenes 包装
@Preview(showBackground = true, name = "Button - Light")
@Composable
private fun ButtonLightPreview() {
    DSPreviewScenes.Light {
        DSButton("Submit", onClick = {})
    }
}

@Preview(showBackground = true, name = "Button - Dark")
@Composable
private fun ButtonDarkPreview() {
    DSPreviewScenes.Dark {
        DSButton("Submit", onClick = {})
    }
}

@Preview(showBackground = true, name = "Button - Emerald Brand")
@Composable
private fun ButtonEmeraldPreview() {
    DSPreviewScenes.Brand(DSBrandColor.Emerald) {
        DSButton("Submit", onClick = {})
    }
}

// ❌ 错误：裸 @Preview 不带主题
@Preview
@Composable
private fun ButtonPreview() {
    DSButton("Submit", onClick = {})  // 没有 DSDesignTheme 包裹
}
```

### 8.4 跨品牌视觉回归

需要验证组件在所有品牌色板下的视觉一致性时：

```kotlin
@Preview(showBackground = true, name = "Button - All Brands")
@Composable
private fun ButtonAllBrandsPreview() {
    DSPreviewScenes.AllBrands {
        DSButton("Submit", onClick = {})
    }
}
```

### 8.5 跨主题视觉回归

需要验证组件在所有主题模式下的视觉一致性时：

```kotlin
@Preview(showBackground = true, name = "Button - All Themes")
@Composable
private fun ButtonAllThemesPreview() {
    DSPreviewScenes.AllThemes {
        DSButton("Submit", onClick = {})
    }
}
```

### 8.6 旧组件迁移策略

- **新组件**：强制遵守 3 场景规范，禁止裸 @Preview
- **旧组件**：逐步迁移，每次修改时补齐 PreviewScenes 包装
- **CI 检查**：未来可通过 Konsist 架构测试自动检测裸 @Preview

---

## 9. Util 通用扩展

> 文件位置：`foundation/util/DSExtensions.kt`

### 9.1 Color 扩展

```kotlin
// 转 #RRGGBBAA 或 #RRGGBB 十六进制字符串
fun Color.toHex(includeAlpha: Boolean = true): String

// 调整透明度（等价 copy(alpha = alpha)）
fun Color.withAlpha(alpha: Float): Color

// 颜色按比例混合（线性插值）
fun Color.blend(target: Color, fraction: Float): Color

// 判断颜色是否为深色（WCAG 相对亮度）
fun Color.isDark(): Boolean

// 计算与另一颜色的对比度比（WCAG，返回 [1, 21]）
fun Color.contrastRatio(other: Color): Float
```

**使用示例**：

```kotlin
// 调试日志
Timber.d("当前色：${MaterialTheme.colorScheme.primary.toHex()}")

// 半透明遮罩
val overlayColor = MaterialTheme.colorScheme.primary.withAlpha(0.4f)

// 自动选择文字颜色
val textColor = if (backgroundColor.isDark()) Color.White else Color.Black

// WCAG 对比度检查
val ratio = textColor.contrastRatio(backgroundColor)
if (ratio < 4.5f) Timber.w("对比度 ${ratio} 不满足 WCAG AA (4.5)")
```

### 9.2 Dp 扩展

```kotlin
// Dp 转 Float Px（安全版本）
fun Dp.toPxSafe(density: Density): Float

// Dp 转 Int Px（向下取整）
fun Dp.toPxInt(density: Density): Int
```

**使用示例**：

```kotlin
val density = LocalDensity.current
val widthPx = 16.dp.toPxSafe(density)
val widthInt = 16.dp.toPxInt(density)

// Canvas 绘制
canvas.drawRect(
    Rect(0f, 0f, widthPx, heightPx),
    paint
)
```

### 9.3 Size 扩展

```kotlin
// Size 转 DpSize（依赖 Density）
fun Size.toDpSize(density: Density): DpSize
```

**使用示例**：

```kotlin
val canvasSizeDp = canvasSize.toDpSize(density)
val widthDp = canvasSizeDp.width
val heightDp = canvasSizeDp.height
```

---

## 10. UiState 状态机范式

> 文件位置：`patterns/DSUiState.kt` + `patterns/DSPageStateLayout.kt`

### 10.1 两种状态范式并存

Pai 提供两种页面状态管理范式，业务自选：

| 范式 | 入口 | 适用场景 |
|---|---|---|
| **slot 驱动** | `DSNetWorkView(isLoading, error, empty, content)` | 简单页面，状态分散传参 |
| **状态机驱动** | `DSPageStateLayout(state: DSUiState, content)` | 复杂状态机，编译期保证 when 分支完整 |

### 10.2 DSUiState sealed interface

```kotlin
@Stable
sealed interface DSUiState<out T> {
    data object Loading : DSUiState<Nothing>                              // 首次加载
    data class Success<T>(val data: T) : DSUiState<T>                     // 成功
    data class Empty(title, description, actionText) : DSUiState<Nothing> // 空状态
    data class Error(message, retryText, errorCode) : DSUiState<Nothing>  // 错误
    data class LoadingMore<T>(val previousData: T) : DSUiState<T>         // 加载更多
    data class Refreshing<T>(val data: T) : DSUiState<T>                  // 刷新中
    data class PartialError<T>(data, errorMessage, retryText) : DSUiState<T> // 部分错误
    
    companion object {
        fun <T> success(data: T): DSUiState<T>
        fun loading(): DSUiState<Nothing>
        fun empty(title, description, actionText): DSUiState<Nothing>
        fun error(message, retryText, errorCode): DSUiState<Nothing>
        fun <T> loadingMore(previousData: T): DSUiState<T>
        fun <T> refreshing(data: T): DSUiState<T>
        fun <T> partialError(data, errorMessage, retryText): DSUiState<T>
    }
}

// 简化版（无数据承载，仅 4 种状态）
@Stable
sealed interface DSSimpleUiState {
    data object Loading : DSSimpleUiState
    data object Content : DSSimpleUiState
    data object Empty : DSSimpleUiState
    data class Error(val message: String) : DSSimpleUiState
}
```

### 10.3 DSPageStateLayout 渲染

```kotlin
@Composable
fun <T> DSPageStateLayout(
    state: DSUiState<T>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onEmptyAction: (() -> Unit)? = null,
    content: @Composable (T) -> Unit
)
```

**自动渲染映射**：

| DSUiState 状态 | 渲染组件 |
|---|---|
| `Loading` | `DSFullScreenLoading()` |
| `Success(data)` | `content(data)` |
| `Empty(...)` | `DSFullScreenEmpty(...)` |
| `Error(...)` | `DSFullScreenError(message, onRetry)` |
| `LoadingMore(previousData)` | `content(previousData)`（业务自加底部加载指示） |
| `Refreshing(data)` | `content(data)`（业务自加顶部刷新指示） |
| `PartialError(data, ...)` | `content(data)`（业务自加错误提示条） |

### 10.4 使用示例

```kotlin
// ViewModel
val uiState: StateFlow<DSUiState<List<Item>>> = flow.map { result ->
    when (result) {
        is ApiResult.Loading -> DSUiState.loading()
        is ApiResult.Success -> {
            if (result.data.isEmpty()) DSUiState.empty(description = "暂无数据")
            else DSUiState.success(result.data)
        }
        is ApiResult.Error -> DSUiState.error(message = result.message)
    }
}.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DSUiState.loading())

// Composable
val state by viewModel.uiState.collectAsStateWithLifecycle()
DSPageStateLayout(
    state = state,
    onRetry = { viewModel.retry() },
    onEmptyAction = { viewModel.refresh() },
    modifier = Modifier.padding(padding)
) { items ->
    LazyColumn { items(items) { ItemRow(it) } }
}
```

### 10.5 简化版（无数据）

```kotlin
// 适合设置页等无数据承载的简单页面
val state: DSSimpleUiState = when {
    isLoading -> DSSimpleUiState.loading()
    hasError -> DSSimpleUiState.error("加载失败")
    isEmpty -> DSSimpleUiState.empty()
    else -> DSSimpleUiState.content()
}

DSPageStateLayout(
    state = state,
    onRetry = { retry() }
) {
    // content
}
```

### 10.6 与 DSNetWorkView 的选择

| 场景 | 推荐范式 | 原因 |
|---|---|---|
| 简单页面（仅 Loading/Success/Error/Empty） | DSNetWorkView | slot 驱动，调用简单 |
| 复杂状态机（含 LoadingMore/Refreshing/PartialError） | DSPageStateLayout + DSUiState | sealed 多态，编译期保证完整 |
| 与 BaseNetWorkViewModel 集成 | DSNetWorkView | 已有 ApiResult 自动映射 |
| 业务侧自定义状态 | DSPageStateLayout | sealed 强制 when 分支完整 |

---

## 附录：Foundation 文件清单

```
foundation/
├── a11y/
│   ├── A11y.kt                    # minTouchTarget / a11yDescription / a11yState / a11yMinSize
│   └── DSStateLayer.kt            # DSStateLayerAlpha / dsStateLayer / dsStateLayerSimple / dsRipple
├── layout/
│   └── DSWindowSizeClass.kt       # DSWidthSizeClass / DSHeightSizeClass / DSWindowSizeClass / DSResponsiveLayout
├── motion/
│   ├── DSAnimatedPressScale.kt    # Modifier.pressScale / pressScaleWith
│   ├── DSListItemAnimation.kt     # Modifier.listItemEnterAnimation
│   ├── DSLottieAnimation.kt       # DSLottieAnimation / DSLottieLoading
│   ├── DSMotionScheme.kt          # DSMotionScheme / DSEnterMotion / DSExitMotion / DSSpring / DSPageTransitions
│   └── DSSharedTransition.kt      # DSSharedTransitionProvider / dsSharedElement / dsSharedBounds
├── perf/
│   └── DSPerformanceUtils.kt      # DSRecompositionTracker / rememberDebounced / DSStableMarker /
│                                  # DSStableList / DSStableMap / DSStableText / DSStableUiState
├── preview/
│   └── DSPreviewScenes.kt         # Light / Dark / Brand / Amoled / HighContrast / AllBrands / AllThemes
├── theme/
│   ├── ColorScheme.kt             # 4 个 build*ColorScheme 工厂 + ExtendedColors
│   ├── DSComponentOverrides.kt    # 6 种 Override + DSComponentThemeOverride
│   ├── DSThemeDebugPanel.kt       # 开发模式主题调试面板
│   ├── Shapes.kt                  # DSShapes / DSShapePill / DSShapeTopMedium / DSShapeTopExtraLarge
│   ├── Theme.kt                   # DSDesignTheme 主题入口
│   └── Typography.kt              # DSTypography 15 种 M3 文字样式
├── util/
│   └── DSExtensions.kt            # Color.toHex/withAlpha/blend/isDark/contrastRatio + Dp.toPxSafe/toPxInt + Size.toDpSize
└── tokens/
    ├── BrandColorPalette.kt       # DSBrandColor 5 套品牌色板
    ├── ColorTokens.kt             # 50 个 BrandXxx 色值常量
    ├── ComponentTokens.kt         # IconSize / ComponentHeight / Border / Alpha / minTouchTarget
    ├── DSMessageType.kt           # DSMessageType 统一消息类型 + 扩展属性
    ├── DSNavItem.kt               # DSNavItem 统一导航项数据类
    ├── DesignTokens.kt            # DSTokens 总入口
    ├── ElevationTokens.kt         # DSElevation 8 级
    ├── FontSizeScale.kt           # DSFontSizeScale 4 档字号缩放
    ├── MotionTokens.kt            # MSDuration / MSEasing
    ├── RadiusTokens.kt            # ShapeScale 圆角常量
    ├── SpacingTokens.kt           # DSSpacing 11 档间距
    └── TypographyTokens.kt        # TypeScale / RobotoFontFamily

# 状态机范式（在 patterns/，跨层组件）
patterns/
├── DSUiState.kt                   # DSUiState sealed interface (8 种状态) + DSSimpleUiState
└── DSPageStateLayout.kt           # DSPageStateLayout (配合 DSUiState 自动渲染)
```
