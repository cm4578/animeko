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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItemsWithLifecycle
import androidx.paging.compose.itemKey
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import me.him188.ani.app.data.models.preference.NsfwMode
import me.him188.ani.app.data.models.schedule.AnimeSeason
import me.him188.ani.app.data.models.subject.CanonicalTagKind
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.domain.search.SearchSort
import me.him188.ani.app.ui.exploration.follow.QuickFollowAnchor
import me.him188.ani.app.ui.exploration.follow.QuickFollowState
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleCover
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleFollowButton
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.ApplePopoverMenu
import me.him188.ani.app.ui.foundation.apple.CoverAspect
import me.him188.ani.app.ui.foundation.apple.FilterPill
import me.him188.ani.app.ui.foundation.apple.FilterPillFlowRow
import me.him188.ani.app.ui.foundation.apple.PopoverMenuItem
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.layout.plus
import me.him188.ani.app.ui.foundation.navigation.BackHandler
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.NsfwMask
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.exploration_quick_follow
import me.him188.ani.app.ui.lang.exploration_quick_follow_followed
import me.him188.ani.app.ui.lang.exploration_search_cancel
import me.him188.ani.app.ui.lang.exploration_search_clear
import me.him188.ani.app.ui.lang.exploration_search_filter_season_all
import me.him188.ani.app.ui.lang.exploration_search_filter_year_all
import me.him188.ani.app.ui.lang.exploration_search_results_shown
import me.him188.ani.app.ui.lang.foundation_load_error_no_results
import me.him188.ani.app.ui.lang.settings_mediasource_test_keyword
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.search.collectItemsWithLifecycle
import me.him188.ani.app.ui.search.hasFirstPage
import me.him188.ani.app.ui.search.isFinishedAndEmpty
import me.him188.ani.app.ui.search.isLoadingFirstPageOrRefreshing
import me.him188.ani.app.ui.search.isLoadingNextPage
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版搜索页 (03 §3.9). 手机全屏任务, 不显示导航栏.
 *
 * 顶部为搜索框与「取消」. 输入框获得焦点时, 下方显示搜索记录 (输入为空时) 或搜索建议;
 * 否则显示筛选胶囊 (排序、年份、季度、标签分类)、结果数与结果列表.
 * 点结果进入条目详情. 每列右侧为快速追番按钮 (05 G2).
 *
 * @param quickFollowState 为 `null` 时不显示快速追番按钮 (未登录).
 * @param onCancel 「取消」: 离开搜索页.
 * @param contentPadding 列表底部要留出的空间, 例如系统安全区.
 */
@Composable
fun AppleSearchPage(
    state: SearchPageState,
    onIntent: (SearchPageIntent) -> Unit,
    suggestionsPager: (String) -> Flow<PagingData<String>>,
    quickFollowState: QuickFollowState?,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val items = state.searchState.collectItemsWithLifecycle()
    val focusManager = LocalFocusManager.current
    var editingQuery by rememberSaveable(state.query.keywords) { mutableStateOf(state.query.keywords) }
    var focused by remember { mutableStateOf(false) }

    BackHandler(focused) {
        focusManager.clearFocus()
    }

    fun submit(keywords: String) {
        focusManager.clearFocus()
        onIntent(SearchPageIntent.UpdateQuery(state.query.copy(keywords = keywords), submit = true))
    }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        SearchBarRow(
            query = editingQuery,
            onQueryChange = { editingQuery = it.trim('\n') },
            onSearch = { submit(editingQuery) },
            onClear = {
                editingQuery = ""
                onIntent(SearchPageIntent.UpdateQuery(state.query.copy(keywords = "")))
            },
            onFocusChange = { focused = it },
            onCancel = onCancel,
            windowInsets = windowInsets,
        )
        if (focused) {
            SearchSuggestions(
                state,
                editingQuery,
                suggestionsPager,
                onPick = {
                    editingQuery = it
                    submit(it)
                },
                onRemoveHistory = { onIntent(SearchPageIntent.RemoveHistory(it)) },
                contentPadding = contentPadding,
            )
        } else {
            SearchResults(state, items, onIntent, quickFollowState, contentPadding)
        }
    }
}

