/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.schedule

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import me.him188.ani.app.ui.exploration.follow.QuickFollowState
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

class AppleSchedulePageTest {
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

    private val morning = item(1, "Morning Show", today, LocalTime(1, 0))
    private val evening = item(2, "Evening Show", today, LocalTime(20, 0))
    private val unknown = item(3, "Unknown Time Show", today, null)
    private val tuesday = LocalDate(2026, 10, 27)
    private val tuesdayShow = item(4, "Tuesday Show", tuesday, LocalTime(22, 0))

    private val presentation = SchedulePagePresentation(
        days = days,
        airingSchedules = listOf(
            AiringSchedule(
                today,
                SchedulePageDataHelper.toColumnItems(listOf(evening, unknown, morning), addIndicator = true, now),
            ),
            AiringSchedule(
                tuesday,
                SchedulePageDataHelper.toColumnItems(listOf(tuesdayShow), addIndicator = false, now),
            ),
        ),
        error = null,
    )

    private fun ComposeUiTest.setSchedulePage(
        initialDate: LocalDate? = null,
        quickFollowState: QuickFollowState? = null,
        onClickItem: (AiringScheduleItemPresentation) -> Unit = {},
    ) {
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    AppleSchedulePage(
                        presentation,
                        state = ScheduleScreenState(initialDate) { days },
                        quickFollowState = quickFollowState,
                        onRetry = {},
                        onClickItem = onClickItem,
                        onBack = {},
                        Modifier.width(390.dp),
                    )
                }
            }
        }
    }

    @Test
    fun `today rows mark aired episodes before the now line`() {
        val rows = appleScheduleRows(presentation.airingSchedules[0].episodes, today, today)
        assertEquals(
            listOf("item-1-10", "now", "item-2-20", "item-3-30"),
            rows.map { it.key },
        )
        val items = rows.filterIsInstance<AppleScheduleRow.Item>()
        assertEquals(listOf(true, false, false), items.map { it.aired })
        // 时间线后面的第一项不画分隔线
        assertEquals(listOf(false, false, true), items.map { it.showDivider })
    }

    @Test
    fun `past days are aired and future days are not`() {
        val columnItems = SchedulePageDataHelper.toColumnItems(listOf(morning, evening), addIndicator = false, now)
        assertEquals(
            listOf(true, true),
            appleScheduleRows(columnItems, tuesday, today).map { (it as AppleScheduleRow.Item).aired },
        )
        assertEquals(
            listOf(false, false),
            appleScheduleRows(columnItems, LocalDate(2026, 11, 2), today).map { (it as AppleScheduleRow.Item).aired },
        )
    }

    @Test
    fun `opens on the initial date and switches days`() = runAniComposeUiTest {
        setSchedulePage(initialDate = tuesday)
        onNodeWithTag(AppleScheduleTestTags.weekday(tuesday)).assertIsSelected()
        onNodeWithText(tuesdayShow.subjectTitle).assertExists()

        onNodeWithTag(AppleScheduleTestTags.weekday(today)).performClick()
        onNodeWithTag(AppleScheduleTestTags.weekday(today)).assertIsSelected()
        onNodeWithText(morning.subjectTitle).assertExists()
        onNodeWithTag(AppleScheduleTestTags.Now).assertExists()
        onNodeWithText(tuesdayShow.subjectTitle).assertDoesNotExist()
    }

    @Test
    fun `week buttons stay within the schedule range`() = runAniComposeUiTest {
        setSchedulePage()
        onNodeWithTag(AppleScheduleTestTags.weekday(today)).assertIsSelected()

        onNodeWithTag(AppleScheduleTestTags.PreviousWeek).performClick()
        // 上周五在范围内, 上周一至周四不在
        onNodeWithTag(AppleScheduleTestTags.weekday(LocalDate(2026, 10, 23))).assertIsSelected()
        onNodeWithTag(AppleScheduleTestTags.weekday(LocalDate(2026, 10, 22))).assertIsNotEnabled()
        onNodeWithTag(AppleScheduleTestTags.PreviousWeek).assertIsNotEnabled()

        onNodeWithTag(AppleScheduleTestTags.NextWeek).performClick()
        onNodeWithTag(AppleScheduleTestTags.NextWeek).performClick()
        onNodeWithTag(AppleScheduleTestTags.weekday(LocalDate(2026, 11, 6))).assertIsSelected()
        onNodeWithTag(AppleScheduleTestTags.weekday(LocalDate(2026, 11, 7))).assertIsNotEnabled()
        onNodeWithTag(AppleScheduleTestTags.NextWeek).assertIsNotEnabled()
        onNodeWithTag(AppleScheduleTestTags.PreviousWeek).assertIsEnabled()
    }

    @Test
    fun `clicking a row opens the subject`() = runAniComposeUiTest {
        var clicked: AiringScheduleItemPresentation? = null
        setSchedulePage(onClickItem = { clicked = it })
        onNodeWithText(evening.subjectTitle).performClick()
        assertEquals(evening, clicked)
    }

    @Test
    fun `follow button follows the subject`() = runAniComposeUiTest {
        val type = MutableStateFlow(UnifiedCollectionType.NOT_COLLECTED)
        val followed = mutableListOf<Int>()
        val state = QuickFollowState(
            collectionTypeFlow = { if (it == morning.subjectId) type else MutableStateFlow(UnifiedCollectionType.NOT_COLLECTED) },
            onFollow = {
                followed += it
                type.value = UnifiedCollectionType.WISH
            },
            onSetCollectionType = { _, _ -> },
        )
        setSchedulePage(quickFollowState = state)

        onNodeWithTag(AppleScheduleTestTags.follow(morning.subjectId)).performClick()
        waitForIdle()
        assertEquals(listOf(morning.subjectId), followed)
    }

    @Test
    fun `follow buttons are hidden without a state`() = runAniComposeUiTest {
        setSchedulePage()
        onNodeWithText(morning.subjectTitle).assertExists()
        onNodeWithTag(AppleScheduleTestTags.follow(morning.subjectId)).assertDoesNotExist()
    }
}
