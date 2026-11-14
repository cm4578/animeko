/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.trends

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.preference.NsfwMode
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.ui.exploration.follow.QuickFollowAnchor
import me.him188.ani.app.ui.exploration.follow.QuickFollowState
import me.him188.ani.app.ui.exploration.search.LoadingIndicator
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleContextMenu
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.MacKeyboardShortcut
import me.him188.ani.app.ui.foundation.apple.MacLargeTitleHeader
import me.him188.ani.app.ui.foundation.apple.MacPageToolbar
import me.him188.ani.app.ui.foundation.apple.MacTableColumn
import me.him188.ani.app.ui.foundation.apple.MacTableColumns
import me.him188.ani.app.ui.foundation.apple.MacTableHeader
import me.him188.ani.app.ui.foundation.apple.MacTableHeaderCell
import me.him188.ani.app.ui.foundation.apple.MacTableHint
import me.him188.ani.app.ui.foundation.apple.MacTableRow
import me.him188.ani.app.ui.foundation.apple.MacToolbarIconButton
import me.him188.ani.app.ui.foundation.apple.PopoverMenuItem
import me.him188.ani.app.ui.foundation.apple.ShareLinkMenuItems
import me.him188.ani.app.ui.foundation.apple.UnfollowConfirmDialog
import me.him188.ani.app.ui.foundation.apple.bangumiSubjectUrl
import me.him188.ani.app.ui.foundation.apple.onSecondaryClick
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.NsfwMask
import me.him188.ani.app.ui.foundation.widgets.showLoadError
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.cache_management_open_subject
import me.him188.ani.app.ui.lang.exploration_quick_follow
import me.him188.ani.app.ui.lang.exploration_quick_follow_followed
import me.him188.ani.app.ui.lang.exploration_refresh
import me.him188.ani.app.ui.lang.exploration_trending_ranking
import me.him188.ani.app.ui.lang.exploration_trending_ranking_col_heat
import me.him188.ani.app.ui.lang.exploration_trending_ranking_col_rank
import me.him188.ani.app.ui.lang.exploration_trending_ranking_col_status
import me.him188.ani.app.ui.lang.exploration_trending_ranking_col_subject
import me.him188.ani.app.ui.lang.exploration_trending_ranking_description
import me.him188.ani.app.ui.lang.exploration_trending_ranking_empty
import me.him188.ani.app.ui.lang.exploration_trending_ranking_followed
import me.him188.ani.app.ui.lang.exploration_trending_ranking_heat_unit
import me.him188.ani.app.ui.lang.exploration_trending_ranking_table_hint
import me.him188.ani.app.ui.lang.subject_collection_collect
import me.him188.ani.app.ui.lang.subject_collection_delete
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.search.isFinishedAndEmpty
import me.him188.ani.app.ui.search.isLoadingFirstPageOrRefreshing
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 版热度排行 (04 §4.1).
 *
 * 工具栏右侧为重新整理 (⌘R). 大标题「熱度排行」与同一基线的说明「依近 30 天在看人數排序」.
 * C19 表格 (card 圆角 14) 的表头与每一行共用 4 列: 名次 56 (居中)、作品 (弹性)、近 30 天在看 160 (居中, 表头带排序箭头)、
 * 状态 96 (居中, 快速追番胶囊, 05 G2). 列间距 24, 左右内距 24; 表头高 36, 行高 76; 分隔线左 104、右 24.
 *
 * 单击选中, 双击打开条目详情. 右键菜单为打开条目详情、追番或取消追番、复制 Bangumi 链接与在浏览器中打开 (05 C3、G6).
 * 表格下方为操作提示.
 *
 * @param quickFollowState 为 `null` 时不显示追番胶囊与右键菜单的追番项 (未登录).
 */
