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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.episode.EpisodeCollectionInfo
import me.him188.ani.app.domain.media.selector.SelectTrigger
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.navigation.LocalBrowserNavigator
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.EpisodeTile
import me.him188.ani.app.ui.foundation.apple.EpisodeTileDefaults
import me.him188.ani.app.ui.foundation.apple.PillTab
import me.him188.ani.app.ui.foundation.apple.episodeTileLabel
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.navigation.BackHandler
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.ModalSideSheet
import me.him188.ani.app.ui.foundation.widgets.rememberModalSideSheetState
import me.him188.ani.app.ui.foundation.widgets.showLoadError
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.episode_comments
import me.him188.ani.app.ui.lang.episode_danmaku_count
import me.him188.ani.app.ui.lang.media_selector_summary_auto_selecting
import me.him188.ani.app.ui.lang.media_selector_summary_change
import me.him188.ani.app.ui.lang.media_selector_summary_select_source
import me.him188.ani.app.ui.lang.media_selector_summary_source
import me.him188.ani.app.ui.lang.subject_details_all_episodes
import me.him188.ani.app.ui.lang.subject_details_episodes
import me.him188.ani.app.ui.lang.subject_details_show_less
import me.him188.ani.app.ui.lang.subject_details_tab_details
import me.him188.ani.app.ui.lang.subject_episode_close_selector
import me.him188.ani.app.ui.lang.subject_episode_danmaku_list_empty
import me.him188.ani.app.ui.lang.subject_episode_danmaku_list_empty_filtered
import me.him188.ani.app.ui.lang.subject_episode_danmaku_list_title
import me.him188.ani.app.ui.lang.subject_episode_related_recommendations
import me.him188.ani.app.ui.lang.subject_episode_select_media_source
import me.him188.ani.app.ui.media.rememberMediaDetailsStrings
import me.him188.ani.app.ui.mediaselect.MediaSelectorMode
import me.him188.ani.app.ui.mediaselect.auto.AutoMatchPage
import me.him188.ani.app.ui.mediaselect.bt.BtResourcesPage
import me.him188.ani.app.ui.mediaselect.common.MediaSelectorDialog
import me.him188.ani.app.ui.mediaselect.common.MediaSelectorDialogContent
import me.him188.ani.app.ui.mediaselect.common.MediaSelectorModeChip
import me.him188.ani.app.ui.mediaselect.common.SourceIcon
import me.him188.ani.app.ui.mediaselect.manual.ManualBrowsePage
import me.him188.ani.app.ui.mediaselect.needsContainer
import me.him188.ani.app.ui.mediaselect.summary.MediaSelectorSummary
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeDialogsHost
import me.him188.ani.app.ui.subject.episode.details.DanmakuInfoSheet
import me.him188.ani.app.ui.subject.episode.details.DanmakuListItem
import me.him188.ani.app.ui.subject.episode.details.DanmakuListState
import me.him188.ani.app.ui.subject.episode.details.DanmakuTimeShiftEditor
import me.him188.ani.app.ui.subject.episode.details.SubjectDetailsSheet
import me.him188.ani.app.ui.subject.episode.details.components.SubjectRecommendationCard
import me.him188.ani.app.ui.subject.episode.details.openSubjectRecommendation
import me.him188.ani.app.ui.subject.episode.statistics.DanmakuMatchInfoSummaryBanner
import me.him188.ani.danmaku.api.DanmakuServiceId
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.topic.toggleCollected
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 播放页的右侧面板 (04 §3.8): 宽 360, bg 底, 左边 1px separator.
 *
 * 顶部高 52 为 C4「詳情 / 評論 N」. 详情为番名与集名、选集 (6 栏)、资料来源卡、弹幕列表,
 * 之后沿用现有的弹幕匹配信息与相关推荐. 评论沿用现有的评论列表.
 */
