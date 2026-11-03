/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.apple

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.subject_collection_cancel
import me.him188.ani.app.ui.lang.subject_collection_delete_action
import me.him188.ani.app.ui.lang.subject_collection_delete_confirm_message
import me.him188.ani.app.ui.lang.subject_collection_delete_confirm_title
import me.him188.ani.app.ui.lang.subject_collection_doing
import me.him188.ani.app.ui.lang.subject_collection_done
import me.him188.ani.app.ui.lang.subject_collection_dropped
import me.him188.ani.app.ui.lang.subject_collection_not_collected
import me.him188.ani.app.ui.lang.subject_collection_on_hold
import me.him188.ani.app.ui.lang.subject_collection_wish
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版收藏状态的顺序: 设计稿的在看、想看、看过在前, 搁置与抛弃在后. 追番页的状态胶囊与收藏状态菜单共用.
 */
val AppleCollectionTypes: List<UnifiedCollectionType> = listOf(
    UnifiedCollectionType.DOING,
    UnifiedCollectionType.WISH,
    UnifiedCollectionType.DONE,
    UnifiedCollectionType.ON_HOLD,
    UnifiedCollectionType.DROPPED,
)

/**
 * 收藏状态的名称, 例如「在看」.
 */
fun UnifiedCollectionType.collectionTypeName(): StringResource = when (this) {
    UnifiedCollectionType.WISH -> Lang.subject_collection_wish
    UnifiedCollectionType.DOING -> Lang.subject_collection_doing
    UnifiedCollectionType.DONE -> Lang.subject_collection_done
    UnifiedCollectionType.ON_HOLD -> Lang.subject_collection_on_hold
    UnifiedCollectionType.DROPPED -> Lang.subject_collection_dropped
    UnifiedCollectionType.NOT_COLLECTED -> Lang.subject_collection_not_collected
}

/**
 * C15 菜单中的收藏状态单选行, 放在 [ApplePopoverMenu] 内: 依 [AppleCollectionTypes] 顺序, 当前状态前有勾号.
 *
 * 选任一行都会关闭菜单; 选当前状态不调用 [onSetType].
 *
 * @param currentType 当前状态. 为 [UnifiedCollectionType.NOT_COLLECTED] 时没有勾号.
 * @param itemModifier 每一行的 Modifier, 例如测试标签.
 */
@Composable
fun CollectionTypeMenuItems(
    currentType: UnifiedCollectionType,
    onDismissRequest: () -> Unit,
    onSetType: (UnifiedCollectionType) -> Unit,
    itemModifier: (UnifiedCollectionType) -> Modifier = { Modifier },
) {
    AppleCollectionTypes.forEachIndexed { index, type ->
        PopoverMenuItem(
            stringResource(type.collectionTypeName()),
            onClick = {
                onDismissRequest()
                if (type != currentType) onSetType(type)
            },
            itemModifier(type),
            showDivider = index > 0,
            checked = type == currentType,
        )
    }
}

/**
 * 「取消追番」确认对话框, 文案同条目详情页的收藏菜单. 快速追番与 Apple 版条目详情共用.
 *
 * @param confirmButtonModifier 确认按钮的 Modifier, 例如测试标签.
 */
@Composable
fun UnfollowConfirmDialog(
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
    confirmButtonModifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Lang.subject_collection_delete_confirm_title)) },
        text = { Text(stringResource(Lang.subject_collection_delete_confirm_message)) },
        icon = { Icon(AppleIcons.minusCircle(), contentDescription = null) },
        confirmButton = {
            TextButton(onConfirm, confirmButtonModifier) {
                Text(stringResource(Lang.subject_collection_delete_action), color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onDismissRequest) {
                Text(stringResource(Lang.subject_collection_cancel))
            }
        },
    )
}
