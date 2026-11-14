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
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.FirstBaseline
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.getAlignmentLinePosition
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * docs/design/ani-style/02-components.md C4–C15 的 macOS 规格, 以及 C2、C14、C18–C21. 内容以 macOS 平台显示.
 */
@OptIn(ExperimentalTestApi::class)
class MacComponentsTest {
    @Test
    fun `pills are 28 high without extra touch area on macOS`() = runAniComposeUiTest {
        setAniStyleContent(MacOSPlatform) {
            Row {
                PillTab("Watching", selected = true, onClick = {}, Modifier.testTag("tab"), count = "12")
                FilterPill("Year", applied = false, onClick = {}, Modifier.testTag("filter"))
            }
        }
        assertEquals(28f, heightOf("tab").value, SIZE_TOLERANCE)
        assertEquals(28f, heightOf("filter").value, SIZE_TOLERANCE)
    }

    @Test
    fun `filter pills wrap with an 8 gap on macOS`() = runAniComposeUiTest {
        setAniStyleContent(MacOSPlatform) {
            FilterPillFlowRow(Modifier.width(120.dp)) {
                FilterPill("Best match", applied = true, onClick = {}, Modifier.testTag("a"))
                FilterPill("All years", applied = false, onClick = {}, Modifier.testTag("b"))
            }
        }
        assertEquals(8f, (boundsOf("b").top - boundsOf("a").bottom).value, SIZE_TOLERANCE)
    }

    @Test
    fun `switch uses the 40 by 24 track on macOS`() = runAniComposeUiTest {
        var checked by mutableStateOf(true)
        setAniStyleContent(MacOSPlatform) {
            AniSwitch(checked, onCheckedChange = { checked = it }, Modifier.testTag("switch"))
        }
        // 点击区高 28, 轨道 24 垂直居中. 开启时圆钮在右, 取左侧轨道; 关闭时取右侧.
        assertSize("switch", width = 40.dp, height = 28.dp)
        assertPixel("switch", x = 5.dp, y = 14.dp, expected = Color(0xFF3072A1))
        checked = false
        waitForIdle()
        assertPixel("switch", x = 35.dp, y = 14.dp, expected = Color(0xFFC7CCD4))

        onNodeWithTag("switch").performClick()
        waitForIdle()
        assertTrue(checked)
        onNodeWithTag("switch").assertIsOn()
    }

    @Test
    fun `settings rows use mac heights and a popup button`() = runAniComposeUiTest {
        setAniStyleContent(MacOSPlatform) {
            GroupedListCard(Modifier.testTag("card")) {
                SettingsRow("Language", Modifier.testTag("row")) {
                    SettingsRowPopupValue("English", Modifier.testTag("popup"))
                }
                SettingsRow(
                    "Start page",
                    Modifier.testTag("rowWithDescription"),
                    description = "Page shown at launch",
                    showDivider = true,
                )
            }
        }
        assertEquals(48f, heightOf("row").value, SIZE_TOLERANCE)
        assertEquals(60f, heightOf("rowWithDescription").value, SIZE_TOLERANCE)
        assertEquals(24f, heightOf("popup").value, SIZE_TOLERANCE)
        // 弹出按钮为 fill 底.
        assertPixel("popup", x = 3.dp, y = 12.dp, expected = Color(0xFFE6EBF3))
        // 行尾与卡片右缘相隔 16.
        assertEquals(16f, (boundsOf("card").right - boundsOf("popup").right).value, SIZE_TOLERANCE)
    }

