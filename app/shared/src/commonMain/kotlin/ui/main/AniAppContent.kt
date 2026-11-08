/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.main

import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.paging.compose.collectAsLazyPagingItemsWithLifecycle
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.models.subject.Tag
import me.him188.ani.app.domain.mediasource.jsonapi.JsonApiMediaSource
import me.him188.ani.app.domain.mediasource.rss.RssMediaSource
import me.him188.ani.app.domain.mediasource.web.SelectorMediaSource
import me.him188.ani.app.domain.notification.NotificationBadgeManager
import me.him188.ani.app.domain.search.SubjectSearchQuery
import me.him188.ani.app.domain.session.auth.OAuthPlatform
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.navigation.MainScreenPage
import me.him188.ani.app.navigation.NavRoutes
import me.him188.ani.app.navigation.OverrideNavigation
import me.him188.ani.app.navigation.SettingsTab
import me.him188.ani.app.navigation.SubjectDetailPlaceholder
import me.him188.ani.app.navigation.rememberAniBackStack
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.navigation.LocalBrowserNavigator
import me.him188.ani.app.shared.loadOpenSourceLibrariesJsons
import me.him188.ani.app.ui.adaptive.navigation.AniNavigationSuiteDefaults
import me.him188.ani.app.ui.bangumi.merge.BangumiMergeScreen
import me.him188.ani.app.ui.bangumi.merge.BangumiMergeViewModel
import me.him188.ani.app.ui.download.DownloadManagementScreen
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.createDownloadManagementViewModel
import me.him188.ani.app.ui.download.createSubjectDownloadsViewModel
import me.him188.ani.app.ui.download.details.AppleMediaCacheDetailsScreen
import me.him188.ani.app.ui.download.details.MediaCacheDetailsPageViewModel
import me.him188.ani.app.ui.download.details.MediaCacheDetailsScreen
import me.him188.ani.app.ui.download.details.MediaDetails
import me.him188.ani.app.ui.download.details.MediaDetailsLazyGrid
import me.him188.ani.app.ui.download.subject.AppleSubjectDownloadsScreen
import me.him188.ani.app.ui.download.subject.MacSubjectDownloadsScreen
import me.him188.ani.app.ui.download.subject.SubjectDownloadsScreen
import me.him188.ani.app.ui.exploration.schedule.AiringScheduleItemPresentation
import me.him188.ani.app.ui.exploration.schedule.AppleSchedulePage
import me.him188.ani.app.ui.exploration.schedule.MacSchedulePage
import me.him188.ani.app.ui.exploration.schedule.ScheduleScreen
import me.him188.ani.app.ui.exploration.schedule.ScheduleViewModel
import me.him188.ani.app.ui.exploration.trends.AppleTrendingRankingPage
import me.him188.ani.app.ui.exploration.trends.MacTrendingRankingPage
import me.him188.ani.app.ui.exploration.trends.TrendingRankingItemPresentation
import me.him188.ani.app.ui.exploration.trends.TrendingRankingScreen
import me.him188.ani.app.ui.exploration.trends.TrendingRankingViewModel
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.animation.NavigationMotionScheme
import me.him188.ani.app.ui.foundation.animation.ProvideAniMotionCompositionLocals
import me.him188.ani.app.ui.foundation.layout.currentWindowAdaptiveInfo1
import me.him188.ani.app.ui.foundation.layout.desktopTitleBar
import me.him188.ani.app.ui.foundation.widgets.BackNavigationIconButton
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.TopAppBarActionButton
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.main_network_check_failed
import me.him188.ani.app.ui.login.EmailLoginStartScreen
import me.him188.ani.app.ui.login.EmailLoginVerifyScreen
import me.him188.ani.app.ui.login.EmailLoginViewModel
import me.him188.ani.app.ui.notification.AnnouncementDetailScreen
import me.him188.ani.app.ui.notification.AnnouncementDetailViewModel
import me.him188.ani.app.ui.notification.AnnouncementsScreen
import me.him188.ani.app.ui.notification.AnnouncementsViewModel
import me.him188.ani.app.ui.notification.NotificationCenterScreen
import me.him188.ani.app.ui.notification.NotificationCenterViewModel
import me.him188.ani.app.ui.notification.NotificationDetailScreen
import me.him188.ani.app.ui.notification.NotificationDetailViewModel
import me.him188.ani.app.ui.notification.NotificationForegroundObserver
import me.him188.ani.app.ui.notification.rememberNotificationActions
import me.him188.ani.app.ui.oauth.OAuthAuthorizeScreen
import me.him188.ani.app.ui.oauth.OAuthAuthorizeViewModel
import me.him188.ani.app.ui.playback.ApplePlaybackHistoryPage
import me.him188.ani.app.ui.playback.MacPlaybackHistoryPage
import me.him188.ani.app.ui.playback.PlaybackHistoryScreen
import me.him188.ani.app.ui.playback.PlaybackHistorySyncStatusScreen
import me.him188.ani.app.ui.playback.PlaybackHistoryUiItem
import me.him188.ani.app.ui.playback.PlaybackHistoryViewModel
import me.him188.ani.app.ui.profile.auth.AniContactList
import me.him188.ani.app.ui.qrlogin.QrLoginConfirmScreen
import me.him188.ani.app.ui.qrlogin.QrLoginConfirmViewModel
import me.him188.ani.app.ui.qrlogin.QrLoginScanScreen
import me.him188.ani.app.ui.qrlogin.isQrCodeScannerSupported
import me.him188.ani.app.ui.search.AppleSearchScreen
import me.him188.ani.app.ui.search.MacSearchScreen
import me.him188.ani.app.ui.search.SearchScreen
import me.him188.ani.app.ui.settings.SettingsScreen
import me.him188.ani.app.ui.settings.SettingsViewModel
import me.him188.ani.app.ui.settings.account.AppleBangumiSyncScreen
import me.him188.ani.app.ui.settings.mediasource.jsonapi.EditJsonApiMediaSourceScreen
import me.him188.ani.app.ui.settings.mediasource.jsonapi.EditJsonApiMediaSourceViewModel
import me.him188.ani.app.ui.settings.mediasource.rss.EditRssMediaSourceScreen
import me.him188.ani.app.ui.settings.mediasource.rss.EditRssMediaSourceViewModel
import me.him188.ani.app.ui.settings.mediasource.selector.EditSelectorMediaSourceScreen
import me.him188.ani.app.ui.settings.mediasource.selector.EditSelectorMediaSourceViewModel
import me.him188.ani.app.ui.settings.notification.NotificationSettingsScreen
import me.him188.ani.app.ui.settings.notification.NotificationSettingsViewModel
import me.him188.ani.app.ui.settings.tabs.media.torrent.peer.PeerFilterSettingsScreen
import me.him188.ani.app.ui.settings.tabs.media.torrent.peer.PeerFilterSettingsViewModel
import me.him188.ani.app.ui.subject.details.AppleSubjectDetailsScreen
import me.him188.ani.app.ui.subject.details.MacSubjectDetailsScreen
import me.him188.ani.app.ui.subject.details.SubjectDetailsScreen
import me.him188.ani.app.ui.subject.details.SubjectDetailsViewModel
import me.him188.ani.app.ui.subject.episode.EpisodeScreen
import me.him188.ani.app.ui.subject.episode.EpisodeViewModel
import me.him188.ani.app.ui.subject.episode.analytics.PlayFlowDebugFloatingWindow
import me.him188.ani.app.ui.subject.person.AppleCharacterDetailsScreen
import me.him188.ani.app.ui.subject.person.ApplePersonDetailsScreen
import me.him188.ani.app.ui.subject.person.CharacterDetailsScreen
import me.him188.ani.app.ui.subject.person.CharacterDetailsViewModel
import me.him188.ani.app.ui.subject.person.MacCharacterDetailsScreen
import me.him188.ani.app.ui.subject.person.MacPersonDetailsScreen
import me.him188.ani.app.ui.subject.person.PersonDetailsScreen
import me.him188.ani.app.ui.subject.person.PersonDetailsViewModel
import me.him188.ani.app.ui.subject.relations.SubjectRelationGraphScreen
import me.him188.ani.app.ui.subject.relations.SubjectRelationGraphViewModel
import me.him188.ani.app.ui.user.SelfInfoStateProducer
import me.him188.ani.app.ui.watchtogether.LocalWatchTogetherPlayerController
import me.him188.ani.app.ui.watchtogether.WatchTogetherOverlayHost
import me.him188.ani.app.ui.watchtogether.WatchTogetherPlayerController
import me.him188.ani.app.ui.watchtogether.WatchTogetherViewModel
import me.him188.ani.datasources.api.source.FactoryId
import me.him188.ani.utils.analytics.Analytics
import me.him188.ani.utils.analytics.AnalyticsEvent.Companion.SubjectEnter
import me.him188.ani.utils.analytics.recordEvent
import me.him188.ani.utils.platform.isIos
import me.him188.ani.utils.platform.isMacOS
import org.jetbrains.compose.resources.stringResource
import org.koin.mp.KoinPlatform

