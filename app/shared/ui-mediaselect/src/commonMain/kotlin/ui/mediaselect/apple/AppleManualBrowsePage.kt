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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.foundation.apple.AniSwitch
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.ApplePopoverMenu
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.PopoverMenuItem
import me.him188.ani.app.ui.foundation.apple.episodeTileLabel
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.media_selector_load_failed
import me.him188.ani.app.ui.lang.media_selector_manual_captcha_unsupported
import me.him188.ani.app.ui.lang.media_selector_manual_episodes_count
import me.him188.ani.app.ui.lang.media_selector_manual_no_result
import me.him188.ani.app.ui.lang.media_selector_manual_no_sources
import me.him188.ani.app.ui.lang.media_selector_manual_remember_selection
import me.him188.ani.app.ui.lang.media_selector_manual_result_count
import me.him188.ani.app.ui.lang.media_selector_retry
import me.him188.ani.app.ui.lang.media_selector_search_hint
import me.him188.ani.app.ui.mediaselect.manual.ManualBrowsePresentation
import me.him188.ani.app.ui.mediaselect.manual.ManualBrowseSource
import me.him188.ani.app.ui.mediaselect.manual.ManualBrowseState
import me.him188.ani.app.ui.mediaselect.manual.ManualLoadState
import me.him188.ani.app.ui.mediaselect.manual.displayLabel
import me.him188.ani.app.ui.mediaselect.manual.shouldShowChannelRow
import me.him188.ani.datasources.api.source.BrowseChannel
import me.him188.ani.datasources.api.source.BrowseEpisode
import me.him188.ani.datasources.api.source.BrowseSubject
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版手动查找 (03 §3.8).
 *
 * 顶部是资料源选择钮与搜索框 (都高 40、圆角 20、card 底), 下方「共 N 條結果」与结果列表.
 * 打开的结果是一张卡片: 标题列高 48、线路小号胶囊 (高 30、圆角 15)、「劇集 N 項」与 7 栏剧集网格 (格高 38、圆角 10);
 * 其他结果收合成一列高 48、labelSoft, 点它改为打开它, 点打开卡片的标题列收起.
 * 底部是「記住選擇」开关.
 *
 * 打开页面、生效源变化与当前集就绪时调用 [ManualBrowseState.searchIfNeeded], 与现有页面相同.
 * 点网格里的一集直接播放, 不确认; 成功后调用 [onPlayed], 失败提示「載入失敗」.
 */
@Composable
fun AppleManualBrowsePage(
    state: ManualBrowseState,
    onPlayed: () -> Unit,
    modifier: Modifier = Modifier,
    showRememberSelection: Boolean = true,
) {
    val presentation by state.presentationFlow.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    val loadFailedText = stringResource(Lang.media_selector_load_failed)
    val currentOnPlayed by rememberUpdatedState(onPlayed)
    val playEpisode: (Int) -> Unit = { index ->
        scope.launch {
            when (state.play(index)) {
                true -> currentOnPlayed()
                false -> toaster.toast(loadFailedText)
                null -> Unit
            }
        }
    }

    LaunchedEffect(state, presentation.selectedSourceId, presentation.target?.subjectName) {
        state.searchIfNeeded()
    }

    Column(modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppleManualSourceButton(
                presentation.sources,
                presentation.selectedSourceId,
                onSelect = state::selectSource,
                enabled = !presentation.isPlaceholder && presentation.sources.isNotEmpty(),
            )
            AppleManualSearchField(
                presentation.keyword,
                onKeywordChange = state::setKeyword,
                onSearch = state::search,
                Modifier.weight(1f),
            )
        }

        if (!presentation.isPlaceholder && presentation.sources.isEmpty()) {
            AppleManualMessage(stringResource(Lang.media_selector_manual_no_sources), Modifier.weight(1f))
        } else {
            AppleManualResults(
                state,
                presentation,
                onPlayEpisode = playEpisode,
                Modifier.weight(1f).fillMaxWidth(),
            )
        }

        if (showRememberSelection) {
            AppleDivider()
            Row(
                Modifier.fillMaxWidth().heightIn(min = 56.dp).padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    stringResource(Lang.media_selector_manual_remember_selection),
                    Modifier.weight(1f),
                    style = AniTextStyles.body,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                AniSwitch(
                    presentation.rememberSelection,
                    onCheckedChange = state::setRememberSelection,
                    Modifier.testTag(AppleMediaSelectorTestTags.RememberSwitch),
                )
            }
        }
    }
}

