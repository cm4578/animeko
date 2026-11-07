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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.compose.ui.unit.width
import me.him188.ani.app.data.models.episode.EpisodeCollectionInfo
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.episode_danmaku_count
import me.him188.ani.app.ui.subject.episode.details.DanmakuListItem
import me.him188.ani.app.ui.subject.episode.details.DanmakuListState
import me.him188.ani.danmaku.api.DanmakuServiceId
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.EpisodeType
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.Uuid
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * docs/design/ani-style/04-macos.md §3.8 macOS 播放页的右侧面板.
 */
class MacEpisodeDetailsTest {
    private fun episode(id: Int, type: UnifiedCollectionType = UnifiedCollectionType.WISH) = EpisodeCollectionInfo(
        EpisodeInfo(id, EpisodeType.MainStory, sort = EpisodeSort(id)),
        type,
    )

    /** 20 集: 4 行 (6、6、6、2). */
    private val episodes = (1..20).map { episode(it, if (it < 14) UnifiedCollectionType.DONE else UnifiedCollectionType.WISH) }

    @Test
    fun `danmaku time is minutes and padded seconds`() {
        assertEquals("0:05", formatDanmakuTime(5_400))
        assertEquals("12:00", formatDanmakuTime(720_000))
    }

    @Test
    fun `episode grid shows the two rows around the playing episode and expands`() = runAniComposeUiTest {
        var expanded by mutableStateOf(false)
        val clicked = mutableListOf<Int>()
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    MacEpisodeGrid(
                        episodes,
                        playingEpisodeId = 14,
                        expanded = expanded,
                        onToggleExpanded = { expanded = !expanded },
                        onClickEpisode = { clicked += it.episodeInfo.episodeId },
                        onLongClickEpisode = {},
                        Modifier.width(328.dp),
                    )
                }
            }
        }
        // 第 14 集在第 3 行, 显示第 3、4 行
        onNodeWithTag(MacEpisodeSidebarTestTags.episode(13)).assertExists()
        onNodeWithTag(MacEpisodeSidebarTestTags.episode(20)).assertExists()
        onNodeWithTag(MacEpisodeSidebarTestTags.episode(12)).assertDoesNotExist()
        onNodeWithTag(MacEpisodeSidebarTestTags.episode(14)).assertIsSelected()

        // 1:1 方块, 6 栏
        val tile = onNodeWithTag(MacEpisodeSidebarTestTags.episode(14)).getUnclippedBoundsInRoot()
        assertEquals(tile.width, tile.height)

        onNodeWithTag(MacEpisodeSidebarTestTags.AllEpisodes).performClick()
        waitForIdle()
        onNodeWithTag(MacEpisodeSidebarTestTags.episode(1)).assertExists()

        onNodeWithTag(MacEpisodeSidebarTestTags.episode(1)).performClick()
        assertEquals(listOf(1), clicked)
    }

    @Test
    fun `short episode lists hide the all episodes link`() = runAniComposeUiTest {
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    MacEpisodeGrid(
                        episodes.take(12),
                        playingEpisodeId = 3,
                        expanded = false,
                        onToggleExpanded = {},
                        onClickEpisode = {},
                        onLongClickEpisode = {},
                        Modifier.width(328.dp),
                    )
                }
            }
        }
        onNodeWithTag(MacEpisodeSidebarTestTags.AllEpisodes).assertDoesNotExist()
        onNodeWithTag(MacEpisodeSidebarTestTags.episode(12)).assertExists()
    }

    @Test
    fun `danmaku list shows the count and rows`() = runAniComposeUiTest {
        var countText = ""
        val items = listOf(
            DanmakuListItem("1", Uuid.random(), "hello", 65_000, DanmakuServiceId.Animeko, isSelf = false),
            DanmakuListItem("2", Uuid.random(), "world", 5_000, DanmakuServiceId.Animeko, isSelf = true),
        )
        setContent {
            countText = stringResource(Lang.episode_danmaku_count, 2)
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    MacDanmakuList(
                        DanmakuListState(items, emptyList(), isLoading = false, isEmpty = false),
                        Modifier.width(328.dp),
                    )
                }
            }
        }
        onNodeWithTag(MacEpisodeSidebarTestTags.DanmakuCount).assertTextEquals(countText)
        onNodeWithText("hello").assertExists()
        onNodeWithText("1:05").assertExists()
        onNodeWithText("0:05").assertExists()
    }
}
