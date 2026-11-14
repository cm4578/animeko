/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.person

import androidx.compose.ui.test.ComposeUiTest
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onFirst
import kotlinx.coroutines.CoroutineScope
import me.him188.ani.app.data.models.person.CharacterDetailsInfo
import me.him188.ani.app.data.models.person.CharacterSubjectInfo
import me.him188.ani.app.data.models.person.InfoboxRowInfo
import me.him188.ani.app.data.models.person.PersonCastInfo
import me.him188.ani.app.data.models.person.PersonCommentTarget
import me.him188.ani.app.data.models.person.PersonDetailsInfo
import me.him188.ani.app.data.models.person.PersonSubjectSummary
import me.him188.ani.app.data.models.person.PersonWorkInfo
import me.him188.ani.app.data.models.subject.CharacterInfo
import me.him188.ani.app.data.models.subject.CharacterRole
import me.him188.ani.app.data.models.subject.PersonInfo
import me.him188.ani.app.data.models.subject.PersonPosition
import me.him188.ani.app.data.models.subject.PersonType
import me.him188.ani.app.domain.comment.CommentImageStatusResult
import me.him188.ani.app.domain.comment.CommentImageUploadResult
import me.him188.ani.app.domain.comment.CommentSendResult
import me.him188.ani.app.ui.comment.CommentEditorState
import me.him188.ani.app.ui.comment.CommentReportState
import me.him188.ani.app.ui.comment.UIComment
import me.him188.ani.app.ui.comment.UIRichText
import me.him188.ani.app.ui.comment.createTestCommentState
import me.him188.ani.app.ui.comment.generateUiComment
import me.him188.ani.app.ui.comment.thread.CommentThreadHost
import me.him188.ani.app.ui.foundation.stateOf
import me.him188.ani.utils.platform.annotations.TestOnly

/**
 * 人物与角色详情 (Apple 版) 测试共用的资料.
 */
@OptIn(TestOnly::class)
internal object TestPeople {
    const val PERSON_ID = 7
    const val CHARACTER_ID = 9

    private fun person(id: Int, name: String, nameCn: String) = PersonInfo(
        id = id,
        name = name,
        type = PersonType.Individual,
        careers = emptyList(),
        imageLarge = "",
        imageMedium = "",
        summary = "",
        locked = null,
        nameCn = nameCn,
    )

    private fun subject(id: Int, name: String) = PersonSubjectSummary(subjectId = id, name = name, nameCn = "", imageLarge = "")

    val personDetails = PersonDetailsInfo(
        person = person(PERSON_ID, name = "Original Person", nameCn = "Person Name").copy(summary = "A short summary."),
        career = listOf("seiyu"),
        infobox = listOf(InfoboxRowInfo("Birthday", "Feb 25")),
        collects = 12345,
        commentCount = 56,
        workCount = 120,
        castCount = 812,
    )

    /** 12 个出演角色: 角色 201–212, 条目 101–112. */
    val casts = (1..12).map {
        PersonCastInfo(
            subject = subject(100 + it, "Subject $it"),
            character = CharacterInfo(
                id = 200 + it,
                name = "Character $it",
                nameCn = "",
                actors = emptyList(),
                imageMedium = "",
                imageLarge = "",
            ),
        )
    }

    /** 12 部参与作品: 条目 301–312. */
    val works = (1..12).map { PersonWorkInfo(subject(300 + it, "Work $it"), positions = listOf(PersonPosition(1))) }

    val actors = listOf(person(21, "Actor A", ""), person(22, "Actor B", ""))

    val characterDetails = CharacterDetailsInfo(
        character = CharacterInfo(
            id = CHARACTER_ID,
            name = "Original Character",
            nameCn = "Character Name",
            actors = actors,
            imageMedium = "",
            imageLarge = "",
        ),
        role = 1,
        summary = "A character summary.",
        infobox = listOf(InfoboxRowInfo("Height", "160 cm")),
        collects = 500,
        commentCount = 3,
        subjectCount = 2,
    )

    /** 出演作品: 条目 401 为主角, 402 为配角. */
    val characterSubjects = listOf(
        CharacterSubjectInfo(subject(401, "Main Work"), CharacterRole.MAIN, actors),
        CharacterSubjectInfo(subject(402, "Side Work"), CharacterRole.SUPPORTING, actors.take(1)),
    )

    fun comments(size: Int = 5): List<UIComment> = generateUiComment(size)

    fun commentsState(scope: CoroutineScope, comments: List<UIComment>) = PeopleCommentsState(
        target = PersonCommentTarget.Person(PERSON_ID),
        commentState = createTestCommentState(scope, comments),
        reportState = CommentReportState(onSubmitReport = { _, _, _ -> }, backgroundScope = scope),
        editorState = CommentEditorState(
            showExpandEditCommentButton = false,
            initialEditExpanded = false,
            panelTitle = stateOf("Person Name"),
            stickers = stateOf(emptyList()),
            richTextRenderer = { UIRichText(emptyList()) },
            onSend = { _, _ -> CommentSendResult.Ok },
            onUploadImage = { CommentImageUploadResult.Ready("https://example.com/image.webp") },
            onCheckImageStatus = { CommentImageStatusResult.Ok(emptyMap()) },
            backgroundScope = scope,
        ),
        originalCommentsUrl = "https://bgm.tv/person/$PERSON_ID",
        threadHost = CommentThreadHost { _, _ -> error("People details tests do not open comment threads") },
        onRefresh = {},
    )
}

/**
 * 记录 [PeopleDetailsNavigation] 的点击.
 */
internal class RecordingPeopleNavigation {
    val persons = mutableListOf<Int>()
    val characters = mutableListOf<Int>()
    val subjects = mutableListOf<Int>()
    val navigation = PeopleDetailsNavigation(
        onClickPerson = { persons += it },
        onClickCharacter = { characters += it },
        onClickSubject = { subjects += it.subjectId },
    )
}

/**
 * 断言标签为 [tag] 的容器里有含 [text] 的文字. 容器不合并子节点的文字, 所以按祖先查找.
 */
internal fun ComposeUiTest.assertTextInside(tag: String, text: String) {
    onAllNodes(hasText(text, substring = true) and hasAnyAncestor(hasTestTag(tag))).onFirst().assertExists()
}
