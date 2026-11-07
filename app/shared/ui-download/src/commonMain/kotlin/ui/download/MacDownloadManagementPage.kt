/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.download

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.io.files.Path
import me.him188.ani.app.domain.media.cache.engine.MediaStats
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.features.getComponentAccessors
import me.him188.ani.app.platform.files
import me.him188.ani.app.ui.download.components.DownloadFilterAndSortState
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.components.DownloadSelectionState
import me.him188.ani.app.ui.download.components.DownloadSortOption
import me.him188.ani.app.ui.download.components.DownloadStatus
import me.him188.ani.app.ui.download.components.DownloadStatusFilter
import me.him188.ani.app.ui.download.components.SubjectDownloadGroup
import me.him188.ani.app.ui.download.components.rememberDownloadFilterAndSortState
import me.him188.ani.app.ui.download.components.rememberDownloadSelectionState
import me.him188.ani.app.ui.download.components.renderFileSize
import me.him188.ani.app.ui.download.components.renderSortOption
import me.him188.ani.app.ui.download.components.renderSpeed
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.apple.AppleContextMenu
import me.him188.ani.app.ui.foundation.apple.AppleCover
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.ApplePopoverMenu
import me.him188.ani.app.ui.foundation.apple.AppleProgressBar
import me.him188.ani.app.ui.foundation.apple.CoverAspect
import me.him188.ani.app.ui.foundation.apple.MacKeyboardShortcut
import me.him188.ani.app.ui.foundation.apple.MacLargeTitleHeader
import me.him188.ani.app.ui.foundation.apple.MacPageToolbar
import me.him188.ani.app.ui.foundation.apple.MacTableColumn
import me.him188.ani.app.ui.foundation.apple.MacTableColumns
import me.him188.ani.app.ui.foundation.apple.MacTableHeader
import me.him188.ani.app.ui.foundation.apple.MacTableHeaderCell
import me.him188.ani.app.ui.foundation.apple.MacTableHint
import me.him188.ani.app.ui.foundation.apple.MacTableRow
import me.him188.ani.app.ui.foundation.apple.MacToolbarTextButton
import me.him188.ani.app.ui.foundation.apple.PillTab
import me.him188.ani.app.ui.foundation.apple.PopoverMenuItem
import me.him188.ani.app.ui.foundation.apple.onSecondaryClick
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_details_open_file_failed
import me.him188.ani.app.ui.lang.cache_episode_downloading
import me.him188.ani.app.ui.lang.cache_episode_pause_download
import me.him188.ani.app.ui.lang.cache_episode_resume_download
import me.him188.ani.app.ui.lang.cache_episode_status_paused
import me.him188.ani.app.ui.lang.cache_filter_status_all
import me.him188.ani.app.ui.lang.cache_filter_status_downloading
import me.him188.ani.app.ui.lang.cache_filter_status_finished
import me.him188.ani.app.ui.lang.cache_management_delete_download
import me.him188.ani.app.ui.lang.cache_management_delete_selected
import me.him188.ani.app.ui.lang.cache_management_deselect_all
import me.him188.ani.app.ui.lang.cache_management_done
import me.him188.ani.app.ui.lang.cache_management_download_label
import me.him188.ani.app.ui.lang.cache_management_failed_count
import me.him188.ani.app.ui.lang.cache_management_finished_count
import me.him188.ani.app.ui.lang.cache_management_location
import me.him188.ani.app.ui.lang.cache_management_open_subject
import me.him188.ani.app.ui.lang.cache_management_pause_all
import me.him188.ani.app.ui.lang.cache_management_pause_selected
import me.him188.ani.app.ui.lang.cache_management_resume_all
import me.him188.ani.app.ui.lang.cache_management_resume_selected
import me.him188.ani.app.ui.lang.cache_management_select
import me.him188.ani.app.ui.lang.cache_management_select_all_action
import me.him188.ani.app.ui.lang.cache_management_selected_count
import me.him188.ani.app.ui.lang.cache_management_show_in_finder
import me.him188.ani.app.ui.lang.cache_management_summary
import me.him188.ani.app.ui.lang.cache_management_table_hint
import me.him188.ani.app.ui.lang.cache_management_upload_label
import me.him188.ani.app.ui.lang.cache_management_view_details
import me.him188.ani.app.ui.lang.cache_subject_cancel
import me.him188.ani.app.ui.lang.cache_table_size
import me.him188.ani.app.ui.lang.cache_table_speed
import me.him188.ani.app.ui.lang.cache_table_status
import me.him188.ani.app.ui.lang.cache_table_subject
import me.him188.ani.app.ui.lang.cache_total_download
import me.him188.ani.app.ui.lang.cache_total_upload
import me.him188.ani.app.ui.lang.collection_table_progress
import me.him188.ani.app.ui.lang.downloads_operation_failed
import me.him188.ani.app.ui.lang.main_screen_page_cache_management
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.FileSize.Companion.bytes
import me.him188.ani.utils.io.absolutePath
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 版快取页 (04 §3.5).
 *
 * 工具栏右侧: 排序、「全部暂停 / 全部继续」(05 G25)、「选择」(05 D2). 内容: 大标题; 统计卡 (下载、上传、快取位置, 05 G5);
 * 下载状态胶囊与「N 部 · 共 X GB」; 按条目分组的 C19 表格; 表下操作提示.
 *
 * 单击选中行, 双击打开单番快取页; 右键打开菜单 (05 C3): 暂停或继续、查看下载详情 (⌘I)、前往条目详情、
 * 在 Finder 中显示 (05 G9)、删除下载. 失败不放重试钮 (现有下载没有重试动作).
 *
 * @param onOpenSubjectDownloads 打开单番快取页 ([me.him188.ani.app.navigation.NavRoutes.SubjectCaches]).
 * @param onOpenSubjectDetails 前往条目详情.
 */