@Composable
fun MacTrendingRankingPage(
    items: LazyPagingItems<TrendingRankingItemPresentation>,
    quickFollowState: QuickFollowState?,
    onOpenItem: (TrendingRankingItemPresentation) -> Unit,
    modifier: Modifier = Modifier,
) {
    MacKeyboardShortcut(Key.R) { items.refresh() }
    var selectedKey by rememberSaveable { mutableStateOf<String?>(null) }
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        MacPageToolbar {
            MacToolbarIconButton(
                onClick = { items.refresh() },
                icon = AppleIcons.refresh(),
                contentDescription = stringResource(Lang.exploration_refresh),
                Modifier.testTag(MacTrendingTestTags.Refresh),
            )
        }
        LazyColumn(
            Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 32.dp, top = 4.dp, end = 32.dp, bottom = 32.dp),
        ) {
            item(key = "header") {
                Column(Modifier.padding(bottom = 20.dp)) {
                    MacLargeTitleHeader(
                        stringResource(Lang.exploration_trending_ranking),
                        subtitle = stringResource(Lang.exploration_trending_ranking_description),
                    )
                }
            }
            if (items.loadState.hasError) {
                item(key = "error") {
                    LoadErrorCard(
                        LoadError.fromCombinedLoadStates(items.loadState),
                        onRetry = { items.retry() },
                        Modifier.padding(bottom = 16.dp),
                    )
                }
            }
            item(key = "table-header") {
                MacTableHeader(
                    MacTrendingDefaults.Columns,
                    Modifier
                        .clip(segmentShape(top = true, bottom = items.itemCount == 0))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    height = 36.dp,
                ) {
                    MacTableHeaderCell(stringResource(Lang.exploration_trending_ranking_col_rank))
                    MacTableHeaderCell(stringResource(Lang.exploration_trending_ranking_col_subject))
                    MacTableHeaderCell(
                        stringResource(Lang.exploration_trending_ranking_col_heat),
                        Modifier.testTag(MacTrendingTestTags.HeatHeader),
                        sorted = true,
                    )
                    MacTableHeaderCell(stringResource(Lang.exploration_trending_ranking_col_status))
                }
            }
            if (items.itemCount == 0) {
                item(key = "table-empty") {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(segmentShape(top = false, bottom = true))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    ) {
                        when {
                            items.isLoadingFirstPageOrRefreshing -> LoadingIndicator()
                            items.isFinishedAndEmpty -> Text(
                                stringResource(Lang.exploration_trending_ranking_empty),
                                Modifier.fillMaxWidth().padding(vertical = 24.dp),
                                style = AniTextStyles.macCallout,
                                color = MaterialTheme.colorScheme.secondary,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }
            items(
                count = items.itemCount,
                key = { index ->
                    // 翻页期间排行可能更新, 同一个条目可能出现两次, 所以 key 带上位置
                    items.peek(index)?.let { "trending-$index-${it.subject.subjectId}" } ?: "trending-placeholder-$index"
                },
            ) { index ->
                val item = items[index]
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(segmentShape(top = false, bottom = index == items.itemCount - 1))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                ) {
                    if (item == null) {
                        Spacer(Modifier.fillMaxWidth().height(MacTrendingDefaults.RowHeight))
                    } else {
                        val key = "$index-${item.subject.subjectId}"
                        TrendingRowWithMenu(
                            item,
                            quickFollowState,
                            selected = selectedKey == key,
                            showDivider = true,
                            onSelect = { selectedKey = key },
                            onOpen = { onOpenItem(item) },
                        )
                    }
                }
            }
            item(key = "hint") {
                MacTableHint(
                    stringResource(Lang.exploration_trending_ranking_table_hint),
                    Modifier.padding(start = 24.dp),
                )
            }
        }
    }
}

/**
 * 表格 card 的一段: [top] / [bottom] 为表头 / 最后一行时圆角 14.
 */
private fun segmentShape(top: Boolean, bottom: Boolean): Shape {
    val topRadius = if (top) 14.dp else 0.dp
    val bottomRadius = if (bottom) 14.dp else 0.dp
    if (!top && !bottom) return RectangleShape
    return RoundedCornerShape(topStart = topRadius, topEnd = topRadius, bottomStart = bottomRadius, bottomEnd = bottomRadius)
}

/**
 * 表格行与它的右键菜单. 菜单在右键的位置打开, 打开时选中该行.
 */
