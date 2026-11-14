/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.mediaselect.apple

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import me.him188.ani.app.domain.media.selector.SelectTrigger
import me.him188.ani.app.ui.foundation.apple.ApplePopoverMenu
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.FilterPill
import me.him188.ani.app.ui.foundation.apple.MacTable
import me.him188.ani.app.ui.foundation.apple.MacTableColumn
import me.him188.ani.app.ui.foundation.apple.MacTableColumns
import me.him188.ani.app.ui.foundation.apple.MacTableHeader
import me.him188.ani.app.ui.foundation.apple.MacTableHeaderCell
import me.him188.ani.app.ui.foundation.apple.MacTableRow
import me.him188.ani.app.ui.foundation.apple.PopoverMenuItem
import me.him188.ani.app.ui.foundation.setClipEntryText
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.media_selector_bt_col_alliance
import me.him188.ani.app.ui.lang.media_selector_bt_col_published
import me.him188.ani.app.ui.lang.media_selector_bt_col_resolution
import me.him188.ani.app.ui.lang.media_selector_bt_col_size
import me.him188.ani.app.ui.lang.media_selector_bt_col_subtitle
import me.him188.ani.app.ui.lang.media_selector_bt_col_title
import me.him188.ani.app.ui.lang.media_selector_bt_filter
import me.him188.ani.app.ui.lang.media_selector_bt_filter_any
import me.him188.ani.app.ui.lang.media_selector_bt_filter_count
import me.him188.ani.app.ui.lang.media_selector_bt_filter_reset
import me.him188.ani.app.ui.lang.media_selector_bt_show_excluded
import me.him188.ani.app.ui.lang.media_selector_bt_source_all
import me.him188.ani.app.ui.lang.media_selector_bt_source_count
import me.him188.ani.app.ui.lang.media_selector_bt_source_failed
import me.him188.ani.app.ui.lang.media_selector_bt_source_querying
import me.him188.ani.app.ui.lang.media_selector_filter_alliance
import me.him188.ani.app.ui.lang.media_selector_filter_resolution
import me.him188.ani.app.ui.lang.media_selector_filter_subtitle
import me.him188.ani.app.ui.lang.media_selector_mac_cancel
import me.him188.ani.app.ui.lang.media_selector_mac_filter_summary
import me.him188.ani.app.ui.lang.media_selector_mac_play_selected
import me.him188.ani.app.ui.lang.settings_debug_copied
import me.him188.ani.app.ui.media.rememberMediaDetailsStrings
import me.him188.ani.app.ui.media.renderSubtitleLanguage
import me.him188.ani.app.ui.mediafetch.MediaPreferenceItemState
import me.him188.ani.app.ui.mediafetch.MediaSelectorState
import me.him188.ani.app.ui.mediafetch.MediaSourceResultListPresentation
import me.him188.ani.app.ui.mediaselect.WatchingEpisode
import me.him188.ani.app.ui.mediaselect.bt.BtLabelStrings
import me.him188.ani.app.ui.mediaselect.bt.BtListPresentation
import me.him188.ani.app.ui.mediaselect.bt.BtRow
import me.him188.ani.app.ui.mediaselect.bt.formatBtSize
import me.him188.ani.app.ui.mediaselect.bt.formatPublishedMMdd
import me.him188.ani.app.ui.mediaselect.bt.rememberBtLabelStrings
import me.him188.ani.app.ui.mediaselect.common.episodeLabel
import me.him188.ani.datasources.api.source.MediaFetchRequest
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.isSingleEpisode
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 版 BT 资源 (04 §3.9, 05 G39).
 *
 * 顶部是搜索框 (同手动查找; 提交新关键字重新查询, 与现有页面相同). 工具行: 「第 N 話」按集筛选开关、
 * 「全部數據源 N 條 ▾」(tint 胶囊, 数据源菜单, 纯界面过滤)、「篩選 (N)」(card 胶囊, 分辨率 / 字幕 / 字幕组筛选),
 * 右侧 accent「顯示已被排除的 N 條資源」(只在本次打开期间有效).
 *
 * C19 表格: 表头高 30, 行至少 52, 标题完整换行; 点行只勾选 (单选, 默认为正在播放的资源), 长按复制磁力链.
 * 底部左侧「已套用 N 個篩選條件，顯示 M 條」, 右侧「取消」与「播放所選資源」; 播放所选资源是正式选择, 之后调用 [onPlayed].
 *
 * @param timeZone 发布日期 MM-dd 的时区.
 */
