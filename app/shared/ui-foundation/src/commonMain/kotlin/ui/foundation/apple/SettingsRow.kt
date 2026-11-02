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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C6 设定行.
 *
 * 手机高 52 (带图标 48), 带说明时上下内距 12、至少 60; 内距 0 12 0 16. 标题 `body` label, 说明 `footnote` label2.
 * macOS 高 48, 带说明时上下内距 10、至少 60; 内距 0 16. 标题 `macBody` label, 说明 `macCaption` label2.
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
 * C6 就地执行的动作行 (复制、导入、登出、检查更新): 标题为 accent, 手机 `body`, macOS `macBody`.
 *
 * 危险动作也用此样式, **不用红色**, 由确认对话框防误触.
 */
@Composable
fun SettingsActionRow(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    showDivider: Boolean = false,
    enabled: Boolean = true,
) {
    SettingsRowLayout(
        modifier = modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        description = description,
        icon = null,
        showDivider = showDivider,
        enabled = enabled,
        title = {
            Text(
                text,
                style = if (isMacStyle) AniTextStyles.macBody else AniTextStyles.body,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        trailing = {},
    )
}

/**
 * C6 行尾 `›` (进入下一页) 或 `↗` (打开外部链接、系统设置), 箭头为 label2.
 *
 * - `›`: 提示点、值、箭头间距 12. 手机箭头 16 号、线宽 2; macOS 14 号、线宽 2.2.
 * - `↗`: 值与箭头间距手机 6、macOS 4. 手机箭头 15 号、线宽 2.2; macOS 14 号、线宽 2.2.
 *
 * @param value 当前值, label2. `›` 在手机用 `subhead`, `↗` 在手机用 `callout`; macOS 都用 `macCallout`.
 * @param alert 在值前显示 C12 提示点 (有新版本等).
 * @param external `true` 时显示 `↗`.
 */
@Composable
fun SettingsRowNavigation(
    value: String? = null,
    alert: Boolean = false,
    external: Boolean = false,
) {
    val mac = isMacStyle
    val spacing = when {
        !external -> 12.dp
        mac -> 4.dp
        else -> 6.dp
    }
    Row(horizontalArrangement = Arrangement.spacedBy(spacing), verticalAlignment = Alignment.CenterVertically) {
        if (alert) {
            AlertDot()
        }
        if (value != null) {
            SettingsRowValueText(
                value,
                style = when {
                    mac -> AniTextStyles.macCallout
                    external -> AniTextStyles.callout
                    else -> AniTextStyles.subhead
                },
            )
        }
        Icon(
            when {
                !external -> AppleIcons.chevronRight(if (mac) 2.2f else 2f)
                mac -> AppleIcons.macExternalLink()
                else -> AppleIcons.externalLink(2.2f)
            },
            contentDescription = null,
            Modifier.size(
                when {
                    mac -> 14.dp
                    external -> 15.dp
                    else -> 16.dp
                },
            ),
            tint = MaterialTheme.colorScheme.secondary,
        )
    }
}

/**
 * C6 行尾弹出值: 当前值 + 上下双箭头. 点击后显示的选单由调用处提供.
 *
 * 手机为 `callout` label2 文字 + 14 号箭头 (线宽 2.4), 间距 4.
 * macOS 为弹出按钮: fill 底、高 24、圆角 6, `macCallout` label 文字 + 12 号 label2 箭头.
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
        SettingsRowValueText(value, style = AniTextStyles.callout)
        Icon(
            AppleIcons.upDown(),
            contentDescription = null,
            Modifier.size(14.dp),
            tint = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun SettingsRowValueText(value: String, style: TextStyle) {
    Text(
        value,
        style = style,
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
    val minHeight = when {
        description != null -> SettingsRowDefaults.HeightWithDescription
        mac -> SettingsRowDefaults.MacHeight
        icon != null -> SettingsRowDefaults.HeightWithIcon
        else -> SettingsRowDefaults.Height
    }
    Box(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
                .heightIn(min = minHeight)
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
                        style = if (mac) AniTextStyles.macCaption else AniTextStyles.footnote,
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
    /** 手机无图标的行高 (设定子页). */
    val Height = 52.dp

    /** 手机带图标的行高 (「我的」页). */
    val HeightWithIcon = 48.dp

    /** macOS 行高. */
    val MacHeight = 48.dp

    /** 带说明时的最小行高, 手机与 macOS 相同. */
    val HeightWithDescription = 60.dp

    /** macOS 弹出按钮的高度. */
    val MacPopupButtonHeight = 24.dp

    val IconTileSize = 30.dp

    /** 无图标时分隔线的起点. */
    val DividerStart = 16.dp

    /** 有图标时分隔线的起点: 16 + 30 + 12. */
    val DividerStartWithIcon = 58.dp
}
