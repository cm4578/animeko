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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import me.him188.ani.app.ui.foundation.apple.AppleComponentDefaults
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * [date] 所在一周的 7 天, 周一在前.
 */
internal fun currentWeek(date: LocalDate): List<LocalDate> {
    val monday = date.minus(DatePeriod(days = date.dayOfWeek.ordinal))
    return List(7) { monday.plus(DatePeriod(days = it)) }
}

/**
 * 7 栏星期条 (03 §3.2、§3.3): 每格高 52、圆角 14, 格距 2. 周名 `caption` label2, 日期 `headline` labelSoft 500;
 * [selectedDate] 为 tint 底 + onTint、日期 700. 停用的格子不能点, 内容 opacity 0.35.
 *
 * @param week 一周的 7 天, 见 [currentWeek].
 * @param cellModifier 每一格的 Modifier, 例如测试标签.
 */
@Composable
internal fun AppleWeekdayStrip(
    week: List<LocalDate>,
    selectedDate: LocalDate?,
    onClickDay: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: (LocalDate) -> Boolean = { true },
    cellModifier: (LocalDate) -> Modifier = { Modifier },
) {
    Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        for (date in week) {
            WeekdayCell(
                date,
                selected = date == selectedDate,
                enabled = isEnabled(date),
                onClick = { onClickDay(date) },
                Modifier.weight(1f).then(cellModifier(date)),
            )
        }
    }
}

@Composable
private fun WeekdayCell(
    date: LocalDate,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier
            .height(52.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) colorScheme.secondaryContainer else Color.Transparent)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.selected = selected }
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Text(
            renderWeekdayName(date.dayOfWeek),
            style = AniTextStyles.caption,
            color = if (selected) colorScheme.onSecondaryContainer else colorScheme.secondary,
            maxLines = 1,
        )
        Text(
            date.day.toString(),
            style = AniTextStyles.headline,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) colorScheme.onSecondaryContainer else AniColors.labelSoft,
            maxLines = 1,
        )
    }
}