    @Test
    fun `settings navigation and group text use mac spacing`() = runAniComposeUiTest {
        setAniStyleContent(MacOSPlatform) {
            GroupedListSection(Modifier.testTag("section"), title = "Group", footer = "Footer") {
                SettingsRow("Backup", onClick = {}) {
                    SettingsRowNavigation(value = "Value")
                }
            }
        }
        val section = boundsOf("section")
        // 行尾内距 16 + 14 号 `›` + 值与箭头间距 12.
        assertEquals(42f, (section.right - textBoundsOf("Value").right).value, SIZE_TOLERANCE)
        assertEquals(12f, (textBoundsOf("Group").left - section.left).value, SIZE_TOLERANCE)
        assertEquals(12f, (textBoundsOf("Footer").left - section.left).value, SIZE_TOLERANCE)
    }

    @Test
    fun `buttons are 28 high on macOS`() = runAniComposeUiTest {
        var clicks = 0
        setAniStyleContent(MacOSPlatform) {
            Row {
                AppleButton(onClick = { clicks++ }, Modifier.testTag("button")) { Text("Save") }
                AppleCircleButton(
                    onClick = { clicks++ },
                    icon = AppleIcons.close(),
                    contentDescription = "Close",
                    modifier = Modifier.testTag("icon"),
                )
            }
        }
        assertEquals(28f, heightOf("button").value, SIZE_TOLERANCE)
        assertSize("icon", width = 28.dp, height = 28.dp)
        onNodeWithContentDescription("Close").performClick()
        onNodeWithTag("button").performClick()
        waitForIdle()
        assertEquals(2, clicks)
    }

    @Test
    fun `large title keeps the subtitle on its baseline`() = runAniComposeUiTest {
        setAniStyleContent(MacOSPlatform) {
            MacLargeTitleHeader("Downloads", subtitle = "5 titles")
        }
        fun baselineOf(text: String) = onNodeWithText(text).let {
            it.getBoundsInRoot().top + it.getAlignmentLinePosition(FirstBaseline)
        }
        assertEquals(baselineOf("Downloads").value, baselineOf("5 titles").value, SIZE_TOLERANCE)
    }

    @Test
    fun `toolbar disables unavailable navigation`() = runAniComposeUiTest {
        var backs = 0
        setAniStyleContent(MacOSPlatform) {
            MacToolbar(
                canGoBack = true,
                onBack = { backs++ },
                backContentDescription = "Back",
                canGoForward = false,
                onForward = {},
                forwardContentDescription = "Forward",
                modifier = Modifier.testTag("toolbar"),
            ) {
                MacToolbarTextButton("Pause all", onClick = {})
            }
        }
        assertEquals(52f, heightOf("toolbar").value, SIZE_TOLERANCE)
        onNodeWithContentDescription("Forward").assertIsNotEnabled()
        onNodeWithContentDescription("Back").performClick()
        waitForIdle()
        assertEquals(1, backs)
        onNodeWithText("Pause all").assertExists()
    }

    @Test
    fun `segmented buttons and inspector toggle expose their state`() = runAniComposeUiTest {
        var grid by mutableStateOf(true)
        var inspectorOpen by mutableStateOf(false)
        setAniStyleContent(MacOSPlatform) {
            Row {
                MacSegmentedButtons {
                    MacSegmentedButton(grid, onClick = { grid = true }, AppleIcons.plus(), "Grid")
                    MacSegmentedButton(!grid, onClick = { grid = false }, AppleIcons.check(), "List")
                }
                MacToolbarDivider()
                MacToolbarIconButton(
                    onClick = { inspectorOpen = !inspectorOpen },
                    icon = AppleIcons.inspector(),
                    contentDescription = "Inspector",
                    checked = inspectorOpen,
                )
            }
        }
        onNodeWithContentDescription("Grid").assertIsSelected()
        onNodeWithContentDescription("List").performClick()
        waitForIdle()
        onNodeWithContentDescription("List").assertIsSelected()
        onNodeWithContentDescription("Grid").assertIsNotSelected()

        onNodeWithContentDescription("Inspector").assertIsOff()
        onNodeWithContentDescription("Inspector").performClick()
        waitForIdle()
        onNodeWithContentDescription("Inspector").assertIsOn()
    }

