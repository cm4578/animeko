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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.util.fastLastOrNull
import androidx.lifecycle.LifecycleOwner
import androidx.navigation3.scene.SceneInfo
import androidx.navigationevent.DirectNavigationEventInput
import androidx.navigationevent.NavigationEventDispatcher
import androidx.navigationevent.NavigationEventInfo

actual object LocalOnBackPressedDispatcherOwner {
    private val LocalOnBackPressedDispatcherOwner =
        staticCompositionLocalOf<OnBackPressedDispatcherOwner?> { null }

    /**
     * Returns current composition local value for the owner or `null` if one has not
     * been provided, one has not been set via
     * [androidx.activity.setViewTreeOnBackPressedDispatcherOwner], nor is one available by
     * looking at the [LocalContext].
     */
    actual val current: OnBackPressedDispatcherOwner?
        @Composable
        get() = LocalOnBackPressedDispatcherOwner.current

    /**
     * Associates a [LocalOnBackPressedDispatcherOwner] key to a value in a call to
     * [CompositionLocalProvider].
     */
    actual infix fun provides(dispatcherOwner: OnBackPressedDispatcherOwner):
            ProvidedValue<OnBackPressedDispatcherOwner?> {
        return LocalOnBackPressedDispatcherOwner.provides(dispatcherOwner)
    }
}

actual interface OnBackPressedDispatcherOwner : LifecycleOwner {
    actual val onBackPressedDispatcher: OnBackPressedDispatcher
}

/**
 * The app's back stack on skiko platforms: the last enabled [BackHandler] takes a back press, otherwise
 * [fallback] runs.
 *
 * @param navigationEventDispatcher the desktop window's [NavigationEventDispatcher]. Compose registers the
 * dismissal of focusable popups and dialogs there, and Navigation 3 registers its own back handling there
 * too. When it is set, every [BackHandler] also registers a marker there, so that dispatcher orders the
 * app's handlers against popups and dialogs by registration. See [onBackPressed].
 */
actual class OnBackPressedDispatcher(
    private val fallback: () -> Unit,
    internal val navigationEventDispatcher: NavigationEventDispatcher? = null,
) {
    private val handlers = mutableListOf<OnBackPressedHandler>()
    private val navigationEventInput = DirectNavigationEventInput()
    private var navigationEventInputAdded = false

    /**
     * Gives a back press to whatever is on top: a popup or dialog in [navigationEventDispatcher], which
     * usually dismisses it, otherwise the last enabled [BackHandler], otherwise [fallback].
     *
     * Navigation 3 back handlers in [navigationEventDispatcher] never take the back press: [fallback]
     * already leaves the page, and [BackHandler]s registered before a `NavDisplay` still come first. A
     * popup or dialog that sits below an enabled `NavDisplay` back handler is therefore not reached.
     */
    actual fun onBackPressed() {
        if (dispatchToPopupOnTop()) return
        handlers.fastLastOrNull { it.enabled }?.onBack() ?: fallback()
    }

    private fun dispatchToPopupOnTop(): Boolean {
        val dispatcher = navigationEventDispatcher ?: return false
        val history = dispatcher.history.value
        when (history.mergedHistory.getOrNull(history.currentIndex)) {
            null, BackHandlerNavigationEventInfo, is SceneInfo<*> -> return false
            else -> {}
        }
        if (!navigationEventInputAdded) {
            dispatcher.addInput(navigationEventInput)
            navigationEventInputAdded = true
        }
        navigationEventInput.backCompleted()
        return true
    }

    fun registerHandler(handler: OnBackPressedHandler) {
        this.handlers.add(handler)
    }

    fun unregisterHandler(handler: OnBackPressedHandler) {
        this.handlers.remove(handler)
    }
}

interface OnBackPressedHandler {
    val enabled: Boolean
    fun onBack()
}

/**
 * Identifies the markers that [BackHandler] registers in [OnBackPressedDispatcher.navigationEventDispatcher].
 */
internal object BackHandlerNavigationEventInfo : NavigationEventInfo()
