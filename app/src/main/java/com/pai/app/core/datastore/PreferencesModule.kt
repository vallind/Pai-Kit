// ============================================================================
// PreferencesModule.kt
// Hilt 偏好模块（占位 + 文档）
// ============================================================================
//
// Low #22：ThemePreferences / UserPreferences 均使用 @Inject constructor，
// Hilt 可直接构造。本模块不再冗余 @Provides 这些类。
// 仅保留模块占位与文档说明，便于业务方未来在此 @Provides 第三方偏好实现。
//
// EncryptedPrefs 的接口绑定见 [EncryptedPrefsModule]（@Binds），
// 因需要接口→实现解耦以支持测试替换，故单独成模块。
// ============================================================================

package com.pai.app.core.datastore

import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * 偏好依赖注入模块
 *
 * 注册到 [SingletonComponent]。
 *
 * 当前由 Hilt 直接构造的类（均带 `@Inject constructor`，无需在此 `@Provides`）：
 * 1. [ThemePreferences] - 主题偏好读写
 * 2. [UserPreferences] - 登录态偏好读写（userId）
 *
 * [EncryptedPrefs] 的绑定见 [EncryptedPrefsModule]（接口 → [EncryptedPrefsImpl] 实现）。
 *
 * 业务方未来若需要替换实现或加装饰器，可在此添加 `@Provides` 方法
 * （届时需移除被替换类的 `@Inject constructor`，二选一）。
 */
@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule
