/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.download.subject

import androidx.compose.runtime.Immutable
import me.him188.ani.app.data.models.subject.SubjectAiringInfo
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.PackedDate
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

@Immutable
data class EpisodeDownloadItem(
    val episodeId: Int,
    val sort: EpisodeSort,
    val title: String,
    val watchStatus: UnifiedCollectionType,
    val hasPublished: Boolean,
    /** 剧集原名, 供"显示原名"设置开启时使用; 默认与 [title] 相同. */
    val originalTitle: String = title,
)

sealed interface SubjectDownloadListItem {
    val key: String

    data class Episode(val episode: EpisodeDownloadItem) : SubjectDownloadListItem {
        override val key = "episode-${episode.episodeId}"
    }

    data class Download(val download: DownloadItem) : SubjectDownloadListItem {
        override val key = "download-${download.id}"
    }
}

@Immutable
data class DownloadRequestUiState(
    val episodeIds: Set<Int> = emptySet(),
    val busy: Boolean = false,
    val canCancel: Boolean = false,
)

/**
 * @param coverUrl 条目封面, 来自已加载的条目信息. 供 Apple 版页头 (03 §4.3、04 §4.3) 使用.
 * @param airDate 放送开始日期, 用于页头的季度文字; 未知时为 [PackedDate.Invalid].
 * @param airingInfo 连载状态与正片话数, 用于页头的「全 N 话」; 条目信息未加载时为 `null`.
 * @param episodeCollectionTypes 各剧集的观看状态 (看过、抛弃等), 键为剧集 id. Apple 版在已完成的下载上显示「看过」.
 */
@Immutable
data class SubjectDownloadsUiState(
    val title: String? = null,
    val items: List<SubjectDownloadListItem> = emptyList(),
    val downloads: List<DownloadItem> = emptyList(),
    val totalEpisodes: Int? = null,
    val episodesLoading: Boolean = true,
    val downloadsLoading: Boolean = true,
    val episodesFailed: Boolean = false,
    val downloadsFailed: Boolean = false,
    val request: DownloadRequestUiState = DownloadRequestUiState(),
    val coverUrl: String? = null,
    val airDate: PackedDate = PackedDate.Invalid,
    val airingInfo: SubjectAiringInfo? = null,
    val episodeCollectionTypes: Map<Int, UnifiedCollectionType> = emptyMap(),
)
