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
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.him188.ani.app.domain.media.selector.SelectTrigger
import me.him188.ani.app.domain.mediasource.web.captcha.SolveOutcome
import me.him188.ani.app.ui.foundation.apple.AppleButton
import me.him188.ani.app.ui.foundation.apple.AppleButtonStyle
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.MacSheet
import me.him188.ani.app.ui.foundation.apple.MacSheetDefaults
import me.him188.ani.app.ui.foundation.apple.PillTab
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.media_selector_auto_rescue
import me.him188.ani.app.ui.lang.media_selector_bt_empty
import me.him188.ani.app.ui.lang.media_selector_bt_source_failed
import me.him188.ani.app.ui.lang.media_selector_channel_playing
import me.him188.ani.app.ui.lang.media_selector_mac_channels_of
import me.him188.ani.app.ui.lang.media_selector_mac_done
import me.him188.ani.app.ui.lang.media_selector_retry
import me.him188.ani.app.ui.lang.media_selector_search_hint
import me.him188.ani.app.ui.lang.media_selector_watching_prefix
import me.him188.ani.app.ui.lang.subject_episode_select_media_source
import me.him188.ani.app.ui.mediafetch.MediaSelectorState
import me.him188.ani.app.ui.mediafetch.MediaSourceResultListPresentation
import me.him188.ani.app.ui.mediaselect.MediaSelectorMode
import me.him188.ani.app.ui.mediaselect.WatchingEpisode
import me.him188.ani.app.ui.mediaselect.common.displayName
import me.him188.ani.app.ui.mediaselect.common.episodeLabel
import me.him188.ani.app.ui.mediaselect.manual.ManualBrowseState
import me.him188.ani.app.ui.mediaselect.selector.WebSource
import me.him188.ani.app.ui.mediaselect.selector.WebSourceChannel
import me.him188.ani.datasources.api.source.MediaFetchRequest
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 版选择资料来源 (04 §3.9): 播放窗口上的 C14 sheet, 从顶端列下缘降下, 宽 760、圆角 12、bg 底, 背景暗化 38%.
 *
 * 头部是标题、「正在觀看 第 N 話」与 C4 模式切换; 会话里没有 BT 资料源时没有「BT 資源」.
 * 各模式的内容与底部操作列见 [MacAutoMatchContent]、[MacManualBrowseContent]、[MacBtResourcesContent].
 * Esc 关闭 sheet.
 *
 * @param topOffset sheet 顶端与窗口顶端的距离, 为播放页顶端列的下缘.
 */
@Composable
fun MacMediaSelectorSheet(
    mode: MediaSelectorMode,
    onModeChange: (MediaSelectorMode) -> Unit,
    mediaSelectorState: MediaSelectorState,
    sourceResults: MediaSourceResultListPresentation,
    watching: WatchingEpisode?,
    fetchRequest: MediaFetchRequest?,
    onFetchRequestChange: (MediaFetchRequest) -> Unit,
    onRestartSource: (instanceId: String) -> Unit,
    manualBrowseState: ManualBrowseState,
    onDismissRequest: () -> Unit,
    topOffset: Dp,
) {
    val windowHeight = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.height.toDp() }
    val height = minOf(MacMediaSelectorDefaults.MaxHeight, windowHeight - topOffset - MacMediaSelectorDefaults.BottomMargin)
        .coerceAtLeast(MacMediaSelectorDefaults.MinHeight)
    MacSheet(
        onDismissRequest,
        Modifier.height(height).testTag(MacMediaSelectorTestTags.Sheet),
        width = MacSheetDefaults.MaxWidth,
        topOffset = topOffset,
        contentPadding = PaddingValues(0.dp),
    ) {
        MacMediaSelectorContent(
            mode,
            onModeChange,
            mediaSelectorState,
            sourceResults,
            watching,
            fetchRequest,
            onFetchRequestChange,
            onRestartSource,
            manualBrowseState,
            onDismissRequest,
            Modifier.fillMaxSize(),
        )
    }
}