/**
 * 搜索列: 距安全区 10. 搜索框高 40、圆角 20、card 底: 放大镜 16 (label2) + 输入 `body` + 清除钮 (18 圆, label2 底、card 色叉).
 * 右侧「取消」`body` accent.
 */
@Composable
private fun SearchBarRow(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit,
    onFocusChange: (Boolean) -> Unit,
    onCancel: () -> Unit,
    windowInsets: WindowInsets,
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
            .padding(start = 16.dp, end = 16.dp, top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            Modifier
                .weight(1f)
                .height(40.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colorScheme.surfaceContainerLow)
                .padding(start = 14.dp, end = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(AppleIcons.search(2.4f), contentDescription = null, Modifier.size(16.dp), tint = colorScheme.secondary)
            val placeholder = stringResource(Lang.settings_mediasource_test_keyword)
            BasicTextField(
                query,
                onQueryChange,
                Modifier
                    .weight(1f)
                    .onFocusChanged { onFocusChange(it.isFocused) }
                    .testTag(AppleSearchTestTags.Field),
                textStyle = AniTextStyles.body.copy(color = colorScheme.onSurface),
                // 番名多为专有名词, 关闭自动修正, 避免提交时被改写.
                keyboardOptions = KeyboardOptions(autoCorrectEnabled = false, imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                singleLine = true,
                cursorBrush = SolidColor(colorScheme.primary),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) {
                            Text(placeholder, style = AniTextStyles.body, color = colorScheme.secondary, maxLines = 1)
                        }
                        innerTextField()
                    }
                },
            )
            if (query.isNotEmpty()) {
                val clearText = stringResource(Lang.exploration_search_clear)
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = onClear)
                        .semantics { contentDescription = clearText }
                        .testTag(AppleSearchTestTags.Clear),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        Modifier.size(18.dp).clip(CircleShape).background(colorScheme.secondary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            AppleIcons.close(3.4f),
                            contentDescription = null,
                            Modifier.size(10.dp),
                            tint = colorScheme.surfaceContainerLow,
                        )
                    }
                }
            }
        }
        Text(
            stringResource(Lang.exploration_search_cancel),
            Modifier
                .clickable(role = Role.Button, onClick = onCancel)
                .testTag(AppleSearchTestTags.Cancel),
            style = AniTextStyles.body,
            color = colorScheme.primary,
            maxLines = 1,
        )
    }
}

/**
 * 输入框获得焦点时的列表: 输入为空时为搜索记录 (可删除), 否则为搜索建议. 输入停顿 800ms 后才更新建议.
 *
 * 列表项不取得焦点: 点选时输入框保持焦点, 列表不会在点击完成前消失.
 */
