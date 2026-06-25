// ============================================================================
// DataStoreExtensions.kt
// 顶层 DataStore 委托：每个文件级单例对应一个 Preferences DataStore
// ============================================================================

package com.pai.app.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * 主题设置 DataStore
 *
 * 文件名 `settings`，存储主题模式、动态色开关等应用级偏好。
 */
internal val Context.themeDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 用户偏好 DataStore
 *
 * 文件名 `user_prefs`，存储登录态、token 等用户级偏好。
 */
internal val Context.userDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")
