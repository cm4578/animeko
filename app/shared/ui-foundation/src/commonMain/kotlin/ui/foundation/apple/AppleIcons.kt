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

    /** 上一项 `‹`, 例如时间表的上一周. */
    fun chevronLeft(strokeWidth: Float = 2f): ImageVector = cached("chevronLeft", strokeWidth) {
        stroke("M15 6l-6 6 6 6")
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

    /** 实心播放三角, 用于「继续观看」的播放钮. */
    fun playFilled(): ImageVector = cached("playFilled", 0f) {
        fill("M8 5v14l11-7z")
    }

    /** 追番「列表排版」. */
    fun listLayout(strokeWidth: Float = 2f): ImageVector = cached("listLayout", strokeWidth) {
        stroke("M9 6h11")
        stroke("M9 12h11")
        stroke("M9 18h11")
        stroke("M4.5 6h0.01")
        stroke("M4.5 12h0.01")
        stroke("M4.5 18h0.01")
    }

    /** 追番「网格排版」. */
    fun gridLayout(strokeWidth: Float = 2f): ImageVector = cached("gridLayout", strokeWidth) {
        stroke(rect(4f, 4f, 6.5f, 6.5f, 1.5f))
        stroke(rect(13.5f, 4f, 6.5f, 6.5f, 1.5f))
        stroke(rect(4f, 13.5f, 6.5f, 6.5f, 1.5f))
        stroke(rect(13.5f, 13.5f, 6.5f, 6.5f, 1.5f))
    }

    /** 「我的」页「Bangumi 同步」. */
    fun sync(strokeWidth: Float = 2f): ImageVector = cached("sync", strokeWidth) {
        stroke("M20 12a8 8 0 0 1-14.3 4.9")
        stroke("M4 12a8 8 0 0 1 14.3-4.9")
        stroke("M18.5 3.5v4h-4")
        stroke("M5.5 20.5v-4h4")
    }

    /** 圆圈内的勾, 用于「标为看过」. */
    fun checkCircle(strokeWidth: Float = 1.9f): ImageVector = cached("checkCircle", strokeWidth) {
        stroke(circle(12f, 12f, 8.5f))
        stroke("M8.5 12.2l2.5 2.5 4.5-5")
    }

    /** 圆圈内的减号, 用于「取消追番」. */
    fun minusCircle(strokeWidth: Float = 1.9f): ImageVector = cached("minusCircle", strokeWidth) {
        stroke(circle(12f, 12f, 8.5f))
        stroke("M8.5 12h7")
    }

    // 以下为设定分类图标: 手机「我的」页的设定列与 macOS 设定窗口的分类栏共用.

    /** 设定「介面」. */
    fun settingsAppearance(strokeWidth: Float = 2f): ImageVector = cached("settingsAppearance", strokeWidth) {
        stroke(rect(4f, 4f, 16f, 16f, 3f))
        stroke("M4 10h16")
        stroke("M10 10v10")
    }

    /** 设定「主题与色彩」. */
    fun settingsTheme(strokeWidth: Float = 2f): ImageVector = cached("settingsTheme", strokeWidth) {
        stroke(circle(12f, 12f, 8f))
        fill("M12 4a8 8 0 0 1 0 16z")
    }

    /** 设定「播放器和弹幕过滤」. */
    fun settingsPlayer(strokeWidth: Float = 2f): ImageVector = cached("settingsPlayer", strokeWidth) {
        stroke(rect(3.5f, 5f, 17f, 14f, 3f))
        fill("M10.5 9.5v5l4-2.5z")
    }

    /** 设定「资料源管理」. */
    fun settingsMediaSource(strokeWidth: Float = 2f): ImageVector = cached("settingsMediaSource", strokeWidth) {
        stroke(ellipse(12f, 6f, 7f, 2.5f))
        stroke("M5 6v6c0 1.4 3.1 2.5 7 2.5s7-1.1 7-2.5V6")
        stroke("M5 12v6c0 1.4 3.1 2.5 7 2.5s7-1.1 7-2.5v-6")
    }

    /** 设定「观看偏好」. */
    fun settingsMediaSelector(strokeWidth: Float = 2f): ImageVector = cached("settingsMediaSelector", strokeWidth) {
        stroke("M2.5 12s3.5-6.5 9.5-6.5 9.5 6.5 9.5 6.5-3.5 6.5-9.5 6.5S2.5 12 2.5 12z")
        stroke(circle(12f, 12f, 2.8f))
    }

    /** 设定「服务器区域」. */
    fun settingsServer(strokeWidth: Float = 2f): ImageVector = cached("settingsServer", strokeWidth) {
        stroke(circle(12f, 12f, 8.5f))
        stroke("M3.5 12h17")
        stroke("M12 3.5c2.5 2.6 3.6 5.4 3.6 8.5s-1.1 5.9-3.6 8.5c-2.5-2.6-3.6-5.4-3.6-8.5s1.1-5.9 3.6-8.5z")
    }

    /** 设定「代理」. */
    fun settingsProxy(strokeWidth: Float = 2f): ImageVector = cached("settingsProxy", strokeWidth) {
        stroke("M4 8h13")
        stroke("M14 4.5L17.5 8 14 11.5")
        stroke("M20 16H7")
        stroke("M10 12.5L6.5 16l3.5 3.5")
    }

    /** 设定「BitTorrent」. */
    fun settingsBitTorrent(strokeWidth: Float = 2f): ImageVector = cached("settingsBitTorrent", strokeWidth) {
        stroke(circle(6f, 12f, 2.5f))
        stroke(circle(18f, 6f, 2.5f))
        stroke(circle(18f, 18f, 2.5f))
        stroke("M8.2 10.9l7.6-3.8")
        stroke("M8.2 13.1l7.6 3.8")
    }

    /** 设定「储存」. */
    fun settingsStorage(strokeWidth: Float = 2f): ImageVector = cached("settingsStorage", strokeWidth) {
        stroke(rect(3.5f, 13f, 17f, 6.5f, 2f))
        stroke("M5 13l2.5-7.5h9L19 13")
        stroke("M16.5 16.3h.01")
    }

    /** 设定「软件更新」. */
    fun settingsUpdate(strokeWidth: Float = 2f): ImageVector = cached("settingsUpdate", strokeWidth) {
        stroke(circle(12f, 12f, 8f))
        stroke("M12 8v7")
        stroke("M9 12.5l3 3 3-3")
    }

    /** 设定「日志」. */
    fun settingsLog(strokeWidth: Float = 2f): ImageVector = cached("settingsLog", strokeWidth) {
        stroke("M7 3.5h7.5l4 4V20a.5.5 0 0 1-.5.5H7a1.5 1.5 0 0 1-1.5-1.5V5A1.5 1.5 0 0 1 7 3.5z")
        stroke("M9 11.5h6")
        stroke("M9 15h6")
    }

    /** 设定「关于」. */
    fun settingsAbout(strokeWidth: Float = 2f): ImageVector = cached("settingsAbout", strokeWidth) {
        stroke(circle(12f, 12f, 8.5f))
        stroke("M12 11v5")
        stroke("M12 8h.01")
    }

    /** 设定「除错」. */
    fun settingsDebug(strokeWidth: Float = 2f): ImageVector = cached("settingsDebug", strokeWidth) {
        stroke(rect(8f, 7f, 8f, 12f, 4f))
        stroke("M10 7.5V6a2 2 0 0 1 4 0v1.5")
        stroke("M4 13h4")
        stroke("M16 13h4")
        stroke("M5 8.5l3 2")
        stroke("M19 8.5l-3 2")
        stroke("M5 18l3-1.5")
        stroke("M19 18l-3-1.5")
    }

    /** 设定「设置备份」. */
    fun settingsBackup(strokeWidth: Float = 2f): ImageVector = cached("settingsBackup", strokeWidth) {
        stroke(rect(3.5f, 4.5f, 17f, 5f, 1.5f))
        stroke("M5 9.5v8.5a1.5 1.5 0 0 0 1.5 1.5h11a1.5 1.5 0 0 0 1.5-1.5V9.5")
        stroke("M10 13.5h4")
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

        /**
         * 填充, 与设计稿 SVG 中 `fill="currentColor"` 的路径相同. 线宽不为 0 时同时描边.
         */
        fun fill(pathData: String) {
            builder.addPath(
                pathData = addPathNodes(pathData),
                fill = SolidColor(Color.Black),
                stroke = if (strokeWidth > 0f) SolidColor(Color.Black) else null,
                strokeLineWidth = strokeWidth,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
            )
        }
    }

    private fun circle(cx: Float, cy: Float, r: Float): String =
        "M${cx - r} ${cy}a$r $r 0 1 0 ${2 * r} 0a$r $r 0 1 0 ${-2 * r} 0z"

    private fun ellipse(cx: Float, cy: Float, rx: Float, ry: Float): String =
        "M${cx - rx} ${cy}a$rx $ry 0 1 0 ${2 * rx} 0a$rx $ry 0 1 0 ${-2 * rx} 0z"

    /** 圆角矩形, 与 SVG 的 `<rect x y width height rx>` 相同. */
    private fun rect(x: Float, y: Float, width: Float, height: Float, r: Float): String =
        "M${x + r} ${y}h${width - 2 * r}a$r $r 0 0 1 $r ${r}v${height - 2 * r}" +
            "a$r $r 0 0 1 ${-r} ${r}h${-(width - 2 * r)}a$r $r 0 0 1 ${-r} ${-r}v${-(height - 2 * r)}" +
            "a$r $r 0 0 1 $r ${-r}z"
}
