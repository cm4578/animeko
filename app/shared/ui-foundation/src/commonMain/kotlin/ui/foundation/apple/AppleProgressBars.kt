/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.AniColors

/**
 * C9 一般进度条: 轨道高 4、圆角 2、fill; 进度 accent.
 *
 * @param muted 暂停、已看完、已完成时为 `true`, 进度改为 label2.
 */
@Composable
fun AppleProgressBar(
    progress: () -> Float,
    modifier: Modifier = Modifier,
    muted: Boolean = false,
) {
    val colorScheme = MaterialTheme.colorScheme
    ProgressTrack(
        progress = progress,
        height = 4.dp,
        trackColor = colorScheme.surfaceContainerHighest,
        progressColor = if (muted) colorScheme.secondary else colorScheme.primary,
        modifier = modifier,
    )
}

/**
 * C9 封面上的进度条: 高 3, 轨道 35% 黑, 进度白色. 由 [AppleCover] 放在封面底部, 左右下各内缩 8.
 */
@Composable
fun CoverProgressBar(
    progress: () -> Float,
    modifier: Modifier = Modifier,
) {
    ProgressTrack(
        progress = progress,
        height = 3.dp,
        trackColor = Color.Black.copy(alpha = 0.35f),
        progressColor = Color.White,
        modifier = modifier,
    )
}

/**
 * C9 播放器进度条的外观: 轨道 3 (白 28%)、缓冲 (白 45%)、已播放 [AniColors.brandRing]、白色拖动点 12.
 *
 * 只负责绘制; 拖动与跳转由播放器的进度控件处理.
 *
 * @param showThumb 是否画拖动点.
 */
@Composable
fun PlayerProgressBar(
    progress: () -> Float,
    buffered: () -> Float,
    modifier: Modifier = Modifier,
    showThumb: Boolean = true,
) {
    val thumb = PlayerProgressBarDefaults.ThumbSize
    Box(
        modifier
            .fillMaxWidth()
            .height(thumb)
            .progressSemantics(progress),
    ) {
        Canvas(Modifier.fillMaxWidth().height(thumb)) {
            val trackHeight = 3.dp.toPx()
            val top = (size.height - trackHeight) / 2
            val p = progress().coerceIn(0f, 1f)
            val b = buffered().coerceIn(0f, 1f)
            drawTrack(top, size.width, trackHeight, Color.White.copy(alpha = 0.28f))
            drawTrack(top, size.width * b, trackHeight, Color.White.copy(alpha = 0.45f))
            drawTrack(top, size.width * p, trackHeight, AniColors.brandRing)
        }
        if (showThumb) {
            Canvas(
                Modifier
                    .fillMaxWidth()
                    .height(thumb),
            ) {
                val radius = thumb.toPx() / 2
                val x = (size.width * progress().coerceIn(0f, 1f)).coerceIn(radius, size.width - radius)
                drawCircle(Color.Black.copy(alpha = 0.3f), radius + 1.dp.toPx(), Offset(x, size.height / 2 + 1.dp.toPx()))
                drawCircle(Color.White, radius, Offset(x, size.height / 2))
            }
        }
    }
}

object PlayerProgressBarDefaults {
    val ThumbSize = 12.dp
}

@Composable
private fun ProgressTrack(
    progress: () -> Float,
    height: Dp,
    trackColor: Color,
    progressColor: Color,
    modifier: Modifier,
) {
    Canvas(
        modifier
            .fillMaxWidth()
            .height(height)
            .progressSemantics(progress),
    ) {
        val h = size.height
        drawTrack(0f, size.width, h, trackColor)
        drawTrack(0f, size.width * progress().coerceIn(0f, 1f), h, progressColor)
    }
}

private fun DrawScope.drawTrack(top: Float, width: Float, height: Float, color: Color) {
    if (width <= 0f) return
    drawRoundRect(
        color = color,
        topLeft = Offset(0f, top),
        size = Size(width, height),
        cornerRadius = CornerRadius(height / 2, height / 2),
    )
}

private fun Modifier.progressSemantics(progress: () -> Float): Modifier = semantics {
    progressBarRangeInfo = ProgressBarRangeInfo(progress().coerceIn(0f, 1f), 0f..1f)
}