@Composable
internal fun MacEpisodeSidebar(
    vm: EpisodeViewModel,
    page: EpisodePageState,
    pauseOnPlaying: () -> Unit,
    setShowEditCommentSheet: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val colorScheme = MaterialTheme.colorScheme
    Row(modifier.fillMaxSize().background(colorScheme.surfaceContainerLowest)) {
        Box(Modifier.width(1.dp).fillMaxHeight().background(colorScheme.outlineVariant))
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Row(
                Modifier.fillMaxWidth().height(MacEpisodeSidebarDefaults.HeaderHeight).padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PillTab(
                    stringResource(Lang.subject_details_tab_details),
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    Modifier.testTag(MacEpisodeSidebarTestTags.tab(0)),
                )
                PillTab(
                    stringResource(Lang.episode_comments),
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    Modifier.testTag(MacEpisodeSidebarTestTags.tab(1)),
                    count = vm.episodeCommentState.count?.toString(),
                )
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (selectedTab) {
                    0 -> MacEpisodeDetailsTab(vm, page, Modifier.fillMaxSize())
                    else -> EpisodeCommentColumn(
                        commentState = vm.episodeCommentState,
                        commentReportState = vm.commentReportState,
                        commentEditorState = vm.commentEditorState,
                        subjectId = vm.subjectId,
                        episodeId = page.episodePresentation.episodeId,
                        setShowEditCommentSheet = setShowEditCommentSheet,
                        pauseOnPlaying = pauseOnPlaying,
                        gridState = vm.commentLazyGirdState,
                    )
                }
            }
        }
    }
}

/**
 * 「詳情」分页. 资料来源卡的「更換」打开现有的选择资料来源对话框.
 */
