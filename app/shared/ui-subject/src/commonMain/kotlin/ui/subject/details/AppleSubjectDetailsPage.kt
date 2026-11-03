/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.subject.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kmpalette.rememberPaletteState
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.models.subject.Tag
import me.him188.ani.app.data.models.subject.preferredDisplayName
import me.him188.ani.app.domain.episode.SetEpisodeCollectionTypeRequest
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.ui.comment.CommentReportHost
import me.him188.ani.app.ui.comment.UIComment
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.ImageViewer
import me.him188.ani.app.ui.foundation.ImageViewerBackHandler
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleButton
import me.him188.ani.app.ui.foundation.apple.AppleButtonStyle
import me.him188.ani.app.ui.foundation.apple.AppleCircleButton
import me.him188.ani.app.ui.foundation.apple.AppleCircleButtonStyle
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.ApplePopoverMenu
import me.him188.ani.app.ui.foundation.apple.CollapsedTopBar
import me.him188.ani.app.ui.foundation.apple.CollectionTypeMenuItems
import me.him188.ani.app.ui.foundation.apple.OverlayTopBar
import me.him188.ani.app.ui.foundation.apple.PillTab
import me.him188.ani.app.ui.foundation.apple.PopoverMenuItem
import me.him188.ani.app.ui.foundation.apple.UnfollowConfirmDialog
import me.him188.ani.app.ui.foundation.input.touchHorizontalScrollOnly
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.layout.NestedScrollableColumn
import me.him188.ani.app.ui.foundation.layout.NestedScrollableScope
import me.him188.ani.app.ui.foundation.layout.rememberNestedScrollableColumnState
import me.him188.ani.app.ui.foundation.rememberImageViewerHandler
import me.him188.ani.app.ui.foundation.setClipEntryText
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.theme.LocalThemeSettings
import me.him188.ani.app.ui.foundation.theme.MaterialThemeFromPaletteAndImage
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.showLoadError
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.foundation_richtext_external_app_link_warning_prefix
import me.him188.ani.app.ui.lang.foundation_richtext_open_failed_prefix
import me.him188.ani.app.ui.lang.subject_collection_collect
import me.him188.ani.app.ui.lang.subject_collection_delete
import me.him188.ani.app.ui.lang.subject_details_coming_soon
import me.him188.ani.app.ui.lang.subject_details_login_to_collect
import me.him188.ani.app.ui.lang.subject_details_manage_cache
import me.him188.ani.app.ui.lang.subject_details_rank
import me.him188.ani.app.ui.lang.subject_details_rating
import me.him188.ani.app.ui.lang.subject_details_share
import me.him188.ani.app.ui.lang.subject_details_share_copy_link
import me.him188.ani.app.ui.lang.subject_details_share_open_in_browser
import me.him188.ani.app.ui.lang.subject_details_stat_collected
import me.him188.ani.app.ui.lang.subject_details_write_review
import me.him188.ani.app.ui.rating.EditableRatingDialogsHost
import me.him188.ani.app.ui.richtext.RichTextDefaults
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.subject.AiringLabel
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeDialogsHost
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeState
import me.him188.ani.app.ui.subject.collection.components.renderCollectionTypeAsCurrent
import me.him188.ani.app.ui.subject.details.components.SubjectCommentColumn
import me.him188.ani.app.ui.subject.details.components.SubjectDetailsDefaults
import me.him188.ani.app.ui.subject.details.layout.AppleDetailsTabContent
import me.him188.ani.app.ui.subject.details.layout.AppleSubjectDetailsTestTags
import me.him188.ani.app.ui.subject.details.sections.groupThousands
import me.him188.ani.app.ui.subject.details.state.SubjectDetailsState
import me.him188.ani.app.ui.subject.details.state.SubjectDetailsUiState
import me.him188.ani.app.ui.subject.details.state.rememberAiringLabelState
import me.him188.ani.app.ui.subject.details.state.rememberSubjectProgressState
import me.him188.ani.app.ui.subject.episode.list.EpisodeListDialog
import me.him188.ani.app.ui.subject.episode.list.EpisodeListItem
import me.him188.ani.app.ui.subject.rememberSubjectStatusStrings
import me.him188.ani.app.ui.subject.renderSubjectSeason
import me.him188.ani.app.ui.user.SelfInfoUiState
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.api.topic.toggleCollected
import org.jetbrains.compose.resources.stringResource

