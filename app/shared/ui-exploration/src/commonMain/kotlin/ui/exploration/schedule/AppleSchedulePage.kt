/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import me.him188.ani.app.ui.exploration.follow.QuickFollowAnchor
import me.him188.ani.app.ui.exploration.follow.QuickFollowState
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleComponentDefaults
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleFollowButton
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.PushedPageHeader
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.exploration_quick_follow
import me.him188.ani.app.ui.lang.exploration_quick_follow_followed
import me.him188.ani.app.ui.lang.exploration_schedule
import me.him188.ani.app.ui.lang.exploration_schedule_count
import me.him188.ani.app.ui.lang.exploration_schedule_day_empty
import me.him188.ani.app.ui.lang.exploration_schedule_day_title
import me.him188.ani.app.ui.lang.exploration_schedule_last_week
import me.him188.ani.app.ui.lang.exploration_schedule_next_week
import me.him188.ani.app.ui.lang.exploration_schedule_this_week
import me.him188.ani.app.ui.lang.exploration_schedule_time_unknown
import me.him188.ani.app.ui.lang.exploration_schedule_view_details
import me.him188.ani.app.ui.search.LoadErrorCard
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版新番时间表 (03 §3.3). 手机推入页.
 *
 * 从上到下: C3 返回钮与标题; 周选择卡 (「本周」等与日期范围、上一周与下一周按钮、星期条); 选中日的标题与部数; 当日列表.
 * 日期范围同 [ScheduleViewModel]: 今天前后各 7 天, 范围外的星期格停用; 换周时选中同一个星期几, 超出范围时取范围内最近的一天.
 * 每列右侧为快速追番按钮 (05 G2).
 *
 * @param state 打开时选中 [ScheduleScreenState.selectedDay], 见 [ScheduleViewModel.pageState].
 * @param quickFollowState 为 `null` 时不显示快速追番按钮 (未登录).
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleSchedulePage(
    presentation: SchedulePagePresentation,
    state: ScheduleScreenState,
    quickFollowState: QuickFollowState?,
    onRetry: () -> Unit,
    onClickItem: (AiringScheduleItemPresentation) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val days = presentation.days
    var selectedDate by rememberSaveable(stateSaver = LocalDateSaver) { mutableStateOf(state.selectedDay?.date) }
    val firstDate = days.firstOrNull()?.date
    val lastDate = days.lastOrNull()?.date
    val today = days.firstOrNull { it.kind == ScheduleDay.Kind.TODAY }?.date

    LazyColumn(
        modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
        contentPadding = contentPadding,
    ) {
        item(key = "header") {
            Column(Modifier.padding(bottom = 16.dp)) {
                PushedPageHeader(stringResource(Lang.exploration_schedule), onBack = onBack, windowInsets = windowInsets)
            }
        }
        if (firstDate == null || lastDate == null || today == null) return@LazyColumn

        // 跨过午夜后日期范围移动, 选中的日期保持在范围内.
        val selected = (selectedDate ?: today).coerceIn(firstDate, lastDate)
        item(key = "week") {
            WeekCard(
                selected = selected,
                today = today,
                firstDate = firstDate,
                lastDate = lastDate,
                onSelect = { selectedDate = it.coerceIn(firstDate, lastDate) },
            )
        }

        val error = presentation.error
        if (error != null) {
            item(key = "error") {
                LoadErrorCard(error, onRetry = onRetry, Modifier.padding(horizontal = 16.dp).padding(top = 20.dp))
            }
            return@LazyColumn
        }

        val rows = appleScheduleRows(
            presentation.airingSchedules.firstOrNull { it.date == selected }?.episodes.orEmpty(),
            date = selected,
            today = today,
        )
        item(key = "day-title") {
            val day = days.first { it.date == selected }
            DayTitle(
                stringResource(
                    Lang.exploration_schedule_day_title,
                    renderDayOfWeek(day.dayOfWeek, day.kind),
                    selected.month.number,
                    selected.day,
                ),
                count = if (presentation.isPlaceholder) null else rows.count { it is AppleScheduleRow.Item },
            )
        }
        if (rows.isEmpty()) {
            item(key = "empty") {
                Text(
                    stringResource(Lang.exploration_schedule_day_empty),
                    Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(16.dp),
                    style = AniTextStyles.callout,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
        }
        itemsIndexed(rows, key = { _, row -> row.key }) { index, row ->
            Box(
                Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .clip(listCardShape(index, rows.size))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                when (row) {
                    is AppleScheduleRow.Item -> ScheduleRow(row, quickFollowState, onClick = { onClickItem(row.item) })
                    is AppleScheduleRow.Placeholder -> PlaceholderRow(row)
                    is AppleScheduleRow.Now -> NowLine(row.time)
                }
            }
        }
    }
}

private val LocalDateSaver = Saver<LocalDate?, String>(
    save = { it?.toString() },
    restore = { LocalDate.parse(it) },
)

/**
 * 周选择卡: card 圆角 16. 上方「本周」`callout` W600 + 日期范围 `footnote` label2, 右侧上一周、下一周两个 36 圆钮 (label2 图标);
 * 下方 7 栏星期条.
 */
@Composable
private fun WeekCard(
    selected: LocalDate,
    today: LocalDate,
    firstDate: LocalDate,
    lastDate: LocalDate,
    onSelect: (LocalDate) -> Unit,
) {
    val week = remember(selected) { currentWeek(selected) }
    val thisMonday = remember(today) { currentWeek(today).first() }
    val weekLabel = when {
        week.first() < thisMonday -> Lang.exploration_schedule_last_week
        week.first() > thisMonday -> Lang.exploration_schedule_next_week
        else -> Lang.exploration_schedule_this_week
    }
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    stringResource(weekLabel),
                    Modifier.alignByBaseline(),
                    style = AniTextStyles.callout,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                )
                Text(
                    "${renderMonthDay(week.first())} – ${renderMonthDay(week.last())}",
                    Modifier.alignByBaseline(),
                    style = AniTextStyles.footnote,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                )
            }
            WeekButton(
                AppleIcons.chevronLeft(2.2f),
                stringResource(Lang.exploration_schedule_last_week),
                enabled = week.first() > firstDate,
                onClick = { onSelect(selected.minus(DatePeriod(days = 7))) },
                Modifier.testTag(AppleScheduleTestTags.PreviousWeek),
            )
            Spacer(Modifier.width(2.dp))
            WeekButton(
                AppleIcons.chevronRight(2.2f),
                stringResource(Lang.exploration_schedule_next_week),
                enabled = week.last() < lastDate,
                onClick = { onSelect(selected.plus(DatePeriod(days = 7))) },
                Modifier.testTag(AppleScheduleTestTags.NextWeek),
            )
        }
        AppleWeekdayStrip(
            week,
            selectedDate = selected,
            onClickDay = onSelect,
            Modifier.padding(start = 8.dp, end = 8.dp, top = 4.dp, bottom = 8.dp),
            isEnabled = { it in firstDate..lastDate },
            cellModifier = { Modifier.testTag(AppleScheduleTestTags.weekday(it)) },
        )
    }
}

