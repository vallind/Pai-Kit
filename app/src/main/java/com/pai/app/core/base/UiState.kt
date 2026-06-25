// ============================================================================
// UiState.kt
// UI 状态标记接口
// 业务页面的状态数据类实现此接口，便于在 ViewModel / Compose 中统一处理
// ============================================================================

package com.pai.app.core.base

/**
 * UI 状态标记接口
 *
 * 用于让 ViewModel 暴露的 `StateFlow<XxxUiState>` 具备统一的类型标识，
 * 便于：
 * 1. 在通用扩展中按类型约束处理状态
 * 2. 团队约定：所有页面级状态数据类必须实现此接口
 *
 * 使用示例：
 * ```kotlin
 * internal data class ProfileUiState(
 *     val user: UserDto? = null,
 *     val isLoading: Boolean = false,
 *     val error: String? = null,
 * ) : UiState
 * ```
 */
internal interface UiState
