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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.theme.AniTextStyles

/**
 * C2 主要分页的大标题页头 (手机): 顶部安全区下 14 起, 左侧 `largeTitle`, 右侧动作按钮; 标题下 18 开始内容.
 *
 * 标题距屏幕左缘 20 (页面内距 16 + 4). Apple 平台的页头不放头像按钮 (05 G14).
 *
 * @param actions 动作按钮, 用 [AppleCircleButton]. 按钮外围带透明点击区, 圆钮之间的视觉间距为 12.
 */
@Composable
fun LargeTitleHeader(
    title: String,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
            // 行高取按钮点击区 44, 标题 (行高 41) 居中, 所以上下各扣 1.5; 按钮点击区比圆钮宽 4, 右侧少留 4.
            .padding(start = 20.dp, end = 16.dp, top = 12.5.dp, bottom = 16.5.dp)
            .heightIn(min = AppleComponentDefaults.MinTouchSize),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            title,
            Modifier
                .weight(1f)
                .semantics { heading() },
            style = AniTextStyles.largeTitle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            actions()
        }
    }
}

/**
 * C3 一般推入页 (设定子页、通知、排行等) 的顶部: 左上返回圆钮 36 (fill 底), 下方 28/34 W800 大标题.
 *
 * 页面滚动后改用 [CollapsedTopBar].
 */
@Composable
fun PushedPageHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backContentDescription: String? = null,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
            .padding(top = 10.dp),
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppleCircleButton(onBack, AppleIcons.back(), backContentDescription)
            Spacer(Modifier.weight(1f))
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                actions()
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            title,
            Modifier
                .padding(horizontal = 20.dp)
                .semantics { heading() },
            style = pushedTitleStyle(),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * C3 滚动后的精简栏: 高 44, 返回钮 + 居中 `headline` 标题 + 底部 1dp separator (不透明度 0.6). 底色 tabBg.
 */
@Composable
fun CollapsedTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backContentDescription: String? = null,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(
        modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .windowInsetsPadding(windowInsets),
    ) {
        CenteredBar(
            title = {
                Text(
                    title,
                    style = AniTextStyles.headline,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            },
            navigation = { AppleCircleButton(onBack, AppleIcons.back(), backContentDescription) },
            actions = actions,
        )
        AppleDivider()
    }
}

/**
 * C3 叠在图片上的顶部按钮列 (条目详情、人物): 返回与其他按钮为 [AppleCircleButtonStyle.Media] 圆钮.
 */
@Composable
fun OverlayTopBar(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    backContentDescription: String? = null,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    actions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(windowInsets)
            .padding(start = 12.dp, end = 12.dp, top = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppleCircleButton(onBack, AppleIcons.back(), backContentDescription, style = AppleCircleButtonStyle.Media)
        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            actions()
        }
    }
}

/**
 * C3 全屏任务 (资料源编辑器) 的顶部栏: 左返回钮, 中间 `headline` 标题, 右侧 label2 小字 (例如「自动保存」).
 */
@Composable
fun FullScreenTaskTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    trailingText: String? = null,
    backContentDescription: String? = null,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
) {
    Box(modifier.fillMaxWidth().windowInsetsPadding(windowInsets)) {
        CenteredBar(
            title = {
                Text(
                    title,
                    style = AniTextStyles.headline,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            },
            navigation = { AppleCircleButton(onBack, AppleIcons.back(), backContentDescription) },
            actions = {
                if (trailingText != null) {
                    Text(
                        trailingText,
                        Modifier.padding(end = 4.dp),
                        style = AniTextStyles.caption,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            },
        )
    }
}

/**
 * 高 44 的栏: 左右按钮贴边, 标题在整栏居中.
 */
@Composable
private fun CenteredBar(
    title: @Composable () -> Unit,
    navigation: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .height(CollapsedTopBarDefaults.Height)
            .padding(horizontal = 12.dp),
    ) {
        Box(Modifier.align(Alignment.CenterStart)) { navigation() }
        Box(
            Modifier
                .align(Alignment.Center)
                .padding(horizontal = 56.dp),
        ) {
            title()
        }
        Row(
            Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions()
        }
    }
}

object CollapsedTopBarDefaults {
    val Height = 44.dp
}

@Composable
private fun pushedTitleStyle(): TextStyle =
    AniTextStyles.largeTitle.copy(fontSize = 28.sp, lineHeight = 34.sp, letterSpacing = (-0.4).sp)
