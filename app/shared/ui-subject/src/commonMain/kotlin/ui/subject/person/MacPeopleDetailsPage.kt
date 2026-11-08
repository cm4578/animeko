/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.person

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItemsWithLifecycle
import me.him188.ani.app.data.models.person.CharacterDetailsInfo
import me.him188.ani.app.data.models.person.CharacterSubjectInfo
import me.him188.ani.app.data.models.person.InfoboxRowInfo
import me.him188.ani.app.data.models.person.PersonCastInfo
import me.him188.ani.app.data.models.person.PersonDetailsInfo
import me.him188.ani.app.data.models.person.PersonSubjectSummary
import me.him188.ani.app.data.models.person.PersonWorkInfo
import me.him188.ani.app.data.models.subject.CharacterRole
import me.him188.ani.app.data.models.subject.PersonInfo
import me.him188.ani.app.data.models.subject.nameCn
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.comment.CommentState
import me.him188.ani.app.ui.comment.UIComment
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.ImageViewer
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.ApplePopoverMenu
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.MacPageToolbar
import me.him188.ani.app.ui.foundation.apple.MacToolbarIconButton
import me.him188.ani.app.ui.foundation.apple.ShareLinkMenuItems
import me.him188.ani.app.ui.foundation.apple.bangumiCharacterUrl
import me.him188.ani.app.ui.foundation.apple.bangumiPersonUrl
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.rememberImageViewerHandler
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.person_details_basic_info
import me.him188.ani.app.ui.lang.person_details_casts
import me.him188.ani.app.ui.lang.person_details_character_subjects
import me.him188.ani.app.ui.lang.person_details_comments
import me.him188.ani.app.ui.lang.person_details_comments_count
import me.him188.ani.app.ui.lang.person_details_no_comments
import me.him188.ani.app.ui.lang.person_details_view_all_count
import me.him188.ani.app.ui.lang.person_details_voice_actors
import me.him188.ani.app.ui.lang.person_details_works
import me.him188.ani.app.ui.lang.subject_details_open_in_bangumi
import me.him188.ani.app.ui.lang.subject_details_share
import me.him188.ani.app.ui.lang.subject_details_summary
import me.him188.ani.app.ui.lang.subject_details_view_all
import me.him188.ani.app.ui.subject.collection.MacPageDefaults
import me.him188.ani.app.ui.subject.details.MacSectionHeader
import me.him188.ani.app.ui.subject.details.MacStatCell
import me.him188.ani.app.ui.subject.details.MacSummary
import me.him188.ani.app.ui.subject.details.components.PersonCard
import me.him188.ani.app.ui.subject.details.sections.ViewAllSheet
import me.him188.ani.app.ui.subject.details.sections.groupThousands
import me.him188.ani.app.ui.subject.details.sections.toPlainText
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 版人物详情 (04 §4.4).
 *
 * 工具栏右侧为「在 Bangumi 開啟」与分享 (05 G6). 左栏 240: 立绘 (点击看大图)、参与作品 / 出演角色 / 评论统计卡、基本资讯;
 * 右栏: 大标题与「原名 · 职业 · N 人收藏」、出演角色 (5 栏, 最多 2 行)、参与作品 (3 栏文字格, 最多 3 行)、简介、评论 (两栏, 最多 4 条).
 */
@Composable
fun MacPersonDetailsScreen(
    vm: PersonDetailsViewModel,
    personId: Int,
    modifier: Modifier = Modifier,
) {
    val details by vm.details.collectAsStateWithLifecycle()
    MacPersonDetailsLayout(
        details,
        personId,
        vm.castsPager.collectAsLazyPagingItems(),
        vm.worksPager.collectAsLazyPagingItems(),
        vm.comments,
        modifier = modifier,
    )
}

/**
 * macOS 版角色详情 (04 §4.4). 版式同 [MacPersonDetailsScreen]: 统计为出演作品 / 声优 / 评论;
 * 右栏为大标题与「日文名 · 角色 · N 人收藏」、声优列表、出演作品 (5 栏, 主角有「主角」标记)、简介、评论.
 */
@Composable
fun MacCharacterDetailsScreen(
    vm: CharacterDetailsViewModel,
    characterId: Int,
    modifier: Modifier = Modifier,
) {
    val details by vm.details.collectAsStateWithLifecycle()
    MacCharacterDetailsLayout(
        details,
        characterId,
        vm.subjectsPager.collectAsLazyPagingItems(),
        vm.comments,
        modifier = modifier,
    )
}

