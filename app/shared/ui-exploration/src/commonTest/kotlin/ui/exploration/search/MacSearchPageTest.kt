/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.search

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.models.preference.NsfwMode
import me.him188.ani.app.data.models.subject.RatingInfo
import me.him188.ani.app.domain.search.SubjectSearchQuery
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.search.TestSearchState
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * macOS 搜索页: 搜索框在侧边栏; 05 C2 ↩ 打开选中的结果.
 */
@OptIn(ExperimentalTestApi::class)
class MacSearchPageTest {
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
        selectedItemIndex: Int = -1,
        hasActiveSearch: Boolean = true,
        onIntent: (SearchPageIntent) -> Unit = {},
    ) {
        val state = createTestSearchPageState(
            searchState = TestSearchState(MutableStateFlow(flowOf(PagingData.from(results)))),
            query = SubjectSearchQuery(if (hasActiveSearch) "Magic" else ""),
            hasActiveSearch = hasActiveSearch,
        ).copy(selectedItemIndex = selectedItemIndex)
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    MacSearchPage(
                        state,
                        onIntent = onIntent,
                        quickFollowState = null,
                        Modifier.size(900.dp, 700.dp),
                    )
                }
            }
        }
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
    fun `results take focus and arrow keys select a result`() = runAniComposeUiTest {
        val intents = mutableListOf<SearchPageIntent>()
        setSearchPage(onIntent = { intents += it })
        waitForIdle()

        onNodeWithTag(MacSearchTestTags.Results).assertIsFocused()
        onNodeWithTag(MacSearchTestTags.Results).performKeyInput { pressKey(Key.DirectionDown) }
        waitForIdle()
        assertEquals(SearchPageIntent.SelectResult(0, results[0]), intents.last())
        // 选中的结果已经完整可见, 列表不滚动, 标题仍在
        onNodeWithText("Magic Girl One").assertIsDisplayed()
        onNodeWithTag(AppleSearchTestTags.Sort).assertIsDisplayed()
    }

    @Test
    fun `enter opens the selected result`() = runAniComposeUiTest {
        val intents = mutableListOf<SearchPageIntent>()
        setSearchPage(selectedItemIndex = 1, onIntent = { intents += it })
        waitForIdle()

        onNodeWithTag(AppleSearchTestTags.result(2)).assertIsSelected()
        onNodeWithTag(MacSearchTestTags.Results).performKeyInput { pressKey(Key.Enter) }
        waitForIdle()
        assertEquals(SearchPageIntent.OpenSubjectDetails(1, results[1]), intents.last())
    }

    @Test
    fun `results do not take focus before a search`() = runAniComposeUiTest {
        setSearchPage(hasActiveSearch = false)
        waitForIdle()
        onNodeWithTag(MacSearchTestTags.Results).assertIsNotFocused()
    }

    @Test
    fun `enter without a selection does nothing`() = runAniComposeUiTest {
        val intents = mutableListOf<SearchPageIntent>()
        setSearchPage(onIntent = { intents += it })
        waitForIdle()

        onNodeWithTag(MacSearchTestTags.Results).performKeyInput { pressKey(Key.Enter) }
        waitForIdle()
        assertEquals(emptyList(), intents.filterIsInstance<SearchPageIntent.OpenSubjectDetails>())
    }
}
