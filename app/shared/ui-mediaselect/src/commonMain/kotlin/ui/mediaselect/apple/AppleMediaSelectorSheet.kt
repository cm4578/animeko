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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.ani.app.domain.media.selector.SelectTrigger
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleModalBottomSheet
import me.him188.ani.app.ui.foundation.apple.ApplePopoverMenu
import me.him188.ani.app.ui.foundation.apple.PopoverMenuItem
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.media_selector_mode_menu
import me.him188.ani.app.ui.lang.media_selector_watching_prefix
import me.him188.ani.app.ui.lang.subject_episode_select_media_source
import me.him188.ani.app.ui.mediafetch.MediaSelectorState
import me.him188.ani.app.ui.mediafetch.MediaSourceResultListPresentation
import me.him188.ani.app.ui.mediaselect.MediaSelectorMode
import me.him188.ani.app.ui.mediaselect.WatchingEpisode
import me.him188.ani.app.ui.mediaselect.bt.BtResourcesPage
import me.him188.ani.app.ui.mediaselect.common.displayName
import me.him188.ani.app.ui.mediaselect.common.episodeLabel
import me.him188.ani.app.ui.mediaselect.manual.ManualBrowseState
import me.him188.ani.datasources.api.source.MediaFetchRequest
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版选择资料来源 (03 §3.8): C14 底部面板, 顶边距屏顶约 132.
 *
 * 顶部是标题、「正在觀看 第 N 話」与模式切换钮; 下方是自动匹配或手动查找.
 * BT 资源沿用现有页面: 只有会话里有 BT 资料源时才出现, iOS 不创建 BT 资料源.
 * 选中资源播放后关闭面板.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleMediaSelectorSheet(
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
) {
    AppleModalBottomSheet(
        onDismissRequest,
        Modifier.padding(top = AppleMediaSelectorDefaults.SheetTopMargin),
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        // 面板顶端离屏幕顶端 132, 碰不到状态栏.
        contentWindowInsets = { BottomSheetDefaults.windowInsets.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom) },
    ) {
        Column(Modifier.fillMaxWidth().fillMaxHeight()) {
            AppleMediaSelectorHeader(
                mode,
                onModeChange,
                showBt = sourceResults.btSources.isNotEmpty(),
                watching,
            )
            Spacer(Modifier.height(12.dp))
            val pageModifier = Modifier.weight(1f).fillMaxWidth()
            when (mode) {
                MediaSelectorMode.AUTO -> AppleAutoMatchPage(
                    mediaSelectorState,
                    onClickItem = {
                        mediaSelectorState.select(it, SelectTrigger.MANUAL_AUTO_PAGE)
                        onDismissRequest()
                    },
                    onRestartSource = onRestartSource,
                    onRequestManualSearch = { onModeChange(MediaSelectorMode.MANUAL) },
                    pageModifier,
                )

                MediaSelectorMode.MANUAL -> AppleManualBrowsePage(
                    manualBrowseState,
                    onPlayed = onDismissRequest,
                    pageModifier,
                )

                MediaSelectorMode.BT -> BtResourcesPage(
                    mediaSelectorState,
                    sourceResults,
                    watching,
                    fetchRequest,
                    onFetchRequestChange,
                    onClickItem = {
                        mediaSelectorState.select(it, SelectTrigger.MANUAL_BT_PAGE)
                        onDismissRequest()
                    },
                    onRestartSource = onRestartSource,
                    pageModifier,
                    showWatchingCard = false,
                )
            }
        }
    }
}

/**
 * 面板顶部: 标题「選擇資料來源」`18/24 700`、副标「正在觀看 第 N 話」`13/18` label2;
 * 右侧模式切换钮 (高 36、圆角 18、fill 底、「{模式} ⌄」`14/20 600`) 打开 C15 菜单, 当前模式有勾号.
 *
 * @param showBt 会话里有 BT 资料源时菜单才有「BT 資源」.
 */
@Composable
internal fun AppleMediaSelectorHeader(
    mode: MediaSelectorMode,
    onModeChange: (MediaSelectorMode) -> Unit,
    showBt: Boolean,
    watching: WatchingEpisode?,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val modes = listOfNotNull(
        MediaSelectorMode.AUTO,
        MediaSelectorMode.MANUAL,
        MediaSelectorMode.BT.takeIf { showBt },
    )
    Row(
        modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                stringResource(Lang.subject_episode_select_media_source),
                style = AniTextStyles.title.copy(fontSize = 18.sp, lineHeight = 24.sp),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            if (watching != null) {
                Text(
                    "${stringResource(Lang.media_selector_watching_prefix)} ${watching.episodeLabel()}",
                    Modifier.testTag(AppleMediaSelectorTestTags.Watching),
                    style = AniTextStyles.footnote,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Box {
            Row(
                Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                    .clickable(role = Role.DropdownList) { expanded = true }
                    .padding(horizontal = 14.dp)
                    .testTag(AppleMediaSelectorTestTags.ModeButton),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    mode.displayName(),
                    style = AniTextStyles.subhead.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Icon(
                    AppleIcons.chevronDown(),
                    contentDescription = stringResource(Lang.media_selector_mode_menu),
                    Modifier.size(12.dp),
                    tint = MaterialTheme.colorScheme.secondary,
                )
            }
            ApplePopoverMenu(
                expanded,
                onDismissRequest = { expanded = false },
                Modifier.testTag(AppleMediaSelectorTestTags.ModeMenu),
            ) {
                modes.forEachIndexed { index, item ->
                    PopoverMenuItem(
                        item.displayName(),
                        onClick = {
                            expanded = false
                            if (item != mode) onModeChange(item)
                        },
                        Modifier.testTag(AppleMediaSelectorTestTags.mode(item)),
                        showDivider = index > 0,
                        checked = item == mode,
                    )
                }
            }
        }
    }
}

object AppleMediaSelectorDefaults {
    /** 面板顶边与屏幕顶端的距离. */
    val SheetTopMargin = 132.dp
}

internal object AppleMediaSelectorTestTags {
    const val ModeButton = "apple-media-selector-mode"
    const val ModeMenu = "apple-media-selector-mode-menu"
    const val Watching = "apple-media-selector-watching"
    const val RescueButton = "apple-media-selector-rescue"
    const val SourceButton = "apple-media-selector-source-button"
    const val SourceMenu = "apple-media-selector-source-menu"
    const val SearchField = "apple-media-selector-search"
    const val OpenedResult = "apple-media-selector-opened-result"
    const val ManualRetry = "apple-media-selector-manual-retry"
    const val RememberSwitch = "apple-media-selector-remember"
    fun mode(mode: MediaSelectorMode) = "apple-media-selector-mode-${mode.name}"
    fun source(instanceId: String) = "apple-media-selector-source-$instanceId"
    fun failedSource(instanceId: String) = "apple-media-selector-failed-$instanceId"
    fun retry(instanceId: String) = "apple-media-selector-retry-$instanceId"
    fun channel(index: Int) = "apple-media-selector-channel-$index"
    fun sourceMenuItem(index: Int) = "apple-media-selector-source-menu-$index"
    fun result(index: Int) = "apple-media-selector-result-$index"
    fun channelPill(index: Int) = "apple-media-selector-channel-pill-$index"
    fun episode(index: Int) = "apple-media-selector-episode-$index"
}
