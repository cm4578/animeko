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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.PillTab
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.media_selector_episode_label
import me.him188.ani.app.ui.lang.media_selector_load_failed
import me.him188.ani.app.ui.lang.media_selector_mac_cancel
import me.him188.ani.app.ui.lang.media_selector_mac_play_episode
import me.him188.ani.app.ui.lang.media_selector_manual_episodes_count
import me.him188.ani.app.ui.lang.media_selector_manual_no_result
import me.him188.ani.app.ui.lang.media_selector_manual_no_sources
import me.him188.ani.app.ui.lang.media_selector_manual_remember_selection
import me.him188.ani.app.ui.lang.media_selector_manual_result_count
import me.him188.ani.app.ui.mediaselect.WatchingEpisode
import me.him188.ani.app.ui.mediaselect.common.episodeLabel
import me.him188.ani.app.ui.mediaselect.manual.ManualBrowsePresentation
import me.him188.ani.app.ui.mediaselect.manual.ManualBrowseSource
import me.him188.ani.app.ui.mediaselect.manual.ManualBrowseState
import me.him188.ani.app.ui.mediaselect.manual.ManualLoadState
import me.him188.ani.app.ui.mediaselect.manual.displayLabel
import me.him188.ani.app.ui.mediaselect.manual.shouldShowChannelRow
import me.him188.ani.datasources.api.source.BrowseEpisode
import me.him188.ani.datasources.api.source.BrowseSubject
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 版手动查找 (04 §3.9).
 *
 * 左栏 220 是资料源清单 (选项高 36、圆角 8, 选中 tint 底). 右栏是搜索框 (高 32、圆角 8、card 底)、「共 N 條結果」与结果:
 * 打开的结果是卡片 (作品名 14 W600、C4 线路胶囊、「劇集 N 項」、10 栏集数方块高 32、圆角 8), 其他结果收合为单行 (高 44).
 *
 * 点集数只选取 (tint 底 + accent 描边), 默认选取预选的一集; 底部左侧是「記住選擇」复选框, 右侧「取消」与「▶ 播放第 N 話」,
 * 按「播放」才把选取的一集交给 [ManualBrowseState.play], 成功后调用 [onPlayed], 失败提示「載入失敗」.
 * 打开页面、生效源变化与当前集就绪时调用 [ManualBrowseState.searchIfNeeded], 与现有页面相同.
 */
@Composable
internal fun MacManualBrowseContent(
    state: ManualBrowseState,
    watching: WatchingEpisode?,
    onPlayed: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val presentation by state.presentationFlow.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    val loadFailedText = stringResource(Lang.media_selector_load_failed)
    val currentOnPlayed by rememberUpdatedState(onPlayed)

    LaunchedEffect(state, presentation.selectedSourceId, presentation.target?.subjectName) {
        state.searchIfNeeded()
    }

    // 点集数只选取; 换条目或线路后回到预选.
    var pickedIndex by remember(presentation.openedSubject, presentation.selectedChannelIndex) {
        mutableStateOf<Int?>(null)
    }
    val episodes = presentation.selectedChannel?.episodes.orEmpty()
    val selectedIndex = (pickedIndex ?: presentation.selectedEpisodeIndex)?.takeIf { it in episodes.indices }

    Column(modifier) {
        Row(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            MacManualSourceList(
                presentation.sources,
                presentation.selectedSourceId,
                onSelect = state::selectSource,
                enabled = !presentation.isPlaceholder,
                Modifier.width(MacMediaSelectorDefaults.SourceListWidth).fillMaxHeight(),
            )
            Column(Modifier.weight(1f).fillMaxHeight()) {
                MacSelectorSearchField(
                    presentation.keyword,
                    onKeywordChange = state::setKeyword,
                    onSearch = state::search,
                    Modifier.fillMaxWidth(),
                )
                if (!presentation.isPlaceholder && presentation.sources.isEmpty()) {
                    AppleManualMessage(stringResource(Lang.media_selector_manual_no_sources), Modifier.weight(1f))
                } else {
                    MacManualResults(
                        state,
                        presentation,
                        selectedIndex = selectedIndex,
                        onSelectEpisode = { pickedIndex = it },
                        Modifier.weight(1f).fillMaxWidth(),
                    )
                }
            }
        }
        MacSelectorBottomBar {
            Row(
                Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable(role = Role.Checkbox) { state.setRememberSelection(!presentation.rememberSelection) }
                    .semantics { this.selected = presentation.rememberSelection }
                    .testTag(MacMediaSelectorTestTags.RememberSelection),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MacCheckbox(presentation.rememberSelection, onCheckedChange = null)
                Text(
                    stringResource(Lang.media_selector_manual_remember_selection),
                    style = AniTextStyles.macCallout,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
            }
            Spacer(Modifier.weight(1f))
            MacSelectorButton(
                stringResource(Lang.media_selector_mac_cancel),
                onClick = onCancel,
                Modifier.testTag(MacMediaSelectorTestTags.Cancel),
                primary = false,
            )
            val episodeLabel = watching?.episodeLabel()
                ?: stringResource(Lang.media_selector_episode_label, presentation.target?.episodeSortText ?: "–")
            MacSelectorButton(
                stringResource(Lang.media_selector_mac_play_episode, episodeLabel),
                onClick = {
                    val index = selectedIndex ?: return@MacSelectorButton
                    scope.launch {
                        when (state.play(index)) {
                            true -> currentOnPlayed()
                            false -> toaster.toast(loadFailedText)
                            null -> Unit
                        }
                    }
                },
                Modifier.testTag(MacMediaSelectorTestTags.Play),
                enabled = selectedIndex != null && presentation.target != null && !presentation.isPlaying,
                leadingIcon = {
                    Icon(AppleIcons.playFilled(), contentDescription = null, Modifier.size(10.dp))
                },
            )
        }
    }
}

/**
 * 资料源清单: 选项高 36、圆角 8, 名称 13 + 阶级标签; 选中 tint 底 + onTint W600.
 */
@Composable
private fun MacManualSourceList(
    sources: List<ManualBrowseSource>,
    selectedSourceId: String?,
    onSelect: (String) -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        contentPadding = PaddingValues(bottom = 16.dp),
    ) {
        items(sources, key = { it.instanceId }) { source ->
            val selected = source.instanceId == selectedSourceId
            val contentColor = if (selected) {
                MaterialTheme.colorScheme.onSecondaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                    .selectable(selected = selected, enabled = enabled, role = Role.RadioButton) { onSelect(source.instanceId) }
                    .padding(horizontal = 10.dp)
                    .testTag(MacMediaSelectorTestTags.manualSource(source.instanceId)),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    source.info.displayName,
                    Modifier.weight(1f),
                    style = AniTextStyles.macCallout,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                source.info.tier?.let { AppleTierBadge(it) }
            }
        }
    }
}

