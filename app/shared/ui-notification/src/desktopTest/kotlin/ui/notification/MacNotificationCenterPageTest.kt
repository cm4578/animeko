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

import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import me.him188.ani.app.data.models.notification.NotificationGroup
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.utils.platform.Arch
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.annotations.TestOnly

/**
 * docs/design/ani-style/04-macos.md §4.6 macOS 通知中心与公告.
 */
class MacNotificationCenterPageTest {
    private class Opened {
        val details = mutableListOf<String>()
        val announcementDetails = mutableListOf<String>()
        val episodes = mutableListOf<String>()
        val commentThreads = mutableListOf<String>()
        var settings = 0
        var announcements = 0

        val actions = NotificationActions.Noop.let { noop ->
            NotificationActions(
                onOpenEpisode = { subjectId, episodeId -> episodes += "$subjectId:$episodeId" },
                onOpenSubject = noop.onOpenSubject,
                onOpenPerson = noop.onOpenPerson,
                onOpenCharacter = noop.onOpenCharacter,
                onOpenCommentThread = { _, commentId, replyId -> commentThreads += "$commentId:$replyId" },
                onReauthorizeBangumi = noop.onReauthorizeBangumi,
                onOpenAccountSettings = noop.onOpenAccountSettings,
                onCheckUpdate = noop.onCheckUpdate,
            )
        }
    }

