/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.danmaku

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import me.him188.ani.app.data.models.preference.DarkMode
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.navigation.LocalOnBackPressedDispatcherOwner
import me.him188.ani.app.ui.foundation.navigation.OnBackPressedDispatcher
import me.him188.ani.app.ui.foundation.navigation.SkikoOnBackPressedDispatcherOwner
import me.him188.ani.app.ui.framework.AniComposeUiTest
import me.him188.ani.app.ui.framework.doesNotExist
import me.him188.ani.app.ui.framework.exists
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.danmaku.api.DanmakuLocation
import kotlin.test.Test
import kotlin.test.assertEquals

class DanmakuStylePickerTest {
    @Test
    fun `default style is white and floating`() {
        assertEquals(0xFFFFFF, DanmakuSendStyle.Default.color)
        assertEquals(DanmakuLocation.NORMAL, DanmakuSendStyle.Default.location)
    }

    @Test
    fun `picker - click button opens panel, selecting color and location updates style`() = runAniComposeUiTest {
        var style by mutableStateOf(DanmakuSendStyle.Default)
        setContent {
            ProvideCompositionLocalsForPreview(darkMode = DarkMode.DARK) {
                // 弹层在按钮上方, 所以把按钮放在底部留出空间
                Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomStart) {
                    DanmakuStylePicker(style, onStyleChange = { style = it })
                }
            }
        }

        runOnIdle {
            waitUntil { onNodeWithTag(TAG_DANMAKU_STYLE_BUTTON).exists() }
            waitUntil { onNodeWithTag(TAG_DANMAKU_STYLE_PANEL).doesNotExist() }
        }

        onNodeWithTag(TAG_DANMAKU_STYLE_BUTTON).performClick()
        runOnIdle {
            waitUntil { onNodeWithTag(TAG_DANMAKU_STYLE_PANEL).exists() }
        }
        onNodeWithTag(danmakuColorSwatchTag(0xFFFFFF)).assertIsSelected()
        onNodeWithTag(danmakuLocationTileTag(DanmakuLocation.NORMAL)).assertIsSelected()

        onNodeWithTag(danmakuColorSwatchTag(0xFF4D4D)).performClick()
        runOnIdle {
            assertEquals(DanmakuSendStyle(0xFF4D4D, DanmakuLocation.NORMAL), style)
        }
        onNodeWithTag(danmakuColorSwatchTag(0xFF4D4D)).assertIsSelected()

        onNodeWithTag(danmakuLocationTileTag(DanmakuLocation.TOP)).performClick()
        runOnIdle {
            assertEquals(DanmakuSendStyle(0xFF4D4D, DanmakuLocation.TOP), style)
        }
        onNodeWithTag(danmakuLocationTileTag(DanmakuLocation.TOP)).assertIsSelected()

