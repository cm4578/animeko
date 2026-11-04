/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.episode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.rounded.AspectRatio
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.FullscreenExit
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import me.him188.ani.app.domain.media.player.MediaCacheProgressInfo
import me.him188.ani.app.domain.player.VideoLoadingState
import me.him188.ani.app.ui.episode.share.MediaShareData
import me.him188.ani.app.ui.foundation.animation.AniAnimatedVisibility
import me.him188.ani.app.ui.foundation.animation.LocalAniMotionScheme
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.ApplePopoverMenu
import me.him188.ani.app.ui.foundation.apple.CollectionTypeMenuItems
import me.him188.ani.app.ui.foundation.apple.PopoverMenuItem
import me.him188.ani.app.ui.foundation.apple.UnfollowConfirmDialog
import me.him188.ani.app.ui.foundation.apple.collectionTypeName
import me.him188.ani.app.ui.foundation.icons.AniIcons
import me.him188.ani.app.ui.foundation.icons.Forward80
import me.him188.ani.app.ui.foundation.icons.Forward85
import me.him188.ani.app.ui.foundation.icons.Forward90
import me.him188.ani.app.ui.foundation.icons.SubtitleGear
import me.him188.ani.app.ui.foundation.navigation.LocalBackDispatcher
import me.him188.ani.app.ui.foundation.rememberAsyncHandler
import me.him188.ani.app.ui.foundation.setClipEntryText
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.showLoadError
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.episode_menu_collection_state
import me.him188.ani.app.ui.lang.subject_collection_delete
import me.him188.ani.app.ui.lang.subject_details_login_to_collect
import me.him188.ani.app.ui.lang.subject_details_share
import me.him188.ani.app.ui.lang.subject_episode_cache
import me.him188.ani.app.ui.lang.subject_episode_danmaku_settings_title
import me.him188.ani.app.ui.lang.subject_episode_fast_forward_seconds
import me.him188.ani.app.ui.lang.subject_episode_more_options
import me.him188.ani.app.ui.lang.subject_episode_share_copy_link
import me.him188.ani.app.ui.lang.subject_episode_share_copy_source_page
import me.him188.ani.app.ui.lang.subject_episode_share_local_file_link
import me.him188.ani.app.ui.lang.subject_episode_share_magnet_link
import me.him188.ani.app.ui.lang.subject_episode_share_open_link
import me.him188.ani.app.ui.lang.subject_episode_share_open_source_page
import me.him188.ani.app.ui.lang.subject_episode_share_stream_link
import me.him188.ani.app.ui.lang.subject_episode_share_torrent_download_link
import me.him188.ani.app.ui.lang.subject_episode_share_webpage_link
import me.him188.ani.app.ui.lang.video_player_aspect_ratio
import me.him188.ani.app.ui.lang.video_player_danmaku_toggle_label
import me.him188.ani.app.ui.lang.video_player_disable_danmaku
import me.him188.ani.app.ui.lang.video_player_enable_danmaku
import me.him188.ani.app.ui.lang.video_player_enter_fullscreen
import me.him188.ani.app.ui.lang.video_player_exit_fullscreen
import me.him188.ani.app.ui.lang.video_player_off
import me.him188.ani.app.ui.lang.video_player_pause
import me.him188.ani.app.ui.lang.video_player_performance
import me.him188.ani.app.ui.lang.video_player_play
import me.him188.ani.app.ui.lang.video_player_quality
import me.him188.ani.app.ui.lang.video_player_stats_title_hide
import me.him188.ani.app.ui.lang.video_player_stats_title_show
import me.him188.ani.app.ui.lang.video_player_video_enhancement
import me.him188.ani.app.ui.lang.watch_together_title
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.episode.video.components.EpisodeVideoSideSheetPage
import me.him188.ani.app.ui.subject.episode.video.loading.shouldShowVideoLoadingIndicator
import me.him188.ani.app.videoplayer.ui.PlayerControllerState
import me.him188.ani.app.videoplayer.ui.VideoAspectRatioControllerState
import me.him188.ani.app.videoplayer.ui.VideoSideSheetsController
import me.him188.ani.app.videoplayer.ui.progress.MediaProgressFramePreviewState
import me.him188.ani.app.videoplayer.ui.progress.MediaProgressSlider
import me.him188.ani.app.videoplayer.ui.progress.MediaProgressSliderDefaults
import me.him188.ani.app.videoplayer.ui.progress.PlayerProgressSliderState
import me.him188.ani.app.videoplayer.ui.progress.TouchSeekState
import me.him188.ani.app.videoplayer.ui.rememberAlwaysOnRequester
import me.him188.ani.app.videoplayer.ui.renderAspectRatioMode
import me.him188.ani.app.videoplayer.videoenhancement.VideoEnhancementController
import me.him188.ani.app.videoplayer.videoenhancement.VideoEnhancementMode
import me.him188.ani.datasources.api.topic.ResourceLocation
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.jetbrains.compose.resources.stringResource
import org.openani.mediamp.MediaStatus
import org.openani.mediamp.MediampPlayer
import org.openani.mediamp.togglePlayWhenReady
import kotlin.time.Duration

