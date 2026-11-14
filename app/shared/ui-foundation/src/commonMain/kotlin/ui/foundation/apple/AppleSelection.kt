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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * 手机选择模式 (05 D2) 行首的勾选圆圈 22: 选中为 accent 底白勾, 未选中为 label2 圆框.
 */
@Composable
fun AppleSelectionCircle(selected: Boolean, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier
            .size(22.dp)
            .clip(CircleShape)
            .then(
                if (selected) {
                    Modifier.background(colorScheme.primary)
                } else {
                    Modifier.border(1.5.dp, colorScheme.secondary, CircleShape)
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (selected) {
            Icon(AppleIcons.check(3f), contentDescription = null, Modifier.size(13.dp), tint = colorScheme.onPrimary)
        }
    }
}

/**
 * 手机选择模式 (05 D2) 的浮动工具栏: 高 [AppleSelectionToolbarDefaults.Height] 的 tabBg 胶囊, floatingShadow 与
 * floatingBorder, 放在浮动导航栏上方. 动作用 [AppleSelectionToolbarAction], 各占一格.
 */
@Composable
fun AppleSelectionToolbar(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(AppleSelectionToolbarDefaults.Height / 2)
    Row(
        modifier
            .fillMaxWidth()
            .height(AppleSelectionToolbarDefaults.Height)
            .dropShadow(shape, Shadow(radius = 24.dp, color = AniColors.floatingShadowColor, offset = DpOffset(0.dp, 8.dp)))
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .border(1.dp, AniColors.floatingBorder, shape),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/**
 * [AppleSelectionToolbar] 的一个动作, 与其他动作平分宽度: `callout` W600 accent 文字. 停用时不透明度
 * [AppleComponentDefaults.DisabledAlpha].
 */
@Composable
fun RowScope.AppleSelectionToolbarAction(
    text: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .weight(1f)
            .fillMaxHeight()
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = AniTextStyles.callout, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
    }
}

object AppleSelectionToolbarDefaults {
    /** 工具栏高度. 列表末尾留出此高度加 12 的空白, 最后一列才能滚到工具栏上方. */
    val Height = 56.dp
}
