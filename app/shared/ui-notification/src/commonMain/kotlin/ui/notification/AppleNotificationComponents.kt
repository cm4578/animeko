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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import me.him188.ani.app.data.models.notification.AccountSecurityEvent
import me.him188.ani.app.data.models.notification.Announcement
import me.him188.ani.app.data.models.notification.AppNotification
import me.him188.ani.app.data.models.notification.AppNotificationContent
import me.him188.ani.app.data.models.notification.NotificationDestination
import me.him188.ani.app.data.models.notification.NotificationGroup
import me.him188.ani.app.data.models.notification.NotificationIcon
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.navigation.rememberAsyncBrowserNavigator
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AlertDot
import me.him188.ani.app.ui.foundation.apple.AppleButton
import me.him188.ani.app.ui.foundation.apple.AppleButtonDefaults
import me.him188.ani.app.ui.foundation.apple.AppleButtonStyle
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.PillTab
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.notification_announcement_overline
import me.him188.ani.app.ui.lang.notification_announcement_pinned
import me.him188.ani.app.ui.lang.notification_announcements_empty
import me.him188.ani.app.ui.lang.notification_announcements_empty_supporting
import me.him188.ani.app.ui.lang.notification_go_to_comments
import me.him188.ani.app.ui.lang.notification_report_result_row_title
import me.him188.ani.app.ui.lang.notification_unread
import me.him188.ani.app.ui.lang.notification_unread_count
import me.him188.ani.app.ui.lang.notification_update_banner
import me.him188.ani.app.ui.lang.notification_update_banner_action
import me.him188.ani.app.ui.lang.notification_view_details
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.search.isLoadingFirstPage
import me.him188.ani.app.ui.search.rememberLoadErrorState
import me.him188.ani.utils.platform.isMacOS
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版通知中心、公告与详情的尺寸 (03 §4.7–4.9, 04 §4.6). 列表行与卡片在 iOS 与 macOS 各有一组.
 */
internal object AppleNotificationDefaults {
    /** 手机通知行: 内距 12 12 12 16, 左侧 40 圆, 与文字相隔 12. */
    val RowPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)
    val LeadingSize = 40.dp
    val RowSpacing = 12.dp

    /** 手机通知行的分隔线起点: 16 + 40 + 12. */
    val DividerIndent = 68.dp

    /** 手机类型徽章 20, 含 2 的描边. */
    val BadgeSize = 20.dp

    /** macOS 通知行: 内距 10 12, 左侧 32 圆, 与文字相隔 10. */
    val MacRowPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    val MacLeadingSize = 32.dp
    val MacRowSpacing = 10.dp
    val MacDividerIndent = 54.dp
    val MacBadgeSize = 14.dp

    /** 手机公告行: 内距 14 12 14 16, 分隔线从 16 起. */
    val AnnouncementRowPadding = PaddingValues(start = 16.dp, top = 14.dp, end = 12.dp, bottom = 14.dp)
    val AnnouncementDividerIndent = 16.dp
    val MacAnnouncementRowPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    val MacAnnouncementDividerIndent = 12.dp

    /** 列表卡片与公告入口的圆角: 手机 16, macOS 12. */
    val CardRadius = 16.dp
    val MacCardRadius = 12.dp

    /** C3 标题列中返回钮所在一行的下缘: 顶部内距 10 + 圆钮 36. 这一行滚出后显示精简栏. */
    val HeaderBackRowBottom = 46.dp

    /** macOS 列表栏宽 380, 详情内容最宽 600. 内容区窄于 [MacTwoPaneMinWidth] 时只显示列表. */
    val MacListWidth = 380.dp
    val MacDetailMaxWidth = 600.dp
    val MacTwoPaneMinWidth = 760.dp
}

internal object AppleNotificationTestTags {
    const val CenterList = "apple-notification-center-list"
    const val AnnouncementList = "apple-announcement-list"
    const val UnreadCount = "apple-notification-unread-count"
    const val CollapsedBar = "apple-notification-collapsed-bar"
    const val RowBody = "apple-notification-row-body"
    const val RowMeta = "apple-notification-row-meta"
    const val Badge = "apple-notification-badge"
    const val Placeholder = "apple-notification-placeholder"
    const val DetailTime = "apple-notification-detail-time"
    const val Quote = "apple-notification-detail-quote"
    const val TargetRow = "apple-notification-detail-target"
    const val ResultCard = "apple-notification-report-result"
    const val Footnote = "apple-notification-detail-footnote"
    const val MacListPane = "mac-notification-list-pane"
}