@Composable
private fun AppleManualResults(
    state: ManualBrowseState,
    presentation: ManualBrowsePresentation,
    onPlayEpisode: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (val results = presentation.results) {
        ManualLoadState.Idle -> Spacer(modifier)
        ManualLoadState.Loading -> AppleManualLoading(modifier)
        is ManualLoadState.Failed -> AppleManualFailed(results, onRetry = state::retry, modifier)
        is ManualLoadState.Success -> {
            val subjects = results.value
            val opened = presentation.openedSubject
            // 打开的卡片由几段 item 拼成, 段与段之间不能有间距; 其他 item 自己留下方 8 的间距.
            LazyColumn(
                modifier,
                contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp),
            ) {
                item("count") {
                    Text(
                        stringResource(Lang.media_selector_manual_result_count, subjects.size),
                        Modifier.padding(bottom = 8.dp),
                        style = AniTextStyles.footnote,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
                if (subjects.isEmpty()) {
                    item("empty") { AppleManualMessage(stringResource(Lang.media_selector_manual_no_result)) }
                }
                if (opened != null && opened !in subjects) {
                    openedSubjectItems(state, presentation, opened, key = "opened", onPlayEpisode)
                }
                subjects.forEachIndexed { index, subject ->
                    if (subject == opened) {
                        openedSubjectItems(state, presentation, subject, key = "result-$index", onPlayEpisode)
                    } else {
                        item("result-$index") {
                            AppleCollapsedResultRow(
                                subject.name,
                                onClick = { state.openSubject(subject) },
                                Modifier.padding(bottom = 8.dp).testTag(AppleMediaSelectorTestTags.result(index)),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 打开的结果卡片拆成几段 item, 剧集网格每行一段, 长列表也是惰性组合. 各段拼成一张圆角 16 的 card.
 */
private fun LazyListScope.openedSubjectItems(
    state: ManualBrowseState,
    presentation: ManualBrowsePresentation,
    subject: BrowseSubject,
    key: String,
    onPlayEpisode: (Int) -> Unit,
) {
    val channels = presentation.channels
    val episodes = (channels as? ManualLoadState.Success)?.let { presentation.selectedChannel?.episodes }.orEmpty()
    val rows = episodes.indices.chunked(AppleManualBrowseDefaults.GridColumns)
    item("$key-title") {
        Row(
            Modifier
                .fillMaxWidth()
                .cardSegment(top = true, bottom = false)
                .clickable(role = Role.Button, onClick = state::closeSubject)
                .heightIn(min = 48.dp)
                .padding(horizontal = 16.dp)
                .testTag(AppleMediaSelectorTestTags.OpenedResult),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                subject.name,
                Modifier.weight(1f),
                style = AniTextStyles.body.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                AppleIcons.chevronDown(),
                contentDescription = null,
                Modifier.size(14.dp).rotate(180f),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
    }
    when (channels) {
        ManualLoadState.Idle, ManualLoadState.Loading -> item("$key-loading") {
            AppleManualLoading(Modifier.fillMaxWidth().height(96.dp).cardSegment(top = false, bottom = true))
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
                    AppleChannelPills(
                        channels.value,
                        presentation.selectedChannelIndex,
                        onSelect = state::selectChannel,
                        Modifier.fillMaxWidth().cardSegment(top = false, bottom = false),
                    )
                }
            }
            item("$key-episode-count") {
                Text(
                    stringResource(Lang.media_selector_manual_episodes_count, episodes.size),
                    Modifier
                        .fillMaxWidth()
                        .cardSegment(top = false, bottom = rows.isEmpty())
                        .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                    style = AniTextStyles.caption,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            rows.forEachIndexed { rowIndex, indices ->
                item("$key-episodes-$rowIndex") {
                    AppleEpisodeGridRow(
                        indices.map { episodes[it] },
                        firstIndex = indices.first(),
                        selectedIndex = presentation.selectedEpisodeIndex,
                        enabled = !presentation.isPlaying,
                        onClick = onPlayEpisode,
                        Modifier
                            .fillMaxWidth()
                            .cardSegment(top = false, bottom = rowIndex == rows.lastIndex)
                            .padding(
                                start = 12.dp,
                                end = 12.dp,
                                bottom = if (rowIndex == rows.lastIndex) 12.dp else AppleManualBrowseDefaults.GridSpacing,
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
 * card 底的一段; [top] / [bottom] 为卡片的首段 / 末段时圆角 16.
 */
@Composable
private fun Modifier.cardSegment(top: Boolean, bottom: Boolean): Modifier {
    val radius = 16.dp
    val shape: Shape = RoundedCornerShape(
        topStart = if (top) radius else 0.dp,
        topEnd = if (top) radius else 0.dp,
        bottomStart = if (bottom) radius else 0.dp,
        bottomEnd = if (bottom) radius else 0.dp,
    )
    return clip(shape).background(MaterialTheme.colorScheme.surfaceContainerLow)
}

@Composable
private fun AppleCollapsedResultRow(
    name: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(role = Role.Button, onClick = onClick)
            .heightIn(min = 48.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            name,
            Modifier.weight(1f),
            style = AniTextStyles.body,
            color = AniColors.labelSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(
            AppleIcons.chevronDown(),
            contentDescription = null,
            Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.secondary,
        )
    }
}

/**
 * 线路: C4 小号胶囊, 高 30、圆角 15; 选中 tint 底 + onTint W600.
 */
@Composable
private fun AppleChannelPills(
    channels: List<BrowseChannel>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier,
        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        itemsIndexed(channels) { index, channel ->
            val selected = index == selectedIndex
            Box(
                Modifier
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                    .clickable(role = Role.Tab) { onSelect(index) }
                    .semantics { this.selected = selected }
                    .padding(horizontal = 12.dp)
                    .testTag(AppleMediaSelectorTestTags.channelPill(index)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    channel.displayLabel(),
                    style = AniTextStyles.subhead.copy(fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal),
                    color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * 剧集网格的一行, 最多 [AppleManualBrowseDefaults.GridColumns] 格, 不足的位置留空.
 * 格高 38、圆角 10: 当前选中 (含预选) 为 C13「正在播放」样式, 其他为「未看」样式.
 */
@Composable
private fun AppleEpisodeGridRow(
    episodes: List<BrowseEpisode>,
    firstIndex: Int,
    selectedIndex: Int?,
    enabled: Boolean,
    onClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(AppleManualBrowseDefaults.GridSpacing)) {
        for (column in 0 until AppleManualBrowseDefaults.GridColumns) {
            val episode = episodes.getOrNull(column)
            if (episode == null) {
                Spacer(Modifier.weight(1f))
                continue
            }
            val index = firstIndex + column
            AppleBrowseEpisodeCell(
                appleBrowseEpisodeLabel(episode),
                selected = index == selectedIndex,
                enabled = enabled,
                onClick = { onClick(index) },
                Modifier.weight(1f).testTag(AppleMediaSelectorTestTags.episode(index)),
            )
        }
    }
}

/**
 * 网格里的文字: 数据源解析出集号时补零显示 (例如「09」), 否则用剧集名.
 */
internal fun appleBrowseEpisodeLabel(episode: BrowseEpisode): String =
    episode.episodeSort?.let { episodeTileLabel(it) } ?: episode.name

@Composable
private fun AppleBrowseEpisodeCell(
    label: String,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier
            .height(38.dp)
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
            style = AniTextStyles.footnote.copy(fontFeatureSettings = "tnum"),
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 资料源选择钮: 高 40、圆角 20、card 底, 显示当前资料源; 点开 C15 菜单, 每列为资料源名称与阶级 (同数据源设置页), 当前资料源有勾号.
 */
@Composable
private fun AppleManualSourceButton(
    sources: List<ManualBrowseSource>,
    selectedSourceId: String?,
    onSelect: (String) -> Unit,
    enabled: Boolean,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = sources.firstOrNull { it.instanceId == selectedSourceId }
    Box {
        Row(
            Modifier
                .height(40.dp)
                .widthIn(max = 160.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable(enabled = enabled, role = Role.Button) { expanded = true }
                .padding(start = 14.dp, end = 12.dp)
                .testTag(AppleMediaSelectorTestTags.SourceButton),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                selected?.info?.displayName ?: "–",
                Modifier.weight(1f, fill = false),
                style = AniTextStyles.callout.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
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
        ApplePopoverMenu(
            expanded,
            onDismissRequest = { expanded = false },
            Modifier.testTag(AppleMediaSelectorTestTags.SourceMenu),
        ) {
            sources.forEachIndexed { index, source ->
                PopoverMenuItem(
                    listOfNotNull(source.info.displayName, source.info.tier?.let { "T${it.value}" }).joinToString(" · "),
                    onClick = {
                        expanded = false
                        onSelect(source.instanceId)
                    },
                    Modifier.testTag(AppleMediaSelectorTestTags.sourceMenuItem(index)),
                    showDivider = index > 0,
                    checked = source.instanceId == selectedSourceId,
                )
            }
        }
    }
}

/**
 * 搜索框: 高 40、圆角 20、card 底, `15` 号字. 输入法的搜索键提交关键字.
 */
@Composable
private fun AppleManualSearchField(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val textStyle = AniTextStyles.callout.copy(color = MaterialTheme.colorScheme.onSurface)
    Row(
        modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            AppleIcons.search(),
            contentDescription = null,
            Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.secondary,
        )
        BasicTextField(
            keyword,
            onValueChange = onKeywordChange,
            Modifier.weight(1f).testTag(AppleMediaSelectorTestTags.SearchField),
            textStyle = textStyle,
            singleLine = true,
            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { inner ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (keyword.isEmpty()) {
                        Text(
                            stringResource(Lang.media_selector_search_hint),
                            style = textStyle,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                        )
                    }
                    inner()
                }
            },
        )
    }
}

@Composable
private fun AppleManualLoading(modifier: Modifier = Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
    }
}

/**
 * 载入失败: 错误文字 + 「重試」; 被验证码挡住而平台不支持时只显示提示.
 */
@Composable
private fun AppleManualFailed(
    failed: ManualLoadState.Failed,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(
                if (failed.captchaUnsupported) Lang.media_selector_manual_captcha_unsupported else Lang.media_selector_load_failed,
            ),
            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = AniTextStyles.footnote,
            color = MaterialTheme.colorScheme.error,
        )
        if (!failed.captchaUnsupported) {
            AppleTextButton(onRetry, Modifier.testTag(AppleMediaSelectorTestTags.ManualRetry)) {
                Text(stringResource(Lang.media_selector_retry))
            }
        }
    }
}

@Composable
private fun AppleManualMessage(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(text, style = AniTextStyles.footnote, color = MaterialTheme.colorScheme.secondary)
    }
}

internal object AppleManualBrowseDefaults {
    const val GridColumns = 7
    val GridSpacing = 6.dp
}
