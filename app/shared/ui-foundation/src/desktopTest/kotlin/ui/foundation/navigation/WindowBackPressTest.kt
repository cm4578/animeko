/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import me.him188.ani.app.ui.framework.AniComposeUiTest
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A back press on desktop ([OnBackPressedDispatcher] with the window's navigation event dispatcher) goes to
 * the topmost of the window's focusable popups and dialogs and the app's [BackHandler]s, and leaves the page
 * only when nothing is open.
 */
class WindowBackPressTest {
    @Test
    fun `back press dismisses the focusable popup instead of the back handler below it`() = runAniComposeUiTest {
        var popupShown by mutableStateOf(true)
        var handlerCount = 0
        val window = setWindowContent {
            BackHandler { handlerCount++ }
            if (popupShown) {
                Popup(onDismissRequest = { popupShown = false }, properties = PopupProperties(focusable = true)) {
                    Box(Modifier.size(50.dp).testTag("popup"))
                }
            }
        }
        onNodeWithTag("popup").assertExists()

        runOnIdle { window.dispatcher.onBackPressed() }
        onNodeWithTag("popup").assertDoesNotExist()
        runOnIdle {
            assertEquals(0, handlerCount)
            assertEquals(0, window.fallbackCount)
        }

        runOnIdle { window.dispatcher.onBackPressed() }
        runOnIdle {
            assertEquals(1, handlerCount)
            assertEquals(0, window.fallbackCount)
        }
    }

    @Test
    fun `back press dismisses the dialog and keeps the page`() = runAniComposeUiTest {
        var dialogShown by mutableStateOf(true)
        val window = setWindowContent {
            if (dialogShown) {
                Dialog(onDismissRequest = { dialogShown = false }) {
                    Box(Modifier.size(50.dp).testTag("dialog"))
                }
            }
        }
        onNodeWithTag("dialog").assertExists()

        runOnIdle { window.dispatcher.onBackPressed() }
        onNodeWithTag("dialog").assertDoesNotExist()
        runOnIdle { assertEquals(0, window.fallbackCount) }

        runOnIdle { window.dispatcher.onBackPressed() }
        runOnIdle { assertEquals(1, window.fallbackCount) }
    }

    @Test
    fun `back handler inside a dialog comes before the dialog itself`() = runAniComposeUiTest {
        var dialogShown by mutableStateOf(true)
        var innerHandlerEnabled by mutableStateOf(true)
        var innerHandlerCount = 0
        val window = setWindowContent {
            if (dialogShown) {
                Dialog(onDismissRequest = { dialogShown = false }) {
                    BackHandler(enabled = innerHandlerEnabled) {
                        innerHandlerCount++
                        innerHandlerEnabled = false
                    }
                    Box(Modifier.size(50.dp).testTag("dialog"))
                }
            }
        }

        runOnIdle { window.dispatcher.onBackPressed() }
        onNodeWithTag("dialog").assertExists()
        runOnIdle { assertEquals(1, innerHandlerCount) }

        runOnIdle { window.dispatcher.onBackPressed() }
        onNodeWithTag("dialog").assertDoesNotExist()
        runOnIdle {
            assertEquals(1, innerHandlerCount)
            assertEquals(0, window.fallbackCount)
        }
    }

    @Test
    fun `nested popups are dismissed from the top`() = runAniComposeUiTest {
        var outerShown by mutableStateOf(true)
        var innerShown by mutableStateOf(true)
        val window = setWindowContent {
            if (outerShown) {
                Popup(onDismissRequest = { outerShown = false }, properties = PopupProperties(focusable = true)) {
                    Box(Modifier.size(50.dp).testTag("outer"))
                    if (innerShown) {
                        Popup(
                            onDismissRequest = { innerShown = false },
                            properties = PopupProperties(focusable = true),
                        ) {
                            Box(Modifier.size(20.dp).testTag("inner"))
                        }
                    }
                }
            }
        }

        runOnIdle { window.dispatcher.onBackPressed() }
        onNodeWithTag("inner").assertDoesNotExist()
        onNodeWithTag("outer").assertExists()

        runOnIdle { window.dispatcher.onBackPressed() }
        onNodeWithTag("outer").assertDoesNotExist()
        runOnIdle { assertEquals(0, window.fallbackCount) }
    }

    @Test
    fun `focusable popup that ignores back presses still keeps the page`() = runAniComposeUiTest {
        var handlerCount = 0
        val window = setWindowContent {
            BackHandler { handlerCount++ }
            Popup(
                onDismissRequest = {},
                properties = PopupProperties(focusable = true, dismissOnBackPress = false),
            ) {
                Box(Modifier.size(50.dp).testTag("popup"))
            }
        }

        runOnIdle { window.dispatcher.onBackPressed() }
        onNodeWithTag("popup").assertExists()
        runOnIdle {
            assertEquals(0, handlerCount)
            assertEquals(0, window.fallbackCount)
        }
    }

    @Test
    fun `navigation 3 back handling does not take back presses from back handlers`() = runAniComposeUiTest {
        var navDisplayBackCount = 0
        var handlerEnabled by mutableStateOf(true)
        var handlerCount = 0
        val window = setWindowContent {
            // Registered before NavDisplay, so NavDisplay's back handler is newer in the window's dispatcher.
            BackHandler(enabled = handlerEnabled) { handlerCount++ }
            NavDisplay(
                backStack = listOf("first", "second"),
                onBack = { navDisplayBackCount++ },
                entryProvider = { key -> NavEntry(key) { Box(Modifier.size(50.dp).testTag(it)) } },
            )
        }
        onNodeWithTag("second").assertExists()

        runOnIdle { window.dispatcher.onBackPressed() }
        runOnIdle {
            assertEquals(1, handlerCount)
            assertEquals(0, navDisplayBackCount)
            assertEquals(0, window.fallbackCount)
        }

        runOnIdle { handlerEnabled = false }
        runOnIdle { window.dispatcher.onBackPressed() }
        runOnIdle {
            assertEquals(1, handlerCount)
            assertEquals(0, navDisplayBackCount)
            assertEquals(1, window.fallbackCount)
        }
    }

    @Test
    fun `popup on a page is dismissed before the page goes back`() = runAniComposeUiTest {
        var popupShown by mutableStateOf(true)
        var navDisplayBackCount = 0
        val window = setWindowContent {
            NavDisplay(
                backStack = listOf("first", "second"),
                onBack = { navDisplayBackCount++ },
                entryProvider = { key ->
                    NavEntry(key) {
                        Box(Modifier.size(50.dp).testTag(it))
                        if (it == "second" && popupShown) {
                            Popup(
                                onDismissRequest = { popupShown = false },
                                properties = PopupProperties(focusable = true),
                            ) {
                                Box(Modifier.size(20.dp).testTag("popup"))
                            }
                        }
                    }
                },
            )
        }
        onNodeWithTag("popup").assertExists()

        runOnIdle { window.dispatcher.onBackPressed() }
        onNodeWithTag("popup").assertDoesNotExist()
        runOnIdle { assertEquals(0, window.fallbackCount) }

        runOnIdle { window.dispatcher.onBackPressed() }
        runOnIdle {
            assertEquals(1, window.fallbackCount)
            assertEquals(0, navDisplayBackCount)
        }
    }

    private class WindowFixture {
        lateinit var dispatcher: OnBackPressedDispatcher
        var fallbackCount = 0
    }

    /**
     * Wires [content] the way the desktop main window does: the app's dispatcher knows the window's
     * navigation event dispatcher, where popups, dialogs and `NavDisplay` register their back handling.
     */
    private fun AniComposeUiTest.setWindowContent(content: @Composable () -> Unit): WindowFixture {
        val fixture = WindowFixture()
        setContent {
            val navigationEventDispatcher =
                checkNotNull(LocalNavigationEventDispatcherOwner.current).navigationEventDispatcher
            val dispatcher = remember(navigationEventDispatcher) {
                OnBackPressedDispatcher(
                    fallback = { fixture.fallbackCount++ },
                    navigationEventDispatcher = navigationEventDispatcher,
                )
            }
            SideEffect { fixture.dispatcher = dispatcher }
            val lifecycleOwner = LocalLifecycleOwner.current
            val owner = remember(dispatcher, lifecycleOwner) {
                SkikoOnBackPressedDispatcherOwner(dispatcher, lifecycleOwner)
            }
            CompositionLocalProvider(LocalOnBackPressedDispatcherOwner provides owner) {
                content()
            }
        }
        return fixture
    }
}
