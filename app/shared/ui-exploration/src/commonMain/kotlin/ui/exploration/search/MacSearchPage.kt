/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.data.models.preference.NsfwMode
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.ui.exploration.follow.QuickFollowAnchor
import me.him188.ani.app.ui.exploration.follow.QuickFollowState
import me.him188.ani.app.ui.exploration.schedule.MacFollowToggle
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleCover
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.CoverAspect
import me.him188.ani.app.ui.foundation.apple.MacLargeTitleHeader
import me.him188.ani.app.ui.foundation.apple.MacPageToolbar
import me.him188.ani.app.ui.foundation.interaction.keyboardDirectionToSelectItem
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.NsfwMask
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.exploration_quick_follow
import me.him188.ani.app.ui.lang.exploration_quick_follow_followed
import me.him188.ani.app.ui.lang.exploration_search
import me.him188.ani.app.ui.lang.exploration_search_results_shown
import me.him188.ani.app.ui.lang.foundation_load_error_no_results
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.search.collectItemsWithLifecycle
import me.him188.ani.app.ui.search.hasFirstPage
import me.him188.ani.app.ui.search.isFinishedAndEmpty
import me.him188.ani.app.ui.search.isLoadingFirstPageOrRefreshing
import me.him188.ani.app.ui.search.isLoadingNextPage
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 搜索页. 搜索框在侧边栏 (05 G11), 本页没有输入框.
 *
 * 大标题「搜尋」与结果数; 筛选胶囊同 iOS (排序、年份、季度、各标签分类); 结果为一张列表卡. 点结果进入条目详情.
 * 键盘: ↑ / ↓ 选取结果 (现有的选取逻辑), ↩ 打开选中的结果 (05 C2). 每次搜索 (含改筛选) 后焦点移到结果列表,
 * 侧边栏的输入框里按 ↩ 只用于搜索. 还没有搜索时不移动焦点.
 *
 * @param quickFollowState 为 `null` 时不显示快速追番按钮 (未登录).
 */
@Composable
fun MacSearchPage(
    state: SearchPageState,
    onIntent: (SearchPageIntent) -> Unit,
    quickFollowState: QuickFollowState?,
    modifier: Modifier = Modifier,
) {
    val items = state.searchState.collectItemsWithLifecycle()
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }
    // 还没有搜索时 (例如从其他入口打开空的搜索页) 不抢焦点, 用户要在侧边栏输入
    LaunchedEffect(state.query, state.hasActiveSearch) {
        if (state.hasActiveSearch) {
            focusRequester.requestFocus()
        }
    }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        MacPageToolbar()
        LazyColumn(
            Modifier
                .fillMaxSize()
                .testTag(MacSearchTestTags.Results)
                .keyboardDirectionToSelectItem(
                    selectedItemIndex = { state.selectedItemIndex },
                    itemCount = { items.itemCount },
                ) { index ->
                    val item = items.peek(index) ?: return@keyboardDirectionToSelectItem
                    onIntent(SearchPageIntent.SelectResult(index, item))
                    listState.revealItem(index + RESULTS_START_INDEX)
                }
                .onPreviewKeyEvent { event ->
                    // 按下时处理: 侧边栏输入框里按 ↩ 搜索后焦点才移过来, 不会接到那次按键的按下
                    val isEnter = event.key == Key.Enter || event.key == Key.NumPadEnter
                    val hasModifier = event.isMetaPressed || event.isCtrlPressed || event.isAltPressed || event.isShiftPressed
                    if (event.type != KeyEventType.KeyDown || !isEnter || hasModifier) return@onPreviewKeyEvent false
                    val index = state.selectedItemIndex
                    if (index !in 0 until items.itemCount) return@onPreviewKeyEvent false
                    val item = items.peek(index) ?: return@onPreviewKeyEvent false
                    onIntent(SearchPageIntent.OpenSubjectDetails(index, item))
                    true
                }
                .focusRequester(focusRequester)
                .focusable(),
            state = listState,
            contentPadding = PaddingValues(start = HorizontalPadding, top = 4.dp, end = HorizontalPadding, bottom = HorizontalPadding),
        ) {
            // 结果之前固定 RESULTS_START_INDEX 项, 键盘选取时据此滚到结果
            item(key = "title") {
                val subtitle = when {
                    !state.hasActiveSearch -> null
                    items.isFinishedAndEmpty -> stringResource(Lang.foundation_load_error_no_results)
                    items.hasFirstPage -> stringResource(Lang.exploration_search_results_shown, items.itemCount)
                    else -> null
                }
                MacLargeTitleHeader(stringResource(Lang.exploration_search), subtitle = subtitle)
            }
            item(key = "filters") {
                SearchFilterPills(state, onIntent, Modifier.padding(top = 16.dp, bottom = 16.dp))
            }
            item(key = "status") {
                when {
                    items.loadState.hasError -> LoadErrorCard(
                        LoadError.fromCombinedLoadStates(items.loadState),
                        onRetry = { items.retry() },
                        Modifier.padding(bottom = 16.dp),
                    )

                    items.itemCount == 0 && items.isLoadingFirstPageOrRefreshing -> LoadingIndicator()
                }
            }
            items(
                items.itemCount,
                key = { index -> items.peek(index)?.let { "result-$index-${it.subjectId}" } ?: "result-placeholder-$index" },
            ) { index ->
                val info = items[index]
                if (info == null || info.hide) return@items
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(macListCardShape(index, items.itemCount))
                        .background(
                            if (index == state.selectedItemIndex) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerLow
                            },
                        ),
                ) {
                    MacSearchResultRow(
                        info,
                        keywords = state.query.keywords,
                        quickFollowState = quickFollowState,
                        selected = index == state.selectedItemIndex,
                        onClick = { onIntent(SearchPageIntent.OpenSubjectDetails(index, info)) },
                        showDivider = index > 0,
                    )
                }
            }
            if (items.isLoadingNextPage) {
                item(key = "loading-next") { LoadingIndicator() }
            }
        }
    }
}

