// ============================================================================
// DataModule.kt
// Hilt 数据层模块：Repository 接口 → 实现 绑定（决策 P1-1）
// ============================================================================

package com.pai.app.core.data.di

import com.pai.app.core.data.AuthRepositoryImpl
import com.pai.app.core.data.ExampleRepositoryImpl
import com.pai.app.core.domain.AuthRepository
import com.pai.app.core.domain.ExampleRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据层依赖注入模块（Repository 接口绑定）
 *
 * 决策 P1-1：feature 层注入 [AuthRepository] / [ExampleRepository] **接口**
 * （[com.pai.app.core.domain]），不感知 [AuthRepositoryImpl] / [ExampleRepositoryImpl]
 * 实现。Hilt 通过本 Module 的 `@Binds` 解析接口 → 实现的绑定关系。
 *
 * - `@Binds` 比 `@Provides` 更高效（编译期生成 Factory，无运行时反射）
 * - `@Singleton` 保证 Impl 全局单例（与原 `@Singleton class` 等价）
 * - 接口与实现解耦后，未来 KMP 迁移时 Impl 可替换为各平台实现，
 *   feature 调用方零感知
 *
 * 与 [com.pai.app.core.database.DatabaseModule]（`@Provides` 提供 DAO）+
 * [com.pai.app.core.network.NetworkModule]（`@Provides` 提供 AppApi）协作，
 * Impl 通过 `@Inject constructor` 自动获得 DAO / API 依赖。
 *
 * 业务方拉取脚手架后：
 * 1. 删除 `bindExampleRepository`（示例 Repository 删除）
 * 2. 在本 Module 追加真实业务 Repository 的 `@Binds` 方法
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    /**
     * 绑定 [AuthRepository] 接口到 [AuthRepositoryImpl] 实现
     *
     * - feature 注入 `AuthRepository` 时，Hilt 解析为 `AuthRepositoryImpl` 单例
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    /**
     * 绑定 [ExampleRepository] 接口到 [ExampleRepositoryImpl] 实现
     *
     * 业务方删除示例 Repository 时一并删除本方法。
     */
    @Binds
    @Singleton
    abstract fun bindExampleRepository(impl: ExampleRepositoryImpl): ExampleRepository
}