/**
 * Apple 版通知列表一行的文字 (03 §4.7): [title], 最多两行的 [body], 以及第三行时间前的 [place] (例如「番名 第 N 話」).
 */
@Immutable
internal data class AppleNotificationRowText(
    val title: NotificationText,
    val body: NotificationText?,
    val place: NotificationText?,
)

/**
 * 计算 Apple 版列表行的文字. 标题与说明沿用 [notificationRowText], 以下不同:
 * - 用通用展示内容展示的通知 ([usesDisplayContent]) 显示正文的前两行, 而不只是第一行;
 * - 举报结果的举报对象 (番名 第 N 话等) 放在第三行, 后接时间, 没有第二行.
 */
internal fun appleNotificationRowText(
    notification: AppNotification,
    useOriginalTitle: Boolean,
): AppleNotificationRowText {
    val content = notification.content
    if (content is AppNotificationContent.ReportResult) {
        return AppleNotificationRowText(
            title = NotificationText.Res(Lang.notification_report_result_row_title),
            body = null,
            place = reportTargetText(content, notification.display, useOriginalTitle),
        )
    }
    val rowText = notificationRowText(notification, useOriginalTitle)
    val body = if (notification.usesDisplayContent) {
        notification.display.body?.let { displayBodyPreview(it) }?.let { NotificationText.Plain(it) }
    } else {
        rowText.supporting
    }
    return AppleNotificationRowText(rowText.headline, body, rowText.overline)
}

/**
 * 这条通知用服务端的通用展示内容 ([AppNotification.display]) 展示, 而不是客户端自己的文案.
 */
internal val AppNotification.usesDisplayContent: Boolean
    get() = when (val content = content) {
        is AppNotificationContent.ReportResult -> false
        is AppNotificationContent.AccountSecurity -> content.event == AccountSecurityEvent.UNKNOWN
        is AppNotificationContent.System -> content.event == null
        AppNotificationContent.Unknown -> true
    }

/**
 * 列表中的正文预览: 去掉空行与每行首尾的空白. 没有内容时为 `null`.
 */
internal fun displayBodyPreview(body: String): String? =
    body.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.joinToString("\n").ifEmpty { null }

/**
 * 互动的类型徽章 (回复、提及、回应、关注). 其他图标没有徽章.
 */
internal fun interactionBadgeIcon(icon: NotificationIcon?): ImageVector? = when (icon) {
    NotificationIcon.REPLY,
    NotificationIcon.MENTION,
    NotificationIcon.REACTION,
    NotificationIcon.FOLLOW,
        -> NotificationListItemDefaults.iconFor(icon)

    NotificationIcon.REPORT,
    NotificationIcon.SECURITY,
    NotificationIcon.INFO,
    null,
        -> null
}

/**
 * 通用展示内容的跳转按钮文字: 互动 (评论的回复、提及等) 为「前往評論區」, 网页链接为「查看詳情」, 其他照 [NotificationDestination.labelRes].
 */
internal fun displayDestinationLabelRes(
    notification: AppNotification,
    destination: NotificationDestination,
): StringResource = when {
    destination is NotificationDestination.Url -> Lang.notification_view_details
    notification.group == NotificationGroup.INTERACTION -> Lang.notification_go_to_comments
    else -> destination.labelRes
}

/**
 * 卡片中第 [index] 行 (共 [count] 行) 的形状: 第一行有上方圆角, 最后一行有下方圆角.
 */
internal fun appleListCardShape(index: Int, count: Int, radius: Dp): Shape = RoundedCornerShape(
    topStart = if (index == 0) radius else 0.dp,
    topEnd = if (index == 0) radius else 0.dp,
    bottomStart = if (index == count - 1) radius else 0.dp,
    bottomEnd = if (index == count - 1) radius else 0.dp,
)

