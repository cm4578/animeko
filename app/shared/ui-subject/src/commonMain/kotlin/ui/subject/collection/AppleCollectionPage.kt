/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.collection

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.collectWithLifecycle
import androidx.paging.compose.itemKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import me.him188.ani.app.data.models.player.EpisodeHistory
import me.him188.ani.app.data.models.preference.CollectionLayout
import me.him188.ani.app.data.models.preference.NsfwMode
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
import me.him188.ani.app.ui.foundation.apple.AppleCircleButton
import me.him188.ani.app.ui.foundation.apple.AppleCover
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleProgressBar
import me.him188.ani.app.ui.foundation.apple.CoverAspect
import me.him188.ani.app.ui.foundation.apple.LargeTitleHeader
import me.him188.ani.app.ui.foundation.apple.PillTab
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.NsfwMask
import me.him188.ani.app.ui.foundation.widgets.PullToRefreshBox
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.collection_continue_watching_remaining_minutes
import me.him188.ani.app.ui.lang.collection_episode_progress
import me.him188.ani.app.ui.lang.collection_layout_grid
import me.him188.ani.app.ui.lang.collection_layout_list
import me.him188.ani.app.ui.lang.exploration_continue_watching
import me.him188.ani.app.ui.lang.exploration_search
import me.him188.ani.app.ui.lang.login_sign_in
import me.him188.ani.app.ui.lang.playback_history_episode_label
import me.him188.ani.app.ui.lang.settings_app_my_collections
import me.him188.ani.app.ui.lang.subject_collection_doing
import me.him188.ani.app.ui.lang.subject_collection_done
import me.him188.ani.app.ui.lang.subject_collection_dropped
import me.him188.ani.app.ui.lang.subject_collection_guest_mode_tip
import me.him188.ani.app.ui.lang.subject_collection_on_hold
import me.him188.ani.app.ui.lang.subject_collection_page_title
import me.him188.ani.app.ui.lang.subject_collection_wish
import me.him188.ani.app.ui.lang.subject_episode_cached
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.search.isLoadingFirstPageOrRefreshing
import me.him188.ani.app.ui.search.isLoadingNextPage
import me.him188.ani.app.ui.subject.AiringLabelState
import me.him188.ani.app.ui.subject.rememberSubjectStatusStrings
import me.him188.ani.app.ui.subject.renderEpAndSort
import me.him188.ani.app.ui.user.SelfInfoUiState
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.api.topic.isDoneOrDropped
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.math.ceil

/**
 * Apple 版追番页 (03 §3.1). 手机.
 *
 * 从上到下: C2 大标题「追番」与搜索钮; 继续观看 (05 G3、G27); 我的追番: 状态胶囊与列表/网格切换 (05 C1), 列表或 3 栏网格.
 * 页头不放头像 (05 G14), 个人入口在「我的」分页. 「管理」在 05 G8 实现前不显示.
 *
 * @param continueWatching 继续观看的条目, 见 [UserCollectionsViewModel.continueWatchingPager].
 * @param episodeHistoryFlow 一集的播放记录, 见 [UserCollectionsViewModel.episodePlayHistoryFlow].
 * @param nextEpisodeCachedFlow 条目的下一集是否已快取, 见 [UserCollectionsViewModel.nextEpisodeCachedFlow].
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleCollectionPage(
    state: UserCollectionsState,
    selfInfo: SelfInfoUiState,
    continueWatching: Flow<PagingData<FollowedSubjectInfo>>,
    layout: CollectionLayout,
    onLayoutChange: (CollectionLayout) -> Unit,
    episodeHistoryFlow: (episodeId: Int) -> Flow<EpisodeHistory?>,
    nextEpisodeCachedFlow: (subjectId: Int) -> Flow<Boolean>,
    onClickSearch: () -> Unit,
    onClickLogin: () -> Unit,
    onClickSubject: (SubjectInfo) -> Unit,
    onPlay: (subjectId: Int, episodeId: Int) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val typeIndex = state.selectedTypeIndex.coerceAtLeast(0)
    AppleCollectionPageLayout(
        items = state.getCollectionLazyPagingItems(typeIndex).collectWithLifecycle(),
        continueWatching = continueWatching.collectAsLazyPagingItems(),
        selectedType = COLLECTION_TABS_SORTED[typeIndex],
        collectionCounts = state.collectionCounts,
        onSelectType = { state.selectTypeIndex(COLLECTION_TABS_SORTED.indexOf(it)) },
        onRefresh = { state.refreshSelectedPage() },
        isGuest = selfInfo.isSessionValid == false,
        layout = layout,
        onLayoutChange = onLayoutChange,
        episodeHistoryFlow = episodeHistoryFlow,
        nextEpisodeCachedFlow = nextEpisodeCachedFlow,
        onClickSearch = onClickSearch,
        onClickLogin = onClickLogin,
        onClickSubject = onClickSubject,
        onPlay = onPlay,
        modifier = modifier,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
    )
}

/**
 * [AppleCollectionPage] 的版面, 不持有状态.
 */
