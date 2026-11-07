/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.collection

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performMouseInput
import androidx.compose.ui.test.rightClick
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.models.episode.EpisodeCollectionInfo
import me.him188.ani.app.data.models.preference.CollectionLayout
import me.him188.ani.app.data.models.subject.ContinueWatchingStatus
import me.him188.ani.app.data.models.subject.FollowedSubjectInfo
import me.him188.ani.app.data.models.subject.SubjectCollectionInfo
import me.him188.ani.app.data.models.subject.SubjectProgressInfo
import me.him188.ani.app.data.models.subject.TestSubjectCollections
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.collection_layout_grid
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * docs/design/ani-style/04-macos.md §3.2 macOS 追番页.
 */
@OptIn(ExperimentalTestApi::class)
class MacCollectionPageTest {
    /**
     * 看过第 1 集, 第 2 集已播出未看.
     */
    private fun continuingCollection(): SubjectCollectionInfo {
        val collection = TestSubjectCollections[0]
        val (first, second) = collection.episodes
        return collection.copy(
            episodes = listOf(first, second.copy(collectionType = UnifiedCollectionType.WISH)),
            progressInfo = SubjectProgressInfo(
                ContinueWatchingStatus.Continue(
                    episodeEp = EpisodeSort(2),
                    episodeSort = EpisodeSort(2),
                    watchedEpisodeEp = EpisodeSort(1),
                    watchedEpisodeSort = EpisodeSort(1),
                ),
                nextEpisodeIdToPlay = second.episodeId,
            ),
        )
    }

    private class Callbacks {
        var layout: CollectionLayout? = null
        var collectionType: UnifiedCollectionType? = null
        var markedEpisode: EpisodeCollectionInfo? = null
        var openedSubject: Int? = null
        var played: Pair<Int, Int>? = null
        var gridLabel = ""
    }

    private fun ComposeUiTest.setCollectionPage(
        collection: SubjectCollectionInfo,
        layout: CollectionLayout,
    ): Callbacks {
        val callbacks = Callbacks()
        setContent {
            callbacks.gridLabel = stringResource(Lang.collection_layout_grid)
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    MacCollectionPageLayout(
                        items = remember { flowOf(PagingData.from(listOf(collection))) }.collectAsLazyPagingItems(),
                        continueWatching = remember { flowOf(PagingData.empty<FollowedSubjectInfo>()) }
                            .collectAsLazyPagingItems(),
                        selectedType = collection.collectionType,
                        collectionCounts = null,
                        onSelectType = {},
                        isGuest = false,
                        layout = layout,
                        onLayoutChange = { callbacks.layout = it },
                        episodeHistoryFlow = { flowOf(null) },
                        nextEpisodeCachedFlow = { flowOf(false) },
                        onClickSearch = {},
                        onClickLogin = {},
                        onClickPlaybackHistory = {},
                        onClickSubject = { callbacks.openedSubject = it.subjectId },
                        onPlay = { subjectId, episodeId -> callbacks.played = subjectId to episodeId },
                        onSetCollectionType = { _, type -> callbacks.collectionType = type },
                        onMarkEpisodeWatched = { _, episode -> callbacks.markedEpisode = episode },
                    )
                }
            }
        }
        return callbacks
    }

    private fun ComposeUiTest.menuRow(type: UnifiedCollectionType) =
        onNodeWithTag(CollectionItemMenuTestTags.type(type)).onChildren().filterToOne(hasClickAction())

    @Test
    fun `right click on a table row changes the collection type`() = runAniComposeUiTest {
        val collection = continuingCollection()
        val callbacks = setCollectionPage(collection, CollectionLayout.LIST)

        onNodeWithText(collection.subjectInfo.nameCn).performMouseInput { rightClick(center) }
        onNodeWithTag(CollectionItemMenuTestTags.Menu).assertIsDisplayed()
        menuRow(UnifiedCollectionType.DOING).assertIsSelected()

        onNodeWithTag(CollectionItemMenuTestTags.type(UnifiedCollectionType.ON_HOLD)).performClick()
        waitForIdle()
        assertEquals(UnifiedCollectionType.ON_HOLD, callbacks.collectionType)
        onNodeWithTag(CollectionItemMenuTestTags.Menu).assertDoesNotExist()
    }

    @Test
    fun `right click on a grid cell marks the next episode watched`() = runAniComposeUiTest {
        val collection = continuingCollection()
        val callbacks = setCollectionPage(collection, CollectionLayout.GRID)

        onNodeWithText(collection.subjectInfo.nameCn).performMouseInput { rightClick(center) }
        onNodeWithTag(CollectionItemMenuTestTags.MarkWatched).performClick()
        waitForIdle()
        assertEquals(collection.episodes[1], callbacks.markedEpisode)
    }

    @Test
    fun `clicking a table row opens the subject`() = runAniComposeUiTest {
        val collection = continuingCollection()
        val callbacks = setCollectionPage(collection, CollectionLayout.LIST)

        onNodeWithText(collection.subjectInfo.nameCn).performClick()
        waitForIdle()
        assertEquals(collection.subjectId, callbacks.openedSubject)
        onNodeWithTag(CollectionItemMenuTestTags.Menu).assertDoesNotExist()
    }

    @Test
    fun `toolbar switches the layout`() = runAniComposeUiTest {
        val callbacks = setCollectionPage(continuingCollection(), CollectionLayout.LIST)

        onNodeWithContentDescription(callbacks.gridLabel).performClick()
        waitForIdle()
        assertEquals(CollectionLayout.GRID, callbacks.layout)
    }
}
