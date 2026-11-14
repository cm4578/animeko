/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import me.him188.ani.app.data.models.user.calculateDisplay
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.MainScreenPage
import me.him188.ani.app.navigation.NavRoutes
import me.him188.ani.app.ui.adaptive.navigation.MacSidebar
import me.him188.ani.app.ui.adaptive.navigation.MacSidebarAccount
import me.him188.ani.app.ui.adaptive.navigation.MacSidebarIconButton
import me.him188.ani.app.ui.adaptive.navigation.MacSidebarItem
import me.him188.ani.app.ui.adaptive.navigation.MacSidebarSearchField
import me.him188.ani.app.ui.adaptive.navigation.MacSidebarSection
import me.him188.ani.app.ui.exploration.search.SearchPageIntent
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.LocalMacShellState
import me.him188.ani.app.ui.foundation.apple.MacKeyboardShortcut
import me.him188.ani.app.ui.foundation.apple.MacShellState
import me.him188.ani.app.ui.foundation.apple.MacToolbarCounter
import me.him188.ani.app.ui.foundation.apple.MacToolbarDefaults
import me.him188.ani.app.ui.foundation.apple.MacToolbarIconButton
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.layout.LocalCaptionButtonInsets
import me.him188.ani.app.ui.foundation.layout.LocalTitleBarInsets
import me.him188.ani.app.ui.foundation.layout.ZeroInsets
import me.him188.ani.app.ui.foundation.layout.desktopCaptionButton
import me.him188.ani.app.ui.foundation.layout.desktopTitleBar
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.exploration_schedule
import me.him188.ani.app.ui.lang.exploration_trending_ranking
import me.him188.ani.app.ui.lang.main_screen_page_cache_management
import me.him188.ani.app.ui.lang.main_screen_page_collection
import me.him188.ani.app.ui.lang.main_screen_page_exploration
import me.him188.ani.app.ui.lang.main_sidebar_browse
import me.him188.ani.app.ui.lang.main_sidebar_hide
import me.him188.ani.app.ui.lang.main_sidebar_library
import me.him188.ani.app.ui.lang.main_sidebar_search_placeholder
import me.him188.ani.app.ui.lang.main_sidebar_show
import me.him188.ani.app.ui.lang.notification_title
import me.him188.ani.app.ui.lang.playback_history_title
import me.him188.ani.app.ui.lang.profile_pending_sync_count
import me.him188.ani.app.ui.lang.settings
import me.him188.ani.app.ui.lang.settings_account_popup_login_register
import me.him188.ani.app.ui.lang.settings_account_popup_not_logged_in
import me.him188.ani.app.ui.user.SelfInfoUiState
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 侧边栏 (C17) 的一级入口 (04 §3.1, 05 B3).
 *
 * 探索、追番、快取是 [MainScreenPage]; 新番时间表、热度排行、播放记录是推入的路由, 选中态按返回栈判断.
 */
internal enum class MacSidebarEntry(val mainPage: MainScreenPage?) {
    Exploration(MainScreenPage.Exploration),
    Schedule(null),
    TrendingRanking(null),
    Collection(MainScreenPage.Collection),
    PlaybackHistory(null),
    CacheManagement(MainScreenPage.CacheManagement),
    ;

    /**
     * [route] 是否为本入口推入的页面. [mainPage] 不为 `null` 的入口总是 `false`.
     */
    fun matches(route: NavRoutes): Boolean = when (this) {
        Schedule -> route is NavRoutes.Schedule
        TrendingRanking -> route == NavRoutes.TrendingRanking
        PlaybackHistory -> route == NavRoutes.PlaybackHistory
        Exploration, Collection, CacheManagement -> false
    }

    fun navigate(navigator: AniNavigator) {
        when (this) {
            Schedule -> navigator.navigateSchedule()
            TrendingRanking -> navigator.navigateTrendingRanking()
            PlaybackHistory -> navigator.navigatePlaybackHistory()
            Exploration, Collection, CacheManagement -> {}
        }
    }
}

/**
 * 侧边栏的高亮 (04 §1.3).
 */
