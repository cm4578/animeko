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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C21 检阅器: 主内容右侧, 宽 320, 底 [AniColors.sidebar], 左侧 1dp separator, 内距 12 16 16.
 *
 * 顶部为 `macWindowTitle` 标题 (W600) + 24 号关闭按钮; 下方内容可滚动, 区块间距 16, 区块用 [MacInspectorCard].
 * 由工具栏按钮或 ⌘I 开关 (05 C3); 打开时工具栏按钮为开启状态 ([MacToolbarIconButton] 的 `checked`).
 */
@Composable
fun MacInspector(
    title: String,
    onClose: () -> Unit,
    closeContentDescription: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier
            .width(MacInspectorDefaults.Width)
            .fillMaxHeight()
            .background(AniColors.sidebar),
    ) {
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(start = 16.dp, top = 12.dp, end = 16.dp),
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(28.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    title,
                    Modifier
                        .weight(1f)
                        .semantics { heading() },
                    style = AniTextStyles.macWindowTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .clickable(role = Role.Button, onClick = onClose)
                        .semantics { contentDescription = closeContentDescription },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        AppleIcons.close(),
                        contentDescription = null,
                        Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
        }
    }
}

/**
 * C21 检阅器内的小卡片: card 底、圆角 12. 可在上方带 12 W600 label2 的标题, 与卡片间距 6.
 */
@Composable
fun MacInspectorCard(
    modifier: Modifier = Modifier,
    title: String? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (title != null) {
            Text(
                title,
                Modifier
                    .padding(horizontal = 4.dp)
                    .semantics { heading() },
                style = AniTextStyles.macCaption,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .padding(contentPadding),
            content = content,
        )
    }
}

object MacInspectorDefaults {
    val Width = 320.dp
}
