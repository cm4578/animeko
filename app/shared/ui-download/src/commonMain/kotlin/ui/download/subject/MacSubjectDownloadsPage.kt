/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.download.subject

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.features.getComponentAccessors
import me.him188.ani.app.platform.files
import me.him188.ani.app.tools.getOrZero
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.components.DownloadSelectionState
import me.him188.ani.app.ui.download.components.DownloadStatus
import me.him188.ani.app.ui.download.components.rememberDownloadSelectionState
import me.him188.ani.app.ui.download.components.renderFileSize
import me.him188.ani.app.ui.download.details.DownloadControlAction
import me.him188.ani.app.ui.download.details.MacMediaCacheDetailsInspector
import me.him188.ani.app.ui.download.details.MediaCacheDetailsPageViewModel
import me.him188.ani.app.ui.download.details.MediaCacheDetailsScreenState
import me.him188.ani.app.ui.download.details.controlAction
import me.him188.ani.app.ui.download.details.showsPlay
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleComponentDefaults
import me.him188.ani.app.ui.foundation.apple.AppleContextMenu
import me.him188.ani.app.ui.foundation.apple.AppleCover
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleProgressBar
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.CoverAspect
import me.him188.ani.app.ui.foundation.apple.MacKeyboardShortcut
import me.him188.ani.app.ui.foundation.apple.MacPageToolbar
import me.him188.ani.app.ui.foundation.apple.MacTableColumn
import me.him188.ani.app.ui.foundation.apple.MacTableColumns
import me.him188.ani.app.ui.foundation.apple.MacTableGroupRow
import me.him188.ani.app.ui.foundation.apple.MacTableHeader
import me.him188.ani.app.ui.foundation.apple.MacTableHeaderCell
import me.him188.ani.app.ui.foundation.apple.MacTableHint
import me.him188.ani.app.ui.foundation.apple.MacTableRow
import me.him188.ani.app.ui.foundation.apple.MacToolbarDefaults
import me.him188.ani.app.ui.foundation.apple.MacToolbarDivider
import me.him188.ani.app.ui.foundation.apple.MacToolbarIconButton
import me.him188.ani.app.ui.foundation.apple.MacToolbarTextButton
import me.him188.ani.app.ui.foundation.apple.PopoverMenuItem
import me.him188.ani.app.ui.foundation.apple.episodeTileLabel
import me.him188.ani.app.ui.foundation.apple.onSecondaryClick
import me.him188.ani.app.ui.foundation.navigation.BackHandler
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_details_open_file_failed
import me.him188.ani.app.ui.lang.cache_episode_download_failed
import me.him188.ani.app.ui.lang.cache_episode_downloading
import me.him188.ani.app.ui.lang.cache_episode_pause_download
import me.him188.ani.app.ui.lang.cache_episode_resume_download
import me.him188.ani.app.ui.lang.cache_episode_status_paused
import me.him188.ani.app.ui.lang.cache_filter_status_finished
import me.him188.ani.app.ui.lang.cache_management_delete_download
import me.him188.ani.app.ui.lang.cache_management_delete_selected
import me.him188.ani.app.ui.lang.cache_management_deselect_all
import me.him188.ani.app.ui.lang.cache_management_done
import me.him188.ani.app.ui.lang.cache_management_open_subject
import me.him188.ani.app.ui.lang.cache_management_pause_all
import me.him188.ani.app.ui.lang.cache_management_pause_selected
import me.him188.ani.app.ui.lang.cache_management_play
import me.him188.ani.app.ui.lang.cache_management_resume_all
import me.him188.ani.app.ui.lang.cache_management_resume_selected
import me.him188.ani.app.ui.lang.cache_management_select
import me.him188.ani.app.ui.lang.cache_management_select_all_action
import me.him188.ani.app.ui.lang.cache_management_selected_count
import me.him188.ani.app.ui.lang.cache_management_show_in_finder
import me.him188.ani.app.ui.lang.cache_management_view_details
import me.him188.ani.app.ui.lang.cache_subject_cache
import me.him188.ani.app.ui.lang.cache_subject_cancel
import me.him188.ani.app.ui.lang.cache_subject_in_progress
import me.him188.ani.app.ui.lang.cache_subject_not_downloaded
import me.him188.ani.app.ui.lang.cache_subject_table_hint
import me.him188.ani.app.ui.lang.cache_table_episode
import me.him188.ani.app.ui.lang.cache_table_size
import me.him188.ani.app.ui.lang.cache_table_speed
import me.him188.ani.app.ui.lang.cache_table_status
import me.him188.ani.app.ui.lang.collection_table_name
import me.him188.ani.app.ui.lang.collection_table_progress
import me.him188.ani.app.ui.lang.downloads_empty
import me.him188.ani.app.ui.lang.downloads_load_failed
import me.him188.ani.app.ui.lang.settings_mediasource_retry
import me.him188.ani.app.ui.mediafetch.MediaSourceInfoProvider
import me.him188.ani.datasources.api.topic.isDoneOrDropped
import me.him188.ani.utils.io.absolutePath
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 版单番快取页与检阅器 (04 §4.3). 由快取表格按两下推入.
 *
 * 工具栏右侧: accent「前往条目详情」、「选择」(05 D2)、「全部暂停 / 全部继续」、分隔线、检阅器开关 (⌘I).
 * 内容: 头部 (封面、季度与话数、完成摘要); C19 表格, 分「进行中」与「已完成」两组, 尚未下载的剧集接在已完成之后; 表下操作提示.
 * 右侧为 C21 检阅器, 显示所选一集的下载详情 (05 C3), 资料来自 [MediaCacheDetailsPageViewModel].
 *
 * 单击选中一集, 双击播放; 右键菜单 (05 C3) 为暂停或继续、播放、查看下载详情 (⌘I)、前往条目详情、在 Finder 中显示 (05 G9)、删除下载.
 *
 * @param onOpenSubjectDetails 前往条目详情.
 */
