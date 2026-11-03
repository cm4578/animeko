/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.preference.DebugSettings
import me.him188.ani.app.data.repository.user.SettingsRepository
import me.him188.ani.app.domain.foundation.VersionExpiryService
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.navigation.MainScreenPage
import me.him188.ani.app.navigation.SettingsTab
import me.him188.ani.app.ui.bangumi.merge.BangumiConflictNotifier
import me.him188.ani.app.ui.download.DownloadManagementScreen
import me.him188.ani.app.ui.download.createDownloadManagementViewModel
import me.him188.ani.app.ui.exploration.ExplorationPageViewModel
import me.him188.ani.app.ui.exploration.ExplorationScreen
import me.him188.ani.app.ui.foundation.animation.LocalAniMotionScheme
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.session.LocalSelfAvatarUnreadDot
import me.him188.ani.app.ui.foundation.theme.LocalAppChromeOverlayInsets
import me.him188.ani.app.ui.foundation.theme.LocalThemeSettings
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.showLoadError
import me.him188.ani.app.ui.settings.account.AppleProfilePage
import me.him188.ani.app.ui.settings.account.ProfileViewModel
import me.him188.ani.app.ui.subject.collection.COLLECTION_TABS_SORTED
import me.him188.ani.app.ui.subject.collection.CollectionPage
import me.him188.ani.app.ui.subject.collection.UserCollectionsViewModel
import me.him188.ani.app.ui.update.AppUpdateViewModel
import me.him188.ani.app.ui.user.SelfInfoUiState
import org.koin.mp.KoinPlatform

/**
 * iOS 的主分页内容: 探索、追番、快取、我的. 导航栏由 [AppleNavigationShell] 画在外层, 这里只画分页内容.
 *
 * 底部的更新提示与 Bangumi 收藏冲突提示同 [MainScreen], 位置在浮动导航栏上方.
 *
 * @param showNotificationDot 通知未读红点, 见 [MainScreenSharedViewModel.showNotificationDot].
 */
