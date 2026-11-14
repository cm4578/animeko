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
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.episode.EpisodeCollectionInfo
import me.him188.ani.app.data.models.episode.EpisodeInfo
import me.him188.ani.app.domain.media.TestMediaList
import me.him188.ani.app.domain.media.createTestMediaProperties
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.apple.EpisodeTileState
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.media.MediaDetailsStrings
import me.him188.ani.app.ui.mediaselect.summary.MediaSelectorSummary
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.EpisodeType
import me.him188.ani.datasources.api.SubtitleKind
import me.him188.ani.datasources.api.topic.SubtitleLanguage
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

class AppleEpisodeDetailsTest {
    private fun episode(id: Int, type: UnifiedCollectionType) = EpisodeCollectionInfo(
        EpisodeInfo(id, EpisodeType.MainStory, sort = EpisodeSort(id)),
        type,
    )

    private val episodes = listOf(
        episode(1, UnifiedCollectionType.DONE),
        episode(2, UnifiedCollectionType.DONE),
        episode(3, UnifiedCollectionType.WISH),
    )

    private val strings = MediaDetailsStrings(
        subtitleKindEmbedded = "Embedded",
        subtitleKindClosed = "Closed",
        subtitleKindExternalProvided = "External",
        subtitleKindExternalDiscovered = "Discovered",
        subtitleKindClosedOrExternalDiscovered = "ClosedOrDiscovered",
        unknown = "Unknown",
        subtitleLanguageChineseCantonese = "CHC",
        subtitleLanguageChineseSimplified = "CHS",
        subtitleLanguageChineseTraditional = "CHT",
        subtitleLanguageJapanese = "JPN",
        subtitleLanguageEnglish = "ENG",
    )

    @Test
    fun `playing episode wins over watched`() {
        assertEquals(EpisodeTileState.Playing, appleEpisodeTileState(episodes[1], playingEpisodeId = 2))
        assertEquals(EpisodeTileState.Watched, appleEpisodeTileState(episodes[0], playingEpisodeId = 2))
        assertEquals(EpisodeTileState.Unwatched, appleEpisodeTileState(episodes[2], playingEpisodeId = 2))
    }

    @Test
    fun `media source text lists the resource properties`() {
        assertEquals("Mikan", appleMediaSourceText("Mikan", null, strings))
        val media = TestMediaList.first().copy(
            properties = createTestMediaProperties(
                subtitleLanguageIds = listOf(SubtitleLanguage.ChineseSimplified.id, SubtitleLanguage.Japanese.id),
                resolution = "1080P",
                subtitleKind = SubtitleKind.EMBEDDED,
            ),
        )
        assertEquals("Mikan · 1080P · CHS/JPN · Embedded", appleMediaSourceText("Mikan", media, strings))
    }

    @Test
    fun `episode row plays and toggles watched`() = runAniComposeUiTest {
        val clicked = mutableListOf<Int>()
        val longClicked = mutableListOf<Int>()
        var toggles = 0
        setContent {
            ProvideCompositionLocalsForPreview {
                AppleEpisodeSection(
                    episodes,
                    playingEpisodeId = 2,
                    expanded = false,
                    onToggleExpanded = { toggles++ },
                    onClickEpisode = { clicked += it.episodeId },
                    onLongClickEpisode = { longClicked += it.episodeId },
                    Modifier.width(390.dp),
                )
            }
        }

        onNodeWithTag(AppleEpisodeDetailsTestTags.episode(2)).assertIsSelected()
        onNodeWithTag(AppleEpisodeDetailsTestTags.episode(1)).assertIsNotSelected()
        onNodeWithTag(AppleEpisodeDetailsTestTags.episode(3)).performClick()
        onNodeWithTag(AppleEpisodeDetailsTestTags.episode(1)).performTouchInput { longClick() }
        onNodeWithTag(AppleEpisodeDetailsTestTags.AllEpisodes).performClick()

        assertEquals(listOf(3), clicked)
        assertEquals(listOf(1), longClicked)
        assertEquals(1, toggles)
    }

    @Test
    fun `expanded episode grid shows every episode`() = runAniComposeUiTest {
        setContent {
            ProvideCompositionLocalsForPreview {
                AppleEpisodeSection(
                    episodes,
                    playingEpisodeId = 3,
                    expanded = true,
                    onToggleExpanded = {},
                    onClickEpisode = {},
                    onLongClickEpisode = {},
                    Modifier.width(390.dp),
                )
            }
        }
        episodes.forEach { onNodeWithTag(AppleEpisodeDetailsTestTags.episode(it.episodeId)).assertExists() }
    }

    @Test
    fun `media source card opens the selector`() = runAniComposeUiTest {
        var clicks = 0
        setContent {
            ProvideCompositionLocalsForPreview {
                AppleMediaSourceCard(
                    MediaSelectorSummary.AutoSelecting(emptyList(), 5.seconds),
                    selectedMedia = null,
                    onClick = { clicks++ },
                    Modifier.width(390.dp),
                )
            }
        }
        onNodeWithTag(AppleEpisodeDetailsTestTags.MediaSource).performClick()
        assertEquals(1, clicks)
    }

    @Test
    fun `danmaku input and style open the editor`() = runAniComposeUiTest {
        val events = mutableListOf<String>()
        setContent {
            ProvideCompositionLocalsForPreview {
                AppleDanmakuInputRow(
                    placeholder = "Say something",
                    onClick = { events += "input" },
                    onClickStyle = { events += "style" },
                    Modifier.width(390.dp),
                )
            }
        }
        onNodeWithTag(AppleEpisodeDetailsTestTags.DanmakuInput).performClick()
        onNodeWithTag(AppleEpisodeDetailsTestTags.DanmakuStyle).performClick()
        assertEquals(listOf("input", "style"), events)
    }
}
