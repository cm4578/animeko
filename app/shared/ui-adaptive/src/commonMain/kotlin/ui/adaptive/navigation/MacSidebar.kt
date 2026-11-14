/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.adaptive.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.apple.AlertDot
import me.him188.ani.app.ui.foundation.apple.AlertDotStyle
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AvatarRing
import me.him188.ani.app.ui.foundation.apple.AvatarRingSize
import me.him188.ani.app.ui.foundation.apple.MacWindowDefaults
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C17 macOS 主侧边栏: 宽 240, 底 [AniColors.sidebar], 右侧 1dp separator.
 *
 * 由上到下: 高 52 的顶部 (左侧让出红黄绿按钮, 右侧「隐藏侧边栏」按钮 28, 05 G31);
 * [search] ([MacSidebarSearchField], 左右 12, 与下方相隔 16); 可滚动的 [content]
 * ([MacSidebarSection], 分组间距 20); 底部 [footer] (顶部 1dp separator, 内距 10 12 12):
 * [MacSidebarAccount] + 铃铛 + 设定 ([MacSidebarIconButton]).
 */
@Composable
fun MacSidebar(
    onHideSidebar: () -> Unit,
    hideSidebarContentDescription: String,
    modifier: Modifier = Modifier,
    search: @Composable () -> Unit = {},
    footer: @Composable RowScope.() -> Unit = {},
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier
            .width(MacSidebarDefaults.Width)
            .fillMaxHeight()
            .background(AniColors.sidebar),
    ) {
        Column(Modifier.weight(1f).fillMaxHeight()) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(MacWindowDefaults.TitleBarHeight)
                    .padding(start = 20.dp, end = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Spacer(Modifier.weight(1f))
                MacSidebarIconButton(
                    onClick = onHideSidebar,
                    icon = AppleIcons.sidebar(),
                    contentDescription = hideSidebarContentDescription,
                )
            }
            Box(Modifier.padding(start = 12.dp, end = 12.dp, bottom = 16.dp)) {
                search()
            }
            Column(
                Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp)
                    .selectableGroup(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                content = content,
            )
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant),
            )
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(start = 12.dp, top = 10.dp, end = 12.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                content = footer,
            )
        }
        Box(
            Modifier
                .width(1.dp)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

/**
 * C17 侧边栏搜索框: 高 32、圆角 8、底 [AniColors.sidebarField]; 14 号放大镜 + 13 号文字 + 右侧 [shortcutHint] 11 号 label2.
 *
 * 可直接输入 (05 G11). 搜索中 ([active]) 加 1.5 accent 内描边, 框内显示查询词.
 *
 * @param onSearch 按下 Enter 时调用.
 */
@Composable
fun MacSidebarSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    shortcutHint: String? = "⌘K",
    active: Boolean = query.isNotEmpty(),
    onSearch: () -> Unit = {},
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(8.dp)
    Row(
        modifier
            .fillMaxWidth()
            .height(MacSidebarDefaults.SearchFieldHeight)
            .clip(shape)
            .background(AniColors.sidebarField)
            .border(1.5.dp, if (active) colorScheme.primary else Color.Transparent, shape)
            .padding(start = 10.dp, end = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(AppleIcons.search(), contentDescription = null, Modifier.size(14.dp), tint = colorScheme.secondary)
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = AniTextStyles.macCallout.copy(color = colorScheme.onSurface),
            cursorBrush = SolidColor(colorScheme.primary),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearch() }),
            decorationBox = { innerTextField ->
                Box(contentAlignment = Alignment.CenterStart) {
                    if (query.isEmpty()) {
                        Text(placeholder, style = AniTextStyles.macCallout, color = colorScheme.secondary, maxLines = 1)
                    }
                    innerTextField()
                }
            },
        )
        if (shortcutHint != null) {
            Text(
                shortcutHint,
                style = AniTextStyles.macSidebarSection,
                fontWeight = FontWeight.Normal,
                color = colorScheme.secondary,
                maxLines = 1,
            )
        }
    }
}

/**
 * C17 侧边栏分组: 标题 `macSidebarSection` label2 (内距 0 10 6), 下方 [MacSidebarItem], 项与项之间 2.
 */