/**
 * [MacPersonDetailsScreen] 的版面. [details] 为 `null` 时为加载中.
 */
@Composable
internal fun MacPersonDetailsLayout(
    details: PersonDetailsInfo?,
    personId: Int,
    casts: LazyPagingItems<PersonCastInfo>,
    works: LazyPagingItems<PersonWorkInfo>,
    comments: PeopleCommentsState,
    modifier: Modifier = Modifier,
    navigation: PeopleDetailsNavigation = rememberPeopleDetailsNavigation(),
) {
    var showAllCasts by rememberSaveable { mutableStateOf(false) }
    var showAllWorks by rememberSaveable { mutableStateOf(false) }
    val person = details?.person
    MacPeopleDetailsScaffold(
        bangumiUrl = bangumiPersonUrl(personId),
        imageUrl = person?.imageLarge,
        stats = listOf(
            PeopleStat(details?.workCount, Lang.person_details_works),
            PeopleStat(details?.castCount, Lang.person_details_casts),
            PeopleStat(details?.commentCount, Lang.person_details_comments),
        ),
        infobox = details?.infobox.orEmpty(),
        comments = comments,
        modifier = modifier,
    ) { onShowAllComments ->
        MacPeopleTitle(
            person?.displayName.orEmpty(),
            details?.let {
                listOfNotNull(
                    person?.name?.takeIf { name -> name.isNotBlank() && name != person.displayName },
                    peopleMetaLine(personKindLabel(it.career), it.collects),
                ).joinToString(" · ")
            },
        )
        if (casts.itemCount > 0) {
            MacGridSection(
                "casts",
                stringResource(Lang.person_details_casts),
                totalCount = details?.castCount,
                onViewAll = { showAllCasts = true },
            ) {
                MacCardGrid(casts.loadedItems(MacPeopleDetailsDefaults.MaxCards)) { cast ->
                    MacPeopleCard(
                        imageUrl = cast.character.imageMedium,
                        name = cast.character.displayName,
                        caption = cast.subject.displayName,
                        onClick = { navigation.onClickCharacter(cast.character.id) },
                        Modifier.testTag(MacPeopleDetailsTestTags.cast(cast.character.id)),
                    )
                }
            }
        }
        if (works.itemCount > 0) {
            MacGridSection(
                "works",
                stringResource(Lang.person_details_works),
                totalCount = details?.workCount,
                onViewAll = { showAllWorks = true },
            ) {
                MacWorksGrid(works.loadedItems(MacPeopleDetailsDefaults.MaxWorks), onClick = navigation.onClickSubject)
            }
        }
        person?.summary?.takeIf { it.isNotBlank() }?.let { MacSummarySection(it) }
        MacCommentsSection(comments.commentState, onShowAll = onShowAllComments)
    }

    if (showAllCasts) {
        ViewAllSheet(
            title = stringResource(Lang.person_details_casts),
            items = casts,
            onDismissRequest = { showAllCasts = false },
        ) { cast ->
            PersonCard(
                avatarUrl = cast.character.imageMedium,
                name = cast.character.displayName,
                relation = cast.subject.displayName,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        showAllCasts = false
                        navigation.onClickCharacter(cast.character.id)
                    },
            )
        }
    }
    if (showAllWorks) {
        ViewAllSheet(
            title = stringResource(Lang.person_details_works),
            items = works,
            onDismissRequest = { showAllWorks = false },
        ) { work ->
            PersonCard(
                avatarUrl = work.subject.imageLarge,
                name = work.subject.displayName,
                relation = work.positions.mapNotNull { it.nameCn }.distinct().joinToString("、"),
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        showAllWorks = false
                        navigation.onClickSubject(work.subject)
                    },
            )
        }
    }
}

/**
 * [MacCharacterDetailsScreen] 的版面. [details] 为 `null` 时为加载中.
 */
