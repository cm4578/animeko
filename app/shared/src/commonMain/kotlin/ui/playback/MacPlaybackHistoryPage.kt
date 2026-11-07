/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.playback

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.apple.AppleButton
import me.him188.ani.app.ui.foundation.apple.AppleButtonStyle
import me.him188.ani.app.ui.foundation.apple.AppleCover
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleProgressBar
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.CoverAspect
import me.him188.ani.app.ui.foundation.apple.MacLargeTitleHeader
import me.him188.ani.app.ui.foundation.apple.MacPageToolbar
import me.him188.ani.app.ui.foundation.apple.MacTableColumn
import me.him188.ani.app.ui.foundation.apple.MacTableColumns
import me.him188.ani.app.ui.foundation.apple.MacTableRow
import me.him188.ani.app.ui.foundation.apple.MacToolbarTextButton
import me.him188.ani.app.ui.foundation.navigation.BackHandler
import me.him188.ani.app.ui.foundation.rememberAsyncHandler
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_management_delete_selected
import me.him188.ani.app.ui.lang.cache_management_deselect_all
import me.him188.ani.app.ui.lang.cache_management_done
import me.him188.ani.app.ui.lang.cache_management_select
import me.him188.ani.app.ui.lang.playback_history_empty
import me.him188.ani.app.ui.lang.playback_history_pending_sync
import me.him188.ani.app.ui.lang.playback_history_select_all
import me.him188.ani.app.ui.lang.playback_history_selected_count
import me.him188.ani.app.ui.lang.playback_history_sync_hint
import me.him188.ani.app.ui.lang.playback_history_sync_now
import me.him188.ani.app.ui.lang.playback_history_sync_pending_episode
import me.him188.ani.app.ui.lang.playback_history_sync_source
import me.him188.ani.app.ui.lang.playback_history_sync_status_pending
import me.him188.ani.app.ui.lang.playback_history_sync_status_synced
import me.him188.ani.app.ui.lang.playback_history_sync_status_title
import me.him188.ani.app.ui.lang.playback_history_title
import me.him188.ani.app.ui.lang.playback_history_unknown_subject
import me.him188.ani.app.ui.lang.video_player_play
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

/**
 * macOS 版播放记录 (04 §4.2).
 *
 * 工具栏右侧为「選擇」. 大标题「播放記錄」下方分两栏: 左栏为按日期分组的记录, 右栏 296 为同步卡 (待同步队列、「立即同步」、
 * 「同步狀態」) 与卡下提示. 内容区窄于 [MacPlaybackHistoryDefaults.TwoColumnMinWidth] 时同步卡放在记录上方.
 * 进入页面时同步一次. 点一列或右侧播放钮播放该集.
 *
 * 选择模式 (05 D2) 由「選擇」进入: 工具栏为「已選擇 N 筆」、全選 / 取消全選、刪除所選、完成; 点一列切换选取 (tint 底),
 * 不显示播放钮; 同步卡照常可用. 删除前先确认, Esc 离开选择模式.
 *
 * @param onOpenHistory 播放该集, 打开剧集页.
 * @param onOpenSyncStatus 打开 [PlaybackHistorySyncStatusScreen].
 */
@Composable
fun MacPlaybackHistoryPage(
    vm: PlaybackHistoryViewModel,
    onOpenHistory: (PlaybackHistoryUiItem) -> Unit,
    onOpenSyncStatus: () -> Unit,
    modifier: Modifier = Modifier,
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

    MacPlaybackHistoryLayout(
        histories = remember(histories) { histories.toUiItems() },
        pendingItems = pendingSyncEpisodes.toSyncStatusUiItems(),
        isSyncing = asyncHandler.isWorking,
        onSync = ::requestSync,
        onOpenHistory = onOpenHistory,
        onOpenSyncStatus = onOpenSyncStatus,
        onDelete = vm::delete,
        modifier = modifier,
    )
}

/**
 * [MacPlaybackHistoryPage] 的版面. 选择状态由本函数持有.
 *
 * @param pendingItems 待同步队列, 一集一项. 其中的剧集在记录上标示「待同步」.
 */
