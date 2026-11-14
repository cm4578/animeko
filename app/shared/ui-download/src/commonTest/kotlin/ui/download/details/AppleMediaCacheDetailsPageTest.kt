/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.download.details

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import me.him188.ani.app.domain.media.cache.DownloaderStatus
import me.him188.ani.app.domain.media.cache.MediaCache
import me.him188.ani.app.domain.media.cache.MediaCacheState
import me.him188.ani.app.tools.toProgress
import me.him188.ani.app.ui.download.DownloadManagementTestTags
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.components.DownloadStatus
import me.him188.ani.app.ui.download.components.createTestDownloadItem
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_details_peers_summary
import me.him188.ani.app.ui.lang.cache_episode_pause_download
import me.him188.ani.app.ui.lang.cache_episode_resume_download
import me.him188.ani.datasources.api.topic.FileSize.Companion.megaBytes
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.annotations.TestOnly
import org.jetbrains.compose.resources.stringResource

/**
 * docs/design/ani-style/03-mobile-pages.md §4.4 iOS 单集下载详情; 05 G28.
 */
@OptIn(TestOnly::class)
class AppleMediaCacheDetailsPageTest {
    private class Page {
        val played = mutableListOf<DownloadItem>()
        val paused = mutableListOf<DownloadItem>()
        val resumed = mutableListOf<DownloadItem>()
        val deleted = mutableListOf<DownloadItem>()
        var pauseLabel = ""
        var resumeLabel = ""
        var peersText = ""
    }

    private fun item(status: DownloadStatus, progress: Float) =
        createTestDownloadItem(9, displayName = "Constellation", initialState = status, progress = progress.toProgress())

    private val torrentDownloader = DownloaderDetails(
        MediaCacheState.IN_PROGRESS,
        MediaCache.SessionStats(
            totalSize = 1400.megaBytes,
            downloadedBytes = 870.megaBytes,
            downloadSpeed = 2.megaBytes,
            uploadedBytes = 10.megaBytes,
            uploadSpeed = 1.megaBytes,
            downloadProgress = 0.62f.toProgress(),
        ),
        DownloaderStatus.Torrent(
            serviceConnected = true,
            startup = DownloaderStatus.TorrentStartup.STARTED,
            state = null,
            connectedPeers = 12,
            seeds = 4,
        ),
    )

    private fun ComposeUiTest.setPage(
        download: DownloadItem?,
        state: MediaCacheDetailsScreenState = MediaCacheDetailsScreenState(TestMediaDetails),
    ): Page {
        val page = Page()
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    page.pauseLabel = stringResource(Lang.cache_episode_pause_download)
                    page.resumeLabel = stringResource(Lang.cache_episode_resume_download)
                    page.peersText = stringResource(Lang.cache_details_peers_summary, 12, 4)
                    AppleMediaCacheDetailsLayout(
                        state,
                        download = download,
                        coverUrl = null,
                        onPlay = { page.played += it },
                        onPause = { page.paused += it },
                        onResume = { page.resumed += it },
                        onDelete = { page.deleted += it },
                        onBack = {},
                        Modifier.size(390.dp, 1600.dp),
                    )
                }
            }
        }
        waitForIdle()
        return page
    }

    /** 容器 (例如 `Column`) 不合并子节点的文字, 所以按祖先查找. */
    private fun ComposeUiTest.assertTextInside(tag: String, text: String) {
        onAllNodes(hasText(text, substring = true) and hasAnyAncestor(hasTestTag(tag))).onFirst().assertExists()
    }

    @Test
    fun `downloading item shows progress transfer and both buttons`() = runAniComposeUiTest {
        val downloading = item(DownloadStatus.IN_PROGRESS, 0.62f)
        val page = setPage(downloading, MediaCacheDetailsScreenState(TestMediaDetails, torrentDownloader))
        onNodeWithTag(AppleMediaCacheDetailsTestTags.Percent).assertTextEquals("62%")
        assertTextInside(AppleMediaCacheDetailsTestTags.StatusCard, page.peersText)
        assertTextInside(AppleMediaCacheDetailsTestTags.StatusCard, "↓")

        onNodeWithTag(AppleMediaCacheDetailsTestTags.Play).performClick()
        onNodeWithTag(AppleMediaCacheDetailsTestTags.PauseOrResume).assertTextContains(page.pauseLabel).performClick()
        assertEquals(listOf(downloading), page.played)
        assertEquals(listOf(downloading), page.paused)
    }

    @Test
    fun `paused item resumes`() = runAniComposeUiTest {
        val paused = item(DownloadStatus.PAUSED, 0.2f)
        val page = setPage(paused)
        onNodeWithTag(AppleMediaCacheDetailsTestTags.PauseOrResume).assertTextContains(page.resumeLabel).performClick()
        assertEquals(listOf(paused), page.resumed)
    }

    @Test
    fun `finished item only plays and failed item has no buttons`() = runAniComposeUiTest {
        setPage(item(DownloadStatus.COMPLETED, 1f))
        onNodeWithTag(AppleMediaCacheDetailsTestTags.Percent).assertTextEquals("100%")
        onNodeWithTag(AppleMediaCacheDetailsTestTags.Play).assertIsDisplayed()
        onNodeWithTag(AppleMediaCacheDetailsTestTags.PauseOrResume).assertDoesNotExist()
    }

    @Test
    fun `failed item has no buttons`() = runAniComposeUiTest {
        setPage(item(DownloadStatus.FAILED, 0.3f))
        onNodeWithTag(AppleMediaCacheDetailsTestTags.Play).assertDoesNotExist()
        onNodeWithTag(AppleMediaCacheDetailsTestTags.PauseOrResume).assertDoesNotExist()
    }

    @Test
    fun `busy item cannot be paused`() = runAniComposeUiTest {
        setPage(item(DownloadStatus.IN_PROGRESS, 0.5f).copy(isBusy = true))
        onNodeWithTag(AppleMediaCacheDetailsTestTags.PauseOrResume).assertIsNotEnabled()
    }

    @Test
    fun `resource and links sections show the media details`() = runAniComposeUiTest {
        setPage(item(DownloadStatus.COMPLETED, 1f))
        assertTextInside(AppleMediaCacheDetailsTestTags.Resource, TestMediaDetails.originalTitle)
        assertTextInside(AppleMediaCacheDetailsTestTags.Resource, TestMediaDetails.properties.alliance)
        assertTextInside(AppleMediaCacheDetailsTestTags.Links, TestMediaDetails.contentOriginalUri)
    }

    @Test
    fun `delete asks for confirmation`() = runAniComposeUiTest {
        val finished = item(DownloadStatus.COMPLETED, 1f)
        val page = setPage(finished)
        onNode(hasScrollAction()).performScrollToNode(hasTestTag(AppleMediaCacheDetailsTestTags.Delete))
        onNodeWithTag(AppleMediaCacheDetailsTestTags.Delete).performClick()
        waitForIdle()
        assertEquals(emptyList(), page.deleted)
        onNodeWithTag(DownloadManagementTestTags.DELETE_CONFIRM_BUTTON).performClick()
        assertEquals(listOf(finished), page.deleted)
    }

    @Test
    fun `missing download hides the status card and delete`() = runAniComposeUiTest {
        setPage(null)
        onNodeWithTag(AppleMediaCacheDetailsTestTags.StatusCard).assertDoesNotExist()
        onNodeWithTag(AppleMediaCacheDetailsTestTags.Delete).assertDoesNotExist()
        onNodeWithTag(AppleMediaCacheDetailsTestTags.Resource).assertIsDisplayed()
    }
}
