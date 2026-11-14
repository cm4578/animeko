/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.relations

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.him188.ani.app.data.models.subject.SubjectRelationGraph
import me.him188.ani.app.data.models.subject.SubjectRelationGraphBranch
import me.him188.ani.app.data.models.subject.SubjectRelationGraphMainNode
import me.him188.ani.app.data.models.subject.SubjectRelationGraphSubject
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.apple.AppleComponentDefaults
import me.him188.ani.app.ui.foundation.apple.AppleCover
import me.him188.ani.app.ui.foundation.apple.CollapsedTopBar
import me.him188.ani.app.ui.foundation.apple.CoverAspect
import me.him188.ani.app.ui.foundation.apple.PushedPageHeader
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.subject_details_air_month
import me.him188.ani.app.ui.lang.subject_relation_graph_episodes
import me.him188.ani.app.ui.lang.subject_relation_graph_label_current
import me.him188.ani.app.ui.lang.subject_relation_graph_show_less
import me.him188.ani.app.ui.lang.subject_relation_graph_show_more
import me.him188.ani.app.ui.lang.subject_relation_graph_title
import me.him188.ani.app.ui.lang.subject_relation_graph_truncated
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.subject.details.components.renderSubjectRelation
import org.jetbrains.compose.resources.stringResource

/**
 * iOS 系列关系图 (03 §4.6): 推入页, 主线条目排成竖直时间线, 相关条目挂在主线条目下.
 *
 * 顶部为 C3 推入页标题: 大标题是系列名, 下方是「系列關係圖 · 主線 N 部 · 其他 M 部」. 返回钮一行滚出后显示精简栏.
 * 加载中与加载失败时还不知道系列名, 大标题为「系列關係圖」.
 *
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleSubjectRelationGraphScreen(
    vm: SubjectRelationGraphViewModel,
    onClickSubject: (SubjectRelationGraphSubject) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    AppleSubjectRelationGraphLayout(state, vm::retry, onClickSubject, onBack, modifier, windowInsets, contentPadding)
}

@Composable
internal fun AppleSubjectRelationGraphLayout(
    state: SubjectRelationGraphUiState,
    onRetry: () -> Unit,
    onClickSubject: (SubjectRelationGraphSubject) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val background = MaterialTheme.colorScheme.background
    Box(modifier.fillMaxSize().background(background)) {
        val graph = state.graph
        if (graph == null) {
            Column(Modifier.fillMaxSize()) {
                PushedPageHeader(
                    stringResource(Lang.subject_relation_graph_title),
                    onBack = onBack,
                    windowInsets = windowInsets,
                )
                AppleRelationGraphLoadState(state.error, onRetry, Modifier.weight(1f).fillMaxWidth().padding(16.dp))
            }
            return@Box
        }

        val presentation = remember(graph) { SubjectRelationGraphPresentation(graph) }
        val listState = rememberRelationGraphListState(presentation)
        AppleRelationGraphTimeline(
            presentation,
            onClickSubject,
            listState,
            background,
            Modifier.fillMaxSize(),
            contentPadding = contentPadding,
        ) {
            Column(Modifier.padding(bottom = 16.dp)) {
                PushedPageHeader(presentation.seriesName, onBack = onBack, windowInsets = windowInsets)
                Text(
                    renderRelationGraphSubtitle(graph),
                    Modifier
                        .padding(start = 20.dp, top = 4.dp, end = 20.dp)
                        .testTag(AppleRelationGraphTestTags.Subtitle),
                    style = AniTextStyles.callout,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }

        val density = LocalDensity.current
        val collapsed by remember(listState, density, windowInsets) {
            derivedStateOf {
                // 标题列的返回钮滚出后显示精简栏
                val threshold = windowInsets.getTop(density) + with(density) { HeaderBackRowBottom.roundToPx() }
                listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > threshold
            }
        }
        if (collapsed) {
            CollapsedTopBar(
                presentation.seriesName,
                onBack,
                Modifier.testTag(AppleRelationGraphTestTags.CollapsedBar),
                windowInsets = windowInsets,
            )
        }
    }
}

/**
 * 加载中显示转圈, 加载失败显示现有的错误卡片 (可重试).
 */
@Composable
internal fun AppleRelationGraphLoadState(
    error: LoadError?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        if (error != null) {
            LoadErrorCard(
                error,
                onRetry,
                Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .testTag(AppleRelationGraphTestTags.Error),
            )
        } else {
            CircularProgressIndicator(Modifier.align(Alignment.Center).testTag(AppleRelationGraphTestTags.Loading))
        }
    }
}