@Composable
fun MacSubjectDownloadsScreen(
    vm: SubjectDownloadsViewModel,
    onPlay: (DownloadItem) -> Unit,
    onOpenSubjectDetails: (subjectId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val presenter = vm.presenter
    val configuredDirectory by presenter.cacheDirectory.collectAsStateWithLifecycle(null)
    val context = LocalContext.current
    val fileRevealer = remember(context) { context.getComponentAccessors().fileRevealer }
    val defaultDirectory = remember(context) { context.files.defaultMediaCacheBaseDir.absolutePath }
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current

    suspend fun reveal(path: String) {
        if (fileRevealer?.revealFile(Path(path)) != true) {
            toaster.toast(getString(Lang.cache_details_open_file_failed, path))
        }
    }

    SubjectDownloadsHost(presenter) { state, actions, sourceInfo ->
        MacSubjectDownloadsLayout(
            state,
            actions,
            sourceInfo,
            onPlay = onPlay,
            onOpenSubjectDetails = { onOpenSubjectDetails(presenter.subjectId) },
            onRevealDownload = { download ->
                scope.launch {
                    reveal(presenter.localFilePath(download) ?: configuredDirectory ?: defaultDirectory)
                }
            },
            onRevealFile = { path -> scope.launch { reveal(path.toString()) } },
            detailsState = { cacheId ->
                val detailsVm = viewModel(key = "cache-details-$cacheId") { MediaCacheDetailsPageViewModel(cacheId) }
                detailsVm.screenStateFlow.collectAsStateWithLifecycle().value
            },
            modifier = modifier,
        )
    }
}

/**
 * [MacSubjectDownloadsScreen] 的版面. 选中的一集、检阅器开关与选择模式由本函数持有.
 *
 * @param onRevealDownload 右键菜单的「在 Finder 中显示」: 定位该集的文件, 取不到时定位快取位置.
 * @param onRevealFile 检阅器里本地下载路径的「在 Finder 中显示」.
 * @param detailsState 检阅器显示的一集的详情.
 */
@Composable
internal fun MacSubjectDownloadsLayout(
    state: SubjectDownloadsUiState,
    actions: SubjectDownloadActions,
    sourceInfoProvider: MediaSourceInfoProvider,
    onPlay: (DownloadItem) -> Unit,
    onOpenSubjectDetails: () -> Unit,
    onRevealDownload: (DownloadItem) -> Unit,
    onRevealFile: (Path) -> Unit,
    detailsState: @Composable (cacheId: String) -> MediaCacheDetailsScreenState,
    modifier: Modifier = Modifier,
    selection: DownloadSelectionState = rememberDownloadSelectionState(),
) {
    val sections = remember(state.items) { state.appleSections() }
    val summary = remember(state.downloads, state.totalEpisodes) { state.appleSummary() }
    val play = downloadPlayHandler(onPlay)
    val selected = state.downloads.filter { it.id in selection.selectedIds }
    val allSelected = state.downloads.isNotEmpty() && selected.size == state.downloads.size

    // 单击选中的一集, 也是检阅器显示的一集; 选择模式下为最后点到的一集.
    var focusedId by rememberSaveable { mutableStateOf<String?>(null) }
    var inspectorOpen by rememberSaveable { mutableStateOf(false) }
    val focused = state.downloads.firstOrNull { it.id == focusedId }
    // 检阅器显示的一集; 检阅器关闭时为 `null`.
    val inspected = focused?.takeIf { inspectorOpen }
    val inspectorVisible = inspected != null

    // 检阅器显示的一集不在了 (例如已删除) 时关闭检阅器.
    LaunchedEffect(focused == null, state.downloadsLoading) {
        if (focused == null && !state.downloadsLoading) inspectorOpen = false
    }
    // 下载列表变化时, 选中项只保留仍存在的下载; 全部消失时离开选择模式. 同既有单番页.
    LaunchedEffect(state.downloads, state.downloadsLoading) {
        if (!state.downloadsLoading && !state.downloadsFailed && selection.inSelection) {
            val validIds = state.downloads.mapTo(hashSetOf()) { it.id }
            val remaining = selection.selectedIds.intersect(validIds)
            if (remaining.isEmpty() && selection.selectedIds.isNotEmpty()) selection.clear()
            else selection.overrideSelected(remaining)
        }
    }
    BackHandler(selection.inSelection) { selection.clear() }

    fun showInspector(download: DownloadItem) {
        focusedId = download.id
        inspectorOpen = true
    }

    val toggleInspector: () -> Unit = {
        if (inspectorVisible) {
            inspectorOpen = false
        } else {
            (focused ?: sections.downloads.firstOrNull())?.let(::showInspector)
        }
    }
    MacKeyboardShortcut(Key.I, enabled = state.downloads.isNotEmpty(), onTrigger = toggleInspector)

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

    val columns = if (inspectorVisible) MacSubjectDownloadsCompactColumns else MacSubjectDownloadsColumns
    val entries = remember(sections) { sections.tableEntries() }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        MacPageToolbar {
            if (selection.inSelection) {
                Text(
                    stringResource(Lang.cache_management_selected_count, selected.size),
                    Modifier.padding(end = 8.dp),
                    style = AniTextStyles.macCallout,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                )
                MacToolbarTextButton(
                    stringResource(if (allSelected) Lang.cache_management_deselect_all else Lang.cache_management_select_all_action),
                    onClick = {
                        selection.overrideSelected(
                            if (allSelected) emptySet() else state.downloads.mapTo(hashSetOf()) { it.id },
                        )
                    },
                    Modifier.testTag(MacSubjectDownloadsTestTags.SelectAll),
                )
                val ids = selected.mapTo(hashSetOf()) { it.id }
                MacToolbarTextButton(
                    stringResource(Lang.cache_management_resume_selected),
                    onClick = { actions.resume(ids) },
                    enabled = selected.none { it.isBusy } && selected.any { it.isPaused && !it.isFinished },
                )
                MacToolbarTextButton(
                    stringResource(Lang.cache_management_pause_selected),
                    onClick = { actions.pause(ids) },
                    enabled = selected.none { it.isBusy } && selected.any { !it.isPaused && !it.isFinished && !it.isFailed },
                )
                MacToolbarTextButton(
                    stringResource(Lang.cache_management_delete_selected),
                    onClick = { pendingDeleteIds = ids },
                    Modifier.testTag(MacSubjectDownloadsTestTags.DeleteSelected),
                    enabled = selected.isNotEmpty() && selected.none { it.isBusy },
                )
                MacToolbarTextButton(
                    stringResource(Lang.cache_management_done),
                    onClick = { selection.clear() },
                    Modifier.testTag(MacSubjectDownloadsTestTags.Done),
                )
            } else {
                AppleTextButton(onOpenSubjectDetails, Modifier.testTag(MacSubjectDownloadsTestTags.OpenSubject)) {
                    Text(stringResource(Lang.cache_management_open_subject), maxLines = 1)
                }
                MacToolbarTextButton(
                    stringResource(Lang.cache_management_select),
                    onClick = { selection.enterSelectionWith(emptySet()) },
                    Modifier.testTag(MacSubjectDownloadsTestTags.Select),
                    enabled = state.downloads.isNotEmpty(),
                )
                val bulk = state.downloads.appleBulkAction()
                MacToolbarTextButton(
                    stringResource(if (bulk == AppleBulkAction.ResumeAll) Lang.cache_management_resume_all else Lang.cache_management_pause_all),
                    onClick = { if (bulk == AppleBulkAction.ResumeAll) actions.resumeAll() else actions.pauseAll() },
                    Modifier.testTag(MacSubjectDownloadsTestTags.PauseAll),
                    enabled = bulk != null && state.downloads.none { it.isBusy },
                )
            }
            MacToolbarDivider()
            MacToolbarIconButton(
                toggleInspector,
                AppleIcons.inspector(),
                stringResource(Lang.cache_management_view_details),
                Modifier.testTag(MacSubjectDownloadsTestTags.InspectorToggle),
                checked = inspectorVisible,
                enabled = state.downloads.isNotEmpty(),
                width = MacToolbarDefaults.SegmentWidth,
            )
        }
        if (inspectorVisible) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
        }
        Row(Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                Modifier.weight(1f).fillMaxHeight(),
                contentPadding = PaddingValues(start = 32.dp, top = 8.dp, end = 32.dp, bottom = 32.dp),
            ) {
                item(key = "header") { Header(state, summary) }
                statusItems(state, actions)
                itemsIndexed(entries, key = { _, entry -> entry.key }) { index, entry ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(tableShape(top = index == 0, bottom = index == entries.lastIndex))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        when (entry) {
                            TableEntry.Header -> MacTableHeader(columns) {
                                MacTableHeaderCell(stringResource(Lang.cache_table_episode))
                                MacTableHeaderCell(stringResource(Lang.collection_table_name))
                                MacTableHeaderCell(stringResource(Lang.collection_table_progress))
                                if (!inspectorVisible) {
                                    MacTableHeaderCell(stringResource(Lang.cache_table_speed))
                                    MacTableHeaderCell(stringResource(Lang.cache_table_size))
                                }
                                MacTableHeaderCell(stringResource(Lang.cache_table_status), Modifier.padding(start = StatusInset))
                                Box {}
                            }

                            is TableEntry.Group -> GroupRow(
                                entry,
                                sections,
                                columns,
                                inSelection = selection.inSelection,
                                onPauseAll = actions.pauseAll,
                                onResumeAll = actions.resumeAll,
                            )

                            is TableEntry.Download -> {
                                val download = entry.download
                                DownloadRowWithMenu(
                                    download,
                                    columns = columns,
                                    compact = inspectorVisible,
                                    sourceName = download.mediaSourceId?.let {
                                        sourceInfoProvider.rememberMediaSourceInfo(it).value?.displayName
                                    },
                                    watchStatus = if (download.isFinished) {
                                        downloadWatchStatusText(download, state.episodeCollectionTypes[download.episodeId])
                                    } else {
                                        null
                                    },
                                    selected = if (selection.inSelection) download.id in selection.selectedIds else focusedId == download.id,
                                    showDivider = entry.showDivider,
                                    menuEnabled = !selection.inSelection,
                                    onClick = {
                                        focusedId = download.id
                                        if (selection.inSelection) selection.toggleSelection(download.id)
                                    },
                                    onDoubleClick = if (selection.inSelection) null else ({ play(download) }),
                                    onSecondaryClick = { focusedId = download.id },
                                    onPlay = { play(download) },
                                    onPause = { actions.pause(setOf(download.id)) },
                                    onResume = { actions.resume(setOf(download.id)) },
                                    onShowDetails = { showInspector(download) },
                                    onOpenSubjectDetails = onOpenSubjectDetails,
                                    onReveal = { onRevealDownload(download) },
                                    onDelete = { pendingDeleteIds = setOf(download.id) },
                                )
                            }

                            is TableEntry.Episode -> EpisodeRow(
                                entry.episode,
                                columns = columns,
                                compact = inspectorVisible,
                                showDivider = entry.showDivider,
                                dimmed = selection.inSelection,
                                enabled = !(state.request.busy && entry.episode.episodeId !in state.request.episodeIds),
                                busy = state.request.busy && entry.episode.episodeId in state.request.episodeIds,
                                canCancel = state.request.canCancel,
                                onDownload = { actions.download(entry.episode.episodeId) },
                                onCancel = actions.cancelRequest,
                            )
                        }
                    }
                }
                if (entries.isNotEmpty()) {
                    item(key = "hint") {
                        MacTableHint(stringResource(Lang.cache_subject_table_hint), Modifier.testTag(MacSubjectDownloadsTestTags.Hint))
                    }
                }
            }
            if (inspected != null) {
                MacMediaCacheDetailsInspector(
                    detailsState(inspected.id),
                    inspected,
                    onPlay = { play(inspected) },
                    onPause = { actions.pause(setOf(inspected.id)) },
                    onResume = { actions.resume(setOf(inspected.id)) },
                    onDelete = { pendingDeleteIds = setOf(inspected.id) },
                    onRevealFile = onRevealFile,
                    onClose = { inspectorOpen = false },
                )
            }
        }
    }
}

