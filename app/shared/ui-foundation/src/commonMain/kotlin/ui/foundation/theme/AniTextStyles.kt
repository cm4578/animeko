/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import me.him188.ani.app.ui.foundation.LocalPlatformFontFamily

/**
 * Ani 版 Composable 使用的字级, 规格见 docs/design/ani-style/01-tokens.md §7.
 *
 * 与 `MaterialTheme.typography` 相互独立. 字体族取 [LocalPlatformFontFamily],
 * 为 `null` 时沿用系统默认字体. 数字列另加 `fontFeatureSettings = "tnum"`.
 * 单行文字的高度等于行高 (首行上方与末行下方保留行距), 与设计稿的 CSS 行高一致.
 */
object AniTextStyles {
    // 手机

    /** 分页大标题 (追番、探索、我的). */
    val largeTitle: TextStyle
        @Composable @ReadOnlyComposable get() = style(34, 41, FontWeight.ExtraBold, letterSpacing = -0.5)

    /** 区块标题 (继续观看、选集). */
    val title: TextStyle
        @Composable @ReadOnlyComposable get() = style(20, 26, FontWeight.Bold)

    /** 卡片主标题、推入页标题. */
    val headline: TextStyle
        @Composable @ReadOnlyComposable get() = style(17, 22, FontWeight.SemiBold)

    /** 设定行; 列表行标题用 [FontWeight.SemiBold]. */
    val body: TextStyle
        @Composable @ReadOnlyComposable get() = style(16, 22, FontWeight.Normal)

    /** 链接文字、按钮文字. */
    val callout: TextStyle
        @Composable @ReadOnlyComposable get() = style(15, 20, FontWeight.Normal)

    /** 胶囊文字、次要标题. */
    val subhead: TextStyle
        @Composable @ReadOnlyComposable get() = style(14, 20, FontWeight.Normal)

    /** 元信息、分组标题. */
    val footnote: TextStyle
        @Composable @ReadOnlyComposable get() = style(13, 18, FontWeight.Normal)

    /** 辅助说明、数字标注. */
    val caption: TextStyle
        @Composable @ReadOnlyComposable get() = style(12, 16, FontWeight.Normal)

    /** 浮动导航栏文字; 选中项用 [FontWeight.Bold]. */
    val tabLabel: TextStyle
        @Composable @ReadOnlyComposable get() = style(10, 12, FontWeight.Medium)

    // macOS

    /** 页面大标题. */
    val macLargeTitle: TextStyle
        @Composable @ReadOnlyComposable get() = style(28, 34, FontWeight.ExtraBold, letterSpacing = -0.4)

    /** 区块标题. */
    val macTitle: TextStyle
        @Composable @ReadOnlyComposable get() = style(17, 22, FontWeight.Bold)

    /** 设定窗格标题、小窗口标题. */
    val macWindowTitle: TextStyle
        @Composable @ReadOnlyComposable get() = style(15, 20, FontWeight.SemiBold)

    /** 侧边栏项目; 表格行标题用 [FontWeight.SemiBold]. */
    val macBody: TextStyle
        @Composable @ReadOnlyComposable get() = style(14, 20, FontWeight.Normal)

    /** 表格次要列、按钮、胶囊. */
    val macCallout: TextStyle
        @Composable @ReadOnlyComposable get() = style(13, 18, FontWeight.Normal)

    /** 表头、辅助说明. */
    val macCaption: TextStyle
        @Composable @ReadOnlyComposable get() = style(12, 16, FontWeight.Normal)

    /** 侧边栏分组标题. */
    val macSidebarSection: TextStyle
        @Composable @ReadOnlyComposable get() = style(11, 14, FontWeight.SemiBold)
}

@Composable
@ReadOnlyComposable
private fun style(
    size: Int,
    lineHeight: Int,
    weight: FontWeight,
    letterSpacing: Double = 0.0,
): TextStyle = TextStyle(
    fontFamily = LocalPlatformFontFamily.current.defaultFontFamily,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    fontWeight = weight,
    letterSpacing = letterSpacing.sp,
    lineHeightStyle = LineHeightStyle(LineHeightStyle.Alignment.Center, LineHeightStyle.Trim.None),
)
