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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.paging.compose.collectAsLazyPagingItems
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.search.createTestPager
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.annotations.TestOnly

/**
 * docs/design/ani-style/04-macos.md §4.4 macOS 人物与角色详情; 05 G6、G45.
 */
@OptIn(TestOnly::class)
class MacPeopleDetailsPageTest {
    private class Page(val nav: RecordingPeopleNavigation) {
        val opened = mutableListOf<String>()
        var meta = ""
        var characterMeta = ""
    }

    private fun recordingUriHandler(page: Page) = object : UriHandler {
        override fun openUri(uri: String) {
            page.opened += uri
        }
    }

    private fun ComposeUiTest.setPerson(): Page {
        val page = Page(RecordingPeopleNavigation())
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(
                    LocalPlatform provides Platform.MacOS(Arch.AARCH64),
                    LocalUriHandler provides remember { recordingUriHandler(page) },
                ) {
                    page.meta = "Original Person · " +
                            peopleMetaLine(personKindLabel(TestPeople.personDetails.career), TestPeople.personDetails.collects)
                    val scope = rememberCoroutineScope()
                    MacPersonDetailsLayout(
                        TestPeople.personDetails,
                        TestPeople.PERSON_ID,
                        remember { createTestPager(TestPeople.casts) }.collectAsLazyPagingItems(),
                        remember { createTestPager(TestPeople.works) }.collectAsLazyPagingItems(),
                        remember { TestPeople.commentsState(scope, TestPeople.comments()) },
                        Modifier.size(1240.dp, 900.dp),
                        navigation = page.nav.navigation,
                    )
                }
            }
        }
        waitForIdle()
        return page
    }

    private fun ComposeUiTest.setCharacter(): Page {
        val page = Page(RecordingPeopleNavigation())
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    page.characterMeta = "Original Character · " +
                            peopleMetaLine(characterRoleLabel(TestPeople.characterDetails.role), TestPeople.characterDetails.collects)
                    val scope = rememberCoroutineScope()
                    MacCharacterDetailsLayout(
                        TestPeople.characterDetails,
                        TestPeople.CHARACTER_ID,
                        remember { createTestPager(TestPeople.characterSubjects) }.collectAsLazyPagingItems(),
                        remember { TestPeople.commentsState(scope, TestPeople.comments()) },
                        Modifier.size(1240.dp, 900.dp),
                        navigation = page.nav.navigation,
                    )
                }
            }
        }
        waitForIdle()
        return page
    }

    @Test
    fun `sidebar is 240 wide with stats and basic info`() = runAniComposeUiTest {
        setPerson()
        val sidebar = onNodeWithTag(MacPeopleDetailsTestTags.Sidebar).getUnclippedBoundsInRoot()
        assertEquals(240f, (sidebar.right - sidebar.left).value, 0.5f)
        assertTextInside(MacPeopleDetailsTestTags.Stats, "120")
        assertTextInside(MacPeopleDetailsTestTags.Stats, "812")
        assertTextInside(MacPeopleDetailsTestTags.Info, "Feb 25")
    }

    @Test
    fun `person meta joins the original name`() = runAniComposeUiTest {
        val page = setPerson()
        onNodeWithTag(MacPeopleDetailsTestTags.Meta).assertTextContains(page.meta)
        assertTextInside(MacPeopleDetailsTestTags.Summary, "A short summary.")
    }

    @Test
    fun `cast grid shows two rows of five and view all shows the total`() = runAniComposeUiTest {
        val page = setPerson()
        (201..210).forEach { onNodeWithTag(MacPeopleDetailsTestTags.cast(it)).assertExists() }
        onNodeWithTag(MacPeopleDetailsTestTags.cast(211)).assertDoesNotExist()
        onNodeWithTag(MacPeopleDetailsTestTags.viewAll("casts")).assertTextContains("812", substring = true)

        val first = onNodeWithTag(MacPeopleDetailsTestTags.cast(201)).getUnclippedBoundsInRoot()
        val fifth = onNodeWithTag(MacPeopleDetailsTestTags.cast(205)).getUnclippedBoundsInRoot()
        val sixth = onNodeWithTag(MacPeopleDetailsTestTags.cast(206)).getUnclippedBoundsInRoot()
        assertEquals(first.top, fifth.top)
        assertTrue(sixth.top > first.bottom, "the sixth cast starts the second row")

        onNodeWithTag(MacPeopleDetailsTestTags.cast(203)).performClick()
        assertEquals(listOf(203), page.nav.characters)
    }

    @Test
    fun `works grid shows three rows of three`() = runAniComposeUiTest {
        val page = setPerson()
        (301..309).forEach { onNodeWithTag(MacPeopleDetailsTestTags.work(it)).assertExists() }
        onNodeWithTag(MacPeopleDetailsTestTags.work(310)).assertDoesNotExist()
        onNodeWithTag(MacPeopleDetailsTestTags.work(305)).performScrollTo().performClick()
        assertEquals(listOf(305), page.nav.subjects)
    }

    @Test
    fun `comments show four cards in two columns`() = runAniComposeUiTest {
        setPerson()
        (0..3).forEach { onNodeWithTag(MacPeopleDetailsTestTags.comment("test:$it")).assertExists() }
        onNodeWithTag(MacPeopleDetailsTestTags.comment("test:4")).assertDoesNotExist()
        val first = onNodeWithTag(MacPeopleDetailsTestTags.comment("test:0")).getUnclippedBoundsInRoot()
        val second = onNodeWithTag(MacPeopleDetailsTestTags.comment("test:1")).getUnclippedBoundsInRoot()
        val third = onNodeWithTag(MacPeopleDetailsTestTags.comment("test:2")).getUnclippedBoundsInRoot()
        assertEquals(first.top, second.top)
        assertTrue(second.left > first.right)
        assertTrue(third.top > first.bottom)

        onNodeWithTag(MacPeopleDetailsTestTags.AllComments).performScrollTo().performClick()
        waitForIdle()
        onNodeWithTag(PersonCommentsSheetTestTags.WriteComment).assertExists()
    }

    @Test
    fun `toolbar opens Bangumi and the share menu`() = runAniComposeUiTest {
        val page = setPerson()
        onNodeWithTag(MacPeopleDetailsTestTags.OpenInBangumi).performClick()
        assertEquals(listOf("https://bgm.tv/person/${TestPeople.PERSON_ID}"), page.opened)
        onNodeWithTag(MacPeopleDetailsTestTags.Share).performClick()
        waitForIdle()
        onNodeWithTag(MacPeopleDetailsTestTags.ShareMenu).assertExists()
    }

    @Test
    fun `character shows voice actors and marks main roles`() = runAniComposeUiTest {
        val page = setCharacter()
        onNodeWithTag(MacPeopleDetailsTestTags.Meta).assertTextContains(page.characterMeta)
        onNodeWithTag(MacPeopleDetailsTestTags.voiceActor(21)).performClick()
        assertEquals(listOf(21), page.nav.persons)

        // 只有主角的作品有「主角」标记. 标记合并在可点击的卡片里, 在未合并的语义树查找.
        onAllNodesWithTag(MacPeopleDetailsTestTags.Badge, useUnmergedTree = true).assertCountEquals(1)
        onNodeWithTag(MacPeopleDetailsTestTags.work(402)).performScrollTo().performClick()
        assertEquals(listOf(402), page.nav.subjects)
    }
}
