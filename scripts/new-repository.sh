#!/usr/bin/env bash
# ============================================================================
# new-repository.sh - Scaffold a new data layer (DTO + Entity + DAO + Repository + Tests)
#
# 生成的代码严格遵守项目 convention（决策 P1-1：Repository 接口/实现分离，KMP-ready）：
#   - DTO:        @Serializable internal data class XxxDto     （core/network/model/）
#   - Entity:     @Entity internal data class XxxEntity         （core/database/entity/）
#   - DAO:        @Dao internal interface XxxDao                （core/database/dao/）
#   - Domain:     public data class XxxItem                     （core/domain/model/，KMP-ready 纯 Kotlin）
#   - Repository: interface XxxRepository                       （core/domain/，KMP-ready，无 Android/Retrofit/Room 依赖）
#   - RepositoryImpl: @Singleton class XxxRepositoryImpl @Inject constructor(api, dao) : XxxRepository
#                                                             （core/data/，@Binds 绑定到接口）
#
# 自动编辑（高风险，失败时打印手动指引）：
#   - AppDatabase.kt:        entities 数组追加 XxxEntity::class + version +1 + abstract fun xxxDao()
#   - DatabaseModule.kt:     追加 @Provides fun provideXxxDao()
#   - AppApi.kt:             追加 @GET suspend fun getXxxs(): List<XxxDto>
#   - DataModule.kt:         追加 @Binds @Singleton abstract fun bindXxxRepository(impl): XxxRepository
#
# 用法:
#   ./scripts/new-repository.sh <name> [--dry-run] [--force]
#
# 示例:
#   ./scripts/new-repository.sh comment
#   ./scripts/new-repository.sh product --dry-run
# ============================================================================

set -eo pipefail

# ---------------------------------------------------------------------------
# TTY-aware color output
# ---------------------------------------------------------------------------
if [ -t 1 ]; then
    GREEN=$'\033[32m'
    YELLOW=$'\033[33m'
    RED=$'\033[31m'
    BLUE=$'\033[34m'
    BOLD=$'\033[1m'
    RESET=$'\033[0m'
else
    GREEN=""
    YELLOW=""
    RED=""
    BLUE=""
    BOLD=""
    RESET=""
fi

ok()   { printf '%s✓%s %s\n'  "$GREEN"  "$RESET" "$1"; }
warn() { printf '%s⚠%s %s\n'  "$YELLOW" "$RESET" "$1"; }
err()  { printf '%s✗%s %s\n'  "$RED"    "$RESET" "$1" >&2; }
info() { printf '%s→%s %s\n'  "$BLUE"   "$RESET" "$1"; }

# ---------------------------------------------------------------------------
# Usage
# ---------------------------------------------------------------------------
usage() {
    cat <<EOF
用法: $0 <name> [--dry-run] [--force]
  name                  repository 名称（小写，如 "comment" / "product"）
  --dry-run, -n         预览将创建的文件，不实际写入
  --force               覆盖已存在的文件
  --help, -h            显示帮助

示例:
  $0 comment
  $0 product --dry-run
EOF
}

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

validate_project_root() {
    local root="$1"
    if [ ! -f "$root/app/build.gradle.kts" ] || [ ! -f "$root/settings.gradle.kts" ]; then
        err "不在项目根目录：未找到 app/build.gradle.kts 或 settings.gradle.kts"
        err "请在项目根目录运行此脚本（当前: $root）"
        exit 1
    fi
}

validate_name() {
    local name="$1"
    if ! printf '%s' "$name" | grep -Eq '^[a-z][a-z0-9-]*$'; then
        err "名称必须为小写 kebab-case（a-z0-9 + 连字符），如 'comment' / 'product'"
        exit 1
    fi
    if printf '%s' "$name" | grep -Eq -- '-$'; then
        err "名称不能以连字符结尾"
        exit 1
    fi
    if printf '%s' "$name" | grep -Eq -- '--'; then
        err "名称不能包含连续连字符"
        exit 1
    fi
}

# kebab-case -> PascalCase: product -> Product, order-detail -> OrderDetail
to_pascal() {
    local name="$1"
    printf '%s' "$name" \
        | tr -- '-_' '  ' \
        | awk '{ for(i=1;i<=NF;i++) $i=toupper(substr($i,1,1)) substr($i,2); print }' \
        | tr -d ' '
}

# kebab-case -> camelCase: product -> product, order-detail -> orderDetail
to_camel() {
    local pascal
    pascal=$(to_pascal "$1")
    printf '%s%s' "$(printf '%s' "$pascal" | cut -c1 | tr '[:upper:]' '[:lower:]')" "$(printf '%s' "$pascal" | cut -c2-)"
}

# kebab-case -> lowercase no separator (for table name / api path): order-detail -> order_detail
# (SQL table names allow underscore; keeps readability)
to_snake() {
    local name="$1"
    printf '%s' "$name" | tr -- '-' '_'
}

# ---------------------------------------------------------------------------
# Argument parsing
# ---------------------------------------------------------------------------
NAME=""
DRY_RUN=0
FORCE=0

while [ $# -gt 0 ]; do
    case "$1" in
        --help|-h) usage; exit 0 ;;
        --dry-run|-n) DRY_RUN=1; shift ;;
        --force) FORCE=1; shift ;;
        -*) err "未知参数: $1"; usage; exit 1 ;;
        *)
            if [ -z "$NAME" ]; then
                NAME="$1"
            else
                err "多余参数: $1"; usage; exit 1
            fi
            shift
            ;;
    esac
done

if [ -z "$NAME" ]; then
    usage
    exit 1
fi

validate_name "$NAME"

# Determine project root
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
validate_project_root "$PROJECT_ROOT"

PASCAL="$(to_pascal "$NAME")"
CAMEL="$(to_camel "$NAME")"
SNAKE="$(to_snake "$NAME")"

# --- P1-1 决策后的包路径：domain (接口+model) 与 data (Impl) 分离 ---
NETWORK_MODEL_DIR="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/network/model"
DATABASE_ENTITY_DIR="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/database/entity"
DATABASE_DAO_DIR="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/database/dao"
DOMAIN_DIR="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/domain"
DOMAIN_MODEL_DIR="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/domain/model"
DATA_DIR="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/data"
DATA_DI_DIR="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/data/di"
TEST_DATA_DIR="$PROJECT_ROOT/app/src/test/java/com/pai/app/core/data"

