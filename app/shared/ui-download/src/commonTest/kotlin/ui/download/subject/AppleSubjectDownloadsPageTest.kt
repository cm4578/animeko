/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.download.subject

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.him188.ani.app.data.models.subject.SubjectAiringInfo
import me.him188.ani.app.data.models.subject.SubjectAiringKind
import me.him188.ani.app.ui.download.DownloadManagementTestTags
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.components.DownloadStatus
import me.him188.ani.app.ui.download.subject.TestSubjectDownloads.downloading
import me.him188.ani.app.ui.download.subject.TestSubjectDownloads.failed
import me.him188.ani.app.ui.download.subject.TestSubjectDownloads.finished1
import me.him188.ani.app.ui.download.subject.TestSubjectDownloads.finished2
import me.him188.ani.app.ui.download.subject.TestSubjectDownloads.paused
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.navigation.LocalOnBackPressedDispatcherOwner
import me.him188.ani.app.ui.foundation.navigation.OnBackPressedDispatcher
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_filter_collection_done
import me.him188.ani.app.ui.lang.cache_management_finished_count
import me.him188.ani.app.ui.mediafetch.createTestMediaSourceInfoProvider
import me.him188.ani.datasources.api.PackedDate
import me.him188.ani.utils.platform.Platform
import org.jetbrains.compose.resources.stringResource

/**
 * docs/design/ani-style/03-mobile-pages.md §4.3 iOS 单番快取页; 05 D2.
 */
@OptIn(ExperimentalTestApi::class)
class AppleSubjectDownloadsPageTest {
    private class Page(val recorder: RecordingSubjectDownloadActions) {
        val opened = mutableListOf<DownloadItem>()
        val played = mutableListOf<DownloadItem>()
        var watchedLabel = ""
        var finishedSummary = ""
        var dispatcher: OnBackPressedDispatcher? = null
    }

