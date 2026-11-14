/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.details

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.width
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.data.models.subject.TestSubjectInfo
import me.him188.ani.app.data.models.subject.TestSubjectProgressInfos
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.ui.comment.createTestCommentState
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.apple.EpisodeTileDefaults
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.subject_details_watched_episodes
import me.him188.ani.app.ui.rating.EditableRatingActions
import me.him188.ani.app.ui.rating.TestEditableRatingUiState
import me.him188.ani.app.ui.search.createTestPager
import me.him188.ani.app.ui.subject.TestSubjectAiringInfo
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.details.layout.AppleSubjectDetailsTestTags
import me.him188.ani.app.ui.subject.details.state.SubjectDetailsActions
import me.him188.ani.app.ui.subject.details.state.SubjectDetailsState
import me.him188.ani.app.ui.subject.details.state.SubjectDetailsUiState
import me.him188.ani.app.ui.subject.episode.list.EpisodeListItem
import me.him188.ani.app.ui.subject.episode.list.EpisodeListUiState
import me.him188.ani.app.ui.user.SelfInfoUiState
import me.him188.ani.app.ui.user.TestSelfInfoUiState
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import org.jetbrains.compose.resources.stringResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * docs/design/ani-style/04-macos.md §3.7 macOS 条目详情; 05 G6.
 */
class MacSubjectDetailsPageTest {
    private class RecordingActions : SubjectDetailsActions, EditableRatingActions by EditableRatingActions.Noop {
        val setTypes = mutableListOf<UnifiedCollectionType>()
        var ratingRequests = 0

        override suspend fun setSelfCollectionType(new: UnifiedCollectionType): LoadError? {
            setTypes += new
            return null
        }

        override val shouldOfferMarkAllWatched: Boolean get() = false
        override fun setAllEpisodesWatched() {}
        override suspend fun setAllEpisodesWatchedAwait(): LoadError? = null
        override fun dismissSetAllEpisodesDoneDialog() {}
        override fun requestEditRating() {
            ratingRequests++
        }
    }

    private fun episode(id: Int, type: UnifiedCollectionType) = EpisodeListItem(
        episodeId = id,
        sort = EpisodeSort(id - 100),
        ep = null,
        name = "",
        nameCn = "Episode $id",
        collectionType = type,
        isBroadcast = true,
    )

    /** 16 集: 第一行 14 个, 第二行 2 个. */
    private val episodes = (101..116).map { episode(it, if (it == 101) UnifiedCollectionType.DONE else UnifiedCollectionType.WISH) }

    private class Labels {
        var watched = ""
    }

