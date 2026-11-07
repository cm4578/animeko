/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.download.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import io.ktor.http.Url
import kotlinx.coroutines.launch
import me.him188.ani.app.domain.media.cache.DownloaderStatus
import me.him188.ani.app.tools.getOrZero
import me.him188.ani.app.ui.download.components.DownloadItem
import me.him188.ani.app.ui.download.components.DownloadStatus
import me.him188.ani.app.ui.download.components.renderFileSize
import me.him188.ani.app.ui.download.components.renderSpeed
import me.him188.ani.app.ui.foundation.setClipEntryText
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_details_copied
import me.him188.ani.app.ui.lang.cache_details_last_error_summary
import me.him188.ani.app.ui.lang.cache_details_peers_none
import me.him188.ani.app.ui.lang.cache_details_peers_summary
import me.him188.ani.app.ui.lang.cache_details_segments
import me.him188.ani.app.ui.lang.cache_details_source_local
import me.him188.ani.app.ui.lang.cache_details_source_online
import me.him188.ani.app.ui.lang.cache_details_state_completed
import me.him188.ani.app.ui.lang.cache_details_state_downloading
import me.him188.ani.app.ui.lang.cache_details_state_failed
import me.him188.ani.app.ui.lang.cache_details_state_paused
import me.him188.ani.app.ui.lang.cache_details_transfer_summary
import me.him188.ani.app.ui.lang.cache_unknown
import me.him188.ani.app.ui.media.MediaDetailsRenderer
import me.him188.ani.app.ui.media.rememberMediaDetailsStrings
import me.him188.ani.datasources.api.source.MediaSourceKind
import me.him188.ani.datasources.api.topic.FileSize
import org.jetbrains.compose.resources.stringResource

/**
 * 单集下载详情 (03 §4.4、04 §4.3 检阅器) 状态卡上的第二个按钮 (05 G28): 下载中为暂停, 已暂停为继续.
 * 已完成与下载失败时没有, 同既有「⋮」菜单.
 */
internal enum class DownloadControlAction {
    Pause,
    Resume,
}

internal val DownloadItem.controlAction: DownloadControlAction?
    get() = when {
        isFinished || isFailed -> null
        isPaused -> DownloadControlAction.Resume
        else -> DownloadControlAction.Pause
    }

/**
 * 状态卡上是否有「播放」. 下载失败时没有, 同既有「⋮」菜单; 不能播放的下载在点击时提示原因.
 */
internal val DownloadItem.showsPlay: Boolean get() = !isFailed

/**
 * 状态卡上的百分比; 已完成为 100.
 */
internal val DownloadItem.progressPercent: Int
    get() = if (isFinished) 100 else (progress.getOrZero() * 100).toInt().coerceIn(0, 100)

/**
 * 状态卡上百分比后的状态文字. 有下载器状态时沿用详情页的写法 (含引擎的补充, 例如「下载中 · 正在获取种子信息」), 否则按下载状态.
 */
@Composable
internal fun downloadStatusLabel(download: DownloadItem, downloader: DownloaderDetails?): String {
    if (downloader != null) return downloadStateText(downloader)
    return stringResource(
        when (download.status) {
            DownloadStatus.IN_PROGRESS -> Lang.cache_details_state_downloading
            DownloadStatus.PAUSED -> Lang.cache_details_state_paused
            DownloadStatus.FAILED -> Lang.cache_details_state_failed
            DownloadStatus.COMPLETED -> Lang.cache_details_state_completed
        },
    )
}

/**
 * 传输行「870 MB / 1.4 GB · ↓ 2.1 MB/s · ↑ 320 KB/s」, 格式同详情页.
 */
@Composable
internal fun transferSummaryText(downloader: DownloaderDetails): String {
    val unknown = stringResource(Lang.cache_unknown)
    val stats = downloader.stats
    return stringResource(
        Lang.cache_details_transfer_summary,
        renderFileSize(stats.downloadedBytes).ifEmpty { unknown },
        renderFileSize(stats.totalSize).ifEmpty { unknown },
        renderSpeed(stats.downloadSpeed.orZero()),
        renderSpeed(stats.uploadSpeed.orZero()),
    )
}

