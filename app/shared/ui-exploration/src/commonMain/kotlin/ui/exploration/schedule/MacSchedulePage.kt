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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
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
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleComponentDefaults
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.MacKeyboardShortcut
import me.him188.ani.app.ui.foundation.apple.MacLargeTitleHeader
import me.him188.ani.app.ui.foundation.apple.MacPageToolbar
import me.him188.ani.app.ui.foundation.apple.MacToolbarIconButton
import me.him188.ani.app.ui.foundation.apple.MacToolbarTextButton
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.exploration_quick_follow
import me.him188.ani.app.ui.lang.exploration_quick_follow_followed
import me.him188.ani.app.ui.lang.exploration_schedule
import me.him188.ani.app.ui.lang.exploration_schedule_last_week
import me.him188.ani.app.ui.lang.exploration_schedule_next_week
import me.him188.ani.app.ui.lang.exploration_schedule_this_week
import me.him188.ani.app.ui.lang.exploration_schedule_time_unknown
import me.him188.ani.app.ui.lang.exploration_schedule_view_details
import me.him188.ani.app.ui.lang.schedule_today
import me.him188.ani.app.ui.search.LoadErrorCard
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 版新番时间表 (04 §3.4).
 *
 * 工具栏右侧: 「‹」上一周 (⌘←)、「今天」(⌘T)、「›」下一周 (⌘→), 05 C2. 大标题「新番時間表」+ 同一基线「本週 · M/D – M/D」.
 * 一周 7 栏等宽, 不横向滚动; 今天整栏 tint 底. 栏内卡片不放封面, 今天栏在最后一个已播出条目之后画现在时间线.
 *
 * 日期范围同 [ScheduleViewModel]: 今天前后各 7 天. 范围外的日期栏停用, 换周在两端停用.
 *
 * @param state 打开时显示 [ScheduleScreenState.selectedDay] 所在的一周, 见 [ScheduleViewModel.pageState].
 * @param quickFollowState 为 `null` 时不显示追番钮 (未登录).
 */
