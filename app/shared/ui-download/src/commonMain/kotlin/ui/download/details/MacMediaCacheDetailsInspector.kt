/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.download.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.io.files.Path
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.components.DownloadStatus
import me.him188.ani.app.ui.download.subject.downloadEpisodeTitle
import me.him188.ani.app.ui.foundation.apple.AppleButton
import me.him188.ani.app.ui.foundation.apple.AppleButtonStyle
import me.him188.ani.app.ui.foundation.apple.AppleCircleButton
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleProgressBar
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.MacInspector
import me.him188.ani.app.ui.foundation.apple.MacInspectorCard
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_details_copy
import me.him188.ani.app.ui.lang.cache_details_external_subtitle
import me.him188.ani.app.ui.lang.cache_details_file_size
import me.him188.ani.app.ui.lang.cache_details_links_and_files
import me.him188.ani.app.ui.lang.cache_details_local_cache_path
import me.him188.ani.app.ui.lang.cache_details_open_link
import me.him188.ani.app.ui.lang.cache_details_original_download_link
import me.him188.ani.app.ui.lang.cache_details_original_link
import me.him188.ani.app.ui.lang.cache_details_publish_time
import me.him188.ani.app.ui.lang.cache_details_resolution
import me.him188.ani.app.ui.lang.cache_details_resource_info
import me.him188.ani.app.ui.lang.cache_details_source
import me.him188.ani.app.ui.lang.cache_details_subtitle_group
import me.him188.ani.app.ui.lang.cache_details_subtitle_language
import me.him188.ani.app.ui.lang.cache_details_title
import me.him188.ani.app.ui.lang.cache_episode_pause_download
import me.him188.ani.app.ui.lang.cache_episode_resume_download
import me.him188.ani.app.ui.lang.cache_management_delete_download
import me.him188.ani.app.ui.lang.cache_management_play
import me.him188.ani.app.ui.lang.cache_management_show_in_finder
import me.him188.ani.app.ui.lang.subject_episode_close
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 单番快取页右侧的 C21 检阅器 (04 §4.3、05 C3): 所选一集的下载详情.
 *
 * 资源与下载器字段同 `MediaCacheDetailsScreen` ([state]); 番名、集名、状态与按钮来自所选的下载 [download], 动作由调用处传入 (05 G28).
 * 从上到下: 状态卡、资源资讯、连结与文件、「删除下载…」(不用红色).
 *
 * @param onDelete 点「删除下载…」, 由调用处先确认.
 * @param onRevealFile 在 Finder 中显示本地文件.
 */
