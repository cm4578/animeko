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
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C4 分页胶囊 (详情/评论、互动/通知、在看/想看/看过).
 *
 * 选中为 tint 底 + onTint W600; 未选中透明 + label2 W500. [count] 跟在文字后, W500.
 * 视觉高 32, 上下补到 44 的点击区.
 */
@Composable
fun PillTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    count: String? = null,
) {
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.secondary
    }
    PillLayout(
        modifier = modifier,
        interaction = { source ->
            selectable(
                selected = selected,
                interactionSource = source,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
        },
        containerColor = if (selected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
        contentPadding = PaddingValues(horizontal = 14.dp),
        spacing = 5,
    ) {
        Text(
            text,
            style = AniTextStyles.subhead,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
        )
        if (count != null) {
            Text(count, style = AniTextStyles.subhead, fontWeight = FontWeight.Medium, color = contentColor, maxLines = 1)
        }
    }
}

/**
 * C4 筛选胶囊 (带下拉箭头).
 *
 * 未套用为 card 底 + labelSoft W500; 已套用 ([applied]) 为 tint 底 + onTint W600.
 * 多个筛选胶囊放进 [FilterPillFlowRow] 换行排列, 不横向滚动.
 */
@Composable
fun FilterPill(
    text: String,
    applied: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor = if (applied) MaterialTheme.colorScheme.onSecondaryContainer else AniColors.labelSoft
    PillLayout(
        modifier = modifier,
        interaction = { source ->
            clickable(
                interactionSource = source,
                indication = null,
                role = Role.DropdownList,
                onClick = onClick,
            )
        },
        containerColor = if (applied) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerLow
        },
        contentPadding = PaddingValues(start = 14.dp, end = 10.dp),
        spacing = 4,
    ) {
        Text(
            text,
            style = AniTextStyles.subhead,
            fontWeight = if (applied) FontWeight.SemiBold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Icon(AppleIcons.chevronDown(), contentDescription = null, Modifier.size(13.dp), tint = contentColor)
    }
}

/**
 * 胶囊的换行排列: 左右与上下的视觉间距都是 8.
 *
 * 胶囊上下各有 6 的透明点击区, 所以行距取 -4, 让相邻两行胶囊的可见间距为 8.
 */
@Composable
fun FilterPillFlowRow(
    modifier: Modifier = Modifier,
    content: @Composable FlowRowScope.() -> Unit,
) {
    FlowRow(
        modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(-(PillDefaults.TouchHeight - PillDefaults.Height) + 8.dp),
        content = content,
    )
}

/**
 * 点击区是含上下透明区的外框, 按压效果只画在胶囊内.
 */
@Composable
private fun PillLayout(
    modifier: Modifier,
    interaction: Modifier.(MutableInteractionSource) -> Modifier,
    containerColor: Color,
    contentPadding: PaddingValues,
    spacing: Int,
    content: @Composable () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier.interaction(interactionSource).heightIn(min = PillDefaults.TouchHeight),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .height(PillDefaults.Height)
                .clip(RoundedCornerShape(PillDefaults.Height / 2))
                .background(containerColor)
                .indication(interactionSource, ripple())
                .padding(contentPadding),
            horizontalArrangement = Arrangement.spacedBy(spacing.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            content()
        }
    }
}

object PillDefaults {
    /** 胶囊的视觉高度. */
    val Height = 32.dp

    /** 含透明点击区的高度. */
    val TouchHeight = 44.dp
}
