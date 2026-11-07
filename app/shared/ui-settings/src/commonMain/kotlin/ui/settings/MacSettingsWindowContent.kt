/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.preference.NotificationSettings
import me.him188.ani.app.data.models.subject.SubjectCollectionCounts
import me.him188.ani.app.data.models.user.SelfInfo
import me.him188.ani.app.data.models.user.calculateDisplay
import me.him188.ani.app.domain.session.auth.OAuthPlatform
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.navigation.rememberAsyncBrowserNavigator
import me.him188.ani.app.ui.foundation.apple.AppleButton
import me.him188.ani.app.ui.foundation.apple.AppleButtonStyle
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AvatarRing
import me.him188.ani.app.ui.foundation.apple.AvatarRingSize
import me.him188.ani.app.ui.foundation.apple.GroupedListCard
import me.him188.ani.app.ui.foundation.apple.GroupedListSection
import me.him188.ani.app.ui.foundation.apple.MacSettingsAccountItem
import me.him188.ani.app.ui.foundation.apple.MacSettingsBackButton
import me.him188.ani.app.ui.foundation.apple.MacSettingsCategoryBar
import me.him188.ani.app.ui.foundation.apple.MacSettingsCategoryGroup
import me.him188.ani.app.ui.foundation.apple.MacSettingsCategoryItem
import me.him188.ani.app.ui.foundation.apple.MacSettingsPane
import me.him188.ani.app.ui.foundation.apple.SettingsActionRow
import me.him188.ani.app.ui.foundation.apple.SettingsRow
import me.him188.ani.app.ui.foundation.apple.SettingsRowNavigation
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.rememberAsyncHandler
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.acknowledgements
import me.him188.ani.app.ui.lang.developer_list
import me.him188.ani.app.ui.lang.notification_title
import me.him188.ani.app.ui.lang.notification_unread_count
import me.him188.ani.app.ui.lang.profile_pending_sync_count
import me.him188.ani.app.ui.lang.settings_about_build_info
import me.him188.ani.app.ui.lang.settings_account_bangumi_sync_title
import me.him188.ani.app.ui.lang.settings_account_github_title
import me.him188.ani.app.ui.lang.settings_account_popup_edit_profile
import me.him188.ani.app.ui.lang.settings_account_popup_login_register
import me.him188.ani.app.ui.lang.settings_account_popup_logout
import me.him188.ani.app.ui.lang.settings_account_popup_not_logged_in
import me.him188.ani.app.ui.lang.settings_account_profile_third_party_accounts
import me.him188.ani.app.ui.lang.settings_acknowledgements_oss_licenses
import me.him188.ani.app.ui.lang.settings_debug_dev_builds
import me.him188.ani.app.ui.lang.settings_debug_mode_enabled
import me.him188.ani.app.ui.lang.settings_mac_account_and_sync
import me.him188.ani.app.ui.lang.settings_mac_bangumi_connected
import me.him188.ani.app.ui.lang.settings_mac_bangumi_not_connected
import me.him188.ani.app.ui.lang.settings_mac_edit_profile
import me.him188.ani.app.ui.lang.settings_mac_notifications_description
import me.him188.ani.app.ui.lang.settings_mac_open_settings_hint
import me.him188.ani.app.ui.lang.settings_mac_sign_in_and_devices
import me.him188.ani.app.ui.lang.settings_mac_sync_and_notifications
import me.him188.ani.app.ui.settings.account.AccountLogoutDialog
import me.him188.ani.app.ui.settings.account.BangumiSyncTab
import me.him188.ani.app.ui.settings.account.GithubAccountTab
import me.him188.ani.app.ui.settings.account.ProfileGroup
import me.him188.ani.app.ui.settings.account.ProfileStats
import me.him188.ani.app.ui.settings.account.ProfileViewModel
import me.him188.ani.app.ui.settings.account.settingsGroups
import me.him188.ani.app.ui.settings.account.settingsTabIcon
import me.him188.ani.app.ui.settings.framework.SettingsState
import me.him188.ani.app.ui.settings.notification.NotificationSettingsGroups
import me.him188.ani.app.ui.settings.tabs.AniHelperDestination
import me.him188.ani.app.ui.settings.tabs.DebugTab
import me.him188.ani.app.ui.settings.tabs.about.AboutTab
import me.him188.ani.app.ui.settings.tabs.about.AcknowledgementsTab
import me.him188.ani.app.ui.settings.tabs.about.BuildInfo
import me.him188.ani.app.ui.settings.tabs.about.BuildInfoTab
import me.him188.ani.app.ui.settings.tabs.about.DevelopersTab
import me.him188.ani.app.ui.settings.tabs.about.OpenSourceLibrariesTab
import me.him188.ani.app.ui.settings.tabs.app.AppearanceGroup
import me.him188.ani.app.ui.settings.tabs.app.PlayerGroup
import me.him188.ani.app.ui.settings.tabs.app.SoftwareUpdateGroup
import me.him188.ani.app.ui.settings.tabs.app.WatchTogetherGroup
import me.him188.ani.app.ui.settings.tabs.log.LogTab
import me.him188.ani.app.ui.settings.tabs.media.BackupSettings
import me.him188.ani.app.ui.settings.tabs.media.CacheDirectoryGroup
import me.him188.ani.app.ui.settings.tabs.media.MediaSelectionGroup
import me.him188.ani.app.ui.settings.tabs.media.PikPakAcceleratorGroup
import me.him188.ani.app.ui.settings.tabs.media.TorrentEngineGroup
import me.him188.ani.app.ui.settings.tabs.media.source.MediaSourceSelectionActions
import me.him188.ani.app.ui.settings.tabs.media.source.MediaSourceSelectionState
import me.him188.ani.app.ui.settings.tabs.media.source.MediaSourceSubscriptionPage
import me.him188.ani.app.ui.settings.tabs.media.source.MediaSourceSubscriptionPageActions
import me.him188.ani.app.ui.settings.tabs.media.source.MediaSourceTab
import me.him188.ani.app.ui.settings.tabs.media.source.pageTitle
import me.him188.ani.app.ui.settings.tabs.media.source.rememberMediaSourceSelectionState
import me.him188.ani.app.ui.settings.tabs.network.ConfigureProxyGroup
import me.him188.ani.app.ui.settings.tabs.network.ServerSelectionGroup
import me.him188.ani.app.ui.settings.tabs.theme.ThemeGroup
import me.him188.ani.app.ui.update.devbuild.DevBuildsTab
import me.him188.ani.app.ui.user.SelfInfoUiState
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 设定窗口的分类 (02 C20). 「通知」没有对应的 [SettingsTab], 嵌入现有的通知设定 (05 B4).
 */