@Composable
internal fun MacPlaybackHistoryLayout(
    histories: List<PlaybackHistoryUiItem>,
    pendingItems: List<PlaybackHistorySyncStatusUiItem>,
    isSyncing: Boolean,
    onSync: () -> Unit,
    onOpenHistory: (PlaybackHistoryUiItem) -> Unit,
    onOpenSyncStatus: () -> Unit,
    onDelete: (Collection<Int>) -> Unit,
    modifier: Modifier = Modifier,
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
    val pendingEpisodeIds = remember(pendingItems) { pendingItems.mapTo(hashSetOf()) { it.episodeId } }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        MacPageToolbar {
            if (inSelection) {
                Text(
                    stringResource(Lang.playback_history_selected_count, selectedEpisodeIds.size),
                    Modifier.padding(end = 8.dp),
                    style = AniTextStyles.macCallout,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                )
                MacToolbarTextButton(
                    stringResource(if (allSelected) Lang.cache_management_deselect_all else Lang.playback_history_select_all),
                    onClick = {
                        selectedEpisodeIds = if (allSelected) emptySet() else histories.mapTo(hashSetOf()) { it.episodeId }
                    },
                    Modifier.testTag(MacPlaybackHistoryTestTags.SelectAll),
                    enabled = histories.isNotEmpty(),
                )
                MacToolbarTextButton(
                    stringResource(Lang.cache_management_delete_selected),
                    onClick = { showDeleteDialog = true },
                    Modifier.testTag(MacPlaybackHistoryTestTags.DeleteSelected),
                    enabled = selectedEpisodeIds.isNotEmpty(),
                )
                MacToolbarTextButton(
                    stringResource(Lang.cache_management_done),
                    onClick = ::exitSelection,
                    Modifier.testTag(MacPlaybackHistoryTestTags.Done),
                )
            } else {
                MacToolbarTextButton(
                    stringResource(Lang.cache_management_select),
                    onClick = { selectionMode = true },
                    Modifier.testTag(MacPlaybackHistoryTestTags.Select),
                    enabled = histories.isNotEmpty(),
                )
            }
        }
        MacLargeTitleHeader(
            stringResource(Lang.playback_history_title),
            Modifier.padding(start = 32.dp, top = 4.dp, end = 32.dp),
        )
        BoxWithConstraints(Modifier.fillMaxSize().padding(start = 32.dp, top = 20.dp, end = 32.dp)) {
            val onClickItem: (PlaybackHistoryUiItem) -> Unit = { item ->
                if (inSelection) {
                    selectedEpisodeIds = selectedEpisodeIds.toggle(item.episodeId)
                } else {
                    onOpenHistory(item)
                }
            }
            val twoColumns = maxWidth >= MacPlaybackHistoryDefaults.TwoColumnMinWidth
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                HistoryList(
                    groups,
                    today = today,
                    pendingEpisodeIds = pendingEpisodeIds,
                    selectionMode = inSelection,
                    selectedEpisodeIds = selectedEpisodeIds,
                    timeZone = timeZone,
                    onClick = onClickItem,
                    onPlay = onOpenHistory,
                    Modifier.weight(1f).fillMaxHeight(),
                    leading = if (twoColumns) {
                        null
                    } else {
                        {
                            SyncColumn(pendingItems, isSyncing, onSync, onOpenSyncStatus, Modifier.padding(bottom = 20.dp))
                        }
                    },
                )
                if (twoColumns) {
                    SyncColumn(
                        pendingItems,
                        isSyncing,
                        onSync,
                        onOpenSyncStatus,
                        Modifier
                            .width(MacPlaybackHistoryDefaults.SidebarWidth)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 32.dp),
                    )
                }
            }
        }
    }
}

/**
 * 右栏: 同步卡与卡下提示 12 label2.
 */
@Composable
private fun SyncColumn(
    pendingItems: List<PlaybackHistorySyncStatusUiItem>,
    isSyncing: Boolean,
    onSync: () -> Unit,
    onOpenSyncStatus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SyncCard(pendingItems, isSyncing, onSync, onOpenSyncStatus)
        Text(
            stringResource(Lang.playback_history_sync_hint),
            Modifier.padding(top = 10.dp).testTag(MacPlaybackHistoryTestTags.Hint),
            style = AniTextStyles.macCaption,
            color = MaterialTheme.colorScheme.secondary,
        )
    }
}

/**
 * 左栏: 按日期分组, 组标题 13/600 label2, 组间距 20; 每组一张 card 圆角 14. 没有记录时显示现有的「暫無播放記錄」.
 *
 * @param leading 放在第一组之前的内容, 窄版面时为同步卡.
 */
