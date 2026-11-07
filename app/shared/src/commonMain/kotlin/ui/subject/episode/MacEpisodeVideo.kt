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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsStartWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.episode.share.MediaShareData
import me.him188.ani.app.ui.foundation.animation.AniAnimatedVisibility
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.ApplePopoverMenu
import me.him188.ani.app.ui.foundation.apple.CollectionTypeMenuItems
import me.him188.ani.app.ui.foundation.apple.PopoverMenuItem
import me.him188.ani.app.ui.foundation.apple.UnfollowConfirmDialog
import me.him188.ani.app.ui.foundation.apple.collectionTypeName
import me.him188.ani.app.ui.foundation.interaction.WindowDragArea
import me.him188.ani.app.ui.foundation.layout.desktopCaptionButton
import me.him188.ani.app.ui.foundation.navigation.LocalBackDispatcher
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.showLoadError
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.episode_mac_shortcut_hint
import me.him188.ani.app.ui.lang.episode_menu_collection_state
import me.him188.ani.app.ui.lang.main_toolbar_back
import me.him188.ani.app.ui.lang.subject_collection_delete
import me.him188.ani.app.ui.lang.subject_details_login_to_collect
import me.him188.ani.app.ui.lang.subject_details_share
import me.him188.ani.app.ui.lang.subject_episode_cache
import me.him188.ani.app.ui.lang.subject_episode_collapse_sidebar
import me.him188.ani.app.ui.lang.subject_episode_expand_sidebar
import me.him188.ani.app.ui.lang.subject_episode_more_options
import me.him188.ani.app.ui.lang.video_player_enter_fullscreen
import me.him188.ani.app.ui.lang.video_player_mute
import me.him188.ani.app.ui.lang.video_player_next_episode
import me.him188.ani.app.ui.lang.video_player_pause
import me.him188.ani.app.ui.lang.video_player_play
import me.him188.ani.app.ui.lang.video_player_volume
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeState
import me.him188.ani.app.videoplayer.ui.PlayerControllerState
import me.him188.ani.app.videoplayer.ui.rememberAlwaysOnRequester
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 播放页视窗模式的影片区 (04 §3.8) 在现有参数之外需要的内容. 只在非全屏时套用; 全屏沿用现有控制列,
 * 字幕与音轨、截图、置顶、画面比例、画质增强、跳过片头、一起看、弹幕设定只在全屏提供 (05 G35).
 *
 * @param title 顶端列的标题, 见 [macEpisodeVideoTitle].
 * @param collectionTypeState 「⋯」菜单中的追番状态, 与现有的收藏按钮共用.
 * @param isLoggedIn 未登录时「追番状态」一列换成「登入後可收藏」.
 */
@Stable
internal class MacEpisodeVideoChrome(
    val title: AnnotatedString,
    val collectionTypeState: EditableSubjectCollectionTypeState,
    val isLoggedIn: Boolean,
    val onClickLogin: () -> Unit,
)

/**
 * 顶端列标题「番名 · 第 N 話 · 集名」: 番名 W600, 集名为空时省略.
 */
internal fun macEpisodeVideoTitle(subjectTitle: String, episodeLabel: String, episodeTitle: String): AnnotatedString =
    buildAnnotatedString {
        if (subjectTitle.isNotBlank()) {
            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(subjectTitle) }
            append(" · ")
        }
        append(episodeLabel)
        if (episodeTitle.isNotBlank()) {
            append(" · ")
            append(episodeTitle)
        }
    }

/**
 * 「第 N 話」里的集数: 纯数字去掉补位的 0 (「09」→「9」), 其他 (例如 SP1) 用原文.
 */
internal fun macEpisodeNumber(ep: String): String =
    if (ep.isNotEmpty() && ep.all { it.isDigit() }) ep.trimStart('0').ifEmpty { "0" } else ep

/**
 * 控制列弹幕输入框的配色: 底 `rgba(255,255,255,0.14)`, 无描边, 白字, 提示文字白 55%.
 */
@Composable
internal fun macDanmakuTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = MacEpisodeVideoDefaults.DanmakuInputBackground,
    focusedContainerColor = MacEpisodeVideoDefaults.DanmakuInputBackground,
    unfocusedBorderColor = Color.Transparent,
    focusedBorderColor = Color.Transparent,
    unfocusedTextColor = Color.White,
    focusedTextColor = Color.White,
    cursorColor = Color.White,
    unfocusedPlaceholderColor = Color.White.copy(alpha = 0.55f),
    focusedPlaceholderColor = Color.White.copy(alpha = 0.55f),
    unfocusedLeadingIconColor = Color.White,
    focusedLeadingIconColor = Color.White,
    unfocusedTrailingIconColor = Color.White,
    focusedTrailingIconColor = Color.White,
)