@Immutable
sealed interface MacSettingsCategory {
    data class Tab(val tab: SettingsTab) : MacSettingsCategory
    data object Notifications : MacSettingsCategory
}

/**
 * 窗格内推入的子页. 分类栏保持父分类选中, 标题栏左侧为「‹ 父页名」(02 C20).
 */
@Immutable
internal sealed interface MacSettingsSubPage {
    /** 帐号窗格的「编辑个人资料…」: 现有的帐号设定内容. */
    data object EditProfile : MacSettingsSubPage

    data class Detail(val route: DetailPaneRoutes) : MacSettingsSubPage
}

/**
 * 帐号窗格与分类栏帐号行显示的数据.
 *
 * @param pendingSyncCount 「N 项待同步」(05 G15).
 * @param notificationUnreadCount 「N 则未读」, 0 时只显示红点 (05 B2).
 * @param showNotificationDot 通知红点, 已与登录状态合并.
 * @param hasNewVersion 「软件更新」分类的提示点.
 */
@Immutable
class MacSettingsAccountInfo(
    val selfInfo: SelfInfoUiState?,
    val collectionCounts: SubjectCollectionCounts?,
    val pendingSyncCount: Int,
    val notificationUnreadCount: Int,
    val showNotificationDot: Boolean,
    val hasNewVersion: Boolean,
) {
    /** 已登录时的用户资料; 未登录或登录状态还在加载时为 `null`. */
    val loggedInSelfInfo: SelfInfo?
        get() = selfInfo?.selfInfo?.takeIf { selfInfo.isSessionValid == true }
}