    private fun ComposeUiTest.setPage(
        actions: RecordingActions,
        selfInfo: SelfInfoUiState = TestSelfInfoUiState,
        onPlay: (Int) -> Unit = {},
    ): Labels {
        val labels = Labels()
        setContent {
            labels.watched = stringResource(Lang.subject_details_watched_episodes, 1, episodes.size)
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    val scope = rememberCoroutineScope()
                    val state = remember {
                        SubjectDetailsState(
                            subjectId = TestSubjectInfo.subjectId,
                            info = TestSubjectInfo,
                            staffPager = createTestPager(emptyList()),
                            exposedStaffPager = createTestPager(emptyList()),
                            charactersPager = createTestPager(emptyList()),
                            exposedCharactersPager = createTestPager(emptyList()),
                            relatedSubjectsPager = createTestPager(emptyList()),
                            subjectCommentState = createTestCommentState(scope),
                            uiState = MutableStateFlow(
                                SubjectDetailsUiState(
                                    subjectId = TestSubjectInfo.subjectId,
                                    displayName = TestSubjectInfo.displayName,
                                    selfCollectionType = UnifiedCollectionType.WISH,
                                    collectionStats = TestSubjectInfo.collectionStats,
                                    airingInfo = TestSubjectAiringInfo,
                                    progressInfo = TestSubjectProgressInfos.ContinueWatching2.copy(nextEpisodeIdToPlay = 102),
                                    episodeListUiState = EpisodeListUiState("", episodes, emptyList()),
                                    totalStaffCount = 0,
                                    totalCharactersCount = 0,
                                    collectionTypeEdit = EditableSubjectCollectionTypeState.Presentation.Placeholder.copy(
                                        selfCollectionType = UnifiedCollectionType.WISH,
                                        isPlaceholder = false,
                                    ),
                                    rating = TestEditableRatingUiState,
                                ),
                            ),
                            actions = actions,
                        )
                    }
                    MacSubjectDetailsPage(
                        state,
                        selfInfo,
                        onPlay = onPlay,
                        onClickTag = {},
                        onEpisodeCollectionUpdate = {},
                        Modifier.size(1060.dp, 900.dp),
                    )
                }
            }
        }
        return labels
    }

    @Test
    fun `play button plays the next episode`() = runAniComposeUiTest {
        val played = mutableListOf<Int>()
        setPage(RecordingActions(), onPlay = { played += it })
        onNodeWithTag(AppleSubjectDetailsTestTags.Play).performClick()
        assertEquals(listOf(102), played)
    }

    @Test
    fun `collection menu changes the collection`() = runAniComposeUiTest {
        val actions = RecordingActions()
        setPage(actions)
        onNodeWithTag(AppleSubjectDetailsTestTags.CollectionButton).performClick()
        onNodeWithTag(AppleSubjectDetailsTestTags.CollectionMenu).assertIsDisplayed()
    }

    @Test
    fun `write review opens the rating editor`() = runAniComposeUiTest {
        val actions = RecordingActions()
        setPage(actions)
        onNodeWithTag(MacSubjectDetailsTestTags.WriteReview).performClick()
        assertEquals(1, actions.ratingRequests)
    }

    @Test
    fun `guests see login and no write review button`() = runAniComposeUiTest {
        setPage(RecordingActions(), selfInfo = TestSelfInfoUiState.copy(isSessionValid = false))
        onNodeWithTag(AppleSubjectDetailsTestTags.CollectionButton).assertDoesNotExist()
        onNodeWithTag(MacSubjectDetailsTestTags.WriteReview).assertDoesNotExist()
    }

    @Test
    fun `toolbar share opens the share menu`() = runAniComposeUiTest {
        setPage(RecordingActions())
        onNodeWithTag(MacSubjectDetailsTestTags.OpenInBangumi).assertIsDisplayed()
        onNodeWithTag(AppleSubjectDetailsTestTags.Share).performClick()
        onNodeWithTag(AppleSubjectDetailsTestTags.ShareMenu).assertIsDisplayed()
    }

    @Test
    fun `episodes are laid out fourteen per row`() = runAniComposeUiTest {
        val labels = setPage(RecordingActions())
        onNodeWithTag(MacSubjectDetailsTestTags.WatchedEpisodes).assertTextEquals(labels.watched)
        onNodeWithTag(AppleSubjectDetailsTestTags.episode(102)).assertIsSelected()

        val first = onNodeWithTag(AppleSubjectDetailsTestTags.episode(101)).getUnclippedBoundsInRoot()
        val last = onNodeWithTag(AppleSubjectDetailsTestTags.episode(114)).getUnclippedBoundsInRoot()
        val nextRow = onNodeWithTag(AppleSubjectDetailsTestTags.episode(115)).getUnclippedBoundsInRoot()
        assertEquals(first.top, last.top)
        assertTrue(nextRow.top > first.top, "episode 15 should start the second row")
        assertEquals(first.left, nextRow.left)
        // 末行补齐空位, 方块与第一行等宽
        assertEquals(first.width, nextRow.width)
        onNodeWithTag(AppleSubjectDetailsTestTags.episode(101)).assertHeightIsEqualTo(EpisodeTileDefaults.MacGridHeight)
    }

    @Test
    fun `tabs switch pages`() = runAniComposeUiTest {
        setPage(RecordingActions())
        onNodeWithTag(AppleSubjectDetailsTestTags.tab(0)).assertIsSelected()
        onNodeWithTag(AppleSubjectDetailsTestTags.tab(2)).performClick()
        waitForIdle()
        onNodeWithTag(AppleSubjectDetailsTestTags.tab(2)).assertIsSelected()
        onNodeWithTag(AppleSubjectDetailsTestTags.episode(101)).assertDoesNotExist()
    }
}