/**
 * Apple 版直式播放器 (03 §3.7) 在现有参数之外需要的内容. 只在非全屏时套用, 全屏沿用现有控制列.
 *
 * @param episodeTitle 顶栏的集名, 见 [appleEpisodeLabel].
 * @param collectionTypeState 「⋯」菜单中的追番状态, 与现有的收藏按钮共用.
 * @param isLoggedIn 未登录时「追番状态」一列换成「登入後可收藏」.
 */
@Stable
internal class AppleEpisodeVideoChrome(
    val episodeTitle: String,
    val collectionTypeState: EditableSubjectCollectionTypeState,
    val isLoggedIn: Boolean,
    val onClickLogin: () -> Unit,
)

/**
 * 集数与集名, 例如「09  集名」; 集名为空时只有集数. 写法同全屏标题.
 */
internal fun appleEpisodeLabel(ep: String, title: String): String =
    if (title.isBlank()) ep else "$ep  $title"

private enum class AppleVideoMenuPage {
    Main,
    Collection,
    Share,
    AspectRatio,
    VideoEnhancement,
}

/**
 * 直式播放器顶栏: 距顶 6、左右 8; 返回 40、集名 `14/20 600` 白色单行、「⋯」40.
 *
 * 「⋯」打开 C15 菜单, 宽 232、列高 48. 先是设计稿的「追番狀態」「下載」「分享」, 之后是现有「更多」菜单的项目
 * (一起看、弹幕设定、快进、画面比例、画质增强、播放信息). 选有下一层的项目时, 菜单换成该层的单选项.
 * 菜单打开时「⋯」加 22% 白底.
 *
 * @param onMenuExpandedChange 菜单开关时调用, 播放器据此保持控制栏显示.
 */