private enum class MacVideoMenuPage {
    Main,
    Collection,
    Share,
}

/**
 * 顶端列: 高 52, 右内距 12; 交通灯 (让出系统按钮的宽度)、返回 28×28、标题 13 白色单行、「⋯」32×28、右侧面板开关 32×28.
 * 空白处可以拖动窗口.
 *
 * 「⋯」打开 C15 菜单, 只有设计稿的三项: 追番状态 ›、分隔线、下载 (打开单番快取页, 05 G36)、分享 ›.
 * 选有下一层的项目时, 菜单换成该层的选项. 菜单打开时「⋯」加 16% 白底.
 *
 * @param onMenuExpandedChange 菜单开关时调用, 播放器据此保持控制栏显示.
 */
@Composable
internal fun MacEpisodeVideoTopBar(
    title: AnnotatedString,
    onBack: () -> Unit,
    collectionType: UnifiedCollectionType,
    collectionTypeEnabled: Boolean,
    isLoggedIn: Boolean,
    onSetCollectionType: (UnifiedCollectionType) -> Unit,
    onClickLogin: () -> Unit,
    onClickCache: () -> Unit,
    shareData: MediaShareData,
    sidebarVisible: Boolean,
    onToggleSidebar: () -> Unit,
    modifier: Modifier = Modifier,
    onMenuExpandedChange: (Boolean) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    var page by remember { mutableStateOf(MacVideoMenuPage.Main) }
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
        page = MacVideoMenuPage.Main
    }

    WindowDragArea(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(MacEpisodeVideoDefaults.TopBarHeight)
                .padding(end = 12.dp)
                .testTag(MacEpisodeVideoTestTags.TopBar),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // 交通灯在这里
            Spacer(Modifier.windowInsetsStartWidth(WindowInsets.desktopCaptionButton))
            MacVideoIconButton(
                onBack,
                AppleIcons.back(),
                stringResource(Lang.main_toolbar_back),
                Modifier.testTag(MacEpisodeVideoTestTags.Back),
                width = 28.dp,
            )
            Text(
                title,
                Modifier.weight(1f).padding(horizontal = 8.dp),
                color = Color.White,
                style = AniTextStyles.macCallout,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Box {
                MacVideoIconButton(
                    { expanded = true },
                    Icons.Rounded.MoreHoriz,
                    stringResource(Lang.subject_episode_more_options),
                    Modifier.testTag(MacEpisodeVideoTestTags.More),
                    highlighted = expanded,
                )
                ApplePopoverMenu(
                    expanded,
                    onDismissRequest = dismiss,
                    Modifier.testTag(MacEpisodeVideoTestTags.Menu),
                ) {
                    when (page) {
                        MacVideoMenuPage.Main -> {
                            if (isLoggedIn) {
                                PopoverMenuItem(
                                    stringResource(Lang.episode_menu_collection_state),
                                    onClick = { page = MacVideoMenuPage.Collection },
                                    Modifier.testTag(MacEpisodeVideoTestTags.CollectionState),
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
                                    Modifier.testTag(MacEpisodeVideoTestTags.CollectionState),
                                    icon = AppleIcons.collection(),
                                )
                            }
                            PopoverMenuItem(
                                stringResource(Lang.subject_episode_cache),
                                onClick = {
                                    dismiss()
                                    onClickCache()
                                },
                                Modifier.testTag(MacEpisodeVideoTestTags.Download),
                                icon = AppleIcons.download(),
                                showDivider = true,
                            )
                            PopoverMenuItem(
                                stringResource(Lang.subject_details_share),
                                onClick = { page = MacVideoMenuPage.Share },
                                Modifier.testTag(MacEpisodeVideoTestTags.Share),
                                icon = AppleIcons.share(),
                                enabled = shareData.download != null || shareData.websiteUrl != null,
                            )
                        }

                        MacVideoMenuPage.Collection -> {
                            CollectionTypeMenuItems(
                                collectionType,
                                onDismissRequest = dismiss,
                                onSetType = onSetCollectionType,
                                itemModifier = { Modifier.testTag(MacEpisodeVideoTestTags.collectionType(it)) },
                            )
                            if (collectionType != UnifiedCollectionType.NOT_COLLECTED) {
                                PopoverMenuItem(
                                    stringResource(Lang.subject_collection_delete),
                                    onClick = {
                                        dismiss()
                                        confirmUnfollow = true
                                    },
                                    icon = AppleIcons.minusCircle(),
                                    showDivider = true,
                                )
                            }
                        }

                        MacVideoMenuPage.Share -> ShareMenuItems(shareData, onDismissRequest = dismiss)
                    }
                }
            }
            MacVideoIconButton(
                onToggleSidebar,
                AppleIcons.inspector(),
                stringResource(if (sidebarVisible) Lang.subject_episode_collapse_sidebar else Lang.subject_episode_expand_sidebar),
                Modifier.testTag(MacEpisodeVideoTestTags.SidebarToggle),
                highlighted = sidebarVisible,
            )
        }
    }

    if (confirmUnfollow) {
        UnfollowConfirmDialog(
            onConfirm = {
                confirmUnfollow = false
                onSetCollectionType(UnifiedCollectionType.NOT_COLLECTED)
            },
            onDismissRequest = { confirmUnfollow = false },
        )
    }
}