@Composable
fun MacSchedulePage(
    presentation: SchedulePagePresentation,
    state: ScheduleScreenState,
    quickFollowState: QuickFollowState?,
    onRetry: () -> Unit,
    onClickItem: (AiringScheduleItemPresentation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val days = presentation.days
    val firstDate = days.firstOrNull()?.date
    val lastDate = days.lastOrNull()?.date
    val today = days.firstOrNull { it.kind == ScheduleDay.Kind.TODAY }?.date
    var weekStart by rememberSaveable(stateSaver = WeekStartSaver) {
        mutableStateOf(state.selectedDay?.date?.let { currentWeek(it).first() })
    }
    // 跨过午夜后日期范围移动, 显示的一周保持在范围内.
    val week = if (firstDate == null || lastDate == null || today == null) {
        null
    } else {
        val start = (weekStart ?: currentWeek(today).first())
            .coerceIn(currentWeek(firstDate).first(), currentWeek(lastDate).first())
        currentWeek(start)
    }
    val canGoPrevious = week != null && firstDate != null && week.first() > firstDate
    val canGoNext = week != null && lastDate != null && week.last() < lastDate

    fun goPrevious() {
        if (canGoPrevious) weekStart = week!!.first().minus(DatePeriod(days = 7))
    }

    fun goNext() {
        if (canGoNext) weekStart = week!!.first().plus(DatePeriod(days = 7))
    }

    fun goToday() {
        if (today != null) weekStart = currentWeek(today).first()
    }

    MacKeyboardShortcut(Key.DirectionLeft, enabled = canGoPrevious, onTrigger = ::goPrevious)
    MacKeyboardShortcut(Key.DirectionRight, enabled = canGoNext, onTrigger = ::goNext)
    MacKeyboardShortcut(Key.T, enabled = today != null, onTrigger = ::goToday)

    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLowest)) {
        MacPageToolbar {
            MacToolbarIconButton(
                onClick = ::goPrevious,
                icon = AppleIcons.chevronLeft(),
                contentDescription = stringResource(Lang.exploration_schedule_last_week),
                Modifier.testTag(MacScheduleTestTags.PreviousWeek),
                enabled = canGoPrevious,
            )
            MacToolbarTextButton(
                stringResource(Lang.schedule_today),
                onClick = ::goToday,
                Modifier.testTag(MacScheduleTestTags.Today),
                enabled = today != null,
            )
            MacToolbarIconButton(
                onClick = ::goNext,
                icon = AppleIcons.chevronRight(),
                contentDescription = stringResource(Lang.exploration_schedule_next_week),
                Modifier.testTag(MacScheduleTestTags.NextWeek),
                enabled = canGoNext,
            )
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 32.dp, top = 4.dp, end = 32.dp, bottom = 32.dp),
        ) {
            MacLargeTitleHeader(
                stringResource(Lang.exploration_schedule),
                subtitle = if (week != null && today != null) weekSubtitle(week, today) else null,
            )
            Spacer(Modifier.height(20.dp))
            val error = presentation.error
            if (error != null) {
                LoadErrorCard(error, onRetry = onRetry)
                return@Column
            }
            if (week == null || today == null || firstDate == null || lastDate == null) return@Column
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (date in week) {
                    DayColumn(
                        date = date,
                        today = today,
                        enabled = date in firstDate..lastDate,
                        items = presentation.airingSchedules.firstOrNull { it.date == date }?.episodes.orEmpty(),
                        quickFollowState = quickFollowState,
                        onClickItem = onClickItem,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

private val WeekStartSaver = Saver<LocalDate?, String>(
    save = { it?.toString() ?: "" },
    restore = { if (it.isEmpty()) null else LocalDate.parse(it) },
)

/**
 * 「本週 · 10/26 – 11/1」: 显示的一周相对今天所在一周为上周、本周或下周.
 */
@Composable
private fun weekSubtitle(week: List<LocalDate>, today: LocalDate): String {
    val thisMonday = currentWeek(today).first()
    val label = when {
        week.first() < thisMonday -> Lang.exploration_schedule_last_week
        week.first() > thisMonday -> Lang.exploration_schedule_next_week
        else -> Lang.exploration_schedule_this_week
    }
    return "${stringResource(label)} · ${renderMonthDay(week.first())} – ${renderMonthDay(week.last())}"
}

private fun renderMonthDay(date: LocalDate) = "${date.month.number}/${date.day}"

/**
 * 一天的栏: 内距 6、圆角 12, 今天为 tint 底. 栏头星期 12 + 日期 20 (今天 onTint、W600 / W700, 其他日期 labelSoft);
 * 下方卡片间距 6. 范围外的日期停用, 内容不透明度 0.35.
 */
@Composable
private fun DayColumn(
    date: LocalDate,
    today: LocalDate,
    enabled: Boolean,
    items: List<AiringScheduleColumnItem>,
    quickFollowState: QuickFollowState?,
    onClickItem: (AiringScheduleItemPresentation) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isToday = date == today
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isToday) colorScheme.secondaryContainer else Color.Transparent)
            .padding(6.dp)
            .testTag(MacScheduleTestTags.day(date))
            .semantics { selected = isToday }
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 2.dp)) {
            Text(
                renderWeekdayName(date.dayOfWeek),
                style = AniTextStyles.macCaption,
                fontWeight = if (isToday) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isToday) colorScheme.onSecondaryContainer else colorScheme.secondary,
                maxLines = 1,
            )
            Text(
                date.day.toString(),
                style = AniTextStyles.macTitle.copy(fontSize = 20.sp, lineHeight = 26.sp),
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = if (isToday) colorScheme.onSecondaryContainer else AniColors.labelSoft,
                maxLines = 1,
            )
        }
        if (!enabled) return@Column
        for (row in appleScheduleRows(items, date, today)) {
            when (row) {
                is AppleScheduleRow.Item -> ScheduleCard(row, quickFollowState, onClick = { onClickItem(row.item) })
                is AppleScheduleRow.Placeholder -> PlaceholderCard()
                is AppleScheduleRow.Now -> NowLine(row.time)
            }
        }
    }
}

/**
 * 番剧卡片: card 底、圆角 10、内距 8. 第一行 (高 22) 时间 12/600 (已播出 label2, 未播出 label; 时间未定 11 号)
 * + 右侧 22 圆形追番钮; 第二行番名 13/17 W600 最多两行, 集数 12 label2.
 */
