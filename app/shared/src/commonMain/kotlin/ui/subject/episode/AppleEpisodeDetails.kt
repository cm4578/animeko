/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.episode.EpisodeCollectionInfo
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.navigation.LocalBrowserNavigator
import me.him188.ani.app.ui.danmaku.DanmakuEditorState
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.EpisodeTile
import me.him188.ani.app.ui.foundation.apple.EpisodeTileState
import me.him188.ani.app.ui.foundation.apple.PillTab
import me.him188.ani.app.ui.foundation.apple.episodeTileLabel
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.showLoadError
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.episode_comments
import me.him188.ani.app.ui.lang.episode_danmaku_style
import me.him188.ani.app.ui.lang.media_selector_summary_auto_selecting
import me.him188.ani.app.ui.lang.media_selector_summary_change
import me.him188.ani.app.ui.lang.media_selector_summary_select_source
import me.him188.ani.app.ui.lang.media_selector_summary_source
import me.him188.ani.app.ui.lang.subject_details_all_episodes
import me.him188.ani.app.ui.lang.subject_details_episodes
import me.him188.ani.app.ui.lang.subject_details_show_less
import me.him188.ani.app.ui.lang.subject_details_tab_details
import me.him188.ani.app.ui.lang.subject_episode_related_recommendations
import me.him188.ani.app.ui.media.MediaDetailsRenderer
import me.him188.ani.app.ui.media.MediaDetailsStrings
import me.him188.ani.app.ui.media.rememberMediaDetailsStrings
import me.him188.ani.app.ui.media.renderResolution
import me.him188.ani.app.ui.media.renderSubtitleLanguage
import me.him188.ani.app.ui.mediaselect.common.SourceIcon
import me.him188.ani.app.ui.mediaselect.summary.MediaSelectorSummary
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeDialogsHost
import me.him188.ani.app.ui.subject.episode.details.CompactMediaSelectorSheet
import me.him188.ani.app.ui.subject.episode.details.DanmakuInfoSheet
import me.him188.ani.app.ui.subject.episode.details.DanmakuTimeShiftEditor
import me.him188.ani.app.ui.subject.episode.details.SubjectDetailsSheet
import me.him188.ani.app.ui.subject.episode.details.components.SubjectRecommendationCard
import me.him188.ani.app.ui.subject.episode.details.openSubjectRecommendation
import me.him188.ani.app.ui.subject.episode.statistics.DanmakuMatchInfoSummaryBanner
import me.him188.ani.app.videoplayer.ui.progress.PlayerControllerDefaults.rememberRandomDanmakuPlaceholder
import me.him188.ani.danmaku.api.DanmakuServiceId
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.isDoneOrDropped
import me.him188.ani.datasources.api.topic.toggleCollected
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版窄屏播放器下方的内容 (03 §3.7): 弹幕输入、番名与集名、「詳情 / 評論」分页.
 *
 * 弹幕输入框与「彈幕樣式」钮都打开现有的发送弹幕底部弹窗, 打开时暂停播放.
 */
