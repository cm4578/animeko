/*
 * Copyright (C) 2024-2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.foundation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.navigationevent.compose.LocalNavigationEventDispatcherOwner
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import me.him188.ani.utils.platform.annotations.TestOnly

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    val onBackUpdated by rememberUpdatedState(onBack)
    val enabledUpdated = rememberUpdatedState(enabled)
    val owner = LocalOnBackPressedDispatcherOwner.current ?: return
    DisposableEffect(true, owner) {
        val handler = object : OnBackPressedHandler {
            override val enabled: Boolean by enabledUpdated
            override fun onBack() {
                onBackUpdated()
            }
        }
        owner.onBackPressedDispatcher.registerHandler(handler)
        onDispose {
            owner.onBackPressedDispatcher.unregisterHandler(handler)
        }
    }

    val navigationEventDispatcher = owner.onBackPressedDispatcher.navigationEventDispatcher
    if (navigationEventDispatcher != null &&
        LocalNavigationEventDispatcherOwner.current?.navigationEventDispatcher === navigationEventDispatcher
    ) {
        // Marks this handler in the window's dispatcher right after registering it above, so that dispatcher
        // knows whether it came before or after each popup and dialog. See OnBackPressedDispatcher.onBackPressed.
        NavigationBackHandler(
            state = rememberNavigationEventState(BackHandlerNavigationEventInfo),
            isBackEnabled = enabled,
            onBackCompleted = { onBackUpdated() },
        )
    }
}

@TestOnly
actual fun OnBackPressedDispatcher(fallbackOnBackPressed: (() -> Unit)?): OnBackPressedDispatcher {
    return OnBackPressedDispatcher(fallback = fallbackOnBackPressed ?: {}) // resolves to constructor
}