internal sealed interface MacSidebarSelection {
    data class Entry(val entry: MacSidebarEntry) : MacSidebarSelection

    /** 搜索结果页及从它推入的页面: 不高亮入口, 搜索框激活. */
    data object Search : MacSidebarSelection

    /** 通知中心及从它推入的页面: 不高亮入口, 铃铛选中. */
    data object Notifications : MacSidebarSelection

    data object None : MacSidebarSelection
}

/**
 * 从栈顶往下找最近的来源: 一级入口、搜索或通知中心. 推入的详情页沿用来源的高亮, 不按页面类型写死.
 * 例如从探索打开条目详情, 高亮「探索」; 从新番时间表打开, 高亮「新番时间表」.
 */
internal fun macSidebarSelection(backStack: List<NavRoutes>, mainPages: AppleMainPages): MacSidebarSelection {
    for (route in backStack.asReversed()) {
        val selection = when (route) {
            is NavRoutes.Main -> MacSidebarSelection.Entry(
                when (mainPages.pageOf(route)) {
                    MainScreenPage.Collection -> MacSidebarEntry.Collection
                    MainScreenPage.CacheManagement -> MacSidebarEntry.CacheManagement
                    // macOS 没有「我的」分页, 按探索处理
                    MainScreenPage.Exploration, MainScreenPage.Profile -> MacSidebarEntry.Exploration
                },
            )

            is NavRoutes.Schedule -> MacSidebarSelection.Entry(MacSidebarEntry.Schedule)
            NavRoutes.TrendingRanking -> MacSidebarSelection.Entry(MacSidebarEntry.TrendingRanking)
            NavRoutes.PlaybackHistory -> MacSidebarSelection.Entry(MacSidebarEntry.PlaybackHistory)
            NavRoutes.Caches -> MacSidebarSelection.Entry(MacSidebarEntry.CacheManagement)
            is NavRoutes.SubjectSearch -> MacSidebarSelection.Search
            NavRoutes.Notifications -> MacSidebarSelection.Notifications
            else -> null
        }
        if (selection != null) return selection
    }
    return MacSidebarSelection.None
}

/**
 * 全屏任务 (播放、登录与授权、资料源编辑器、合并收藏) 不显示侧边栏, 其余页面显示.
 */
internal fun NavRoutes.showsMacSidebar(): Boolean = when (this) {
    is NavRoutes.EpisodeDetail,
    is NavRoutes.EditMediaSource,
    NavRoutes.EmailLoginStart,
    NavRoutes.EmailLoginVerify,
    is NavRoutes.OAuthAuthorize,
    NavRoutes.QrLoginScan,
    is NavRoutes.QrLoginConfirm,
    NavRoutes.BangumiMerge -> false

    else -> true
}

/**
 * 侧边栏行尾与底部帐号区显示的状态.
 *
 * @property notificationDot 铃铛的未读点, 已与登录状态合并 (05 B2).
 * @property pendingSyncCount 「N 项待同步」, 见 [MainScreenSharedViewModel.bangumiPendingSyncCount].
 * @property hasNewEpisodes 「追番」的新集数提示点, 见 [MainScreenSharedViewModel.hasNewEpisodes].
 * @property activeDownloadCount 「快取」的进行中下载数, 见 [MainScreenSharedViewModel.activeDownloadCount].
 */
@Immutable
internal class MacSidebarStatus(
    val selfInfo: SelfInfoUiState?,
    val notificationDot: Boolean,
    val pendingSyncCount: Int,
    val hasNewEpisodes: Boolean,
    val activeDownloadCount: Int,
)

/**
 * 侧边栏搜索框的状态 (05 G11): 输入的文字, 以及正在显示的搜索页的 [SearchViewModel].
 *
 * 搜索页显示时用 [attach] 交出自己的 ViewModel, 侧边栏的输入框就是它的搜索框: 按 Enter 在该页搜索.
 * 没有搜索页时, 按 Enter 打开搜索页.
 */
@Stable
internal class MacSearchFieldState {
    var text by mutableStateOf("")

