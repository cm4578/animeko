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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.apple.AlertDot
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C1 浮动胶囊导航栏 (iPhone).
 *
 * 悬浮于内容上方: 左右各 16, 距底部 20 + 系统底部安全区. 高 64、圆角 32、左右内距 8;
 * 实心 tabBg 底 + floatingShadow, 深色加 1dp separator 描边. **不读取**「毛玻璃效果」设置.
 *
 * 内容区底部要留出 [FloatingTabBarDefaults.ContentBottomSpace] 加底部安全区, 最后一项才不会被遮住.
 * 项目用 [FloatingTabBarItem], 顺序为探索、追番、快取、我的.
 */
@Composable
fun FloatingTabBar(
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forNavigationBar(),
    content: @Composable RowScope.() -> Unit,
) {
    val shape = RoundedCornerShape(FloatingTabBarDefaults.Height / 2)
    Box(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
            .padding(start = 16.dp, end = 16.dp, bottom = FloatingTabBarDefaults.BottomMargin),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .height(FloatingTabBarDefaults.Height)
                .dropShadow(
                    shape,
                    Shadow(radius = 24.dp, color = AniColors.floatingShadowColor, offset = DpOffset(0.dp, 8.dp)),
                )
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(1.dp, AniColors.floatingBorder, shape)
                .padding(horizontal = 8.dp)
                .selectableGroup(),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

/**
 * C1 导航栏项目: 各项平均分宽; 图标框 36×36 + 文字, 间距 2.
 *
 * 未选中: 图标 24 (线宽 1.8)、label2; 文字 `tabLabel` W500、label2.
 * 选中: 图标框圆角 18、tint 底、1.5 accent 内描边; 图标 22 (线宽 2)、accent; 文字 W700、accent.
 *
 * @param icon 未选中的图标, 线宽 1.8. 例如 `AppleIcons.explore()`.
 * @param selectedIcon 选中的图标, 线宽 2. 例如 `AppleIcons.explore(2f)`.
 * @param alert 在图标右上显示 C12 提示点 (例如追番有新集数).
 */
@Composable
fun RowScope.FloatingTabBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    alert: Boolean = false,
) {
    TabItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        selectedIcon = selectedIcon,
        label = label,
        alert = alert,
        modifier = modifier
            .weight(1f)
            .height(FloatingTabBarDefaults.ItemHeight),
    )
}

/**
 * C1 的 Apple 版 rail (iPad 宽屏). 项目与 [FloatingTabBar] 相同, 竖向排列; 底色 tabBg, 右侧 1dp separator.
 *
 * @param header 项目上方的内容, 例如搜索按钮.
 */
@Composable
fun AppleNavigationRail(
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forNavigationRail(),
    header: (@Composable ColumnScope.() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Row(modifier.fillMaxHeight()) {
        Column(
            Modifier
                .fillMaxHeight()
                .width(FloatingTabBarDefaults.RailWidth)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .windowInsetsPadding(windowInsets)
                .padding(top = 12.dp)
                .selectableGroup(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            header?.invoke(this)
            content()
        }
        Box(
            Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(MaterialTheme.colorScheme.outlineVariant),
        )
    }
}

/**
 * [AppleNavigationRail] 的项目. 外观同 [FloatingTabBarItem].
 */
@Composable
fun ColumnScope.AppleNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    alert: Boolean = false,
) {
    TabItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        selectedIcon = selectedIcon,
        label = label,
        alert = alert,
        modifier = modifier
            .width(FloatingTabBarDefaults.RailItemWidth)
            .height(FloatingTabBarDefaults.ItemHeight + 8.dp),
    )
}

@Composable
private fun TabItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    alert: Boolean,
    modifier: Modifier,
) {
    val colorScheme = MaterialTheme.colorScheme
    val contentColor = if (selected) colorScheme.primary else colorScheme.secondary
    val frameShape = RoundedCornerShape(FloatingTabBarDefaults.IconFrameSize / 2)
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .selectable(selected = selected, role = Role.Tab, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
    ) {
        Box(
            Modifier
                .size(FloatingTabBarDefaults.IconFrameSize)
                .then(
                    if (selected) {
                        Modifier
                            .background(colorScheme.secondaryContainer, frameShape)
                            .border(1.5.dp, colorScheme.primary, frameShape)
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                if (selected) selectedIcon else icon,
                contentDescription = null,
                Modifier.size(iconSize(selected)),
                tint = contentColor,
            )
            if (alert) {
                AlertDot(Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp))
            }
        }
        Text(
            label,
            style = AniTextStyles.tabLabel,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            maxLines = 1,
        )
    }
}

private fun iconSize(selected: Boolean): Dp = if (selected) 22.dp else 24.dp

object FloatingTabBarDefaults {
    val Height = 64.dp
    val ItemHeight = 52.dp
    val IconFrameSize = 36.dp

    /** 导航栏下缘与底部安全区的距离. */
    val BottomMargin = 20.dp

    /**
     * 内容区底部要留出的空白 (不含底部安全区): 导航栏高 64 + 距底 20 + 16.
     */
    val ContentBottomSpace = 64.dp + 20.dp + 16.dp

    val RailWidth = 80.dp
    val RailItemWidth = 72.dp
}
