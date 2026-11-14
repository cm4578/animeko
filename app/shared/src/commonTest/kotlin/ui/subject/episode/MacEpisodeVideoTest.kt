/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import me.him188.ani.app.ui.episode.share.MediaShareData
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * docs/design/ani-style/04-macos.md §3.8 macOS 播放页的影片区; 05 G35、G36.
 */
class MacEpisodeVideoTest {
    private class Recorder {
        val events = mutableListOf<String>()
        val setTypes = mutableListOf<UnifiedCollectionType>()
    }

    private fun ComposeUiTest.setTopBar(recorder: Recorder, sidebarVisible: Boolean = true) {
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    MacEpisodeVideoTopBar(
                        title = macEpisodeVideoTitle("Subject", "第 9 話", "Episode"),
                        onBack = { recorder.events += "back" },
                        collectionType = UnifiedCollectionType.WISH,
                        collectionTypeEnabled = true,
                        isLoggedIn = true,
                        onSetCollectionType = { recorder.setTypes += it },
                        onClickLogin = { recorder.events += "login" },
                        onClickCache = { recorder.events += "cache" },
                        shareData = MediaShareData(websiteUrl = "https://example.com", download = null),
                        sidebarVisible = sidebarVisible,
                        onToggleSidebar = { recorder.events += "sidebar" },
                        Modifier.width(900.dp),
                    )
                }
            }
        }
    }

    @Test
    fun `title joins subject episode label and episode title`() {
        assertEquals("Subject · 第 9 話 · Episode", macEpisodeVideoTitle("Subject", "第 9 話", "Episode").text)
        assertEquals("Subject · 第 9 話", macEpisodeVideoTitle("Subject", "第 9 話", " ").text)
        assertEquals("9", macEpisodeNumber("09"))
        assertEquals("0", macEpisodeNumber("00"))
        assertEquals("SP1", macEpisodeNumber("SP1"))
    }

    @Test
    fun `menu has only collection state download and share`() = runAniComposeUiTest {
        val recorder = Recorder()
        setTopBar(recorder)

        onNodeWithTag(MacEpisodeVideoTestTags.More).performClick()
        onNodeWithTag(MacEpisodeVideoTestTags.Menu).assertIsDisplayed()
        onNodeWithTag(MacEpisodeVideoTestTags.CollectionState).assertIsDisplayed()
        onNodeWithTag(MacEpisodeVideoTestTags.Share).assertIsDisplayed()
        // 现有「更多」菜单的项目只在全屏的控制列 (05 G35)
        onNodeWithTag(AppleEpisodeVideoTestTags.DanmakuSettings).assertDoesNotExist()
        onNodeWithTag(AppleEpisodeVideoTestTags.SkipOpEd).assertDoesNotExist()

        onNodeWithTag(MacEpisodeVideoTestTags.Download).performClick()
        waitForIdle()
        assertEquals(listOf("cache"), recorder.events)
        onNodeWithTag(MacEpisodeVideoTestTags.Menu).assertDoesNotExist()
    }

    @Test
    fun `menu changes the collection type`() = runAniComposeUiTest {
        val recorder = Recorder()
        setTopBar(recorder)

        onNodeWithTag(MacEpisodeVideoTestTags.More).performClick()
        onNodeWithTag(MacEpisodeVideoTestTags.CollectionState).performClick()
        onNodeWithTag(MacEpisodeVideoTestTags.collectionType(UnifiedCollectionType.DOING)).performClick()
        waitForIdle()
        assertEquals(listOf(UnifiedCollectionType.DOING), recorder.setTypes)
    }

    @Test
    fun `back and sidebar toggle`() = runAniComposeUiTest {
        val recorder = Recorder()
        setTopBar(recorder)
        onNodeWithTag(MacEpisodeVideoTestTags.Back).performClick()
        onNodeWithTag(MacEpisodeVideoTestTags.SidebarToggle).performClick()
        assertEquals(listOf("back", "sidebar"), recorder.events)
    }

    @Test
    fun `controls play mute volume and fullscreen`() = runAniComposeUiTest {
        val events = mutableListOf<String>()
        var level by mutableFloatStateOf(50f)
        var isMute by mutableStateOf(false)
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    MacEpisodeVideoControls(
                        progressSlider = {},
                        progressText = {},
                        isPlaying = false,
                        onTogglePlay = { events += "play" },
                        hasNextEpisode = true,
                        onClickNextEpisode = { events += "next" },
                        volume = MacVolumeControl(
                            level = level,
                            maxLevel = 100f,
                            isMute = isMute,
                            onSetLevel = { level = it },
                            onToggleMute = { isMute = !isMute },
                        ),
                        danmakuEditor = { modifier -> Box(modifier.height(32.dp).testTag("danmaku-editor")) },
                        speedSwitcher = {},
                        danmakuEnabled = true,
                        onToggleDanmaku = { events += "danmaku" },
                        onEnterFullscreen = { events += "fullscreen" },
                        Modifier.width(900.dp),
                    )
                }
            }
        }
        onNodeWithTag(MacEpisodeVideoTestTags.Play).performClick()
        onNodeWithTag(MacEpisodeVideoTestTags.NextEpisode).performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.DanmakuToggle).performClick()
        onNodeWithTag(MacEpisodeVideoTestTags.Fullscreen).performClick()
        assertEquals(listOf("play", "next", "danmaku", "fullscreen"), events)

        onNodeWithTag(MacEpisodeVideoTestTags.Mute).performClick()
        waitForIdle()
        assertTrue(isMute)

        onNodeWithTag(MacEpisodeVideoTestTags.Volume).performSemanticsAction(SemanticsActions.SetProgress) { it(80f) }
        waitForIdle()
        assertEquals(80f, level)
    }

    @Test
    fun `danmaku input is centered and at most 280 wide`() = runAniComposeUiTest {
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    Box(Modifier.width(1200.dp).testTag("root")) {
                        MacEpisodeVideoControls(
                            progressSlider = {},
                            progressText = {},
                            isPlaying = true,
                            onTogglePlay = {},
                            hasNextEpisode = false,
                            onClickNextEpisode = {},
                            volume = null,
                            danmakuEditor = { modifier -> Box(modifier.height(32.dp).testTag("danmaku-editor")) },
                            speedSwitcher = {},
                            danmakuEnabled = false,
                            onToggleDanmaku = {},
                            onEnterFullscreen = {},
                            Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        }
        val editor = onNodeWithTag("danmaku-editor").getUnclippedBoundsInRoot()
        val play = onNodeWithTag(MacEpisodeVideoTestTags.Play).getUnclippedBoundsInRoot()
        val fullscreen = onNodeWithTag(MacEpisodeVideoTestTags.Fullscreen).getUnclippedBoundsInRoot()
        assertEquals(280.dp, editor.width)
        // 在左右两组之间置中
        val leftGap = editor.left - play.right
        val rightGap = fullscreen.left - editor.right
        assertTrue(leftGap > 0.dp && rightGap > 0.dp, "left=$leftGap right=$rightGap")
        assertTrue(abs((editor.left + editor.right).value / 2 - 600f) < 200f)
    }
}
