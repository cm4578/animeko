/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.colorspace.ColorSpaces
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamicColorScheme
import com.materialkolor.hct.Hct
import com.materialkolor.ktx.toHct
import me.him188.ani.app.ui.theme.DefaultSeedColor

/**
 * Ani 配色: 背景、卡片、导航为中性石板色, 强调色由主题色推导.
 *
 * 在 materialkolor 以强调色生成的配色上覆写角色; `*Fixed` 系列沿用生成结果.
 * 角色映射与色值见 docs/design/ani-style/01-tokens.md §4.
 *
 * 高对比度深色主题由调用方在结果上套用 [modifyColorSchemeForBlackBackground].
 */
fun aniColorScheme(seedColor: Color, isDark: Boolean): ColorScheme {
    val a = aniAccent(seedColor, isDark)
    val n = if (isDark) AniNeutrals.Dark else AniNeutrals.Light
    val base = dynamicColorScheme(
        primary = a.accent,
        isDark = isDark,
        isAmoled = false,
        style = PaletteStyle.TonalSpot,
    )
    return base.copy(
        primary = a.accent,
        onPrimary = a.onAccent,
        primaryContainer = a.tint,
        onPrimaryContainer = a.onTint,
        inversePrimary = a.inverse,
        secondary = n.label2,
        onSecondary = n.onSecondary,
        secondaryContainer = a.tint,
        onSecondaryContainer = a.onTint,
        // 没有第三种强调色: 评分等文字与图标用 label, 选中态等容器用 tint.
        tertiary = n.label,
        onTertiary = n.onSecondary,
        tertiaryContainer = a.tint,
        onTertiaryContainer = a.onTint,
        background = n.bg,
        onBackground = n.label,
        surface = n.bg,
        onSurface = n.label,
        surfaceVariant = n.fill,
        onSurfaceVariant = n.label2,
        // 与 surface 同值, 带 tonalElevation 的 Surface 不会泛出强调色.
        surfaceTint = n.bg,
        inverseSurface = n.inverseSurface,
        inverseOnSurface = n.inverseOnSurface,
        error = n.error,
        onError = n.onError,
        errorContainer = n.errorContainer,
        onErrorContainer = n.onErrorContainer,
        outline = n.outline,
        outlineVariant = n.separator,
        scrim = n.scrim,
        surfaceBright = n.surfaceBright,
        surfaceDim = n.surfaceDim,
        surfaceContainerLowest = n.bg,
        surfaceContainerLow = n.card,
        surfaceContainer = n.tabBg,
        surfaceContainerHigh = n.card,
        surfaceContainerHighest = n.fill,
    )
}

/**
 * 由主题色推导的强调色组.
 *
 * @property accent 链接文字、当前位置、进度、选中图标. 对应 M3 `primary`.
 * @property onAccent M3 `onPrimary`.
 * @property tint 强调淡底: 选中胶囊、选中行、当前集. 对应 M3 `primaryContainer` 与 `secondaryContainer`.
 * @property onTint [tint] 上的文字.
 * @property inverse M3 `inversePrimary`, 显示在 `inverseSurface` 上.
 */
@Immutable
data class AniAccent(
    val accent: Color,
    val onAccent: Color,
    val tint: Color,
    val onTint: Color,
    val inverse: Color,
)

/**
 * 按 docs/design/ani-style/01-tokens.md §6 推导强调色.
 *
 * [seedColor] 为 [DefaultSeedColor] (用户没改过主题色) 时返回品牌固定值;
 * 其他种子色取其 HCT 色相, 按固定的 chroma 与 tone 生成, 对比度只由 tone 决定.
 */
fun aniAccent(seedColor: Color, isDark: Boolean): AniAccent {
    if (seedColor == DefaultSeedColor) {
        return if (isDark) AniBrandAccents.Dark else AniBrandAccents.Light
    }
    val hue = seedColor.toHct().hue
    return if (isDark) {
        val accent = hctColor(hue, 36.0, 70.0)
        AniAccent(
            accent = accent,
            onAccent = AniNeutrals.Dark.bg,
            tint = mixSrgb(accent, AniNeutrals.Dark.card, 0.18f),
            onTint = hctColor(hue, 24.0, 87.0),
            inverse = hctColor(hue, 48.0, 40.0),
        )
    } else {
        val accent = hctColor(hue, 48.0, 46.0)
        AniAccent(
            accent = accent,
            onAccent = Color.White,
            tint = mixSrgb(accent, Color.White, 0.12f),
            onTint = hctColor(hue, 40.0, 30.0),
            inverse = hctColor(hue, 36.0, 70.0),
        )
    }
}

/**
 * 主要按钮底色. 浅色为 [primary]; 深色为白字可读的 tone 48 版本, 默认品牌色取固定值.
 *
 * 只读 [primary] 而不读种子色, 所以用 [primary] 是否等于品牌值判断是否为默认主题色.
 */