@Composable
internal fun MacCharacterDetailsLayout(
    details: CharacterDetailsInfo?,
    characterId: Int,
    subjects: LazyPagingItems<CharacterSubjectInfo>,
    comments: PeopleCommentsState,
    modifier: Modifier = Modifier,
    navigation: PeopleDetailsNavigation = rememberPeopleDetailsNavigation(),
) {
    var showAllSubjects by rememberSaveable { mutableStateOf(false) }
    val character = details?.character
    val actors = character?.actors.orEmpty()
    MacPeopleDetailsScaffold(
        bangumiUrl = bangumiCharacterUrl(characterId),
        imageUrl = character?.imageLarge,
        stats = listOf(
            PeopleStat(details?.subjectCount, Lang.person_details_character_subjects),
            PeopleStat(details?.let { actors.size }, Lang.person_details_voice_actors),
            PeopleStat(details?.commentCount, Lang.person_details_comments),
        ),
        infobox = details?.infobox.orEmpty(),
        comments = comments,
        modifier = modifier,
    ) { onShowAllComments ->
        MacPeopleTitle(
            character?.displayName.orEmpty(),
            details?.let {
                listOfNotNull(
                    character?.name?.takeIf { name -> name.isNotBlank() && name != character.displayName },
                    peopleMetaLine(characterRoleLabel(it.role), it.collects),
                ).joinToString(" · ")
            },
        )
        if (actors.isNotEmpty()) {
            MacVoiceActorsSection(actors, onClick = { navigation.onClickPerson(it.id) })
        }
        if (subjects.itemCount > 0) {
            MacGridSection(
                "subjects",
                stringResource(Lang.person_details_character_subjects),
                totalCount = details?.subjectCount,
                onViewAll = { showAllSubjects = true },
            ) {
                MacCardGrid(subjects.loadedItems(MacPeopleDetailsDefaults.MaxCards)) { item ->
                    MacPeopleCard(
                        imageUrl = item.subject.imageLarge,
                        name = item.subject.displayName,
                        caption = null,
                        onClick = { navigation.onClickSubject(item.subject) },
                        Modifier.testTag(MacPeopleDetailsTestTags.work(item.subject.subjectId)),
                        badge = if (item.role == CharacterRole.MAIN) item.role.nameCn else null,
                    )
                }
            }
        }
        details?.summary?.takeIf { it.isNotBlank() }?.let { MacSummarySection(it) }
        MacCommentsSection(comments.commentState, onShowAll = onShowAllComments)
    }

    if (showAllSubjects) {
        ViewAllSheet(
            title = stringResource(Lang.person_details_character_subjects),
            items = subjects,
            onDismissRequest = { showAllSubjects = false },
        ) { item ->
            PersonCard(
                avatarUrl = item.subject.imageLarge,
                name = item.subject.displayName,
                relation = item.role.nameCn,
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        showAllSubjects = false
                        navigation.onClickSubject(item.subject)
                    },
            )
        }
    }
}

/**
 * 已加载的前 [max] 项. 横向卡片与文字格只显示这些, 其余从「查看全部」查看.
 */
private fun <T : Any> LazyPagingItems<T>.loadedItems(max: Int): List<T> =
    (0 until minOf(itemCount, max)).mapNotNull { peek(it) }

/**
 * 人物与角色共用的骨架: 工具栏 (「在 Bangumi 開啟」与分享), 其下为 240 宽的左栏 (立绘、统计卡、基本资讯) 与右栏 [content].
 * 两栏各自滚动. [content] 的参数为打开全部评论.
 */
