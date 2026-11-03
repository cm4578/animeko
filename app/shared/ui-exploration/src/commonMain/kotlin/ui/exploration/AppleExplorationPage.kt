/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItemsWithLifecycle
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import me.him188.ani.app.data.models.recommend.RecommendedItemInfo
import me.him188.ani.app.data.models.recommend.RecommendedSubjectInfo
import me.him188.ani.app.data.models.recommend.preferredDisplayName
import me.him188.ani.app.data.models.trending.TrendingSubjectInfo
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.navigation.SubjectDetailPlaceholder
import me.him188.ani.app.ui.exploration.follow.QuickFollowAnchor
import me.him188.ani.app.ui.exploration.follow.QuickFollowState
import me.him188.ani.app.ui.exploration.schedule.AppleWeekdayStrip
import me.him188.ani.app.ui.exploration.schedule.currentWeek
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleCircleButton
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.LargeTitleHeader
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.exploration_quick_follow
import me.him188.ani.app.ui.lang.exploration_quick_follow_followed
import me.him188.ani.app.ui.lang.exploration_recommendations
import me.him188.ani.app.ui.lang.exploration_schedule
import me.him188.ani.app.ui.lang.exploration_schedule_full
import me.him188.ani.app.ui.lang.exploration_schedule_today
import me.him188.ani.app.ui.lang.exploration_search
import me.him188.ani.app.ui.lang.exploration_title
import me.him188.ani.app.ui.lang.exploration_trending
import me.him188.ani.app.ui.lang.exploration_trending_more
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.user.SelfInfoUiState
import me.him188.ani.utils.analytics.Analytics
import me.him188.ani.utils.analytics.AnalyticsEvent.Companion.SubjectEnter
import me.him188.ani.utils.analytics.recordEvent
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

/**
 * Apple 版探索页 (03 §3.2). 手机.
 *
 * 从上到下: C2 大标题「探索」与搜索钮; 最高热度 (横向卡片); 新番时间表 (本周星期条); 推荐 (3 栏网格).
 * 页头不放头像与设置入口 (05 G14、B1), 个人入口在「我的」分页. 热度与推荐资料没有类型, 卡片只显示番名.
 * 推荐卡片右上角是快速追番按钮 (05 G2), 未登录时不显示.
 *
 * @param quickFollowState 推荐卡片的快速追番, 见 [ExplorationPageViewModel.quickFollowState].
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleExplorationPage(
    state: ExplorationPageState,
    quickFollowState: QuickFollowState,
    selfInfo: SelfInfoUiState,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val navigator = LocalNavigator.current
    // 每次进入探索分页时重新读取今天.
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    AppleExplorationPageLayout(
        trending = state.trendingSubjectInfoPager,
        recommendations = state.recommendationPager.collectAsLazyPagingItemsWithLifecycle(),
        today = today,
        quickFollowState = quickFollowState.takeIf { selfInfo.isSessionValid == true },
        onSearch = onSearch,
        onClickTrending = {
            Analytics.recordEvent(SubjectEnter) {
                put("source", "home_trending")
                put("subject_id", it.bangumiId)
            }
            navigator.navigateSubjectDetails(
                subjectId = it.bangumiId,
                placeholder = SubjectDetailPlaceholder(id = it.bangumiId, name = it.nameCn, coverUrl = it.imageLarge),
            )
        },
        onClickTrendingMore = { navigator.navigateTrendingRanking() },
        onClickSchedule = { navigator.navigateSchedule(it) },
        onClickRecommendation = {
            Analytics.recordEvent(SubjectEnter) {
                put("source", "home_recommendation")
                put("subject_id", it.bangumiId)
            }
            navigator.navigateSubjectDetails(subjectId = it.bangumiId, placeholder = it.toNavPlaceholder())
        },
        modifier = modifier,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
    )
}

/**
 * [AppleExplorationPage] 的版面, 不持有状态.
 *
 * @param today 星期条以今天所在的一周 (周一至周日) 显示, 今天为 tint 底.
 * @param quickFollowState 为 `null` 时推荐卡片不显示快速追番按钮.
 * @param onClickSchedule 打开新番时间表. 参数为选中的日期, `null` 为今天.
 */
