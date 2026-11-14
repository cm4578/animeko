/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.schedule

import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduleScreenStateTest {
    private val today = LocalDate(2026, 10, 30)
    private val days = ScheduleDay.generateForRecentTwoWeeks(today)

    @Test
    fun `selects today by default`() {
        assertEquals(today, ScheduleScreenState { days }.selectedDay?.date)
    }

    @Test
    fun `selects the initial date`() {
        val date = LocalDate(2026, 10, 27)
        assertEquals(date, ScheduleScreenState(initialDate = date) { days }.selectedDay?.date)
    }

    @Test
    fun `selects today when the initial date is out of range`() {
        assertEquals(today, ScheduleScreenState(initialDate = LocalDate(2026, 12, 1)) { days }.selectedDay?.date)
    }
}