@Composable
internal fun MacBtResourcesContent(
    state: MediaSelectorState,
    sourceResults: MediaSourceResultListPresentation,
    watching: WatchingEpisode?,
    fetchRequest: MediaFetchRequest?,
    onFetchRequestChange: (MediaFetchRequest) -> Unit,
    onRestartSource: (instanceId: String) -> Unit,
    onPlayed: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
) {
    val presentation by state.btPresentationFlow.collectAsStateWithLifecycle()
    val episodeFilterEnabled by state.btFilterState.episodeFilterEnabled.collectAsStateWithLifecycle()
    val sourceFilter by state.btFilterState.sourceFilter.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    val clipboard = LocalClipboard.current

    var revealExcluded by rememberSaveable { mutableStateOf(false) }
    // 用户勾选的资源; null 时勾选正在播放的资源.
    var checkedId by rememberSaveable { mutableStateOf<String?>(null) }

    val btSourceIds = sourceResults.btSources.map { it.mediaSourceId }
    LaunchedEffect(btSourceIds, sourceFilter) {
        if (sourceFilter != null && sourceFilter !in btSourceIds) {
            state.btFilterState.sourceFilter.value = null
        }
    }

    val initialKeyword = fetchRequest?.subjectNames?.firstOrNull() ?: ""
    var keyword by rememberSaveable(initialKeyword) { mutableStateOf(initialKeyword) }
    val submitKeyword = {
        val request = fetchRequest
        val trimmed = keyword.trim()
        if (request != null && trimmed.isNotEmpty()) {
            onFetchRequestChange(request.copy(subjectNames = listOf(trimmed), subjectNameCN = trimmed))
        }
    }

    val rows = if (revealExcluded) presentation.included + presentation.excluded else presentation.included
    val checkedRow = rows.firstOrNull { it.id == (checkedId ?: presentation.selected?.mediaId) }
    // 占位 presentation 与「BT 源仍在查询」都不是「没有资源」.
    val isLoading = presentation.isPlaceholder || sourceResults.btSources.any { it.isWorking }

    val copyMagnet: (BtRow) -> Unit = { row ->
        val download = row.media.download
        if (download is ResourceLocation.MagnetLink) {
            scope.launch {
                clipboard.setClipEntryText(download.uri)
                toaster.toast(getString(Lang.settings_debug_copied, download.uri))
            }
        }
    }

    Column(modifier) {
        Column(Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp)) {
            MacSelectorSearchField(
                keyword,
                onKeywordChange = { keyword = it },
                onSearch = submitKeyword,
                Modifier.fillMaxWidth(),
                enabled = fetchRequest != null,
            )
            Row(
                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (watching != null) {
                    MacTogglePill(
                        watching.episodeLabel(),
                        selected = episodeFilterEnabled,
                        onClick = { state.btFilterState.episodeFilterEnabled.value = !episodeFilterEnabled },
                        Modifier.testTag(MacMediaSelectorTestTags.EpisodeFilter),
                    )
                }
                MacBtSourceButton(
                    presentation,
                    sourceResults,
                    onSelect = { state.btFilterState.sourceFilter.value = it },
                    onRestartSource = onRestartSource,
                )
                MacBtFilterButton(
                    presentation,
                    resolution = state.resolution,
                    subtitleLanguageId = state.subtitleLanguageId,
                    alliance = state.alliance,
                )
                Spacer(Modifier.weight(1f))
                if (presentation.excluded.isNotEmpty() && !revealExcluded) {
                    AppleTextButton(
                        { revealExcluded = true },
                        Modifier.testTag(MacMediaSelectorTestTags.ShowExcluded),
                    ) {
                        Text(
                            stringResource(Lang.media_selector_bt_show_excluded, presentation.excluded.size),
                            style = AniTextStyles.macCallout,
                            maxLines = 1,
                        )
                    }
                }
            }
            MacBtTable(
                rows,
                checkedId = checkedRow?.id,
                selected = presentation.selected?.mediaId,
                onCheck = { checkedId = it.id },
                onLongClick = copyMagnet,
                isLoading = isLoading,
                timeZone = timeZone,
                Modifier.weight(1f).padding(bottom = 16.dp),
            )
        }
        MacSelectorBottomBar {
            Text(
                stringResource(Lang.media_selector_mac_filter_summary, presentation.activeFilterCount, rows.size),
                Modifier.testTag(MacMediaSelectorTestTags.FilterSummary),
                style = AniTextStyles.macCaption,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
            )
            Spacer(Modifier.weight(1f))
            MacSelectorButton(
                stringResource(Lang.media_selector_mac_cancel),
                onClick = onCancel,
                Modifier.testTag(MacMediaSelectorTestTags.Cancel),
                primary = false,
            )
            MacSelectorButton(
                stringResource(Lang.media_selector_mac_play_selected),
                onClick = {
                    val media = checkedRow?.media ?: return@MacSelectorButton
                    state.select(media, SelectTrigger.MANUAL_BT_PAGE)
                    onPlayed()
                },
                Modifier.testTag(MacMediaSelectorTestTags.Play),
                enabled = checkedRow != null && checkedRow.media != presentation.selected,
            )
        }
    }
}