/**
 * sheet 的内容: 头部与当前模式. 选中资源播放、「完成」与「取消」都调用 [onDismissRequest].
 */
@Composable
internal fun MacMediaSelectorContent(
    mode: MediaSelectorMode,
    onModeChange: (MediaSelectorMode) -> Unit,
    mediaSelectorState: MediaSelectorState,
    sourceResults: MediaSourceResultListPresentation,
    watching: WatchingEpisode?,
    fetchRequest: MediaFetchRequest?,
    onFetchRequestChange: (MediaFetchRequest) -> Unit,
    onRestartSource: (instanceId: String) -> Unit,
    manualBrowseState: ManualBrowseState,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        MacMediaSelectorHeader(
            mode,
            onModeChange,
            showBt = sourceResults.btSources.isNotEmpty(),
            watching,
        )
        val pageModifier = Modifier.weight(1f).fillMaxWidth()
        when (mode) {
            MediaSelectorMode.AUTO -> MacAutoMatchContent(
                mediaSelectorState,
                onRestartSource,
                onRequestManualSearch = { onModeChange(MediaSelectorMode.MANUAL) },
                onDone = onDismissRequest,
                pageModifier,
            )

            MediaSelectorMode.MANUAL -> MacManualBrowseContent(
                manualBrowseState,
                watching,
                onPlayed = onDismissRequest,
                onCancel = onDismissRequest,
                pageModifier,
            )

            MediaSelectorMode.BT -> MacBtResourcesContent(
                mediaSelectorState,
                sourceResults,
                watching,
                fetchRequest,
                onFetchRequestChange,
                onRestartSource,
                onPlayed = onDismissRequest,
                onCancel = onDismissRequest,
                pageModifier,
            )
        }
    }
}

/**
 * 头部 (内距上 20、左右 24、下 16): 标题「選擇資料來源」`macTitle`、「正在觀看 第 N 話」`macCaption` label2;
 * 右侧 C4 模式切换.
 *
 * @param showBt 会话里有 BT 资料源时才有「BT 資源」.
 */
