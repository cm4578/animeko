/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.playback

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.him188.ani.app.ui.foundation.AsyncImage
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
import me.him188.ani.app.ui.foundation.apple.PushedPageHeader
import me.him188.ani.app.ui.foundation.apple.SettingsRowNavigation
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.navigation.BackHandler
import me.him188.ani.app.ui.foundation.rememberAsyncHandler
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.PullToRefreshBox
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_management_delete_selected
import me.him188.ani.app.ui.lang.cache_management_deselect_all
import me.him188.ani.app.ui.lang.cache_management_done
import me.him188.ani.app.ui.lang.playback_history_empty
import me.him188.ani.app.ui.lang.playback_history_enter_selection_mode
import me.him188.ani.app.ui.lang.playback_history_pending_sync
import me.him188.ani.app.ui.lang.playback_history_progress_sync
import me.him188.ani.app.ui.lang.playback_history_pull_to_sync
import me.him188.ani.app.ui.lang.playback_history_select_all
import me.him188.ani.app.ui.lang.playback_history_selected_count
import me.him188.ani.app.ui.lang.playback_history_sync_status_pending
import me.him188.ani.app.ui.lang.playback_history_sync_status_synced
import me.him188.ani.app.ui.lang.playback_history_title
import me.him188.ani.app.ui.lang.playback_history_unknown_subject
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

/**
 * Apple 版播放记录 (03 §4.2). 手机推入页.
 *
 * 从上到下: C3 返回钮与「選擇播放記錄」圆钮、标题; 同步状态列 (点击进入同步状态页); 按日期分组的 C5 列表.
 * 进入页面时同步一次, 下拉也会同步. 点一列播放该集.
 *
 * 选择模式 (05 D2) 由右上圆钮或长按一列进入: 左上为「全選 / 取消全選」, 右上为「完成」, 标题为「已選擇 N 筆」;
 * 行首为勾选圆圈, 同步状态列与下拉同步停用; 底部浮出只有「刪除所選」的工具栏, 删除前先确认. 返回也会离开选择模式.
 *
 * @param onOpenHistory 播放该集, 打开剧集页.
 * @param onOpenSyncStatus 打开 [PlaybackHistorySyncStatusScreen].
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun ApplePlaybackHistoryPage(
    vm: PlaybackHistoryViewModel,
    onOpenHistory: (PlaybackHistoryUiItem) -> Unit,
    onOpenSyncStatus: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val asyncHandler = rememberAsyncHandler()
    val histories by vm.stateFlow.collectAsStateWithLifecycle()
    val pendingSyncEpisodes by vm.pendingSyncEpisodesFlow.collectAsStateWithLifecycle()
    fun requestSync() {
        if (asyncHandler.isWorking) return
        asyncHandler.launch {
            vm.syncOnce()
        }
    }

    LaunchedEffect(vm) {
        requestSync()
    }

    ApplePlaybackHistoryLayout(
        histories = remember(histories) { histories.toUiItems() },
        pendingEpisodeIds = remember(pendingSyncEpisodes) { pendingSyncEpisodes.mapTo(hashSetOf()) { it.episodeId } },
        isSyncing = asyncHandler.isWorking,
        onSync = ::requestSync,
        onOpenHistory = onOpenHistory,
        onOpenSyncStatus = onOpenSyncStatus,
        onDelete = vm::delete,
        onBack = onBack,
        modifier = modifier,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
    )
}

/**
 * [ApplePlaybackHistoryPage] 的版面. 选择状态由本函数持有.
 *
 * @param pendingEpisodeIds 有待同步操作的剧集, 其数量即「有 N 項待同步」.
 */
