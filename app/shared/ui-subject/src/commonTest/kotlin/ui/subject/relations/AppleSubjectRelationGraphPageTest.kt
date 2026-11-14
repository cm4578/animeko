/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.relations

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import me.him188.ani.app.data.models.subject.SubjectRelationGraph
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.settings_mediasource_retry
import me.him188.ani.app.ui.lang.subject_details_air_month
import me.him188.ani.app.ui.lang.subject_relation_graph_episodes
import me.him188.ani.app.ui.lang.subject_relation_graph_label_current
import me.him188.ani.app.ui.lang.subject_relation_graph_ordinal
import me.him188.ani.app.ui.lang.subject_relation_graph_show_less
import me.him188.ani.app.ui.lang.subject_relation_graph_show_more
import me.him188.ani.app.ui.lang.subject_relation_graph_summary
import me.him188.ani.app.ui.lang.subject_relation_graph_title
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.annotations.TestOnly
import org.jetbrains.compose.resources.stringResource

/**
 * docs/design/ani-style/03-mobile-pages.md §4.6 iOS 系列关系图; 05 G46.
 */
@OptIn(TestOnly::class)
class AppleSubjectRelationGraphPageTest {
    private class Page {
        val clicked = mutableListOf<Int>()
        var retries = 0
        val texts = mutableMapOf<String, String>()
    }