/**
 * Apple 版通知列表的一行 (03 §4.7, 04 §4.6).
 *
 * 左侧为头像加右下角的类型徽章, 或圆形图标 (没有头像的通知). 右侧为标题、最多两行的内文与「番名 第 N 話 · 时间」,
 * 有行内操作时在最下方. 未读时标题加粗, 标题后有提示点.
 *
 * @param selected macOS 双栏中这一行是否正在右侧显示, 选中时 tint 底. 为 `null` 时不可选中 (手机).
 */
@Composable
internal fun AppleNotificationRow(
    notification: AppNotification,
    read: Boolean,
    onClick: () -> Unit,
    onInlineAction: (NotificationInlineAction) -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean? = null,
) {
    val mac = LocalPlatform.current.isMacOS()
    val colorScheme = MaterialTheme.colorScheme
    val useOriginalTitle = LocalSubjectAppearanceSettings.current.useOriginalTitle
    val text = remember(notification, useOriginalTitle) { appleNotificationRowText(notification, useOriginalTitle) }
    val inlineActions = remember(notification.content) { notificationInlineActions(notification.content) }
    val meta = listOfNotNull(text.place?.resolve(), formatNotificationRowTime(notification.createdAtMillis))
        .joinToString(" · ")
    val unreadText = stringResource(Lang.notification_unread)
    val isSelected = selected == true
    val background = if (isSelected) colorScheme.secondaryContainer else colorScheme.surfaceContainerLow
    Row(
        modifier
            .fillMaxWidth()
            .background(background)
            .clickable(onClick = onClick, role = Role.Button)
            .semantics {
                if (!read) stateDescription = unreadText
                if (selected != null) this.selected = isSelected
            }
            .padding(if (mac) AppleNotificationDefaults.MacRowPadding else AppleNotificationDefaults.RowPadding),
        horizontalArrangement = Arrangement.spacedBy(
            if (mac) AppleNotificationDefaults.MacRowSpacing else AppleNotificationDefaults.RowSpacing,
        ),
    ) {
        AppleNotificationLeading(notification, mac, ringColor = background)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text.title.resolve(),
                    Modifier.weight(1f),
                    style = if (mac) AniTextStyles.macBody else AniTextStyles.callout,
                    fontWeight = if (read) FontWeight.Normal else FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!read) {
                    // 与标题第一行 (行高 20) 居中
                    AlertDot(Modifier.padding(top = 6.dp).testTag(NotificationTestTags.UNREAD_DOT))
                }
            }
            val body = text.body?.resolve()
            if (body != null) {
                Text(
                    body,
                    Modifier.testTag(AppleNotificationTestTags.RowBody),
                    style = if (mac) AniTextStyles.macCallout else AniTextStyles.subhead,
                    color = AniColors.labelSoft,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                meta,
                Modifier.testTag(AppleNotificationTestTags.RowMeta),
                style = if (mac) AniTextStyles.macCaption else AniTextStyles.caption,
                color = colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (inlineActions.isNotEmpty()) {
                Row(Modifier.padding(top = 6.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (action in inlineActions) {
                        AppleButton(
                            onClick = { onInlineAction(action) },
                            Modifier.testTag(NotificationTestTags.INLINE_ACTION),
                            style = AppleButtonStyle.Tinted,
                            height = if (mac) AppleButtonDefaults.MacHeight else 32.dp,
                            contentPadding = PaddingValues(horizontal = if (mac) 12.dp else 14.dp),
                        ) {
                            Text(stringResource(action.labelRes), maxLines = 1)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 通知行左侧的圆: 有头像时显示头像 (加载完成前与加载失败时显示类型图标), 互动的头像右下角挂类型徽章;
 * 没有头像时为 fill 底的类型图标.
 *
 * @param ringColor 徽章描边的颜色, 与行的底色相同.
 */
@Composable
private fun AppleNotificationLeading(
    notification: AppNotification,
    mac: Boolean,
    ringColor: Color,
) {
    val colorScheme = MaterialTheme.colorScheme
    val imageUrl = NotificationListItemDefaults.imageUrlFor(notification)
    Box(Modifier.size(if (mac) AppleNotificationDefaults.MacLeadingSize else AppleNotificationDefaults.LeadingSize)) {
        Box(
            Modifier
                .matchParentSize()
                .clip(CircleShape)
                .background(colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                NotificationListItemDefaults.iconFor(notification),
                contentDescription = null,
                Modifier.size(if (mac) 16.dp else 20.dp),
                tint = colorScheme.onSurface,
            )
            if (imageUrl != null) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    Modifier
                        .matchParentSize()
                        .testTag(NotificationTestTags.LEADING_IMAGE),
                    contentScale = ContentScale.Crop,
                )
            }
        }
        val badgeIcon = if (imageUrl != null) interactionBadgeIcon(notification.display.icon) else null
        if (badgeIcon != null) {
            val ring = if (mac) 1.5.dp else 2.dp
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = ring * 2, y = ring * 2)
                    .size(if (mac) AppleNotificationDefaults.MacBadgeSize else AppleNotificationDefaults.BadgeSize)
                    .background(ringColor, CircleShape)
                    .padding(ring)
                    .background(colorScheme.surfaceContainerHighest, CircleShape)
                    .testTag(AppleNotificationTestTags.Badge),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    badgeIcon,
                    contentDescription = null,
                    Modifier.size(if (mac) 8.dp else 11.dp),
                    tint = colorScheme.onSurface,
                )
            }
        }
    }
}

/**
 * 首次加载时卡片中的占位行.
 */
@Composable
internal fun AppleNotificationPlaceholderRow(modifier: Modifier = Modifier) {
    val mac = LocalPlatform.current.isMacOS()
    Row(
        modifier
            .fillMaxWidth()
            .testTag(AppleNotificationTestTags.Placeholder)
            .padding(if (mac) AppleNotificationDefaults.MacRowPadding else AppleNotificationDefaults.RowPadding),
        horizontalArrangement = Arrangement.spacedBy(
            if (mac) AppleNotificationDefaults.MacRowSpacing else AppleNotificationDefaults.RowSpacing,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(if (mac) AppleNotificationDefaults.MacLeadingSize else AppleNotificationDefaults.LeadingSize)
                .placeholder(true, shape = CircleShape),
        )
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.fillMaxWidth(0.6f).height(14.dp).placeholder(true))
            Box(Modifier.fillMaxWidth(0.4f).height(12.dp).placeholder(true))
        }
    }
}

