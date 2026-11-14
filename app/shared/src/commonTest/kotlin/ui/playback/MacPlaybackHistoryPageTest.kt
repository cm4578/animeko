/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.playback

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.navigation.LocalOnBackPressedDispatcherOwner
import me.him188.ani.app.ui.foundation.navigation.OnBackPressedDispatcher
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_subject_delete
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MacPlaybackHistoryPageTest {
    private class Callbacks {
        val opened = mutableListOf<Int>()
        var syncStatusOpened = 0
        var syncs = 0
        val deleted = mutableListOf<Set<Int>>()
        var confirmText = ""
        var dispatcher: OnBackPressedDispatcher? = null
    }

    private val pendingItems = listOf(
        PlaybackHistorySyncStatusUiItem(
            episodeId = 1,
            operationNames = listOf("Update progress"),
            subjectName = "Today Show",
            episodeName = "Episode 1",
            versionMillis = 0,
            playbackOpId = 10,
        ),
        PlaybackHistorySyncStatusUiItem(
            episodeId = 99,
            operationNames = listOf("Mark as watched"),
            subjectName = null,
            episodeName = null,
            versionMillis = 0,
            collectionOpId = 20,
        ),
    )

    private fun ComposeUiTest.setPage(
        callbacks: Callbacks = Callbacks(),
        width: Dp = 1040.dp,
        isSyncing: Boolean = false,
        histories: List<PlaybackHistoryUiItem> = TestPlaybackHistories,
    ): Callbacks {
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    callbacks.confirmText = stringResource(Lang.cache_subject_delete)
                    callbacks.dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                    MacPlaybackHistoryLayout(
                        histories,
                        pendingItems = pendingItems,
                        isSyncing = isSyncing,
                        onSync = { callbacks.syncs++ },
                        onOpenHistory = { callbacks.opened += it.episodeId },
                        onOpenSyncStatus = { callbacks.syncStatusOpened++ },
                        onDelete = { callbacks.deleted += it.toSet() },
                        Modifier.size(width, 720.dp),
                        timeZone = TimeZone.UTC,
                        clock = TestPlaybackHistoryClock,
                    )
                }
            }
        }
        waitForIdle()
        return callbacks
    }

    @Test
    fun `sync card lists the pending queue beside the records`() = runAniComposeUiTest {
        val callbacks = setPage()
        onNodeWithTag(MacPlaybackHistoryTestTags.queueItem(1)).assertIsDisplayed()
        onNodeWithTag(MacPlaybackHistoryTestTags.queueItem(99)).assertIsDisplayed()
        onNodeWithTag(MacPlaybackHistoryTestTags.Hint).assertIsDisplayed()
        onNodeWithTag(MacPlaybackHistoryTestTags.pending(1), useUnmergedTree = true).assertExists()
        onNodeWithTag(MacPlaybackHistoryTestTags.pending(2), useUnmergedTree = true).assertDoesNotExist()

        // 右栏在记录右侧, 宽 296
        val card = onNodeWithTag(MacPlaybackHistoryTestTags.SyncCard).getUnclippedBoundsInRoot()
        val row = onNodeWithTag(MacPlaybackHistoryTestTags.row(1)).getUnclippedBoundsInRoot()
        assertTrue(card.left > row.right, "card should be right of the rows: $card, $row")
        assertEquals(296f, (card.right - card.left).value, 0.5f)

        onNodeWithTag(MacPlaybackHistoryTestTags.SyncNow).performClick()
        assertEquals(1, callbacks.syncs)
        onNodeWithTag(MacPlaybackHistoryTestTags.SyncStatus).performClick()
        assertEquals(1, callbacks.syncStatusOpened)
    }

    @Test
    fun `sync now is disabled while syncing`() = runAniComposeUiTest {
        setPage(isSyncing = true)
        onNodeWithTag(MacPlaybackHistoryTestTags.SyncNow).assertIsNotEnabled()
        onNodeWithTag(MacPlaybackHistoryTestTags.SyncStatus).assertIsEnabled()
    }

    @Test
    fun `narrow layout puts the sync card above the records`() = runAniComposeUiTest {
        setPage(width = 760.dp)
        val card = onNodeWithTag(MacPlaybackHistoryTestTags.SyncCard).getUnclippedBoundsInRoot()
        val day = onNodeWithTag(MacPlaybackHistoryTestTags.day(0)).getUnclippedBoundsInRoot()
        assertTrue(card.bottom < day.top, "card should be above the first day: $card, $day")
    }

    @Test
    fun `row click and play button open the episode`() = runAniComposeUiTest {
        val callbacks = setPage()
        onNodeWithTag(MacPlaybackHistoryTestTags.row(1)).performClick()
        onNodeWithTag(MacPlaybackHistoryTestTags.play(2)).performClick()
        assertEquals(listOf(1, 2), callbacks.opened)
        onNodeWithTag(MacPlaybackHistoryTestTags.row(1)).assertIsNotSelected()
    }

    @Test
    fun `selection toggles rows and deletes after confirmation`() = runAniComposeUiTest {
        val callbacks = setPage()
        onNodeWithTag(MacPlaybackHistoryTestTags.Select).performClick()
        onNodeWithTag(MacPlaybackHistoryTestTags.DeleteSelected).assertIsNotEnabled()
        onNodeWithTag(MacPlaybackHistoryTestTags.play(1)).assertDoesNotExist()
        // 同步卡照常可用
        onNodeWithTag(MacPlaybackHistoryTestTags.SyncNow).assertIsEnabled()

        onNodeWithTag(MacPlaybackHistoryTestTags.row(1)).performClick()
        onNodeWithTag(MacPlaybackHistoryTestTags.row(3)).performClick()
        onNodeWithTag(MacPlaybackHistoryTestTags.row(1)).assertIsSelected()
        onNodeWithTag(MacPlaybackHistoryTestTags.row(2)).assertIsNotSelected()
        assertEquals(emptyList(), callbacks.opened)

        onNodeWithTag(MacPlaybackHistoryTestTags.DeleteSelected).assertIsEnabled().performClick()
        onNodeWithText(callbacks.confirmText).performClick()
        assertEquals(listOf(setOf(1, 3)), callbacks.deleted)
        onNodeWithTag(MacPlaybackHistoryTestTags.Done).assertDoesNotExist()
        onNodeWithTag(MacPlaybackHistoryTestTags.Select).assertIsDisplayed()
    }

    @Test
    fun `select all and done`() = runAniComposeUiTest {
        setPage()
        onNodeWithTag(MacPlaybackHistoryTestTags.Select).performClick()
        onNodeWithTag(MacPlaybackHistoryTestTags.SelectAll).performClick()
        TestPlaybackHistories.forEach { onNodeWithTag(MacPlaybackHistoryTestTags.row(it.episodeId)).assertIsSelected() }
        onNodeWithTag(MacPlaybackHistoryTestTags.SelectAll).performClick()
        TestPlaybackHistories.forEach { onNodeWithTag(MacPlaybackHistoryTestTags.row(it.episodeId)).assertIsNotSelected() }

        onNodeWithTag(MacPlaybackHistoryTestTags.Done).performClick()
        onNodeWithTag(MacPlaybackHistoryTestTags.Select).assertIsDisplayed()
        onNodeWithTag(MacPlaybackHistoryTestTags.play(1)).assertExists()
    }

    @Test
    fun `escape leaves selection and then closes nothing else`() = runAniComposeUiTest {
        val callbacks = setPage()
        onNodeWithTag(MacPlaybackHistoryTestTags.Select).performClick()
        onNodeWithTag(MacPlaybackHistoryTestTags.Done).assertIsDisplayed()

        runOnIdle { callbacks.dispatcher!!.onBackPressed() }
        waitForIdle()
        onNodeWithTag(MacPlaybackHistoryTestTags.Done).assertDoesNotExist()
        onNodeWithTag(MacPlaybackHistoryTestTags.Select).assertIsDisplayed()
    }

    @Test
    fun `select is disabled without records`() = runAniComposeUiTest {
        setPage(histories = emptyList())
        onNodeWithTag(MacPlaybackHistoryTestTags.Select).assertIsNotEnabled()
        onNodeWithTag(MacPlaybackHistoryTestTags.SyncCard).assertIsDisplayed()
    }
}
