/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.download.subject

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import me.him188.ani.app.data.models.subject.SubjectAiringKind
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.components.DownloadStatus
import me.him188.ani.app.ui.download.components.renderFileSize
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_episode_watched_progress
import me.him188.ani.app.ui.lang.cache_filter_collection_done
import me.him188.ani.app.ui.lang.cache_filter_collection_dropped
import me.him188.ani.app.ui.lang.cache_management_downloading_count
import me.him188.ani.app.ui.lang.cache_management_episode_label
import me.him188.ani.app.ui.lang.cache_management_finished_count
import me.him188.ani.app.ui.lang.cache_management_invalid_cache_info
import me.him188.ani.app.ui.lang.cache_management_streaming_not_supported
import me.him188.ani.app.ui.lang.cache_subject_total_size
import me.him188.ani.app.ui.lang.subject_airing_total_episodes_completed
import me.him188.ani.app.ui.lang.subject_airing_total_episodes_scheduled
import me.him188.ani.app.ui.subject.renderSubjectSeason
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版单番快取页 (03 §4.3、04 §4.3) 的分组, 各组保持 [SubjectDownloadsUiState.items] 的集数顺序.
 *
 * @param active「进行中」: 下载中、已暂停与下载失败的下载.
 * @param finished「已完成」的下载.
 * @param notDownloaded 尚未下载的剧集, 接在已完成之后, 右侧为下载按钮.
 */
@Immutable
internal class AppleSubjectDownloadSections(
    val active: List<DownloadItem>,
    val finished: List<DownloadItem>,
    val notDownloaded: List<EpisodeDownloadItem>,
) {
    /** 显示顺序的全部下载: 进行中在前, 已完成在后. */
    val downloads: List<DownloadItem> get() = active + finished
}

internal fun SubjectDownloadsUiState.appleSections(): AppleSubjectDownloadSections {
    val active = mutableListOf<DownloadItem>()
    val finished = mutableListOf<DownloadItem>()
    val notDownloaded = mutableListOf<EpisodeDownloadItem>()
    for (item in items) {
        when (item) {
            is SubjectDownloadListItem.Episode -> notDownloaded += item.episode
            is SubjectDownloadListItem.Download -> if (item.download.isFinished) {
                finished += item.download
            } else {
                active += item.download
            }
        }
    }
    return AppleSubjectDownloadSections(active, finished, notDownloaded)
}

/**
 * 页头摘要「8/28 已完成 · 1 个下载中 · 共 10.6 GB」的数字.
 *
 * 已完成与总集数按剧集计 (同一集有多个下载只算一次), 总集数不少于有下载的集数, 同既有摘要;
 * 下载中只计正在下载的项目, 已暂停与失败的不算, 同 Apple 版快取页.
 *
 * @param totalSize 全部下载的总大小; 都未知时为 `null`.
 */
@Immutable
internal class AppleSubjectDownloadsSummary(
    val finishedEpisodes: Int,
    val totalEpisodes: Int,
    val downloadingCount: Int,
    val totalSize: FileSize?,
)

internal fun SubjectDownloadsUiState.appleSummary(): AppleSubjectDownloadsSummary {
    val downloadedEpisodes = downloads.map { it.episodeId }.distinct().size
    return AppleSubjectDownloadsSummary(
        finishedEpisodes = downloads.filter { it.isFinished }.map { it.episodeId }.distinct().size,
        totalEpisodes = totalEpisodes?.coerceAtLeast(downloadedEpisodes) ?: downloadedEpisodes,
        downloadingCount = downloads.count { it.status == DownloadStatus.IN_PROGRESS },
        totalSize = downloads.totalSizeOrNull(),
    )
}

/**
 * 已知大小的总和; 都未知时为 `null`.
 */
internal fun List<DownloadItem>.totalSizeOrNull(): FileSize? {
    val known = filter { !it.totalSize.isUnspecified }
    if (known.isEmpty()) return null
    return known.sumOf { it.totalSize.inBytes }.bytes
}

/**
 * 「进行中」分组右侧的批量动作: 有下载中的项目时为全部暂停, 只剩暂停的项目时为全部继续, 其余 (例如只剩失败) 不显示.
 */
internal enum class AppleBulkAction {
    PauseAll,
    ResumeAll,
}