/**
 * C19 表格: 表头高 30, 左右 12/16, 列间距 12. 列为勾选 16、标题 (弹性, 完整换行)、字幕组 72、解析度 52、字幕 40、大小 60 (右)、发布 48 (右).
 * 勾选的行 tint 底; 排除的行为 label2 字, 标题前加原因标签.
 */
@Composable
private fun MacBtTable(
    rows: List<BtRow>,
    checkedId: String?,
    selected: String?,
    onCheck: (BtRow) -> Unit,
    onLongClick: (BtRow) -> Unit,
    isLoading: Boolean,
    timeZone: TimeZone,
    modifier: Modifier = Modifier,
) {
    val strings = rememberBtLabelStrings()
    val columns = remember {
        MacTableColumns(
            listOf(
                MacTableColumn(16.dp, alignment = Alignment.CenterHorizontally),
                MacTableColumn(weight = 1f),
                MacTableColumn(72.dp),
                MacTableColumn(52.dp),
                MacTableColumn(40.dp),
                MacTableColumn(60.dp, alignment = Alignment.End),
                MacTableColumn(48.dp, alignment = Alignment.End),
            ),
            horizontalPadding = 12.dp,
            spacing = 12.dp,
            endPadding = 16.dp,
        )
    }
    val listState = rememberLazyListState()
    // 正在播放的资源只在首次出现在列表时定位一次, 之后筛选改变它的位置时不滚动.
    var scrolledTo by remember { mutableStateOf<String?>(null) }
    val selectedIndex = rows.indexOfFirst { it.id == selected }
    LaunchedEffect(selected, selectedIndex) {
        if (selected != null && selectedIndex >= 0 && selected != scrolledTo) {
            scrolledTo = selected
            listState.scrollToItem(selectedIndex)
        }
    }
    MacTable(modifier.testTag(MacMediaSelectorTestTags.Table)) {
        MacTableHeader(columns, height = 30.dp) {
            Spacer(Modifier)
            MacTableHeaderCell(stringResource(Lang.media_selector_bt_col_title))
            MacTableHeaderCell(stringResource(Lang.media_selector_bt_col_alliance))
            MacTableHeaderCell(stringResource(Lang.media_selector_bt_col_resolution))
            MacTableHeaderCell(stringResource(Lang.media_selector_bt_col_subtitle))
            MacTableHeaderCell(stringResource(Lang.media_selector_bt_col_size))
            MacTableHeaderCell(stringResource(Lang.media_selector_bt_col_published))
        }
        if (rows.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    MacSelectorEmptyText()
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), listState) {
                itemsIndexed(rows, key = { _, row -> row.id }) { index, row ->
                    MacBtTableRow(
                        row,
                        columns,
                        strings,
                        checked = row.id == checkedId,
                        showDivider = index > 0,
                        onClick = { onCheck(row) },
                        onLongClick = { onLongClick(row) },
                        timeZone = timeZone,
                    )
                }
            }
        }
    }
}

