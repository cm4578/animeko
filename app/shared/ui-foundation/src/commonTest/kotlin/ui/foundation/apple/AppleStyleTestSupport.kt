/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpRect
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.LocalPlatformFontFamily
import me.him188.ani.app.ui.foundation.PlatformFontFamily
import me.him188.ani.app.ui.foundation.theme.aniColorScheme
import me.him188.ani.app.ui.theme.DefaultSeedColor
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import kotlin.math.abs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal val MacOSPlatform: Platform = Platform.MacOS(Arch.AARCH64)

internal const val SIZE_TOLERANCE = 0.5f
private const val COLOR_TOLERANCE = 2f / 255f

/**
 * 以 Ani 浅色配色显示 [content]. 组件按 [platform] 选择手机或 macOS 规格, 测试不受执行测试的系统影响.
 */
internal fun ComposeUiTest.setAniStyleContent(
    platform: Platform = Platform.Ios,
    content: @Composable () -> Unit,
) {
    setContent {
        CompositionLocalProvider(
            LocalPlatform provides platform,
            LocalPlatformFontFamily provides PlatformFontFamily(null),
        ) {
            MaterialTheme(colorScheme = aniColorScheme(DefaultSeedColor, isDark = false), content = content)
        }
    }
}

internal fun ComposeUiTest.boundsOf(tag: String): DpRect = onNodeWithTag(tag, useUnmergedTree = true).getBoundsInRoot()

internal fun ComposeUiTest.textBoundsOf(text: String): DpRect =
    onNodeWithText(text, useUnmergedTree = true).getBoundsInRoot()

internal fun ComposeUiTest.heightOf(tag: String): Dp = boundsOf(tag).let { it.bottom - it.top }

internal fun ComposeUiTest.widthOf(tag: String): Dp = boundsOf(tag).let { it.right - it.left }

internal fun ComposeUiTest.assertSize(tag: String, width: Dp, height: Dp) {
    assertEquals(width.value, widthOf(tag).value, SIZE_TOLERANCE, "width of $tag")
    assertEquals(height.value, heightOf(tag).value, SIZE_TOLERANCE, "height of $tag")
}

internal fun ComposeUiTest.assertPixel(tag: String, x: Dp, y: Dp, expected: Color) {
    val pixels = onNodeWithTag(tag, useUnmergedTree = true).captureToImage().toPixelMap()
    val actual = with(density) { pixels[x.roundToPx(), y.roundToPx()] }
    val matches = abs(actual.red - expected.red) < COLOR_TOLERANCE &&
            abs(actual.green - expected.green) < COLOR_TOLERANCE &&
            abs(actual.blue - expected.blue) < COLOR_TOLERANCE
    assertTrue(matches, "pixel ($x, $y) of $tag: expected $expected, actual $actual")
}
