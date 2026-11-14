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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.datetime.TimeZone
import me.him188.ani.app.data.repository.media.ManualBrowseMemory
import me.him188.ani.app.domain.media.SOURCE_ACG
import me.him188.ani.app.domain.media.SOURCE_DMHY
import me.him188.ani.app.domain.media.TestMediaList
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.rememberBackgroundScope
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.media_selector_mac_filter_summary
import me.him188.ani.app.ui.mediafetch.MediaSelectorState
import me.him188.ani.app.ui.mediafetch.TestMediaFetchRequest
import me.him188.ani.app.ui.mediafetch.TestMediaSourceResultListPresentation
import me.him188.ani.app.ui.mediafetch.createTestManualBrowseState
import me.him188.ani.app.ui.mediafetch.rememberTestMediaSelectorState
import me.him188.ani.app.ui.mediaselect.MediaSelectorMode
import me.him188.ani.app.ui.mediaselect.WatchingEpisode
import me.him188.ani.app.ui.mediaselect.manual.ManualBrowseTarget
import me.him188.ani.app.ui.mediaselect.selector.WebSource
import me.him188.ani.app.ui.mediaselect.selector.WebSourceChannel
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.source.MediaSourceTier
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.annotations.TestOnly
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * docs/design/ani-style/04-macos.md §3.9 macOS 选择资料来源; 05 G38、G39.
 */
@OptIn(TestOnly::class)
class MacMediaSelectorTest {
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
    private val watching = WatchingEpisode("12", "Episode")