    var searchViewModel: SearchViewModel? by mutableStateOf(null)
        private set

    fun attach(vm: SearchViewModel) {
        searchViewModel = vm
    }

    fun detach(vm: SearchViewModel) {
        if (searchViewModel === vm) searchViewModel = null
    }

    fun submit(navigator: AniNavigator) {
        val keywords = text.trim()
        val vm = searchViewModel
        if (vm != null) {
            val query = vm.searchPageState.value.query
            vm.onSearchPageIntent(SearchPageIntent.UpdateQuery(query.copy(keywords = keywords), submit = true))
        } else if (keywords.isNotEmpty()) {
            navigator.navigateSubjectSearch(NavRoutes.SubjectSearch(keyword = keywords))
        }
    }
}

/**
 * macOS 主窗口的侧边栏搜索框. 只在 macOS 外壳内提供.
 */
internal val LocalMacSearchField = staticCompositionLocalOf<MacSearchFieldState?> { null }

/**
 * 搜索页把自己的 ViewModel 交给侧边栏搜索框, 离开组合时收回. 框内的文字跟随该页的查询词.
 */
@Composable
internal fun AttachMacSearchField(vm: SearchViewModel) {
    val field = LocalMacSearchField.current ?: return
    LaunchedEffect(field, vm) {
        field.attach(vm)
        try {
            vm.searchPageState.map { it.query.keywords }.distinctUntilChanged().collect { field.text = it }
        } finally {
            field.detach(vm)
        }
    }
}

/**
 * macOS 的主窗口外壳: 左侧 C17 侧边栏 (05 B3), 右侧 [content] (`NavDisplay`).
 *
 * - 高亮按 [macSidebarSelection]. 点入口: 弹回最近的 [NavRoutes.Main] 条目, 主分页直接切换, 推入的入口再打开它的页面;
 *   入口的页面已在栈中时, 弹回该页即回到它的根页面. 铃铛同推入的入口.
 * - 全屏任务 ([showsMacSidebar]) 不显示侧边栏. 「隐藏侧边栏」收起后 (05 G31), 页面工具栏左侧显示「显示侧边栏」,
 *   状态只在内存中.
 * - ⌘K 显示侧边栏并聚焦搜索框 (05 C2).
 * - 侧边栏显示时, 红黄绿按钮落在侧边栏顶部, 内容区不需要让出标题栏.
 *
 * [content] 在各种状态下组合在同一位置, 切换侧边栏不会重建 `NavDisplay`.
 *
 * @param onOpenAccount 点底部帐号行.
 * @param onOpenSettings 点底部设定按钮.
 */
