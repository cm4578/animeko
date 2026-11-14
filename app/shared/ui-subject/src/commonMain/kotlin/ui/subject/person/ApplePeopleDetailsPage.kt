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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItemsWithLifecycle
import com.kmpalette.rememberPaletteState
import me.him188.ani.app.data.models.person.CharacterDetailsInfo
import me.him188.ani.app.data.models.person.CharacterSubjectInfo
import me.him188.ani.app.data.models.person.InfoboxRowInfo
import me.him188.ani.app.data.models.person.PersonCastInfo
import me.him188.ani.app.data.models.person.PersonDetailsInfo
import me.him188.ani.app.data.models.person.PersonWorkInfo
import me.him188.ani.app.data.models.subject.PersonInfo
import me.him188.ani.app.data.models.subject.nameCn
import me.him188.ani.app.tools.formatDateTime
import me.him188.ani.app.ui.comment.CommentState
import me.him188.ani.app.ui.comment.UIComment
import me.him188.ani.app.ui.external.placeholder.placeholder
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.ImageViewer
import me.him188.ani.app.ui.foundation.ImageViewerBackHandler
import me.him188.ani.app.ui.foundation.apple.AppleCircleButton
import me.him188.ani.app.ui.foundation.apple.AppleCircleButtonStyle
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.ApplePopoverMenu
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.CollapsedTopBar
import me.him188.ani.app.ui.foundation.apple.OverlayTopBar
import me.him188.ani.app.ui.foundation.apple.ShareLinkMenuItems
import me.him188.ani.app.ui.foundation.apple.bangumiCharacterUrl
import me.him188.ani.app.ui.foundation.apple.bangumiPersonUrl
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
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
import me.him188.ani.app.ui.lang.person_details_voice_actors
import me.him188.ani.app.ui.lang.person_details_works
import me.him188.ani.app.ui.lang.subject_details_share
import me.him188.ani.app.ui.lang.subject_details_summary
import me.him188.ani.app.ui.lang.subject_details_view_all
import me.him188.ani.app.ui.subject.details.StatCell
import me.him188.ani.app.ui.subject.details.StatDivider
import me.him188.ani.app.ui.subject.details.components.PersonCard
import me.him188.ani.app.ui.subject.details.layout.AppleSectionTitle
import me.him188.ani.app.ui.subject.details.layout.AppleSummary
import me.him188.ani.app.ui.subject.details.sections.ViewAllSheet
import me.him188.ani.app.ui.subject.details.sections.groupThousands
import me.him188.ani.app.ui.subject.details.sections.toPlainText
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版人物详情 (03 §4.5). iOS 推入页.
 *
 * 头图 (立绘主色) 上为返回与分享 (05 G6); 立绘与名字、「职业 · N 人收藏」; 参与作品 / 出演角色 / 评论统计卡;
 * 出演角色与参与作品横向卡片; 简介; 基本资讯; 评论. 头部滚出后顶部收为精简栏.
 *
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun ApplePersonDetailsScreen(
    vm: PersonDetailsViewModel,
    personId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val details by vm.details.collectAsStateWithLifecycle()
    ApplePersonDetailsLayout(
        details,
        personId,
        vm.castsPager.collectAsLazyPagingItems(),
        vm.worksPager.collectAsLazyPagingItems(),
        vm.comments,
        onBack = onBack,
        modifier = modifier,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
    )
}

/**
 * Apple 版角色详情 (03 §4.5). 版式同 [ApplePersonDetailsScreen]: 名字下另有日文名; 统计为出演作品 / 声优 / 评论;
 * 声优为列表, 出演作品为横向卡片.
 */
@Composable
fun AppleCharacterDetailsScreen(
    vm: CharacterDetailsViewModel,
    characterId: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
) {
    val details by vm.details.collectAsStateWithLifecycle()
    AppleCharacterDetailsLayout(
        details,
        characterId,
        vm.subjectsPager.collectAsLazyPagingItems(),
        vm.comments,
        onBack = onBack,
        modifier = modifier,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
    )
}

/**
 * [ApplePersonDetailsScreen] 的版面. [details] 为 `null` 时为加载中.
 */
