/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import me.him188.ani.app.data.models.recommend.RecommendedItemInfo
import me.him188.ani.app.data.models.recommend.RecommendedSubjectInfo
import me.him188.ani.app.data.models.trending.TrendingSubjectInfo
import me.him188.ani.app.ui.exploration.schedule.AiringSchedule
import me.him188.ani.app.ui.exploration.schedule.AiringScheduleColumnItem
import me.him188.ani.app.ui.exploration.schedule.AiringScheduleItemPresentation
import me.him188.ani.app.ui.exploration.schedule.SchedulePagePresentation
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.exploration_refresh
import me.him188.ani.app.ui.lang.exploration_schedule_today_count
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * docs/design/ani-style/04-macos.md §3.3 macOS 探索页.
 */
class MacExplorationPageTest {
    private val today = LocalDate(2026, 10, 30) // 星期五

    private fun airing(id: Int, title: String, time: LocalTime?) = AiringScheduleItemPresentation(
        subjectId = id,
        subjectTitle = title,
        imageUrl = "",
        episodeId = id * 10,
        episodeSort = EpisodeSort(3),
        episodeEp = EpisodeSort(3),
        episodeName = null,
        subjectCollectionType = UnifiedCollectionType.NOT_COLLECTED,
        dayOfWeek = DayOfWeek.FRIDAY,
        time = time,
    )

    private val schedule = SchedulePagePresentation(
        days = emptyList(),
        airingSchedules = listOf(
            AiringSchedule(
                today,
                listOf(
                    AiringScheduleColumnItem.Data(airing(1, "Airing One", LocalTime(1, 30)), showTime = true),
                    AiringScheduleColumnItem.CurrentTimeIndicator(LocalTime(12, 0), isPlaceholder = false),
                    AiringScheduleColumnItem.Data(airing(2, "Airing Two", null), showTime = true),
                ),
            ),
        ),
        error = null,
    )

    private class Labels {
        var refresh = ""
        var todayCount = ""
    }

    private fun ComposeUiTest.setExplorationPage(
        onRefresh: () -> Unit = {},
        onClickSchedule: (LocalDate?) -> Unit = {},
        onClickScheduleItem: (AiringScheduleItemPresentation) -> Unit = {},
    ): Labels {
        val labels = Labels()
        setContent {
            labels.refresh = stringResource(Lang.exploration_refresh)
            labels.todayCount = stringResource(Lang.exploration_schedule_today_count, 2)
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    MacExplorationPageLayout(
                        trending = remember {
                            flowOf(PagingData.from(listOf(TrendingSubjectInfo(bangumiId = 100, nameCn = "Trending", imageLarge = ""))))
                        }.collectAsLazyPagingItems(),
                        recommendations = remember {
                            flowOf(
                                PagingData.from<RecommendedItemInfo>(
                                    List(3) {
                                        RecommendedSubjectInfo(bangumiId = it + 1, nameCn = "Recommended", name = "", imageLarge = "")
                                    },
                                ),
                            )
                        }.collectAsLazyPagingItems(),
                        today = today,
                        schedule = schedule,
                        quickFollowState = null,
                        onRefresh = onRefresh,
                        onClickTrending = {},
                        onClickTrendingMore = {},
                        onClickSchedule = onClickSchedule,
                        onClickScheduleItem = onClickScheduleItem,
                        onClickRecommendation = {},
                        modifier = Modifier.size(1040.dp, 1600.dp),
                    )
                }
            }
        }
        return labels
    }

    @Test
    fun `schedule widget lists today and opens the schedule`() = runAniComposeUiTest {
        val opened = mutableListOf<LocalDate?>()
        val openedItems = mutableListOf<Int>()
        val labels = setExplorationPage(
            onClickSchedule = { opened += it },
            onClickScheduleItem = { openedItems += it.subjectId },
        )

        onNodeWithTag(AppleExplorationTestTags.weekday(today)).assertIsSelected()
        onNodeWithText("Airing One").assertExists()
        onNodeWithText("01:30").assertExists()
        onNodeWithText(labels.todayCount).assertExists()

        onNodeWithTag(AppleExplorationTestTags.weekday(LocalDate(2026, 10, 27))).performClick()
        onNodeWithTag(AppleExplorationTestTags.ScheduleToday).performClick()
        onNodeWithText("Airing Two").performClick()
        assertEquals(listOf(LocalDate(2026, 10, 27), null), opened)
        assertEquals(listOf(2), openedItems)
    }

    @Test
    fun `toolbar refresh reloads the page`() = runAniComposeUiTest {
        var refreshed = 0
        val labels = setExplorationPage(onRefresh = { refreshed++ })

        onNodeWithContentDescription(labels.refresh).performClick()
        assertEquals(1, refreshed)
    }
}
