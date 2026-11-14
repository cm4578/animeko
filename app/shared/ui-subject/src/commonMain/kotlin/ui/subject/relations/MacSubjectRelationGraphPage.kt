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
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.subject.SubjectRelationGraphBranch
import me.him188.ani.app.data.models.subject.SubjectRelationGraphMainNode
import me.him188.ani.app.data.models.subject.SubjectRelationGraphSubject
import me.him188.ani.app.ui.adaptive.HorizontalScrollControlScaffoldOnDesktop
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.HorizontalScrollControlState
import me.him188.ani.app.ui.foundation.apple.AppleComponentDefaults
import me.him188.ani.app.ui.foundation.apple.AppleCover
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.CoverAspect
import me.him188.ani.app.ui.foundation.apple.MacLargeTitleHeader
import me.him188.ani.app.ui.foundation.apple.MacPageToolbar
import me.him188.ani.app.ui.foundation.apple.MacWindowDefaults
import me.him188.ani.app.ui.foundation.rememberHorizontalScrollControlState
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.subject_relation_graph_related_count
import me.him188.ani.app.ui.lang.subject_relation_graph_show_less
import me.him188.ani.app.ui.lang.subject_relation_graph_show_more
import me.him188.ani.app.ui.lang.subject_relation_graph_title
import me.him188.ani.app.ui.lang.subject_relation_graph_truncated
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 系列关系图 (04 §4.5): 工具栏之下为大标题 (系列名) 与「系列關係圖 · 主線 N 部 · 其他 M 部」.
 *
 * 内容宽度不小于 600 时主线为横向时间线, 每部一栏 ([MacRelationGraphColumns]); 更窄时用 iOS 的竖直时间线.
 * 加载中与加载失败时还不知道系列名, 大标题为「系列關係圖」.
 */
@Composable
fun MacSubjectRelationGraphScreen(
    vm: SubjectRelationGraphViewModel,
    onClickSubject: (SubjectRelationGraphSubject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by vm.state.collectAsStateWithLifecycle()
    MacSubjectRelationGraphLayout(state, vm::retry, onClickSubject, modifier)
}

@Composable
internal fun MacSubjectRelationGraphLayout(
    state: SubjectRelationGraphUiState,
    onRetry: () -> Unit,
    onClickSubject: (SubjectRelationGraphSubject) -> Unit,
    modifier: Modifier = Modifier,
) {
    val background = MaterialTheme.colorScheme.surfaceContainerLowest
    Column(modifier.fillMaxSize().background(background)) {
        MacPageToolbar()
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val graph = state.graph
            if (graph == null) {
                Column(Modifier.fillMaxSize().padding(MacWindowDefaults.ContentPadding)) {
                    MacLargeTitleHeader(stringResource(Lang.subject_relation_graph_title))
                    AppleRelationGraphLoadState(state.error, onRetry, Modifier.weight(1f).fillMaxWidth().padding(top = 16.dp))
                }
                return@BoxWithConstraints
            }

            val presentation = remember(graph) { SubjectRelationGraphPresentation(graph) }
            val header: @Composable (Modifier) -> Unit = {
                MacLargeTitleHeader(presentation.seriesName, it, subtitle = renderRelationGraphSubtitle(graph))
            }
            if (maxWidth < WIDE_LAYOUT_MIN_WIDTH) {
                AppleRelationGraphTimeline(
                    presentation,
                    onClickSubject,
                    rememberRelationGraphListState(presentation),
                    background,
                    Modifier.fillMaxSize(),
                    horizontalPadding = MacRelationGraphDefaults.HorizontalPadding,
                ) {
                    header(
                        Modifier.padding(
                            start = MacRelationGraphDefaults.HorizontalPadding,
                            top = 4.dp,
                            end = MacRelationGraphDefaults.HorizontalPadding,
                            bottom = 20.dp,
                        ),
                    )
                }
            } else {
                MacRelationGraphColumns(
                    presentation,
                    onClickSubject,
                    viewportWidth = maxWidth - MacRelationGraphDefaults.HorizontalPadding * 2,
                    background,
                    Modifier.fillMaxSize(),
                    header = header,
                )
            }
        }
    }
}