/**
 * Apple 版条目详情 (03 §3.6). 手机推入页, 内容延伸到浮动导航栏下方.
 *
 * 头图为封面主色, 左上返回、右上分享 (05 G6); 海报与标题; 评分、排名、收藏统计卡; 继续观看、收藏状态、下载;
 * 详情、评价、讨论三个分页. 头部滚出后顶部收为精简栏, 分页胶囊固定在其下方.
 *
 * @param contentPadding 内容底部要留出浮动导航栏的空间.
 */
@Composable
fun AppleSubjectDetailsScreen(
    vm: SubjectDetailsViewModel,
    onPlay: (episodeId: Int) -> Unit,
    onLoadErrorRetry: () -> Unit,
    onClickTag: (Tag) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val state by vm.state.collectAsStateWithLifecycle()
    val selfInfo by vm.authState.collectAsStateWithLifecycle()
    val toaster = LocalToaster.current
    val scope = rememberCoroutineScope()

    // 每次进入组合只是「确保已加载」, 同 SubjectDetailsScreen.
    LaunchedEffect(Unit) {
        vm.load()
    }

    when (val current = state) {
        is SubjectDetailsLoadState.Ok -> AppleSubjectDetailsPage(
            current.value,
            selfInfo,
            onPlay = onPlay,
            onClickTag = onClickTag,
            onEpisodeCollectionUpdate = { request ->
                scope.launch {
                    vm.setEpisodeCollectionType.invokeSafe(request)?.let { toaster.showLoadError(it) }
                }
            },
            onBack = onBack,
            modifier = modifier,
            contentPadding = contentPadding,
        )

        is SubjectDetailsLoadState.Placeholder -> AppleSubjectDetailsLoading(
            current.subjectId,
            current.subjectInfo,
            error = null,
            onRetry = onLoadErrorRetry,
            onBack = onBack,
            modifier = modifier,
        )

        is SubjectDetailsLoadState.Err -> AppleSubjectDetailsLoading(
            current.subjectId,
            current.placeholder,
            error = current.error,
            onRetry = onLoadErrorRetry,
            onBack = onBack,
            modifier = modifier,
        )
    }
}

/**
 * 数据就绪的条目详情.
 */