@Composable
internal fun MacMediaSelectorHeader(
    mode: MediaSelectorMode,
    onModeChange: (MediaSelectorMode) -> Unit,
    showBt: Boolean,
    watching: WatchingEpisode?,
    modifier: Modifier = Modifier,
) {
    val modes = listOfNotNull(
        MediaSelectorMode.AUTO,
        MediaSelectorMode.MANUAL,
        MediaSelectorMode.BT.takeIf { showBt },
    )
    Row(
        modifier.fillMaxWidth().padding(start = 24.dp, top = 20.dp, end = 24.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(Lang.subject_episode_select_media_source),
                style = AniTextStyles.macTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            if (watching != null) {
                Text(
                    "${stringResource(Lang.media_selector_watching_prefix)} ${watching.episodeLabel()}",
                    Modifier.testTag(MacMediaSelectorTestTags.Watching),
                    style = AniTextStyles.macCaption,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(
            Modifier.selectableGroup(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            for (item in modes) {
                PillTab(
                    item.displayName(),
                    selected = item == mode,
                    onClick = { if (item != mode) onModeChange(item) },
                    Modifier.testTag(MacMediaSelectorTestTags.mode(item)),
                )
            }
        }
    }
}

/**
 * 底部操作列: 高 60, 左右 24, 顶边 1px separator.
 */
@Composable
internal fun MacSelectorBottomBar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        AppleDivider()
        Row(
            Modifier
                .fillMaxWidth()
                .height(MacMediaSelectorDefaults.BottomBarHeight - 1.dp)
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/**
 * 底部操作列的按钮: 主要 (accentFill) 或次要 (fill), 高 28、最小宽 80.
 */
@Composable
internal fun MacSelectorButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    primary: Boolean = true,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    AppleButton(
        onClick = onClick,
        modifier = modifier.widthIn(min = MacSheetDefaults.ButtonMinWidth),
        style = if (primary) AppleButtonStyle.Primary else AppleButtonStyle.Secondary,
        enabled = enabled,
        contentPadding = MacSheetDefaults.ButtonContentPadding,
    ) {
        if (leadingIcon != null) {
            leadingIcon()
            Spacer(Modifier.width(6.dp))
        }
        Text(text, maxLines = 1)
    }
}

/**
 * 自动匹配: 左栏资料源列表, 右栏 300 宽「X 的線路」. 点线路立即换源并留在 sheet, 「播放中」随之移动; 底部只有「完成」.
 */
@Composable
internal fun MacAutoMatchContent(
    state: MediaSelectorState,
    onRestartSource: (instanceId: String) -> Unit,
    onRequestManualSearch: () -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val presentation by state.presentationFlow.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    Column(modifier) {
        MacWebSourceColumns(
            presentation.webSources,
            selectedSource = presentation.selectedWebSource,
            selectedChannel = presentation.selectedWebSourceChannel,
            onSelect = { channel ->
                channel.original?.let { state.select(it, SelectTrigger.MANUAL_AUTO_PAGE) }
            },
            onRefresh = { onRestartSource(it.instanceId) },
            onResolveCaptcha = { source ->
                scope.launch {
                    if (state.resolveCaptcha(source) == SolveOutcome.Solved) {
                        onRestartSource(source.instanceId)
                    }
                }
            },
            onRequestManualSearch = onRequestManualSearch,
            Modifier.weight(1f).fillMaxWidth().padding(bottom = 16.dp),
        )
        MacSelectorBottomBar {
            Spacer(Modifier.weight(1f))
            MacSelectorButton(
                stringResource(Lang.media_selector_mac_done),
                onClick = onDone,
                Modifier.testTag(MacMediaSelectorTestTags.Done),
            )
        }
    }
}

/**
 * 自动匹配的两栏.
 *
 * 左栏是 card (圆角 12) 里的资料源, 行高 48、左右 16/12: 名称 14 W600 + 阶级标签 + 右侧状态 12 label2;
 * 顺序同现有的自动匹配页, 查询失败的源排在最后, 状态为 error「查詢失敗」+ accent「重試」.
 * 点有线路的源在右栏显示它的线路, 选中行 tint 底; 默认为当前资料源.
 *
 * 右栏宽 300: 「X 的線路」与线路列表 (高 40、圆角 8, 当前线路 tint 底 +「播放中」), 下方 12 号 accent「找不到想看的？手動查找」.
 */
@Composable
internal fun MacWebSourceColumns(
    sources: List<WebSource>,
    selectedSource: WebSource?,
    selectedChannel: WebSourceChannel?,
    onSelect: (WebSourceChannel) -> Unit,
    onRefresh: (WebSource) -> Unit,
    onResolveCaptcha: (WebSource) -> Unit,
    onRequestManualSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // null: 跟随当前资料源; 用户点过之后为点开的资料源.
    var focusedInstanceId by rememberSaveable { mutableStateOf<String?>(null) }
    val (failedSources, otherSources) = sources.partition { it.isError }
    val focused = otherSources.firstOrNull { it.instanceId == focusedInstanceId }
        ?: otherSources.firstOrNull { it.instanceId == selectedSource?.instanceId }
        ?: otherSources.firstOrNull { it.channels.isNotEmpty() }
    Row(
        modifier.padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Box(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState())) {
            if (sources.isNotEmpty()) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    otherSources.forEachIndexed { index, source ->
                        if (index > 0) AppleDivider(startIndent = 16.dp)
                        MacWebSourceRow(
                            source,
                            selected = source.instanceId == focused?.instanceId,
                            onClick = { focusedInstanceId = source.instanceId },
                            onResolveCaptcha = { onResolveCaptcha(source) },
                        )
                    }
                    failedSources.forEachIndexed { index, source ->
                        if (otherSources.isNotEmpty() || index > 0) AppleDivider(startIndent = 16.dp)
                        MacFailedSourceRow(source, onRetry = { onRefresh(source) })
                    }
                }
            }
        }
        Column(Modifier.width(MacMediaSelectorDefaults.SideColumnWidth).fillMaxHeight()) {
            if (focused != null) {
                Text(
                    stringResource(Lang.media_selector_mac_channels_of, focused.name),
                    Modifier.padding(bottom = 8.dp).testTag(MacMediaSelectorTestTags.ChannelsTitle),
                    style = AniTextStyles.macCallout.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Column(
                    Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    focused.channels.forEachIndexed { index, channel ->
                        MacChannelRow(
                            channel,
                            selected = channel == selectedChannel && focused.instanceId == selectedSource?.instanceId,
                            onClick = { onSelect(channel) },
                            Modifier.testTag(MacMediaSelectorTestTags.channel(index)),
                        )
                    }
                    if (focused.channels.isEmpty()) {
                        AppleWebSourceStatus(
                            focused,
                            onResolveCaptcha = { onResolveCaptcha(focused) },
                            Modifier.padding(vertical = 8.dp),
                            style = AniTextStyles.macCaption,
                        )
                    }
                }
            }
            AppleTextButton(
                onRequestManualSearch,
                Modifier.padding(top = 12.dp).testTag(MacMediaSelectorTestTags.RescueButton),
            ) {
                Text(stringResource(Lang.media_selector_auto_rescue), style = AniTextStyles.macCaption)
            }
        }
    }
}

@Composable
private fun MacWebSourceRow(
    source: WebSource,
    selected: Boolean,
    onClick: () -> Unit,
    onResolveCaptcha: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(start = 16.dp, end = 12.dp, top = 6.dp, bottom = 6.dp)
            .testTag(MacMediaSelectorTestTags.source(source.instanceId)),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                source.name,
                Modifier.weight(1f, fill = false),
                style = AniTextStyles.macBody.copy(fontWeight = FontWeight.SemiBold),
                color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            source.tier?.let { AppleTierBadge(it) }
        }
        AppleWebSourceStatus(
            source,
            onResolveCaptcha,
            Modifier.widthIn(max = MacMediaSelectorDefaults.StatusMaxWidth),
            style = AniTextStyles.macCaption,
        )
    }
}

