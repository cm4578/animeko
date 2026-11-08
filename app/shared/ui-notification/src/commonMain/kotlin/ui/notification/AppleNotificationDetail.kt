/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.notification

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.him188.ani.app.data.models.notification.Announcement
import me.him188.ani.app.data.models.notification.AppNotification
import me.him188.ani.app.data.models.notification.AppNotificationContent
import me.him188.ani.app.data.models.notification.NotificationDestination
import me.him188.ani.app.data.models.notification.NotificationDisplay
import me.him188.ani.app.data.models.notification.SystemNotificationEvent
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.navigation.rememberAsyncBrowserNavigator
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleButton
import me.him188.ani.app.ui.foundation.apple.AppleButtonStyle
import me.him188.ani.app.ui.foundation.apple.AppleCircleButton
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.CollapsedTopBar
import me.him188.ani.app.ui.foundation.apple.PushedPageHeader
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.notification_announcement_detail_title
import me.him188.ani.app.ui.lang.notification_announcement_not_found
import me.him188.ani.app.ui.lang.notification_check_update
import me.him188.ani.app.ui.lang.notification_not_found
import me.him188.ani.app.ui.lang.notification_report_handled_at
import me.him188.ani.app.ui.lang.notification_report_info_title
import me.him188.ani.app.ui.lang.notification_report_outcome
import me.him188.ani.app.ui.lang.notification_report_outcome_handled
import me.him188.ani.app.ui.lang.notification_report_outcome_removed
import me.him188.ani.app.ui.lang.notification_report_privacy_footnote
import me.him188.ani.app.ui.lang.notification_report_reason
import me.him188.ani.app.ui.lang.notification_report_reported_at
import me.him188.ani.app.ui.lang.notification_report_result_description_handled
import me.him188.ani.app.ui.lang.notification_report_result_description_removed
import me.him188.ani.app.ui.lang.notification_report_result_detail_title
import me.him188.ani.app.ui.lang.notification_report_result_upheld
import me.him188.ani.app.ui.lang.notification_report_snapshot_footnote
import me.him188.ani.app.ui.lang.notification_report_snapshot_title
import me.him188.ani.app.ui.lang.notification_report_target
import me.him188.ani.app.ui.lang.notification_system_bangumi_expired
import me.him188.ani.app.ui.lang.notification_system_bangumi_expired_supporting
import me.him188.ani.app.ui.lang.notification_system_bangumi_reauthorize
import me.him188.ani.app.ui.lang.notification_title
import me.him188.ani.app.ui.lang.notification_update_hint
import me.him188.ani.app.ui.lang.notification_view_details
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.utils.platform.isMacOS
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/*
 * iOS 通知详情与公告详情 (03 §4.9), 以及 macOS 右栏的详情内容 (04 §4.6).
 *
 * - 用通用展示内容展示的通知 (互动等): 标题 `display.title`、时间、`display.body` 引用块、目标一列与跳转按钮 (05 C4、G20).
 * - 举报结果: 结果卡、「處理資訊」键值列表、被举报内容的快照与注脚.
 * - 系统通知与公告: 标题、时间、分隔线、正文与底部按钮.
 */

