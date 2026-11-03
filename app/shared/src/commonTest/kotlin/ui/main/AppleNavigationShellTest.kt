/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.main

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.MainScreenPage
import me.him188.ani.app.navigation.NavRoutes
import me.him188.ani.app.navigation.SettingsTab
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.LocalPlatformFontFamily
import me.him188.ani.app.ui.foundation.PlatformFontFamily
import me.him188.ani.app.ui.foundation.theme.aniColorScheme
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.theme.DefaultSeedColor
import me.him188.ani.utils.platform.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

class AppleNavigationShellTest {
    private val tabs = SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Tab)

    private fun ComposeUiTest.setShell(navigator: AniNavigator, backStack: MutableList<NavRoutes>, pages: AppleMainPages) {
        setContent {
            CompositionLocalProvider(
                LocalPlatform provides Platform.Ios,
                LocalPlatformFontFamily provides PlatformFontFamily(null),
            ) {
                MaterialTheme(colorScheme = aniColorScheme(DefaultSeedColor, isDark = false)) {
                    AppleNavigationShell(backStack, navigator, pages, NavigationSuiteType.NavigationBar) {
                        val top = backStack.last()
                        Text(if (top is NavRoutes.Main) "page ${pages.pageOf(top)}" else "route $top")
                    }
                }
            }
        }
    }

    private fun ComposeUiTest.tabNodes() = onAllNodes(tabs)

    @Test
    fun `pushed page shows the tab bar with the source tab selected`() = runAniComposeUiTest {
        val backStack = mutableStateListOf<NavRoutes>(NavRoutes.Main(MainScreenPage.Collection))
        val navigator = AniNavigator().apply { setBackStack(backStack) }
        val pages = AppleMainPages(backStack)
        setShell(navigator, backStack, pages)

        runOnIdle { navigator.navigate(NavRoutes.Schedule()) }
        onNodeWithText("route ${NavRoutes.Schedule()}").assertExists()
        tabNodes()[MainScreenPage.visibleEntries.indexOf(MainScreenPage.Collection)].assertIsSelected()
    }

    @Test
    fun `tapping a tab on a pushed page returns to main and switches the page`() = runAniComposeUiTest {
        val main = NavRoutes.Main(MainScreenPage.Exploration)
        val backStack = mutableStateListOf<NavRoutes>(main, NavRoutes.Schedule(), NavRoutes.TrendingRanking)
        val navigator = AniNavigator().apply { setBackStack(backStack) }
        val pages = AppleMainPages(backStack)
        setShell(navigator, backStack, pages)

        val collectionIndex = MainScreenPage.visibleEntries.indexOf(MainScreenPage.Collection)
        tabNodes()[collectionIndex].performClick()

        onNodeWithText("page Collection").assertExists()
        assertEquals(listOf<NavRoutes>(main), backStack.toList())
        tabNodes()[collectionIndex].assertIsSelected()
    }

    @Test
    fun `tapping the source tab on a pushed page returns to its root`() = runAniComposeUiTest {
        val main = NavRoutes.Main(MainScreenPage.Exploration)
        val backStack = mutableStateListOf<NavRoutes>(main, NavRoutes.Schedule())
        val navigator = AniNavigator().apply { setBackStack(backStack) }
        val pages = AppleMainPages(backStack)
        setShell(navigator, backStack, pages)

        tabNodes()[MainScreenPage.visibleEntries.indexOf(MainScreenPage.Exploration)].performClick()

        onNodeWithText("page Exploration").assertExists()
        assertEquals(listOf<NavRoutes>(main), backStack.toList())
    }

    @Test
    fun `full screen tasks hide the tab bar`() = runAniComposeUiTest {
        val backStack = mutableStateListOf<NavRoutes>(NavRoutes.Main(MainScreenPage.Exploration))
        val navigator = AniNavigator().apply { setBackStack(backStack) }
        setShell(navigator, backStack, AppleMainPages(backStack))

        runOnIdle { navigator.navigate(NavRoutes.EpisodeDetail(1, 2)) }
        waitForIdle()
        assertEquals(0, tabNodes().fetchSemanticsNodes().size)
    }

    @Test
    fun `each main entry keeps its own page`() {
        val first = NavRoutes.Main(MainScreenPage.Exploration)
        val second = NavRoutes.Main(MainScreenPage.Exploration)
        val backStack = mutableListOf<NavRoutes>(first, NavRoutes.Schedule(), second)
        val pages = AppleMainPages(backStack)

        pages.setPage(second, MainScreenPage.CacheManagement)

        assertEquals(MainScreenPage.Exploration, pages.pageOf(first))
        assertEquals(MainScreenPage.CacheManagement, pages.pageOf(second))
    }

    @Test
    fun `pages are restored by back stack position`() {
        val backStack = mutableListOf<NavRoutes>(NavRoutes.Main(MainScreenPage.Exploration), NavRoutes.Schedule())
        val pages = AppleMainPages(backStack)
        pages.setPage(backStack[0] as NavRoutes.Main, MainScreenPage.Collection)

        val saver = AppleMainPages.saver(backStack)
        val saved = with(saver) { SaverScope { true }.save(pages) }!!
        // 进程重建后返回栈是新的实例
        val restoredStack = mutableListOf<NavRoutes>(NavRoutes.Main(MainScreenPage.Exploration), NavRoutes.Schedule())
        val restored = AppleMainPages.saver(restoredStack).restore(saved)!!

        assertEquals(MainScreenPage.Collection, restored.pageOf(restoredStack[0] as NavRoutes.Main))
    }

    @Test
    fun `tab bar visibility follows the components spec`() {
        assertEquals(AppleTabBarMode.Overlay, NavRoutes.Main(MainScreenPage.Exploration).appleTabBarMode())
        assertEquals(AppleTabBarMode.Overlay, NavRoutes.Schedule().appleTabBarMode())
        assertEquals(AppleTabBarMode.Overlay, NavRoutes.SubjectDetail(1).appleTabBarMode())
        assertEquals(AppleTabBarMode.Inset, NavRoutes.TrendingRanking.appleTabBarMode())
        assertEquals(AppleTabBarMode.Inset, NavRoutes.Settings(SettingsTab.THEME).appleTabBarMode())
        assertEquals(AppleTabBarMode.Hidden, NavRoutes.Settings(SettingsTab.PROFILE).appleTabBarMode())
        assertEquals(AppleTabBarMode.Hidden, NavRoutes.EpisodeDetail(1, 2).appleTabBarMode())
        assertEquals(AppleTabBarMode.Hidden, NavRoutes.SubjectSearch().appleTabBarMode())
        assertEquals(AppleTabBarMode.Hidden, NavRoutes.BangumiMerge.appleTabBarMode())
        assertEquals(AppleTabBarMode.Hidden, NavRoutes.EmailLoginStart.appleTabBarMode())
    }
}