/**
 * UI 入口点. 包含所有子页面, 以及组合这些子页面的方式 (navigation).
 */
@Composable
fun AniAppContent(aniNavigator: AniNavigator) {
    val aniAppViewModel = viewModel<AniAppViewModel>()
    val appState = aniAppViewModel.appState.collectAsStateWithLifecycle(null).value ?: return
    val watchTogetherViewModel = viewModel { WatchTogetherViewModel() }
    val watchTogetherPlayerController = remember(watchTogetherViewModel) {
        WatchTogetherPlayerController(watchTogetherViewModel::onPlayerEntryClick)
    }

    // 只有在 APP 首次启动的时候使用 initialNavRoute, 之后 back stack 自己维护并跨进程恢复
    val backStack = rememberAniBackStack(appState.initialNavRoute)
    aniNavigator.setBackStack(backStack)
    // 调试用的播放流程悬浮窗, 从设置的调试页打开, 盖在所有页面之上
    var playFlowDebugWindowVisible by rememberSaveable { mutableStateOf(false) }

    Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        CompositionLocalProvider(
            LocalNavigator provides aniNavigator,
            LocalBrowserNavigator providesDefault aniAppViewModel.browserNavigator,
            LocalWatchTogetherPlayerController provides watchTogetherPlayerController,
        ) {
            ProvideAniMotionCompositionLocals {
                AniAppContentImpl(
                    aniNavigator,
                    backStack,
                    appState.mainSceneInitialPage,
                    Modifier.fillMaxSize(),
                    onOpenPlayFlowDebugWindow = { playFlowDebugWindowVisible = true },
                )
                BangumiSessionExpiredPromptHost(
                    viewModel = aniAppViewModel,
                    enabled = appState.initialNavRoute is NavRoutes.Main,
                    onLogin = {
                        aniNavigator.navigateBangumiAuthorize()
                    },
                )
                WatchTogetherOverlayHost(
                    viewModel = watchTogetherViewModel,
                    aniNavigator = aniNavigator,
                )
                PikPakNotEnoughSpaceDialogHost(aniAppViewModel.pikPakNotEnoughSpace)
                val notificationBadgeManager = remember { KoinPlatform.getKoin().get<NotificationBadgeManager>() }
                NotificationForegroundObserver(notificationBadgeManager)
                if (playFlowDebugWindowVisible) {
                    PlayFlowDebugFloatingWindow(onClose = { playFlowDebugWindowVisible = false })
                }
            }
        }
    }
}