/**
 * macOS 独立设定窗口的内容 (05 B4, 04 §3.6): 左侧 C20 分类栏, 右侧窗格.
 *
 * 分类栏: 帐号行, 以及应用与界面 (界面、主题与色彩、通知)、资料源与播放、网络与存储、其他四组, 分组同 [SettingsScreen] (05 G16);
 * 除错只在除错模式下出现. 「帐号」为 Apple 版帐号窗格; 其他分类沿用现有设定页的各 Tab 内容, 第三层再改版.
 * 子页 (Bangumi 同步、编辑个人资料、关于的子页等) 在窗格内推入, 换分类时清空.
 *
 * @param onNavigateToEmailLogin 登录. 现有登录流程在主窗口.
 * @param onOpenNotifications 打开主窗口的通知中心.
 */
@Composable
fun MacSettingsWindowContent(
    vm: SettingsViewModel,
    profileViewModel: ProfileViewModel,
    notificationSettings: SettingsState<NotificationSettings>,
    account: MacSettingsAccountInfo,
    selected: MacSettingsCategory,
    onSelect: (MacSettingsCategory) -> Unit,
    onNavigateToEmailLogin: () -> Unit,
    onNavigateToOAuth: (OAuthPlatform) -> Unit,
    onOpenNotifications: () -> Unit,
    loadOpenSourceLibrariesJsons: suspend () -> List<ByteArray>,
    modifier: Modifier = Modifier,
    onOpenPlayFlowDebugWindow: () -> Unit = {},
) {
    val subPages = remember(selected) { mutableStateListOf<MacSettingsSubPage>() }
    val mediaSourceSelectionState = rememberMediaSourceSelectionState()
    LaunchedEffect(selected) {
        if (selected != MacSettingsCategory.Tab(SettingsTab.MEDIA_SOURCE)) {
            mediaSourceSelectionState.clear()
        }
    }

    Row(modifier.fillMaxSize()) {
        MacSettingsCategories(account, showDebugSettings = vm.isInDebugMode, selected, onSelect)
        Box(Modifier.weight(1f).fillMaxHeight()) {
            val top = subPages.lastOrNull()
            if (top == null) {
                CategoryPane(
                    selected,
                    vm,
                    profileViewModel,
                    notificationSettings,
                    account,
                    mediaSourceSelectionState,
                    onPush = { subPages.add(it) },
                    onNavigateToEmailLogin = onNavigateToEmailLogin,
                    onOpenNotifications = onOpenNotifications,
                    onOpenPlayFlowDebugWindow = onOpenPlayFlowDebugWindow,
                )
            } else {
                val parentTitle = subPages.getOrNull(subPages.lastIndex - 1)?.let { subPageTitle(it, vm) }
                    ?: categoryTitle(selected)
                SubPagePane(
                    top,
                    parentTitle,
                    vm,
                    profileViewModel,
                    onBack = { subPages.removeAt(subPages.lastIndex) },
                    onPush = { subPages.add(it) },
                    onNavigateToEmailLogin = onNavigateToEmailLogin,
                    onNavigateToOAuth = onNavigateToOAuth,
                    loadOpenSourceLibrariesJsons = loadOpenSourceLibrariesJsons,
                )
            }
        }
    }
}

/**
 * C20 分类栏: 帐号行与四组分类. 「通知」接在「主题与色彩」之后; 「软件更新」在有新版本时显示提示点.
 */
