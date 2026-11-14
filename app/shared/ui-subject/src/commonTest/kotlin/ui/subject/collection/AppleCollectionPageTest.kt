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
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.paging.PagingData
import androidx.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.models.player.EpisodeHistory
import me.him188.ani.app.data.models.preference.CollectionLayout
import me.him188.ani.app.data.models.subject.ContinueWatchingStatus
import me.him188.ani.app.data.models.subject.FollowedSubjectInfo
import me.him188.ani.app.data.models.subject.SubjectCollectionInfo
import me.him188.ani.app.data.models.subject.SubjectProgressInfo
import me.him188.ani.app.data.models.subject.TestSubjectCollections
import me.him188.ani.app.data.models.subject.TestSubjectProgressInfos
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Platform
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppleCollectionPageTest {
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

    @Test
    fun `next episode to mark watched is the episode to play`() {
        val collection = continuingCollection()
        assertEquals(collection.episodes[1], collection.nextEpisodeToMarkWatched())
    }

    @Test
    fun `nothing to mark watched without a new episode`() {
        val collection = continuingCollection()
        assertNull(collection.copy(progressInfo = TestSubjectProgressInfos.Watched2).nextEpisodeToMarkWatched())
        assertNull(collection.copy(progressInfo = TestSubjectProgressInfos.Done).nextEpisodeToMarkWatched())
    }

    @Test
    fun `nothing to mark watched when the episode to play is watched`() {
        val collection = continuingCollection()
        val watched = collection.episodes.map { it.copy(collectionType = UnifiedCollectionType.DONE) }
        assertNull(collection.copy(episodes = watched).nextEpisodeToMarkWatched())
    }

    private fun ComposeUiTest.setCollectionPage(
        collection: SubjectCollectionInfo,
        layout: CollectionLayout,
        onSetCollectionType: (UnifiedCollectionType) -> Unit = {},
        onMarkEpisodeWatched: (episodeId: Int) -> Unit = {},
    ) {
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    AppleCollectionPageLayout(
                        items = remember { flowOf(PagingData.from(listOf(collection))) }.collectAsLazyPagingItems(),
                        continueWatching = remember { flowOf(PagingData.empty<FollowedSubjectInfo>()) }
                            .collectAsLazyPagingItems(),
                        selectedType = collection.collectionType,
                        collectionCounts = null,
                        onSelectType = {},
                        onRefresh = {},
                        isGuest = false,
                        layout = layout,
                        onLayoutChange = {},
                        episodeHistoryFlow = { flowOf(null) },
                        nextEpisodeCachedFlow = { flowOf(false) },
                        onClickSearch = {},
                        onClickLogin = {},
                        onClickSubject = {},
                        onPlay = { _, _ -> },
                        onSetCollectionType = { _, type -> onSetCollectionType(type) },
                        onMarkEpisodeWatched = { _, episode -> onMarkEpisodeWatched(episode.episodeId) },
                    )
                }
            }
        }
    }

    /**
     * 菜单行内可点击的节点, 选中状态在这个节点上.
     */
    private fun ComposeUiTest.menuRow(type: UnifiedCollectionType) =
        onNodeWithTag(CollectionItemMenuTestTags.type(type)).onChildren().filterToOne(hasClickAction())

    @Test
    fun `long press on a list row changes the collection type`() = runAniComposeUiTest {
        val collection = continuingCollection()
        var newType: UnifiedCollectionType? = null
        setCollectionPage(collection, CollectionLayout.LIST, onSetCollectionType = { newType = it })

        onNodeWithText(collection.subjectInfo.nameCn).performTouchInput { longClick() }
        onNodeWithTag(CollectionItemMenuTestTags.Menu).assertIsDisplayed()
        menuRow(UnifiedCollectionType.DOING).assertIsSelected()
        menuRow(UnifiedCollectionType.ON_HOLD).assertIsNotSelected()

        onNodeWithTag(CollectionItemMenuTestTags.type(UnifiedCollectionType.ON_HOLD)).performClick()
        waitForIdle()
        assertEquals(UnifiedCollectionType.ON_HOLD, newType)
        onNodeWithTag(CollectionItemMenuTestTags.Menu).assertDoesNotExist()
    }

    @Test
    fun `choosing the current collection type only closes the menu`() = runAniComposeUiTest {
        val collection = continuingCollection()
        var newType: UnifiedCollectionType? = null
        setCollectionPage(collection, CollectionLayout.LIST, onSetCollectionType = { newType = it })

        onNodeWithText(collection.subjectInfo.nameCn).performTouchInput { longClick() }
        onNodeWithTag(CollectionItemMenuTestTags.type(UnifiedCollectionType.DOING)).performClick()
        waitForIdle()
        assertNull(newType)
        onNodeWithTag(CollectionItemMenuTestTags.Menu).assertDoesNotExist()
    }

    @Test
    fun `long press on a grid cell marks the next episode watched`() = runAniComposeUiTest {
        val collection = continuingCollection()
        var marked: Int? = null
        setCollectionPage(collection, CollectionLayout.GRID, onMarkEpisodeWatched = { marked = it })

        onNodeWithText(collection.subjectInfo.nameCn).performTouchInput { longClick() }
        onNodeWithTag(CollectionItemMenuTestTags.MarkWatched).performClick()
        waitForIdle()
        assertEquals(collection.episodes[1].episodeId, marked)
        onNodeWithTag(CollectionItemMenuTestTags.Menu).assertDoesNotExist()
    }

    @Test
    fun `menu hides mark watched without a new episode`() = runAniComposeUiTest {
        val collection = continuingCollection().copy(progressInfo = TestSubjectProgressInfos.Done)
        setCollectionPage(collection, CollectionLayout.LIST)

        onNodeWithText(collection.subjectInfo.nameCn).performTouchInput { longClick() }
        onNodeWithTag(CollectionItemMenuTestTags.Menu).assertIsDisplayed()
        onNodeWithTag(CollectionItemMenuTestTags.MarkWatched).assertDoesNotExist()
    }

    @Test
    fun `remaining minutes round up`() {
        val progress = continueWatchingProgress(
            EpisodeHistory(episodeId = 1, positionMillis = 6 * 60_000L, durationMillis = 24 * 60_000L + 1),
        )!!
        assertEquals(19, progress.remainingMinutes)
        assertEquals(0.25f, progress.fraction, 0.001f)
    }

    @Test
    fun `no remaining minutes without a duration`() {
        assertNull(continueWatchingProgress(null))
        assertNull(continueWatchingProgress(EpisodeHistory(episodeId = 1, positionMillis = 1000, durationMillis = null)))
        assertNull(continueWatchingProgress(EpisodeHistory(episodeId = 1, positionMillis = 1000, durationMillis = 0)))
    }

    @Test
    fun `position past the end counts as finished`() {
        val progress = continueWatchingProgress(
            EpisodeHistory(episodeId = 1, positionMillis = 30 * 60_000L, durationMillis = 24 * 60_000L),
        )!!
        assertEquals(0, progress.remainingMinutes)
        assertEquals(1f, progress.fraction)
    }

    @Test
    fun `episode progress counts watched episodes`() {
        val collection = TestSubjectCollections[0]
        val progress = collection.episodeProgress()!!
        assertEquals(collection.episodes.count { it.collectionType == UnifiedCollectionType.DONE }, progress.watched)
        assertEquals(collection.episodes.size, progress.total)
    }

    @Test
    fun `no episode progress without episodes`() {
        assertNull(TestSubjectCollections[0].copy(episodes = emptyList()).episodeProgress())
    }
}