    /**
     * 宽 1000: 放得下左列表 380 与右详情.
     */
    private fun ComposeUiTest.showCenter(
        vm: NotificationCenterViewModel,
        width: Dp = 1000.dp,
    ): Opened {
        val opened = Opened()
        setContent {
            NotificationTestHost {
                CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                    MacNotificationCenterScreen(
                        vm,
                        onNavigateToDetail = { opened.details += it },
                        onNavigateToAnnouncements = { opened.announcements++ },
                        onNavigateToSettings = { opened.settings++ },
                        onLogin = {},
                        actions = opened.actions,
                        Modifier.size(width, 720.dp),
                    )
                }
            }
        }
        return opened
    }

    private fun ComposeUiTest.waitForTag(tag: String) {
        waitUntil { onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty() }
    }

    private fun ComposeUiTest.exists(tag: String) = onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `selecting a notification shows it beside the list`() = withSimplifiedChinese {
        runAniComposeUiTest {
            val opened = showCenter(NotificationCenterViewModel(notificationTestKoin()))
            waitForTag(NotificationTestTags.item("report-1"))
            onNodeWithTag(NotificationTestTags.DETAIL_PLACEHOLDER).assertExists()

            onNodeWithTag(NotificationTestTags.item("report-1")).performClick()
            onNodeWithTag(NotificationTestTags.item("report-1")).assertIsSelected()
            onNodeWithTag(NotificationTestTags.item("report-2")).assertIsNotSelected()
            assertTrue(opened.details.isEmpty())
            onNodeWithTag(AppleNotificationTestTags.ResultCard).assertExists()

            // 举报结果的跳转在标题行右侧
            onNodeWithTag(NotificationTestTags.DETAIL_TOP_BAR_ACTION).assertTextContains("前往该剧集").performClick()
            assertEquals(listOf("400602:1227087"), opened.episodes)
        }
    }

    @Test
    fun `interaction detail has go to comments in the title row`() = withSimplifiedChinese {
        runAniComposeUiTest {
            val repository = FakeNotificationRepository(notifications = TestNotifications + TestInteractionNotifications)
            val opened = showCenter(NotificationCenterViewModel(notificationTestKoin(repository)))
            waitForTag(NotificationTestTags.tab(NotificationGroup.INTERACTION))
            onNodeWithTag(NotificationTestTags.tab(NotificationGroup.INTERACTION)).performClick()
            waitForTag(NotificationTestTags.item("mention-1"))
            onNodeWithTag(NotificationTestTags.item("mention-1")).performClick()

            onNodeWithTag(AppleNotificationTestTags.TargetRow).assertTextEquals("孤独摇滚！ 第 8 话")
            onNode(
                hasText("阿九：@你 之前说的那个伏笔，这集回收了") and hasAnyAncestor(hasTestTag(AppleNotificationTestTags.Quote)),
                useUnmergedTree = true,
            ).assertExists()
            onNodeWithTag(NotificationTestTags.DETAIL_ACTION).assertTextContains("前往该评论").performClick()
            assertEquals(listOf("328609:1144392"), opened.episodes)
        }
    }

    @Test
    fun `a comment reply opens the comment thread instead of the detail`() = withSimplifiedChinese {
        runAniComposeUiTest {
            val repository = FakeNotificationRepository(notifications = TestNotifications + TestInteractionNotifications)
            val opened = showCenter(NotificationCenterViewModel(notificationTestKoin(repository)))
            waitForTag(NotificationTestTags.tab(NotificationGroup.INTERACTION))
            onNodeWithTag(NotificationTestTags.tab(NotificationGroup.INTERACTION)).performClick()
            waitForTag(NotificationTestTags.item("reply-1"))
            onNodeWithTag(NotificationTestTags.item("reply-1")).performClick()

            assertEquals(listOf("ani:root-1:ani:reply-1"), opened.commentThreads)
            assertTrue(onAllNodesWithTag(AppleNotificationTestTags.TargetRow).fetchSemanticsNodes().isEmpty())
        }
    }

    @Test
    fun `list column is 380 wide with the detail beside it`() = runAniComposeUiTest {
        showCenter(NotificationCenterViewModel(notificationTestKoin()))
        waitForTag(NotificationTestTags.item("report-1"))
        val list = onNodeWithTag(AppleNotificationTestTags.MacListPane).getUnclippedBoundsInRoot()
        // 列表栏 380, 内距左 32 右 16
        assertEquals(32f, list.left.value, 0.5f)
        assertEquals(332f, (list.right - list.left).value, 0.5f)
        val detail = onNodeWithTag(NotificationTestTags.DETAIL_PANE).getUnclippedBoundsInRoot()
        assertEquals(381f, detail.left.value, 0.5f)
    }

    @Test
    fun `narrow width shows only the list and pushes the detail`() = runAniComposeUiTest {
        val opened = showCenter(NotificationCenterViewModel(notificationTestKoin()), width = 700.dp)
        waitForTag(NotificationTestTags.item("report-1"))
        assertTrue(!exists(NotificationTestTags.DETAIL_PANE))
        onNodeWithTag(NotificationTestTags.item("report-1")).performClick()
        assertEquals(listOf("report-1"), opened.details)
    }

    @Test
    fun `toolbar marks all read and opens the notification settings`() = runAniComposeUiTest {
        val repository = FakeNotificationRepository()
        val opened = showCenter(NotificationCenterViewModel(notificationTestKoin(repository)))
        waitForTag(NotificationTestTags.item("report-1"))
        onNodeWithTag(NotificationTestTags.MARK_ALL_READ).performClick()
        waitUntil { repository.markAllCalls.isNotEmpty() }
        onNodeWithTag(NotificationTestTags.SETTINGS).performClick()
        assertEquals(1, opened.settings)
    }

    @Test
    fun `announcements show the selected one beside the list`() = withSimplifiedChinese {
        runAniComposeUiTest {
            val opened = Opened()
            setContent {
                NotificationTestHost {
                    CompositionLocalProvider(LocalPlatform provides Platform.MacOS(Arch.AARCH64)) {
                        MacAnnouncementsScreen(
                            AnnouncementsViewModel(notificationTestKoin()),
                            onNavigateToDetail = { opened.announcementDetails += it },
                            onLogin = {},
                            Modifier.size(1000.dp, 720.dp),
                        )
                    }
                }
            }
            waitForTag(AnnouncementTestTags.item("announcement-2"))
            onNodeWithTag(AnnouncementTestTags.DETAIL_PLACEHOLDER).assertExists()
            onNodeWithTag(AnnouncementTestTags.item("announcement-2")).performClick()
            onNodeWithTag(AnnouncementTestTags.item("announcement-2")).assertIsSelected()
            onNode(
                hasText("新版本发布：评论回应与一起看改进") and hasAnyAncestor(hasTestTag(AnnouncementTestTags.DETAIL)),
                useUnmergedTree = true,
            ).assertExists()
            assertTrue(opened.announcementDetails.isEmpty())
        }
    }
}
