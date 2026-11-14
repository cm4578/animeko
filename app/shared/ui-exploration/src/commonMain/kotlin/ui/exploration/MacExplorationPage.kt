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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItemsWithLifecycle
import kotlin.time.Clock
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
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
import me.him188.ani.app.ui.exploration.schedule.AiringScheduleItemPresentation
import me.him188.ani.app.ui.exploration.schedule.AppleScheduleRow
import me.him188.ani.app.ui.exploration.schedule.AppleWeekdayStrip
import me.him188.ani.app.ui.exploration.schedule.ScheduleItemDefaults
import me.him188.ani.app.ui.exploration.schedule.SchedulePagePresentation
import me.him188.ani.app.ui.exploration.schedule.ScheduleViewModel
import me.him188.ani.app.ui.exploration.schedule.appleScheduleRows
import me.him188.ani.app.ui.exploration.schedule.currentWeek
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.MacKeyboardShortcut
import me.him188.ani.app.ui.foundation.apple.MacLargeTitleHeader
import me.him188.ani.app.ui.foundation.apple.MacPageToolbar
import me.him188.ani.app.ui.foundation.apple.MacToolbarIconButton
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.exploration_quick_follow
import me.him188.ani.app.ui.lang.exploration_quick_follow_followed
import me.him188.ani.app.ui.lang.exploration_recommendations
import me.him188.ani.app.ui.lang.exploration_refresh
import me.him188.ani.app.ui.lang.exploration_schedule
import me.him188.ani.app.ui.lang.exploration_schedule_day_empty
import me.him188.ani.app.ui.lang.exploration_schedule_full
import me.him188.ani.app.ui.lang.exploration_schedule_time_unknown
import me.him188.ani.app.ui.lang.exploration_schedule_today_count
import me.him188.ani.app.ui.lang.exploration_title
import me.him188.ani.app.ui.lang.exploration_trending
import me.him188.ani.app.ui.lang.exploration_trending_more
import me.him188.ani.app.ui.lang.exploration_trending_next
import me.him188.ani.app.ui.lang.exploration_trending_previous
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.user.SelfInfoUiState
import me.him188.ani.utils.analytics.Analytics
import me.him188.ani.utils.analytics.AnalyticsEvent.Companion.SubjectEnter
import me.him188.ani.utils.analytics.recordEvent
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 版探索页 (04 §3.3).
 *
 * 工具栏右侧为重新整理 (⌘R, 05 C2). 内容由上到下: 大标题「探索」; 最高热度 (4 栏卡片, 左右滚动钮);
 * 下方双栏: 左为推荐 (5 栏网格), 右为 320 宽的新番时间表小工具 (今天, 05 G4).
 * 热度与推荐资料没有类型, 卡片只显示番名. 推荐卡片右上角是快速追番按钮 (05 G2), 未登录时不显示.
 *
 * @param schedule 新番时间表的资料, 见 [ScheduleViewModel.presentationFlow].
 * @param onRefreshSchedule 重新请求时间表. 重新整理 (⌘R) 时与热度、推荐一起刷新.
 */
@Composable
fun MacExplorationPage(
    state: ExplorationPageState,
    quickFollowState: QuickFollowState,
    selfInfo: SelfInfoUiState,
    schedule: SchedulePagePresentation,
    onRefreshSchedule: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigator = LocalNavigator.current
    val trending = state.trendingSubjectInfoPager
    val recommendations = state.recommendationPager.collectAsLazyPagingItemsWithLifecycle()
    // 每次进入探索分页时重新读取今天.
    val today = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    MacExplorationPageLayout(
        trending = trending,
        recommendations = recommendations,
        today = today,
        schedule = schedule,
        quickFollowState = quickFollowState.takeIf { selfInfo.isSessionValid == true },
        onRefresh = {
            trending.refresh()
            recommendations.refresh()
            onRefreshSchedule()
        },
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
        onClickScheduleItem = {
            navigator.navigateSubjectDetails(
                it.subjectId,
                placeholder = SubjectDetailPlaceholder(
                    id = it.subjectId,
                    name = it.subjectOriginalTitle,
                    nameCN = it.subjectTitle,
                    coverUrl = it.imageUrl,
                ),
            )
        },
        onClickRecommendation = {
            Analytics.recordEvent(SubjectEnter) {
                put("source", "home_recommendation")
                put("subject_id", it.bangumiId)
            }
            navigator.navigateSubjectDetails(subjectId = it.bangumiId, placeholder = it.toNavPlaceholder())
        },
        modifier = modifier,
    )
}