@Composable
internal fun MacMediaCacheDetailsInspector(
    state: MediaCacheDetailsScreenState,
    download: DownloadItem,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    onRevealFile: (Path) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MacInspector(
        title = stringResource(Lang.cache_details_title),
        onClose = onClose,
        closeContentDescription = stringResource(Lang.subject_episode_close),
        modifier = modifier.testTag(MacMediaCacheDetailsTestTags.Inspector),
    ) {
        StatusCard(download, state.downloader, onPlay, onPause, onResume)
        state.details?.let { details ->
            ResourceCard(details)
            LinksCard(details, onRevealFile)
        }
        Box(
            Modifier
                .fillMaxWidth()
                .height(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable(role = Role.Button, onClick = onDelete)
                .testTag(MacMediaCacheDetailsTestTags.Delete),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(Lang.cache_management_delete_download),
                style = AniTextStyles.macCallout,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

/**
 * 状态卡 (card 圆角 12、内距 14): 番名 12 label2、「第 N 话 · 集名」14/700; 「62%」28/700 + 状态 13; 6 高进度条;
 * 传输行与节点行 12 label2; 等宽的「播放」(accentFill) 与「暂停下载 / 继续下载」(fill), 已完成时只有播放.
 */
@Composable
private fun StatusCard(
    download: DownloadItem,
    downloader: DownloaderDetails?,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    MacInspectorCard(Modifier.testTag(MacMediaCacheDetailsTestTags.StatusCard), contentPadding = PaddingValues(14.dp)) {
        Text(
            download.subjectName,
            style = AniTextStyles.macCaption,
            color = colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            downloadEpisodeTitle(download.sort, download.displayName),
            Modifier.padding(top = 2.dp),
            style = AniTextStyles.macBody,
            fontWeight = FontWeight.Bold,
            color = colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                "${download.progressPercent}%",
                Modifier.alignByBaseline().testTag(MacMediaCacheDetailsTestTags.Percent),
                style = AniTextStyles.macLargeTitle.copy(fontSize = 28.sp, lineHeight = 34.sp, fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                downloadStatusLabel(download, downloader),
                Modifier.alignByBaseline().weight(1f, fill = false),
                style = AniTextStyles.macCallout,
                color = colorScheme.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AppleProgressBar(
            { download.progressPercent / 100f },
            Modifier.padding(top = 8.dp),
            muted = download.status != DownloadStatus.IN_PROGRESS,
            height = 6.dp,
        )
        if (downloader != null) {
            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    transferSummaryText(downloader),
                    style = AniTextStyles.macCaption.copy(fontFeatureSettings = "tnum"),
                    color = colorScheme.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                downloaderNotes(downloader).forEach { note ->
                    Text(
                        note.text,
                        style = AniTextStyles.macCaption,
                        color = if (note.isError) colorScheme.error else colorScheme.secondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        val control = download.controlAction
        if (download.showsPlay || control != null) {
            Row(Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (download.showsPlay) {
                    AppleButton(
                        onPlay,
                        Modifier.weight(1f).testTag(MacMediaCacheDetailsTestTags.Play),
                        style = AppleButtonStyle.Primary,
                    ) { Text(stringResource(Lang.cache_management_play), maxLines = 1) }
                }
                if (control != null) {
                    AppleButton(
                        if (control == DownloadControlAction.Pause) onPause else onResume,
                        Modifier.weight(1f).testTag(MacMediaCacheDetailsTestTags.PauseOrResume),
                        style = AppleButtonStyle.Secondary,
                        enabled = !download.isBusy,
                    ) {
                        Text(
                            stringResource(
                                if (control == DownloadControlAction.Pause) Lang.cache_episode_pause_download else Lang.cache_episode_resume_download,
                            ),
                            maxLines = 1,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 资源资讯: 原始标题 (两行 + 拷贝钮), 资料来源、字幕组、字幕语言、解析度、文件大小、发布时间 (行高 30, 左右 12, 值右对齐).
 */
@Composable
private fun ResourceCard(details: MediaDetails) {
    val copy = copyTextHandler()
    val openLink = openLinkHandler()
    MacInspectorCard(Modifier.testTag(MacMediaCacheDetailsTestTags.Resource), title = stringResource(Lang.cache_details_resource_info)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 6.dp, end = 4.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                details.originalTitle,
                Modifier.weight(1f),
                style = AniTextStyles.macCallout,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            AppleCircleButton({ copy(details.originalTitle) }, AppleIcons.copy(), stringResource(Lang.cache_details_copy))
        }
        KeyValueRow(stringResource(Lang.cache_details_source), mediaSourceText(details)) {
            if (details.isUrlLegal) {
                AppleCircleButton(
                    { openLink(details.originalUrl) },
                    AppleIcons.macExternalLink(),
                    stringResource(Lang.cache_details_open_link),
                )
            }
        }
        KeyValueRow(stringResource(Lang.cache_details_subtitle_group), details.properties.alliance)
        KeyValueRow(stringResource(Lang.cache_details_subtitle_language), subtitleLanguagesText(details))
        KeyValueRow(stringResource(Lang.cache_details_resolution), details.properties.resolution)
        KeyValueRow(stringResource(Lang.cache_details_file_size), fileSizeText(details))
        KeyValueRow(stringResource(Lang.cache_details_publish_time), formatDateTime(details.publishedTimeMillis))
    }
}

@Composable
private fun KeyValueRow(
    key: String,
    value: String,
    trailing: @Composable () -> Unit = {},
) {
    Column {
        AppleDivider(startIndent = 12.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 30.dp)
                .padding(start = 12.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(key, style = AniTextStyles.macCallout, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(
                value,
                Modifier.weight(1f),
                style = AniTextStyles.macCallout,
                color = MaterialTheme.colorScheme.secondary,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            trailing()
        }
    }
}

/**
 * 连结与文件: 原始链接 (magnet 等)、原始下载链接、本地下载路径与外挂字幕, 值为等宽字体; 下方为「打开链接」或「在 Finder 中显示」.
 */
@Composable
private fun LinksCard(details: MediaDetails, onRevealFile: (Path) -> Unit) {
    val openLink = openLinkHandler()
    MacInspectorCard(Modifier.testTag(MacMediaCacheDetailsTestTags.Links), title = stringResource(Lang.cache_details_links_and_files)) {
        LinkRow(stringResource(Lang.cache_details_original_link), details.contentOriginalUri, showDivider = false) {
            AppleTextButton({ openLink(details.contentOriginalUri) }) { Text(stringResource(Lang.cache_details_open_link)) }
        }
        details.contentDownloadUri?.let { uri ->
            LinkRow(stringResource(Lang.cache_details_original_download_link), uri) {
                AppleTextButton({ openLink(uri) }) { Text(stringResource(Lang.cache_details_open_link)) }
            }
        }
        details.localCacheFilePath?.let { path ->
            LinkRow(stringResource(Lang.cache_details_local_cache_path), path.toString()) {
                AppleTextButton({ onRevealFile(path) }, Modifier.testTag(MacMediaCacheDetailsTestTags.RevealFile)) {
                    Text(stringResource(Lang.cache_management_show_in_finder))
                }
            }
        }
        details.extraFiles.subtitles.forEachIndexed { index, subtitle ->
            val label = buildString {
                append(stringResource(Lang.cache_details_external_subtitle, index + 1))
                subtitle.language?.let { append(": ").append(it) }
            }
            LinkRow(label, subtitle.uri) {
                AppleTextButton({ openLink(subtitle.uri) }) { Text(stringResource(Lang.cache_details_open_link)) }
            }
        }
    }
}

@Composable
private fun LinkRow(
    label: String,
    value: String,
    showDivider: Boolean = true,
    action: @Composable () -> Unit,
) {
    Column {
        if (showDivider) AppleDivider(startIndent = 12.dp)
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, top = 8.dp, end = 12.dp, bottom = 2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(label, style = AniTextStyles.macCaption, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
            Text(
                value,
                style = AniTextStyles.macCaption.copy(fontFamily = FontFamily.Monospace),
                color = AniColors.labelSoft,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            // 文字按钮左右各有 4 的内距, 左移 4 让文字与标签对齐.
            Box(Modifier.offset(x = (-4).dp)) { action() }
        }
    }
}

internal object MacMediaCacheDetailsTestTags {
    const val Inspector = "mac-cache-details-inspector"
    const val StatusCard = "mac-cache-details-status"
    const val Percent = "mac-cache-details-percent"
    const val Play = "mac-cache-details-play"
    const val PauseOrResume = "mac-cache-details-pause-or-resume"
    const val Resource = "mac-cache-details-resource"
    const val Links = "mac-cache-details-links"
    const val RevealFile = "mac-cache-details-reveal-file"
    const val Delete = "mac-cache-details-delete"
}