/**
 * 把 [MacEpisodeVideoTopBar] 接到播放器: 返回走返回键, 菜单打开期间保持控制栏显示, 追番状态读写 [MacEpisodeVideoChrome.collectionTypeState].
 */
@Composable
internal fun MacEpisodeVideoTopBarHost(
    chrome: MacEpisodeVideoChrome,
    playerControllerState: PlayerControllerState,
    shareData: MediaShareData,
    onClickCache: () -> Unit,
    sidebarVisible: Boolean,
    onToggleSidebar: () -> Unit,
) {
    val back = LocalBackDispatcher.current
    val toaster = LocalToaster.current
    val scope = rememberCoroutineScope()
    val presentation by chrome.collectionTypeState.presentationFlow.collectAsStateWithLifecycle()
    val menuAlwaysOnRequester = rememberAlwaysOnRequester(playerControllerState, "macTopBarMenu")
    MacEpisodeVideoTopBar(
        chrome.title,
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
        sidebarVisible = sidebarVisible,
        onToggleSidebar = onToggleSidebar,
        onMenuExpandedChange = { expanded ->
            if (expanded) menuAlwaysOnRequester.request() else menuAlwaysOnRequester.cancelRequest()
        },
    )
}

/**
 * 音量: 静音钮与 72 宽的音量条. 为 `null` 时不显示 (播放器没有音量控制).
 */
@Stable
internal class MacVolumeControl(
    val level: Float,
    val maxLevel: Float,
    val isMute: Boolean,
    val onSetLevel: (Float) -> Unit,
    val onToggleMute: () -> Unit,
)

/**
 * 视窗模式的控制列: 上方为 4 粗的进度条 (C9 配色); 下方一列高 36, 左右 20、底 14:
 * 左侧播放 (32)、下一集、静音与 72 宽音量条、时间 12 等宽; 中间弹幕输入 (最宽 280); 右侧倍速、「彈」、全屏.
 * 画中画钮不放 (macOS 没有画中画).
 *
 * @param controlsVisible 为 `false` 时只显示进度条, 其余保留位置但不显示, 用于拖动进度时.
 */
