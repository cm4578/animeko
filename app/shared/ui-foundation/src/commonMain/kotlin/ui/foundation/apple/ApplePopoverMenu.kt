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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
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
 * C15 手机弹出菜单: tabBg、圆角 14、floatingShadow (深色加 separator 描边)、宽 232.
 *
 * 放在锚点元素所在的 `Box` 内. 菜单的右缘与锚点对齐, 显示在锚点下方 8; 下方空间不足时显示在上方.
 * 点菜单外或按返回键时调用 [onDismissRequest].
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
    val shape = RoundedCornerShape(14.dp)
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
                    .border(1.dp, AniColors.floatingBorder, shape),
                content = content,
            )
        }
    }
}

/**
 * C15 菜单行: 高 48; 20 号图标 + `body` 文字.
 *
 * @param value 当前值, 行尾显示 accent W600 文字 + `›` (例如「追番状态 在看 ›」).
 * @param showDivider 在行顶画分隔线, 从 48 开始. 用于第二行起.
 */
@Composable
fun PopoverMenuItem(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    value: String? = null,
    showDivider: Boolean = false,
    enabled: Boolean = true,
) {
    Box(modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(48.dp)
                .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
                .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
                .padding(start = 16.dp, end = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
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
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
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
