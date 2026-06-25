// ============================================================================
// HomeViewModel.kt
// 通用空白首页 ViewModel
// 业务方拉取脚手架后，从此处开始开发业务
// ============================================================================

package com.pai.app.feature.home

import com.pai.app.core.base.BaseViewModel
import com.pai.app.navigation.AppNavigator
import com.pai.app.navigation.UserState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * 空白首页 ViewModel
 *
 * 脚手架默认入口，展示「开始开发」提示与导航入口。
 * 业务方拉取后，可在此处替换为真实业务列表/Feed/Tab 等。
 *
 * 当前提供的导航入口：
 * - [onOpenGallery] → 组件 Gallery
 * - [onOpenSettings] → 设置页
 * - [onOpenProfile] → 个人中心
 */
@HiltViewModel
internal class HomeViewModel @Inject constructor(
    navigator: AppNavigator,
    userState: UserState,
) : BaseViewModel(navigator, userState)
