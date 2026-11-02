/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.unit.dp

/**
 * Apple 版组件使用的线条图标. 24×24 视窗, 圆头圆角; 线宽以视窗单位计, 与设计稿的 SVG 相同.
 *
 * 颜色由 `Icon` 的 `tint` 决定. 同一图标与线宽只构建一次.
 *
 * @see docs/design/ani-style/02-components.md §0
 */
object AppleIcons {
    /** 浮动导航栏「探索」. */
    fun explore(strokeWidth: Float = 1.8f): ImageVector = cached("explore", strokeWidth) {
        stroke(circle(12f, 12f, 9f))
        stroke("M15.5 8.5l-2 5-5 2 2-5z")
    }

    /** 浮动导航栏「追番」. */
    fun collection(strokeWidth: Float = 1.8f): ImageVector = cached("collection", strokeWidth) {
        stroke("M6 3.5h12v17l-6-4-6 4z")
    }

    /** 浮动导航栏「快取」. */
    fun download(strokeWidth: Float = 1.8f): ImageVector = cached("download", strokeWidth) {
        stroke("M12 4v11")
        stroke("M7.5 10.5L12 15l4.5-4.5")
        stroke("M5 19.5h14")
    }

    /** 浮动导航栏「我的」. */
    fun profile(strokeWidth: Float = 1.8f): ImageVector = cached("profile", strokeWidth) {
        stroke(circle(12f, 8.5f, 3.5f))
        stroke("M5 20c1.2-3.6 4-5.5 7-5.5s5.8 1.9 7 5.5")
    }

    /** 进入下一页 `›`. */
    fun chevronRight(strokeWidth: Float = 2f): ImageVector = cached("chevronRight", strokeWidth) {
        stroke("M9 6l6 6-6 6")
    }

    /** 下拉 `⌄`. */
    fun chevronDown(strokeWidth: Float = 2.4f): ImageVector = cached("chevronDown", strokeWidth) {
        stroke("M6 9l6 6 6-6")
    }

    /** 返回. */
    fun back(strokeWidth: Float = 2.2f): ImageVector = cached("back", strokeWidth) {
        stroke("M15 5l-7 7 7 7")
    }

    /** 打开外部链接或系统设置 `↗`. */
    fun externalLink(strokeWidth: Float = 2.4f): ImageVector = cached("externalLink", strokeWidth) {
        stroke("M8 16L16 8")
        stroke("M9.5 8H16v6.5")
    }

    /** 弹出值的上下双箭头. */
    fun upDown(strokeWidth: Float = 2.4f): ImageVector = cached("upDown", strokeWidth) {
        stroke("M8 9.5l4-4 4 4")
        stroke("M8 14.5l4 4 4-4")
    }

    /** 勾. */
    fun check(strokeWidth: Float = 2.4f): ImageVector = cached("check", strokeWidth) {
        stroke("M5 12.5l4.5 4.5L19 7.5")
    }

    /** 加号. */
    fun plus(strokeWidth: Float = 2.2f): ImageVector = cached("plus", strokeWidth) {
        stroke("M12 5v14")
        stroke("M5 12h14")
    }

    private val cache = mutableMapOf<Pair<String, Float>, ImageVector>()

    private fun cached(name: String, strokeWidth: Float, paths: LineIconBuilder.() -> Unit): ImageVector =
        cache.getOrPut(name to strokeWidth) {
            val builder = ImageVector.Builder(
                name = "AppleIcons.$name",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
            LineIconBuilder(builder, strokeWidth).paths()
            builder.build()
        }

    private class LineIconBuilder(
        private val builder: ImageVector.Builder,
        private val strokeWidth: Float,
    ) {
        fun stroke(pathData: String) {
            builder.addPath(
                pathData = addPathNodes(pathData),
                stroke = SolidColor(Color.Black),
                strokeLineWidth = strokeWidth,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }

    private fun circle(cx: Float, cy: Float, r: Float): String =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0z"
}