@Composable
internal fun AppleEpisodeVideoTopBar(
    episodeTitle: String,
    onBack: () -> Unit,
    collectionType: UnifiedCollectionType,
    collectionTypeEnabled: Boolean,
    isLoggedIn: Boolean,
    onSetCollectionType: (UnifiedCollectionType) -> Unit,
    onClickLogin: () -> Unit,
    onClickCache: () -> Unit,
    shareData: MediaShareData,
    onClickWatchTogether: () -> Unit,
    onClickDanmakuSettings: () -> Unit,
    skipOpEdSeconds: Long,
    onClickSkipOpEd: () -> Unit,
    playerStatsVisible: Boolean,
    onTogglePlayerStats: () -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: VideoAspectRatioControllerState? = null,
    videoEnhancement: VideoEnhancementController? = null,
    onMenuExpandedChange: (Boolean) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf(AppleVideoMenuPage.Main) }
    var confirmUnfollow by remember { mutableStateOf(false) }
    val onMenuExpandedChangeUpdated by rememberUpdatedState(onMenuExpandedChange)
    LaunchedEffect(expanded) {
        onMenuExpandedChangeUpdated(expanded)
    }
    DisposableEffect(Unit) {
        onDispose { onMenuExpandedChangeUpdated(false) }
    }
    val dismiss = {
        expanded = false
        page = AppleVideoMenuPage.Main
    }

    Row(
        modifier
            .fillMaxWidth()
            .padding(start = 8.dp, top = 6.dp, end = 8.dp)
            .testTag(AppleEpisodeVideoTestTags.TopBar),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        VideoIconButton(onBack, AppleIcons.back(), null, Modifier.testTag(AppleEpisodeVideoTestTags.Back))
        Text(
            episodeTitle,
            Modifier.weight(1f).padding(horizontal = 4.dp),
            color = Color.White,
            style = AniTextStyles.subhead.copy(fontWeight = FontWeight.SemiBold),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Box {
            VideoIconButton(
                { expanded = true },
                Icons.Rounded.MoreHoriz,
                stringResource(Lang.subject_episode_more_options),
                Modifier.testTag(AppleEpisodeVideoTestTags.More),
                highlighted = expanded,
            )
            ApplePopoverMenu(
                expanded,
                onDismissRequest = dismiss,
                Modifier.testTag(AppleEpisodeVideoTestTags.Menu),
            ) {
                when (page) {
                    AppleVideoMenuPage.Main -> {
                        if (isLoggedIn) {
                            PopoverMenuItem(
                                stringResource(Lang.episode_menu_collection_state),
                                onClick = { page = AppleVideoMenuPage.Collection },
                                Modifier.testTag(AppleEpisodeVideoTestTags.CollectionState),
                                icon = AppleIcons.collection(),
                                value = stringResource(collectionType.collectionTypeName()),
                                enabled = collectionTypeEnabled,
                            )
                        } else {
                            PopoverMenuItem(
                                stringResource(Lang.subject_details_login_to_collect),
                                onClick = {
                                    dismiss()
                                    onClickLogin()
                                },
                                Modifier.testTag(AppleEpisodeVideoTestTags.CollectionState),
                                icon = AppleIcons.collection(),
                            )
                        }
                        PopoverMenuItem(
                            stringResource(Lang.subject_episode_cache),
                            onClick = {
                                dismiss()
                                onClickCache()
                            },
                            Modifier.testTag(AppleEpisodeVideoTestTags.Download),
                            icon = AppleIcons.download(),
                            showDivider = true,
                        )
                        PopoverMenuItem(
                            stringResource(Lang.subject_details_share),
                            onClick = { page = AppleVideoMenuPage.Share },
                            Modifier.testTag(AppleEpisodeVideoTestTags.Share),
                            icon = AppleIcons.share(),
                            showDivider = true,
                            enabled = shareData.download != null || shareData.websiteUrl != null,
                        )
                        PopoverMenuItem(
                            stringResource(Lang.watch_together_title),
                            onClick = {
                                dismiss()
                                onClickWatchTogether()
                            },
                            icon = Icons.Rounded.Groups,
                            showDivider = true,
                        )
                        PopoverMenuItem(
                            stringResource(Lang.subject_episode_danmaku_settings_title),
                            onClick = {
                                dismiss()
                                onClickDanmakuSettings()
                            },
                            Modifier.testTag(AppleEpisodeVideoTestTags.DanmakuSettings),
                            icon = AniIcons.SubtitleGear,
                            showDivider = true,
                        )
                        PopoverMenuItem(
                            stringResource(Lang.subject_episode_fast_forward_seconds, skipOpEdSeconds),
                            onClick = {
                                dismiss()
                                onClickSkipOpEd()
                            },
                            Modifier.testTag(AppleEpisodeVideoTestTags.SkipOpEd),
                            icon = when (skipOpEdSeconds) {
                                85L -> AniIcons.Forward85
                                90L -> AniIcons.Forward90
                                else -> AniIcons.Forward80
                            },
                            showDivider = true,
                        )
                        if (aspectRatio != null) {
                            PopoverMenuItem(
                                stringResource(Lang.video_player_aspect_ratio),
                                onClick = { page = AppleVideoMenuPage.AspectRatio },
                                icon = Icons.Rounded.AspectRatio,
                                value = renderAspectRatioMode(aspectRatio.currentMode),
                                showDivider = true,
                            )
                        }
                        if (videoEnhancement != null) {
                            val mode by videoEnhancement.mode.collectAsState()
                            PopoverMenuItem(
                                stringResource(Lang.video_player_video_enhancement),
                                onClick = { page = AppleVideoMenuPage.VideoEnhancement },
                                icon = Icons.Rounded.AutoAwesome,
                                value = renderVideoEnhancementMode(mode),
                                showDivider = true,
                            )
                        }
                        PopoverMenuItem(
                            stringResource(
                                if (playerStatsVisible) Lang.video_player_stats_title_hide else Lang.video_player_stats_title_show,
                            ),
                            onClick = {
                                dismiss()
                                onTogglePlayerStats()
                            },
                            icon = Icons.Outlined.Analytics,
                            showDivider = true,
                        )
                    }

                    AppleVideoMenuPage.Collection -> {
                        CollectionTypeMenuItems(
                            collectionType,
                            onDismissRequest = dismiss,
                            onSetType = onSetCollectionType,
                            itemModifier = { Modifier.testTag(AppleEpisodeVideoTestTags.collectionType(it)) },
                        )
                        if (collectionType != UnifiedCollectionType.NOT_COLLECTED) {
                            PopoverMenuItem(
                                stringResource(Lang.subject_collection_delete),
                                onClick = {
                                    dismiss()
                                    confirmUnfollow = true
                                },
                                Modifier.testTag(AppleEpisodeVideoTestTags.Unfollow),
                                icon = AppleIcons.minusCircle(),
                                showDivider = true,
                            )
                        }
                    }

                    AppleVideoMenuPage.Share -> ShareMenuItems(shareData, onDismissRequest = dismiss)

                    AppleVideoMenuPage.AspectRatio -> aspectRatio?.let { state ->
                        VideoAspectRatioControllerState.Entries.forEachIndexed { index, mode ->
                            PopoverMenuItem(
                                renderAspectRatioMode(mode),
                                onClick = {
                                    dismiss()
                                    state.setMode(mode)
                                },
                                showDivider = index > 0,
                                checked = mode == state.currentMode,
                            )
                        }
                    }

                    AppleVideoMenuPage.VideoEnhancement -> videoEnhancement?.let { controller ->
                        val current by controller.mode.collectAsState()
                        listOf(
                            VideoEnhancementMode.PERFORMANCE,
                            VideoEnhancementMode.QUALITY,
                            VideoEnhancementMode.OFF,
                        ).forEachIndexed { index, mode ->
                            PopoverMenuItem(
                                renderVideoEnhancementMode(mode),
                                onClick = {
                                    dismiss()
                                    controller.setMode(mode)
                                },
                                showDivider = index > 0,
                                checked = mode == current,
                            )
                        }
                    }
                }
            }
        }
    }

    if (confirmUnfollow) {
        UnfollowConfirmDialog(
            onConfirm = {
                confirmUnfollow = false
                onSetCollectionType(UnifiedCollectionType.NOT_COLLECTED)
            },
            onDismissRequest = { confirmUnfollow = false },
            confirmButtonModifier = Modifier.testTag(AppleEpisodeVideoTestTags.ConfirmUnfollow),
        )
    }
}

