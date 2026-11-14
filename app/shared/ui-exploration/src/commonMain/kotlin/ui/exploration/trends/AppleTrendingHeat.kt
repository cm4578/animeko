/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.trends

import androidx.compose.runtime.Composable
import kotlin.math.roundToLong
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.exploration_trending_ranking_heat
import me.him188.ani.app.ui.lang.exploration_trending_ranking_heat_compact
import me.him188.ani.app.ui.lang.exploration_trending_ranking_heat_compact_divisor
import me.him188.ani.app.ui.lang.exploration_trending_ranking_heat_compact_value
import org.jetbrains.compose.resources.stringResource

/*
 * Apple 版热度排行的人数 (03 §4.1、04 §4.1, 05 G52): 不到 1 万为整数; 1 万以上缩写, 中文为「3.2 万」, 英文为「32.1K」.
 * 缩写的单位与除数由各语言的字串决定.
 */

/**
 * 人数在 [TrendingHeatCompactThreshold] 以上时缩写. 不到时为 `null`, 显示整数.
 */
internal const val TrendingHeatCompactThreshold = 10_000

/**
 * 缩写后的数字: 除以 [divisor] 后四舍五入到一位小数, 小数为 0 时省略. 例如除数 10000 时 32145 为「3.2」, 39999 为「4」.
 *
 * @return 不缩写 (不到 [TrendingHeatCompactThreshold]) 或除数无效时为 `null`.
 */
internal fun compactTrendingHeat(heat: Int, divisor: Int): String? {
    if (heat < TrendingHeatCompactThreshold || divisor <= 0) return null
    val tenths = (heat * 10.0 / divisor).roundToLong()
    val whole = tenths / 10
    val fraction = tenths % 10
    return if (fraction == 0L) whole.toString() else "$whole.$fraction"
}

@Composable
private fun trendingHeatDivisor(): Int =
    stringResource(Lang.exploration_trending_ranking_heat_compact_divisor).toIntOrNull() ?: TrendingHeatCompactThreshold

/**
 * iOS 列表行的人数: 「近 30 天 3.2 萬人在看」, 不到 1 万时为现有的「近 30 天 7559 人在看」.
 */
@Composable
internal fun renderTrendingHeatLine(heat: Int): String {
    val compact = compactTrendingHeat(heat, trendingHeatDivisor())
    return if (compact != null) {
        stringResource(Lang.exploration_trending_ranking_heat_compact, compact)
    } else {
        stringResource(Lang.exploration_trending_ranking_heat, heat.toString())
    }
}

/**
 * macOS 表格的人数: 「3.2 萬」, 不到 1 万时为整数. 后面由调用处接「人」.
 */
@Composable
internal fun renderTrendingHeatValue(heat: Int): String {
    val compact = compactTrendingHeat(heat, trendingHeatDivisor())
    return if (compact != null) {
        stringResource(Lang.exploration_trending_ranking_heat_compact_value, compact)
    } else {
        heat.toString()
    }
}
