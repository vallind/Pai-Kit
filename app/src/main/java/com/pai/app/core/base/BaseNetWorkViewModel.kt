// ============================================================================
// BaseNetWorkViewModel.kt
// 网络请求 ViewModel 基类 - 借鉴 AndroidProject-Compose
// 继承 BaseViewModel，自动管理 Loading/Success/Error 三态
// 子类只需实现 requestApiFlow()，无需写 when 分支
// ============================================================================

package com.pai.app.core.base

import androidx.lifecycle.viewModelScope
import com.pai.app.navigation.AppNavigator
import com.pai.app.navigation.UserState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 网络请求 ViewModel 基类（不分页）
 *
 * 继承 [BaseViewModel]，自动管理 [BaseNetWorkUiState] 三态。
 * 子类只需：
 * 1. 实现 [requestApiFlow] 返回网络数据 Flow
 * 2. 在 init 中调用 [executeRequest]
 * 3. UI 层用 [uiState] 订阅 + [DSNetWorkView] 渲染
 *
 * 使用示例：
 * ```kotlin
 * @HiltViewModel
 * class ProductDetailViewModel @Inject constructor(
 *     navigator: AppNavigator,
 *     userState: UserState,
 *     private val repository: ProductRepository,
 * ) : BaseNetWorkViewModel<ProductDetail>(navigator, userState) {
 *
 *     override fun requestApiFlow(): Flow<ApiResult<ProductDetail>> {
 *         return repository.getProductDetail(productId).asResult()
 *     }
 *
 *     init { executeRequest() }
 * }
 * ```
 *
 * UI 层：
 * ```kotlin
 * @Composable
 * fun ProductDetailScreen(viewModel: ProductDetailViewModel = hiltViewModel()) {
 *     val uiState by viewModel.uiState.collectAsStateWithLifecycle()
 *     DSNetWorkView(
 *         uiState = uiState,
 *         onRetry = viewModel::retryRequest,
 *     ) { product ->
 *         // 成功态：渲染 product 数据
 *         ProductContent(product)
 *     }
 * }
 * ```
 *
 * @param T 成功态数据类型
 * @param navigator 全局导航器（BaseViewModel 注入）
 * @param userState 全局用户状态（BaseViewModel 注入）
 */
abstract class BaseNetWorkViewModel<T>(
    navigator: AppNavigator,
    userState: UserState,
) : BaseViewModel(navigator, userState) {

    /** 内部可变 UI 状态 */
    private val _uiState = MutableStateFlow<BaseNetWorkUiState<T>>(BaseNetWorkUiState.Loading)

    /** 对外暴露的三态 UI 状态 */
    val uiState: StateFlow<BaseNetWorkUiState<T>> = _uiState.asStateFlow()

    /**
     * 子类实现：返回网络数据 Flow
     *
     * 必须用 [asResult] 包装：
     * ```kotlin
     * override fun requestApiFlow() = repository.getData().asResult()
     * ```
     *
     * @return 三态 [ApiResult] Flow（Success / Error / Loading）
     */
    protected abstract fun requestApiFlow(): Flow<ApiResult<T>>

    /**
     * 触发请求
     *
     * 自动派发 Loading → Success/Error，子类在 init 中调用。
     */
    fun executeRequest() {
        _uiState.value = BaseNetWorkUiState.Loading
        ResultHandler.handle(
            scope = viewModelScope,
            flow = requestApiFlow(),
            onLoading = { _uiState.value = BaseNetWorkUiState.Loading },
            onSuccess = { data ->
                _uiState.value = if (isEmptyData(data)) {
                    BaseNetWorkUiState.Empty
                } else {
                    BaseNetWorkUiState.Success(data)
                }
                onRequestSuccess(data)
            },
            onError = { message, throwable ->
                // ResultHandler 已把 ApiResult.Error 拆成 (message, exception)
                _uiState.value = BaseNetWorkUiState.Error(message, throwable)
                onRequestError(message, throwable)
            },
        )
    }

    /**
     * 重试
     *
     * 重置为 Loading 并重新执行请求。
     * UI 层绑定到错误页的"重试"按钮。
     */
    fun retryRequest() {
        executeRequest()
    }

    /**
     * 请求成功钩子（可选覆写）
     *
     * 默认空实现。子类可覆写以插入副作用（如写缓存、埋点）。
     */
    protected open fun onRequestSuccess(data: T) {}

    /**
     * 请求失败钩子（可选覆写）
     *
     * 默认空实现。子类可覆写以插入副作用（如 Toast 提示）。
     */
    protected open fun onRequestError(message: String, throwable: Throwable?) {}

    /**
     * 判断数据是否为空（可选覆写）
     *
     * 默认对 List 判断 isEmpty()，其他类型返回 false。
     * 子类可覆写以自定义空数据逻辑。
     */
    protected open fun isEmptyData(data: T): Boolean {
        return when (data) {
            is Collection<*> -> data.isEmpty()
            is Array<*> -> data.isEmpty()
            else -> false
        }
    }

    /**
     * 获取成功态数据（非成功态返回 null）
     */
    fun getSuccessData(): T? = _uiState.value.getOrNull()
}