/**
 * [MacExplorationPage] 的版面, 不持有状态.
 *
 * 整页纵向滚动; 推荐在接近底部时加载下一页.
 *
 * @param today 星期条以今天所在的一周显示, 今天为 tint 底; 小工具列出今天的番剧.
 * @param onClickSchedule 打开新番时间表. 参数为选中的日期, `null` 为今天.
 */
@Composable
internal fun MacExplorationPageLayout(
    trending: LazyPagingItems<TrendingSubjectInfo>,
    recommendations: LazyPagingItems<RecommendedItemInfo>,
    today: LocalDate,
    schedule: SchedulePagePresentation,
    quickFollowState: QuickFollowState?,
    onRefresh: () -> Unit,
    onClickTrending: (TrendingSubjectInfo) -> Unit,
    onClickTrendingMore: () -> Unit,
    onClickSchedule: (date: LocalDate?) -> Unit,
    onClickScheduleItem: (AiringScheduleItemPresentation) -> Unit,
    onClickRecommendation: (RecommendedSubjectInfo) -> Unit,
    modifier: Modifier = Modifier,
) {
    MacKeyboardShortcut(Key.R, onTrigger = onRefresh)
    val scrollState = rememberScrollState()
    // 接近底部时读取推荐的最后一项, 让 Paging 加载下一页. 版面用 peek 读取, 不会触发加载.
    LaunchedEffect(scrollState, recommendations) {
        snapshotFlow { recommendations.itemCount > 0 && scrollState.value >= scrollState.maxValue - LoadMoreThresholdPx }
            .distinctUntilChanged()
            .filter { it }
            .collect { recommendations[recommendations.itemCount - 1] }
    }

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        MacPageToolbar {
            MacToolbarIconButton(
                onClick = onRefresh,
                icon = AppleIcons.refresh(),
                contentDescription = stringResource(Lang.exploration_refresh),
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(start = 32.dp, top = 4.dp, end = 32.dp, bottom = 32.dp),
        ) {
            MacLargeTitleHeader(stringResource(Lang.exploration_title))
            Spacer(Modifier.height(24.dp))
            MacTrendingSection(trending, onClickTrending, onClickTrendingMore)
            Spacer(Modifier.height(28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(1f)) {
                    MacSectionHeader(stringResource(Lang.exploration_recommendations))
                    Spacer(Modifier.height(12.dp))
                    MacRecommendationGrid(recommendations, quickFollowState, onClickRecommendation)
                }
                Column(Modifier.width(MacScheduleWidgetWidth)) {
                    MacSectionHeader(stringResource(Lang.exploration_schedule)) {
                        MacLink(stringResource(Lang.exploration_schedule_full), onClick = { onClickSchedule(null) })
                    }
                    Spacer(Modifier.height(12.dp))
                    MacScheduleWidget(today, schedule, onClickSchedule, onClickScheduleItem, onRetry = onRefresh)
                }
            }
        }
    }
}

private const val LoadMoreThresholdPx = 600
private val MacScheduleWidgetWidth = 320.dp

/**
 * 区块标题 17/22 W700, 右侧放 [trailing].
 */
@Composable
private fun MacSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
) {
    Row(
        modifier.fillMaxWidth().height(28.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            Modifier.weight(1f),
            style = AniTextStyles.macTitle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        trailing()
    }
}

/**
 * accent 文字链接 (`macCallout`).
 */
@Composable
private fun MacLink(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text,
        modifier.clip(RoundedCornerShape(4.dp)).clickable(role = Role.Button, onClick = onClick),
        style = AniTextStyles.macCallout,
        color = MaterialTheme.colorScheme.primary,
        maxLines = 1,
    )
}

