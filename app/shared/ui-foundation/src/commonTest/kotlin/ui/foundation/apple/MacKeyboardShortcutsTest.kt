/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.test.withKeyDown
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * 05 C2 macOS 快捷键的注册与分派. 测试中由根节点的 `onKeyEvent` 代替主窗口的 `Window(onKeyEvent)`.
 */
@OptIn(ExperimentalTestApi::class)
class MacKeyboardShortcutsTest {
    @Test
    fun `the latest registration handles command shortcuts until it leaves`() = runAniComposeUiTest {
        val shortcuts = MacKeyboardShortcuts()
        var showSecond by mutableStateOf(true)
        val triggered = mutableListOf<String>()
        setContent {
            CompositionLocalProvider(LocalMacKeyboardShortcuts provides shortcuts) {
                val focusRequester = remember { FocusRequester() }
                Box(
                    Modifier
                        .size(10.dp)
                        .testTag("root")
                        .focusRequester(focusRequester)
                        .focusable()
                        .onKeyEvent { shortcuts.dispatch(it) },
                ) {
                    MacKeyboardShortcut(Key.R) { triggered += "first" }
                    if (showSecond) {
                        MacKeyboardShortcut(Key.R) { triggered += "second" }
                    }
                    MacKeyboardShortcut(Key.T, enabled = false) { triggered += "disabled" }
                }
                LaunchedEffect(Unit) { focusRequester.requestFocus() }
            }
        }
        waitForIdle()

        onNodeWithTag("root").performKeyInput { withKeyDown(Key.MetaLeft) { pressKey(Key.R) } }
        assertEquals(listOf("second"), triggered)

        showSecond = false
        waitForIdle()
        onNodeWithTag("root").performKeyInput { withKeyDown(Key.MetaLeft) { pressKey(Key.R) } }
        assertEquals(listOf("second", "first"), triggered)

        // 不带 ⌘、带其他修饰键、停用的快捷键都不触发
        onNodeWithTag("root").performKeyInput { pressKey(Key.R) }
        onNodeWithTag("root").performKeyInput {
            withKeyDown(Key.MetaLeft) { withKeyDown(Key.ShiftLeft) { pressKey(Key.R) } }
        }
        onNodeWithTag("root").performKeyInput { withKeyDown(Key.MetaLeft) { pressKey(Key.T) } }
        assertEquals(listOf("second", "first"), triggered)
    }
}
