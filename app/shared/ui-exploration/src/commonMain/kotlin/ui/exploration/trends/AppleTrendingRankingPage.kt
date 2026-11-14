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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import me.him188.ani.app.data.models.preference.NsfwMode
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.ui.exploration.follow.QuickFollowAnchor
import me.him188.ani.app.ui.exploration.follow.QuickFollowState
import me.him188.ani.app.ui.exploration.search.LoadingIndicator
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleFollowButton
import me.him188.ani.app.ui.foundation.apple.PushedPageHeader
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.widgets.NsfwMask
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.exploration_quick_follow
import me.him188.ani.app.ui.lang.exploration_quick_follow_followed
import me.him188.ani.app.ui.lang.exploration_trending_ranking
import me.him188.ani.app.ui.lang.exploration_trending_ranking_description
import me.him188.ani.app.ui.lang.exploration_trending_ranking_empty
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.search.isFinishedAndEmpty
import me.him188.ani.app.ui.search.isLoadingFirstPageOrRefreshing
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版热度排行 (03 §4.1). 手机推入页.
 *
 * 顶部是 C3 返回钮与标题「熱度排行」, 下距 4 是说明「依近 30 天在看人數排序」. 列表在 C5 卡片里, 每列为名次、封面、
 * 番名、放送季度与话数、「近 30 天 N 人在看」, 右侧为快速追番钮 (05 G2). 点击一列打开条目详情.
 *
 * @param quickFollowState 为 `null` 时不显示快速追番钮 (未登录).
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleTrendingRankingPage(
    items: LazyPagingItems<TrendingRankingItemPresentation>,
    quickFollowState: QuickFollowState?,
    onClickItem: (TrendingRankingItemPresentation) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = contentPadding,
    ) {
        item(key = "header") {
            Column(Modifier.padding(bottom = 16.dp)) {
                PushedPageHeader(stringResource(Lang.exploration_trending_ranking), onBack = onBack, windowInsets = windowInsets)
                Text(
                    stringResource(Lang.exploration_trending_ranking_description),
                    Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp).testTag(AppleTrendingTestTags.Description),
                    style = AniTextStyles.footnote,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        if (items.loadState.hasError) {
            item(key = "error") {
                LoadErrorCard(
                    LoadError.fromCombinedLoadStates(items.loadState),
                    onRetry = { items.retry() },
                    Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
                )
            }
        }
        if (items.itemCount == 0 && items.isLoadingFirstPageOrRefreshing) {
            item(key = "loading") { LoadingIndicator() }
        }
        if (items.isFinishedAndEmpty) {
            item(key = "empty") {
                Text(
                    stringResource(Lang.exploration_trending_ranking_empty),
                    Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    style = AniTextStyles.footnote,
                    color = MaterialTheme.colorScheme.secondary,
                    textAlign = TextAlign.Center,
                )
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
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(listCardShape(index, items.itemCount))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                if (item == null) {
                    Box(Modifier.fillMaxWidth().height(AppleTrendingDefaults.RowHeight))
                } else {
                    TrendingRow(item, quickFollowState, onClick = { onClickItem(item) })
                }
                if (index > 0) {
                    AppleDivider(Modifier.align(Alignment.TopStart), startIndent = 116.dp)
                }
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

/**
 * 排行的一列: 内距 `12 12 12 8`; 名次栏宽 28; 封面 56×75 圆角 8; 番名 `body` W600、放送季度与话数 `footnote` label2、
 * 「近 30 天 N 人在看」`footnote` labelSoft (前面是中性色图标); 右侧 36 追番圆钮. 分隔线从 116 开始.
 */
@Composable
private fun TrendingRow(
    item: TrendingRankingItemPresentation,
    quickFollowState: QuickFollowState?,
    onClick: () -> Unit,
) {
    val subject = item.subject
    val title = if (LocalSubjectAppearanceSettings.current.useOriginalTitle) subject.originalTitle else subject.title
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 8.dp, top = 12.dp, end = 12.dp, bottom = 12.dp)
            .testTag(AppleTrendingTestTags.row(subject.subjectId)),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TrendingRankText(item.rank, Modifier.width(28.dp))
        var nsfwMode by rememberSaveable(subject.subjectId) { mutableStateOf(subject.nsfwMode) }
        NsfwMask(
            nsfwMode,
            onTemporarilyDisplay = { nsfwMode = NsfwMode.DISPLAY },
            shape = RoundedCornerShape(8.dp),
        ) {
            AsyncImage(
                subject.imageUrl,
                contentDescription = null,
                Modifier
                    .size(56.dp, 75.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentScale = ContentScale.Crop,
            )
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                title,
                style = AniTextStyles.body,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subject.airingText,
                style = AniTextStyles.footnote,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    Modifier.size(14.dp),
                    tint = AniColors.labelSoft,
                )
                Text(
                    renderTrendingHeatLine(item.heat),
                    style = AniTextStyles.footnote,
                    color = AniColors.labelSoft,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (quickFollowState != null) {
            QuickFollowAnchor(quickFollowState, subject.subjectId) { followed, enabled, onClickFollow ->
                AppleFollowButton(
                    followed = followed,
                    onClick = onClickFollow,
                    contentDescription = if (followed) {
                        stringResource(Lang.exploration_quick_follow_followed, title)
                    } else {
                        stringResource(Lang.exploration_quick_follow, title)
                    },
                    Modifier.testTag(AppleTrendingTestTags.follow(subject.subjectId)),
                    enabled = enabled,
                )
            }
        }
    }
}

/**
 * 名次: 等宽数字, 置中. 前 3 名 20 W800 label, 第 4 名起 16 W600 label2. 不用彩色徽章. 手机与 macOS 相同.
 */
@Composable
internal fun TrendingRankText(rank: Int, modifier: Modifier = Modifier) {
    val top = rank <= 3
    val style = if (top) {
        AniTextStyles.title.copy(fontWeight = FontWeight.ExtraBold)
    } else {
        AniTextStyles.body.copy(fontWeight = FontWeight.SemiBold)
    }
    Text(
        rank.toString(),
        modifier.testTag(AppleTrendingTestTags.rank(rank)),
        style = style.copy(fontFeatureSettings = "tnum"),
        color = if (top) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary,
        textAlign = TextAlign.Center,
        maxLines = 1,
    )
}

internal object AppleTrendingDefaults {
    /** 列高: 封面 75 + 上下内距 12. */
    val RowHeight = 99.dp
}

internal object AppleTrendingTestTags {
    const val Description = "apple-trending-description"
    fun row(subjectId: Int) = "apple-trending-row-$subjectId"
    fun follow(subjectId: Int) = "apple-trending-follow-$subjectId"
    fun rank(rank: Int) = "apple-trending-rank-$rank"
}
