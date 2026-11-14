/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.details.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItemsWithLifecycle
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.models.subject.Tag
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.EpisodeTile
import me.him188.ani.app.ui.foundation.apple.EpisodeTileState
import me.him188.ani.app.ui.foundation.apple.episodeTileLabel
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.subject_details_all_episodes
import me.him188.ani.app.ui.lang.subject_details_episodes
import me.him188.ani.app.ui.lang.subject_details_info
import me.him188.ani.app.ui.lang.subject_details_show_less
import me.him188.ani.app.ui.lang.subject_details_show_more
import me.him188.ani.app.ui.lang.subject_details_summary
import me.him188.ani.app.ui.subject.details.sections.CharactersSection
import me.him188.ani.app.ui.subject.details.sections.StaffSection
import me.him188.ani.app.ui.subject.details.sections.SubjectInfoTable
import me.him188.ani.app.ui.subject.details.sections.SubjectTagsSection
import me.him188.ani.app.ui.subject.details.state.SubjectDetailsState
import me.him188.ani.app.ui.subject.episode.list.EpisodeListItem
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版条目详情的「详情」分页 (03 §3.6).
 *
 * 选集为 C13 横排, 延伸到屏幕边缘; 简介 `15/23` labelSoft. 之后的标签、作品信息、角色、制作人员、关联作品沿用现有区块.
 */
@Composable
internal fun AppleDetailsTabContent(
    state: SubjectDetailsState,
    info: SubjectInfo,
    onPlay: (episodeId: Int) -> Unit,
    onEpisodeLongClick: (EpisodeListItem) -> Unit,
    onClickTag: (Tag) -> Unit,
    onShowEpisodeList: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    val uiState by state.uiState.collectAsStateWithLifecycle()
    val episodes = uiState.episodeListUiState.mainEpisodes
    val currentEpisodeId = remember(episodes) { episodes.firstOrNull { !it.isDoneOrDropped }?.episodeId }

    val exposedCharacters = state.exposedCharactersPager.collectAsLazyPagingItemsWithLifecycle()
    val allCharacters = state.charactersPager.collectAsLazyPagingItemsWithLifecycle()
    val exposedStaff = state.exposedStaffPager.collectAsLazyPagingItemsWithLifecycle()
    val allStaff = state.staffPager.collectAsLazyPagingItemsWithLifecycle()
    val related = state.relatedSubjectsPager.collectAsLazyPagingItemsWithLifecycle()

    val horizontalPadding = PaddingValues(horizontal = 16.dp)
    val horizontalPaddingModifier = Modifier.padding(horizontalPadding)

    LazyColumn(
        modifier,
        state = listState,
        contentPadding = PaddingValues(top = 20.dp).plusBottom(contentPadding),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        // 剧集异步加载; item 恒定存在, 加载完成后才不会出现在锚点上方而滚不回去.
        item("episodes") {
            if (episodes.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    AppleSectionTitle(stringResource(Lang.subject_details_episodes)) {
                        AppleTextButton(onShowEpisodeList, Modifier.testTag(AppleSubjectDetailsTestTags.AllEpisodes)) {
                            Text(stringResource(Lang.subject_details_all_episodes, episodes.size))
                        }
                    }
                    val rowState = rememberLazyListState(
                        initialFirstVisibleItemIndex = episodes.indexOfFirst { it.episodeId == currentEpisodeId }
                            .minus(2).coerceAtLeast(0),
                    )
                    LazyRow(
                        state = rowState,
                        contentPadding = horizontalPadding,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(episodes, key = { it.episodeId }) { episode ->
                            EpisodeTile(
                                episodeTileLabel(episode.sort),
                                state = when {
                                    episode.isDoneOrDropped -> EpisodeTileState.Watched
                                    episode.episodeId == currentEpisodeId -> EpisodeTileState.Playing
                                    else -> EpisodeTileState.Unwatched
                                },
                                onClick = { onPlay(episode.episodeId) },
                                Modifier.testTag(AppleSubjectDetailsTestTags.episode(episode.episodeId)),
                                onLongClick = { onEpisodeLongClick(episode) },
                            )
                        }
                    }
                }
            }
        }

        if (info.summary.isNotBlank()) {
            item("summary") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppleSectionTitle(stringResource(Lang.subject_details_summary))
                    AppleSummary(info.summary, horizontalPaddingModifier)
                }
            }
        }

        item("tags") {
            SubjectTagsSection(info.tags, onClickTag, horizontalPaddingModifier)
        }

        item("info") {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                AppleSectionTitle(stringResource(Lang.subject_details_info))
                Column(horizontalPaddingModifier.fillMaxWidth()) {
                    SubjectInfoTable(info, mainEpisodeCount = episodes.size.takeIf { it > 0 })
                }
            }
        }

        item("characters") {
            CharactersSection(
                exposedCharacters,
                allCharacters,
                uiState.totalCharactersCount,
                contentPadding = horizontalPadding,
                avatarSize = 56.dp,
                itemSpacing = 0.dp,
            )
        }

        item("staff") {
            StaffSection(
                exposedStaff,
                allStaff,
                uiState.totalStaffCount,
                horizontalPaddingModifier,
                gridColumns = 3,
            )
        }

        if (related.itemCount > 0) {
            item("related") {
                RelatedSubjectsCompactSection(
                    state.subjectId,
                    related,
                    headerModifier = horizontalPaddingModifier,
                    contentPadding = horizontalPadding,
                )
            }
        }
    }
}

private fun PaddingValues.plusBottom(other: PaddingValues): PaddingValues =
    PaddingValues(top = calculateTopPadding(), bottom = other.calculateBottomPadding())

/**
 * 区块标题 `title`, 右侧可放 accent 文字链接. 标题行左右内缩 20.
 */
@Composable
internal fun AppleSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
) {
    Row(
        modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            Modifier.weight(1f),
            style = AniTextStyles.title,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        action()
    }
}

/**
 * 简介: `15/23` labelSoft, 默认 5 行, 溢出时下方显示「显示更多 / 显示更少」.
 */
@Composable
private fun AppleSummary(summary: String, modifier: Modifier = Modifier) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var hasOverflow by remember { mutableStateOf(false) }
    Column(modifier.fillMaxWidth().padding(horizontal = 4.dp)) {
        Text(
            summary,
            style = AniTextStyles.callout.copy(lineHeight = 23.sp),
            color = AniColors.labelSoft,
            maxLines = if (expanded) Int.MAX_VALUE else 5,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!expanded) hasOverflow = it.hasVisualOverflow },
        )
        if (hasOverflow || expanded) {
            AppleTextButton({ expanded = !expanded }, Modifier.align(Alignment.End)) {
                Text(stringResource(if (expanded) Lang.subject_details_show_less else Lang.subject_details_show_more))
            }
        }
    }
}

internal object AppleSubjectDetailsTestTags {
    const val Share = "subject-details-share"
    const val ShareMenu = "subject-details-share-menu"
    const val CollectionButton = "subject-details-collection"
    const val CollectionMenu = "subject-details-collection-menu"
    const val Unfollow = "subject-details-unfollow"
    const val ConfirmUnfollow = "subject-details-confirm-unfollow"
    const val Play = "subject-details-play"
    const val Download = "subject-details-download"
    const val Rating = "subject-details-rating"
    const val AllEpisodes = "subject-details-all-episodes"
    const val CollapsedBar = "subject-details-collapsed-bar"
    fun tab(index: Int) = "subject-details-tab-$index"
    fun episode(episodeId: Int) = "subject-details-episode-$episodeId"
}
