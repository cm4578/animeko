/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * docs/design/ani-style/02-components.md 手机组件 C4–C15 的尺寸、语义与配色. 内容以 iOS 平台显示.
 */
class AppleComponentsTest {
    @Test
    fun `episode tile label pads integers to two digits`() {
        assertEquals("04", episodeTileLabel(4))
        assertEquals("12", episodeTileLabel(12))
        assertEquals("120", episodeTileLabel(120))
    }

    @Test
    fun `switch toggles and keeps a 44 touch target`() = runAniComposeUiTest {
        var checked by mutableStateOf(false)
        setAniStyleContent {
            AniSwitch(checked, onCheckedChange = { checked = it }, Modifier.testTag("switch"))
        }
        onNodeWithTag("switch").assertIsOff()
        assertSize("switch", width = 48.dp, height = 44.dp)

        onNodeWithTag("switch").performClick()
        waitForIdle()
        assertTrue(checked)
        onNodeWithTag("switch").assertIsOn()
    }

    @Test
    fun `switch track uses accent fill when on and separator when off`() = runAniComposeUiTest {
        var checked by mutableStateOf(true)
        setAniStyleContent {
            AniSwitch(checked, onCheckedChange = null, Modifier.testTag("switch"))
        }
        // 轨道在 44 高的点击区内垂直居中; 开启时圆钮在右, 取左侧轨道; 关闭时取右侧.
        assertPixel("switch", x = 6.dp, y = 22.dp, expected = Color(0xFF3072A1))
        checked = false
        waitForIdle()
        assertPixel("switch", x = 42.dp, y = 22.dp, expected = Color(0xFFC7CCD4))
    }

    @Test
    fun `settings switch row toggles from the whole row`() = runAniComposeUiTest {
        var checked by mutableStateOf(false)
        setAniStyleContent {
            GroupedListCard {
                SettingsSwitchRow(
                    title = "Title",
                    checked = checked,
                    onCheckedChange = { checked = it },
                    modifier = Modifier.testTag("row"),
                )
                SettingsSwitchRow(
                    title = "With description",
                    description = "Description",
                    checked = false,
                    onCheckedChange = {},
                    showDivider = true,
                    modifier = Modifier.testTag("rowWithDescription"),
                )
            }
        }
        onNodeWithText("Title").performClick()
        waitForIdle()
        assertTrue(checked)
        onNodeWithTag("row").assertIsOn()
        assertEquals(48f, heightOf("row").value, SIZE_TOLERANCE)
        assertTrue(heightOf("rowWithDescription") >= 60.dp)
    }

    @Test
    fun `disabled settings row ignores clicks`() = runAniComposeUiTest {
        var clicked = false
        setAniStyleContent {
            SettingsRow(
                title = "Title",
                enabled = false,
                onClick = { clicked = true },
                modifier = Modifier.testTag("row"),
            ) {
                SettingsRowNavigation(value = "Value")
            }
        }
        onNodeWithTag("row").assertIsNotEnabled()
        onNodeWithTag("row").performClick()
        waitForIdle()
        assertFalse(clicked)
    }

    @Test
    fun `pill tab exposes selection and a 44 touch target`() = runAniComposeUiTest {
        var selected by mutableStateOf("a")
        setAniStyleContent {
            Box {
                Row {
                    PillTab("A", selected == "a", { selected = "a" }, Modifier.testTag("a"), count = "12")
                    PillTab("B", selected == "b", { selected = "b" }, Modifier.testTag("b"))
                }
            }
        }
        onNodeWithTag("a").assertIsSelected()
        onNodeWithTag("b").assertIsNotSelected()
        assertEquals(44f, heightOf("a").value, SIZE_TOLERANCE)

        onNodeWithTag("b").performClick()
        waitForIdle()
        onNodeWithTag("b").assertIsSelected()
    }

    @Test
    fun `filter pills wrap with an 8 visual gap`() = runAniComposeUiTest {
        setAniStyleContent {
            FilterPillFlowRow(Modifier.width(200.dp)) {
                FilterPill("First filter", applied = true, onClick = {}, Modifier.testTag("p1"))
                FilterPill("Second filter", applied = false, onClick = {}, Modifier.testTag("p2"))
            }
        }
        val first = onNodeWithTag("p1").getBoundsInRoot()
        val second = onNodeWithTag("p2").getBoundsInRoot()
        assertTrue(second.top > first.top, "second pill should wrap to the next line")
        // 点击区高 44, 胶囊高 32: 下一行胶囊的上缘比上一行胶囊的下缘低 8.
        val visualGap = (second.top + 6.dp) - (first.bottom - 6.dp)
        assertEquals(8f, visualGap.value, SIZE_TOLERANCE)
    }