DTO_FILE="$NETWORK_MODEL_DIR/${PASCAL}Dto.kt"
ENTITY_FILE="$DATABASE_ENTITY_DIR/${PASCAL}Entity.kt"
DAO_FILE="$DATABASE_DAO_DIR/${PASCAL}Dao.kt"
ITEM_FILE="$DOMAIN_MODEL_DIR/${PASCAL}Item.kt"
REPO_INTERFACE_FILE="$DOMAIN_DIR/${PASCAL}Repository.kt"
REPO_IMPL_FILE="$DATA_DIR/${PASCAL}RepositoryImpl.kt"
REPO_TEST_FILE="$TEST_DATA_DIR/${PASCAL}RepositoryTest.kt"

APP_DATABASE="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/database/AppDatabase.kt"
DATABASE_MODULE="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/database/DatabaseModule.kt"
APP_API="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/network/AppApi.kt"
DATA_MODULE="$PROJECT_ROOT/app/src/main/java/com/pai/app/core/data/di/DataModule.kt"
APP_API_TEST="$PROJECT_ROOT/app/src/test/java/com/pai/app/core/network/AppApiTest.kt"

# ---------------------------------------------------------------------------
# Summary registry
# ---------------------------------------------------------------------------
CREATED_FILES=()
MODIFIED_FILES=()
SKIPPED_FILES=()
MANUAL_HINTS=()

record_create() { CREATED_FILES+=("$1"); }
record_modify() { MODIFIED_FILES+=("$1"); }
record_skip()   { SKIPPED_FILES+=("$1"); }
record_manual() { MANUAL_HINTS+=("$1"); }

# ---------------------------------------------------------------------------
# Content generators
# ---------------------------------------------------------------------------

gen_dto() {
    local pascal="$1"
    local snake="$2"
    cat <<'KOTLIN' | sed "s/__PASCAL__/$pascal/g; s/__SNAKE__/$snake/g"
// ============================================================================
// __PASCAL__Dto.kt
// __PASCAL__ 网络数据传输对象 — 由 scripts/new-repository.sh 生成
// 业务方根据真实后端契约调整字段后删除本注释块
// ============================================================================
package com.pai.app.core.network.model

import kotlinx.serialization.Serializable

/**
 * __PASCAL__ DTO（Data Transfer Object）
 *
 * 仅用于网络传输，**禁止**直接传给 Composable。
 * UI 层应使用 [com.pai.app.core.domain.model.__PASCAL__Item] domain model（决策 P1-1）。
 *
 * 由 new-repository.sh 生成的基础骨架，业务方按真实后端契约调整字段。
 * - 所有 DTO 必须加 `@Serializable` 注解（kotlinx.serialization）
 * - 默认值用于反序列化容错（后端字段缺失时不抛错）
 *
 * @param id 主键
 * @param name 名称（占位字段，业务方替换）
 * @param description 描述（可空，占位字段）
 */
@Serializable
internal data class __PASCAL__Dto(
    val id: Long = 0,
    val name: String = "",
    val description: String? = null,
)
KOTLIN
}

gen_entity() {
    local pascal="$1"
    local snake="$2"
    cat <<'KOTLIN' | sed "s/__PASCAL__/$pascal/g; s/__SNAKE__/$snake/g"
// ============================================================================
// __PASCAL__Entity.kt
// __PASCAL__ Room Entity — 由 scripts/new-repository.sh 生成
// 业务方根据真实存储需求调整字段后删除本注释块
// ============================================================================
package com.pai.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * __PASCAL__ Room Entity
 *
 * 仅用于数据库存储，**禁止**直接传给 Composable。
 * Entity 与 DTO 字段可不同，通过 Repository 转换。
 *
 * 由 new-repository.sh 生成的基础骨架，业务方按真实存储需求调整字段。
 * - `@Entity` 注解必加
 * - `tableName` 用 snake_case（SQL 约定）
 * - 索引字段加 `@Index` 提升查询性能（业务方按查询模式添加）
 *
 * AI 规则：
 * - 字段加 `@PrimaryKey` 标记主键
 * - 时间戳字段用 `Long = System.currentTimeMillis()` 默认值
 *
 * @param id 主键
 * @param name 名称（占位字段）
 * @param description 描述（可空，占位字段）
 * @param createdAt 创建时间戳（毫秒）
 */
@Entity(
    tableName = "__SNAKE__s",
    indices = [Index("name")],
)
internal data class __PASCAL__Entity(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String?,
    val createdAt: Long = System.currentTimeMillis(),
)
KOTLIN
}

gen_dao() {
    local pascal="$1"
    local camel="$2"
    local snake="$3"
    cat <<'KOTLIN' | sed "s/__PASCAL__/$pascal/g; s/__CAMEL__/$camel/g; s/__SNAKE__/$snake/g"
// ============================================================================
// __PASCAL__Dao.kt
// __PASCAL__ 数据访问对象 — 由 scripts/new-repository.sh 生成
// 业务方根据真实查询需求调整方法后删除本注释块
// ============================================================================
package com.pai.app.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pai.app.core.database.entity.__PASCAL__Entity
import kotlinx.coroutines.flow.Flow

/**
 * __PASCAL__ Room DAO
 *
 * 由 new-repository.sh 生成的基础骨架，业务方按真实查询需求调整方法。
 *
 * AI 规则：
 * - DAO 方法用 `suspend` 或返回 `Flow`（禁用 `Single<ListenableFuture>` 等旧 API）
 * - SQL 语句加注释说明意图
 * - 暴露给 Repository 的方法标 `internal`
 */
@Dao
internal interface __PASCAL__Dao {

    /** 插入或更新（主键冲突时替换） */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<__PASCAL__Entity>)

    /** 观察全部，按创建时间倒序 */
    @Query("SELECT * FROM __SNAKE__s ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<__PASCAL__Entity>>

    /** 按 ID 查询 */
    @Query("SELECT * FROM __SNAKE__s WHERE id = :id")
    suspend fun getById(id: Long): __PASCAL__Entity?

    /** 清空表 */
    @Query("DELETE FROM __SNAKE__s")
    suspend fun clearAll()
}
KOTLIN
}

