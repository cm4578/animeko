/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.flow.collectLatest
import me.him188.ani.app.navigation.SubjectDetailPlaceholder
import me.him188.ani.app.ui.exploration.search.AppleSearchPage
import me.him188.ani.app.ui.exploration.search.SearchPageEffect
import me.him188.ani.app.ui.exploration.search.SearchPageIntent
import me.him188.ani.app.ui.main.SearchViewModel

/**
 * Apple 版搜索页 (03 §3.9) 的状态接入: 结果点击进入条目详情, 「取消」离开搜索页.
 *
 * 与 [SearchScreen] 同一个 [SearchViewModel]; 不使用列表与详情并排的版式, 宽屏也推入条目详情页.
 */
@Composable
fun AppleSearchScreen(
    vm: SearchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSubjectDetails: (subjectId: Int, placeholder: SubjectDetailPlaceholder?) -> Unit,
    onNavigateToEpisodeDetails: (subjectId: Int, episodeId: Int) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val searchPageState by vm.searchPageState.collectAsStateWithLifecycle()
    val selfInfo by vm.selfInfoFlow.collectAsStateWithLifecycle(null)

    LaunchedEffect(vm) {
        vm.searchPageEffects.collectLatest { effect ->
            when (effect) {
                is SearchPageEffect.NavigateToSubjectDetails -> {
                    onNavigateToSubjectDetails(
                        effect.subjectId,
                        SubjectDetailPlaceholder(
                            id = effect.subjectId,
                            name = effect.originalTitle,
                            nameCN = effect.title,
                            coverUrl = effect.imageUrl,
                        ),
                    )
                }

                is SearchPageEffect.NavigateToEpisodeDetails -> {
                    onNavigateToEpisodeDetails(effect.subjectId, effect.episodeId)
                }
            }
        }
    }

    AppleSearchPage(
        state = searchPageState,
        onIntent = vm::onSearchPageIntent,
        suggestionsPager = vm::suggestionsPager,
        quickFollowState = vm.quickFollowState.takeIf { selfInfo?.isSessionValid == true },
        onCancel = onNavigateBack,
        modifier = modifier.fillMaxSize(),
        contentPadding = contentPadding,
    )
    LaunchedEffect(vm) {
        vm.onSearchPageIntent(SearchPageIntent.StartInitialSearch)
    }
}
