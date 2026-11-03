/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.details

import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.MutableStateFlow
import me.him188.ani.app.data.models.subject.TestSubjectInfo
import me.him188.ani.app.data.models.subject.TestSubjectProgressInfos
import me.him188.ani.app.domain.episode.SetEpisodeCollectionTypeRequest
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.ui.comment.createTestCommentState
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.rating.EditableRatingActions
import me.him188.ani.app.ui.rating.TestEditableRatingUiState
import me.him188.ani.app.ui.search.createTestPager
import me.him188.ani.app.ui.subject.TestSubjectAiringInfo
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.details.layout.AppleSubjectDetailsTestTags
import me.him188.ani.app.ui.subject.details.layout.episodeTileText
import me.him188.ani.app.ui.subject.details.state.SubjectDetailsActions
import me.him188.ani.app.ui.subject.details.state.SubjectDetailsState
import me.him188.ani.app.ui.subject.details.state.SubjectDetailsUiState
import me.him188.ani.app.ui.subject.episode.list.EpisodeListItem
import me.him188.ani.app.ui.subject.episode.list.EpisodeListUiState
import me.him188.ani.app.ui.user.SelfInfoUiState
import me.him188.ani.app.ui.user.TestSelfInfoUiState
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.utils.platform.Platform
import kotlin.test.Test
import kotlin.test.assertEquals

class AppleSubjectDetailsPageTest {
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
        sort = EpisodeSort(id),
        ep = null,
        name = "",
        nameCn = "Episode $id",
        collectionType = type,
        isBroadcast = true,
    )

    private val episodes = listOf(
        episode(101, UnifiedCollectionType.DONE),
        episode(102, UnifiedCollectionType.WISH),
        episode(103, UnifiedCollectionType.WISH),
    )

    private fun ComposeUiTest.setPage(
        actions: RecordingActions,
        selfInfo: SelfInfoUiState = TestSelfInfoUiState,
        onPlay: (Int) -> Unit = {},
        onEpisodeCollectionUpdate: (SetEpisodeCollectionTypeRequest) -> Unit = {},
    ) {
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
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
                    AppleSubjectDetailsPage(
                        state,
                        selfInfo,
                        onPlay = onPlay,
                        onClickTag = {},
                        onEpisodeCollectionUpdate = onEpisodeCollectionUpdate,
                        onBack = {},
                        Modifier.width(390.dp),
                    )
                }
            }
        }
    }

    @Test
    fun `episode tile text pads integers`() {
        assertEquals("04", episodeTileText(EpisodeSort(4)))
        assertEquals("12", episodeTileText(EpisodeSort(12)))
        assertEquals(EpisodeSort("SP").toString(), episodeTileText(EpisodeSort("SP")))
        assertEquals("https://bgm.tv/subject/1", bangumiSubjectUrl(1))
    }

    @Test
    fun `play button plays the next episode`() = runAniComposeUiTest {
        val played = mutableListOf<Int>()
        setPage(RecordingActions(), onPlay = { played += it })
        onNodeWithTag(AppleSubjectDetailsTestTags.Play).performClick()
        assertEquals(listOf(102), played)
    }

    @Test
    fun `collection menu changes and removes the collection`() = runAniComposeUiTest {
        val actions = RecordingActions()
        setPage(actions)

        onNodeWithTag(AppleSubjectDetailsTestTags.CollectionButton).performClick()
        onNodeWithTag(AppleSubjectDetailsTestTags.CollectionMenu).assertIsDisplayed()
        onNodeWithTag(AppleSubjectDetailsTestTags.Unfollow).assertIsDisplayed()

        onNodeWithTag(AppleSubjectDetailsTestTags.Unfollow).performClick()
        onNodeWithTag(AppleSubjectDetailsTestTags.ConfirmUnfollow).performClick()
        waitForIdle()
        assertEquals(listOf(UnifiedCollectionType.NOT_COLLECTED), actions.setTypes)
    }

    @Test
    fun `guest sees login instead of the collection button`() = runAniComposeUiTest {
        val actions = RecordingActions()
        setPage(actions, selfInfo = TestSelfInfoUiState.copy(isSessionValid = false))
        onNodeWithTag(AppleSubjectDetailsTestTags.CollectionButton).assertDoesNotExist()
        onNodeWithTag(AppleSubjectDetailsTestTags.Rating).performClick()
        assertEquals(0, actions.ratingRequests)
    }

    @Test
    fun `rating cell opens the rating editor`() = runAniComposeUiTest {
        val actions = RecordingActions()
        setPage(actions)
        onNodeWithTag(AppleSubjectDetailsTestTags.Rating).performClick()
        assertEquals(1, actions.ratingRequests)
    }

    @Test
    fun `share opens the share menu`() = runAniComposeUiTest {
        setPage(RecordingActions())
        onNodeWithTag(AppleSubjectDetailsTestTags.Share).performClick()
        onNodeWithTag(AppleSubjectDetailsTestTags.ShareMenu).assertIsDisplayed()
    }

    @Test
    fun `episode tiles mark the next episode and long press toggles watched`() = runAniComposeUiTest {
        val requests = mutableListOf<SetEpisodeCollectionTypeRequest>()
        setPage(RecordingActions(), onEpisodeCollectionUpdate = { requests += it })

        onNodeWithTag(AppleSubjectDetailsTestTags.episode(102)).assertIsSelected()
        onNodeWithTag(AppleSubjectDetailsTestTags.episode(101)).assertIsNotSelected()

        onNodeWithTag(AppleSubjectDetailsTestTags.episode(102)).performTouchInput { longClick() }
        assertEquals(102, requests.single().episodeId)
        assertEquals(UnifiedCollectionType.DONE, requests.single().collectionType)
    }

    @Test
    fun `tab pills switch pages`() = runAniComposeUiTest {
        setPage(RecordingActions())
        onNodeWithTag(AppleSubjectDetailsTestTags.tab(0)).assertIsSelected()
        onNodeWithTag(AppleSubjectDetailsTestTags.tab(2)).performClick()
        waitForIdle()
        onNodeWithTag(AppleSubjectDetailsTestTags.tab(2)).assertIsSelected()
    }
}