@Composable
internal fun ApplePlaybackHistoryLayout(
    histories: List<PlaybackHistoryUiItem>,
    pendingEpisodeIds: Set<Int>,
    isSyncing: Boolean,
    onSync: () -> Unit,
    onOpenHistory: (PlaybackHistoryUiItem) -> Unit,
    onOpenSyncStatus: () -> Unit,
    onDelete: (Collection<Int>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
    clock: Clock = Clock.System,
) {
    var selectionMode by rememberSaveable { mutableStateOf(false) }
    var selectedEpisodeIds by rememberSaveable { mutableStateOf(emptySet<Int>()) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }
    val inSelection = selectionMode || selectedEpisodeIds.isNotEmpty()
    val allSelected = histories.isNotEmpty() && selectedEpisodeIds.size == histories.size
    fun exitSelection() {
        selectionMode = false
        selectedEpisodeIds = emptySet()
    }

    LaunchedEffect(histories) {
        val validIds = histories.mapTo(hashSetOf()) { it.episodeId }
        selectedEpisodeIds = selectedEpisodeIds.filterTo(hashSetOf()) { it in validIds }
    }
    BackHandler(inSelection) { exitSelection() }

    if (showDeleteDialog) {
        ApplePlaybackHistoryDeleteDialog(
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDelete(selectedEpisodeIds)
                exitSelection()
                showDeleteDialog = false
            },
        )
    }

    val groups = remember(histories, timeZone) { groupPlaybackHistoryByDay(histories, timeZone) }
    val today = remember(histories, timeZone, clock) { clock.now().toLocalDateTime(timeZone).date }
    val pullToRefreshState = rememberPullToRefreshState()

    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PullToRefreshBox(
            isRefreshing = isSyncing,
            onRefresh = onSync,
            Modifier.fillMaxSize(),
            state = pullToRefreshState,
            enabled = !inSelection,
            touchOnly = true,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.align(Alignment.TopCenter).windowInsetsPadding(windowInsets),
                    isRefreshing = isSyncing,
                    state = pullToRefreshState,
                )
            },
        ) {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = contentPadding) {
                item(key = "header") {
                    Header(
                        inSelection = inSelection,
                        selectedCount = selectedEpisodeIds.size,
                        allSelected = allSelected,
                        hasEntries = histories.isNotEmpty(),
                        onEnterSelection = { selectionMode = true },
                        onToggleSelectAll = {
                            selectionMode = true
                            selectedEpisodeIds = if (allSelected) emptySet() else histories.mapTo(hashSetOf()) { it.episodeId }
                        },
                        onDone = ::exitSelection,
                        onBack = onBack,
                        windowInsets = windowInsets,
                    )
                }
                item(key = "sync") {
                    SyncStatusRow(
                        pendingCount = pendingEpisodeIds.size,
                        enabled = !inSelection,
                        onClick = onOpenSyncStatus,
                        Modifier.padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 24.dp),
                    )
                }
                if (histories.isEmpty()) {
                    item(key = "empty") {
                        Text(
                            stringResource(Lang.playback_history_empty),
                            Modifier.fillMaxWidth().padding(vertical = 32.dp),
                            style = AniTextStyles.footnote,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
                groups.forEachIndexed { groupIndex, group ->
                    item(key = "day-${group.date}") {
                        Text(
                            playbackHistoryDayLabel(group.date, today),
                            Modifier
                                .padding(start = 32.dp, top = if (groupIndex > 0) 24.dp else 0.dp, end = 32.dp, bottom = 8.dp)
                                .testTag(ApplePlaybackHistoryTestTags.day(groupIndex)),
                            style = AniTextStyles.footnote,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                        )
                    }
                    itemsIndexed(group.items, key = { _, item -> item.episodeId }) { index, item ->
                        Box(
                            Modifier
                                .padding(horizontal = 16.dp)
                                .fillMaxWidth()
                                .clip(listCardShape(index, group.items.size))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow),
                        ) {
                            val selected = item.episodeId in selectedEpisodeIds
                            HistoryRow(
                                item,
                                pending = item.episodeId in pendingEpisodeIds,
                                selectionMode = inSelection,
                                selected = selected,
                                timeZone = timeZone,
                                onClick = {
                                    if (inSelection) {
                                        selectedEpisodeIds = selectedEpisodeIds.toggle(item.episodeId)
                                    } else {
                                        onOpenHistory(item)
                                    }
                                },
                                onLongClick = {
                                    selectionMode = true
                                    selectedEpisodeIds = selectedEpisodeIds + item.episodeId
                                },
                            )
                            if (index > 0) {
                                // 选择模式下内容右移勾选圆圈 22 + 间距 12.
                                AppleDivider(
                                    Modifier.align(Alignment.TopStart),
                                    startIndent = if (inSelection) 118.dp else 84.dp,
                                )
                            }
                        }
                    }
                }
                if (inSelection) {
                    // 让最后一列可以滚到选择工具栏上方.
                    item(key = "toolbar-space") { Spacer(Modifier.height(AppleSelectionToolbarDefaults.Height + 12.dp)) }
                }
            }
        }

        if (inSelection) {
            val layoutDirection = LocalLayoutDirection.current
            AppleSelectionToolbar(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(
                        start = contentPadding.calculateStartPadding(layoutDirection) + 16.dp,
                        end = contentPadding.calculateEndPadding(layoutDirection) + 16.dp,
                        bottom = contentPadding.calculateBottomPadding() + 12.dp,
                    )
                    .testTag(ApplePlaybackHistoryTestTags.Toolbar),
            ) {
                AppleSelectionToolbarAction(
                    stringResource(Lang.cache_management_delete_selected),
                    enabled = selectedEpisodeIds.isNotEmpty(),
                    onClick = { showDeleteDialog = true },
                    Modifier.testTag(ApplePlaybackHistoryTestTags.DeleteSelected),
                )
            }
        }
    }
}

