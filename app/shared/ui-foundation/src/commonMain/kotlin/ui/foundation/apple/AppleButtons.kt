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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C8 有底色的按钮样式.
 */
enum class AppleButtonStyle {
    /** 每屏最多一个的主要动作: accentFill 底 + onAccentFill W600. */
    Primary,

    /** 并列的其他动作、危险动作: fill 底 + label W500. 危险动作不用红色. */
    Secondary,

    /** 次要但需要提示的动作 (立即同步、重新授权、查看详情): tint 底 + onTint W600. */
    Tinted,
}

/**
 * C8 全圆角按钮.
 *
 * @param height 一般 44–48; 登录流程的满版按钮用 [AppleButtonDefaults.LoginHeight].
 */
@Composable
fun AppleButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppleButtonStyle = AppleButtonStyle.Primary,
    enabled: Boolean = true,
    height: Dp = AppleButtonDefaults.Height,
    contentPadding: PaddingValues = AppleButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    val containerColor: Color
    val contentColor: Color
    val fontWeight: FontWeight
    when (style) {
        AppleButtonStyle.Primary -> {
            containerColor = AniColors.accentFill
            contentColor = AniColors.onAccentFill
            fontWeight = FontWeight.SemiBold
        }

        AppleButtonStyle.Secondary -> {
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            contentColor = MaterialTheme.colorScheme.onSurface
            fontWeight = FontWeight.Medium
        }

        AppleButtonStyle.Tinted -> {
            containerColor = MaterialTheme.colorScheme.secondaryContainer
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
            fontWeight = FontWeight.SemiBold
        }
    }
    Row(
        modifier
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
            .heightIn(min = height)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(AniTextStyles.callout.copy(fontWeight = fontWeight)) {
                content()
            }
        }
    }
}

/**
 * C8 文字按钮: 行内链接 (更换、管理、全部 28 话), accent 色, 无底.
 */
@Composable
fun AppleTextButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
            .heightIn(min = AppleComponentDefaults.MinTouchSize)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.primary) {
            ProvideTextStyle(AniTextStyles.callout) {
                content()
            }
        }
    }
}

/**
 * C8 圆形图标按钮的底色.
 */
enum class AppleCircleButtonStyle {
    /** 工具按钮: fill 底 + label 图标. */
    Fill,

    /** 叠在图片或视频上: [AppleComponentDefaults.MediaScrim] 底 + 白色图标. */
    Media,
}

/**
 * C8 圆形图标按钮. 视觉尺寸为 [size], 外围补到 44 的透明点击区.
 *
 * @param size 36 或 40.
 */
@Composable
fun AppleCircleButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    style: AppleCircleButtonStyle = AppleCircleButtonStyle.Fill,
    size: Dp = AppleButtonDefaults.CircleSize,
    enabled: Boolean = true,
) {
    val containerColor: Color
    val iconColor: Color
    when (style) {
        AppleCircleButtonStyle.Fill -> {
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            iconColor = MaterialTheme.colorScheme.onSurface
        }

        AppleCircleButtonStyle.Media -> {
            containerColor = AppleComponentDefaults.MediaScrim
            iconColor = Color.White
        }
    }
    CircleButtonLayout(
        onClick = onClick,
        icon = icon,
        contentDescription = contentDescription,
        containerColor = containerColor,
        iconColor = iconColor,
        size = size,
        enabled = enabled,
        modifier = modifier,
    )
}

/**
 * C8「追番」切换按钮: 未追番为 fill 底 + label2 加号; 已追番为 tint 底 + accent 勾.
 */
@Composable
fun AppleFollowButton(
    followed: Boolean,
    onClick: () -> Unit,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    CircleButtonLayout(
        onClick = onClick,
        icon = if (followed) AppleIcons.check() else AppleIcons.plus(),
        contentDescription = contentDescription,
        containerColor = if (followed) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
        iconColor = if (followed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
        size = AppleButtonDefaults.CircleSize,
        enabled = enabled,
        modifier = modifier,
    )
}

@Composable
private fun CircleButtonLayout(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    containerColor: Color,
    iconColor: Color,
    size: Dp,
    enabled: Boolean,
    modifier: Modifier,
) {
    // 点击区是外围补足 44 的方框, 按压效果只画在圆内.
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier
            .sizeIn(minWidth = AppleComponentDefaults.MinTouchSize, minHeight = AppleComponentDefaults.MinTouchSize)
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .then(
                if (contentDescription != null) {
                    Modifier.semantics { this.contentDescription = contentDescription }
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(size)
                .clip(CircleShape)
                .background(containerColor)
                .indication(interactionSource, ripple()),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, Modifier.size(AppleButtonDefaults.CircleIconSize), tint = iconColor)
        }
    }
}

object AppleButtonDefaults {
    /** 一般按钮高度. */
    val Height = 44.dp

    /** 登录流程的满版按钮高度. */
    val LoginHeight = 50.dp

    val ContentPadding = PaddingValues(horizontal = 20.dp)

    /** 圆形图标按钮的视觉尺寸; 另有 40. */
    val CircleSize = 36.dp

    val CircleIconSize = 18.dp
}