/**
 * 「分享」的下一层: 复制或打开播放链接与资料来源页面, 项目同现有的「外部链接」菜单.
 */
@Composable
private fun ShareMenuItems(
    data: MediaShareData,
    onDismissRequest: () -> Unit,
) {
    val clipboard = LocalClipboard.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberAsyncHandler()
    var index = 0
    data.download?.let { download ->
        val name = when (download) {
            is ResourceLocation.HttpStreamingFile -> stringResource(Lang.subject_episode_share_stream_link)
            is ResourceLocation.HttpTorrentFile -> stringResource(Lang.subject_episode_share_torrent_download_link)
            is ResourceLocation.LocalFile -> stringResource(Lang.subject_episode_share_local_file_link)
            is ResourceLocation.MagnetLink -> stringResource(Lang.subject_episode_share_magnet_link)
            is ResourceLocation.WebVideo -> stringResource(Lang.subject_episode_share_webpage_link)
        }
        PopoverMenuItem(
            stringResource(Lang.subject_episode_share_copy_link, name),
            onClick = {
                onDismissRequest()
                scope.launch { clipboard.setClipEntryText(download.uri) }
            },
            Modifier.testTag(AppleEpisodeVideoTestTags.ShareCopyLink),
            icon = AppleIcons.copy(),
            showDivider = index++ > 0,
        )
        PopoverMenuItem(
            stringResource(Lang.subject_episode_share_open_link, name),
            onClick = {
                onDismissRequest()
                uriHandler.openUri(download.uri)
            },
            icon = AppleIcons.externalLink(),
            showDivider = index++ > 0,
        )
    }
    data.websiteUrl?.let { websiteUrl ->
        PopoverMenuItem(
            stringResource(Lang.subject_episode_share_copy_source_page),
            onClick = {
                onDismissRequest()
                scope.launch { clipboard.setClipEntryText(websiteUrl) }
            },
            icon = AppleIcons.copy(),
            showDivider = index++ > 0,
        )
        PopoverMenuItem(
            stringResource(Lang.subject_episode_share_open_source_page),
            onClick = {
                onDismissRequest()
                uriHandler.openUri(websiteUrl)
            },
            icon = AppleIcons.externalLink(),
            showDivider = index++ > 0,
        )
    }
}