@Composable
private fun AniAppContentImpl(
    aniNavigator: AniNavigator,
    backStack: List<NavRoutes>,
    mainSceneInitialPage: MainScreenPage,
    modifier: Modifier = Modifier,
    onOpenPlayFlowDebugWindow: () -> Unit,
) {
    val navMotionScheme by rememberUpdatedState(NavigationMotionScheme.current)
    val emailLoginViewModel = viewModel<EmailLoginViewModel> { EmailLoginViewModel() }

    // Apple 平台的导航画在 NavDisplay 外层, 推入页也显示: iOS 为浮动导航栏或 rail (05 G10), macOS 为侧边栏 (05 B3).
    // 其他平台的导航栏在 MainScreen 内.
    val platform = LocalPlatform.current
    val appleShell = when {
        platform.isIos() -> AppleShell.Ios
        platform.isMacOS() -> AppleShell.Mac
        else -> null
    }
    val navigationLayoutType = AniNavigationSuiteDefaults.calculateLayoutType(currentWindowAdaptiveInfo1())
    val appleMainPages = if (appleShell != null) rememberAppleMainPages(backStack) else null

    when (appleShell) {
        AppleShell.Ios -> {
            val appleTabBarInsetDecorator = rememberAppleTabBarInsetDecorator(backStack, navigationLayoutType)
            AppleNavigationShell(
                backStack,
                aniNavigator,
                appleMainPages!!,
                navigationLayoutType,
                modifier,
            ) {
                AniNavDisplay(
                    aniNavigator,
                    backStack,
                    mainSceneInitialPage,
                    Modifier.fillMaxSize(),
                    navMotionScheme,
                    emailLoginViewModel,
                    appleShell,
                    appleMainPages,
                    sharedViewModel = null,
                    extraEntryDecorators = listOf(appleTabBarInsetDecorator),
                    onOpenSettings = { aniNavigator.navigateSettings(it) },
                    onOpenPlayFlowDebugWindow,
                )
            }
        }

        AppleShell.Mac -> {
            // 侧边栏与主分页共用一个实例: 侧边栏在 NavDisplay 外层, 读不到 Main 条目里的 ViewModel.
            val sharedViewModel = viewModel { MainScreenSharedViewModel() }
            val searchField = remember { MacSearchFieldState() }
            // 设定为独立窗口 (05 B4): 有宿主时, 主窗口里所有打开设定的入口都打开设定窗口
            val settingsWindowHost = LocalMacSettingsWindowHost.current
            val settingsWindow = remember { MacSettingsWindowState() }
            val navigator = remember(aniNavigator, settingsWindowHost, settingsWindow) {
                if (settingsWindowHost != null) MacSettingsWindowNavigator(aniNavigator, settingsWindow) else aniNavigator
            }
            val onOpenSettings: (SettingsTab?) -> Unit = { navigator.navigateSettings(it) }
            CompositionLocalProvider(LocalNavigator provides navigator) {
                MacNavigationShell(
                    backStack,
                    navigator,
                    appleMainPages!!,
                    status = rememberMacSidebarStatus(sharedViewModel),
                    searchField = searchField,
                    onOpenAccount = {
                        val selfInfo = sharedViewModel.selfInfo.value
                        if (selfInfo.isSessionValid == true) {
                            onOpenSettings(SettingsTab.PROFILE)
                        } else {
                            navigator.navigateEmailLoginStart()
                        }
                    },
                    onOpenSettings = { onOpenSettings(null) },
                    modifier = modifier,
                ) {
                    CompositionLocalProvider(LocalMacSearchField provides searchField) {
                        AniNavDisplay(
                            navigator,
                            backStack,
                            mainSceneInitialPage,
                            Modifier.fillMaxSize(),
                            navMotionScheme,
                            emailLoginViewModel,
                            appleShell,
                            appleMainPages,
                            sharedViewModel = sharedViewModel,
                            extraEntryDecorators = emptyList(),
                            onOpenSettings = onOpenSettings,
                            onOpenPlayFlowDebugWindow,
                        )
                    }
                }
            }
            settingsWindowHost?.SettingsWindow(settingsWindow) {
                MacSettingsWindowScreen(
                    settingsWindow,
                    sharedViewModel,
                    navigator,
                    onShowMainWindow = settingsWindowHost::showMainWindow,
                    onOpenPlayFlowDebugWindow = onOpenPlayFlowDebugWindow,
                )
            }
        }

        null -> {
            AniNavDisplay(
                aniNavigator,
                backStack,
                mainSceneInitialPage,
                modifier,
                navMotionScheme,
                emailLoginViewModel,
                appleShell = null,
                appleMainPages = null,
                sharedViewModel = null,
                extraEntryDecorators = emptyList(),
                onOpenSettings = { aniNavigator.navigateSettings(it) },
                onOpenPlayFlowDebugWindow,
            )
        }
    }
}

/**
 * Apple 平台的导航外壳.
 */
internal enum class AppleShell {
    /** [AppleNavigationShell]: iPhone 浮动导航栏, iPad rail. */
    Ios,

    /** [MacNavigationShell]: macOS 侧边栏. */
    Mac,
}

/**
 * macOS 侧边栏的状态. 各项都是冷流, 只有侧边栏订阅.
 */
@Composable
private fun rememberMacSidebarStatus(vm: MainScreenSharedViewModel): MacSidebarStatus {
    val selfInfo by vm.selfInfo.collectAsStateWithLifecycle()
    val showNotificationDot by vm.showNotificationDot.collectAsStateWithLifecycle()
    val pendingSyncCount by vm.bangumiPendingSyncCount.collectAsStateWithLifecycle()
    val hasNewEpisodes by vm.hasNewEpisodes.collectAsStateWithLifecycle()
    val activeDownloadCount by vm.activeDownloadCount.collectAsStateWithLifecycle()
    // 与 MainScreen 相同: 未登录 (包括 token 过期且因网络错误未能刷新) 时不显示红点.
    val notificationDot = showNotificationDot && selfInfo.isSessionValid == true && selfInfo.selfInfo != null
    return MacSidebarStatus(
        selfInfo = selfInfo,
        notificationDot = notificationDot,
        pendingSyncCount = pendingSyncCount,
        hasNewEpisodes = hasNewEpisodes,
        activeDownloadCount = activeDownloadCount,
    )
}

/**
 * @param appleShell Apple 平台的导航外壳, 其他平台为 `null`. 决定各页面用 Apple 版还是既有版本.
 * @param appleMainPages Apple 平台主分页的选中状态, 其他平台为 `null`.
 * @param sharedViewModel 外壳持有的 [MainScreenSharedViewModel] (macOS). 为 `null` 时 Main 条目自己创建.
 * @param onOpenSettings 主分页上的设定入口.
 */
