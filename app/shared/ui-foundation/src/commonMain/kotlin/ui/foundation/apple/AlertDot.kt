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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.AniColors

/**
 * C12 提示点的尺寸. 环画在圆点外侧, 组件总尺寸为 `size + 2 * ringWidth`.
 */
enum class AlertDotStyle(val size: Dp, val ringWidth: Dp) {
    /** 行内与图标上. */
    Default(8.dp, 0.dp),

    /** 封面右上角, 外加白环. */
    OnCover(10.dp, 2.dp),

    /** macOS 铃铛上, 外加侧边栏色环. */
    MacBell(7.dp, 1.5.dp),
}

/**
 * C12 提示点: alert 色圆点.
 *
 * **只表示**新集数、未读通知、新版本、合并冲突, 其他状态不用. 圆点本身不带语义, 调用处在所属元素上说明.
 *
 * @param ringColor 环的颜色. [AlertDotStyle.OnCover] 用白色, [AlertDotStyle.MacBell] 用 [AniColors.sidebar].
 */
@Composable
fun AlertDot(
    modifier: Modifier = Modifier,
    style: AlertDotStyle = AlertDotStyle.Default,
    ringColor: Color = Color.White,
) {
    val ring = style.ringWidth
    Box(
        modifier
            .size(style.size + ring * 2)
            .background(AniColors.alert, CircleShape)
            .then(
                if (ring > 0.dp) Modifier.border(ring, ringColor, CircleShape) else Modifier,
            ),
    )
}
