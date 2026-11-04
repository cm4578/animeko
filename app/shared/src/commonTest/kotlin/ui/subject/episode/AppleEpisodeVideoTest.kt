/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode

import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.episode.share.MediaShareData
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

class AppleEpisodeVideoTest {
    private class Recorder {
        val events = mutableListOf<String>()
        val setTypes = mutableListOf<UnifiedCollectionType>()
        val menuExpanded = mutableListOf<Boolean>()
    }

    private fun ComposeUiTest.setTopBar(
        recorder: Recorder,
        collectionType: UnifiedCollectionType = UnifiedCollectionType.WISH,
        isLoggedIn: Boolean = true,
        shareData: MediaShareData = MediaShareData(websiteUrl = null, download = null),
    ) {
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    AppleEpisodeVideoTopBar(
                        episodeTitle = appleEpisodeLabel("09", "Episode"),
                        onBack = { recorder.events += "back" },
                        collectionType = collectionType,
                        collectionTypeEnabled = true,
                        isLoggedIn = isLoggedIn,
                        onSetCollectionType = { recorder.setTypes += it },
                        onClickLogin = { recorder.events += "login" },
                        onClickCache = { recorder.events += "cache" },
                        shareData = shareData,
                        onClickWatchTogether = { recorder.events += "watchTogether" },
                        onClickDanmakuSettings = { recorder.events += "danmakuSettings" },
                        skipOpEdSeconds = 85,
                        onClickSkipOpEd = { recorder.events += "skip" },
                        playerStatsVisible = false,
                        onTogglePlayerStats = { recorder.events += "stats" },
                        Modifier.width(390.dp),
                        onMenuExpandedChange = { recorder.menuExpanded += it },
                    )
                }
            }
        }
    }

    @Test
    fun `episode label joins ep and title`() {
        assertEquals("09  Title", appleEpisodeLabel("09", "Title"))
        assertEquals("09", appleEpisodeLabel("09", " "))
    }

    @Test
    fun `back button goes back`() = runAniComposeUiTest {
        val recorder = Recorder()
        setTopBar(recorder)
        onNodeWithTag(AppleEpisodeVideoTestTags.Back).performClick()
        assertEquals(listOf("back"), recorder.events)
    }

    @Test
    fun `menu changes the collection type`() = runAniComposeUiTest {
        val recorder = Recorder()
        setTopBar(recorder)

        onNodeWithTag(AppleEpisodeVideoTestTags.More).performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.Menu).assertIsDisplayed()
        onNodeWithTag(AppleEpisodeVideoTestTags.CollectionState).performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.collectionType(UnifiedCollectionType.DOING)).performClick()
        waitForIdle()

        assertEquals(listOf(UnifiedCollectionType.DOING), recorder.setTypes)
        onNodeWithTag(AppleEpisodeVideoTestTags.Menu).assertDoesNotExist()
        assertEquals(listOf(false, true, false), recorder.menuExpanded)
    }

    @Test
    fun `unfollow asks for confirmation`() = runAniComposeUiTest {
        val recorder = Recorder()
        setTopBar(recorder)

        onNodeWithTag(AppleEpisodeVideoTestTags.More).performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.CollectionState).performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.Unfollow).performClick()
        assertEquals(emptyList(), recorder.setTypes)
        onNodeWithTag(AppleEpisodeVideoTestTags.ConfirmUnfollow).performClick()
        waitForIdle()

        assertEquals(listOf(UnifiedCollectionType.NOT_COLLECTED), recorder.setTypes)
    }

    @Test
    fun `guest is asked to log in`() = runAniComposeUiTest {
        val recorder = Recorder()
        setTopBar(recorder, isLoggedIn = false)

        onNodeWithTag(AppleEpisodeVideoTestTags.More).performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.CollectionState).performClick()

        assertEquals(listOf("login"), recorder.events)
        assertEquals(emptyList(), recorder.setTypes)
    }

    @Test
    fun `menu opens downloads and skips op ed`() = runAniComposeUiTest {
        val recorder = Recorder()
        setTopBar(recorder)

        onNodeWithTag(AppleEpisodeVideoTestTags.More).performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.Download).performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.More).performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.SkipOpEd).performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.More).performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.DanmakuSettings).performClick()

        assertEquals(listOf("cache", "skip", "danmakuSettings"), recorder.events)
    }

    @Test
    fun `share needs a link`() = runAniComposeUiTest {
        val recorder = Recorder()
        setTopBar(recorder)

        onNodeWithTag(AppleEpisodeVideoTestTags.More).performClick()
        // 菜单行的标签在外层, 停用状态在可点击的子节点上.
        onNodeWithTag(AppleEpisodeVideoTestTags.Share)
            .onChildren()
            .filterToOne(hasClickAction())
            .assertIsNotEnabled()
    }

    @Test
    fun `share lists the stream link`() = runAniComposeUiTest {
        val recorder = Recorder()
        setTopBar(
            recorder,
            shareData = MediaShareData(
                websiteUrl = "https://example.com/1",
                download = ResourceLocation.HttpStreamingFile("https://example.com/1.m3u8"),
            ),
        )

        onNodeWithTag(AppleEpisodeVideoTestTags.More).performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.Share).performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.ShareCopyLink).assertIsDisplayed()
    }

    @Test
    fun `controls toggle danmaku and fullscreen`() = runAniComposeUiTest {
        val events = mutableListOf<String>()
        setContent {
            ProvideCompositionLocalsForPreview {
                AppleEpisodeVideoControls(
                    progressSlider = {},
                    progressText = { Text("00:00 / 24:00") },
                    danmakuEnabled = true,
                    onToggleDanmaku = { events += "danmaku" },
                    isFullscreen = false,
                    onToggleFullscreen = { events += "fullscreen" },
                    Modifier.width(390.dp),
                )
            }
        }

        onNodeWithTag(AppleEpisodeVideoTestTags.DanmakuToggle).assertIsOn().performClick()
        onNodeWithTag(AppleEpisodeVideoTestTags.Fullscreen).performClick()
        assertEquals(listOf("danmaku", "fullscreen"), events)
    }

    @Test
    fun `danmaku toggle reports off`() = runAniComposeUiTest {
        setContent {
            ProvideCompositionLocalsForPreview {
                AppleEpisodeVideoControls(
                    progressSlider = {},
                    progressText = {},
                    danmakuEnabled = false,
                    onToggleDanmaku = {},
                    isFullscreen = false,
                    onToggleFullscreen = {},
                )
            }
        }
        onNodeWithTag(AppleEpisodeVideoTestTags.DanmakuToggle).assertIsOff()
    }

    @Test
    fun `play button toggles playback`() = runAniComposeUiTest {
        var clicks = 0
        setContent {
            ProvideCompositionLocalsForPreview {
                AppleVideoPlayButton(isPlaying = false, onClick = { clicks++ })
            }
        }
        onNodeWithTag(AppleEpisodeVideoTestTags.PlayButton).performClick()
        assertEquals(1, clicks)
    }
}