/**
 * 状态列与操作列的左外距.
 */
private val StatusInset: Dp = 8.dp

/**
 * 表格列 (检阅器关闭): 集 28 / 名称 (弹性) / 进度 180 / 速度 88 / 大小 72 / 状态 88 / 操作 56, 列间距 16、左右内距 16.
 *
 * 状态列与操作列各有左外距 8 (04 §4.3), 用加宽 8 的列加单元格左内距 [StatusInset] 实现, 所以状态列的右缘不变;
 * 操作列另有右外距 -8, 即右内距为 8, 让操作列与分组行右侧的文字共用中线.
 */
internal val MacSubjectDownloadsColumns = MacTableColumns(
    listOf(
        MacTableColumn(width = 28.dp),
        MacTableColumn(weight = 1f),
        MacTableColumn(width = 180.dp),
        MacTableColumn(width = 88.dp, alignment = Alignment.End),
        MacTableColumn(width = 72.dp, alignment = Alignment.End),
        MacTableColumn(width = 88.dp + StatusInset),
        MacTableColumn(width = 56.dp + StatusInset),
    ),
    horizontalPadding = 16.dp,
    spacing = 16.dp,
    endPadding = 8.dp,
)

/**
 * 检阅器打开时的表格列: 隐藏速度与大小, 进度改为 140; 其余同 [MacSubjectDownloadsColumns].
 */
