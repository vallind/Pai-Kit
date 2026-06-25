// ============================================================================
// DatabaseModule.kt
// Hilt 数据库模块：提供 AppDatabase / UserDao / ExampleDao 单例
// ============================================================================

package com.pai.app.core.database

import android.content.Context
import androidx.room.Room
import com.pai.app.core.database.dao.ExampleDao
import com.pai.app.core.database.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库依赖注入模块
 *
 * 注册到 [SingletonComponent]，提供以下单例：
 * 1. [AppDatabase] - 应用主数据库
 * 2. [UserDao] - 用户表 DAO（登录场景通用）
 * 3. [ExampleDao] - 示例表 DAO（业务方删除）
 *
 * 决策 8（Medium #12）：
 * - 已移除 `fallbackToDestructiveMigration`：版本升级时不再静默清表
 * - 业务方拉取脚手架后，按版本编写 [androidx.room.migration.Migration] 并在此
 *   `.addMigrations(...)` 注册；schema JSON 已通过 `exportSchema=true` 导出到
 *   `app/schemas/`（Config agent 配置 `room.schemaLocation`）
 *
 * 业务方拉取脚手架后：
 * 1. 删除 `provideExampleDao`
 * 2. 添加真实业务 DAO 的 `@Provides` 方法
 *
 * 注意：Hilt 要求 `@Module` 与 `@Provides` 方法均为 public 可见性。
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * 提供应用主数据库
     *
     * 决策 8：不再 `fallbackToDestructiveMigration`，强制业务方按版本迁移。
     * TODO 业务方添加 Migration（例：Migration(1,2) { /* ALTER TABLE ... */ }）后，
     * 在此 `.addMigrations(Migration_1_2)` 注册。
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pai_app.db",
        )
            // .addMigrations(...)  // TODO 业务方按版本迁移编写 Migration
            .build()
    }

    /** 提供用户 DAO（登录场景通用，保留） */
    @Provides
    @Singleton
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    /** 提供示例 DAO（业务方删除） */
    @Provides
    @Singleton
    fun provideExampleDao(db: AppDatabase): ExampleDao = db.exampleDao()
}
