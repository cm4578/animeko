/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.main

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.MainScreenPage
import me.him188.ani.app.navigation.NavRoutes
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.LocalPlatformFontFamily
import me.him188.ani.app.ui.foundation.PlatformFontFamily
import me.him188.ani.app.ui.foundation.apple.MacPageToolbar
import me.him188.ani.app.ui.foundation.theme.aniColorScheme
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.main_sidebar_hide
import me.him188.ani.app.ui.lang.main_sidebar_show
import me.him188.ani.app.ui.lang.main_toolbar_back
import me.him188.ani.app.ui.lang.main_toolbar_forward
import me.him188.ani.app.ui.theme.DefaultSeedColor
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * docs/design/ani-style/04-macos.md §1.3、§3.1; 05 B3、G11、G31.
 */
class MacNavigationShellTest {
    private val tabs = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private class Labels {
        var hideSidebar = ""
        var showSidebar = ""
        var back = ""
        var forward = ""
    }

    private val status = MacSidebarStatus(
        selfInfo = null,
        notificationDot = false,
        pendingSyncCount = 0,
        hasNewEpisodes = true,
        activeDownloadCount = 2,
    )

    private fun ComposeUiTest.setShell(
        navigator: AniNavigator,
        backStack: MutableList<NavRoutes>,
        pages: AppleMainPages,
        searchField: MacSearchFieldState = MacSearchFieldState(),
        withToolbar: Boolean = true,
    ): Labels {
        val labels = Labels()
        setContent {
            labels.hideSidebar = stringResource(Lang.main_sidebar_hide)
            labels.showSidebar = stringResource(Lang.main_sidebar_show)
            labels.back = stringResource(Lang.main_toolbar_back)
            labels.forward = stringResource(Lang.main_toolbar_forward)
            CompositionLocalProvider(
                LocalPlatform provides Platform.MacOS(Arch.AARCH64),
                LocalPlatformFontFamily provides PlatformFontFamily(null),
            ) {
                MaterialTheme(colorScheme = aniColorScheme(DefaultSeedColor, isDark = false)) {
                    MacNavigationShell(
                        backStack,
                        navigator,
                        pages,
                        status,
                        searchField,
                        onOpenAccount = {},
                        onOpenSettings = {},
                    ) {
                        Column {
                            if (withToolbar) {
                                MacPageToolbar()
                            }
                            val top = backStack.last()
                            Text(if (top is NavRoutes.Main) "page ${pages.pageOf(top)}" else "route $top")
                        }
                    }
                }
            }
        }
        return labels
    }

    /**
     * 侧边栏入口的顺序: 浏览 (探索、新番时间表、热度排行), 我的片库 (追番、播放记录、快取).
     */
    private fun ComposeUiTest.entry(entry: MacSidebarEntry) = onAllNodes(tabs)[entry.ordinal]

    @Test
    fun `pushed detail page keeps the source entry selected`() = runAniComposeUiTest {
        val backStack = mutableStateListOf<NavRoutes>(NavRoutes.Main(MainScreenPage.Collection))
        val navigator = AniNavigator().apply { setBackStack(backStack) }
        setShell(navigator, backStack, AppleMainPages(backStack))

        runOnIdle { navigator.navigate(NavRoutes.SubjectDetail(1)) }
        entry(MacSidebarEntry.Collection).assertIsSelected()

        runOnIdle { navigator.navigate(NavRoutes.Schedule()) }
        runOnIdle { navigator.navigate(NavRoutes.SubjectDetail(2)) }
        entry(MacSidebarEntry.Schedule).assertIsSelected()
        entry(MacSidebarEntry.Collection).assertIsNotSelected()
    }

    @Test
    fun `clicking a main page entry returns to main and switches the page`() = runAniComposeUiTest {
        val main = NavRoutes.Main(MainScreenPage.Exploration)
        val backStack = mutableStateListOf<NavRoutes>(main, NavRoutes.Schedule(), NavRoutes.SubjectDetail(1))
        val navigator = AniNavigator().apply { setBackStack(backStack) }
        setShell(navigator, backStack, AppleMainPages(backStack))

        entry(MacSidebarEntry.Collection).performClick()

        onNodeWithText("page Collection").assertExists()
        assertEquals(listOf<NavRoutes>(main), backStack.toList())
        entry(MacSidebarEntry.Collection).assertIsSelected()
    }

    @Test
    fun `clicking a pushed entry opens it on top of main`() = runAniComposeUiTest {
        val main = NavRoutes.Main(MainScreenPage.Collection)
        val backStack = mutableStateListOf<NavRoutes>(main, NavRoutes.SubjectDetail(1))
        val navigator = AniNavigator().apply { setBackStack(backStack) }
        setShell(navigator, backStack, AppleMainPages(backStack))

        entry(MacSidebarEntry.TrendingRanking).performClick()

        assertEquals(listOf(main, NavRoutes.TrendingRanking), backStack.toList())
        entry(MacSidebarEntry.TrendingRanking).assertIsSelected()
    }

    @Test
    fun `clicking the source entry returns to its root`() = runAniComposeUiTest {
        val main = NavRoutes.Main(MainScreenPage.Exploration)
        val backStack = mutableStateListOf<NavRoutes>(main, NavRoutes.Schedule(), NavRoutes.SubjectDetail(1))
        val navigator = AniNavigator().apply { setBackStack(backStack) }
        setShell(navigator, backStack, AppleMainPages(backStack))

        entry(MacSidebarEntry.Schedule).performClick()

        assertEquals(listOf(main, NavRoutes.Schedule()), backStack.toList())
    }

