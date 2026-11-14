/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * Material 3 没有对应角色的 Ani 颜色 token. 取值规则见 docs/design/ani-style/01-tokens.md §5.
 *
 * 从当前 [MaterialTheme.colorScheme] 推出: 以 `surfaceContainerLowest` 的亮度判断深浅,
 * 高对比度深色主题 (纯黑背景) 按深色处理.
 *
 * 只供 iOS 与 macOS 的 Ani 版 Composable 使用.
 */
object AniColors {
    private val isDark: Boolean
        @Composable @ReadOnlyComposable get() =
            MaterialTheme.colorScheme.surfaceContainerLowest.luminance() < 0.5f

    private val neutrals: AniNeutralColors
        @Composable @ReadOnlyComposable get() = if (isDark) AniNeutrals.Dark else AniNeutrals.Light

    /**
     * 次一级文字: 未选中筛选胶囊、表格次要列.
     */
    val labelSoft: Color
        @Composable @ReadOnlyComposable get() = neutrals.labelSoft

    /**
     * 主要按钮底、开关开启.
     */
    val accentFill: Color
        @Composable @ReadOnlyComposable get() = aniAccentFill(MaterialTheme.colorScheme.primary, isDark)

    /**
     * [accentFill] 上的文字.
     */
    val onAccentFill: Color
        @Composable @ReadOnlyComposable get() = Color.White

    /**
     * 唯一提示色: 新内容、未读小圆点.
     */
    val alert: Color
        @Composable @ReadOnlyComposable get() = neutrals.alert

    /**
     * 浮动导航栏、弹出菜单的阴影颜色. 阴影为 y 8、模糊 24.
     */
    val floatingShadowColor: Color
        @Composable @ReadOnlyComposable get() = neutrals.floatingShadow

    /**
     * 浮起表面的描边: 浅色为 [Color.Transparent], 深色为 separator.
     */
    val floatingBorder: Color
        @Composable @ReadOnlyComposable get() =
            if (isDark) MaterialTheme.colorScheme.outlineVariant else Color.Transparent

    /**
     * macOS 主侧边栏、设定窗口分类栏、检阅器底.
     */
    val sidebar: Color
        @Composable @ReadOnlyComposable get() = neutrals.sidebar

    /**
     * macOS 侧边栏搜索框底.
     */
    val sidebarField: Color
        @Composable @ReadOnlyComposable get() = neutrals.sidebarField

    /**
     * macOS 悬停底.
     */
    val hover: Color
        @Composable @ReadOnlyComposable get() = neutrals.hover

    /**
     * 品牌头像底、App 图标底. 不随主题变.
     */
    val brandSlate: Color = Color(0xFF2F3943)

    /**
     * 头像光环、播放器进度条已播放段、播放器「弹」按钮底. 不随主题变.
     */
    val brandRing: Color = Color(0xFF7EBBED)
}