/**
 * iOS 单独打开的通知详情页 (03 §4.9). 账号安全通知没有详情 (见 [NotificationOpenTarget]), 只有标题.
 *
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleNotificationDetailScreen(
    vm: NotificationDetailViewModel,
    actions: NotificationActions,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    AppleNotificationDetailLayout(state, actions, vm::reload, onBack, modifier, windowInsets, contentPadding)
}

@Composable
internal fun AppleNotificationDetailLayout(
    state: DetailLoadState<AppNotification>,
    actions: NotificationActions,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    if (state !is DetailLoadState.Loaded) {
        AppleDetailLoadState(
            stringResource(Lang.notification_title),
            state,
            notFoundText = stringResource(Lang.notification_not_found),
            notFoundTestTag = NotificationTestTags.DETAIL_NOT_FOUND,
            onRetry = onRetry,
            onBack = onBack,
            modifier = modifier,
            windowInsets = windowInsets,
        )
        return
    }
    val notification = state.value
    AppleDetailScaffold(
        collapsedTitle = appleNotificationDetailTitle(notification),
        onBack = onBack,
        modifier = modifier,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
    ) {
        when (val content = notification.content) {
            is AppNotificationContent.ReportResult ->
                AppleReportResultDetail(notification, content, actions, onBack, windowInsets)

            is AppNotificationContent.AccountSecurity ->
                PushedPageHeader(stringResource(Lang.notification_title), onBack, windowInsets = windowInsets)

            is AppNotificationContent.System -> AppleSystemNotificationDetail(notification, content, actions, onBack, windowInsets)
            AppNotificationContent.Unknown -> AppleDisplayNotificationDetail(notification, actions, onBack, windowInsets)
        }
    }
}

/**
 * iOS 单独打开的公告详情页 (03 §4.9).
 *
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleAnnouncementDetailScreen(
    vm: AnnouncementDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    AppleAnnouncementDetailLayout(state, vm::reload, onBack, modifier, windowInsets, contentPadding)
}

@Composable
internal fun AppleAnnouncementDetailLayout(
    state: DetailLoadState<Announcement>,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    if (state !is DetailLoadState.Loaded) {
        AppleDetailLoadState(
            stringResource(Lang.notification_announcement_detail_title),
            state,
            notFoundText = stringResource(Lang.notification_announcement_not_found),
            notFoundTestTag = AnnouncementTestTags.DETAIL_NOT_FOUND,
            onRetry = onRetry,
            onBack = onBack,
            modifier = modifier,
            windowInsets = windowInsets,
        )
        return
    }
    val announcement = state.value
    val openUrl = rememberOpenUrl()
    AppleDetailScaffold(
        collapsedTitle = announcement.title,
        onBack = onBack,
        modifier = modifier,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
    ) {
        AppleArticleDetail(
            category = { AppleAnnouncementCategory(announcement, AniTextStyles.footnote) },
            title = announcement.title,
            timeMillis = announcement.publishedAtMillis,
            body = announcement.body,
            onBack = onBack,
            windowInsets = windowInsets,
            action = announcement.linkUrl?.let { url ->
                {
                    AppleDetailButton(
                        stringResource(Lang.notification_view_details),
                        onClick = { openUrl(url) },
                        Modifier.testTag(AnnouncementTestTags.DETAIL_LINK),
                        external = true,
                    )
                }
            },
        )
    }
}

/**
 * 精简栏的标题, 与页面的标题一致.
 */
@Composable
private fun appleNotificationDetailTitle(notification: AppNotification): String =
    when (val content = notification.content) {
        is AppNotificationContent.ReportResult -> stringResource(Lang.notification_report_result_detail_title)
        is AppNotificationContent.AccountSecurity -> stringResource(Lang.notification_title)
        is AppNotificationContent.System -> when (content.event) {
            SystemNotificationEvent.BANGUMI_AUTHORIZATION_EXPIRED -> stringResource(Lang.notification_system_bangumi_expired)
            null -> notification.display.title
        }

        AppNotificationContent.Unknown -> notification.display.title
    }

/**
 * 可滚动的详情页: 返回钮一行滚出后显示精简栏 ([collapsedTitle]).
 */
@Composable
private fun AppleDetailScaffold(
    collapsedTitle: String,
    onBack: () -> Unit,
    modifier: Modifier,
    windowInsets: WindowInsets,
    contentPadding: PaddingValues,
    content: @Composable ColumnScope.() -> Unit,
) {
    val scrollState = rememberScrollState()
    val layoutDirection = LocalLayoutDirection.current
    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(
                    start = contentPadding.calculateStartPadding(layoutDirection),
                    top = contentPadding.calculateTopPadding(),
                    end = contentPadding.calculateEndPadding(layoutDirection),
                    bottom = contentPadding.calculateBottomPadding() + 32.dp,
                )
                .testTag(NotificationTestTags.DETAIL),
            content = content,
        )
        val collapsed by rememberScrollCollapsed(scrollState, windowInsets)
        if (collapsed) {
            CollapsedTopBar(
                collapsedTitle,
                onBack,
                Modifier.testTag(AppleNotificationTestTags.CollapsedBar),
                windowInsets = windowInsets,
            )
        }
    }
}

@Composable
private fun rememberScrollCollapsed(scrollState: ScrollState, windowInsets: WindowInsets): State<Boolean> {
    val density = LocalDensity.current
    return remember(scrollState, density, windowInsets) {
        derivedStateOf {
            scrollState.value > windowInsets.getTop(density) +
                    with(density) { AppleNotificationDefaults.HeaderBackRowBottom.roundToPx() }
        }
    }
}

/**
 * 加载中显示转圈, 不存在时显示提示, 加载失败显示现有的错误卡片 (可重试). 大标题为 [title].
 */
@Composable
private fun AppleDetailLoadState(
    title: String,
    state: DetailLoadState<*>,
    notFoundText: String,
    notFoundTestTag: String,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier,
    windowInsets: WindowInsets,
) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PushedPageHeader(title, onBack, windowInsets = windowInsets)
        DetailLoadStateContent(
            state,
            notFoundText = notFoundText,
            notFoundTestTag = notFoundTestTag,
            onRetry = onRetry,
            Modifier.weight(1f).fillMaxWidth().padding(16.dp),
        )
    }
}

