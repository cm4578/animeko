/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.adaptive.navigation

import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import me.him188.ani.app.ui.foundation.LocalPlatformFontFamily
import me.him188.ani.app.ui.foundation.PlatformFontFamily
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.theme.aniColorScheme
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.theme.DefaultSeedColor
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * docs/design/ani-style/02-components.md C1 浮动胶囊导航栏.
 */
class FloatingTabBarTest {
    @Test
    fun `tab bar items are tabs that follow the selection`() = runAniComposeUiTest {
        var selected by mutableStateOf(0)
        setContent {
            CompositionLocalProvider(LocalPlatformFontFamily provides PlatformFontFamily(null)) {
                MaterialTheme(colorScheme = aniColorScheme(DefaultSeedColor, isDark = false)) {
                    FloatingTabBar(Modifier.testTag("bar"), windowInsets = WindowInsets(0)) {
                        FloatingTabBarItem(
                            selected = selected == 0,
                            onClick = { selected = 0 },
                            icon = AppleIcons.explore(),
                            selectedIcon = AppleIcons.explore(2f),
                            label = "Explore",
                            modifier = Modifier.testTag("explore"),
                        )
                        FloatingTabBarItem(
                            selected = selected == 1,
                            onClick = { selected = 1 },
                            icon = AppleIcons.collection(),
                            selectedIcon = AppleIcons.collection(2f),
                            label = "Collection",
                            modifier = Modifier.testTag("collection"),
                            alert = true,
                        )
                    }
                }
            }
        }
        onNodeWithTag("explore").assertIsSelected()
        onNodeWithTag("collection").assertIsNotSelected()

        onNodeWithTag("collection").performClick()
        waitForIdle()
        onNodeWithTag("collection").assertIsSelected()
        onNodeWithTag("explore").assertIsNotSelected()

        // 导航栏高 64, 外加距底 20.
        val bar = onNodeWithTag("bar").getBoundsInRoot()
        assertEquals(84f, (bar.bottom - bar.top).value, 0.5f)
        val item = onNodeWithTag("explore").getBoundsInRoot()
        assertEquals(52f, (item.bottom - item.top).value, 0.5f)
    }
}