@Composable
private fun HistoryList(
    groups: List<PlaybackHistoryDayGroup>,
    today: LocalDate,
    pendingEpisodeIds: Set<Int>,
    selectionMode: Boolean,
    selectedEpisodeIds: Set<Int>,
    timeZone: TimeZone,
    onClick: (PlaybackHistoryUiItem) -> Unit,
    onPlay: (PlaybackHistoryUiItem) -> Unit,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    LazyColumn(modifier, contentPadding = PaddingValues(bottom = 32.dp)) {
        if (leading != null) {
            item(key = "leading") { leading() }
        }
        if (groups.isEmpty()) {
            item(key = "empty") {
                Text(
                    stringResource(Lang.playback_history_empty),
                    Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    style = AniTextStyles.macCallout,
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
                        .padding(top = if (groupIndex > 0) 20.dp else 0.dp, bottom = 8.dp)
                        .testTag(MacPlaybackHistoryTestTags.day(groupIndex)),
                    style = AniTextStyles.macCallout,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                )
            }
            itemsIndexed(group.items, key = { _, item -> item.episodeId }) { index, item ->
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(cardShape(top = index == 0, bottom = index == group.items.lastIndex))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    HistoryRow(
                        item,
                        pending = item.episodeId in pendingEpisodeIds,
                        selectionMode = selectionMode,
                        selected = selectionMode && item.episodeId in selectedEpisodeIds,
                        showDivider = index > 0,
                        timeZone = timeZone,
                        onClick = { onClick(item) },
                        onPlay = { onPlay(item) },
                    )
                }
            }
        }
    }
}

/**
 * 表格列: 作品 (弹性) / 进度 220 / 操作 28, 列间距 16, 左右内距 16.
 */
private val MacPlaybackHistoryColumns = MacTableColumns(
    listOf(
        MacTableColumn(weight = 1f),
        MacTableColumn(width = 220.dp),
        MacTableColumn(width = 28.dp, alignment = Alignment.CenterHorizontally),
    ),
    horizontalPadding = 16.dp,
    spacing = 16.dp,
)

/**
 * 记录的一行: 高 76, 分隔线从 74 开始. 作品为 42×56 封面 + 番名 `macBody` W600 + 「第 9 集 · 21:40」`macCaption` label2,
 * 待同步的加 accent 云图标与「待同步」; 进度为 4 高的进度条 (没看完 accent、看完 label2) + 右对齐至少 92 宽的
 * 「06:12 / 23:40」12 号等宽数字; 操作为 28 圆形播放钮 (fill 底), 选择模式下不显示.
 */
