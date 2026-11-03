/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C13 选集方块的状态.
 */
enum class EpisodeTileState {
    /** fill 底、label2 W500. */
    Watched,

    /** tint 底、1.5 accent 内描边、底部 accent 短横、onTint W700. */
    Playing,

    /** card 底、1 separator 内描边、label W500. */
    Unwatched,
}

/**
 * C13 选集方块: 52×52、圆角 12, 文字为 17 号等宽数字.
 *
 * @param label 集数文字. 整数集数用 [episodeTileLabel] 补零到两位.
 * @param onLongClick 长按, 例如切换看过. `null` 时不响应长按.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EpisodeTile(
    label: String,
    state: EpisodeTileState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onLongClick: (() -> Unit)? = null,
) {
    val colorScheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    val containerColor: Color
    val textColor: Color
    when (state) {
        EpisodeTileState.Watched -> {
            containerColor = colorScheme.surfaceContainerHighest
            textColor = colorScheme.secondary
        }

        EpisodeTileState.Playing -> {
            containerColor = colorScheme.secondaryContainer
            textColor = colorScheme.onSecondaryContainer
        }

        EpisodeTileState.Unwatched -> {
            containerColor = colorScheme.surfaceContainerLow
            textColor = colorScheme.onSurface
        }
    }
    Box(
        modifier
            .size(EpisodeTileDefaults.Size)
            .clip(shape)
            .background(containerColor)
            .then(
                when (state) {
                    EpisodeTileState.Playing -> Modifier.border(1.5.dp, colorScheme.primary, shape)
                    EpisodeTileState.Unwatched -> Modifier.border(1.dp, colorScheme.outlineVariant, shape)
                    EpisodeTileState.Watched -> Modifier
                },
            )
            .semantics { selected = state == EpisodeTileState.Playing }
            .combinedClickable(role = Role.Button, onLongClick = onLongClick, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = episodeTileTextStyle(),
            fontWeight = if (state == EpisodeTileState.Playing) FontWeight.Bold else FontWeight.Medium,
            color = textColor,
            maxLines = 1,
        )
        if (state == EpisodeTileState.Playing) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp)
                    .size(16.dp, 3.dp)
                    .background(colorScheme.primary, RoundedCornerShape(2.dp)),
            )
        }
    }
}

/**
 * 整数集数补零到两位: 4 → "04", 12 → "12", 120 → "120".
 */
fun episodeTileLabel(number: Int): String = number.toString().padStart(2, '0')

object EpisodeTileDefaults {
    val Size = 52.dp
}

@Composable
private fun episodeTileTextStyle(): TextStyle =
    AniTextStyles.headline.copy(fontSize = 17.sp, lineHeight = 22.sp, fontFeatureSettings = "tnum")
