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
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasClickAction
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
import kotlinx.io.files.Path
import me.him188.ani.app.ui.download.DownloadManagementTestTags
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.details.MacMediaCacheDetailsTestTags
import me.him188.ani.app.ui.download.details.MediaCacheDetailsScreenState
import me.him188.ani.app.ui.download.details.TestMediaDetails
import me.him188.ani.app.ui.download.subject.TestSubjectDownloads.downloading
import me.him188.ani.app.ui.download.subject.TestSubjectDownloads.finished1
import me.him188.ani.app.ui.download.subject.TestSubjectDownloads.paused
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.navigation.LocalOnBackPressedDispatcherOwner
import me.him188.ani.app.ui.foundation.navigation.OnBackPressedDispatcher
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_episode_download_failed
import me.him188.ani.app.ui.lang.cache_table_speed
import me.him188.ani.app.ui.mediafetch.createTestMediaSourceInfoProvider
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.annotations.TestOnly
import org.jetbrains.compose.resources.stringResource

/**
 * docs/design/ani-style/04-macos.md §4.3 macOS 单番快取页与检阅器; 05 C2 (⌘I)、C3、D2、G9、G28.
 */
@OptIn(ExperimentalTestApi::class, TestOnly::class)
class MacSubjectDownloadsPageTest {
    private class Page(val recorder: RecordingSubjectDownloadActions) {
        val played = mutableListOf<DownloadItem>()
        val revealed = mutableListOf<DownloadItem>()
        val revealedFiles = mutableListOf<Path>()
        val detailsRequested = mutableListOf<String>()
        var openedSubject = 0
        var speedLabel = ""
        var failedLabel = ""
        var dispatcher: OnBackPressedDispatcher? = null
    }