@Composable
internal fun AppleEpisodePhoneDetails(
    vm: EpisodeViewModel,
    page: EpisodePageState,
    danmakuEditorState: DanmakuEditorState,
    pauseOnPlaying: () -> Unit,
    tryUnpause: () -> Unit,
    setShowEditCommentSheet: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = ScaffoldDefaults.contentWindowInsets,
) {
    var showDanmakuEditor by rememberSaveable { mutableStateOf(false) }
    var showSubjectDetails by rememberSaveable { mutableStateOf(false) }
    val openDanmakuEditor = {
        showDanmakuEditor = true
        pauseOnPlaying()
    }
    val pagerState = rememberPagerState(initialPage = 0) { 2 }
    val scope = rememberCoroutineScope()
    val useOriginalTitle = LocalSubjectAppearanceSettings.current.useOriginalTitle
    val subject = page.subjectPresentation
    val episode = page.episodePresentation

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        AppleDanmakuInputRow(
            placeholder = rememberRandomDanmakuPlaceholder(),
            onClick = openDanmakuEditor,
            onClickStyle = openDanmakuEditor,
            Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp),
        )
        AppleEpisodeTitle(
            subjectTitle = if (useOriginalTitle) subject.originalTitle else subject.title,
            episodeTitle = appleEpisodeLabel(episode.ep, if (useOriginalTitle) episode.originalTitle else episode.title),
            onClickSubject = {
                showSubjectDetails = true
                vm.episodeDetailsState.subjectDetailsStateLoader.load(
                    vm.episodeDetailsState.subjectId,
                    vm.episodeDetailsState.subjectInfo.value,
                )
            },
            Modifier
                .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                .placeholder(subject.isPlaceholder || episode.isPlaceholder),
        )
        Row(
            Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            PillTab(
                stringResource(Lang.subject_details_tab_details),
                selected = pagerState.currentPage == 0,
                onClick = { scope.launch { pagerState.animateScrollToPage(0) } },
                Modifier.testTag(AppleEpisodeDetailsTestTags.tab(0)),
            )
            PillTab(
                stringResource(Lang.episode_comments),
                selected = pagerState.currentPage == 1,
                onClick = { scope.launch { pagerState.animateScrollToPage(1) } },
                Modifier.testTag(AppleEpisodeDetailsTestTags.tab(1)),
                count = vm.episodeCommentState.count?.toString(),
            )
        }
        HorizontalPager(pagerState, Modifier.fillMaxSize()) { index ->
            when (index) {
                0 -> AppleEpisodeDetailsTab(vm, page, Modifier.fillMaxSize())
                else -> EpisodeCommentColumn(
                    commentState = vm.episodeCommentState,
                    commentReportState = vm.commentReportState,
                    commentEditorState = vm.commentEditorState,
                    subjectId = vm.subjectId,
                    episodeId = episode.episodeId,
                    setShowEditCommentSheet = setShowEditCommentSheet,
                    pauseOnPlaying = pauseOnPlaying,
                    gridState = vm.commentLazyGirdState,
                )
            }
        }
    }

    if (showDanmakuEditor) {
        DanmakuEditorSheet(
            vm,
            danmakuEditorState,
            onDismissRequest = {
                showDanmakuEditor = false
                tryUnpause()
            },
            windowInsets = windowInsets,
        )
    }
    if (showSubjectDetails) {
        val navigator = LocalNavigator.current
        val toaster = LocalToaster.current
        SubjectDetailsSheet(
            vm.episodeDetailsState,
            page.selfInfo,
            onPlay = { episodeId ->
                if (!vm.episodeSelectorState.selectEpisodeId(episodeId)) {
                    navigator.navigateEpisodeDetails(vm.subjectId, episodeId)
                }
            },
            onClickTag = { navigator.navigateSubjectSearch(it.name) },
            onEpisodeCollectionUpdate = { request ->
                scope.launch {
                    vm.setEpisodeCollectionType.invokeSafe(request)?.let { toaster.showLoadError(it) }
                }
            },
            onDismissRequest = { showSubjectDetails = false },
        )
    }
}

/**
 * 「詳情」分页: 选集、资料来源卡; 之后沿用现有的弹幕匹配信息与相关推荐.
 */
