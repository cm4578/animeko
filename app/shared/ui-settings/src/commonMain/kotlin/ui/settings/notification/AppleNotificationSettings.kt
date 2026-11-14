/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.preference.NotificationSettings
import me.him188.ani.app.ui.foundation.apple.CollapsedTopBar
import me.him188.ani.app.ui.foundation.apple.GroupedListCard
import me.him188.ani.app.ui.foundation.apple.GroupedListDefaults
import me.him188.ani.app.ui.foundation.apple.GroupedListSection
import me.him188.ani.app.ui.foundation.apple.PushedPageHeader
import me.him188.ani.app.ui.foundation.apple.SettingsRow
import me.him188.ani.app.ui.foundation.apple.SettingsRowNavigation
import me.him188.ani.app.ui.foundation.apple.SettingsSwitchRow
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.notification_center_title
import me.him188.ani.app.ui.lang.notification_settings
import me.him188.ani.app.ui.lang.notification_settings_account_security_item
import me.him188.ani.app.ui.lang.notification_settings_announcement_description_apple
import me.him188.ani.app.ui.lang.notification_settings_announcement_item
import me.him188.ani.app.ui.lang.notification_settings_group_announcements
import me.him188.ani.app.ui.lang.notification_settings_group_system
import me.him188.ani.app.ui.lang.notification_settings_report_result_item
import me.him188.ani.app.ui.lang.notification_settings_system_group_description
import me.him188.ani.app.ui.lang.notification_unread_count
import me.him188.ani.app.ui.settings.framework.SettingsState
import org.jetbrains.compose.resources.stringResource

/**
 * iOS 通知设定 (03 §4.10): C3 推入页 (返回通知中心), 大标题「通知設定」, 下面为 [AppleNotificationSettingsGroups].
 * 返回钮一行滚出后显示精简栏.
 *
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleNotificationSettingsScreen(
    state: SettingsState<NotificationSettings>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
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
                ),
        ) {
            PushedPageHeader(stringResource(Lang.notification_settings), onBack, windowInsets = windowInsets)
            Column(
                Modifier.padding(start = 16.dp, top = 24.dp, end = 16.dp),
                verticalArrangement = Arrangement.spacedBy(GroupedListDefaults.SectionSpacing),
            ) {
                AppleNotificationSettingsGroups(state)
            }
        }

        val density = LocalDensity.current
        val collapsed by remember(scrollState, density, windowInsets) {
            derivedStateOf {
                // 标题列的返回钮一行 (顶部内距 10 + 圆钮 36) 滚出后显示精简栏
                scrollState.value > windowInsets.getTop(density) + with(density) { 46.dp.roundToPx() }
            }
        }
        if (collapsed) {
            CollapsedTopBar(
                stringResource(Lang.notification_settings),
                onBack,
                Modifier.testTag(NotificationSettingsTestTags.COLLAPSED_BAR),
                windowInsets = windowInsets,
            )
        }
    }
}

/**
 * 「系統」与「公告」两组 (03 §4.10, 04 §4.7): C6 列不放图标方块, 行尾为开关, 组说明在卡片下方.
 * iOS 通知设定页与 macOS 设定窗口的「通知」窗格共用. 两组之间的间距由调用处排列 ([GroupedListDefaults.SectionSpacing]).
 *
 * 举报结果和账号安全各有一个开关, 关闭后本机不显示也不统计这类通知; 公告只控制未读提示点.
 * 公告组的说明用 Apple 平台的字串: 其他平台的说明提到的头像红点在 Apple 平台没有 (05 §8.3).
 */
@Composable
fun AppleNotificationSettingsGroups(state: SettingsState<NotificationSettings>) {
    val settings by state
    GroupedListSection(
        title = stringResource(Lang.notification_settings_group_system),
        footer = stringResource(Lang.notification_settings_system_group_description),
    ) {
        SettingsSwitchRow(
            stringResource(Lang.notification_settings_report_result_item),
            checked = settings.showReportResults,
            onCheckedChange = { state.update(settings.copy(showReportResults = it)) },
            Modifier.testTag(NotificationSettingsTestTags.REPORT_RESULT),
        )
        SettingsSwitchRow(
            stringResource(Lang.notification_settings_account_security_item),
            checked = settings.showAccountSecurity,
            onCheckedChange = { state.update(settings.copy(showAccountSecurity = it)) },
            Modifier.testTag(NotificationSettingsTestTags.ACCOUNT_SECURITY),
            showDivider = true,
        )
    }
    GroupedListSection(
        title = stringResource(Lang.notification_settings_group_announcements),
        footer = stringResource(Lang.notification_settings_announcement_description_apple),
    ) {
        SettingsSwitchRow(
            stringResource(Lang.notification_settings_announcement_item),
            checked = settings.showAnnouncementDot,
            onCheckedChange = { state.update(settings.copy(showAnnouncementDot = it)) },
            Modifier.testTag(NotificationSettingsTestTags.ANNOUNCEMENT),
        )
    }
}

/**
 * macOS 设定窗口的「通知」窗格 (04 §4.7): 顶部一行「通知中心」, 行尾为提示点与「N 則未讀」(与帐号窗格的通知列相同),
 * 点击在主窗口打开通知中心; 下面为 [AppleNotificationSettingsGroups]. 放在 `MacSettingsPane` 中, 组间距由窗格排列.
 *
 * @param unreadCount 「N 則未讀」, 0 时只显示提示点 (05 B2).
 * @param showNotificationDot 通知提示点, 已与登录状态合并.
 */
@Composable
fun MacNotificationSettingsPaneContent(
    state: SettingsState<NotificationSettings>,
    unreadCount: Int,
    showNotificationDot: Boolean,
    onOpenNotifications: () -> Unit,
) {
    GroupedListCard {
        SettingsRow(
            stringResource(Lang.notification_center_title),
            Modifier.testTag(NotificationSettingsTestTags.NOTIFICATION_CENTER),
            onClick = onOpenNotifications,
        ) {
            SettingsRowNavigation(
                value = unreadCount.takeIf { it > 0 && showNotificationDot }
                    ?.let { stringResource(Lang.notification_unread_count, it) },
                alert = showNotificationDot,
            )
        }
    }
    AppleNotificationSettingsGroups(state)
}