@Composable
private fun renderVideoEnhancementMode(mode: VideoEnhancementMode): String = when (mode) {
    VideoEnhancementMode.OFF -> stringResource(Lang.video_player_off)
    VideoEnhancementMode.PERFORMANCE -> stringResource(Lang.video_player_performance)
    VideoEnhancementMode.QUALITY -> stringResource(Lang.video_player_quality)
}

/**
 * 视频中央的播放钮: 56 圆, 底为 `rgba(10,14,18,0.45)`, 白色图标.
 */
@Composable
internal fun AppleVideoPlayButton(
    isPlaying: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(AppleEpisodeVideoDefaults.PlayButtonScrim)
            .clickable(role = Role.Button, onClick = onClick)
            .testTag(AppleEpisodeVideoTestTags.PlayButton),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            if (isPlaying) AppleIcons.pauseFilled() else AppleIcons.playFilled(),
            contentDescription = stringResource(if (isPlaying) Lang.video_player_pause else Lang.video_player_play),
            Modifier.size(24.dp),
            tint = Color.White,
        )
    }
}

/**
 * 直式播放器的控制列: 上方为进度条, 下方为时间 (`12/16` 等宽数字) 与右侧 36 按钮「彈」、全屏.
 *
 * 画中画在 iOS 由系统手势进入, 不放按钮 (Android 不使用 Apple 版控制列).
 *
 * @param controlsVisible 为 `false` 时只显示进度条, 时间与按钮保留位置但不显示, 用于拖动进度时.
 */
