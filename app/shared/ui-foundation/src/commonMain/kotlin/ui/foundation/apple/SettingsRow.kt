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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C6 设定行.
 *
 * 手机高 48, 带说明时至少 60; 内距 0 12 0 16. 标题 `body` label, 说明 `caption` label2.
 * macOS 高 48, 带说明时至少 60; 内距 0 16. 标题 `macBody` label, 说明 `macCaption` label2.
 * 行尾用下列其中一种, 不要混用: [SettingsRowNavigation] (`›` 或 `↗`)、[AniSwitch] (见 [SettingsSwitchRow])、
 * [SettingsRowPopupValue]; 就地执行的动作用 [SettingsActionRow].
 *
 * @param icon 30×30 的图标小方块. **只用于手机「我的」页**, 设定子页与 macOS 设定窗口的行不放图标.
 * @param showDivider 在行顶画分隔线, 用于卡片内第二行起. 有图标时从 58 开始, 无图标时从 16 开始.
 */
@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector? = null,
    showDivider: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    SettingsRowLayout(
        modifier = if (onClick != null) {
            modifier.clickable(enabled = enabled, onClick = onClick)
        } else {
            modifier
        },
        description = description,
        icon = icon,
        showDivider = showDivider,
        enabled = enabled,
        title = {
            Text(
                title,
                style = if (isMacStyle) AniTextStyles.macBody else AniTextStyles.body,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailing = trailing,
    )
}

/**
 * C6 布尔设定行. 整行可点击切换, 行尾为 [AniSwitch].
 */
@Composable
fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    showDivider: Boolean = false,
    enabled: Boolean = true,
) {
    SettingsRow(
        title = title,
        modifier = modifier.toggleable(
            value = checked,
            enabled = enabled,
            role = Role.Switch,
            onValueChange = onCheckedChange,
        ),
        description = description,
        showDivider = showDivider,
        enabled = enabled,
    ) {
        AniSwitch(checked = checked, onCheckedChange = null, enabled = enabled)
    }
}

/**
 * C6 就地执行的动作行 (复制、导入、登出、检查更新): 整行文字为 accent, 手机 `callout`, macOS `macBody`.
 *
 * 危险动作也用此样式, **不用红色**, 由确认对话框防误触.
 */
@Composable
fun SettingsActionRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDivider: Boolean = false,
    enabled: Boolean = true,
) {
    SettingsRowLayout(
        modifier = modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        description = null,
        icon = null,
        showDivider = showDivider,
        enabled = enabled,
        title = {
            Text(
                text,
                style = if (isMacStyle) AniTextStyles.macBody else AniTextStyles.callout,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        },
        trailing = {},
    )
}

/**
 * C6 行尾 `›` (进入下一页) 或 `↗` (打开外部链接、系统设置).
 *
 * @param value 当前值, label2; 手机 `subhead`, macOS `macCallout`.
 * @param alert 在值前显示 C12 提示点 (有新版本等).
 * @param external `true` 时显示 `↗`.
 */
@Composable
fun SettingsRowNavigation(
    value: String? = null,
    alert: Boolean = false,
    external: Boolean = false,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
        if (alert) {
            AlertDot()
        }
        if (value != null) {
            SettingsRowValueText(value)
        }
        Icon(
            if (external) AppleIcons.externalLink() else AppleIcons.chevronRight(),
            contentDescription = null,
            Modifier.size(SettingsRowDefaults.TrailingIconSize),
            tint = MaterialTheme.colorScheme.secondary,
        )
    }
}

/**
 * C6 行尾弹出值: 当前值 + 上下双箭头. 点击后显示的选单由调用处提供.
 *
 * 手机为 label2 文字 + 16 号箭头. macOS 为弹出按钮: fill 底、高 24、圆角 6, `macCallout` label 文字 + 12 号 label2 箭头.
 */
@Composable
fun SettingsRowPopupValue(
    value: String,
    modifier: Modifier = Modifier,
) {
    if (isMacStyle) {
        Row(
            modifier
                .height(SettingsRowDefaults.MacPopupButtonHeight)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(6.dp))
                .padding(start = 10.dp, end = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                value,
                style = AniTextStyles.macCallout,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Icon(
                AppleIcons.upDown(2.6f),
                contentDescription = null,
                Modifier.size(12.dp),
                tint = MaterialTheme.colorScheme.secondary,
            )
        }
        return
    }
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        SettingsRowValueText(value)
        Icon(
            AppleIcons.upDown(),
            contentDescription = null,
            Modifier.size(SettingsRowDefaults.TrailingIconSize),
            tint = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun SettingsRowValueText(value: String) {
    Text(
        value,
        style = if (isMacStyle) AniTextStyles.macCallout else AniTextStyles.subhead,
        color = MaterialTheme.colorScheme.secondary,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}

@Composable
private fun SettingsRowLayout(
    modifier: Modifier,
    description: String?,
    icon: ImageVector?,
    showDivider: Boolean,
    enabled: Boolean,
    title: @Composable () -> Unit,
    trailing: @Composable RowScope.() -> Unit,
) {
    val mac = isMacStyle
    val verticalPadding = when {
        description == null -> 0.dp
        mac -> 10.dp
        else -> 12.dp
    }
    Box(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
                .heightIn(
                    min = if (description != null) {
                        SettingsRowDefaults.HeightWithDescription
                    } else {
                        SettingsRowDefaults.Height
                    },
                )
                .padding(
                    start = 16.dp,
                    end = if (mac) 16.dp else 12.dp,
                    top = verticalPadding,
                    bottom = verticalPadding,
                ),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Box(
                    Modifier
                        .size(SettingsRowDefaults.IconTileSize)
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        Modifier.size(17.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                title()
                if (description != null) {
                    Text(
                        description,
                        style = if (mac) AniTextStyles.macCaption else AniTextStyles.caption,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
            trailing()
        }
        if (showDivider) {
            AppleDivider(
                Modifier.align(Alignment.TopStart),
                startIndent = if (icon != null) {
                    SettingsRowDefaults.DividerStartWithIcon
                } else {
                    SettingsRowDefaults.DividerStart
                },
            )
        }
    }
}

object SettingsRowDefaults {
    /** 行高, 手机与 macOS 相同. */
    val Height = 48.dp

    /** 带说明时的最小行高, 手机与 macOS 相同. */
    val HeightWithDescription = 60.dp

    /** macOS 弹出按钮的高度. */
    val MacPopupButtonHeight = 24.dp

    val IconTileSize = 30.dp
    val TrailingIconSize = 16.dp

    /** 无图标时分隔线的起点. */
    val DividerStart = 16.dp

    /** 有图标时分隔线的起点: 16 + 30 + 12. */
    val DividerStartWithIcon = 58.dp
}