@Composable
private fun HistoryRow(
    item: PlaybackHistoryUiItem,
    pending: Boolean,
    selectionMode: Boolean,
    selected: Boolean,
    showDivider: Boolean,
    timeZone: TimeZone,
    onClick: () -> Unit,
    onPlay: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val subjectName = item.subjectName?.takeIf { it.isNotBlank() } ?: stringResource(Lang.playback_history_unknown_subject)
    MacTableRow(
        MacPlaybackHistoryColumns,
        Modifier.testTag(MacPlaybackHistoryTestTags.row(item.episodeId)),
        height = 76.dp,
        selected = selected,
        showDivider = showDivider,
        dividerStartIndent = 74.dp,
        onClick = onClick,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
            AppleCover(CoverAspect.Portrait3x4, subjectName, Modifier.width(42.dp), cornerRadius = 6.dp) {
                AsyncImage(item.subjectImageUrl, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    subjectName,
                    style = AniTextStyles.macBody,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        playbackHistoryEpisodeLine(item, timeZone),
                        Modifier.weight(1f, fill = false),
                        style = AniTextStyles.macCaption.copy(fontFeatureSettings = "tnum"),
                        color = colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (pending) {
                        Row(
                            Modifier.testTag(MacPlaybackHistoryTestTags.pending(item.episodeId)),
                            horizontalArrangement = Arrangement.spacedBy(3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(AppleIcons.cloud(), contentDescription = null, Modifier.size(13.dp), tint = colorScheme.primary)
                            Text(
                                stringResource(Lang.playback_history_pending_sync),
                                style = AniTextStyles.macCaption,
                                color = colorScheme.primary,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            val progress = item.progress
            if (progress != null) {
                AppleProgressBar({ progress }, Modifier.weight(1f), muted = item.isFinished)
            } else {
                Spacer(Modifier.weight(1f))
            }
            Text(
                playbackHistoryProgressText(item),
                Modifier.widthIn(min = 92.dp),
                style = AniTextStyles.macCaption.copy(fontFeatureSettings = "tnum"),
                color = colorScheme.secondary,
                textAlign = TextAlign.End,
                maxLines = 1,
            )
        }
        if (selectionMode) {
            Box {}
        } else {
            val playText = stringResource(Lang.video_player_play)
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(colorScheme.surfaceContainerHighest)
                    .clickable(role = Role.Button, onClick = onPlay)
                    .semantics { contentDescription = playText }
                    .testTag(MacPlaybackHistoryTestTags.play(item.episodeId)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(AppleIcons.playFilled(), contentDescription = null, Modifier.size(12.dp), tint = colorScheme.onSurface)
            }
        }
    }
}

/**
 * 同步卡: card 圆角 14、内距 16. 「Bangumi 播放進度」12 label2 + 「有 N 項待同步」17/700; 待同步队列每项为番名 13/600
 * 与操作 12 label2 (操作名后接集名, 同同步状态页); 底部「立即同步」(tint 底、高 28) 与 accent「同步狀態」.
 * 没有待同步操作时标题为「播放記錄已同步」, 不显示队列.
 */
@Composable
private fun SyncCard(
    pendingItems: List<PlaybackHistorySyncStatusUiItem>,
    isSyncing: Boolean,
    onSync: () -> Unit,
    onOpenSyncStatus: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier
            .fillMaxWidth()
            .testTag(MacPlaybackHistoryTestTags.SyncCard)
            .clip(RoundedCornerShape(14.dp))
            .background(colorScheme.surfaceContainerLow)
            .padding(16.dp),
    ) {
        Text(
            stringResource(Lang.playback_history_sync_source),
            style = AniTextStyles.macCaption,
            color = colorScheme.secondary,
            maxLines = 1,
        )
        Text(
            if (pendingItems.isNotEmpty()) {
                stringResource(Lang.playback_history_sync_status_pending, pendingItems.size)
            } else {
                stringResource(Lang.playback_history_sync_status_synced)
            },
            Modifier.padding(top = 2.dp),
            style = AniTextStyles.macTitle,
            color = colorScheme.onSurface,
        )
        if (pendingItems.isNotEmpty()) {
            Column(Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (item in pendingItems) {
                    Column(Modifier.testTag(MacPlaybackHistoryTestTags.queueItem(item.episodeId))) {
                        Text(
                            item.subjectName?.takeIf { it.isNotBlank() }
                                ?: stringResource(Lang.playback_history_sync_pending_episode, item.episodeId),
                            style = AniTextStyles.macCallout,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            (item.operationNames + listOfNotNull(item.episodeName?.takeIf { it.isNotBlank() })).joinToString(" · "),
                            style = AniTextStyles.macCaption,
                            color = colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
        Row(
            Modifier.padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppleButton(
                onSync,
                Modifier.testTag(MacPlaybackHistoryTestTags.SyncNow),
                style = AppleButtonStyle.Tinted,
                enabled = !isSyncing,
            ) {
                Text(stringResource(Lang.playback_history_sync_now))
            }
            AppleTextButton(onOpenSyncStatus, Modifier.testTag(MacPlaybackHistoryTestTags.SyncStatus)) {
                Text(stringResource(Lang.playback_history_sync_status_title))
            }
        }
    }
}

private fun cardShape(top: Boolean, bottom: Boolean): Shape = RoundedCornerShape(
    topStart = if (top) 14.dp else 0.dp,
    topEnd = if (top) 14.dp else 0.dp,
    bottomStart = if (bottom) 14.dp else 0.dp,
    bottomEnd = if (bottom) 14.dp else 0.dp,
)

internal object MacPlaybackHistoryDefaults {
    /** 右栏宽度. */
    val SidebarWidth = 296.dp

    /** 两栏版面需要的内容宽度 (扣除左右内距): 左栏 480 + 栏间距 24 + 右栏 296. */
    val TwoColumnMinWidth = 800.dp
}

internal object MacPlaybackHistoryTestTags {
    const val Select = "mac-playback-history-select"
    const val SelectAll = "mac-playback-history-select-all"
    const val DeleteSelected = "mac-playback-history-delete-selected"
    const val Done = "mac-playback-history-done"
    const val SyncCard = "mac-playback-history-sync-card"
    const val SyncNow = "mac-playback-history-sync-now"
    const val SyncStatus = "mac-playback-history-sync-status"
    const val Hint = "mac-playback-history-hint"
    fun day(index: Int) = "mac-playback-history-day-$index"
    fun row(episodeId: Int) = "mac-playback-history-row-$episodeId"
    fun play(episodeId: Int) = "mac-playback-history-play-$episodeId"
    fun pending(episodeId: Int) = "mac-playback-history-pending-$episodeId"
    fun queueItem(episodeId: Int) = "mac-playback-history-queue-$episodeId"
}