private fun renderMonthDay(date: LocalDate) = "${date.month.number}/${date.day}"

/**
 * 换周按钮: 36 圆形, 无底, label2 图标 16.
 */
@Composable
private fun WeekButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription }
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.secondary)
    }
}

/**
 * 选中日的标题 `title`, 右侧部数 `footnote` label2. 标题行左右内缩 20, 上距 20、下距 12.
 */
@Composable
private fun DayTitle(title: String, count: Int?) {
    Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)) {
        Text(
            title,
            Modifier.weight(1f).alignByBaseline(),
            style = AniTextStyles.title,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (count != null) {
            Text(
                stringResource(Lang.exploration_schedule_count, count),
                Modifier.alignByBaseline().padding(start = 12.dp),
                style = AniTextStyles.footnote,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
            )
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
 * 当日列表的一列.
 */
internal sealed class AppleScheduleRow {
    abstract val key: String

    /**
     * @param showTime 与上一项时间相同时不重复显示时间.
     * @param aired 是否已播出. 已播的时间用 label2, 未播用 label.
     * @param showDivider 上一列也是番剧时, 在本列上方画分隔线.
     */
    data class Item(
        val item: AiringScheduleItemPresentation,
        val showTime: Boolean,
        val aired: Boolean,
        val showDivider: Boolean,
    ) : AppleScheduleRow() {
        override val key: String get() = "item-${item.subjectId}-${item.episodeId}"
    }

    data class Placeholder(val id: Int, val showTime: Boolean, val showDivider: Boolean) : AppleScheduleRow() {
        override val key: String get() = "placeholder-$id"
    }

    /** 现在时间线, 只在今天出现. */
    data class Now(val time: LocalTime) : AppleScheduleRow() {
        override val key: String get() = "now"
    }
}

/**
 * 把一天的 [AiringScheduleColumnItem] 转为列表的列.
 *
 * 已播出: [date] 早于 [today] 时全部已播, 晚于时全部未播; 今天则是现在时间线之前的项目 (时间未定的项目排在时间线之后, 视为未播).
 */
internal fun appleScheduleRows(
    items: List<AiringScheduleColumnItem>,
    date: LocalDate,
    today: LocalDate,
): List<AppleScheduleRow> {
    val nowIndex = items.indexOfFirst { it is AiringScheduleColumnItem.CurrentTimeIndicator }
    var previousIsItem = false
    return items.mapIndexed { index, columnItem ->
        val showDivider = previousIsItem
        previousIsItem = columnItem !is AiringScheduleColumnItem.CurrentTimeIndicator
        when (columnItem) {
            is AiringScheduleColumnItem.Data -> AppleScheduleRow.Item(
                columnItem.item,
                showTime = columnItem.showTime,
                aired = when {
                    date < today -> true
                    date > today -> false
                    else -> index < nowIndex
                },
                showDivider = showDivider,
            )

            is AiringScheduleColumnItem.PlaceholderData -> AppleScheduleRow.Placeholder(
                columnItem.id,
                showTime = columnItem.showTime,
                showDivider = showDivider,
            )

            is AiringScheduleColumnItem.CurrentTimeIndicator -> AppleScheduleRow.Now(columnItem.currentTime)
        }
    }
}

/**
 * 番剧列: 内距 `12 12 12 16`; 时间栏宽 44; 封面 48×64 圆角 8; 番名 `body` W600, 集数 `footnote` label2; 右侧 36 追番圆钮.
 * 分隔线从 72 开始.
 */
@Composable
private fun ScheduleRow(
    row: AppleScheduleRow.Item,
    quickFollowState: QuickFollowState?,
    onClick: () -> Unit,
) {
    val item = row.item
    val useOriginalTitle = LocalSubjectAppearanceSettings.current.useOriginalTitle
    val title = if (useOriginalTitle) item.subjectOriginalTitle else item.subjectTitle
    val episodeName = if (useOriginalTitle) item.episodeOriginalName else item.episodeName
    Box {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(
                    onClickLabel = stringResource(Lang.exploration_schedule_view_details),
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TimeText(item.time.takeIf { row.showTime }, isTimeUnknown = row.showTime && item.time == null, aired = row.aired)
            ScheduleCover {
                AsyncImage(item.imageUrl, contentDescription = null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
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
                    ScheduleItemDefaults.renderEpisode(item.episodeSort, item.episodeEp, episodeName),
                    style = AniTextStyles.footnote,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (quickFollowState != null) {
                QuickFollowAnchor(quickFollowState, item.subjectId) { followed, enabled, onClickFollow ->
                    AppleFollowButton(
                        followed = followed,
                        onClick = onClickFollow,
                        contentDescription = if (followed) {
                            stringResource(Lang.exploration_quick_follow_followed, title)
                        } else {
                            stringResource(Lang.exploration_quick_follow, title)
                        },
                        Modifier.testTag(AppleScheduleTestTags.follow(item.subjectId)),
                        enabled = enabled,
                    )
                }
            }
        }
        if (row.showDivider) {
            AppleDivider(Modifier.align(Alignment.TopStart), startIndent = 72.dp)
        }
    }
}

/**
 * 时间栏: 宽 44, `callout` W600; 时间未定改为 11 号字. 已播 label2, 未播 label. [time] 与 [isTimeUnknown] 都不成立时留空.
 *
 * 时刻不换行, 字形较宽时允许超出栏宽, 各列的封面仍然对齐.
 */
@Composable
private fun TimeText(time: LocalTime?, isTimeUnknown: Boolean, aired: Boolean) {
    val color = if (aired) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
    if (isTimeUnknown) {
        Text(
            stringResource(Lang.exploration_schedule_time_unknown),
            Modifier.width(44.dp),
            style = AniTextStyles.callout.copy(fontSize = 11.sp),
            fontWeight = FontWeight.SemiBold,
            color = color,
            maxLines = 2,
        )
    } else {
        Text(
            if (time != null) ScheduleItemDefaults.renderTime(null, time) else "",
            Modifier.width(44.dp),
            style = AniTextStyles.callout,
            fontWeight = FontWeight.SemiBold,
            color = color,
            softWrap = false,
            overflow = TextOverflow.Visible,
            maxLines = 1,
        )
    }
}

@Composable
private fun ScheduleCover(content: @Composable () -> Unit) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        Modifier
            .size(48.dp, 64.dp)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        content()
        Box(Modifier.fillMaxSize().border(1.dp, Color.Black.copy(alpha = 0.06f), shape))
    }
}

/**
 * 加载中的占位列, 版式同 [ScheduleRow].
 */
@Composable
private fun PlaceholderRow(row: AppleScheduleRow.Placeholder) {
    Box {
        Row(
            Modifier.fillMaxWidth().padding(start = 16.dp, end = 12.dp, top = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.width(44.dp)) {
                if (row.showTime) {
                    Text("00:00", Modifier.placeholder(true), style = AniTextStyles.callout)
                }
            }
            ScheduleCover { Box(Modifier.fillMaxSize().placeholder(true, shape = RectangleShape)) }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Placeholder title", Modifier.placeholder(true), style = AniTextStyles.body)
                Text("Placeholder", Modifier.placeholder(true), style = AniTextStyles.footnote)
            }
        }
        if (row.showDivider) {
            AppleDivider(Modifier.align(Alignment.TopStart), startIndent = 72.dp)
        }
    }
}

/**
 * 现在时间线: 高 28; 时间 `caption` W700、8 圆点与 2 高的线, 全部 accent.
 */
@Composable
private fun NowLine(time: LocalTime) {
    Row(
        Modifier
            .fillMaxWidth()
            .height(28.dp)
            .padding(horizontal = 16.dp)
            .testTag(AppleScheduleTestTags.Now),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val color = MaterialTheme.colorScheme.primary
        Text(
            ScheduleItemDefaults.renderTime(null, time),
            Modifier.width(44.dp),
            style = AniTextStyles.caption,
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
        )
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Box(Modifier.weight(1f).height(2.dp).clip(RoundedCornerShape(1.dp)).background(color))
    }
}

internal object AppleScheduleTestTags {
    const val PreviousWeek = "schedule-previous-week"
    const val NextWeek = "schedule-next-week"
    const val Now = "schedule-now"
    fun weekday(date: LocalDate) = "schedule-weekday-$date"
    fun follow(subjectId: Int) = "schedule-follow-$subjectId"
}
