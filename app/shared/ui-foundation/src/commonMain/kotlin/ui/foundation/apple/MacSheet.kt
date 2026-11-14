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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties

/**
 * C14 macOS sheet: 从工具栏下缘降下, 水平居中; 宽 560–760、圆角 12、底色 bg; 背景盖 38% 黑色遮罩.
 *
 * sheet 为模态: 遮罩拦截点击, 不关闭 sheet; 按 Esc 时调用 [onDismissRequest].
 * 底部操作列用 [MacSheetActions].
 *
 * @param width 560–760.
 * @param topOffset sheet 顶端与窗口顶端的距离, 为工具栏或窗格标题栏的下缘.
 */
@Composable
fun MacSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    width: Dp = MacSheetDefaults.MinWidth,
    topOffset: Dp = MacWindowDefaults.TitleBarHeight,
    contentPadding: PaddingValues = PaddingValues(24.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Popup(
        popupPositionProvider = WindowOriginPositionProvider,
        onDismissRequest = onDismissRequest,
        properties = PopupProperties(focusable = true, dismissOnClickOutside = false),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(MacSheetDefaults.Scrim)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                Modifier
                    .padding(top = topOffset)
                    .then(modifier)
                    .width(width)
                    .dropShadow(
                        shape,
                        Shadow(radius = 64.dp, color = Color.Black, offset = DpOffset(0.dp, 24.dp), alpha = 0.45f),
                    )
                    .border(0.5.dp, Color.Black.copy(alpha = 0.3f), shape)
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                    // sheet 本身吸收点击, 不传给遮罩.
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {},
                    )
                    .padding(contentPadding),
                content = content,
            )
        }
    }
}

/**
 * C14 macOS sheet 底部操作列: 右对齐「取消」(次要) + 主要按钮, 间距 8; 按钮高 28、最小宽 80.
 *
 * @param leading 左侧的其他动作, 例如「删除帐号…」.
 */
@Composable
fun MacSheetActions(
    cancelText: String,
    onCancel: () -> Unit,
    confirmText: String,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    confirmEnabled: Boolean = true,
    leading: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        Spacer(Modifier.weight(1f))
        AppleButton(
            onClick = onCancel,
            modifier = Modifier.widthIn(min = MacSheetDefaults.ButtonMinWidth),
            style = AppleButtonStyle.Secondary,
            contentPadding = MacSheetDefaults.ButtonContentPadding,
        ) {
            Text(cancelText, maxLines = 1)
        }
        AppleButton(
            onClick = onConfirm,
            modifier = Modifier.widthIn(min = MacSheetDefaults.ButtonMinWidth),
            enabled = confirmEnabled,
            contentPadding = MacSheetDefaults.ButtonContentPadding,
        ) {
            Text(confirmText, maxLines = 1)
        }
    }
}

object MacSheetDefaults {
    val MinWidth = 560.dp
    val MaxWidth = 760.dp

    /** 背景遮罩: 38% 黑色. */
    val Scrim = Color.Black.copy(alpha = 0.38f)

    val ButtonMinWidth = 80.dp
    val ButtonContentPadding = PaddingValues(horizontal = 14.dp)
}

/**
 * 弹出窗口放在窗口左上角, 内容以 `fillMaxSize` 盖住整个窗口.
 */
private object WindowOriginPositionProvider : PopupPositionProvider {
    override fun calculatePosition(
        anchorBounds: IntRect,
        windowSize: IntSize,
        layoutDirection: LayoutDirection,
        popupContentSize: IntSize,
    ): IntOffset = IntOffset.Zero
}
