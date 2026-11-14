/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimeSource

/**
 * C19 表格的一列.
 *
 * @param width 固定宽度; 为 `null` 时按 [weight] 分配剩余宽度.
 * @param alignment 本列所有单元格 (含表头) 的水平对齐.
 */
@Immutable
class MacTableColumn(
    val width: Dp? = null,
    val weight: Float = 1f,
    val alignment: Alignment.Horizontal = Alignment.Start,
)

/**
 * C19 表格的列模板. **表头与每一行用同一个实例**, 所以各行的列宽、列间距与左右内距一致.
 *
 * @param horizontalPadding 左右内距, 16–24.
 * @param spacing 列间距.
 */
@Immutable
class MacTableColumns(
    val columns: List<MacTableColumn>,
    val horizontalPadding: Dp = 16.dp,
    val spacing: Dp = 16.dp,
) {
    init {
        require(columns.isNotEmpty()) { "MacTableColumns needs at least one column" }
    }

    /**
     * 按总宽度 [totalWidth] (px) 计算各列的起点与宽度 (px), 从左到右.
     */
    internal fun arrange(totalWidth: Int, density: Density): List<ColumnBounds> = with(density) {
        val padding = horizontalPadding.roundToPx()
        val gap = spacing.roundToPx()
        val fixed = columns.sumOf { it.width?.roundToPx() ?: 0 }
        val totalWeight = columns.filter { it.width == null }.sumOf { it.weight.toDouble() }.toFloat()
        val flexible = (totalWidth - padding * 2 - gap * (columns.size - 1) - fixed).coerceAtLeast(0)
        var x = padding
        columns.map { column ->
            val width = column.width?.roundToPx()
                ?: if (totalWeight > 0f) (flexible * column.weight / totalWeight).toInt() else 0
            ColumnBounds(x, width).also { x += width + gap }
        }
    }
}

internal class ColumnBounds(val start: Int, val width: Int)

/**
 * C19 表格容器: card 底、圆角 14、裁切. 内容依次为 [MacTableHeader] 与 [MacTableRow] / [MacTableGroupRow].
 */
@Composable
fun MacTable(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        content = content,
    )
}

/**
 * C19 表头: 高 32–36, 底部 1dp separator. 单元格用 [MacTableHeaderCell], 依 [columns] 的顺序排列.
 */
@Composable
fun MacTableHeader(
    columns: MacTableColumns,
    modifier: Modifier = Modifier,
    height: Dp = MacTableDefaults.HeaderHeight,
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxWidth()) {
        MacTableCells(columns, Modifier.height(height - 1.dp), content)
        Separator()
    }
}

/**
 * C19 表头单元格: `macCaption` W500 label2. 当前排序列 ([sorted]) 为 label W600 + 11 号 `↓`.
 */
@Composable
fun MacTableHeaderCell(
    text: String,
    modifier: Modifier = Modifier,
    sorted: Boolean = false,
) {
    val color = if (sorted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.secondary
    Row(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text,
            style = AniTextStyles.macCaption,
            fontWeight = if (sorted) FontWeight.SemiBold else FontWeight.Medium,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (sorted) {
            Text("↓", style = AniTextStyles.macCaption, fontSize = 11.sp, color = color)
        }
    }
}

/**
 * C19 表格行: 高 52–76; 选中为 tint 底, 悬停为 hover 底. 单元格依 [columns] 的顺序排列, 垂直居中.
 *
 * 单击立即调用 [onClick] (选中), 不等待双击判定; 双击间隔内的第二次单击调用 [onDoubleClick].
 *
 * @param showDivider 在行顶画分隔线 (不透明度 0.6), 用于第二行起.
 * @param dividerStartIndent 分隔线起点, 为第一个内容列的起点 (例如封面右侧的文字).
 * @param onDoubleClick 双击, 例如播放.
 */
@Composable
fun MacTableRow(
    columns: MacTableColumns,
    modifier: Modifier = Modifier,
    height: Dp = MacTableDefaults.RowHeight,
    selected: Boolean = false,
    showDivider: Boolean = false,
    dividerStartIndent: Dp = columns.horizontalPadding,
    onClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered = interactionSource.collectIsHoveredAsState().value
    val background = when {
        selected -> MaterialTheme.colorScheme.secondaryContainer
        hovered -> AniColors.hover
        else -> Color.Transparent
    }
    val doubleClickTimeout = LocalViewConfiguration.current.doubleTapTimeoutMillis.milliseconds
    var lastClick by remember { mutableStateOf<TimeSource.Monotonic.ValueTimeMark?>(null) }
    val interaction = if (onClick != null || onDoubleClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            role = Role.Button,
        ) {
            val previous = lastClick
            if (onDoubleClick != null && previous != null && previous.elapsedNow() < doubleClickTimeout) {
                lastClick = null
                onDoubleClick()
            } else {
                lastClick = TimeSource.Monotonic.markNow()
                onClick?.invoke()
            }
        }
    } else {
        Modifier
    }
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .background(background)
            .semantics { this.selected = selected }
            .then(interaction),
    ) {
        MacTableCells(columns, Modifier.height(height), content)
        if (showDivider) {
            AppleDivider(Modifier.align(Alignment.TopStart), startIndent = dividerStartIndent)
        }
    }
}