gen_item() {
    local pascal="$1"
    cat <<'KOTLIN' | sed "s/__PASCAL__/$pascal/g"
// ============================================================================
// __PASCAL__Item.kt
// __PASCAL__ 领域模型 — 由 scripts/new-repository.sh 生成
// 决策 P1-1：domain model 放 core/domain/model/（KMP-ready 纯 Kotlin，无 Android import）
// 决策 8：Repository 不向外暴露 Entity / DTO
// ============================================================================
package com.pai.app.core.domain.model

/**
 * __PASCAL__ 领域模型
 *
 * 决策 P1-1：domain model 放 [com.pai.app.core.domain.model] 包（KMP-ready，
 *
 * 决策 8：[com.pai.app.core.domain.__PASCAL__Repository] 接口是唯一的数据出口，
 * 禁止把 Room Entity（[com.pai.app.core.database.entity.__PASCAL__Entity]）
 * 或网络 DTO（[com.pai.app.core.network.model.__PASCAL__Dto]）直接暴露给 feature/UI 层。
 * 本 data class 是 Repository 对外的领域模型。
 *
 * 由 new-repository.sh 生成的基础骨架，业务方按真实业务字段调整。
 *
 * @param id 主键
 * @param name 名称
 * @param description 描述（可空）
 */
data class __PASCAL__Item(
    val id: Long,
    val name: String,
    val description: String?,
)
KOTLIN
}

# ---------------------------------------------------------------------------
# gen_repository_interface — 生成 core/domain/<Name>Repository.kt (interface)
# 决策 P1-1：接口在 core.domain，KMP-ready，仅 import ApiResult + domain model + Flow
# 不暴露 Entity / DTO / Retrofit / Room 类型
# ---------------------------------------------------------------------------
gen_repository_interface() {
    local pascal="$1"
    local camel="$2"
    cat <<'KOTLIN' | sed "s/__PASCAL__/$pascal/g; s/__CAMEL__/$camel/g"
// ============================================================================
// __PASCAL__Repository.kt
// __PASCAL__ 仓库接口（domain layer，决策 P1-1：KMP-ready，无 Android 依赖）
// 实现见 core/data/__PASCAL__RepositoryImpl.kt（@Inject constructor + @Binds 绑定）
// 由 scripts/new-repository.sh 生成
// ============================================================================
package com.pai.app.core.domain

import com.pai.app.core.base.ApiResult
import com.pai.app.core.domain.model.__PASCAL__Item
import kotlinx.coroutines.flow.Flow

/**
 * __PASCAL__ Repository 接口（domain layer）
 *
 * 决策 P1-1：接口在 [com.pai.app.core.domain] 包（KMP-ready，**无 Android / Retrofit /
 * [com.pai.app.core.data.__PASCAL__RepositoryImpl]（`@Inject constructor` + `@Singleton`）。
 *
 * feature 层注入本接口（非 Impl），由 Hilt `@Binds`（见
 * [com.pai.app.core.data.di.DataModule]）解析到 Impl。这样实现的替换
 *
 * 设计要点（决策 1 + 决策 8 + P1-1）：
 * 1. 本接口只暴露 domain model [__PASCAL__Item] —— **不**返回 Room Entity 或网络 DTO
 * 2. 网络请求返回 [ApiResult]（决策 1：唯一规范结果类型）
 * 3. 数据库观察返回 `Flow<ApiResult<...>>`，便于 UI 层订阅
 *
 * 注：DTO → Entity 的本地缓存同步（`syncToCache`）属于实现层细节，
 * 不在本接口暴露（避免 domain 接口依赖 [com.pai.app.core.network.model.__PASCAL__Dto]）。
 * 如需触发同步，由 Impl 内部在 [get__PASCAL__s] 成功后自动完成。
 *
 * 业务方拉取脚手架后：
 * 1. 调整 DTO / Entity 字段以匹配真实业务
 * 2. 在本接口添加业务方法（如 getById / search 等），Impl 同步实现
 * 3. 如需分页，参考 docs/rules/09-feature-templates.md 模板 D
 */
interface __PASCAL__Repository {

    /**
     * 从网络拉取 __PASCAL__ 列表
     *
     * 实现层用 [com.pai.app.core.network.safeApiCall] 包装网络请求，返回 [ApiResult]；
     * 内部把 DTO 列表映射为 domain model [__PASCAL__Item] 列表（决策 8：不外泄 DTO）。
     *
     * @return 成功返回 [__PASCAL__Item] 列表，失败返回 [ApiResult.Error]
     */
    suspend fun get__PASCAL__s(): ApiResult<List<__PASCAL__Item>>

    /**
     * 观察本地缓存的 __PASCAL__ 列表
     *
     * 返回 `Flow<ApiResult<List<__PASCAL__Item>>>`，便于 UI 层直接配合
     * [com.pai.app.core.base.asResult] 或单独订阅。Room Flow 内部把
     * [com.pai.app.core.database.entity.__PASCAL__Entity] 映射为 [__PASCAL__Item] 后暴露，
     * Entity 不外泄。
     */
    fun observe__PASCAL__s(): Flow<ApiResult<List<__PASCAL__Item>>>
}
KOTLIN
}

