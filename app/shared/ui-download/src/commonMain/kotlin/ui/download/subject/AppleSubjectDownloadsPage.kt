/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.download.subject

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.ani.app.tools.getOrZero
import me.him188.ani.app.ui.download.DeleteActionDialog
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.components.DownloadSelectionState
import me.him188.ani.app.ui.download.components.DownloadStatus
import me.him188.ani.app.ui.download.components.rememberDownloadSelectionState
import me.him188.ani.app.ui.download.components.renderFileSize
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleCircleButton
import me.him188.ani.app.ui.foundation.apple.AppleComponentDefaults
import me.him188.ani.app.ui.foundation.apple.AppleCover
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleProgressBar
import me.him188.ani.app.ui.foundation.apple.AppleSelectionCircle
import me.him188.ani.app.ui.foundation.apple.AppleSelectionToolbar
import me.him188.ani.app.ui.foundation.apple.AppleSelectionToolbarAction
import me.him188.ani.app.ui.foundation.apple.AppleSelectionToolbarDefaults
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.CoverAspect
import me.him188.ani.app.ui.foundation.apple.episodeTileLabel
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.navigation.BackHandler
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_episode_download_failed
import me.him188.ani.app.ui.lang.cache_episode_pause_download
import me.him188.ani.app.ui.lang.cache_episode_resume_download
import me.him188.ani.app.ui.lang.cache_episode_status_paused
import me.him188.ani.app.ui.lang.cache_filter_status_finished
import me.him188.ani.app.ui.lang.cache_management_delete_selected
import me.him188.ani.app.ui.lang.cache_management_deselect_all
import me.him188.ani.app.ui.lang.cache_management_done
import me.him188.ani.app.ui.lang.cache_management_pause_all
import me.him188.ani.app.ui.lang.cache_management_pause_selected
import me.him188.ani.app.ui.lang.cache_management_play
import me.him188.ani.app.ui.lang.cache_management_resume_all
import me.him188.ani.app.ui.lang.cache_management_resume_selected
import me.him188.ani.app.ui.lang.cache_management_select
import me.him188.ani.app.ui.lang.cache_management_select_all_action
import me.him188.ani.app.ui.lang.cache_management_selected_count
import me.him188.ani.app.ui.lang.cache_subject_cache
import me.him188.ani.app.ui.lang.cache_subject_cancel
import me.him188.ani.app.ui.lang.cache_subject_in_progress
import me.him188.ani.app.ui.lang.cache_subject_not_downloaded
import me.him188.ani.app.ui.lang.downloads_empty
import me.him188.ani.app.ui.lang.downloads_load_failed
import me.him188.ani.app.ui.lang.settings_mediasource_retry
import me.him188.ani.app.ui.mediafetch.MediaSourceInfoProvider
import me.him188.ani.datasources.api.EpisodeSort
import me.him188.ani.datasources.api.topic.isDoneOrDropped
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版单番快取页 (03 §4.3). iOS 推入页, 由快取页点某部番进入.
 *
 * 从上到下: 返回钮与「选择」; 番剧资讯 (封面、季度与话数、完成摘要); 「进行中」与「已完成」两组, 尚未下载的剧集接在已完成之后.
 * 点下载推入单集下载详情; 长按或点「选择」进入选择模式 (05 D2), 底部浮出继续、暂停、删除.
 *
 * @param onOpenDetail 打开单集下载详情 ([me.him188.ani.app.navigation.NavRoutes.CacheDetail]).
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleSubjectDownloadsScreen(
    vm: SubjectDownloadsViewModel,
    onPlay: (DownloadItem) -> Unit,
    onOpenDetail: (DownloadItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    SubjectDownloadsHost(vm.presenter) { state, actions, sourceInfo ->
        AppleSubjectDownloadsLayout(
            state,
            actions,
            sourceInfo,
            onPlay = onPlay,
            onOpenDetail = onOpenDetail,
            onBack = onBack,
            modifier = modifier,
            windowInsets = windowInsets,
            contentPadding = contentPadding,
        )
    }
}

/**
 * [AppleSubjectDownloadsScreen] 的版面. 选择状态由本函数持有.
 */
@Composable
internal fun AppleSubjectDownloadsLayout(
    state: SubjectDownloadsUiState,
    actions: SubjectDownloadActions,
    sourceInfoProvider: MediaSourceInfoProvider,
    onPlay: (DownloadItem) -> Unit,
    onOpenDetail: (DownloadItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
    selection: DownloadSelectionState = rememberDownloadSelectionState(),
) {
    val sections = remember(state.items) { state.appleSections() }
    val summary = remember(state.downloads, state.totalEpisodes) { state.appleSummary() }
    val selected = state.downloads.filter { it.id in selection.selectedIds }
    val allSelected = state.downloads.isNotEmpty() && selected.size == state.downloads.size
    val play = downloadPlayHandler(onPlay)

    // 下载列表变化时, 选中项只保留仍存在的下载; 全部消失 (例如删除后) 时离开选择模式. 同既有单番页.
    LaunchedEffect(state.downloads, state.downloadsLoading) {
        if (!state.downloadsLoading && !state.downloadsFailed && selection.inSelection) {
            val validIds = state.downloads.mapTo(hashSetOf()) { it.id }
            val remaining = selection.selectedIds.intersect(validIds)
            if (remaining.isEmpty() && selection.selectedIds.isNotEmpty()) selection.clear()
            else selection.overrideSelected(remaining)
        }
    }
    BackHandler(selection.inSelection) { selection.clear() }

    var pendingDeleteIds by remember { mutableStateOf<Set<String>?>(null) }
    pendingDeleteIds?.let { ids ->
        AppleDeleteDownloadsDialog(
            onDismiss = { pendingDeleteIds = null },
            confirmEnabled = state.downloads.none { it.id in ids && it.isBusy },
            onConfirm = {
                actions.delete(ids)
                pendingDeleteIds = null
            },
        )
    }

    Box(modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = contentPadding) {
            item(key = "top-bar") {
                TopBar(
                    inSelection = selection.inSelection,
                    selectedCount = selected.size,
                    allSelected = allSelected,
                    hasDownloads = state.downloads.isNotEmpty(),
                    onBack = onBack,
                    onSelect = { selection.enterSelectionWith(emptySet()) },
                    onToggleSelectAll = {
                        selection.overrideSelected(
                            if (allSelected) emptySet() else state.downloads.mapTo(hashSetOf()) { it.id },
                        )
                    },
                    onDone = { selection.clear() },
                    windowInsets = windowInsets,
                )
            }
            item(key = "header") {
                SubjectHeader(state, summary)
            }
            statusItems(state, actions)

            if (sections.active.isNotEmpty()) {
                item(key = "active-title") {
                    SectionTitle(
                        stringResource(Lang.cache_subject_in_progress),
                        count = sections.active.size,
                        Modifier.testTag(AppleSubjectDownloadsTestTags.ActiveTitle),
                    ) {
                        val bulk = sections.active.appleBulkAction()
                        if (!selection.inSelection && bulk != null) {
                            val enabled = sections.active.none { it.isBusy }
                            when (bulk) {
                                AppleBulkAction.PauseAll -> AppleTextButton(
                                    actions.pauseAll,
                                    Modifier.testTag(AppleSubjectDownloadsTestTags.PauseAll),
                                    enabled = enabled,
                                ) { Text(stringResource(Lang.cache_management_pause_all)) }

                                AppleBulkAction.ResumeAll -> AppleTextButton(
                                    actions.resumeAll,
                                    Modifier.testTag(AppleSubjectDownloadsTestTags.ResumeAll),
                                    enabled = enabled,
                                ) { Text(stringResource(Lang.cache_management_resume_all)) }
                            }
                        }
                    }
                }
                downloadRows(sections.active, "active", state, sourceInfoProvider, selection, onOpenDetail, play, actions)
            }

            // 尚未下载的剧集接在已完成之后 (03 §4.3), 同一个分组标题.
            if (sections.finished.isNotEmpty() || sections.notDownloaded.isNotEmpty()) {
                item(key = "finished-title") {
                    SectionTitle(
                        stringResource(Lang.cache_filter_status_finished),
                        count = sections.finished.size,
                        Modifier.testTag(AppleSubjectDownloadsTestTags.FinishedTitle),
                    ) {
                        sections.finished.totalSizeOrNull()?.let {
                            Text(
                                renderFileSize(it),
                                Modifier.padding(end = 4.dp),
                                style = AniTextStyles.footnote.copy(fontFeatureSettings = "tnum"),
                                color = MaterialTheme.colorScheme.secondary,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
            // 已完成与尚未下载的剧集在同一张卡片里.
            val cardSize = sections.finished.size + sections.notDownloaded.size
            downloadRows(
                sections.finished, "finished", state, sourceInfoProvider, selection, onOpenDetail, play, actions,
                cardSize = cardSize,
            )
            sections.notDownloaded.forEachIndexed { i, episode ->
                val index = sections.finished.size + i
                item(key = "episode-${episode.episodeId}") {
                    CardCell(index, cardSize) {
                        EpisodeRow(
                            episode,
                            selectionMode = selection.inSelection,
                            enabled = !(state.request.busy && episode.episodeId !in state.request.episodeIds),
                            busy = state.request.busy && episode.episodeId in state.request.episodeIds,
                            canCancel = state.request.canCancel,
                            showDivider = index > 0,
                            onDownload = { actions.download(episode.episodeId) },
                            onCancel = actions.cancelRequest,
                        )
                    }
                }
            }

            if (selection.inSelection) {
                // 让最后一列可以滚到选择工具栏上方.
                item(key = "toolbar-space") { Spacer(Modifier.height(AppleSelectionToolbarDefaults.Height + 12.dp)) }
            }
        }

        if (selection.inSelection) {
            val layoutDirection = LocalLayoutDirection.current
            AppleSelectionToolbar(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = contentPadding.calculateStartPadding(layoutDirection) + 16.dp,
                        end = contentPadding.calculateEndPadding(layoutDirection) + 16.dp,
                        bottom = contentPadding.calculateBottomPadding() + 12.dp,
                    )
                    .testTag(AppleSubjectDownloadsTestTags.Toolbar),
            ) {
                val ids = selected.mapTo(hashSetOf()) { it.id }
                AppleSelectionToolbarAction(
                    stringResource(Lang.cache_management_resume_selected),
                    enabled = selected.none { it.isBusy } && selected.any { it.isPaused && !it.isFinished },
                    onClick = { actions.resume(ids) },
                    Modifier.testTag(AppleSubjectDownloadsTestTags.ResumeSelected),
                )
                AppleSelectionToolbarAction(
                    stringResource(Lang.cache_management_pause_selected),
                    enabled = selected.none { it.isBusy } && selected.any { !it.isPaused && !it.isFinished && !it.isFailed },
                    onClick = { actions.pause(ids) },
                    Modifier.testTag(AppleSubjectDownloadsTestTags.PauseSelected),
                )
                AppleSelectionToolbarAction(
                    stringResource(Lang.cache_management_delete_selected),
                    enabled = selected.isNotEmpty() && selected.none { it.isBusy },
                    onClick = { pendingDeleteIds = ids },
                    Modifier.testTag(AppleSubjectDownloadsTestTags.DeleteSelected),
                )
            }
        }
    }
}

/**
 * 加载中、加载失败 (可重试) 与没有剧集时的提示, 文案同既有单番页.
 */
private fun LazyListScope.statusItems(state: SubjectDownloadsUiState, actions: SubjectDownloadActions) {
    if (state.episodesFailed || state.downloadsFailed) {
        item(key = "load-error") {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(Lang.downloads_load_failed),
                    Modifier.weight(1f),
                    style = AniTextStyles.callout,
                    color = MaterialTheme.colorScheme.secondary,
                )
                AppleTextButton(actions.reload) { Text(stringResource(Lang.settings_mediasource_retry)) }
            }
        }
    }
    if (state.episodesLoading || state.downloadsLoading) {
        item(key = "loading") {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(Modifier.size(24.dp).testTag(SubjectDownloadsTestTags.LOADING))
            }
        }
    } else if (state.items.isEmpty() && !state.episodesFailed && !state.downloadsFailed) {
        item(key = "empty") {
            Text(
                stringResource(Lang.downloads_empty),
                Modifier.padding(horizontal = 20.dp, vertical = 24.dp),
                style = AniTextStyles.callout,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

/**
 * 一组下载的列, 放在 C5 卡片里. [cardSize] 为卡片的总列数 (已完成一组之后还接着尚未下载的剧集).
 */
private fun LazyListScope.downloadRows(
    downloads: List<DownloadItem>,
    keyPrefix: String,
    state: SubjectDownloadsUiState,
    sourceInfoProvider: MediaSourceInfoProvider,
    selection: DownloadSelectionState,
    onOpenDetail: (DownloadItem) -> Unit,
    onPlay: (DownloadItem) -> Unit,
    actions: SubjectDownloadActions,
    cardSize: Int = downloads.size,
) {
    downloads.forEachIndexed { index, download ->
        item(key = "$keyPrefix-${download.id}") {
            CardCell(index, cardSize) {
                DownloadRow(
                    download,
                    sourceName = download.mediaSourceId?.let { sourceInfoProvider.rememberMediaSourceInfo(it).value?.displayName },
                    watchStatus = if (download.isFinished) {
                        downloadWatchStatusText(download, state.episodeCollectionTypes[download.episodeId])
                    } else {
                        null
                    },
                    selectionMode = selection.inSelection,
                    selected = download.id in selection.selectedIds,
                    showDivider = index > 0,
                    onClick = {
                        if (selection.inSelection) selection.toggleSelection(download.id) else onOpenDetail(download)
                    },
                    onLongClick = {
                        if (!selection.inSelection) selection.enterSelectionWith(selection.selectedIds + download.id)
                    },
                    onPlay = { onPlay(download) },
                    onPause = { actions.pause(setOf(download.id)) },
                    onResume = { actions.resume(setOf(download.id)) },
                )
            }
        }
    }
}

/**
 * C5 卡片的一列: 页面左右边距 16, card 底; 第一列圆上角, 最后一列圆下角.
 */
@Composable
private fun CardCell(index: Int, count: Int, content: @Composable () -> Unit) {
    Box(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(listCardShape(index, count))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        content()
    }
}

private fun listCardShape(index: Int, count: Int): Shape {
    val top = if (index == 0) 16.dp else 0.dp
    val bottom = if (index == count - 1) 16.dp else 0.dp
    if (top == 0.dp && bottom == 0.dp) return RectangleShape
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

/**
 * 顶部一列. 一般为返回圆钮与「选择」; 选择模式为左「全选 / 取消全选」、中「已选 N」、右「完成」.
 */
@Composable
private fun TopBar(
    inSelection: Boolean,
    selectedCount: Int,
    allSelected: Boolean,
    hasDownloads: Boolean,
    onBack: () -> Unit,
    onSelect: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onDone: () -> Unit,
    windowInsets: WindowInsets,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
            .padding(top = 10.dp)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (inSelection) {
                AppleTextButton(onToggleSelectAll, Modifier.testTag(AppleSubjectDownloadsTestTags.SelectAll)) {
                    Text(
                        stringResource(if (allSelected) Lang.cache_management_deselect_all else Lang.cache_management_select_all_action),
                        maxLines = 1,
                    )
                }
            } else {
                AppleCircleButton(onBack, AppleIcons.back(), contentDescription = null)
            }
        }
        if (inSelection) {
            Text(
                stringResource(Lang.cache_management_selected_count, selectedCount),
                Modifier.semantics { heading() },
                style = AniTextStyles.headline,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
            if (inSelection) {
                AppleTextButton(onDone, Modifier.testTag(AppleSubjectDownloadsTestTags.Done)) {
                    Text(stringResource(Lang.cache_management_done), fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            } else {
                AppleTextButton(
                    onSelect,
                    Modifier.testTag(AppleSubjectDownloadsTestTags.Select),
                    enabled = hasDownloads,
                ) {
                    Text(stringResource(Lang.cache_management_select), maxLines = 1)
                }
            }
        }
    }
}

/**
 * 番剧资讯: 封面 72×96 圆角 10; 番名 22/28 W800; 季度与话数 `footnote` label2; 完成摘要 `footnote` labelSoft.
 */
@Composable
private fun SubjectHeader(state: SubjectDownloadsUiState, summary: AppleSubjectDownloadsSummary) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 12.dp, end = 16.dp)
            .testTag(AppleSubjectDownloadsTestTags.Header),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppleCover(CoverAspect.Portrait3x4, state.title.orEmpty(), Modifier.width(72.dp), cornerRadius = 10.dp) {
            AsyncImage(state.coverUrl, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                state.title.orEmpty(),
                Modifier.semantics { heading() },
                style = AniTextStyles.title.copy(fontSize = 22.sp, lineHeight = 28.sp),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            subjectAiringLine(state)?.let {
                Text(it, style = AniTextStyles.footnote, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
            }
            Text(
                subjectSummaryLine(summary),
                style = AniTextStyles.footnote.copy(fontFeatureSettings = "tnum"),
                color = AniColors.labelSoft,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 分组标题: 标题 `title` (20/26 W700) + 数量 `callout` W500 label2, 右侧 [trailing]; 与上方相隔 24、与卡片相隔 12.
 */
@Composable
private fun SectionTitle(
    title: String,
    count: Int,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            // 行高 44 (文字按钮的点击区), 文字上下各多 9, 所以上下距各减 9.
            .padding(start = 20.dp, top = 15.dp, end = 16.dp, bottom = 3.dp)
            .heightIn(min = 44.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                title,
                Modifier
                    .alignByBaseline()
                    .semantics { heading() },
                style = AniTextStyles.title,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                count.toString(),
                Modifier.alignByBaseline(),
                style = AniTextStyles.callout.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
            )
        }
        trailing()
    }
}

/**
 * 集数方块: 44×44、圆角 12、fill 底, labelSoft `body` W600.
 */
@Composable
private fun EpisodeNumberTile(sort: EpisodeSort) {
    Box(
        Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            episodeTileLabel(sort),
            style = AniTextStyles.body.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.SemiBold,
            color = AniColors.labelSoft,
            maxLines = 1,
        )
    }
}

/**
 * 下载列: 内距 `12 12 12 16`; 集数方块; 集名 `body` W600; 摘要 `footnote` label2 (大小 · 来源, 已完成再接观看状态);
 * 失败时加一行 error「下载失败」; 下载中或暂停时加进度条与速度 (暂停为「已暂停 N%」, 进度条改 label2). 分隔线从 72 开始 (选择模式从 106).
 *
 * 右侧 36 圆钮 (同 03 §3.4): 下载中为暂停 (fill 底), 已暂停为继续 (tint 底、accent 图标), 已完成为播放 (fill 底), 失败为「›」.
 * 选择模式下行首为勾选圆圈, 不显示右侧按钮.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DownloadRow(
    download: DownloadItem,
    sourceName: String?,
    watchStatus: String?,
    selectionMode: Boolean,
    selected: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(Modifier.testTag(AppleSubjectDownloadsTestTags.row(download.id))) {
        Row(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onLongClick = onLongClick, onClick = onClick)
                .then(if (selectionMode) Modifier.semantics { this.selected = selected } else Modifier)
                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectionMode) {
                AppleSelectionCircle(selected)
            }
            EpisodeNumberTile(download.sort)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    downloadEpisodeTitle(download.sort, download.displayName),
                    style = AniTextStyles.body,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val meta = listOfNotNull(
                    if (download.isFinished) download.sizeText else download.detailedSizeText,
                    sourceName,
                    watchStatus,
                ).joinToString(" · ")
                if (meta.isNotEmpty()) {
                    Text(
                        meta,
                        style = AniTextStyles.footnote.copy(fontFeatureSettings = "tnum"),
                        color = colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (download.isFailed) {
                    Text(
                        stringResource(Lang.cache_episode_download_failed),
                        style = AniTextStyles.footnote,
                        fontWeight = FontWeight.SemiBold,
                        color = colorScheme.error,
                        maxLines = 1,
                    )
                }
                if (download.status == DownloadStatus.IN_PROGRESS || download.isPaused) {
                    val progress = download.progress.getOrZero()
                    Row(
                        Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppleProgressBar({ progress }, Modifier.weight(1f), muted = download.isPaused)
                        Text(
                            if (download.isPaused) {
                                stringResource(Lang.cache_episode_status_paused) + " " + "${(progress * 100).toInt()}%"
                            } else {
                                download.speedText.orEmpty()
                            },
                            Modifier.widthIn(min = 64.dp),
                            style = AniTextStyles.caption.copy(fontFeatureSettings = "tnum"),
                            color = colorScheme.secondary,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                        )
                    }
                }
            }
            if (!selectionMode) {
                when {
                    download.isFinished -> RowActionButton(
                        AppleIcons.playFilled(),
                        stringResource(Lang.cache_management_play),
                        containerColor = colorScheme.surfaceContainerHighest,
                        iconColor = colorScheme.onSurface,
                        onClick = onPlay,
                        Modifier.testTag(AppleSubjectDownloadsTestTags.play(download.id)),
                    )

                    download.isPaused -> RowActionButton(
                        AppleIcons.playFilled(),
                        stringResource(Lang.cache_episode_resume_download),
                        containerColor = colorScheme.secondaryContainer,
                        iconColor = colorScheme.primary,
                        onClick = onResume,
                        Modifier.testTag(AppleSubjectDownloadsTestTags.resume(download.id)),
                        enabled = !download.isBusy,
                    )

                    download.isFailed -> Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        Icon(AppleIcons.chevronRight(), contentDescription = null, Modifier.size(16.dp), tint = colorScheme.secondary)
                    }

                    else -> RowActionButton(
                        AppleIcons.pauseFilled(),
                        stringResource(Lang.cache_episode_pause_download),
                        containerColor = colorScheme.surfaceContainerHighest,
                        iconColor = colorScheme.onSurface,
                        onClick = onPause,
                        Modifier.testTag(AppleSubjectDownloadsTestTags.pause(download.id)),
                        enabled = !download.isBusy,
                    )
                }
            }
        }
        if (showDivider) {
            // 选择模式下内容右移勾选圆圈 22 + 间距 12.
            AppleDivider(Modifier.align(Alignment.TopStart), startIndent = if (selectionMode) 106.dp else 72.dp)
        }
    }
}

/**
 * 尚未下载的剧集: 版式同下载列; 摘要为「尚未下载」(看过或抛弃时接在后面), 右侧为现有的下载动作.
 * 选取中的剧集显示进度圈与取消钮; 其他剧集的请求正在准备时按钮停用 (同既有单番页).
 * 选择模式下变淡、不能选, 不显示右侧按钮; 行首留出勾选圆圈的位置, 与下载列对齐.
 */
@Composable
private fun EpisodeRow(
    episode: EpisodeDownloadItem,
    selectionMode: Boolean,
    enabled: Boolean,
    busy: Boolean,
    canCancel: Boolean,
    showDivider: Boolean,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val useOriginalTitle = LocalSubjectAppearanceSettings.current.useOriginalTitle
    Box(
        Modifier
            .testTag(AppleSubjectDownloadsTestTags.episode(episode.episodeId))
            .alpha(if (selectionMode) AppleComponentDefaults.DisabledAlpha else 1f),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (selectionMode) {
                Spacer(Modifier.width(22.dp))
            }
            EpisodeNumberTile(episode.sort)
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    downloadEpisodeTitle(episode.sort, if (useOriginalTitle) episode.originalTitle else episode.title),
                    style = AniTextStyles.body,
                    fontWeight = FontWeight.SemiBold,
                    // 看过、抛弃与尚未放送的剧集变淡, 同既有剧集行.
                    color = if (episode.watchStatus.isDoneOrDropped() || !episode.hasPublished) colorScheme.secondary else colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    listOfNotNull(stringResource(Lang.cache_subject_not_downloaded), episodeWatchStatusText(episode.watchStatus))
                        .joinToString(" · "),
                    style = AniTextStyles.footnote,
                    color = colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (!selectionMode) {
                if (busy) {
                    Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    }
                    if (canCancel) {
                        RowActionButton(
                            AppleIcons.close(),
                            stringResource(Lang.cache_subject_cancel),
                            containerColor = colorScheme.surfaceContainerHighest,
                            iconColor = colorScheme.onSurface,
                            onClick = onCancel,
                            Modifier.testTag(AppleSubjectDownloadsTestTags.cancel(episode.episodeId)),
                        )
                    }
                } else {
                    RowActionButton(
                        AppleIcons.download(2.2f),
                        stringResource(Lang.cache_subject_cache),
                        containerColor = colorScheme.surfaceContainerHighest,
                        iconColor = colorScheme.primary,
                        onClick = onDownload,
                        Modifier.testTag(AppleSubjectDownloadsTestTags.download(episode.episodeId)),
                        enabled = enabled,
                        iconSize = 16.dp,
                    )
                }
            }
        }
        if (showDivider) {
            AppleDivider(Modifier.align(Alignment.TopStart), startIndent = if (selectionMode) 106.dp else 72.dp)
        }
    }
}

/**
 * 行右侧的 36 圆形按钮, 图标默认 14.
 */
@Composable
private fun RowActionButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    iconSize: Dp = 14.dp,
) {
    Box(
        modifier
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
            .size(36.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, Modifier.size(iconSize), tint = iconColor)
    }
}

/**
 * 现有的删除确认. 对话框打开时返回 (Esc) 先关闭对话框, 不离开选择模式.
 */
@Composable
internal fun AppleDeleteDownloadsDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmEnabled: Boolean = true,
) {
    BackHandler(onBack = onDismiss)
    DeleteActionDialog(onDismiss = onDismiss, onConfirm = onConfirm, confirmEnabled = confirmEnabled)
}

internal object AppleSubjectDownloadsTestTags {
    const val Header = "subject-downloads-header"
    const val Select = "subject-downloads-select"
    const val SelectAll = "subject-downloads-select-all"
    const val Done = "subject-downloads-done"
    const val ActiveTitle = "subject-downloads-active-title"
    const val FinishedTitle = "subject-downloads-finished-title"
    const val PauseAll = "subject-downloads-pause-all"
    const val ResumeAll = "subject-downloads-resume-all"
    const val Toolbar = "subject-downloads-toolbar"
    const val ResumeSelected = "subject-downloads-resume-selected"
    const val PauseSelected = "subject-downloads-pause-selected"
    const val DeleteSelected = "subject-downloads-delete-selected"
    fun row(id: String) = "subject-downloads-row-$id"
    fun play(id: String) = "subject-downloads-play-$id"
    fun pause(id: String) = "subject-downloads-pause-$id"
    fun resume(id: String) = "subject-downloads-resume-$id"
    fun episode(episodeId: Int) = "subject-downloads-episode-$episodeId"
    fun download(episodeId: Int) = "subject-downloads-download-$episodeId"
    fun cancel(episodeId: Int) = "subject-downloads-cancel-$episodeId"
}