    private fun ComposeUiTest.setState(
        state: SubjectRelationGraphUiState,
        texts: @Composable () -> Map<String, String> = { emptyMap() },
    ): Page {
        val page = Page()
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    page.texts.putAll(texts())
                    AppleSubjectRelationGraphLayout(
                        state,
                        onRetry = { page.retries++ },
                        onClickSubject = { page.clicked += it.subjectId },
                        onBack = {},
                        Modifier.size(390.dp, 844.dp),
                    )
                }
            }
        }
        waitForIdle()
        return page
    }

    private fun ComposeUiTest.setGraph(
        graph: SubjectRelationGraph,
        texts: @Composable () -> Map<String, String> = { emptyMap() },
    ): Page = setState(SubjectRelationGraphUiState(graph, error = null), texts)

    private fun ComposeUiTest.scrollTo(tag: String) {
        onNodeWithTag(AppleRelationGraphTestTags.List).performScrollToNode(hasTestTag(tag))
        waitForIdle()
    }

    @Test
    fun `header shows the series summary below the title`() = runAniComposeUiTest {
        val page = setGraph(TestSubjectRelationGraphs.ReZero) {
            mapOf(
                "subtitle" to stringResource(Lang.subject_relation_graph_title) + " · " +
                        stringResource(Lang.subject_relation_graph_summary, 7, 8),
            )
        }
        onNodeWithTag(AppleRelationGraphTestTags.Subtitle).assertTextEquals(page.texts.getValue("subtitle"))
        onNodeWithTag(AppleRelationGraphTestTags.CollapsedBar).assertDoesNotExist()
    }

    @Test
    fun `current part is highlighted with its label and air month`() = runAniComposeUiTest {
        val page = setGraph(TestSubjectRelationGraphs.ReZero) {
            mapOf(
                "label" to stringResource(
                    Lang.subject_relation_graph_label_current,
                    stringResource(Lang.subject_relation_graph_ordinal, 2),
                ),
                "meta" to stringResource(Lang.subject_details_air_month, 2020, 7) + " · " +
                        stringResource(Lang.subject_relation_graph_episodes, 13),
            )
        }
        onNodeWithTag(AppleRelationGraphTestTags.main(278826))
            .assertIsSelected()
            .assertTextContains(page.texts.getValue("label"))
            .assertTextContains(page.texts.getValue("meta"))
        onNodeWithTag(AppleRelationGraphTestTags.main(140001)).assertIsNotSelected()
    }

    @Test
    fun `clicking a part or a related subject opens it`() = runAniComposeUiTest {
        val page = setGraph(TestSubjectRelationGraphs.ReZero)
        onNodeWithTag(AppleRelationGraphTestTags.main(140001)).performClick()
        onNodeWithTag(AppleRelationGraphTestTags.branch(177998)).performClick()
        assertEquals(listOf(140001, 177998), page.clicked)
    }

    @Test
    fun `related subjects beyond three are collapsed until expanded`() = runAniComposeUiTest {
        val page = setGraph(TestSubjectRelationGraphs.manyBranches(5)) {
            mapOf(
                "more" to stringResource(Lang.subject_relation_graph_show_more, 2),
                "less" to stringResource(Lang.subject_relation_graph_show_less),
            )
        }
        val toggle = AppleRelationGraphTestTags.toggle(1)
        onNodeWithTag(AppleRelationGraphTestTags.branch(103)).assertExists()
        onNodeWithTag(AppleRelationGraphTestTags.branch(104)).assertDoesNotExist()
        onNodeWithTag(toggle).assertTextEquals(page.texts.getValue("more")).performClick()
        onNodeWithTag(AppleRelationGraphTestTags.branch(105)).assertExists()
        onNodeWithTag(toggle).assertTextEquals(page.texts.getValue("less")).performClick()
        onNodeWithTag(AppleRelationGraphTestTags.branch(104)).assertDoesNotExist()

        scrollTo(AppleRelationGraphTestTags.Truncated)
        onNodeWithTag(AppleRelationGraphTestTags.Truncated).assertExists()
    }

    @Test
    fun `current related subject is highlighted and its collapsed part is expanded`() = runAniComposeUiTest {
        setGraph(TestSubjectRelationGraphs.manyBranches(5).copy(subjectId = 105))
        onNodeWithTag(AppleRelationGraphTestTags.branch(105)).assertIsSelected()
        onNodeWithTag(AppleRelationGraphTestTags.branch(101)).assertIsNotSelected()
        // 主线卡片只在它本身是目前这部时标记
        onNodeWithTag(AppleRelationGraphTestTags.main(1)).assertIsNotSelected()
    }

    @Test
    fun `long series opens at the previous part with the collapsed bar`() = runAniComposeUiTest {
        setGraph(TestSubjectRelationGraphs.Kimetsu)
        // 目前为第四项 (游郭篇), 前一部在顶部; 返回钮所在的标题列已在上方, 显示精简栏
        onNodeWithTag(AppleRelationGraphTestTags.CollapsedBar).assertExists()
        onNodeWithTag(AppleRelationGraphTestTags.main(350764)).assertIsDisplayed()
        // 更早的部数在可视范围之上, 列表没有组合它们
        onNodeWithTag(AppleRelationGraphTestTags.main(245665)).assertDoesNotExist()
    }

    @Test
    fun `scrolling past the header shows the collapsed bar`() = runAniComposeUiTest {
        setGraph(TestSubjectRelationGraphs.ReZero)
        onNodeWithTag(AppleRelationGraphTestTags.CollapsedBar).assertDoesNotExist()
        scrollTo(AppleRelationGraphTestTags.main(633836))
        onNodeWithTag(AppleRelationGraphTestTags.CollapsedBar).assertExists()
    }

    @Test
    fun `loading keeps the title and shows a spinner`() = runAniComposeUiTest {
        val page = setState(SubjectRelationGraphUiState.Loading) {
            mapOf("title" to stringResource(Lang.subject_relation_graph_title))
        }
        onNodeWithText(page.texts.getValue("title")).assertExists()
        onNodeWithTag(AppleRelationGraphTestTags.Loading).assertExists()
    }

    @Test
    fun `error can be retried`() = runAniComposeUiTest {
        val page = setState(SubjectRelationGraphUiState(graph = null, error = LoadError.NetworkError)) {
            mapOf("retry" to stringResource(Lang.settings_mediasource_retry))
        }
        onNodeWithTag(AppleRelationGraphTestTags.Error).assertExists()
        onNodeWithText(page.texts.getValue("retry")).performClick()
        assertEquals(1, page.retries)
    }

    @Test
    fun `air month line omits unknown parts`() = runAniComposeUiTest {
        val kimetsu = TestSubjectRelationGraphs.Kimetsu.mainline
        val railgun = TestSubjectRelationGraphs.Railgun.mainline
        var movie: String? = null
        var expectedMovie = ""
        var unknown: String? = "not computed"
        setContent {
            ProvideCompositionLocalsForPreview {
                // 剧场版只有一集, 不显示话数
                movie = renderAirMonthAndEpisodes(kimetsu.first { it.subject.subjectId == 291494 }.subject)
                expectedMovie = stringResource(Lang.subject_details_air_month, 2020, 10)
                // 放送日期与话数都未知
                unknown = renderAirMonthAndEpisodes(railgun.first { it.subject.subjectId == 537743 }.subject)
            }
        }
        waitForIdle()
        assertEquals(expectedMovie, movie)
        assertNull(unknown)
    }
}