@Composable
fun MacDownloadManagementPage(
    vm: DownloadManagementViewModel,
    onOpenSubjectDownloads: (subjectId: Int) -> Unit,
    onOpenSubjectDetails: (subjectId: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    val configuredDirectory by vm.cacheDirectory.collectAsStateWithLifecycle(null)
    val failedOperations by vm.operationFailures.collectAsStateWithLifecycle()
    if (failedOperations > 0) {
        AlertDialog(
            onDismissRequest = vm::dismissOperationFailures,
            text = { Text(stringResource(Lang.downloads_operation_failed, failedOperations)) },
            confirmButton = {
                TextButton(onClick = vm::dismissOperationFailures) { Text(stringResource(Lang.cache_subject_cancel)) }
            },
        )
    }
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

    val cacheDirectory = configuredDirectory ?: defaultDirectory
    MacDownloadManagementLayout(
        state,
        cacheDirectory = cacheDirectory,
        onOpenSubjectDownloads = onOpenSubjectDownloads,
        onOpenSubjectDetails = onOpenSubjectDetails,
        onPause = vm::pauseDownload,
        onResume = vm::resumeDownload,
        onDelete = vm::deleteDownload,
        onPauseAll = vm::pauseAll,
        onResumeAll = vm::resumeAll,
        onRevealDirectory = { scope.launch { reveal(cacheDirectory) } },
        onRevealGroup = { group ->
            scope.launch {
                val path = group.entries.firstNotNullOfOrNull { vm.localFilePath(it) }
                reveal(path ?: cacheDirectory)
            }
        },
        modifier = modifier,
    )
}

/**
 * [MacDownloadManagementPage] 的版面. 筛选、排序、选中与选择模式由本函数持有.
 */
@Composable
internal fun MacDownloadManagementLayout(
    state: DownloadManagementUiState,
    cacheDirectory: String,
    onOpenSubjectDownloads: (subjectId: Int) -> Unit,
    onOpenSubjectDetails: (subjectId: Int) -> Unit,
    onPause: (DownloadItem) -> Unit,
    onResume: (DownloadItem) -> Unit,
    onDelete: (DownloadItem) -> Unit,
    onPauseAll: () -> Unit,
    onResumeAll: () -> Unit,
    onRevealDirectory: () -> Unit,
    onRevealGroup: (SubjectDownloadGroup) -> Unit,
    modifier: Modifier = Modifier,
    filterState: DownloadFilterAndSortState = rememberDownloadFilterAndSortState(),
    selectionState: DownloadSelectionState = rememberDownloadSelectionState(),
) {
    val groups = filterState.applyFilterAndSortGrouped(state.groups)
    val statusCounts by remember(state.groups) {
        derivedStateOf {
            listOf(null, DownloadStatusFilter.Downloading, DownloadStatusFilter.Finished)
                .associateWith { filterState.filterAndSort(state.groups, it).size }
        }
    }
    val visibleEntries = remember(groups) { groups.flatMap { it.entries } }
    val selectedEntries = remember(visibleEntries, selectionState.selectedIds) {
        visibleEntries.filter { it.id in selectionState.selectedIds }
    }
    val allSelected = visibleEntries.isNotEmpty() && visibleEntries.all { it.id in selectionState.selectedIds }
    // 非选择模式下单击选中的条目
    var selectedSubject by remember { mutableStateOf<Int?>(null) }

    var pendingDeleteEntries by remember { mutableStateOf<List<DownloadItem>?>(null) }
    pendingDeleteEntries?.let { entries ->
        DeleteActionDialog(
            onDismiss = { pendingDeleteEntries = null },
            confirmEnabled = state.entries.none { current -> current.isBusy && entries.any { it.id == current.id } },
            onConfirm = {
                entries.forEach(onDelete)
                pendingDeleteEntries = null
            },
        )
    }

    MacKeyboardShortcut(Key.I, enabled = !selectionState.inSelection && selectedSubject != null) {
        selectedSubject?.let(onOpenSubjectDownloads)
    }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        MacPageToolbar {
            if (selectionState.inSelection) {
                SelectionActions(
                    selectedCount = selectedEntries.size,
                    allSelected = allSelected,
                    resumeEnabled = selectedEntries.none { it.isBusy } && selectedEntries.any { !it.isFinished && it.isPaused },
                    pauseEnabled = selectedEntries.none { it.isBusy } &&
                            selectedEntries.any { !it.isFinished && !it.isPaused && !it.isFailed },
                    deleteEnabled = selectedEntries.isNotEmpty() && selectedEntries.none { it.isBusy },
                    onToggleSelectAll = {
                        val ids = visibleEntries.map { it.id }
                        selectionState.enterSelectionWith(
                            if (allSelected) selectionState.selectedIds - ids.toSet() else selectionState.selectedIds + ids,
                        )
                    },
                    onResume = { selectedEntries.forEach(onResume) },
                    onPause = { selectedEntries.forEach(onPause) },
                    onDelete = { pendingDeleteEntries = selectedEntries },
                    onDone = { selectionState.clear() },
                )
            } else {
                SortButton(filterState.sortOption, onSelect = { filterState.sortOption = it })
                val downloading = state.entries.any { it.status == DownloadStatus.IN_PROGRESS }
                val paused = state.entries.any { it.isPaused }
                MacToolbarTextButton(
                    stringResource(if (!downloading && paused) Lang.cache_management_resume_all else Lang.cache_management_pause_all),
                    onClick = { if (downloading) onPauseAll() else onResumeAll() },
                    Modifier.testTag(MacDownloadTestTags.PauseAll),
                    enabled = downloading || paused,
                )
                MacToolbarTextButton(
                    stringResource(Lang.cache_management_select),
                    onClick = { selectionState.enterSelectionWith(emptySet()) },
                    Modifier.testTag(MacDownloadTestTags.Select),
                    enabled = groups.isNotEmpty(),
                )
            }
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 32.dp, top = 4.dp, end = 32.dp, bottom = 32.dp),
        ) {
            item(key = "title") {
                MacLargeTitleHeader(stringResource(Lang.main_screen_page_cache_management))
            }
            item(key = "stats") {
                StatsCards(state.overallStats, cacheDirectory, onRevealDirectory, Modifier.padding(top = 20.dp))
            }
            item(key = "filter") {
                FilterRow(
                    selected = filterState.selectedStatus,
                    counts = statusCounts,
                    onSelect = { filterState.selectedStatus = it },
                    groupCount = groups.size,
                    totalSize = groups.fold(0L) { acc, group -> acc + group.totalSize.inBytes.coerceAtLeast(0) }.bytes,
                    Modifier.padding(top = 24.dp, bottom = 12.dp),
                )
            }
            item(key = "table-header") {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(tableShape(top = true, bottom = groups.isEmpty()))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    MacTableHeader(MacDownloadTableColumns) {
                        MacTableHeaderCell(stringResource(Lang.cache_table_subject))
                        MacTableHeaderCell(stringResource(Lang.collection_table_progress))
                        MacTableHeaderCell(stringResource(Lang.cache_table_speed))
                        MacTableHeaderCell(stringResource(Lang.cache_table_size))
                        MacTableHeaderCell(stringResource(Lang.cache_table_status))
                        Box {}
                    }
                }
            }
            itemsIndexed(groups, key = { _, group -> group.key }) { index, group ->
                val ids = group.entries.map { it.id }
                val selected = if (selectionState.inSelection) {
                    ids.all { it in selectionState.selectedIds }
                } else {
                    selectedSubject == group.subjectId
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(tableShape(top = false, bottom = index == groups.lastIndex))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    GroupRowWithMenu(
                        group,
                        selected = selected,
                        showDivider = index > 0,
                        menuEnabled = !selectionState.inSelection,
                        onClick = {
                            if (selectionState.inSelection) {
                                selectionState.toggleSelection(*ids.toTypedArray())
                            } else {
                                selectedSubject = group.subjectId
                            }
                        },
                        onDoubleClick = {
                            if (!selectionState.inSelection) onOpenSubjectDownloads(group.subjectId)
                        },
                        onSecondaryClick = { selectedSubject = group.subjectId },
                        onPause = { group.entries.filter { it.status == DownloadStatus.IN_PROGRESS && !it.isBusy }.forEach(onPause) },
                        onResume = { group.entries.filter { it.isPaused && !it.isBusy }.forEach(onResume) },
                        onOpenDownloads = { onOpenSubjectDownloads(group.subjectId) },
                        onOpenDetails = { onOpenSubjectDetails(group.subjectId) },
                        onReveal = { onRevealGroup(group) },
                        onDelete = { pendingDeleteEntries = group.entries },
                    )
                }
            }
            item(key = "hint") {
                MacTableHint(stringResource(Lang.cache_management_table_hint))
            }
        }
    }
}

