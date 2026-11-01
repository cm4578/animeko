/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color

@Composable
actual fun appColorScheme(
    seedColor: Color,
    useDynamicTheme: Boolean,
    useBlackBackground: Boolean,
    isDark: Boolean,
): ColorScheme {
    // iOS 使用 Ani 配色. 按输入缓存的原因见 AppTheme.desktop.kt.
    return remember(seedColor, isDark, useBlackBackground) {
        modifyColorSchemeForBlackBackground(aniColorScheme(seedColor, isDark), isDark, useBlackBackground)
    }
}

@Composable
actual fun isPlatformSupportDynamicTheme(): Boolean {
    return false
}