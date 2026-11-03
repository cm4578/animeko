/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.exploration.follow

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.him188.ani.app.data.repository.subject.SetSubjectCollectionTypeOrDeleteUseCase
import me.him188.ani.app.data.repository.subject.SubjectCollectionRepository
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.ApplePopoverMenu
import me.him188.ani.app.ui.foundation.apple.CollectionTypeMenuItems
import me.him188.ani.app.ui.foundation.apple.PopoverMenuItem
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.showLoadError
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.subject_collection_cancel
import me.him188.ani.app.ui.lang.subject_collection_delete
import me.him188.ani.app.ui.lang.subject_collection_delete_action
import me.him188.ani.app.ui.lang.subject_collection_delete_confirm_message
import me.him188.ani.app.ui.lang.subject_collection_delete_confirm_title
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import org.jetbrains.compose.resources.stringResource

/**
 * 列表上的快速追番 (05 G2): 探索推荐、新番时间表、搜索结果与热度排行共用.
 *
 * @param collectionTypeFlow 条目的收藏状态. 本地没有记录时为 [UnifiedCollectionType.NOT_COLLECTED].
 * @param onFollow 追番: 条目没有收藏时设为想看, 已有收藏时不修改.
 * @param onSetCollectionType 修改收藏状态; [UnifiedCollectionType.NOT_COLLECTED] 为取消追番.
 */
@Stable
class QuickFollowState(
    val collectionTypeFlow: (subjectId: Int) -> Flow<UnifiedCollectionType>,
    private val onFollow: suspend (subjectId: Int) -> Unit,
    private val onSetCollectionType: suspend (subjectId: Int, type: UnifiedCollectionType) -> Unit,
) {
    suspend fun follow(subjectId: Int): LoadError? =
        LoadError.runAndWrapOrThrowCancellation { onFollow(subjectId) }

    suspend fun setCollectionType(subjectId: Int, type: UnifiedCollectionType): LoadError? =
        LoadError.runAndWrapOrThrowCancellation { onSetCollectionType(subjectId, type) }
}

/**
 * 用条目详情页的收藏动作创建 [QuickFollowState]. 收藏状态只读本地缓存, 不发网络请求.
 *
 * 追番前先取得条目 ([SubjectCollectionRepository.subjectCollectionFlow]): 本地没有缓存时从服务端拉取并写入缓存.
 * 这样在其他设备已收藏、但本地还没有缓存的条目不会被改为想看; 设定后的状态也会写回本地缓存, 按钮随之更新.
 */
fun QuickFollowState(
    subjectCollectionRepository: SubjectCollectionRepository,
    setSubjectCollectionTypeOrDelete: SetSubjectCollectionTypeOrDeleteUseCase,
): QuickFollowState = QuickFollowState(
    collectionTypeFlow = { subjectId ->
        subjectCollectionRepository.getSubjectCollectionTypeOffline(subjectId)
            .map { it ?: UnifiedCollectionType.NOT_COLLECTED }
    },
    onFollow = { subjectId ->
        val current = subjectCollectionRepository.subjectCollectionFlow(subjectId).first().collectionType
        if (current == UnifiedCollectionType.NOT_COLLECTED) {
            setSubjectCollectionTypeOrDelete(subjectId, UnifiedCollectionType.WISH)
        }
    },
    onSetCollectionType = { subjectId, type -> setSubjectCollectionTypeOrDelete(subjectId, type) },
)

/**
 * 快速追番按钮的行为: 未追番时点击设为想看; 已追番时点击打开 C15 收藏状态菜单, 末行「取消追番」确认后执行.
 *
 * 菜单以 [content] 为锚点. 读到收藏状态之前与请求进行中, 按钮停用. 请求失败时显示提示.
 *
 * @param content 按钮外观. `followed` 为是否已追番.
 */
@Composable
fun QuickFollowAnchor(
    state: QuickFollowState,
    subjectId: Int,
    modifier: Modifier = Modifier,
    content: @Composable (followed: Boolean, enabled: Boolean, onClick: () -> Unit) -> Unit,
) {
    val type by remember(state, subjectId) { state.collectionTypeFlow(subjectId) }
        .collectAsStateWithLifecycle(null)
    var working by remember(subjectId) { mutableStateOf(false) }
    var menuExpanded by remember(subjectId) { mutableStateOf(false) }
    var confirmUnfollow by remember(subjectId) { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val toaster = LocalToaster.current

    fun launchAction(action: suspend () -> LoadError?) {
        working = true
        scope.launch {
            try {
                action()?.let { toaster.showLoadError(it) }
            } finally {
                working = false
            }
        }
    }

    val currentType = type
    Box(modifier) {
        content(
            currentType != null && currentType != UnifiedCollectionType.NOT_COLLECTED,
            currentType != null && !working,
        ) {
            if (currentType == UnifiedCollectionType.NOT_COLLECTED) {
                launchAction { state.follow(subjectId) }
            } else {
                menuExpanded = true
            }
        }
        if (currentType != null) {
            ApplePopoverMenu(
                menuExpanded,
                onDismissRequest = { menuExpanded = false },
                Modifier.testTag(QuickFollowTestTags.Menu),
            ) {
                CollectionTypeMenuItems(
                    currentType,
                    onDismissRequest = { menuExpanded = false },
                    onSetType = { launchAction { state.setCollectionType(subjectId, it) } },
                    itemModifier = { Modifier.testTag(QuickFollowTestTags.type(it)) },
                )
                PopoverMenuItem(
                    stringResource(Lang.subject_collection_delete),
                    onClick = {
                        menuExpanded = false
                        confirmUnfollow = true
                    },
                    Modifier.testTag(QuickFollowTestTags.Unfollow),
                    icon = AppleIcons.minusCircle(),
                    showDivider = true,
                )
            }
        }
    }

    if (confirmUnfollow) {
        UnfollowConfirmDialog(
            onConfirm = {
                confirmUnfollow = false
                launchAction { state.setCollectionType(subjectId, UnifiedCollectionType.NOT_COLLECTED) }
            },
            onDismissRequest = { confirmUnfollow = false },
        )
    }
}

/**
 * 「取消追番」确认对话框, 文案同条目详情页的收藏菜单.
 */
@Composable
private fun UnfollowConfirmDialog(
    onConfirm: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Lang.subject_collection_delete_confirm_title)) },
        text = { Text(stringResource(Lang.subject_collection_delete_confirm_message)) },
        icon = { Icon(AppleIcons.minusCircle(), contentDescription = null) },
        confirmButton = {
            TextButton(onConfirm, Modifier.testTag(QuickFollowTestTags.ConfirmUnfollow)) {
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

internal object QuickFollowTestTags {
    const val Menu = "quick-follow-menu"
    const val Unfollow = "quick-follow-menu-unfollow"
    const val ConfirmUnfollow = "quick-follow-confirm-unfollow"
    fun type(type: UnifiedCollectionType) = "quick-follow-menu-${type.name}"
}