/**
 * 详情的加载中、不存在与加载失败. iOS 与 macOS 共用.
 */
@Composable
internal fun DetailLoadStateContent(
    state: DetailLoadState<*>,
    notFoundText: String,
    notFoundTestTag: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        when (state) {
            DetailLoadState.Loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

            DetailLoadState.NotFound -> AppleNotificationEmptyState(
                Icons.Outlined.SearchOff,
                notFoundText,
                supportingText = null,
                Modifier.align(Alignment.Center).testTag(notFoundTestTag),
            )

            is DetailLoadState.Failed -> LoadErrorCard(
                state.error,
                onRetry = onRetry,
                Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
            )

            is DetailLoadState.Loaded -> {}
        }
    }
}

/**
 * 互动等用通用展示内容展示的通知 (03 §4.9 通知详情 (互动), 05 C4): C3 大标题 `display.title`, 时间,
 * `display.body` 引用块, 目标一列 (`display.overline`, 有跳转位置时可点) 与跳转按钮. 不显示评论串 (05 G20).
 * 客户端不认识这个类型时, 底部提示更新.
 */
@Composable
private fun AppleDisplayNotificationDetail(
    notification: AppNotification,
    actions: NotificationActions,
    onBack: () -> Unit,
    windowInsets: WindowInsets,
) {
    val display = notification.display
    val destination = display.target?.destination
    val openUrl = rememberOpenUrl()
    val openDestination: (() -> Unit)? = destination?.let { { actions.open(it, openUrl) } }

    PushedPageHeader(display.title, onBack, windowInsets = windowInsets)
    DetailTime(notification.createdAtMillis, Modifier.padding(start = 20.dp, top = 4.dp, end = 20.dp))

    val body = display.body?.takeIf { it.isNotBlank() }
    if (body != null) {
        AppleQuoteBlock(body.trim(), Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp))
    }

    val overline = display.overline?.trim()?.takeIf { it.isNotEmpty() }
    if (overline != null) {
        AppleTargetRow(overline, openDestination, Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp))
    }

    if (destination != null && openDestination != null) {
        AppleDetailButton(
            stringResource(displayDestinationLabelRes(notification, destination)),
            onClick = openDestination,
            Modifier
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .testTag(destinationTestTag(destination)),
            external = destination is NotificationDestination.Url,
        )
    }

    if (notification.isUnknownType) {
        AppleUpdateHint(actions.onCheckUpdate, Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp))
    }
}

/**
 * 举报结果 (03 §4.9): 结果卡、「處理資訊」键值列表 (「檢舉對象」可点, 前往该剧集等)、被举报内容的快照与注脚.
 */
@Composable
private fun AppleReportResultDetail(
    notification: AppNotification,
    content: AppNotificationContent.ReportResult,
    actions: NotificationActions,
    onBack: () -> Unit,
    windowInsets: WindowInsets,
) {
    PushedPageHeader(stringResource(Lang.notification_report_result_detail_title), onBack, windowInsets = windowInsets)
    ReportResultSections(notification, content, actions, Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp))
}

/**
 * 举报结果的结果卡、「處理資訊」、快照与注脚. iOS 与 macOS 共用, 尺寸按平台.
 *
 * @param targetClickable 「檢舉對象」一列是否可点. macOS 的跳转按钮在标题行右侧, 这一列不可点.
 */