@Composable
internal fun MacEpisodeVideoControls(
    progressSlider: @Composable () -> Unit,
    progressText: @Composable () -> Unit,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    hasNextEpisode: Boolean,
    onClickNextEpisode: () -> Unit,
    volume: MacVolumeControl?,
    danmakuEditor: @Composable (Modifier) -> Unit,
    speedSwitcher: @Composable () -> Unit,
    danmakuEnabled: Boolean,
    onToggleDanmaku: () -> Unit,
    onEnterFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
    controlsVisible: Boolean = true,
) {
    val playHovered = remember { MutableInteractionSource() }
    val isPlayHovered by playHovered.collectIsHoveredAsState()
    val hintVisible = rememberMacShortcutHintVisible(isPlayHovered)
    Box(modifier.fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 14.dp)) {
            progressSlider()
            MacControlsRow(
                start = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MacVideoIconButton(
                            onTogglePlay,
                            if (isPlaying) AppleIcons.pauseFilled() else AppleIcons.playFilled(),
                            stringResource(if (isPlaying) Lang.video_player_pause else Lang.video_player_play),
                            Modifier.hoverable(playHovered).testTag(MacEpisodeVideoTestTags.Play),
                            width = 32.dp,
                            height = 32.dp,
                        )
                        if (hasNextEpisode) {
                            MacVideoIconButton(
                                onClickNextEpisode,
                                Icons.Rounded.SkipNext,
                                stringResource(Lang.video_player_next_episode),
                                Modifier.testTag(MacEpisodeVideoTestTags.NextEpisode),
                            )
                        }
                        if (volume != null) {
                            MacVideoIconButton(
                                volume.onToggleMute,
                                if (volume.isMute) Icons.AutoMirrored.Rounded.VolumeOff else Icons.AutoMirrored.Rounded.VolumeUp,
                                stringResource(Lang.video_player_mute),
                                Modifier.testTag(MacEpisodeVideoTestTags.Mute),
                            )
                            MacVolumeBar(
                                if (volume.isMute) 0f else volume.level,
                                volume.maxLevel,
                                volume.onSetLevel,
                                Modifier.width(72.dp).testTag(MacEpisodeVideoTestTags.Volume),
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        ProvideTextStyle(AniTextStyles.macCaption.copy(color = Color.White, fontFeatureSettings = "tnum")) {
                            progressText()
                        }
                    }
                },
                center = { danmakuEditor(Modifier) },
                end = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        speedSwitcher()
                        AppleDanmakuToggle(danmakuEnabled, onToggleDanmaku)
                        MacVideoIconButton(
                            onEnterFullscreen,
                            Icons.Rounded.Fullscreen,
                            stringResource(Lang.video_player_enter_fullscreen),
                            Modifier.testTag(MacEpisodeVideoTestTags.Fullscreen),
                        )
                    }
                },
                Modifier.fillMaxWidth().height(36.dp).alpha(if (controlsVisible) 1f else 0f),
            )
        }
        AniAnimatedVisibility(
            hintVisible && controlsVisible,
            Modifier.align(Alignment.TopStart).offset(x = 20.dp, y = (-30).dp),
        ) {
            MacShortcutHint()
        }
    }
}

/**
 * 控制列的版式: [start] 靠左、[end] 靠右, [center] (弹幕输入) 在两者之间的空间里居中, 最宽 280.
 * 空间不够时 [center] 变窄, 不挤压两侧.
 */
@Composable
private fun MacControlsRow(
    start: @Composable () -> Unit,
    center: @Composable () -> Unit,
    end: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(
        contents = listOf(start, center, end),
        modifier = modifier,
    ) { (startMeasurables, centerMeasurables, endMeasurables), constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val loose = constraints.copy(minWidth = 0, minHeight = 0)
        val startPlaceables = startMeasurables.map { it.measure(loose) }
        val endPlaceables = endMeasurables.map { it.measure(loose) }
        val startWidth = startPlaceables.maxOfOrNull { it.width } ?: 0
        val endWidth = endPlaceables.maxOfOrNull { it.width } ?: 0
        val gap = 16.dp.roundToPx()
        val available = (width - startWidth - endWidth - gap * 2).coerceAtLeast(0)
        val centerWidth = minOf(available, MacEpisodeVideoDefaults.DanmakuInputMaxWidth.roundToPx())
        val centerPlaceables = centerMeasurables.map {
            it.measure(Constraints(minWidth = centerWidth, maxWidth = centerWidth, minHeight = 0, maxHeight = height))
        }
        layout(width, height) {
            startPlaceables.forEach { it.placeRelative(0, (height - it.height) / 2) }
            endPlaceables.forEach { it.placeRelative(width - it.width, (height - it.height) / 2) }
            val middleStart = startWidth + gap
            val middleWidth = width - startWidth - endWidth - gap * 2
            val centerX = middleStart + ((middleWidth - centerWidth) / 2).coerceAtLeast(0)
            centerPlaceables.forEach { it.placeRelative(centerX, (height - it.height) / 2) }
        }
    }
}

/**
 * 72 宽的音量条: 轨道白 28%、音量白色、10 白色圆点. 点击或拖动设定音量.
 */