@Composable
internal fun AppleCollectionPageLayout(
    items: LazyPagingItems<SubjectCollectionInfo>,
    continueWatching: LazyPagingItems<FollowedSubjectInfo>,
    selectedType: UnifiedCollectionType,
    collectionCounts: SubjectCollectionCounts?,
    onSelectType: (UnifiedCollectionType) -> Unit,
    onRefresh: () -> Unit,
    isGuest: Boolean,
    layout: CollectionLayout,
    onLayoutChange: (CollectionLayout) -> Unit,
    episodeHistoryFlow: (episodeId: Int) -> Flow<EpisodeHistory?>,
    nextEpisodeCachedFlow: (subjectId: Int) -> Flow<Boolean>,
    onClickSearch: () -> Unit,
    onClickLogin: () -> Unit,
    onClickSubject: (SubjectInfo) -> Unit,
    onPlay: (subjectId: Int, episodeId: Int) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        items.isLoadingFirstPageOrRefreshing,
        onRefresh = onRefresh,
        modifier.fillMaxSize(),
        state = pullToRefreshState,
        touchOnly = true,
        indicator = {
            PullToRefreshDefaults.Indicator(
                modifier = Modifier.align(Alignment.TopCenter).windowInsetsPadding(windowInsets),
                isRefreshing = items.isLoadingFirstPageOrRefreshing,
                state = pullToRefreshState,
            )
        },
    ) {
        LazyColumn(Modifier.fillMaxSize(), listState, contentPadding = contentPadding) {
            item(key = "header") {
                LargeTitleHeader(stringResource(Lang.subject_collection_page_title), windowInsets = windowInsets) {
                    AppleCircleButton(onClickSearch, AppleIcons.search(2.2f), stringResource(Lang.exploration_search))
                }
            }

            if (isGuest) {
                item(key = "guest") {
                    GuestCard(onClickLogin = onClickLogin, onClickSearch = onClickSearch)
                }
                return@LazyColumn
            }

            if (continueWatching.itemCount > 0) {
                item(key = "continue-watching") {
                    ContinueWatchingSection(continueWatching, episodeHistoryFlow, onClickSubject, onPlay)
                }
            }

            item(key = "my-collections") {
                MyCollectionsHeader(
                    selectedType = selectedType,
                    collectionCounts = collectionCounts,
                    onSelectType = onSelectType,
                    layout = layout,
                    onLayoutChange = onLayoutChange,
                )
            }

            if (items.loadState.hasError) {
                item(key = "error") {
                    LoadErrorCard(
                        LoadError.fromCombinedLoadStates(items.loadState),
                        onRetry = { items.refresh() },
                        Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }
            }

            when (layout) {
                CollectionLayout.LIST -> collectionListItems(items, nextEpisodeCachedFlow, onClickSubject)
                CollectionLayout.GRID -> collectionGridItems(items, onClickSubject)
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

@Composable
private fun GuestCard(
    onClickLogin: () -> Unit,
    onClickSearch: () -> Unit,
) {
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            stringResource(Lang.subject_collection_guest_mode_tip),
            style = AniTextStyles.callout,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AppleButton(onClickLogin, Modifier.weight(1f)) { Text(stringResource(Lang.login_sign_in)) }
            AppleButton(onClickSearch, Modifier.weight(1f), style = AppleButtonStyle.Tinted) {
                Text(stringResource(Lang.exploration_search))
            }
        }
    }
}

/**
 * 继续观看: 区块标题 + 横向卡片 (宽 300、间距 12、圆角 22).
 */
@Composable
private fun ContinueWatchingSection(
    heroes: LazyPagingItems<FollowedSubjectInfo>,
    episodeHistoryFlow: (episodeId: Int) -> Flow<EpisodeHistory?>,
    onClickSubject: (SubjectInfo) -> Unit,
    onPlay: (subjectId: Int, episodeId: Int) -> Unit,
) {
    Column(Modifier.padding(bottom = 24.dp)) {
        SectionTitle(stringResource(Lang.exploration_continue_watching))
        Spacer(Modifier.height(12.dp))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(heroes.itemCount, key = heroes.itemKey { "hero-" + it.subjectInfo.subjectId }) { index ->
                val hero = heroes[index] ?: return@items
                ContinueWatchingCard(hero, episodeHistoryFlow, onClickSubject, onPlay)
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(
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
            .width(300.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { onClickSubject(hero.subjectInfo) },
    ) {
        AsyncImage(
            hero.subjectInfo.imageLarge,
            contentDescription = null,
            Modifier.fillMaxWidth().height(160.dp).background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentScale = ContentScale.Crop,
        )
        Row(
            Modifier.fillMaxWidth().height(70.dp).padding(start = 16.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    title,
                    style = AniTextStyles.headline,
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
                    style = AniTextStyles.footnote,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (episodeId != null) {
                PlayRingButton(
                    progress = progress?.fraction ?: 0f,
                    onClick = { onPlay(subjectId, episodeId) },
                    contentDescription = title,
                )
            }
        }
    }
}

/**
 * 「继续观看」卡片上的进度 (05 G27). duration 为 0 或没有记录时为 `null`.
 */
internal class ContinueWatchingProgress(val fraction: Float, val remainingMinutes: Int)

internal fun continueWatchingProgress(history: EpisodeHistory?): ContinueWatchingProgress? {
    val duration = history?.durationMillis ?: return null
    if (duration <= 0) return null
    val position = history.positionMillis.coerceIn(0, duration)
    return ContinueWatchingProgress(
        fraction = position.toFloat() / duration,
        remainingMinutes = ceil((duration - position) / 60_000.0).toInt(),
    )
}

/**
 * 54 进度圆环 (accent 弧、fill 轨道), 中心 40 播放钮 (accentFill).
 */
@Composable
private fun PlayRingButton(
    progress: Float,
    onClick: () -> Unit,
    contentDescription: String,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(Modifier.size(54.dp), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 3.dp.toPx()
            val inset = stroke / 2
            val arcSize = Size(size.width - stroke, size.height - stroke)
            drawArc(colorScheme.surfaceContainerHighest, 0f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            if (progress > 0f) {
                drawArc(colorScheme.primary, -90f, 360f * progress, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
            }
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(colorScheme.primary)
                .clickable(role = Role.Button, onClick = onClick)
                .semantics { this.contentDescription = contentDescription },
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppleIcons.playFilled(), contentDescription = null, Modifier.size(16.dp), tint = colorScheme.onPrimary)
        }
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier.padding(horizontal = 20.dp),
        style = AniTextStyles.title,
        color = MaterialTheme.colorScheme.onSurface,
    )
}

/**
 * 「我的追番」标题, 状态胶囊与排版切换.
 *
 * 胶囊依序为在看、想看、看过、搁置、抛弃; 放不下时横向滚动, 排版切换固定在右侧.
 */
@Composable
private fun MyCollectionsHeader(
    selectedType: UnifiedCollectionType,
    collectionCounts: SubjectCollectionCounts?,
    onSelectType: (UnifiedCollectionType) -> Unit,
    layout: CollectionLayout,
    onLayoutChange: (CollectionLayout) -> Unit,
) {
    Column(Modifier.padding(bottom = 16.dp)) {
        SectionTitle(stringResource(Lang.settings_app_my_collections))
        Spacer(Modifier.height(12.dp))
        Row(
            Modifier.fillMaxWidth().padding(end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (type in AppleCollectionTypes) {
                    PillTab(
                        stringResource(type.label()),
                        selected = type == selectedType,
                        onClick = { onSelectType(type) },
                        count = collectionCounts?.getCount(type)?.toString(),
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                LayoutToggleButton(
                    AppleIcons.listLayout(),
                    stringResource(Lang.collection_layout_list),
                    selected = layout == CollectionLayout.LIST,
                    onClick = { onLayoutChange(CollectionLayout.LIST) },
                )
                LayoutToggleButton(
                    AppleIcons.gridLayout(),
                    stringResource(Lang.collection_layout_grid),
                    selected = layout == CollectionLayout.GRID,
                    onClick = { onLayoutChange(CollectionLayout.GRID) },
                )
            }
        }
    }
}

/**
 * 排版切换钮: 32 圆钮, 选中 tint 底 + accent 图标, 未选中透明 + label2 图标.
 */
@Composable
private fun LayoutToggleButton(
    icon: ImageVector,
    contentDescription: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(if (selected) colorScheme.secondaryContainer else Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
                this.selected = selected
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            Modifier.size(18.dp),
            tint = if (selected) colorScheme.primary else colorScheme.secondary,
        )
    }
}

/**
 * 列表模式: C5 卡片, 每列 padding 12 16; 封面 56×84 圆角 8; 分隔线从 84 起.
 */
private fun LazyListScope.collectionListItems(
    items: LazyPagingItems<SubjectCollectionInfo>,
    nextEpisodeCachedFlow: (subjectId: Int) -> Flow<Boolean>,
    onClickSubject: (SubjectInfo) -> Unit,
) {
    val count = items.itemCount
    items(count, key = items.itemKey { "list-" + it.subjectId }) { index ->
        val item = items[index] ?: return@items
        val shape = RoundedCornerShape(
            topStart = if (index == 0) 16.dp else 0.dp,
            topEnd = if (index == 0) 16.dp else 0.dp,
            bottomStart = if (index == count - 1) 16.dp else 0.dp,
            bottomEnd = if (index == count - 1) 16.dp else 0.dp,
        )
        Box(
            Modifier
                .padding(horizontal = 16.dp)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            NsfwCollectionItem(item) {
                CollectionListRow(item, nextEpisodeCachedFlow, onClick = { onClickSubject(item.subjectInfo) })
            }
            if (index > 0) {
                AppleDivider(Modifier.align(Alignment.TopStart), startIndent = 84.dp)
            }
        }
    }
}

@Composable
private fun NsfwCollectionItem(item: SubjectCollectionInfo, content: @Composable () -> Unit) {
    var nsfwMode: NsfwMode by rememberSaveable(item) { mutableStateOf(item.nsfwMode) }
    NsfwMask(nsfwMode, onTemporarilyDisplay = { nsfwMode = NsfwMode.DISPLAY }, shape = RoundedCornerShape(0.dp)) {
        content()
    }
}

@Composable
private fun CollectionListRow(
    item: SubjectCollectionInfo,
    nextEpisodeCachedFlow: (subjectId: Int) -> Flow<Boolean>,
    onClick: () -> Unit,
) {
    val title = item.subjectInfo.preferredDisplayName(LocalSubjectAppearanceSettings.current.useOriginalTitle)
    val progress = remember(item) { item.episodeProgress() }
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppleCover(CoverAspect.Portrait2x3, title, Modifier.width(56.dp), cornerRadius = 8.dp) {
            AsyncImage(item.subjectInfo.listCoverUrl, null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                title,
                style = AniTextStyles.body,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            CollectionStatusLine(item, nextEpisodeCachedFlow)
            if (progress != null) {
                Row(
                    Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    AppleProgressBar({ progress.fraction }, Modifier.weight(1f))
                    EpisodeProgressText(progress, Modifier.widthIn(min = 52.dp))
                }
            }
        }
    }
}

/**
 * 状态行: 有新集 > 已快取 > 其他提示, 一行只显示一种.
 */
@Composable
private fun CollectionStatusLine(
    item: SubjectCollectionInfo,
    nextEpisodeCachedFlow: (subjectId: Int) -> Flow<Boolean>,
) {
    val labelState = remember(item) { AiringLabelState(item.airingInfo, item.progressInfo) }
    val fresh = labelState.highlightProgress
    val cached by remember(item.subjectId) { nextEpisodeCachedFlow(item.subjectId) }
        .collectAsStateWithLifecycle(false)
    val colorScheme = MaterialTheme.colorScheme
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        when {
            fresh -> {
                AlertDot()
                labelState.progressText(rememberSubjectStatusStrings())?.let {
                    StatusText(it, colorScheme.onSurface, FontWeight.SemiBold)
                }
            }

            cached -> {
                Icon(
                    AppleIcons.download(2.4f),
                    contentDescription = null,
                    Modifier.size(13.dp),
                    tint = colorScheme.secondary,
                )
                StatusText(stringResource(Lang.subject_episode_cached), colorScheme.secondary, FontWeight.Medium)
            }

            else -> labelState.progressText(rememberSubjectStatusStrings())?.let {
                StatusText(it, colorScheme.secondary, FontWeight.Medium)
            }
        }
    }
}

@Composable
private fun StatusText(text: String, color: Color, fontWeight: FontWeight) {
    Text(
        text,
        style = AniTextStyles.footnote,
        fontWeight = fontWeight,
        color = color,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * 「**8** / 28 集」: 数字 label W600, 其余 label2.
 */
@Composable
private fun EpisodeProgressText(progress: CollectionEpisodeProgress, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val watched = progress.watched.toString()
    val full = stringResource(Lang.collection_episode_progress, progress.watched, progress.total)
    Text(
        buildAnnotatedString {
            val start = full.indexOf(watched)
            if (start == -1) {
                append(full)
            } else {
                append(full.substring(0, start))
                withStyle(SpanStyle(color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold)) {
                    append(watched)
                }
                append(full.substring(start + watched.length))
            }
        },
        modifier,
        style = AniTextStyles.caption.copy(fontFeatureSettings = "tnum"),
        color = colorScheme.secondary,
        textAlign = TextAlign.End,
        maxLines = 1,
    )
}

/**
 * 网格模式: 3 栏, 列距 12、行距 24. 封面 2:3 圆角 10, 底部进度条, 有新集时右上角提示点.
 */
private fun LazyListScope.collectionGridItems(
    items: LazyPagingItems<SubjectCollectionInfo>,
    onClickSubject: (SubjectInfo) -> Unit,
) {
    val rows = (items.itemCount + GridColumns - 1) / GridColumns
    items(rows, key = { "grid-row-$it" }) { row ->
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = if (row == rows - 1) 0.dp else 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            for (column in 0 until GridColumns) {
                val index = row * GridColumns + column
                Box(Modifier.weight(1f)) {
                    val item = if (index < items.itemCount) items[index] else null
                    if (item != null) {
                        NsfwCollectionItem(item) {
                            CollectionGridCell(item, onClick = { onClickSubject(item.subjectInfo) })
                        }
                    }
                }
            }
        }
    }
}

private const val GridColumns = 3

@Composable
private fun CollectionGridCell(
    item: SubjectCollectionInfo,
    onClick: () -> Unit,
) {
    val title = item.subjectInfo.preferredDisplayName(LocalSubjectAppearanceSettings.current.useOriginalTitle)
    val progress = remember(item) { item.episodeProgress() }
    val fresh = remember(item) { AiringLabelState(item.airingInfo, item.progressInfo) }.highlightProgress
    Column(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
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
                style = AniTextStyles.subhead,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (progress != null) {
                Text(
                    stringResource(Lang.collection_episode_progress, progress.watched, progress.total),
                    style = AniTextStyles.caption,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * 看过的集数与总集数. 没有剧集信息时为 `null`.
 */
internal class CollectionEpisodeProgress(val watched: Int, val total: Int) {
    val fraction: Float get() = if (total == 0) 0f else watched.toFloat() / total
}

internal fun SubjectCollectionInfo.episodeProgress(): CollectionEpisodeProgress? {
    if (episodes.isEmpty()) return null
    return CollectionEpisodeProgress(
        watched = episodes.count { it.collectionType.isDoneOrDropped() },
        total = episodes.size,
    )
}

/**
 * Apple 版的状态胶囊顺序: 设计稿的在看、想看、看过在前, 搁置与抛弃在后.
 */
private val AppleCollectionTypes = listOf(
    UnifiedCollectionType.DOING,
    UnifiedCollectionType.WISH,
    UnifiedCollectionType.DONE,
    UnifiedCollectionType.ON_HOLD,
    UnifiedCollectionType.DROPPED,
)

private fun UnifiedCollectionType.label(): StringResource = when (this) {
    UnifiedCollectionType.WISH -> Lang.subject_collection_wish
    UnifiedCollectionType.DOING -> Lang.subject_collection_doing
    UnifiedCollectionType.DONE -> Lang.subject_collection_done
    UnifiedCollectionType.ON_HOLD -> Lang.subject_collection_on_hold
    UnifiedCollectionType.DROPPED -> Lang.subject_collection_dropped
    UnifiedCollectionType.NOT_COLLECTED -> Lang.subject_collection_doing
}