@Composable
internal fun AppleMainScreen(
    page: MainScreenPage,
    selfInfo: SelfInfoUiState,
    sharedViewModel: MainScreenSharedViewModel,
    showNotificationDot: Boolean,
    onNavigateToPage: (MainScreenPage) -> Unit,
    modifier: Modifier = Modifier,
) {
    val explorationPageViewModel = viewModel { ExplorationPageViewModel() }
    val userCollectionsViewModel = viewModel<UserCollectionsViewModel> { UserCollectionsViewModel() }
    val downloadManagementViewModel = viewModel { createDownloadManagementViewModel() }
    val profileViewModel = viewModel { ProfileViewModel() }

    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val toaster = LocalToaster.current

    // 与 MainScreen 相同: 未登录 (包括 token 过期且因网络错误未能刷新) 时不显示红点.
    val notificationDotVisible = showNotificationDot && selfInfo.isSessionValid == true && selfInfo.selfInfo != null
    val pageWindowInsets = AniWindowInsets.forPageContent().add(LocalAppChromeOverlayInsets.current)
    val bottomPadding = appleBottomInsets().asPaddingValues()

    CompositionLocalProvider(LocalSelfAvatarUnreadDot provides notificationDotVisible) {
        Box(modifier.background(MaterialTheme.colorScheme.background)) {
            val aniMotionScheme = LocalAniMotionScheme.current
            AnimatedContent(
                page,
                Modifier.fillMaxSize(),
                transitionSpec = { aniMotionScheme.topLevelTransition },
            ) { page ->
                when (page) {
                    MainScreenPage.Exploration -> {
                        ExplorationScreen(
                            explorationPageViewModel.explorationPageState,
                            selfInfo,
                            onSearch = { navigator.navigateSubjectSearch() },
                            onClickSettings = { onNavigateToPage(MainScreenPage.Profile) },
                            onClickLogin = { onNavigateToPage(MainScreenPage.Profile) },
                            modifier = Modifier.fillMaxSize(),
                            windowInsets = pageWindowInsets,
                        )
                    }

                    MainScreenPage.Collection -> {
                        CollectionPage(
                            state = userCollectionsViewModel.state,
                            selfInfo = selfInfo,
                            fullSyncState = userCollectionsViewModel.fullSyncState.collectAsStateWithLifecycle().value,
                            onClickSearch = { navigator.navigateSubjectSearch() },
                            onClickLogin = { onNavigateToPage(MainScreenPage.Profile) },
                            onClickSettings = { onNavigateToPage(MainScreenPage.Profile) },
                            onCollectionUpdate = { subjectId, episode ->
                                coroutineScope.launch {
                                    userCollectionsViewModel.toggleEpisodeCollection(
                                        subjectId,
                                        episode.episodeId,
                                        episode.collectionType,
                                    )?.let { toaster.showLoadError(it) }
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                            windowInsets = pageWindowInsets,
                            enableAnimation = userCollectionsViewModel.myCollectionsSettings.enableListAnimation1,
                        )
                    }

                    MainScreenPage.CacheManagement -> {
                        DownloadManagementScreen(
                            downloadManagementViewModel,
                            selfInfo = selfInfo,
                            onPlay = { navigator.navigateEpisodeDetails(it.subjectId, it.episodeId) },
                            onNavigateCacheDetail = { navigator.navigateCacheDetails(it) },
                            onClickLogin = { onNavigateToPage(MainScreenPage.Profile) },
                            modifier = Modifier.fillMaxSize(),
                            navigationIcon = { },
                            windowInsets = pageWindowInsets,
                        )
                    }

                    MainScreenPage.Profile -> {
                        val profileState by profileViewModel.stateFlow.collectAsStateWithLifecycle()
                        val unreadCount by sharedViewModel.notificationUnreadCount.collectAsStateWithLifecycle()
                        val pendingSyncCount by sharedViewModel.bangumiPendingSyncCount.collectAsStateWithLifecycle()
                        val updateViewModel = viewModel { AppUpdateViewModel() }
                        val updatePresentation by updateViewModel.presentationFlow.collectAsStateWithLifecycle()
                        val settingsRepository = remember { KoinPlatform.getKoin().get<SettingsRepository>() }
                        val debugSettings by settingsRepository.debugSettings.flow
                            .collectAsStateWithLifecycle(DebugSettings(_placeHolder = -1))
                        val collectionsState = userCollectionsViewModel.state

                        AppleProfilePage(
                            state = profileState,
                            collectionCounts = collectionsState.collectionCounts,
                            showNotificationDot = notificationDotVisible,
                            notificationUnreadCount = unreadCount,
                            pendingSyncCount = pendingSyncCount,
                            themeMode = LocalThemeSettings.current.darkMode,
                            hasNewVersion = updatePresentation.newVersion != null,
                            showDebugSettings = debugSettings.enabled,
                            onClickProfile = { navigator.navigateSettings(SettingsTab.PROFILE) },
                            onClickLogin = { navigator.navigateEmailLoginStart() },
                            onClickCollectionType = { type ->
                                val index = COLLECTION_TABS_SORTED.indexOf(type)
                                if (index != -1) {
                                    collectionsState.selectTypeIndex(index)
                                    coroutineScope.launch { collectionsState.pagerState.scrollToPage(index) }
                                }
                                onNavigateToPage(MainScreenPage.Collection)
                            },
                            onClickNotifications = { navigator.navigateNotifications() },
                            onClickPlaybackHistory = { navigator.navigatePlaybackHistory() },
                            onClickBangumiSync = { navigator.navigateBangumiSync() },
                            onClickSettings = { navigator.navigateSettings(it) },
                            onLogout = { profileViewModel.logout() },
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = bottomPadding,
                        )
                    }
                }
            }

            BottomNotifierStack(
                Modifier.matchParentSize().padding(bottomPadding),
                top = { UpdateNotifierWithVersionExpiryCheck() },
                bottom = {
                    // 版本过期锁定页展示时不检查 Bangumi 收藏冲突, 也不在其上叠加可跳转的提示.
                    val versionExpiryService = remember { KoinPlatform.getKoin().get<VersionExpiryService>() }
                    val versionExpired by versionExpiryService.state.collectAsStateWithLifecycle(null)
                    if (versionExpired == null) {
                        BangumiConflictNotifier(
                            selfInfo = selfInfo,
                            onNavigateToMerge = { navigator.navigateBangumiMerge() },
                        )
                    }
                },
            )
        }
    }
}