/**
 * 结果列: 内距 8 12 8 12; 封面 40 宽 3:4、圆角 6; 标题 `macBody` W600, 命中关键字为 accent W700;
 * 放送季度与话数、类型 `macCaption` label2; 右侧 28 追番切换钮. 分隔线从 64 开始. 选中为 tint 底.
 */
@Composable
private fun MacSearchResultRow(
    info: SubjectPreviewItemInfo,
    keywords: String,
    quickFollowState: QuickFollowState?,
    selected: Boolean,
    onClick: () -> Unit,
    showDivider: Boolean,
) {
    val useOriginalTitle = LocalSubjectAppearanceSettings.current.useOriginalTitle
    val title = if (useOriginalTitle) info.originalTitle else info.title
    var nsfwMode by rememberSaveable(info.subjectId) { mutableStateOf(info.nsfwMode) }
    val colorScheme = MaterialTheme.colorScheme
    Box(Modifier.testTag(AppleSearchTestTags.result(info.subjectId)).semantics { this.selected = selected }) {
        NsfwMask(nsfwMode, onTemporarilyDisplay = { nsfwMode = NsfwMode.DISPLAY }, shape = RectangleShape) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppleCover(CoverAspect.Portrait3x4, title, Modifier.width(40.dp), cornerRadius = 6.dp) {
                    AsyncImage(info.imageUrl, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        highlightKeywords(title, keywords, colorScheme.primary),
                        style = AniTextStyles.macBody,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    val details = listOf(info.airingText, info.genreText).filter { it.isNotBlank() }
                    for (line in details) {
                        Text(
                            line,
                            style = AniTextStyles.macCaption,
                            color = colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (quickFollowState != null) {
                    QuickFollowAnchor(quickFollowState, info.subjectId) { followed, enabled, onClickFollow ->
                        MacFollowToggle(
                            followed = followed,
                            enabled = enabled,
                            onClick = onClickFollow,
                            contentDescription = if (followed) {
                                stringResource(Lang.exploration_quick_follow_followed, title)
                            } else {
                                stringResource(Lang.exploration_quick_follow, title)
                            },
                            Modifier.testTag(AppleSearchTestTags.follow(info.subjectId)),
                        )
                    }
                }
            }
        }
        if (showDivider && !selected) {
            AppleDivider(Modifier.align(Alignment.TopStart), startIndent = 64.dp)
        }
    }
}

/**
 * 列表卡的一列: 第一列圆上角, 最后一列圆下角, 圆角 12.
 */
private fun macListCardShape(index: Int, count: Int): Shape {
    val top = if (index == 0) 12.dp else 0.dp
    val bottom = if (index == count - 1) 12.dp else 0.dp
    if (top == 0.dp && bottom == 0.dp) return RectangleShape
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

/**
 * 让第 [index] 项完整出现在可见范围内, 只滚动必要的距离; 已经完整可见时不滚动.
 */
private suspend fun LazyListState.revealItem(index: Int) {
    val layout = layoutInfo
    val visible = layout.visibleItemsInfo
    if (visible.isEmpty()) {
        animateScrollToItem(index)
        return
    }
    val start = layout.viewportStartOffset
    val end = layout.viewportEndOffset
    val item = visible.firstOrNull { it.index == index }
    when {
        item != null && item.offset < start -> animateScrollBy((item.offset - start).toFloat())
        item != null && item.offset + item.size > end -> animateScrollBy((item.offset + item.size - end).toFloat())
        item != null -> {}
        index < visible.first().index -> animateScrollToItem(index)
        else -> {
            // 在可见范围下方: 按可见项的平均高度估算, 滚到它贴着底边
            val last = visible.last()
            val averageSize = visible.sumOf { it.size } / visible.size
            animateScrollBy((last.offset + last.size - end + (index - last.index) * averageSize).toFloat())
        }
    }
}

/** 结果之前的列表项数: 标题、筛选、状态. */
private const val RESULTS_START_INDEX = 3

private val HorizontalPadding = 32.dp

internal object MacSearchTestTags {
    const val Results = "mac-search-results"
}
