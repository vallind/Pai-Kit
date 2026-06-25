// ============================================================================
// DSMessageType.kt
// 统一消息类型枚举（Snackbar / Banner / Dialog 共用）
// 替代旧的 DSSnackbarType / DSBannerType / DSDialogType 三套枚举
// ============================================================================

package com.pai.app.core.designsystem.foundation.tokens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * DSMessageType - 统一消息类型
 *
 * 用于 [DSSnackbar] / [DSBanner] / [DSDialog] 等所有反馈类组件，
 * 替代旧的 DSSnackbarType / DSBannerType / DSDialogType 三套枚举。
 *
 * - [Info]    通用信息提示（旧 Default）
 * - [Success] 成功反馈
 * - [Warning] 警告提示
 * - [Error]   错误反馈
 */
enum class DSMessageType {
    Info,
    Success,
    Warning,
    Error
}

/**
 * 读取 [DSMessageType] 对应的图标
 */
val DSMessageType.icon: ImageVector
    get() = when (this) {
        DSMessageType.Info -> Icons.Default.Info
        DSMessageType.Success -> Icons.Default.CheckCircle
        DSMessageType.Warning -> Icons.Default.Warning
        DSMessageType.Error -> Icons.Default.Error
    }

/**
 * 读取 [DSMessageType] 对应的容器色（如 Snackbar 背景、Banner 背景）
 */
val DSMessageType.containerColor: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        DSMessageType.Info -> MaterialTheme.colorScheme.inverseSurface
        DSMessageType.Success -> MaterialTheme.colorScheme.primary
        DSMessageType.Warning -> MaterialTheme.colorScheme.tertiary
        DSMessageType.Error -> MaterialTheme.colorScheme.error
    }

/**
 * 读取 [DSMessageType] 对应的内容色（如 Snackbar 文字、Banner 文字）
 */
val DSMessageType.contentColor: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        DSMessageType.Info -> MaterialTheme.colorScheme.inverseOnSurface
        DSMessageType.Success -> MaterialTheme.colorScheme.onPrimary
        DSMessageType.Warning -> MaterialTheme.colorScheme.onTertiary
        DSMessageType.Error -> MaterialTheme.colorScheme.onError
    }

/**
 * 读取 [DSMessageType] 对应的容器色（淡色版，用于 Banner / Dialog 浅色背景）
 */
val DSMessageType.containerColorLight: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        DSMessageType.Info -> MaterialTheme.colorScheme.primaryContainer
        DSMessageType.Success -> MaterialTheme.colorScheme.primaryContainer
        DSMessageType.Warning -> MaterialTheme.colorScheme.tertiaryContainer
        DSMessageType.Error -> MaterialTheme.colorScheme.errorContainer
    }

/**
 * 读取 [DSMessageType] 对应的内容色（深色版，与 containerColorLight 配对）
 */
val DSMessageType.contentColorOnLight: Color
    @Composable
    @ReadOnlyComposable
    get() = when (this) {
        DSMessageType.Info -> MaterialTheme.colorScheme.onPrimaryContainer
        DSMessageType.Success -> MaterialTheme.colorScheme.onPrimaryContainer
        DSMessageType.Warning -> MaterialTheme.colorScheme.onTertiaryContainer
        DSMessageType.Error -> MaterialTheme.colorScheme.onErrorContainer
    }
