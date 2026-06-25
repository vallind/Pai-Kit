// ============================================================================
// ExampleRepositoryTest.kt
// ExampleRepositoryImpl 单元测试：网络→domain model 映射 / Error 路径 / Room 缓存
// 决策 1 + 决策 8 + P1-1：测试构造 ExampleRepositoryImpl，断言对接口 ExampleRepository
// ============================================================================

package com.pai.app.core.data

import com.pai.app.core.base.ApiResult
import com.pai.app.core.base.AppException
import com.pai.app.core.database.dao.ExampleDao
import com.pai.app.core.database.entity.ExampleEntity
import com.pai.app.core.domain.ExampleRepository
import com.pai.app.core.domain.model.ExampleItem
import com.pai.app.core.network.AppApi
import com.pai.app.core.network.model.ExampleDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
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
 * [ExampleRepositoryImpl] 单元测试
 *
 * 决策 P1-1：本测试构造 [ExampleRepositoryImpl]（实现），接口方法（[getExamples] /
 * [observeExamples]）断言类型为 [ExampleRepository]；`syncToCache` 是 Impl
 * 具体方法（不在接口中，因接口不暴露 [ExampleDto] 以保持 KMP-ready），
 * 故需要 Impl 类型变量调用。
 *
 * 决策 1：Repository 用 `safeApiCall { api.xxx() }` 包装成 ApiResult，
 * 并把 DTO 列表映射为 domain model [ExampleItem] 列表对外暴露。
 * 决策 8：Entity / DTO 不外泄 —— [ExampleRepository.getExamples] 返回
 * `ApiResult<List<ExampleItem>>`，调用方拿不到 [ExampleEntity] 或 [ExampleDto]。
 *
 * 验证：
 * - getExamples() Success 路径：api 返回 DTOs → ApiResult.Success + 映射为 ExampleItem
 * - getExamples() Error 路径：api 抛 IOException → ApiResult.Error + NetworkException
 * - observeExamples() Success 路径：Room Flow 的 Entity 列表映射为 ExampleItem 列表
 * - syncToCache() 把 DTO 列表 upsertAll 到 DAO（Entity 不外泄）
 *
 * 用 MockK mock [AppApi] + [ExampleDao]；不依赖真实网络/数据库。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ExampleRepositoryTest {

    private val api: AppApi = mockk(relaxed = true)
    private val exampleDao: ExampleDao = mockk(relaxed = true)

    // 决策 P1-1：用 Impl 类型变量 —— 需要调用 syncToCache（Impl 具体方法，不在接口中）
    private lateinit var repository: ExampleRepositoryImpl

    @Before
    fun setUp() {
        repository = ExampleRepositoryImpl(api, exampleDao)
    }

    @Test
    fun `getExamples Success 路径返回 ApiResult Success 且映射 DTO 为 ExampleItem`() = runTest {
        val dtos = listOf(
            ExampleDto(id = 1L, name = "Item1", description = "Desc1"),
            ExampleDto(id = 2L, name = "Item2", description = null),
        )
        coEvery { api.getExamples() } returns dtos

        val result = repository.getExamples()

        assertTrue(result is ApiResult.Success)
        val items = (result as ApiResult.Success).data
        assertEquals(2, items.size)
        assertEquals(ExampleItem(1L, "Item1", "Desc1"), items[0])
        assertEquals(ExampleItem(2L, "Item2", null), items[1])
    }

    @Test
    fun `getExamples api 抛 IOException 返回 ApiResult Error NetworkException`() = runTest {
        coEvery { api.getExamples() } throws IOException("network down")

        val result = repository.getExamples()

        assertTrue(result is ApiResult.Error)
        val error = result as ApiResult.Error
        assertTrue(error.exception is AppException.NetworkException)
        assertEquals("network down", error.exception.message)
    }

    @Test
    fun `getExamples api 返回空列表 映射为空 ExampleItem 列表`() = runTest {
        coEvery { api.getExamples() } returns emptyList()

        val result = repository.getExamples()

        assertTrue(result is ApiResult.Success)
        assertEquals(0, (result as ApiResult.Success).data.size)
    }

    @Test
    fun `observeExamples 把 Room Flow 的 Entity 列表映射为 ExampleItem 列表`() = runTest {
        val entities = listOf(
            ExampleEntity(id = 10L, name = "Cached1", description = "d1"),
            ExampleEntity(id = 20L, name = "Cached2", description = null),
        )
        coEvery { exampleDao.observeAll() } returns flowOf(entities)

        val result = repository.observeExamples().first()

        assertTrue(result is ApiResult.Success)
        val items = (result as ApiResult.Success).data
        assertEquals(2, items.size)
        assertEquals(ExampleItem(10L, "Cached1", "d1"), items[0])
        assertEquals(ExampleItem(20L, "Cached2", null), items[1])
    }

    @Test
    fun `syncToCache 把 DTO 列表 upsertAll 到 DAO`() = runTest {
        val dtos = listOf(
            ExampleDto(id = 1L, name = "Item1", description = "Desc1"),
            ExampleDto(id = 2L, name = "Item2", description = null),
        )

        repository.syncToCache(dtos)

        // 决策 8：syncToCache 内部把 DTO 转 Entity 后写入 DAO，外部不感知 Entity 类型
        coVerify {
            exampleDao.upsertAll(match { entities ->
                entities.size == 2 &&
                    entities[0].id == 1L &&
                    entities[0].name == "Item1" &&
                    entities[0].description == "Desc1" &&
                    entities[1].id == 2L &&
                    entities[1].name == "Item2"
            })
        }
    }

    @Test
    fun `observeExamples 空缓存 返回 Success 空列表`() = runTest {
        coEvery { exampleDao.observeAll() } returns flowOf(emptyList())

        val result = repository.observeExamples().first()

        assertTrue(result is ApiResult.Success)
        assertEquals(0, (result as ApiResult.Success).data.size)
    }
}