# ---------------------------------------------------------------------------
# gen_repository_impl — 生成 core/data/<Name>RepositoryImpl.kt
# 决策 P1-1：@Singleton class @Inject constructor(api, dao) : <Name>Repository
# 用 safeApiCall 包装；Entity→Item / DTO→Item / DTO→Entity 私有扩展
# syncToCache 不进接口（保留为 Impl 具体方法，避免接口暴露 DTO 类型）
# ---------------------------------------------------------------------------
gen_repository_impl() {
    local pascal="$1"
    local camel="$2"
    cat <<'KOTLIN' | sed "s/__PASCAL__/$pascal/g; s/__CAMEL__/$camel/g"
// ============================================================================
// __PASCAL__RepositoryImpl.kt
// __PASCAL__ 仓库实现（决策 P1-1：实现层 @Inject constructor + @Singleton）
// 接口在 core/domain/__PASCAL__Repository.kt；Hilt @Binds 见 core/data/di/DataModule.kt
// 由 scripts/new-repository.sh 生成
// ============================================================================
package com.pai.app.core.data

import com.pai.app.core.base.ApiResult
import com.pai.app.core.database.dao.__PASCAL__Dao
import com.pai.app.core.database.entity.__PASCAL__Entity
import com.pai.app.core.domain.__PASCAL__Repository
import com.pai.app.core.domain.model.__PASCAL__Item
import com.pai.app.core.network.AppApi
import com.pai.app.core.network.model.__PASCAL__Dto
import com.pai.app.core.network.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * __PASCAL__ Repository 实现
 *
 * 决策 P1-1：实现层 `@Inject constructor` + `@Singleton`，由
 * [com.pai.app.core.data.di.DataModule] 通过 `@Binds` 绑定到
 * [__PASCAL__Repository] 接口。feature 层注入接口（不感知 Impl）。
 *
 * 组合多个数据源：
 * - [AppApi] - 网络数据源（Retrofit 接口返回**纯 DTO**，决策 1）
 * - [__PASCAL__Dao] - 数据库缓存
 *
 * 设计要点（决策 1 + 决策 8 + P1-1）：
 * 1. Impl 是 `@Inject constructor` 具体类，由 Hilt `@Binds` 绑定到
 *    [__PASCAL__Repository] 接口（决策 P1-1：feature 注入接口）
 * 2. 网络请求用 [safeApiCall] 包装返回 [ApiResult]（决策 1：唯一规范结果类型）
 * 3. 数据库操作返回 `Flow` 便于 UI 层订阅
 * 4. **不向外暴露 [__PASCAL__Entity] / [__PASCAL__Dto]**：转换为 domain model [__PASCAL__Item]
 *
 * 业务方拉取脚手架后：
 * 1. 调整 DTO / Entity 字段以匹配真实业务
 * 2. 在 [__PASCAL__Repository] 接口添加业务方法，本 Impl 同步 override
 * 3. 如需分页，参考 docs/rules/09-feature-templates.md 模板 D
 */
@Singleton
class __PASCAL__RepositoryImpl @Inject constructor(
    private val api: AppApi,
    private val __CAMEL__Dao: __PASCAL__Dao,
) : __PASCAL__Repository {

    /**
     * 从网络拉取 __PASCAL__ 列表
     *
     * 用 [safeApiCall] 包装 `api.get__PASCAL__s()`，返回 [ApiResult]；
     * 内部把 DTO 列表映射为 domain model [__PASCAL__Item] 列表。
     *
     * @return 成功返回 [__PASCAL__Item] 列表，失败返回 [ApiResult.Error]
     */
    override suspend fun get__PASCAL__s(): ApiResult<List<__PASCAL__Item>> =
        safeApiCall { api.get__PASCAL__s() }.map { dtos -> dtos.map { it.toItem() } }

    /**
     * 观察本地缓存的 __PASCAL__ 列表
     *
     * 返回 `Flow<ApiResult<List<__PASCAL__Item>>>`，便于 UI 层直接配合
     * [com.pai.app.core.base.asResult] 或单独订阅。Room Flow 内部把
     * [__PASCAL__Entity] 映射为 [__PASCAL__Item] 后暴露，Entity 不外泄。
     */
    override fun observe__PASCAL__s(): Flow<ApiResult<List<__PASCAL__Item>>> =
        __CAMEL__Dao.observeAll()
            .map { entities -> ApiResult.Success(entities.map { it.toItem() }) }

    /**
     * 将网络数据同步到本地缓存
     *
     * 演示 DTO → Entity 转换（Entity 仅供 Repository 内部使用）。
     * 本方法不属于 [__PASCAL__Repository] 接口（接口不暴露 DTO 类型，KMP-ready），
     * 仅供 Impl 内部或测试调用。
     */
    suspend fun syncToCache(dtos: List<__PASCAL__Dto>) {
        __CAMEL__Dao.upsertAll(dtos.map { it.toEntity() })
    }

    // ------------------------------------------------------------------------
    // 内部转换：DTO ↔ Entity ↔ domain model（不暴露给外部）
    // ------------------------------------------------------------------------

    /** DTO → domain model */
    private fun __PASCAL__Dto.toItem(): __PASCAL__Item = __PASCAL__Item(
        id = id,
        name = name,
        description = description,
    )

    /** Entity → domain model */
    private fun __PASCAL__Entity.toItem(): __PASCAL__Item = __PASCAL__Item(
        id = id,
        name = name,
        description = description,
    )

    /** DTO → Entity */
    private fun __PASCAL__Dto.toEntity(): __PASCAL__Entity = __PASCAL__Entity(
        id = id,
        name = name,
        description = description,
    )
}
KOTLIN
}