/**
 * 最高热度: 标题行 (「更多」+ 左右滚动钮, 到头时不透明度 0.35); 4 栏等宽 4:3 卡片, 间距 16, 横向滚动.
 * 左右钮一次滚动 4 张, 取代现有的「按住 Shift + 滚轮」提示.
 */
@Composable
private fun MacTrendingSection(
    items: LazyPagingItems<TrendingSubjectInfo>,
    onClick: (TrendingSubjectInfo) -> Unit,
    onClickMore: () -> Unit,
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    MacSectionHeader(stringResource(Lang.exploration_trending)) {
        MacLink(stringResource(Lang.exploration_trending_more), onClick = onClickMore)
        Spacer(Modifier.width(4.dp))
        MacToolbarIconButton(
            onClick = {
                scope.launch {
                    listState.animateScrollToItem((listState.firstVisibleItemIndex - TrendingColumns).coerceAtLeast(0))
                }
            },
            icon = AppleIcons.chevronLeft(),
            contentDescription = stringResource(Lang.exploration_trending_previous),
            enabled = listState.canScrollBackward,
        )
        MacToolbarIconButton(
            onClick = {
                scope.launch { listState.animateScrollToItem(listState.firstVisibleItemIndex + TrendingColumns) }
            },
            icon = AppleIcons.chevronRight(),
            contentDescription = stringResource(Lang.exploration_trending_next),
            enabled = listState.canScrollForward,
        )
    }
    Spacer(Modifier.height(12.dp))
    if (items.itemCount == 0 && items.loadState.hasError) {
        LoadErrorCard(LoadError.fromCombinedLoadStates(items.loadState), onRetry = { items.retry() })
        return
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val cardWidth = (maxWidth - 16.dp * (TrendingColumns - 1)) / TrendingColumns
        LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            if (items.itemCount == 0) {
                // 首次加载时的占位卡片
                items(TrendingColumns) { TrendingCard(Modifier.width(cardWidth)) {} }
            }
            items(items.itemCount) { index ->
                val item = items[index] ?: return@items
                TrendingCard(Modifier.width(cardWidth).clickable { onClick(item) }) {
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
                        style = AniTextStyles.macBody.copy(fontSize = 16.sp, lineHeight = 22.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

private const val TrendingColumns = 4

@Composable
private fun TrendingCard(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        content = content,
    )
}

/**
 * 推荐: 5 栏网格, 间距 12. 只排已加载的项目.
 */
@Composable
private fun MacRecommendationGrid(
    items: LazyPagingItems<RecommendedItemInfo>,
    quickFollowState: QuickFollowState?,
    onClick: (RecommendedSubjectInfo) -> Unit,
) {
    if (items.loadState.hasError && items.itemCount == 0) {
        LoadErrorCard(LoadError.fromCombinedLoadStates(items.loadState), onRetry = { items.retry() })
        return
    }
    val rows = (items.itemCount + RecommendationColumns - 1) / RecommendationColumns
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (row in 0 until rows) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (column in 0 until RecommendationColumns) {
                    val index = row * RecommendationColumns + column
                    Box(Modifier.weight(1f)) {
                        when (val item = if (index < items.itemCount) items.peek(index) else null) {
                            is RecommendedSubjectInfo -> MacRecommendationCard(item, quickFollowState) { onClick(item) }
                            null -> {}
                        }
                    }
                }
            }
        }
        if (items.loadState.append is LoadState.Loading) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

private const val RecommendationColumns = 5

/**
 * 推荐卡片: card 底、圆角 12; 封面 3:4, 右上 32 点击区内放 24 圆形快速追番钮; 文字区内距 8/10/10, 番名 13/600.
 */
@Composable
private fun MacRecommendationCard(
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
                        hitSize = 32.dp,
                        circleSize = 24.dp,
                    )
                }
            }
        }
        Text(
            title,
            Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 10.dp),
            style = AniTextStyles.macCallout,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 新番时间表小工具 (05 G4): card 圆角 14; 顶部本周 7 栏星期条 (内距 8, 每格高 48, 今天 tint 底); 分隔线;
 * 今天的番剧 (行高 44, 内距 0 16: 时间 44 宽 13/600 + 番名 13/600 + 集数 12 label2); 分隔线;
 * 底部「查看今天更新的 N 部番劇 ›」高 40.
 *
 * 点星期格打开时间表并选中那天, 点番剧打开条目详情, 点底部一列打开时间表的今天.
 */
