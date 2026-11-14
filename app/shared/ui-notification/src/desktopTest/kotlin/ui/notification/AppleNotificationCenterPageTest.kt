/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

@file:OptIn(TestOnly::class)

package me.him188.ani.app.ui.notification

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.data.models.notification.NotificationGroup
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.login_sign_in
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.annotations.TestOnly
import org.jetbrains.compose.resources.getString

/**
 * docs/design/ani-style/03-mobile-pages.md §4.7 iOS 通知中心, §4.8 公告.
 */
@OptIn(ExperimentalTestApi::class)
class AppleNotificationCenterPageTest {
    private class Opened {
        val details = mutableListOf<String>()
        val announcementDetails = mutableListOf<String>()
        val commentThreads = mutableListOf<String>()
        var announcements = 0
        var settings = 0
        var login = 0
        var accountSettings = 0
        var reauthorize = 0

        val actions = NotificationActions.Noop.let { noop ->
            NotificationActions(
                onOpenEpisode = noop.onOpenEpisode,
                onOpenSubject = noop.onOpenSubject,
                onOpenPerson = noop.onOpenPerson,
                onOpenCharacter = noop.onOpenCharacter,
                onOpenCommentThread = { _, commentId, replyId -> commentThreads += "$commentId:$replyId" },
                onReauthorizeBangumi = { reauthorize++ },
                onOpenAccountSettings = { accountSettings++ },
                onCheckUpdate = noop.onCheckUpdate,
            )
        }
    }

