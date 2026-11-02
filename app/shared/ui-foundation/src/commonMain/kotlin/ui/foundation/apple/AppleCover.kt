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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C10 封面比例.
 */
enum class CoverAspect(val ratio: Float) {
    /** 探索推荐、搜索、条目详情、排行、快取、关系图. */
    Portrait3x4(3f / 4f),

    /** 追番列表与网格. */
    Portrait2x3(2f / 3f),
}

/**
 * C10 封面. 按 [aspect] 定比例, 圆角 [cornerRadius], 叠 1dp `rgba(0,0,0,0.06)` 内描边.
 *
 * 宽度小于 [AppleCoverDefaults.MinWidth] 时不放封面, 改为显示 [fallbackText].
 *
 * @param cornerRadius 列表 8, 网格 10, 推荐卡 12.
 * @param alert 在右上角显示 C12 提示点 (新集数).
 * @param progress 观看进度, 不为 `null` 时在底部显示 [CoverProgressBar].
 * @param image 封面图, 填满封面区域.
 */
@Composable
fun AppleCover(
    aspect: CoverAspect,
    fallbackText: String,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp,
    alert: Boolean = false,
    progress: (() -> Float)? = null,
    image: @Composable BoxScope.() -> Unit,
) {
    BoxWithConstraints(modifier) {
        if (maxWidth < AppleCoverDefaults.MinWidth) {
            Text(
                fallbackText,
                style = AniTextStyles.caption,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            return@BoxWithConstraints
        }
        val shape = RoundedCornerShape(cornerRadius)
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(aspect.ratio)
                .clip(shape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            Box(Modifier.fillMaxSize(), content = image)
            Box(Modifier.fillMaxSize().border(1.dp, Color.Black.copy(alpha = 0.06f), shape))
            if (progress != null) {
                CoverProgressBar(
                    progress,
                    Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 8.dp, end = 8.dp, bottom = 8.dp),
                )
            }
            if (alert) {
                AlertDot(
                    Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    style = AlertDotStyle.OnCover,
                )
            }
        }
    }
}

object AppleCoverDefaults {
    /** 小于此宽度不放封面, 改为纯文字 (设计决策). */
    val MinWidth = 42.dp
}
