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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.him188.ani.app.platform.LocalContext
import me.him188.ani.app.platform.features.getComponentAccessors
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.download.DownloadManagementViewModel
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.components.DownloadStatus
import me.him188.ani.app.ui.download.subject.AppleDeleteDownloadsDialog
import me.him188.ani.app.ui.download.subject.downloadEpisodeTitle
import me.him188.ani.app.ui.download.subject.downloadPlayHandler
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.apple.AppleButton
import me.him188.ani.app.ui.foundation.apple.AppleButtonStyle
import me.him188.ani.app.ui.foundation.apple.AppleCircleButton
import me.him188.ani.app.ui.foundation.apple.AppleCover
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleProgressBar
import me.him188.ani.app.ui.foundation.apple.CoverAspect
import me.him188.ani.app.ui.foundation.apple.FullScreenTaskTopBar
import me.him188.ani.app.ui.foundation.apple.GroupedListDefaults
import me.him188.ani.app.ui.foundation.apple.GroupedListSection
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_details_browse_file
import me.him188.ani.app.ui.lang.cache_details_copy
import me.him188.ani.app.ui.lang.cache_details_external_subtitle
import me.him188.ani.app.ui.lang.cache_details_file_size
import me.him188.ani.app.ui.lang.cache_details_links_and_files
import me.him188.ani.app.ui.lang.cache_details_local_cache_path
import me.him188.ani.app.ui.lang.cache_details_open_file_failed
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
import me.him188.ani.app.ui.lang.cache_management_delete_cache_title
import me.him188.ani.app.ui.lang.cache_management_play
import me.him188.ani.app.ui.lang.cache_subject_cancel
import me.him188.ani.app.ui.lang.downloads_operation_failed
import me.him188.ani.utils.io.absolutePath
import me.him188.ani.utils.io.inSystem
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版单集下载详情 (03 §4.4). iOS 推入页, 由单番快取页点一集进入.
 *
 * 资源与下载器字段来自 [vm] (同 `MediaCacheDetailsScreen`); 番名、集名、封面、状态与按钮动作 (05 G28) 来自 [downloads]:
 * 「播放」同列表的播放, 「暂停下载 / 继续下载」为 [DownloadManagementViewModel.pauseDownload]、[DownloadManagementViewModel.resumeDownload];
 * 「删除下载」先确认, 删除后返回.
 *
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleMediaCacheDetailsScreen(
    vm: MediaCacheDetailsPageViewModel,
    downloads: DownloadManagementViewModel,
    cacheId: String,
    onPlay: (DownloadItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by vm.screenStateFlow.collectAsStateWithLifecycle()
    val downloadsState by downloads.uiState.collectAsStateWithLifecycle()
    val failedOperations by downloads.operationFailures.collectAsStateWithLifecycle()
    if (failedOperations > 0) {
        AlertDialog(
            onDismissRequest = downloads::dismissOperationFailures,
            text = { Text(stringResource(Lang.downloads_operation_failed, failedOperations)) },
            confirmButton = {
                TextButton(onClick = downloads::dismissOperationFailures) { Text(stringResource(Lang.cache_subject_cancel)) }
            },
        )
    }
    val group = downloadsState.groups.firstOrNull { group -> group.entries.any { it.id == cacheId } }
    AppleMediaCacheDetailsLayout(
        state,
        download = group?.entries?.firstOrNull { it.id == cacheId },
        coverUrl = group?.imageUrl,
        onPlay = onPlay,
        onPause = downloads::pauseDownload,
        onResume = downloads::resumeDownload,
        onDelete = {
            downloads.deleteDownload(it)
            onBack()
        },
        onBack = onBack,
        modifier = modifier,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
    )
}

/**
 * [AppleMediaCacheDetailsScreen] 的版面. [download] 为 `null` (尚未加载或已删除) 时不显示状态卡与删除.
 *
 * @param onDelete 确认删除之后调用.
 */
@Composable
internal fun AppleMediaCacheDetailsLayout(
    state: MediaCacheDetailsScreenState,
    download: DownloadItem?,
    coverUrl: String?,
    onPlay: (DownloadItem) -> Unit,
    onPause: (DownloadItem) -> Unit,
    onResume: (DownloadItem) -> Unit,
    onDelete: (DownloadItem) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val play = downloadPlayHandler(onPlay)
    var confirmDelete by remember { mutableStateOf(false) }
    if (confirmDelete && download != null) {
        AppleDeleteDownloadsDialog(
            onDismiss = { confirmDelete = false },
            confirmEnabled = !download.isBusy,
            onConfirm = {
                confirmDelete = false
                onDelete(download)
            },
        )
    }
    Column(modifier.fillMaxSize()) {
        FullScreenTaskTopBar(stringResource(Lang.cache_details_title), onBack = onBack, windowInsets = windowInsets)
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(
                top = 8.dp,
                bottom = contentPadding.calculateBottomPadding() + 24.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(GroupedListDefaults.SectionSpacing),
        ) {
            if (download != null) {
                item(key = "status") {
                    StatusCard(
                        download,
                        state.downloader,
                        coverUrl,
                        onPlay = { play(download) },
                        onPause = { onPause(download) },
                        onResume = { onResume(download) },
                        Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
            val details = state.details
            if (details != null) {
                item(key = "resource") { ResourceSection(details, Modifier.padding(horizontal = 16.dp)) }
                item(key = "links") { LinksSection(details, Modifier.padding(horizontal = 16.dp)) }
            }
            if (download != null) {
                item(key = "delete") {
                    Box(
                        Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .clickable(role = Role.Button) { confirmDelete = true }
                            .testTag(AppleMediaCacheDetailsTestTags.Delete),
                        contentAlignment = Alignment.Center,
                    ) {
                        // 不用红色 (03 §4.4).
                        Text(
                            stringResource(Lang.cache_management_delete_cache_title),
                            style = AniTextStyles.body,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 状态卡: card 圆角 16、内距 16. 封面 48×64 + 番名 `footnote` label2 + 集名 17/22 W700; 「62%」28/34 W700 + 状态 `callout` label2;
 * 6 高进度条; 传输行 `footnote` labelSoft 与节点行 `footnote` label2; 「播放」(主要) 与「暂停下载 / 继续下载」(次要), 高 44.
 */
@Composable
private fun StatusCard(
    download: DownloadItem,
    downloader: DownloaderDetails?,
    coverUrl: String?,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(colorScheme.surfaceContainerLow)
            .padding(16.dp)
            .testTag(AppleMediaCacheDetailsTestTags.StatusCard),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            AppleCover(CoverAspect.Portrait3x4, download.subjectName, Modifier.width(48.dp), cornerRadius = 8.dp) {
                AsyncImage(coverUrl, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    download.subjectName,
                    style = AniTextStyles.footnote,
                    color = colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    downloadEpisodeTitle(download.sort, download.displayName),
                    style = AniTextStyles.headline,
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "${download.progressPercent}%",
                    Modifier.alignByBaseline().testTag(AppleMediaCacheDetailsTestTags.Percent),
                    style = AniTextStyles.largeTitle.copy(fontSize = 28.sp, lineHeight = 34.sp, fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Bold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    downloadStatusLabel(download, downloader),
                    Modifier.alignByBaseline().weight(1f, fill = false),
                    style = AniTextStyles.callout,
                    color = colorScheme.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            AppleProgressBar(
                { download.progressPercent / 100f },
                muted = download.status != DownloadStatus.IN_PROGRESS,
                height = 6.dp,
            )
            if (downloader != null) {
                Text(
                    transferSummaryText(downloader),
                    style = AniTextStyles.footnote.copy(fontFeatureSettings = "tnum"),
                    color = AniColors.labelSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                downloaderNotes(downloader).forEach { note ->
                    Text(
                        note.text,
                        style = AniTextStyles.footnote,
                        color = if (note.isError) colorScheme.error else colorScheme.secondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        val control = download.controlAction
        if (download.showsPlay || control != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (download.showsPlay) {
                    AppleButton(
                        onPlay,
                        Modifier.weight(1f).testTag(AppleMediaCacheDetailsTestTags.Play),
                        style = AppleButtonStyle.Primary,
                    ) { Text(stringResource(Lang.cache_management_play)) }
                }
                if (control != null) {
                    AppleButton(
                        if (control == DownloadControlAction.Pause) onPause else onResume,
                        Modifier.weight(1f).testTag(AppleMediaCacheDetailsTestTags.PauseOrResume),
                        style = AppleButtonStyle.Secondary,
                        enabled = !download.isBusy,
                    ) {
                        Text(
                            stringResource(
                                if (control == DownloadControlAction.Pause) Lang.cache_episode_pause_download else Lang.cache_episode_resume_download,
                            ),
                        )
                    }
                }
            }
        }
    }
}

/**
 * 资源资讯: 原始标题 (两行, 附复制钮) 与资料来源、字幕组、字幕语言、解析度、文件大小、发布时间.
 * 资料来源在网址可用时附「打开链接」, 同详情页.
 */
@Composable
private fun ResourceSection(details: MediaDetails, modifier: Modifier = Modifier) {
    val copy = copyTextHandler()
    val openLink = openLinkHandler()
    GroupedListSection(modifier.testTag(AppleMediaCacheDetailsTestTags.Resource), title = stringResource(Lang.cache_details_resource_info)) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                details.originalTitle,
                Modifier.weight(1f),
                style = AniTextStyles.body,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            AppleCircleButton(
                { copy(details.originalTitle) },
                AppleIcons.copy(),
                stringResource(Lang.cache_details_copy),
                Modifier.testTag(AppleMediaCacheDetailsTestTags.CopyTitle),
            )
        }
        KeyValueRow(stringResource(Lang.cache_details_source), mediaSourceText(details)) {
            if (details.isUrlLegal) {
                AppleCircleButton({ openLink(details.originalUrl) }, AppleIcons.externalLink(), stringResource(Lang.cache_details_open_link))
            }
        }
        KeyValueRow(stringResource(Lang.cache_details_subtitle_group), details.properties.alliance)
        KeyValueRow(stringResource(Lang.cache_details_subtitle_language), subtitleLanguagesText(details))
        KeyValueRow(stringResource(Lang.cache_details_resolution), details.properties.resolution)
        KeyValueRow(stringResource(Lang.cache_details_file_size), fileSizeText(details))
        KeyValueRow(stringResource(Lang.cache_details_publish_time), formatDateTime(details.publishedTimeMillis))
    }
}

/**
 * 键值列: 高 48, 键 `body`, 值 `callout` label2 右对齐; 分隔线从 16 开始.
 */
@Composable
private fun KeyValueRow(
    key: String,
    value: String,
    trailing: @Composable () -> Unit = {},
) {
    Column {
        AppleDivider(startIndent = 16.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .padding(start = 16.dp, end = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(key, style = AniTextStyles.body, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
            Text(
                value,
                Modifier.weight(1f),
                style = AniTextStyles.callout,
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
 * 连结与文件: 原始链接、原始下载链接、本地下载路径、外挂字幕. 每列为标签 `footnote` label2 + 值 `subhead` labelSoft + 36 动作钮
 * (打开链接; 本地文件为浏览文件, 平台不支持时不显示).
 */
@Composable
private fun LinksSection(details: MediaDetails, modifier: Modifier = Modifier) {
    val openLink = openLinkHandler()
    val fileRevealer = LocalContext.current.getComponentAccessors().fileRevealer
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    GroupedListSection(modifier.testTag(AppleMediaCacheDetailsTestTags.Links), title = stringResource(Lang.cache_details_links_and_files)) {
        LinkRow(stringResource(Lang.cache_details_original_link), details.contentOriginalUri, showDivider = false) {
            AppleCircleButton({ openLink(details.contentOriginalUri) }, AppleIcons.externalLink(), stringResource(Lang.cache_details_open_link))
        }
        details.contentDownloadUri?.let { uri ->
            LinkRow(stringResource(Lang.cache_details_original_download_link), uri) {
                AppleCircleButton({ openLink(uri) }, AppleIcons.externalLink(), stringResource(Lang.cache_details_open_link))
            }
        }
        details.localCacheFilePath?.let { path ->
            LinkRow(stringResource(Lang.cache_details_local_cache_path), path.toString()) {
                if (fileRevealer != null) {
                    AppleCircleButton(
                        {
                            scope.launch {
                                if (!fileRevealer.revealFile(path)) {
                                    toaster.toast(getString(Lang.cache_details_open_file_failed, path.inSystem.absolutePath))
                                }
                            }
                        },
                        AppleIcons.externalLink(),
                        stringResource(Lang.cache_details_browse_file),
                    )
                }
            }
        }
        details.extraFiles.subtitles.forEachIndexed { index, subtitle ->
            val label = buildString {
                append(stringResource(Lang.cache_details_external_subtitle, index + 1))
                subtitle.language?.let { append(": ").append(it) }
            }
            LinkRow(label, subtitle.uri) {
                AppleCircleButton({ openLink(subtitle.uri) }, AppleIcons.externalLink(), stringResource(Lang.cache_details_open_link))
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
        if (showDivider) AppleDivider(startIndent = 16.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 10.dp, end = 8.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(label, style = AniTextStyles.footnote, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
                Text(
                    value,
                    style = AniTextStyles.subhead,
                    color = AniColors.labelSoft,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            action()
        }
    }
}

internal object AppleMediaCacheDetailsTestTags {
    const val StatusCard = "cache-details-status"
    const val Percent = "cache-details-percent"
    const val Play = "cache-details-play"
    const val PauseOrResume = "cache-details-pause-or-resume"
    const val Resource = "cache-details-resource"
    const val CopyTitle = "cache-details-copy-title"
    const val Links = "cache-details-links"
    const val Delete = "cache-details-delete"
}
