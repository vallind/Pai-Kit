// ============================================================================
// UiModels.kt
// 全局共享 UI 模型
// 跨 feature 复用的 UI 层数据结构（Snackbar 消息、加载态等）
// ============================================================================

package com.pai.app.core.appstate.model

/**
 * Snackbar 消息模型
 *
 * 用于在 ViewModel 中通过 AppEventBus 发送、在 MainActivity 顶层收集后展示。
 *
 * @param message 文案
 * @param actionLabel 可选操作按钮文案（如"撤销"），为 null 时不显示按钮
 * @param duration 展示时长（毫秒），默认 2000ms
 */
internal data class SnackbarMessage(
    val message: String,
    val actionLabel: String? = null,
    val duration: Long = 2000
)

/**
 * 加载状态模型
 *
 * 用于在 ViewModel 中暴露带文案的加载态，
 * 配合全屏遮罩 [com.pai.app.core.designsystem.patterns.DSLoadingOverlay] 使用。
 *
 * @param isLoading 是否加载中
 * @param message 可选加载文案，为 null 时仅显示圆形进度
 */
internal data class LoadingState(
    val isLoading: Boolean,
    val message: String? = null
)