@Composable
internal fun ApplePersonDetailsLayout(
    details: PersonDetailsInfo?,
    personId: Int,
    casts: LazyPagingItems<PersonCastInfo>,
    works: LazyPagingItems<PersonWorkInfo>,
    comments: PeopleCommentsState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
    navigation: PeopleDetailsNavigation = rememberPeopleDetailsNavigation(),
) {
    var showAllCasts by rememberSaveable { mutableStateOf(false) }
    var showAllWorks by rememberSaveable { mutableStateOf(false) }
    ApplePeopleDetailsScaffold(
        name = details?.person?.displayName.orEmpty(),
        secondaryName = null,
        metaLine = details?.let { peopleMetaLine(personKindLabel(it.career), it.collects) },
        imageUrl = details?.person?.imageLarge,
        stats = listOf(
            PeopleStat(details?.workCount, Lang.person_details_works),
            PeopleStat(details?.castCount, Lang.person_details_casts),
            PeopleStat(details?.commentCount, Lang.person_details_comments),
        ),
        summary = details?.person?.summary.orEmpty(),
        infobox = details?.infobox.orEmpty(),
        comments = comments,
        shareUrl = bangumiPersonUrl(personId),
        onBack = onBack,
        modifier = modifier,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
    ) {
        cardStrip(
            "casts",
            title = { stringResource(Lang.person_details_casts) },
            items = casts,
            onViewAll = { showAllCasts = true },
        ) { cast ->
            PeopleCard(
                imageUrl = cast.character.imageMedium,
                name = cast.character.displayName,
                caption = cast.subject.displayName,
                onClick = { navigation.onClickCharacter(cast.character.id) },
                Modifier.testTag(ApplePeopleDetailsTestTags.cast(cast.character.id)),
            )
        }
        cardStrip(
            "works",
            title = { stringResource(Lang.person_details_works) },
            items = works,
            onViewAll = { showAllWorks = true },
        ) { work ->
            PeopleCard(
                imageUrl = work.subject.imageLarge,
                name = work.subject.displayName,
                caption = work.positions.firstNotNullOfOrNull { it.nameCn },
                onClick = { navigation.onClickSubject(work.subject) },
                Modifier.testTag(ApplePeopleDetailsTestTags.work(work.subject.subjectId)),
            )
        }
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
 * [AppleCharacterDetailsScreen] 的版面. [details] 为 `null` 时为加载中.
 */
@Composable
internal fun AppleCharacterDetailsLayout(
    details: CharacterDetailsInfo?,
    characterId: Int,
    subjects: LazyPagingItems<CharacterSubjectInfo>,
    comments: PeopleCommentsState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    contentPadding: PaddingValues = PaddingValues(),
    navigation: PeopleDetailsNavigation = rememberPeopleDetailsNavigation(),
) {
    var showAllSubjects by rememberSaveable { mutableStateOf(false) }
    val character = details?.character
    val actors = character?.actors.orEmpty()
    ApplePeopleDetailsScaffold(
        name = character?.displayName.orEmpty(),
        secondaryName = character?.name?.takeIf { it.isNotBlank() && it != character.displayName },
        metaLine = details?.let { peopleMetaLine(characterRoleLabel(it.role), it.collects) },
        imageUrl = character?.imageLarge,
        stats = listOf(
            PeopleStat(details?.subjectCount, Lang.person_details_character_subjects),
            PeopleStat(details?.let { actors.size }, Lang.person_details_voice_actors),
            PeopleStat(details?.commentCount, Lang.person_details_comments),
        ),
        summary = details?.summary.orEmpty(),
        infobox = details?.infobox.orEmpty(),
        comments = comments,
        shareUrl = bangumiCharacterUrl(characterId),
        onBack = onBack,
        modifier = modifier,
        windowInsets = windowInsets,
        contentPadding = contentPadding,
    ) {
        if (actors.isNotEmpty()) {
            item(key = "voice-actors") {
                VoiceActorsSection(actors, onClick = { navigation.onClickPerson(it.id) })
            }
        }
        cardStrip(
            "subjects",
            title = { stringResource(Lang.person_details_character_subjects) },
            items = subjects,
            onViewAll = { showAllSubjects = true },
        ) { item ->
            PeopleCard(
                imageUrl = item.subject.imageLarge,
                name = item.subject.displayName,
                caption = item.role.nameCn,
                onClick = { navigation.onClickSubject(item.subject) },
                Modifier.testTag(ApplePeopleDetailsTestTags.work(item.subject.subjectId)),
            )
        }
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
 * 统计卡的一栏. [value] 为 `null` 时 (加载中) 显示「–」.
 */
@Immutable
internal class PeopleStat(val value: Int?, val label: StringResource)

/**
 * 人物与角色详情共用的骨架: 头图与名字、统计卡、[sections] (各页的横向卡片与声优)、简介、基本资讯、评论.
 * 顶部为叠在头图上的返回与分享, 头部滚出后换成精简栏 (返回、居中名字、分享).
 */
@Composable
private fun ApplePeopleDetailsScaffold(
    name: String,
    secondaryName: String?,
    metaLine: String?,
    imageUrl: String?,
    stats: List<PeopleStat>,
    summary: String,
    infobox: List<InfoboxRowInfo>,
    comments: PeopleCommentsState,
    shareUrl: String,
    onBack: () -> Unit,
    modifier: Modifier,
    windowInsets: WindowInsets,
    contentPadding: PaddingValues,
    sections: LazyListScope.() -> Unit,
) {
    val imageViewer = rememberImageViewerHandler()
    ImageViewerBackHandler(imageViewer)
    var showAllComments by rememberSaveable { mutableStateOf(false) }

    // 头图用立绘的主色 (优先取鲜艳色), 同条目详情.
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val paletteState = rememberPaletteState()
    LaunchedEffect(bitmap) {
        bitmap?.let { paletteState.generate(it) }
    }
    val heroColor = paletteState.palette?.let { it.vibrantSwatch ?: it.dominantSwatch }?.rgb?.let { Color(it) }

    val topInset = windowInsets.asPaddingValues().calculateTopPadding()
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    // 头图 (安全区 + 178) 的下缘滚到精简栏 (安全区 + 44 + 1) 下缘时换成精简栏.
    val collapseOffsetPx = with(density) { (HeroHeight - CollapsedBarHeight).roundToPx() }
    val collapsed by remember(listState, collapseOffsetPx) {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > collapseOffsetPx
        }
    }

    // 复用的现有区块有的文字不指定颜色, 由内容色决定.
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            LazyColumn(
                Modifier.fillMaxSize().testTag(ApplePeopleDetailsTestTags.List),
                state = listState,
                contentPadding = PaddingValues(bottom = contentPadding.calculateBottomPadding() + 24.dp),
            ) {
                item(key = "header") {
                    PeopleHeroHeader(
                        name = name,
                        secondaryName = secondaryName,
                        metaLine = metaLine,
                        imageUrl = imageUrl,
                        heroColor = heroColor,
                        topInset = topInset,
                        onImageLoaded = { bitmap = it },
                        onClickImage = imageUrl?.takeIf { it.isNotBlank() }?.let { url -> { imageViewer.viewImage(url) } },
                    )
                }
                item(key = "stats") {
                    PeopleStatsCard(stats, Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp))
                }
                sections()
                if (summary.isNotBlank()) {
                    item(key = "summary") {
                        Section(stringResource(Lang.subject_details_summary)) {
                            AppleSummary(summary, Modifier.padding(horizontal = 16.dp), collapsedLines = 3)
                        }
                    }
                }
                if (infobox.isNotEmpty()) {
                    item(key = "info") {
                        Section(stringResource(Lang.person_details_basic_info)) {
                            InfoCard(infobox)
                        }
                    }
                }
                item(key = "comments") {
                    CommentsSection(comments.commentState, onShowAll = { showAllComments = true })
                }
            }

            if (collapsed) {
                CollapsedTopBar(name, onBack, Modifier.testTag(ApplePeopleDetailsTestTags.CollapsedBar), windowInsets = windowInsets) {
                    ShareButton(shareUrl, AppleCircleButtonStyle.Fill)
                }
            } else {
                OverlayTopBar(onBack, windowInsets = windowInsets) {
                    ShareButton(shareUrl, AppleCircleButtonStyle.Media)
                }
            }
        }
    }

    ImageViewer(imageViewer) { imageViewer.clear() }
    PeopleCommentsHost(comments, showAllComments, onDismissAllComments = { showAllComments = false })
}