    private fun ComposeUiTest.setPage(state: SubjectDownloadsUiState = TestSubjectDownloads.state()): Page {
        val page = Page(RecordingSubjectDownloadActions())
        val sourceInfo = createTestMediaSourceInfoProvider()
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    page.speedLabel = stringResource(Lang.cache_table_speed)
                    page.failedLabel = stringResource(Lang.cache_episode_download_failed)
                    page.dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                    MacSubjectDownloadsLayout(
                        state,
                        page.recorder.actions,
                        sourceInfo,
                        onPlay = { page.played += it },
                        onOpenSubjectDetails = { page.openedSubject++ },
                        onRevealDownload = { page.revealed += it },
                        onRevealFile = { page.revealedFiles += it },
                        detailsState = { cacheId ->
                            page.detailsRequested += cacheId
                            MediaCacheDetailsScreenState(TestMediaDetails)
                        },
                        Modifier.size(1240.dp, 1000.dp),
                    )
                }
            }
        }
        waitForIdle()
        return page
    }

    private fun ComposeUiTest.row(download: DownloadItem) = onNodeWithTag(MacSubjectDownloadsTestTags.row(download.id))

    /** 行的选中状态在测试标签节点的子节点 (表格行) 上. */
    private fun SemanticsNodeInteraction.assertIsSelectedRow() = onChildren().filterToOne(isSelected()).assertExists()
    private fun SemanticsNodeInteraction.assertIsNotSelectedRow() = onChildren().filterToOne(isSelected()).assertDoesNotExist()

    private fun ComposeUiTest.percent() = onNodeWithTag(MacMediaCacheDetailsTestTags.Percent)

    @Test
    fun `click selects a row and double click plays it`() = runAniComposeUiTest {
        val page = setPage()
        row(finished1).performClick()
        waitForIdle()
        row(finished1).assertIsSelectedRow()
        row(downloading).assertIsNotSelectedRow()
        assertEquals(emptyList(), page.played)

        row(downloading).performTouchInput { doubleClick() }
        waitForIdle()
        assertEquals(listOf(downloading), page.played)
    }

    @Test
    fun `inspector toggle shows the selected episode and hides speed and size`() = runAniComposeUiTest {
        val page = setPage()
        onNodeWithText(page.speedLabel).assertIsDisplayed()
        row(downloading).performClick()
        onNodeWithTag(MacSubjectDownloadsTestTags.InspectorToggle).performClick()
        waitForIdle()
        onNodeWithTag(MacMediaCacheDetailsTestTags.Inspector).assertIsDisplayed()
        percent().assertTextEquals("62%")
        onNodeWithText(page.speedLabel).assertDoesNotExist()

        // 检阅器跟着单击选中的一集
        row(finished1).performClick()
        waitForIdle()
        percent().assertTextEquals("100%")
        assertEquals(finished1.id, page.detailsRequested.last())

        onNodeWithTag(MacSubjectDownloadsTestTags.InspectorToggle).performClick()
        waitForIdle()
        onNodeWithTag(MacMediaCacheDetailsTestTags.Inspector).assertDoesNotExist()
        onNodeWithText(page.speedLabel).assertIsDisplayed()
    }

    @Test
    fun `inspector opens on the first download when nothing is selected`() = runAniComposeUiTest {
        setPage()
        onNodeWithTag(MacSubjectDownloadsTestTags.InspectorToggle).performClick()
        waitForIdle()
        // 进行中一组在前, 第 3 集是第一个下载
        percent().assertTextEquals("62%")
        row(downloading).assertIsSelectedRow()
    }

    @Test
    fun `inspector buttons play and pause the episode`() = runAniComposeUiTest {
        val page = setPage()
        row(downloading).performClick()
        onNodeWithTag(MacSubjectDownloadsTestTags.InspectorToggle).performClick()
        waitForIdle()
        onNodeWithTag(MacMediaCacheDetailsTestTags.Play).performClick()
        onNodeWithTag(MacMediaCacheDetailsTestTags.PauseOrResume).performClick()
        assertEquals(listOf(downloading), page.played)
        assertEquals(listOf(setOf(downloading.id)), page.recorder.paused)

        onNodeWithTag(MacMediaCacheDetailsTestTags.Delete).performClick()
        onNodeWithTag(DownloadManagementTestTags.DELETE_CONFIRM_BUTTON).performClick()
        assertEquals(listOf(setOf(downloading.id)), page.recorder.deleted)
    }

    @Test
    fun `inspector reveals the local file`() = runAniComposeUiTest {
        val details = TestMediaDetails.copy(localCacheFilePath = Path("/Users/test/Ani/episode.mkv"))
        val page = Page(RecordingSubjectDownloadActions())
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    MacSubjectDownloadsLayout(
                        TestSubjectDownloads.state(),
                        page.recorder.actions,
                        createTestMediaSourceInfoProvider(),
                        onPlay = {},
                        onOpenSubjectDetails = {},
                        onRevealDownload = {},
                        onRevealFile = { page.revealedFiles += it },
                        detailsState = { MediaCacheDetailsScreenState(details) },
                        Modifier.size(1240.dp, 1000.dp),
                    )
                }
            }
        }
        onNodeWithTag(MacSubjectDownloadsTestTags.InspectorToggle).performClick()
        waitForIdle()
        onNodeWithTag(MacMediaCacheDetailsTestTags.RevealFile).performClick()
        assertEquals(listOf(Path("/Users/test/Ani/episode.mkv")), page.revealedFiles)
    }

    @Test
    fun `right click menu opens details reveals in Finder and deletes after confirmation`() = runAniComposeUiTest {
        val page = setPage()
        row(finished1).performMouseInput { rightClick(center) }
        onNodeWithTag(MacSubjectDownloadsTestTags.Menu).assertIsDisplayed()
        onNodeWithTag(MacSubjectDownloadsTestTags.MenuReveal).performClick()
        waitForIdle()
        assertEquals(listOf(finished1), page.revealed)
        onNodeWithTag(MacSubjectDownloadsTestTags.Menu).assertDoesNotExist()

        row(finished1).performMouseInput { rightClick(center) }
        onNodeWithTag(MacSubjectDownloadsTestTags.MenuDetails).performClick()
        waitForIdle()
        percent().assertTextEquals("100%")

        row(paused).performMouseInput { rightClick(center) }
        onNodeWithTag(MacSubjectDownloadsTestTags.MenuDelete).performClick()
        waitForIdle()
        assertEquals(emptyList(), page.recorder.deleted)
        onNodeWithTag(DownloadManagementTestTags.DELETE_CONFIRM_BUTTON).performClick()
        assertEquals(listOf(setOf(paused.id)), page.recorder.deleted)
    }

    @Test
    fun `toolbar opens the subject and pauses all`() = runAniComposeUiTest {
        val page = setPage()
        onNodeWithTag(MacSubjectDownloadsTestTags.OpenSubject).performClick()
        onNodeWithTag(MacSubjectDownloadsTestTags.PauseAll).performClick()
        onNodeWithTag(MacSubjectDownloadsTestTags.GroupPauseAll).performClick()
        assertEquals(1, page.openedSubject)
        assertEquals(2, page.recorder.pauseAll)
    }

    @Test
    fun `group action shares the center line of the action column`() = runAniComposeUiTest {
        setPage()
        val group = onNodeWithTag(MacSubjectDownloadsTestTags.GroupPauseAll).getUnclippedBoundsInRoot()
        val button = onNodeWithTag(MacSubjectDownloadsTestTags.pause(downloading.id)).getUnclippedBoundsInRoot()
        assertEquals((button.left + button.right).value / 2, (group.left + group.right).value / 2, 0.5f)
    }

    @Test
    fun `selection mode toggles rows keeps the inspector and disables the menu`() = runAniComposeUiTest {
        val page = setPage()
        row(finished1).performClick()
        onNodeWithTag(MacSubjectDownloadsTestTags.InspectorToggle).performClick()
        onNodeWithTag(MacSubjectDownloadsTestTags.Select).performClick()
        waitForIdle()
        onNodeWithTag(MacSubjectDownloadsTestTags.DeleteSelected).assertIsNotEnabled()
        row(finished1).assertIsNotSelectedRow()

        row(downloading).performClick()
        waitForIdle()
        row(downloading).assertIsSelectedRow()
        // 检阅器显示最后点到的一集
        percent().assertTextEquals("62%")
        onNodeWithTag(MacSubjectDownloadsTestTags.DeleteSelected).assertIsEnabled()

        // 选择模式下双击只切换选取, 不播放; 右键不打开菜单
        row(paused).performTouchInput { doubleClick() }
        waitForIdle()
        assertEquals(emptyList(), page.played)
        row(paused).performMouseInput { rightClick(center) }
        onNodeWithTag(MacSubjectDownloadsTestTags.Menu).assertDoesNotExist()

        onNodeWithTag(MacSubjectDownloadsTestTags.DeleteSelected).performClick()
        onNodeWithTag(DownloadManagementTestTags.DELETE_CONFIRM_BUTTON).performClick()
        assertEquals(listOf(setOf(downloading.id)), page.recorder.deleted)
    }

    @Test
    fun `select all and escape leave selection`() = runAniComposeUiTest {
        val page = setPage()
        onNodeWithTag(MacSubjectDownloadsTestTags.Select).performClick()
        onNodeWithTag(MacSubjectDownloadsTestTags.SelectAll).performClick()
        waitForIdle()
        TestSubjectDownloads.downloads.forEach { row(it).assertIsSelectedRow() }

        runOnIdle { page.dispatcher!!.onBackPressed() }
        waitForIdle()
        onNodeWithTag(MacSubjectDownloadsTestTags.Done).assertDoesNotExist()
        onNodeWithTag(MacSubjectDownloadsTestTags.Select).assertIsDisplayed()
    }

    @Test
    fun `not downloaded episodes keep the download action`() = runAniComposeUiTest {
        val page = setPage()
        onNodeWithTag(MacSubjectDownloadsTestTags.download(6)).performClick()
        assertEquals(listOf(6), page.recorder.downloaded)
        onNodeWithTag(MacSubjectDownloadsTestTags.Hint).assertIsDisplayed()
    }

    @Test
    fun `failed downloads show the error status and no action`() = runAniComposeUiTest {
        val page = setPage()
        onNodeWithTag(MacSubjectDownloadsTestTags.row(TestSubjectDownloads.failed.id))
            .onChildren().filterToOne(hasClickAction())
            .assertTextContains(page.failedLabel, substring = true)
        onNodeWithTag(MacSubjectDownloadsTestTags.pause(TestSubjectDownloads.failed.id)).assertDoesNotExist()
        onNodeWithTag(MacSubjectDownloadsTestTags.resume(TestSubjectDownloads.failed.id)).assertDoesNotExist()
    }
}