    private fun ComposeUiTest.showCenter(vm: NotificationCenterViewModel): Opened {
        val opened = Opened()
        setContent {
            NotificationTestHost {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    AppleNotificationCenterScreen(
                        vm,
                        onNavigateToDetail = { opened.details += it },
                        onNavigateToAnnouncements = { opened.announcements++ },
                        onNavigateToSettings = { opened.settings++ },
                        onLogin = { opened.login++ },
                        actions = opened.actions,
                        onBack = {},
                        Modifier.width(390.dp).fillMaxHeight(),
                    )
                }
            }
        }
        return opened
    }

    private fun ComposeUiTest.showAnnouncements(vm: AnnouncementsViewModel): Opened {
        val opened = Opened()
        setContent {
            NotificationTestHost {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    AppleAnnouncementsScreen(
                        vm,
                        onNavigateToDetail = { opened.announcementDetails += it },
                        onLogin = { opened.login++ },
                        onBack = {},
                        Modifier.width(390.dp).fillMaxHeight(),
                    )
                }
            }
        }
        return opened
    }

    private fun ComposeUiTest.waitForTag(tag: String) {
        waitUntil { onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun ComposeUiTest.row(id: String) =
        onNodeWithTag(AppleNotificationTestTags.CenterList)
            .performScrollToNode(hasTestTag(NotificationTestTags.item(id)))
            .let { onNodeWithTag(NotificationTestTags.item(id)) }

    /**
     * 行内带 [tag] 的节点. 行可点击, 文字合并在行上, 所以在未合并的树中查找.
     */
    private fun ComposeUiTest.inRow(id: String, tag: String) = onNode(
        hasTestTag(tag) and hasAnyAncestor(hasTestTag(NotificationTestTags.item(id))),
        useUnmergedTree = true,
    )

    private fun ComposeUiTest.countInRow(id: String, tag: String) =
        onAllNodes(hasTestTag(tag) and hasAnyAncestor(hasTestTag(NotificationTestTags.item(id))), useUnmergedTree = true)
            .fetchSemanticsNodes().size

    @Test
    fun `rows show the title the body and the place with the time`() = withSimplifiedChinese {
        runAniComposeUiTest {
            showCenter(NotificationCenterViewModel(notificationTestKoin()))
            waitForTag(NotificationTestTags.item("report-1"))

            // 举报结果: 举报对象放在第三行, 没有内文
            row("report-1")
            inRow("report-1", AppleNotificationTestTags.RowMeta).assertTextEquals("葬送的芙莉莲 第 12 话 · 2 小时前")
            assertEquals(0, countInRow("report-1", AppleNotificationTestTags.RowBody))

            row("security-1")
            inRow("security-1", AppleNotificationTestTags.RowBody).assertTextEquals("该账号已登录另一个 Animeko 账号")
            inRow("security-1", AppleNotificationTestTags.RowMeta).assertTextEquals("昨天")

            // 通用展示内容的正文显示两行
            row("system-1")
            inRow("system-1", AppleNotificationTestTags.RowBody)
                .assertTextEquals("绑定 Bangumi 账号后可同步追番进度与收藏。\n详情见 https://myani.org/help")
            inRow("system-1", AppleNotificationTestTags.RowMeta).assertTextEquals("9 月 3 日")
        }
    }

    @Test
    fun `unread rows have a dot and read rows do not`() = runAniComposeUiTest {
        showCenter(NotificationCenterViewModel(notificationTestKoin()))
        waitForTag(NotificationTestTags.item("report-1"))
        row("report-1")
        assertEquals(1, countInRow("report-1", NotificationTestTags.UNREAD_DOT))
        row("report-2")
        assertEquals(0, countInRow("report-2", NotificationTestTags.UNREAD_DOT))
    }

    @Test
    fun `interaction rows show the place and a type badge on the avatar`() = withSimplifiedChinese {
        runAniComposeUiTest {
            val repository = FakeNotificationRepository(notifications = TestNotifications + TestInteractionNotifications)
            val vm = NotificationCenterViewModel(notificationTestKoin(repository))
            showCenter(vm)
            waitForTag(NotificationTestTags.tab(NotificationGroup.INTERACTION))
            onNodeWithTag(NotificationTestTags.tab(NotificationGroup.INTERACTION)).performClick()
            waitForTag(NotificationTestTags.item("mention-1"))

            row("reply-1")
            inRow("reply-1", AppleNotificationTestTags.RowMeta).assertTextEquals("葬送的芙莉莲 第 1 话 · 30 分钟前")
            assertEquals(1, countInRow("reply-1", AppleNotificationTestTags.Badge))
            assertEquals(1, countInRow("reply-1", NotificationTestTags.LEADING_IMAGE))

            row("mention-1")
            assertEquals(1, countInRow("mention-1", AppleNotificationTestTags.Badge))
            assertEquals(1, countInRow("mention-1", NotificationTestTags.LEADING_IMAGE))

            // 没有头像: 圆形类型图标, 不加徽章
            row("follow-1")
            assertEquals(0, countInRow("follow-1", AppleNotificationTestTags.Badge))

            // 提及、关注等类型客户端不认识, 列表顶部提示更新
            waitForTag(NotificationTestTags.UPDATE_BANNER)
        }
    }

    @Test
    fun `a comment reply opens the comment thread directly`() = withSimplifiedChinese {
        runAniComposeUiTest {
            val repository = FakeNotificationRepository(notifications = TestNotifications + TestInteractionNotifications)
            val opened = showCenter(NotificationCenterViewModel(notificationTestKoin(repository)))
            waitForTag(NotificationTestTags.tab(NotificationGroup.INTERACTION))
            onNodeWithTag(NotificationTestTags.tab(NotificationGroup.INTERACTION)).performClick()
            waitForTag(NotificationTestTags.item("reply-1"))

            row("reply-1").performClick()
            assertEquals(listOf("ani:root-1:ani:reply-1"), opened.commentThreads)
            assertTrue(opened.details.isEmpty())
        }
    }

    @Test
    fun `header shows the unread count and the announcement entry`() = withSimplifiedChinese {
        runAniComposeUiTest {
            val opened = showCenter(NotificationCenterViewModel(notificationTestKoin()))
            waitForTag(NotificationTestTags.item("report-1"))
            onNodeWithTag(AppleNotificationTestTags.UnreadCount).assertTextEquals("3 条未读")
            onNodeWithTag(NotificationTestTags.ANNOUNCEMENT_ENTRY).performClick()
            assertEquals(1, opened.announcements)
            assertEquals(1, onAllNodesWithTag(NotificationTestTags.ANNOUNCEMENT_ENTRY_DOT, useUnmergedTree = true).fetchSemanticsNodes().size)
        }
    }

    @Test
    fun `an unselected tab with unread notifications has an alert dot`() = withSimplifiedChinese {
        runAniComposeUiTest {
            val badgeManager = FakeNotificationBadgeManager(
                TestNotificationBadgeState.copy(
                    unreadCounts = mapOf(NotificationGroup.NOTICE to 3, NotificationGroup.INTERACTION to 2),
                ),
            )
            showCenter(NotificationCenterViewModel(notificationTestKoin(badgeManager = badgeManager)))
            waitForTag(NotificationTestTags.item("report-1"))
            val stateDescription = SemanticsProperties.StateDescription
            onNodeWithTag(NotificationTestTags.tab(NotificationGroup.INTERACTION))
                .assert(SemanticsMatcher.expectValue(stateDescription, "2 条未读"))
            // 选中的分页不加提示点
            onNodeWithTag(NotificationTestTags.tab(NotificationGroup.NOTICE))
                .assert(SemanticsMatcher.keyNotDefined(stateDescription))
        }
    }

    @Test
    fun `clicking a row marks it read and opens the detail or account settings`() = runAniComposeUiTest {
        val repository = FakeNotificationRepository()
        val opened = showCenter(NotificationCenterViewModel(notificationTestKoin(repository)))
        waitForTag(NotificationTestTags.item("report-1"))

        row("report-1").performClick()
        assertEquals(listOf("report-1"), opened.details)
        waitUntil { repository.markedRead.flatten().contains("report-1") }
        waitUntil { countInRow("report-1", NotificationTestTags.UNREAD_DOT) == 0 }

        row("security-1").performClick()
        assertEquals(1, opened.accountSettings)
        assertEquals(listOf("report-1"), opened.details)
    }

    @Test
    fun `inline action reauthorizes bangumi`() = runAniComposeUiTest {
        val opened = showCenter(NotificationCenterViewModel(notificationTestKoin()))
        waitForTag(NotificationTestTags.item("bangumi-expired"))
        row("bangumi-expired")
        inRow("bangumi-expired", NotificationTestTags.INLINE_ACTION).performClick()
        assertEquals(1, opened.reauthorize)
        assertTrue(opened.details.isEmpty())
    }

    @Test
    fun `header buttons mark all read and open the settings`() = runAniComposeUiTest {
        val repository = FakeNotificationRepository()
        val opened = showCenter(NotificationCenterViewModel(notificationTestKoin(repository)))
        waitForTag(NotificationTestTags.item("report-1"))
        onNodeWithTag(NotificationTestTags.MARK_ALL_READ).assertIsEnabled().performClick()
        waitUntil { repository.markAllCalls.isNotEmpty() }
        assertEquals(NotificationGroup.NOTICE, repository.markAllCalls.single().group)

        onNodeWithTag(NotificationTestTags.SETTINGS).performClick()
        assertEquals(1, opened.settings)
    }

    @Test
    fun `scrolling past the header shows the collapsed bar`() = runAniComposeUiTest {
        val many = (1..12).map { createTestSystemNotification(id = "system-$it", createdAtMillis = TestNotificationNowMillis - it * 60_000L) }
        showCenter(NotificationCenterViewModel(notificationTestKoin(FakeNotificationRepository(notifications = many))))
        waitForTag(NotificationTestTags.item("system-1"))
        assertFalse(onAllNodesWithTag(AppleNotificationTestTags.CollapsedBar).fetchSemanticsNodes().isNotEmpty())
        row("system-12")
        waitForIdle()
        onNodeWithTag(AppleNotificationTestTags.CollapsedBar).assertExists()
    }

    @Test
    fun `guest sees a login prompt and the announcement entry`() = runAniComposeUiTest {
        val signInText = runBlocking { getString(Lang.login_sign_in) }
        val vm = NotificationCenterViewModel(notificationTestKoin(sessionStateProvider = FakeSessionStateProvider.Guest))
        val opened = showCenter(vm)
        waitForTag(NotificationTestTags.ANNOUNCEMENT_ENTRY)
        onNodeWithText(signInText).performClick()
        assertEquals(1, opened.login)
        assertTrue(onAllNodesWithTag(AppleNotificationTestTags.UnreadCount).fetchSemanticsNodes().isEmpty())
        assertTrue(onAllNodesWithTag(NotificationTestTags.item("report-1")).fetchSemanticsNodes().isEmpty())
    }

    @Test
    fun `empty tab shows the empty state`() = runAniComposeUiTest {
        showCenter(NotificationCenterViewModel(notificationTestKoin(FakeNotificationRepository(notifications = emptyList()))))
        waitForTag(NotificationTestTags.EMPTY)
    }

    @Test
    fun `announcement rows show the category the preview and the date`() = withSimplifiedChinese {
        runAniComposeUiTest {
            showAnnouncements(AnnouncementsViewModel(notificationTestKoin()))
            waitForTag(AnnouncementTestTags.item("announcement-1"))

            val first = AnnouncementTestTags.item("announcement-1")
            onNode(hasTestTag(AnnouncementTestTags.TAG) and hasAnyAncestor(hasTestTag(first)), useUnmergedTree = true)
                .assertTextEquals("置顶 · 维护")
            onNode(hasTestTag(AnnouncementTestTags.PIN) and hasAnyAncestor(hasTestTag(first)), useUnmergedTree = true)
                .assertExists()
            onNode(hasTestTag(AnnouncementTestTags.UNREAD_DOT) and hasAnyAncestor(hasTestTag(first)), useUnmergedTree = true)
                .assertExists()

            // 进入列表前未读的两条
            onNodeWithTag(AppleNotificationTestTags.UnreadCount).assertTextEquals("2 条未读")

            val read = AnnouncementTestTags.item("announcement-3")
            assertTrue(
                onAllNodes(hasTestTag(AnnouncementTestTags.UNREAD_DOT) and hasAnyAncestor(hasTestTag(read)), useUnmergedTree = true)
                    .fetchSemanticsNodes().isEmpty(),
            )
        }
    }

    @Test
    fun `clicking an announcement opens its detail`() = runAniComposeUiTest {
        val opened = showAnnouncements(AnnouncementsViewModel(notificationTestKoin()))
        waitForTag(AnnouncementTestTags.item("announcement-2"))
        onNodeWithTag(AnnouncementTestTags.item("announcement-2")).performClick()
        assertEquals(listOf("announcement-2"), opened.announcementDetails)
    }
}
