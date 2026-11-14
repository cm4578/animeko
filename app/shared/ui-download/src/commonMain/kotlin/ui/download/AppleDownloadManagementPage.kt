/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.download

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.him188.ani.app.domain.media.cache.engine.MediaCacheEngineKey
import me.him188.ani.app.domain.media.cache.engine.MediaStats
import me.him188.ani.app.tools.getOrZero
import me.him188.ani.app.ui.download.components.DownloadFilterAndSortState
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.components.DownloadSelectionState
import me.him188.ani.app.ui.download.components.DownloadSortOption
import me.him188.ani.app.ui.download.components.DownloadStatus
import me.him188.ani.app.ui.download.components.DownloadStatusFilter
import me.him188.ani.app.ui.download.components.SubjectDownloadGroup
import me.him188.ani.app.ui.download.components.rememberDownloadFilterAndSortState
import me.him188.ani.app.ui.download.components.rememberDownloadSelectionState
import me.him188.ani.app.ui.download.components.renderCollectionType
import me.him188.ani.app.ui.download.components.renderEngineKey
import me.him188.ani.app.ui.download.components.renderFileSize
import me.him188.ani.app.ui.download.components.renderSortOption
import me.him188.ani.app.ui.download.components.renderSpeed
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.apple.AppleCircleButton
import me.him188.ani.app.ui.foundation.apple.AppleComponentDefaults
import me.him188.ani.app.ui.foundation.apple.AppleCover
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleModalBottomSheet
import me.him188.ani.app.ui.foundation.apple.AppleProgressBar
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.CoverAspect
import me.him188.ani.app.ui.foundation.apple.GroupedListDefaults
import me.him188.ani.app.ui.foundation.apple.GroupedListSection
import me.him188.ani.app.ui.foundation.apple.LargeTitleHeader
import me.him188.ani.app.ui.foundation.apple.PillTab
import me.him188.ani.app.ui.foundation.apple.SettingsRow
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_episode_pause_download
import me.him188.ani.app.ui.lang.cache_episode_resume_download
import me.him188.ani.app.ui.lang.cache_episode_status_paused
import me.him188.ani.app.ui.lang.cache_filter_and_sort
import me.him188.ani.app.ui.lang.cache_filter_cache_type
import me.him188.ani.app.ui.lang.cache_filter_collection_state
import me.him188.ani.app.ui.lang.cache_filter_sort
import me.him188.ani.app.ui.lang.cache_filter_status_all
import me.him188.ani.app.ui.lang.cache_filter_status_downloading
import me.him188.ani.app.ui.lang.cache_filter_status_finished
import me.him188.ani.app.ui.lang.cache_management_delete_selected
import me.him188.ani.app.ui.lang.cache_management_deselect_all
import me.him188.ani.app.ui.lang.cache_management_done
import me.him188.ani.app.ui.lang.cache_management_download_label
import me.him188.ani.app.ui.lang.cache_management_downloading_count
import me.him188.ani.app.ui.lang.cache_management_failed_count
import me.him188.ani.app.ui.lang.cache_management_finished_count
import me.him188.ani.app.ui.lang.cache_management_pause_selected
import me.him188.ani.app.ui.lang.cache_management_resume_selected
import me.him188.ani.app.ui.lang.cache_management_select
import me.him188.ani.app.ui.lang.cache_management_select_all_action
import me.him188.ani.app.ui.lang.cache_management_selected_count
import me.him188.ani.app.ui.lang.cache_management_upload_label
import me.him188.ani.app.ui.lang.cache_subject_cancel
import me.him188.ani.app.ui.lang.cache_total_download
import me.him188.ani.app.ui.lang.cache_total_upload
import me.him188.ani.app.ui.lang.downloads_operation_failed
import me.him188.ani.app.ui.lang.main_screen_page_cache_management
import me.him188.ani.datasources.api.topic.FileSize
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版快取页 (03 §3.4). 手机主要分页.
 *
 * 从上到下: C2 大标题「快取」, 右侧「筛选与排序」与「选择」; 速度卡 (05 G30); 下载状态胶囊; 按条目分组的列表.
 * 点条目推入单番快取页; 长按或点「选择」进入选择模式, 底部浮出继续、暂停、删除.
 *
 * @param onOpenSubject 打开单番快取页 ([me.him188.ani.app.navigation.NavRoutes.SubjectCaches]).
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleDownloadManagementPage(
    vm: DownloadManagementViewModel,
    onOpenSubject: (subjectId: Int) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by vm.uiState.collectAsStateWithLifecycle()
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
    AppleDownloadManagementLayout(
        state,
        onOpenSubject = onOpenSubject,
        onPause = { vm.pauseDownload(it) },
        onResume = { vm.resumeDownload(it) },
        onDelete = { vm.deleteDownload(it) },
        modifier = modifier,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
    )
}

