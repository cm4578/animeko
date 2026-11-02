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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C5 分组列表: 分组标题 + [GroupedListCard] + 分组说明.
 *
 * 分组之间相隔 [GroupedListDefaults.SectionSpacing], 由调用处排列.
 *
 * @param title 分组标题, `footnote` label2, 左内距 16, 与卡片间距 8.
 * @param footer 分组下方说明, `caption` label2, 内距 8 16.
 */
@Composable
fun GroupedListSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    footer: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier) {
        if (title != null) {
            Text(
                title,
                Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                    .semantics { heading() },
                style = AniTextStyles.footnote,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        GroupedListCard(content = content)
        if (footer != null) {
            Text(
                footer,
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = AniTextStyles.caption,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}

/**
 * C5 列表卡: card 底, 圆角 16, 裁切内容. 行间分隔线用 [AppleDivider], 从文字起点开始.
 */
@Composable
fun GroupedListCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(GroupedListDefaults.CornerRadius))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        content = content,
    )
}

object GroupedListDefaults {
    val CornerRadius = 16.dp

    /** 分组之间的间距. */
    val SectionSpacing = 24.dp
}
