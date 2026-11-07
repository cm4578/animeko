/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.download

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import me.him188.ani.app.domain.media.cache.engine.MediaStats
import me.him188.ani.app.tools.toProgress
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.components.DownloadStatus
import me.him188.ani.app.ui.download.components.SubjectDownloadGroup
import me.him188.ani.app.ui.download.components.createTestDownloadItem
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_management_pause_all
import me.him188.ani.app.ui.lang.cache_management_resume_all
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import org.jetbrains.compose.resources.stringResource

/**
 * docs/design/ani-style/04-macos.md §3.5 macOS 快取页; 05 C3、D2、G5、G9、G25.
 */
@OptIn(ExperimentalTestApi::class)
class MacDownloadManagementPageTest {
    private fun group(subjectId: Int, name: String, vararg entries: DownloadItem) = SubjectDownloadGroup(
        subjectId = subjectId,
        subjectName = name,
        entries = entries.toList(),
        collectionType = UnifiedCollectionType.DOING,
        totalEpisodeCount = 12,
    )

    private fun item(subjectId: Int, sort: Int, status: DownloadStatus, progress: Float = 0.4f) =
        createTestDownloadItem(sort, subjectId = subjectId, initialState = status, progress = progress.toProgress())

    private val downloading = group(
        1, "Downloading Show",
        item(1, 1, DownloadStatus.COMPLETED, 1f),
        item(1, 2, DownloadStatus.IN_PROGRESS, 0.6f),
    )
    private val finished = group(2, "Finished Show", item(2, 1, DownloadStatus.COMPLETED, 1f))
    private val paused = group(3, "Paused Show", item(3, 1, DownloadStatus.PAUSED, 0.4f))

    private class Callbacks {
        val openedDownloads = mutableListOf<Int>()
        val openedDetails = mutableListOf<Int>()
        val deleted = mutableListOf<DownloadItem>()
        val revealed = mutableListOf<Int>()
        var pauseAll = 0
        var resumeAll = 0
        var revealDirectory = 0
        var pauseAllLabel = ""
        var resumeAllLabel = ""
    }

    private fun ComposeUiTest.setPage(groups: List<SubjectDownloadGroup>): Callbacks {
        val callbacks = Callbacks()
        setContent {
            callbacks.pauseAllLabel = stringResource(Lang.cache_management_pause_all)
            callbacks.resumeAllLabel = stringResource(Lang.cache_management_resume_all)
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    MacDownloadManagementLayout(
                        DownloadManagementUiState(MediaStats.Zero, groups),
                        cacheDirectory = "/Users/test/Ani",
                        onOpenSubjectDownloads = { callbacks.openedDownloads += it },
                        onOpenSubjectDetails = { callbacks.openedDetails += it },
                        onPause = {},
                        onResume = {},
                        onDelete = { callbacks.deleted += it },
                        onPauseAll = { callbacks.pauseAll++ },
                        onResumeAll = { callbacks.resumeAll++ },
                        onRevealDirectory = { callbacks.revealDirectory++ },
                        onRevealGroup = { callbacks.revealed += it.subjectId },
                        Modifier.size(1040.dp, 900.dp),
                    )
                }
            }
        }
        return callbacks
    }

    @Test
    fun `pause all becomes resume all when nothing is downloading`() = runAniComposeUiTest {
        val withDownloading = setPage(listOf(downloading, paused))
        onNodeWithTag(MacDownloadTestTags.PauseAll).assertTextContains(withDownloading.pauseAllLabel)
        onNodeWithTag(MacDownloadTestTags.PauseAll).performClick()
        assertEquals(1, withDownloading.pauseAll)
    }

    @Test
    fun `resume all when only paused downloads are left`() = runAniComposeUiTest {
        val callbacks = setPage(listOf(finished, paused))
        onNodeWithTag(MacDownloadTestTags.PauseAll).assertTextContains(callbacks.resumeAllLabel)
        onNodeWithTag(MacDownloadTestTags.PauseAll).performClick()
        assertEquals(1, callbacks.resumeAll)
        assertEquals(0, callbacks.pauseAll)
    }

    @Test
    fun `click selects a row and double click opens the subject downloads`() = runAniComposeUiTest {
        val callbacks = setPage(listOf(downloading, finished))

        onNodeWithText("Finished Show").performClick()
        waitForIdle()
        onNodeWithTag(MacDownloadTestTags.group(finished.subjectId)).assertIsSelectedRow()
        assertEquals(emptyList(), callbacks.openedDownloads)

        onNodeWithText("Downloading Show").performTouchInput { doubleClick() }
        waitForIdle()
        assertEquals(listOf(downloading.subjectId), callbacks.openedDownloads)
    }

    @Test
    fun `right click menu reveals in Finder and deletes after confirmation`() = runAniComposeUiTest {
        val callbacks = setPage(listOf(downloading, finished))

        onNodeWithText("Finished Show").performMouseInput { rightClick(center) }
        onNodeWithTag(MacDownloadTestTags.Menu).assertIsDisplayed()
        onNodeWithTag(MacDownloadTestTags.MenuReveal).performClick()
        waitForIdle()
        assertEquals(listOf(finished.subjectId), callbacks.revealed)
        onNodeWithTag(MacDownloadTestTags.Menu).assertDoesNotExist()

        onNodeWithText("Finished Show").performMouseInput { rightClick(center) }
        onNodeWithTag(MacDownloadTestTags.MenuDelete).performClick()
        waitForIdle()
        // 删除先确认
        assertEquals(emptyList(), callbacks.deleted)
    }

    @Test
    fun `cache location links to Finder`() = runAniComposeUiTest {
        val callbacks = setPage(listOf(finished))
        onNodeWithText("/Users/test/Ani").assertIsDisplayed()
        onNodeWithTag(MacDownloadTestTags.RevealDirectory).performClick()
        assertEquals(1, callbacks.revealDirectory)
    }

    /**
     * 行的选中状态在 [MacTableRow] 的节点上 (testTag 节点的子节点).
     */
    private fun SemanticsNodeInteraction.assertIsSelectedRow() =
        onChildren().filterToOne(isSelected()).assertExists()
}