gen_repository_test() {
    local pascal="$1"
    local camel="$2"
    cat <<'KOTLIN' | sed "s/__PASCAL__/$pascal/g; s/__CAMEL__/$camel/g"
// ============================================================================
// __PASCAL__RepositoryTest.kt
// __PASCAL__RepositoryImpl 单元测试：网络→domain model 映射 / Error 路径 / Room 缓存
// 决策 1 + 决策 8 + P1-1：测试构造 __PASCAL__RepositoryImpl，断言对接口 __PASCAL__Repository
// 由 scripts/new-repository.sh 生成
// ============================================================================

package com.pai.app.core.data

import com.pai.app.core.base.ApiResult
import com.pai.app.core.base.AppException
import com.pai.app.core.database.dao.__PASCAL__Dao
import com.pai.app.core.database.entity.__PASCAL__Entity
import com.pai.app.core.domain.__PASCAL__Repository
import com.pai.app.core.domain.model.__PASCAL__Item
import com.pai.app.core.network.AppApi
import com.pai.app.core.network.model.__PASCAL__Dto
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

/**
 * [__PASCAL__RepositoryImpl] 单元测试
 *
 * 决策 P1-1：本测试构造 [__PASCAL__RepositoryImpl]（实现），接口方法
 * （[get__PASCAL__s] / [observe__PASCAL__s]）断言类型为 [__PASCAL__Repository]；
 * `syncToCache` 是 Impl 具体方法（不在接口中，因接口不暴露 [__PASCAL__Dto] 以保持
 * KMP-ready），故需要 Impl 类型变量调用。
 *
 * 决策 1：Repository 用 `safeApiCall { api.xxx() }` 包装成 ApiResult，
 * 并把 DTO 列表映射为 domain model [__PASCAL__Item] 列表对外暴露。
 * 决策 8：Entity / DTO 不外泄 —— [__PASCAL__Repository.get__PASCAL__s] 返回
 * `ApiResult<List<__PASCAL__Item>>`，调用方拿不到 [__PASCAL__Entity] 或 [__PASCAL__Dto]。
 *
 * 验证：
 * - get__PASCAL__s() Success 路径：api 返回 DTOs → ApiResult.Success + 映射为 __PASCAL__Item
 * - get__PASCAL__s() Error 路径：api 抛 IOException → ApiResult.Error + NetworkException
 * - observe__PASCAL__s() Success 路径：Room Flow 的 Entity 列表映射为 __PASCAL__Item 列表
 * - syncToCache() 把 DTO 列表 upsertAll 到 DAO（Entity 不外泄）
 *
 * 参考 ExampleRepositoryTest.kt 的完整测试模式。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class __PASCAL__RepositoryTest {


    // 决策 P1-1：用 Impl 类型变量 —— 需要调用 syncToCache（Impl 具体方法，不在接口中）
    private lateinit var repository: __PASCAL__RepositoryImpl

    @Before
    fun setUp() {
        repository = __PASCAL__RepositoryImpl(api, __CAMEL__Dao)
    }

    @Test
    fun `get__PASCAL__s Success 路径返回 ApiResult Success 且映射 DTO 为 __PASCAL__Item`() = runTest {
        val dtos = listOf(
            __PASCAL__Dto(id = 1L, name = "Item1", description = "Desc1"),
            __PASCAL__Dto(id = 2L, name = "Item2", description = null),
        )
        coEvery { api.get__PASCAL__s() } returns dtos

        val result = repository.get__PASCAL__s()

        assertTrue(result is ApiResult.Success)
        val items = (result as ApiResult.Success).data
        assertEquals(2, items.size)
        assertEquals(__PASCAL__Item(1L, "Item1", "Desc1"), items[0])
        assertEquals(__PASCAL__Item(2L, "Item2", null), items[1])
    }

    @Test
    fun `get__PASCAL__s api 抛 IOException 返回 ApiResult Error NetworkException`() = runTest {
        coEvery { api.get__PASCAL__s() } throws IOException("network down")

        val result = repository.get__PASCAL__s()

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is AppException.NetworkException)
        assertEquals("network down", error.exception.message)
    }

    @Test
    fun `observe__PASCAL__s 把 Room Flow 的 Entity 列表映射为 __PASCAL__Item 列表`() = runTest {
        val entities = listOf(
            __PASCAL__Entity(id = 10L, name = "Cached1", description = "d1"),
            __PASCAL__Entity(id = 20L, name = "Cached2", description = null),
        )
        coEvery { __CAMEL__Dao.observeAll() } returns flowOf(entities)

        val result = repository.observe__PASCAL__s().first()

        assertTrue(result is ApiResult.Success)
        val items = (result as ApiResult.Success).data
        assertEquals(2, items.size)
        assertEquals(__PASCAL__Item(10L, "Cached1", "d1"), items[0])
        assertEquals(__PASCAL__Item(20L, "Cached2", null), items[1])
    }

    @Test
    fun `syncToCache 把 DTO 列表 upsertAll 到 DAO`() = runTest {
        val dtos = listOf(
            __PASCAL__Dto(id = 1L, name = "Item1", description = "Desc1"),
            __PASCAL__Dto(id = 2L, name = "Item2", description = null),
        )

        repository.syncToCache(dtos)

        // 决策 8：syncToCache 内部把 DTO 转 Entity 后写入 DAO，外部不感知 Entity 类型
        coVerify {
            __CAMEL__Dao.upsertAll(match { entities ->
                entities.size == 2 &&
                    entities[0].id == 1L &&
                    entities[0].name == "Item1" &&
                    entities[0].description == "Desc1" &&
                    entities[1].id == 2L &&
                    entities[1].name == "Item2"
            })
        }
    }
}
KOTLIN
}

# ---------------------------------------------------------------------------
# write_or_preview <path>
# ---------------------------------------------------------------------------
write_or_preview() {
    local path="$1"
    if [ $DRY_RUN -eq 1 ]; then
        printf '%s[DRY]%s would create: %s\n' "$BLUE" "$RESET" "$path"
        return 0
    fi
    if [ -f "$path" ]; then
        if [ $FORCE -eq 1 ]; then
            warn "覆盖已存在文件: $path"
        else
            warn "已存在（跳过）: $path  使用 --force 覆盖"
            record_skip "$path"
            return 1
        fi
    fi
    mkdir -p "$(dirname "$path")"
    return 0
}

# ---------------------------------------------------------------------------
# File-edit operations
# ---------------------------------------------------------------------------

# Edit AppDatabase.kt:
#   1. Add <Name>Entity::class to entities = [...]
#   2. Increment version = N → N+1
#   3. Add abstract fun <name>Dao(): <Name>Dao
edit_app_database() {
    local path="$1"

    if [ $DRY_RUN -eq 1 ]; then
        printf '%s[DRY]%s would edit (add entity + version+1 + abstract dao): %s\n' "$BLUE" "$RESET" "$path"
        record_modify "$path"
        return 0
    fi

    if [ $FORCE -eq 0 ] && grep -q "${PASCAL}Entity::class" "$path" 2>/dev/null; then
        warn "AppDatabase.kt 已含 ${PASCAL}Entity，跳过"
        record_skip "$path"
        return 0
    fi

    if python3 - "$path" "$PASCAL" "$CAMEL" "$FORCE" <<'PY'; then
import sys, re

path = sys.argv[1]
pascal = sys.argv[2]
camel = sys.argv[3]
force = sys.argv[4] == "1"

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

entity_class = f"{pascal}Entity::class"

# Idempotency
if entity_class in content and not force:
    print("SKIP: entity already in AppDatabase", file=sys.stderr)
    sys.exit(0)

# 1. Add <Name>Entity::class to entities = [...]
# Pattern: entities = [ ... ]   (single line OR multiline)
# Strategy: find `entities = [` then the matching `]`, insert before `]`.
if entity_class not in content:
    m = re.search(r"entities\s*=\s*\[", content)
    if m is None:
        print("ERROR: cannot find `entities = [` in AppDatabase.kt", file=sys.stderr)
        sys.exit(1)
    bracket_open = m.end() - 1  # position of `[`
    # Find matching `]` (assume no nested brackets in entities array)
    bracket_close = content.find("]", bracket_open)
    if bracket_close == -1:
        print("ERROR: cannot find closing `]` of entities array", file=sys.stderr)
        sys.exit(1)
    # Check if array is empty `[]` or has content `[A, B]`
    inner = content[bracket_open + 1:bracket_close].strip()
    if inner == "":
        new_inner = f" {entity_class} "
    else:
        new_inner = f"{inner}, {entity_class}"
    content = content[:bracket_open + 1] + new_inner + content[bracket_close:]

# 2. Increment version = N → N+1
# Pattern: `version = <int>`
def bump_version(match):
    n = int(match.group(1))
    return f"version = {n + 1}"

new_content, n_subs = re.subn(r"version\s*=\s*(\d+)", bump_version, content, count=1)
if n_subs == 0:
    print("ERROR: cannot find `version = N` in AppDatabase.kt", file=sys.stderr)
    sys.exit(1)
content = new_content

# 3. Add `abstract fun <name>Dao(): <Name>Dao` before the class's closing `}`
# Strategy: find the LAST `}` in the file (class closing brace)
# Insert before it, with proper indentation (4 spaces)
dao_method = f"\n    /** {pascal} 表 DAO（由 new-repository.sh 生成） */\n    abstract fun {camel}Dao(): {pascal}Dao\n"

# Find the last `}` in the file
last_brace = content.rfind("}")
if last_brace == -1:
    print("ERROR: cannot find class closing brace in AppDatabase.kt", file=sys.stderr)
    sys.exit(1)

# Insert before the last `}`
# Ensure there's a blank line separator if needed
content = content[:last_brace] + dao_method + content[last_brace:]

# Also add the imports for the new Entity AND the new DAO
# (DAO is in com.pai.app.core.database.dao, AppDatabase is in com.pai.app.core.database —
#  different package, so DAO import is REQUIRED for `abstract fun xxxDao(): XxxDao`)
entity_import = f"import com.pai.app.core.database.entity.{pascal}Entity"
if entity_import not in content:
    pat = re.compile(r"^(import com\.pai\.app\.core\.database\.entity\..*\n)", re.MULTILINE)
    matches = list(pat.finditer(content))
    if matches:
        last = matches[-1]
        content = content[:last.end()] + entity_import + "\n" + content[last.end():]

dao_import = f"import com.pai.app.core.database.dao.{pascal}Dao"
if dao_import not in content:
    pat = re.compile(r"^(import com\.pai\.app\.core\.database\.dao\..*\n)", re.MULTILINE)
    matches = list(pat.finditer(content))
    if matches:
        last = matches[-1]
        content = content[:last.end()] + dao_import + "\n" + content[last.end():]

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
print("OK")
PY
        ok "已编辑（追加 ${PASCAL}Entity + ${PASCAL}Dao import + version+1 + abstract ${CAMEL}Dao()）: $path"
        record_modify "$path"
    else
        warn "AppDatabase.kt 自动编辑失败，请手动修改："
        printf '  1. entities = [...] 数组追加: %sEntity::class\n' "$PASCAL"
        printf '  2. version = N → N+1\n'
        printf '  3. class body 内追加: abstract fun %sDao(): %sDao\n' "$CAMEL" "$PASCAL"
        printf '  4. 顶部 import: com.pai.app.core.database.entity.%sEntity\n' "$PASCAL"
        printf '  5. 顶部 import: com.pai.app.core.database.dao.%sDao\n' "$PASCAL"
        record_manual "$path (add ${PASCAL}Entity + ${PASCAL}Dao import + version bump + ${CAMEL}Dao())"
    fi
}