@Composable
internal fun AppleExplorationPageLayout(
    trending: LazyPagingItems<TrendingSubjectInfo>,
    recommendations: LazyPagingItems<RecommendedItemInfo>,
    today: LocalDate,
    quickFollowState: QuickFollowState?,
    onSearch: () -> Unit,
    onClickTrending: (TrendingSubjectInfo) -> Unit,
    onClickTrendingMore: () -> Unit,
    onClickSchedule: (date: LocalDate?) -> Unit,
    onClickRecommendation: (RecommendedSubjectInfo) -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyColumn(modifier.fillMaxSize(), contentPadding = contentPadding) {
        item(key = "header") {
            LargeTitleHeader(stringResource(Lang.exploration_title), windowInsets = windowInsets) {
                AppleCircleButton(onSearch, AppleIcons.search(2.2f), stringResource(Lang.exploration_search))
            }
        }

        item(key = "trending") {
            Column(Modifier.padding(bottom = 24.dp)) {
                SectionHeader(
                    stringResource(Lang.exploration_trending),
                    action = stringResource(Lang.exploration_trending_more),
                    onAction = onClickTrendingMore,
                )
                Spacer(Modifier.height(12.dp))
                TrendingRow(trending, onClickTrending)
            }
        }

        item(key = "schedule") {
            Column(Modifier.padding(bottom = 24.dp)) {
                SectionHeader(
                    stringResource(Lang.exploration_schedule),
                    action = stringResource(Lang.exploration_schedule_full),
                    onAction = { onClickSchedule(null) },
                )
                Spacer(Modifier.height(12.dp))
                WeekScheduleCard(today, onClickSchedule)
            }
        }

        item(key = "recommendations-header") {
            Column(Modifier.padding(bottom = 12.dp)) {
                SectionHeader(stringResource(Lang.exploration_recommendations))
                if (recommendations.loadState.hasError) {
                    LoadErrorCard(
                        LoadError.fromCombinedLoadStates(recommendations.loadState),
                        onRetry = { recommendations.retry() },
                        Modifier.padding(horizontal = 16.dp).padding(top = 12.dp),
                    )
                }
            }
        }
        recommendationRows(recommendations, quickFollowState, onClickRecommendation)
    }
}

/**
 * 区块标题 `title` 与右侧 accent 文字链接. 标题行左右内缩 20 (页面内距 16 + 4).
 */