/**
 * [AppleDownloadManagementPage] 的版面. 筛选、排序与选择状态由本函数持有.
 */
@Composable
internal fun AppleDownloadManagementLayout(
    state: DownloadManagementUiState,
    onOpenSubject: (subjectId: Int) -> Unit,
    onPause: (DownloadItem) -> Unit,
    onResume: (DownloadItem) -> Unit,
    onDelete: (DownloadItem) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
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

    // 筛选变化时, 选中项只保留仍可见的缓存, 批量操作与计数一致. 同 DownloadManagementScreen.
    LaunchedEffect(visibleEntries, selectionState.inSelection, state.isLoading) {
        if (selectionState.inSelection && !state.isLoading) {
            val validIds = visibleEntries.mapTo(hashSetOf()) { it.id }
            selectionState.overrideSelected(selectionState.selectedIds.filterTo(hashSetOf()) { it in validIds })
        }
    }

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

    var showFilterSheet by rememberSaveable { mutableStateOf(false) }
    if (showFilterSheet) {
        FilterAndSortSheet(
            filterState,
            engineOptions = remember(state.entries) { state.entries.mapNotNull { it.engineKey }.distinct() },
            onDismissRequest = { showFilterSheet = false },
        )
    }

    Box(modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = contentPadding) {
            item(key = "header") {
                Header(
                    selectionState = selectionState,
                    selectedCount = selectedEntries.size,
                    allSelected = allSelected,
                    hasEntries = visibleEntries.isNotEmpty(),
                    onFilterAndSort = { showFilterSheet = true },
                    onToggleSelectAll = {
                        val ids = visibleEntries.map { it.id }
                        selectionState.enterSelectionWith(
                            if (allSelected) selectionState.selectedIds - ids.toSet() else selectionState.selectedIds + ids,
                        )
                    },
                    windowInsets = windowInsets,
                )
            }
            item(key = "stats") {
                SpeedCard(state.overallStats, Modifier.padding(horizontal = 16.dp).padding(bottom = 20.dp))
            }
            item(key = "status") {
                StatusPills(
                    selected = filterState.selectedStatus,
                    counts = statusCounts,
                    onSelect = { filterState.selectedStatus = it },
                    Modifier.padding(bottom = 16.dp),
                )
            }
            itemsIndexed(groups, key = { _, group -> group.key }) { index, group ->
                Box(
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(listCardShape(index, groups.size))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    val ids = group.entries.map { it.id }
                    GroupRow(
                        group,
                        selectionMode = selectionState.inSelection,
                        selected = ids.all { it in selectionState.selectedIds },
                        onClick = {
                            if (selectionState.inSelection) {
                                selectionState.toggleSelection(*ids.toTypedArray())
                            } else {
                                onOpenSubject(group.subjectId)
                            }
                        },
                        onLongClick = {
                            if (!selectionState.inSelection) {
                                selectionState.enterSelectionWith(selectionState.selectedIds + ids)
                            }
                        },
                        onPause = {
                            group.entries.filter { it.status == DownloadStatus.IN_PROGRESS && !it.isBusy }.forEach(onPause)
                        },
                        onResume = { group.entries.filter { it.isPaused && !it.isBusy }.forEach(onResume) },
                        showDivider = index > 0,
                    )
                }
            }
            if (selectionState.inSelection) {
                // 让最后一列可以滚到选择工具栏上方.
                item(key = "toolbar-space") { Spacer(Modifier.height(SelectionToolbarHeight + 12.dp)) }
            }
        }

        if (selectionState.inSelection) {
            val layoutDirection = LocalLayoutDirection.current
            SelectionToolbar(
                resumeEnabled = selectedEntries.none { it.isBusy } && selectedEntries.any { !it.isFinished && it.isPaused },
                pauseEnabled = selectedEntries.none { it.isBusy } &&
                        selectedEntries.any { !it.isFinished && !it.isPaused && !it.isFailed },
                deleteEnabled = selectedEntries.isNotEmpty() && selectedEntries.none { it.isBusy },
                onResume = { selectedEntries.forEach(onResume) },
                onPause = { selectedEntries.forEach(onPause) },
                onDelete = { pendingDeleteEntries = selectedEntries },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = contentPadding.calculateStartPadding(layoutDirection) + 16.dp,
                        end = contentPadding.calculateEndPadding(layoutDirection) + 16.dp,
                        bottom = contentPadding.calculateBottomPadding() + 12.dp,
                    ),
            )
        }
    }
}

