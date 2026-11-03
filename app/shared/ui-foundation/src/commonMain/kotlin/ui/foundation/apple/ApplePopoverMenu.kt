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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C15 弹出菜单: tabBg、floatingShadow (深色加 separator 描边).
 *
 * 手机圆角 14、宽 232. macOS 圆角 10、内距 5、宽 232 (可用 [modifier] 改为 220–232).
 *
 * 放在锚点元素所在的 `Box` 内. 菜单的右缘与锚点对齐, 显示在锚点下方 8; 下方空间不足时显示在上方.
 * 点菜单外或按返回键 (macOS 为 Esc) 时调用 [onDismissRequest].
 */
@Composable
fun ApplePopoverMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!expanded) return
    val density = LocalDensity.current
    val positionProvider = remember(density) {
        PopoverPositionProvider(
            gap = with(density) { 8.dp.roundToPx() },
            shadowPadding = with(density) { ApplePopoverMenuDefaults.ShadowPadding.roundToPx() },
        )
    }
    val mac = isMacStyle
    val shape = RoundedCornerShape(if (mac) 10.dp else 14.dp)
    Popup(
        popupPositionProvider = positionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true),
    ) {
        // 外围留出阴影的绘制空间, 定位时扣除.
        Box(Modifier.padding(ApplePopoverMenuDefaults.ShadowPadding)) {
            Column(
                modifier
                    .width(ApplePopoverMenuDefaults.Width)
                    .dropShadow(
                        shape,
                        Shadow(radius = 24.dp, color = AniColors.floatingShadowColor, offset = DpOffset(0.dp, 8.dp)),
                    )
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(1.dp, AniColors.floatingBorder, shape)
                    .padding(if (mac) 5.dp else 0.dp),
                content = content,
            )
        }
    }
}

/**
 * C15 菜单行.
 *
 * 手机: 高 48; 20 号图标 + `body` 文字; 分隔线从 48 开始.
 * macOS: 高 26、圆角 5 (有图标时高 30、圆角 6、16 号图标); `macCallout` 文字, 右侧 [shortcut] label2;
 * 悬停的行为 accentFill 底 + onAccentFill 文字. 分隔线左右各缩进 10.
 *
 * @param value 当前值, 行尾显示 accent W600 文字 + `›` (例如「追番状态 在看 ›」).
 * @param checked 单选项是否选中. 不为 `null` 时, 图标位置显示 accent 勾号 (未选中则留空), 同组的行文字对齐; [icon] 不显示.
 * @param shortcut macOS 的快捷键提示, 例如「⌘I」. 只写 05 C2 批准的快捷键. 手机不显示.
 * @param showDivider 在行上方画分隔线. 用于分组的第一行.
 */
@Composable
fun PopoverMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    value: String? = null,
    shortcut: String? = null,
    showDivider: Boolean = false,
    enabled: Boolean = true,
    checked: Boolean? = null,
) {
    if (isMacStyle) {
        MacPopoverMenuItem(text, onClick, modifier, icon, value, shortcut, showDivider, enabled, checked)
        return
    }
    Box(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .then(if (checked != null) Modifier.semantics { selected = checked } else Modifier)
                .padding(start = 16.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (checked != null) {
                CheckSlot(checked, Modifier.size(20.dp), MaterialTheme.colorScheme.primary)
            } else if (icon != null) {
                Icon(icon, contentDescription = null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.onSurface)
            }
            Text(
                text,
                Modifier.weight(1f),
                style = AniTextStyles.body,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (value != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        value,
                        style = AniTextStyles.callout,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                    Icon(
                        AppleIcons.chevronRight(2.6f),
                        contentDescription = null,
                        Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        if (showDivider) {
            AppleDivider(Modifier.align(Alignment.TopStart), startIndent = 48.dp)
        }
    }
}

@Composable
private fun MacPopoverMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier,
    icon: ImageVector?,
    value: String?,
    shortcut: String?,
    showDivider: Boolean,
    enabled: Boolean,
    checked: Boolean?,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val highlighted = enabled && interactionSource.collectIsHoveredAsState().value
    val labelColor = if (highlighted) AniColors.onAccentFill else MaterialTheme.colorScheme.onSurface
    val secondaryColor = if (highlighted) AniColors.onAccentFill else MaterialTheme.colorScheme.secondary
    val shape = RoundedCornerShape(if (icon != null) 6.dp else 5.dp)
    Column(modifier.fillMaxWidth()) {
        if (showDivider) {
            Box(
                Modifier
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = AppleComponentDefaults.DividerAlpha),
                    ),
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .height(if (icon != null) 30.dp else 26.dp)
                .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
                .clip(shape)
                .background(if (highlighted) AniColors.accentFill else Color.Transparent)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                )
                .then(if (checked != null) Modifier.semantics { selected = checked } else Modifier)
                .padding(start = 10.dp, end = if (icon != null) 8.dp else 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (checked != null) {
                CheckSlot(
                    checked,
                    Modifier.size(16.dp),
                    if (highlighted) AniColors.onAccentFill else MaterialTheme.colorScheme.primary,
                )
            } else if (icon != null) {
                Icon(icon, contentDescription = null, Modifier.size(16.dp), tint = labelColor)
            }
            Text(
                text,
                Modifier.weight(1f),
                style = AniTextStyles.macCallout,
                color = labelColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (value != null) {
                Text(
                    value,
                    style = AniTextStyles.macCallout,
                    fontWeight = FontWeight.SemiBold,
                    color = if (highlighted) AniColors.onAccentFill else MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                )
                Icon(
                    AppleIcons.chevronRight(2.8f),
                    contentDescription = null,
                    Modifier.size(10.dp),
                    tint = secondaryColor,
                )
            }
            if (shortcut != null) {
                Text(shortcut, style = AniTextStyles.macCallout, color = secondaryColor, maxLines = 1)
            }
        }
    }
}

/**
 * 单选项的勾号位置: 选中时画勾, 未选中时留空.
 */
@Composable
private fun CheckSlot(checked: Boolean, modifier: Modifier, color: Color) {
    Box(modifier, contentAlignment = Alignment.Center) {
        if (checked) {
            Icon(AppleIcons.check(2.4f), contentDescription = null, Modifier.fillMaxSize(), tint = color)
        }
    }
}

object ApplePopoverMenuDefaults {
    val Width = 232.dp

    /** 弹出窗口四周为阴影留出的空间. */
    val ShadowPadding = 32.dp
}

/**
 * 菜单右缘对齐锚点右缘, 显示在锚点下方; 下方放不下时改到上方. 计算时扣除阴影空间 [shadowPadding].
 */
private class PopoverPositionProvider(
    private val gap: Int,
    private val shadowPadding: Int,
) : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset {
        val menuWidth = popupContentSize.width - shadowPadding * 2
        val menuHeight = popupContentSize.height - shadowPadding * 2
        val menuLeft = if (layoutDirection == LayoutDirection.Ltr) {
            anchorBounds.right - menuWidth
        } else {
            anchorBounds.left
        }.coerceIn(0, (windowSize.width - menuWidth).coerceAtLeast(0))
        val below = anchorBounds.bottom + gap
        val menuTop = if (below + menuHeight <= windowSize.height) {
            below
        } else {
            (anchorBounds.top - gap - menuHeight).coerceAtLeast(0)
        }
        return IntOffset(menuLeft - shadowPadding, menuTop - shadowPadding)
    }
}