    @Test
    fun `circle button keeps a 44 touch target and clicks`() = runAniComposeUiTest {
        var clicks = 0
        setAniStyleContent {
            AppleCircleButton(
                onClick = { clicks++ },
                icon = AppleIcons.back(),
                contentDescription = "Back",
                modifier = Modifier.testTag("button"),
            )
        }
        assertSize("button", width = 44.dp, height = 44.dp)
        onNodeWithTag("button").assert(SemanticsMatcher.expectValue(SemanticsProperties.ContentDescription, listOf("Back")))
        onNodeWithTag("button").performClick()
        waitForIdle()
        assertEquals(1, clicks)
    }

    @Test
    fun `disabled button does not click`() = runAniComposeUiTest {
        var clicks = 0
        setAniStyleContent {
            AppleButton(onClick = { clicks++ }, enabled = false, modifier = Modifier.testTag("button")) {
                Text("Play")
            }
        }
        onNodeWithTag("button").assertIsNotEnabled()
        onNodeWithTag("button").performClick()
        waitForIdle()
        assertEquals(0, clicks)
    }

    @Test
    fun `episode tile is 52 square and marks the playing episode`() = runAniComposeUiTest {
        setAniStyleContent {
            Row {
                EpisodeTile("08", EpisodeTileState.Watched, {}, Modifier.testTag("watched"))
                EpisodeTile("09", EpisodeTileState.Playing, {}, Modifier.testTag("playing"))
            }
        }
        assertSize("playing", width = 52.dp, height = 52.dp)
        onNodeWithTag("playing").assertIsSelected()
        onNodeWithTag("watched").assertIsNotSelected()
        // 正在播放为 tint 底; 取描边内侧、文字左侧的一点.
        assertPixel("playing", x = 4.dp, y = 26.dp, expected = Color(0xFFE6EEF4))
    }

    @Test
    fun `cover shows text instead of an image below 42 wide`() = runAniComposeUiTest {
        var width by mutableStateOf(40.dp)
        setAniStyleContent {
            AppleCover(
                aspect = CoverAspect.Portrait3x4,
                fallbackText = "Fallback",
                modifier = Modifier.width(width),
            ) {
                Box(Modifier.fillMaxSize().testTag("image"))
            }
        }
        onNodeWithText("Fallback").assertExists()
        onNodeWithTag("image").assertDoesNotExist()

        width = 60.dp
        waitForIdle()
        onNodeWithText("Fallback").assertDoesNotExist()
        assertSize("image", width = 60.dp, height = 80.dp)
    }

    @Test
    fun `alert dot on cover includes its ring`() = runAniComposeUiTest {
        setAniStyleContent {
            AlertDot(Modifier.testTag("dot"), style = AlertDotStyle.OnCover)
        }
        assertSize("dot", width = 14.dp, height = 14.dp)
    }

    @Test
    fun `progress bar reports its progress`() = runAniComposeUiTest {
        setAniStyleContent {
            AppleProgressBar(progress = { 0.3f }, modifier = Modifier.testTag("progress"))
        }
        onNodeWithTag("progress").assert(
            SemanticsMatcher.expectValue(SemanticsProperties.ProgressBarRangeInfo, ProgressBarRangeInfo(0.3f, 0f..1f)),
        )
        assertEquals(4f, heightOf("progress").value, SIZE_TOLERANCE)
    }

    @Test
    fun `popover menu shows items and reports clicks`() = runAniComposeUiTest {
        var expanded by mutableStateOf(true)
        var clicked: String? = null
        setAniStyleContent {
            Box {
                ApplePopoverMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    PopoverMenuItem("Status", onClick = { clicked = "status" }, value = "Watching")
                    PopoverMenuItem("Share", onClick = { clicked = "share" }, showDivider = true)
                }
            }
        }
        onNodeWithText("Watching").assertExists()
        onNodeWithText("Share").performClick()
        waitForIdle()
        assertEquals("share", clicked)

        expanded = false
        waitForIdle()
        onNodeWithText("Share").assertDoesNotExist()
    }
}
