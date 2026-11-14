/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import me.him188.ani.app.data.models.notification.Announcement
import me.him188.ani.app.data.models.notification.AppNotification
import me.him188.ani.app.ui.foundation.apple.AppleCircleButton
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.CollapsedTopBar
import me.him188.ani.app.ui.foundation.apple.PushedPageHeader
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.PullToRefreshBox
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.notification_announcements_title
import me.him188.ani.app.ui.lang.notification_mark_all_read
import me.him188.ani.app.ui.lang.notification_settings
import me.him188.ani.app.ui.lang.notification_title
import me.him188.ani.app.ui.lang.notification_unread_count
import org.jetbrains.compose.resources.stringResource

/**
 * iOS 通知中心 (03 §4.7): C3 推入页, 右上角为「全部標為已讀」与「通知設定」圆钮, 大标题下为「N 則未讀」.
 *
 * 标题、公告入口与「互動 / 通知」胶囊随列表滚动, 返回钮一行滚出后显示精简栏. 胶囊点选切换分页.
 * 点击通知照现有规则打开 ([notificationOpenTarget]): 详情为单独的页面 ([onNavigateToDetail]), 账号安全通知打开账号设置,
 * 评论被回复直接前往那条回复所在的评论详情.
 *
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleNotificationCenterScreen(
    vm: NotificationCenterViewModel,
    onNavigateToDetail: (notificationId: String) -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogin: () -> Unit,
    actions: NotificationActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    AppleNotificationCenterLayout(
        collectNotificationCenterState(vm),
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToAnnouncements = onNavigateToAnnouncements,
        onNavigateToSettings = onNavigateToSettings,
        onLogin = onLogin,
        actions = actions,
        onBack = onBack,
        modifier = modifier,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
    )
}

@Composable
internal fun AppleNotificationCenterLayout(
    state: NotificationCenterState,
    onNavigateToDetail: (notificationId: String) -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogin: () -> Unit,
    actions: NotificationActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val page = state.selectedPage
    val items = page.items
    val markAllRead = state.markAllReadRequest
    val hasUnknownTypes by remember(items) {
        derivedStateOf { items.itemSnapshotList.items.any { it.isUnknownType } }
    }
    val listState = rememberLazyListState()
    val openUrl = rememberOpenUrl()

    fun openNotification(notification: AppNotification) {
        state.onOpenNotification(notification)
        when (val target = notificationOpenTarget(notification)) {
            NotificationOpenTarget.AccountSettings -> actions.onOpenAccountSettings()
            is NotificationOpenTarget.Destination -> actions.open(target.destination, openUrl)
            NotificationOpenTarget.Detail -> onNavigateToDetail(notification.id)
        }
    }

    fun performInlineAction(notification: AppNotification, action: NotificationInlineAction) {
        state.onOpenNotification(notification)
        when (action) {
            NotificationInlineAction.REAUTHORIZE_BANGUMI -> actions.onReauthorizeBangumi()
        }
    }

    // 标题列与精简栏各有一组; 只给标题列的加测试标签
    val headerActions: @Composable (tagged: Boolean) -> Unit = { tagged ->
        AppleCircleButton(
            onClick = { markAllRead?.let { state.onMarkAllRead(it.group, it.newestCreatedAtMillis) } },
            icon = AppleIcons.check(),
            contentDescription = stringResource(Lang.notification_mark_all_read),
            if (tagged) Modifier.testTag(NotificationTestTags.MARK_ALL_READ) else Modifier,
            enabled = markAllRead != null,
        )
        AppleCircleButton(
            onClick = onNavigateToSettings,
            icon = AppleIcons.settings(),
            contentDescription = stringResource(Lang.notification_settings),
            if (tagged) Modifier.testTag(NotificationTestTags.SETTINGS) else Modifier,
        )
    }

    val unreadCount = if (state.access == NotificationAccess.AUTHORIZED) state.badgeState.unreadCount else 0
    val isRefreshing = items.loadState.refresh is LoadState.Loading && items.itemCount > 0
    val pullToRefreshState = rememberPullToRefreshState()
    val layoutDirection = LocalLayoutDirection.current
    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { state.onRefresh(page.group) },
            Modifier.fillMaxSize(),
            state = pullToRefreshState,
            enabled = state.access == NotificationAccess.AUTHORIZED,
            touchOnly = true,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.align(Alignment.TopCenter).windowInsetsPadding(windowInsets),
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                )
            },
        ) {
            // 只有列表行显示相对时间
            ProvideNotificationNow {
                LazyColumn(
                    Modifier.fillMaxSize().testTag(AppleNotificationTestTags.CenterList),
                    listState,
                    contentPadding = PaddingValues(
                        start = contentPadding.calculateStartPadding(layoutDirection),
                        top = contentPadding.calculateTopPadding(),
                        end = contentPadding.calculateEndPadding(layoutDirection),
                        bottom = contentPadding.calculateBottomPadding() + 24.dp,
                    ),
                ) {
                    item(key = "header", contentType = "header") {
                        Column(Modifier.padding(bottom = 4.dp)) {
                            PushedPageHeader(
                                stringResource(Lang.notification_title),
                                onBack = onBack,
                                windowInsets = windowInsets,
                                actions = { headerActions(true) },
                            )
                            if (unreadCount > 0) {
                                Text(
                                    stringResource(Lang.notification_unread_count, unreadCount),
                                    Modifier
                                        .padding(start = 20.dp, top = 4.dp, end = 20.dp)
                                        .testTag(AppleNotificationTestTags.UnreadCount),
                                    style = AniTextStyles.callout,
                                    color = MaterialTheme.colorScheme.secondary,
                                )
                            }
                        }
                    }

                    val announcement = state.badgeState.latestAnnouncement
                    if (announcement != null) {
                        item(key = "announcement-entry", contentType = "announcement-entry") {
                            AppleAnnouncementEntryCard(
                                announcement,
                                hasUnread = state.hasUnreadAnnouncements,
                                onClick = onNavigateToAnnouncements,
                                Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                            )
                        }
                    }

                    if (state.pages.size > 1) {
                        item(key = "groups", contentType = "groups") {
                            AppleNotificationGroupPills(
                                state.pages.map { it.group },
                                selectedGroup = page.group,
                                unreadCountOf = { state.badgeState.unreadCountOf(it) },
                                onSelect = state.onSelectGroup,
                                Modifier
                                    .padding(start = 16.dp, top = 10.dp, end = 16.dp)
                                    .testTag(NotificationTestTags.TAB_ROW),
                            )
                        }
                    }

                    item(key = "list-top", contentType = "spacer") {
                        Spacer(Modifier.height(8.dp))
                    }

                    appleNotificationItems(
                        page,
                        access = state.access,
                        showUpdateBanner = hasUnknownTypes,
                        mac = false,
                        horizontalPadding = 16.dp,
                        selectedId = null,
                        selectable = false,
                        onClickNotification = ::openNotification,
                        onInlineAction = ::performInlineAction,
                        onCheckUpdate = actions.onCheckUpdate,
                        onLogin = onLogin,
                    )
                }
            }
        }

        val collapsed by rememberHeaderCollapsed(listState, windowInsets)
        if (collapsed) {
            CollapsedTopBar(
                stringResource(Lang.notification_title),
                onBack,
                Modifier.testTag(AppleNotificationTestTags.CollapsedBar),
                windowInsets = windowInsets,
                actions = { headerActions(false) },
            )
        }
    }
}

/**
 * iOS 公告列表 (03 §4.8): C3 推入页, 大标题下为「N 則未讀」(列表中显示提示点的公告数, 见 [displayedUnreadCount]).
 * 登录用户进入列表即已读 ([collectAnnouncementsState]). 点击公告打开单独的详情页.
 *
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleAnnouncementsScreen(
    vm: AnnouncementsViewModel,
    onNavigateToDetail: (announcementId: String) -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    AppleAnnouncementsLayout(
        collectAnnouncementsState(vm),
        onNavigateToDetail = onNavigateToDetail,
        onLogin = onLogin,
        onBack = onBack,
        modifier = modifier,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
    )
}

@Composable
internal fun AppleAnnouncementsLayout(
    state: AnnouncementsState,
    onNavigateToDetail: (announcementId: String) -> Unit,
    onLogin: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val items = state.items
    val listState = rememberLazyListState()

    fun openAnnouncement(announcement: Announcement) {
        state.onOpenAnnouncement(announcement)
        onNavigateToDetail(announcement.id)
    }

    val unreadCount = state.displayedUnreadCount()
    val isRefreshing = items.loadState.refresh is LoadState.Loading && items.itemCount > 0
    val pullToRefreshState = rememberPullToRefreshState()
    val layoutDirection = LocalLayoutDirection.current
    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = state.onRefresh,
            Modifier.fillMaxSize(),
            state = pullToRefreshState,
            enabled = state.access != NotificationAccess.LOADING,
            touchOnly = true,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.align(Alignment.TopCenter).windowInsetsPadding(windowInsets),
                    isRefreshing = isRefreshing,
                    state = pullToRefreshState,
                )
            },
        ) {
            LazyColumn(
                Modifier.fillMaxSize().testTag(AppleNotificationTestTags.AnnouncementList),
                listState,
                contentPadding = PaddingValues(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    top = contentPadding.calculateTopPadding(),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPadding.calculateBottomPadding() + 24.dp,
                ),
            ) {
                item(key = "header", contentType = "header") {
                    Column(Modifier.padding(bottom = 20.dp)) {
                        PushedPageHeader(
                            stringResource(Lang.notification_announcements_title),
                            onBack = onBack,
                            windowInsets = windowInsets,
                        )
                        if (unreadCount > 0) {
                            Text(
                                stringResource(Lang.notification_unread_count, unreadCount),
                                Modifier
                                    .padding(start = 20.dp, top = 4.dp, end = 20.dp)
                                    .testTag(AppleNotificationTestTags.UnreadCount),
                                style = AniTextStyles.callout,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }

                appleAnnouncementItems(
                    items,
                    access = state.access,
                    readOverlay = state.readOverlay,
                    mac = false,
                    horizontalPadding = 16.dp,
                    selectedId = null,
                    selectable = false,
                    onClickAnnouncement = ::openAnnouncement,
                    onLogin = onLogin,
                )
            }
        }

        val collapsed by rememberHeaderCollapsed(listState, windowInsets)
        if (collapsed) {
            CollapsedTopBar(
                stringResource(Lang.notification_announcements_title),
                onBack,
                Modifier.testTag(AppleNotificationTestTags.CollapsedBar),
                windowInsets = windowInsets,
            )
        }
    }
}

/**
 * 列表第 0 项是 C3 标题列: 返回钮一行滚出后为 `true`, 此时显示精简栏.
 */
@Composable
internal fun rememberHeaderCollapsed(listState: LazyListState, windowInsets: WindowInsets): State<Boolean> {
    val density = LocalDensity.current
    return remember(listState, density, windowInsets) {
        derivedStateOf {
            val threshold = windowInsets.getTop(density) +
                    with(density) { AppleNotificationDefaults.HeaderBackRowBottom.roundToPx() }
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > threshold
        }
    }
}
