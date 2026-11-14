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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.subject.toNavPlaceholder
import me.him188.ani.app.domain.foundation.VersionExpiryService
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.navigation.MainScreenPage
import me.him188.ani.app.navigation.SettingsTab
import me.him188.ani.app.ui.bangumi.merge.BangumiConflictNotifier
import me.him188.ani.app.ui.download.MacDownloadManagementPage
import me.him188.ani.app.ui.download.createDownloadManagementViewModel
import me.him188.ani.app.ui.exploration.ExplorationPageViewModel
import me.him188.ani.app.ui.exploration.MacExplorationPage
import me.him188.ani.app.ui.exploration.schedule.ScheduleViewModel
import me.him188.ani.app.ui.foundation.animation.LocalAniMotionScheme
import me.him188.ani.app.ui.foundation.session.LocalSelfAvatarUnreadDot
import me.him188.ani.app.ui.foundation.theme.LocalAppChromeHazeState
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.showLoadError
import me.him188.ani.app.ui.subject.collection.MacCollectionPage
import me.him188.ani.app.ui.subject.collection.UserCollectionsViewModel
import me.him188.ani.app.ui.user.SelfInfoUiState
import org.koin.mp.KoinPlatform

/**
 * macOS 的主分页内容: 探索、追番、快取. 侧边栏由 [MacNavigationShell] 画在外层, 这里只画分页内容.
 *
 * 底部的更新提示与 Bangumi 收藏冲突提示同 [MainScreen].
 *
 * @param showNotificationDot 通知未读红点, 见 [MainScreenSharedViewModel.showNotificationDot].
 * @param onOpenSettings 打开设定.
 */
@Composable
internal fun MacMainScreen(
    page: MainScreenPage,
    selfInfo: SelfInfoUiState,
    showNotificationDot: Boolean,
    onOpenSettings: (SettingsTab?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val explorationPageViewModel = viewModel { ExplorationPageViewModel() }
    val userCollectionsViewModel = viewModel<UserCollectionsViewModel> { UserCollectionsViewModel() }
    val downloadManagementViewModel = viewModel { createDownloadManagementViewModel() }

    val navigator = LocalNavigator.current
    val coroutineScope = rememberCoroutineScope()
    val toaster = LocalToaster.current

    // 与 MainScreen 相同: 未登录 (包括 token 过期且因网络错误未能刷新) 时不显示红点.
    val notificationDotVisible = showNotificationDot && selfInfo.isSessionValid == true && selfInfo.selfInfo != null

    CompositionLocalProvider(
        LocalSelfAvatarUnreadDot provides notificationDotVisible,
        LocalAppChromeHazeState provides rememberHazeState(),
    ) {
        Box(modifier.background(MaterialTheme.colorScheme.background)) {
            val aniMotionScheme = LocalAniMotionScheme.current
            AnimatedContent(
                page,
                Modifier.fillMaxSize(),
                transitionSpec = { aniMotionScheme.topLevelTransition },
            ) { page ->
                when (page) {
                    // macOS 没有「我的」分页, 按探索处理
                    MainScreenPage.Exploration, MainScreenPage.Profile -> {
                        // 新番时间表小工具 (05 G4) 读取时间表的数据源, 只有 macOS 探索页创建与订阅
                        val scheduleViewModel = viewModel { ScheduleViewModel() }
                        val schedule by scheduleViewModel.presentationFlow.collectAsStateWithLifecycle()
                        MacExplorationPage(
                            explorationPageViewModel.explorationPageState,
                            explorationPageViewModel.quickFollowState,
                            selfInfo,
                            schedule = schedule,
                            onRefreshSchedule = { scheduleViewModel.refresh() },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    MainScreenPage.Collection -> {
                        MacCollectionPage(
                            state = userCollectionsViewModel.state,
                            selfInfo = selfInfo,
                            continueWatching = userCollectionsViewModel.continueWatchingPager,
                            layout = userCollectionsViewModel.myCollectionsSettings.layout,
                            onLayoutChange = { userCollectionsViewModel.setCollectionLayout(it) },
                            episodeHistoryFlow = userCollectionsViewModel::episodePlayHistoryFlow,
                            nextEpisodeCachedFlow = userCollectionsViewModel::nextEpisodeCachedFlow,
                            onClickSearch = { navigator.navigateSubjectSearch() },
                            onClickLogin = { navigator.navigateEmailLoginStart() },
                            onClickPlaybackHistory = { navigator.navigatePlaybackHistory() },
                            onClickSubject = { navigator.navigateSubjectDetails(it.subjectId, it.toNavPlaceholder()) },
                            onPlay = { subjectId, episodeId -> navigator.navigateEpisodeDetails(subjectId, episodeId) },
                            onMarkEpisodeWatched = { subjectId, episode ->
                                coroutineScope.launch {
                                    userCollectionsViewModel.toggleEpisodeCollection(
                                        subjectId,
                                        episode.episodeId,
                                        episode.collectionType,
                                    )?.let { toaster.showLoadError(it) }
                                }
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    MainScreenPage.CacheManagement -> {
                        MacDownloadManagementPage(
                            downloadManagementViewModel,
                            onOpenSubjectDownloads = { navigator.navigateSubjectCaches(it) },
                            onOpenSubjectDetails = { navigator.navigateSubjectDetails(it, placeholder = null) },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }

            BottomNotifierStack(
                Modifier.matchParentSize(),
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
