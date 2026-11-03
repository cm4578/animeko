/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.account

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.preference.DarkMode
import me.him188.ani.app.data.models.subject.SubjectCollectionCounts
import me.him188.ani.app.data.models.user.calculateDisplay
import me.him188.ani.app.navigation.SettingsTab
import me.him188.ani.app.ui.foundation.apple.AlertDot
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AvatarRing
import me.him188.ani.app.ui.foundation.apple.AvatarRingSize
import me.him188.ani.app.ui.foundation.apple.GroupedListCard
import me.him188.ani.app.ui.foundation.apple.GroupedListDefaults
import me.him188.ani.app.ui.foundation.apple.GroupedListSection
import me.him188.ani.app.ui.foundation.apple.LargeTitleHeader
import me.him188.ani.app.ui.foundation.apple.SettingsRow
import me.him188.ani.app.ui.foundation.apple.SettingsRowDefaults
import me.him188.ani.app.ui.foundation.apple.SettingsRowNavigation
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.rememberAsyncHandler
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.main_screen_page_profile
import me.him188.ani.app.ui.lang.notification_title
import me.him188.ani.app.ui.lang.notification_unread_count
import me.him188.ani.app.ui.lang.playback_history_title
import me.him188.ani.app.ui.lang.profile_pending_sync_count
import me.him188.ani.app.ui.lang.settings_account_bangumi_sync_title
import me.him188.ani.app.ui.lang.settings_account_popup_edit_profile
import me.him188.ani.app.ui.lang.settings_account_popup_login_register
import me.him188.ani.app.ui.lang.settings_account_popup_logout
import me.him188.ani.app.ui.lang.settings_account_popup_not_logged_in
import me.him188.ani.app.ui.lang.settings_category_app_ui
import me.him188.ani.app.ui.lang.settings_category_data_playback
import me.him188.ani.app.ui.lang.settings_category_network_storage
import me.him188.ani.app.ui.lang.settings_category_others
import me.him188.ani.app.ui.lang.settings_update_popup_new_version
import me.him188.ani.app.ui.lang.subject_collection_doing
import me.him188.ani.app.ui.lang.subject_collection_done
import me.him188.ani.app.ui.lang.subject_collection_wish
import me.him188.ani.app.ui.settings.getName
import me.him188.ani.app.ui.settings.tabs.theme.renderThemeModeText
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * 「我的」页 (iOS 第 4 分页). 承接顶栏头像弹窗 [ProfilePopup] 的全部功能: 个人资料、登录、通知、播放记录、设置、登出.
 *
 * 从上到下: C2 大标题「我的」; 个人卡 (头像、昵称、在看/想看/看过); 通知、播放记录、Bangumi 同步;
 * 设定分组 (与设置页的分组相同, 「软件更新」在「其他」); 登出.
 *
 * @param collectionCounts 在看、想看、看过的数量. 为 `null` 时显示 `-`.
 * @param showNotificationDot 通知列的红点, 与其他平台头像上的红点一致 (未登录时为 `false`).
 * @param notificationUnreadCount 「N 則未讀」的数字. 为 0 时只显示红点.
 * @param pendingSyncCount 「N 項待同步」的数字. 为 0 时不显示.
 * @param themeMode 显示在「主题与色彩」列的值.
 * @param hasNewVersion 有新版本时,「软件更新」列显示 C12 提示点与「新版本」.
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleProfilePage(
    state: AccountSettingsState,
    collectionCounts: SubjectCollectionCounts?,
    showNotificationDot: Boolean,
    notificationUnreadCount: Int,
    pendingSyncCount: Int,
    themeMode: DarkMode,
    hasNewVersion: Boolean,
    showDebugSettings: Boolean,
    onClickProfile: () -> Unit,
    onClickLogin: () -> Unit,
    onClickCollectionType: (UnifiedCollectionType) -> Unit,
    onClickNotifications: () -> Unit,
    onClickPlaybackHistory: () -> Unit,
    onClickBangumiSync: () -> Unit,
    onClickSettings: (SettingsTab) -> Unit,
    onLogout: suspend () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }
    val isLogin = state.selfInfo.isSessionValid == true

    Column(
        modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding),
    ) {
        LargeTitleHeader(stringResource(Lang.main_screen_page_profile), windowInsets = windowInsets)
        Column(Modifier.padding(horizontal = 16.dp)) {
            ProfileCard(
                state,
                isLogin = isLogin,
                collectionCounts = collectionCounts,
                onClick = if (isLogin) onClickProfile else onClickLogin,
                onClickCollectionType = onClickCollectionType,
            )
            Spacer(Modifier.height(12.dp))
            GroupedListCard {
                SettingsRow(
                    stringResource(Lang.notification_title),
                    icon = AppleIcons.bell(2f),
                    minHeight = SettingsRowDefaults.Height,
                    onClick = onClickNotifications,
                ) {
                    val unread = if (showNotificationDot && notificationUnreadCount > 0) {
                        stringResource(Lang.notification_unread_count, notificationUnreadCount)
                    } else {
                        null
                    }
                    SettingsRowNavigation(value = unread, alert = showNotificationDot)
                }
                SettingsRow(
                    stringResource(Lang.playback_history_title),
                    icon = AppleIcons.history(2f),
                    showDivider = true,
                    minHeight = SettingsRowDefaults.Height,
                    onClick = onClickPlaybackHistory,
                ) {
                    SettingsRowNavigation()
                }
                SettingsRow(
                    stringResource(Lang.settings_account_bangumi_sync_title),
                    icon = AppleIcons.sync(),
                    showDivider = true,
                    minHeight = SettingsRowDefaults.Height,
                    onClick = onClickBangumiSync,
                ) {
                    SettingsRowNavigation(
                        value = if (pendingSyncCount > 0) {
                            stringResource(Lang.profile_pending_sync_count, pendingSyncCount)
                        } else {
                            null
                        },
                    )
                }
            }

            for (group in settingsGroups(showDebugSettings)) {
                Spacer(Modifier.height(GroupedListDefaults.SectionSpacing))
                GroupedListSection(title = stringResource(group.title)) {
                    group.tabs.forEachIndexed { index, tab ->
                        SettingsRow(
                            getName(tab),
                            icon = settingsTabIcon(tab),
                            showDivider = index > 0,
                            onClick = { onClickSettings(tab) },
                        ) {
                            when (tab) {
                                SettingsTab.THEME -> SettingsRowNavigation(value = renderThemeModeText(themeMode))
                                SettingsTab.UPDATE -> SettingsRowNavigation(
                                    value = if (hasNewVersion) stringResource(Lang.settings_update_popup_new_version) else null,
                                    alert = hasNewVersion,
                                )

                                else -> SettingsRowNavigation()
                            }
                        }
                    }
                }
            }

            if (isLogin) {
                Spacer(Modifier.height(GroupedListDefaults.SectionSpacing))
                GroupedListCard {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(min = SettingsRowDefaults.Height)
                            .clickable(role = Role.Button) { showLogoutDialog = true },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            stringResource(Lang.settings_account_popup_logout),
                            style = AniTextStyles.body,
                            color = MaterialTheme.colorScheme.error,
                        )
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        val asyncHandler = rememberAsyncHandler()
        AccountLogoutDialog(
            {
                asyncHandler.launch {
                    onLogout()
                    showLogoutDialog = false
                }
            },
            onCancel = { showLogoutDialog = false },
            confirmEnabled = !asyncHandler.isWorking,
        )
    }
}

/**
 * 个人卡: 上方头像 60 (C11 光环)、昵称与「@帐号 · 编辑个人资料」; 分隔线; 下方在看/想看/看过三栏.
 * 未登录时改为登录卡, 不显示统计.
 */
@Composable
private fun ProfileCard(
    state: AccountSettingsState,
    isLogin: Boolean,
    collectionCounts: SubjectCollectionCounts?,
    onClick: () -> Unit,
    onClickCollectionType: (UnifiedCollectionType) -> Unit,
) {
    val selfInfo = state.selfInfo.selfInfo
    GroupedListCard {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(start = 16.dp, top = 16.dp, end = 12.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val title = if (isLogin) selfInfo.calculateDisplay().title else stringResource(Lang.settings_account_popup_not_logged_in)
            val avatarUrl = selfInfo?.avatarUrl?.takeIf { isLogin }
            AvatarRing(
                initial = title.take(1).uppercase(),
                size = AvatarRingSize.ProfileCard,
                image = when {
                    avatarUrl != null -> {
                        { AvatarImage(avatarUrl, Modifier.fillMaxSize().clip(CircleShape)) }
                    }

                    !isLogin -> {
                        {
                            Icon(
                                AppleIcons.profile(2f),
                                contentDescription = null,
                                Modifier.size(28.dp),
                                tint = Color.White,
                            )
                        }
                    }

                    else -> null
                },
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = AniTextStyles.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = if (isLogin) {
                    val editProfile = stringResource(Lang.settings_account_popup_edit_profile)
                    val handle = selfInfo?.bangumiUsername?.takeIf { it.isNotBlank() } ?: selfInfo?.email
                    if (handle != null) "@$handle · $editProfile" else editProfile
                } else {
                    stringResource(Lang.settings_account_popup_login_register)
                }
                Text(
                    subtitle,
                    style = AniTextStyles.footnote,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Icon(
                AppleIcons.chevronRight(),
                contentDescription = null,
                Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
        if (isLogin) {
            AppleDivider(Modifier.padding(end = 16.dp), startIndent = 16.dp)
            Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 14.dp)) {
                for ((type, label) in ProfileStats) {
                    val labelText = stringResource(label)
                    val count = collectionCounts?.getCount(type)?.toString() ?: "-"
                    Column(
                        Modifier
                            .weight(1f)
                            .clickable(role = Role.Button) { onClickCollectionType(type) }
                            .semantics(mergeDescendants = true) { contentDescription = "$labelText $count" },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            count,
                            style = AniTextStyles.title.copy(fontFeatureSettings = "tnum"),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            labelText,
                            style = AniTextStyles.caption,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }
}

private val ProfileStats = listOf(
    UnifiedCollectionType.DOING to Lang.subject_collection_doing,
    UnifiedCollectionType.WISH to Lang.subject_collection_wish,
    UnifiedCollectionType.DONE to Lang.subject_collection_done,
)

private class SettingsGroup(val title: StringResource, val tabs: List<SettingsTab>)

/**
 * 与设置页的分组相同 (05 G16): 「软件更新」在「其他」.
 */
private fun settingsGroups(showDebugSettings: Boolean): List<SettingsGroup> = listOf(
    SettingsGroup(Lang.settings_category_app_ui, listOf(SettingsTab.APPEARANCE, SettingsTab.THEME)),
    SettingsGroup(
        Lang.settings_category_data_playback,
        listOf(SettingsTab.PLAYER, SettingsTab.MEDIA_SOURCE, SettingsTab.MEDIA_SELECTOR),
    ),
    SettingsGroup(
        Lang.settings_category_network_storage,
        listOf(SettingsTab.SERVER, SettingsTab.PROXY, SettingsTab.BT, SettingsTab.STORAGE),
    ),
    SettingsGroup(
        Lang.settings_category_others,
        buildList {
            add(SettingsTab.UPDATE)
            add(SettingsTab.LOG)
            add(SettingsTab.ABOUT)
            if (showDebugSettings) add(SettingsTab.DEBUG)
            add(SettingsTab.SETTINGS_BACKUP)
        },
    ),
)

private fun settingsTabIcon(tab: SettingsTab): ImageVector = when (tab) {
    SettingsTab.PROFILE -> AppleIcons.profile(2f)
    SettingsTab.APPEARANCE -> AppleIcons.settingsAppearance()
    SettingsTab.THEME -> AppleIcons.settingsTheme()
    SettingsTab.UPDATE -> AppleIcons.settingsUpdate()
    SettingsTab.PLAYER -> AppleIcons.settingsPlayer()
    SettingsTab.MEDIA_SOURCE -> AppleIcons.settingsMediaSource()
    SettingsTab.MEDIA_SELECTOR -> AppleIcons.settingsMediaSelector()
    SettingsTab.SERVER -> AppleIcons.settingsServer()
    SettingsTab.PROXY -> AppleIcons.settingsProxy()
    SettingsTab.BT -> AppleIcons.settingsBitTorrent()
    SettingsTab.STORAGE -> AppleIcons.settingsStorage()
    SettingsTab.SETTINGS_BACKUP -> AppleIcons.settingsBackup()
    SettingsTab.ABOUT -> AppleIcons.settingsAbout()
    SettingsTab.LOG -> AppleIcons.settingsLog()
    SettingsTab.DEBUG -> AppleIcons.settingsDebug()
}