    private fun ComposeUiTest.setPage(state: SubjectDownloadsUiState = TestSubjectDownloads.state()): Page {
        val page = Page(RecordingSubjectDownloadActions())
        val sourceInfo = createTestMediaSourceInfoProvider()
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    page.watchedLabel = stringResource(Lang.cache_filter_collection_done)
                    page.finishedSummary = stringResource(Lang.cache_management_finished_count, 2, 6)
                    page.dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                    AppleSubjectDownloadsLayout(
                        state,
                        page.recorder.actions,
                        sourceInfo,
                        onPlay = { page.played += it },
                        onOpenDetail = { page.opened += it },
                        onBack = {},
                        Modifier.size(390.dp, 2400.dp),
                    )
                }
            }
        }
        waitForIdle()
        return page
    }

    /** 下载列可点击的节点 (测试标签在外层的 `Box`). */
    private fun ComposeUiTest.row(download: DownloadItem): SemanticsNodeInteraction =
        onNodeWithTag(AppleSubjectDownloadsTestTags.row(download.id)).onChildren().filterToOne(hasClickAction())

    private fun ComposeUiTest.top(tag: String) = onNodeWithTag(tag).getUnclippedBoundsInRoot().top

    @Test
    fun `groups list in progress first then finished with not downloaded episodes last`() = runAniComposeUiTest {
        setPage()
        val active = top(AppleSubjectDownloadsTestTags.ActiveTitle)
        val finishedTitle = top(AppleSubjectDownloadsTestTags.FinishedTitle)
        assertTrue(active < top(AppleSubjectDownloadsTestTags.row(downloading.id)))
        assertTrue(top(AppleSubjectDownloadsTestTags.row(failed.id)) < finishedTitle)
        assertTrue(finishedTitle < top(AppleSubjectDownloadsTestTags.row(finished1.id)))
        assertTrue(top(AppleSubjectDownloadsTestTags.row(finished2.id)) < top(AppleSubjectDownloadsTestTags.episode(6)))
        assertTextInside(AppleSubjectDownloadsTestTags.ActiveTitle, "3")
        assertTextInside(AppleSubjectDownloadsTestTags.FinishedTitle, "2")
    }

    @Test
    fun `header summarizes finished episodes and airing`() = runAniComposeUiTest {
        val page = setPage(
            TestSubjectDownloads.state().copy(
                airDate = PackedDate(2023, 10, 1),
                airingInfo = SubjectAiringInfo.EmptyCompleted.copy(kind = SubjectAiringKind.COMPLETED, mainEpisodeCount = 28),
            ),
        )
        assertTextInside(AppleSubjectDownloadsTestTags.Header, page.finishedSummary)
        assertTextInside(AppleSubjectDownloadsTestTags.Header, "2023")
        assertTextInside(AppleSubjectDownloadsTestTags.Header, "28")
    }

    @Test
    fun `finished rows show the watch status`() = runAniComposeUiTest {
        val page = setPage()
        row(finished1).assertTextContains(page.watchedLabel, substring = true)
    }

    @Test
    fun `row buttons pause resume and play`() = runAniComposeUiTest {
        val page = setPage()
        onNodeWithTag(AppleSubjectDownloadsTestTags.pause(downloading.id)).performClick()
        onNodeWithTag(AppleSubjectDownloadsTestTags.resume(paused.id)).performClick()
        onNodeWithTag(AppleSubjectDownloadsTestTags.play(finished1.id)).performClick()
        assertEquals(listOf(setOf(downloading.id)), page.recorder.paused)
        assertEquals(listOf(setOf(paused.id)), page.recorder.resumed)
        assertEquals(listOf(finished1), page.played)
        // 失败的下载没有按钮
        onNodeWithTag(AppleSubjectDownloadsTestTags.pause(failed.id)).assertDoesNotExist()
        onNodeWithTag(AppleSubjectDownloadsTestTags.resume(failed.id)).assertDoesNotExist()
    }

    @Test
    fun `pause all becomes resume all when nothing is downloading`() = runAniComposeUiTest {
        val withDownloading = setPage()
        onNodeWithTag(AppleSubjectDownloadsTestTags.PauseAll).performClick()
        assertEquals(1, withDownloading.recorder.pauseAll)
        onNodeWithTag(AppleSubjectDownloadsTestTags.ResumeAll).assertDoesNotExist()
    }

    @Test
    fun `resume all when only paused downloads are in progress`() = runAniComposeUiTest {
        val page = setPage(TestSubjectDownloads.state(listOf(finished1, paused)))
        onNodeWithTag(AppleSubjectDownloadsTestTags.PauseAll).assertDoesNotExist()
        onNodeWithTag(AppleSubjectDownloadsTestTags.ResumeAll).performClick()
        assertEquals(1, page.recorder.resumeAll)
    }

    @Test
    fun `row opens the download details`() = runAniComposeUiTest {
        val page = setPage()
        row(downloading).performClick()
        row(finished2).performClick()
        assertEquals(listOf(downloading, finished2), page.opened)
    }

    @Test
    fun `not downloaded episode starts a download`() = runAniComposeUiTest {
        val page = setPage()
        onNodeWithTag(AppleSubjectDownloadsTestTags.download(6)).performClick()
        assertEquals(listOf(6), page.recorder.downloaded)
    }

    @Test
    fun `episode download is disabled while another request is preparing`() = runAniComposeUiTest {
        setPage(TestSubjectDownloads.state(request = DownloadRequestUiState(episodeIds = setOf(99), busy = true)))
        onNodeWithTag(AppleSubjectDownloadsTestTags.download(6)).assertIsNotEnabled()
    }

    @Test
    fun `long press enters selection and hides row actions`() = runAniComposeUiTest {
        val page = setPage()
        row(finished2).performTouchInput { longClick() }
        waitForIdle()
        onNodeWithTag(AppleSubjectDownloadsTestTags.Toolbar).assertIsDisplayed()
        row(finished2).assertIsSelected()
        onNodeWithTag(AppleSubjectDownloadsTestTags.PauseAll).assertDoesNotExist()
        onNodeWithTag(AppleSubjectDownloadsTestTags.play(finished1.id)).assertDoesNotExist()
        // 尚未下载的剧集不能选, 也不显示下载按钮
        onNodeWithTag(AppleSubjectDownloadsTestTags.download(6)).assertDoesNotExist()
        assertEquals(emptyList(), page.opened)
    }

    @Test
    fun `selection toolbar acts on the selected downloads and delete asks first`() = runAniComposeUiTest {
        val page = setPage()
        onNodeWithTag(AppleSubjectDownloadsTestTags.Select).performClick()
        onNodeWithTag(AppleSubjectDownloadsTestTags.DeleteSelected).assertIsNotEnabled()
        row(downloading).performClick()
        row(paused).performClick()
        row(downloading).assertIsSelected()
        row(finished1).assertIsNotSelected()
        assertEquals(emptyList(), page.opened)

        onNodeWithTag(AppleSubjectDownloadsTestTags.PauseSelected).performClick()
        onNodeWithTag(AppleSubjectDownloadsTestTags.ResumeSelected).performClick()
        assertEquals(listOf(setOf(downloading.id, paused.id)), page.recorder.paused)
        assertEquals(listOf(setOf(downloading.id, paused.id)), page.recorder.resumed)

        onNodeWithTag(AppleSubjectDownloadsTestTags.DeleteSelected).performClick()
        assertEquals(emptyList(), page.recorder.deleted)
        onNodeWithTag(DownloadManagementTestTags.DELETE_CONFIRM_BUTTON).performClick()
        assertEquals(listOf(setOf(downloading.id, paused.id)), page.recorder.deleted)
    }

    @Test
    fun `select all toggles every download and done leaves selection`() = runAniComposeUiTest {
        setPage()
        onNodeWithTag(AppleSubjectDownloadsTestTags.Select).performClick()
        onNodeWithTag(AppleSubjectDownloadsTestTags.SelectAll).performClick()
        TestSubjectDownloads.downloads.forEach { row(it).assertIsSelected() }
        onNodeWithTag(AppleSubjectDownloadsTestTags.SelectAll).performClick()
        TestSubjectDownloads.downloads.forEach { row(it).assertIsNotSelected() }

        onNodeWithTag(AppleSubjectDownloadsTestTags.Done).performClick()
        onNodeWithTag(AppleSubjectDownloadsTestTags.Toolbar).assertDoesNotExist()
        onNodeWithTag(AppleSubjectDownloadsTestTags.Select).assertIsDisplayed()
    }

    @Test
    fun `back leaves selection first`() = runAniComposeUiTest {
        val page = setPage()
        onNodeWithTag(AppleSubjectDownloadsTestTags.Select).performClick()
        onNodeWithTag(AppleSubjectDownloadsTestTags.Done).assertIsDisplayed()
        runOnIdle { page.dispatcher!!.onBackPressed() }
        waitForIdle()
        onNodeWithTag(AppleSubjectDownloadsTestTags.Done).assertDoesNotExist()
        onNodeWithTag(AppleSubjectDownloadsTestTags.Select).assertIsDisplayed()
    }

    @Test
    fun `select is disabled without downloads`() = runAniComposeUiTest {
        setPage(TestSubjectDownloads.state(emptyList()))
        onNodeWithTag(AppleSubjectDownloadsTestTags.Select).assertIsNotEnabled()
        onNodeWithTag(AppleSubjectDownloadsTestTags.episode(6)).assertIsDisplayed()
    }

    @Test
    fun `summary counts only downloading items and the known total size`() {
        val summary = TestSubjectDownloads.state().appleSummary()
        assertEquals(2, summary.finishedEpisodes)
        assertEquals(6, summary.totalEpisodes)
        assertEquals(1, summary.downloadingCount)
        assertEquals(TestSubjectDownloads.downloads.sumOf { it.totalSize.inBytes }, summary.totalSize?.inBytes)

        val sections = TestSubjectDownloads.state().appleSections()
        assertEquals(listOf(downloading, paused, failed), sections.active)
        assertEquals(listOf(finished1, finished2), sections.finished)
        assertEquals(listOf(6), sections.notDownloaded.map { it.episodeId })
        assertEquals(AppleBulkAction.PauseAll, sections.active.appleBulkAction())
        assertEquals(null, listOf(failed).appleBulkAction())
        assertEquals(DownloadStatus.FAILED, failed.status)
    }
}
