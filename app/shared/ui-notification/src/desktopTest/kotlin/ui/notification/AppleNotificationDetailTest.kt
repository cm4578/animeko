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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.runBlocking
import me.him188.ani.app.data.models.notification.Announcement
import me.him188.ani.app.data.models.notification.AppNotification
import me.him188.ani.app.data.models.notification.NotificationIcon
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.platform.navigation.LocalBrowserNavigator
import me.him188.ani.app.ui.foundation.LocalPlatform
import me.him188.ani.app.ui.framework.runAniComposeUiTest
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.settings_mediasource_retry
import me.him188.ani.utils.platform.Platform
import me.him188.ani.utils.platform.annotations.TestOnly
import org.jetbrains.compose.resources.getString

/**
 * docs/design/ani-style/03-mobile-pages.md §4.9 iOS 通知详情与公告详情; 05 C4、G20.
 */
class AppleNotificationDetailTest {
    private class Opened {
        val episodes = mutableListOf<String>()
        val commentThreads = mutableListOf<String>()
        var reauthorize = 0
        var checkUpdate = 0
        var retries = 0
        val browser = RecordingBrowserNavigator()

        val actions = NotificationActions.Noop.let { noop ->
            NotificationActions(
                onOpenEpisode = { subjectId, episodeId -> episodes += "$subjectId:$episodeId" },
                onOpenSubject = noop.onOpenSubject,
                onOpenPerson = noop.onOpenPerson,
                onOpenCharacter = noop.onOpenCharacter,
                onOpenCommentThread = { _, commentId, replyId -> commentThreads += "$commentId:$replyId" },
                onReauthorizeBangumi = { reauthorize++ },
                onOpenAccountSettings = noop.onOpenAccountSettings,
                onCheckUpdate = { checkUpdate++ },
            )
        }
    }

    private fun ComposeUiTest.showNotification(state: DetailLoadState<AppNotification>): Opened {
        val opened = Opened()
        setContent {
            NotificationTestHost {
                CompositionLocalProvider(
                    LocalPlatform provides Platform.Ios,
                    LocalBrowserNavigator provides opened.browser,
                ) {
                    AppleNotificationDetailLayout(
                        state,
                        opened.actions,
                        onRetry = { opened.retries++ },
                        onBack = {},
                        Modifier.width(390.dp).fillMaxHeight(),
                    )
                }
            }
        }
        waitForIdle()
        return opened
    }

    private fun ComposeUiTest.showAnnouncement(announcement: Announcement): Opened {
        val opened = Opened()
        setContent {
            NotificationTestHost {
                CompositionLocalProvider(
                    LocalPlatform provides Platform.Ios,
                    LocalBrowserNavigator provides opened.browser,
                ) {
                    AppleAnnouncementDetailLayout(
                        DetailLoadState.Loaded(announcement),
                        onRetry = {},
                        onBack = {},
                        Modifier.width(390.dp).fillMaxHeight(),
                    )
                }
            }
        }
        waitForIdle()
        return opened
    }

    private fun ComposeUiTest.exists(tag: String) = onAllNodesWithTag(tag).fetchSemanticsNodes().isNotEmpty()

    @Test
    fun `interaction detail quotes the body and goes to the comments`() = withSimplifiedChinese {
        runAniComposeUiTest {
            val opened = showNotification(DetailLoadState.Loaded(createTestGenericNotification()))
            onNodeWithText("Bob 在评论中提到了你").assertExists()
            onNode(
                hasText("同意，这一集的作画太好了") and hasAnyAncestor(hasTestTag(AppleNotificationTestTags.Quote)),
                useUnmergedTree = true,
            ).assertExists()
            onNodeWithTag(AppleNotificationTestTags.TargetRow).assertTextEquals("葬送的芙莉莲 第 1 话")

            onNodeWithTag(NotificationTestTags.DETAIL_ACTION).assertTextContains("前往该评论").performClick()
            assertEquals(listOf("400602:1227087"), opened.episodes)
            // 目标一列前往同一个位置
            onNodeWithTag(AppleNotificationTestTags.TargetRow).performClick()
            assertEquals(listOf("400602:1227087", "400602:1227087"), opened.episodes)

            // 客户端不认识提及的类型, 底部提示更新
            onNodeWithTag(NotificationTestTags.DETAIL_CHECK_UPDATE).performScrollTo().performClick()
            assertEquals(1, opened.checkUpdate)
        }
    }

    @Test
    fun `comment reply detail goes to the comment thread`() = withSimplifiedChinese {
        runAniComposeUiTest {
            val opened = showNotification(DetailLoadState.Loaded(createTestCommentReply()))
            onNodeWithText("Bob 回复了你的评论").assertExists()
            onNode(
                hasText("同意，这一集的作画太好了") and hasAnyAncestor(hasTestTag(AppleNotificationTestTags.Quote)),
                useUnmergedTree = true,
            ).assertExists()
            onNodeWithTag(AppleNotificationTestTags.TargetRow).assertTextEquals("葬送的芙莉莲 第 1 话")

            onNodeWithTag(NotificationTestTags.DETAIL_ACTION).assertTextContains("前往该评论").performClick()
            assertEquals(listOf("ani:root-1:ani:reply-1"), opened.commentThreads)
            assertTrue(opened.episodes.isEmpty())
            // 认识的类型, 不提示更新
            assertFalse(exists(NotificationTestTags.DETAIL_CHECK_UPDATE))
        }
    }

