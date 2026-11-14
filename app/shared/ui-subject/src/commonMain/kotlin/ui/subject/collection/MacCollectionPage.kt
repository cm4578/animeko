/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.collectWithLifecycle
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.episode.EpisodeCollectionInfo
import me.him188.ani.app.data.models.player.EpisodeHistory
import me.him188.ani.app.data.models.preference.CollectionLayout
import me.him188.ani.app.data.models.subject.FollowedSubjectInfo
import me.him188.ani.app.data.models.subject.SubjectCollectionCounts
import me.him188.ani.app.data.models.subject.SubjectCollectionInfo
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.models.subject.listCoverUrl
import me.him188.ani.app.data.models.subject.preferredDisplayName
import me.him188.ani.app.data.models.subject.subjectInfo
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AlertDot
import me.him188.ani.app.ui.foundation.apple.AppleButton
import me.him188.ani.app.ui.foundation.apple.AppleButtonStyle
import me.him188.ani.app.ui.foundation.apple.AppleCollectionTypes
import me.him188.ani.app.ui.foundation.apple.AppleComponentDefaults
import me.him188.ani.app.ui.foundation.apple.AppleContextMenu
import me.him188.ani.app.ui.foundation.apple.AppleCover
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleProgressBar
import me.him188.ani.app.ui.foundation.apple.CoverAspect
import me.him188.ani.app.ui.foundation.apple.MacLargeTitleHeader
import me.him188.ani.app.ui.foundation.apple.MacPageToolbar
import me.him188.ani.app.ui.foundation.apple.MacSegmentedButton
import me.him188.ani.app.ui.foundation.apple.MacSegmentedButtons
import me.him188.ani.app.ui.foundation.apple.MacTableColumn
import me.him188.ani.app.ui.foundation.apple.MacTableColumns
import me.him188.ani.app.ui.foundation.apple.MacTableHeader
import me.him188.ani.app.ui.foundation.apple.MacTableHeaderCell
import me.him188.ani.app.ui.foundation.apple.MacTableRow
import me.him188.ani.app.ui.foundation.apple.MacWindowDefaults
import me.him188.ani.app.ui.foundation.apple.PillTab
import me.him188.ani.app.ui.foundation.apple.collectionTypeName
import me.him188.ani.app.ui.foundation.apple.onSecondaryClick
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.showLoadError
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.collection_all_playback_history
import me.him188.ani.app.ui.lang.collection_continue_watching_remaining_minutes
import me.him188.ani.app.ui.lang.collection_episode_progress
import me.him188.ani.app.ui.lang.collection_layout_grid
import me.him188.ani.app.ui.lang.collection_layout_list
import me.him188.ani.app.ui.lang.collection_table_activity
import me.him188.ani.app.ui.lang.collection_table_name
import me.him188.ani.app.ui.lang.collection_table_progress
import me.him188.ani.app.ui.lang.exploration_continue_watching
import me.him188.ani.app.ui.lang.exploration_search
import me.him188.ani.app.ui.lang.login_sign_in
import me.him188.ani.app.ui.lang.playback_history_episode_label
import me.him188.ani.app.ui.lang.settings_app_my_collections
import me.him188.ani.app.ui.lang.subject_collection_guest_mode_tip
import me.him188.ani.app.ui.lang.subject_collection_page_title
import me.him188.ani.app.ui.lang.subject_episode_cached
import me.him188.ani.app.ui.lang.video_player_play
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.search.isLoadingNextPage
import me.him188.ani.app.ui.subject.AiringLabelState
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeDialogsHost
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.renderEpAndSort
import me.him188.ani.app.ui.subject.renderSubjectSeason
import me.him188.ani.app.ui.subject.rememberSubjectStatusStrings
import me.him188.ani.app.ui.user.SelfInfoUiState
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 版追番页 (04 §3.2).
 *
 * 工具栏右侧为排版切换 (网格 / 清单, 05 C1), 不放排序按钮 (05 G26). 内容由上到下: 大标题「追番」;
 * 继续观看 (05 G3、G27): 最前面 3 部, 3 栏卡片, 标题行右侧「全部播放记录」; 我的追番: 标题与状态胶囊,
 * 7 栏海报网格或 C19 表格. 「管理」在 05 G8 实现前不显示.
 *
 * 点项目进入条目详情; 右键打开收藏状态菜单, 可修改收藏状态或把下一集标为看过 (同 iOS 的长按菜单).
 *
 * 参数同 [AppleCollectionPage]. [onClickPlaybackHistory] 为「全部播放记录」.
 */