# Edit DatabaseModule.kt: add @Provides fun provide<Name>Dao(db: AppDatabase): <Name>Dao
edit_database_module() {
    local path="$1"

    if [ $DRY_RUN -eq 1 ]; then
        printf '%s[DRY]%s would edit (add @Provides provide%sDao): %s\n' "$BLUE" "$RESET" "$PASCAL" "$path"
        record_modify "$path"
        return 0
    fi

    if [ $FORCE -eq 0 ] && grep -q "fun provide${PASCAL}Dao" "$path" 2>/dev/null; then
        warn "DatabaseModule.kt 已含 provide${PASCAL}Dao，跳过"
        record_skip "$path"
        return 0
    fi

    if python3 - "$path" "$PASCAL" "$CAMEL" "$FORCE" <<'PY'; then
import sys, re

path = sys.argv[1]
pascal = sys.argv[2]
camel = sys.argv[3]
force = sys.argv[4] == "1"

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

method_name = f"provide{pascal}Dao"

# Idempotency
if re.search(r"fun\s+" + re.escape(method_name) + r"\b", content):
    if not force:
        print("SKIP: provide method already in DatabaseModule", file=sys.stderr)
        sys.exit(0)

# 1. Add DAO import after the last `import com.pai.app.core.database.dao.` line
dao_import = f"import com.pai.app.core.database.dao.{pascal}Dao"
if dao_import not in content:
    pat = re.compile(r"^(import com\.pai\.app\.core\.database\.dao\..*\n)", re.MULTILINE)
    matches = list(pat.finditer(content))
    if matches:
        last = matches[-1]
        content = content[:last.end()] + dao_import + "\n" + content[last.end():]

# 2. Append @Provides method before object's closing `}`
provides_method = (
    f"\n    /** 提供 {pascal} 表 DAO（由 new-repository.sh 生成） */\n"
    f"    @Provides\n"
    f"    @Singleton\n"
    f"    fun {method_name}(db: AppDatabase): {pascal}Dao = db.{camel}Dao()\n"
)

# Find the LAST `}` in the file (object closing brace)
last_brace = content.rfind("}")
if last_brace == -1:
    print("ERROR: cannot find object closing brace in DatabaseModule.kt", file=sys.stderr)
    sys.exit(1)
content = content[:last_brace] + provides_method + content[last_brace:]

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
print("OK")
PY
        ok "已编辑（追加 @Provides provide${PASCAL}Dao）: $path"
        record_modify "$path"
    else
        warn "DatabaseModule.kt 自动编辑失败，请手动追加："
        printf '  import com.pai.app.core.database.dao.%sDao\n' "$PASCAL"
        printf '  @Provides @Singleton\n'
        printf '  fun provide%sDao(db: AppDatabase): %sDao = db.%sDao()\n' "$PASCAL" "$PASCAL" "$CAMEL"
        record_manual "$path (add @Provides provide${PASCAL}Dao)"
    fi
}