@Composable
internal fun AppleEpisodeVideoControls(
    progressSlider: @Composable () -> Unit,
    progressText: @Composable () -> Unit,
    danmakuEnabled: Boolean,
    onToggleDanmaku: () -> Unit,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
    controlsVisible: Boolean = true,
) {
    Column(modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        // 与 VideoScaffold 的上边距合计, 渐层约 80 高.
        Spacer(Modifier.height(12.dp))
        progressSlider()
        Row(
            Modifier
                .fillMaxWidth()
                .height(36.dp)
                .alpha(if (controlsVisible) 1f else 0f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ProvideTextStyle(AniTextStyles.caption.copy(color = Color.White, fontFeatureSettings = "tnum")) {
                progressText()
            }
            Spacer(Modifier.weight(1f))
            AppleDanmakuToggle(danmakuEnabled, onToggleDanmaku)
            VideoIconButton(
                onToggleFullscreen,
                if (isFullscreen) Icons.Rounded.FullscreenExit else Icons.Rounded.Fullscreen,
                stringResource(if (isFullscreen) Lang.video_player_exit_fullscreen else Lang.video_player_enter_fullscreen),
                Modifier.testTag(AppleEpisodeVideoTestTags.Fullscreen),
                size = 36.dp,
            )
        }
    }
}

/**
 * 弹幕开关「彈」: 22×22 圆角 6. 开启时底为品牌天蓝, 字 #0D141B `12 700`; 关闭时白色描边与白字.
 */
@Composable
private fun AppleDanmakuToggle(
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    val shape = RoundedCornerShape(6.dp)
    val description = stringResource(if (enabled) Lang.video_player_disable_danmaku else Lang.video_player_enable_danmaku)
    Box(
        Modifier
            .size(36.dp)
            .toggleable(value = enabled, role = Role.Switch, onValueChange = { onToggle() })
            .semantics { contentDescription = description }
            .testTag(AppleEpisodeVideoTestTags.DanmakuToggle),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(shape)
                .then(
                    if (enabled) {
                        Modifier.background(AniColors.brandRing)
                    } else {
                        Modifier.border(1.5.dp, Color.White, shape)
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                stringResource(Lang.video_player_danmaku_toggle_label),
                color = if (enabled) AppleEpisodeVideoDefaults.DanmakuToggleOnText else Color.White,
                style = AniTextStyles.caption.copy(fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
        }
    }
}

/**
 * 叠在视频上的无底图标按钮, 白色图标. [highlighted] 时加 22% 白底.
 */
@Composable
private fun VideoIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    highlighted: Boolean = false,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .then(if (highlighted) Modifier.background(Color.White.copy(alpha = 0.22f)) else Modifier)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, Modifier.size(24.dp), tint = Color.White)
    }
}

/**
 * 把 [AppleEpisodeVideoTopBar] 接到播放器: 返回走返回键, 菜单打开期间保持控制栏显示, 追番状态读写 [AppleEpisodeVideoChrome.collectionTypeState].
 */
@Composable
internal fun AppleEpisodeVideoTopBarHost(
    chrome: AppleEpisodeVideoChrome,
    playerState: MediampPlayer,
    playerControllerState: PlayerControllerState,
    sheetsController: VideoSideSheetsController<EpisodeVideoSideSheetPage>,
    shareData: MediaShareData,
    onClickCache: () -> Unit,
    onClickWatchTogether: () -> Unit,
    opEdSkipDuration: Duration,
    onClickSkipOpEd: (currentPositionMillis: Long) -> Unit,
    playerStatsVisible: Boolean,
    onTogglePlayerStats: () -> Unit,
    aspectRatio: VideoAspectRatioControllerState?,
    videoEnhancement: VideoEnhancementController?,
) {
    val back = LocalBackDispatcher.current
    val toaster = LocalToaster.current
    val scope = rememberCoroutineScope()
    val presentation by chrome.collectionTypeState.presentationFlow.collectAsStateWithLifecycle()
    val menuAlwaysOnRequester = rememberAlwaysOnRequester(playerControllerState, "appleTopBarMenu")
    AppleEpisodeVideoTopBar(
        chrome.episodeTitle,
        onBack = { back.onBackPressed() },
        collectionType = presentation.selfCollectionType,
        collectionTypeEnabled = !presentation.isSetSelfCollectionTypeWorking && !presentation.isPlaceholder,
        isLoggedIn = chrome.isLoggedIn,
        onSetCollectionType = { type ->
            scope.launch {
                chrome.collectionTypeState.setSelfCollectionType(type)?.let { toaster.showLoadError(it) }
            }
        },
        onClickLogin = chrome.onClickLogin,
        onClickCache = onClickCache,
        shareData = shareData,
        onClickWatchTogether = onClickWatchTogether,
        onClickDanmakuSettings = { sheetsController.navigateTo(EpisodeVideoSideSheetPage.PLAYER_SETTINGS) },
        skipOpEdSeconds = opEdSkipDuration.inWholeSeconds,
        onClickSkipOpEd = { onClickSkipOpEd(playerState.currentPositionMillis.value) },
        playerStatsVisible = playerStatsVisible,
        onTogglePlayerStats = onTogglePlayerStats,
        aspectRatio = aspectRatio,
        videoEnhancement = videoEnhancement,
        onMenuExpandedChange = { expanded ->
            if (expanded) menuAlwaysOnRequester.request() else menuAlwaysOnRequester.cancelRequest()
        },
    )
}