/**
 * 表格列: 番剧 (弹性) / 进度 220 / 速度 96 / 大小 80 / 状态 120 / 操作 32, 列间距 16, 左右内距 16.
 */
internal val MacDownloadTableColumns = MacTableColumns(
    listOf(
        MacTableColumn(weight = 1f),
        MacTableColumn(width = 220.dp),
        MacTableColumn(width = 96.dp, alignment = Alignment.End),
        MacTableColumn(width = 80.dp, alignment = Alignment.End),
        MacTableColumn(width = 120.dp),
        MacTableColumn(width = 32.dp, alignment = Alignment.CenterHorizontally),
    ),
    horizontalPadding = 16.dp,
    spacing = 16.dp,
)

private fun tableShape(top: Boolean, bottom: Boolean): Shape = RoundedCornerShape(
    topStart = if (top) 14.dp else 0.dp,
    topEnd = if (top) 14.dp else 0.dp,
    bottomStart = if (bottom) 14.dp else 0.dp,
    bottomEnd = if (bottom) 14.dp else 0.dp,
)

/**
 * 排序「最近新增 ▾」: 13 号 labelSoft, 点击打开 C15 菜单, 当前选项有勾号.
 */
@Composable
private fun SortButton(
    current: DownloadSortOption,
    onSelect: (DownloadSortOption) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(role = Role.Button) { expanded = true }
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .testTag(MacDownloadTestTags.Sort),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(renderSortOption(current), style = AniTextStyles.macCallout, color = AniColors.labelSoft, maxLines = 1)
            Icon(AppleIcons.chevronDown(), contentDescription = null, Modifier.size(11.dp), tint = AniColors.labelSoft)
        }
        ApplePopoverMenu(expanded, onDismissRequest = { expanded = false }) {
            for (option in DownloadSortOption.entries) {
                PopoverMenuItem(
                    renderSortOption(option),
                    onClick = {
                        expanded = false
                        onSelect(option)
                    },
                    checked = option == current,
                )
            }
        }
    }
}