/**
 * C19 分组行 (例如「进行中 3」「已完成 8」): 高 32、bg 底, 底部 1dp separator.
 *
 * 左侧为 [title] (13 W600 label) + [count] (label2); [trailing] 放在最后一列, 与下方行的操作列同宽、同中线.
 *
 * @param showTopDivider 在行顶画 1dp separator, 用于第二个分组起.
 */
@Composable
fun MacTableGroupRow(
    columns: MacTableColumns,
    title: String,
    modifier: Modifier = Modifier,
    count: String? = null,
    showTopDivider: Boolean = false,
    trailing: @Composable () -> Unit = {},
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        if (showTopDivider) {
            Separator()
        }
        Layout(
            content = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        title,
                        style = AniTextStyles.macCallout,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                    )
                    if (count != null) {
                        Text(
                            count,
                            style = AniTextStyles.macCallout,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.secondary,
                            maxLines = 1,
                        )
                    }
                }
                Box(contentAlignment = Alignment.Center) { trailing() }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(MacTableDefaults.GroupRowHeight - 1.dp - if (showTopDivider) 1.dp else 0.dp),
        ) { measurables, constraints ->
            val width = constraints.maxWidth
            val height = constraints.maxHeight
            val last = columns.arrange(width, this).last()
            val padding = columns.horizontalPadding.roundToPx()
            val gap = columns.spacing.roundToPx()
            val titlePlaceable = measurables[0].measure(
                Constraints(maxWidth = (last.start - gap - padding).coerceAtLeast(0), maxHeight = height),
            )
            val trailingPlaceable = measurables[1].measure(
                Constraints(minWidth = last.width, maxWidth = last.width, maxHeight = height),
            )
            layout(width, height) {
                titlePlaceable.placeRelative(padding, (height - titlePlaceable.height) / 2)
                trailingPlaceable.placeRelative(last.start, (height - trailingPlaceable.height) / 2)
            }
        }
        Separator()
    }
}

/**
 * C19 表格下方的操作提示 (双击、⌘I、右键): `macCaption` label2, 与表格相隔 10.
 */
@Composable
fun MacTableHint(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        modifier.padding(top = 10.dp),
        style = AniTextStyles.macCaption,
        color = MaterialTheme.colorScheme.secondary,
    )
}

object MacTableDefaults {
    /** 表头高度; 可取 32–36. */
    val HeaderHeight = 32.dp

    /** 一般行高; 可取 52–76. */
    val RowHeight = 52.dp

    val GroupRowHeight = 32.dp
}

/**
 * 按 [columns] 放置单元格: 第 i 个子元素放在第 i 列, 宽度不超过该列, 按列的对齐方式水平放置、垂直居中.
 */
@Composable
private fun MacTableCells(
    columns: MacTableColumns,
    modifier: Modifier,
    content: @Composable () -> Unit,
) {
    Layout(content, modifier.fillMaxWidth()) { measurables, constraints ->
        require(measurables.size <= columns.columns.size) {
            "MacTable row has ${measurables.size} cells, but only ${columns.columns.size} columns"
        }
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val bounds = columns.arrange(width, this)
        val placeables = measurables.mapIndexed { index, measurable ->
            measurable.measure(Constraints(maxWidth = bounds[index].width, maxHeight = height))
        }
        layout(width, height) {
            placeables.forEachIndexed { index, placeable ->
                val column = bounds[index]
                val x = column.start + columns.columns[index].alignment.align(
                    placeable.width,
                    column.width,
                    LayoutDirection.Ltr,
                )
                placeable.placeRelative(x, (height - placeable.height) / 2)
            }
        }
    }
}

@Composable
private fun Separator() {
    Box(
        Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}
