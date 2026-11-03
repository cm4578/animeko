/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.download

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import me.him188.ani.app.domain.media.cache.engine.MediaStats
import me.him188.ani.app.tools.toProgress
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.components.DownloadStatus
import me.him188.ani.app.ui.download.components.DownloadStatusFilter
import me.him188.ani.app.ui.download.components.SubjectDownloadGroup
import me.him188.ani.app.ui.download.components.createTestDownloadItem
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

class AppleDownloadManagementPageTest {
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
        item(1, 3, DownloadStatus.FAILED),
    )
    private val finished = group(2, "Finished Show", item(2, 1, DownloadStatus.COMPLETED, 1f))
    private val paused = group(3, "Paused Show", item(3, 1, DownloadStatus.PAUSED, 0.4f))

    private val state = DownloadManagementUiState(MediaStats.Zero, listOf(downloading, finished, paused))

    private fun ComposeUiTest.setPage(
        onOpenSubject: (Int) -> Unit = {},
        onPause: (DownloadItem) -> Unit = {},
        onResume: (DownloadItem) -> Unit = {},
        onDelete: (DownloadItem) -> Unit = {},
    ) {
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    AppleDownloadManagementLayout(
                        state,
                        onOpenSubject = onOpenSubject,
                        onPause = onPause,
                        onResume = onResume,
                        onDelete = onDelete,
                        Modifier.width(390.dp),
                    )
                }
            }
        }
    }

    @Test
    fun `group summary follows the most active download`() {
        val summary = downloading.appleSummary()
        assertEquals(GroupState.Downloading, summary.state)
        assertEquals(1, summary.downloadingCount)
        assertEquals(1, summary.failedCount)
        // 已完成与失败的不计入进度
        assertEquals(0.6f, summary.progress, 0.001f)

        assertEquals(GroupState.Paused, paused.appleSummary().state)
        assertEquals(GroupState.Idle, finished.appleSummary().state)
        assertEquals(GroupState.Idle, group(4, "Failed", item(4, 1, DownloadStatus.FAILED)).appleSummary().state)
    }

    @Test
    fun `status pills count and filter groups`() = runAniComposeUiTest {
        setPage()
        onNodeWithTag(AppleDownloadTestTags.status(null)).assertTextContains("3", substring = true)
        onNodeWithTag(AppleDownloadTestTags.status(DownloadStatusFilter.Downloading)).assertTextContains("2", substring = true)
        onNodeWithTag(AppleDownloadTestTags.status(DownloadStatusFilter.Finished)).assertTextContains("2", substring = true)

        onNodeWithTag(AppleDownloadTestTags.status(DownloadStatusFilter.Finished)).performClick()
        onNodeWithTag(AppleDownloadTestTags.group(paused.subjectId)).assertDoesNotExist()
        onNodeWithTag(AppleDownloadTestTags.group(finished.subjectId)).assertIsDisplayed()
    }

    @Test
    fun `row opens the subject and buttons pause or resume the group`() = runAniComposeUiTest {
        val opened = mutableListOf<Int>()
        val pausedItems = mutableListOf<DownloadItem>()
        val resumedItems = mutableListOf<DownloadItem>()
        setPage(onOpenSubject = { opened += it }, onPause = { pausedItems += it }, onResume = { resumedItems += it })

        onNodeWithText(finished.subjectName).performClick()
        assertEquals(listOf(finished.subjectId), opened)

        onNodeWithTag(AppleDownloadTestTags.pause(downloading.subjectId)).performClick()
        assertEquals(listOf(downloading.entries[1]), pausedItems)

        onNodeWithTag(AppleDownloadTestTags.resume(paused.subjectId)).performClick()
        assertEquals(paused.entries, resumedItems)
        onNodeWithTag(AppleDownloadTestTags.pause(finished.subjectId)).assertDoesNotExist()
    }

    @Test
    fun `long press selects the group and delete asks for confirmation`() = runAniComposeUiTest {
        val deleted = mutableListOf<DownloadItem>()
        setPage(onDelete = { deleted += it })

        onNodeWithText(finished.subjectName).performTouchInput { longClick() }
        onNodeWithTag(AppleDownloadTestTags.Toolbar).assertIsDisplayed()
        onNodeWithTag(AppleDownloadTestTags.group(finished.subjectId)).assertExists()

        onNodeWithTag(AppleDownloadTestTags.DeleteSelected).performClick()
        onNodeWithTag(DownloadManagementTestTags.DELETE_CONFIRM_BUTTON).performClick()
        assertEquals(finished.entries, deleted)

        onNodeWithTag(AppleDownloadTestTags.Done).performClick()
        onNodeWithTag(AppleDownloadTestTags.Toolbar).assertDoesNotExist()
    }

    @Test
    fun `select button enters selection and rows toggle`() = runAniComposeUiTest {
        setPage()
        onNodeWithTag(AppleDownloadTestTags.Select).performClick()
        onNodeWithTag(AppleDownloadTestTags.Toolbar).assertIsDisplayed()

        onNodeWithText(paused.subjectName).performClick()
        onNodeWithText(paused.subjectName).assertExists()
        onNodeWithTag(AppleDownloadTestTags.SelectAll).performClick()
        onNodeWithTag(AppleDownloadTestTags.Done).assertIsDisplayed()
    }

    @Test
    fun `filter button opens the filter sheet`() = runAniComposeUiTest {
        setPage()
        onNodeWithTag(AppleDownloadTestTags.FilterAndSort).performClick()
        waitForIdle()
        onNodeWithTag(AppleDownloadTestTags.FilterSheet).assertExists()
    }

    @Test
    fun `selected rows report selection`() = runAniComposeUiTest {
        setPage()
        onNodeWithText(finished.subjectName).performTouchInput { longClick() }
        onNodeWithTag(AppleDownloadTestTags.group(finished.subjectId))
            .onChildren()
            .filterToOne(hasClickAction())
            .assertIsSelected()
    }
}
