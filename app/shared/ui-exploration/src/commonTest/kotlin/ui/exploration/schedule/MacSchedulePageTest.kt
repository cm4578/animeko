/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.schedule

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.withKeyDown
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.apple.LocalMacKeyboardShortcuts
import me.him188.ani.app.ui.foundation.apple.MacKeyboardShortcuts
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * docs/design/ani-style/04-macos.md §3.4 macOS 新番时间表.
 */
@OptIn(ExperimentalTestApi::class)
class MacSchedulePageTest {
    private val today = LocalDate(2026, 10, 30) // 星期五, 时间表范围为 10/23 – 11/6
    private val days = ScheduleDay.generateForRecentTwoWeeks(today)
    private val now = LocalTime(12, 0)

    private fun item(id: Int, title: String, date: LocalDate, time: LocalTime?) = AiringScheduleItemPresentation(
        subjectId = id,
        subjectTitle = title,
        imageUrl = "",
        episodeId = id * 10,
        episodeSort = EpisodeSort(3),
        episodeEp = null,
        episodeName = null,
        subjectCollectionType = UnifiedCollectionType.NOT_COLLECTED,
        dayOfWeek = date.dayOfWeek,
        time = time,
    )

    private val presentation = SchedulePagePresentation(
        days = days,
        airingSchedules = listOf(
            AiringSchedule(
                today,
                SchedulePageDataHelper.toColumnItems(
                    listOf(item(1, "Morning Show", today, LocalTime(1, 0)), item(2, "Evening Show", today, LocalTime(20, 0))),
                    addIndicator = true,
                    now,
                ),
            ),
            AiringSchedule(
                LocalDate(2026, 11, 4),
                SchedulePageDataHelper.toColumnItems(
                    listOf(item(3, "Next Week Show", LocalDate(2026, 11, 4), LocalTime(22, 0))),
                    addIndicator = false,
                    now,
                ),
            ),
        ),
        error = null,
    )

    private fun ComposeUiTest.setSchedulePage(
        shortcuts: MacKeyboardShortcuts = MacKeyboardShortcuts(),
        onClickItem: (AiringScheduleItemPresentation) -> Unit = {},
    ) {
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(
                    LocalPlatform provides Platform.MacOS(Arch.AARCH64),
                    LocalMacKeyboardShortcuts provides shortcuts,
                ) {
                    // 测试中由根节点的 onKeyEvent 代替主窗口的 Window(onKeyEvent)
                    val focusRequester = remember { FocusRequester() }
                    Box(
                        Modifier
                            .testTag("root")
                            .focusRequester(focusRequester)
                            .focusable()
                            .onKeyEvent { shortcuts.dispatch(it) },
                    ) {
                        MacSchedulePage(
                            presentation,
                            state = ScheduleScreenState(initialDate = null) { days },
                            quickFollowState = null,
                            onRetry = {},
                            onClickItem = onClickItem,
                            Modifier.size(1040.dp, 900.dp),
                        )
                    }
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                }
            }
        }
    }

    @Test
    fun `this week shows today highlighted with the now line`() = runAniComposeUiTest {
        val opened = mutableListOf<Int>()
        setSchedulePage(onClickItem = { opened += it.subjectId })

        onNodeWithTag(MacScheduleTestTags.day(today)).assertIsSelected()
        onNodeWithTag(MacScheduleTestTags.Now).assertExists()
        onNodeWithText("Evening Show").performClick()
        assertEquals(listOf(2), opened)
        // 本周从 10/26 (周一) 开始
        onNodeWithTag(MacScheduleTestTags.day(LocalDate(2026, 10, 26))).assertExists()
    }

    @Test
    fun `week buttons move by a week and stop at the range ends`() = runAniComposeUiTest {
        setSchedulePage()

        onNodeWithTag(MacScheduleTestTags.NextWeek).performClick()
        waitForIdle()
        onNodeWithText("Next Week Show").assertExists()
        onNodeWithTag(MacScheduleTestTags.NextWeek).assertIsNotEnabled()

        onNodeWithTag(MacScheduleTestTags.Today).performClick()
        waitForIdle()
        onNodeWithTag(MacScheduleTestTags.day(today)).assertIsSelected()

        onNodeWithTag(MacScheduleTestTags.PreviousWeek).performClick()
        waitForIdle()
        onNodeWithTag(MacScheduleTestTags.PreviousWeek).assertIsNotEnabled()
        onNodeWithTag(MacScheduleTestTags.NextWeek).assertIsEnabled()
    }

    @Test
    fun `command arrows and command T change the week`() = runAniComposeUiTest {
        setSchedulePage()
        waitForIdle()

        onNodeWithTag("root").performKeyInput { withKeyDown(Key.MetaLeft) { pressKey(Key.DirectionRight) } }
        waitForIdle()
        onNodeWithText("Next Week Show").assertExists()

        onNodeWithTag("root").performKeyInput { withKeyDown(Key.MetaLeft) { pressKey(Key.T) } }
        waitForIdle()
        onNodeWithTag(MacScheduleTestTags.day(today)).assertExists()
        onNodeWithText("Next Week Show").assertDoesNotExist()

        onNodeWithTag("root").performKeyInput { withKeyDown(Key.MetaLeft) { pressKey(Key.DirectionLeft) } }
        waitForIdle()
        onNodeWithTag(MacScheduleTestTags.day(LocalDate(2026, 10, 19))).assertExists()
    }
}