@Composable
internal fun ReportResultSections(
    notification: AppNotification,
    content: AppNotificationContent.ReportResult,
    actions: NotificationActions,
    modifier: Modifier = Modifier,
    targetClickable: Boolean = true,
) {
    val mac = LocalPlatform.current.isMacOS()
    val colorScheme = MaterialTheme.colorScheme
    val useOriginalTitle = LocalSubjectAppearanceSettings.current.useOriginalTitle
    val openUrl = rememberOpenUrl()
    val sectionSpacing = if (mac) 20.dp else 24.dp
    Column(modifier) {
        // 结果卡
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(if (mac) AppleNotificationDefaults.MacCardRadius else AppleNotificationDefaults.CardRadius))
                .background(colorScheme.surfaceContainerLow)
                .testTag(AppleNotificationTestTags.ResultCard)
                .padding(horizontal = 16.dp, vertical = if (mac) 14.dp else 16.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (mac) 10.dp else 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier
                        .size(if (mac) 32.dp else 40.dp)
                        .clip(CircleShape)
                        .background(colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        AppleIcons.check(),
                        contentDescription = null,
                        Modifier.size(if (mac) 16.dp else 20.dp),
                        tint = colorScheme.onSecondaryContainer,
                    )
                }
                Column {
                    Text(
                        stringResource(Lang.notification_report_result_upheld),
                        style = if (mac) AniTextStyles.macTitle else AniTextStyles.headline.copy(fontWeight = FontWeight.Bold),
                        color = colorScheme.onSurface,
                    )
                    DetailTime(notification.createdAtMillis)
                }
            }
            Text(
                stringResource(
                    if (content.contentRemoved) Lang.notification_report_result_description_removed
                    else Lang.notification_report_result_description_handled,
                ),
                Modifier.padding(top = if (mac) 10.dp else 12.dp),
                style = if (mac) AniTextStyles.macCallout.copy(lineHeight = 20.sp) else AniTextStyles.callout.copy(lineHeight = 23.sp),
                color = AniColors.labelSoft,
            )
        }

        // 處理資訊
        val onClickTarget = if (targetClickable) reportTargetClick(content, notification.display, actions, openUrl) else null
        AppleDetailSection(stringResource(Lang.notification_report_info_title), Modifier.padding(top = sectionSpacing)) {
            AppleKeyValueRow(
                stringResource(Lang.notification_report_reason),
                stringResource(content.reason.labelRes),
                Modifier.testTag(NotificationTestTags.REPORT_REASON),
            )
            AppleKeyValueRow(
                stringResource(Lang.notification_report_target),
                reportTargetText(content, notification.display, useOriginalTitle).resolve(),
                Modifier.testTag(NotificationTestTags.REPORT_TARGET),
                onClick = onClickTarget,
                divider = true,
            )
            AppleKeyValueRow(
                stringResource(Lang.notification_report_reported_at),
                formatNotificationDetailTime(content.reportedAtMillis),
                divider = true,
            )
            AppleKeyValueRow(
                stringResource(Lang.notification_report_handled_at),
                formatNotificationDetailTime(content.handledAtMillis),
                divider = true,
            )
            AppleKeyValueRow(
                stringResource(Lang.notification_report_outcome),
                stringResource(
                    if (content.contentRemoved) Lang.notification_report_outcome_removed
                    else Lang.notification_report_outcome_handled,
                ),
                Modifier.testTag(NotificationTestTags.REPORT_OUTCOME),
                divider = true,
            )
        }

        // 举报时可以不附带快照
        val snapshot = content.contentSnapshot?.takeIf { it.isNotBlank() }?.let { parseReportSnapshot(it) }
        if (snapshot != null) {
            AppleDetailSection(stringResource(Lang.notification_report_snapshot_title), Modifier.padding(top = sectionSpacing)) {
                ReportSnapshotCard(snapshot, content.contentCreatedAtMillis, mac)
            }
        }

        val privacyFootnote = stringResource(Lang.notification_report_privacy_footnote)
        Text(
            if (snapshot != null) {
                stringResource(Lang.notification_report_snapshot_footnote, privacyFootnote)
            } else {
                privacyFootnote
            },
            Modifier
                .padding(start = if (mac) 12.dp else 4.dp, top = 12.dp, end = if (mac) 12.dp else 4.dp)
                .testTag(AppleNotificationTestTags.Footnote),
            style = if (mac) AniTextStyles.macCaption else AniTextStyles.caption.copy(lineHeight = 18.sp),
            color = colorScheme.secondary,
        )
    }
}

/**
 * 被举报评论的快照: 头像 (昵称首字)、昵称与发表时间, 下面是正文.
 */
@Composable
private fun ReportSnapshotCard(snapshot: ReportSnapshot, createdAtMillis: Long?, mac: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        Modifier.padding(horizontal = if (mac) 12.dp else 16.dp, vertical = if (mac) 12.dp else 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val author = snapshot.author
        if (author != null || createdAtMillis != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (author != null) {
                    Box(
                        Modifier
                            .size(if (mac) 24.dp else 28.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surfaceContainerHighest),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            author.take(1),
                            style = if (mac) AniTextStyles.macCaption else AniTextStyles.caption,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.secondary,
                        )
                    }
                    Text(
                        author,
                        Modifier.weight(1f, fill = false).testTag(NotificationTestTags.REPORT_SNAPSHOT_AUTHOR),
                        style = if (mac) AniTextStyles.macCallout else AniTextStyles.subhead,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (createdAtMillis != null) {
                    Text(
                        formatNotificationDetailTime(createdAtMillis),
                        Modifier.testTag(NotificationTestTags.REPORT_SNAPSHOT_TIME),
                        style = if (mac) AniTextStyles.macCaption else AniTextStyles.footnote,
                        color = colorScheme.secondary,
                        maxLines = 1,
                    )
                }
            }
        }
        Text(
            snapshot.text,
            Modifier.testTag(NotificationTestTags.REPORT_SNAPSHOT_TEXT),
            style = if (mac) AniTextStyles.macCallout.copy(lineHeight = 20.sp) else AniTextStyles.callout.copy(lineHeight = 22.sp),
            color = AniColors.labelSoft,
        )
    }
}

