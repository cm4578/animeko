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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import me.him188.ani.app.ui.theme.DefaultSeedColor
import me.him188.ani.app.ui.theme.themeColorOptions
import kotlin.math.max
import kotlin.math.min
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * docs/design/ani-style/01-tokens.md §10.
 */
class AniColorSchemeTest {
    private val testedSeeds: List<Color> = AniThemeDefaults.themeColorOptions + listOf(
        Color(0xFF808080),
        Color(0xFFFFFF00),
        Color(0xFF0000FF),
        Color(0xFFFF0000),
    )

    @Test
    fun `default seed uses brand colors in light scheme`() {
        val expected = mapOf(
            "primary" to 0xFF3072A1,
            "onPrimary" to 0xFFFFFFFF,
            "primaryContainer" to 0xFFE6EEF4,
            "onPrimaryContainer" to 0xFF1B4A6E,
            "inversePrimary" to 0xFF76AEDD,
            "secondary" to 0xFF5C646D,
            "onSecondary" to 0xFFFFFFFF,
            "secondaryContainer" to 0xFFE6EEF4,
            "onSecondaryContainer" to 0xFF1B4A6E,
            "tertiary" to 0xFF131D26,
            "onTertiary" to 0xFFFFFFFF,
            "tertiaryContainer" to 0xFFE6EEF4,
            "onTertiaryContainer" to 0xFF1B4A6E,
            "background" to 0xFFF1F4F9,
            "onBackground" to 0xFF131D26,
            "surface" to 0xFFF1F4F9,
            "onSurface" to 0xFF131D26,
            "surfaceVariant" to 0xFFE6EBF3,
            "onSurfaceVariant" to 0xFF5C646D,
            "surfaceTint" to 0xFFF1F4F9,
            "inverseSurface" to 0xFF1E2833,
            "inverseOnSurface" to 0xFFE6EAEF,
            "error" to 0xFFD70015,
            "onError" to 0xFFFFFFFF,
            "errorContainer" to 0xFFFBE6E8,
            "onErrorContainer" to 0xFF8C0009,
            "outline" to 0xFF7A828B,
            "outlineVariant" to 0xFFC7CCD4,
            "scrim" to 0xFF000000,
            "surfaceBright" to 0xFFFFFFFF,
            "surfaceDim" to 0xFFE6EBF3,
            "surfaceContainerLowest" to 0xFFF1F4F9,
            "surfaceContainerLow" to 0xFFFFFFFF,
            "surfaceContainer" to 0xFFFFFFFF,
            "surfaceContainerHigh" to 0xFFFFFFFF,
            "surfaceContainerHighest" to 0xFFE6EBF3,
        )
        assertEquals(
            expected.mapValues { Color(it.value) },
            aniColorScheme(DefaultSeedColor, isDark = false).overriddenRoles(),
        )
    }

    @Test
    fun `default seed uses brand colors in dark scheme`() {
        val expected = mapOf(
            "primary" to 0xFF76AEDD,
            "onPrimary" to 0xFF0D141B,
            "primaryContainer" to 0xFF293A49,
            "onPrimaryContainer" to 0xFFC1DDF5,
            "inversePrimary" to 0xFF2A6590,
            "secondary" to 0xFF9FA7B0,
            "onSecondary" to 0xFF0D141B,
            "secondaryContainer" to 0xFF293A49,
            "onSecondaryContainer" to 0xFFC1DDF5,
            "tertiary" to 0xFFE6EAEF,
            "onTertiary" to 0xFF0D141B,
            "tertiaryContainer" to 0xFF293A49,
            "onTertiaryContainer" to 0xFFC1DDF5,
            "background" to 0xFF0D141B,
            "onBackground" to 0xFFE6EAEF,
            "surface" to 0xFF0D141B,
            "onSurface" to 0xFFE6EAEF,
            "surfaceVariant" to 0xFF252F39,
            "onSurfaceVariant" to 0xFF9FA7B0,
            "surfaceTint" to 0xFF0D141B,
            "inverseSurface" to 0xFFE6EAEF,
            "inverseOnSurface" to 0xFF182029,
            "error" to 0xFFFF6961,
            "onError" to 0xFF3C110E,
            "errorContainer" to 0xFF452A2A,
            "onErrorContainer" to 0xFFFFB4AB,
            "outline" to 0xFF7D8792,
            "outlineVariant" to 0xFF303A44,
            "scrim" to 0xFF000000,
            "surfaceBright" to 0xFF252F39,
            "surfaceDim" to 0xFF0D141B,
            "surfaceContainerLowest" to 0xFF0D141B,
            "surfaceContainerLow" to 0xFF182029,
            "surfaceContainer" to 0xFF1E2833,
            "surfaceContainerHigh" to 0xFF182029,
            "surfaceContainerHighest" to 0xFF252F39,
        )
        assertEquals(
            expected.mapValues { Color(it.value) },
            aniColorScheme(DefaultSeedColor, isDark = true).overriddenRoles(),
        )
    }