@Composable
fun MacSidebarSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            title,
            Modifier
                .padding(start = 10.dp, end = 10.dp, bottom = 6.dp)
                .semantics { heading() },
            style = AniTextStyles.macSidebarSection,
            color = MaterialTheme.colorScheme.secondary,
        )
        content()
    }
}

/**
 * C17 侧边栏项目: 高 32、圆角 8、内距 0 10、间距 10; 18 号图标 + `macBody`.
 *
 * 选中为 tint 底 + onTint W600 + accent 图标; 未选中为 label W500 + label2 图标, 悬停为 hover 底.
 * 推入的详情页沿用来源入口的高亮 (04 §1.3).
 *
 * @param alert 行尾显示 C12 提示点 (追番有新集数).
 * @param count 行尾的数字 (快取进行中的下载数), 12 号等宽数字 label2, 选中时 onTint.
 */
@Composable
fun MacSidebarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    alert: Boolean = false,
    count: String? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val hovered = interactionSource.collectIsHoveredAsState().value
    val background = when {
        selected -> colorScheme.secondaryContainer
        hovered -> AniColors.hover
        else -> Color.Transparent
    }
    Row(
        modifier
            .fillMaxWidth()
            .height(MacSidebarDefaults.ItemHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(background)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            Modifier.size(18.dp),
            tint = if (selected) colorScheme.primary else colorScheme.secondary,
        )
        Text(
            label,
            Modifier.weight(1f),
            style = AniTextStyles.macBody,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (alert) {
            AlertDot()
        }
        if (count != null) {
            Text(
                count,
                style = AniTextStyles.macCaption.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.Medium,
                color = if (selected) colorScheme.onSecondaryContainer else colorScheme.secondary,
                maxLines = 1,
            )
        }
    }
}

/**
 * C17 底部帐号行: C11 28 号头像 + 名称 13 W600 + [status] 11 label2 (「N 项待同步」, 05 G15).
 * 放在 [MacSidebar] 的 `footer` 内, 通常加 `Modifier.weight(1f)`.
 */
@Composable
fun MacSidebarAccount(
    name: String,
    status: String,
    initial: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: (@Composable () -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val hovered = interactionSource.collectIsHoveredAsState().value
    Row(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (hovered) AniColors.hover else Color.Transparent)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .padding(start = 6.dp, top = 6.dp, end = 8.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarRing(initial = initial, size = AvatarRingSize.MacSidebar, gapColor = AniColors.sidebar, image = image)
        Column(Modifier.weight(1f)) {
            Text(
                name,
                style = AniTextStyles.macCallout,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                status,
                style = AniTextStyles.macSidebarSection,
                fontWeight = FontWeight.Normal,
                color = colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * C17 侧边栏的图标按钮 (隐藏侧边栏、铃铛、设定): 28×28、圆角 6, 18 号 label2 图标.
 *
 * @param selected 选中 (通知中心打开时的铃铛) 为 tint 底 + accent 图标.
 * @param alert 在图标右上显示 C12 提示点 (7 号 + 1.5 侧边栏色环), 只表示有未读, 不显示数字 (05 B2).
 */
@Composable
fun MacSidebarIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    alert: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val interactionSource = remember { MutableInteractionSource() }
    val hovered = interactionSource.collectIsHoveredAsState().value
    val background = when {
        selected -> colorScheme.secondaryContainer
        hovered -> AniColors.hover
        else -> Color.Transparent
    }
    Box(
        modifier
            .size(MacSidebarDefaults.IconButtonSize)
            .clip(RoundedCornerShape(6.dp))
            .background(background)
            .clickable(interactionSource = interactionSource, indication = null, role = Role.Button, onClick = onClick)
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            icon,
            contentDescription = null,
            Modifier.size(18.dp),
            tint = if (selected) colorScheme.primary else colorScheme.secondary,
        )
        if (alert) {
            AlertDot(
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 4.dp, end = 4.dp),
                style = AlertDotStyle.MacBell,
                ringColor = if (selected) colorScheme.secondaryContainer else AniColors.sidebar,
            )
        }
    }
}

object MacSidebarDefaults {
    val Width = 240.dp
    val ItemHeight = 32.dp
    val SearchFieldHeight = 32.dp
    val IconButtonSize = 28.dp
}