/**
 * 系统通知: 照公告详情的版式. 认识的事件使用客户端的文案和操作, 否则为服务端的通用展示内容.
 */
@Composable
private fun AppleSystemNotificationDetail(
    notification: AppNotification,
    content: AppNotificationContent.System,
    actions: NotificationActions,
    onBack: () -> Unit,
    windowInsets: WindowInsets,
) {
    val article = systemNotificationArticle(notification, content, actions)
    AppleArticleDetail(
        category = null,
        title = article.title,
        timeMillis = notification.createdAtMillis,
        body = article.body,
        onBack = onBack,
        windowInsets = windowInsets,
        action = article.action?.let { action ->
            {
                AppleDetailButton(
                    action.text,
                    onClick = action.onClick,
                    Modifier.testTag(action.testTag),
                    external = action.external,
                )
            }
        },
    )
}

/**
 * 系统通知详情的文字与底部按钮.
 */
internal class SystemNotificationArticle(
    val title: String,
    val body: String,
    val action: DetailAction?,
)

/**
 * 详情底部或标题行的跳转按钮.
 *
 * @param external 在浏览器中打开, 文字后有斜箭头.
 */
internal class DetailAction(
    val text: String,
    val onClick: () -> Unit,
    val testTag: String,
    val external: Boolean = false,
)

@Composable
internal fun systemNotificationArticle(
    notification: AppNotification,
    content: AppNotificationContent.System,
    actions: NotificationActions,
): SystemNotificationArticle {
    val openUrl = rememberOpenUrl()
    return when (content.event) {
        SystemNotificationEvent.BANGUMI_AUTHORIZATION_EXPIRED -> SystemNotificationArticle(
            title = stringResource(Lang.notification_system_bangumi_expired),
            body = stringResource(Lang.notification_system_bangumi_expired_supporting),
            action = DetailAction(
                stringResource(Lang.notification_system_bangumi_reauthorize),
                onClick = actions.onReauthorizeBangumi,
                testTag = NotificationTestTags.DETAIL_ACTION,
            ),
        )

        null -> {
            val display = notification.display
            val destination = display.target?.destination
            SystemNotificationArticle(
                title = display.title,
                body = display.body.orEmpty(),
                action = destination?.let {
                    DetailAction(
                        stringResource(displayDestinationLabelRes(notification, it)),
                        onClick = { actions.open(it, openUrl) },
                        testTag = destinationTestTag(it),
                        external = it is NotificationDestination.Url,
                    )
                },
            )
        }
    }
}

/**
 * 系统通知与公告的版式 (03 §4.9 公告详情): 返回钮, 分类, 标题 24/31 W800, 时间, 分隔线, 正文 16/26 labelSoft 与底部按钮.
 * 正文是纯文字: 保留换行, 网址可点.
 */
@Composable
private fun AppleArticleDetail(
    category: (@Composable () -> Unit)?,
    title: String,
    timeMillis: Long,
    body: String,
    onBack: () -> Unit,
    windowInsets: WindowInsets,
    action: (@Composable () -> Unit)?,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
            .padding(start = 12.dp, top = 10.dp, end = 12.dp),
    ) {
        AppleCircleButton(onBack, AppleIcons.back(), contentDescription = null)
    }
    Column(
        Modifier.padding(start = 20.dp, top = 12.dp, end = 20.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        category?.invoke()
        Text(
            title,
            Modifier.semantics { heading() },
            style = AniTextStyles.largeTitle.copy(fontSize = 24.sp, lineHeight = 31.sp),
            color = MaterialTheme.colorScheme.onSurface,
        )
        DetailTime(timeMillis)
    }
    AppleDivider(Modifier.padding(horizontal = 20.dp, vertical = 20.dp))
    if (body.isNotBlank()) {
        AppleLinkifiedText(
            body.trim(),
            style = AniTextStyles.body.copy(lineHeight = 26.sp),
            color = AniColors.labelSoft,
            Modifier.padding(horizontal = 20.dp),
        )
    }
    if (action != null) {
        Box(Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp)) {
            action()
        }
    }
}

/**
 * 详情中的时间: 手机 13/18 label2, macOS 12/16 label2.
 */
