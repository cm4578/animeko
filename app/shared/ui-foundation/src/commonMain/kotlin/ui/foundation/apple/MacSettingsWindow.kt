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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C20 macOS 设定窗口的左侧分类栏: 宽 232, 底 [AniColors.sidebar], 右侧 1dp separator, 内距 0 12.
 *
 * 顶部 52 让出红黄绿按钮; 其下为 [account] ([MacSettingsAccountItem]), 再下 16 起为 [content]:
 * 四组 [MacSettingsCategoryGroup], 组与组之间 12.
 */
@Composable
fun MacSettingsCategoryBar(
    account: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(
        modifier
            .width(MacSettingsWindowDefaults.CategoryBarWidth)
            .fillMaxHeight()
            .background(AniColors.sidebar),
    ) {
        Column(
            Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(top = MacWindowDefaults.TitleBarHeight)
                .verticalScroll(rememberScrollState())
                .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
                .selectableGroup(),
        ) {
            account()
            Spacer(Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), content = content)
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
 * C20 分类栏的帐号行: 内距 8 10、圆角 8; C11 32 号头像 + 名称 13/18 W600 + [subtitle] (「帐号与同步」) 11/14 label2.
 * 选中为 tint 底.
 */
@Composable
fun MacSettingsAccountItem(
    name: String,
    subtitle: String,
    initial: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    image: (@Composable () -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val background = categoryBackground(selected)
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(background.color)
            .selectable(
                selected = selected,
                interactionSource = background.interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarRing(
            initial = initial,
            size = AvatarRingSize.MacSettingsCategory,
            gapColor = if (selected) colorScheme.secondaryContainer else AniColors.sidebar,
            image = image,
        )
        Column(Modifier.weight(1f)) {
            Text(
                name,
                style = AniTextStyles.macCallout,
                fontWeight = FontWeight.SemiBold,
                color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle,
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
 * C20 分类组: 标题 `macSidebarSection` label2 (内距 0 10 4), 下方 [MacSettingsCategoryItem], 项与项之间 1.
 */
@Composable
fun MacSettingsCategoryGroup(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(1.dp)) {
        Text(
            title,
            Modifier
                .padding(start = 10.dp, end = 10.dp, bottom = 4.dp)
                .semantics { heading() },
            style = AniTextStyles.macSidebarSection,
            color = MaterialTheme.colorScheme.secondary,
        )
        content()
    }
}

/**
 * C20 分类项: 高 28、圆角 6、内距 0 10; 16 号图标 + 间距 8 + `macCallout` 文字.
 *
 * 选中为 tint 底 + onTint W600 + accent 图标; 未选中为 label + label2 图标, 悬停为 hover 底.
 * 子页 (Bangumi 同步、Peer 过滤) 打开时, 父分类保持选中.
 *
 * @param alert 行尾显示 C12 提示点 (软件更新有新版本).
 */
@Composable
fun MacSettingsCategoryItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    alert: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    val background = categoryBackground(selected)
    Row(
        modifier
            .fillMaxWidth()
            .height(MacSettingsWindowDefaults.CategoryItemHeight)
            .clip(RoundedCornerShape(6.dp))
            .background(background.color)
            .selectable(
                selected = selected,
                interactionSource = background.interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            icon,
            contentDescription = null,
            Modifier.size(16.dp),
            tint = if (selected) colorScheme.primary else colorScheme.secondary,
        )
        Text(
            label,
            Modifier.weight(1f),
            style = AniTextStyles.macCallout,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) colorScheme.onSecondaryContainer else colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (alert) {
            AlertDot()
        }
    }
}

/**
 * C20 右侧窗格: 底色 bg; 标题栏高 52、内距 0 28, `macWindowTitle` 标题; 内容左右 28, 可滚动.
 *
 * 分组用 [GroupedListSection] (macOS 圆角 14), 行用 [SettingsRow] 系列 (macOS 规格).
 *
 * @param back 子页 (Bangumi 同步、Peer 过滤) 的返回按钮, 用 [MacSettingsBackButton].
 */
@Composable
fun MacSettingsPane(
    title: String,
    modifier: Modifier = Modifier,
    back: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(MacWindowDefaults.TitleBarHeight)
                .padding(horizontal = MacSettingsWindowDefaults.PanePadding),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            back?.invoke()
            Text(
                title,
                Modifier.semantics { heading() },
                style = AniTextStyles.macWindowTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(
                    start = MacSettingsWindowDefaults.PanePadding,
                    top = 4.dp,
                    end = MacSettingsWindowDefaults.PanePadding,
                    bottom = MacSettingsWindowDefaults.PanePadding,
                ),
            verticalArrangement = Arrangement.spacedBy(GroupedListDefaults.SectionSpacing),
            content = content,
        )
    }
}

/**
 * C20 子页标题栏左侧的返回按钮「‹ 父页名」: 次要按钮样式 (fill 底、高 28、圆角 6), `macCallout` W500 label.
 */
@Composable
fun MacSettingsBackButton(
    parentTitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .height(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = 6.dp, end = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            AppleIcons.back(2.4f),
            contentDescription = null,
            Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            parentTitle,
            style = AniTextStyles.macCallout,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

object MacSettingsWindowDefaults {
    val CategoryBarWidth = 232.dp
    val CategoryItemHeight = 28.dp

    /** 窗格标题栏与内容的左右内距. */
    val PanePadding = 28.dp
}

private class CategoryBackground(
    val color: Color,
    val interactionSource: MutableInteractionSource,
)

/**
 * 分类栏项目的底色: 选中为 tint, 悬停为 hover.
 */
@Composable
private fun categoryBackground(selected: Boolean): CategoryBackground {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered = interactionSource.collectIsHoveredAsState().value
    val color = when {
        selected -> MaterialTheme.colorScheme.secondaryContainer
        hovered -> AniColors.hover
        else -> Color.Transparent
    }
    return CategoryBackground(color, interactionSource)
}