# Edit AppApi.kt: append @GET suspend fun get<Name>s(): List<<Name>Dto>
edit_app_api() {
    local path="$1"

    if [ $DRY_RUN -eq 1 ]; then
        printf '%s[DRY]%s would edit (append get%ss): %s\n' "$BLUE" "$RESET" "$PASCAL" "$path"
        record_modify "$path"
        return 0
    fi

    if [ $FORCE -eq 0 ] && grep -q "fun get${PASCAL}s" "$path" 2>/dev/null; then
        warn "AppApi.kt 已含 get${PASCAL}s，跳过"
        record_skip "$path"
        return 0
    fi

    if python3 - "$path" "$PASCAL" "$CAMEL" "$SNAKE" "$FORCE" <<'PY'; then
import sys, re

path = sys.argv[1]
pascal = sys.argv[2]
camel = sys.argv[3]
snake = sys.argv[4]
force = sys.argv[5] == "1"

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

method_name = f"get{pascal}s"

# Idempotency
if re.search(r"fun\s+" + re.escape(method_name) + r"\b", content):
    if not force:
        print("SKIP: get method already in AppApi", file=sys.stderr)
        sys.exit(0)

# 1. Add DTO import after the last `import com.pai.app.core.network.model.` line
dto_import = f"import com.pai.app.core.network.model.{pascal}Dto"
if dto_import not in content:
    pat = re.compile(r"^(import com\.pai\.app\.core\.network\.model\..*\n)", re.MULTILINE)
    matches = list(pat.finditer(content))
    if matches:
        last = matches[-1]
        content = content[:last.end()] + dto_import + "\n" + content[last.end():]

# 2. Append @GET method before interface's closing `}`
api_method = (
    f"\n    /**\n"
    f"     * 获取 {pascal} 列表（由 new-repository.sh 生成）\n"
    f"     *\n"
    f"     * 业务方按需调整路径（默认 `/{snake}s`）与参数。\n"
    f"     */\n"
    f"    @GET(\"{snake}s\")\n"
    f"    suspend fun {method_name}(): List<{pascal}Dto>\n"
)

# Find the LAST `}` in the file (interface closing brace)
last_brace = content.rfind("}")
if last_brace == -1:
    print("ERROR: cannot find interface closing brace in AppApi.kt", file=sys.stderr)
    sys.exit(1)
content = content[:last_brace] + api_method + content[last_brace:]

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
print("OK")
PY
        ok "已编辑（追加 @GET get${PASCAL}s）: $path"
        record_modify "$path"
    else
        warn "AppApi.kt 自动编辑失败，请手动追加："
        printf '  import com.pai.app.core.network.model.%sDto\n' "$PASCAL"
        printf '  @GET("%ss")\n' "$SNAKE"
        printf '  suspend fun get%s(): List<%sDto>\n' "$PASCAL" "$PASCAL" "$PASCAL"
        record_manual "$path (add @GET get${PASCAL}s)"
    fi
}

# Edit DataModule.kt (core/data/di/DataModule.kt):
#   追加 @Binds @Singleton abstract fun bind<Name>Repository(impl: <Name>RepositoryImpl): <Name>Repository
#   决策 P1-1：Hilt 通过 @Binds 把接口绑定到 Impl，feature 注入接口而非 Impl
edit_data_module() {
    local path="$1"

    if [ $DRY_RUN -eq 1 ]; then
        printf '%s[DRY]%s would edit (append @Binds bind%sRepository): %s\n' "$BLUE" "$RESET" "$PASCAL" "$path"
        record_modify "$path"
        return 0
    fi

    if [ $FORCE -eq 0 ] && grep -q "fun bind${PASCAL}Repository" "$path" 2>/dev/null; then
        warn "DataModule.kt 已含 bind${PASCAL}Repository，跳过"
        record_skip "$path"
        return 0
    fi

    if python3 - "$path" "$PASCAL" "$FORCE" <<'PY'; then
import sys, re

path = sys.argv[1]
pascal = sys.argv[2]
force = sys.argv[3] == "1"

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

bind_method_name = f"bind{pascal}Repository"

# Idempotency
if re.search(r"fun\s+" + re.escape(bind_method_name) + r"\b", content):
    if not force:
        print("SKIP: bind method already in DataModule", file=sys.stderr)
        sys.exit(0)

# 1. Add Impl import + Interface import (after the last matching import line)
impl_import = f"import com.pai.app.core.data.{pascal}RepositoryImpl"
iface_import = f"import com.pai.app.core.domain.{pascal}Repository"

for imp in (impl_import, iface_import):
    if imp not in content:
        pat = re.compile(r"^(import com\.pai\.app\.core\.(data|domain)\.[^\n]+\n)", re.MULTILINE)
        matches = list(pat.finditer(content))
        if matches:
            last = matches[-1]
            content = content[:last.end()] + imp + "\n" + content[last.end():]

# 2. Append @Binds abstract method before class's closing `}`
binds_method = (
    f"\n    /**\n"
    f"     * 绑定 [{pascal}Repository] 接口到 [{pascal}RepositoryImpl] 实现\n"
    f"     *\n"
    f"     * - feature 注入 `{pascal}Repository` 时，Hilt 解析为 `{pascal}RepositoryImpl` 单例\n"
    f"     */\n"
    f"    @Binds\n"
    f"    @Singleton\n"
    f"    abstract fun {bind_method_name}(impl: {pascal}RepositoryImpl): {pascal}Repository\n"
)

# Find the LAST `}` in the file (abstract class closing brace)
last_brace = content.rfind("}")
if last_brace == -1:
    print("ERROR: cannot find class closing brace in DataModule.kt", file=sys.stderr)
    sys.exit(1)
content = content[:last_brace] + binds_method + content[last_brace:]

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
print("OK")
PY
        ok "已编辑（追加 @Binds bind${PASCAL}Repository）: $path"
        record_modify "$path"
    else
        warn "DataModule.kt 自动编辑失败，请手动追加："
        printf '  import com.pai.app.core.data.%sRepositoryImpl\n' "$PASCAL"
        printf '  import com.pai.app.core.domain.%sRepository\n' "$PASCAL"
        printf '  @Binds @Singleton\n'
        printf '  abstract fun bind%sRepository(impl: %sRepositoryImpl): %sRepository\n' "$PASCAL" "$PASCAL" "$PASCAL"
        record_manual "$path (add @Binds bind${PASCAL}Repository)"
    fi
}

