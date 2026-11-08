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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.him188.ani.app.data.models.notification.Announcement
import me.him188.ani.app.data.models.notification.AppNotification
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.MacLargeTitleHeader
import me.him188.ani.app.ui.foundation.apple.MacPageToolbar
import me.him188.ani.app.ui.foundation.apple.MacToolbarIconButton
import me.him188.ani.app.ui.foundation.apple.MacToolbarTextButton
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.notification_announcement_detail_placeholder
import me.him188.ani.app.ui.lang.notification_announcement_not_found
import me.him188.ani.app.ui.lang.notification_announcements_title
import me.him188.ani.app.ui.lang.notification_detail_placeholder
import me.him188.ani.app.ui.lang.notification_mark_all_read
import me.him188.ani.app.ui.lang.notification_not_found
import me.him188.ani.app.ui.lang.notification_settings
import me.him188.ani.app.ui.lang.notification_title
import me.him188.ani.app.ui.lang.notification_unread_count
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 通知中心 (04 §4.6): 工具栏右侧为「全部標為已讀」与通知设置钮 ([onNavigateToSettings] 打开设置窗口的「通知」分类).
 *
 * 主从分栏: 左列表 380 (大标题、公告入口、「互動 / 通知」胶囊与通知卡片), 右详情 (内容最宽 600). 点击的通知在右栏显示,
 * 账号安全通知打开账号设置. 内容区窄于 [AppleNotificationDefaults.MacTwoPaneMinWidth] 时只显示列表,
 * 点击通知推入详情页 ([onNavigateToDetail]).
 */
@Composable
fun MacNotificationCenterScreen(
    vm: NotificationCenterViewModel,
    onNavigateToDetail: (notificationId: String) -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogin: () -> Unit,
    actions: NotificationActions,
    modifier: Modifier = Modifier,
) {
    MacNotificationCenterLayout(
        collectNotificationCenterState(vm),
        onNavigateToDetail = onNavigateToDetail,
        onNavigateToAnnouncements = onNavigateToAnnouncements,
        onNavigateToSettings = onNavigateToSettings,
        onLogin = onLogin,
        actions = actions,
        modifier = modifier,
    )
}

@Composable
internal fun MacNotificationCenterLayout(
    state: NotificationCenterState,
    onNavigateToDetail: (notificationId: String) -> Unit,
    onNavigateToAnnouncements: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onLogin: () -> Unit,
    actions: NotificationActions,
    modifier: Modifier = Modifier,
) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val markAllRead = state.markAllReadRequest
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        MacPageToolbar {
            MacToolbarTextButton(
                stringResource(Lang.notification_mark_all_read),
                onClick = { markAllRead?.let { state.onMarkAllRead(it.group, it.newestCreatedAtMillis) } },
                Modifier.testTag(NotificationTestTags.MARK_ALL_READ),
                enabled = markAllRead != null,
            )
            MacToolbarIconButton(
                onClick = onNavigateToSettings,
                icon = AppleIcons.settings(),
                contentDescription = stringResource(Lang.notification_settings),
                Modifier.testTag(NotificationTestTags.SETTINGS),
            )
        }
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val twoPane = maxWidth >= AppleNotificationDefaults.MacTwoPaneMinWidth

            fun openNotification(notification: AppNotification) {
                state.onOpenNotification(notification)
                when (notificationOpenTarget(notification.content)) {
                    NotificationOpenTarget.ACCOUNT_SETTINGS -> actions.onOpenAccountSettings()
                    NotificationOpenTarget.DETAIL -> if (twoPane) {
                        selectedId = notification.id
                    } else {
                        onNavigateToDetail(notification.id)
                    }
                }
            }

            fun performInlineAction(notification: AppNotification, action: NotificationInlineAction) {
                state.onOpenNotification(notification)
                when (action) {
                    NotificationInlineAction.REAUTHORIZE_BANGUMI -> actions.onReauthorizeBangumi()
                }
            }

            MacMasterDetail(
                twoPane = twoPane,
                list = { listModifier ->
                    MacNotificationListPane(
                        state,
                        selectedId = if (twoPane) selectedId else null,
                        selectable = twoPane,
                        onClickNotification = ::openNotification,
                        onInlineAction = ::performInlineAction,
                        onClickAnnouncements = onNavigateToAnnouncements,
                        onCheckUpdate = actions.onCheckUpdate,
                        onLogin = onLogin,
                        listModifier,
                    )
                },
                detail = {
                    val notification = selectedId?.let { id ->
                        state.pages.firstNotNullOfOrNull { page -> page.loadedItems.firstOrNull { it.id == id } }
                    }
                    if (notification == null) {
                        // 列表为空时没有可选的通知, 详情栏留空
                        if (state.selectedPage.loadedItems.isNotEmpty()) {
                            MacDetailPlaceholder(
                                AppleIcons.bell(),
                                stringResource(Lang.notification_detail_placeholder),
                                Modifier.testTag(NotificationTestTags.DETAIL_PLACEHOLDER),
                            )
                        }
                    } else {
                        key(notification.id) {
                            MacNotificationDetailContent(notification, actions, Modifier.fillMaxSize())
                        }
                    }
                },
            )
        }
    }
}

