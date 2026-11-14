/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.trends

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.data.models.preference.NsfwMode
import me.him188.ani.app.data.models.subject.RatingCounts
import me.him188.ani.app.data.models.subject.RatingInfo
import me.him188.ani.app.ui.exploration.follow.QuickFollowState
import me.him188.ani.app.ui.exploration.search.SubjectPreviewItemInfo
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.exploration_trending_ranking_col_heat
import me.him188.ani.app.ui.lang.exploration_trending_ranking_col_rank
import me.him188.ani.app.ui.lang.exploration_trending_ranking_col_status
import me.him188.ani.app.ui.lang.exploration_trending_ranking_empty
import me.him188.ani.app.ui.lang.exploration_trending_ranking_heat
import me.him188.ani.app.ui.lang.exploration_trending_ranking_heat_compact
import me.him188.ani.app.ui.lang.exploration_trending_ranking_heat_compact_value
import me.him188.ani.app.ui.search.createTestPager
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.annotations.TestOnly
import org.jetbrains.compose.resources.stringResource
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * docs/design/ani-style/03-mobile-pages.md §4.1、04-macos.md §4.1 热度排行; 05 G2、C3.
 */
@OptIn(TestOnly::class, ExperimentalTestApi::class)
class TrendingRankingPagesTest {
    private fun item(rank: Int, heat: Int, title: String) = TrendingRankingItemPresentation(
        rank = rank,
        heat = heat,
        subject = SubjectPreviewItemInfo(
            subjectId = rank,
            imageUrl = "",
            title = title,
            tags = "2026 年 10 月 · 全 12 话",
            staff = null,
            actors = null,
            rating = RatingInfo(rank = 100, total = 500, count = RatingCounts.Zero, score = "7.5"),
            nsfw = false,
            nsfwMode = NsfwMode.DISPLAY,
        ),
    )

    private val items = listOf(item(1, 7559, "Subject One"), item(2, 5330, "Subject Two"), item(4, 120, "Subject Four"))

    private class FollowRecorder {
        val types = mutableMapOf<Int, MutableStateFlow<UnifiedCollectionType>>()
        val followed = mutableListOf<Int>()
        val state = QuickFollowState(
            collectionTypeFlow = { types.getOrPut(it) { MutableStateFlow(UnifiedCollectionType.NOT_COLLECTED) } },
            onFollow = {
                followed += it
                types.getValue(it).value = UnifiedCollectionType.WISH
            },
            onSetCollectionType = { id, type -> types.getValue(id).value = type },
        )
    }

