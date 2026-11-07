/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.playback

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.navigation.LocalOnBackPressedDispatcherOwner
import me.him188.ani.app.ui.foundation.navigation.OnBackPressedDispatcher
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_subject_delete
import me.him188.ani.app.ui.lang.exploration_schedule_weekday_tuesday
import me.him188.ani.app.ui.lang.exploration_schedule_weekday_wednesday
import me.him188.ani.app.ui.lang.playback_history_empty
import me.him188.ani.app.ui.lang.playback_history_group_date
import me.him188.ani.app.ui.lang.playback_history_group_date_with_year
import me.him188.ani.app.ui.lang.playback_history_sync_status_pending
import me.him188.ani.app.ui.lang.playback_history_yesterday
import me.him188.ani.app.ui.lang.schedule_today
import me.him188.ani.utils.platform.Platform
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock
import kotlin.time.Instant

private val TestTimeZone = TimeZone.UTC

private fun millisAt(year: Int, month: Int, day: Int, hour: Int, minute: Int): Long =
    LocalDateTime(year, month, day, hour, minute).toInstant(TestTimeZone).toEpochMilliseconds()

/** 2026-11-07 (週六) 12:00 UTC. */
internal val TestPlaybackHistoryClock = object : Clock {
    override fun now(): Instant = Instant.fromEpochMilliseconds(millisAt(2026, 11, 7, 12, 0))
}

internal fun testPlaybackHistoryItem(
    episodeId: Int,
    subjectName: String,
    updatedAtMillis: Long,
    episodeSort: Float? = 9f,
    positionMillis: Long = 372_000,
    durationMillis: Long? = 1_420_000,
    isFinished: Boolean = false,
) = PlaybackHistoryUiItem(
    episodeId = episodeId,
    subjectId = 100 + episodeId,
    episodeSort = episodeSort,
    subjectName = subjectName,
    subjectImageUrl = null,
    episodeName = "Episode $episodeId",
    positionMillis = positionMillis,
    durationMillis = durationMillis,
    updatedAtMillis = updatedAtMillis,
    isFinished = isFinished,
)

/** 今天、昨天、10 月 27 日各一笔, 新的在前. */
internal val TestPlaybackHistories = listOf(
    testPlaybackHistoryItem(1, "Today Show", millisAt(2026, 11, 7, 9, 5)),
    testPlaybackHistoryItem(2, "Yesterday Show", millisAt(2026, 11, 6, 21, 40), positionMillis = 1_418_000, isFinished = true),
    testPlaybackHistoryItem(3, "October Show", millisAt(2026, 10, 27, 8, 0), episodeSort = null, durationMillis = null),
)

class ApplePlaybackHistoryPageTest {
    private class Callbacks {
        val opened = mutableListOf<Int>()
        var syncStatusOpened = 0
        var syncs = 0
        val deleted = mutableListOf<Collection<Int>>()
    }