/**
 * 左栏: 大标题与「N 則未讀」、公告入口、「互動 / 通知」胶囊固定在上方, 下面是通知卡片的列表.
 */
@Composable
private fun MacNotificationListPane(
    state: NotificationCenterState,
    selectedId: String?,
    selectable: Boolean,
    onClickNotification: (AppNotification) -> Unit,
    onInlineAction: (AppNotification, NotificationInlineAction) -> Unit,
    onClickAnnouncements: () -> Unit,
    onCheckUpdate: () -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val page = state.selectedPage
    val hasUnknownTypes by remember(page.items) {
        derivedStateOf { page.items.itemSnapshotList.items.any { it.isUnknownType } }
    }
    val unreadCount = if (state.access == NotificationAccess.AUTHORIZED) state.badgeState.unreadCount else 0
    Column(modifier) {
        MacLargeTitleHeader(
            stringResource(Lang.notification_title),
            subtitle = if (unreadCount > 0) stringResource(Lang.notification_unread_count, unreadCount) else null,
        )
        state.badgeState.latestAnnouncement?.let { announcement ->
            AppleAnnouncementEntryCard(
                announcement,
                hasUnread = state.hasUnreadAnnouncements,
                onClick = onClickAnnouncements,
                Modifier.padding(top = 16.dp),
            )
        }
        if (state.pages.size > 1) {
            AppleNotificationGroupPills(
                state.pages.map { it.group },
                selectedGroup = page.group,
                unreadCountOf = { state.badgeState.unreadCountOf(it) },
                onSelect = state.onSelectGroup,
                Modifier
                    .padding(top = 12.dp)
                    .testTag(NotificationTestTags.TAB_ROW),
            )
        }
        ProvideNotificationNow {
            LazyColumn(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag(AppleNotificationTestTags.MacListPane),
                contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
            ) {
                appleNotificationItems(
                    page,
                    access = state.access,
                    showUpdateBanner = hasUnknownTypes,
                    mac = true,
                    horizontalPadding = 0.dp,
                    selectedId = selectedId,
                    selectable = selectable,
                    onClickNotification = onClickNotification,
                    onInlineAction = onInlineAction,
                    onCheckUpdate = onCheckUpdate,
                    onLogin = onLogin,
                )
            }
        }
    }
}

/**
 * macOS 公告 (04 §4.6 公告模式): 大标题「公告」与「N 則未讀」, 左列表右详情, 版面同通知中心. 工具栏没有本页动作.
 * 登录用户进入列表即已读 ([collectAnnouncementsState]). 内容区窄时只显示列表, 点击公告推入详情页 ([onNavigateToDetail]).
 */
@Composable
fun MacAnnouncementsScreen(
    vm: AnnouncementsViewModel,
    onNavigateToDetail: (announcementId: String) -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MacAnnouncementsLayout(collectAnnouncementsState(vm), onNavigateToDetail, onLogin, modifier)
}

