/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type

/**
 * macOS 主窗口的键盘快捷键 (05 C2): ⌘ 加一个键, 不带其他修饰键.
 *
 * desktop 的主窗口在 `Window(onKeyEvent)` 调用 [dispatch]. 那里只收到焦点所在的元件没有消费的按键,
 * 所以输入框自己要用的按键 (例如移动光标的 ⌘← / ⌘→) 不会触发快捷键.
 *
 * 页面用 [MacKeyboardShortcut] 注册. 同一个键有多个注册时, 最后注册的生效: 新打开的页面优先.
 */
@Stable
class MacKeyboardShortcuts {
    private class Handler(val key: Key, val onTrigger: () -> Unit)

    private val handlers = mutableListOf<Handler>()

    /**
     * 注册 ⌘[key]. 返回取消注册的函数.
     */
    fun register(key: Key, onTrigger: () -> Unit): () -> Unit {
        val handler = Handler(key, onTrigger)
        handlers += handler
        return { handlers -= handler }
    }

    /**
     * 有快捷键处理了 [event] 时返回 `true`.
     */
    fun dispatch(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown) return false
        if (!event.isMetaPressed || event.isCtrlPressed || event.isAltPressed || event.isShiftPressed) return false
        val handler = handlers.lastOrNull { it.key == event.key } ?: return false
        handler.onTrigger()
        return true
    }
}

/**
 * macOS 主窗口的快捷键分派. 只在 macOS 主窗口提供, 其他平台为 `null`.
 */
val LocalMacKeyboardShortcuts = staticCompositionLocalOf<MacKeyboardShortcuts?> { null }

/**
 * 在组合期间注册 ⌘[key] (05 C2). [enabled] 为 `false` 或没有 [LocalMacKeyboardShortcuts] 时不注册.
 */
@Composable
fun MacKeyboardShortcut(
    key: Key,
    enabled: Boolean = true,
    onTrigger: () -> Unit,
) {
    val shortcuts = LocalMacKeyboardShortcuts.current ?: return
    val currentOnTrigger by rememberUpdatedState(onTrigger)
    DisposableEffect(shortcuts, key, enabled) {
        val unregister = if (enabled) shortcuts.register(key) { currentOnTrigger() } else null
        onDispose { unregister?.invoke() }
    }
}
