// ============================================================================
// AppDatabase.kt
// Room 数据库定义（通用模板）
// 业务方在此注册新 Entity，删除 ExampleEntity 后开始开发
// ============================================================================

package com.pai.app.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.pai.app.core.database.dao.ExampleDao
import com.pai.app.core.database.dao.UserDao
import com.pai.app.core.database.entity.ExampleEntity
import com.pai.app.core.database.entity.UserEntity

/**
 * 应用主数据库
 *
 * 当前包含：
 * - [UserEntity] → `users` 表（用户登录态缓存，登录场景通用）
 * - [ExampleEntity] → `examples` 表（示例，业务方删除）
 *
 * 业务方拉取脚手架后：
 * 1. 删除 `ExampleEntity` 与 `ExampleDao`
 * 2. 在 `entities` 数组中添加真实业务 Entity
 * 3. 添加对应的 `abstract fun xxxDao(): XxxDao`
 * 4. **version 必须 +1**（如 1 → 2），并编写对应 [androidx.room.migration.Migration]
 *
 * 决策 8（Medium #12）：
 * - [exportSchema]`=true`：导出 schema JSON 到 `app/schemas/`，便于 CI 校验迁移
 * - 已移除 `fallbackToDestructiveMigration`：避免业务升级时静默丢数据
 * - 业务方按版本迁移编写 Migration，例：
 *   ```kotlin
 *   val Migration_1_2 = object : Migration(1, 2) {
 *       override fun migrate(database: SupportSQLiteDatabase) {
 *           database.execSQL("ALTER TABLE users ADD COLUMN avatarUrl TEXT")
 *       }
 *   }
 *   ```
 *   并在 `DatabaseModule.provideDatabase` 中 `.addMigrations(Migration_1_2)` 注册
 *
 * 此类必须保持 public 可见性（由 Hilt `@Provides` 返回）。
 */
@Database(
    entities = [UserEntity::class, ExampleEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {

    /** 用户表 DAO（登录场景通用，保留） */
    abstract fun userDao(): UserDao

    /** 示例表 DAO（业务方删除） */
    abstract fun exampleDao(): ExampleDao
}
