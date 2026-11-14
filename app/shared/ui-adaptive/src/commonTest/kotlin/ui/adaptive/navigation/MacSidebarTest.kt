/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.adaptive.navigation

import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.LocalPlatformFontFamily
import me.him188.ani.app.ui.foundation.PlatformFontFamily
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.theme.aniColorScheme
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.theme.DefaultSeedColor
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * docs/design/ani-style/02-components.md C17 macOS 主侧边栏.
 */
class MacSidebarTest {
    @Test
    fun `sidebar items select and the footer buttons click`() = runAniComposeUiTest {
        var selected by mutableStateOf("explore")
        var query by mutableStateOf("")
        var opened by mutableStateOf("")
        setContent {
            CompositionLocalProvider(
                LocalPlatform provides Platform.MacOS(Arch.AARCH64),
                LocalPlatformFontFamily provides PlatformFontFamily(null),
            ) {
                MaterialTheme(colorScheme = aniColorScheme(DefaultSeedColor, isDark = false)) {
                    MacSidebar(
                        onHideSidebar = { opened = "hide" },
                        hideSidebarContentDescription = "Hide sidebar",
                        modifier = Modifier.height(800.dp).testTag("sidebar"),
                        search = {
                            MacSidebarSearchField(
                                query = query,
                                onQueryChange = { query = it },
                                placeholder = "Search",
                                modifier = Modifier.testTag("search"),
                            )
                        },
                        footer = {
                            MacSidebarAccount(
                                name = "Ani",
                                status = "2 pending",
                                initial = "A",
                                onClick = { opened = "account" },
                                modifier = Modifier.weight(1f),
                            )
                            MacSidebarIconButton(
                                onClick = { opened = "notifications" },
                                icon = AppleIcons.bell(),
                                contentDescription = "Notifications",
                                alert = true,
                            )
                            MacSidebarIconButton(
                                onClick = { opened = "settings" },
                                icon = AppleIcons.settings(),
                                contentDescription = "Settings",
                            )
                        },
                    ) {
                        MacSidebarSection("Browse") {
                            MacSidebarItem(
                                selected = selected == "explore",
                                onClick = { selected = "explore" },
                                icon = AppleIcons.explore(),
                                label = "Explore",
                                modifier = Modifier.testTag("explore"),
                            )
                        }
                        MacSidebarSection("Library") {
                            MacSidebarItem(
                                selected = selected == "collection",
                                onClick = { selected = "collection" },
                                icon = AppleIcons.collection(),
                                label = "Collection",
                                modifier = Modifier.testTag("collection"),
                                alert = true,
                            )
                            MacSidebarItem(
                                selected = selected == "cache",
                                onClick = { selected = "cache" },
                                icon = AppleIcons.download(),
                                label = "Cache",
                                count = "2",
                            )
                        }
                    }
                }
            }
        }
        val sidebar = onNodeWithTag("sidebar").getBoundsInRoot()
        assertEquals(240f, (sidebar.right - sidebar.left).value, 0.5f)
        val item = onNodeWithTag("explore").getBoundsInRoot()
        assertEquals(32f, (item.bottom - item.top).value, 0.5f)
        // 搜索框在顶部 52 之下.
        assertEquals(52f, onNodeWithTag("search").getBoundsInRoot().top.value, 0.5f)

        onNodeWithTag("explore").assertIsSelected()
        onNodeWithTag("collection").performClick()
        waitForIdle()
        onNodeWithTag("collection").assertIsSelected()
        onNodeWithTag("explore").assertIsNotSelected()
        onNodeWithText("2").assertExists()

        onNode(hasSetTextAction()).performTextInput("Frieren")
        waitForIdle()
        assertEquals("Frieren", query)

        onNodeWithContentDescription("Notifications").performClick()
        waitForIdle()
        assertEquals("notifications", opened)
        onNodeWithContentDescription("Hide sidebar").performClick()
        waitForIdle()
        assertEquals("hide", opened)
        onNodeWithText("Ani").performClick()
        waitForIdle()
        assertEquals("account", opened)
    }
}