/**
 * 通知中心顶部的公告入口 (03 §4.7, 04 §4.6): 喇叭图标方块,「公告」与最新一条公告的标题, 有未读公告时加提示点, 最后是箭头.
 * 手机高 64、圆角 16; macOS 高 52、圆角 12.
 */
@Composable
internal fun AppleAnnouncementEntryCard(
    announcement: Announcement,
    hasUnread: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mac = LocalPlatform.current.isMacOS()
    val colorScheme = MaterialTheme.colorScheme
    val unreadText = stringResource(Lang.notification_unread)
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = if (mac) 52.dp else 64.dp)
            .clip(RoundedCornerShape(if (mac) AppleNotificationDefaults.MacCardRadius else AppleNotificationDefaults.CardRadius))
            .background(colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick, role = Role.Button)
            .semantics {
                if (hasUnread) stateDescription = unreadText
            }
            .testTag(NotificationTestTags.ANNOUNCEMENT_ENTRY)
            .padding(start = if (mac) 12.dp else 14.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(if (mac) 10.dp else 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(if (mac) 26.dp else 30.dp)
                .clip(RoundedCornerShape(if (mac) 7.dp else 8.dp))
                .background(colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Rounded.Campaign,
                contentDescription = null,
                Modifier.size(if (mac) 15.dp else 17.dp),
                tint = colorScheme.onSurface,
            )
        }
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(Lang.notification_announcement_overline),
                style = if (mac) AniTextStyles.macCaption else AniTextStyles.caption,
                color = colorScheme.secondary,
                maxLines = 1,
            )
            Text(
                announcement.title,
                style = if (mac) AniTextStyles.macBody else AniTextStyles.callout,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (hasUnread) {
            AlertDot(Modifier.testTag(NotificationTestTags.ANNOUNCEMENT_ENTRY_DOT))
        }
        Icon(
            AppleIcons.chevronRight(),
            contentDescription = null,
            Modifier.size(if (mac) 14.dp else 16.dp),
            tint = colorScheme.secondary,
        )
    }
}

/**
 * 公告入口是否显示提示点. 与现有通知中心相同.
 */