@Composable
fun MacCollectionPage(
    state: UserCollectionsState,
    selfInfo: SelfInfoUiState,
    continueWatching: Flow<PagingData<FollowedSubjectInfo>>,
    layout: CollectionLayout,
    onLayoutChange: (CollectionLayout) -> Unit,
    episodeHistoryFlow: (episodeId: Int) -> Flow<EpisodeHistory?>,
    nextEpisodeCachedFlow: (subjectId: Int) -> Flow<Boolean>,
    onClickSearch: () -> Unit,
    onClickLogin: () -> Unit,
    onClickPlaybackHistory: () -> Unit,
    onClickSubject: (SubjectInfo) -> Unit,
    onPlay: (subjectId: Int, episodeId: Int) -> Unit,
    onMarkEpisodeWatched: (subjectId: Int, episode: EpisodeCollectionInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val typeIndex = state.selectedTypeIndex.coerceAtLeast(0)
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    // 改为「看过」后项目会移出当前列表, 询问「同时将所有剧集设为看过」的对话框由页面显示.
    var markAllWatchedState by remember { mutableStateOf<EditableSubjectCollectionTypeState?>(null) }
    markAllWatchedState?.let { EditableSubjectCollectionTypeDialogsHost(it) }

    MacCollectionPageLayout(
        items = state.getCollectionLazyPagingItems(typeIndex).collectWithLifecycle(),
        continueWatching = continueWatching.collectAsLazyPagingItems(),
        selectedType = COLLECTION_TABS_SORTED[typeIndex],
        collectionCounts = state.collectionCounts,
        onSelectType = { state.selectTypeIndex(COLLECTION_TABS_SORTED.indexOf(it)) },
        isGuest = selfInfo.isSessionValid == false,
        layout = layout,
        onLayoutChange = onLayoutChange,
        episodeHistoryFlow = episodeHistoryFlow,
        nextEpisodeCachedFlow = nextEpisodeCachedFlow,
        onClickSearch = onClickSearch,
        onClickLogin = onClickLogin,
        onClickPlaybackHistory = onClickPlaybackHistory,
        onClickSubject = onClickSubject,
        onPlay = onPlay,
        onSetCollectionType = { item, type ->
            scope.launch {
                val editState = state.createEditableSubjectCollectionTypeState(item)
                val error = editState.setSelfCollectionType(type)
                if (error != null) {
                    toaster.showLoadError(error)
                } else if (editState.shouldOfferMarkAllWatched) {
                    markAllWatchedState = editState
                }
            }
        },
        onMarkEpisodeWatched = onMarkEpisodeWatched,
        modifier = modifier,
    )
}

/**
 * [MacCollectionPage] 的版面, 不持有状态.
 */
@Composable
internal fun MacCollectionPageLayout(
    items: LazyPagingItems<SubjectCollectionInfo>,
    continueWatching: LazyPagingItems<FollowedSubjectInfo>,
    selectedType: UnifiedCollectionType,
    collectionCounts: SubjectCollectionCounts?,
    onSelectType: (UnifiedCollectionType) -> Unit,
    isGuest: Boolean,
    layout: CollectionLayout,
    onLayoutChange: (CollectionLayout) -> Unit,
    episodeHistoryFlow: (episodeId: Int) -> Flow<EpisodeHistory?>,
    nextEpisodeCachedFlow: (subjectId: Int) -> Flow<Boolean>,
    onClickSearch: () -> Unit,
    onClickLogin: () -> Unit,
    onClickPlaybackHistory: () -> Unit,
    onClickSubject: (SubjectInfo) -> Unit,
    onPlay: (subjectId: Int, episodeId: Int) -> Unit,
    onSetCollectionType: (SubjectCollectionInfo, UnifiedCollectionType) -> Unit,
    onMarkEpisodeWatched: (subjectId: Int, episode: EpisodeCollectionInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val itemActions = CollectionItemActions(onClickSubject, onSetCollectionType, onMarkEpisodeWatched)
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        MacPageToolbar {
            if (!isGuest) {
                MacSegmentedButtons {
                    MacSegmentedButton(
                        selected = layout == CollectionLayout.GRID,
                        onClick = { onLayoutChange(CollectionLayout.GRID) },
                        icon = AppleIcons.gridLayout(),
                        contentDescription = stringResource(Lang.collection_layout_grid),
                    )
                    MacSegmentedButton(
                        selected = layout == CollectionLayout.LIST,
                        onClick = { onLayoutChange(CollectionLayout.LIST) },
                        icon = AppleIcons.listLayout(),
                        contentDescription = stringResource(Lang.collection_layout_list),
                    )
                }
            }
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            rememberLazyListState(),
            contentPadding = PaddingValues(
                start = MacPageDefaults.HorizontalPadding,
                top = 4.dp,
                end = MacPageDefaults.HorizontalPadding,
                bottom = MacPageDefaults.HorizontalPadding,
            ),
        ) {
            item(key = "title") {
                MacLargeTitleHeader(stringResource(Lang.subject_collection_page_title))
            }

            if (isGuest) {
                item(key = "guest") {
                    MacGuestCard(onClickLogin = onClickLogin, onClickSearch = onClickSearch)
                }
                return@LazyColumn
            }

            if (continueWatching.itemCount > 0) {
                item(key = "continue-watching") {
                    MacContinueWatchingSection(
                        continueWatching,
                        episodeHistoryFlow,
                        onClickPlaybackHistory,
                        onClickSubject,
                        onPlay,
                    )
                }
            }

            item(key = "my-collections") {
                MacMyCollectionsHeader(selectedType, collectionCounts, onSelectType)
            }

            if (items.loadState.hasError) {
                item(key = "error") {
                    LoadErrorCard(
                        LoadError.fromCombinedLoadStates(items.loadState),
                        onRetry = { items.refresh() },
                        Modifier.padding(bottom = 16.dp),
                    )
                }
            }

            when (layout) {
                CollectionLayout.GRID -> macCollectionGridItems(items, itemActions)
                CollectionLayout.LIST -> macCollectionTableItems(items, nextEpisodeCachedFlow, itemActions, onPlay)
            }

            if (items.isLoadingNextPage) {
                item(key = "loading") {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

internal object MacPageDefaults {
    /** 内容区左右内距 (04 §1.1). */
    val HorizontalPadding = 32.dp
}

@Composable
private fun MacGuestCard(
    onClickLogin: () -> Unit,
    onClickSearch: () -> Unit,
) {
    Column(
        Modifier
            .padding(top = 24.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(
            stringResource(Lang.subject_collection_guest_mode_tip),
            style = AniTextStyles.macBody,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            AppleButton(onClickLogin) { Text(stringResource(Lang.login_sign_in)) }
            AppleButton(onClickSearch, style = AppleButtonStyle.Secondary) {
                Text(stringResource(Lang.exploration_search))
            }
        }
    }
}

/**
 * 区块标题行: 区块标题 17/22 W700, 右侧可放 accent 链接.
 */
@Composable
private fun MacSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            Modifier.weight(1f),
            style = AniTextStyles.macTitle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        trailing()
    }
}

/**
 * 继续观看: 标题行 + 3 栏等宽卡片 (间距 16), 只放最前面 3 部.
 */
@Composable
private fun MacContinueWatchingSection(
    heroes: LazyPagingItems<FollowedSubjectInfo>,
    episodeHistoryFlow: (episodeId: Int) -> Flow<EpisodeHistory?>,
    onClickPlaybackHistory: () -> Unit,
    onClickSubject: (SubjectInfo) -> Unit,
    onPlay: (subjectId: Int, episodeId: Int) -> Unit,
) {
    Column(Modifier.padding(top = 24.dp)) {
        MacSectionHeader(stringResource(Lang.exploration_continue_watching)) {
            Text(
                stringResource(Lang.collection_all_playback_history),
                Modifier.clip(RoundedCornerShape(4.dp)).clickable(role = Role.Button, onClick = onClickPlaybackHistory),
                style = AniTextStyles.macCallout,
                color = MaterialTheme.colorScheme.primary,
            )
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            for (index in 0 until ContinueWatchingColumns) {
                Box(Modifier.weight(1f)) {
                    val hero = if (index < heroes.itemCount) heroes[index] else null
                    if (hero != null) {
                        MacContinueWatchingCard(hero, episodeHistoryFlow, onClickSubject, onPlay)
                    }
                }
            }
        }
    }
}

private const val ContinueWatchingColumns = 3

/**
 * 继续观看卡片: card 底、圆角 14; 上方 16:9 剧照, 底边 3 高进度 (轨道 30% 黑、进度白色);
 * 下方番名 14/600、「第 N 集 · 剩 M 分钟」12 label2, 右侧 32 圆形播放钮 (accentFill).
 */
@Composable
private fun MacContinueWatchingCard(
    hero: FollowedSubjectInfo,
    episodeHistoryFlow: (episodeId: Int) -> Flow<EpisodeHistory?>,
    onClickSubject: (SubjectInfo) -> Unit,
    onPlay: (subjectId: Int, episodeId: Int) -> Unit,
) {
    val subjectId = hero.subjectInfo.subjectId
    val episodeId = hero.subjectProgressInfo.nextEpisodeIdToPlay
    val episode = hero.subjectCollectionInfo.episodes.firstOrNull { it.episodeId == episodeId }?.episodeInfo
    val history by remember(episodeId) {
        if (episodeId == null) flowOf(null) else episodeHistoryFlow(episodeId)
    }.collectAsStateWithLifecycle(null)
    val progress = continueWatchingProgress(history)
    val title = hero.subjectInfo.preferredDisplayName(LocalSubjectAppearanceSettings.current.useOriginalTitle)

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { onClickSubject(hero.subjectInfo) },
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            AsyncImage(
                hero.subjectInfo.imageLarge,
                contentDescription = null,
                Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(Color.Black.copy(alpha = 0.30f)),
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress?.fraction ?: 0f)
                        .background(Color.White),
                )
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(start = 14.dp, top = 10.dp, end = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = AniTextStyles.macBody,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val episodeText = episode?.let {
                    stringResource(Lang.playback_history_episode_label, renderEpAndSort(it.ep, it.sort))
                }
                val remaining = progress?.remainingMinutes?.let {
                    stringResource(Lang.collection_continue_watching_remaining_minutes, it)
                }
                Text(
                    listOfNotNull(episodeText, remaining).joinToString(" · "),
                    style = AniTextStyles.macCaption,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (episodeId != null) {
                MacPlayButton(
                    onClick = { onPlay(subjectId, episodeId) },
                    contentDescription = stringResource(Lang.video_player_play),
                    primary = true,
                )
            }
        }
    }
}

/**
 * 32 圆形播放钮. [primary] 为 accentFill 底 + onAccentFill 图标 (继续观看), 否则为 fill 底 + label 图标 (表格).
 */
@Composable
private fun MacPlayButton(
    onClick: () -> Unit,
    contentDescription: String,
    primary: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Box(
        modifier
            .size(32.dp)
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
            .clip(CircleShape)
            .background(if (primary) AniColors.accentFill else MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            AppleIcons.playFilled(),
            contentDescription = null,
            Modifier.size(12.dp),
            tint = if (primary) AniColors.onAccentFill else MaterialTheme.colorScheme.onSurface,
        )
    }
}

/**
 * 「我的追番」标题与同一行的状态胶囊 (在看、想看、看过、搁置、抛弃, 带数量), 间距 16.
 */
@Composable
private fun MacMyCollectionsHeader(
    selectedType: UnifiedCollectionType,
    collectionCounts: SubjectCollectionCounts?,
    onSelectType: (UnifiedCollectionType) -> Unit,
) {
    Row(
        Modifier.padding(top = 28.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            stringResource(Lang.settings_app_my_collections),
            style = AniTextStyles.macTitle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (type in AppleCollectionTypes) {
                PillTab(
                    stringResource(type.collectionTypeName()),
                    selected = type == selectedType,
                    onClick = { onSelectType(type) },
                    count = collectionCounts?.getCount(type)?.toString(),
                )
            }
        }
    }
}

/**
 * 右键 [content] 时, 在右键的位置打开收藏状态菜单 ([CollectionItemMenuItems]).
 */
@Composable
private fun MacCollectionItemWithMenu(
    item: SubjectCollectionInfo,
    actions: CollectionItemActions,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var menuPosition by remember(item.subjectId) { mutableStateOf<Offset?>(null) }
    Box(modifier.onSecondaryClick { menuPosition = it }) {
        content()
        val position = menuPosition
        if (position != null) {
            AppleContextMenu(
                expanded = true,
                position = position,
                onDismissRequest = { menuPosition = null },
                modifier = Modifier.testTag(CollectionItemMenuTestTags.Menu),
            ) {
                CollectionItemMenuItems(
                    currentType = item.collectionType,
                    nextEpisode = remember(item) { item.nextEpisodeToMarkWatched() },
                    onDismissRequest = { menuPosition = null },
                    onSetType = { actions.onSetCollectionType(item, it) },
                    onMarkEpisodeWatched = { actions.onMarkEpisodeWatched(item.subjectId, it) },
                )
            }
        }
    }
}

/**
 * 网格模式: 7 栏, 列间距 16、行间距 20. 封面 2:3 圆角 10, 有新集时右上提示点, 底部 3 高进度.
 */
private fun LazyListScope.macCollectionGridItems(
    items: LazyPagingItems<SubjectCollectionInfo>,
    actions: CollectionItemActions,
) {
    val rows = (items.itemCount + MacGridColumns - 1) / MacGridColumns
    items(rows, key = { "mac-grid-row-$it" }) { row ->
        Row(
            Modifier
                .fillMaxWidth()
                .padding(bottom = if (row == rows - 1) 0.dp else 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            for (column in 0 until MacGridColumns) {
                val index = row * MacGridColumns + column
                Box(Modifier.weight(1f)) {
                    val item = if (index < items.itemCount) items[index] else null
                    if (item != null) {
                        MacCollectionItemWithMenu(item, actions) {
                            NsfwCollectionItem(item) {
                                MacCollectionGridCell(item, onClick = { actions.onClickSubject(item.subjectInfo) })
                            }
                        }
                    }
                }
            }
        }
    }
}

private const val MacGridColumns = 7

/**
 * 网格项目: 封面下方番名 13/600; 次行 12: 有新集时为 label W600 的进度文字, 否则 label2「已看 / 总集数」.
 */
@Composable
private fun MacCollectionGridCell(
    item: SubjectCollectionInfo,
    onClick: () -> Unit,
) {
    val title = item.subjectInfo.preferredDisplayName(LocalSubjectAppearanceSettings.current.useOriginalTitle)
    val progress = remember(item) { item.episodeProgress() }
    val labelState = remember(item) { AiringLabelState(item.airingInfo, item.progressInfo) }
    val fresh = labelState.highlightProgress
    val colorScheme = MaterialTheme.colorScheme
    Column(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AppleCover(
            CoverAspect.Portrait2x3,
            title,
            Modifier.fillMaxWidth(),
            cornerRadius = 10.dp,
            alert = fresh,
            progress = progress?.let { { it.fraction } },
        ) {
            AsyncImage(item.subjectInfo.listCoverUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                style = AniTextStyles.macCallout,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val progressText = labelState.progressText(rememberSubjectStatusStrings())
            val secondLine = when {
                fresh -> progressText
                progress != null -> stringResource(Lang.collection_episode_progress, progress.watched, progress.total)
                else -> progressText
            }
            if (secondLine != null) {
                Text(
                    secondLine,
                    style = AniTextStyles.macCaption,
                    fontWeight = if (fresh) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (fresh) colorScheme.onSurface else colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * 清单模式的 C19 表格列: 名称 (弹性) / 进度 220 / 动态 160 / 操作 32, 列间距 16, 左右内距 16.
 */
internal val MacCollectionTableColumns = MacTableColumns(
    listOf(
        MacTableColumn(weight = 1f),
        MacTableColumn(width = 220.dp),
        MacTableColumn(width = 160.dp),
        MacTableColumn(width = 32.dp, alignment = Alignment.CenterHorizontally),
    ),
    horizontalPadding = 16.dp,
    spacing = 16.dp,
)

/**
 * 清单模式: card 底圆角 14 的 C19 表格, 表头高 32、行高 76; 分隔线从 74 (封面右侧) 开始.
 */
private fun LazyListScope.macCollectionTableItems(
    items: LazyPagingItems<SubjectCollectionInfo>,
    nextEpisodeCachedFlow: (subjectId: Int) -> Flow<Boolean>,
    actions: CollectionItemActions,
    onPlay: (subjectId: Int, episodeId: Int) -> Unit,
) {
    val count = items.itemCount
    item(key = "mac-table-header") {
        Box(
            Modifier
                .fillMaxWidth()
                .clip(tableCardShape(top = true, bottom = count == 0))
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            MacTableHeader(MacCollectionTableColumns) {
                MacTableHeaderCell(stringResource(Lang.collection_table_name))
                MacTableHeaderCell(stringResource(Lang.collection_table_progress))
                MacTableHeaderCell(stringResource(Lang.collection_table_activity))
                Box {}
            }
        }
    }
    items(count, key = items.itemKey { "mac-table-" + it.subjectId }) { index ->
        val item = items[index] ?: return@items
        Box(
            Modifier
                .fillMaxWidth()
                .clip(tableCardShape(top = false, bottom = index == count - 1))
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            MacCollectionItemWithMenu(item, actions) {
                NsfwCollectionItem(item) {
                    MacCollectionTableRow(
                        item,
                        nextEpisodeCachedFlow,
                        showDivider = index > 0,
                        onClick = { actions.onClickSubject(item.subjectInfo) },
                        onPlay = onPlay,
                    )
                }
            }
        }
    }
}

private fun tableCardShape(top: Boolean, bottom: Boolean): Shape {
    val radius: Dp = 14.dp
    return RoundedCornerShape(
        topStart = if (top) radius else 0.dp,
        topEnd = if (top) radius else 0.dp,
        bottomStart = if (bottom) radius else 0.dp,
        bottomEnd = if (bottom) radius else 0.dp,
    )
}

/**
 * 表格行: 名称 (42×63 封面 + 番名 14/600 + 季度 12 label2)、进度 (4 高进度条 + 右对齐「已看 / 总数 集」)、
 * 动态 (新集为提示点 + label W600; 已快取; 其他为 label2 进度文字)、操作 (32 圆形播放钮, fill 底).
 */
@Composable
private fun MacCollectionTableRow(
    item: SubjectCollectionInfo,
    nextEpisodeCachedFlow: (subjectId: Int) -> Flow<Boolean>,
    showDivider: Boolean,
    onClick: () -> Unit,
    onPlay: (subjectId: Int, episodeId: Int) -> Unit,
) {
    val title = item.subjectInfo.preferredDisplayName(LocalSubjectAppearanceSettings.current.useOriginalTitle)
    val progress = remember(item) { item.episodeProgress() }
    val colorScheme = MaterialTheme.colorScheme
    MacTableRow(
        MacCollectionTableColumns,
        height = 76.dp,
        showDivider = showDivider,
        dividerStartIndent = 74.dp,
        onClick = onClick,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AppleCover(CoverAspect.Portrait2x3, title, Modifier.width(42.dp), cornerRadius = 6.dp) {
                AsyncImage(item.subjectInfo.listCoverUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = AniTextStyles.macBody,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (item.subjectInfo.airDate.isValid) {
                    Text(
                        renderSubjectSeason(item.subjectInfo.airDate),
                        style = AniTextStyles.macCaption,
                        color = colorScheme.secondary,
                        maxLines = 1,
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth()) {
            if (progress != null) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    AppleProgressBar({ progress.fraction }, Modifier.weight(1f))
                    EpisodeProgressText(progress, Modifier.widthIn(min = 56.dp))
                }
            }
        }
        MacCollectionActivity(item, nextEpisodeCachedFlow)
        val episodeId = item.progressInfo.nextEpisodeIdToPlay
        MacPlayButton(
            onClick = { if (episodeId != null) onPlay(item.subjectId, episodeId) },
            contentDescription = stringResource(Lang.video_player_play),
            primary = false,
            enabled = episodeId != null,
        )
    }
}

/**
 * 动态列: 有新集 > 已快取 > 其他提示, 一行只显示一种 (同 iOS 追番列表的状态行).
 */
@Composable
private fun MacCollectionActivity(
    item: SubjectCollectionInfo,
    nextEpisodeCachedFlow: (subjectId: Int) -> Flow<Boolean>,
) {
    val labelState = remember(item) { AiringLabelState(item.airingInfo, item.progressInfo) }
    val fresh = labelState.highlightProgress
    val cached by remember(item.subjectId) { nextEpisodeCachedFlow(item.subjectId) }
        .collectAsStateWithLifecycle(false)
    val colorScheme = MaterialTheme.colorScheme
    val progressText = labelState.progressText(rememberSubjectStatusStrings())
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        when {
            fresh -> {
                AlertDot()
                if (progressText != null) {
                    ActivityText(progressText, colorScheme.onSurface, FontWeight.SemiBold)
                }
            }

            cached -> {
                Icon(AppleIcons.download(2.4f), contentDescription = null, Modifier.size(13.dp), tint = colorScheme.secondary)
                ActivityText(stringResource(Lang.subject_episode_cached), colorScheme.secondary, FontWeight.Normal)
            }

            progressText != null -> ActivityText(progressText, colorScheme.secondary, FontWeight.Normal)
        }
    }
}

@Composable
private fun ActivityText(text: String, color: Color, fontWeight: FontWeight) {
    Text(
        text,
        style = AniTextStyles.macCallout,
        fontWeight = fontWeight,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
