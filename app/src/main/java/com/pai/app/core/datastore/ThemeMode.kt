// ============================================================================
// ThemeMode.kt
// 主题模式枚举
// ============================================================================

package com.pai.app.core.datastore

/**
 * 主题模式
 *
 * - [System]：跟随系统暗色模式
 * - [Light]：强制浅色
 * - [Dark]：强制深色
 * - [AMOLED]：纯黑 AMOLED 模式（深色变种，background / surface 为 Color.Black，省电）
 */
internal enum class ThemeMode {
    System,
    Light,
    Dark,
    AMOLED,
}