    @Test
    fun `table header and rows share column widths`() = runAniComposeUiTest {
        val columns = MacTableColumns(
            listOf(
                MacTableColumn(),
                MacTableColumn(width = 120.dp, alignment = Alignment.End),
                MacTableColumn(width = 56.dp, alignment = Alignment.CenterHorizontally),
            ),
        )
        setAniStyleContent(MacOSPlatform) {
            Box(Modifier.width(600.dp)) {
                MacTable {
                    MacTableHeader(columns) {
                        MacTableHeaderCell("Name", Modifier.testTag("h0"))
                        MacTableHeaderCell("Size", Modifier.testTag("h1"), sorted = true)
                        MacTableHeaderCell("Act", Modifier.testTag("h2"))
                    }
                    MacTableGroupRow(columns, "Downloading", count = "3") {
                        Text("Pause", Modifier.testTag("groupAction"))
                    }
                    MacTableRow(columns, Modifier.testTag("row")) {
                        Text("Frieren", Modifier.testTag("c0"))
                        Text("10.6 GB", Modifier.testTag("c1"))
                        Text("▶", Modifier.testTag("c2"))
                    }
                }
            }
        }
        // 第一列左对齐, 第二列右对齐, 第三列居中.
        assertEquals(16f, boundsOf("h0").left.value, SIZE_TOLERANCE)
        assertEquals(boundsOf("h0").left.value, boundsOf("c0").left.value, SIZE_TOLERANCE)
        assertEquals(boundsOf("h1").right.value, boundsOf("c1").right.value, SIZE_TOLERANCE)
        assertEquals(600f - 16f - 56f - 16f, boundsOf("c1").right.value, SIZE_TOLERANCE)
        val actionCenter = 600f - 16f - 28f
        assertEquals(actionCenter, boundsOf("h2").let { (it.left + it.right) / 2 }.value, SIZE_TOLERANCE)
        assertEquals(actionCenter, boundsOf("c2").let { (it.left + it.right) / 2 }.value, SIZE_TOLERANCE)
        assertEquals(actionCenter, boundsOf("groupAction").let { (it.left + it.right) / 2 }.value, SIZE_TOLERANCE)
        assertEquals(52f, heightOf("row").value, SIZE_TOLERANCE)
    }

    @Test
    fun `table rows report selection and double clicks`() = runAniComposeUiTest {
        var selected by mutableStateOf(false)
        var opened = 0
        val columns = MacTableColumns(listOf(MacTableColumn()))
        setAniStyleContent(MacOSPlatform) {
            MacTable {
                MacTableRow(
                    columns,
                    Modifier.testTag("row"),
                    selected = selected,
                    onClick = { selected = true },
                    onDoubleClick = { opened++ },
                ) {
                    Text("Episode 9")
                }
            }
        }
        onNodeWithTag("row").assertIsNotSelected()
        // 双击的第一下立即选中, 第二下打开.
        onNodeWithTag("row").performMouseInput { doubleClick() }
        waitForIdle()
        onNodeWithTag("row").assertIsSelected()
        assertEquals(1, opened)
    }

    @Test
    fun `popover rows are 26 high and highlight on hover`() = runAniComposeUiTest {
        var clicked: String? = null
        setAniStyleContent(MacOSPlatform) {
            Box {
                ApplePopoverMenu(expanded = true, onDismissRequest = {}) {
                    PopoverMenuItem("Resume", onClick = { clicked = "resume" }, Modifier.testTag("resume"))
                    PopoverMenuItem("Details", onClick = {}, Modifier.testTag("details"), shortcut = "⌘I")
                    PopoverMenuItem("Delete", onClick = {}, Modifier.testTag("delete"), showDivider = true)
                }
            }
        }
        assertEquals(26f, heightOf("resume").value, SIZE_TOLERANCE)
        // 分隔线上下各留 4.
        assertEquals(26f + 9f, heightOf("delete").value, SIZE_TOLERANCE)
        onNodeWithText("⌘I").assertExists()

        onNodeWithTag("resume").performMouseInput { moveTo(center) }
        waitForIdle()
        assertPixel("resume", x = 3.dp, y = 13.dp, expected = Color(0xFF3072A1))

        onNodeWithTag("resume").performClick()
        waitForIdle()
        assertEquals("resume", clicked)
    }