/**
 * C3 页头. 一般为返回钮、「選擇播放記錄」圆钮与标题「播放記錄」; 选择模式为左上「全選 / 取消全選」、右上「完成」,
 * 标题「已選擇 N 筆」.
 */
@Composable
private fun Header(
    inSelection: Boolean,
    selectedCount: Int,
    allSelected: Boolean,
    hasEntries: Boolean,
    onEnterSelection: () -> Unit,
    onToggleSelectAll: () -> Unit,
    onDone: () -> Unit,
    onBack: () -> Unit,
    windowInsets: WindowInsets,
) {
    if (inSelection) {
        PushedPageHeader(
            stringResource(Lang.playback_history_selected_count, selectedCount),
            navigation = {
                AppleTextButton(
                    onToggleSelectAll,
                    Modifier.testTag(ApplePlaybackHistoryTestTags.SelectAll),
                    enabled = hasEntries,
                ) {
                    Text(
                        stringResource(if (allSelected) Lang.cache_management_deselect_all else Lang.playback_history_select_all),
                    )
                }
            },
            windowInsets = windowInsets,
        ) {
            AppleTextButton(onDone, Modifier.testTag(ApplePlaybackHistoryTestTags.Done)) {
                Text(stringResource(Lang.cache_management_done), fontWeight = FontWeight.SemiBold)
            }
        }
    } else {
        PushedPageHeader(stringResource(Lang.playback_history_title), onBack = onBack, windowInsets = windowInsets) {
            AppleCircleButton(
                onEnterSelection,
                AppleIcons.checkCircle(),
                stringResource(Lang.playback_history_enter_selection_mode),
                Modifier.testTag(ApplePlaybackHistoryTestTags.Select),
                enabled = hasEntries,
            )
        }
    }
}

/**
 * 同步状态列: card 高 60、圆角 16; 30 图标方块 + 「有 N 項待同步」`body` W600 + 「播放進度同步 · 下拉可重新同步」
 * `caption` label2 + `›`. 没有待同步操作时标题为「播放記錄已同步」.
 */