/**
 * 查询失败的资料源: 名称 labelSoft + 「查詢失敗」12 W600 error + 「重試」accent.
 */
@Composable
private fun MacFailedSourceRow(
    source: WebSource,
    onRetry: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .padding(start = 16.dp, end = 12.dp)
            .testTag(MacMediaSelectorTestTags.failedSource(source.instanceId)),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            source.name,
            Modifier.weight(1f),
            style = AniTextStyles.macBody,
            color = AniColors.labelSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            stringResource(Lang.media_selector_bt_source_failed),
            style = AniTextStyles.macCaption.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.error,
            maxLines = 1,
        )
        AppleTextButton(onRetry, Modifier.testTag(MacMediaSelectorTestTags.retry(source.instanceId))) {
            Text(stringResource(Lang.media_selector_retry), style = AniTextStyles.macCaption)
        }
    }
}

/**
 * 线路: 高 40、圆角 8, 「線路 · 解析度」13; 当前线路 tint 底 + onTint W600 +「播放中」.
 */
@Composable
private fun MacChannelRow(
    channel: WebSourceChannel,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurface
    Row(
        modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.selected = selected }
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            appleChannelLabel(channel.name, channel.original),
            Modifier.weight(1f),
            style = AniTextStyles.macCallout,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (selected) {
            Text(
                stringResource(Lang.media_selector_channel_playing),
                style = AniTextStyles.macCaption,
                color = contentColor,
                maxLines = 1,
            )
        }
    }
}

/**
 * 搜索框: 高 32、圆角 8、card 底, 13 号字. ↩ 提交关键字.
 */
