/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import me.him188.ani.app.data.models.recommend.RecommendedItemInfo
import me.him188.ani.app.data.models.recommend.RecommendedSubjectInfo
import me.him188.ani.app.data.models.trending.TrendingSubjectInfo
import me.him188.ani.app.ui.exploration.follow.QuickFollowState
import me.him188.ani.app.ui.exploration.follow.QuickFollowTestTags
import me.him188.ani.app.ui.exploration.schedule.currentWeek
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

class AppleExplorationPageTest {
    private val today = LocalDate(2026, 10, 30) // 星期五

    private val trending = TrendingSubjectInfo(bangumiId = 100, nameCn = "Trending One", imageLarge = "")

    private val recommendations = List(3) {
        RecommendedSubjectInfo(bangumiId = it + 1, nameCn = "Recommended ${it + 1}", name = "", imageLarge = "")
    }

    /**
     * 收藏状态存在内存里的 [QuickFollowState], 记录收到的动作.
     */
    private class FakeQuickFollow(initial: Map<Int, UnifiedCollectionType> = emptyMap()) {
        private val types = mutableMapOf<Int, MutableStateFlow<UnifiedCollectionType>>()
        val followed = mutableListOf<Int>()
        val changes = mutableListOf<Pair<Int, UnifiedCollectionType>>()

        init {
            initial.forEach { (id, type) -> flow(id).value = type }
        }

        private fun flow(subjectId: Int) =
            types.getOrPut(subjectId) { MutableStateFlow(UnifiedCollectionType.NOT_COLLECTED) }

        val state = QuickFollowState(
            collectionTypeFlow = { flow(it) },
            onFollow = {
                followed += it
                flow(it).value = UnifiedCollectionType.WISH
            },
            onSetCollectionType = { id, type ->
                changes += id to type
                flow(id).value = type
            },
        )
    }