@Composable
private fun DetailTime(timeMillis: Long, modifier: Modifier = Modifier) {
    val mac = LocalPlatform.current.isMacOS()
    Text(
        formatNotificationDetailTime(timeMillis),
        modifier.testTag(AppleNotificationTestTags.DetailTime),
        style = if (mac) AniTextStyles.macCaption else AniTextStyles.footnote,
        color = MaterialTheme.colorScheme.secondary,
        maxLines = 1,
    )
}

/**
 * 互动详情的目标一列 (C5 高 52): 条目与剧集 (`display.overline`) 16/22 W600. [onClick] 不为 `null` 时可点, 末尾有箭头.
 */
@Composable
private fun AppleTargetRow(
    text: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(AppleNotificationDefaults.CardRadius))
            .background(colorScheme.surfaceContainerLow)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick, role = Role.Button) else Modifier)
            .testTag(AppleNotificationTestTags.TargetRow)
            .padding(start = 16.dp, end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            Modifier.weight(1f),
            style = AniTextStyles.body,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (onClick != null) {
            Icon(AppleIcons.chevronRight(), contentDescription = null, Modifier.size(16.dp), tint = colorScheme.secondary)
        }
    }
}

/**
 * 详情底部的 C8 淡底按钮: 手机高 48, 满版; 网页链接的文字后有斜箭头.
 */
@Composable
private fun AppleDetailButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    external: Boolean = false,
) {
    AppleButton(
        onClick = onClick,
        modifier.fillMaxWidth(),
        style = AppleButtonStyle.Tinted,
        height = 48.dp,
    ) {
        Text(text, maxLines = 1)
        if (external) {
            Icon(AppleIcons.externalLink(), contentDescription = null, Modifier.size(15.dp))
        }
    }
}

/**
 * 当前版本可能无法完整显示这条通知, 提示用户更新. 「檢查更新」前往软件更新设置.
 */
@Composable
internal fun AppleUpdateHint(onCheckUpdate: () -> Unit, modifier: Modifier = Modifier) {
    val mac = LocalPlatform.current.isMacOS()
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(if (mac) AppleNotificationDefaults.MacCardRadius else AppleNotificationDefaults.CardRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .testTag(NotificationTestTags.DETAIL_UPDATE_HINT)
            .padding(start = 16.dp, top = 12.dp, end = 12.dp, bottom = 4.dp),
    ) {
        Text(
            stringResource(Lang.notification_update_hint),
            style = if (mac) AniTextStyles.macCallout else AniTextStyles.footnote,
            color = MaterialTheme.colorScheme.secondary,
        )
        AppleTextButton(
            onCheckUpdate,
            Modifier.testTag(NotificationTestTags.DETAIL_CHECK_UPDATE),
        ) {
            Text(stringResource(Lang.notification_check_update))
        }
    }
}

/**
 * 详情中的分组: 标题 + 卡片 (手机圆角 16, macOS 12).
 */
@Composable
internal fun AppleDetailSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val mac = LocalPlatform.current.isMacOS()
    Column(modifier) {
        Text(
            title,
            Modifier
                .padding(start = if (mac) 12.dp else 16.dp, end = if (mac) 12.dp else 16.dp, bottom = 8.dp)
                .semantics { heading() },
            style = if (mac) AniTextStyles.macCaption else AniTextStyles.footnote,
            fontWeight = if (mac) FontWeight.SemiBold else null,
            color = MaterialTheme.colorScheme.secondary,
        )
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(if (mac) AppleNotificationDefaults.MacCardRadius else AppleNotificationDefaults.CardRadius))
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            content = content,
        )
    }
}

/**
 * C5 键值列: 手机高 44、键 15/20 label2、值 15/20 W500; macOS 高 36、13 号. [onClick] 不为 `null` 时整列可点, 末尾有箭头.
 *
 * @param divider 上方有分隔线 (不是卡片的第一列).
 */
@Composable
internal fun AppleKeyValueRow(
    key: String,
    value: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    divider: Boolean = false,
) {
    val mac = LocalPlatform.current.isMacOS()
    val colorScheme = MaterialTheme.colorScheme
    val horizontalPadding = if (mac) 12.dp else 16.dp
    Box {
        Row(
            modifier
                .fillMaxWidth()
                .heightIn(min = if (mac) 36.dp else 44.dp)
                // 读屏把键与值作为一个整体读出
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick, role = Role.Button)
                    } else {
                        Modifier.semantics(mergeDescendants = true) {}
                    },
                )
                .padding(start = horizontalPadding, end = if (onClick != null) horizontalPadding - 4.dp else horizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val style = if (mac) AniTextStyles.macCallout else AniTextStyles.callout
            Text(key, style = style, color = colorScheme.secondary, maxLines = 1)
            Text(
                value,
                Modifier.weight(1f),
                style = style,
                fontWeight = FontWeight.Medium,
                color = colorScheme.onSurface,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (onClick != null) {
                Icon(
                    AppleIcons.chevronRight(),
                    contentDescription = null,
                    Modifier.size(if (mac) 14.dp else 16.dp),
                    tint = colorScheme.secondary,
                )
            }
        }
        if (divider) {
            AppleDivider(Modifier.align(Alignment.TopStart), startIndent = horizontalPadding)
        }
    }
}

