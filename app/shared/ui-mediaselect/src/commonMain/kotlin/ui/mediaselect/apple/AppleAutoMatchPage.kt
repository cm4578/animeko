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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.him188.ani.app.domain.mediasource.web.captcha.SolveOutcome
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.GroupedListCard
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.media_selector_auto_rescue
import me.him188.ani.app.ui.lang.media_selector_bt_empty
import me.him188.ani.app.ui.lang.media_selector_bt_source_failed
import me.him188.ani.app.ui.lang.media_selector_bt_source_querying
import me.him188.ani.app.ui.lang.media_selector_channel_playing
import me.him188.ani.app.ui.lang.media_selector_manual_captcha_unsupported
import me.him188.ani.app.ui.lang.media_selector_retry
import me.him188.ani.app.ui.lang.media_selector_web_channel_count
import me.him188.ani.app.ui.lang.media_selector_web_rate_limited
import me.him188.ani.app.ui.lang.media_selector_web_waiting_captcha
import me.him188.ani.app.ui.media.renderResolution
import me.him188.ani.app.ui.media.webCaptchaRequiredMessage
import me.him188.ani.app.ui.mediafetch.MediaSelectorState
import me.him188.ani.app.ui.mediaselect.selector.WebSource
import me.him188.ani.app.ui.mediaselect.selector.WebSourceChannel
import me.him188.ani.app.ui.mediaselect.selector.rememberRateLimitRemainingSeconds
import me.him188.ani.datasources.api.Media
import me.him188.ani.datasources.api.source.MediaSourceTier
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版自动匹配页 (03 §3.8): 资料源列在 C5 卡片里, 当前资料源下方展开线路. 整页可滚动.
 *
 * @param onClickItem 用户点了线路; 宿主负责 select 并关闭面板.
 * @param onRequestManualSearch 点了「找不到想看的？手动查找」; null 时不显示.
 */
@Composable
fun AppleAutoMatchPage(
    state: MediaSelectorState,
    onClickItem: (Media) -> Unit,
    onRestartSource: (instanceId: String) -> Unit,
    onRequestManualSearch: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val presentation by state.presentationFlow.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    AppleWebSourceList(
        presentation.webSources,
        selectedSource = presentation.selectedWebSource,
        selectedChannel = presentation.selectedWebSourceChannel,
        onSelect = { channel -> channel.original?.let(onClickItem) },
        onRefresh = { onRestartSource(it.instanceId) },
        onResolveCaptcha = { source ->
            scope.launch {
                if (state.resolveCaptcha(source) == SolveOutcome.Solved) {
                    onRestartSource(source.instanceId)
                }
            }
        },
        onRequestManualSearch = onRequestManualSearch,
        modifier.verticalScroll(rememberScrollState()),
    )
}

/**
 * 顺序同现有的自动匹配页: 正常的源 → 「找不到想看的？手動查找」 → 查询失败的源. 失败的源单独一张卡.
 *
 * 资料源列高 52: 名称 `16/22 600` + 阶级标签 + 右侧状态 `13/18` label2. 默认展开当前资料源, 点其他有线路的源改为展开它, 再点收起.
 * 线路列高 44、圆角 12、左右内缩 8: 当前线路 tint 底 + onTint、「線路 · 解析度」`15/20 600` + 「播放中」; 其他线路 labelSoft.
 */
@Composable
internal fun AppleWebSourceList(
    sources: List<WebSource>,
    selectedSource: WebSource?,
    selectedChannel: WebSourceChannel?,
    onSelect: (WebSourceChannel) -> Unit,
    onRefresh: (WebSource) -> Unit,
    onResolveCaptcha: (WebSource) -> Unit,
    onRequestManualSearch: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    // null: 跟随当前资料源; 用户点过之后为点开的资料源, 空串表示都收起.
    var expandedInstanceId by rememberSaveable { mutableStateOf<String?>(null) }
    val effectiveExpandedId = expandedInstanceId ?: selectedSource?.instanceId
    val (failedSources, otherSources) = sources.partition { it.isError }
    Column(
        modifier.fillMaxWidth().padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (otherSources.isNotEmpty()) {
            GroupedListCard {
                otherSources.forEachIndexed { index, source ->
                    val expanded = source.instanceId == effectiveExpandedId && source.channels.isNotEmpty()
                    if (index > 0) AppleDivider(startIndent = 16.dp)
                    AppleWebSourceRow(
                        source,
                        expanded = expanded,
                        onClick = { expandedInstanceId = if (expanded) "" else source.instanceId },
                        onResolveCaptcha = { onResolveCaptcha(source) },
                    )
                    if (expanded) {
                        AppleChannelList(
                            source.channels,
                            selected = selectedChannel.takeIf { selectedSource?.instanceId == source.instanceId },
                            onSelect = onSelect,
                        )
                    }
                }
            }
        }
        if (onRequestManualSearch != null) {
            AppleTextButton(onRequestManualSearch, Modifier.testTag(AppleMediaSelectorTestTags.RescueButton)) {
                Text(stringResource(Lang.media_selector_auto_rescue))
            }
        }
        if (failedSources.isNotEmpty()) {
            GroupedListCard {
                failedSources.forEachIndexed { index, source ->
                    if (index > 0) AppleDivider(startIndent = 16.dp)
                    AppleFailedSourceRow(source, onRetry = { onRefresh(source) })
                }
            }
        }
    }
}