    @Test
    fun `derived schemes meet contrast requirements`() {
        val failures = buildList<String> {
            for (seed in testedSeeds) {
                for (isDark in listOf(false, true)) {
                    val s = aniColorScheme(seed, isDark)
                    fun check(name: String, foreground: Color, background: Color, required: Double) {
                        val ratio = contrastRatio(foreground, background)
                        if (ratio < required) {
                            add("seed=$seed isDark=$isDark $name: $ratio < $required")
                        }
                    }
                    check("primary/surfaceContainerLowest", s.primary, s.surfaceContainerLowest, 4.5)
                    check("primary/surfaceContainerLow", s.primary, s.surfaceContainerLow, 4.5)
                    check("onPrimary/primary", s.onPrimary, s.primary, 4.5)
                    check("onSecondaryContainer/secondaryContainer", s.onSecondaryContainer, s.secondaryContainer, 4.5)
                    check("tertiary/surfaceContainerLowest", s.tertiary, s.surfaceContainerLowest, 4.5)
                    check("onTertiary/tertiary", s.onTertiary, s.tertiary, 4.5)
                    check("onTertiaryContainer/tertiaryContainer", s.onTertiaryContainer, s.tertiaryContainer, 4.5)
                    check(
                        "onSurfaceVariant/surfaceContainerHighest",
                        s.onSurfaceVariant,
                        s.surfaceContainerHighest,
                        4.5,
                    )
                    check("inversePrimary/inverseSurface", s.inversePrimary, s.inverseSurface, 4.5)
                    check("outline/surfaceContainerLowest", s.outline, s.surfaceContainerLowest, 3.0)
                }
            }
        }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }

    @Test
    fun `accent fill keeps white text readable`() {
        val failures = buildList<String> {
            for (seed in testedSeeds) {
                for (isDark in listOf(false, true)) {
                    val accentFill = aniAccentFill(aniColorScheme(seed, isDark).primary, isDark)
                    val ratio = contrastRatio(Color.White, accentFill)
                    if (ratio < 4.5) {
                        add("seed=$seed isDark=$isDark accentFill=$accentFill: $ratio < 4.5")
                    }
                }
            }
        }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
        val brandDark = aniColorScheme(DefaultSeedColor, isDark = true)
        assertEquals(Color(0xFF2F78A9), aniAccentFill(brandDark.primary, isDark = true))
        val brandLight = aniColorScheme(DefaultSeedColor, isDark = false)
        assertEquals(Color(0xFF3072A1), aniAccentFill(brandLight.primary, isDark = false))
    }

    @Test
    fun `surface tint equals surface`() {
        for (seed in testedSeeds) {
            for (isDark in listOf(false, true)) {
                val s = aniColorScheme(seed, isDark)
                assertEquals(s.surface, s.surfaceTint, "seed=$seed isDark=$isDark")
            }
        }
    }

    private fun contrastRatio(a: Color, b: Color): Double {
        val la = a.luminance().toDouble()
        val lb = b.luminance().toDouble()
        return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)
    }

    /**
     * 01-tokens.md §4.2 覆写的角色. `*Fixed` 系列沿用 materialkolor 的生成结果, 不在此列.
     */
    private fun ColorScheme.overriddenRoles(): Map<String, Color> = mapOf(
        "primary" to primary,
        "onPrimary" to onPrimary,
        "primaryContainer" to primaryContainer,
        "onPrimaryContainer" to onPrimaryContainer,
        "inversePrimary" to inversePrimary,
        "secondary" to secondary,
        "onSecondary" to onSecondary,
        "secondaryContainer" to secondaryContainer,
        "onSecondaryContainer" to onSecondaryContainer,
        "tertiary" to tertiary,
        "onTertiary" to onTertiary,
        "tertiaryContainer" to tertiaryContainer,
        "onTertiaryContainer" to onTertiaryContainer,
        "background" to background,
        "onBackground" to onBackground,
        "surface" to surface,
        "onSurface" to onSurface,
        "surfaceVariant" to surfaceVariant,
        "onSurfaceVariant" to onSurfaceVariant,
        "surfaceTint" to surfaceTint,
        "inverseSurface" to inverseSurface,
        "inverseOnSurface" to inverseOnSurface,
        "error" to error,
        "onError" to onError,
        "errorContainer" to errorContainer,
        "onErrorContainer" to onErrorContainer,
        "outline" to outline,
        "outlineVariant" to outlineVariant,
        "scrim" to scrim,
        "surfaceBright" to surfaceBright,
        "surfaceDim" to surfaceDim,
        "surfaceContainerLowest" to surfaceContainerLowest,
        "surfaceContainerLow" to surfaceContainerLow,
        "surfaceContainer" to surfaceContainer,
        "surfaceContainerHigh" to surfaceContainerHigh,
        "surfaceContainerHighest" to surfaceContainerHighest,
    )
}