/**
 * 举报对象的跳转: 认识的评论类型用 [reportResultAction], 不认识的类型用通用展示内容中的位置. 都没有时为 `null`.
 */
internal fun reportTargetClick(
    content: AppNotificationContent.ReportResult,
    display: NotificationDisplay,
    actions: NotificationActions,
    openUrl: (String) -> Unit,
): (() -> Unit)? {
    val action = reportResultAction(content)
    if (action != null) return { actions.perform(action, content) }
    val destination = reportResultFallbackDestination(content, display) ?: return null
    return { actions.open(destination, openUrl) }
}

/**
 * [reportTargetClick] 的按钮文字, 例如「前往該劇集」. 没有跳转时为 `null`.
 */
internal fun reportTargetLabelRes(
    content: AppNotificationContent.ReportResult,
    display: NotificationDisplay,
): StringResource? =
    reportResultAction(content)?.labelRes ?: reportResultFallbackDestination(content, display)?.labelRes

internal fun destinationTestTag(destination: NotificationDestination): String =
    if (destination is NotificationDestination.Url) NotificationTestTags.DETAIL_LINK else NotificationTestTags.DETAIL_ACTION

/**
 * 在浏览器中打开网页链接.
 */
@Composable
internal fun rememberOpenUrl(): (String) -> Unit {
    val browserNavigator = rememberAsyncBrowserNavigator()
    val context = LocalContext.current
    return remember(browserNavigator, context) {
        { url -> browserNavigator.openBrowser(context, url) }
    }
}

/**
 * macOS 详情栏中的一条通知 (04 §4.6). 自带纵向滚动, 内容最宽 600.
 *
 * - 举报结果: 标题行右侧为「前往該劇集」等, 结果卡、「處理資訊」、快照与注脚.
 * - 互动等用通用展示内容展示的通知: 标题行右侧为「前往評論區」(没有跳转位置时不显示), 引用块与目标一行. 不显示评论串.
 * - 系统通知: 照公告详情的版式.
 */
@Composable
internal fun MacNotificationDetailContent(
    notification: AppNotification,
    actions: NotificationActions,
    modifier: Modifier = Modifier,
) {
    MacDetailColumn(modifier) {
        when (val content = notification.content) {
            is AppNotificationContent.ReportResult -> {
                val openUrl = rememberOpenUrl()
                val onClickTarget = reportTargetClick(content, notification.display, actions, openUrl)
                val label = reportTargetLabelRes(content, notification.display)
                MacDetailTitleRow(
                    stringResource(Lang.notification_report_result_detail_title),
                    action = if (onClickTarget != null && label != null) {
                        DetailAction(
                            stringResource(label),
                            onClick = onClickTarget,
                            testTag = NotificationTestTags.DETAIL_TOP_BAR_ACTION,
                        )
                    } else {
                        null
                    },
                )
                ReportResultSections(
                    notification,
                    content,
                    actions,
                    Modifier.padding(top = 16.dp),
                    targetClickable = false,
                )
            }

            is AppNotificationContent.AccountSecurity -> {}

            is AppNotificationContent.System -> {
                val article = systemNotificationArticle(notification, content, actions)
                MacArticleDetail(
                    category = null,
                    title = article.title,
                    timeMillis = notification.createdAtMillis,
                    body = article.body,
                    action = article.action,
                )
            }

            AppNotificationContent.Unknown -> MacDisplayNotificationDetail(notification, actions)
        }
    }
}

/**
 * macOS 详情栏中的一条公告 (04 §4.6): 分类小字、标题、发布时间、分隔线、正文, 末尾为「查看詳情 ↗」胶囊.
 */
