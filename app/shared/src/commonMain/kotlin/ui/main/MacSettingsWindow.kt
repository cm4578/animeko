/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.drop
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.navigation.NavRoutes
import me.him188.ani.app.navigation.SettingsTab
import me.him188.ani.app.shared.loadOpenSourceLibrariesJsons
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.Toast
import me.him188.ani.app.ui.foundation.widgets.ToastViewModel
import me.him188.ani.app.ui.foundation.widgets.Toaster
import me.him188.ani.app.ui.settings.MacSettingsAccountInfo
import me.him188.ani.app.ui.settings.MacSettingsCategory
import me.him188.ani.app.ui.settings.MacSettingsWindowContent
import me.him188.ani.app.ui.settings.SettingsViewModel
import me.him188.ani.app.ui.settings.account.ProfileViewModel
import me.him188.ani.app.ui.settings.notification.NotificationSettingsViewModel
import me.him188.ani.app.ui.update.AppUpdateViewModel

/**
 * macOS 设定窗口的开关状态 (05 B4). 同一时间只有一个设定窗口; 窗口已开着时 [open] 把它带到前面.
 */
@Stable
class MacSettingsWindowState {
    var isOpen by mutableStateOf(false)
        private set

    /**
     * 选中的分类. 关闭窗口后保留, 下次打开回到同一分类.
     */
    var category: MacSettingsCategory by mutableStateOf(MacSettingsCategory.Tab(SettingsTab.PROFILE))

    /**
     * 每次 [open] 加一. 窗口已开着时, 宿主据此把窗口带到前面.
     */
    var openRequests by mutableIntStateOf(0)
        private set

    /**
     * 打开设定窗口. [tab] 不为 `null` 时切换到该分类, 否则停在上次的分类.
     */
    fun open(tab: SettingsTab? = null) {
        if (tab != null) category = MacSettingsCategory.Tab(tab)
        isOpen = true
        openRequests++
    }

    fun close() {
        isOpen = false
    }
}

/**
 * macOS 设定窗口的宿主, 由 app-desktop 提供: 窗口取用 `LocalSecondaryWindowFrame`, 并接上 App 菜单的「设定…」(⌘,).
 *
 * 没有宿主 (其他平台、UI 测试) 时, 设定在主窗口内打开.
 */
@Stable
interface MacSettingsWindowHost {
    /**
     * 设定窗口. 一直组合; [MacSettingsWindowState.isOpen] 时才建立窗口并组合 [content].
     */
    @Composable
    fun SettingsWindow(state: MacSettingsWindowState, content: @Composable () -> Unit)

    /**
     * 把主窗口带到前面.
     */
    fun showMainWindow()
}

val LocalMacSettingsWindowHost = staticCompositionLocalOf<MacSettingsWindowHost?> { null }

/**
 * macOS 主窗口用的 [AniNavigator]: 打开设定 ([navigateSettings] 或 [NavRoutes.Settings]) 时打开设定窗口, 其他导航照常.
 */
internal class MacSettingsWindowNavigator(
    private val delegate: AniNavigator,
    private val settingsWindow: MacSettingsWindowState,
) : AniNavigator by delegate {
    override fun navigate(route: NavRoutes) {
        if (route is NavRoutes.Settings) {
            settingsWindow.open(route.tab)
        } else {
            delegate.navigate(route)
        }
    }

    override fun navigateSettings(tab: SettingsTab?) {
        settingsWindow.open(tab)
    }
}

/**
 * 设定窗口的画面 (05 B4, 04 §3.6).
 *
 * 窗口内的 ViewModel 放在窗口自己的 [ViewModelStoreOwner], 关闭窗口时清除. 帐号窗格与侧边栏的数据来自主窗口的 [sharedViewModel].
 * 登录、通知中心、编辑资料源等页面在主窗口打开, 打开后把主窗口带到前面.
 *
 * @param navigator 主窗口的 [MacSettingsWindowNavigator].
 */