@Composable
private fun MacBtTableRow(
    row: BtRow,
    columns: MacTableColumns,
    strings: BtLabelStrings,
    checked: Boolean,
    showDivider: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    timeZone: TimeZone,
) {
    val media = row.media
    val color = when {
        checked -> MaterialTheme.colorScheme.onSecondaryContainer
        row.isExcluded -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.onSurface
    }
    val style = AniTextStyles.macCallout
    val numbers = style.copy(fontFeatureSettings = "tnum")
    MacTableRow(
        columns,
        Modifier.testTag(MacMediaSelectorTestTags.btRow(row.id)),
        selected = checked,
        showDivider = showDivider,
        dividerStartIndent = 40.dp,
        onClick = onClick,
        wrapContentHeight = true,
        onLongClick = onLongClick,
    ) {
        MacCheckbox(checked, onCheckedChange = null)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (row.isCached) MacBtBadge(strings.cached)
            row.exclusion?.let { MacBtBadge(strings.exclusion(it)) }
            if (media.episodeRange?.isSingleEpisode() == false) MacBtBadge(strings.collection)
            Text(media.originalTitle, style = style, color = color)
        }
        Text(media.properties.alliance, style = style, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(media.properties.resolution, style = style, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(strings.subtitleCell(row), style = style, color = color, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(formatBtSize(media.properties.size), style = numbers, color = color, maxLines = 1)
        Text(formatPublishedMMdd(media.publishedTime, timeZone), style = numbers, color = color, maxLines = 1)
    }
}

/**
 * 标题前的小标 (合集、已缓存、排除原因): 高 18、圆角 5、fill 底, 11 W600 label2.
 */
@Composable
private fun MacBtBadge(text: String) {
    Box(
        Modifier
            .padding(top = 0.5.dp)
            .height(18.dp)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = AniTextStyles.macCaption.copy(fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            softWrap = false,
        )
    }
}

/**
 * 开关胶囊 (高 28、圆角 14、左右 12): 开为 tint 底 + onTint W600, 关为 card 底 + labelSoft W500.
 */
@Composable
private fun MacTogglePill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .height(28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainerLow)
            .selectable(selected = selected, role = Role.Checkbox, onClick = onClick)
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text,
            style = AniTextStyles.macCallout,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else AniColors.labelSoft,
            maxLines = 1,
        )
    }
}

/**
 * 「全部數據源 N 條 ▾」: tint 胶囊, 选了具体的源时显示源名与条数. 打开 C15 菜单, 每列为源名与状态 (条数、查询中、查询失败), 当前选择有勾号.
 * 查询失败或停用的源点击后重新查询, 与现有的数据源下拉相同.
 */
