/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.him188.ani.app.ui.foundation.apple.PushedPageHeader
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.settings_account_bangumi_sync_title
import org.jetbrains.compose.resources.stringResource

/**
 * 「我的」→ Bangumi 同步 (iOS 推入页): C3 推入页顶部 + [BangumiSyncTab].
 */
@Composable
fun AppleBangumiSyncScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        PushedPageHeader(stringResource(Lang.settings_account_bangumi_sync_title), onBack = onNavigateBack)
        Spacer(Modifier.height(12.dp))
        BangumiSyncTab(modifier = Modifier.weight(1f).padding(horizontal = 16.dp))
    }
}