/**
 * 时间线列表的滚动状态. 第 0 项是标题, 因此下标 currentMainIndex 是当前条目的前一部: 把它显示在顶部以保留上下文.
 * 当前条目在前两部时从标题开始显示.
 */
@Composable
internal fun rememberRelationGraphListState(presentation: SubjectRelationGraphPresentation): LazyListState =
    rememberLazyListState(initialFirstVisibleItemIndex = presentation.currentMainIndex.takeIf { it > 1 } ?: 0)

/**
 * 「系列關係圖 · 主線 N 部 · 其他 M 部」.
 */
@Composable
internal fun renderRelationGraphSubtitle(graph: SubjectRelationGraph): String =
    stringResource(Lang.subject_relation_graph_title) + " · " + renderRelationGraphSummary(graph)

/**
 * Apple 版的竖直时间线: 第 0 项为 [header], 之后每个主线条目一项: 左侧时间线, 右侧主线卡片与它的相关条目.
 * iOS 与 macOS 窄宽度共用.
 *
 * @param background 页面底色, 填在目前这部的圆圈内.
 * @param horizontalPadding 时间线与卡片的左右内距. [header] 不受影响.
 */
@Composable
internal fun AppleRelationGraphTimeline(
    presentation: SubjectRelationGraphPresentation,
    onClickSubject: (SubjectRelationGraphSubject) -> Unit,
    listState: LazyListState,
    background: Color,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    horizontalPadding: Dp = 16.dp,
    header: @Composable () -> Unit,
) {
    val graph = presentation.graph
    val colors = AppleRelationGraphDefaults.timelineColors(background)
    val layoutDirection = LocalLayoutDirection.current
    LazyColumn(
        modifier.testTag(AppleRelationGraphTestTags.List),
        listState,
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(layoutDirection),
            top = contentPadding.calculateTopPadding(),
            end = contentPadding.calculateEndPadding(layoutDirection),
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
    ) {
        item("header") { header() }
        itemsIndexed(graph.mainline, key = { _, node -> node.subject.subjectId }) { index, node ->
            AppleTimelineItem(
                presentation,
                index,
                node,
                colors,
                onClickSubject,
                Modifier.padding(horizontal = horizontalPadding),
            )
        }
        if (graph.truncated) {
            item("truncated") {
                Text(
                    stringResource(Lang.subject_relation_graph_truncated),
                    Modifier
                        // 与封面左缘对齐
                        .padding(
                            start = horizontalPadding + AppleRelationGraphDefaults.RailWidth +
                                    AppleRelationGraphDefaults.RailGap + AppleRelationGraphDefaults.CardPadding,
                            end = horizontalPadding,
                        )
                        .testTag(AppleRelationGraphTestTags.Truncated),
                    style = AniTextStyles.footnote,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
    }
}

@Composable
private fun AppleTimelineItem(
    presentation: SubjectRelationGraphPresentation,
    index: Int,
    node: SubjectRelationGraphMainNode,
    colors: AppleTimelineColors,
    onClickSubject: (SubjectRelationGraphSubject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val graph = presentation.graph
    val defaults = AppleRelationGraphDefaults
    Row(
        modifier
            .fillMaxWidth()
            .appleTimelineVertical(
                colors,
                // 圆点对齐封面的垂直中心
                dotCenterY = defaults.CardPadding + defaults.CoverHeight / 2,
                dot = timelineDot(presentation, index),
                lineBefore = timelineLine(presentation, index, before = true),
                lineAfter = timelineLine(presentation, index, before = false),
                centerX = defaults.RailWidth / 2,
                // 横线连到封面
                tickEndX = defaults.RailWidth + defaults.RailGap + defaults.CardPadding,
            ),
    ) {
        Spacer(Modifier.width(defaults.RailWidth + defaults.RailGap))
        Column(Modifier.weight(1f).padding(bottom = defaults.ItemSpacing)) {
            AppleMainCard(
                node,
                ordinal = presentation.ordinals[index],
                isCurrent = node.subject.subjectId == graph.subjectId,
                onClick = { onClickSubject(node.subject) },
                Modifier.fillMaxWidth(),
            )
            AppleBranchList(
                node.branches,
                currentSubjectId = graph.subjectId,
                seriesName = presentation.seriesName,
                onClick = onClickSubject,
                connectorTopExtent = defaults.CardPadding + defaults.BranchListSpacing,
                toggleTag = AppleRelationGraphTestTags.toggle(node.subject.subjectId),
                // 列表左缘与封面对齐, 支线从封面下方垂下
                Modifier.padding(start = defaults.CardPadding, top = defaults.BranchListSpacing),
            )
        }
    }
}

/**
 * 主线卡片: 封面 84×112, 右侧「第 N 部」与收藏状态、番名、年月与话数. 目前这部用 tint 底, 「第 N 部 · 目前」用 accent.
 */
@Composable
private fun AppleMainCard(
    node: SubjectRelationGraphMainNode,
    ordinal: Int?,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subject = node.subject
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isCurrent) colorScheme.secondaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .semantics { selected = isCurrent }
            .padding(AppleRelationGraphDefaults.CardPadding)
            .testTag(AppleRelationGraphTestTags.main(subject.subjectId)),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppleCover(
            CoverAspect.Portrait3x4,
            subject.displayName,
            Modifier.width(AppleRelationGraphDefaults.CoverWidth),
            cornerRadius = 10.dp,
        ) {
            AsyncImage(subject.image, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    renderMainNodeLabel(node, ordinal, isCurrent),
                    Modifier.weight(1f),
                    style = AniTextStyles.footnote,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isCurrent) colorScheme.primary else colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                renderCollectionType(subject.collectionType)?.let {
                    Text(
                        it,
                        Modifier.padding(start = 8.dp),
                        style = AniTextStyles.caption,
                        color = AniColors.labelSoft,
                        maxLines = 1,
                    )
                }
            }
            Text(
                subject.displayName,
                style = AniTextStyles.headline,
                fontWeight = FontWeight.Bold,
                color = colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            renderAirMonthAndEpisodes(subject)?.let {
                Text(
                    it,
                    style = AniTextStyles.footnote,
                    color = colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * 一个主线条目下的相关条目: 纯文字列表, 左侧一条从封面下方垂下的细支线连到每一行.
 * 超过 [AppleRelationGraphDefaults.COLLAPSED_BRANCH_COUNT] 个时其余收合为「其餘 N 部」. 用户查看的条目在收合的部分时默认展开.
 *
 * @param connectorTopExtent 列表顶部到封面底部的距离.
 */
@Composable
private fun AppleBranchList(
    branches: List<SubjectRelationGraphBranch>,
    currentSubjectId: Int,
    seriesName: String,
    onClick: (SubjectRelationGraphSubject) -> Unit,
    connectorTopExtent: Dp,
    toggleTag: String,
    modifier: Modifier = Modifier,
) {
    if (branches.isEmpty()) return
    val collapsedCount = AppleRelationGraphDefaults.COLLAPSED_BRANCH_COUNT
    val canCollapse = branches.size > collapsedCount
    var expanded by rememberSaveable(branches, currentSubjectId) {
        mutableStateOf(branches.drop(collapsedCount).any { it.subject.subjectId == currentSubjectId })
    }
    val connectorColor = MaterialTheme.colorScheme.outlineVariant
    Column(modifier) {
        val visible = if (canCollapse && !expanded) branches.take(collapsedCount) else branches
        visible.forEachIndexed { index, branch ->
            AppleBranchRow(
                branch,
                name = remember(branch, seriesName) { branch.subject.displayName.removeSeriesPrefix(seriesName) },
                isCurrent = branch.subject.subjectId == currentSubjectId,
                onClick = { onClick(branch.subject) },
                Modifier
                    .appleBranchConnector(
                        connectorColor,
                        topExtent = if (index == 0) connectorTopExtent else 0.dp,
                        isLast = index == visible.lastIndex,
                    )
                    .padding(start = AppleRelationGraphDefaults.BranchIndent),
            )
        }
        if (canCollapse) {
            Box(
                Modifier
                    .padding(start = AppleRelationGraphDefaults.BranchIndent)
                    .heightIn(min = AppleComponentDefaults.MinTouchSize)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable(role = Role.Button) { expanded = !expanded }
                    .padding(horizontal = AppleRelationGraphDefaults.BranchRowPadding)
                    .testTag(toggleTag),
                contentAlignment = Alignment.CenterStart,
            ) {
                Text(
                    if (expanded) {
                        stringResource(Lang.subject_relation_graph_show_less)
                    } else {
                        stringResource(Lang.subject_relation_graph_show_more, branches.size - collapsedCount)
                    },
                    style = AniTextStyles.subhead,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/**
 * 相关条目一行: 最小高 48, 名称 `15/20 500`, 下一行「关系 · 年份 · 状态」`caption` label2.
 * 用户查看的条目用 tint 底, 名称用 accent.
 */
@Composable
private fun AppleBranchRow(
    branch: SubjectRelationGraphBranch,
    name: String,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier
            .fillMaxWidth()
            .heightIn(min = AppleRelationGraphDefaults.BranchRowMinHeight)
            .clip(RoundedCornerShape(10.dp))
            .background(if (isCurrent) colorScheme.secondaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .semantics { selected = isCurrent }
            .padding(horizontal = AppleRelationGraphDefaults.BranchRowPadding, vertical = 6.dp)
            .testTag(AppleRelationGraphTestTags.branch(branch.subject.subjectId)),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            name,
            style = AniTextStyles.callout,
            fontWeight = FontWeight.Medium,
            color = if (isCurrent) colorScheme.primary else colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            renderBranchMeta(branch),
            style = AniTextStyles.caption,
            color = colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 「第 N 部」, 目前这部后接「· 目前」. 非正片条目为「劇場版」、「OVA」或「特別篇」.
 */
@Composable
internal fun renderMainNodeLabel(node: SubjectRelationGraphMainNode, ordinal: Int?, isCurrent: Boolean): String {
    val label = renderMainNodeLabel(node, ordinal)
    return if (isCurrent) stringResource(Lang.subject_relation_graph_label_current, label) else label
}

/**
 * 「2020 年 7 月 · 13 話」. 未知的部分省略; 只有一集 (剧场版, OVA) 时不显示话数. 都未知时为 `null`.
 */
@Composable
internal fun renderAirMonthAndEpisodes(subject: SubjectRelationGraphSubject): String? {
    val date = subject.airDate
    val month = if (date.isValid) stringResource(Lang.subject_details_air_month, date.year, date.month) else null
    val episodes = if (subject.episodeCount > 1) {
        stringResource(Lang.subject_relation_graph_episodes, subject.episodeCount)
    } else {
        null
    }
    return listOfNotNull(month, episodes).joinToString(" · ").ifEmpty { null }
}

/**
 * 「关系 · 年份 · 收藏状态」, 未知的部分省略.
 */
@Composable
internal fun renderBranchMeta(branch: SubjectRelationGraphBranch): String = listOfNotNull(
    branch.relation?.let { renderSubjectRelation(it) },
    renderYear(branch.subject),
    renderCollectionType(branch.subject.collectionType),
).joinToString(" · ")

/**
 * Apple 版时间线的颜色: 已经过的段落与圆点为 label2, 还没到的为 separator, 目前这部的圆圈为 accent.
 *
 * @param background 页面底色, 填在目前这部的圆圈内.
 */
@Immutable
internal class AppleTimelineColors(
    val reached: Color,
    val upcoming: Color,
    val current: Color,
    val background: Color,
)

/**
 * 在左侧画竖直时间线: 贯穿整个高度的 2 宽线, 位于 [dotCenterY] 的圆点, 以及从圆点向右连到 [tickEndX] 的 2 高横线.
 */
internal fun Modifier.appleTimelineVertical(
    colors: AppleTimelineColors,
    dotCenterY: Dp,
    dot: TimelineDot,
    lineBefore: TimelineLine,
    lineAfter: TimelineLine,
    centerX: Dp,
    tickEndX: Dp,
): Modifier = drawBehind {
    val center = Offset(centerX.toPx(), dotCenterY.toPx())
    drawAppleTimelineLine(colors, lineBefore, Offset(center.x, 0f), center)
    drawAppleTimelineLine(colors, lineAfter, center, Offset(center.x, size.height))
    val tick = if (dot == TimelineDot.UPCOMING) TimelineLine.UPCOMING else TimelineLine.REACHED
    drawAppleTimelineLine(colors, tick, center, Offset(tickEndX.toPx(), center.y))
    drawAppleTimelineDot(colors, dot, center)
}

/**
 * 画横向时间线: 贯穿整个宽度的 2 高线, 以及位于 [dotCenterX] 的圆点.
 */
internal fun Modifier.appleTimelineHorizontal(
    colors: AppleTimelineColors,
    dotCenterX: Dp,
    dot: TimelineDot,
    lineBefore: TimelineLine,
    lineAfter: TimelineLine,
): Modifier = drawBehind {
    val center = Offset(dotCenterX.toPx(), size.height / 2)
    drawAppleTimelineLine(colors, lineBefore, Offset(0f, center.y), center)
    drawAppleTimelineLine(colors, lineAfter, center, Offset(size.width, center.y))
    drawAppleTimelineDot(colors, dot, center)
}

private fun DrawScope.drawAppleTimelineLine(colors: AppleTimelineColors, line: TimelineLine, start: Offset, end: Offset) {
    val color = when (line) {
        TimelineLine.NONE -> return
        TimelineLine.REACHED -> colors.reached
        TimelineLine.UPCOMING -> colors.upcoming
    }
    drawLine(color, start, end, strokeWidth = 2.dp.toPx())
}

/**
 * 目前这部为 20 圆: accent 圈与中心的 accent 圆点; 其他为 8 的小圆点.
 */
private fun DrawScope.drawAppleTimelineDot(colors: AppleTimelineColors, dot: TimelineDot, center: Offset) {
    when (dot) {
        TimelineDot.CURRENT -> {
            val ringWidth = 2.5.dp.toPx()
            drawCircle(colors.background, 10.dp.toPx(), center)
            drawCircle(colors.current, 10.dp.toPx() - ringWidth / 2, center, style = Stroke(ringWidth))
            drawCircle(colors.current, 4.dp.toPx(), center)
        }

        TimelineDot.REACHED -> drawCircle(colors.reached, 4.dp.toPx(), center)
        TimelineDot.UPCOMING -> drawCircle(colors.upcoming, 4.dp.toPx(), center)
    }
}

/**
 * 相关条目左侧的细支线: 从上方垂下的竖线, 在行的垂直中心以圆角折向右侧. 每行只画自己的一段, 各段首尾相接.
 *
 * @param topExtent 竖线向上超出这一行的距离: 第一行为到封面底部的距离, 其余行为 0.
 * @param isLast 最后一行的竖线止于折角, 否则贯穿到底部.
 */
private fun Modifier.appleBranchConnector(color: Color, topExtent: Dp, isLast: Boolean): Modifier = drawBehind {
    val railX = AppleRelationGraphDefaults.BranchRailX.toPx()
    val endX = (AppleRelationGraphDefaults.BranchIndent - 4.dp).toPx()
    val centerY = size.height / 2
    val radius = 8.dp.toPx()
    val path = Path().apply {
        moveTo(railX, -topExtent.toPx())
        lineTo(railX, centerY - radius)
        quadraticTo(railX, centerY, railX + radius, centerY)
        lineTo(endX, centerY)
        if (!isLast) {
            moveTo(railX, centerY - radius)
            lineTo(railX, size.height)
        }
    }
    drawPath(path, color, style = Stroke(1.5.dp.toPx()))
}

internal object AppleRelationGraphDefaults {
    /** 时间线一栏的宽度, 等于目前这部的圆圈直径. */
    val RailWidth = 20.dp

    /** 时间线一栏到主线卡片的距离. */
    val RailGap = 12.dp

    val CardPadding = 8.dp
    val CoverWidth = 84.dp
    val CoverHeight = 112.dp

    /** 主线条目之间的距离. */
    val ItemSpacing = 16.dp

    /** 主线卡片到相关条目列表的距离. */
    val BranchListSpacing = 4.dp

    /** 支线相对列表左缘 (与封面左缘对齐) 的位置. */
    val BranchRailX = 12.dp

    /** 相关条目的缩进: 支线与折线之后才是文字. */
    val BranchIndent = 30.dp

    val BranchRowPadding = 10.dp
    val BranchRowMinHeight = 48.dp

    /** 相关条目超过这个数量时, 其余的收合. */
    const val COLLAPSED_BRANCH_COUNT = 3

    @Composable
    fun timelineColors(background: Color): AppleTimelineColors = AppleTimelineColors(
        reached = MaterialTheme.colorScheme.secondary,
        upcoming = MaterialTheme.colorScheme.outlineVariant,
        current = MaterialTheme.colorScheme.primary,
        background = background,
    )
}

internal object AppleRelationGraphTestTags {
    const val List = "relation-graph-list"
    const val Subtitle = "relation-graph-subtitle"
    const val CollapsedBar = "relation-graph-collapsed-bar"
    const val Loading = "relation-graph-loading"
    const val Error = "relation-graph-error"
    const val Truncated = "relation-graph-truncated"
    fun main(subjectId: Int) = "relation-graph-main-$subjectId"
    fun branch(subjectId: Int) = "relation-graph-branch-$subjectId"
    fun toggle(mainSubjectId: Int) = "relation-graph-toggle-$mainSubjectId"
}

/** 标题列返回钮一行的下缘, 相对标题列顶部 (安全区之下): 上距 10 + 圆钮 36. */
private val HeaderBackRowBottom = 46.dp