/**
 * 横向时间线: 主线每部一栏, 由上到下为年份、时间线、主线卡片、分隔线与相关条目. 栏宽见 [macRelationColumnWidth].
 *
 * 主线多于可视栏数时横向滚动, 打开时目前这部的前一部在最左. 滚动方式同 App 桌面版: 悬停时显示左右翻页钮,
 * 页面无法纵向滚动时纵向滚轮横向滚动时间线.
 *
 * @param viewportWidth 栏所在的可视宽度, 不含左右内距.
 */
@Composable
private fun MacRelationGraphColumns(
    presentation: SubjectRelationGraphPresentation,
    onClickSubject: (SubjectRelationGraphSubject) -> Unit,
    viewportWidth: Dp,
    background: Color,
    modifier: Modifier = Modifier,
    header: @Composable (Modifier) -> Unit,
) {
    val graph = presentation.graph
    val defaults = MacRelationGraphDefaults
    val columnWidth = macRelationColumnWidth(viewportWidth, defaults.ColumnSpacing, defaults.MinColumnWidth)
    val step = columnWidth + defaults.ColumnSpacing
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()
    val density = LocalDensity.current
    LaunchedEffect(presentation) {
        val index = (presentation.currentMainIndex - 1).coerceAtLeast(0)
        horizontalScrollState.scrollTo(with(density) { (step * index).roundToPx() })
    }
    val scope = rememberCoroutineScope()
    val colors = AppleRelationGraphDefaults.timelineColors(background)
    Column(
        modifier
            .verticalWheelScrollsHorizontally(horizontalScrollState, verticalScrollState)
            .verticalScroll(verticalScrollState)
            .padding(top = 4.dp, bottom = 32.dp)
            .testTag(MacRelationGraphTestTags.Columns),
    ) {
        header(Modifier.padding(start = defaults.HorizontalPadding, end = defaults.HorizontalPadding, bottom = 20.dp))
        // 桌面端鼠标没有横向滚轮, 悬停时显示左右翻页钮, 一次翻过可视的栏数
        HorizontalScrollControlScaffoldOnDesktop(
            rememberHorizontalScrollControlState(horizontalScrollState) { direction ->
                val visibleColumns = ((viewportWidth + defaults.ColumnSpacing) / step).toInt().coerceAtLeast(1)
                val distance = with(density) { (step * visibleColumns).toPx() }
                scope.launch {
                    horizontalScrollState.animateScrollBy(
                        if (direction == HorizontalScrollControlState.Direction.BACKWARD) -distance else distance,
                    )
                }
            },
        ) {
            Row(Modifier.horizontalScroll(horizontalScrollState).padding(horizontal = defaults.HorizontalPadding)) {
                graph.mainline.forEachIndexed { index, node ->
                    // 栏间距算在前一栏里, 时间线因此连续
                    val isLast = index == graph.mainline.lastIndex
                    MacRelationColumn(
                        presentation,
                        index,
                        node,
                        colors,
                        onClickSubject,
                        trailingSpacing = if (isLast) 0.dp else defaults.ColumnSpacing,
                        Modifier.width(if (isLast) columnWidth else step),
                    )
                }
            }
        }
        if (graph.truncated) {
            Text(
                stringResource(Lang.subject_relation_graph_truncated),
                Modifier
                    .padding(horizontal = defaults.HorizontalPadding)
                    .padding(top = 24.dp)
                    .testTag(MacRelationGraphTestTags.Truncated),
                style = AniTextStyles.macCallout,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

/**
 * 每栏的宽度: 可视宽度 [viewport] 按放得下的栏数等分, 每栏至少 [minWidth]. 主线比可视栏数少时右侧留空, 栏宽不变.
 */
internal fun macRelationColumnWidth(viewport: Dp, spacing: Dp, minWidth: Dp): Dp {
    val count = ((viewport + spacing) / (minWidth + spacing)).toInt().coerceAtLeast(1)
    return ((viewport - spacing * (count - 1)) / count).coerceAtLeast(minWidth)
}

@Composable
private fun MacRelationColumn(
    presentation: SubjectRelationGraphPresentation,
    index: Int,
    node: SubjectRelationGraphMainNode,
    colors: AppleTimelineColors,
    onClickSubject: (SubjectRelationGraphSubject) -> Unit,
    trailingSpacing: Dp,
    modifier: Modifier = Modifier,
) {
    val graph = presentation.graph
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier.testTag(MacRelationGraphTestTags.column(node.subject.subjectId))) {
        Text(
            renderYear(node.subject).orEmpty(),
            style = AniTextStyles.macCallout,
            fontWeight = FontWeight.Bold,
            color = if (index == presentation.currentMainIndex) colorScheme.primary else colorScheme.secondary,
            maxLines = 1,
        )
        Spacer(
            Modifier
                .fillMaxWidth()
                .height(MacRelationGraphDefaults.TimelineHeight)
                .appleTimelineHorizontal(
                    colors,
                    // 目前这部的圆圈左缘与卡片左缘对齐
                    dotCenterX = AppleRelationGraphDefaults.RailWidth / 2,
                    dot = timelineDot(presentation, index),
                    lineBefore = timelineLine(presentation, index, before = true),
                    lineAfter = timelineLine(presentation, index, before = false),
                ),
        )
        Column(Modifier.padding(end = trailingSpacing), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            MacMainCard(
                node,
                ordinal = presentation.ordinals[index],
                isCurrent = node.subject.subjectId == graph.subjectId,
                onClick = { onClickSubject(node.subject) },
            )
            if (node.branches.isNotEmpty()) {
                AppleDivider()
                MacBranchList(
                    node.branches,
                    currentSubjectId = graph.subjectId,
                    seriesName = presentation.seriesName,
                    onClick = onClickSubject,
                    toggleTag = MacRelationGraphTestTags.toggle(node.subject.subjectId),
                )
            }
        }
    }
}

/**
 * 主线卡片: 圆角 14、内距 12; 176×235 海报, 右侧「第 N 部」、追番状态、番名 `20/700`、年月与话数.
 * 目前这部用 tint 底, 其余为 card 底.
 */
@Composable
private fun MacMainCard(
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
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isCurrent) colorScheme.secondaryContainer else colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .semantics { selected = isCurrent }
            .padding(12.dp)
            .testTag(MacRelationGraphTestTags.main(subject.subjectId)),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AppleCover(
            CoverAspect.Portrait3x4,
            subject.displayName,
            Modifier.width(MacRelationGraphDefaults.PosterWidth),
            cornerRadius = 10.dp,
        ) {
            AsyncImage(subject.image, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        }
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                renderMainNodeLabel(node, ordinal, isCurrent),
                style = AniTextStyles.macCallout,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isCurrent) colorScheme.primary else colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            renderCollectionType(subject.collectionType)?.let {
                Text(it, style = AniTextStyles.macCaption, color = AniColors.labelSoft, maxLines = 1)
            }
            Text(
                subject.displayName,
                style = AniTextStyles.title,
                color = colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            renderAirMonthAndEpisodes(subject)?.let {
                Text(
                    it,
                    style = AniTextStyles.macCallout,
                    color = colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

/**
 * 「相關條目 N」与纯文字列表. 超过 [SubjectRelationGraphDefaults.COLLAPSED_BRANCH_COUNT_WIDE] 个时其余收合为「其餘 N 部 ⌄」;
 * 用户查看的条目在收合的部分时默认展开.
 */
@Composable
private fun MacBranchList(
    branches: List<SubjectRelationGraphBranch>,
    currentSubjectId: Int,
    seriesName: String,
    onClick: (SubjectRelationGraphSubject) -> Unit,
    toggleTag: String,
    modifier: Modifier = Modifier,
) {
    val collapsedCount = SubjectRelationGraphDefaults.COLLAPSED_BRANCH_COUNT_WIDE
    val canCollapse = branches.size > collapsedCount
    var expanded by rememberSaveable(branches, currentSubjectId) {
        mutableStateOf(branches.drop(collapsedCount).any { it.subject.subjectId == currentSubjectId })
    }
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier) {
        Text(
            stringResource(Lang.subject_relation_graph_related_count, branches.size),
            Modifier
                .padding(horizontal = MacRelationGraphDefaults.BranchRowPadding)
                .padding(bottom = 4.dp),
            style = AniTextStyles.macCaption,
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.secondary,
        )
        val visible = if (canCollapse && !expanded) branches.take(collapsedCount) else branches
        visible.forEach { branch ->
            MacBranchRow(
                branch,
                name = remember(branch, seriesName) { branch.subject.displayName.removeSeriesPrefix(seriesName) },
                isCurrent = branch.subject.subjectId == currentSubjectId,
                onClick = { onClick(branch.subject) },
            )
        }
        if (canCollapse) {
            Row(
                Modifier
                    .heightIn(min = AppleComponentDefaults.MacMinTouchSize)
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(role = Role.Button) { expanded = !expanded }
                    .padding(horizontal = MacRelationGraphDefaults.BranchRowPadding)
                    .testTag(toggleTag),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    if (expanded) {
                        stringResource(Lang.subject_relation_graph_show_less)
                    } else {
                        stringResource(Lang.subject_relation_graph_show_more, branches.size - collapsedCount)
                    },
                    style = AniTextStyles.macCallout,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.primary,
                )
                Icon(
                    AppleIcons.chevronDown(),
                    contentDescription = null,
                    Modifier.size(12.dp).rotate(if (expanded) 180f else 0f),
                    tint = colorScheme.primary,
                )
            }
        }
    }
}

/**
 * 相关条目一行: 行高 44, 名称 `macBody` W500, 下一行「关系 · 年份 · 状态」`macCaption` label2.
 * 用户查看的条目用 tint 底, 名称用 accent.
 */
@Composable
private fun MacBranchRow(
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
            .heightIn(min = MacRelationGraphDefaults.BranchRowHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(if (isCurrent) colorScheme.secondaryContainer else Color.Transparent)
            .clickable(onClick = onClick)
            .semantics { selected = isCurrent }
            .padding(horizontal = MacRelationGraphDefaults.BranchRowPadding, vertical = 4.dp)
            .testTag(MacRelationGraphTestTags.branch(branch.subject.subjectId)),
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            name,
            style = AniTextStyles.macBody,
            fontWeight = FontWeight.Medium,
            color = if (isCurrent) colorScheme.primary else colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            renderBranchMeta(branch),
            style = AniTextStyles.macCaption,
            color = colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal object MacRelationGraphDefaults {
    /** 内容区左右内距 (04 §1.1). */
    val HorizontalPadding = 32.dp

    /** 栏间距. */
    val ColumnSpacing = 24.dp

    /** 一栏最窄的宽度: 放得下 176 海报与文字. */
    val MinColumnWidth = 440.dp

    val TimelineHeight = 28.dp
    val PosterWidth = 176.dp
    val BranchRowHeight = 44.dp
    val BranchRowPadding = 8.dp
}

internal object MacRelationGraphTestTags {
    const val Columns = "mac-relation-graph-columns"
    const val Truncated = "mac-relation-graph-truncated"
    fun column(subjectId: Int) = "mac-relation-graph-column-$subjectId"
    fun main(subjectId: Int) = "mac-relation-graph-main-$subjectId"
    fun branch(subjectId: Int) = "mac-relation-graph-branch-$subjectId"
    fun toggle(mainSubjectId: Int) = "mac-relation-graph-toggle-$mainSubjectId"
}