    @Composable
    private fun ProvideMac(content: @Composable () -> Unit) {
        ProvideCompositionLocalsForPreview {
            CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64), content = content)
        }
    }

    private fun ComposeUiTest.awaitTag(tag: String) {
        waitUntil(timeoutMillis = 10_000) { onAllNodes(hasTestTag(tag)).fetchSemanticsNodes().isNotEmpty() }
    }

    @Test
    fun `header switches modes and hides bt without bt sources`() = runAniComposeUiTest {
        val modes = mutableListOf<MediaSelectorMode>()
        setContent {
            ProvideMac {
                MacMediaSelectorHeader(
                    MediaSelectorMode.AUTO,
                    onModeChange = { modes += it },
                    showBt = false,
                    watching,
                    Modifier.width(760.dp),
                )
            }
        }
        onNodeWithTag(MacMediaSelectorTestTags.Watching).assertExists()
        onNodeWithTag(MacMediaSelectorTestTags.mode(MediaSelectorMode.AUTO)).assertIsSelected()
        onNodeWithTag(MacMediaSelectorTestTags.mode(MediaSelectorMode.BT)).assertDoesNotExist()
        onNodeWithTag(MacMediaSelectorTestTags.mode(MediaSelectorMode.MANUAL)).performClick()
        assertEquals(listOf(MediaSelectorMode.MANUAL), modes)
    }

    @Test
    fun `auto mode shows the lines of the playing source and of a clicked source`() = runAniComposeUiTest {
        val selected = mutableListOf<String>()
        val refreshed = mutableListOf<String>()
        var rescue = 0
        setContent {
            ProvideMac {
                MacWebSourceColumns(
                    listOf(sourceA, failedSource, sourceB),
                    selectedSource = sourceA,
                    selectedChannel = channelsA[1],
                    onSelect = { selected += it.name },
                    onRefresh = { refreshed += it.instanceId },
                    onResolveCaptcha = {},
                    onRequestManualSearch = { rescue++ },
                    Modifier.size(760.dp, 400.dp),
                )
            }
        }

        // 默认显示当前资料源的线路, 当前线路为「播放中」
        onNodeWithTag(MacMediaSelectorTestTags.source(sourceA.instanceId)).assertIsSelected()
        onNodeWithTag(MacMediaSelectorTestTags.channel(1)).assertIsSelected()
        onNodeWithTag(MacMediaSelectorTestTags.channel(0)).assertIsNotSelected().performClick()
        assertEquals(listOf("A1"), selected)

        // 点其他资料源显示它的线路
        onNodeWithTag(MacMediaSelectorTestTags.source(sourceB.instanceId)).performClick()
        onNodeWithTag(MacMediaSelectorTestTags.source(sourceB.instanceId)).assertIsSelected()
        onNodeWithTag(MacMediaSelectorTestTags.channel(1)).assertDoesNotExist()
        onNodeWithTag(MacMediaSelectorTestTags.channel(0)).assertIsNotSelected().performClick()
        assertEquals(listOf("A1", "B1"), selected)

        // 查询失败的源排在最后, 可重试; 右栏有手动查找入口
        onNodeWithTag(MacMediaSelectorTestTags.source(failedSource.instanceId)).assertDoesNotExist()
        onNodeWithTag(MacMediaSelectorTestTags.retry(failedSource.instanceId)).performClick()
        onNodeWithTag(MacMediaSelectorTestTags.RescueButton).performClick()
        assertEquals(listOf(failedSource.instanceId), refreshed)
        assertEquals(1, rescue)
    }

    @Test
    fun `manual mode selects an episode and plays it with the button`() = runAniComposeUiTest {
        val target = ManualBrowseTarget(1, "Steins Gate", EpisodeSort(12), "12")
        val plays = mutableListOf<ManualBrowseMemory?>()
        val rememberSelection = MutableStateFlow(true)
        var played = 0
        setContent {
            ProvideMac {
                val scope = rememberBackgroundScope()
                val state = remember {
                    createTestManualBrowseState(
                        scope.backgroundScope,
                        target = target,
                        onPlay = { _, memory -> plays += memory },
                        rememberSelection = rememberSelection,
                    )
                }
                Box(Modifier.size(760.dp, 520.dp)) {
                    MacManualBrowseContent(state, watching, onPlayed = { played++ }, onCancel = {})
                }
            }
        }

        onNodeWithTag(MacMediaSelectorTestTags.SearchField).performImeAction()
        awaitTag(MacMediaSelectorTestTags.result(0))
        onNodeWithTag(MacMediaSelectorTestTags.result(0)).performClick()
        awaitTag(MacMediaSelectorTestTags.episode(11))
        onNodeWithTag(MacMediaSelectorTestTags.OpenedResult).assertExists()

        // 预选第 12 集; 点其他集只选取, 不播放
        onNodeWithTag(MacMediaSelectorTestTags.episode(11)).assertIsSelected()
        onNodeWithTag(MacMediaSelectorTestTags.episode(3)).performClick()
        onNodeWithTag(MacMediaSelectorTestTags.episode(3)).assertIsSelected()
        onNodeWithTag(MacMediaSelectorTestTags.episode(11)).assertIsNotSelected()
        runOnIdle { assertTrue(plays.isEmpty()) }

        // 「記住選擇」复选框
        onNodeWithTag(MacMediaSelectorTestTags.RememberSelection).assertIsSelected().performClick()
        waitUntil(timeoutMillis = 10_000) { !rememberSelection.value }
        onNodeWithTag(MacMediaSelectorTestTags.RememberSelection).assertIsNotSelected()

        onNodeWithTag(MacMediaSelectorTestTags.Play).assertIsEnabled().performClick()
        waitUntil(timeoutMillis = 10_000) { played == 1 }
        runOnIdle {
            // 关掉记住选择时为临时选择, 不写记忆
            assertEquals(listOf<ManualBrowseMemory?>(null), plays)
        }
    }

    @Test
    fun `manual mode plays the preselected episode with remember selection on`() = runAniComposeUiTest {
        val target = ManualBrowseTarget(1, "Steins Gate", EpisodeSort(12), "12")
        val plays = mutableListOf<ManualBrowseMemory?>()
        setContent {
            ProvideMac {
                val scope = rememberBackgroundScope()
                val state = remember {
                    createTestManualBrowseState(
                        scope.backgroundScope,
                        target = target,
                        onPlay = { _, memory -> plays += memory },
                    )
                }
                Box(Modifier.size(760.dp, 520.dp)) {
                    MacManualBrowseContent(state, watching, onPlayed = {}, onCancel = {})
                }
            }
        }

        onNodeWithTag(MacMediaSelectorTestTags.SearchField).performImeAction()
        awaitTag(MacMediaSelectorTestTags.result(0))
        onNodeWithTag(MacMediaSelectorTestTags.result(0)).performClick()
        awaitTag(MacMediaSelectorTestTags.episode(11))
        onNodeWithTag(MacMediaSelectorTestTags.Play).performClick()
        waitUntil(timeoutMillis = 10_000) { plays.isNotEmpty() }
        runOnIdle {
            assertEquals(11, assertNotNull(plays.single()).episodeIndex)
        }
    }

    @Test
    fun `bt rows are checked first and played with the button`() = runAniComposeUiTest {
        lateinit var state: MediaSelectorState
        var played = 0
        var summary = ""
        setContent {
            summary = stringResource(Lang.media_selector_mac_filter_summary, 0, 4)
            ProvideMac {
                state = rememberTestMediaSelectorState(TestMediaList)
                Box(Modifier.size(760.dp, 520.dp)) {
                    MacBtResourcesContent(
                        state,
                        TestMediaSourceResultListPresentation,
                        WatchingEpisode("01", "Episode"),
                        TestMediaFetchRequest,
                        onFetchRequestChange = {},
                        onRestartSource = {},
                        onPlayed = { played++ },
                        onCancel = {},
                        timeZone = TimeZone.UTC,
                    )
                }
            }
        }
        val row = MacMediaSelectorTestTags.btRow("$SOURCE_DMHY.1")
        awaitTag(row)
        onNodeWithTag(MacMediaSelectorTestTags.FilterSummary).assertTextEquals(summary)
        onNodeWithTag(MacMediaSelectorTestTags.Play).assertIsNotEnabled()

        // 点行只勾选
        onNodeWithTag(row).performClick()
        onNodeWithTag(row).assertIsSelected()
        runOnIdle {
            assertEquals(0, played)
            assertEquals(null, state.btPresentationFlow.value.selected)
        }

        onNodeWithTag(MacMediaSelectorTestTags.Play).assertIsEnabled().performClick()
        waitUntil(timeoutMillis = 10_000) { state.btPresentationFlow.value.selected?.mediaId == "$SOURCE_DMHY.1" }
        assertEquals(1, played)
    }

    @Test
    fun `bt excluded rows are revealed and the episode filter toggles`() = runAniComposeUiTest {
        lateinit var state: MediaSelectorState
        setContent {
            ProvideMac {
                state = rememberTestMediaSelectorState(TestMediaList)
                Box(Modifier.size(760.dp, 520.dp)) {
                    MacBtResourcesContent(
                        state,
                        TestMediaSourceResultListPresentation,
                        WatchingEpisode("01", "Episode"),
                        TestMediaFetchRequest,
                        onFetchRequestChange = {},
                        onRestartSource = {},
                        onPlayed = {},
                        onCancel = {},
                        timeZone = TimeZone.UTC,
                    )
                }
            }
        }
        val raw = MacMediaSelectorTestTags.btRow("$SOURCE_ACG.3")
        awaitTag(MacMediaSelectorTestTags.btRow("$SOURCE_DMHY.1"))
        onNodeWithTag(raw).assertDoesNotExist()
        onNodeWithTag(MacMediaSelectorTestTags.ShowExcluded).performClick()
        awaitTag(raw)
        onNodeWithTag(MacMediaSelectorTestTags.ShowExcluded).assertDoesNotExist()

        onNodeWithTag(MacMediaSelectorTestTags.EpisodeFilter).assertIsSelected().performClick()
        runOnIdle { assertFalse(state.btFilterState.episodeFilterEnabled.value) }
        onNodeWithTag(MacMediaSelectorTestTags.EpisodeFilter).assertIsNotSelected()
    }
}