internal val MacSubjectDownloadsCompactColumns = MacTableColumns(
    listOf(
        MacTableColumn(width = 28.dp),
        MacTableColumn(weight = 1f),
        MacTableColumn(width = 140.dp),
        MacTableColumn(width = 88.dp + StatusInset),
        MacTableColumn(width = 56.dp + StatusInset),
    ),
    horizontalPadding = 16.dp,
    spacing = 16.dp,
    endPadding = 8.dp,
)

/**
 * 表格的一行: 表头、分组行、下载行或尚未下载的剧集. [showDivider] 为行顶的分隔线, 紧接表头或分组行时不画 (它们自带底部分隔线).
 */
@Immutable
private sealed interface TableEntry {
    val key: String

    data object Header : TableEntry {
        override val key: String get() = "table-header"
    }

    /**
     * @param active `true` 为「进行中」, 否则为「已完成」.
     * @param showTopDivider 与上方的行之间画分隔线.
     */
    class Group(val active: Boolean, val showTopDivider: Boolean) : TableEntry {
        override val key: String get() = if (active) "group-active" else "group-finished"
    }

    class Download(val download: DownloadItem, val showDivider: Boolean) : TableEntry {
        override val key: String get() = "download-${download.id}"
    }

    class Episode(val episode: EpisodeDownloadItem, val showDivider: Boolean) : TableEntry {
        override val key: String get() = "episode-${episode.episodeId}"
    }
}

