/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.main

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.him188.ani.app.data.repository.episode.EpisodeCollectionRepository
import me.him188.ani.app.data.repository.player.EpisodePlayHistoryRepository
import me.him188.ani.app.data.repository.subject.BangumiSyncCommandRepository
import me.him188.ani.app.data.repository.user.SettingsRepository
import me.him188.ani.app.domain.foundation.HttpClientProvider
import me.him188.ani.app.domain.foundation.ServerListFeature
import me.him188.ani.app.domain.foundation.ServerListFeatureConfig
import me.him188.ani.app.domain.foundation.get
import me.him188.ani.app.domain.foundation.withValue
import me.him188.ani.app.domain.notification.NotificationBadgeManager
import me.him188.ani.app.domain.settings.ServiceConnectionTester
import me.him188.ani.app.domain.settings.ServiceConnectionTesters
import me.him188.ani.app.ui.foundation.AbstractViewModel
import me.him188.ani.app.ui.foundation.launchInBackground
import me.him188.ani.app.ui.notification.showsEntryDot
import me.him188.ani.app.ui.user.SelfInfoStateProducer
import me.him188.ani.utils.logging.warn
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

open class MainScreenSharedViewModel : AbstractViewModel(), KoinComponent {
    val selfInfo = SelfInfoStateProducer(koin = getKoin()).flow

    private val clientProvider: HttpClientProvider by inject()

    private val networkCheckFailedChannel = Channel<Unit>(Channel.BUFFERED)

    /**
     * 启动时检测 Animeko 服务连接, 失败时发出一个事件, UI 提示用户检查网络或配置代理.
     */
    val networkCheckFailed: Flow<Unit> = networkCheckFailedChannel.receiveAsFlow()

    private val notificationBadgeManager: NotificationBadgeManager by inject()
    private val settingsRepository: SettingsRepository by inject()

    /**
     * 头像和头像菜单里的“通知”是否显示未读红点.
     *
     * 只在首次访问时创建并订阅 [NotificationBadgeManager]: 继承本类但没有通知入口的界面 (TV) 不会访问它.
     */
    val showNotificationDot: StateFlow<Boolean> by lazy {
        combine(
            notificationBadgeManager.state,
            settingsRepository.notificationSettings.flow,
        ) { state, settings ->
            state.showsEntryDot(settings)
        }.stateIn(backgroundScope, SharingStarted.WhileSubscribed(5000), false)
    }

    /**
     * 未读通知数, 显示在「我的」页的通知列. 公告不计入, 所以只有未读公告时为 0, 入口只显示红点.
     *
     * 只有 Apple 版画面订阅.
     */
    val notificationUnreadCount: StateFlow<Int> by lazy {
        notificationBadgeManager.state
            .map { it.unreadCount }
            .stateIn(backgroundScope, SharingStarted.WhileSubscribed(5000), 0)
    }

    private val playHistoryRepository: EpisodePlayHistoryRepository by inject()
    private val episodeCollectionRepository: EpisodeCollectionRepository by inject()
    private val bangumiSyncCommandRepository: BangumiSyncCommandRepository by inject()

    /**
     * 「N 項待同步」的数字: 播放进度与看过状态两条本地队列按集合并后的数量 (即播放记录同步状态页的项目数),
     * 加上 Bangumi 收藏同步队列中待执行的命令数. 未连接 Bangumi 或查询失败时, 收藏队列按 0 计.
     *
     * 只有 Apple 版画面订阅.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val bangumiPendingSyncCount: StateFlow<Int> by lazy {
        val localCount = combine(
            playHistoryRepository.pendingOpsFlow,
            episodeCollectionRepository.pendingOpsFlow,
        ) { playbackOps, collectionOps ->
            (playbackOps.mapTo(mutableSetOf()) { it.episodeId } + collectionOps.map { it.episodeId }).size
        }
        val remoteCount = selfInfo
            .map { it.isSessionValid == true && it.bangumiConnected == true }
            .distinctUntilChanged()
            .mapLatest { connected ->
                if (!connected) return@mapLatest 0
                try {
                    bangumiSyncCommandRepository.listSyncCommands(limit = 1).total.toInt()
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    logger.warn(e) { "Failed to count Bangumi sync commands" }
                    0
                }
            }
        combine(localCount, remoteCount) { local, remote -> local + remote }
            .stateIn(backgroundScope, SharingStarted.WhileSubscribed(5000), 0)
    }

    init {
        launchInBackground {
            val client = clientProvider.get(
                setOf(ServerListFeature.withValue(ServerListFeatureConfig.Default)),
            )
            val tester = ServiceConnectionTesters.createDefault(aniClient = client)
            coroutineScope {
                launch { tester.testAll() }
                // 内部 state 是 StateFlow, 测试完成后再订阅也能拿到最终结果
                val results = tester.results.first { it.allCompleted() }
                val state = results.findStateById(ServiceConnectionTesters.ID_ANI)
                if (state != null && state !is ServiceConnectionTester.TestState.Success) {
                    networkCheckFailedChannel.send(Unit)
                }
            }
        }
    }
}