/**
 * 选择模式的工具栏: 「已選 N」、全选或取消全选、继续、暂停、删除所选、完成.
 */
@Composable
private fun SelectionActions(
    selectedCount: Int,
    allSelected: Boolean,
    resumeEnabled: Boolean,
    pauseEnabled: Boolean,
    deleteEnabled: Boolean,
    onToggleSelectAll: () -> Unit,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onDelete: () -> Unit,
    onDone: () -> Unit,
) {
    Text(
        stringResource(Lang.cache_management_selected_count, selectedCount),
        Modifier.padding(end = 8.dp),
        style = AniTextStyles.macCallout,
        color = MaterialTheme.colorScheme.secondary,
        maxLines = 1,
    )
    MacToolbarTextButton(
        stringResource(if (allSelected) Lang.cache_management_deselect_all else Lang.cache_management_select_all_action),
        onClick = onToggleSelectAll,
    )
    MacToolbarTextButton(stringResource(Lang.cache_management_resume_selected), onClick = onResume, enabled = resumeEnabled)
    MacToolbarTextButton(stringResource(Lang.cache_management_pause_selected), onClick = onPause, enabled = pauseEnabled)
    MacToolbarTextButton(stringResource(Lang.cache_management_delete_selected), onClick = onDelete, enabled = deleteEnabled)
    MacToolbarTextButton(
        stringResource(Lang.cache_management_done),
        onClick = onDone,
        Modifier.testTag(MacDownloadTestTags.Done),
    )
}

