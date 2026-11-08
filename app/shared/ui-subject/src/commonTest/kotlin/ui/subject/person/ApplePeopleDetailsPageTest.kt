/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.person

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import kotlin.test.Test
import kotlin.test.assertEquals
import me.him188.ani.app.data.models.person.CharacterDetailsInfo
import me.him188.ani.app.data.models.person.PersonDetailsInfo
import me.him188.ani.app.ui.comment.UIComment
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.person_details_casts
import me.him188.ani.app.ui.search.createTestPager
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.annotations.TestOnly
import org.jetbrains.compose.resources.stringResource

/**
 * docs/design/ani-style/03-mobile-pages.md §4.5 iOS 人物与角色详情; 05 G6、G45.
 */
@OptIn(TestOnly::class)
class ApplePeopleDetailsPageTest {
    private class Page(val nav: RecordingPeopleNavigation) {
        var meta = ""
        var castsTitle = ""
    }

    private fun ComposeUiTest.setPerson(
        details: PersonDetailsInfo? = TestPeople.personDetails,
        comments: List<UIComment> = TestPeople.comments(),
    ): Page {
        val page = Page(RecordingPeopleNavigation())
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    page.meta = peopleMetaLine(personKindLabel(TestPeople.personDetails.career), TestPeople.personDetails.collects)
                    page.castsTitle = stringResource(Lang.person_details_casts)
                    val scope = rememberCoroutineScope()
                    ApplePersonDetailsLayout(
                        details,
                        TestPeople.PERSON_ID,
                        remember { createTestPager(TestPeople.casts) }.collectAsLazyPagingItems(),
                        remember { createTestPager(TestPeople.works) }.collectAsLazyPagingItems(),
                        remember { TestPeople.commentsState(scope, comments) },
                        onBack = {},
                        Modifier.size(390.dp, 844.dp),
                        navigation = page.nav.navigation,
                    )
                }
            }
        }
        waitForIdle()
        return page
    }

    private fun ComposeUiTest.setCharacter(details: CharacterDetailsInfo? = TestPeople.characterDetails): Page {
        val page = Page(RecordingPeopleNavigation())
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    val scope = rememberCoroutineScope()
                    AppleCharacterDetailsLayout(
                        details,
                        TestPeople.CHARACTER_ID,
                        remember { createTestPager(TestPeople.characterSubjects) }.collectAsLazyPagingItems(),
                        remember { TestPeople.commentsState(scope, TestPeople.comments()) },
                        onBack = {},
                        Modifier.size(390.dp, 844.dp),
                        navigation = page.nav.navigation,
                    )
                }
            }
        }
        waitForIdle()
        return page
    }

    private fun ComposeUiTest.scrollTo(tag: String) {
        onNodeWithTag(ApplePeopleDetailsTestTags.List).performScrollToNode(hasTestTag(tag))
        waitForIdle()
    }

    @Test
    fun `person header shows the meta line and stats`() = runAniComposeUiTest {
        val page = setPerson()
        onNodeWithText("Person Name").assertIsDisplayed()
        onNodeWithTag(ApplePeopleDetailsTestTags.Meta).assertTextContains(page.meta)
        // 人物页没有日文名一行
        onNodeWithText("Original Person").assertDoesNotExist()
        assertTextInside(ApplePeopleDetailsTestTags.Stats, "120")
        assertTextInside(ApplePeopleDetailsTestTags.Stats, "812")
        assertTextInside(ApplePeopleDetailsTestTags.Stats, "56")
    }

    @Test
    fun `loading shows dashes in the stats`() = runAniComposeUiTest {
        setPerson(details = null)
        assertTextInside(ApplePeopleDetailsTestTags.Stats, "–")
    }

    @Test
    fun `cast and work cards navigate`() = runAniComposeUiTest {
        val page = setPerson()
        onNodeWithTag(ApplePeopleDetailsTestTags.cast(201)).performClick()
        scrollTo(ApplePeopleDetailsTestTags.section("works"))
        onNodeWithTag(ApplePeopleDetailsTestTags.work(301)).performClick()
        assertEquals(listOf(201), page.nav.characters)
        assertEquals(listOf(301), page.nav.subjects)
    }

    @Test
    fun `view all opens the existing sheet`() = runAniComposeUiTest {
        val page = setPerson()
        // 统计卡标签与区块标题
        val before = onAllNodesWithText(page.castsTitle).fetchSemanticsNodes().size
        onNodeWithTag(ApplePeopleDetailsTestTags.viewAll("casts")).performClick()
        waitForIdle()
        // 多出面板标题
        onAllNodesWithText(page.castsTitle).assertCountEquals(before + 1)
    }

    @Test
    fun `comments preview shows three and opens all comments`() = runAniComposeUiTest {
        setPerson()
        scrollTo(ApplePeopleDetailsTestTags.Comments)
        onNodeWithTag(ApplePeopleDetailsTestTags.comment("test:0")).assertExists()
        onNodeWithTag(ApplePeopleDetailsTestTags.comment("test:2")).assertExists()
        onNodeWithTag(ApplePeopleDetailsTestTags.comment("test:3")).assertDoesNotExist()

        onNodeWithTag(ApplePeopleDetailsTestTags.AllComments).performClick()
        waitForIdle()
        onNodeWithTag(PersonCommentsSheetTestTags.WriteComment).assertExists()
    }

    @Test
    fun `summary and basic info follow the strips`() = runAniComposeUiTest {
        setPerson()
        scrollTo(ApplePeopleDetailsTestTags.Info)
        onNodeWithText("A short summary.").assertExists()
        assertTextInside(ApplePeopleDetailsTestTags.Info, "Feb 25")
    }

    @Test
    fun `share opens the link menu`() = runAniComposeUiTest {
        setPerson()
        onNodeWithTag(ApplePeopleDetailsTestTags.CollapsedBar).assertDoesNotExist()
        onNodeWithTag(ApplePeopleDetailsTestTags.Share).performClick()
        waitForIdle()
        onNodeWithTag(ApplePeopleDetailsTestTags.ShareMenu).assertExists()
    }

    @Test
    fun `scrolling past the header collapses the top bar`() = runAniComposeUiTest {
        setPerson()
        scrollTo(ApplePeopleDetailsTestTags.Comments)
        onNodeWithTag(ApplePeopleDetailsTestTags.CollapsedBar).assertExists()
    }

    @Test
    fun `character page shows the original name and voice actors`() = runAniComposeUiTest {
        val page = setCharacter()
        onNodeWithText("Character Name").assertIsDisplayed()
        onNodeWithText("Original Character").assertIsDisplayed()
        // 出演作品 2 / 声优 2 / 评论 3
        assertTextInside(ApplePeopleDetailsTestTags.Stats, "3")

        scrollTo(ApplePeopleDetailsTestTags.VoiceActors)
        onNodeWithTag(ApplePeopleDetailsTestTags.voiceActor(22)).performClick()
        assertEquals(listOf(22), page.nav.persons)

        scrollTo(ApplePeopleDetailsTestTags.section("subjects"))
        onNodeWithTag(ApplePeopleDetailsTestTags.work(401)).performClick()
        assertEquals(listOf(401), page.nav.subjects)
    }
}
