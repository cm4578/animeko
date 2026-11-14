/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.mediaselect.apple

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.repository.media.ManualBrowseMemory
import me.him188.ani.app.domain.media.TestMediaList
import me.him188.ani.app.domain.media.createTestMediaProperties
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.mediafetch.createTestManualBrowseState
import me.him188.ani.app.ui.mediaselect.MediaSelectorMode
import me.him188.ani.app.ui.mediaselect.WatchingEpisode
import me.him188.ani.app.ui.mediaselect.manual.ManualBrowseTarget
import me.him188.ani.app.ui.mediaselect.selector.WebSource
import me.him188.ani.app.ui.mediaselect.selector.WebSourceChannel
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.source.BrowseEpisode
import me.him188.ani.datasources.api.source.MediaSourceTier
import me.him188.ani.utils.platform.annotations.TestOnly
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(TestOnly::class)
class AppleMediaSelectorTest {
    private fun source(
        id: String,
        channels: List<WebSourceChannel>,
        isError: Boolean = false,
        tier: MediaSourceTier? = null,
    ) = WebSource(
        instanceId = id,
        mediaSourceId = id,
        iconUrl = "",
        name = "Source $id",
        channels = channels,
        isLoading = false,
        isError = isError,
        isPreferred = false,
        tier = tier,
    )

    private val channelsA = listOf(WebSourceChannel("A1"), WebSourceChannel("A2"))
    private val channelsB = listOf(WebSourceChannel("B1"))
    private val sourceA = source("a", channelsA, tier = MediaSourceTier(0u))
    private val sourceB = source("b", channelsB)
    private val failedSource = source("c", emptyList(), isError = true)

    private class Events {
        val selected = mutableListOf<String>()
        val refreshed = mutableListOf<String>()
        var rescue = 0
    }

    private fun ComposeUiTest.setSourceList(events: Events) {
        setContent {
            ProvideCompositionLocalsForPreview {
                AppleWebSourceList(
                    listOf(sourceA, failedSource, sourceB),
                    selectedSource = sourceA,
                    selectedChannel = channelsA[1],
                    onSelect = { events.selected += it.name },
                    onRefresh = { events.refreshed += it.instanceId },
                    onResolveCaptcha = {},
                    onRequestManualSearch = { events.rescue++ },
                    Modifier.width(390.dp),
                )
            }
        }
    }

    private fun ComposeUiTest.awaitTag(tag: String) {
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun `current source is expanded and a channel can be picked`() = runAniComposeUiTest {
        val events = Events()
        setSourceList(events)

        onNodeWithTag(AppleMediaSelectorTestTags.channel(1)).assertIsSelected()
        onNodeWithTag(AppleMediaSelectorTestTags.channel(0)).assertIsNotSelected().performClick()

        assertEquals(listOf("A1"), events.selected)
    }

    @Test
    fun `tapping another source expands it instead`() = runAniComposeUiTest {
        val events = Events()
        setSourceList(events)

        onNodeWithTag(AppleMediaSelectorTestTags.source(sourceB.instanceId)).performClick()
        onNodeWithTag(AppleMediaSelectorTestTags.channel(1)).assertDoesNotExist()
        onNodeWithTag(AppleMediaSelectorTestTags.channel(0)).performClick()

        assertEquals(listOf("B1"), events.selected)
    }

    @Test
    fun `failed sources follow the rescue button and can retry`() = runAniComposeUiTest {
        val events = Events()
        setSourceList(events)

        val rescueTop = onNodeWithTag(AppleMediaSelectorTestTags.RescueButton).fetchSemanticsNode().boundsInRoot.top
        val failedTop = onNodeWithTag(AppleMediaSelectorTestTags.failedSource(failedSource.instanceId))
            .fetchSemanticsNode().boundsInRoot.top
        assertTrue(failedTop > rescueTop)
        onNodeWithTag(AppleMediaSelectorTestTags.source(failedSource.instanceId)).assertDoesNotExist()

        onNodeWithTag(AppleMediaSelectorTestTags.retry(failedSource.instanceId)).performClick()
        onNodeWithTag(AppleMediaSelectorTestTags.RescueButton).performClick()

        assertEquals(listOf(failedSource.instanceId), events.refreshed)
        assertEquals(1, events.rescue)
    }

    @Test
    fun `channel label adds the resolution`() {
        assertEquals("A1", appleChannelLabel("A1", null))
        val media = TestMediaList.first().copy(properties = createTestMediaProperties(resolution = "1080P"))
        assertEquals("A1 · 1080P", appleChannelLabel("A1", media))
    }

    @Test
    fun `browse episode label pads the parsed number`() {
        assertEquals("09", appleBrowseEpisodeLabel(BrowseEpisode("Episode 9", "https://example.com/9", EpisodeSort(9))))
        assertEquals("OVA", appleBrowseEpisodeLabel(BrowseEpisode("OVA", "https://example.com/ova")))
    }

    @Test
    fun `mode menu offers auto and manual without bt sources`() = runAniComposeUiTest {
        val modes = mutableListOf<MediaSelectorMode>()
        setContent {
            ProvideCompositionLocalsForPreview {
                AppleMediaSelectorHeader(
                    MediaSelectorMode.AUTO,
                    onModeChange = { modes += it },
                    showBt = false,
                    WatchingEpisode("09", "Episode"),
                )
            }
        }

        onNodeWithTag(AppleMediaSelectorTestTags.Watching).assertExists()
        onNodeWithTag(AppleMediaSelectorTestTags.ModeButton).performClick()
        onNodeWithTag(AppleMediaSelectorTestTags.mode(MediaSelectorMode.BT)).assertDoesNotExist()
        onNodeWithTag(AppleMediaSelectorTestTags.mode(MediaSelectorMode.MANUAL)).performClick()

        assertEquals(listOf(MediaSelectorMode.MANUAL), modes)
        onNodeWithTag(AppleMediaSelectorTestTags.ModeMenu).assertDoesNotExist()
    }

    @Test
    fun `manual page opens a result and plays the episode`() = runAniComposeUiTest {
        val target = ManualBrowseTarget(1, "Steins Gate", EpisodeSort(12), "12")
        var played = 0
        val plays = mutableListOf<ManualBrowseMemory?>()
        setContent {
            ProvideCompositionLocalsForPreview {
                val scope = rememberBackgroundScope()
                val state = remember {
                    createTestManualBrowseState(
                        scope.backgroundScope,
                        target = target,
                        onPlay = { _, memory -> plays += memory },
                    )
                }
                Box(Modifier.size(400.dp, 900.dp)) {
                    AppleManualBrowsePage(state, onPlayed = { played++ })
                }
            }
        }

        onNodeWithTag(AppleMediaSelectorTestTags.SearchField).performImeAction()
        awaitTag(AppleMediaSelectorTestTags.result(0))
        onNodeWithTag(AppleMediaSelectorTestTags.RememberSwitch).assertIsOn()

        onNodeWithTag(AppleMediaSelectorTestTags.result(0)).performClick()
        awaitTag(AppleMediaSelectorTestTags.episode(11))
        onNodeWithTag(AppleMediaSelectorTestTags.OpenedResult).assertExists()
        onNodeWithTag(AppleMediaSelectorTestTags.channelPill(0)).assertIsSelected()
        onNodeWithTag(AppleMediaSelectorTestTags.episode(11)).assertIsSelected()

        onNodeWithTag(AppleMediaSelectorTestTags.episode(11)).performClick()
        waitUntil(timeoutMillis = 10_000) { played == 1 }
        runOnIdle {
            assertEquals(11, assertNotNull(plays.single()).episodeIndex)
        }
    }
}