@Composable
private fun MacManualResults(
    state: ManualBrowseState,
    presentation: ManualBrowsePresentation,
    selectedIndex: Int?,
    onSelectEpisode: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val results = presentation.results) {
        ManualLoadState.Idle -> Spacer(modifier)
        ManualLoadState.Loading -> AppleManualLoading(modifier)
        is ManualLoadState.Failed -> AppleManualFailed(results, onRetry = state::retry, modifier.padding(top = 12.dp))
        is ManualLoadState.Success -> {
            val subjects = results.value
            val opened = presentation.openedSubject
            // 打开的卡片由几段 item 拼成, 段与段之间不能有间距; 其他 item 自己留下方 8 的间距.
            LazyColumn(
                modifier,
                contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
            ) {
                item("count") {
                    Text(
                        stringResource(Lang.media_selector_manual_result_count, subjects.size),
                        Modifier.padding(bottom = 8.dp),
                        style = AniTextStyles.macCaption,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                if (subjects.isEmpty()) {
                    item("empty") { AppleManualMessage(stringResource(Lang.media_selector_manual_no_result)) }
                }
                if (opened != null && opened !in subjects) {
                    openedSubjectItems(state, presentation, opened, key = "opened", selectedIndex, onSelectEpisode)
                }
                subjects.forEachIndexed { index, subject ->
                    if (subject == opened) {
                        openedSubjectItems(state, presentation, subject, key = "result-$index", selectedIndex, onSelectEpisode)
                    } else {
                        item("result-$index") {
                            MacCollapsedResultRow(
                                subject.name,
                                onClick = { state.openSubject(subject) },
                                Modifier.padding(bottom = 8.dp).testTag(MacMediaSelectorTestTags.result(index)),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 打开的结果卡片拆成几段 item, 集数网格每行一段. 各段拼成一张圆角 12 的 card.
 */
private fun LazyListScope.openedSubjectItems(
    state: ManualBrowseState,
    presentation: ManualBrowsePresentation,
    subject: BrowseSubject,
    key: String,
    selectedIndex: Int?,
    onSelectEpisode: (Int) -> Unit,
) {
    val channels = presentation.channels
    val episodes = (channels as? ManualLoadState.Success)?.let { presentation.selectedChannel?.episodes }.orEmpty()
    val rows = episodes.indices.chunked(MacManualBrowseDefaults.GridColumns)
    item("$key-title") {
        Row(
            Modifier
                .fillMaxWidth()
                .cardSegment(top = true, bottom = false)
                .clickable(role = Role.Button, onClick = state::closeSubject)
                .heightIn(min = 44.dp)
                .padding(horizontal = 14.dp)
                .testTag(MacMediaSelectorTestTags.OpenedResult),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                subject.name,
                Modifier.weight(1f),
                style = AniTextStyles.macBody.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                AppleIcons.chevronDown(),
                contentDescription = null,
                Modifier.size(12.dp).rotate(180f),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
    }
    when (channels) {
        ManualLoadState.Idle, ManualLoadState.Loading -> item("$key-loading") {
            AppleManualLoading(Modifier.fillMaxWidth().height(88.dp).cardSegment(top = false, bottom = true))
        }

        is ManualLoadState.Failed -> item("$key-failed") {
            AppleManualFailed(
                channels,
                onRetry = state::retry,
                Modifier.fillMaxWidth().cardSegment(top = false, bottom = true).padding(vertical = 12.dp),
            )
        }

        is ManualLoadState.Success -> {
            if (shouldShowChannelRow(channels.value)) {
                item("$key-channels") {
                    FlowRow(
                        Modifier
                            .fillMaxWidth()
                            .cardSegment(top = false, bottom = false)
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        channels.value.forEachIndexed { index, channel ->
                            PillTab(
                                channel.displayLabel(),
                                selected = index == presentation.selectedChannelIndex,
                                onClick = { state.selectChannel(index) },
                                Modifier.testTag(MacMediaSelectorTestTags.channelPill(index)),
                            )
                        }
                    }
                }
            }
            item("$key-episode-count") {
                Text(
                    stringResource(Lang.media_selector_manual_episodes_count, episodes.size),
                    Modifier
                        .fillMaxWidth()
                        .cardSegment(top = false, bottom = rows.isEmpty())
                        .padding(start = 14.dp, top = 8.dp, end = 14.dp, bottom = 8.dp),
                    style = AniTextStyles.macCaption,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            rows.forEachIndexed { rowIndex, indices ->
                item("$key-episodes-$rowIndex") {
                    MacEpisodeGridRow(
                        indices.map { episodes[it] },
                        firstIndex = indices.first(),
                        selectedIndex = selectedIndex,
                        enabled = !presentation.isPlaying,
                        onClick = onSelectEpisode,
                        Modifier
                            .fillMaxWidth()
                            .cardSegment(top = false, bottom = rowIndex == rows.lastIndex)
                            .padding(
                                start = 10.dp,
                                end = 10.dp,
                                bottom = if (rowIndex == rows.lastIndex) 10.dp else MacManualBrowseDefaults.GridSpacing,
                            ),
                    )
                }
            }
        }
    }
    item("$key-gap") {
        Spacer(Modifier.height(8.dp))
    }
}

/**
 * card 底的一段; [top] / [bottom] 为卡片的首段 / 末段时圆角 12.
 */
@Composable
private fun Modifier.cardSegment(top: Boolean, bottom: Boolean): Modifier {
    val radius = 12.dp
    val shape: Shape = RoundedCornerShape(
        topStart = if (top) radius else 0.dp,
        topEnd = if (top) radius else 0.dp,
        bottomStart = if (bottom) radius else 0.dp,
        bottomEnd = if (bottom) radius else 0.dp,
    )
    return clip(shape).background(MaterialTheme.colorScheme.surfaceContainerLow)
}

/**
 * 收合的结果: 单行高 44、card 底、圆角 12, 作品名 labelSoft.
 */
@Composable
private fun MacCollapsedResultRow(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(role = Role.Button, onClick = onClick)
            .height(44.dp)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            name,
            Modifier.weight(1f),
            style = AniTextStyles.macBody,
            color = AniColors.labelSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            AppleIcons.chevronDown(),
            contentDescription = null,
            Modifier.size(12.dp),
            tint = MaterialTheme.colorScheme.secondary,
        )
    }
}

/**
 * 集数网格的一行, 最多 [MacManualBrowseDefaults.GridColumns] 格, 不足的位置留空.
 */
@Composable
private fun MacEpisodeGridRow(
    episodes: List<BrowseEpisode>,
    firstIndex: Int,
    selectedIndex: Int?,
    enabled: Boolean,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(MacManualBrowseDefaults.GridSpacing)) {
        for (column in 0 until MacManualBrowseDefaults.GridColumns) {
            val episode = episodes.getOrNull(column)
            if (episode == null) {
                Spacer(Modifier.weight(1f))
                continue
            }
            val index = firstIndex + column
            MacBrowseEpisodeCell(
                appleBrowseEpisodeLabel(episode),
                selected = index == selectedIndex,
                enabled = enabled,
                onClick = { onClick(index) },
                Modifier.weight(1f).testTag(MacMediaSelectorTestTags.episode(index)),
            )
        }
    }
}

/**
 * 集数方块: 高 32、圆角 8; 选取的一集 (含预选) 为 tint 底 + accent 描边, 其他为 card 底 + separator 描边.
 */
@Composable
private fun MacBrowseEpisodeCell(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier
            .height(32.dp)
            .clip(shape)
            .background(if (selected) colorScheme.secondaryContainer else colorScheme.surfaceContainerLow)
            .border(
                if (selected) 1.5.dp else 1.dp,
                if (selected) colorScheme.primary else colorScheme.outlineVariant,
                shape,
            )
            .semantics { this.selected = selected }
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            Modifier.padding(horizontal = 2.dp),
            style = AniTextStyles.macCaption.copy(fontFeatureSettings = "tnum"),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal object MacManualBrowseDefaults {
    const val GridColumns = 10
    val GridSpacing = 6.dp
}
