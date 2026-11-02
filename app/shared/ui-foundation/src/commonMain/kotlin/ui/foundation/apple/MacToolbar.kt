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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C18 macOS 工具栏: 高 52, 内距 0 20 0 16, 项目间距 4.
 *
 * 左侧为上一页 / 下一页按钮 28 (不可用时不透明度 0.35), 右侧 [actions] 放本页动作:
 * [MacToolbarIconButton]、[MacToolbarTextButton]、[MacSegmentedButtons], 分组之间用 [MacToolbarDivider].
 *
 * @param leading 上一页按钮左侧的内容, 例如侧边栏隐藏后的「显示侧边栏」按钮 (05 G31).
 */
@Composable
fun MacToolbar(
    canGoBack: Boolean,
    onBack: () -> Unit,
    backContentDescription: String,
    canGoForward: Boolean,
    onForward: () -> Unit,
    forwardContentDescription: String,
    modifier: Modifier = Modifier,
    leading: @Composable RowScope.() -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .height(MacWindowDefaults.TitleBarHeight)
            .padding(start = 16.dp, end = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading()
        MacToolbarIconButton(
            onClick = onBack,
            icon = AppleIcons.back(),
            contentDescription = backContentDescription,
            enabled = canGoBack,
        )
        MacToolbarIconButton(
            onClick = onForward,
            icon = AppleIcons.forward(),
            contentDescription = forwardContentDescription,
            enabled = canGoForward,
        )
        Spacer(Modifier.weight(1f))
        actions()
    }
}

/**
 * C18 工具栏图标按钮: 28×28、圆角 6、无底, 16 号 label2 图标.
 *
 * @param checked 不为 `null` 时为开关按钮 (例如检阅器): 开启时 tint 底 + accent 图标.
 */
@Composable
fun MacToolbarIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    checked: Boolean? = null,
    enabled: Boolean = true,
) {
    val interaction = if (checked != null) {
        Modifier.toggleable(value = checked, enabled = enabled, role = Role.Switch, onValueChange = { onClick() })
    } else {
        Modifier.clickable(enabled = enabled, role = Role.Button, onClick = onClick)
    }
    ToolbarIconBox(
        icon = icon,
        contentDescription = contentDescription,
        highlighted = checked == true,
        width = MacToolbarDefaults.ButtonSize,
        modifier = modifier
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
            .clip(RoundedCornerShape(6.dp))
            .then(interaction),
    )
}

/**
 * C18 工具栏文字按钮: 高 28、圆角 6、fill 底; `macCallout` W500 label, 可带 13 号图标.
 */
@Composable
fun MacToolbarTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    Row(
        modifier
            .alpha(if (enabled) 1f else AppleComponentDefaults.DisabledAlpha)
            .height(MacToolbarDefaults.ButtonSize)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(start = if (icon != null) 10.dp else 12.dp, end = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (icon != null) {
            Icon(icon, contentDescription = null, Modifier.size(13.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
        Text(
            text,
            style = AniTextStyles.macCallout,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
    }
}

/**
 * C18 分段切换按钮组 (例如网格 / 清单), 按钮间距 2. 项目用 [MacSegmentedButton].
 */
@Composable
fun MacSegmentedButtons(
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier.selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content,
    )
}

/**
 * C18 分段切换按钮: 32×28、圆角 6; 选中为 tint 底 + accent 图标, 未选中为 label2 图标.
 */
@Composable
fun MacSegmentedButton(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    ToolbarIconBox(
        icon = icon,
        contentDescription = contentDescription,
        highlighted = selected,
        width = MacToolbarDefaults.SegmentWidth,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    )
}

/**
 * C18 工具栏中分隔两组按钮的竖线: 1×18 separator, 左右各留 8.
 */
@Composable
fun MacToolbarDivider(modifier: Modifier = Modifier) {
    Box(
        modifier
            .padding(horizontal = 8.dp)
            .size(1.dp, 18.dp)
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

@Composable
private fun ToolbarIconBox(
    icon: ImageVector,
    contentDescription: String?,
    highlighted: Boolean,
    width: Dp,
    modifier: Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier
            .background(if (highlighted) colorScheme.secondaryContainer else Color.Transparent)
            .size(width, MacToolbarDefaults.ButtonSize)
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
            Modifier.size(MacToolbarDefaults.IconSize),
            tint = if (highlighted) colorScheme.primary else colorScheme.secondary,
        )
    }
}

object MacToolbarDefaults {
    /** 按钮的高度, 也是图标按钮的宽度. */
    val ButtonSize = 28.dp

    /** 分段切换按钮的宽度. */
    val SegmentWidth = 32.dp

    val IconSize = 16.dp
}