@Composable
private fun MacEpisodeDetailsTab(
    vm: EpisodeViewModel,
    page: EpisodePageState,
    modifier: Modifier = Modifier,
) {
    val navigator = LocalNavigator.current
    val context = LocalContext.current
    val browserNavigator = LocalBrowserNavigator.current
    val toaster = LocalToaster.current
    val scope = rememberCoroutineScope()
    val carousel = vm.episodeCarouselState
    val recommendations by vm.episodeDetailsState.recommendations
    val selectorPresentation by page.mediaSelectorState.presentationFlow.collectAsStateWithLifecycle()
    val danmakuListState by vm.danmakuListState.collectAsStateWithLifecycle()
    val useOriginalTitle = LocalSubjectAppearanceSettings.current.useOriginalTitle
    val subject = page.subjectPresentation
    val episode = page.episodePresentation

    var showAllEpisodes by rememberSaveable { mutableStateOf(false) }
    var showMediaSelector by rememberSaveable { mutableStateOf(false) }
    var showDanmakuInfoSheet by rememberSaveable { mutableStateOf(false) }
    var showSubjectDetails by rememberSaveable { mutableStateOf(false) }
    var editingShiftServiceId by remember { mutableStateOf<DanmakuServiceId?>(null) }
    // 会话未就绪时选择器是占位 state, 不打开也不保持打开.
    val mediaSelectorAvailable = page.watchingEpisode != null
    LaunchedEffect(mediaSelectorAvailable) {
        if (!mediaSelectorAvailable) showMediaSelector = false
    }

    EditableSubjectCollectionTypeDialogsHost(vm.editableSubjectCollectionTypeState)

    LazyColumn(
        modifier,
        contentPadding = PaddingValues(start = 16.dp, top = 4.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        page.loadError?.let { error ->
            item("load_error") {
                val loadError = when (error) {
                    is EpisodePageLoadError.SubjectError -> error.loadError
                    is EpisodePageLoadError.SeriesError -> error.loadError
                }
                LoadErrorCard(loadError, onRetry = { vm.retryLoad(error) })
            }
        }

        item("title") {
            MacEpisodeTitle(
                subjectTitle = if (useOriginalTitle) subject.originalTitle else subject.title,
                episodeTitle = appleEpisodeLabel(episode.ep, if (useOriginalTitle) episode.originalTitle else episode.title),
                onClickSubject = {
                    showSubjectDetails = true
                    vm.episodeDetailsState.subjectDetailsStateLoader.load(
                        vm.episodeDetailsState.subjectId,
                        vm.episodeDetailsState.subjectInfo.value,
                    )
                },
                Modifier.placeholder(subject.isPlaceholder || episode.isPlaceholder),
            )
        }

        item("episodes") {
            if (carousel.episodes.isNotEmpty()) {
                MacEpisodeGrid(
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
            MacMediaSourceCard(
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
            )
        }

        item("danmaku_list") {
            MacDanmakuList(danmakuListState)
        }

        item("danmaku") {
            DanmakuMatchInfoSummaryBanner(
                page.danmakuStatistics,
                expanded = showDanmakuInfoSheet,
                toggleExpanded = { showDanmakuInfoSheet = true },
            )
        }

        if (recommendations.isNotEmpty()) {
            item("recommendation_header") {
                MacSectionTitle(stringResource(Lang.subject_episode_related_recommendations))
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
                    Modifier.fillMaxWidth(),
                )
            }
        }
    }

    if (showMediaSelector) {
        MacMediaSelectorHost(vm, page, onDismissRequest = { showMediaSelector = false })
    }
    if (showDanmakuInfoSheet) {
        DanmakuInfoSheet(
            danmakuListState,
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
    if (showSubjectDetails) {
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
 * 番名 20/700 与集名 13 label2. 点番名打开条目详情.
 */
@Composable
private fun MacEpisodeTitle(
    subjectTitle: String,
    episodeTitle: String,
    onClickSubject: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            subjectTitle,
            Modifier.clickable(onClick = onClickSubject),
            color = MaterialTheme.colorScheme.onSurface,
            style = AniTextStyles.macTitle.copy(fontSize = 20.sp, lineHeight = 26.sp),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            episodeTitle,
            color = MaterialTheme.colorScheme.secondary,
            style = AniTextStyles.macCallout,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}


/**
 * 「更換」打开的选择资料来源, 沿用现有宽屏版: 侧边 sheet 里切换自动匹配、手动查找与 BT;
 * 窗口太矮时手动查找与 BT 用窗口级对话框, 关掉对话框回到 sheet 的自动匹配. 选中播放后全部关闭.
 * 桌面的 Esc 由窗口交给 App 的返回分派, 这里接住, 只关闭 sheet 或对话框.
 */
@Composable
private fun MacMediaSelectorHost(
    vm: EpisodeViewModel,
    page: EpisodePageState,
    onDismissRequest: () -> Unit,
) {
    val mode = vm.mediaSelectorMode ?: page.initialMediaSelectorMode
    val onModeChange: (MediaSelectorMode) -> Unit = { vm.mediaSelectorMode = it }
    val sourceResults = page.mediaSourceResultListPresentation
    val windowHeight = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }
    if (mode.needsContainer(windowHeight)) {
        val backToSheet = { onModeChange(MediaSelectorMode.AUTO) }
        BackHandler(onBack = backToSheet)
        MediaSelectorDialog(onDismissRequest = backToSheet) { compact ->
            MediaSelectorDialogContent(
                mode,
                compact,
                onModeChange = onModeChange,
                onClose = backToSheet,
                onPlayed = onDismissRequest,
                mediaSelectorState = page.mediaSelectorState,
                sourceResults = sourceResults,
                watching = page.watchingEpisode,
                fetchRequest = page.fetchRequest,
                onFetchRequestChange = { vm.updateFetchRequest(it) },
                onRestartSource = { vm.restartSource(it) },
                manualBrowseState = vm.manualBrowseState,
            )
        }
        return
    }

    val sheetState = rememberModalSideSheetState()
    BackHandler { sheetState.close() }
    ModalSideSheet(
        onDismissRequest,
        state = sheetState,
        containerColor = BottomSheetDefaults.ContainerColor,
    ) {
        val closeSelectorText = stringResource(Lang.subject_episode_close_selector)
        val closeButton = @Composable {
            IconButton(onClick = { sheetState.close() }, Modifier.padding(end = 8.dp)) {
                Icon(Icons.Rounded.Close, contentDescription = closeSelectorText)
            }
        }
        val topBar = @Composable {
            TopAppBar(
                title = {
                    Text(stringResource(Lang.subject_episode_select_media_source), Modifier.padding(start = 8.dp))
                },
                actions = {
                    MediaSelectorModeChip(mode, onModeChange, showBt = sourceResults.btSources.isNotEmpty())
                    closeButton()
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BottomSheetDefaults.ContainerColor),
            )
        }
        Column(
            Modifier
                .fillMaxHeight()
                .widthIn(300.dp, 400.dp)
                .windowInsetsPadding(AniWindowInsets.safeDrawing),
        ) {
            when (mode) {
                MediaSelectorMode.AUTO -> {
                    topBar()
                    AutoMatchPage(
                        page.mediaSelectorState,
                        onClickItem = {
                            page.mediaSelectorState.select(it, SelectTrigger.MANUAL_AUTO_PAGE)
                            onDismissRequest()
                        },
                        onRestartSource = { vm.restartSource(it) },
                        onRequestManualSearch = { onModeChange(MediaSelectorMode.MANUAL) },
                        modifier = Modifier
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                            .fillMaxWidth(),
                    )
                }

                MediaSelectorMode.MANUAL -> ManualBrowsePage(
                    vm.manualBrowseState,
                    page.watchingEpisode,
                    onPlayed = onDismissRequest,
                    topBar = topBar,
                    modifier = Modifier.fillMaxSize(),
                    closeButton = closeButton,
                )

                MediaSelectorMode.BT -> {
                    topBar()
                    BtResourcesPage(
                        page.mediaSelectorState,
                        sourceResults,
                        page.watchingEpisode,
                        page.fetchRequest,
                        onFetchRequestChange = { vm.updateFetchRequest(it) },
                        onClickItem = {
                            page.mediaSelectorState.select(it, SelectTrigger.MANUAL_BT_PAGE)
                            onDismissRequest()
                        },
                        onRestartSource = { vm.restartSource(it) },
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * 选集: 标题「選集」与 accent「全部 N 話」, 下方 6 栏方块 (1:1, 圆角 10; 正在播放的集底部 accent 短横).
 * 默认只显示正在播放那一集所在的两行, 「全部 N 話」展开全部, 再点收起; 不超过两行时不显示「全部 N 話」.
 *
 * 点击播放该集, 长按切换看过.
 */
@Composable
internal fun MacEpisodeGrid(
    episodes: List<EpisodeCollectionInfo>,
    playingEpisodeId: Int?,
    expanded: Boolean,
    onToggleExpanded: () -> Unit,
    onClickEpisode: (EpisodeCollectionInfo) -> Unit,
    onLongClickEpisode: (EpisodeCollectionInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    val columns = MacEpisodeSidebarDefaults.EpisodeColumns
    val rows = remember(episodes) { episodes.chunked(columns) }
    val collapsedRows = MacEpisodeSidebarDefaults.CollapsedEpisodeRows
    val canExpand = rows.size > collapsedRows
    val visibleRows = if (expanded || !canExpand) {
        rows
    } else {
        val playingIndex = episodes.indexOfFirst { it.episodeInfo.episodeId == playingEpisodeId }.coerceAtLeast(0)
        val startRow = (playingIndex / columns).coerceAtMost(rows.size - collapsedRows)
        rows.subList(startRow, startRow + collapsedRows)
    }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MacSectionTitle(stringResource(Lang.subject_details_episodes)) {
            if (canExpand) {
                AppleTextButton(onToggleExpanded, Modifier.testTag(MacEpisodeSidebarTestTags.AllEpisodes)) {
                    Text(
                        if (expanded) {
                            stringResource(Lang.subject_details_show_less)
                        } else {
                            stringResource(Lang.subject_details_all_episodes, episodes.size)
                        },
                    )
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (row in visibleRows) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (episode in row) {
                        EpisodeTile(
                            episodeTileLabel(episode.episodeInfo.sort),
                            state = appleEpisodeTileState(episode, playingEpisodeId),
                            onClick = { onClickEpisode(episode) },
                            Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .testTag(MacEpisodeSidebarTestTags.episode(episode.episodeInfo.episodeId)),
                            onLongClick = { onLongClickEpisode(episode) },
                            cornerRadius = EpisodeTileDefaults.MacCornerRadius,
                        )
                    }
                    repeat(columns - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * 资料来源卡: card 高 56 圆角 12; 28 方形图标 (fill) + 「資料來源」11 label2 + 「来源 · 画质 · 字幕」13/600 (单行省略)
 * + accent「更換」. 点击打开选择资料来源.
 */
@Composable
internal fun MacMediaSourceCard(
    summary: MediaSelectorSummary,
    selectedMedia: Media?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = rememberMediaDetailsStrings()
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceContainerLow)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 14.dp, end = 14.dp)
            .testTag(MacEpisodeSidebarTestTags.MediaSource),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            if (summary is MediaSelectorSummary.Selected) {
                SourceIcon(summary.source.sourceIconUrl, summary.source.sourceName, Modifier.size(18.dp))
            } else {
                Icon(AppleIcons.sliders(), contentDescription = null, Modifier.size(14.dp), tint = colorScheme.secondary)
            }
        }
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(Lang.media_selector_summary_source),
                color = colorScheme.secondary,
                style = AniTextStyles.macCaption.copy(fontSize = 11.sp, lineHeight = 14.sp),
                maxLines = 1,
            )
            Text(
                when (summary) {
                    is MediaSelectorSummary.AutoSelecting -> stringResource(Lang.media_selector_summary_auto_selecting)
                    is MediaSelectorSummary.RequiresManualSelection -> stringResource(Lang.media_selector_summary_select_source)
                    is MediaSelectorSummary.Selected -> appleMediaSourceText(summary.source.sourceName, selectedMedia, strings)
                },
                color = if (summary is MediaSelectorSummary.RequiresManualSelection) colorScheme.error else colorScheme.onSurface,
                style = AniTextStyles.macCallout.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            stringResource(Lang.media_selector_summary_change),
            color = colorScheme.primary,
            style = AniTextStyles.macCallout,
            maxLines = 1,
        )
    }
}

/**
 * 弹幕列表: 标题与「N 條」12 label2; card 圆角 12, 行高 36: 时间 40 宽 12 label2 + 内容 13. 最高 360, 内部滚动.
 * 加载中显示加载指示, 没有弹幕时显示现有的空状态文字.
 */
@Composable
internal fun MacDanmakuList(state: DanmakuListState, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        MacSectionTitle(stringResource(Lang.subject_episode_danmaku_list_title)) {
            if (!state.isLoading) {
                Text(
                    stringResource(Lang.episode_danmaku_count, state.danmakuItems.size),
                    color = colorScheme.secondary,
                    style = AniTextStyles.macCaption,
                    modifier = Modifier.testTag(MacEpisodeSidebarTestTags.DanmakuCount),
                )
            }
        }
        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.surfaceContainerLow),
        ) {
            when {
                state.danmakuItems.isNotEmpty() -> LazyColumn(Modifier.fillMaxWidth().heightIn(max = 360.dp)) {
                    items(state.danmakuItems, key = { it.randomId.toString() }) { item ->
                        MacDanmakuListRow(item)
                    }
                }

                state.isLoading -> Box(Modifier.fillMaxWidth().height(72.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                }

                else -> Text(
                    stringResource(
                        if (state.isEmpty) Lang.subject_episode_danmaku_list_empty else Lang.subject_episode_danmaku_list_empty_filtered,
                    ),
                    Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 16.dp),
                    color = colorScheme.secondary,
                    style = AniTextStyles.macCallout,
                )
            }
        }
    }
}

@Composable
private fun MacDanmakuListRow(item: DanmakuListItem) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .height(36.dp)
            .then(if (item.isSelf) Modifier.background(colorScheme.secondaryContainer) else Modifier)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            formatDanmakuTime(item.timeMillis),
            Modifier.width(40.dp),
            color = colorScheme.secondary,
            style = AniTextStyles.macCaption.copy(fontFeatureSettings = "tnum"),
            maxLines = 1,
        )
        Text(
            item.content,
            Modifier.weight(1f),
            color = if (item.isSelf) colorScheme.onSecondaryContainer else colorScheme.onSurface,
            style = AniTextStyles.macCallout,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 弹幕时间「分:秒」, 秒补零到两位, 同现有弹幕列表.
 */
internal fun formatDanmakuTime(timeMillis: Long): String {
    val totalSeconds = timeMillis / 1000
    return "${totalSeconds / 60}:${(totalSeconds % 60).toString().padStart(2, '0')}"
}

/**
 * 区块标题 15/600, 右侧可放说明或 accent 链接.
 */
@Composable
private fun MacSectionTitle(
    title: String,
    trailing: @Composable () -> Unit = {},
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            Modifier.weight(1f),
            style = AniTextStyles.macWindowTitle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        trailing()
    }
}

internal object MacEpisodeSidebarDefaults {
    val Width = 360.dp
    val HeaderHeight = 52.dp
    const val EpisodeColumns = 6

    /** 选集默认显示的行数. */
    const val CollapsedEpisodeRows = 2
}

internal object MacEpisodeSidebarTestTags {
    const val AllEpisodes = "mac-episode-all-episodes"
    const val MediaSource = "mac-episode-media-source"
    const val DanmakuCount = "mac-episode-danmaku-count"
    fun tab(index: Int) = "mac-episode-tab-$index"
    fun episode(episodeId: Int) = "mac-episode-episode-$episodeId"
}