@Composable
internal fun MacNavigationShell(
    backStack: List<NavRoutes>,
    navigator: AniNavigator,
    mainPages: AppleMainPages,
    status: MacSidebarStatus,
    searchField: MacSearchFieldState,
    onOpenAccount: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val main = backStack.lastOrNull { it is NavRoutes.Main } as NavRoutes.Main?
    val routeShowsSidebar = main != null && backStack.last().showsMacSidebar()
    var hiddenByUser by rememberSaveable { mutableStateOf(false) }
    val sidebarVisible = routeShowsSidebar && !hiddenByUser
    val selection = macSidebarSelection(backStack, mainPages)

    // 离开搜索后清空搜索框
    val searchInStack = backStack.any { it is NavRoutes.SubjectSearch }
    LaunchedEffect(searchInStack) {
        if (!searchInStack) searchField.text = ""
    }

    val toolbars = remember { MacToolbarCounter() }
    val searchFocusRequester = remember { FocusRequester() }
    var focusSearchRequested by remember { mutableStateOf(false) }
    MacKeyboardShortcut(Key.K, enabled = routeShowsSidebar) {
        hiddenByUser = false
        focusSearchRequested = true
    }
    LaunchedEffect(focusSearchRequested, sidebarVisible) {
        if (focusSearchRequested && sidebarVisible) {
            withFrameNanos { } // 等侧边栏放上搜索框
            runCatching { searchFocusRequester.requestFocus() }
            focusSearchRequested = false
        }
    }

    fun popToMain() {
        if (main != null && backStack.last() != main) {
            navigator.popBackStack(main, inclusive = false)
        }
    }

    fun onClickEntry(entry: MacSidebarEntry) {
        main ?: return
        val page = entry.mainPage
        if (page != null) {
            mainPages.setPage(main, page)
            popToMain()
            return
        }
        val mainIndex = backStack.lastIndexOf(main)
        val existing = backStack.indexOfLast { entry.matches(it) }
        if (existing > mainIndex) {
            navigator.popBackStack(backStack[existing], inclusive = false)
        } else {
            popToMain()
            entry.navigate(navigator)
        }
    }

    fun onClickNotifications() {
        main ?: return
        val mainIndex = backStack.lastIndexOf(main)
        val existing = backStack.indexOfLast { it == NavRoutes.Notifications }
        if (existing > mainIndex) {
            navigator.popBackStack(NavRoutes.Notifications, inclusive = false)
        } else {
            popToMain()
            navigator.navigateNotifications()
        }
    }

    Row(modifier) {
        AnimatedVisibility(
            sidebarVisible,
            enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
            exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut(),
        ) {
            MacSidebar(
                onHideSidebar = { hiddenByUser = true },
                hideSidebarContentDescription = stringResource(Lang.main_sidebar_hide),
                search = {
                    MacSidebarSearchField(
                        query = searchField.text,
                        onQueryChange = { searchField.text = it },
                        placeholder = stringResource(Lang.main_sidebar_search_placeholder),
                        active = selection == MacSidebarSelection.Search,
                        focusRequester = searchFocusRequester,
                        onSearch = { searchField.submit(navigator) },
                    )
                },
                footer = {
                    MacSidebarFooter(
                        status = status,
                        notificationsSelected = selection == MacSidebarSelection.Notifications,
                        onOpenAccount = onOpenAccount,
                        onOpenNotifications = ::onClickNotifications,
                        onOpenSettings = onOpenSettings,
                    )
                },
            ) {
                val selectedEntry = (selection as? MacSidebarSelection.Entry)?.entry
                MacSidebarSection(stringResource(Lang.main_sidebar_browse)) {
                    EntryItem(MacSidebarEntry.Exploration, selectedEntry, ::onClickEntry)
                    EntryItem(MacSidebarEntry.Schedule, selectedEntry, ::onClickEntry)
                    EntryItem(MacSidebarEntry.TrendingRanking, selectedEntry, ::onClickEntry)
                }
                MacSidebarSection(stringResource(Lang.main_sidebar_library)) {
                    EntryItem(
                        MacSidebarEntry.Collection,
                        selectedEntry,
                        ::onClickEntry,
                        alert = status.hasNewEpisodes,
                    )
                    EntryItem(MacSidebarEntry.PlaybackHistory, selectedEntry, ::onClickEntry)
                    EntryItem(
                        MacSidebarEntry.CacheManagement,
                        selectedEntry,
                        ::onClickEntry,
                        count = status.activeDownloadCount.takeIf { it > 0 }?.toString(),
                    )
                }
            }
        }
        Box(Modifier.weight(1f).fillMaxHeight()) {
            val sidebarHidden = routeShowsSidebar && hiddenByUser
            val shellState = MacShellState(
                canGoBack = backStack.size > 1,
                onBack = { navigator.popBackStack() },
                sidebarHidden = sidebarHidden,
                onShowSidebar = { hiddenByUser = false },
                toolbars = toolbars,
            )
            CompositionLocalProvider(
                LocalMacShellState provides shellState,
                LocalTitleBarInsets provides if (sidebarVisible) ZeroInsets else LocalTitleBarInsets.current,
                LocalCaptionButtonInsets provides if (sidebarVisible) ZeroInsets else LocalCaptionButtonInsets.current,
            ) {
                Box(Modifier.fillMaxSize()) {
                    content()
                }
            }
            // 尚未改版的页面没有 C18 工具栏: 「显示侧边栏」放在红黄绿按钮右侧, 与它们同一行.
            if (sidebarHidden && toolbars.count == 0) {
                Row(
                    Modifier
                        .windowInsetsPadding(WindowInsets.desktopCaptionButton.only(WindowInsetsSides.Start))
                        .height(WindowInsets.desktopTitleBar.asPaddingValues().calculateTopPadding().coerceAtLeast(MacToolbarDefaults.ButtonSize))
                        .padding(start = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MacToolbarIconButton(
                        onClick = { hiddenByUser = false },
                        icon = AppleIcons.sidebar(),
                        contentDescription = stringResource(Lang.main_sidebar_show),
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryItem(
    entry: MacSidebarEntry,
    selectedEntry: MacSidebarEntry?,
    onClick: (MacSidebarEntry) -> Unit,
    alert: Boolean = false,
    count: String? = null,
) {
    MacSidebarItem(
        selected = entry == selectedEntry,
        onClick = { onClick(entry) },
        icon = entry.icon(),
        label = stringResource(entry.label()),
        alert = alert,
        count = count,
    )
}

private fun MacSidebarEntry.icon(): ImageVector = when (this) {
    MacSidebarEntry.Exploration -> AppleIcons.explore()
    MacSidebarEntry.Schedule -> AppleIcons.schedule()
    MacSidebarEntry.TrendingRanking -> AppleIcons.trending()
    MacSidebarEntry.Collection -> AppleIcons.collection()
    MacSidebarEntry.PlaybackHistory -> AppleIcons.history()
    MacSidebarEntry.CacheManagement -> AppleIcons.download()
}

private fun MacSidebarEntry.label(): StringResource = when (this) {
    MacSidebarEntry.Exploration -> Lang.main_screen_page_exploration
    MacSidebarEntry.Schedule -> Lang.exploration_schedule
    MacSidebarEntry.TrendingRanking -> Lang.exploration_trending_ranking
    MacSidebarEntry.Collection -> Lang.main_screen_page_collection
    MacSidebarEntry.PlaybackHistory -> Lang.playback_history_title
    MacSidebarEntry.CacheManagement -> Lang.main_screen_page_cache_management
}

/**
 * 侧边栏底部: 帐号行、铃铛 (只显示未读点, 05 B2)、设定.
 *
 * 帐号行: 登录时为昵称 + 「N 项待同步」(没有待同步时只显示昵称); 未登录时为「未登入」+「登入 / 註冊」.
 */
@Composable
private fun RowScope.MacSidebarFooter(
    status: MacSidebarStatus,
    notificationsSelected: Boolean,
    onOpenAccount: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val selfInfo = status.selfInfo?.selfInfo
    val isLogin = status.selfInfo?.isSessionValid == true && selfInfo != null
    val name = if (isLogin) {
        selfInfo.calculateDisplay().title
    } else {
        stringResource(Lang.settings_account_popup_not_logged_in)
    }
    val avatarUrl = selfInfo?.avatarUrl?.takeIf { isLogin }
    MacSidebarAccount(
        name = name,
        status = when {
            !isLogin -> stringResource(Lang.settings_account_popup_login_register)
            status.pendingSyncCount > 0 -> stringResource(Lang.profile_pending_sync_count, status.pendingSyncCount)
            else -> null
        },
        initial = name.take(1).uppercase(),
        onClick = onOpenAccount,
        modifier = Modifier.weight(1f),
        image = when {
            avatarUrl != null -> {
                { AvatarImage(avatarUrl, Modifier.fillMaxSize().clip(CircleShape)) }
            }

            !isLogin -> {
                { Icon(AppleIcons.profile(2f), contentDescription = null, Modifier.size(15.dp), tint = Color.White) }
            }

            else -> null
        },
    )
    MacSidebarIconButton(
        onClick = onOpenNotifications,
        icon = AppleIcons.bell(),
        contentDescription = stringResource(Lang.notification_title),
        selected = notificationsSelected,
        alert = status.notificationDot,
    )
    MacSidebarIconButton(
        onClick = onOpenSettings,
        icon = AppleIcons.settings(),
        contentDescription = stringResource(Lang.settings),
    )
}