    private fun ComposeUiTest.setExplorationPage(
        quickFollowState: QuickFollowState?,
        onClickTrending: (TrendingSubjectInfo) -> Unit = {},
        onClickSchedule: (LocalDate?) -> Unit = {},
        onClickRecommendation: (RecommendedSubjectInfo) -> Unit = {},
    ) {
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    AppleExplorationPageLayout(
                        trending = remember { flowOf(PagingData.from(listOf(trending))) }.collectAsLazyPagingItems(),
                        recommendations = remember {
                            flowOf(PagingData.from<RecommendedItemInfo>(recommendations))
                        }.collectAsLazyPagingItems(),
                        today = today,
                        quickFollowState = quickFollowState,
                        onSearch = {},
                        onClickTrending = onClickTrending,
                        onClickTrendingMore = {},
                        onClickSchedule = onClickSchedule,
                        onClickRecommendation = onClickRecommendation,
                        // 手机宽度, 推荐的第一行在测试窗口内
                        modifier = Modifier.width(390.dp),
                    )
                }
            }
        }
    }

    /**
     * 菜单行内可点击的节点, 选中状态在这个节点上.
     */
    private fun ComposeUiTest.menuRow(type: UnifiedCollectionType) =
        onNodeWithTag(QuickFollowTestTags.type(type)).onChildren().filterToOne(hasClickAction())

    @Test
    fun `current week runs from Monday to Sunday`() {
        val week = currentWeek(today)
        assertEquals(LocalDate(2026, 10, 26), week.first())
        assertEquals(LocalDate(2026, 11, 1), week.last())
        assertEquals(DayOfWeek.entries, week.map { it.dayOfWeek })
        assertEquals(week, currentWeek(LocalDate(2026, 11, 1)))
    }

    @Test
    fun `week strip selects today and opens the schedule at the tapped day`() = runAniComposeUiTest {
        val opened = mutableListOf<LocalDate?>()
        setExplorationPage(quickFollowState = null, onClickSchedule = { opened += it })

        onNodeWithTag(AppleExplorationTestTags.weekday(today)).assertIsSelected()
        onNodeWithTag(AppleExplorationTestTags.weekday(LocalDate(2026, 10, 27))).assertIsNotSelected()

        onNodeWithTag(AppleExplorationTestTags.weekday(LocalDate(2026, 10, 27))).performClick()
        onNodeWithTag(AppleExplorationTestTags.ScheduleToday).performClick()
        assertEquals(listOf(LocalDate(2026, 10, 27), null), opened)
    }

    @Test
    fun `clicking cards opens the subject`() = runAniComposeUiTest {
        var clickedTrending: TrendingSubjectInfo? = null
        var clickedRecommendation: RecommendedSubjectInfo? = null
        setExplorationPage(
            quickFollowState = null,
            onClickTrending = { clickedTrending = it },
            onClickRecommendation = { clickedRecommendation = it },
        )

        onNodeWithText(trending.nameCn).performClick()
        onNodeWithText(recommendations[1].nameCn).performClick()
        assertEquals(trending, clickedTrending)
        assertEquals(recommendations[1], clickedRecommendation)
    }

    @Test
    fun `plus button follows the subject`() = runAniComposeUiTest {
        val fake = FakeQuickFollow()
        setExplorationPage(fake.state)
        val subjectId = recommendations[0].bangumiId

        onNodeWithTag(AppleExplorationTestTags.follow(subjectId)).assertIsNotSelected().performClick()
        waitForIdle()
        assertEquals(listOf(subjectId), fake.followed)
        onNodeWithTag(AppleExplorationTestTags.follow(subjectId)).assertIsSelected()
        onNodeWithTag(QuickFollowTestTags.Menu).assertDoesNotExist()
    }

    @Test
    fun `check button opens the collection type menu`() = runAniComposeUiTest {
        val subjectId = recommendations[0].bangumiId
        val fake = FakeQuickFollow(mapOf(subjectId to UnifiedCollectionType.WISH))
        setExplorationPage(fake.state)

        onNodeWithTag(AppleExplorationTestTags.follow(subjectId)).assertIsSelected().performClick()
        onNodeWithTag(QuickFollowTestTags.Menu).assertIsDisplayed()
        menuRow(UnifiedCollectionType.WISH).assertIsSelected()
        menuRow(UnifiedCollectionType.DOING).assertIsNotSelected()

        onNodeWithTag(QuickFollowTestTags.type(UnifiedCollectionType.DOING)).performClick()
        waitForIdle()
        assertEquals(listOf(subjectId to UnifiedCollectionType.DOING), fake.changes)
        assertEquals(emptyList(), fake.followed)
        onNodeWithTag(QuickFollowTestTags.Menu).assertDoesNotExist()
    }

    @Test
    fun `unfollow asks for confirmation`() = runAniComposeUiTest {
        val subjectId = recommendations[0].bangumiId
        val fake = FakeQuickFollow(mapOf(subjectId to UnifiedCollectionType.DOING))
        setExplorationPage(fake.state)

        onNodeWithTag(AppleExplorationTestTags.follow(subjectId)).performClick()
        onNodeWithTag(QuickFollowTestTags.Unfollow).performClick()
        waitForIdle()
        assertEquals(emptyList(), fake.changes)

        onNodeWithTag(QuickFollowTestTags.ConfirmUnfollow).performClick()
        waitForIdle()
        assertEquals(listOf(subjectId to UnifiedCollectionType.NOT_COLLECTED), fake.changes)
        onNodeWithTag(AppleExplorationTestTags.follow(subjectId)).assertIsNotSelected()
    }

    @Test
    fun `quick follow is hidden without a state`() = runAniComposeUiTest {
        setExplorationPage(quickFollowState = null)
        onNodeWithText(recommendations[0].nameCn).assertIsDisplayed()
        onNodeWithTag(AppleExplorationTestTags.follow(recommendations[0].bangumiId)).assertDoesNotExist()
    }
}
