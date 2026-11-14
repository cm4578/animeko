/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.settings.notification

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import me.him188.ani.app.data.models.preference.NotificationSettings
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.foundation.ProvideCompositionLocalsForPreview
import me.him188.ani.app.ui.foundation.apple.MacSettingsPane
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.notification_center_title
import me.him188.ani.app.ui.lang.notification_settings_announcement_description
import me.him188.ani.app.ui.lang.notification_settings_announcement_description_apple
import me.him188.ani.app.ui.lang.notification_settings_system_group_description
import me.him188.ani.app.ui.lang.notification_unread_count
import me.him188.ani.app.ui.settings.framework.SettingsState
import me.him188.ani.app.ui.settings.framework.rememberTestSettingsState
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.annotations.TestOnly
import org.jetbrains.compose.resources.stringResource

/**
 * docs/design/ani-style/03-mobile-pages.md §4.10 iOS 通知设定; 04-macos.md §4.7 macOS 设定窗口的「通知」窗格.
 */
@OptIn(TestOnly::class)
class AppleNotificationSettingsTest {
    private class Page {
        lateinit var state: SettingsState<NotificationSettings>
        var notifications = 0
        val texts = mutableMapOf<String, String>()
    }

    private fun ComposeUiTest.showIos(
        initial: NotificationSettings = NotificationSettings.Default,
        texts: @Composable () -> Map<String, String> = { emptyMap() },
    ): Page {
        val page = Page()
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    page.state = rememberTestSettingsState(initial)
                    page.texts.putAll(texts())
                    AppleNotificationSettingsScreen(page.state, onBack = {}, Modifier.size(390.dp, 760.dp))
                }
            }
        }
        waitForIdle()
        return page
    }

    private fun ComposeUiTest.showMac(
        unreadCount: Int,
        showNotificationDot: Boolean,
        texts: @Composable () -> Map<String, String> = { emptyMap() },
    ): Page {
        val page = Page()
        setContent {
            ProvideCompositionLocalsForPreview {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    page.state = rememberTestSettingsState(NotificationSettings.Default)
                    page.texts.putAll(texts())
                    MacSettingsPane("Notifications", Modifier.size(700.dp, 680.dp)) {
                        MacNotificationSettingsPaneContent(
                            page.state,
                            unreadCount = unreadCount,
                            showNotificationDot = showNotificationDot,
                            onOpenNotifications = { page.notifications++ },
                        )
                    }
                }
            }
        }
        waitForIdle()
        return page
    }

    @Test
    fun `each switch updates its own setting`() = runAniComposeUiTest {
        val page = showIos()
        onNodeWithTag(NotificationSettingsTestTags.REPORT_RESULT).assertIsOn().performClick()
        waitUntil { !page.state.value.showReportResults }
        onNodeWithTag(NotificationSettingsTestTags.REPORT_RESULT).assertIsOff()
        onNodeWithTag(NotificationSettingsTestTags.ACCOUNT_SECURITY).assertIsOn()

        onNodeWithTag(NotificationSettingsTestTags.ANNOUNCEMENT).assertIsOn().performClick()
        waitUntil { !page.state.value.showAnnouncementDot }
        assertEquals(
            NotificationSettings(showReportResults = false, showAnnouncementDot = false),
            page.state.value,
        )
    }

    @Test
    fun `switches show the saved settings`() = runAniComposeUiTest {
        showIos(NotificationSettings(showAccountSecurity = false, showAnnouncementDot = false))
        onNodeWithTag(NotificationSettingsTestTags.REPORT_RESULT).assertIsOn()
        onNodeWithTag(NotificationSettingsTestTags.ACCOUNT_SECURITY).assertIsOff()
        onNodeWithTag(NotificationSettingsTestTags.ANNOUNCEMENT).assertIsOff()
    }

    @Test
    fun `group descriptions use the apple announcement text`() = runAniComposeUiTest {
        val page = showIos {
            mapOf(
                "system" to stringResource(Lang.notification_settings_system_group_description),
                "apple" to stringResource(Lang.notification_settings_announcement_description_apple),
                "other" to stringResource(Lang.notification_settings_announcement_description),
            )
        }
        onNodeWithText(page.texts.getValue("system")).assertExists()
        onNodeWithText(page.texts.getValue("apple")).assertExists()
        onNodeWithText(page.texts.getValue("other")).assertDoesNotExist()
    }

    @Test
    fun `mac notification center row shows the unread count and opens the center`() = runAniComposeUiTest {
        val page = showMac(unreadCount = 4, showNotificationDot = true) {
            mapOf(
                "title" to stringResource(Lang.notification_center_title),
                "unread" to stringResource(Lang.notification_unread_count, 4),
            )
        }
        onNodeWithTag(NotificationSettingsTestTags.NOTIFICATION_CENTER)
            .assertTextContains(page.texts.getValue("title"))
            .assertTextContains(page.texts.getValue("unread"))
            .performClick()
        assertEquals(1, page.notifications)
        // 窗格也有两组开关
        onNodeWithTag(NotificationSettingsTestTags.REPORT_RESULT).assertIsOn()
        onNodeWithTag(NotificationSettingsTestTags.ANNOUNCEMENT).assertIsOn()
    }

    @Test
    fun `mac notification center row hides the count without the dot`() = runAniComposeUiTest {
        val page = showMac(unreadCount = 4, showNotificationDot = false) {
            mapOf("unread" to stringResource(Lang.notification_unread_count, 4))
        }
        onNodeWithTag(NotificationSettingsTestTags.NOTIFICATION_CENTER).assertExists()
        onNodeWithText(page.texts.getValue("unread"), substring = true).assertDoesNotExist()
    }
}