    @Test
    fun `search and notifications select no entry`() {
        val main = NavRoutes.Main(MainScreenPage.Exploration)
        fun selectionOf(vararg routes: NavRoutes): MacSidebarSelection {
            val backStack = listOf(main, *routes)
            return macSidebarSelection(backStack, AppleMainPages(backStack))
        }

        assertEquals(MacSidebarSelection.Search, selectionOf(NavRoutes.SubjectSearch(keyword = "a")))
        assertEquals(
            MacSidebarSelection.Search,
            selectionOf(NavRoutes.SubjectSearch(keyword = "a"), NavRoutes.SubjectDetail(1)),
        )
        assertEquals(
            MacSidebarSelection.Notifications,
            selectionOf(NavRoutes.Notifications, NavRoutes.NotificationDetail("n")),
        )
        assertEquals(
            MacSidebarSelection.Entry(MacSidebarEntry.PlaybackHistory),
            selectionOf(NavRoutes.PlaybackHistory, NavRoutes.PlaybackHistorySyncStatus),
        )
    }

    @Test
    fun `full screen tasks hide the sidebar`() = runAniComposeUiTest {
        val backStack = mutableStateListOf<NavRoutes>(NavRoutes.Main(MainScreenPage.Exploration))
        val navigator = AniNavigator().apply { setBackStack(backStack) }
        setShell(navigator, backStack, AppleMainPages(backStack))
        assertEquals(MacSidebarEntry.entries.size, onAllNodes(tabs).fetchSemanticsNodes().size)

        runOnIdle { navigator.navigate(NavRoutes.EpisodeDetail(1, 2)) }
        waitForIdle()
        assertEquals(0, onAllNodes(tabs).fetchSemanticsNodes().size)
    }

    @Test
    fun `hiding the sidebar shows the show sidebar button in the toolbar`() = runAniComposeUiTest {
        val backStack = mutableStateListOf<NavRoutes>(NavRoutes.Main(MainScreenPage.Exploration))
        val navigator = AniNavigator().apply { setBackStack(backStack) }
        val labels = setShell(navigator, backStack, AppleMainPages(backStack))

        onNodeWithContentDescription(labels.showSidebar).assertDoesNotExist()
        onNodeWithContentDescription(labels.hideSidebar).performClick()
        waitForIdle()
        assertEquals(0, onAllNodes(tabs).fetchSemanticsNodes().size)

        onNodeWithContentDescription(labels.showSidebar).performClick()
        waitForIdle()
        assertEquals(MacSidebarEntry.entries.size, onAllNodes(tabs).fetchSemanticsNodes().size)
        onNodeWithContentDescription(labels.showSidebar).assertDoesNotExist()
    }

    @Test
    fun `pages without the toolbar get the show sidebar button from the shell`() = runAniComposeUiTest {
        val backStack = mutableStateListOf<NavRoutes>(NavRoutes.Main(MainScreenPage.Exploration))
        val navigator = AniNavigator().apply { setBackStack(backStack) }
        val labels = setShell(navigator, backStack, AppleMainPages(backStack), withToolbar = false)

        onNodeWithContentDescription(labels.hideSidebar).performClick()
        waitForIdle()
        onNodeWithContentDescription(labels.showSidebar).performClick()
        waitForIdle()
        assertEquals(MacSidebarEntry.entries.size, onAllNodes(tabs).fetchSemanticsNodes().size)
    }

    @Test
    fun `toolbar back pops the back stack and forward stays disabled`() = runAniComposeUiTest {
        val main = NavRoutes.Main(MainScreenPage.Exploration)
        val backStack = mutableStateListOf<NavRoutes>(main)
        val navigator = AniNavigator().apply { setBackStack(backStack) }
        val labels = setShell(navigator, backStack, AppleMainPages(backStack))

        onNodeWithContentDescription(labels.back).assertIsNotEnabled()
        onNodeWithContentDescription(labels.forward).assertIsNotEnabled()

        runOnIdle { navigator.navigate(NavRoutes.SubjectDetail(1)) }
        onNodeWithContentDescription(labels.back).performClick()
        waitForIdle()
        assertEquals(listOf<NavRoutes>(main), backStack.toList())
        onNodeWithContentDescription(labels.forward).assertIsNotEnabled()
    }

    @Test
    fun `typing in the sidebar search field and pressing enter opens the search page`() = runAniComposeUiTest {
        val main = NavRoutes.Main(MainScreenPage.Collection)
        val backStack = mutableStateListOf<NavRoutes>(main)
        val navigator = AniNavigator().apply { setBackStack(backStack) }
        val searchField = MacSearchFieldState()
        setShell(navigator, backStack, AppleMainPages(backStack), searchField)

        onNode(hasSetTextAction()).performTextInput("Frieren")
        onNode(hasSetTextAction()).performImeAction()
        waitForIdle()

        assertEquals(listOf(main, NavRoutes.SubjectSearch(keyword = "Frieren")), backStack.toList())
        onAllNodes(tabs).fetchSemanticsNodes().forEachIndexed { index, _ -> onAllNodes(tabs)[index].assertIsNotSelected() }

        // 离开搜索后清空搜索框
        entry(MacSidebarEntry.Exploration).performClick()
        waitForIdle()
        assertEquals("", searchField.text)
    }
}
