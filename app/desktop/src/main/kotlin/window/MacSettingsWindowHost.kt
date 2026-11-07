/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.desktop.window

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.rememberWindowState
import java.awt.Desktop
import java.awt.Dimension
import me.him188.ani.app.platform.PlatformWindow
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.apple.MacWindowDefaults
import me.him188.ani.app.ui.foundation.layout.LocalPlatformWindow
import me.him188.ani.app.ui.foundation.layout.LocalSecondaryWindowFrame
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.settings
import me.him188.ani.app.ui.main.MacSettingsWindowHost
import me.him188.ani.app.ui.main.MacSettingsWindowState
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 设定窗口的宿主 (05 B4): 920×680 的独立窗口, 照图片查看器沿用主窗口的窗口外观 ([LocalSecondaryWindowFrame]).
 *
 * App 菜单的「设定…」(⌘,) 打开窗口, 窗口已开着时带到前面; ⌘W 或红色按钮关闭窗口, 不影响主窗口.
 *
 * @param onShowMainWindow 把主窗口带到前面 (包括从托盘还原).
 */
@Stable
class DesktopMacSettingsWindowHost(
    private val onShowMainWindow: () -> Unit,
) : MacSettingsWindowHost {
    @Composable
    override fun SettingsWindow(state: MacSettingsWindowState, content: @Composable () -> Unit) {
        PreferencesHandlerEffect(state)
        if (!state.isOpen) return

        val windowState = rememberWindowState(
            size = MacWindowDefaults.SettingsWindowSize,
            position = WindowPosition.Aligned(Alignment.Center),
        )
        Window(
            onCloseRequest = state::close,
            state = windowState,
            title = stringResource(Lang.settings),
            onKeyEvent = { event ->
                if (event.type == KeyEventType.KeyDown && event.isMetaPressed && event.key == Key.W) {
                    state.close()
                    true
                } else {
                    false
                }
            },
        ) {
            SideEffect {
                window.minimumSize = Dimension(MIN_WIDTH, MIN_HEIGHT)
            }
            // 窗口已开着时再次打开 (⌘, 、侧边栏), 把它带到前面
            LaunchedEffect(state.openRequests) {
                windowState.isMinimized = false
                window.toFront()
                window.requestFocus()
            }
            val platform = LocalPlatform.current
            val platformWindow = remember(window.windowHandle, this, platform, windowState) {
                PlatformWindow(
                    windowHandle = window.windowHandle,
                    windowScope = this,
                    windowState = windowState,
                    platform = platform,
                )
            }
            CompositionLocalProvider(LocalPlatformWindow provides platformWindow) {
                val frame = LocalSecondaryWindowFrame.current
                if (frame != null) {
                    frame(windowState, state::close, content)
                } else {
                    content()
                }
            }
        }
    }

    override fun showMainWindow() {
        onShowMainWindow()
    }

    private companion object {
        /** 窗口可缩放; 窗格内容超出时窗格内纵向滚动 (04 §5.1). */
        const val MIN_WIDTH = 760
        const val MIN_HEIGHT = 520
    }
}

/**
 * 接上 App 菜单的「设定…」(⌘,). 离开组合时移除, 菜单项恢复为不可用.
 */
@Composable
private fun PreferencesHandlerEffect(state: MacSettingsWindowState) {
    DisposableEffect(state) {
        if (!Desktop.isDesktopSupported()) return@DisposableEffect onDispose {}
        val desktop = Desktop.getDesktop()
        if (!desktop.isSupported(Desktop.Action.APP_PREFERENCES)) return@DisposableEffect onDispose {}

        desktop.setPreferencesHandler { state.open() }
        onDispose {
            desktop.setPreferencesHandler(null)
        }
    }
}