@Composable
internal fun AppleSubjectDetailsPage(
    state: SubjectDetailsState,
    selfInfo: SelfInfoUiState,
    onPlay: (episodeId: Int) -> Unit,
    onClickTag: (Tag) -> Unit,
    onEpisodeCollectionUpdate: (SetEpisodeCollectionTypeRequest) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    val uiState by state.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    val toaster = LocalToaster.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val isLoggedIn = selfInfo.isSessionValid != false

    var showEpisodeList by rememberSaveable { mutableStateOf(false) }
    val imageViewer = rememberImageViewerHandler()
    ImageViewerBackHandler(imageViewer)

    val onEpisodeLongClick: (EpisodeListItem) -> Unit = {
        onEpisodeCollectionUpdate(
            SetEpisodeCollectionTypeRequest(uiState.subjectId, it.episodeId, it.collectionType.toggleCollected()),
        )
    }
    val externalAppLinkWarningPrefix = stringResource(Lang.foundation_richtext_external_app_link_warning_prefix)
    val openLinkFailedPrefix = stringResource(Lang.foundation_richtext_open_failed_prefix)
    val onClickCommentUrl = { url: String ->
        RichTextDefaults.checkSanityAndOpen(url, uriHandler, toaster, externalAppLinkWarningPrefix, openLinkFailedPrefix)
    }

    // 头图用封面主色 (优先取鲜艳色, 避免取到封面留白); 「条目详情页动态主题」开启时, 页面配色也跟随封面 (05 G17).
    val themeSettings = LocalThemeSettings.current
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val paletteState = rememberPaletteState()
    LaunchedEffect(bitmap) {
        bitmap?.let { paletteState.generate(it) }
    }
    val heroColor = paletteState.palette?.let { it.vibrantSwatch ?: it.dominantSwatch }?.rgb?.let { Color(it) }

    MaterialThemeFromPaletteAndImage(
        if (themeSettings.useDynamicSubjectPageTheme) paletteState.palette else null,
        if (themeSettings.useDynamicSubjectPageTheme) bitmap else null,
    ) {
        if (showEpisodeList) {
            EpisodeListDialog(
                uiState.episodeListUiState,
                onDismissRequest = { showEpisodeList = false },
                { navigator.navigateSubjectCaches(uiState.subjectId) },
                { navigator.navigateEpisodeDetails(uiState.subjectId, it.episodeId) },
                onEpisodeLongClick,
            )
        }
        state.subjectCommentReportState?.let { CommentReportHost(it) }
        EditableRatingDialogsHost(uiState.rating, state)
        EditableSubjectCollectionTypeDialogsHost(uiState.collectionTypeEdit, state)

        val pagerState = rememberPagerState(initialPage = SubjectDetailsTab.DETAILS.ordinal, pageCount = { 3 })
        AppleSubjectDetailsScaffold(
            subjectId = state.subjectId,
            info = state.info,
            heroColor = heroColor,
            onBack = onBack,
            onCoverLoaded = { bitmap = it },
            onClickCover = state.info?.imageLarge?.takeIf { it.isNotBlank() }?.let { url -> { imageViewer.viewImage(url) } },
            airingLabel = {
                AiringLabel(
                    uiState.rememberAiringLabelState(),
                    style = AniTextStyles.footnote,
                    progressColor = MaterialTheme.colorScheme.secondary,
                )
            },
            pagerState = pagerState,
            modifier = modifier,
            body = {
                StatsCard(
                    state.info,
                    uiState,
                    onClickRating = if (isLoggedIn) ({ state.requestEditRating() }) else null,
                )
                Spacer(Modifier.height(12.dp))
                ActionsRow(
                    uiState,
                    isLoggedIn = isLoggedIn,
                    onPlay = onPlay,
                    onSetCollectionType = { type ->
                        scope.launch { state.setSelfCollectionType(type)?.let { toaster.showLoadError(it) } }
                    },
                    onClickLogin = { navigator.navigateEmailLoginStart() },
                    onClickDownload = { navigator.navigateSubjectCaches(uiState.subjectId) },
                )
            },
        ) { page ->
            when (SubjectDetailsTab.entries[page]) {
                SubjectDetailsTab.DETAILS -> {
                    val info = state.info ?: return@AppleSubjectDetailsScaffold
                    AppleDetailsTabContent(
                        state,
                        info,
                        onPlay = onPlay,
                        onEpisodeLongClick = onEpisodeLongClick,
                        onClickTag = onClickTag,
                        onShowEpisodeList = { showEpisodeList = true },
                        modifier = Modifier.fillMaxSize().nestedScrollWorkaround(state.detailsTabLazyListState),
                        listState = state.detailsTabLazyListState,
                        contentPadding = contentPadding,
                    )
                }

                SubjectDetailsTab.COMMENTS -> Column(Modifier.fillMaxSize()) {
                    if (isLoggedIn) {
                        AppleButton(
                            { state.requestEditRating() },
                            Modifier.padding(horizontal = 16.dp).padding(top = 16.dp).fillMaxWidth(),
                            style = AppleButtonStyle.Tinted,
                        ) {
                            Text(stringResource(Lang.subject_details_write_review))
                        }
                    }
                    SubjectDetailsDefaults.SubjectCommentColumn(
                        state = state.subjectCommentState,
                        onClickUrl = onClickCommentUrl,
                        onClickImage = { imageViewer.viewImage(it) },
                        reportState = state.subjectCommentReportState,
                        onOpenOriginal = { _: UIComment -> uriHandler.openUri(bangumiSubjectUrl(uiState.subjectId)) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .nestedScrollWorkaround(state.commentTabLazyGridState),
                        gridState = state.commentTabLazyGridState,
                        contentPadding = contentPadding,
                    )
                }

                SubjectDetailsTab.DISCUSSIONS -> Box(Modifier.fillMaxSize().padding(top = 32.dp), Alignment.TopCenter) {
                    Text(
                        stringResource(Lang.subject_details_coming_soon),
                        style = AniTextStyles.callout,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                }
            }
        }
    }

    ImageViewer(imageViewer) { imageViewer.clear() }
}

/**
 * 加载中或加载失败: 头部用已知的条目信息 (可能为 `null`), 下方为加载指示或错误卡片.
 */
@Composable
private fun AppleSubjectDetailsLoading(
    subjectId: Int,
    info: SubjectInfo?,
    error: LoadError?,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppleSubjectDetailsScaffold(
        subjectId = subjectId,
        info = info,
        heroColor = null,
        onBack = onBack,
        onCoverLoaded = {},
        onClickCover = null,
        airingLabel = null,
        pagerState = null,
        modifier = modifier,
        body = {
            if (error != null) {
                LoadErrorCard(error, onRetry = onRetry, Modifier.fillMaxWidth())
            }
        },
    ) {}
    if (error == null) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    }
}

/**
 * 页面骨架: 头图、海报与标题、[body] (统计卡与操作), 分页胶囊与分页内容. 顶部为叠在头图上的返回与分享;
 * 头部滚出后换成 C3 精简栏, 分页胶囊固定在其下方.
 *
 * @param pagerState 为 `null` 时不显示分页 (加载中).
 */
@Composable
private fun AppleSubjectDetailsScaffold(
    subjectId: Int,
    info: SubjectInfo?,
    heroColor: Color?,
    onBack: () -> Unit,
    onCoverLoaded: (ImageBitmap) -> Unit,
    onClickCover: (() -> Unit)?,
    airingLabel: (@Composable () -> Unit)?,
    pagerState: PagerState?,
    modifier: Modifier = Modifier,
    windowInsets: WindowInsets = AniWindowInsets.forTopAppBar(),
    body: @Composable ColumnScope.() -> Unit,
    page: @Composable NestedScrollableScope.(index: Int) -> Unit,
) {
    val density = LocalDensity.current
    val topInset = windowInsets.asPaddingValues().calculateTopPadding()
    val nestedState = rememberNestedScrollableColumnState()
    var tabRowHeightPx by remember { mutableIntStateOf(0) }
    // 精简栏 = 安全区 + 44 + 1 分隔线. 分页胶囊滚到精简栏底部时换成固定的那一份.
    val anchorPx = rememberUpdatedState(with(density) { (topInset + 45.dp).roundToPx() })
    val stickyVisible by remember(nestedState) {
        derivedStateOf {
            nestedState.headerHeight > 0 && nestedState.scrolledOffset > 0f &&
                    nestedState.scrolledOffset >= nestedState.headerHeight - tabRowHeightPx - anchorPx.value
        }
    }
    val title = info?.preferredDisplayName(LocalSubjectAppearanceSettings.current.useOriginalTitle).orEmpty()

    // 复用的现有区块有的文字不指定颜色, 由内容色决定.
    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            NestedScrollableColumn(
                header = {
                    Column(Modifier.fillMaxWidth()) {
                        HeroHeader(info, title, heroColor, topInset, onCoverLoaded, onClickCover, airingLabel)
                        Column(Modifier.padding(horizontal = 16.dp).padding(top = 16.dp)) { body() }
                        if (pagerState != null) {
                            TabPills(
                                pagerState,
                                Modifier
                                    .padding(top = 20.dp)
                                    .onSizeChanged { tabRowHeightPx = it.height }
                                    .alpha(if (stickyVisible) 0f else 1f),
                            )
                        }
                    }
                },
                content = {
                    if (pagerState != null) {
                        HorizontalPager(
                            pagerState,
                            Modifier.fillMaxSize().touchHorizontalScrollOnly(),
                            verticalAlignment = Alignment.Top,
                        ) { index ->
                            page(index)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
                state = nestedState,
            )

            if (stickyVisible && pagerState != null) {
                Column(Modifier.align(Alignment.TopCenter).fillMaxWidth().testTag(AppleSubjectDetailsTestTags.CollapsedBar)) {
                    CollapsedTopBar(title, onBack, windowInsets = windowInsets) {
                        ShareButton(subjectId, AppleCircleButtonStyle.Fill)
                    }
                    TabPills(pagerState, Modifier.background(MaterialTheme.colorScheme.background))
                }
            } else {
                OverlayTopBar(onBack, windowInsets = windowInsets) {
                    ShareButton(subjectId, AppleCircleButtonStyle.Media)
                }
            }
        }
    }
}

/**
 * 头图 (安全区 + 178, 封面主色) 与海报 (96×128, 圆角 12, 3 bg 描边与阴影)、番名 `22/28 800`、原名与放送信息 `footnote` label2.
 */
@Composable
private fun HeroHeader(
    info: SubjectInfo?,
    title: String,
    heroColor: Color?,
    topInset: Dp,
    onCoverLoaded: (ImageBitmap) -> Unit,
    onClickCover: (() -> Unit)?,
    airingLabel: (@Composable () -> Unit)?,
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(Modifier.fillMaxWidth()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(topInset + 178.dp)
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
                    info?.imageLarge,
                    contentDescription = null,
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(colorScheme.surfaceContainerHighest)
                        .then(if (onClickCover != null) Modifier.clickable(onClick = onClickCover) else Modifier),
                    contentScale = ContentScale.Crop,
                    onSuccess = { success -> success.bitmap?.let(onCoverLoaded) },
                )
            }
            Column(
                Modifier.weight(1f).padding(bottom = 2.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    title,
                    style = AniTextStyles.title.copy(fontSize = 22.sp, lineHeight = 28.sp, fontWeight = FontWeight.ExtraBold),
                    color = colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                val originalName = info?.name?.takeIf { it.isNotBlank() && it != title }
                if (originalName != null) {
                    Text(
                        originalName,
                        style = AniTextStyles.footnote,
                        color = colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                if (info != null && info.airDate.isValid) {
                    CompositionLocalProvider(LocalContentColor provides colorScheme.secondary) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(renderSubjectSeason(info.airDate), style = AniTextStyles.footnote, maxLines = 1)
                            if (airingLabel != null) {
                                Text(" · ", style = AniTextStyles.footnote)
                                airingLabel()
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 统计卡: card 圆角 16, 评分 / 排名 / 收藏三栏, 栏间 1 竖线. 数字 `20/26 700`, 标签 `caption` label2.
 * 登录后点评分打开评分编辑.
 */
@Composable
private fun StatsCard(
    info: SubjectInfo?,
    uiState: SubjectDetailsUiState,
    onClickRating: (() -> Unit)?,
) {
    val rating = info?.ratingInfo
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .height(IntrinsicSize.Min),
    ) {
        StatCell(
            rating?.score?.takeIf { rating.scoreFloat > 0f } ?: "–",
            stringResource(Lang.subject_details_rating),
            Modifier
                .weight(1f)
                .then(if (onClickRating != null) Modifier.clickable(onClick = onClickRating) else Modifier)
                .testTag(AppleSubjectDetailsTestTags.Rating),
        )
        StatDivider()
        StatCell(
            rating?.rank?.takeIf { it > 0 }?.let { "#$it" } ?: "–",
            stringResource(Lang.subject_details_rank),
            Modifier.weight(1f),
        )
        StatDivider()
        StatCell(
            groupThousands(uiState.collectionStats.collect),
            stringResource(Lang.subject_details_stat_collected),
            Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier.padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            value,
            style = AniTextStyles.title.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        Text(label, style = AniTextStyles.caption, color = MaterialTheme.colorScheme.secondary, maxLines = 1)
    }
}

@Composable
private fun StatDivider() {
    Box(
        Modifier
            .padding(vertical = 12.dp)
            .width(1.dp)
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.outlineVariant),
    )
}

/**
 * 主要操作: 继续观看 (C8 主要, 高 48, 占满剩余宽)、收藏状态 (C8 淡底, 带下拉箭头, 打开 C15 菜单)、下载 48 圆钮.
 * 未登录时收藏状态换成「登录后可收藏」.
 */
@Composable
private fun ActionsRow(
    uiState: SubjectDetailsUiState,
    isLoggedIn: Boolean,
    onPlay: (episodeId: Int) -> Unit,
    onSetCollectionType: (UnifiedCollectionType) -> Unit,
    onClickLogin: () -> Unit,
    onClickDownload: () -> Unit,
) {
    val progressState = uiState.rememberSubjectProgressState()
    val strings = rememberSubjectStatusStrings()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        val episodeId = progressState.episodeIdToPlay
        AppleButton(
            { episodeId?.let(onPlay) },
            Modifier.weight(1f).testTag(AppleSubjectDetailsTestTags.Play),
            style = if (progressState.buttonIsPrimary) AppleButtonStyle.Primary else AppleButtonStyle.Tinted,
            enabled = episodeId != null,
            height = 48.dp,
        ) {
            Icon(AppleIcons.playFilled(), contentDescription = null, Modifier.size(16.dp))
            Text(progressState.buttonText(strings), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        if (isLoggedIn) {
            CollectionTypeButton(uiState.collectionTypeEdit, onSetCollectionType)
        } else {
            AppleButton(onClickLogin, style = AppleButtonStyle.Tinted, height = 48.dp) {
                Text(stringResource(Lang.subject_details_login_to_collect), maxLines = 1)
            }
        }
        AppleCircleButton(
            onClickDownload,
            AppleIcons.download(2f),
            stringResource(Lang.subject_details_manage_cache),
            Modifier.testTag(AppleSubjectDetailsTestTags.Download),
            size = 48.dp,
        )
    }
}

/**
 * 收藏状态按钮: 已收藏为「已在看」等, 未收藏为「追番」, 点击打开 C15 菜单 (五种状态; 已收藏时加「取消追番」, 先确认).
 */
@Composable
private fun CollectionTypeButton(
    presentation: EditableSubjectCollectionTypeState.Presentation,
    onSetType: (UnifiedCollectionType) -> Unit,
) {
    val current = presentation.selfCollectionType
    var expanded by remember { mutableStateOf(false) }
    var confirmUnfollow by remember { mutableStateOf(false) }
    Box {
        AppleButton(
            { expanded = true },
            Modifier.testTag(AppleSubjectDetailsTestTags.CollectionButton),
            style = AppleButtonStyle.Tinted,
            enabled = !presentation.isSetSelfCollectionTypeWorking && !presentation.isPlaceholder,
            height = 48.dp,
            contentPadding = PaddingValues(horizontal = 16.dp),
        ) {
            Text(
                if (current == UnifiedCollectionType.NOT_COLLECTED) {
                    stringResource(Lang.subject_collection_collect)
                } else {
                    renderCollectionTypeAsCurrent(current)
                },
                maxLines = 1,
            )
            Icon(AppleIcons.chevronDown(), contentDescription = null, Modifier.size(14.dp))
        }
        ApplePopoverMenu(
            expanded,
            onDismissRequest = { expanded = false },
            Modifier.testTag(AppleSubjectDetailsTestTags.CollectionMenu),
        ) {
            CollectionTypeMenuItems(current, onDismissRequest = { expanded = false }, onSetType = onSetType)
            if (current != UnifiedCollectionType.NOT_COLLECTED) {
                PopoverMenuItem(
                    stringResource(Lang.subject_collection_delete),
                    onClick = {
                        expanded = false
                        confirmUnfollow = true
                    },
                    Modifier.testTag(AppleSubjectDetailsTestTags.Unfollow),
                    icon = AppleIcons.minusCircle(),
                    showDivider = true,
                )
            }
        }
    }
    if (confirmUnfollow) {
        UnfollowConfirmDialog(
            onConfirm = {
                confirmUnfollow = false
                onSetType(UnifiedCollectionType.NOT_COLLECTED)
            },
            onDismissRequest = { confirmUnfollow = false },
            confirmButtonModifier = Modifier.testTag(AppleSubjectDetailsTestTags.ConfirmUnfollow),
        )
    }
}

/**
 * 分享 (05 G6): C15 菜单「复制链接」「在浏览器中打开」, 链接为 Bangumi 条目页. 不接系统分享面板.
 */
@Composable
private fun ShareButton(subjectId: Int, style: AppleCircleButtonStyle) {
    val clipboard = LocalClipboard.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    var expanded by remember { mutableStateOf(false) }
    val url = bangumiSubjectUrl(subjectId)
    Box {
        AppleCircleButton(
            { expanded = true },
            AppleIcons.share(),
            stringResource(Lang.subject_details_share),
            Modifier.testTag(AppleSubjectDetailsTestTags.Share),
            style = style,
        )
        ApplePopoverMenu(expanded, onDismissRequest = { expanded = false }, Modifier.testTag(AppleSubjectDetailsTestTags.ShareMenu)) {
            PopoverMenuItem(
                stringResource(Lang.subject_details_share_copy_link),
                onClick = {
                    expanded = false
                    scope.launch { clipboard.setClipEntryText(url) }
                },
                icon = AppleIcons.copy(),
            )
            PopoverMenuItem(
                stringResource(Lang.subject_details_share_open_in_browser),
                onClick = {
                    expanded = false
                    uriHandler.openUri(url)
                },
                icon = AppleIcons.externalLink(),
                showDivider = true,
            )
        }
    }
}

internal fun bangumiSubjectUrl(subjectId: Int) = "https://bgm.tv/subject/$subjectId"

/**
 * 分页胶囊 (C4): 详情、评价、讨论.
 */
@Composable
private fun TabPills(pagerState: PagerState, modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    Row(
        modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        SubjectDetailsTab.entries.forEachIndexed { index, tab ->
            PillTab(
                renderSubjectDetailsTab(tab),
                selected = pagerState.currentPage == index,
                onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                Modifier.testTag(AppleSubjectDetailsTestTags.tab(index)),
            )
        }
    }
}