@Composable
private fun SyncStatusRow(
    pendingCount: Int,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
            .padding(start = 16.dp, end = 12.dp)
            .testTag(ApplePlaybackHistoryTestTags.SyncRow),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(30.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(AppleIcons.sync(), contentDescription = null, Modifier.size(17.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
        Column(Modifier.weight(1f)) {
            Text(
                if (pendingCount > 0) {
                    stringResource(Lang.playback_history_sync_status_pending, pendingCount)
                } else {
                    stringResource(Lang.playback_history_sync_status_synced)
                },
                style = AniTextStyles.body,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                stringResource(Lang.playback_history_progress_sync) + " · " + stringResource(Lang.playback_history_pull_to_sync),
                style = AniTextStyles.caption,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        SettingsRowNavigation()
    }
}

/**
 * 记录的一列: 内距 `12 16`; 封面 56×75 圆角 8; 番名 `body` W600; 「第 9 集 · 21:40」`footnote` label2, 待同步的加 accent
 * 云图标与「待同步」; 间距 8 后是 C9 进度条与「06:12 / 23:40」`caption` label2, 没看完为 accent、看完为 label2.
 * 选择模式下行首为勾选圆圈.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HistoryRow(
    item: PlaybackHistoryUiItem,
    pending: Boolean,
    selectionMode: Boolean,
    selected: Boolean,
    timeZone: TimeZone,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val subjectName = item.subjectName?.takeIf { it.isNotBlank() } ?: stringResource(Lang.playback_history_unknown_subject)
    Row(
        Modifier
            .fillMaxWidth()
            .combinedClickable(onLongClick = onLongClick, onClick = onClick)
            .then(if (selectionMode) Modifier.semantics { this.selected = selected } else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(ApplePlaybackHistoryTestTags.row(item.episodeId)),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selectionMode) {
            AppleSelectionCircle(selected)
        }
        AppleCover(CoverAspect.Portrait3x4, subjectName, Modifier.width(56.dp), cornerRadius = 8.dp) {
            AsyncImage(item.subjectImageUrl, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Column(Modifier.weight(1f)) {
            Text(
                subjectName,
                style = AniTextStyles.body,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    playbackHistoryEpisodeLine(item, timeZone),
                    Modifier.weight(1f, fill = false),
                    style = AniTextStyles.footnote.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (pending) {
                    PendingSyncMark(Modifier.testTag(ApplePlaybackHistoryTestTags.pending(item.episodeId)))
                }
            }
            val progress = item.progress
            Row(
                Modifier.padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (progress != null) {
                    AppleProgressBar({ progress }, Modifier.weight(1f), muted = item.isFinished)
                }
                Text(
                    playbackHistoryProgressText(item),
                    style = AniTextStyles.caption.copy(fontFeatureSettings = "tnum"),
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * 「待同步」标示: accent 云图标 + 文字.
 */
@Composable
private fun PendingSyncMark(modifier: Modifier = Modifier) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(3.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(AppleIcons.cloud(), contentDescription = null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
        Text(
            stringResource(Lang.playback_history_pending_sync),
            style = AniTextStyles.footnote,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
        )
    }
}

/**
 * 删除确认. 桌面的 Esc 由窗口交给 App 的返回分派, 对话框打开时返回只关闭对话框.
 */
@Composable
internal fun ApplePlaybackHistoryDeleteDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    BackHandler(onBack = onDismiss)
    PlaybackHistoryDeleteDialog(onDismiss = onDismiss, onConfirm = onConfirm)
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

internal object ApplePlaybackHistoryTestTags {
    const val Select = "apple-playback-history-select"
    const val SelectAll = "apple-playback-history-select-all"
    const val Done = "apple-playback-history-done"
    const val SyncRow = "apple-playback-history-sync-row"
    const val Toolbar = "apple-playback-history-selection-toolbar"
    const val DeleteSelected = "apple-playback-history-delete-selected"
    fun day(index: Int) = "apple-playback-history-day-$index"
    fun row(episodeId: Int) = "apple-playback-history-row-$episodeId"
    fun pending(episodeId: Int) = "apple-playback-history-pending-$episodeId"
}
