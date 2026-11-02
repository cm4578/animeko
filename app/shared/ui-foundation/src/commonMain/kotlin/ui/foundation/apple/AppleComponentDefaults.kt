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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Apple 版组件 (02-components.md) 共用的常量.
 */
object AppleComponentDefaults {
    /** 停用态的内容不透明度. */
    const val DisabledAlpha = 0.35f

    /** 手机上点击区域的最小边长. 视觉小于此尺寸的按钮在外围补透明点击区. */
    val MinTouchSize = 44.dp

    /** 分隔线的不透明度. */
    const val DividerAlpha = 0.6f

    /** 叠在图片或视频上的圆形按钮底色. */
    val MediaScrim = Color(0xFF0A0E12).copy(alpha = 0.38f)
}

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
