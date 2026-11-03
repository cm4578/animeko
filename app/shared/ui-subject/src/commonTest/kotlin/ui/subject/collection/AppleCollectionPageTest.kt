/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.collection

import me.him188.ani.app.data.models.player.EpisodeHistory
import me.him188.ani.app.data.models.subject.TestSubjectCollections
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AppleCollectionPageTest {
    @Test
    fun `remaining minutes round up`() {
        val progress = continueWatchingProgress(
            EpisodeHistory(episodeId = 1, positionMillis = 6 * 60_000L, durationMillis = 24 * 60_000L + 1),
        )!!
        assertEquals(19, progress.remainingMinutes)
        assertEquals(0.25f, progress.fraction, 0.001f)
    }

    @Test
    fun `no remaining minutes without a duration`() {
        assertNull(continueWatchingProgress(null))
        assertNull(continueWatchingProgress(EpisodeHistory(episodeId = 1, positionMillis = 1000, durationMillis = null)))
        assertNull(continueWatchingProgress(EpisodeHistory(episodeId = 1, positionMillis = 1000, durationMillis = 0)))
    }

    @Test
    fun `position past the end counts as finished`() {
        val progress = continueWatchingProgress(
            EpisodeHistory(episodeId = 1, positionMillis = 30 * 60_000L, durationMillis = 24 * 60_000L),
        )!!
        assertEquals(0, progress.remainingMinutes)
        assertEquals(1f, progress.fraction)
    }

    @Test
    fun `episode progress counts watched episodes`() {
        val collection = TestSubjectCollections[0]
        val progress = collection.episodeProgress()!!
        assertEquals(collection.episodes.count { it.collectionType == UnifiedCollectionType.DONE }, progress.watched)
        assertEquals(collection.episodes.size, progress.total)
    }

    @Test
    fun `no episode progress without episodes`() {
        assertNull(TestSubjectCollections[0].copy(episodes = emptyList()).episodeProgress())
    }
}