@Composable
private fun AniNavDisplay(
    aniNavigator: AniNavigator,
    backStack: List<NavRoutes>,
    mainSceneInitialPage: MainScreenPage,
    modifier: Modifier,
    navMotionScheme: NavigationMotionScheme,
    emailLoginViewModel: EmailLoginViewModel,
    appleShell: AppleShell?,
    appleMainPages: AppleMainPages?,
    sharedViewModel: MainScreenSharedViewModel?,
    extraEntryDecorators: List<NavEntryDecorator<NavRoutes>>,
    onOpenSettings: (SettingsTab?) -> Unit,
    onOpenPlayFlowDebugWindow: () -> Unit,
) {
    val isIosShell = appleShell == AppleShell.Ios
    // 必须传给所有 Scaffold 和 TopAppBar. 注意, 如果你不传, 你的 UI 很可能会在 macOS 不工作.
    // 在这里读取标题栏: macOS 侧边栏显示时, 外壳把内容区的标题栏设为 0.
    val windowInsetsWithoutTitleBar = ScaffoldDefaults.contentWindowInsets
    val windowInsets = ScaffoldDefaults.contentWindowInsets
        .add(WindowInsets.desktopTitleBar()) // Compose 目前不支持这个所以我们要自己加上
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        onBack = { aniNavigator.popBackStack() },
        entryDecorators = listOf<NavEntryDecorator<NavRoutes>>(
            // 让每个页面各自持有 rememberSaveable 状态和 ViewModel, 出栈时一并销毁
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ) + extraEntryDecorators,
        transitionSpec = {
            navMotionScheme.enterTransition togetherWith navMotionScheme.exitTransition
        },
        popTransitionSpec = {
            navMotionScheme.popEnterTransition togetherWith navMotionScheme.popExitTransition
        },
        predictivePopTransitionSpec = {
            navMotionScheme.popEnterTransition togetherWith navMotionScheme.popExitTransition
        },
        entryProvider = entryProvider {
            entry<NavRoutes.EmailLoginStart> {
                EmailLoginStartScreen(
                    onOtpSent = {
                        aniNavigator.navigateEmailLoginVerify()
                    },
                    onThirdPartyLoginClick = {
                        aniNavigator.navigateOAuthAuthorize(it.id)
                    },
                    onNavigateSettings = {
                        aniNavigator.navigateSettings()
                    },
                    onNavigateBack = {
                        aniNavigator.popBackStack(NavRoutes.EmailLoginStart, true)
                    },
                    vm = emailLoginViewModel,
                )
            }
            entry<NavRoutes.EmailLoginVerify> {
                EmailLoginVerifyScreen(
                    onSuccess = {
                        aniNavigator.popBackOrNavigateToMain(mainSceneInitialPage)
                    },
                    onThirdPartyLoginClick = {
                        aniNavigator.navigateOAuthAuthorize(it.id)
                    },
                    onNavigateSettings = {
                        aniNavigator.navigateSettings()
                    },
                    onNavigateBack = {
                        aniNavigator.popBackStack(NavRoutes.EmailLoginVerify, true)
                    },
                    vm = emailLoginViewModel,
                )
            }
            entry<NavRoutes.OAuthAuthorize> { route ->
                // 未知平台 (例如旧版本保存的导航状态) 回退到 Bangumi
                val platform = OAuthPlatform.fromId(route.provider) ?: OAuthPlatform.BANGUMI
                val vm = viewModel<OAuthAuthorizeViewModel>(key = platform.id) { OAuthAuthorizeViewModel(platform) }
                OAuthAuthorizeScreen(
                    vm,
                    onNavigateBack = {
                        aniNavigator.popBackStack(route, true)
                    },
                    onNavigateSettings = {
                        aniNavigator.navigateSettings()
                    },
                    contactActions = {
                        AniContactList()
                    },
                    onAuthorizeSuccess = {
                        aniNavigator.popBackStack(route, true)
                        aniNavigator.popBackStack(NavRoutes.EmailLoginVerify, true)
                        aniNavigator.popBackStack(NavRoutes.EmailLoginStart, true)
                    },
                )
            }
            entry<NavRoutes.QrLoginScan> { route ->
                QrLoginScanScreen(
                    onScanned = { requestId ->
                        // 确认页取代扫码页: 从确认页返回时不再回到相机
                        aniNavigator.popBackStack(route, true)
                        aniNavigator.navigateQrLoginConfirm(requestId)
                    },
                    onNavigateBack = { aniNavigator.popBackStack(route, true) },
                )
            }
            entry<NavRoutes.QrLoginConfirm> { route ->
                val vm = viewModel<QrLoginConfirmViewModel>(key = route.requestId) {
                    QrLoginConfirmViewModel.create(route.requestId)
                }
                QrLoginConfirmScreen(
                    vm,
                    onNavigateBack = { aniNavigator.popBackStack(route, true) },
                    onNavigateLogin = {
                        aniNavigator.popBackStack(route, true)
                        aniNavigator.navigateLogin()
                    },
                )
            }
            entry<NavRoutes.Main> { route ->
                val navigationLayoutType =
                    AniNavigationSuiteDefaults.calculateLayoutType(
                        currentWindowAdaptiveInfo1(),
                    )

                val vm = sharedViewModel ?: viewModel { MainScreenSharedViewModel() }
                var currentPage by rememberSaveable { mutableStateOf(route.initialPage) }

                val toaster = LocalToaster.current
                val networkCheckFailedMessage = stringResource(Lang.main_network_check_failed)
                LaunchedEffect(vm) {
                    vm.networkCheckFailed.collect {
                        toaster.toast(networkCheckFailedMessage)
                    }
                }

                // Apple 平台的选中分页放在 NavDisplay 外层, 推入页上的导航栏与侧边栏也要读写它
                fun setPage(page: MainScreenPage) {
                    if (appleMainPages != null) appleMainPages.setPage(route, page) else currentPage = page
                }

                OverrideNavigation(
                    {
                        object : AniNavigator by it {
                            override fun navigateMain(page: MainScreenPage, popUpTargetInclusive: NavRoutes?) {
                                setPage(page)
                            }
                        }
                    },
                ) {
                    val selfInfo by vm.selfInfo.collectAsState() // not -WithLifecycle
                    val showNotificationDot by vm.showNotificationDot.collectAsStateWithLifecycle()
                    when (appleShell) {
                        AppleShell.Ios -> AppleMainScreen(
                            page = appleMainPages!!.pageOf(route),
                            selfInfo = selfInfo,
                            sharedViewModel = vm,
                            showNotificationDot = showNotificationDot,
                            onNavigateToPage = ::setPage,
                            modifier = Modifier.fillMaxSize(),
                        )

                        AppleShell.Mac -> MacMainScreen(
                            page = appleMainPages!!.pageOf(route),
                            selfInfo = selfInfo,
                            showNotificationDot = showNotificationDot,
                            onOpenSettings = onOpenSettings,
                            modifier = Modifier.fillMaxSize(),
                        )

                        null -> MainScreen(
                            page = currentPage,
                            selfInfo = selfInfo,
                            onNavigateToPage = { currentPage = it },
                            onNavigateToSettings = { aniNavigator.navigateSettings(it) },
                            onNavigateToSearch = { aniNavigator.navigateSubjectSearch() },
                            showNotificationDot = showNotificationDot,
                            navigationLayoutType = navigationLayoutType,
                        )
                    }
                }
            }
            entry<NavRoutes.SubjectSearch> { route ->
                val navigator = LocalNavigator.current
                val vm = viewModel(key = route.toString()) { SearchViewModel(route.toQuery()) }
                if (appleShell == AppleShell.Mac) {
                    AttachMacSearchField(vm)
                }

                if (isIosShell) {
                    AppleSearchScreen(
                        vm,
                        onNavigateBack = {
                            aniNavigator.popBackStack()
                        },
                        onNavigateToSubjectDetails = { subjectId, placeholder ->
                            navigator.navigateSubjectDetails(subjectId, placeholder)
                        },
                        onNavigateToEpisodeDetails = { subjectId, episodeId ->
                            navigator.navigateEpisodeDetails(subjectId, episodeId)
                        },
                        contentPadding = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).asPaddingValues(),
                    )
                } else if (appleShell == AppleShell.Mac) {
                    MacSearchScreen(
                        vm,
                        onNavigateToSubjectDetails = { subjectId, placeholder ->
                            navigator.navigateSubjectDetails(subjectId, placeholder)
                        },
                        onNavigateToEpisodeDetails = { subjectId, episodeId ->
                            navigator.navigateEpisodeDetails(subjectId, episodeId)
                        },
                    )
                } else {
                    SearchScreen(
                        vm,
                        onNavigateBack = {
                            aniNavigator.popBackStack()
                        },
                        onNavigateToSubjectDetails = { subjectId, placeholder ->
                            navigator.navigateSubjectDetails(subjectId, placeholder)
                        },
                        onNavigateToEpisodeDetails = { subjectId, episodeId ->
                            navigator.navigateEpisodeDetails(subjectId, episodeId)
                        },
                        windowInsets = windowInsets,
                    )
                }
            }
            entry<NavRoutes.SubjectDetail> { route ->
                val vm = viewModel<SubjectDetailsViewModel>(key = route.subjectId.toString()) {
                    val placeholder = route.placeholder?.run {
                        SubjectInfo.createPlaceholder(id, name, coverUrl, nameCN)
                    }
                    SubjectDetailsViewModel(route.subjectId, placeholder)
                }
                val onClickTag: (Tag) -> Unit = {
                    aniNavigator.navigateSubjectSearch(NavRoutes.SubjectSearch(tags = listOf(it.name)))
                }
                if (isIosShell) {
                    AppleSubjectDetailsScreen(
                        vm,
                        onPlay = { aniNavigator.navigateEpisodeDetails(route.subjectId, it) },
                        onLoadErrorRetry = { vm.reload() },
                        onClickTag = onClickTag,
                        onBack = { aniNavigator.popBackStack(route, inclusive = true) },
                        contentPadding = appleBottomInsets().asPaddingValues(),
                    )
                } else if (appleShell == AppleShell.Mac) {
                    MacSubjectDetailsScreen(
                        vm,
                        onPlay = { aniNavigator.navigateEpisodeDetails(route.subjectId, it) },
                        onLoadErrorRetry = { vm.reload() },
                        onClickTag = onClickTag,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    SubjectDetailsScreen(
                        vm,
                        onPlay = { aniNavigator.navigateEpisodeDetails(route.subjectId, it) },
                        onLoadErrorRetry = { vm.reload() },
                        onClickTag = onClickTag,
                        windowInsets = windowInsets,
                        navigationIcon = {
                            Row {
                                BackNavigationIconButton(
                                    {
                                        aniNavigator.popBackStack(route, inclusive = true)
                                    },
                                )
                                TopAppBarActionButton(
                                    {
                                        aniNavigator.popBackOrNavigateToMain(mainSceneInitialPage)
                                    },
                                ) {
                                    Icon(
                                        Icons.Rounded.Home,
                                        contentDescription = null,
                                    )
                                }
                            }
                        },
                    )
                }
            }
            entry<NavRoutes.EpisodeDetail> { route ->
                val context = LocalContext.current
                val vm = viewModel<EpisodeViewModel>(
                    key = route.toString(),
                ) {
                    EpisodeViewModel(
                        subjectId = route.subjectId,
                        initialEpisodeId = route.episodeId,
                        initialIsFullscreen = false,
                        context,
                    )
                }
                EpisodeScreen(
                    vm,
                    Modifier.fillMaxSize(),
                    windowInsets,
                    appleStyle = isIosShell,
                    macStyle = appleShell == AppleShell.Mac,
                )
            }
            entry<NavRoutes.Settings> { route ->
                SettingsScreen(
                    viewModel {
                        SettingsViewModel()
                    },
                    onNavigateToEmailLogin = { aniNavigator.navigateEmailLoginStart() },
                    onNavigateToOAuth = { aniNavigator.navigateOAuthAuthorize(it.id) },
                    loadOpenSourceLibrariesJsons = ::loadOpenSourceLibrariesJsons,
                    Modifier.fillMaxSize(),
                    route.tab,
                    onNavigateToQrLogin = if (isQrCodeScannerSupported) {
                        { aniNavigator.navigateQrLoginScan() }
                    } else null,
                    onOpenPlayFlowDebugWindow = onOpenPlayFlowDebugWindow,
                    navigationIcon = {
                        BackNavigationIconButton(
                            {
                                aniNavigator.popBackStack(route, inclusive = true)
                            },
                        )
                    },
                )
            }
            entry<NavRoutes.PlaybackHistory> { route ->
                val vm = viewModel { PlaybackHistoryViewModel() }
                val onOpenHistory: (PlaybackHistoryUiItem) -> Unit = { history ->
                    val subjectId = history.subjectId
                    if (subjectId != null) {
                        aniNavigator.navigateEpisodeDetails(subjectId, history.episodeId)
                    }
                }
                if (appleShell == AppleShell.Mac) {
                    MacPlaybackHistoryPage(
                        vm,
                        onOpenHistory = onOpenHistory,
                        onOpenSyncStatus = { aniNavigator.navigatePlaybackHistorySyncStatus() },
                        Modifier.fillMaxSize(),
                    )
                } else if (isIosShell) {
                    ApplePlaybackHistoryPage(
                        vm,
                        onOpenHistory = onOpenHistory,
                        onOpenSyncStatus = { aniNavigator.navigatePlaybackHistorySyncStatus() },
                        onBack = { aniNavigator.popBackStack(route, inclusive = true) },
                        Modifier.fillMaxSize(),
                        contentPadding = appleBottomInsets().asPaddingValues(),
                    )
                } else {
                    PlaybackHistoryScreen(
                        vm = vm,
                        onNavigateBack = { aniNavigator.popBackStack(route, inclusive = true) },
                        onOpenHistory = onOpenHistory,
                        onOpenSyncStatus = {
                            aniNavigator.navigatePlaybackHistorySyncStatus()
                        },
                        modifier = Modifier.fillMaxSize(),
                        navigationIcon = {
                            BackNavigationIconButton(
                                {
                                    aniNavigator.popBackStack(route, inclusive = true)
                                },
                            )
                        },
                        windowInsets = windowInsetsWithoutTitleBar,
                    )
                }
            }
            entry<NavRoutes.PlaybackHistorySyncStatus> { route ->
                PlaybackHistorySyncStatusScreen(
                    vm = viewModel { PlaybackHistoryViewModel() },
                    onNavigateBack = { aniNavigator.popBackStack(route, inclusive = true) },
                    modifier = Modifier.fillMaxSize(),
                    navigationIcon = {
                        BackNavigationIconButton(
                            {
                                aniNavigator.popBackStack(route, inclusive = true)
                            },
                        )
                    },
                    windowInsets = windowInsetsWithoutTitleBar,
                )
            }
            entry<NavRoutes.BangumiSync> { route ->
                AppleBangumiSyncScreen(
                    onNavigateBack = { aniNavigator.popBackStack(route, inclusive = true) },
                    modifier = Modifier.fillMaxSize(),
                )
            }
            entry<NavRoutes.BangumiMerge> { route ->
                BangumiMergeScreen(
                    vm = viewModel { BangumiMergeViewModel() },
                    onNavigateBack = { aniNavigator.popBackStack(route, inclusive = true) },
                    modifier = Modifier.fillMaxSize(),
                    navigationIcon = {
                        BackNavigationIconButton(
                            {
                                aniNavigator.popBackStack(route, inclusive = true)
                            },
                        )
                    },
                    windowInsets = windowInsetsWithoutTitleBar,
                )
            }
            entry<NavRoutes.Notifications> { route ->
                NotificationCenterScreen(
                    vm = viewModel { NotificationCenterViewModel() },
                    onNavigateToDetail = { aniNavigator.navigateNotificationDetail(it) },
                    onNavigateToAnnouncements = { aniNavigator.navigateAnnouncements() },
                    onNavigateToSettings = { aniNavigator.navigateNotificationSettings() },
                    onLogin = { aniNavigator.navigateEmailLoginStart() },
                    actions = rememberNotificationActions(aniNavigator),
                    modifier = Modifier.fillMaxSize(),
                    navigationIcon = {
                        BackNavigationIconButton({ aniNavigator.popBackStack(route, inclusive = true) })
                    },
                    windowInsets = windowInsets,
                )
            }
            entry<NavRoutes.NotificationDetail> { route ->
                NotificationDetailScreen(
                    vm = viewModel(key = route.toString()) { NotificationDetailViewModel(route.notificationId) },
                    actions = rememberNotificationActions(aniNavigator),
                    modifier = Modifier.fillMaxSize(),
                    navigationIcon = {
                        BackNavigationIconButton({ aniNavigator.popBackStack(route, inclusive = true) })
                    },
                    windowInsets = windowInsetsWithoutTitleBar,
                )
            }
            entry<NavRoutes.Announcements> { route ->
                AnnouncementsScreen(
                    vm = viewModel { AnnouncementsViewModel() },
                    onNavigateToDetail = { aniNavigator.navigateAnnouncementDetail(it) },
                    onLogin = { aniNavigator.navigateEmailLoginStart() },
                    modifier = Modifier.fillMaxSize(),
                    navigationIcon = {
                        BackNavigationIconButton({ aniNavigator.popBackStack(route, inclusive = true) })
                    },
                    windowInsets = windowInsets,
                )
            }
            entry<NavRoutes.AnnouncementDetail> { route ->
                AnnouncementDetailScreen(
                    vm = viewModel(key = route.toString()) { AnnouncementDetailViewModel(route.announcementId) },
                    modifier = Modifier.fillMaxSize(),
                    navigationIcon = {
                        BackNavigationIconButton({ aniNavigator.popBackStack(route, inclusive = true) })
                    },
                    windowInsets = windowInsetsWithoutTitleBar,
                )
            }
            entry<NavRoutes.NotificationSettings> { route ->
                val vm = viewModel { NotificationSettingsViewModel() }
                NotificationSettingsScreen(
                    vm.notificationSettings,
                    modifier = Modifier.fillMaxSize(),
                    navigationIcon = {
                        BackNavigationIconButton({ aniNavigator.popBackStack(route, inclusive = true) })
                    },
                    windowInsets = windowInsetsWithoutTitleBar,
                )
            }
            entry<NavRoutes.Caches> { route ->
                val selfInfo by remember { SelfInfoStateProducer() }.flow.collectAsState(null)
                DownloadManagementScreen(
                    vm = viewModel { createDownloadManagementViewModel() },
                    selfInfo = selfInfo,
                    onPlay = {
                        aniNavigator.navigateEpisodeDetails(it.subjectId, it.episodeId)
                    },
                    onClickLogin = { },
                    onNavigateCacheDetail = { aniNavigator.navigateCacheDetails(it) },
                    modifier = Modifier.fillMaxSize(),
                    navigationIcon = {
                        BackNavigationIconButton(
                            {
                                aniNavigator.popBackStack(route, inclusive = true)
                            },
                        )
                    },
                )
            }
            entry<NavRoutes.CacheDetail> { route ->
                val detailsVm = viewModel(key = route.toString()) { MediaCacheDetailsPageViewModel(route.cacheId) }
                if (isIosShell) {
                    AppleMediaCacheDetailsScreen(
                        detailsVm,
                        downloads = viewModel { createDownloadManagementViewModel() },
                        cacheId = route.cacheId,
                        onPlay = { aniNavigator.navigateEpisodeDetails(it.subjectId, it.episodeId) },
                        onBack = { aniNavigator.popBackStack(route, inclusive = true) },
                        Modifier.fillMaxSize(),
                        contentPadding = appleBottomInsets().asPaddingValues(),
                    )
                } else {
                    MediaCacheDetailsScreen(
                        detailsVm,
                        navigationIcon = {
                            BackNavigationIconButton(
                                {
                                    aniNavigator.popBackStack(route, inclusive = true)
                                },
                            )
                        },
                        Modifier.fillMaxSize(),
                        windowInsets = windowInsets,
                    )
                }
            }
            entry<NavRoutes.SubjectRelationGraph> { route ->
                val vm = viewModel<SubjectRelationGraphViewModel>(key = "subject-relation-graph-${route.subjectId}") {
                    SubjectRelationGraphViewModel(route.subjectId)
                }
                SubjectRelationGraphScreen(
                    vm,
                    onClickSubject = {
                        aniNavigator.navigateSubjectDetails(
                            it.subjectId,
                            SubjectDetailPlaceholder(it.subjectId, it.name, it.nameCn, it.image),
                        )
                    },
                    Modifier.fillMaxSize(),
                    windowInsets = windowInsets,
                    navigationIcon = {
                        BackNavigationIconButton({ aniNavigator.popBackStack(route, inclusive = true) })
                    },
                )
            }
            entry<NavRoutes.PersonDetail> { route ->
                val vm = viewModel<PersonDetailsViewModel>(key = "person-${route.personId}") {
                    PersonDetailsViewModel(route.personId)
                }
                if (appleShell == AppleShell.Mac) {
                    MacPersonDetailsScreen(vm, route.personId, Modifier.fillMaxSize())
                } else if (isIosShell) {
                    ApplePersonDetailsScreen(
                        vm,
                        route.personId,
                        onBack = { aniNavigator.popBackStack(route, inclusive = true) },
                        Modifier.fillMaxSize(),
                        contentPadding = appleBottomInsets().asPaddingValues(),
                    )
                } else {
                    PersonDetailsScreen(
                        vm,
                        Modifier.fillMaxSize(),
                        windowInsets = windowInsets,
                        navigationIcon = {
                            BackNavigationIconButton({ aniNavigator.popBackStack(route, inclusive = true) })
                        },
                    )
                }
            }
            entry<NavRoutes.CharacterDetail> { route ->
                val vm = viewModel<CharacterDetailsViewModel>(key = "character-${route.characterId}") {
                    CharacterDetailsViewModel(route.characterId)
                }
                if (appleShell == AppleShell.Mac) {
                    MacCharacterDetailsScreen(vm, route.characterId, Modifier.fillMaxSize())
                } else if (isIosShell) {
                    AppleCharacterDetailsScreen(
                        vm,
                        route.characterId,
                        onBack = { aniNavigator.popBackStack(route, inclusive = true) },
                        Modifier.fillMaxSize(),
                        contentPadding = appleBottomInsets().asPaddingValues(),
                    )
                } else {
                    CharacterDetailsScreen(
                        vm,
                        Modifier.fillMaxSize(),
                        windowInsets = windowInsets,
                        navigationIcon = {
                            BackNavigationIconButton({ aniNavigator.popBackStack(route, inclusive = true) })
                        },
                    )
                }
            }
            entry<NavRoutes.SubjectCaches> { route ->
                val vm = viewModel(key = route.toString()) { createSubjectDownloadsViewModel(route.subjectId) }
                val onPlay: (DownloadItem) -> Unit = { aniNavigator.navigateEpisodeDetails(it.subjectId, it.episodeId) }
                if (appleShell == AppleShell.Mac) {
                    MacSubjectDownloadsScreen(
                        vm,
                        onPlay = onPlay,
                        onOpenSubjectDetails = { aniNavigator.navigateSubjectDetails(it, placeholder = null) },
                        Modifier.fillMaxSize(),
                    )
                } else if (isIosShell) {
                    AppleSubjectDownloadsScreen(
                        vm,
                        onPlay = onPlay,
                        onOpenDetail = { aniNavigator.navigateCacheDetails(it.id) },
                        onBack = { aniNavigator.popBackStack(route, inclusive = true) },
                        Modifier.fillMaxSize(),
                        contentPadding = appleBottomInsets().asPaddingValues(),
                    )
                } else {
                    SubjectDownloadsScreen(
                        vm = vm,
                        onPlay = onPlay,
                        onNavigateDownloadDetail = { aniNavigator.navigateCacheDetails(it) },
                        modifier = Modifier.fillMaxSize(),
                        windowInsets = windowInsets,
                        navigationIcon = {
                            BackNavigationIconButton(
                                {
                                    aniNavigator.popBackStack(route, inclusive = true)
                                },
                            )
                        },
                    )
                }
            }
            entry<NavRoutes.EditMediaSource> { route ->
                val factoryId = FactoryId(route.factoryId)
                val mediaSourceInstanceId = route.mediaSourceInstanceId
                when (factoryId) {
                    RssMediaSource.FactoryId -> EditRssMediaSourceScreen(
                        viewModel<EditRssMediaSourceViewModel>(key = mediaSourceInstanceId) {
                            EditRssMediaSourceViewModel(mediaSourceInstanceId)
                        },
                        mediaDetailsColumn = { media ->
                            MediaDetailsLazyGrid(
                                MediaDetails.from(media, null, null),
                                Modifier.fillMaxSize(),
                                showSourceInfo = false,
                            )
                        },
                        Modifier,
                        windowInsets,
                        navigationIcon = {
                            BackNavigationIconButton(
                                {
                                    aniNavigator.popBackStack(route, inclusive = true)
                                },
                            )
                        },
                    )

                    SelectorMediaSource.FactoryId -> {
                        val context = LocalContext.current
                        EditSelectorMediaSourceScreen(
                            viewModel<EditSelectorMediaSourceViewModel>(key = mediaSourceInstanceId) {
                                EditSelectorMediaSourceViewModel(mediaSourceInstanceId, context)
                            },
                            Modifier,
                            windowInsets = windowInsets,
                            navigationIcon = {
                                BackNavigationIconButton(
                                    {
                                        aniNavigator.popBackStack(route, inclusive = true)
                                    },
                                )
                            },
                        )
                    }

                    JsonApiMediaSource.FactoryId -> {
                        EditJsonApiMediaSourceScreen(
                            viewModel<EditJsonApiMediaSourceViewModel>(key = mediaSourceInstanceId) {
                                EditJsonApiMediaSourceViewModel(mediaSourceInstanceId)
                            },
                            Modifier,
                            windowInsets = windowInsets,
                            navigationIcon = {
                                BackNavigationIconButton(
                                    {
                                        aniNavigator.popBackStack(route, inclusive = true)
                                    },
                                )
                            },
                        )
                    }

                    else -> error("Unknown factoryId: $factoryId")
                }
            }
            entry<NavRoutes.TorrentPeerSettings> { route ->
                val viewModel = viewModel { PeerFilterSettingsViewModel() }
                PeerFilterSettingsScreen(
                    viewModel.state,
                    navigationIcon = {
                        BackNavigationIconButton(
                            {
                                aniNavigator.popBackStack(route, inclusive = true)
                            },
                        )
                    },
                )
            }
            entry<NavRoutes.Schedule> { route ->
                val vm = viewModel { ScheduleViewModel(initialDate = route.initialDate) }
                val presentation by vm.presentationFlow.collectAsStateWithLifecycle()
                val onClickItem: (AiringScheduleItemPresentation) -> Unit = {
                    aniNavigator.navigateSubjectDetails(
                        it.subjectId,
                        placeholder = SubjectDetailPlaceholder(
                            id = it.subjectId,
                            name = it.subjectOriginalTitle,
                            nameCN = it.subjectTitle,
                            coverUrl = it.imageUrl,
                        ),
                    )
                }
                if (appleShell == AppleShell.Mac) {
                    val selfInfo by remember { SelfInfoStateProducer() }.flow.collectAsState(null)
                    MacSchedulePage(
                        presentation,
                        state = vm.pageState,
                        quickFollowState = vm.quickFollowState.takeIf { selfInfo?.isSessionValid == true },
                        onRetry = { vm.refresh() },
                        onClickItem = onClickItem,
                        Modifier.fillMaxSize(),
                    )
                } else if (isIosShell) {
                    val selfInfo by remember { SelfInfoStateProducer() }.flow.collectAsState(null)
                    AppleSchedulePage(
                        presentation,
                        state = vm.pageState,
                        quickFollowState = vm.quickFollowState.takeIf { selfInfo?.isSessionValid == true },
                        onRetry = { vm.refresh() },
                        onClickItem = onClickItem,
                        onBack = { aniNavigator.popBackStack(route, inclusive = true) },
                        Modifier.fillMaxSize(),
                        contentPadding = appleBottomInsets().asPaddingValues(),
                    )
                } else {
                    ScheduleScreen(
                        presentation,
                        onRetry = { vm.refresh() },
                        onClickItem = onClickItem,
                        Modifier.fillMaxSize(),
                        windowInsets = windowInsets,
                        navigationIcon = {
                            BackNavigationIconButton(
                                {
                                    aniNavigator.popBackStack(route, inclusive = true)
                                },
                            )
                        },
                        state = vm.pageState,
                    )
                }
            }
            entry<NavRoutes.TrendingRanking> { route ->
                val vm = viewModel { TrendingRankingViewModel() }
                val items = vm.items.collectAsLazyPagingItemsWithLifecycle()
                val onClickItem: (TrendingRankingItemPresentation) -> Unit = {
                    Analytics.recordEvent(SubjectEnter) {
                        put("source", "trending_ranking")
                        put("subject_id", it.subject.subjectId)
                    }
                    aniNavigator.navigateSubjectDetails(
                        it.subject.subjectId,
                        placeholder = SubjectDetailPlaceholder(
                            id = it.subject.subjectId,
                            name = it.subject.originalTitle,
                            nameCN = it.subject.title,
                            coverUrl = it.subject.imageUrl,
                        ),
                    )
                }
                if (appleShell == AppleShell.Mac) {
                    val selfInfo by remember { SelfInfoStateProducer() }.flow.collectAsState(null)
                    MacTrendingRankingPage(
                        items,
                        quickFollowState = vm.quickFollowState.takeIf { selfInfo?.isSessionValid == true },
                        onOpenItem = onClickItem,
                        Modifier.fillMaxSize(),
                    )
                } else if (isIosShell) {
                    val selfInfo by remember { SelfInfoStateProducer() }.flow.collectAsState(null)
                    AppleTrendingRankingPage(
                        items,
                        quickFollowState = vm.quickFollowState.takeIf { selfInfo?.isSessionValid == true },
                        onClickItem = onClickItem,
                        onBack = { aniNavigator.popBackStack(route, inclusive = true) },
                        Modifier.fillMaxSize(),
                        contentPadding = appleBottomInsets().asPaddingValues(),
                    )
                } else {
                    TrendingRankingScreen(
                        items,
                        onClickItem = onClickItem,
                        Modifier.fillMaxSize(),
                        windowInsets = windowInsets,
                        navigationIcon = {
                            BackNavigationIconButton(
                                {
                                    aniNavigator.popBackStack(route, inclusive = true)
                                },
                            )
                        },
                    )
                }
            }
        },
    )
}

private fun NavRoutes.SubjectSearch.toQuery(): SubjectSearchQuery {
    return SubjectSearchQuery(
        keywords = keyword ?: "",
        tags = tags,
    )
}