    @Composable
    private fun ProvideMac(content: @Composable () -> Unit) {
        ProvideCompositionLocalsForPreview {
            CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64), content = content)
        }
    }

    private fun ComposeUiTest.setApplePage(
        list: List<TrendingRankingItemPresentation> = items,
        quickFollowState: QuickFollowState? = null,
        onClickItem: (TrendingRankingItemPresentation) -> Unit = {},
    ) {
        val pager = createTestPager(list)
        setContent {
            ProvideCompositionLocalsForPreview {
                Box(Modifier.size(390.dp, 844.dp)) {
                    AppleTrendingRankingPage(
                        pager.collectAsLazyPagingItems(),
                        quickFollowState = quickFollowState,
                        onClickItem = onClickItem,
                        onBack = {},
                    )
                }
            }
        }
    }

    private fun ComposeUiTest.setMacPage(
        quickFollowState: QuickFollowState? = null,
        onOpenItem: (TrendingRankingItemPresentation) -> Unit = {},
        headers: MutableMap<String, String> = mutableMapOf(),
    ) {
        val pager = createTestPager(items)
        setContent {
            headers["rank"] = stringResource(Lang.exploration_trending_ranking_col_rank)
            headers["heat"] = stringResource(Lang.exploration_trending_ranking_col_heat)
            headers["status"] = stringResource(Lang.exploration_trending_ranking_col_status)
            ProvideMac {
                Box(Modifier.size(1040.dp, 760.dp)) {
                    MacTrendingRankingPage(
                        pager.collectAsLazyPagingItems(),
                        quickFollowState = quickFollowState,
                        onOpenItem = onOpenItem,
                    )
                }
            }
        }
    }

    @Test
    fun `apple page shows rank title heat and the description`() = runAniComposeUiTest {
        var heat = ""
        val pager = createTestPager(items)
        setContent {
            heat = stringResource(Lang.exploration_trending_ranking_heat, "7559")
            ProvideCompositionLocalsForPreview {
                Box(Modifier.size(390.dp, 844.dp)) {
                    AppleTrendingRankingPage(pager.collectAsLazyPagingItems(), null, onClickItem = {}, onBack = {})
                }
            }
        }
        onNodeWithTag(AppleTrendingTestTags.Description).assertExists()
        onNodeWithText("Subject One").assertExists()
        onNodeWithText(heat).assertExists()
        // 前 3 名的名次比第 4 名起大
        val first = onNodeWithTag(AppleTrendingTestTags.rank(1), useUnmergedTree = true).getBoundsInRoot()
        val fourth = onNodeWithTag(AppleTrendingTestTags.rank(4), useUnmergedTree = true).getBoundsInRoot()
        assertTrue(first.bottom - first.top > fourth.bottom - fourth.top)
    }

    @Test
    fun `apple page opens a subject and follows it`() = runAniComposeUiTest {
        val recorder = FollowRecorder()
        var clicked: TrendingRankingItemPresentation? = null
        setApplePage(quickFollowState = recorder.state, onClickItem = { clicked = it })

        onNodeWithText("Subject Two").performClick()
        assertEquals(2, clicked?.rank)

        onNodeWithTag(AppleTrendingTestTags.follow(1)).performClick()
        waitForIdle()
        assertEquals(listOf(1), recorder.followed)
    }

    @Test
    fun `apple page hides follow buttons without a state and shows the empty text`() = runAniComposeUiTest {
        var empty = ""
        val pager = createTestPager(emptyList<TrendingRankingItemPresentation>())
        setContent {
            empty = stringResource(Lang.exploration_trending_ranking_empty)
            ProvideCompositionLocalsForPreview {
                Box(Modifier.size(390.dp, 844.dp)) {
                    AppleTrendingRankingPage(pager.collectAsLazyPagingItems(), null, onClickItem = {}, onBack = {})
                }
            }
        }
        onNodeWithText(empty).assertExists()
        onNodeWithTag(AppleTrendingTestTags.follow(1)).assertDoesNotExist()
    }

    @Test
    fun `mac header cells share the column centers with the rows`() = runAniComposeUiTest {
        val headers = mutableMapOf<String, String>()
        setMacPage(quickFollowState = FollowRecorder().state, headers = headers)

        fun centerX(tag: String? = null, text: String? = null): Dp {
            val bounds = if (tag != null) {
                onNodeWithTag(tag, useUnmergedTree = true).getBoundsInRoot()
            } else {
                onNodeWithText(text!!, useUnmergedTree = true).getBoundsInRoot()
            }
            return (bounds.left + bounds.right) / 2
        }

        val rankHeader = centerX(text = headers.getValue("rank"))
        val rank = centerX(tag = AppleTrendingTestTags.rank(1))
        assertTrue(abs((rankHeader - rank).value) < 1f, "rank header=$rankHeader cell=$rank")
        val statusHeader = centerX(text = headers.getValue("status"))
        val follow = centerX(tag = MacTrendingTestTags.follow(1))
        assertTrue(abs((statusHeader - follow).value) < 1f, "status header=$statusHeader pill=$follow")
        // 表头带排序箭头, 文字与箭头一起居中
        val heatHeader = centerX(tag = MacTrendingTestTags.HeatHeader)
        val heat = centerX(tag = MacTrendingTestTags.heat(1))
        assertTrue(abs((heatHeader - heat).value) < 1f, "heat header=$heatHeader cell=$heat")
    }

    @Test
    fun `mac rows select on click and open on double click`() = runAniComposeUiTest {
        var opened: TrendingRankingItemPresentation? = null
        setMacPage(onOpenItem = { opened = it })

        onNodeWithTag(MacTrendingTestTags.row(2)).assertIsNotSelected().performClick()
        onNodeWithTag(MacTrendingTestTags.row(2)).assertIsSelected()
        assertEquals(null, opened)

        onNodeWithTag(MacTrendingTestTags.row(1)).performMouseInput { doubleClick(center) }
        waitForIdle()
        assertEquals(1, opened?.rank)
    }

    @Test
    fun `mac context menu opens the subject follows and shares`() = runAniComposeUiTest {
        val recorder = FollowRecorder()
        var opened: TrendingRankingItemPresentation? = null
        setMacPage(quickFollowState = recorder.state, onOpenItem = { opened = it })

        onNodeWithTag(MacTrendingTestTags.row(1)).performMouseInput { rightClick(center) }
        onNodeWithTag(MacTrendingTestTags.Menu).assertExists()
        onNodeWithTag(MacTrendingTestTags.row(1)).assertIsSelected()
        onNodeWithTag(MacTrendingTestTags.MenuCopyLink).assertExists()
        onNodeWithTag(MacTrendingTestTags.MenuFollow).performClick()
        waitForIdle()
        assertEquals(listOf(1), recorder.followed)
        onNodeWithTag(MacTrendingTestTags.Menu).assertDoesNotExist()

        // 已追番时为取消追番
        onNodeWithTag(MacTrendingTestTags.row(1)).performMouseInput { rightClick(center) }
        onNodeWithTag(MacTrendingTestTags.MenuUnfollow).assertExists()
        onNodeWithTag(MacTrendingTestTags.MenuOpen).performClick()
        waitForIdle()
        assertEquals(1, opened?.rank)
    }

    @Test
    fun `mac follow pill follows the subject`() = runAniComposeUiTest {
        val recorder = FollowRecorder()
        setMacPage(quickFollowState = recorder.state)
        onNodeWithTag(MacTrendingTestTags.follow(2)).assertIsNotSelected().performClick()
        waitForIdle()
        assertEquals(listOf(2), recorder.followed)
        onNodeWithTag(MacTrendingTestTags.follow(2)).assertIsSelected()
    }

    @Test
    fun `heat of ten thousand or more is abbreviated to one decimal`() {
        assertEquals(null, compactTrendingHeat(9_999, 10_000))
        assertEquals("1", compactTrendingHeat(10_000, 10_000))
        assertEquals("3.2", compactTrendingHeat(32_145, 10_000))
        assertEquals("4", compactTrendingHeat(39_999, 10_000))
        assertEquals("32.1", compactTrendingHeat(32_145, 1_000))
        assertEquals(null, compactTrendingHeat(32_145, 0))
    }

    @Test
    fun `apple and mac pages abbreviate large heat`() = runAniComposeUiTest {
        var line = ""
        var value = ""
        val pager = createTestPager(listOf(item(1, 32_145, "Popular"), item(2, 7559, "Subject Two")))
        setContent {
            line = stringResource(Lang.exploration_trending_ranking_heat_compact, "3.2")
            value = stringResource(Lang.exploration_trending_ranking_heat_compact_value, "3.2")
            ProvideCompositionLocalsForPreview {
                val items = pager.collectAsLazyPagingItems()
                Column {
                    Box(Modifier.size(390.dp, 400.dp)) {
                        AppleTrendingRankingPage(items, null, onClickItem = {}, onBack = {})
                    }
                    CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                        Box(Modifier.size(1040.dp, 400.dp)) {
                            MacTrendingRankingPage(items, quickFollowState = null, onOpenItem = {})
                        }
                    }
                }
            }
        }
        onNodeWithText(line).assertExists()
        onNode(
            hasText(value) and hasAnyAncestor(hasTestTag(MacTrendingTestTags.heat(1))),
            useUnmergedTree = true,
        ).assertExists()
    }
}