internal fun aniAccentFill(primary: Color, isDark: Boolean): Color {
    if (!isDark) return primary
    if (primary == AniBrandAccents.Dark.accent) return AniBrandAccents.DarkAccentFill
    val hct = primary.toHct()
    return hctColor(hct.hue, hct.chroma, 48.0)
}

/**
 * 在 sRGB 通道上线性混合: `top * fraction + bottom * (1 - fraction)`.
 */
internal fun mixSrgb(top: Color, bottom: Color, fraction: Float): Color {
    val t = top.convert(ColorSpaces.Srgb)
    val b = bottom.convert(ColorSpaces.Srgb)
    val rest = 1f - fraction
    return Color(
        red = t.red * fraction + b.red * rest,
        green = t.green * fraction + b.green * rest,
        blue = t.blue * fraction + b.blue * rest,
        alpha = t.alpha * fraction + b.alpha * rest,
    )
}

private fun hctColor(hue: Double, chroma: Double, tone: Double): Color =
    Color(Hct.from(hue, chroma, tone).toInt())

/**
 * 默认主题色的品牌强调色.
 */
internal object AniBrandAccents {
    val Light = AniAccent(
        accent = Color(0xFF3072A1),
        onAccent = Color(0xFFFFFFFF),
        tint = Color(0xFFE6EEF4),
        onTint = Color(0xFF1B4A6E),
        inverse = Color(0xFF76AEDD),
    )
    val Dark = AniAccent(
        accent = Color(0xFF76AEDD),
        onAccent = Color(0xFF0D141B),
        tint = Color(0xFF293A49),
        onTint = Color(0xFFC1DDF5),
        inverse = Color(0xFF2A6590),
    )
    val DarkAccentFill = Color(0xFF2F78A9)
}

/**
 * 不随主题色改变的中性色, 见 docs/design/ani-style/01-tokens.md §2.
 */
@Immutable
internal class AniNeutralColors(
    val bg: Color,
    val card: Color,
    val tabBg: Color,
    val fill: Color,
    val separator: Color,
    val label: Color,
    val labelSoft: Color,
    val label2: Color,
    val onSecondary: Color,
    val inverseSurface: Color,
    val inverseOnSurface: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val outline: Color,
    val scrim: Color,
    val surfaceBright: Color,
    val surfaceDim: Color,
    val alert: Color,
    val floatingShadow: Color,
    val sidebar: Color,
    val sidebarField: Color,
    val hover: Color,
)

internal object AniNeutrals {
    val Light = AniNeutralColors(
        bg = Color(0xFFF1F4F9),
        card = Color(0xFFFFFFFF),
        tabBg = Color(0xFFFFFFFF),
        fill = Color(0xFFE6EBF3),
        separator = Color(0xFFC7CCD4),
        label = Color(0xFF131D26),
        labelSoft = Color(0xFF3A434C),
        label2 = Color(0xFF5C646D),
        onSecondary = Color(0xFFFFFFFF),
        inverseSurface = Color(0xFF1E2833),
        inverseOnSurface = Color(0xFFE6EAEF),
        error = Color(0xFFD70015),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFBE6E8),
        onErrorContainer = Color(0xFF8C0009),
        outline = Color(0xFF7A828B),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFFFFFFFF),
        surfaceDim = Color(0xFFE6EBF3),
        alert = Color(0xFFE2553A),
        floatingShadow = Color(0xFF131D26).copy(alpha = 0.14f),
        sidebar = Color(0xFFE7ECF2),
        sidebarField = Color(0xFF131D26).copy(alpha = 0.06f),
        hover = Color(0xFF131D26).copy(alpha = 0.05f),
    )
    val Dark = AniNeutralColors(
        bg = Color(0xFF0D141B),
        card = Color(0xFF182029),
        tabBg = Color(0xFF1E2833),
        fill = Color(0xFF252F39),
        separator = Color(0xFF303A44),
        label = Color(0xFFE6EAEF),
        labelSoft = Color(0xFFC5CBD1),
        label2 = Color(0xFF9FA7B0),
        onSecondary = Color(0xFF0D141B),
        inverseSurface = Color(0xFFE6EAEF),
        inverseOnSurface = Color(0xFF182029),
        error = Color(0xFFFF6961),
        onError = Color(0xFF3C110E),
        errorContainer = Color(0xFF452A2A),
        onErrorContainer = Color(0xFFFFB4AB),
        outline = Color(0xFF7D8792),
        scrim = Color(0xFF000000),
        surfaceBright = Color(0xFF252F39),
        surfaceDim = Color(0xFF0D141B),
        alert = Color(0xFFF07457),
        floatingShadow = Color.Black.copy(alpha = 0.45f),
        sidebar = Color(0xFF141C24),
        sidebarField = Color.White.copy(alpha = 0.07f),
        hover = Color.White.copy(alpha = 0.05f),
    )
}