/**
 * 统计卡: 3 栏等宽, 间距 16. 每卡 card 底、圆角 14、内距 14/16: 标签 13 label2; 数值 22/700 (速度) 或 15 等宽字体 (路径);
 * 说明 12 label2 (累计流量) 或 accent「在 Finder 中显示」.
 */
@Composable
private fun StatsCards(
    stats: MediaStats,
    cacheDirectory: String,
    onRevealDirectory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        StatCard(
            stringResource(Lang.cache_management_download_label),
            Modifier.weight(1f).testTag(MacDownloadTestTags.DownloadSpeed),
        ) {
            SpeedText(renderSpeed(stats.downloadSpeed).ifEmpty { "–" })
            CaptionText(stringResource(Lang.cache_total_download) + " " + renderFileSize(stats.downloaded).ifEmpty { "–" })
        }
        StatCard(stringResource(Lang.cache_management_upload_label), Modifier.weight(1f)) {
            SpeedText(renderSpeed(stats.uploadSpeed).ifEmpty { "–" })
            CaptionText(stringResource(Lang.cache_total_upload) + " " + renderFileSize(stats.uploaded).ifEmpty { "–" })
        }
        StatCard(stringResource(Lang.cache_management_location), Modifier.weight(1f)) {
            Text(
                cacheDirectory,
                Modifier.padding(vertical = 3.dp),
                style = AniTextStyles.macBody.copy(fontSize = 15.sp, lineHeight = 22.sp, fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
            )
            Text(
                stringResource(Lang.cache_management_show_in_finder),
                Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(role = Role.Button, onClick = onRevealDirectory)
                    .testTag(MacDownloadTestTags.RevealDirectory),
                style = AniTextStyles.macCaption,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(label, style = AniTextStyles.macCallout, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
        content()
    }
}

@Composable
private fun SpeedText(text: String) {
    Text(
        text,
        style = AniTextStyles.macTitle.copy(fontSize = 22.sp, lineHeight = 28.sp, fontFeatureSettings = "tnum"),
        color = MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
    )
}

@Composable
private fun CaptionText(text: String) {
    Text(
        text,
        style = AniTextStyles.macCaption.copy(fontFeatureSettings = "tnum"),
        color = MaterialTheme.colorScheme.secondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

/**
 * 筛选行: 左为下载状态胶囊 (全部、下载中、已完成, 数量为符合条件的条目数), 右为「N 部 · 共 X GB」12 label2.
 */
@Composable
private fun FilterRow(
    selected: DownloadStatusFilter?,
    counts: Map<DownloadStatusFilter?, Int>,
    onSelect: (DownloadStatusFilter?) -> Unit,
    groupCount: Int,
    totalSize: FileSize,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            for (filter in listOf(null, DownloadStatusFilter.Downloading, DownloadStatusFilter.Finished)) {
                PillTab(
                    stringResource(
                        when (filter) {
                            null -> Lang.cache_filter_status_all
                            DownloadStatusFilter.Downloading -> Lang.cache_filter_status_downloading
                            DownloadStatusFilter.Finished -> Lang.cache_filter_status_finished
                        },
                    ),
                    selected = filter == selected,
                    onClick = { onSelect(filter) },
                    count = counts[filter]?.toString(),
                )
            }
        }
        Text(
            stringResource(Lang.cache_management_summary, groupCount, renderFileSize(totalSize).ifEmpty { "0 B" }),
            style = AniTextStyles.macCaption.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
        )
    }
}

/**
 * 表格行与它的右键菜单. 菜单在右键的位置打开, 打开时选中该行.
 */
@Composable
private fun GroupRowWithMenu(
    group: SubjectDownloadGroup,
    selected: Boolean,
    showDivider: Boolean,
    menuEnabled: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onSecondaryClick: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenDetails: () -> Unit,
    onReveal: () -> Unit,
    onDelete: () -> Unit,
) {
    val summary = remember(group) { group.appleSummary() }
    var menuPosition by remember(group.subjectId) { mutableStateOf<Offset?>(null) }
    Box(
        Modifier
            .testTag(MacDownloadTestTags.group(group.subjectId))
            .onSecondaryClick(enabled = menuEnabled) {
                onSecondaryClick()
                menuPosition = it
            },
    ) {
        GroupRow(group, summary, selected, showDivider, onClick, onDoubleClick, onPause, onResume, onOpenDownloads)
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
                modifier = Modifier.testTag(MacDownloadTestTags.Menu),
            ) {
                when (summary.state) {
                    GroupState.Downloading -> PopoverMenuItem(
                        stringResource(Lang.cache_episode_pause_download),
                        onClick = dismissThen(onPause),
                    )

                    GroupState.Paused -> PopoverMenuItem(
                        stringResource(Lang.cache_episode_resume_download),
                        onClick = dismissThen(onResume),
                    )

                    GroupState.Idle -> {}
                }
                PopoverMenuItem(
                    stringResource(Lang.cache_management_view_details),
                    onClick = dismissThen(onOpenDownloads),
                    shortcut = "⌘I",
                )
                PopoverMenuItem(stringResource(Lang.cache_management_open_subject), onClick = dismissThen(onOpenDetails))
                PopoverMenuItem(
                    stringResource(Lang.cache_management_show_in_finder),
                    onClick = dismissThen(onReveal),
                    Modifier.testTag(MacDownloadTestTags.MenuReveal),
                )
                PopoverMenuItem(
                    stringResource(Lang.cache_management_delete_download),
                    onClick = dismissThen(onDelete),
                    Modifier.testTag(MacDownloadTestTags.MenuDelete),
                    showDivider = true,
                )
            }
        }
    }
}

/**
 * 表格行 (高 72): 番剧 (42×56 封面 + 番名 14/600 + 集数状态 12 label2)、进度 (4 高进度条 + 右对齐百分比或集数)、
 * 速度、大小、状态 (失败为 error「N 个下载失败」)、操作 (下载中为暂停钮, 暂停为继续钮, 其余为打开单番快取页的箭头).
 * 分隔线从 74 开始.
 */
@Composable
private fun GroupRow(
    group: SubjectDownloadGroup,
    summary: AppleGroupSummary,
    selected: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit,
    onDoubleClick: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onOpen: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val downloading = summary.state == GroupState.Downloading
    MacTableRow(
        MacDownloadTableColumns,
        height = 72.dp,
        selected = selected,
        showDivider = showDivider,
        dividerStartIndent = 74.dp,
        onClick = onClick,
        onDoubleClick = onDoubleClick,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AppleCover(CoverAspect.Portrait3x4, group.subjectName, Modifier.width(42.dp), cornerRadius = 6.dp) {
                AsyncImage(group.imageUrl, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    group.subjectName,
                    style = AniTextStyles.macBody,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    stringResource(
                        Lang.cache_management_finished_count,
                        group.finishedCount,
                        group.displayTotalCount,
                    ),
                    style = AniTextStyles.macCaption.copy(fontFeatureSettings = "tnum"),
                    color = colorScheme.secondary,
                    maxLines = 1,
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val finished = summary.state == GroupState.Idle
            val fraction = if (finished) {
                if (group.displayTotalCount == 0) 0f else group.finishedCount.toFloat() / group.displayTotalCount
            } else {
                summary.progress
            }
            AppleProgressBar({ fraction }, Modifier.weight(1f), muted = !downloading)
            Text(
                if (finished) "${group.finishedCount}/${group.displayTotalCount}" else "${(summary.progress * 100).toInt()}%",
                Modifier.widthIn(min = 40.dp),
                style = AniTextStyles.macCaption.copy(fontFeatureSettings = "tnum"),
                color = colorScheme.secondary,
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
        Text(
            if (downloading) renderSpeed(group.downloadSpeed).ifEmpty { "–" } else "–",
            style = AniTextStyles.macCallout.copy(fontFeatureSettings = "tnum"),
            color = if (downloading) colorScheme.onSurface else colorScheme.secondary,
            maxLines = 1,
        )
        Text(
            renderFileSize(group.totalSize).ifEmpty { "–" },
            style = AniTextStyles.macCallout.copy(fontFeatureSettings = "tnum"),
            color = AniColors.labelSoft,
            maxLines = 1,
        )
        if (summary.failedCount > 0) {
            Text(
                stringResource(Lang.cache_management_failed_count, summary.failedCount),
                style = AniTextStyles.macCallout,
                color = colorScheme.error,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        } else {
            Text(
                stringResource(
                    when (summary.state) {
                        GroupState.Downloading -> Lang.cache_episode_downloading
                        GroupState.Paused -> Lang.cache_episode_status_paused
                        GroupState.Idle -> Lang.cache_filter_status_finished
                    },
                ),
                style = AniTextStyles.macCallout,
                color = colorScheme.secondary,
                maxLines = 1,
            )
        }
        when (summary.state) {
            GroupState.Downloading -> RowActionButton(
                AppleIcons.pauseFilled(),
                stringResource(Lang.cache_episode_pause_download),
                containerColor = colorScheme.surfaceContainerHighest,
                iconColor = colorScheme.onSurface,
                onClick = onPause,
                Modifier.testTag(MacDownloadTestTags.pause(group.subjectId)),
            )

            GroupState.Paused -> RowActionButton(
                AppleIcons.playFilled(),
                stringResource(Lang.cache_episode_resume_download),
                containerColor = colorScheme.secondaryContainer,
                iconColor = colorScheme.primary,
                onClick = onResume,
                Modifier.testTag(MacDownloadTestTags.resume(group.subjectId)),
            )

            GroupState.Idle -> RowActionButton(
                AppleIcons.chevronRight(2.4f),
                stringResource(Lang.cache_management_view_details),
                containerColor = Color.Transparent,
                iconColor = colorScheme.secondary,
                onClick = onOpen,
            )
        }
    }
}

/**
 * 操作列的 28 圆形按钮, 图标 12.
 */
@Composable
private fun RowActionButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, Modifier.size(12.dp), tint = iconColor)
    }
}

internal object MacDownloadTestTags {
    const val Sort = "mac-download-sort"
    const val PauseAll = "mac-download-pause-all"
    const val Select = "mac-download-select"
    const val Done = "mac-download-done"
    const val DownloadSpeed = "mac-download-speed"
    const val RevealDirectory = "mac-download-reveal-directory"
    const val Menu = "mac-download-menu"
    const val MenuReveal = "mac-download-menu-reveal"
    const val MenuDelete = "mac-download-menu-delete"
    fun group(subjectId: Int) = "mac-download-group-$subjectId"
    fun pause(subjectId: Int) = "mac-download-pause-$subjectId"
    fun resume(subjectId: Int) = "mac-download-resume-$subjectId"
}