@Composable
private fun AppleEpisodeDetailsTab(
    vm: EpisodeViewModel,
    page: EpisodePageState,
    modifier: Modifier = Modifier,
) {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val browserNavigator = LocalBrowserNavigator.current
    val carousel = vm.episodeCarouselState
    val recommendations by vm.episodeDetailsState.recommendations
    val selectorPresentation by page.mediaSelectorState.presentationFlow.collectAsStateWithLifecycle()

    var showAllEpisodes by rememberSaveable { mutableStateOf(false) }
    var showMediaSelector by rememberSaveable { mutableStateOf(false) }
    var showDanmakuInfoSheet by rememberSaveable { mutableStateOf(false) }
    var editingShiftServiceId by remember { mutableStateOf<DanmakuServiceId?>(null) }
    // 会话未就绪时选择器是占位 state, 不打开也不保持打开.
    val mediaSelectorAvailable = page.watchingEpisode != null
    LaunchedEffect(mediaSelectorAvailable) {
        if (!mediaSelectorAvailable) showMediaSelector = false
    }

    EditableSubjectCollectionTypeDialogsHost(vm.editableSubjectCollectionTypeState)

    LazyColumn(
        modifier,
        contentPadding = PaddingValues(top = 20.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        page.loadError?.let { error ->
            item("load_error") {
                val loadError = when (error) {
                    is EpisodePageLoadError.SubjectError -> error.loadError
                    is EpisodePageLoadError.SeriesError -> error.loadError
                }
                Box(Modifier.padding(horizontal = 16.dp)) {
                    LoadErrorCard(loadError, onRetry = { vm.retryLoad(error) })
                }
            }
        }

        item("episodes") {
            if (carousel.episodes.isNotEmpty()) {
                AppleEpisodeSection(
                    carousel.episodes,
                    playingEpisodeId = carousel.playingEpisode?.episodeInfo?.episodeId,
                    expanded = showAllEpisodes,
                    onToggleExpanded = { showAllEpisodes = !showAllEpisodes },
                    onClickEpisode = { carousel.onSelect(it) },
                    onLongClickEpisode = { carousel.setCollectionType(it, it.collectionType.toggleCollected()) },
                )
            }
        }

        item("media_source") {
            AppleMediaSourceCard(
                page.mediaSelectorSummary,
                selectedMedia = selectorPresentation.selected,
                onClick = {
                    if (mediaSelectorAvailable) {
                        if (vm.mediaSelectorMode == null && !page.isLoading) {
                            vm.mediaSelectorMode = page.initialMediaSelectorMode
                        }
                        showMediaSelector = true
                    }
                },
                Modifier.padding(horizontal = 16.dp),
            )
        }

        item("danmaku") {
            Box(Modifier.padding(horizontal = 16.dp)) {
                DanmakuMatchInfoSummaryBanner(
                    page.danmakuStatistics,
                    expanded = showDanmakuInfoSheet,
                    toggleExpanded = { showDanmakuInfoSheet = true },
                )
            }
        }

        if (recommendations.isNotEmpty()) {
            item("recommendation_header") {
                AppleEpisodeSectionTitle(stringResource(Lang.subject_episode_related_recommendations))
            }
            items(recommendations, key = { "recommendation_${it.uniqueId}" }) { recommendation ->
                SubjectRecommendationCard(
                    {
                        openSubjectRecommendation(
                            recommendation,
                            openBrowser = { browserNavigator.openBrowser(context, it) },
                            navigator,
                        )
                    },
                    recommendation,
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                )
            }
        }

        item("navigation_bar_spacer") {
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }

    if (showMediaSelector) {
        CompactMediaSelectorSheet(
            vm.mediaSelectorMode ?: page.initialMediaSelectorMode,
            onMediaSelectorModeChange = { vm.mediaSelectorMode = it },
            page.mediaSelectorState,
            page.mediaSourceResultListPresentation,
            page.watchingEpisode,
            page.fetchRequest,
            onFetchRequestChange = { vm.updateFetchRequest(it) },
            onRestartSource = { vm.restartSource(it) },
            vm.manualBrowseState,
            onDismissRequest = { showMediaSelector = false },
        )
    }
    if (showDanmakuInfoSheet) {
        DanmakuInfoSheet(
            vm.danmakuListState.collectAsStateWithLifecycle().value,
            onDismissRequest = { showDanmakuInfoSheet = false },
            onSetDanmakuSourceEnabled = { providerId, enabled -> vm.setDanmakuSourceEnabled(providerId, enabled) },
            onManualMatchDanmaku = vm::startMatchingDanmakuForService,
            onAdjustShift = { editingShiftServiceId = it },
            onSetDanmakuTextConversion = vm::setDanmakuTextConversionOverride,
            onSetDanmakuTextConversionGlobal = vm::setDanmakuTextConversionGlobal,
            onResetDanmakuTextConversionOverrides = vm::resetDanmakuTextConversionOverrides,
        )
    }
    DanmakuTimeShiftEditor(
        page.danmakuStatistics,
        editingShiftServiceId,
        onDismissRequest = { editingShiftServiceId = null },
        onAdjustDanmakuSourceShift = { serviceId, shiftMillis -> vm.setDanmakuSourceShiftMillis(serviceId, shiftMillis) },
    )
}

/**
 * 弹幕输入: 输入框高 40 圆角 20、fill 底、`15` 号提示文字; 右侧「彈幕樣式」40 圆钮 (fill).
 */
@Composable
internal fun AppleDanmakuInputRow(
    placeholder: String,
    onClick: () -> Unit,
    onClickStyle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val fill = MaterialTheme.colorScheme.surfaceContainerHighest
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(fill)
                .clickable(role = Role.Button, onClick = onClick)
                .padding(horizontal = 16.dp)
                .testTag(AppleEpisodeDetailsTestTags.DanmakuInput),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                placeholder,
                color = MaterialTheme.colorScheme.secondary,
                style = AniTextStyles.callout,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Box(
            Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(fill)
                .clickable(role = Role.Button, onClick = onClickStyle)
                .testTag(AppleEpisodeDetailsTestTags.DanmakuStyle),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                AppleIcons.sliders(),
                contentDescription = stringResource(Lang.episode_danmaku_style),
                Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * 番名 `20/26 700` 与集名 `14/20` label2. 点番名打开条目详情.
 */
@Composable
private fun AppleEpisodeTitle(
    subjectTitle: String,
    episodeTitle: String,
    onClickSubject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth()) {
        Text(
            subjectTitle,
            Modifier.clickable(onClick = onClickSubject),
            color = MaterialTheme.colorScheme.onSurface,
            style = AniTextStyles.title,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            episodeTitle,
            color = MaterialTheme.colorScheme.secondary,
            style = AniTextStyles.subhead,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 选集: 标题「選集」与 accent「全部 N 話」, 下方 C13 横向一排 (看过 / 正在播放 / 未看), 延伸到屏幕边缘.
 * 初始滚到正在播放那一集的前两集. 「全部 N 話」展开为网格, 再点收起.
 *
 * 点击播放该集, 长按切换看过.
 */
@Composable
internal fun AppleEpisodeSection(
    episodes: List<EpisodeCollectionInfo>,
    playingEpisodeId: Int?,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onClickEpisode: (EpisodeCollectionInfo) -> Unit,
    onLongClickEpisode: (EpisodeCollectionInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AppleEpisodeSectionTitle(stringResource(Lang.subject_details_episodes)) {
            AppleTextButton(onToggleExpanded, Modifier.testTag(AppleEpisodeDetailsTestTags.AllEpisodes)) {
                Text(
                    if (expanded) {
                        stringResource(Lang.subject_details_show_less)
                    } else {
                        stringResource(Lang.subject_details_all_episodes, episodes.size)
                    },
                )
            }
        }
        val tile: @Composable (EpisodeCollectionInfo) -> Unit = { episode ->
            EpisodeTile(
                episodeTileLabel(episode.episodeInfo.sort),
                state = appleEpisodeTileState(episode, playingEpisodeId),
                onClick = { onClickEpisode(episode) },
                Modifier.testTag(AppleEpisodeDetailsTestTags.episode(episode.episodeInfo.episodeId)),
                onLongClick = { onLongClickEpisode(episode) },
            )
        }
        if (expanded) {
            FlowRow(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                episodes.forEach { tile(it) }
            }
        } else {
            val rowState = rememberLazyListState(
                initialFirstVisibleItemIndex = episodes.indexOfFirst { it.episodeInfo.episodeId == playingEpisodeId }
                    .minus(2).coerceAtLeast(0),
            )
            LazyRow(
                state = rowState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(episodes, key = { it.episodeInfo.episodeId }) { tile(it) }
            }
        }
    }
}

/**
 * 正在播放的集为「正在播放」样式, 其余看过或抛弃的为「看过」.
 */
internal fun appleEpisodeTileState(episode: EpisodeCollectionInfo, playingEpisodeId: Int?): EpisodeTileState = when {
    episode.episodeInfo.episodeId == playingEpisodeId -> EpisodeTileState.Playing
    episode.collectionType.isDoneOrDropped() -> EpisodeTileState.Watched
    else -> EpisodeTileState.Unwatched
}

@Composable
private fun AppleEpisodeSectionTitle(
    title: String,
    action: @Composable () -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().padding(start = 20.dp, end = 16.dp),
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
 * 资料来源卡: card 高 60 圆角 16; 30 图标方块 (fill) + 「資料來源」`12/16` label2 + 当前资源 `15/20 600` (单行省略)
 * + accent「更換」. 点击打开选择资料来源.
 *
 * @param selectedMedia 当前播放的资源, 用于显示解析度与字幕.
 */
@Composable
internal fun AppleMediaSourceCard(
    summary: MediaSelectorSummary,
    selectedMedia: Media?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = rememberMediaDetailsStrings()
    Row(
        modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 14.dp, end = 16.dp)
            .testTag(AppleEpisodeDetailsTestTags.MediaSource),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            if (summary is MediaSelectorSummary.Selected) {
                SourceIcon(summary.source.sourceIconUrl, summary.source.sourceName, Modifier.size(20.dp))
            } else {
                Icon(
                    AppleIcons.sliders(),
                    contentDescription = null,
                    Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(Lang.media_selector_summary_source),
                color = MaterialTheme.colorScheme.secondary,
                style = AniTextStyles.caption,
                maxLines = 1,
            )
            Text(
                when (summary) {
                    is MediaSelectorSummary.AutoSelecting -> stringResource(Lang.media_selector_summary_auto_selecting)
                    is MediaSelectorSummary.RequiresManualSelection -> stringResource(Lang.media_selector_summary_select_source)
                    is MediaSelectorSummary.Selected -> appleMediaSourceText(summary.source.sourceName, selectedMedia, strings)
                },
                color = if (summary is MediaSelectorSummary.RequiresManualSelection) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                style = AniTextStyles.callout.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            stringResource(Lang.media_selector_summary_change),
            color = MaterialTheme.colorScheme.primary,
            style = AniTextStyles.callout,
            maxLines = 1,
        )
    }
}

/**
 * 资料来源卡的值: 资料来源名称、解析度、字幕语言与字幕类型, 以「 · 」分隔, 没有的项目省略.
 * 例如「蜜柑計劃 · 1080P · 簡中/日語 · 內嵌」.
 */
internal fun appleMediaSourceText(sourceName: String, media: Media?, strings: MediaDetailsStrings): String {
    val properties = media?.properties
    return listOfNotNull(
        sourceName,
        properties?.resolution?.takeIf { it.isNotBlank() }?.let(::renderResolution),
        properties?.subtitleLanguageIds?.takeIf { it.isNotEmpty() }
            ?.joinToString("/") { renderSubtitleLanguage(it, strings) },
        properties?.subtitleKind?.let { MediaDetailsRenderer.renderSubtitleKind(it, strings) },
    ).joinToString(" · ")
}

internal object AppleEpisodeDetailsTestTags {
    const val DanmakuInput = "apple-episode-danmaku-input"
    const val DanmakuStyle = "apple-episode-danmaku-style"
    const val AllEpisodes = "apple-episode-all-episodes"
    const val MediaSource = "apple-episode-media-source"
    fun tab(index: Int) = "apple-episode-tab-$index"
    fun episode(episodeId: Int) = "apple-episode-episode-$episodeId"
}