@Composable
private fun TrendingRowWithMenu(
    item: TrendingRankingItemPresentation,
    quickFollowState: QuickFollowState?,
    selected: Boolean,
    showDivider: Boolean,
    onSelect: () -> Unit,
    onOpen: () -> Unit,
) {
    val subjectId = item.subject.subjectId
    var menuPosition by remember(subjectId) { mutableStateOf<Offset?>(null) }
    var confirmUnfollow by remember(subjectId) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current
    Box(
        Modifier
            .onSecondaryClick {
                onSelect()
                menuPosition = it
            },
    ) {
        TrendingTableRow(item, quickFollowState, selected, showDivider, onSelect, onOpen)
        val position = menuPosition
        if (position != null) {
            fun dismissThen(action: () -> Unit): () -> Unit = {
                menuPosition = null
                action()
            }
            AppleContextMenu(
                expanded = true,
                position = position,
                onDismissRequest = { menuPosition = null },
                modifier = Modifier.testTag(MacTrendingTestTags.Menu),
            ) {
                PopoverMenuItem(
                    stringResource(Lang.cache_management_open_subject),
                    onClick = dismissThen(onOpen),
                    Modifier.testTag(MacTrendingTestTags.MenuOpen),
                )
                if (quickFollowState != null) {
                    val type by remember(quickFollowState, subjectId) { quickFollowState.collectionTypeFlow(subjectId) }
                        .collectAsStateWithLifecycle(null)
                    val currentType = type
                    if (currentType == UnifiedCollectionType.NOT_COLLECTED) {
                        PopoverMenuItem(
                            stringResource(Lang.subject_collection_collect),
                            onClick = dismissThen {
                                scope.launch { quickFollowState.follow(subjectId)?.let { toaster.showLoadError(it) } }
                            },
                            Modifier.testTag(MacTrendingTestTags.MenuFollow),
                            showDivider = true,
                        )
                    } else if (currentType != null) {
                        PopoverMenuItem(
                            stringResource(Lang.subject_collection_delete),
                            onClick = dismissThen { confirmUnfollow = true },
                            Modifier.testTag(MacTrendingTestTags.MenuUnfollow),
                            showDivider = true,
                        )
                    }
                }
                ShareLinkMenuItems(
                    bangumiSubjectUrl(subjectId),
                    scope,
                    onDismissRequest = { menuPosition = null },
                    showDivider = true,
                    copyModifier = Modifier.testTag(MacTrendingTestTags.MenuCopyLink),
                )
            }
        }
    }
    if (confirmUnfollow && quickFollowState != null) {
        UnfollowConfirmDialog(
            onConfirm = {
                confirmUnfollow = false
                scope.launch {
                    quickFollowState.setCollectionType(subjectId, UnifiedCollectionType.NOT_COLLECTED)
                        ?.let { toaster.showLoadError(it) }
                }
            },
            onDismissRequest = { confirmUnfollow = false },
        )
    }
}

/**
 * 表格行 (高 76): 名次、作品 (42×56 封面圆角 6 + 间距 16 + 番名 15/600 + 放送季度与话数 13 label2)、
 * 近 30 天在看 (人数 15/600 +「人」13 label2)、追番胶囊. 选中行 tint 底.
 */