/**
 * C2 页头. 一般为「快取」与「筛选与排序」圆钮、「选择」; 选择模式为「已选 N」与「全选 / 取消全选」、「完成」.
 */
@Composable
private fun Header(
    selectionState: DownloadSelectionState,
    selectedCount: Int,
    allSelected: Boolean,
    hasEntries: Boolean,
    onFilterAndSort: () -> Unit,
    onToggleSelectAll: () -> Unit,
    windowInsets: WindowInsets,
) {
    val title = if (selectionState.inSelection) {
        stringResource(Lang.cache_management_selected_count, selectedCount)
    } else {
        stringResource(Lang.main_screen_page_cache_management)
    }
    LargeTitleHeader(title, windowInsets = windowInsets) {
        if (selectionState.inSelection) {
            AppleTextButton(
                onToggleSelectAll,
                Modifier.testTag(AppleDownloadTestTags.SelectAll),
                enabled = hasEntries,
            ) {
                Text(
                    stringResource(
                        if (allSelected) Lang.cache_management_deselect_all else Lang.cache_management_select_all_action,
                    ),
                )
            }
            AppleTextButton({ selectionState.clear() }, Modifier.testTag(AppleDownloadTestTags.Done)) {
                Text(stringResource(Lang.cache_management_done), fontWeight = FontWeight.SemiBold)
            }
        } else {
            AppleCircleButton(
                onFilterAndSort,
                AppleIcons.sliders(),
                stringResource(Lang.cache_filter_and_sort),
                Modifier.testTag(AppleDownloadTestTags.FilterAndSort),
            )
            AppleTextButton(
                { selectionState.enterSelectionWith(emptySet()) },
                Modifier.testTag(AppleDownloadTestTags.Select),
                enabled = hasEntries,
            ) {
                Text(stringResource(Lang.cache_management_select))
            }
        }
    }
}

/**
 * 速度卡 (05 G30): card 圆角 16、上下内距 14; 下载与上传两栏, 中间分隔线.
 */
@Composable
private fun SpeedCard(stats: MediaStats, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(vertical = 14.dp)
            .height(IntrinsicSize.Min),
    ) {
        SpeedColumn(
            AppleIcons.arrowDown(),
            stringResource(Lang.cache_management_download_label),
            speed = stats.downloadSpeed,
            total = stringResource(Lang.cache_total_download) + " " + renderTotal(stats.downloaded),
            Modifier.weight(1f).testTag(AppleDownloadTestTags.DownloadSpeed),
        )
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        SpeedColumn(
            AppleIcons.arrowUp(),
            stringResource(Lang.cache_management_upload_label),
            speed = stats.uploadSpeed,
            total = stringResource(Lang.cache_total_upload) + " " + renderTotal(stats.uploaded),
            Modifier.weight(1f),
        )
    }
}

private fun renderTotal(size: FileSize): String = renderFileSize(size).ifEmpty { "–" }