    private fun ComposeUiTest.setPage(
        histories: List<PlaybackHistoryUiItem> = TestPlaybackHistories,
        pendingEpisodeIds: Set<Int> = setOf(1),
        callbacks: Callbacks = Callbacks(),
    ): OnBackPressedDispatcher {
        var dispatcher: OnBackPressedDispatcher? = null
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                    ApplePlaybackHistoryLayout(
                        histories,
                        pendingEpisodeIds = pendingEpisodeIds,
                        isSyncing = false,
                        onSync = { callbacks.syncs++ },
                        onOpenHistory = { callbacks.opened += it.episodeId },
                        onOpenSyncStatus = { callbacks.syncStatusOpened++ },
                        onDelete = { callbacks.deleted += it.toSet() },
                        onBack = {},
                        Modifier.width(390.dp),
                        timeZone = TestTimeZone,
                        clock = TestPlaybackHistoryClock,
                    )
                }
            }
        }
        waitForIdle()
        return dispatcher!!
    }

    @Test
    fun `records are grouped by local day in order`() {
        val groups = groupPlaybackHistoryByDay(TestPlaybackHistories, TestTimeZone)
        assertEquals(
            listOf(LocalDate(2026, 11, 7), LocalDate(2026, 11, 6), LocalDate(2026, 10, 27)),
            groups.map { it.date },
        )
        assertEquals(listOf(listOf(1), listOf(2), listOf(3)), groups.map { group -> group.items.map { it.episodeId } })

        // 同一笔记录在 UTC+8 已是隔天
        val shifted = groupPlaybackHistoryByDay(TestPlaybackHistories.take(2), TimeZone.of("Asia/Taipei"))
        assertEquals(listOf(LocalDate(2026, 11, 7)), shifted.map { it.date })
    }

    @Test
    fun `times and positions are padded`() {
        assertEquals("09:05", formatPlaybackHistoryTime(millisAt(2026, 11, 7, 9, 5), TestTimeZone))
        assertEquals("06:12", formatPlaybackPosition(372_000))
        assertEquals("00:00", formatPlaybackPosition(0))
        assertEquals("1:02:03", formatPlaybackPosition(3_723_000))
        assertEquals(0.5f, testPlaybackHistoryItem(1, "A", 0, positionMillis = 500, durationMillis = 1000).progress)
        assertEquals(null, testPlaybackHistoryItem(1, "A", 0, durationMillis = null).progress)
    }

    @Test
    fun `day labels use today yesterday weekday and year`() = runAniComposeUiTest {
        val today = LocalDate(2026, 11, 7)
        val expected = mutableListOf<String>()
        setContent {
            ProvideCompositionLocalsForPreview {
                val tuesday = stringResource(Lang.exploration_schedule_weekday_tuesday)
                val wednesday = stringResource(Lang.exploration_schedule_weekday_wednesday)
                expected.clear()
                expected += stringResource(Lang.schedule_today)
                expected += stringResource(Lang.playback_history_yesterday)
                expected += stringResource(Lang.playback_history_group_date, 10, 27, tuesday)
                expected += stringResource(Lang.playback_history_group_date_with_year, 2025, 12, 31, wednesday)
                Text(playbackHistoryDayLabel(today, today), Modifier.testTag("today"))
                Text(playbackHistoryDayLabel(LocalDate(2026, 11, 6), today), Modifier.testTag("yesterday"))
                Text(playbackHistoryDayLabel(LocalDate(2026, 10, 27), today), Modifier.testTag("this-year"))
                Text(playbackHistoryDayLabel(LocalDate(2025, 12, 31), today), Modifier.testTag("last-year"))
            }
        }
        onNodeWithTag("today").assertTextEquals(expected[0])
        onNodeWithTag("yesterday").assertTextEquals(expected[1])
        onNodeWithTag("this-year").assertTextEquals(expected[2])
        onNodeWithTag("last-year").assertTextEquals(expected[3])
    }

    @Test
    fun `rows open the episode and the sync row opens sync status`() = runAniComposeUiTest {
        val callbacks = Callbacks()
        setPage(callbacks = callbacks)
        onNodeWithTag(ApplePlaybackHistoryTestTags.day(0)).assertIsDisplayed()
        onNodeWithTag(ApplePlaybackHistoryTestTags.day(2)).assertIsDisplayed()

        onNodeWithTag(ApplePlaybackHistoryTestTags.row(2)).performClick()
        assertEquals(listOf(2), callbacks.opened)

        onNodeWithTag(ApplePlaybackHistoryTestTags.SyncRow).performClick()
        assertEquals(1, callbacks.syncStatusOpened)
    }

    @Test
    fun `pending records are marked and counted`() = runAniComposeUiTest {
        var pendingText = ""
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    pendingText = stringResource(Lang.playback_history_sync_status_pending, 2)
                    ApplePlaybackHistoryLayout(
                        TestPlaybackHistories,
                        pendingEpisodeIds = setOf(1, 99),
                        isSyncing = false,
                        onSync = {},
                        onOpenHistory = {},
                        onOpenSyncStatus = {},
                        onDelete = {},
                        onBack = {},
                        Modifier.width(390.dp),
                        timeZone = TestTimeZone,
                        clock = TestPlaybackHistoryClock,
                    )
                }
            }
        }
        onNodeWithTag(ApplePlaybackHistoryTestTags.pending(1), useUnmergedTree = true).assertExists()
        onNodeWithTag(ApplePlaybackHistoryTestTags.pending(2), useUnmergedTree = true).assertDoesNotExist()
        onNodeWithTag(ApplePlaybackHistoryTestTags.SyncRow).assertTextContains(pendingText, substring = true)
    }

    @Test
    fun `select button enters selection and delete asks for confirmation`() = runAniComposeUiTest {
        val callbacks = Callbacks()
        var confirmText = ""
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    confirmText = stringResource(Lang.cache_subject_delete)
                    ApplePlaybackHistoryLayout(
                        TestPlaybackHistories,
                        pendingEpisodeIds = emptySet(),
                        isSyncing = false,
                        onSync = {},
                        onOpenHistory = { callbacks.opened += it.episodeId },
                        onOpenSyncStatus = { callbacks.syncStatusOpened++ },
                        onDelete = { callbacks.deleted += it.toSet() },
                        onBack = {},
                        Modifier.width(390.dp),
                        timeZone = TestTimeZone,
                        clock = TestPlaybackHistoryClock,
                    )
                }
            }
        }
        onNodeWithTag(ApplePlaybackHistoryTestTags.Select).performClick()
        onNodeWithTag(ApplePlaybackHistoryTestTags.Toolbar).assertIsDisplayed()
        onNodeWithTag(ApplePlaybackHistoryTestTags.DeleteSelected).assertIsNotEnabled()

        // 选择模式下同步状态列停用, 点列只切换选取
        onNodeWithTag(ApplePlaybackHistoryTestTags.SyncRow).assertIsNotEnabled()
        onNodeWithTag(ApplePlaybackHistoryTestTags.row(1)).performClick()
        onNodeWithTag(ApplePlaybackHistoryTestTags.row(1)).assertIsSelected()
        assertEquals(emptyList(), callbacks.opened)

        onNodeWithTag(ApplePlaybackHistoryTestTags.DeleteSelected).assertIsEnabled().performClick()
        onNodeWithText(confirmText).performClick()
        assertEquals(listOf(setOf(1)), callbacks.deleted.map { it.toSet() })
        onNodeWithTag(ApplePlaybackHistoryTestTags.Toolbar).assertDoesNotExist()
        onNodeWithTag(ApplePlaybackHistoryTestTags.Select).assertIsDisplayed()
    }

    @Test
    fun `long press selects the row and select all toggles every row`() = runAniComposeUiTest {
        setPage()
        onNodeWithTag(ApplePlaybackHistoryTestTags.row(2)).performTouchInput { longClick() }
        onNodeWithTag(ApplePlaybackHistoryTestTags.row(2)).assertIsSelected()
        onNodeWithTag(ApplePlaybackHistoryTestTags.row(1)).assertIsNotSelected()

        onNodeWithTag(ApplePlaybackHistoryTestTags.SelectAll).performClick()
        TestPlaybackHistories.forEach { onNodeWithTag(ApplePlaybackHistoryTestTags.row(it.episodeId)).assertIsSelected() }

        onNodeWithTag(ApplePlaybackHistoryTestTags.SelectAll).performClick()
        TestPlaybackHistories.forEach { onNodeWithTag(ApplePlaybackHistoryTestTags.row(it.episodeId)).assertIsNotSelected() }
        onNodeWithTag(ApplePlaybackHistoryTestTags.Toolbar).assertIsDisplayed()

        onNodeWithTag(ApplePlaybackHistoryTestTags.Done).performClick()
        onNodeWithTag(ApplePlaybackHistoryTestTags.Toolbar).assertDoesNotExist()
    }

    @Test
    fun `back leaves selection first`() = runAniComposeUiTest {
        val dispatcher = setPage()
        onNodeWithTag(ApplePlaybackHistoryTestTags.Select).performClick()
        onNodeWithTag(ApplePlaybackHistoryTestTags.Toolbar).assertIsDisplayed()

        runOnIdle { dispatcher.onBackPressed() }
        waitForIdle()
        onNodeWithTag(ApplePlaybackHistoryTestTags.Toolbar).assertDoesNotExist()
        onNodeWithTag(ApplePlaybackHistoryTestTags.Select).assertIsDisplayed()
    }

    @Test
    fun `back closes the delete dialog and keeps the selection`() = runAniComposeUiTest {
        val callbacks = Callbacks()
        var confirmText = ""
        var dispatcher: OnBackPressedDispatcher? = null
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    confirmText = stringResource(Lang.cache_subject_delete)
                    dispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
                    ApplePlaybackHistoryLayout(
                        TestPlaybackHistories,
                        pendingEpisodeIds = emptySet(),
                        isSyncing = false,
                        onSync = {},
                        onOpenHistory = {},
                        onOpenSyncStatus = {},
                        onDelete = { callbacks.deleted += it.toSet() },
                        onBack = {},
                        Modifier.width(390.dp),
                        timeZone = TestTimeZone,
                        clock = TestPlaybackHistoryClock,
                    )
                }
            }
        }
        onNodeWithTag(ApplePlaybackHistoryTestTags.row(1)).performTouchInput { longClick() }
        onNodeWithTag(ApplePlaybackHistoryTestTags.DeleteSelected).performClick()
        onNodeWithText(confirmText).assertExists()

        runOnIdle { dispatcher!!.onBackPressed() }
        waitForIdle()
        onNodeWithText(confirmText).assertDoesNotExist()
        onNodeWithTag(ApplePlaybackHistoryTestTags.row(1)).assertIsSelected()
        assertEquals(emptyList(), callbacks.deleted)
    }

    @Test
    fun `empty history disables selection`() = runAniComposeUiTest {
        var emptyText = ""
        var histories by mutableStateOf(emptyList<PlaybackHistoryUiItem>())
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    emptyText = stringResource(Lang.playback_history_empty)
                    ApplePlaybackHistoryLayout(
                        histories,
                        pendingEpisodeIds = emptySet(),
                        isSyncing = false,
                        onSync = {},
                        onOpenHistory = {},
                        onOpenSyncStatus = {},
                        onDelete = {},
                        onBack = {},
                        Modifier.width(390.dp),
                        timeZone = TestTimeZone,
                        clock = TestPlaybackHistoryClock,
                    )
                }
            }
        }
        onNodeWithTag(ApplePlaybackHistoryTestTags.Select).assertIsNotEnabled()
        onNodeWithText(emptyText).assertIsDisplayed()

        histories = TestPlaybackHistories
        waitForIdle()
        onNodeWithTag(ApplePlaybackHistoryTestTags.Select).assertIsEnabled()
    }
}