/** 头图高度 (不含安全区). */
private val HeroHeight: Dp = 178.dp

/** 精简栏高度 (不含安全区): 44 + 1 分隔线. */
private val CollapsedBarHeight: Dp = 45.dp

/**
 * 头图 (安全区 + 178, 立绘主色) 与立绘 (96×128, 圆角 12, 3 bg 描边与阴影)、名字 `22/28 800`、日文名与 meta 行 `footnote` label2.
 * [metaLine] 为 `null` 时为加载中.
 */
@Composable
private fun PeopleHeroHeader(
    name: String,
    secondaryName: String?,
    metaLine: String?,
    imageUrl: String?,
    heroColor: Color?,
    topInset: Dp,
    onImageLoaded: (ImageBitmap) -> Unit,
    onClickImage: (() -> Unit)?,
) {
    val colorScheme = MaterialTheme.colorScheme
    val loading = metaLine == null
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(topInset + HeroHeight)
                .background(heroColor ?: colorScheme.surfaceContainerHighest),
        )
        Row(
            Modifier.padding(start = 16.dp, end = 16.dp, top = topInset + 122.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            val outerShape = RoundedCornerShape(15.dp)
            Box(
                Modifier
                    .size(102.dp, 134.dp)
                    .dropShadow(outerShape, Shadow(radius = 20.dp, color = Color.Black.copy(alpha = 0.18f), offset = DpOffset(0.dp, 8.dp)))
                    .background(colorScheme.background, outerShape)
                    .padding(3.dp),
            ) {
                AsyncImage(
                    imageUrl,
                    contentDescription = null,
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.surfaceContainerHighest)
                        .then(if (onClickImage != null) Modifier.clickable(onClick = onClickImage) else Modifier)
                        .testTag(ApplePeopleDetailsTestTags.Portrait),
                    onSuccess = { success -> success.bitmap?.let(onImageLoaded) },
                    // 立绘顶部为脸部, 顶部对齐
                    alignment = Alignment.TopCenter,
                    contentScale = ContentScale.Crop,
                )
            }
            Column(
                Modifier.weight(1f).padding(bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    name,
                    Modifier
                        .placeholder(loading)
                        .semantics { heading() },
                    style = AniTextStyles.title.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold),
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (secondaryName != null) {
                    Text(
                        secondaryName,
                        style = AniTextStyles.footnote,
                        color = colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    metaLine.orEmpty(),
                    Modifier.placeholder(loading).testTag(ApplePeopleDetailsTestTags.Meta),
                    style = AniTextStyles.footnote,
                    color = colorScheme.secondary,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * 统计卡: card 圆角 16, 3 栏, 栏间 1 竖线; 数字 `20/26 700`, 标签 `caption` label2. 同条目详情.
 */
@Composable
private fun PeopleStatsCard(stats: List<PeopleStat>, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .height(IntrinsicSize.Min)
            .testTag(ApplePeopleDetailsTestTags.Stats),
    ) {
        stats.forEachIndexed { index, stat ->
            if (index > 0) StatDivider()
            StatCell(
                stat.value?.let { groupThousands(it) } ?: "–",
                stringResource(stat.label),
                Modifier.weight(1f),
            )
        }
    }
}

/**
 * 区块: 与上方相隔 24, 标题 (`title`, 右侧可放 accent 链接) 与内容相隔 12.
 */
@Composable
private fun Section(
    title: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Column(modifier.fillMaxWidth().padding(top = 24.dp)) {
        AppleSectionTitle(title, action = action)
        Spacer(Modifier.height(12.dp))
        content()
    }
}

/**
 * 横向卡片区块 (出演角色、参与作品、出演作品): 卡宽 100, 间距 10, 延伸到屏幕边缘; 标题行右侧「查看全部」.
 * 没有项目时不显示.
 */
private fun <T : Any> LazyListScope.cardStrip(
    key: String,
    title: @Composable () -> String,
    items: LazyPagingItems<T>,
    onViewAll: () -> Unit,
    card: @Composable (T) -> Unit,
) {
    if (items.itemCount == 0) return
    item(key = key) {
        Section(
            title(),
            Modifier.testTag(ApplePeopleDetailsTestTags.section(key)),
            action = {
                AppleTextButton(onViewAll, Modifier.testTag(ApplePeopleDetailsTestTags.viewAll(key))) {
                    Text(stringResource(Lang.subject_details_view_all))
                }
            },
        ) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(items.itemCount) { index ->
                    items[index]?.let { card(it) }
                }
            }
        }
    }
}

/**
 * 横向卡片: 封面 100×133 (3:4) 圆角 12; 名字 `subhead` W600; 说明 `caption` label2.
 */
@Composable
private fun PeopleCard(
    imageUrl: String?,
    name: String,
    caption: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .width(100.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        AsyncImage(
            imageUrl,
            contentDescription = null,
            Modifier
                .size(100.dp, 133.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
            alignment = Alignment.TopCenter,
            contentScale = ContentScale.Crop,
        )
        Text(
            name,
            Modifier.padding(top = 2.dp),
            style = AniTextStyles.subhead,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (caption != null) {
            Text(
                caption,
                style = AniTextStyles.caption,
                color = MaterialTheme.colorScheme.secondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 声优 (角色页): C5, 每列内距 `10 12 10 16`, 40 圆形头像、名字 `body` W500、「›」; 分隔线从 68 开始.
 */
@Composable
private fun VoiceActorsSection(actors: List<PersonInfo>, onClick: (PersonInfo) -> Unit) {
    Section(stringResource(Lang.person_details_voice_actors), Modifier.testTag(ApplePeopleDetailsTestTags.VoiceActors)) {
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            actors.forEachIndexed { index, actor ->
                Box {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onClick(actor) }
                            .padding(start = 16.dp, top = 10.dp, end = 12.dp, bottom = 10.dp)
                            .testTag(ApplePeopleDetailsTestTags.voiceActor(actor.id)),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        AvatarImage(
                            actor.imageMedium.ifBlank { null },
                            Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                            alignment = Alignment.TopCenter,
                        )
                        Text(
                            actor.displayName,
                            Modifier.weight(1f),
                            style = AniTextStyles.body,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Icon(
                            AppleIcons.chevronRight(),
                            contentDescription = null,
                            Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.secondary,
                        )
                    }
                    if (index > 0) {
                        AppleDivider(Modifier.align(Alignment.TopStart), startIndent = 68.dp)
                    }
                }
            }
        }
    }
}

/**
 * 基本资讯: C5 键值列, 高 44; 键宽 78 `callout` label2, 值 `callout`.
 */
@Composable
private fun InfoCard(rows: List<InfoboxRowInfo>) {
    Column(
        Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .testTag(ApplePeopleDetailsTestTags.Info),
    ) {
        rows.forEachIndexed { index, row ->
            if (index > 0) AppleDivider(startIndent = 16.dp)
            Row(
                Modifier
                    .fillMaxWidth()
                    .heightIn(min = 44.dp)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    row.key,
                    Modifier.width(78.dp),
                    style = AniTextStyles.callout,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(row.value, Modifier.weight(1f), style = AniTextStyles.callout, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

/**
 * 评论: 标题与「N 条」accent (打开现有的全部评论); C5 列表为前 3 条, 每列为 28 圆形头像、名字 `subhead` W600、
 * 时间 `caption` label2 与正文 `15/22` labelSoft (最多 3 行). 没有评论时显示现有的「还没有评论」.
 */
@Composable
private fun CommentsSection(state: CommentState, onShowAll: () -> Unit) {
    val comments = state.list.collectAsLazyPagingItemsWithLifecycle()
    Section(
        stringResource(Lang.person_details_comments),
        Modifier.testTag(ApplePeopleDetailsTestTags.Comments),
        action = {
            AppleTextButton(onShowAll, Modifier.testTag(ApplePeopleDetailsTestTags.AllComments)) {
                Text(
                    state.count?.takeIf { it > 0 }
                        ?.let { stringResource(Lang.person_details_comments_count, remember(it) { groupThousands(it) }) }
                        ?: stringResource(Lang.subject_details_view_all),
                )
            }
        },
    ) {
        val previewCount = minOf(comments.itemCount, PreviewComments)
        if (previewCount == 0) {
            Text(
                stringResource(Lang.person_details_no_comments),
                Modifier.padding(horizontal = 20.dp),
                style = AniTextStyles.callout,
                color = MaterialTheme.colorScheme.secondary,
            )
            return@Section
        }
        Column(
            Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
        ) {
            for (index in 0 until previewCount) {
                val comment = comments[index] ?: continue
                Box {
                    CommentRow(comment, onClick = onShowAll)
                    if (index > 0) {
                        AppleDivider(Modifier.align(Alignment.TopStart), startIndent = 54.dp)
                    }
                }
            }
        }
    }
}

/** 评论预览的条数. */
private const val PreviewComments = 3

@Composable
private fun CommentRow(comment: UIComment, onClick: () -> Unit) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
            .testTag(ApplePeopleDetailsTestTags.comment(comment.stableId)),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        CommentAvatar(comment, 28.dp)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    comment.author?.nickname.orEmpty(),
                    Modifier.weight(1f, fill = false),
                    style = AniTextStyles.subhead,
                    fontWeight = FontWeight.SemiBold,
                    color = colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    formatDateTime(comment.createdAt, showTime = false),
                    style = AniTextStyles.caption,
                    color = colorScheme.secondary,
                    maxLines = 1,
                )
            }
            Text(
                remember(comment) { comment.content.toPlainText() },
                style = AniTextStyles.callout.copy(lineHeight = 22.sp),
                color = AniColors.labelSoft,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/**
 * 评论者头像: 有头像时显示头像, 没有时为 fill 底与名字的第一个字.
 */
@Composable
internal fun CommentAvatar(comment: UIComment, size: Dp, modifier: Modifier = Modifier) {
    val colorScheme = MaterialTheme.colorScheme
    val avatarUrl = comment.author?.avatarUrl?.takeIf { it.isNotBlank() }
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (avatarUrl != null) {
            AvatarImage(avatarUrl, Modifier.fillMaxSize())
        } else {
            Text(
                comment.author?.nickname?.firstOrNull()?.toString().orEmpty(),
                style = AniTextStyles.caption,
                fontWeight = FontWeight.SemiBold,
                color = colorScheme.secondary,
                maxLines = 1,
            )
        }
    }
}

/**
 * 分享 (05 G6): C15 菜单「复制链接」「在浏览器中打开」, 链接为 Bangumi 的人物页或角色页.
 */
@Composable
private fun ShareButton(url: String, style: AppleCircleButtonStyle) {
    var expanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    Box {
        AppleCircleButton(
            { expanded = true },
            AppleIcons.share(),
            stringResource(Lang.subject_details_share),
            Modifier.testTag(ApplePeopleDetailsTestTags.Share),
            style = style,
        )
        ApplePopoverMenu(expanded, onDismissRequest = { expanded = false }, Modifier.testTag(ApplePeopleDetailsTestTags.ShareMenu)) {
            ShareLinkMenuItems(url, scope, onDismissRequest = { expanded = false })
        }
    }
}

internal object ApplePeopleDetailsTestTags {
    const val List = "people-details-list"
    const val Portrait = "people-details-portrait"
    const val Meta = "people-details-meta"
    const val Stats = "people-details-stats"
    const val VoiceActors = "people-details-voice-actors"
    const val Info = "people-details-info"
    const val Comments = "people-details-comments"
    const val AllComments = "people-details-all-comments"
    const val Share = "people-details-share"
    const val ShareMenu = "people-details-share-menu"
    const val CollapsedBar = "people-details-collapsed-bar"
    fun section(key: String) = "people-details-section-$key"
    fun viewAll(key: String) = "people-details-view-all-$key"
    fun cast(characterId: Int) = "people-details-cast-$characterId"
    fun work(subjectId: Int) = "people-details-work-$subjectId"
    fun voiceActor(personId: Int) = "people-details-voice-actor-$personId"
    fun comment(stableId: String) = "people-details-comment-$stableId"
}