@Composable
private fun MacVolumeBar(
    level: Float,
    maxLevel: Float,
    onLevelChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    val fraction = if (maxLevel > 0f) (level / maxLevel).coerceIn(0f, 1f) else 0f
    val onLevelChangeUpdated by rememberUpdatedState(onLevelChange)
    val volumeText = stringResource(Lang.video_player_volume)
    BoxWithConstraints(
        modifier
            .height(20.dp)
            .pointerInput(maxLevel) {
                detectTapGestures { offset ->
                    onLevelChangeUpdated((offset.x / size.width).coerceIn(0f, 1f) * maxLevel)
                }
            }
            .pointerInput(maxLevel) {
                detectHorizontalDragGestures { change, _ ->
                    onLevelChangeUpdated((change.position.x / size.width).coerceIn(0f, 1f) * maxLevel)
                }
            }
            .semantics {
                contentDescription = volumeText
                progressBarRangeInfo = ProgressBarRangeInfo(level, 0f..maxLevel)
                setProgress { target ->
                    onLevelChangeUpdated(target.coerceIn(0f, maxLevel))
                    true
                }
            },
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(Modifier.fillMaxWidth().height(4.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.28f)))
        Box(Modifier.fillMaxWidth(fraction).height(4.dp).clip(CircleShape).background(Color.White))
        Box(
            Modifier
                .offset(x = (maxWidth - 10.dp) * fraction)
                .size(10.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
    }
}

/**
 * 叠在视频上的按钮: 圆角 6, 白色图标 18. [highlighted] 时加 16% 白底.
 */
@Composable
private fun MacVideoIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    width: Dp = 32.dp,
    height: Dp = 28.dp,
    highlighted: Boolean = false,
) {
    Box(
        modifier
            .size(width, height)
            .clip(RoundedCornerShape(6.dp))
            .then(if (highlighted) Modifier.background(Color.White.copy(alpha = 0.16f)) else Modifier)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, Modifier.size(18.dp), tint = Color.White)
    }
}

/**
 * 快捷键提示气泡「播放／暫停：空白鍵 · 快轉：← →」, 11 号.
 */
@Composable
private fun MacShortcutHint(modifier: Modifier = Modifier) {
    Text(
        stringResource(Lang.episode_mac_shortcut_hint),
        modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.62f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag(MacEpisodeVideoTestTags.ShortcutHint),
        color = Color.White,
        style = AniTextStyles.macCaption.copy(fontSize = 11.sp, lineHeight = 14.sp),
        maxLines = 1,
    )
}

/**
 * 快捷键提示只在首次 (本次启动第一次打开播放页, 显示几秒) 与悬停在播放钮上时出现.
 */
@Composable
private fun rememberMacShortcutHintVisible(hovered: Boolean): Boolean {
    var firstTime by remember { mutableStateOf(!MacShortcutHintSession.shown) }
    LaunchedEffect(Unit) {
        if (firstTime) {
            MacShortcutHintSession.shown = true
            delay(MacEpisodeVideoDefaults.ShortcutHintDuration)
            firstTime = false
        }
    }
    return firstTime || hovered
}

/** 本次启动是否已经自动显示过快捷键提示. */
private object MacShortcutHintSession {
    var shown = false
}

internal object MacEpisodeVideoDefaults {
    val TopBarHeight = 52.dp
    val DanmakuInputMaxWidth = 280.dp
    val DanmakuInputHeight = 32.dp

    /** 弹幕输入框底色 `rgba(255,255,255,0.14)`. */
    val DanmakuInputBackground = Color.White.copy(alpha = 0.14f)

    /** 首次自动显示快捷键提示的时长 (毫秒). */
    const val ShortcutHintDuration = 4000L
}

internal object MacEpisodeVideoTestTags {
    const val TopBar = "mac-episode-video-top-bar"
    const val Back = "mac-episode-video-back"
    const val More = "mac-episode-video-more"
    const val Menu = "mac-episode-video-menu"
    const val CollectionState = "mac-episode-video-collection-state"
    const val Download = "mac-episode-video-download"
    const val Share = "mac-episode-video-share"
    const val SidebarToggle = "mac-episode-video-sidebar-toggle"
    const val Play = "mac-episode-video-play"
    const val NextEpisode = "mac-episode-video-next"
    const val Mute = "mac-episode-video-mute"
    const val Volume = "mac-episode-video-volume"
    const val Fullscreen = "mac-episode-video-fullscreen"
    const val ShortcutHint = "mac-episode-video-shortcut-hint"
    fun collectionType(type: UnifiedCollectionType) = "mac-episode-video-collection-${type.name}"
}