private fun FileSize.orZero(): FileSize = if (isUnspecified) FileSize.Zero else this

/**
 * 传输行之后的一行说明. [isError] 为最终错误, 用 error 色.
 */
@Immutable
internal class DownloaderNote(val text: String, val isError: Boolean = false)

/**
 * 节点行等说明, 内容同详情页: BT 为节点数; HTTP 为片段进度、重试中的最近失败与最终错误; 云端为错误.
 */
@Composable
internal fun downloaderNotes(downloader: DownloaderDetails): List<DownloaderNote> = buildList {
    when (val status = downloader.status) {
        is DownloaderStatus.Torrent -> if (status.serviceConnected && status.startup == DownloaderStatus.TorrentStartup.STARTED) {
            add(
                DownloaderNote(
                    if (status.connectedPeers == 0) {
                        stringResource(Lang.cache_details_peers_none)
                    } else {
                        stringResource(Lang.cache_details_peers_summary, status.connectedPeers, status.seeds)
                    },
                ),
            )
        }

        is DownloaderStatus.Http -> {
            if (status.totalSegments > 0) {
                add(DownloaderNote(stringResource(Lang.cache_details_segments, status.downloadedSegments, status.totalSegments)))
            }
            status.lastSegmentFailure?.let {
                add(DownloaderNote(stringResource(Lang.cache_details_last_error_summary, it.attempt, it.maxAttempts, it.message)))
            }
            status.error?.let { add(DownloaderNote(it.technicalMessage ?: it.code.name, isError = true)) }
        }

        is DownloaderStatus.Cloud -> status.errorMessage?.let { add(DownloaderNote(it, isError = true)) }
        DownloaderStatus.Resolving, null -> Unit
    }
}

/**
 * 资料来源「[BT] 动漫花园」, 同详情页.
 */
@Composable
internal fun mediaSourceText(details: MediaDetails): String {
    val kind = when (details.kind) {
        MediaSourceKind.WEB -> stringResource(Lang.cache_details_source_online)
        MediaSourceKind.BitTorrent -> "BT"
        MediaSourceKind.LocalCache -> stringResource(Lang.cache_details_source_local)
    }
    return "[$kind] ${details.sourceInfo?.displayName ?: stringResource(Lang.cache_unknown)}"
}

@Composable
internal fun subtitleLanguagesText(details: MediaDetails): String {
    val strings = rememberMediaDetailsStrings()
    return remember(details, strings) {
        MediaDetailsRenderer.renderSubtitleLanguages(
            details.properties.subtitleKind,
            details.properties.subtitleLanguageIds,
            strings,
        )
    }
}

@Composable
internal fun fileSizeText(details: MediaDetails): String =
    if (details.fileSize == FileSize.Unspecified) stringResource(Lang.cache_unknown) else details.fileSize.toString()

/**
 * 复制文字并提示「已复制」.
 */
@Composable
internal fun copyTextHandler(): (String) -> Unit {
    val clipboard = LocalClipboard.current
    val toaster = LocalToaster.current
    val scope = rememberCoroutineScope()
    val copiedText = stringResource(Lang.cache_details_copied)
    return { text ->
        scope.launch {
            clipboard.setClipEntryText(text)
            toaster.toast(copiedText)
        }
    }
}

/**
 * 打开链接; 不是合法网址或系统无法打开时复制该链接, 同详情页的「打开链接」.
 */
@Composable
internal fun openLinkHandler(): (String) -> Unit {
    val uriHandler = LocalUriHandler.current
    val copy = copyTextHandler()
    return { url ->
        val opened = runCatching { Url(url) }.isSuccess && runCatching { uriHandler.openUri(url) }.isSuccess
        if (!opened) copy(url)
    }
}