@Composable
private fun TrendingTableRow(
    item: TrendingRankingItemPresentation,
    quickFollowState: QuickFollowState?,
    selected: Boolean,
    showDivider: Boolean,
    onSelect: () -> Unit,
    onOpen: () -> Unit,
) {
    val subject = item.subject
    val title = if (LocalSubjectAppearanceSettings.current.useOriginalTitle) subject.originalTitle else subject.title
    MacTableRow(
        MacTrendingDefaults.Columns,
        Modifier.testTag(MacTrendingTestTags.row(subject.subjectId)),
        height = MacTrendingDefaults.RowHeight,
        selected = selected,
        showDivider = showDivider,
        dividerStartIndent = 104.dp,
        dividerEndIndent = 24.dp,
        onClick = onSelect,
        onDoubleClick = onOpen,
    ) {
        TrendingRankText(item.rank)
        Row(verticalAlignment = Alignment.CenterVertically) {
            var nsfwMode by rememberSaveable(subject.subjectId) { mutableStateOf(subject.nsfwMode) }
            NsfwMask(
                nsfwMode,
                onTemporarilyDisplay = { nsfwMode = NsfwMode.DISPLAY },
                shape = RoundedCornerShape(6.dp),
            ) {
                AsyncImage(
                    subject.imageUrl,
                    contentDescription = null,
                    Modifier
                        .size(42.dp, 56.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentScale = ContentScale.Crop,
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    title,
                    style = AniTextStyles.macWindowTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    subject.airingText,
                    style = AniTextStyles.macCallout,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(Modifier.testTag(MacTrendingTestTags.heat(subject.subjectId)), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                renderTrendingHeatValue(item.heat),
                Modifier.alignByBaseline(),
                style = AniTextStyles.macWindowTitle.copy(fontFeatureSettings = "tnum"),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
            Text(
                stringResource(Lang.exploration_trending_ranking_heat_unit),
                Modifier.alignByBaseline(),
                style = AniTextStyles.macCallout,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
            )
        }
        if (quickFollowState != null) {
            QuickFollowAnchor(quickFollowState, subject.subjectId) { followed, enabled, onClickFollow ->
                MacFollowPill(
                    followed = followed,
                    enabled = enabled,
                    onClick = onClickFollow,
                    contentDescription = if (followed) {
                        stringResource(Lang.exploration_quick_follow_followed, title)
                    } else {
                        stringResource(Lang.exploration_quick_follow, title)
                    },
                    Modifier.testTag(MacTrendingTestTags.follow(subject.subjectId)),
                )
            }
        } else {
            Spacer(Modifier)
        }
    }
}

/**
 * 追番胶囊: 96×28, 「追番」与「已追番」同宽. 未追番为 fill 底 label「＋ 追番」; 已追番为 tint 底 accent「✓ 已追番」.
 */
@Composable
private fun MacFollowPill(
    followed: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val contentColor = if (followed) colorScheme.primary else colorScheme.onSurface
    Row(
        modifier
            .size(96.dp, 28.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (followed) colorScheme.secondaryContainer else colorScheme.surfaceContainerHighest)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
                this.selected = followed
            },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            if (followed) AppleIcons.check(2.6f) else AppleIcons.plus(2.6f),
            contentDescription = null,
            Modifier.size(11.dp),
            tint = contentColor,
        )
        Spacer(Modifier.width(4.dp))
        Text(
            stringResource(if (followed) Lang.exploration_trending_ranking_followed else Lang.subject_collection_collect),
            style = AniTextStyles.macCallout,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
        )
    }
}

internal object MacTrendingDefaults {
    val RowHeight = 76.dp

    /** 名次 56 | 作品 | 近 30 天在看 160 | 状态 96, 列间距 24, 左右内距 24. */
    val Columns = MacTableColumns(
        listOf(
            MacTableColumn(56.dp, alignment = Alignment.CenterHorizontally),
            MacTableColumn(weight = 1f),
            MacTableColumn(160.dp, alignment = Alignment.CenterHorizontally),
            MacTableColumn(96.dp, alignment = Alignment.CenterHorizontally),
        ),
        horizontalPadding = 24.dp,
        spacing = 24.dp,
    )
}

internal object MacTrendingTestTags {
    const val Refresh = "mac-trending-refresh"
    const val HeatHeader = "mac-trending-heat-header"
    const val Menu = "mac-trending-menu"
    const val MenuOpen = "mac-trending-menu-open"
    const val MenuFollow = "mac-trending-menu-follow"
    const val MenuUnfollow = "mac-trending-menu-unfollow"
    const val MenuCopyLink = "mac-trending-menu-copy-link"
    fun row(subjectId: Int) = "mac-trending-row-$subjectId"
    fun follow(subjectId: Int) = "mac-trending-follow-$subjectId"
    fun heat(subjectId: Int) = "mac-trending-heat-$subjectId"
}
