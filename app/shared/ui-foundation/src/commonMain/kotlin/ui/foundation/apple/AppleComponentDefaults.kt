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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.utils.platform.isMacOS

/**
 * Apple 版组件 (02-components.md) 共用的常量.
 */
object AppleComponentDefaults {
    /** 停用态的内容不透明度. */
    const val DisabledAlpha = 0.35f

    /** 手机上点击区域的最小边长. 视觉小于此尺寸的按钮在外围补透明点击区. */
    val MinTouchSize = 44.dp

    /** macOS 上点击区域的最小边长. */
    val MacMinTouchSize = 28.dp

    /** 当前平台点击区域的最小边长: 手机 44, macOS 28. */
    val minTouchSize: Dp
        @Composable @ReadOnlyComposable get() = if (isMacStyle) MacMinTouchSize else MinTouchSize

    /** 分隔线的不透明度. */
    const val DividerAlpha = 0.6f

    /** 叠在图片或视频上的圆形按钮底色. */
    val MediaScrim = Color(0xFF0A0E12).copy(alpha = 0.38f)
}

/**
 * Apple 版组件是否按 macOS 规格绘制. 同一个组件在 iOS 用手机规格, 在 macOS 用 02 各节的 macOS 列.
 */
internal val isMacStyle: Boolean
    @Composable @ReadOnlyComposable get() = LocalPlatform.current.isMacOS()

/**
 * 行间分隔线: 1dp separator, 不透明度 0.6, 从 [startIndent] 延伸到右缘.
 */
@Composable
fun AppleDivider(
    modifier: Modifier = Modifier,
    startIndent: Dp = 0.dp,
) {
    Box(
        modifier
            .padding(start = startIndent)
            .fillMaxWidth()
            .height(1.dp)
            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = AppleComponentDefaults.DividerAlpha)),
    )
}