@Composable
private fun SectionHeader(
    title: String,
    action: String? = null,
    onAction: () -> Unit = {},
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Text(
            title,
            Modifier.weight(1f).alignByBaseline(),
            style = AniTextStyles.title,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (action != null) {
            Text(
                action,
                Modifier
                    .alignByBaseline()
                    .clickable(role = Role.Button, onClick = onAction)
                    .padding(start = 12.dp),
                style = AniTextStyles.callout,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
    }
}

/**
 * 最高热度: 横向卡片 240×180, 圆角 12, 间距 12. 不放名次标签, 靠排列顺序表达.
 */
@Composable
private fun TrendingRow(
    items: LazyPagingItems<TrendingSubjectInfo>,
    onClick: (TrendingSubjectInfo) -> Unit,
) {
    if (items.itemCount == 0 && items.loadState.hasError) {
        LoadErrorCard(
            LoadError.fromCombinedLoadStates(items.loadState),
            onRetry = { items.retry() },
            Modifier.padding(horizontal = 16.dp),
        )
        return
    }
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (items.itemCount == 0) {
            // 首次加载时的占位卡片
            items(3) { TrendingCardFrame {} }
        }
        items(items.itemCount) { index ->
            val item = items[index] ?: return@items
            TrendingCardFrame(Modifier.clickable { onClick(item) }) {
                AsyncImage(item.imageLarge, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                Box(
                    Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .fillMaxHeight(0.64f)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.64f)))),
                )
                Text(
                    item.nameCn,
                    Modifier.align(Alignment.BottomStart).padding(start = 14.dp, end = 14.dp, bottom = 12.dp),
                    style = AniTextStyles.headline,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun TrendingCardFrame(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        Modifier
            .size(240.dp, 180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .then(modifier),
        content = content,
    )
}

/**
 * 新番时间表卡片: card 圆角 16. 上排为本周 (周一至周日) 7 格星期条, 今天为 tint 底; 分隔线; 底部「查看今天更新的番剧 ›」.
 *
 * 点星期格打开时间表并选中那天; 点底部一列打开时间表的今天.
 */
@Composable
private fun WeekScheduleCard(
    today: LocalDate,
    onClickSchedule: (date: LocalDate?) -> Unit,
) {
    val week = remember(today) { currentWeek(today) }
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        AppleWeekdayStrip(
            week,
            selectedDate = today,
            onClickDay = { onClickSchedule(it) },
            Modifier.padding(8.dp),
            cellModifier = { Modifier.testTag(AppleExplorationTestTags.weekday(it)) },
        )
        AppleDivider(startIndent = 16.dp)
        Row(
            Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clickable(role = Role.Button) { onClickSchedule(null) }
                .padding(horizontal = 16.dp)
                .testTag(AppleExplorationTestTags.ScheduleToday),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Lang.exploration_schedule_today),
                Modifier.weight(1f),
                style = AniTextStyles.callout,
                color = AniColors.labelSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                AppleIcons.chevronRight(),
                contentDescription = null,
                Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

/**
 * 推荐: 3 栏网格, 间距 8.
 */
private fun LazyListScope.recommendationRows(
    items: LazyPagingItems<RecommendedItemInfo>,
    quickFollowState: QuickFollowState?,
    onClick: (RecommendedSubjectInfo) -> Unit,
) {
    val rows = (items.itemCount + RecommendationColumns - 1) / RecommendationColumns
    items(rows, key = { "recommendation-row-$it" }) { row ->
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = if (row == rows - 1) 0.dp else 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            for (column in 0 until RecommendationColumns) {
                val index = row * RecommendationColumns + column
                Box(Modifier.weight(1f)) {
                    val item = if (index < items.itemCount) items[index] else null
                    when (item) {
                        is RecommendedSubjectInfo -> RecommendationCard(item, quickFollowState, onClick = { onClick(item) })
                        null -> {}
                    }
                }
            }
        }
    }
}

private const val RecommendationColumns = 3

/**
 * 推荐卡片: 圆角 12、card 底; 封面 3:4; 文字区内距 `8 10 10`, 番名 `subhead` W600. 右上角为快速追番按钮.
 */
@Composable
private fun RecommendationCard(
    item: RecommendedSubjectInfo,
    quickFollowState: QuickFollowState?,
    onClick: () -> Unit,
) {
    val title = item.preferredDisplayName(LocalSubjectAppearanceSettings.current.useOriginalTitle)
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            AsyncImage(item.imageLarge, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            if (quickFollowState != null) {
                QuickFollowAnchor(
                    quickFollowState,
                    item.bangumiId,
                    Modifier.align(Alignment.TopEnd).padding(top = 2.dp, end = 2.dp),
                ) { followed, enabled, onClickFollow ->
                    CoverFollowButton(
                        followed = followed,
                        enabled = enabled,
                        onClick = onClickFollow,
                        contentDescription = if (followed) {
                            stringResource(Lang.exploration_quick_follow_followed, title)
                        } else {
                            stringResource(Lang.exploration_quick_follow, title)
                        },
                        Modifier.testTag(AppleExplorationTestTags.follow(item.bangumiId)),
                    )
                }
            }
        }
        Text(
            title,
            Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 10.dp),
            style = AniTextStyles.subhead,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 封面上的快速追番按钮: 点击区 36, 内为 26 圆 (阴影 `0 1 3` 黑 18%). 未追番 card 底 + accent 加号; 已追番 tint 底 + accent 勾.
 */
@Composable
private fun CoverFollowButton(
    followed: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier
            .size(36.dp)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
                selected = followed
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(26.dp)
                .dropShadow(
                    CircleShape,
                    Shadow(radius = 3.dp, color = Color.Black.copy(alpha = 0.18f), offset = DpOffset(0.dp, 1.dp)),
                )
                .clip(CircleShape)
                .background(if (followed) colorScheme.secondaryContainer else colorScheme.surfaceContainerLow),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (followed) AppleIcons.check(2.6f) else AppleIcons.plus(2.6f),
                contentDescription = null,
                Modifier.size(14.dp),
                tint = colorScheme.primary,
            )
        }
    }
}

internal object AppleExplorationTestTags {
    const val ScheduleToday = "exploration-schedule-today"
    fun weekday(date: LocalDate) = "exploration-weekday-$date"
    fun follow(subjectId: Int) = "exploration-follow-$subjectId"
}