/**
 * 一栏: 图标 14 + 标题 `footnote` label2; 速度 22/28 700; 累计 `caption` label2. 未知时显示「–」.
 */
@Composable
private fun SpeedColumn(
    icon: ImageVector,
    label: String,
    speed: FileSize,
    total: String,
    modifier: Modifier = Modifier,
) {
    Column(modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.secondary)
            Text(label, style = AniTextStyles.footnote, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
        }
        Text(
            renderSpeed(speed).ifEmpty { "–" },
            style = AniTextStyles.title.copy(fontSize = 22.sp, lineHeight = 28.sp, fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        Text(
            total,
            style = AniTextStyles.caption.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * C4 下载状态胶囊: 全部、下载中、已完成, 数量为符合条件的条目数.
 */
@Composable
private fun StatusPills(
    selected: DownloadStatusFilter?,
    counts: Map<DownloadStatusFilter?, Int>,
    onSelect: (DownloadStatusFilter?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        for (status in listOf(null, DownloadStatusFilter.Downloading, DownloadStatusFilter.Finished)) {
            PillTab(
                stringResource(
                    when (status) {
                        null -> Lang.cache_filter_status_all
                        DownloadStatusFilter.Downloading -> Lang.cache_filter_status_downloading
                        DownloadStatusFilter.Finished -> Lang.cache_filter_status_finished
                    },
                ),
                selected = status == selected,
                onClick = { onSelect(status) },
                Modifier.testTag(AppleDownloadTestTags.status(status)),
                count = counts[status]?.toString(),
            )
        }
    }
}

/**
 * 条目列: 内距 `12 12 12 16`; 封面 48×64 圆角 8; 番名 `body` W600; 摘要 `footnote` label2; 有失败时加一行 error;
 * 下载中或暂停时加进度条与速度 (暂停为「已暂停 N%」, 进度条改 label2). 分隔线从 76 开始 (选择模式从 110).
 *
 * 右侧: 下载中为暂停钮 (fill 底), 已暂停为继续钮 (tint 底、accent 图标), 其余为「›」. 选择模式下行首为勾选圆圈, 不显示右侧按钮.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GroupRow(
    group: SubjectDownloadGroup,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    showDivider: Boolean,
) {
    val summary = remember(group) { group.appleSummary() }
    Box(Modifier.testTag(AppleDownloadTestTags.group(group.subjectId))) {
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
                SelectionCircle(selected)
            }
            AppleCover(CoverAspect.Portrait3x4, group.subjectName, Modifier.width(48.dp), cornerRadius = 8.dp) {
                AsyncImage(group.imageUrl, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    group.subjectName,
                    style = AniTextStyles.body,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    summaryText(group, summary),
                    style = AniTextStyles.footnote.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (summary.failedCount > 0) {
                    Text(
                        stringResource(Lang.cache_management_failed_count, summary.failedCount),
                        style = AniTextStyles.footnote,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                    )
                }
                if (summary.state == GroupState.Downloading || summary.state == GroupState.Paused) {
                    val paused = summary.state == GroupState.Paused
                    Row(
                        Modifier.padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AppleProgressBar({ summary.progress }, Modifier.weight(1f), muted = paused)
                        Text(
                            if (paused) {
                                stringResource(Lang.cache_episode_status_paused) + " " + "${(summary.progress * 100).toInt()}%"
                            } else {
                                renderSpeed(group.downloadSpeed)
                            },
                            Modifier.widthIn(min = 64.dp),
                            style = AniTextStyles.caption.copy(fontFeatureSettings = "tnum"),
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.End,
                            maxLines = 1,
                        )
                    }
                }
            }
            if (!selectionMode) {
                when (summary.state) {
                    GroupState.Downloading -> RowActionButton(
                        AppleIcons.pauseFilled(),
                        stringResource(Lang.cache_episode_pause_download),
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        iconColor = MaterialTheme.colorScheme.onSurface,
                        onClick = onPause,
                        Modifier.testTag(AppleDownloadTestTags.pause(group.subjectId)),
                    )

                    GroupState.Paused -> RowActionButton(
                        AppleIcons.playFilled(),
                        stringResource(Lang.cache_episode_resume_download),
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        iconColor = MaterialTheme.colorScheme.primary,
                        onClick = onResume,
                        Modifier.testTag(AppleDownloadTestTags.resume(group.subjectId)),
                    )

                    GroupState.Idle -> Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
                        Icon(
                            AppleIcons.chevronRight(),
                            contentDescription = null,
                            Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
        if (showDivider) {
            // 选择模式下内容右移勾选圆圈 22 + 间距 12.
            AppleDivider(Modifier.align(Alignment.TopStart), startIndent = if (selectionMode) 110.dp else 76.dp)
        }
    }
}

/**
 * 摘要: 「8/9 已完成」之后接「N 个下载中」「已暂停」, 或全部完成时接总大小.
 */
@Composable
private fun summaryText(group: SubjectDownloadGroup, summary: AppleGroupSummary): String {
    val finished = stringResource(Lang.cache_management_finished_count, group.finishedCount, group.displayTotalCount)
    val suffix = when (summary.state) {
        GroupState.Downloading -> stringResource(Lang.cache_management_downloading_count, summary.downloadingCount)
        GroupState.Paused -> stringResource(Lang.cache_episode_status_paused)
        GroupState.Idle -> if (!group.hasUnfinished) renderFileSize(group.totalSize).ifEmpty { null } else null
    }
    return if (suffix == null) finished else "$finished · $suffix"
}

internal enum class GroupState {
    /** 有下载中的缓存. */
    Downloading,

    /** 没有下载中, 但有暂停的缓存. */
    Paused,

    /** 全部完成, 或只剩失败的缓存. */
    Idle,
}

/**
 * @param progress 未完成缓存的平均进度.
 */
internal class AppleGroupSummary(
    val state: GroupState,
    val downloadingCount: Int,
    val failedCount: Int,
    val progress: Float,
)

internal fun SubjectDownloadGroup.appleSummary(): AppleGroupSummary {
    val downloading = entries.count { it.status == DownloadStatus.IN_PROGRESS }
    val paused = entries.count { it.status == DownloadStatus.PAUSED }
    val unfinished = entries.filter { !it.isFinished && !it.isFailed }
    return AppleGroupSummary(
        state = when {
            downloading > 0 -> GroupState.Downloading
            paused > 0 -> GroupState.Paused
            else -> GroupState.Idle
        },
        downloadingCount = downloading,
        failedCount = entries.count { it.isFailed },
        progress = unfinished.map { it.progress.getOrZero() }.average().toFloat().takeIf { !it.isNaN() } ?: 0f,
    )
}

/**
 * 行右侧的 36 圆形按钮, 图标 14.
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
            .size(36.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, Modifier.size(14.dp), tint = iconColor)
    }
}

/**
 * 选择模式的勾选圆圈: 选中为 accent 底白勾, 未选中为 label2 圆框.
 */
@Composable
private fun SelectionCircle(selected: Boolean) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        Modifier
            .size(22.dp)
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(colorScheme.primary)
                } else {
                    Modifier.border(1.5.dp, colorScheme.secondary, CircleShape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(AppleIcons.check(3f), contentDescription = null, Modifier.size(13.dp), tint = colorScheme.onPrimary)
        }
    }
}

private val SelectionToolbarHeight = 56.dp

/**
 * 选择模式的浮动工具栏: tabBg 胶囊、floatingShadow, 放在浮动导航栏上方. 继续、暂停、删除各占一格.
 */
@Composable
private fun SelectionToolbar(
    resumeEnabled: Boolean,
    pauseEnabled: Boolean,
    deleteEnabled: Boolean,
    onResume: () -> Unit,
    onPause: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(SelectionToolbarHeight / 2)
    Row(
        modifier
            .fillMaxWidth()
            .height(SelectionToolbarHeight)
            .dropShadow(shape, Shadow(radius = 24.dp, color = AniColors.floatingShadowColor, offset = DpOffset(0.dp, 8.dp)))
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, AniColors.floatingBorder, shape)
            .testTag(AppleDownloadTestTags.Toolbar),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ToolbarAction(stringResource(Lang.cache_management_resume_selected), resumeEnabled, onResume, Modifier.weight(1f))
        ToolbarAction(stringResource(Lang.cache_management_pause_selected), pauseEnabled, onPause, Modifier.weight(1f))
        ToolbarAction(
            stringResource(Lang.cache_management_delete_selected),
            deleteEnabled,
            onDelete,
            Modifier.weight(1f).testTag(AppleDownloadTestTags.DeleteSelected),
        )
    }
}

@Composable
private fun ToolbarAction(text: String, enabled: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier
            .fillMaxHeight()
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = AniTextStyles.callout, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
    }
}

