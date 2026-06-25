// ============================================================================
// EncryptedPrefsModule.kt
// Hilt 模块：绑定 EncryptedPrefs 接口到生产实现
// ============================================================================

package com.pai.app.core.datastore

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt 模块：绑定 [EncryptedPrefs] 接口到 [EncryptedPrefsImpl]
 *
 * 决策 4：接口隔离后，Hilt 通过 `@Binds` 将接口解析为生产实现。
 * 测试时可用 `@UninstallModules(EncryptedPrefsModule::class)` + 测试模块
 * 替换为 [com.pai.app.core.datastore.InMemoryEncryptedPrefs]。
 *
 * 注册到 [SingletonComponent]（全局单例）。
 *
 * 用法变更：
 * - 旧：`EncryptedPrefs` 是 `@Singleton class @Inject constructor(...)`，Hilt 直接构造
 * - 新：`EncryptedPrefs` 是接口，由本模块 `@Binds` 绑定到 `EncryptedPrefsImpl`
 * - 消费者注入的 `EncryptedPrefs` 类型不变，零感知
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class EncryptedPrefsModule {

    /**
     * 绑定 [EncryptedPrefs] 接口到 [EncryptedPrefsImpl]
     *
     * [EncryptedPrefsImpl] 保留 `@Inject constructor` + `@Singleton`，Hilt 自动构造其实例。
     * `@Binds` 在编译期生成 Factory，无运行时反射开销。
     */
    @Binds
    @Singleton
    abstract fun bindEncryptedPrefs(impl: EncryptedPrefsImpl): EncryptedPrefs
}
