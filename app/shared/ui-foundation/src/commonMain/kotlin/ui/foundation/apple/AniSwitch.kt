/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.AniColors

/**
 * C7 开关. 轨道 48×28, 白色圆钮 24; 开启为 [AniColors.accentFill], 关闭为 separator.
 *
 * 关闭时的轨道与背景的对比不到 3:1, 开关状态靠圆钮位置与开启时的颜色区分 (01 §1).
 *
 * @param onCheckedChange 为 `null` 时开关本身不可点击, 由所在的行处理点击; 状态仍写入语义.
 */
@Composable
fun AniSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val trackColor by animateColorAsState(
        if (checked) AniColors.accentFill else MaterialTheme.colorScheme.outlineVariant,
        label = "AniSwitch track",
    )
    val thumbOffset by animateDpAsState(
        if (checked) AniSwitchDefaults.TrackWidth - AniSwitchDefaults.ThumbSize - AniSwitchDefaults.ThumbPadding
        else AniSwitchDefaults.ThumbPadding,
        label = "AniSwitch thumb",
    )
    val interaction = if (onCheckedChange != null) {
        Modifier
            .height(AniSwitchDefaults.TouchHeight)
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Switch,
                onValueChange = onCheckedChange,
            )
    } else {
        Modifier.semantics {
            role = Role.Switch
            toggleableState = ToggleableState(checked)
        }
    }
    Box(
        modifier.then(interaction).alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(AniSwitchDefaults.TrackWidth, AniSwitchDefaults.TrackHeight)
                .background(trackColor, RoundedCornerShape(AniSwitchDefaults.TrackHeight / 2)),
            contentAlignment = Alignment.CenterStart,
        ) {
            Box(
                Modifier
                    .offset(x = thumbOffset)
                    .size(AniSwitchDefaults.ThumbSize)
                    .dropShadow(
                        CircleShape,
                        Shadow(
                            radius = 3.dp,
                            color = Color.Black,
                            offset = DpOffset(0.dp, 1.dp),
                            alpha = 0.25f,
                        ),
                    )
                    .background(Color.White, CircleShape),
            )
        }
    }
}

object AniSwitchDefaults {
    val TrackWidth = 48.dp
    val TrackHeight = 28.dp
    val ThumbSize = 24.dp
    val ThumbPadding = 2.dp

    /** 点击区域高度, 不小于 44 (02 §0). */
    val TouchHeight = 44.dp
}