@Composable
internal fun MacSettingsWindowScreen(
    state: MacSettingsWindowState,
    sharedViewModel: MainScreenSharedViewModel,
    navigator: AniNavigator,
    onShowMainWindow: () -> Unit,
    onOpenPlayFlowDebugWindow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selfInfo by sharedViewModel.selfInfo.collectAsStateWithLifecycle()
    val showNotificationDot by sharedViewModel.showNotificationDot.collectAsStateWithLifecycle()
    val notificationUnreadCount by sharedViewModel.notificationUnreadCount.collectAsStateWithLifecycle()
    val pendingSyncCount by sharedViewModel.bangumiPendingSyncCount.collectAsStateWithLifecycle()
    val collectionCounts by sharedViewModel.subjectCollectionCounts.collectAsStateWithLifecycle()

    // 页面在主窗口打开: 主窗口的栈顶变了就把主窗口带到前面. 用户本来就在主窗口操作时, 带到前面没有效果.
    LaunchedEffect(navigator) {
        snapshotFlow { navigator.backStack.lastOrNull() }
            .drop(1)
            .collect { onShowMainWindow() }
    }

    CompositionLocalProvider(LocalViewModelStoreOwner provides rememberWindowViewModelStoreOwner()) {
        val settingsViewModel = viewModel { SettingsViewModel() }
        val profileViewModel = viewModel { ProfileViewModel() }
        val notificationSettingsViewModel = viewModel { NotificationSettingsViewModel() }
        // 与「软件更新」窗格的 SoftwareUpdateGroup 是同一个实例. 打开窗口时自动检查一次 (同主页的更新提示, 遵循自动检查设定)
        val updateViewModel = viewModel { AppUpdateViewModel() }
        LaunchedEffect(updateViewModel) {
            updateViewModel.startAutomaticCheckLatestVersion()
        }
        val updatePresentation by updateViewModel.presentationFlow.collectAsStateWithLifecycle()
        val toastViewModel = viewModel { ToastViewModel() }

        CompositionLocalProvider(
            LocalNavigator provides navigator,
            LocalToaster provides remember(toastViewModel) {
                object : Toaster {
                    override fun toast(text: String) {
                        toastViewModel.show(text)
                    }
                }
            },
        ) {
            Box(modifier.fillMaxSize()) {
                MacSettingsWindowContent(
                    settingsViewModel,
                    profileViewModel,
                    notificationSettingsViewModel.notificationSettings,
                    account = MacSettingsAccountInfo(
                        selfInfo = selfInfo,
                        collectionCounts = collectionCounts,
                        pendingSyncCount = pendingSyncCount,
                        notificationUnreadCount = notificationUnreadCount,
                        showNotificationDot = showNotificationDot,
                        hasNewVersion = updatePresentation.newVersion != null,
                    ),
                    selected = state.category,
                    onSelect = { state.category = it },
                    onNavigateToEmailLogin = { navigator.navigateEmailLoginStart() },
                    onNavigateToOAuth = { navigator.navigateOAuthAuthorize(it.id) },
                    onOpenNotifications = { navigator.navigateNotifications() },
                    loadOpenSourceLibrariesJsons = ::loadOpenSourceLibrariesJsons,
                    Modifier.fillMaxSize(),
                    onOpenPlayFlowDebugWindow = {
                        // 悬浮窗盖在主窗口的页面上
                        onOpenPlayFlowDebugWindow()
                        onShowMainWindow()
                    },
                )
                val toastShowing by toastViewModel.showing.collectAsStateWithLifecycle()
                val toastContent by toastViewModel.content.collectAsStateWithLifecycle()
                Toast({ toastShowing }, { Text(toastContent) })
            }
        }
    }
}

/**
 * 跟随窗口的 [ViewModelStoreOwner]: 窗口关闭 (离开组合) 时清除其中的 ViewModel.
 */
@Composable
private fun rememberWindowViewModelStoreOwner(): ViewModelStoreOwner {
    val owner = remember {
        object : ViewModelStoreOwner {
            override val viewModelStore: ViewModelStore = ViewModelStore()
        }
    }
    DisposableEffect(owner) {
        onDispose { owner.viewModelStore.clear() }
    }
    return owner
}