@Composable
internal fun MacSettingsCategories(
    account: MacSettingsAccountInfo,
    showDebugSettings: Boolean,
    selected: MacSettingsCategory,
    onSelect: (MacSettingsCategory) -> Unit,
    modifier: Modifier = Modifier,
) {
    MacSettingsCategoryBar(
        account = {
            val selfInfo = account.loggedInSelfInfo
            val name = selfInfo?.calculateDisplay()?.title
                ?: stringResource(Lang.settings_account_popup_not_logged_in)
            val profile = MacSettingsCategory.Tab(SettingsTab.PROFILE)
            MacSettingsAccountItem(
                name = name,
                subtitle = stringResource(Lang.settings_mac_account_and_sync),
                initial = name.take(1).uppercase(),
                selected = selected == profile,
                onClick = { onSelect(profile) },
                Modifier.testTag(MacSettingsTestTags.category(profile)),
                image = avatarImage(selfInfo?.avatarUrl, isLogin = selfInfo != null, iconSize = 16),
            )
        },
        modifier,
    ) {
        for (group in settingsGroups(showDebugSettings)) {
            MacSettingsCategoryGroup(stringResource(group.title)) {
                for (tab in group.tabs) {
                    CategoryItem(
                        MacSettingsCategory.Tab(tab),
                        selected,
                        onSelect,
                        alert = tab == SettingsTab.UPDATE && account.hasNewVersion,
                    )
                    if (tab == SettingsTab.THEME) {
                        CategoryItem(MacSettingsCategory.Notifications, selected, onSelect)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    category: MacSettingsCategory,
    selected: MacSettingsCategory,
    onSelect: (MacSettingsCategory) -> Unit,
    alert: Boolean = false,
) {
    MacSettingsCategoryItem(
        selected = category == selected,
        onClick = { onSelect(category) },
        icon = when (category) {
            is MacSettingsCategory.Tab -> settingsTabIcon(category.tab)
            MacSettingsCategory.Notifications -> AppleIcons.bell(2f)
        },
        label = categoryTitle(category),
        Modifier.testTag(MacSettingsTestTags.category(category)),
        alert = alert,
    )
}

@Composable
private fun categoryTitle(category: MacSettingsCategory): String = when (category) {
    is MacSettingsCategory.Tab -> getName(category.tab)
    MacSettingsCategory.Notifications -> stringResource(Lang.notification_title)
}

@Composable
private fun subPageTitle(page: MacSettingsSubPage, vm: SettingsViewModel): String = when (page) {
    MacSettingsSubPage.EditProfile -> stringResource(Lang.settings_account_popup_edit_profile)
    is MacSettingsSubPage.Detail -> when (val route = page.route) {
        DetailPaneRoutes.Acknowledgements -> stringResource(Lang.acknowledgements)
        DetailPaneRoutes.OpenSourceLicenses -> stringResource(Lang.settings_acknowledgements_oss_licenses)
        DetailPaneRoutes.Developers -> stringResource(Lang.developer_list)
        DetailPaneRoutes.BuildInfo -> stringResource(Lang.settings_about_build_info)
        DetailPaneRoutes.BangumiSync -> stringResource(Lang.settings_account_bangumi_sync_title)
        DetailPaneRoutes.GithubAccount -> stringResource(Lang.settings_account_github_title)
        DetailPaneRoutes.DevBuilds -> stringResource(Lang.settings_debug_dev_builds)
        is DetailPaneRoutes.MediaSourceSubscription ->
            vm.mediaSourceSubscriptionGroupState.findSubscription(route.subscriptionId)?.pageTitle.orEmpty()

        DetailPaneRoutes.Main -> ""
    }
}

/**
 * 分类的窗格. 帐号为 Apple 版帐号窗格, 其他沿用现有设定页的内容.
 */
@Composable
private fun CategoryPane(
    category: MacSettingsCategory,
    vm: SettingsViewModel,
    profileViewModel: ProfileViewModel,
    notificationSettings: SettingsState<NotificationSettings>,
    account: MacSettingsAccountInfo,
    mediaSourceSelectionState: MediaSourceSelectionState,
    onPush: (MacSettingsSubPage) -> Unit,
    onNavigateToEmailLogin: () -> Unit,
    onOpenNotifications: () -> Unit,
    onOpenPlayFlowDebugWindow: () -> Unit,
) {
    val title = categoryTitle(category)
    val context = LocalContext.current
    val browserNavigator = rememberAsyncBrowserNavigator()
    val toaster = LocalToaster.current
    val scope = rememberCoroutineScope()
    when (category) {
        MacSettingsCategory.Notifications -> MacSettingsPane(title) {
            SettingsTab { NotificationSettingsGroups(notificationSettings) }
        }

        is MacSettingsCategory.Tab -> when (val tab = category.tab) {
            SettingsTab.PROFILE -> MacSettingsPane(title) {
                MacAccountPaneContent(
                    account,
                    onEditProfile = { onPush(MacSettingsSubPage.EditProfile) },
                    onBangumiSync = { onPush(MacSettingsSubPage.Detail(DetailPaneRoutes.BangumiSync)) },
                    onOpenNotifications = onOpenNotifications,
                    onLogin = onNavigateToEmailLogin,
                    onLogout = { profileViewModel.logout() },
                )
            }

            SettingsTab.ABOUT -> MacSettingsPane(title) {
                AboutTab(
                    vm.aboutTabInfo,
                    onTriggerDebugMode = {
                        scope.launch {
                            if (vm.debugTriggerState.triggerDebugMode()) {
                                toaster.toast(getString(Lang.settings_debug_mode_enabled))
                            }
                        }
                    },
                    onClickBuildInfo = { onPush(MacSettingsSubPage.Detail(DetailPaneRoutes.BuildInfo)) },
                    onClickReleaseNotes = {
                        browserNavigator.openBrowser(context, AniHelperDestination.RELEASE_PREFIX + vm.aboutTabInfo.version)
                    },
                    onClickWebsite = { browserNavigator.openBrowser(context, AniHelperDestination.ANI_WEBSITE) },
                    onClickFeedback = { browserNavigator.openBrowser(context, AniHelperDestination.ISSUE_TRACKER) },
                    onClickSource = { browserNavigator.openBrowser(context, AniHelperDestination.GITHUB_HOME) },
                    onClickDevelopers = { onPush(MacSettingsSubPage.Detail(DetailPaneRoutes.Developers)) },
                    onClickAcknowledgements = { onPush(MacSettingsSubPage.Detail(DetailPaneRoutes.Acknowledgements)) },
                )
            }

            SettingsTab.LOG -> MacSettingsPane(title) {
                LogTab(onClickFeedback = { browserNavigator.openBrowser(context, AniHelperDestination.ISSUE_TRACKER) })
            }

            SettingsTab.DEBUG -> MacSettingsPane(title) {
                DebugTab(
                    vm.debugSettingsState,
                    onNavigateToDevBuilds = { onPush(MacSettingsSubPage.Detail(DetailPaneRoutes.DevBuilds)) },
                    onOpenPlayFlowDebugWindow = onOpenPlayFlowDebugWindow,
                )
            }

            SettingsTab.MEDIA_SOURCE -> Box(Modifier.fillMaxSize()) {
                MacSettingsPane(title) {
                    SettingsTab {
                        MediaSourceTab(
                            vm.mediaSourceSubscriptionGroupState,
                            vm.mediaSourceGroupState,
                            vm.editMediaSourceState,
                            mediaSourceSelectionState,
                            backgroundColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                            onOpenSubscription = {
                                onPush(MacSettingsSubPage.Detail(DetailPaneRoutes.MediaSourceSubscription(it)))
                            },
                        )
                    }
                    if (mediaSourceSelectionState.inSelection) {
                        Spacer(Modifier.height(80.dp))
                    }
                }
                if (mediaSourceSelectionState.inSelection) {
                    MediaSourceSelectionActions(
                        mediaSources = vm.mediaSourceGroupState.mediaSources,
                        selectionState = mediaSourceSelectionState,
                        editState = vm.editMediaSourceState,
                        windowInsets = WindowInsets(0),
                        modifier = Modifier.align(Alignment.BottomCenter),
                    )
                }
            }

            else -> MacSettingsPane(title) {
                SettingsTab {
                    when (tab) {
                        SettingsTab.APPEARANCE -> AppearanceGroup(vm.uiSettings)
                        SettingsTab.THEME -> ThemeGroup(vm.themeSettings)
                        SettingsTab.UPDATE -> SoftwareUpdateGroup(vm.softwareUpdateGroupState)
                        SettingsTab.PLAYER -> {
                            PlayerGroup(
                                vm.videoScaffoldConfig,
                                vm.playerKernelConfig,
                                vm.danmakuFilterConfigState,
                                vm.danmakuRegexFilterState,
                                vm.isInDebugMode,
                            )
                            WatchTogetherGroup(vm.watchTogetherSettings)
                        }

                        SettingsTab.MEDIA_SELECTOR -> MediaSelectionGroup(vm.mediaSelectionGroupState)
                        SettingsTab.SERVER -> ServerSelectionGroup(vm.danmakuSettingsState, vm.danmakuServerTesters)
                        SettingsTab.PROXY -> ConfigureProxyGroup(
                            state = vm.configureProxyState,
                            onStartProxyTestLoop = { vm.startProxyTesterLoop() },
                        )

                        SettingsTab.BT -> {
                            TorrentEngineGroup(vm.torrentSettingsState)
                            PikPakAcceleratorGroup(
                                vm.pikpakSettingsState,
                                vm.mediaSelectorSettingsState,
                                vm.pikpakDriveUsageState,
                                vm.pikpakLegacyNoticeState,
                            )
                        }

                        SettingsTab.STORAGE -> CacheDirectoryGroup(vm.cacheDirectoryGroupState)
                        SettingsTab.SETTINGS_BACKUP -> BackupSettings(vm.cacheDirectoryGroupState)
                        SettingsTab.PROFILE, SettingsTab.ABOUT, SettingsTab.LOG,
                        SettingsTab.DEBUG, SettingsTab.MEDIA_SOURCE -> {}
                    }
                }
            }
        }
    }
}

/**
 * 子页窗格: 标题栏左侧为「‹ 父页名」.
 */
@Composable
private fun SubPagePane(
    page: MacSettingsSubPage,
    parentTitle: String,
    vm: SettingsViewModel,
    profileViewModel: ProfileViewModel,
    onBack: () -> Unit,
    onPush: (MacSettingsSubPage) -> Unit,
    onNavigateToEmailLogin: () -> Unit,
    onNavigateToOAuth: (OAuthPlatform) -> Unit,
    loadOpenSourceLibrariesJsons: suspend () -> List<ByteArray>,
) {
    val title = subPageTitle(page, vm)
    val back: @Composable () -> Unit = {
        MacSettingsBackButton(parentTitle, onClick = onBack, Modifier.testTag(MacSettingsTestTags.Back))
    }
    when (page) {
        MacSettingsSubPage.EditProfile -> MacSettingsPane(title, back = back) {
            SettingsTab {
                ProfileGroup(
                    onNavigateToEmail = onNavigateToEmailLogin,
                    onNavigateToBangumiSync = { onPush(MacSettingsSubPage.Detail(DetailPaneRoutes.BangumiSync)) },
                    onNavigateToOAuth = onNavigateToOAuth,
                    onNavigateToGithubAccount = { onPush(MacSettingsSubPage.Detail(DetailPaneRoutes.GithubAccount)) },
                    vm = profileViewModel,
                )
            }
        }

        is MacSettingsSubPage.Detail -> when (val route = page.route) {
            DetailPaneRoutes.BangumiSync -> MacSettingsPane(title, back = back, scrollable = false) {
                BangumiSyncTab()
            }

            DetailPaneRoutes.GithubAccount -> MacSettingsPane(title, back = back) { GithubAccountTab() }
            DetailPaneRoutes.BuildInfo -> MacSettingsPane(title, back = back) {
                BuildInfoTab(remember { BuildInfo.current() }, Modifier.fillMaxWidth())
            }

            DetailPaneRoutes.Developers -> MacSettingsPane(title, back = back) { DevelopersTab(Modifier.fillMaxWidth()) }
            DetailPaneRoutes.Acknowledgements -> MacSettingsPane(title, back = back) {
                AcknowledgementsTab(
                    onClickOpenSourceLicenses = { onPush(MacSettingsSubPage.Detail(DetailPaneRoutes.OpenSourceLicenses)) },
                    Modifier.fillMaxWidth(),
                )
            }

            // LibrariesContainer 自带 LazyColumn, 窗格本身不纵向滚动
            DetailPaneRoutes.OpenSourceLicenses -> MacSettingsPane(title, back = back, scrollable = false) {
                OpenSourceLibrariesTab(loadOpenSourceLibrariesJsons, Modifier.fillMaxSize())
            }

            DetailPaneRoutes.DevBuilds -> MacSettingsPane(title, back = back) { DevBuildsTab(Modifier.fillMaxWidth()) }
            is DetailPaneRoutes.MediaSourceSubscription -> MacSettingsPane(
                title,
                back = back,
                actions = {
                    MediaSourceSubscriptionPageActions(
                        route.subscriptionId,
                        vm.mediaSourceSubscriptionGroupState,
                        vm.mediaSourceGroupState,
                        vm.editMediaSourceState,
                    )
                },
            ) {
                MacMediaSourceSubscriptionContent(route.subscriptionId, vm, onDeleted = onBack)
            }

            DetailPaneRoutes.Main -> {}
        }
    }
}

/**
 * 订阅详情. 订阅被删除后返回上一页, 同设定页的 `MediaSourceSubscriptionPageContent`.
 */
@Composable
private fun MacMediaSourceSubscriptionContent(
    subscriptionId: String,
    vm: SettingsViewModel,
    onDeleted: () -> Unit,
) {
    val subscriptionState = vm.mediaSourceSubscriptionGroupState
    val groupState = vm.mediaSourceGroupState
    val subscription = subscriptionState.findSubscription(subscriptionId)
    // 订阅列表可能还没加载, 只有在订阅出现过之后消失才说明被删除了
    var seen by remember { mutableStateOf(false) }
    if (subscription == null) {
        if (seen) {
            LaunchedEffect(Unit) { onDeleted() }
        }
        return
    }
    SideEffect { seen = true }
    val mediaSources = groupState.mediaSourcesOfSubscription(subscriptionId)
    val testers = remember(mediaSources) { groupState.createTesterRunner(mediaSources) }
    SettingsTab {
        MediaSourceSubscriptionPage(subscription, mediaSources, testers, subscriptionState, vm.editMediaSourceState)
    }
}

/**
 * 帐号窗格 (04 §3.6).
 *
 * 个人资料卡 (64 头像光环、昵称、「@帐号 · 信箱」、「编辑个人资料…」; 分隔线后在看 / 想看 / 看过统计);
 * 同步与通知 (Bangumi 同步与「N 项待同步」、通知与红点「N 则未读」); 登录与装置 (第三方帐号、登出);
 * 底注「按 ⌘, 随时开启设定」. 未登录时为登录卡.
 */
@Composable
internal fun ColumnScope.MacAccountPaneContent(
    account: MacSettingsAccountInfo,
    onEditProfile: () -> Unit,
    onBangumiSync: () -> Unit,
    onOpenNotifications: () -> Unit,
    onLogin: () -> Unit,
    onLogout: suspend () -> Unit,
) {
    val selfInfo = account.loggedInSelfInfo
    val colorScheme = MaterialTheme.colorScheme
    var showLogoutDialog by rememberSaveable { mutableStateOf(false) }

    if (selfInfo == null) {
        GroupedListCard(Modifier.testTag(MacSettingsTestTags.LoginCard)) {
            Row(
                Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onLogin).padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarRing(
                    initial = "",
                    size = AvatarRingSize.MacSettingsAccount,
                    image = avatarImage(null, isLogin = false, iconSize = 30),
                )
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        stringResource(Lang.settings_account_popup_not_logged_in),
                        style = AniTextStyles.macTitle.copy(fontSize = 20.sp, lineHeight = 26.sp),
                        color = colorScheme.onSurface,
                    )
                    Text(
                        stringResource(Lang.settings_account_popup_login_register),
                        style = AniTextStyles.macCallout,
                        color = colorScheme.primary,
                    )
                }
            }
        }
        AccountFootnote()
        return
    }

    // 个人资料卡
    GroupedListCard {
        val display = selfInfo.calculateDisplay()
        Row(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AvatarRing(
                initial = display.title.take(1).uppercase(),
                size = AvatarRingSize.MacSettingsAccount,
                image = avatarImage(selfInfo.avatarUrl, isLogin = true, iconSize = 30),
            )
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    display.title,
                    style = AniTextStyles.macTitle.copy(fontSize = 20.sp, lineHeight = 26.sp),
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = listOfNotNull(
                    selfInfo.bangumiUsername?.let { "@$it" },
                    selfInfo.email,
                ).joinToString(" · ")
                if (subtitle.isNotEmpty()) {
                    Text(
                        subtitle,
                        style = AniTextStyles.macCallout,
                        color = colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            AppleButton(onEditProfile, Modifier.testTag(MacSettingsTestTags.EditProfile), style = AppleButtonStyle.Secondary) {
                Text(stringResource(Lang.settings_mac_edit_profile))
            }
        }
        AppleDivider()
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
            for ((type, label) in ProfileStats) {
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        account.collectionCounts?.getCount(type)?.toString() ?: "–",
                        style = AniTextStyles.macTitle.copy(fontSize = 20.sp, lineHeight = 26.sp, fontFeatureSettings = "tnum"),
                        color = colorScheme.onSurface,
                    )
                    Text(stringResource(label), style = AniTextStyles.macCaption, color = colorScheme.secondary)
                }
            }
        }
    }

    GroupedListSection(title = stringResource(Lang.settings_mac_sync_and_notifications)) {
        val bangumiUsername = selfInfo.bangumiUsername?.takeIf { it.isNotBlank() }
        SettingsRow(
            stringResource(Lang.settings_account_bangumi_sync_title),
            Modifier.testTag(MacSettingsTestTags.BangumiSync),
            description = if (bangumiUsername != null) {
                stringResource(Lang.settings_mac_bangumi_connected, bangumiUsername)
            } else {
                stringResource(Lang.settings_mac_bangumi_not_connected)
            },
            onClick = onBangumiSync,
        ) {
            SettingsRowNavigation(
                value = account.pendingSyncCount.takeIf { it > 0 }
                    ?.let { stringResource(Lang.profile_pending_sync_count, it) },
            )
        }
        SettingsRow(
            stringResource(Lang.notification_title),
            Modifier.testTag(MacSettingsTestTags.Notifications),
            description = stringResource(Lang.settings_mac_notifications_description),
            showDivider = true,
            onClick = onOpenNotifications,
        ) {
            SettingsRowNavigation(
                value = account.notificationUnreadCount.takeIf { it > 0 && account.showNotificationDot }
                    ?.let { stringResource(Lang.notification_unread_count, it) },
                alert = account.showNotificationDot,
            )
        }
    }

    GroupedListSection(title = stringResource(Lang.settings_mac_sign_in_and_devices)) {
        // 同帐号设定: Bangumi 以是否有 Bangumi 用户名判断, 其他平台来自 externalAccounts, 不认识的平台显示其 ID
        val bound = buildList {
            if (!selfInfo.bangumiUsername.isNullOrBlank()) add(OAuthPlatform.BANGUMI.displayName)
            selfInfo.externalAccounts.mapTo(this) { OAuthPlatform.fromId(it.provider)?.displayName ?: it.provider }
        }
        SettingsRow(stringResource(Lang.settings_account_profile_third_party_accounts), onClick = onEditProfile) {
            SettingsRowNavigation(value = bound.joinToString("、").ifEmpty { null })
        }
        SettingsActionRow(
            stringResource(Lang.settings_account_popup_logout),
            onClick = { showLogoutDialog = true },
            Modifier.testTag(MacSettingsTestTags.Logout),
            showDivider = true,
        )
    }

    AccountFootnote()

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

@Composable
private fun AccountFootnote() {
    Text(
        stringResource(Lang.settings_mac_open_settings_hint),
        Modifier.padding(horizontal = 12.dp),
        style = AniTextStyles.macCaption,
        color = MaterialTheme.colorScheme.secondary,
    )
}

/**
 * 头像: 有图片时为图片; 未登录时为白色人像图标; 否则为 `null` (显示首字母).
 */
private fun avatarImage(avatarUrl: String?, isLogin: Boolean, iconSize: Int): (@Composable () -> Unit)? = when {
    avatarUrl != null -> {
        { AvatarImage(avatarUrl, Modifier.fillMaxSize().clip(CircleShape)) }
    }

    !isLogin -> {
        { Icon(AppleIcons.profile(2f), contentDescription = null, Modifier.size(iconSize.dp), tint = Color.White) }
    }

    else -> null
}

internal object MacSettingsTestTags {
    const val Back = "mac-settings-back"
    const val LoginCard = "mac-settings-login-card"
    const val EditProfile = "mac-settings-edit-profile"
    const val BangumiSync = "mac-settings-bangumi-sync"
    const val Notifications = "mac-settings-notifications"
    const val Logout = "mac-settings-logout"
    fun category(category: MacSettingsCategory) = when (category) {
        is MacSettingsCategory.Tab -> "mac-settings-category-${category.tab.name}"
        MacSettingsCategory.Notifications -> "mac-settings-category-notifications"
    }
}