@Composable
private fun MacPeopleDetailsScaffold(
    bangumiUrl: String,
    imageUrl: String?,
    stats: List<PeopleStat>,
    infobox: List<InfoboxRowInfo>,
    comments: PeopleCommentsState,
    modifier: Modifier,
    content: @Composable ColumnScope.(onShowAllComments: () -> Unit) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val imageViewer = rememberImageViewerHandler()
    var showAllComments by rememberSaveable { mutableStateOf(false) }
    // 复用的现有区块有的文字不指定颜色, 由内容色决定.
    CompositionLocalProvider(LocalContentColor provides colorScheme.onSurface) {
        Column(modifier.fillMaxSize().background(colorScheme.surfaceContainerLowest)) {
            MacPageToolbar {
                val uriHandler = LocalUriHandler.current
                MacToolbarIconButton(
                    { uriHandler.openUri(bangumiUrl) },
                    AppleIcons.macExternalLink(),
                    stringResource(Lang.subject_details_open_in_bangumi),
                    Modifier.testTag(MacPeopleDetailsTestTags.OpenInBangumi),
                )
                var shareExpanded by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()
                Box {
                    MacToolbarIconButton(
                        { shareExpanded = true },
                        AppleIcons.share(),
                        stringResource(Lang.subject_details_share),
                        Modifier.testTag(MacPeopleDetailsTestTags.Share),
                    )
                    ApplePopoverMenu(
                        shareExpanded,
                        onDismissRequest = { shareExpanded = false },
                        Modifier.testTag(MacPeopleDetailsTestTags.ShareMenu),
                    ) {
                        ShareLinkMenuItems(bangumiUrl, scope, onDismissRequest = { shareExpanded = false })
                    }
                }
            }
            Row(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = MacPageDefaults.HorizontalPadding),
                horizontalArrangement = Arrangement.spacedBy(MacPeopleDetailsDefaults.ColumnSpacing),
            ) {
                Column(
                    Modifier
                        .width(MacPeopleDetailsDefaults.SidebarWidth)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 4.dp, bottom = 32.dp)
                        .testTag(MacPeopleDetailsTestTags.Sidebar),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    MacPortrait(imageUrl, onClick = imageUrl?.takeIf { it.isNotBlank() }?.let { url -> { imageViewer.viewImage(url) } })
                    MacPeopleStatsCard(stats)
                    if (infobox.isNotEmpty()) {
                        MacInfoSection(infobox)
                    }
                }
                Column(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 4.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(MacPeopleDetailsDefaults.SectionSpacing),
                ) {
                    content { showAllComments = true }
                }
            }
        }
    }
    ImageViewer(imageViewer) { imageViewer.clear() }
    PeopleCommentsHost(comments, showAllComments, onDismissAllComments = { showAllComments = false })
}

/**
 * 立绘: 3:4、圆角 12、阴影同条目封面; 有大图时点击打开图片查看器.
 */
@Composable
private fun MacPortrait(imageUrl: String?, onClick: (() -> Unit)?) {
    val shape = RoundedCornerShape(12.dp)
    AsyncImage(
        imageUrl,
        contentDescription = null,
        Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .dropShadow(shape, Shadow(radius = 20.dp, color = Color.Black.copy(alpha = 0.12f), offset = DpOffset(0.dp, 8.dp)))
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .testTag(MacPeopleDetailsTestTags.Portrait),
        // 立绘顶部为脸部, 顶部对齐
        alignment = Alignment.TopCenter,
        contentScale = ContentScale.Crop,
    )
}

/**
 * 统计卡: card 圆角 12, 3 栏, 数值 17/700, 标签 11 label2. 同条目详情.
 */
@Composable
private fun MacPeopleStatsCard(stats: List<PeopleStat>) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .testTag(MacPeopleDetailsTestTags.Stats),
    ) {
        for (stat in stats) {
            MacStatCell(
                stat.value?.let { groupThousands(it) } ?: "–",
                stringResource(stat.label),
                Modifier.weight(1f),
            )
        }
    }
}

/**
 * 基本资讯: 小节标题 13/600 + card 圆角 12 的定义列表 (行高 34, 左右内距 12, 键 52 宽 12 label2, 值 13).
 */
@Composable
private fun MacInfoSection(rows: List<InfoboxRowInfo>) {
    val colorScheme = MaterialTheme.colorScheme
    Column(Modifier.testTag(MacPeopleDetailsTestTags.Info), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(Lang.person_details_basic_info),
            style = AniTextStyles.macCallout.copy(fontWeight = FontWeight.SemiBold),
            color = colorScheme.onSurface,
        )
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.surfaceContainerLow),
        ) {
            rows.forEachIndexed { index, row ->
                if (index > 0) AppleDivider(startIndent = 12.dp)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 34.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        row.key,
                        Modifier.width(52.dp).alignByBaseline(),
                        style = AniTextStyles.macCaption,
                        color = colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        row.value,
                        Modifier.weight(1f).alignByBaseline(),
                        style = AniTextStyles.macCallout,
                        color = colorScheme.onSurface,
                    )
                }
            }
        }
    }
}

/**
 * 标题: 大标题 + 一行 meta 13 label2. [meta] 为 `null` 时为加载中.
 */
@Composable
private fun MacPeopleTitle(name: String, meta: String?) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            name,
            Modifier
                .placeholder(meta == null)
                .semantics { heading() },
            style = AniTextStyles.macLargeTitle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            meta.orEmpty(),
            Modifier.placeholder(meta == null).testTag(MacPeopleDetailsTestTags.Meta),
            style = AniTextStyles.macCallout,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * 简介: 区块标题与条目详情同款的简介 (14 号 labelSoft, 默认 5 行, 溢出时可展开).
 */