@Composable
internal fun MacAnnouncementsLayout(
    state: AnnouncementsState,
    onNavigateToDetail: (announcementId: String) -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedId by rememberSaveable { mutableStateOf<String?>(null) }
    val items = state.items
    val unreadCount = state.displayedUnreadCount()
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        MacPageToolbar()
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val twoPane = maxWidth >= AppleNotificationDefaults.MacTwoPaneMinWidth

            fun openAnnouncement(announcement: Announcement) {
                state.onOpenAnnouncement(announcement)
                if (twoPane) selectedId = announcement.id else onNavigateToDetail(announcement.id)
            }

            MacMasterDetail(
                twoPane = twoPane,
                list = { listModifier ->
                    Column(listModifier) {
                        MacLargeTitleHeader(
                            stringResource(Lang.notification_announcements_title),
                            subtitle = if (unreadCount > 0) stringResource(Lang.notification_unread_count, unreadCount) else null,
                        )
                        LazyColumn(
                            Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .testTag(AnnouncementTestTags.LIST),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                        ) {
                            appleAnnouncementItems(
                                items,
                                access = state.access,
                                readOverlay = state.readOverlay,
                                mac = true,
                                horizontalPadding = 0.dp,
                                selectedId = if (twoPane) selectedId else null,
                                selectable = twoPane,
                                onClickAnnouncement = ::openAnnouncement,
                                onLogin = onLogin,
                            )
                        }
                    }
                },
                detail = {
                    val announcement = selectedId?.let { id -> items.itemSnapshotList.items.firstOrNull { it.id == id } }
                    if (announcement == null) {
                        // 列表为空时没有可选的公告, 详情栏留空
                        if (items.itemSnapshotList.items.isNotEmpty()) {
                            MacDetailPlaceholder(
                                Icons.Rounded.Campaign,
                                stringResource(Lang.notification_announcement_detail_placeholder),
                                Modifier.testTag(AnnouncementTestTags.DETAIL_PLACEHOLDER),
                            )
                        }
                    } else {
                        key(announcement.id) {
                            MacAnnouncementDetailContent(announcement, Modifier.fillMaxSize())
                        }
                    }
                },
            )
        }
    }
}

/**
 * 主从分栏: 左列表 380 (内距 4 16 0 32, 右边 1 的 separator), 右详情. 单栏时列表填满, 左右内距 32.
 *
 * @param list 参数为列表栏的 Modifier (尺寸与内距).
 */
@Composable
private fun MacMasterDetail(
    twoPane: Boolean,
    list: @Composable (Modifier) -> Unit,
    detail: @Composable () -> Unit,
) {
    if (!twoPane) {
        list(Modifier.fillMaxSize().padding(start = 32.dp, top = 4.dp, end = 32.dp))
        return
    }
    Row(Modifier.fillMaxSize()) {
        list(
            Modifier
                .width(AppleNotificationDefaults.MacListWidth)
                .fillMaxHeight()
                .padding(start = 32.dp, top = 4.dp, end = 16.dp),
        )
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .testTag(NotificationTestTags.DETAIL_PANE),
        ) {
            detail()
        }
    }
}

/**
 * 右栏尚未选择时的提示, 在详情栏中央.
 */
@Composable
private fun MacDetailPlaceholder(
    icon: ImageVector,
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        AppleNotificationEmptyState(icon, text, supportingText = null, modifier)
    }
}

/**
 * macOS 窄宽度时推入的通知详情页: 工具栏与右栏相同的详情内容.
 */
@Composable
fun MacNotificationDetailScreen(
    vm: NotificationDetailViewModel,
    actions: NotificationActions,
    modifier: Modifier = Modifier,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    MacDetailPage(
        state,
        notFoundText = stringResource(Lang.notification_not_found),
        notFoundTestTag = NotificationTestTags.DETAIL_NOT_FOUND,
        onRetry = vm::reload,
        modifier = modifier,
    ) { notification ->
        MacNotificationDetailContent(notification, actions, Modifier.fillMaxSize())
    }
}

/**
 * macOS 窄宽度时推入的公告详情页.
 */
@Composable
fun MacAnnouncementDetailScreen(
    vm: AnnouncementDetailViewModel,
    modifier: Modifier = Modifier,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    MacDetailPage(
        state,
        notFoundText = stringResource(Lang.notification_announcement_not_found),
        notFoundTestTag = AnnouncementTestTags.DETAIL_NOT_FOUND,
        onRetry = vm::reload,
        modifier = modifier,
    ) { announcement ->
        MacAnnouncementDetailContent(announcement, Modifier.fillMaxSize())
    }
}

@Composable
private fun <T> MacDetailPage(
    state: DetailLoadState<T>,
    notFoundText: String,
    notFoundTestTag: String,
    onRetry: () -> Unit,
    modifier: Modifier,
    content: @Composable ColumnScope.(T) -> Unit,
) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        MacPageToolbar()
        if (state is DetailLoadState.Loaded) {
            content(state.value)
        } else {
            DetailLoadStateContent(
                state,
                notFoundText = notFoundText,
                notFoundTestTag = notFoundTestTag,
                onRetry = onRetry,
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 32.dp, vertical = 8.dp),
            )
        }
    }
}