@Composable
private fun ScheduleCard(
    row: AppleScheduleRow.Item,
    quickFollowState: QuickFollowState?,
    onClick: () -> Unit,
) {
    val item = row.item
    val colorScheme = MaterialTheme.colorScheme
    val title = if (LocalSubjectAppearanceSettings.current.useOriginalTitle) item.subjectOriginalTitle else item.subjectTitle
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colorScheme.surfaceContainerLow)
            .clickable(
                onClickLabel = stringResource(Lang.exploration_schedule_view_details),
                role = Role.Button,
                onClick = onClick,
            )
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(Modifier.fillMaxWidth().height(22.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                if (row.showTime) {
                    val time: LocalTime? = item.time
                    Text(
                        if (time != null) {
                            ScheduleItemDefaults.renderTime(null, time)
                        } else {
                            stringResource(Lang.exploration_schedule_time_unknown)
                        },
                        style = AniTextStyles.macCaption.copy(fontSize = if (time == null) 11.sp else 12.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = if (row.aired) colorScheme.secondary else colorScheme.onSurface,
                        maxLines = 1,
                    )
                }
            }
            if (quickFollowState != null) {
                QuickFollowAnchor(quickFollowState, item.subjectId) { followed, enabled, onClickFollow ->
                    MacFollowToggle(
                        followed = followed,
                        enabled = enabled,
                        onClick = onClickFollow,
                        contentDescription = if (followed) {
                            stringResource(Lang.exploration_quick_follow_followed, title)
                        } else {
                            stringResource(Lang.exploration_quick_follow, title)
                        },
                        Modifier.testTag(MacScheduleTestTags.follow(item.subjectId)),
                    )
                }
            }
        }
        Text(
            title,
            style = AniTextStyles.macCallout.copy(lineHeight = 17.sp),
            fontWeight = FontWeight.SemiBold,
            color = colorScheme.onSurface,
            maxLines = 2,
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

/**
 * 22 圆形追番切换钮 (点击区 28): 已追番为 tint 底 + accent 勾, 未追番为 fill 底 + label2「+」.
 */
@Composable
internal fun MacFollowToggle(
    followed: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics {
                this.contentDescription = contentDescription
                selected = followed
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (followed) colorScheme.secondaryContainer else colorScheme.surfaceContainerHighest),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (followed) AppleIcons.check(2.6f) else AppleIcons.plus(2.6f),
                contentDescription = null,
                Modifier.size(12.dp),
                tint = if (followed) colorScheme.primary else colorScheme.secondary,
            )
        }
    }
}

/**
 * 加载中的占位卡片, 版式同 [ScheduleCard].
 */
@Composable
private fun PlaceholderCard() {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text("00:00", Modifier.placeholder(true), style = AniTextStyles.macCaption)
        Text("Placeholder title", Modifier.placeholder(true), style = AniTextStyles.macCallout)
        Text("Placeholder", Modifier.placeholder(true), style = AniTextStyles.macCaption)
    }
}

/**
 * 现在时间线: 时间 11/700 accent + 6 圆点 + 2 高横线, 全部 accent.
 */
@Composable
private fun NowLine(time: LocalTime) {
    val color = MaterialTheme.colorScheme.primary
    Row(
        Modifier.fillMaxWidth().height(16.dp).testTag(MacScheduleTestTags.Now),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            ScheduleItemDefaults.renderTime(null, time),
            style = AniTextStyles.macCaption.copy(fontSize = 11.sp),
            fontWeight = FontWeight.Bold,
            color = color,
            maxLines = 1,
        )
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Box(Modifier.weight(1f).height(2.dp).clip(RoundedCornerShape(1.dp)).background(color))
    }
}

internal object MacScheduleTestTags {
    const val PreviousWeek = "mac-schedule-previous-week"
    const val NextWeek = "mac-schedule-next-week"
    const val Today = "mac-schedule-today"
    const val Now = "mac-schedule-now"
    fun day(date: LocalDate) = "mac-schedule-day-$date"
    fun follow(subjectId: Int) = "mac-schedule-follow-$subjectId"
}