internal val NotificationCenterState.hasUnreadAnnouncements: Boolean
    get() = badgeState.unreadAnnouncementCount > 0 || badgeState.hasUnreadAnnouncements

/**
 * C4「互動 / 通知」分页胶囊. 未选中的分页有未读时, 文字后有 6 号提示点, 读屏读作「N 則未讀」.
 */
@Composable
internal fun AppleNotificationGroupPills(
    groups: List<NotificationGroup>,
    selectedGroup: NotificationGroup,
    unreadCountOf: (NotificationGroup) -> Int,
    onSelect: (NotificationGroup) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        for (group in groups) {
            val selected = group == selectedGroup
            val unreadCount = unreadCountOf(group)
            val alertDescription = if (!selected && unreadCount > 0) {
                stringResource(Lang.notification_unread_count, unreadCount)
            } else {
                null
            }
            PillTab(
                stringResource(group.labelRes),
                selected = selected,
                onClick = { onSelect(group) },
                Modifier
                    .testTag(NotificationTestTags.tab(group))
                    .semantics {
                        if (alertDescription != null) stateDescription = alertDescription
                    },
                alert = alertDescription != null,
            )
        }
    }
}

/**
 * 公告的分类小字, 例如「置頂 · 維護」, 置顶时前面有图钉. 分类用灰字, 不用彩色 tag.
 */
@Composable
internal fun AppleAnnouncementCategory(
    announcement: Announcement,
    style: TextStyle,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.secondary
    val tag = stringResource(announcement.tag.labelRes)
    val text = if (announcement.pinned) stringResource(Lang.notification_announcement_pinned) + " · " + tag else tag
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (announcement.pinned) {
            Icon(
                Icons.Rounded.PushPin,
                contentDescription = null,
                Modifier.size(12.dp).testTag(AnnouncementTestTags.PIN),
                tint = color,
            )
        }
        Text(text, Modifier.testTag(AnnouncementTestTags.TAG), style = style, color = color, maxLines = 1)
    }
}

/**
 * Apple 版公告列表的一行 (03 §4.8, 04 §4.6): 分类小字、标题、最多两行的内文与日期. 未读时标题加粗, 标题后有提示点.
 *
 * @param read 游客没有已读状态, 为 `null`: 不加粗也不显示提示点.
 * @param selected macOS 双栏中这一行是否正在右侧显示. 为 `null` 时不可选中 (手机).
 */
@Composable
internal fun AppleAnnouncementRow(
    announcement: Announcement,
    read: Boolean?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean? = null,
) {
    val mac = LocalPlatform.current.isMacOS()
    val colorScheme = MaterialTheme.colorScheme
    val unreadText = stringResource(Lang.notification_unread)
    val preview = remember(announcement.body) { displayBodyPreview(announcement.body) }
    val isSelected = selected == true
    Column(
        modifier
            .fillMaxWidth()
            .background(if (isSelected) colorScheme.secondaryContainer else colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick, role = Role.Button)
            .semantics {
                if (read == false) stateDescription = unreadText
                if (selected != null) this.selected = isSelected
            }
            .padding(
                if (mac) AppleNotificationDefaults.MacAnnouncementRowPadding else AppleNotificationDefaults.AnnouncementRowPadding,
            ),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        AppleAnnouncementCategory(announcement, if (mac) AniTextStyles.macCaption else AniTextStyles.caption)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                announcement.title,
                Modifier.weight(1f),
                style = if (mac) AniTextStyles.macBody else AniTextStyles.body,
                fontWeight = if (read == false) FontWeight.SemiBold else FontWeight.Normal,
                color = colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (read == false) {
                // 与标题第一行居中: 手机行高 22, macOS 20
                AlertDot(Modifier.padding(top = if (mac) 6.dp else 7.dp).testTag(AnnouncementTestTags.UNREAD_DOT))
            }
        }
        if (preview != null) {
            Text(
                preview,
                Modifier.testTag(AppleNotificationTestTags.RowBody),
                style = if (mac) AniTextStyles.macCallout else AniTextStyles.subhead,
                color = AniColors.labelSoft,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            formatAnnouncementRowTime(announcement.publishedAtMillis),
            Modifier.testTag(AppleNotificationTestTags.RowMeta),
            style = if (mac) AniTextStyles.macCaption else AniTextStyles.caption,
            color = colorScheme.secondary,
            maxLines = 1,
        )
    }
}

