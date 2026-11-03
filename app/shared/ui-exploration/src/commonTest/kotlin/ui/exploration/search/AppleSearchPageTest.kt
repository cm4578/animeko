/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.search

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.models.preference.NsfwMode
import me.him188.ani.app.data.models.subject.CanonicalTagKind
import me.him188.ani.app.data.models.subject.RatingInfo
import me.him188.ani.app.domain.search.SearchSort
import me.him188.ani.app.domain.search.SubjectSearchQuery
import me.him188.ani.app.ui.exploration.follow.QuickFollowState
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.search.TestSearchState
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Platform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppleSearchPageTest {
    private fun result(id: Int, title: String) = SubjectPreviewItemInfo(
        subjectId = id,
        imageUrl = "",
        title = title,
        tags = "2011 年 1 月 · 全 12 话 · 魔法少女",
        staff = null,
        actors = null,
        rating = RatingInfo.Empty,
        nsfw = false,
        nsfwMode = NsfwMode.DISPLAY,
        airingText = "2011 年 1 月 · 全 12 话",
        genreText = "魔法少女",
    )

    private val results = listOf(result(1, "Magic Girl One"), result(2, "Magic Girl Two"))

    private fun ComposeUiTest.setSearchPage(
        keywords: String = "Magic",
        quickFollowState: QuickFollowState? = null,
        onIntent: (SearchPageIntent) -> Unit = {},
        onCancel: () -> Unit = {},
    ) {
        val state = createTestSearchPageState(
            searchState = TestSearchState(MutableStateFlow(flowOf(PagingData.from(results)))),
            query = SubjectSearchQuery(keywords),
        )
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    AppleSearchPage(
                        state,
                        onIntent = onIntent,
                        suggestionsPager = { flowOf(PagingData.from(listOf("suggestion"))) },
                        quickFollowState = quickFollowState,
                        onCancel = onCancel,
                        Modifier.width(390.dp),
                    )
                }
            }
        }
    }

    @Test
    fun `keyword ranges ignore case and merge overlapping words`() {
        assertEquals(listOf(0..4, 11..15), keywordRanges("Magic girl magic", "MAGIC"))
        assertEquals(listOf(0..3), keywordRanges("魔法少女小圓", "魔法 少女"))
        assertEquals(emptyList(), keywordRanges("魔法少女小圓", " "))
    }

    @Test
    fun `clicking a result opens the subject`() = runAniComposeUiTest {
        val intents = mutableListOf<SearchPageIntent>()
        setSearchPage(onIntent = { intents += it })

        onNodeWithTag(AppleSearchTestTags.result(2)).assertIsDisplayed()
        onNodeWithText("Magic Girl Two").performClick()
        assertEquals(SearchPageIntent.OpenSubjectDetails(1, results[1]), intents.last())
    }

    @Test
    fun `focusing the field shows history and picking submits`() = runAniComposeUiTest {
        val intents = mutableListOf<SearchPageIntent>()
        setSearchPage(keywords = "", onIntent = { intents += it })

        onNodeWithTag(AppleSearchTestTags.Field).performClick()
        onNodeWithTag(AppleSearchTestTags.Suggestions).assertIsDisplayed()
        onNodeWithText("test history").performClick()
        waitForIdle()

        val intent = intents.last() as SearchPageIntent.UpdateQuery
        assertEquals("test history", intent.query.keywords)
        assertTrue(intent.submit)
        onNodeWithTag(AppleSearchTestTags.Suggestions).assertDoesNotExist()
    }

    @Test
    fun `clear and cancel buttons`() = runAniComposeUiTest {
        val intents = mutableListOf<SearchPageIntent>()
        var cancelled = false
        setSearchPage(onIntent = { intents += it }, onCancel = { cancelled = true })

        onNodeWithTag(AppleSearchTestTags.Clear).performClick()
        assertEquals("", (intents.last() as SearchPageIntent.UpdateQuery).query.keywords)
        onNodeWithTag(AppleSearchTestTags.Clear).assertDoesNotExist()

        onNodeWithTag(AppleSearchTestTags.Cancel).performClick()
        assertTrue(cancelled)
    }

    @Test
    fun `sort pill changes the sort`() = runAniComposeUiTest {
        val intents = mutableListOf<SearchPageIntent>()
        setSearchPage(onIntent = { intents += it })

        onNodeWithTag(AppleSearchTestTags.Sort).performClick()
        onNodeWithTag(AppleSearchTestTags.sortOption(SearchSort.RANK)).performClick()
        assertEquals(SearchPageIntent.ChangeSort(SearchSort.RANK), intents.last())
        onNodeWithTag(AppleSearchTestTags.sortOption(SearchSort.RANK)).assertDoesNotExist()
    }

    @Test
    fun `tag pill toggles values and keeps the menu open`() = runAniComposeUiTest {
        val intents = mutableListOf<SearchPageIntent>()
        setSearchPage(onIntent = { intents += it })

        onNodeWithTag(AppleSearchTestTags.tagKind(CanonicalTagKind.Genre)).performClick()
        onNodeWithText("科幻").performClick()
        assertEquals(listOf("科幻"), (intents.last() as SearchPageIntent.UpdateQuery).query.tags)
        onNodeWithText("科幻").assertIsDisplayed()
    }

    @Test
    fun `season pill is disabled without a year`() = runAniComposeUiTest {
        setSearchPage()
        onNodeWithTag(AppleSearchTestTags.Season).assertIsNotEnabled()
    }

    @Test
    fun `follow button follows the subject`() = runAniComposeUiTest {
        val followed = mutableListOf<Int>()
        val state = QuickFollowState(
            collectionTypeFlow = { MutableStateFlow(UnifiedCollectionType.NOT_COLLECTED) },
            onFollow = { followed += it },
            onSetCollectionType = { _, _ -> },
        )
        setSearchPage(quickFollowState = state)
        onNodeWithTag(AppleSearchTestTags.follow(1)).performClick()
        waitForIdle()
        assertEquals(listOf(1), followed)
    }

    @Test
    fun `follow button is hidden without a state`() = runAniComposeUiTest {
        setSearchPage()
        onNodeWithTag(AppleSearchTestTags.result(1)).assertIsDisplayed()
        onNodeWithTag(AppleSearchTestTags.follow(1)).assertDoesNotExist()
    }
}