@Composable
private fun SearchSuggestions(
    state: SearchPageState,
    editingQuery: String,
    suggestionsPager: (String) -> Flow<PagingData<String>>,
    onPick: (String) -> Unit,
    onRemoveHistory: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    var debouncedQuery by remember { mutableStateOf(editingQuery) }
    LaunchedEffect(editingQuery) {
        delay(800)
        debouncedQuery = editingQuery
    }
    val isHistory = editingQuery.isEmpty()
    val pager = remember(isHistory, debouncedQuery, state.searchHistoryPager) {
        if (isHistory) state.searchHistoryPager else suggestionsPager(debouncedQuery)
    }
    val values = pager.collectAsLazyPagingItemsWithLifecycle()

    LazyColumn(
        Modifier.fillMaxSize().testTag(AppleSearchTestTags.Suggestions),
        contentPadding = PaddingValues(top = 12.dp).plus(contentPadding),
    ) {
        items(values.itemCount, key = values.itemKey { "suggestion-$it" }) { index ->
            val text = values[index] ?: return@items
            Box(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(listCardShape(index, values.itemCount))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .focusProperties { canFocus = false }
                        .clickable(role = Role.Button) { onPick(text) }
                        .padding(start = 16.dp, end = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        if (isHistory) AppleIcons.history() else AppleIcons.search(2.2f),
                        contentDescription = null,
                        Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text,
                        Modifier.weight(1f).padding(vertical = 12.dp),
                        style = AniTextStyles.body,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (isHistory) {
                        Box(
                            Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .focusProperties { canFocus = false }
                                .clickable(enabled = state.removingHistory != text, role = Role.Button) {
                                    onRemoveHistory(text)
                                }
                                .semantics { contentDescription = text },
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(
                                AppleIcons.close(),
                                contentDescription = null,
                                Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
                if (index > 0) {
                    AppleDivider(Modifier.align(Alignment.TopStart), startIndent = 48.dp)
                }
            }
        }
    }
}

/**
 * 筛选胶囊、结果数与结果列表.
 */
@Composable
private fun SearchResults(
    state: SearchPageState,
    items: LazyPagingItems<SubjectPreviewItemInfo>,
    onIntent: (SearchPageIntent) -> Unit,
    quickFollowState: QuickFollowState?,
    contentPadding: PaddingValues,
) {
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 12.dp).plus(contentPadding)) {
        item(key = "filters") {
            SearchFilterPills(state, onIntent, Modifier.padding(horizontal = 16.dp))
        }
        if (state.hasActiveSearch) {
            item(key = "summary") {
                val text = when {
                    items.isFinishedAndEmpty -> stringResource(Lang.foundation_load_error_no_results)
                    items.hasFirstPage -> stringResource(Lang.exploration_search_results_shown, items.itemCount)
                    else -> null
                }
                if (text != null) {
                    Text(
                        text,
                        Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                        style = AniTextStyles.footnote,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
        if (items.loadState.hasError) {
            item(key = "error") {
                LoadErrorCard(
                    LoadError.fromCombinedLoadStates(items.loadState),
                    onRetry = { items.retry() },
                    Modifier.padding(horizontal = 16.dp).padding(top = 16.dp),
                )
            }
        }
        if (items.itemCount == 0 && items.isLoadingFirstPageOrRefreshing) {
            item(key = "loading") { LoadingIndicator() }
        }
        items(
            items.itemCount,
            key = { index -> items.peek(index)?.let { "result-$index-${it.subjectId}" } ?: "result-placeholder-$index" },
        ) { index ->
            val info = items[index]
            if (info == null || info.hide) return@items
            Box(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(listCardShape(index, items.itemCount))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                SearchResultRow(
                    info,
                    keywords = state.query.keywords,
                    quickFollowState = quickFollowState,
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

@Composable
private fun LoadingIndicator() {
    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * 筛选胶囊 (C4), 换行排列: 排序 (总是套用)、年份、季度 (未选年份时停用)、各标签分类.
 *
 * 排序、年份、季度为单选, 选后关闭菜单; 标签分类可多选, 点选项切换选中, 菜单保持打开.
 */
@Composable
private fun SearchFilterPills(
    state: SearchPageState,
    onIntent: (SearchPageIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sortLabels = rememberSearchSortLabels()
    val filterLabels = rememberSearchFilterLabels()
    val allYearsText = stringResource(Lang.exploration_search_filter_year_all)
    val allSeasonsText = stringResource(Lang.exploration_search_filter_season_all)
    val query = state.query

    FilterPillFlowRow(modifier.fillMaxWidth()) {
        MenuFilterPill(
            getSortText(query.sort, sortLabels),
            applied = true,
            Modifier.testTag(AppleSearchTestTags.Sort),
        ) { dismiss ->
            for (sort in SearchSort.entries) {
                PopoverMenuItem(
                    getSortText(sort, sortLabels),
                    onClick = {
                        dismiss()
                        if (sort != query.sort) onIntent(SearchPageIntent.ChangeSort(sort))
                    },
                    Modifier.testTag(AppleSearchTestTags.sortOption(sort)),
                    showDivider = sort.ordinal > 0,
                    checked = sort == query.sort,
                )
            }
        }

        val years = remember(state.seasons) { state.seasons.map { it.year }.distinct() }
        MenuFilterPill(
            query.year?.toString() ?: allYearsText,
            applied = query.year != null,
            Modifier.testTag(AppleSearchTestTags.Year),
        ) { dismiss ->
            PopoverMenuItem(
                allYearsText,
                onClick = {
                    dismiss()
                    onIntent(SearchPageIntent.ChangeYear(null))
                },
                checked = query.year == null,
            )
            for (year in years) {
                PopoverMenuItem(
                    year.toString(),
                    onClick = {
                        dismiss()
                        onIntent(SearchPageIntent.ChangeYear(year))
                    },
                    showDivider = true,
                    checked = year == query.year,
                )
            }
        }

        MenuFilterPill(
            query.season?.let { "Q${it.quarterNumber}" } ?: allSeasonsText,
            applied = query.season != null,
            Modifier.testTag(AppleSearchTestTags.Season),
            // 季度从属于年份: 未选年份时停用.
            enabled = query.year != null,
        ) { dismiss ->
            PopoverMenuItem(
                allSeasonsText,
                onClick = {
                    dismiss()
                    onIntent(SearchPageIntent.ChangeSeason(null))
                },
                checked = query.season == null,
            )
            for (season in AnimeSeason.entries) {
                PopoverMenuItem(
                    "Q${season.quarterNumber}",
                    onClick = {
                        dismiss()
                        onIntent(SearchPageIntent.ChangeSeason(season))
                    },
                    showDivider = true,
                    checked = season == query.season,
                )
            }
        }

        for (chip in state.searchFilterState.chips) {
            key(chip.kind) {
                MenuFilterPill(
                    renderChipLabel(chip, filterLabels),
                    applied = chip.hasSelection,
                    Modifier.testTag(AppleSearchTestTags.tagKind(chip.kind)),
                ) {
                    chip.values.forEachIndexed { index, value ->
                        PopoverMenuItem(
                            value,
                            onClick = {
                                val updated = state.toggleTagSelection(chip, value, unselectOthersOfSameKind = false)
                                onIntent(SearchPageIntent.UpdateQuery(updated.query))
                            },
                            showDivider = index > 0,
                            checked = value in chip.selected,
                        )
                    }
                }
            }
        }
    }
}

/**
 * 点击打开 C15 菜单的筛选胶囊. 选项多时菜单内可滚动.
 *
 * @param menuContent 菜单内容, 参数为关闭菜单.
 */
@Composable
private fun MenuFilterPill(
    text: String,
    applied: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    menuContent: @Composable (dismiss: () -> Unit) -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box {
        FilterPill(text, applied, onClick = { expanded = true }, modifier, enabled = enabled)
        ApplePopoverMenu(expanded, onDismissRequest = { expanded = false }) {
            Column(Modifier.heightIn(max = 360.dp).verticalScroll(rememberScrollState())) {
                menuContent { expanded = false }
            }
        }
    }
}

/**
 * 结果列: 内距 `12 12 12 16`; 封面 56 宽 3:4、圆角 8; 标题 `body` W600, 命中关键字为 accent W700;
 * 放送季度与话数 `footnote` label2; 类型 `caption` label2; 右侧 36 追番圆钮. 分隔线从 84 开始.
 */
@Composable
private fun SearchResultRow(
    info: SubjectPreviewItemInfo,
    keywords: String,
    quickFollowState: QuickFollowState?,
    onClick: () -> Unit,
    showDivider: Boolean,
) {
    val useOriginalTitle = LocalSubjectAppearanceSettings.current.useOriginalTitle
    val title = if (useOriginalTitle) info.originalTitle else info.title
    var nsfwMode by rememberSaveable(info.subjectId) { mutableStateOf(info.nsfwMode) }
    Box(Modifier.testTag(AppleSearchTestTags.result(info.subjectId))) {
        NsfwMask(nsfwMode, onTemporarilyDisplay = { nsfwMode = NsfwMode.DISPLAY }, shape = RectangleShape) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(role = Role.Button, onClick = onClick)
                    .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppleCover(CoverAspect.Portrait3x4, title, Modifier.width(56.dp), cornerRadius = 8.dp) {
                    AsyncImage(info.imageUrl, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        highlightKeywords(title, keywords, MaterialTheme.colorScheme.primary),
                        style = AniTextStyles.body,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (info.airingText.isNotBlank()) {
                        Text(
                            info.airingText,
                            style = AniTextStyles.footnote,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    if (info.genreText.isNotBlank()) {
                        Text(
                            info.genreText,
                            style = AniTextStyles.caption,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (quickFollowState != null) {
                    QuickFollowAnchor(quickFollowState, info.subjectId) { followed, enabled, onClickFollow ->
                        AppleFollowButton(
                            followed = followed,
                            onClick = onClickFollow,
                            contentDescription = if (followed) {
                                stringResource(Lang.exploration_quick_follow_followed, title)
                            } else {
                                stringResource(Lang.exploration_quick_follow, title)
                            },
                            Modifier.testTag(AppleSearchTestTags.follow(info.subjectId)),
                            enabled = enabled,
                        )
                    }
                }
            }
        }
        if (showDivider) {
            AppleDivider(Modifier.align(Alignment.TopStart), startIndent = 84.dp)
        }
    }
}

/**
 * 标题中命中 [keywords] 的部分 (不分大小写; 以空白分隔的每个词) 改为 accent W700.
 */
private fun highlightKeywords(
    title: String,
    keywords: String,
    color: Color,
): AnnotatedString = buildAnnotatedString {
    append(title)
    for (range in keywordRanges(title, keywords)) {
        addStyle(SpanStyle(color = color, fontWeight = FontWeight.Bold), range.first, range.last + 1)
    }
}

/**
 * [text] 中命中 [keywords] 的范围, 不分大小写; [keywords] 以空白分隔为多个词. 重叠或相邻的范围合并, 按位置排序.
 */
internal fun keywordRanges(text: String, keywords: String): List<IntRange> {
    val words = keywords.split(Regex("\\s+")).filter { it.isNotEmpty() }
    val ranges = buildList {
        for (word in words) {
            var start = text.indexOf(word, ignoreCase = true)
            while (start >= 0) {
                add(start until start + word.length)
                start = text.indexOf(word, start + 1, ignoreCase = true)
            }
        }
    }.sortedBy { it.first }
    return buildList {
        for (range in ranges) {
            val last = lastOrNull()
            if (last != null && range.first <= last.last + 1) {
                set(lastIndex, last.first..maxOf(last.last, range.last))
            } else {
                add(range)
            }
        }
    }
}

/**
 * C5 列表卡的一列: 第一列圆上角, 最后一列圆下角.
 */
private fun listCardShape(index: Int, count: Int): Shape {
    val top = if (index == 0) 16.dp else 0.dp
    val bottom = if (index == count - 1) 16.dp else 0.dp
    if (top == 0.dp && bottom == 0.dp) return RectangleShape
    return RoundedCornerShape(topStart = top, topEnd = top, bottomStart = bottom, bottomEnd = bottom)
}

internal object AppleSearchTestTags {
    const val Field = "search-field"
    const val Clear = "search-clear"
    const val Cancel = "search-cancel"
    const val Suggestions = "search-suggestions"
    const val Sort = "search-filter-sort"
    const val Year = "search-filter-year"
    const val Season = "search-filter-season"
    fun sortOption(sort: SearchSort) = "search-sort-$sort"
    fun tagKind(kind: CanonicalTagKind?) = "search-filter-tag-${kind ?: "custom"}"
    fun result(subjectId: Int) = "search-result-$subjectId"
    fun follow(subjectId: Int) = "search-follow-$subjectId"
}
