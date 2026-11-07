/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.isSecondaryPressed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.window.PopupPositionProvider

/**
 * C15 macOS 右键菜单: 外观同 [ApplePopoverMenu], 菜单左上角对齐右键的位置 [position];
 * 右侧或下方放不下时改为向左或向上展开.
 *
 * 放在被右键的元素所在的 `Box` 内, [position] 相对该 `Box`, 通常来自 [onSecondaryClick].
 * 点菜单外或按 Esc 时调用 [onDismissRequest].
 */
@Composable
fun AppleContextMenu(
    expanded: Boolean,
    position: Offset,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!expanded) return
    val density = LocalDensity.current
    val positionProvider = remember(density, position) {
        ContextMenuPositionProvider(
            position = IntOffset(position.x.toInt(), position.y.toInt()),
            shadowPadding = with(density) { ApplePopoverMenuDefaults.ShadowPadding.roundToPx() },
        )
    }
    PopoverMenuPopup(positionProvider, onDismissRequest, modifier, content)
}

/**
 * 鼠标右键按下时调用 [onClick], 参数为按下的位置 (相对本元素). 用于在该位置打开 [AppleContextMenu]. 只有鼠标右键触发.
 */
fun Modifier.onSecondaryClick(
    enabled: Boolean = true,
    onClick: (Offset) -> Unit,
): Modifier = composed {
    val currentOnClick by rememberUpdatedState(onClick)
    if (!enabled) {
        Modifier
    } else {
        Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    if (event.type == PointerEventType.Press && event.buttons.isSecondaryPressed) {
                        val change = event.changes.firstOrNull() ?: continue
                        change.consume()
                        currentOnClick(change.position)
                    }
                }
            }
        }
    }
}

/**
 * 菜单左上角放在锚点内的 [position]; 右侧放不下时改到左侧, 下方放不下时改到上方. 计算时扣除阴影空间 [shadowPadding].
 */
private class ContextMenuPositionProvider(
    private val position: IntOffset,
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
        val x = anchorBounds.left + position.x
        val y = anchorBounds.top + position.y
        val menuLeft = if (x + menuWidth <= windowSize.width) x else (x - menuWidth).coerceAtLeast(0)
        val menuTop = if (y + menuHeight <= windowSize.height) y else (y - menuHeight).coerceAtLeast(0)
        return IntOffset(menuLeft - shadowPadding, menuTop - shadowPadding)
    }
}
