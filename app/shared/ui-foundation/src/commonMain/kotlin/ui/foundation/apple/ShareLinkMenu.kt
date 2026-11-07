/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalUriHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.him188.ani.app.ui.foundation.setClipEntryText
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.subject_details_share_copy_link
import me.him188.ani.app.ui.lang.subject_details_share_open_in_browser
import org.jetbrains.compose.resources.stringResource

/**
 * Bangumi 条目页的链接.
 */
fun bangumiSubjectUrl(subjectId: Int): String = "https://bgm.tv/subject/$subjectId"

/**
 * 分享链接的两项菜单 (05 G6): 「复制链接」「在浏览器中打开」. 放在 [ApplePopoverMenu] 或 [AppleContextMenu] 里.
 *
 * 选中后先调用 [onDismissRequest] 关闭菜单. 复制在 [scope] 中进行, [scope] 要比菜单活得久, 例如菜单所在页面的作用域.
 *
 * @param showDivider 在「复制链接」上方画分隔线, 用于接在其他菜单项之后.
 */
@Composable
fun ShareLinkMenuItems(
    url: String,
    scope: CoroutineScope,
    onDismissRequest: () -> Unit,
    showDivider: Boolean = false,
    copyModifier: Modifier = Modifier,
    openModifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboard.current
    val uriHandler = LocalUriHandler.current
    PopoverMenuItem(
        stringResource(Lang.subject_details_share_copy_link),
        onClick = {
            onDismissRequest()
            scope.launch { clipboard.setClipEntryText(url) }
        },
        copyModifier,
        icon = AppleIcons.copy(),
        showDivider = showDivider,
    )
    PopoverMenuItem(
        stringResource(Lang.subject_details_share_open_in_browser),
        onClick = {
            onDismissRequest()
            uriHandler.openUri(url)
        },
        openModifier,
        icon = AppleIcons.externalLink(),
        showDivider = true,
    )
}