@Composable
private fun MacSummarySection(summary: String) {
    Column(Modifier.testTag(MacPeopleDetailsTestTags.Summary), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MacSectionHeader(stringResource(Lang.subject_details_summary))
        MacSummary(summary)
    }
}

/**
 * 区块: 标题行 (区块标题, 右侧 accent「查看全部 N 個」) 与内容相隔 12. [totalCount] 未知时为「查看全部」.
 */
@Composable
private fun MacGridSection(
    key: String,
    title: String,
    totalCount: Int?,
    onViewAll: () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(Modifier.testTag(MacPeopleDetailsTestTags.section(key)), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MacSectionHeader(title) {
            Spacer(Modifier.weight(1f))
            AppleTextButton(onViewAll, Modifier.testTag(MacPeopleDetailsTestTags.viewAll(key))) {
                Text(
                    if (totalCount != null && totalCount > 0) {
                        stringResource(Lang.person_details_view_all_count, totalCount)
                    } else {
                        stringResource(Lang.subject_details_view_all)
                    },
                )
            }
        }
        content()
    }
}

/**
 * 5 栏的卡片格, 间距 16; 不足一行时用空位补齐, 卡宽一致.
 */
@Composable
private fun <T> MacCardGrid(items: List<T>, card: @Composable (T) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        for (row in items.chunked(MacPeopleDetailsDefaults.CardColumns)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (item in row) {
                    Box(Modifier.weight(1f)) { card(item) }
                }
                repeat(MacPeopleDetailsDefaults.CardColumns - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 卡片: 3:4 封面 (圆角 10) + 名字 13/600 + 说明 12 label2. [badge] 不为 `null` 时在封面左上角显示标记 (例如「主角」).
 */
@Composable
private fun MacPeopleCard(
    imageUrl: String?,
    name: String,
    caption: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    Column(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box {
            AsyncImage(
                imageUrl,
                contentDescription = null,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                alignment = Alignment.TopCenter,
                contentScale = ContentScale.Crop,
            )
            if (badge != null) {
                Text(
                    badge,
                    Modifier
                        .padding(6.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(AniColors.accentFill)
                        .padding(horizontal = 6.dp, vertical = 1.dp)
                        .testTag(MacPeopleDetailsTestTags.Badge),
                    style = AniTextStyles.macCaption.copy(fontSize = 11.sp, lineHeight = 14.sp),
                    fontWeight = FontWeight.SemiBold,
                    color = AniColors.onAccentFill,
                    maxLines = 1,
                )
            }
        }
        Text(
            name,
            Modifier.padding(top = 2.dp),
            style = AniTextStyles.macCallout,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (caption != null) {
            Text(
                caption,
                style = AniTextStyles.macCaption,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 参与作品: card 圆角 12 的 3 栏文字格 (内距 10/14, 作品名 13/600 + 职位 12 label2), 不放缩图.
 */
@Composable
private fun MacWorksGrid(works: List<PersonWorkInfo>, onClick: (PersonSubjectSummary) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceContainerLow),
    ) {
        for (row in works.chunked(MacPeopleDetailsDefaults.WorkColumns)) {
            Row(Modifier.fillMaxWidth()) {
                for (work in row) {
                    Column(
                        Modifier
                            .weight(1f)
                            .clickable { onClick(work.subject) }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag(MacPeopleDetailsTestTags.work(work.subject.subjectId)),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            work.subject.displayName,
                            style = AniTextStyles.macCallout.copy(fontWeight = FontWeight.SemiBold),
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            work.positions.mapNotNull { it.nameCn }.distinct().joinToString("、"),
                            style = AniTextStyles.macCaption,
                            color = colorScheme.secondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                repeat(MacPeopleDetailsDefaults.WorkColumns - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * 声优 (角色页): card 列表, 每行 40 圆形头像 + 名字 14/600 + 箭头; 分隔线从 64 开始.
 */
@Composable
private fun MacVoiceActorsSection(actors: List<PersonInfo>, onClick: (PersonInfo) -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Column(Modifier.testTag(MacPeopleDetailsTestTags.VoiceActors), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MacSectionHeader(stringResource(Lang.person_details_voice_actors))
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.surfaceContainerLow),
        ) {
            actors.forEachIndexed { index, actor ->
                Box {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onClick(actor) }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .testTag(MacPeopleDetailsTestTags.voiceActor(actor.id)),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AvatarImage(
                            actor.imageMedium.ifBlank { null },
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colorScheme.surfaceContainerHighest),
                            alignment = Alignment.TopCenter,
                        )
                        Text(
                            actor.displayName,
                            Modifier.weight(1f),
                            style = AniTextStyles.macBody,
                            fontWeight = FontWeight.SemiBold,
                            color = colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(AppleIcons.chevronRight(), contentDescription = null, Modifier.size(14.dp), tint = colorScheme.secondary)
                    }
                    if (index > 0) {
                        AppleDivider(Modifier.align(Alignment.TopStart), startIndent = 64.dp)
                    }
                }
            }
        }
    }
}

/**
 * 评论: 标题行右侧 accent「N 条」打开现有的全部评论; 两栏卡片, 最多 4 条. 没有评论时显示现有的「还没有评论」.
 */
@Composable
private fun MacCommentsSection(state: CommentState, onShowAll: () -> Unit) {
    val comments = state.list.collectAsLazyPagingItemsWithLifecycle()
    Column(Modifier.testTag(MacPeopleDetailsTestTags.Comments), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MacSectionHeader(stringResource(Lang.person_details_comments)) {
            Spacer(Modifier.weight(1f))
            AppleTextButton(onShowAll, Modifier.testTag(MacPeopleDetailsTestTags.AllComments)) {
                Text(
                    state.count?.takeIf { it > 0 }
                        ?.let { stringResource(Lang.person_details_comments_count, remember(it) { groupThousands(it) }) }
                        ?: stringResource(Lang.subject_details_view_all),
                )
            }
        }
        val preview = comments.loadedItems(MacPeopleDetailsDefaults.MaxComments)
        if (preview.isEmpty()) {
            Text(
                stringResource(Lang.person_details_no_comments),
                style = AniTextStyles.macBody,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
        for (row in preview.chunked(2)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                for (comment in row) {
                    MacCommentCard(comment, onClick = onShowAll, Modifier.weight(1f))
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun MacCommentCard(comment: UIComment, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    Column(
        modifier
            .clip(RoundedCornerShape(12.dp))
            .background(colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag(MacPeopleDetailsTestTags.comment(comment.stableId)),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            CommentAvatar(comment, 20.dp)
            Text(
                comment.author?.nickname.orEmpty(),
                Modifier.weight(1f, fill = false),
                style = AniTextStyles.macCallout,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                formatDateTime(comment.createdAt, showTime = false),
                style = AniTextStyles.macCaption,
                color = colorScheme.secondary,
                maxLines = 1,
            )
        }
        Text(
            remember(comment) { comment.content.toPlainText() },
            style = AniTextStyles.macCallout,
            color = AniColors.labelSoft,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

internal object MacPeopleDetailsDefaults {
    val SidebarWidth = 240.dp
    val ColumnSpacing = 32.dp
    val SectionSpacing = 28.dp
    const val CardColumns = 5

    /** 出演角色与出演作品最多 2 行. */
    const val MaxCards = 10
    const val WorkColumns = 3

    /** 参与作品最多 3 行, 同条目详情的制作人员. */
    const val MaxWorks = 9

    /** 评论两栏最多 2 行. */
    const val MaxComments = 4
}

internal object MacPeopleDetailsTestTags {
    const val OpenInBangumi = "mac-people-details-open-in-bangumi"
    const val Share = "mac-people-details-share"
    const val ShareMenu = "mac-people-details-share-menu"
    const val Sidebar = "mac-people-details-sidebar"
    const val Portrait = "mac-people-details-portrait"
    const val Stats = "mac-people-details-stats"
    const val Info = "mac-people-details-info"
    const val Summary = "mac-people-details-summary"
    const val Meta = "mac-people-details-meta"
    const val VoiceActors = "mac-people-details-voice-actors"
    const val Comments = "mac-people-details-comments"
    const val AllComments = "mac-people-details-all-comments"
    const val Badge = "mac-people-details-badge"
    fun section(key: String) = "mac-people-details-section-$key"
    fun viewAll(key: String) = "mac-people-details-view-all-$key"
    fun cast(characterId: Int) = "mac-people-details-cast-$characterId"
    fun work(subjectId: Int) = "mac-people-details-work-$subjectId"
    fun voiceActor(personId: Int) = "mac-people-details-voice-actor-$personId"
    fun comment(stableId: String) = "mac-people-details-comment-$stableId"
}