/**
 * 「筛选与排序」C14 底部面板: 排序、收藏状态、下载类型 (有多种下载类型时), 当前选项后有 accent 勾.
 */
@Composable
private fun FilterAndSortSheet(
    state: DownloadFilterAndSortState,
    engineOptions: List<MediaCacheEngineKey>,
    onDismissRequest: () -> Unit,
) {
    AppleModalBottomSheet(onDismissRequest) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .testTag(AppleDownloadTestTags.FilterSheet),
            verticalArrangement = Arrangement.spacedBy(GroupedListDefaults.SectionSpacing),
        ) {
            GroupedListSection(title = stringResource(Lang.cache_filter_sort)) {
                DownloadSortOption.entries.forEachIndexed { index, option ->
                    CheckRow(renderSortOption(option), checked = state.sortOption == option, showDivider = index > 0) {
                        state.sortOption = option
                    }
                }
            }
            GroupedListSection(title = stringResource(Lang.cache_filter_collection_state)) {
                CheckRow(stringResource(Lang.cache_filter_status_all), checked = state.selectedCollectionType == null) {
                    state.selectedCollectionType = null
                }
                for (type in UnifiedCollectionType.entries) {
                    CheckRow(renderCollectionType(type), checked = state.selectedCollectionType == type, showDivider = true) {
                        state.selectedCollectionType = type
                    }
                }
            }
            if (engineOptions.size > 1) {
                GroupedListSection(title = stringResource(Lang.cache_filter_cache_type)) {
                    CheckRow(stringResource(Lang.cache_filter_status_all), checked = state.selectedEngineKey == null) {
                        state.selectedEngineKey = null
                    }
                    for (engine in engineOptions) {
                        CheckRow(renderEngineKey(engine), checked = state.selectedEngineKey == engine, showDivider = true) {
                            state.selectedEngineKey = engine
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckRow(text: String, checked: Boolean, showDivider: Boolean = false, onClick: () -> Unit) {
    SettingsRow(
        text,
        Modifier.semantics { selected = checked },
        showDivider = showDivider,
        onClick = onClick,
    ) {
        if (checked) {
            Icon(AppleIcons.check(), contentDescription = null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

/**
 * C5 列表卡的一列: 第一列圆上角, 最后一列圆下角.
 */
private fun listCardShape(index: Int, count: Int): Shape {
    val top = if (index == 0) 16.dp else 0.dp
    val bottom = if (index == count - 1) 16.dp else 0.dp
    if (top == 0.dp && bottom == 0.dp) return RectangleShape
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

internal object AppleDownloadTestTags {
    const val FilterAndSort = "downloads-filter-and-sort"
    const val FilterSheet = "downloads-filter-sheet"
    const val Select = "downloads-select"
    const val SelectAll = "downloads-select-all"
    const val Done = "downloads-done"
    const val Toolbar = "downloads-selection-toolbar"
    const val DeleteSelected = "downloads-delete-selected"
    const val DownloadSpeed = "downloads-download-speed"
    fun status(status: DownloadStatusFilter?) = "downloads-status-${status ?: "all"}"
    fun group(subjectId: Int) = "downloads-group-$subjectId"
    fun pause(subjectId: Int) = "downloads-pause-$subjectId"
    fun resume(subjectId: Int) = "downloads-resume-$subjectId"
}
