/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * C16 macOS 窗口的设计尺寸与共用的版面数值.
 *
 * 窗口沿用 desktop 的 `MacOSWindowFrame`: 透明标题栏 + 全尺寸内容 (`apple.awt.fullWindowContent`),
 * 红黄绿按钮由系统绘制. 内容要让出左上角: 主窗口由侧边栏 ([TitleBarHeight] 高的顶部栏) 让出,
 * 设定窗口由分类栏让出.
 */
object MacWindowDefaults {
    /** 主窗口. 窗口可缩放, 版式按此尺寸验收. */
    val MainWindowSize = DpSize(1280.dp, 800.dp)

    /** 设定窗口. */
    val SettingsWindowSize = DpSize(920.dp, 680.dp)

    /** 小窗口: 登录、授权、扫码. */
    val SmallWindowSize = DpSize(480.dp, 640.dp)

    /** 资料源编辑器. */
    val MediaSourceEditorWindowSize = DpSize(1200.dp, 760.dp)

    /** 合并收藏. */
    val MergeCollectionWindowSize = DpSize(1040.dp, 720.dp)

    /**
     * 窗口顶部一栏的高度: 侧边栏与分类栏的顶部 (红黄绿按钮所在)、工具栏、设定窗格标题栏.
     * macOS sheet 从这一栏的下缘降下.
     */
    val TitleBarHeight = 52.dp

    /** 主窗口内容区 (工具栏之下) 的内距: 上 4、左右 32. */
    val ContentPadding = PaddingValues(start = 32.dp, top = 4.dp, end = 32.dp)
}