/**
 * 列表中有当前版本不认识类型的通知时, 卡片上方的提示. 「更新」前往软件更新设置.
 */
@Composable
internal fun AppleNotificationUpdateBanner(
    onUpdate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val mac = LocalPlatform.current.isMacOS()
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (mac) AppleNotificationDefaults.MacCardRadius else AppleNotificationDefaults.CardRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .testTag(NotificationTestTags.UPDATE_BANNER)
            // 读屏把提示作为一个整体读出, 按钮仍可单独操作
            .semantics(mergeDescendants = true) {}
            .padding(start = if (mac) 12.dp else 16.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(Lang.notification_update_banner),
            Modifier.weight(1f).padding(vertical = if (mac) 8.dp else 12.dp),
            style = if (mac) AniTextStyles.macCallout else AniTextStyles.footnote,
            color = MaterialTheme.colorScheme.secondary,
        )
        AppleTextButton(onUpdate, Modifier.testTag(NotificationTestTags.UPDATE_BANNER_ACTION)) {
            Text(stringResource(Lang.notification_update_banner_action))
        }
    }
}

/**
 * 没有内容时的提示: fill 底圆形图标, 下面是标题与说明, 文字居中. 读屏时作为一个整体朗读.
 */
@Composable
internal fun AppleNotificationEmptyState(
    icon: ImageVector,
    title: String,
    supportingText: String?,
    modifier: Modifier = Modifier,
) {
    val mac = LocalPlatform.current.isMacOS()
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {}
            .padding(horizontal = 32.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(if (mac) 48.dp else 56.dp)
                .clip(CircleShape)
                .background(colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, Modifier.size(if (mac) 22.dp else 26.dp), tint = colorScheme.secondary)
        }
        Spacer(Modifier.height(12.dp))
        Text(
            title,
            style = if (mac) AniTextStyles.macWindowTitle else AniTextStyles.headline,
            color = colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        if (supportingText != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                supportingText,
                style = if (mac) AniTextStyles.macCallout else AniTextStyles.footnote,
                color = colorScheme.secondary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/**
 * Apple 版通知列表的内容 (iOS 与 macOS 共用): 游客的登录提示、加载中、出错与空列表, 或卡片中的通知.
 * 已加载的通知中有不认识的类型时, 卡片上方提示更新.
 *
 * @param mac 按 macOS 规格排列卡片与分隔线.
 * @param horizontalPadding 卡片与提示的左右外距.
 * @param selectedId macOS 双栏中正在显示的通知.
 * @param selectable 各行是否带选中状态 (macOS 双栏).
 */
internal fun LazyListScope.appleNotificationItems(
    page: NotificationCenterPage,
    access: NotificationAccess,
    showUpdateBanner: Boolean,
    mac: Boolean,
    horizontalPadding: Dp,
    selectedId: String?,
    selectable: Boolean,
    onClickNotification: (AppNotification) -> Unit,
    onInlineAction: (AppNotification, NotificationInlineAction) -> Unit,
    onCheckUpdate: () -> Unit,
    onLogin: () -> Unit,
) {
    val items = page.items
    when (access) {
        NotificationAccess.GUEST -> {
            item(key = "login", contentType = "error") {
                LoadErrorCard(
                    LoadError.RequiresLogin,
                    onRetry = {},
                    Modifier.padding(horizontal = horizontalPadding),
                    onLogin = onLogin,
                )
            }
            return
        }

        NotificationAccess.LOADING -> {
            placeholderCard(mac, horizontalPadding)
            return
        }

        NotificationAccess.AUTHORIZED -> {}
    }

    if (items.itemCount == 0) {
        when {
            items.loadState.refresh is LoadState.Error -> item(key = "error", contentType = "error") {
                val error by items.rememberLoadErrorState()
                LoadErrorCard(
                    error,
                    onRetry = { items.retry() },
                    Modifier.padding(horizontal = horizontalPadding),
                    onLogin = onLogin,
                )
            }

            items.isLoadingFirstPage || items.loadState.refresh is LoadState.Loading -> placeholderCard(mac, horizontalPadding)

            else -> item(key = "empty", contentType = "empty") {
                AppleNotificationEmptyState(
                    page.group.emptyIcon,
                    stringResource(page.group.emptyTextRes),
                    stringResource(page.group.emptySupportingTextRes),
                    Modifier.testTag(NotificationTestTags.EMPTY),
                )
            }
        }
        return
    }

    if (showUpdateBanner) {
        item(key = "update-banner", contentType = "update-banner") {
            AppleNotificationUpdateBanner(
                onCheckUpdate,
                Modifier
                    .padding(horizontal = horizontalPadding)
                    .padding(bottom = 8.dp),
            )
        }
    }

    val radius = if (mac) AppleNotificationDefaults.MacCardRadius else AppleNotificationDefaults.CardRadius
    val dividerIndent = if (mac) AppleNotificationDefaults.MacDividerIndent else AppleNotificationDefaults.DividerIndent
    items(
        items.itemCount,
        key = items.itemKey { it.id },
        contentType = { "notification" },
    ) { index ->
        Box(
            Modifier
                .padding(horizontal = horizontalPadding)
                .fillMaxWidth()
                .clip(appleListCardShape(index, items.itemCount, radius))
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            val notification = items[index]
            if (notification == null) {
                AppleNotificationPlaceholderRow()
            } else {
                AppleNotificationRow(
                    notification,
                    read = page.readOverlay.isRead(notification),
                    onClick = { onClickNotification(notification) },
                    onInlineAction = { onInlineAction(notification, it) },
                    Modifier.testTag(NotificationTestTags.item(notification.id)),
                    selected = if (selectable) notification.id == selectedId else null,
                )
            }
            if (index > 0) {
                AppleDivider(Modifier.align(Alignment.TopStart), startIndent = dividerIndent)
            }
        }
    }

    appendStateItems(items, horizontalPadding, onLogin)
}

/**
 * Apple 版公告列表的内容 (iOS 与 macOS 共用): 加载中、出错与空列表, 或卡片中的公告. 游客也能浏览公告.
 *
 * @param selectedId macOS 双栏中正在显示的公告.
 * @param selectable 各行是否带选中状态 (macOS 双栏).
 */
internal fun LazyListScope.appleAnnouncementItems(
    items: LazyPagingItems<Announcement>,
    access: NotificationAccess,
    readOverlay: ReadOverlay,
    mac: Boolean,
    horizontalPadding: Dp,
    selectedId: String?,
    selectable: Boolean,
    onClickAnnouncement: (Announcement) -> Unit,
    onLogin: () -> Unit,
) {
    if (items.itemCount == 0) {
        when {
            access == NotificationAccess.LOADING -> placeholderCard(mac, horizontalPadding)

            items.loadState.refresh is LoadState.Error -> item(key = "error", contentType = "error") {
                val error by items.rememberLoadErrorState()
                LoadErrorCard(
                    error,
                    onRetry = { items.retry() },
                    Modifier.padding(horizontal = horizontalPadding),
                    onLogin = onLogin,
                )
            }

            items.isLoadingFirstPage || items.loadState.refresh is LoadState.Loading -> placeholderCard(mac, horizontalPadding)

            else -> item(key = "empty", contentType = "empty") {
                AppleNotificationEmptyState(
                    Icons.Rounded.Campaign,
                    stringResource(Lang.notification_announcements_empty),
                    stringResource(Lang.notification_announcements_empty_supporting),
                    Modifier.testTag(AnnouncementTestTags.EMPTY),
                )
            }
        }
        return
    }

    val radius = if (mac) AppleNotificationDefaults.MacCardRadius else AppleNotificationDefaults.CardRadius
    val dividerIndent = if (mac) {
        AppleNotificationDefaults.MacAnnouncementDividerIndent
    } else {
        AppleNotificationDefaults.AnnouncementDividerIndent
    }
    items(
        items.itemCount,
        key = items.itemKey { it.id },
        contentType = { "announcement" },
    ) { index ->
        Box(
            Modifier
                .padding(horizontal = horizontalPadding)
                .fillMaxWidth()
                .clip(appleListCardShape(index, items.itemCount, radius))
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            val announcement = items[index]
            if (announcement == null) {
                AppleNotificationPlaceholderRow()
            } else {
                AppleAnnouncementRow(
                    announcement,
                    read = readOverlay.isRead(announcement),
                    onClick = { onClickAnnouncement(announcement) },
                    Modifier.testTag(AnnouncementTestTags.item(announcement.id)),
                    selected = if (selectable) announcement.id == selectedId else null,
                )
            }
            if (index > 0) {
                AppleDivider(Modifier.align(Alignment.TopStart), startIndent = dividerIndent)
            }
        }
    }

    appendStateItems(items, horizontalPadding, onLogin)
}

/**
 * 公告列表中显示提示点的公告数, 即进入列表前未读的公告. 进入列表时服务端就把它们标为已读, 所以不用服务端的未读数.
 */
internal fun AnnouncementsState.displayedUnreadCount(): Int =
    items.itemSnapshotList.items.count { readOverlay.isRead(it) == false }

private fun LazyListScope.placeholderCard(mac: Boolean, horizontalPadding: Dp) {
    val radius = if (mac) AppleNotificationDefaults.MacCardRadius else AppleNotificationDefaults.CardRadius
    val count = 3
    items(count, key = { "placeholder-$it" }, contentType = { "placeholder" }) { index ->
        AppleNotificationPlaceholderRow(
            Modifier
                .padding(horizontal = horizontalPadding)
                .clip(appleListCardShape(index, count, radius))
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        )
    }
}

private fun LazyListScope.appendStateItems(
    items: LazyPagingItems<*>,
    horizontalPadding: Dp,
    onLogin: () -> Unit,
) {
    when (items.loadState.append) {
        is LoadState.Error -> item(key = "append-error", contentType = "error") {
            val error by items.rememberLoadErrorState()
            LoadErrorCard(
                error,
                onRetry = { items.retry() },
                Modifier.padding(horizontal = horizontalPadding).padding(top = 12.dp),
                onLogin = onLogin,
            )
        }

        is LoadState.Loading -> item(key = "append-loading", contentType = "loading") {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(24.dp))
            }
        }

        is LoadState.NotLoading -> {}
    }
}

/**
 * 可选择复制的正文, 其中的网址可点击, 点击后在浏览器中打开.
 */
@Composable
internal fun AppleLinkifiedText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
) {
    val browserNavigator = rememberAsyncBrowserNavigator()
    val context = LocalContext.current
    val linkColor = MaterialTheme.colorScheme.primary
    val annotated = remember(text, linkColor, browserNavigator, context) {
        linkifyText(
            text,
            linkStyles = TextLinkStyles(style = SpanStyle(color = linkColor)),
            onClickUrl = { browserNavigator.openBrowser(context, it) },
        )
    }
    SelectionContainer(modifier) {
        Text(annotated, style = style, color = color)
    }
}