/**
 * 中央播放钮随控制栏显示; 视频加载或缓冲时让给加载提示.
 */
@Composable
internal fun AppleVideoPlayButtonHost(
    playerState: MediampPlayer,
    playerControllerState: PlayerControllerState,
    videoLoadingStateFlow: Flow<VideoLoadingState>,
) {
    val state by playerState.state.collectAsStateWithLifecycle()
    val videoLoadingState by videoLoadingStateFlow.collectAsStateWithLifecycle(VideoLoadingState.Initial)
    val loading = shouldShowVideoLoadingIndicator(
        videoLoadingState,
        buffering = state.isBuffering,
        playerError = state.mediaStatus is MediaStatus.Error,
    )
    AniAnimatedVisibility(
        playerControllerState.visibility.bottomBar && !loading,
        enter = LocalAniMotionScheme.current.animatedVisibility.standardEnter,
        exit = LocalAniMotionScheme.current.animatedVisibility.standardExit,
    ) {
        AppleVideoPlayButton(
            isPlaying = state.playWhenReady,
            onClick = { playerState.togglePlayWhenReady() },
        )
    }
}

/**
 * C9 播放器进度条的配色: 轨道白 28%、缓冲白 45%、已播放品牌天蓝、白色拖动点. 拖动、预览与 BT 分块状态沿用现有进度条.
 */
@Composable
internal fun AppleVideoProgressSlider(
    progressSliderState: PlayerProgressSliderState,
    cacheProgressInfoFlow: Flow<MediaCacheProgressInfo>,
    framePreview: MediaProgressFramePreviewState?,
    touchSeekState: TouchSeekState?,
) {
    val cacheProgressInfo by cacheProgressInfoFlow.collectAsStateWithLifecycle(null)
    MediaProgressSlider(
        progressSliderState,
        { cacheProgressInfo },
        colors = MediaProgressSliderDefaults.colors(
            trackBackgroundColor = Color.White.copy(alpha = 0.28f),
            trackProgressColor = AniColors.brandRing,
            thumbColor = Color.White,
            cachedProgressColor = Color.White.copy(alpha = 0.45f),
        ),
        showPreviewTimeTextOnThumb = false,
        framePreview = framePreview,
        showFramePreviewInPopup = false,
        touchSeekState = touchSeekState,
    )
}

internal object AppleEpisodeVideoDefaults {
    /** 播放钮底色 `rgba(10,14,18,0.45)`. */
    val PlayButtonScrim = Color(0xFF0A0E12).copy(alpha = 0.45f)

    /** 弹幕开关开启时的文字颜色. */
    val DanmakuToggleOnText = Color(0xFF0D141B)
}

internal object AppleEpisodeVideoTestTags {
    const val TopBar = "apple-episode-video-top-bar"
    const val Back = "apple-episode-video-back"
    const val More = "apple-episode-video-more"
    const val Menu = "apple-episode-video-menu"
    const val CollectionState = "apple-episode-video-collection-state"
    const val Download = "apple-episode-video-download"
    const val Share = "apple-episode-video-share"
    const val ShareCopyLink = "apple-episode-video-share-copy-link"
    const val DanmakuSettings = "apple-episode-video-danmaku-settings"
    const val SkipOpEd = "apple-episode-video-skip-op-ed"
    const val Unfollow = "apple-episode-video-unfollow"
    const val ConfirmUnfollow = "apple-episode-video-confirm-unfollow"
    const val PlayButton = "apple-episode-video-play"
    const val DanmakuToggle = "apple-episode-video-danmaku-toggle"
    const val Fullscreen = "apple-episode-video-fullscreen"
    fun collectionType(type: UnifiedCollectionType) = "apple-episode-video-collection-${type.name}"
}