    @Test
    fun `interaction without a target has no target row or button`() = runAniComposeUiTest {
        val follow = createTestGenericNotification(
            id = "follow-1",
            rawType = "follow",
            overline = null,
            title = "Alice 关注了你",
            body = null,
            icon = NotificationIcon.FOLLOW,
            target = null,
        )
        showNotification(DetailLoadState.Loaded(follow))
        assertTrue(!exists(AppleNotificationTestTags.TargetRow))
        assertTrue(!exists(AppleNotificationTestTags.Quote))
        assertTrue(!exists(NotificationTestTags.DETAIL_ACTION))
        assertTrue(!exists(NotificationTestTags.DETAIL_LINK))
        onNodeWithTag(AppleNotificationTestTags.DetailTime).assertExists()
    }

    @Test
    fun `report result shows the result card the details and the snapshot`() = withSimplifiedChinese {
        runAniComposeUiTest {
            val opened = showNotification(DetailLoadState.Loaded(createTestReportResult()))
            onNode(
                hasText("举报成立") and hasAnyAncestor(hasTestTag(AppleNotificationTestTags.ResultCard)),
                useUnmergedTree = true,
            ).assertExists()
            onNodeWithText("处理信息").assertExists()
            onNodeWithTag(NotificationTestTags.REPORT_REASON).assertTextContains("剧透", substring = true)

            // 「举报对象」一列前往该剧集
            onNodeWithTag(NotificationTestTags.REPORT_TARGET)
                .assertTextContains("葬送的芙莉莲 第 12 话", substring = true)
                .performScrollTo()
                .performClick()
            assertEquals(listOf("400602:1227087"), opened.episodes)

            onNodeWithTag(NotificationTestTags.REPORT_SNAPSHOT_AUTHOR, useUnmergedTree = true).assertTextEquals("星野")
            onNodeWithTag(NotificationTestTags.REPORT_SNAPSHOT_TEXT, useUnmergedTree = true)
                .assertTextEquals("最后一集芙莉莲会和费伦分开")
            onNodeWithTag(AppleNotificationTestTags.Footnote).performScrollTo().assertExists()
            // 跳转只在「举报对象」一列, 没有另外的按钮
            assertTrue(!exists(NotificationTestTags.DETAIL_ACTION))
        }
    }

    @Test
    fun `report result without a known target is not clickable`() = runAniComposeUiTest {
        val opened = showNotification(
            DetailLoadState.Loaded(createTestReportResult(subjectId = null, episodeId = null)),
        )
        onNodeWithTag(NotificationTestTags.REPORT_TARGET).performScrollTo().performClick()
        assertTrue(opened.episodes.isEmpty())
    }

    @Test
    fun `system detail follows the article layout with reauthorization`() = withSimplifiedChinese {
        runAniComposeUiTest {
            val opened = showNotification(DetailLoadState.Loaded(createTestBangumiExpired()))
            onNodeWithText("Bangumi 授权已过期").assertExists()
            onNodeWithText("追番进度已停止同步，重新授权后自动恢复。").assertExists()
            onNodeWithTag(NotificationTestTags.DETAIL_ACTION).assertTextContains("重新授权").performClick()
            assertEquals(1, opened.reauthorize)
        }
    }

    @Test
    fun `system detail with a link opens the browser`() = runAniComposeUiTest {
        val opened = showNotification(DetailLoadState.Loaded(createTestSystemNotification()))
        onNodeWithTag(NotificationTestTags.DETAIL_LINK).performScrollTo().performClick()
        assertEquals(listOf("https://myani.org"), opened.browser.openedUrls.toList())
    }

    @Test
    fun `announcement detail shows the category and the link button`() = withSimplifiedChinese {
        runAniComposeUiTest {
            val opened = showAnnouncement(createTestAnnouncement(pinned = true, linkUrl = "https://myani.org/status"))
            onNodeWithTag(AnnouncementTestTags.TAG, useUnmergedTree = true).assertTextEquals("置顶 · 维护")
            onNodeWithTag(AnnouncementTestTags.DETAIL_LINK).performScrollTo().performClick()
            assertEquals(listOf("https://myani.org/status"), opened.browser.openedUrls.toList())
        }
    }

    @Test
    fun `missing and failed details show their states`() = runAniComposeUiTest {
        val retryText = runBlocking { getString(Lang.settings_mediasource_retry) }
        var state: DetailLoadState<AppNotification> by mutableStateOf(DetailLoadState.NotFound)
        val opened = Opened()
        setContent {
            NotificationTestHost {
                CompositionLocalProvider(LocalPlatform provides Platform.Ios) {
                    AppleNotificationDetailLayout(
                        state,
                        opened.actions,
                        onRetry = { opened.retries++ },
                        onBack = {},
                        Modifier.width(390.dp).fillMaxHeight(),
                    )
                }
            }
        }
        onNodeWithTag(NotificationTestTags.DETAIL_NOT_FOUND).assertExists()

        state = DetailLoadState.Failed(LoadError.NetworkError)
        waitForIdle()
        assertTrue(!exists(NotificationTestTags.DETAIL_NOT_FOUND))
        onNodeWithText(retryText).performClick()
        assertEquals(1, opened.retries)
    }
}
