/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.main

import androidx.compose.runtime.mutableStateListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.MainScreenPage
import me.him188.ani.app.navigation.NavRoutes
import me.him188.ani.app.navigation.SettingsTab
import me.him188.ani.app.ui.settings.MacSettingsCategory

/**
 * 05 B4 macOS 设定窗口: 窗口状态与主窗口的设定入口.
 */
class MacSettingsWindowTest {
    @Test
    fun `open switches the category and closing keeps it`() {
        val state = MacSettingsWindowState()
        assertFalse(state.isOpen)
        assertEquals(MacSettingsCategory.Tab(SettingsTab.PROFILE), state.category)

        state.open(SettingsTab.UPDATE)
        assertTrue(state.isOpen)
        assertEquals(MacSettingsCategory.Tab(SettingsTab.UPDATE), state.category)
        assertEquals(1, state.openRequests)

        // 再次打开 (例如 ⌘,): 停在原分类, 由宿主把窗口带到前面
        state.open()
        assertEquals(MacSettingsCategory.Tab(SettingsTab.UPDATE), state.category)
        assertEquals(2, state.openRequests)

        state.close()
        assertFalse(state.isOpen)
        assertEquals(MacSettingsCategory.Tab(SettingsTab.UPDATE), state.category)
    }

    @Test
    fun `settings navigation opens the settings window instead of a page`() {
        val backStack = mutableStateListOf<NavRoutes>(NavRoutes.Main(MainScreenPage.Exploration))
        val state = MacSettingsWindowState()
        val navigator = MacSettingsWindowNavigator(AniNavigator().apply { setBackStack(backStack) }, state)

        navigator.navigateSettings(SettingsTab.ABOUT)
        assertTrue(state.isOpen)
        assertEquals(MacSettingsCategory.Tab(SettingsTab.ABOUT), state.category)

        navigator.navigate(NavRoutes.Settings(SettingsTab.PROXY))
        assertEquals(MacSettingsCategory.Tab(SettingsTab.PROXY), state.category)
        assertEquals(listOf<NavRoutes>(NavRoutes.Main(MainScreenPage.Exploration)), backStack.toList())

        // 其他页面照常推入主窗口
        navigator.navigatePlaybackHistory()
        assertEquals(NavRoutes.PlaybackHistory, backStack.last())
    }
}
