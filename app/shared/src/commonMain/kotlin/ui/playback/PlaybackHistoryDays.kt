/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.playback

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.exploration_schedule_weekday_friday
import me.him188.ani.app.ui.lang.exploration_schedule_weekday_monday
import me.him188.ani.app.ui.lang.exploration_schedule_weekday_saturday
import me.him188.ani.app.ui.lang.exploration_schedule_weekday_sunday
import me.him188.ani.app.ui.lang.exploration_schedule_weekday_thursday
import me.him188.ani.app.ui.lang.exploration_schedule_weekday_tuesday
import me.him188.ani.app.ui.lang.exploration_schedule_weekday_wednesday
import me.him188.ani.app.ui.lang.playback_history_episode_label
import me.him188.ani.app.ui.lang.playback_history_group_date
import me.him188.ani.app.ui.lang.playback_history_group_date_with_year
import me.him188.ani.app.ui.lang.playback_history_progress_unknown_duration
import me.him188.ani.app.ui.lang.playback_history_unknown_episode
import me.him188.ani.app.ui.lang.playback_history_yesterday
import me.him188.ani.app.ui.lang.schedule_today
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Instant

/**
 * Apple 版播放记录 (03 §4.2、04 §4.2) 的一个日期分组: 同一天 (本地时区) 的记录, 新的在前.
 */
@Immutable
internal data class PlaybackHistoryDayGroup(
    val date: LocalDate,
    val items: List<PlaybackHistoryUiItem>,
)

/**
 * 按 [timeZone] 的日期把记录分组. 只在界面上分组, 记录本身不变. [items] 已按更新时间从新到旧排列, 分组保持这个顺序.
 */
internal fun groupPlaybackHistoryByDay(
    items: List<PlaybackHistoryUiItem>,
    timeZone: TimeZone,
): List<PlaybackHistoryDayGroup> {
    return items
        .groupBy { it.updatedAtMillis.toLocalDate(timeZone) }
        .map { (date, dayItems) -> PlaybackHistoryDayGroup(date, dayItems) }
}

/**
 * 分组标题: 「今天」「昨天」, 其他日期为「10 月 27 日（週二）」, 不是今年的加上年份.
 */
@Composable
internal fun playbackHistoryDayLabel(date: LocalDate, today: LocalDate): String {
    return when (date) {
        today -> stringResource(Lang.schedule_today)
        today.minus(1, DateTimeUnit.DAY) -> stringResource(Lang.playback_history_yesterday)
        else -> {
            val weekday = stringResource(date.dayOfWeek.shortNameResource())
            if (date.year == today.year) {
                stringResource(Lang.playback_history_group_date, date.month.number, date.day, weekday)
            } else {
                stringResource(Lang.playback_history_group_date_with_year, date.year, date.month.number, date.day, weekday)
            }
        }
    }
}

/**
 * 记录的第二行: 「第 9 集 · 21:40」. 没有集数时用集名, 时间为记录更新时间的时与分 (补零到两位).
 */
@Composable
internal fun playbackHistoryEpisodeLine(item: PlaybackHistoryUiItem, timeZone: TimeZone): String {
    val episode = item.episodeSort?.let { stringResource(Lang.playback_history_episode_label, it.formatEpisodeSort()) }
        ?: item.episodeName?.takeIf { it.isNotBlank() }
        ?: stringResource(Lang.playback_history_unknown_episode)
    return "$episode · ${formatPlaybackHistoryTime(item.updatedAtMillis, timeZone)}"
}

/**
 * 进度文字「06:12 / 23:40」. 时长未知时后半为现有的「未知」.
 */
@Composable
internal fun playbackHistoryProgressText(item: PlaybackHistoryUiItem): String {
    val duration = item.durationMillis?.takeIf { it > 0L }?.let(::formatPlaybackPosition)
        ?: stringResource(Lang.playback_history_progress_unknown_duration)
    return "${formatPlaybackPosition(item.positionMillis)} / $duration"
}

/**
 * 观看进度 0–1; 时长未知时为 `null`, 不显示进度条.
 */
internal val PlaybackHistoryUiItem.progress: Float?
    get() = durationMillis?.takeIf { it > 0L }?.let { (positionMillis.toFloat() / it).coerceIn(0f, 1f) }

/**
 * 时与分, 都补零到两位, 例如 `09:05`.
 */
internal fun formatPlaybackHistoryTime(millis: Long, timeZone: TimeZone): String {
    val time = Instant.fromEpochMilliseconds(millis).toLocalDateTime(timeZone)
    return "${time.hour.toString().padStart(2, '0')}:${time.minute.toString().padStart(2, '0')}"
}

/**
 * 播放位置: 不足一小时为 `06:12`, 否则为 `1:02:03`.
 */
internal fun formatPlaybackPosition(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600 / 60).toString().padStart(2, '0')
    val seconds = (totalSeconds % 60).toString().padStart(2, '0')
    return if (hours > 0) "$hours:$minutes:$seconds" else "$minutes:$seconds"
}

private fun Long.toLocalDate(timeZone: TimeZone): LocalDate =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(timeZone).date

private fun DayOfWeek.shortNameResource(): StringResource = when (this) {
    DayOfWeek.MONDAY -> Lang.exploration_schedule_weekday_monday
    DayOfWeek.TUESDAY -> Lang.exploration_schedule_weekday_tuesday
    DayOfWeek.WEDNESDAY -> Lang.exploration_schedule_weekday_wednesday
    DayOfWeek.THURSDAY -> Lang.exploration_schedule_weekday_thursday
    DayOfWeek.FRIDAY -> Lang.exploration_schedule_weekday_friday
    DayOfWeek.SATURDAY -> Lang.exploration_schedule_weekday_saturday
    DayOfWeek.SUNDAY -> Lang.exploration_schedule_weekday_sunday
}