@Composable
private fun MacBtSourceButton(
    presentation: BtListPresentation,
    sourceResults: MediaSourceResultListPresentation,
    onSelect: (mediaSourceId: String?) -> Unit,
    onRestartSource: (instanceId: String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedSource = presentation.sourceFilter?.let { id -> sourceResults.btSources.firstOrNull { it.mediaSourceId == id } }
    val label = if (selectedSource != null) {
        "${selectedSource.info.displayName} " +
                stringResource(Lang.media_selector_bt_source_count, presentation.sourceCounts[selectedSource.mediaSourceId] ?: 0)
    } else {
        stringResource(Lang.media_selector_bt_source_all, presentation.totalCount)
    }
    Box {
        FilterPill(
            label,
            applied = true,
            onClick = { expanded = true },
            Modifier.testTag(MacMediaSelectorTestTags.SourceFilter),
        )
        ApplePopoverMenu(
            expanded,
            onDismissRequest = { expanded = false },
            Modifier.testTag(MacMediaSelectorTestTags.SourceMenu),
        ) {
            PopoverMenuItem(
                stringResource(Lang.media_selector_bt_source_all, presentation.totalCount),
                onClick = {
                    expanded = false
                    onSelect(null)
                },
                checked = presentation.sourceFilter == null,
            )
            sourceResults.btSources.forEachIndexed { index, source ->
                val status = when {
                    source.isWorking -> stringResource(Lang.media_selector_bt_source_querying)
                    source.isFailedOrAbandoned -> stringResource(Lang.media_selector_bt_source_failed)
                    else -> stringResource(Lang.media_selector_bt_source_count, presentation.sourceCounts[source.mediaSourceId] ?: 0)
                }
                PopoverMenuItem(
                    "${source.info.displayName} · $status",
                    onClick = {
                        expanded = false
                        if (source.isDisabled || source.isFailedOrAbandoned) {
                            onRestartSource(source.instanceId)
                        } else {
                            onSelect(source.mediaSourceId)
                        }
                    },
                    Modifier.testTag(MacMediaSelectorTestTags.btSource(source.mediaSourceId)),
                    showDivider = index == 0,
                    checked = presentation.sourceFilter == source.mediaSourceId,
                )
            }
        }
    }
}

/**
 * 「篩選 (N)」: card 胶囊. 打开 C15 菜单, 分辨率 / 字幕 / 字幕组三组单选, 每组首项「不限」; 最后是「重置」.
 * 选择写入会话偏好, 与现有的筛选面板相同.
 */
@Composable
private fun MacBtFilterButton(
    presentation: BtListPresentation,
    resolution: MediaPreferenceItemState<String>,
    subtitleLanguageId: MediaPreferenceItemState<String>,
    alliance: MediaPreferenceItemState<String>,
) {
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val mediaDetailsStrings = rememberMediaDetailsStrings()
    val count = presentation.activeFilterCount
    Box {
        FilterPill(
            if (count > 0) {
                stringResource(Lang.media_selector_bt_filter_count, count)
            } else {
                stringResource(Lang.media_selector_bt_filter)
            },
            applied = false,
            onClick = { expanded = true },
            Modifier.testTag(MacMediaSelectorTestTags.FilterButton),
        )
        ApplePopoverMenu(
            expanded,
            onDismissRequest = { expanded = false },
            Modifier.testTag(MacMediaSelectorTestTags.FilterMenu),
        ) {
            val choose: (MediaPreferenceItemState<String>, String?) -> Unit = { item, value ->
                expanded = false
                scope.launch { if (value == null) item.removePreference() else item.prefer(value) }
            }
            MacFilterMenuGroup(
                stringResource(Lang.media_selector_filter_resolution),
                presentation.availableResolutions,
                presentation.resolution,
                onSelect = { choose(resolution, it) },
                showDivider = false,
            )
            MacFilterMenuGroup(
                stringResource(Lang.media_selector_filter_subtitle),
                presentation.availableSubtitleLanguageIds,
                presentation.subtitleLanguageId,
                onSelect = { choose(subtitleLanguageId, it) },
                label = { renderSubtitleLanguage(it, mediaDetailsStrings) },
            )
            MacFilterMenuGroup(
                stringResource(Lang.media_selector_filter_alliance),
                presentation.availableAlliances,
                presentation.alliance,
                onSelect = { choose(alliance, it) },
            )
            PopoverMenuItem(
                stringResource(Lang.media_selector_bt_filter_reset),
                onClick = {
                    expanded = false
                    scope.launch {
                        resolution.removePreference()
                        subtitleLanguageId.removePreference()
                        alliance.removePreference()
                    }
                },
                showDivider = true,
                enabled = count > 0,
            )
        }
    }
}

/**
 * 筛选菜单的一组: 小节标题 12 label2, 之后「不限」与各个值, 当前值有勾号.
 */
@Composable
private fun MacFilterMenuGroup(
    title: String,
    values: List<String>,
    selected: String?,
    onSelect: (String?) -> Unit,
    showDivider: Boolean = true,
    label: (String) -> String = { it },
) {
    if (showDivider) {
        Box(Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 4.dp).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
    }
    Text(
        title,
        Modifier.padding(start = 10.dp, end = 10.dp, top = 4.dp, bottom = 2.dp),
        style = AniTextStyles.macCaption.copy(fontWeight = FontWeight.SemiBold),
        color = MaterialTheme.colorScheme.secondary,
        maxLines = 1,
    )
    PopoverMenuItem(
        stringResource(Lang.media_selector_bt_filter_any),
        onClick = { onSelect(null) },
        checked = selected == null,
    )
    for (value in values) {
        PopoverMenuItem(
            label(value),
            onClick = { onSelect(value) },
            checked = value == selected,
        )
    }
}