        // 选择后弹层保持打开, 方便继续调整
        onNodeWithTag(TAG_DANMAKU_STYLE_PANEL).assertExists()
    }

    @Test
    fun `picker - back press closes the preset panel before leaving the page`() = runAniComposeUiTest {
        val window = setPickerInWindow()
        onNodeWithTag(TAG_DANMAKU_STYLE_BUTTON).performClick()
        runOnIdle {
            waitUntil { onNodeWithTag(TAG_DANMAKU_STYLE_PANEL).exists() }
        }

        runOnIdle { window.dispatcher.onBackPressed() }
        runOnIdle {
            waitUntil { onNodeWithTag(TAG_DANMAKU_STYLE_PANEL).doesNotExist() }
            assertEquals(0, window.pageBackCount)
        }

        runOnIdle { window.dispatcher.onBackPressed() }
        runOnIdle { assertEquals(1, window.pageBackCount) }
    }

    @Test
    fun `picker - back press closes the focusable custom color panel before leaving the page`() =
        runAniComposeUiTest {
            val window = setPickerInWindow()
            onNodeWithTag(TAG_DANMAKU_STYLE_BUTTON).performClick()
            runOnIdle {
                waitUntil { onNodeWithTag(TAG_DANMAKU_STYLE_PANEL).exists() }
            }
            // 进入自定义模式后弹层重建为可聚焦的 Popup
            onNodeWithTag(TAG_DANMAKU_CUSTOM_COLOR_SWATCH).performClick()
            onNodeWithTag(TAG_DANMAKU_CUSTOM_COLOR_HEX).assertExists()

            runOnIdle { window.dispatcher.onBackPressed() }
            runOnIdle {
                waitUntil { onNodeWithTag(TAG_DANMAKU_STYLE_PANEL).doesNotExist() }
                assertEquals(0, window.pageBackCount)
            }
        }

    private class PickerWindow {
        lateinit var dispatcher: OnBackPressedDispatcher
        var pageBackCount = 0
    }

    /**
     * 按桌面主窗口的方式接上返回: dispatcher 带窗口的 NavigationEventDispatcher, 没有可关闭的东西时才算离开页面.
     */
    private fun AniComposeUiTest.setPickerInWindow(): PickerWindow {
        val window = PickerWindow()
        setContent {
            ProvideCompositionLocalsForPreview(darkMode = DarkMode.DARK) {
                val navigationEventDispatcher =
                    checkNotNull(LocalNavigationEventDispatcherOwner.current).navigationEventDispatcher
                val dispatcher = remember(navigationEventDispatcher) {
                    OnBackPressedDispatcher(
                        fallback = { window.pageBackCount++ },
                        navigationEventDispatcher = navigationEventDispatcher,
                    )
                }
                SideEffect { window.dispatcher = dispatcher }
                val lifecycleOwner = LocalLifecycleOwner.current
                val owner = remember(dispatcher, lifecycleOwner) {
                    SkikoOnBackPressedDispatcherOwner(dispatcher, lifecycleOwner)
                }
                CompositionLocalProvider(LocalOnBackPressedDispatcherOwner provides owner) {
                    // 弹层在按钮上方, 所以把按钮放在底部留出空间
                    Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomStart) {
                        DanmakuStylePicker(DanmakuSendStyle.Default, onStyleChange = {})
                    }
                }
            }
        }
        return window
    }

    @Test
    fun `panel - custom color via hex input`() = runAniComposeUiTest {
        var style by mutableStateOf(DanmakuSendStyle.Default)
        setContent {
            ProvideCompositionLocalsForPreview {
                DanmakuStylePanel(style, onStyleChange = { style = it })
            }
        }

        onNodeWithTag(TAG_DANMAKU_CUSTOM_COLOR_HEX).assertDoesNotExist()
        onNodeWithTag(TAG_DANMAKU_CUSTOM_COLOR_SWATCH).performClick()
        onNodeWithTag(TAG_DANMAKU_CUSTOM_COLOR_SWATCH).assertIsSelected()
        // 进入自定义模式不改变颜色
        runOnIdle { assertEquals(0xFFFFFF, style.color) }

        // 面板设置了 canFocus = false, 十六进制输入框必须能单独获得焦点, 否则实际运行时无法键入
        onNodeWithTag(TAG_DANMAKU_CUSTOM_COLOR_HEX).performClick()
        onNodeWithTag(TAG_DANMAKU_CUSTOM_COLOR_HEX).assertIsFocused()
        onNodeWithTag(TAG_DANMAKU_CUSTOM_COLOR_HEX).performTextReplacement("ff00aa")
        runOnIdle { assertEquals(0xFF00AA, style.color) }

        // 不完整的输入不生效
        onNodeWithTag(TAG_DANMAKU_CUSTOM_COLOR_HEX).performTextReplacement("12")
        runOnIdle { assertEquals(0xFF00AA, style.color) }

        // 选回预设后退出自定义模式
        onNodeWithTag(danmakuColorSwatchTag(0xFF4D4D)).performClick()
        runOnIdle { assertEquals(0xFF4D4D, style.color) }
        onNodeWithTag(TAG_DANMAKU_CUSTOM_COLOR_HEX).assertDoesNotExist()
    }

    @Test
    fun `panel - every preset color and location is selectable`() = runAniComposeUiTest {
        var style by mutableStateOf(DanmakuSendStyle.Default)
        setContent {
            ProvideCompositionLocalsForPreview {
                DanmakuStylePanel(style, onStyleChange = { style = it })
            }
        }

        for (color in DanmakuSendColors.Presets) {
            onNodeWithTag(danmakuColorSwatchTag(color)).performClick()
            runOnIdle { assertEquals(color, style.color) }
        }
        for (location in DanmakuSendColors.Locations) {
            onNodeWithTag(danmakuLocationTileTag(location)).performClick()
            runOnIdle { assertEquals(location, style.location) }
        }
    }
}