@Composable
private fun AppleWebSourceRow(
    source: WebSource,
    expanded: Boolean,
    onClick: () -> Unit,
    onResolveCaptcha: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .clickable(enabled = source.channels.isNotEmpty(), role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag(AppleMediaSelectorTestTags.source(source.instanceId)),
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
                style = AniTextStyles.body.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            source.tier?.let { AppleTierBadge(it) }
        }
        AppleWebSourceStatus(source, onResolveCaptcha, Modifier.widthIn(max = 168.dp))
        if (source.channels.isNotEmpty()) {
            Icon(
                AppleIcons.chevronDown(),
                contentDescription = null,
                Modifier.size(14.dp).rotate(if (expanded) 180f else 0f),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

/**
 * 资料源列右侧的状态: 验证码、限流倒数、查询中、线路数量或没有资源.
 */
@Composable
private fun AppleWebSourceStatus(
    source: WebSource,
    onResolveCaptcha: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val style = AniTextStyles.footnote
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        val rateLimitedUntil = source.rateLimitedUntilMillis
        when {
            source.isCaptchaRequired && !source.isCaptchaSupported -> Text(
                stringResource(Lang.media_selector_manual_captcha_unsupported),
                style = style,
                color = MaterialTheme.colorScheme.error,
                maxLines = 2,
            )

            source.isCaptchaRequired -> Text(
                if (source.isResolvingCaptcha) {
                    stringResource(Lang.media_selector_web_waiting_captcha)
                } else {
                    source.captchaKind?.let { webCaptchaRequiredMessage(it) }.orEmpty()
                },
                Modifier.clickable(enabled = !source.isResolvingCaptcha, onClick = onResolveCaptcha),
                style = style,
                color = MaterialTheme.colorScheme.error,
                maxLines = 2,
            )

            rateLimitedUntil != null -> {
                val remainingSeconds by rememberRateLimitRemainingSeconds(rateLimitedUntil)
                Text(
                    stringResource(Lang.media_selector_web_rate_limited, remainingSeconds),
                    style = style,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                )
            }

            source.channels.isNotEmpty() -> Text(
                stringResource(Lang.media_selector_web_channel_count, source.channels.size),
                style = style,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
            )

            source.isLoading -> Text(
                stringResource(Lang.media_selector_bt_source_querying),
                style = style,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
            )

            else -> Text(
                stringResource(Lang.media_selector_bt_empty),
                style = style,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
            )
        }
        if (source.isLoading) {
            CircularProgressIndicator(
                Modifier.size(14.dp),
                color = MaterialTheme.colorScheme.secondary,
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
private fun AppleChannelList(
    channels: List<WebSourceChannel>,
    selected: WebSourceChannel?,
    onSelect: (WebSourceChannel) -> Unit,
) {
    Column(Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
        channels.forEachIndexed { index, channel ->
            val isSelected = channel == selected
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                    .clickable(role = Role.Button) { onSelect(channel) }
                    .semantics { this.selected = isSelected }
                    .padding(horizontal = 12.dp)
                    .testTag(AppleMediaSelectorTestTags.channel(index)),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    appleChannelLabel(channel.name, channel.original),
                    Modifier.weight(1f),
                    style = AniTextStyles.callout.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else AniColors.labelSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isSelected) {
                    Text(
                        stringResource(Lang.media_selector_channel_playing),
                        style = AniTextStyles.footnote,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * 线路名与解析度, 例如「線路 1 · 1080P」; 没有解析度时只有线路名.
 */
internal fun appleChannelLabel(name: String, media: Media?): String = listOfNotNull(
    name,
    media?.properties?.resolution?.takeIf { it.isNotBlank() }?.let(::renderResolution),
).joinToString(" · ")

/**
 * 查询失败的资料源: 名称 labelSoft + 「查詢失敗」`13/18 600` error + 「重試」accent.
 */
@Composable
private fun AppleFailedSourceRow(
    source: WebSource,
    onRetry: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .padding(start = 16.dp, end = 8.dp)
            .testTag(AppleMediaSelectorTestTags.failedSource(source.instanceId)),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            source.name,
            Modifier.weight(1f),
            style = AniTextStyles.body,
            color = AniColors.labelSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            stringResource(Lang.media_selector_bt_source_failed),
            style = AniTextStyles.footnote.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.error,
            maxLines = 1,
        )
        AppleTextButton(onRetry, Modifier.testTag(AppleMediaSelectorTestTags.retry(source.instanceId))) {
            Text(stringResource(Lang.media_selector_retry))
        }
    }
}

/**
 * 阶级标签: 高 18、圆角 9、fill 底、`11 700` label2. 文字同数据源设置页的「T0」.
 */
@Composable
internal fun AppleTierBadge(tier: MediaSourceTier, modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(18.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "T${tier.value}",
            style = AniTextStyles.caption.copy(fontSize = 11.sp, lineHeight = 13.sp, fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
        )
    }
}