private fun AppleSubjectDownloadSections.tableEntries(): List<TableEntry> {
    if (active.isEmpty() && finished.isEmpty() && notDownloaded.isEmpty()) return emptyList()
    return buildList {
        add(TableEntry.Header)
        if (active.isNotEmpty()) {
            add(TableEntry.Group(active = true, showTopDivider = false))
            active.forEachIndexed { index, download -> add(TableEntry.Download(download, showDivider = index > 0)) }
        }
        // 尚未下载的剧集接在已完成之后 (03 §4.3), 同一个分组.
        if (finished.isNotEmpty() || notDownloaded.isNotEmpty()) {
            add(TableEntry.Group(active = false, showTopDivider = active.isNotEmpty()))
            finished.forEachIndexed { index, download -> add(TableEntry.Download(download, showDivider = index > 0)) }
            notDownloaded.forEachIndexed { index, episode ->
                add(TableEntry.Episode(episode, showDivider = index > 0 || finished.isNotEmpty()))
            }
        }
    }
}

private fun tableShape(top: Boolean, bottom: Boolean): Shape = RoundedCornerShape(
    topStart = if (top) 14.dp else 0.dp,
    topEnd = if (top) 14.dp else 0.dp,
    bottomStart = if (bottom) 14.dp else 0.dp,
    bottomEnd = if (bottom) 14.dp else 0.dp,
)

/**
 * 头部 (内距 8/32/0): 72×96 封面 (圆角 10) + 大标题 + 季度与话数 13 label2 + 完成摘要 13 labelSoft.
 */
