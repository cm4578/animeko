/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.download.subject

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import me.him188.ani.app.tools.toProgress
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.components.DownloadStatus
import me.him188.ani.app.ui.download.components.createTestDownloadItem
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.UnifiedCollectionType

/**
 * Apple 版单番快取页测试共用的资料: 第 1、2 集已完成 (第 1 集标为看过), 第 3 集下载中, 第 4 集暂停, 第 5 集失败, 第 6 集尚未下载.
 */
internal object TestSubjectDownloads {
    fun download(sort: Int, status: DownloadStatus, progress: Float): DownloadItem =
        createTestDownloadItem(sort, displayName = "Episode $sort", initialState = status, progress = progress.toProgress())
            .copy(id = "d$sort")

    val finished1 = download(1, DownloadStatus.COMPLETED, 1f)
    val finished2 = download(2, DownloadStatus.COMPLETED, 1f)
    val downloading = download(3, DownloadStatus.IN_PROGRESS, 0.62f)
    val paused = download(4, DownloadStatus.PAUSED, 0.2f)
    val failed = download(5, DownloadStatus.FAILED, 0.3f)

    val downloads = listOf(finished1, finished2, downloading, paused, failed)

    val episodes = (1..6).map {
        EpisodeDownloadItem(
            episodeId = it,
            sort = EpisodeSort(it),
            title = "Episode $it",
            watchStatus = if (it == 1) UnifiedCollectionType.DONE else UnifiedCollectionType.NOT_COLLECTED,
            hasPublished = true,
        )
    }

    fun state(
        downloads: List<DownloadItem> = this.downloads,
        request: DownloadRequestUiState = DownloadRequestUiState(),
    ) = SubjectDownloadsUiState(
        title = "Test Show",
        items = buildSubjectDownloadItems(episodes, downloads),
        downloads = downloads,
        totalEpisodes = episodes.size,
        episodesLoading = false,
        downloadsLoading = false,
        request = request,
        episodeCollectionTypes = episodes.associate { it.episodeId to it.watchStatus },
    )
}

/**
 * 记录 [SubjectDownloadActions] 的调用.
 */
internal class RecordingSubjectDownloadActions {
    val downloaded = mutableListOf<Int>()
    val paused = mutableListOf<Set<String>>()
    val resumed = mutableListOf<Set<String>>()
    val deleted = mutableListOf<Set<String>>()
    var pauseAll = 0
    var resumeAll = 0

    val actions = SubjectDownloadActions(
        download = { downloaded += it },
        cancelRequest = {},
        pause = { paused += it },
        resume = { resumed += it },
        delete = { deleted += it },
        pauseAll = { pauseAll++ },
        resumeAll = { resumeAll++ },
        reload = {},
    )
}

/**
 * 断言标签为 [tag] 的容器里有含 [text] 的文字. 容器 (例如 `Row`) 不合并子节点的文字, 所以按祖先查找.
 */
internal fun ComposeUiTest.assertTextInside(tag: String, text: String) {
    onAllNodes(hasText(text, substring = true) and hasAnyAncestor(hasTestTag(tag))).onFirst().assertExists()
}