internal fun List<DownloadItem>.appleBulkAction(): AppleBulkAction? = when {
    any { it.status == DownloadStatus.IN_PROGRESS } -> AppleBulkAction.PauseAll
    any { it.isPaused } -> AppleBulkAction.ResumeAll
    else -> null
}

/**
 * 页头第二行「2023 年 10 月 · 全 28 话」: 季度沿用 [renderSubjectSeason], 话数沿用搜索结果的写法. 都没有时为 `null`.
 */
@Composable
internal fun subjectAiringLine(state: SubjectDownloadsUiState): String? {
    val season = if (state.airDate.isValid) renderSubjectSeason(state.airDate) else null
    val episodes = state.airingInfo?.let { info ->
        when {
            info.kind == SubjectAiringKind.UPCOMING && info.mainEpisodeCount == 0 -> null
            info.kind == SubjectAiringKind.COMPLETED ->
                stringResource(Lang.subject_airing_total_episodes_completed, info.mainEpisodeCount.toString())

            else -> stringResource(Lang.subject_airing_total_episodes_scheduled, info.mainEpisodeCount.toString())
        }
    }
    return listOfNotNull(season, episodes).joinToString(" · ").ifEmpty { null }
}

/**
 * 页头第三行「8/28 已完成 · 1 个下载中 · 共 10.6 GB」; 没有下载中的项目时省略中间一段, 大小未知时省略最后一段.
 */
@Composable
internal fun subjectSummaryLine(summary: AppleSubjectDownloadsSummary): String = buildList {
    add(stringResource(Lang.cache_management_finished_count, summary.finishedEpisodes, summary.totalEpisodes))
    if (summary.downloadingCount > 0) {
        add(stringResource(Lang.cache_management_downloading_count, summary.downloadingCount))
    }
    summary.totalSize?.let { add(stringResource(Lang.cache_subject_total_size, renderFileSize(it))) }
}.joinToString(" · ")

/**
 * 剧集标题, 用现有的「第09话 · 星座」; 没有集名时去掉结尾的「 · 」, 为「第09话」, 与其他行同一个写法.
 */
@Composable
internal fun downloadEpisodeTitle(sort: EpisodeSort, name: String): String {
    val label = stringResource(Lang.cache_management_episode_label, sort, name)
    return if (name.isBlank()) label.trimEnd(' ', '·') else label
}

/**
 * 已完成的下载的观看状态: 剧集标为看过时为「看过」, 抛弃时为「抛弃」, 否则为现有的「已观看 N%」. 都没有时为 `null`.
 */
@Composable
internal fun downloadWatchStatusText(download: DownloadItem, collectionType: UnifiedCollectionType?): String? =
    when (collectionType) {
        UnifiedCollectionType.DONE -> stringResource(Lang.cache_filter_collection_done)
        UnifiedCollectionType.DROPPED -> stringResource(Lang.cache_filter_collection_dropped)
        else -> download.playbackProgressText?.let { stringResource(Lang.cache_episode_watched_progress, it) }
    }

/**
 * 尚未下载的剧集的观看状态: 看过或抛弃; 其余为 `null`. 同既有的剧集行.
 */
@Composable
internal fun episodeWatchStatusText(collectionType: UnifiedCollectionType): String? = when (collectionType) {
    UnifiedCollectionType.DONE -> stringResource(Lang.cache_filter_collection_done)
    UnifiedCollectionType.DROPPED -> stringResource(Lang.cache_filter_collection_dropped)
    else -> null
}

/**
 * 播放下载: 可播放时调用 [onPlay], 下载信息无效或不支持边下边播时提示, 同既有的行操作 (`DownloadPrimaryAction`).
 */
@Composable
internal fun downloadPlayHandler(onPlay: (DownloadItem) -> Unit): (DownloadItem) -> Unit {
    val toaster = LocalToaster.current
    val invalidCacheInfoText = stringResource(Lang.cache_management_invalid_cache_info)
    val streamingNotSupportedText = stringResource(Lang.cache_management_streaming_not_supported)
    return { download ->
        when (download.playability) {
            DownloadItem.Playability.PLAYABLE -> onPlay(download)
            DownloadItem.Playability.INVALID_SUBJECT_EPISODE_ID -> toaster.toast(invalidCacheInfoText)
            DownloadItem.Playability.STREAMING_NOT_SUPPORTED -> toaster.toast(streamingNotSupportedText)
        }
    }
}