    @Test
    fun `sheet sits below the title bar and confirms from its actions`() = runAniComposeUiTest {
        var result: String? = null
        setAniStyleContent(MacOSPlatform) {
            Box(Modifier.width(920.dp)) {
                MacSheet(onDismissRequest = { result = "dismiss" }, Modifier.testTag("sheet")) {
                    Text("Edit profile")
                    MacSheetActions(
                        cancelText = "Cancel",
                        onCancel = { result = "cancel" },
                        confirmText = "Save",
                        onConfirm = { result = "save" },
                    )
                }
            }
        }
        assertEquals(52f, boundsOf("sheet").top.value, SIZE_TOLERANCE)
        assertEquals(560f, widthOf("sheet").value, SIZE_TOLERANCE)
        onNodeWithText("Save").performClick()
        waitForIdle()
        assertEquals("save", result)
    }

    @Test
    fun `settings window categories select and sub pages go back`() = runAniComposeUiTest {
        var category by mutableStateOf("interface")
        var backs = 0
        setAniStyleContent(MacOSPlatform) {
            Row {
                MacSettingsCategoryBar(
                    account = {
                        MacSettingsAccountItem(
                            name = "Ani",
                            subtitle = "Account and sync",
                            initial = "A",
                            selected = category == "account",
                            onClick = { category = "account" },
                            modifier = Modifier.testTag("account"),
                        )
                    },
                    modifier = Modifier.testTag("bar"),
                ) {
                    MacSettingsCategoryGroup("App") {
                        MacSettingsCategoryItem(
                            category == "interface",
                            onClick = { category = "interface" },
                            icon = AppleIcons.settings(),
                            label = "Interface",
                            modifier = Modifier.testTag("interface"),
                        )
                        MacSettingsCategoryItem(
                            category == "update",
                            onClick = { category = "update" },
                            icon = AppleIcons.download(),
                            label = "Update",
                            alert = true,
                        )
                    }
                }
                MacSettingsPane(
                    "Bangumi sync",
                    Modifier.weight(1f),
                    back = { MacSettingsBackButton("Account", onClick = { backs++ }) },
                ) {
                    GroupedListSection(title = "Status") {
                        SettingsActionRow("Sync now", onClick = {})
                    }
                }
            }
        }
        assertEquals(232f, widthOf("bar").value, SIZE_TOLERANCE)
        assertEquals(28f, heightOf("interface").value, SIZE_TOLERANCE)
        assertEquals(48f, heightOf("account").value, SIZE_TOLERANCE)
        onNodeWithTag("interface").assertIsSelected()
        onNodeWithText("Update").performClick()
        waitForIdle()
        assertEquals("update", category)

        onNodeWithText("Account").performClick()
        waitForIdle()
        assertEquals(1, backs)
    }

    @Test
    fun `inspector is 320 wide and closes`() = runAniComposeUiTest {
        var open by mutableStateOf(true)
        setAniStyleContent(MacOSPlatform) {
            if (open) {
                MacInspector(
                    "Download details",
                    onClose = { open = false },
                    closeContentDescription = "Close",
                    modifier = Modifier.testTag("inspector"),
                ) {
                    MacInspectorCard(title = "Resource") {
                        Text("1080P")
                    }
                }
            }
        }
        assertEquals(320f, widthOf("inspector").value, SIZE_TOLERANCE)
        onNodeWithText("1080P").assertExists()
        onNodeWithContentDescription("Close").performClick()
        waitForIdle()
        onNodeWithTag("inspector").assertDoesNotExist()
    }
}