@Composable
private fun Header(state: SubjectDownloadsUiState, summary: AppleSubjectDownloadsSummary) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
            .testTag(MacSubjectDownloadsTestTags.Header),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppleCover(CoverAspect.Portrait3x4, state.title.orEmpty(), Modifier.width(72.dp), cornerRadius = 10.dp) {
            AsyncImage(state.coverUrl, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                state.title.orEmpty(),
                Modifier.semantics { heading() },
                style = AniTextStyles.macLargeTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            subjectAiringLine(state)?.let {
                Text(it, style = AniTextStyles.macCallout, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
            }
            Text(
                subjectSummaryLine(summary),
                style = AniTextStyles.macCallout.copy(fontFeatureSettings = "tnum"),
                color = AniColors.labelSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 加载中、加载失败 (可重试) 与没有剧集时的提示, 文案同既有单番页.
 */
private fun LazyListScope.statusItems(state: SubjectDownloadsUiState, actions: SubjectDownloadActions) {
    if (state.episodesFailed || state.downloadsFailed) {
        item(key = "load-error") {
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(Lang.downloads_load_failed),
                    Modifier.weight(1f),
                    style = AniTextStyles.macBody,
                    color = MaterialTheme.colorScheme.secondary,
                )
                AppleTextButton(actions.reload) { Text(stringResource(Lang.settings_mediasource_retry)) }
            }
        }
    }
    if (state.episodesLoading || state.downloadsLoading) {
        item(key = "loading") {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(20.dp).testTag(SubjectDownloadsTestTags.LOADING), strokeWidth = 2.dp)
            }
        }
    } else if (state.items.isEmpty() && !state.episodesFailed && !state.downloadsFailed) {
        item(key = "empty") {
            Text(
                stringResource(Lang.downloads_empty),
                style = AniTextStyles.macBody,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

/**
 * 分组行: 左「进行中 N」/「已完成 N」; 右侧与操作列同宽、同中线, 进行中为 accent「全部暂停 / 全部继续」(选择模式下不显示),
 * 已完成为 label2 总大小.
 */
@Composable
private fun GroupRow(
    group: TableEntry.Group,
    sections: AppleSubjectDownloadSections,
    columns: MacTableColumns,
    inSelection: Boolean,
    onPauseAll: () -> Unit,
    onResumeAll: () -> Unit,
) {
    val downloads = if (group.active) sections.active else sections.finished
    MacTableGroupRow(
        columns,
        stringResource(if (group.active) Lang.cache_subject_in_progress else Lang.cache_filter_status_finished),
        Modifier.testTag(MacSubjectDownloadsTestTags.group(group.active)),
        count = downloads.size.toString(),
        showTopDivider = group.showTopDivider,
    ) {
        Box(Modifier.padding(start = StatusInset).fillMaxWidth(), contentAlignment = Alignment.Center) {
            if (group.active) {
                val bulk = downloads.appleBulkAction()
                if (!inSelection && bulk != null) {
                    Text(
                        stringResource(if (bulk == AppleBulkAction.ResumeAll) Lang.cache_management_resume_all else Lang.cache_management_pause_all),
                        Modifier
                            .wrapContentWidth(unbounded = true)
                            .clip(RoundedCornerShape(4.dp))
                            .clickable(role = Role.Button, enabled = downloads.none { it.isBusy }) {
                                if (bulk == AppleBulkAction.ResumeAll) onResumeAll() else onPauseAll()
                            }
                            .testTag(MacSubjectDownloadsTestTags.GroupPauseAll),
                        style = AniTextStyles.macCallout,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                }
            } else {
                downloads.totalSizeOrNull()?.let {
                    Text(
                        renderFileSize(it),
                        Modifier.wrapContentWidth(unbounded = true),
                        style = AniTextStyles.macCallout.copy(fontFeatureSettings = "tnum"),
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * 下载行与它的右键菜单. 菜单在右键的位置打开, 打开时选中该行; 选择模式下不打开.
 */
@Composable
private fun DownloadRowWithMenu(
    download: DownloadItem,
    columns: MacTableColumns,
    compact: Boolean,
    sourceName: String?,
    watchStatus: String?,
    selected: Boolean,
    showDivider: Boolean,
    menuEnabled: Boolean,
    onClick: () -> Unit,
    onDoubleClick: (() -> Unit)?,
    onSecondaryClick: () -> Unit,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onShowDetails: () -> Unit,
    onOpenSubjectDetails: () -> Unit,
    onReveal: () -> Unit,
    onDelete: () -> Unit,
) {
    var menuPosition by remember(download.id) { mutableStateOf<Offset?>(null) }
    Box(
        Modifier
            .testTag(MacSubjectDownloadsTestTags.row(download.id))
            .onSecondaryClick(enabled = menuEnabled) {
                onSecondaryClick()
                menuPosition = it
            },
    ) {
        DownloadRow(
            download, columns, compact, sourceName, watchStatus, selected, showDivider,
            onClick, onDoubleClick, onPlay, onPause, onResume,
        )
        val position = menuPosition
        if (position != null) {
            fun dismissThen(action: () -> Unit): () -> Unit = {
                menuPosition = null
                action()
            }
            AppleContextMenu(
                expanded = true,
                position = position,
                onDismissRequest = { menuPosition = null },
                modifier = Modifier.testTag(MacSubjectDownloadsTestTags.Menu),
            ) {
                when (download.controlAction) {
                    DownloadControlAction.Pause -> PopoverMenuItem(
                        stringResource(Lang.cache_episode_pause_download),
                        onClick = dismissThen(onPause),
                        enabled = !download.isBusy,
                    )

                    DownloadControlAction.Resume -> PopoverMenuItem(
                        stringResource(Lang.cache_episode_resume_download),
                        onClick = dismissThen(onResume),
                        enabled = !download.isBusy,
                    )

                    null -> {}
                }
                if (download.showsPlay) {
                    PopoverMenuItem(
                        stringResource(Lang.cache_management_play),
                        onClick = dismissThen(onPlay),
                        Modifier.testTag(MacSubjectDownloadsTestTags.MenuPlay),
                    )
                }
                PopoverMenuItem(
                    stringResource(Lang.cache_management_view_details),
                    onClick = dismissThen(onShowDetails),
                    Modifier.testTag(MacSubjectDownloadsTestTags.MenuDetails),
                    shortcut = "⌘I",
                )
                PopoverMenuItem(stringResource(Lang.cache_management_open_subject), onClick = dismissThen(onOpenSubjectDetails))
                PopoverMenuItem(
                    stringResource(Lang.cache_management_show_in_finder),
                    onClick = dismissThen(onReveal),
                    Modifier.testTag(MacSubjectDownloadsTestTags.MenuReveal),
                )
                PopoverMenuItem(
                    stringResource(Lang.cache_management_delete_download),
                    onClick = dismissThen(onDelete),
                    Modifier.testTag(MacSubjectDownloadsTestTags.MenuDelete),
                    showDivider = true,
                )
            }
        }
    }
}

/**
 * 下载行 (高 52, 分隔线从 60 开始): 集 13/600 等宽; 名称 (集名 14/600 + 来源 12 label2); 进度 (4 高进度条 + 右对齐百分比);
 * 速度 (下载中为 label, 其余「–」); 大小 13 labelSoft; 状态 (失败为 error「下载失败」, 已完成为观看状态); 操作 32 圆钮.
 * [compact] 为检阅器打开时的版式, 不画速度与大小.
 */
@Composable
private fun DownloadRow(
    download: DownloadItem,
    columns: MacTableColumns,
    compact: Boolean,
    sourceName: String?,
    watchStatus: String?,
    selected: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit,
    onDoubleClick: (() -> Unit)?,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val downloading = download.status == DownloadStatus.IN_PROGRESS
    MacTableRow(
        columns,
        height = 52.dp,
        selected = selected,
        showDivider = showDivider,
        dividerStartIndent = 60.dp,
        onClick = onClick,
        onDoubleClick = onDoubleClick,
    ) {
        Text(
            episodeTileLabel(download.sort),
            style = AniTextStyles.macCallout.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface,
            maxLines = 1,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                downloadEpisodeTitle(download.sort, download.displayName),
                style = AniTextStyles.macBody,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (sourceName != null) {
                Text(sourceName, style = AniTextStyles.macCaption, color = colorScheme.secondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val fraction = if (download.isFinished) 1f else download.progress.getOrZero()
            AppleProgressBar({ fraction }, Modifier.weight(1f), muted = !downloading)
            Text(
                "${(fraction * 100).toInt()}%",
                Modifier.widthIn(min = 36.dp),
                style = AniTextStyles.macCaption.copy(fontFeatureSettings = "tnum"),
                color = colorScheme.secondary,
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
        if (!compact) {
            Text(
                if (downloading) download.speedText ?: "–" else "–",
                style = AniTextStyles.macCallout.copy(fontFeatureSettings = "tnum"),
                color = if (downloading) colorScheme.onSurface else colorScheme.secondary,
                maxLines = 1,
            )
            Text(
                download.sizeText ?: "–",
                style = AniTextStyles.macCallout.copy(fontFeatureSettings = "tnum"),
                color = AniColors.labelSoft,
                maxLines = 1,
            )
        }
        Text(
            when {
                download.isFailed -> stringResource(Lang.cache_episode_download_failed)
                download.isPaused -> stringResource(Lang.cache_episode_status_paused)
                download.isFinished -> watchStatus ?: stringResource(Lang.cache_filter_status_finished)
                else -> stringResource(Lang.cache_episode_downloading)
            },
            Modifier.padding(start = StatusInset),
            style = AniTextStyles.macCallout,
            color = if (download.isFailed) colorScheme.error else colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box(Modifier.padding(start = StatusInset).width(56.dp), contentAlignment = Alignment.Center) {
            when {
                download.isFinished -> RowActionButton(
                    AppleIcons.playFilled(),
                    stringResource(Lang.cache_management_play),
                    containerColor = colorScheme.surfaceContainerHighest,
                    iconColor = colorScheme.onSurface,
                    onClick = onPlay,
                    Modifier.testTag(MacSubjectDownloadsTestTags.play(download.id)),
                )

                download.isPaused -> RowActionButton(
                    AppleIcons.playFilled(),
                    stringResource(Lang.cache_episode_resume_download),
                    containerColor = colorScheme.secondaryContainer,
                    iconColor = colorScheme.primary,
                    onClick = onResume,
                    Modifier.testTag(MacSubjectDownloadsTestTags.resume(download.id)),
                    enabled = !download.isBusy,
                )

                downloading -> RowActionButton(
                    AppleIcons.pauseFilled(),
                    stringResource(Lang.cache_episode_pause_download),
                    containerColor = colorScheme.surfaceContainerHighest,
                    iconColor = colorScheme.onSurface,
                    onClick = onPause,
                    Modifier.testTag(MacSubjectDownloadsTestTags.pause(download.id)),
                    enabled = !download.isBusy,
                )
            }
        }
    }
}

/**
 * 尚未下载的剧集: 集与名称 (集名 + 「尚未下载」, 看过或抛弃时接在后面), 操作列为现有的下载动作 (准备中为进度圈与取消).
 * 不能选中; 选择模式下变淡、不显示下载按钮.
 */
@Composable
private fun EpisodeRow(
    episode: EpisodeDownloadItem,
    columns: MacTableColumns,
    compact: Boolean,
    showDivider: Boolean,
    dimmed: Boolean,
    enabled: Boolean,
    busy: Boolean,
    canCancel: Boolean,
    onDownload: () -> Unit,
    onCancel: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val useOriginalTitle = LocalSubjectAppearanceSettings.current.useOriginalTitle
    val weakened = episode.watchStatus.isDoneOrDropped() || !episode.hasPublished
    MacTableRow(
        columns,
        Modifier
            .alpha(if (dimmed) AppleComponentDefaults.DisabledAlpha else 1f)
            .testTag(MacSubjectDownloadsTestTags.episode(episode.episodeId)),
        height = 52.dp,
        showDivider = showDivider,
        dividerStartIndent = 60.dp,
    ) {
        Text(
            episodeTileLabel(episode.sort),
            style = AniTextStyles.macCallout.copy(fontFeatureSettings = "tnum"),
            fontWeight = FontWeight.SemiBold,
            color = if (weakened) colorScheme.secondary else colorScheme.onSurface,
            maxLines = 1,
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                downloadEpisodeTitle(episode.sort, if (useOriginalTitle) episode.originalTitle else episode.title),
                style = AniTextStyles.macBody,
                fontWeight = FontWeight.SemiBold,
                // 看过、抛弃与尚未放送的剧集变淡, 同既有剧集行.
                color = if (weakened) colorScheme.secondary else colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                listOfNotNull(stringResource(Lang.cache_subject_not_downloaded), episodeWatchStatusText(episode.watchStatus))
                    .joinToString(" · "),
                style = AniTextStyles.macCaption,
                color = colorScheme.secondary,
                maxLines = 1,
            )
        }
        Box {}
        if (!compact) {
            Box {}
            Box {}
        }
        Box {}
        Box(Modifier.padding(start = StatusInset).width(56.dp), contentAlignment = Alignment.Center) {
            if (!dimmed) {
                if (busy) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                        if (canCancel) {
                            RowActionButton(
                                AppleIcons.close(),
                                stringResource(Lang.cache_subject_cancel),
                                containerColor = Color.Transparent,
                                iconColor = colorScheme.secondary,
                                onClick = onCancel,
                                Modifier.testTag(MacSubjectDownloadsTestTags.cancel(episode.episodeId)),
                                size = 24.dp,
                            )
                        }
                    }
                } else {
                    RowActionButton(
                        AppleIcons.download(2.2f),
                        stringResource(Lang.cache_subject_cache),
                        containerColor = colorScheme.surfaceContainerHighest,
                        iconColor = colorScheme.primary,
                        onClick = onDownload,
                        Modifier.testTag(MacSubjectDownloadsTestTags.download(episode.episodeId)),
                        enabled = enabled,
                        iconSize = 14.dp,
                    )
                }
            }
        }
    }
}

/**
 * 操作列的圆形按钮, 默认 32, 图标 12.
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
    size: Dp = 32.dp,
    iconSize: Dp = 12.dp,
) {
    Box(
        modifier
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
            .size(size)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, Modifier.size(iconSize), tint = iconColor)
    }
}

internal object MacSubjectDownloadsTestTags {
    const val OpenSubject = "mac-subject-downloads-open-subject"
    const val Select = "mac-subject-downloads-select"
    const val SelectAll = "mac-subject-downloads-select-all"
    const val DeleteSelected = "mac-subject-downloads-delete-selected"
    const val Done = "mac-subject-downloads-done"
    const val PauseAll = "mac-subject-downloads-pause-all"
    const val GroupPauseAll = "mac-subject-downloads-group-pause-all"
    const val InspectorToggle = "mac-subject-downloads-inspector-toggle"
    const val Header = "mac-subject-downloads-header"
    const val Hint = "mac-subject-downloads-hint"
    const val Menu = "mac-subject-downloads-menu"
    const val MenuPlay = "mac-subject-downloads-menu-play"
    const val MenuDetails = "mac-subject-downloads-menu-details"
    const val MenuReveal = "mac-subject-downloads-menu-reveal"
    const val MenuDelete = "mac-subject-downloads-menu-delete"
    fun group(active: Boolean) = if (active) "mac-subject-downloads-group-active" else "mac-subject-downloads-group-finished"
    fun row(id: String) = "mac-subject-downloads-row-$id"
    fun play(id: String) = "mac-subject-downloads-play-$id"
    fun pause(id: String) = "mac-subject-downloads-pause-$id"
    fun resume(id: String) = "mac-subject-downloads-resume-$id"
    fun episode(episodeId: Int) = "mac-subject-downloads-episode-$episodeId"
    fun download(episodeId: Int) = "mac-subject-downloads-download-$episodeId"
    fun cancel(episodeId: Int) = "mac-subject-downloads-cancel-$episodeId"
}