@Composable
internal fun MacSelectorSearchField(
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val textStyle = AniTextStyles.macCallout.copy(color = MaterialTheme.colorScheme.onSurface)
    Row(
        modifier
            .height(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            AppleIcons.search(),
            contentDescription = null,
            Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.secondary,
        )
        BasicTextField(
            keyword,
            onValueChange = onKeywordChange,
            Modifier.weight(1f).testTag(MacMediaSelectorTestTags.SearchField),
            enabled = enabled,
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

/**
 * macOS 复选框: 16×16、圆角 4; 勾选为 accentFill 底 + 白色勾, 未勾选为 card 底 + separator 描边.
 * [onCheckedChange] 为 null 时只显示状态, 由外层处理点击.
 */
@Composable
internal fun MacCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(4.dp)
    Box(
        modifier
            .size(16.dp)
            .clip(shape)
            .background(if (checked) AniColors.accentFill else MaterialTheme.colorScheme.surfaceContainerLowest)
            .border(1.dp, if (checked) AniColors.accentFill else MaterialTheme.colorScheme.outlineVariant, shape)
            .then(
                if (onCheckedChange != null) {
                    Modifier.toggleable(checked, role = Role.Checkbox, onValueChange = onCheckedChange)
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(AppleIcons.check(), contentDescription = null, Modifier.size(11.dp), tint = AniColors.onAccentFill)
        }
    }
}

/**
 * 资料源没有资源时右栏的提示.
 */
@Composable
internal fun MacSelectorEmptyText(modifier: Modifier = Modifier) {
    Text(
        stringResource(Lang.media_selector_bt_empty),
        modifier,
        style = AniTextStyles.macCaption,
        color = MaterialTheme.colorScheme.secondary,
    )
}

internal object MacMediaSelectorDefaults {
    /** sheet 最高; 窗口较矮时扣除顶端列与下方留白. */
    val MaxHeight = 620.dp
    val MinHeight = 320.dp

    /** sheet 下缘与窗口底端的最小距离. */
    val BottomMargin = 24.dp

    val BottomBarHeight = 60.dp

    /** 自动匹配右栏的宽度. */
    val SideColumnWidth = 300.dp

    /** 手动查找左栏资料源清单的宽度. */
    val SourceListWidth = 220.dp

    /** 资料源行右侧状态的最大宽度. */
    val StatusMaxWidth = 180.dp
}

internal object MacMediaSelectorTestTags {
    const val Sheet = "mac-media-selector"
    const val Watching = "mac-media-selector-watching"
    const val Done = "mac-media-selector-done"
    const val Cancel = "mac-media-selector-cancel"
    const val Play = "mac-media-selector-play"
    const val ChannelsTitle = "mac-media-selector-channels-title"
    const val RescueButton = "mac-media-selector-rescue"
    const val SearchField = "mac-media-selector-search"
    const val RememberSelection = "mac-media-selector-remember"
    const val OpenedResult = "mac-media-selector-opened-result"
    const val EpisodeFilter = "mac-media-selector-episode-filter"
    const val SourceFilter = "mac-media-selector-source-filter"
    const val SourceMenu = "mac-media-selector-source-menu"
    const val FilterButton = "mac-media-selector-filter"
    const val FilterMenu = "mac-media-selector-filter-menu"
    const val ShowExcluded = "mac-media-selector-show-excluded"
    const val FilterSummary = "mac-media-selector-filter-summary"
    const val Table = "mac-media-selector-table"
    fun mode(mode: MediaSelectorMode) = "mac-media-selector-mode-${mode.name}"
    fun source(instanceId: String) = "mac-media-selector-source-$instanceId"
    fun failedSource(instanceId: String) = "mac-media-selector-failed-$instanceId"
    fun retry(instanceId: String) = "mac-media-selector-retry-$instanceId"
    fun channel(index: Int) = "mac-media-selector-channel-$index"
    fun manualSource(instanceId: String) = "mac-media-selector-manual-source-$instanceId"
    fun result(index: Int) = "mac-media-selector-result-$index"
    fun channelPill(index: Int) = "mac-media-selector-channel-pill-$index"
    fun episode(index: Int) = "mac-media-selector-episode-$index"
    fun btRow(mediaId: String) = "mac-media-selector-bt-row-$mediaId"
    fun btSource(mediaSourceId: String) = "mac-media-selector-bt-source-$mediaSourceId"
}
