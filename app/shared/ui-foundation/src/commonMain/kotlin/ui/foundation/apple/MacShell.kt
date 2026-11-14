/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsStartWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.him188.ani.app.ui.foundation.layout.desktopCaptionButton
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.main_sidebar_show
import me.him188.ani.app.ui.lang.main_toolbar_back
import me.him188.ani.app.ui.lang.main_toolbar_forward
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 主窗口外壳 (左侧 C17 侧边栏 + 右侧内容区) 提供给页面的导航状态. 页面用 [MacPageToolbar] 画自己的 C18 工具栏.
 *
 * @property canGoBack 返回栈中有上一页.
 * @property sidebarHidden 侧边栏被「隐藏侧边栏」收起 (05 G31). 这时工具栏左侧要让出红黄绿按钮, 并放「显示侧边栏」按钮.
 * @property toolbars 当前显示的 [MacPageToolbar] 数. 为 0 时 (尚未改版的页面) 外壳自己放「显示侧边栏」按钮.
 */
@Stable
class MacShellState(
    val canGoBack: Boolean,
    val onBack: () -> Unit,
    val sidebarHidden: Boolean,
    val onShowSidebar: () -> Unit,
    val toolbars: MacToolbarCounter,
)

/**
 * 记录内容区里有几个 [MacPageToolbar]. 切换页面的动画中, 新旧两页可能同时显示.
 */
@Stable
class MacToolbarCounter {
    var count by mutableIntStateOf(0)
        private set

    internal fun increment() {
        count++
    }

    internal fun decrement() {
        count--
    }
}

/**
 * 当前页面所在的 macOS 外壳. 不在外壳内 (例如测试) 时为 `null`.
 */
val LocalMacShellState = compositionLocalOf<MacShellState?> { null }

/**
 * 页面的 C18 工具栏: 左侧上一页 / 下一页, 右侧 [actions].
 *
 * - 「上一页」弹出返回栈的栈顶; 不在外壳内时停用.
 * - 「下一页」一直停用: 返回栈没有前进记录.
 * - 侧边栏隐藏时 (05 G31), 左侧先让出红黄绿按钮, 再放「显示侧边栏」按钮.
 */
@Composable
fun MacPageToolbar(
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val shell = LocalMacShellState.current
    if (shell != null) {
        val toolbars = shell.toolbars
        DisposableEffect(toolbars) {
            toolbars.increment()
            onDispose { toolbars.decrement() }
        }
    }
    MacToolbar(
        canGoBack = shell?.canGoBack == true,
        onBack = { shell?.onBack?.invoke() },
        backContentDescription = stringResource(Lang.main_toolbar_back),
        canGoForward = false,
        onForward = {},
        forwardContentDescription = stringResource(Lang.main_toolbar_forward),
        modifier = modifier,
        leading = {
            if (shell?.sidebarHidden == true) {
                Spacer(Modifier.windowInsetsStartWidth(WindowInsets.desktopCaptionButton))
                MacToolbarIconButton(
                    onClick = shell.onShowSidebar,
                    icon = AppleIcons.sidebar(),
                    contentDescription = stringResource(Lang.main_sidebar_show),
                )
            }
        },
        actions = actions,
    )
}
