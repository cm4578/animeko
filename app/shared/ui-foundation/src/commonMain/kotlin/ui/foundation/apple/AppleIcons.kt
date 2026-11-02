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
    /** 浮动导航栏与 macOS 侧边栏「探索」. */
    fun explore(strokeWidth: Float = 1.8f): ImageVector = cached("explore", strokeWidth) {
        stroke(circle(12f, 12f, 9f))
        stroke("M15.5 8.5l-2 5-5 2 2-5z")
    }

    /** 浮动导航栏与 macOS 侧边栏「追番」. */
    fun collection(strokeWidth: Float = 1.8f): ImageVector = cached("collection", strokeWidth) {
        stroke("M6 3.5h12v17l-6-4-6 4z")
    }

    /** 浮动导航栏与 macOS 侧边栏「快取」. */
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

    /** macOS 侧边栏「新番时间表」. */
    fun schedule(strokeWidth: Float = 1.8f): ImageVector = cached("schedule", strokeWidth) {
        stroke(rect(3.5f, 5f, 17f, 15f, 3f))
        stroke("M3.5 10h17")
        stroke("M8 3v4")
        stroke("M16 3v4")
    }

    /** macOS 侧边栏「热度排行」. */
    fun trending(strokeWidth: Float = 1.8f): ImageVector = cached("trending", strokeWidth) {
        stroke("M4 16l5-5 4 4 7-7")
        stroke("M15 8h5v5")
    }

    /** macOS 侧边栏「播放记录」. */
    fun history(strokeWidth: Float = 1.8f): ImageVector = cached("history", strokeWidth) {
        stroke(circle(12f, 12f, 8.5f))
        stroke("M12 7.5V12l3 2")
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

    /** macOS 设定行的 `↗`, 箭身比 [externalLink] 长. */
    fun macExternalLink(strokeWidth: Float = 2.2f): ImageVector = cached("macExternalLink", strokeWidth) {
        stroke("M7 17L17 7")
        stroke("M9 7h8v8")
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

    /** macOS 工具栏「下一页」. */
    fun forward(strokeWidth: Float = 2.2f): ImageVector = cached("forward", strokeWidth) {
        stroke("M9 5l7 7-7 7")
    }

    /** 关闭 `×`. */
    fun close(strokeWidth: Float = 2.2f): ImageVector = cached("close", strokeWidth) {
        stroke("M6 6l12 12")
        stroke("M18 6L6 18")
    }

    /** 搜索框的放大镜. */
    fun search(strokeWidth: Float = 2.4f): ImageVector = cached("search", strokeWidth) {
        stroke(circle(11f, 11f, 7f))
        stroke("M20 20l-3.5-3.5")
    }

    /** macOS「隐藏侧边栏」与「显示侧边栏」. */
    fun sidebar(strokeWidth: Float = 1.8f): ImageVector = cached("sidebar", strokeWidth) {
        stroke(rect(3f, 5f, 18f, 14f, 3f))
        stroke("M9.5 5v14")
    }

    /** macOS 工具栏的检阅器开关. */
    fun inspector(strokeWidth: Float = 1.9f): ImageVector = cached("inspector", strokeWidth) {
        stroke(rect(3f, 5f, 18f, 14f, 3f))
        stroke("M14.5 5v14")
    }

    /** 通知铃铛. */
    fun bell(strokeWidth: Float = 1.8f): ImageVector = cached("bell", strokeWidth) {
        stroke("M6 16v-5a6 6 0 0 1 12 0v5l1.5 2h-15z")
        stroke("M10 20.5a2 2 0 0 0 4 0")
    }

    /** 设定齿轮. */
    fun settings(strokeWidth: Float = 1.8f): ImageVector = cached("settings", strokeWidth) {
        stroke(circle(12f, 12f, 3f))
        stroke(
            "M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 " +
                "1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.1a1.7 1.7 0 0 0-1.1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1" +
                "a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.1" +
                "a1.7 1.7 0 0 0 1.5-1.1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9" +
                "a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.1a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1" +
                "a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9a1.7 1.7 0 0 0 1.5 1H21a2 2 0 1 1 0 4h-.1" +
                "a1.7 1.7 0 0 0-1.5 1z",
        )
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

    /** 圆角矩形, 与 SVG 的 `<rect x y width height rx>` 相同. */
    private fun rect(x: Float, y: Float, width: Float, height: Float, r: Float): String =
        "M${x + r} ${y}h${width - 2 * r}a$r $r 0 0 1 $r ${r}v${height - 2 * r}" +
            "a$r $r 0 0 1 ${-r} ${r}h${-(width - 2 * r)}a$r $r 0 0 1 ${-r} ${-r}v${-(height - 2 * r)}" +
            "a$r $r 0 0 1 $r ${-r}z"
}