/**
 * 引用块: card 底, 左侧 3 宽的 separator 竖线, 正文可选择复制, 网址可点击.
 * 手机圆角 16、内距 16、正文 15/23; macOS 圆角 12、内距 14 16、正文 13/20.
 */
@Composable
internal fun AppleQuoteBlock(
    text: String,
    modifier: Modifier = Modifier,
) {
    val mac = LocalPlatform.current.isMacOS()
    val barColor = MaterialTheme.colorScheme.outlineVariant
    val horizontalPadding = 16.dp
    val verticalPadding = if (mac) 14.dp else 16.dp
    Box(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (mac) AppleNotificationDefaults.MacCardRadius else AppleNotificationDefaults.CardRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .drawBehind {
                drawRect(
                    barColor,
                    topLeft = Offset(horizontalPadding.toPx(), verticalPadding.toPx()),
                    size = Size(3.dp.toPx(), size.height - verticalPadding.toPx() * 2),
                )
            }
            .testTag(AppleNotificationTestTags.Quote)
            .padding(start = horizontalPadding + 3.dp + 12.dp, top = verticalPadding, end = horizontalPadding, bottom = verticalPadding),
    ) {
        AppleLinkifiedText(
            text,
            style = if (mac) {
                AniTextStyles.macCallout.copy(lineHeight = 20.sp)
            } else {
                AniTextStyles.callout.copy(lineHeight = 23.sp)
            },
            color = AniColors.labelSoft,
        )
    }
}
