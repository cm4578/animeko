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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.ani.app.ui.foundation.theme.AniColors

/**
 * C11 头像与光环的尺寸.
 *
 * @property size 头像直径.
 * @property gap 头像与光环之间的间隙.
 * @property ring 光环宽度.
 */
enum class AvatarRingSize(val size: Dp, val gap: Dp, val ring: Dp) {
    /** 手机「我的」页个人卡. */
    ProfileCard(60.dp, 2.5.dp, 2.5.dp),

    /** 手机编辑个人资料. */
    EditProfile(88.dp, 3.dp, 3.dp),

    /** macOS 侧边栏帐号行. */
    MacSidebar(28.dp, 2.dp, 1.5.dp),
}

/**
 * C11 品牌头像光环: brandSlate 圆底 + 白色粗体首字母; 外侧为间隙 + [AniColors.brandRing] 光环.
 *
 * 间隙与光环画在头像外侧, 不占布局尺寸, 与设计稿相同.
 *
 * @param gapColor 间隙颜色, 为头像所在的背景色. 默认 card.
 * @param image 头像图片; 为 `null` 时显示 [initial].
 */
@Composable
fun AvatarRing(
    initial: String,
    size: AvatarRingSize,
    modifier: Modifier = Modifier,
    gapColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    image: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier
            .size(size.size)
            .ring(gap = size.gap, ring = size.ring, gapColor = gapColor)
            .clip(CircleShape)
            .background(AniColors.brandSlate),
        contentAlignment = Alignment.Center,
    ) {
        if (image != null) {
            image()
        } else {
            Text(
                initial,
                color = Color.White,
                fontSize = (size.size.value * 0.4f).sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
            )
        }
    }
}

/**
 * C11 品牌标志: 石板圆 + 光环 (间隙 3、光环 3) + 中间 brandRing 播放三角.
 *
 * @param size 手机登录与关于 76; macOS 登录 72、关于 64、扫码面板 40.
 * @param gapColor 间隙颜色. macOS 关于页为 card.
 */
@Composable
fun BrandMark(
    size: Dp,
    modifier: Modifier = Modifier,
    gapColor: Color = MaterialTheme.colorScheme.surfaceContainerLowest,
) {
    Box(
        modifier
            .size(size)
            .ring(gap = 3.dp, ring = 3.dp, gapColor = gapColor)
            .background(AniColors.brandSlate, CircleShape),
    ) {
        Canvas(Modifier.fillMaxSize()) {
            // 视觉重心略向右, 三角形外接框为直径的 36%.
            val side = this.size.minDimension * 0.36f
            val left = center.x - side * 0.38f
            val top = center.y - side / 2
            val triangle = Path().apply {
                moveTo(left, top)
                lineTo(left, top + side)
                lineTo(left + side * 0.9f, center.y)
                close()
            }
            drawPath(triangle, AniColors.brandRing)
        }
    }
}

/**
 * C11 软件更新新版本卡的 App 图标: 52×52、圆角 14、brandSlate 底, 中央 22 圆环 (3.5 宽 brandRing).
 */
@Composable
fun AppIconBadge(modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(52.dp)
            .background(AniColors.brandSlate, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(22.dp)) {
            val strokeWidth = 3.5.dp.toPx()
            drawCircle(
                AniColors.brandRing,
                radius = (this.size.minDimension - strokeWidth) / 2,
                center = Offset(this.size.width / 2, this.size.height / 2),
                style = Stroke(strokeWidth),
            )
        }
    }
}

/**
 * 在元素外侧画间隙与光环, 不改变布局尺寸.
 */
private fun Modifier.ring(gap: Dp, ring: Dp, gapColor: Color): Modifier = drawBehind {
    val radius = size.minDimension / 2
    drawCircle(AniColors.brandRing, radius + (gap + ring).toPx())
    drawCircle(gapColor, radius + gap.toPx())
}