# ---------------------------------------------------------------------------
# Main flow
# ---------------------------------------------------------------------------

info "Repository: $NAME  →  PascalCase: $PASCAL  camelCase: $CAMEL  snake_case: $SNAKE"
if [ $DRY_RUN -eq 1 ]; then info "DRY-RUN 模式：仅预览，不写入"; fi
if [ $FORCE -eq 1 ];  then info "FORCE 模式：覆盖已存在文件"; fi
echo ""

# 1. Generate new files
if write_or_preview "$DTO_FILE"; then
    if [ $DRY_RUN -eq 0 ]; then
        gen_dto "$PASCAL" "$SNAKE" > "$DTO_FILE"
        ok "已创建: $DTO_FILE"
    fi
    record_create "$DTO_FILE"
fi

if write_or_preview "$ENTITY_FILE"; then
    if [ $DRY_RUN -eq 0 ]; then
        gen_entity "$PASCAL" "$SNAKE" > "$ENTITY_FILE"
        ok "已创建: $ENTITY_FILE"
    fi
    record_create "$ENTITY_FILE"
fi

if write_or_preview "$DAO_FILE"; then
    if [ $DRY_RUN -eq 0 ]; then
        gen_dao "$PASCAL" "$CAMEL" "$SNAKE" > "$DAO_FILE"
        ok "已创建: $DAO_FILE"
    fi
    record_create "$DAO_FILE"
fi

if write_or_preview "$ITEM_FILE"; then
    if [ $DRY_RUN -eq 0 ]; then
        gen_item "$PASCAL" > "$ITEM_FILE"
        ok "已创建: $ITEM_FILE"
    fi
    record_create "$ITEM_FILE"
fi

# 决策 P1-1：接口在 core/domain/（KMP-ready），实现在 core/data/（@Inject + @Binds 绑定）
if write_or_preview "$REPO_INTERFACE_FILE"; then
    if [ $DRY_RUN -eq 0 ]; then
        gen_repository_interface "$PASCAL" "$CAMEL" > "$REPO_INTERFACE_FILE"
        ok "已创建: $REPO_INTERFACE_FILE"
    fi
    record_create "$REPO_INTERFACE_FILE"
fi

if write_or_preview "$REPO_IMPL_FILE"; then
    if [ $DRY_RUN -eq 0 ]; then
        gen_repository_impl "$PASCAL" "$CAMEL" > "$REPO_IMPL_FILE"
        ok "已创建: $REPO_IMPL_FILE"
    fi
    record_create "$REPO_IMPL_FILE"
fi

if write_or_preview "$REPO_TEST_FILE"; then
    if [ $DRY_RUN -eq 0 ]; then
        gen_repository_test "$PASCAL" "$CAMEL" > "$REPO_TEST_FILE"
        ok "已创建: $REPO_TEST_FILE"
    fi
    record_create "$REPO_TEST_FILE"
fi

echo ""

# 2. Edit existing files
edit_app_database "$APP_DATABASE"
edit_database_module "$DATABASE_MODULE"
edit_app_api "$APP_API"
edit_data_module "$DATA_MODULE"

# 3. AppApiTest reminder (auto-edit risky, just print)
echo ""
info "手动追加 MockWebServer 测试用例到 docs/rules/10-testing.md 风格："
printf '  // 在 AppApiTest.kt 内追加：\n'
printf '  @Test\n'
printf '  fun `HTTP 200 返回 %sDto 列表`() = runTest {\n' "$PASCAL"
printf '      mockWebServer.enqueue(MockResponse().setResponseCode(200)\n'
printf '          .setBody("[{\\"id\\":1,\\"name\\":\\"item\\"}]"))\n'
printf '      val result = safeApiCall { api.get%ss() }\n' "$PASCAL"
printf '      assertTrue(result is ApiResult.Success)\n'
printf '  }\n'
record_manual "$APP_API_TEST (append MockWebServer test case for get${PASCAL}s)"

# 4. Room Migration reminder (since version was bumped, business user needs migration)
info "Room version 已 +1：业务方需在 DatabaseModule.provideDatabase 中 .addMigrations(Migration_N_to_N+1) 注册迁移"
record_manual "DatabaseModule.kt: register Room Migration for version bump (see docs/rules/08-state-management.md)"

# ---------------------------------------------------------------------------
# Summary
# ---------------------------------------------------------------------------
echo ""
printf '%s=== Summary ===%s\n' "$BOLD" "$RESET"
printf '%sCreated (%d):%s\n' "$GREEN" "${#CREATED_FILES[@]}" "$RESET"
for f in "${CREATED_FILES[@]}"; do printf '  + %s\n' "$f"; done
printf '%sModified (%d):%s\n' "$YELLOW" "${#MODIFIED_FILES[@]}" "$RESET"
for f in "${MODIFIED_FILES[@]}"; do printf '  ~ %s\n' "$f"; done
if [ ${#SKIPPED_FILES[@]} -gt 0 ]; then
    printf '%sSkipped (%d):%s\n' "$YELLOW" "${#SKIPPED_FILES[@]}" "$RESET"
    for f in "${SKIPPED_FILES[@]}"; do printf '  - %s\n' "$f"; done
fi
if [ ${#MANUAL_HINTS[@]} -gt 0 ]; then
    printf '%sManual follow-up (%d):%s\n' "$YELLOW" "${#MANUAL_HINTS[@]}" "$RESET"
    for f in "${MANUAL_HINTS[@]}"; do printf '  ! %s\n' "$f"; done
fi
echo ""
if [ $DRY_RUN -eq 1 ]; then
    info "DRY-RUN：未实际写入。去掉 --dry-run 实际生成代码。"
else
    ok "完成。下一步：填充业务逻辑 + 运行 ./gradlew :app:compileDebugKotlin 验证。"
fi