@Composable
private fun MacScheduleWidget(
    today: LocalDate,
    schedule: SchedulePagePresentation,
    onClickSchedule: (date: LocalDate?) -> Unit,
    onClickItem: (AiringScheduleItemPresentation) -> Unit,
    onRetry: () -> Unit,
) {
    val week = remember(today) { currentWeek(today) }
    val rows = remember(schedule, today) {
        val items = schedule.airingSchedules.firstOrNull { it.date == today }?.episodes.orEmpty()
        appleScheduleRows(items, today, today).filterIsInstance<AppleScheduleRow.Item>()
    }
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        AppleWeekdayStrip(
            week,
            selectedDate = today,
            onClickDay = { onClickSchedule(it) },
            Modifier.padding(8.dp),
            cellModifier = { Modifier.testTag(AppleExplorationTestTags.weekday(it)) },
        )
        AppleDivider()
        when {
            schedule.isPlaceholder -> Box(Modifier.fillMaxWidth().height(88.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(Modifier.size(24.dp))
            }

            schedule.error != null -> LoadErrorCard(schedule.error, onRetry = onRetry, Modifier.padding(12.dp))

            rows.isEmpty() -> Text(
                stringResource(Lang.exploration_schedule_day_empty),
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                style = AniTextStyles.macCallout,
                color = MaterialTheme.colorScheme.secondary,
            )

            else -> Column {
                for (row in rows) {
                    MacScheduleWidgetRow(row, onClick = { onClickItem(row.item) })
                }
            }
        }
        AppleDivider()
        Row(
            Modifier
                .fillMaxWidth()
                .height(40.dp)
                .clickable(role = Role.Button) { onClickSchedule(null) }
                .padding(horizontal = 16.dp)
                .testTag(AppleExplorationTestTags.ScheduleToday),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                stringResource(Lang.exploration_schedule_today_count, rows.size),
                Modifier.weight(1f),
                style = AniTextStyles.macCallout,
                color = AniColors.labelSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                AppleIcons.chevronRight(2.2f),
                contentDescription = null,
                Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

/**
 * 小工具的一行: 时间 (已播出 label2, 未播出 label; 时间未定为 11 号「時間未定」)、番名、集数.
 * 与上一项时间相同时不重复显示时间.
 */
@Composable
private fun MacScheduleWidgetRow(
    row: AppleScheduleRow.Item,
    onClick: () -> Unit,
) {
    val item = row.item
    val colorScheme = MaterialTheme.colorScheme
    val useOriginal = LocalSubjectAppearanceSettings.current.useOriginalTitle
    Row(
        Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.width(44.dp)) {
            if (row.showTime) {
                val time: LocalTime? = item.time
                Text(
                    time?.let { ScheduleItemDefaults.renderTime(null, it) }
                        ?: stringResource(Lang.exploration_schedule_time_unknown),
                    style = if (time == null) AniTextStyles.macSidebarSection else AniTextStyles.macCallout,
                    fontWeight = FontWeight.SemiBold,
                    color = if (row.aired) colorScheme.secondary else colorScheme.onSurface,
                    maxLines = 1,
                )
            }
        }
        Text(
            if (useOriginal) item.subjectOriginalTitle else item.subjectTitle,
            Modifier.weight(1f),
            style = AniTextStyles.macCallout,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            ScheduleItemDefaults.renderEpisode(item.episodeSort, item.episodeEp, null),
            style = AniTextStyles.macCaption,
            color = colorScheme.secondary,
            maxLines = 1,
        )
    }
}

