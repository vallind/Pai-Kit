// ============================================================================
// Extensions.kt
// 通用 Kotlin / Android 扩展函数
// ============================================================================

package com.pai.app.core.util

import android.content.Context
import android.widget.Toast
import androidx.annotation.MainThread

/**
 * 显示短时 Toast（约 2 秒）
 *
 * 使用示例：
 * ```kotlin
 * "已保存".toastShort(context)
 * // 或
 * context.toastShort("已保存")
 * ```
 */
@MainThread
internal fun Context.toastShort(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * 显示长时 Toast（约 3.5 秒）
 */
@MainThread
internal fun Context.toastLong(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
