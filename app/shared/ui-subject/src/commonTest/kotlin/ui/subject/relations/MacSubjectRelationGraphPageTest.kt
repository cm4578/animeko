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
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import me.him188.ani.app.data.models.subject.SubjectRelationGraph
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.settings_mediasource_retry
import me.him188.ani.app.ui.lang.subject_relation_graph_label_current
import me.him188.ani.app.ui.lang.subject_relation_graph_ordinal
import me.him188.ani.app.ui.lang.subject_relation_graph_related_count
import me.him188.ani.app.ui.lang.subject_relation_graph_show_more
import me.him188.ani.app.ui.lang.subject_relation_graph_summary
import me.him188.ani.app.ui.lang.subject_relation_graph_title
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.annotations.TestOnly
import org.jetbrains.compose.resources.stringResource

/**
 * docs/design/ani-style/04-macos.md §4.5 macOS 系列关系图; 05 G46.
 */
@OptIn(TestOnly::class)
class MacSubjectRelationGraphPageTest {
    private class Page {
        val clicked = mutableListOf<Int>()
        var retries = 0
        val texts = mutableMapOf<String, String>()
    }

    /**
     * 宽 1000: 可视宽度 936, 放得下两栏, 每栏 456.
     */
    private fun ComposeUiTest.setState(
        state: SubjectRelationGraphUiState,
        width: Dp = 1000.dp,
        texts: @Composable () -> Map<String, String> = { emptyMap() },
    ): Page {
        val page = Page()
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    page.texts.putAll(texts())
                    MacSubjectRelationGraphLayout(
                        state,
                        onRetry = { page.retries++ },
                        onClickSubject = { page.clicked += it.subjectId },
                        Modifier.size(width, 760.dp),
                    )
                }
            }
        }
        waitForIdle()
        return page
    }

    private fun ComposeUiTest.setGraph(
        graph: SubjectRelationGraph,
        width: Dp = 1000.dp,
        texts: @Composable () -> Map<String, String> = { emptyMap() },
    ): Page = setState(SubjectRelationGraphUiState(graph, error = null), width, texts)

    private fun ComposeUiTest.mainBounds(subjectId: Int) =
        onNodeWithTag(MacRelationGraphTestTags.main(subjectId)).getUnclippedBoundsInRoot()

    @Test
    fun `column width divides the viewport by the columns that fit`() {
        // 1280 宽的主窗口: 内容区 1039, 减去左右内距为 975
        assertEquals(475.5.dp, macRelationColumnWidth(975.dp, 24.dp, 440.dp))
        assertEquals(555.5.dp, macRelationColumnWidth(1135.dp, 24.dp, 440.dp))
        assertEquals(484.dp, macRelationColumnWidth(1500.dp, 24.dp, 440.dp))
        assertEquals(536.dp, macRelationColumnWidth(536.dp, 24.dp, 440.dp))
        assertEquals(440.dp, macRelationColumnWidth(400.dp, 24.dp, 440.dp))
    }

    @Test
    fun `parts are equal columns with the current part highlighted`() = runAniComposeUiTest {
        val page = setGraph(TestSubjectRelationGraphs.ReZero) {
            mapOf(
                "subtitle" to stringResource(Lang.subject_relation_graph_title) + " · " +
                        stringResource(Lang.subject_relation_graph_summary, 7, 8),
                "label" to stringResource(
                    Lang.subject_relation_graph_label_current,
                    stringResource(Lang.subject_relation_graph_ordinal, 2),
                ),
            )
        }
        onNodeWithText(page.texts.getValue("subtitle")).assertExists()
        val first = mainBounds(140001)
        val second = mainBounds(278826)
        assertEquals(456f, (first.right - first.left).value, 0.5f)
        assertEquals(456f, (second.right - second.left).value, 0.5f)
        assertEquals(24f, (second.left - first.right).value, 0.5f)
        onNodeWithTag(MacRelationGraphTestTags.main(278826))
            .assertIsSelected()
            .assertTextContains(page.texts.getValue("label"))
        onNodeWithTag(MacRelationGraphTestTags.main(140001)).assertIsNotSelected()
    }

    @Test
    fun `a single part keeps the column width`() = runAniComposeUiTest {
        val reZero = TestSubjectRelationGraphs.ReZero
        setGraph(reZero.copy(subjectId = 140001, mainline = reZero.mainline.take(1)))
        val bounds = mainBounds(140001)
        assertEquals(456f, (bounds.right - bounds.left).value, 0.5f)
    }

    @Test
    fun `related subjects show the count and collapse beyond four`() = runAniComposeUiTest {
        val page = setGraph(TestSubjectRelationGraphs.manyBranches(5)) {
            mapOf(
                "count" to stringResource(Lang.subject_relation_graph_related_count, 5),
                "more" to stringResource(Lang.subject_relation_graph_show_more, 1),
            )
        }
        onNodeWithText(page.texts.getValue("count")).assertExists()
        onNodeWithTag(MacRelationGraphTestTags.branch(104)).assertExists()
        onNodeWithTag(MacRelationGraphTestTags.branch(105)).assertDoesNotExist()
        onNodeWithTag(MacRelationGraphTestTags.toggle(1))
            .assertTextContains(page.texts.getValue("more"))
            .performClick()
        onNodeWithTag(MacRelationGraphTestTags.branch(105)).assertExists()
        onNodeWithTag(MacRelationGraphTestTags.Truncated).assertExists()
    }

    @Test
    fun `current related subject is highlighted`() = runAniComposeUiTest {
        setGraph(TestSubjectRelationGraphs.ReZeroFromOva)
        onNodeWithTag(MacRelationGraphTestTags.branch(225462)).assertIsSelected()
        onNodeWithTag(MacRelationGraphTestTags.branch(177998)).assertIsNotSelected()
        onNodeWithTag(MacRelationGraphTestTags.main(140001)).assertIsNotSelected()
    }

    @Test
    fun `clicking a card or a related subject opens it`() = runAniComposeUiTest {
        val page = setGraph(TestSubjectRelationGraphs.ReZero)
        onNodeWithTag(MacRelationGraphTestTags.main(140001)).performClick()
        onNodeWithTag(MacRelationGraphTestTags.branch(310194)).performClick()
        assertEquals(listOf(140001, 310194), page.clicked)
    }

    @Test
    fun `long series opens with the previous part at the left`() = runAniComposeUiTest {
        // 目前为第四项 (游郭篇): 第三项在最左
        setGraph(TestSubjectRelationGraphs.Kimetsu)
        assertEquals(32f, mainBounds(350764).left.value, 0.5f)
        onNodeWithTag(MacRelationGraphTestTags.main(245665)).assertIsNotDisplayed()
    }

    @Test
    fun `narrow width uses the vertical timeline`() = runAniComposeUiTest {
        setGraph(TestSubjectRelationGraphs.ReZero, width = 560.dp)
        onNodeWithTag(AppleRelationGraphTestTags.List).assertExists()
        onNodeWithTag(MacRelationGraphTestTags.Columns).assertDoesNotExist()
        onNodeWithTag(AppleRelationGraphTestTags.main(278826)).assertIsSelected()
    }

    @Test
    fun `loading keeps the title and error can be retried`() = runAniComposeUiTest {
        val page = setState(SubjectRelationGraphUiState(graph = null, error = LoadError.NetworkError)) {
            mapOf(
                "title" to stringResource(Lang.subject_relation_graph_title),
                "retry" to stringResource(Lang.settings_mediasource_retry),
            )
        }
        onNodeWithText(page.texts.getValue("title")).assertExists()
        onNodeWithText(page.texts.getValue("retry")).performClick()
        assertEquals(1, page.retries)
    }
}