@Composable
internal fun MacAnnouncementDetailContent(
    announcement: Announcement,
    modifier: Modifier = Modifier,
) {
    val openUrl = rememberOpenUrl()
    val linkText = stringResource(Lang.notification_view_details)
    MacDetailColumn(modifier, testTag = AnnouncementTestTags.DETAIL) {
        MacArticleDetail(
            category = { AppleAnnouncementCategory(announcement, AniTextStyles.macCaption) },
            title = announcement.title,
            timeMillis = announcement.publishedAtMillis,
            body = announcement.body,
            action = announcement.linkUrl?.let { url ->
                DetailAction(linkText, onClick = { openUrl(url) }, testTag = AnnouncementTestTags.DETAIL_LINK, external = true)
            },
        )
    }
}

/**
 * macOS 详情的滚动区域: 内距 8 40 0 32, 内容最宽 [AppleNotificationDefaults.MacDetailMaxWidth].
 */
@Composable
private fun MacDetailColumn(
    modifier: Modifier,
    testTag: String = NotificationTestTags.DETAIL,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier.verticalScroll(rememberScrollState())) {
        Column(
            Modifier
                .padding(start = 32.dp, top = 8.dp, end = 40.dp, bottom = 32.dp)
                .widthIn(max = AppleNotificationDefaults.MacDetailMaxWidth)
                .fillMaxWidth()
                .testTag(testTag),
            content = content,
        )
    }
}

@Composable
private fun MacDisplayNotificationDetail(
    notification: AppNotification,
    actions: NotificationActions,
) {
    val display = notification.display
    val destination = display.target?.destination
    val openUrl = rememberOpenUrl()
    MacDetailTitleRow(
        display.title,
        action = destination?.let {
            DetailAction(
                stringResource(displayDestinationLabelRes(notification, it)),
                onClick = { actions.open(it, openUrl) },
                testTag = destinationTestTag(it),
                external = it is NotificationDestination.Url,
            )
        },
    )
    DetailTime(notification.createdAtMillis, Modifier.padding(top = 4.dp))

    val body = display.body?.takeIf { it.isNotBlank() }
    if (body != null) {
        AppleQuoteBlock(body.trim(), Modifier.padding(top = 16.dp))
    }

    val overline = display.overline?.trim()?.takeIf { it.isNotEmpty() }
    if (overline != null) {
        Text(
            overline,
            Modifier
                .padding(top = 10.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(AppleNotificationDefaults.MacCardRadius))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .testTag(AppleNotificationTestTags.TargetRow)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            style = AniTextStyles.macCallout,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }

    if (notification.isUnknownType) {
        AppleUpdateHint(actions.onCheckUpdate, Modifier.padding(top = 20.dp))
    }
}

/**
 * macOS 详情的标题行: 标题 20/26 Bold, 右侧为 [action] 胶囊.
 */
@Composable
private fun MacDetailTitleRow(
    title: String,
    action: DetailAction?,
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            title,
            Modifier.weight(1f).semantics { heading() },
            style = AniTextStyles.title,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (action != null) {
            MacDetailCapsuleButton(action, Modifier.padding(top = 1.dp))
        }
    }
}

/**
 * macOS 系统通知与公告的版式: 分类, 标题, 时间, 分隔线, 正文与末尾的胶囊按钮.
 */
@Composable
private fun MacArticleDetail(
    category: (@Composable () -> Unit)?,
    title: String,
    timeMillis: Long,
    body: String,
    action: DetailAction?,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        category?.invoke()
        Text(
            title,
            Modifier.semantics { heading() },
            style = AniTextStyles.title.copy(fontWeight = FontWeight.ExtraBold),
            color = MaterialTheme.colorScheme.onSurface,
        )
        DetailTime(timeMillis)
    }
    AppleDivider(Modifier.padding(vertical = 16.dp))
    if (body.isNotBlank()) {
        AppleLinkifiedText(
            body.trim(),
            style = AniTextStyles.macBody.copy(lineHeight = 22.sp),
            color = AniColors.labelSoft,
        )
    }
    if (action != null) {
        Spacer(Modifier.height(20.dp))
        MacDetailCapsuleButton(action)
    }
}

/**
 * macOS 详情的 tint 胶囊按钮: 高 28, `macCallout` W600; 网页链接的文字后有斜箭头.
 */
@Composable
private fun MacDetailCapsuleButton(
    action: DetailAction,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier
            .height(28.dp)
            .clip(CircleShape)
            .background(colorScheme.secondaryContainer)
            .clickable(onClick = action.onClick, role = Role.Button)
            .testTag(action.testTag)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            action.text,
            style = AniTextStyles.macCallout,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSecondaryContainer,
            maxLines = 1,
        )
        if (action.external) {
            Icon(
                AppleIcons.macExternalLink(),
                contentDescription = null,
                Modifier.size(12.dp),
                tint = colorScheme.onSecondaryContainer,
            )
        }
    }
}
