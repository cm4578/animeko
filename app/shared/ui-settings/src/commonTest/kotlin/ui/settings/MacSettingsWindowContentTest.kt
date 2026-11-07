/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.him188.ani.app.data.models.subject.SubjectCollectionCounts
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.apple.MacSettingsPane
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.notification_unread_count
import me.him188.ani.app.ui.lang.profile_pending_sync_count
import me.him188.ani.app.ui.lang.settings_account_popup_logout_confirm
import me.him188.ani.app.ui.lang.settings_mac_bangumi_connected
import me.him188.ani.app.ui.user.SelfInfoUiState
import me.him188.ani.app.ui.user.TestSelfInfoUiState
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.annotations.TestOnly
import org.jetbrains.compose.resources.stringResource

/**
 * docs/design/ani-style/04-macos.md §3.6 设定窗口的分类栏与帐号窗格; 05 B4.
 */
@OptIn(ExperimentalTestApi::class, TestOnly::class)
class MacSettingsWindowContentTest {
    private val guest = SelfInfoUiState(selfInfo = null, isLoading = false, isSessionValid = false, bangumiConnected = null)

    private fun account(
        selfInfo: SelfInfoUiState = TestSelfInfoUiState,
        pendingSyncCount: Int = 0,
        notificationUnreadCount: Int = 0,
        showNotificationDot: Boolean = false,
    ) = MacSettingsAccountInfo(
        selfInfo = selfInfo,
        collectionCounts = SubjectCollectionCounts(wish = 4, doing = 7, done = 12, onHold = 0, dropped = 0, total = 23),
        pendingSyncCount = pendingSyncCount,
        notificationUnreadCount = notificationUnreadCount,
        showNotificationDot = showNotificationDot,
        hasNewVersion = false,
    )

    @Composable
    private fun Mac(content: @Composable () -> Unit) {
        ProvideCompositionLocalsForPreview {
            CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64), content = content)
        }
    }

    private class AccountClicks {
        val clicks = mutableListOf<String>()
        var logouts = 0
    }

    @Composable
    private fun AccountPane(account: MacSettingsAccountInfo, clicks: AccountClicks) {
        Mac {
            MacSettingsPane("Account", Modifier.size(700.dp, 900.dp)) {
                MacAccountPaneContent(
                    account,
                    onEditProfile = { clicks.clicks += "edit" },
                    onBangumiSync = { clicks.clicks += "sync" },
                    onOpenNotifications = { clicks.clicks += "notifications" },
                    onLogin = { clicks.clicks += "login" },
                    onLogout = { clicks.logouts++ },
                )
            }
        }
    }

    @Test
    fun `notifications follows theme and debug only shows in debug mode`() = runAniComposeUiTest {
        var showDebugSettings by mutableStateOf(false)
        setContent {
            Mac {
                MacSettingsCategories(
                    account(),
                    showDebugSettings = showDebugSettings,
                    selected = MacSettingsCategory.Tab(SettingsTab.PROFILE),
                    onSelect = {},
                    Modifier.size(232.dp, 900.dp),
                )
            }
        }
        fun top(category: MacSettingsCategory) =
            onNodeWithTag(MacSettingsTestTags.category(category)).getUnclippedBoundsInRoot().top

        val theme = top(MacSettingsCategory.Tab(SettingsTab.THEME))
        val notifications = top(MacSettingsCategory.Notifications)
        val player = top(MacSettingsCategory.Tab(SettingsTab.PLAYER))
        assertTrue(theme < notifications && notifications < player, "theme=$theme notifications=$notifications player=$player")

        onNodeWithTag(MacSettingsTestTags.category(MacSettingsCategory.Tab(SettingsTab.DEBUG))).assertDoesNotExist()
        showDebugSettings = true
        waitForIdle()
        onNodeWithTag(MacSettingsTestTags.category(MacSettingsCategory.Tab(SettingsTab.DEBUG))).assertExists()
    }

    @Test
    fun `clicking a category selects it`() = runAniComposeUiTest {
        var selected by mutableStateOf<MacSettingsCategory>(MacSettingsCategory.Tab(SettingsTab.PROFILE))
        setContent {
            Mac {
                MacSettingsCategories(account(), showDebugSettings = false, selected, onSelect = { selected = it })
            }
        }
        onNodeWithTag(MacSettingsTestTags.category(MacSettingsCategory.Tab(SettingsTab.PROFILE))).assertIsSelected()

        onNodeWithTag(MacSettingsTestTags.category(MacSettingsCategory.Notifications)).performClick()
        waitForIdle()
        assertEquals(MacSettingsCategory.Notifications, selected)
        onNodeWithTag(MacSettingsTestTags.category(MacSettingsCategory.Notifications)).assertIsSelected()
    }

    @Test
    fun `account pane shows the profile card and sync rows`() = runAniComposeUiTest {
        val clicks = AccountClicks()
        var pendingText = ""
        var unreadText = ""
        var connectedText = ""
        setContent {
            pendingText = stringResource(Lang.profile_pending_sync_count, 3)
            unreadText = stringResource(Lang.notification_unread_count, 2)
            connectedText = stringResource(Lang.settings_mac_bangumi_connected, "TestBangumiUser")
            AccountPane(account(pendingSyncCount = 3, notificationUnreadCount = 2, showNotificationDot = true), clicks)
        }
        onNodeWithText("TestUser").assertExists()
        onNodeWithText("@TestBangumiUser · test@animeko.org").assertExists()
        onNodeWithText("12").assertExists()
        onNodeWithText(pendingText).assertExists()
        onNodeWithText(unreadText).assertExists()
        onNodeWithText(connectedText).assertExists()

        onNodeWithTag(MacSettingsTestTags.EditProfile).performClick()
        onNodeWithTag(MacSettingsTestTags.BangumiSync).performClick()
        onNodeWithTag(MacSettingsTestTags.Notifications).performClick()
        waitForIdle()
        assertEquals(listOf("edit", "sync", "notifications"), clicks.clicks)
    }

    @Test
    fun `logout asks for confirmation first`() = runAniComposeUiTest {
        val clicks = AccountClicks()
        var confirmText = ""
        setContent {
            confirmText = stringResource(Lang.settings_account_popup_logout_confirm)
            AccountPane(account(), clicks)
        }
        onNodeWithTag(MacSettingsTestTags.Logout).performClick()
        waitForIdle()
        onNodeWithText(confirmText).assertExists()
        assertEquals(0, clicks.logouts)
    }

    @Test
    fun `guests see a login card instead of the account rows`() = runAniComposeUiTest {
        val clicks = AccountClicks()
        setContent {
            AccountPane(account(selfInfo = guest), clicks)
        }
        onNodeWithTag(MacSettingsTestTags.Logout).assertDoesNotExist()
        onNodeWithTag(MacSettingsTestTags.BangumiSync).assertDoesNotExist()
        onNodeWithTag(MacSettingsTestTags.LoginCard).performClick()
        waitForIdle()
        assertEquals(listOf("login"), clicks.clicks)
    }
}
