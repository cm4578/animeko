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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItemsWithLifecycle
import com.kmpalette.color
import com.kmpalette.palette.graphics.Palette
import com.kmpalette.rememberPaletteState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import me.him188.ani.app.data.models.subject.RelatedCharacterInfo
import me.him188.ani.app.data.models.subject.RelatedPersonInfo
import me.him188.ani.app.data.models.subject.SubjectInfo
import me.him188.ani.app.data.models.subject.Tag
import me.him188.ani.app.data.models.subject.nameCn
import me.him188.ani.app.data.models.subject.preferredDisplayName
import me.him188.ani.app.domain.episode.SetEpisodeCollectionTypeRequest
import me.him188.ani.app.domain.foundation.LoadError
import me.him188.ani.app.navigation.LocalNavigator
import me.him188.ani.app.tools.ColorUtils
import me.him188.ani.app.ui.comment.CommentReportHost
import me.him188.ani.app.ui.comment.UIComment
import me.him188.ani.app.ui.foundation.AsyncImage
import me.him188.ani.app.ui.foundation.ImageViewer
import me.him188.ani.app.ui.foundation.ImageViewerBackHandler
import me.him188.ani.app.ui.foundation.LocalSubjectAppearanceSettings
import me.him188.ani.app.ui.foundation.apple.AppleButton
import me.him188.ani.app.ui.foundation.apple.AppleButtonDefaults
import me.him188.ani.app.ui.foundation.apple.AppleButtonStyle
import me.him188.ani.app.ui.foundation.apple.AppleDivider
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.apple.AppleTextButton
import me.him188.ani.app.ui.foundation.apple.EpisodeTile
import me.him188.ani.app.ui.foundation.apple.EpisodeTileDefaults
import me.him188.ani.app.ui.foundation.apple.EpisodeTileState
import me.him188.ani.app.ui.foundation.apple.MacPageToolbar
import me.him188.ani.app.ui.foundation.apple.MacToolbarIconButton
import me.him188.ani.app.ui.foundation.apple.PillTab
import me.him188.ani.app.ui.foundation.apple.bangumiSubjectUrl
import me.him188.ani.app.ui.foundation.apple.episodeTileLabel
import me.him188.ani.app.ui.foundation.avatar.AvatarImage
import me.him188.ani.app.ui.foundation.layout.NestedScrollableColumn
import me.him188.ani.app.ui.foundation.rememberImageViewerHandler
import me.him188.ani.app.ui.foundation.theme.AniColors
import me.him188.ani.app.ui.foundation.theme.AniTextStyles
import me.him188.ani.app.ui.foundation.theme.LocalThemeSettings
import me.him188.ani.app.ui.foundation.theme.MaterialThemeFromPaletteAndImage
import me.him188.ani.app.ui.foundation.widgets.LocalToaster
import me.him188.ani.app.ui.foundation.widgets.showLoadError
import me.him188.ani.app.ui.lang.Lang
import me.him188.ani.app.ui.lang.foundation_richtext_external_app_link_warning_prefix
import me.him188.ani.app.ui.lang.foundation_richtext_open_failed_prefix
import me.him188.ani.app.ui.lang.subject_details_characters
import me.him188.ani.app.ui.lang.subject_details_characters_with_count
import me.him188.ani.app.ui.lang.subject_details_collection_summary
import me.him188.ani.app.ui.lang.subject_details_coming_soon
import me.him188.ani.app.ui.lang.subject_details_episodes
import me.him188.ani.app.ui.lang.subject_details_info
import me.him188.ani.app.ui.lang.subject_details_login_to_collect
import me.him188.ani.app.ui.lang.subject_details_manage_cache
import me.him188.ani.app.ui.lang.subject_details_open_in_bangumi
import me.him188.ani.app.ui.lang.subject_details_rank
import me.him188.ani.app.ui.lang.subject_details_rating
import me.him188.ani.app.ui.lang.subject_details_share
import me.him188.ani.app.ui.lang.subject_details_show_less
import me.him188.ani.app.ui.lang.subject_details_show_more
import me.him188.ani.app.ui.lang.subject_details_staff
import me.him188.ani.app.ui.lang.subject_details_staff_with_count
import me.him188.ani.app.ui.lang.subject_details_stat_collected
import me.him188.ani.app.ui.lang.subject_details_summary
import me.him188.ani.app.ui.lang.subject_details_view_all
import me.him188.ani.app.ui.lang.subject_details_watched_episodes
import me.him188.ani.app.ui.lang.subject_details_write_review
import me.him188.ani.app.ui.rating.EditableRatingDialogsHost
import me.him188.ani.app.ui.richtext.RichTextDefaults
import me.him188.ani.app.ui.search.LoadErrorCard
import me.him188.ani.app.ui.subject.AiringLabel
import me.him188.ani.app.ui.subject.collection.MacPageDefaults
import me.him188.ani.app.ui.subject.collection.components.EditableSubjectCollectionTypeDialogsHost
import me.him188.ani.app.ui.subject.details.components.AnimatedGradientBackground
import me.him188.ani.app.ui.subject.details.components.PersonCard
import me.him188.ani.app.ui.subject.details.components.SubjectCommentColumn
import me.him188.ani.app.ui.subject.details.components.SubjectDetailsDefaults
import me.him188.ani.app.ui.subject.details.layout.AppleSubjectDetailsTestTags
import me.him188.ani.app.ui.subject.details.layout.RelatedSubjectsCompactSection
import me.him188.ani.app.ui.subject.details.sections.SubjectTagsSection
import me.him188.ani.app.ui.subject.details.sections.ViewAllSheet
import me.him188.ani.app.ui.subject.details.sections.groupThousands
import me.him188.ani.app.ui.subject.details.sections.subjectInfoRows
import me.him188.ani.app.ui.subject.details.state.SubjectDetailsState
import me.him188.ani.app.ui.subject.details.state.SubjectDetailsUiState
import me.him188.ani.app.ui.subject.details.state.rememberAiringLabelState
import me.him188.ani.app.ui.subject.details.state.rememberSubjectProgressState
import me.him188.ani.app.ui.subject.episode.list.EpisodeListItem
import me.him188.ani.app.ui.subject.person.PeoplePreviewTarget
import me.him188.ani.app.ui.subject.person.rememberPeopleClickHandler
import me.him188.ani.app.ui.subject.rememberSubjectStatusStrings
import me.him188.ani.app.ui.subject.renderSubjectSeason
import me.him188.ani.app.ui.user.SelfInfoUiState
import me.him188.ani.datasources.api.topic.UnifiedCollectionType
import me.him188.ani.datasources.api.topic.toggleCollected
import org.jetbrains.compose.resources.stringResource

/**
 * macOS 条目详情 (04 §3.7). 推入页, 显示在侧边栏右侧.
 *
 * 工具栏右侧为「在 Bangumi 開啟」与分享 (05 G6). 内容两栏 `240 | 1fr`, 栏间距 32:
 * 左栏为封面、继续观看、收藏状态与快取管理 / 写评价、统计卡与作品信息, 单独纵向滚动;
 * 右栏为标题、分页 (详情 / 评价 / 讨论) 与分页内容, 标题与分页先随内容滚出.
 */
@Composable
fun MacSubjectDetailsScreen(
    vm: SubjectDetailsViewModel,
    onPlay: (episodeId: Int) -> Unit,
    onLoadErrorRetry: () -> Unit,
    onClickTag: (Tag) -> Unit,
    modifier: Modifier = Modifier,
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
        is SubjectDetailsLoadState.Ok -> MacSubjectDetailsPage(
            current.value,
            selfInfo,
            onPlay = onPlay,
            onClickTag = onClickTag,
            onEpisodeCollectionUpdate = { request ->
                scope.launch {
                    vm.setEpisodeCollectionType.invokeSafe(request)?.let { toaster.showLoadError(it) }
                }
            },
            modifier = modifier,
        )

        is SubjectDetailsLoadState.Placeholder -> MacSubjectDetailsLoading(
            current.subjectId,
            current.subjectInfo,
            error = null,
            onRetry = onLoadErrorRetry,
            modifier = modifier,
        )

        is SubjectDetailsLoadState.Err -> MacSubjectDetailsLoading(
            current.subjectId,
            current.placeholder,
            error = current.error,
            onRetry = onLoadErrorRetry,
            modifier = modifier,
        )
    }
}

/**
 * 数据就绪的条目详情.
 */
@Composable
internal fun MacSubjectDetailsPage(
    state: SubjectDetailsState,
    selfInfo: SelfInfoUiState,
    onPlay: (episodeId: Int) -> Unit,
    onClickTag: (Tag) -> Unit,
    onEpisodeCollectionUpdate: (SetEpisodeCollectionTypeRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by state.uiState.collectAsStateWithLifecycle()
    val navigator = LocalNavigator.current
    val toaster = LocalToaster.current
    val uriHandler = LocalUriHandler.current
    val scope = rememberCoroutineScope()
    val isLoggedIn = selfInfo.isSessionValid != false
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

    // 「条目详情页动态主题」开启时页面配色跟随封面; 「动态渐层背景」开启时才显示封面取色的背景 (04 §3.7).
    val themeSettings = LocalThemeSettings.current
    var bitmap by remember { mutableStateOf<ImageBitmap?>(null) }
    val paletteState = rememberPaletteState()
    LaunchedEffect(themeSettings, bitmap) {
        val loaded = bitmap ?: return@LaunchedEffect
        if (themeSettings.useDynamicSubjectPageTheme || themeSettings.enableAnimatedGradientSubjectPage) {
            paletteState.generate(loaded)
        }
    }

    MaterialThemeFromPaletteAndImage(
        if (themeSettings.useDynamicSubjectPageTheme) paletteState.palette else null,
        if (themeSettings.useDynamicSubjectPageTheme) bitmap else null,
    ) {
        state.subjectCommentReportState?.let { CommentReportHost(it) }
        EditableRatingDialogsHost(uiState.rating, state)
        EditableSubjectCollectionTypeDialogsHost(uiState.collectionTypeEdit, state)

        val info = state.info
        MacSubjectDetailsScaffold(
            subjectId = state.subjectId,
            gradientPalette = if (themeSettings.enableAnimatedGradientSubjectPage) paletteState.palette else null,
            modifier = modifier,
            sidebar = {
                MacSubjectCover(
                    info,
                    onLoaded = { bitmap = it },
                    onClick = info?.imageLarge?.takeIf { it.isNotBlank() }?.let { url -> { imageViewer.viewImage(url) } },
                )
                MacSubjectActions(
                    uiState,
                    isLoggedIn = isLoggedIn,
                    onPlay = onPlay,
                    onSetCollectionType = { type ->
                        scope.launch { state.setSelfCollectionType(type)?.let { toaster.showLoadError(it) } }
                    },
                    onClickLogin = { navigator.navigateEmailLoginStart() },
                    onClickCache = { navigator.navigateSubjectCaches(uiState.subjectId) },
                    onClickWriteReview = { state.requestEditRating() },
                )
                MacStatsCard(info, uiState, onClickRating = if (isLoggedIn) ({ state.requestEditRating() }) else null)
                if (info != null) {
                    MacSubjectInfoSection(info, mainEpisodeCount = uiState.episodeListUiState.mainEpisodes.size)
                }
            },
        ) {
            var selectedTab by rememberSaveable { mutableIntStateOf(SubjectDetailsTab.DETAILS.ordinal) }
            NestedScrollableColumn(
                header = {
                    Column(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 16.dp)) {
                        MacSubjectTitle(info, uiState)
                        MacSubjectTabs(
                            selectedTab,
                            commentCount = state.subjectCommentState.count,
                            onSelect = { selectedTab = it },
                            Modifier.padding(top = 16.dp),
                        )
                    }
                },
                content = {
                    when (SubjectDetailsTab.entries[selectedTab]) {
                        SubjectDetailsTab.DETAILS -> {
                            if (info != null) {
                                MacDetailsTabContent(
                                    state,
                                    info,
                                    onPlay = onPlay,
                                    onEpisodeLongClick = onEpisodeLongClick,
                                    onClickTag = onClickTag,
                                    onClickCache = { navigator.navigateSubjectCaches(uiState.subjectId) },
                                    modifier = Modifier.fillMaxSize().nestedScrollWorkaround(state.detailsTabLazyListState),
                                    listState = state.detailsTabLazyListState,
                                )
                            }
                        }

                        SubjectDetailsTab.COMMENTS -> SubjectDetailsDefaults.SubjectCommentColumn(
                            state = state.subjectCommentState,
                            onClickUrl = onClickCommentUrl,
                            onClickImage = { imageViewer.viewImage(it) },
                            reportState = state.subjectCommentReportState,
                            onOpenOriginal = { _: UIComment -> uriHandler.openUri(bangumiSubjectUrl(uiState.subjectId)) },
                            modifier = Modifier.fillMaxSize().nestedScrollWorkaround(state.commentTabLazyGridState),
                            gridState = state.commentTabLazyGridState,
                            contentPadding = PaddingValues(bottom = 32.dp),
                        )

                        SubjectDetailsTab.DISCUSSIONS -> Box(Modifier.fillMaxSize().padding(top = 32.dp), Alignment.TopCenter) {
                            Text(
                                stringResource(Lang.subject_details_coming_soon),
                                style = AniTextStyles.macBody,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )
        }
    }

    ImageViewer(imageViewer) { imageViewer.clear() }
}

/**
 * 加载中或加载失败: 用已知的条目信息 (可能为 `null`) 画封面与标题, 右栏为加载指示或错误卡片.
 */
@Composable
private fun MacSubjectDetailsLoading(
    subjectId: Int,
    info: SubjectInfo?,
    error: LoadError?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MacSubjectDetailsScaffold(
        subjectId = subjectId,
        gradientPalette = null,
        modifier = modifier,
        sidebar = {
            MacSubjectCover(info, onLoaded = {}, onClick = null)
        },
    ) {
        Column(Modifier.fillMaxSize().padding(top = 4.dp)) {
            MacSubjectTitle(info, uiState = null)
            Box(Modifier.fillMaxWidth().padding(top = 32.dp), Alignment.TopCenter) {
                if (error != null) {
                    LoadErrorCard(error, onRetry = onRetry, Modifier.fillMaxWidth())
                } else {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

/**
 * 页面骨架: 工具栏 (「在 Bangumi 開啟」与分享), 其下为 240 宽的左栏 [sidebar] 与右栏 [content].
 *
 * @param gradientPalette 不为 `null` 时在内容下方显示封面取色的动态渐层背景.
 */
@Composable
private fun MacSubjectDetailsScaffold(
    subjectId: Int,
    gradientPalette: Palette?,
    modifier: Modifier = Modifier,
    sidebar: @Composable ColumnScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    // 复用的现有区块有的文字不指定颜色, 由内容色决定.
    CompositionLocalProvider(LocalContentColor provides colorScheme.onSurface) {
        Column(modifier.fillMaxSize().background(colorScheme.surfaceContainerLowest)) {
            MacPageToolbar {
                val uriHandler = LocalUriHandler.current
                MacToolbarIconButton(
                    { uriHandler.openUri(bangumiSubjectUrl(subjectId)) },
                    AppleIcons.macExternalLink(),
                    stringResource(Lang.subject_details_open_in_bangumi),
                    Modifier.testTag(MacSubjectDetailsTestTags.OpenInBangumi),
                )
                var shareExpanded by remember { mutableStateOf(false) }
                Box {
                    MacToolbarIconButton(
                        { shareExpanded = true },
                        AppleIcons.share(),
                        stringResource(Lang.subject_details_share),
                        Modifier.testTag(AppleSubjectDetailsTestTags.Share),
                    )
                    SubjectShareMenu(subjectId, shareExpanded, onDismissRequest = { shareExpanded = false })
                }
            }
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (gradientPalette != null) {
                    val surfaceColor = colorScheme.surface
                    val colors = remember(gradientPalette, surfaceColor) {
                        gradientPalette.swatches
                            .map { ColorUtils.blendColor(it.color, surfaceColor, 0.85f) }
                            .toImmutableList()
                    }
                    AnimatedGradientBackground(colors, speed = 0.05, modifier = Modifier.fillMaxSize())
                }
                Row(
                    Modifier.fillMaxSize().padding(horizontal = MacPageDefaults.HorizontalPadding),
                    horizontalArrangement = Arrangement.spacedBy(MacSubjectDetailsDefaults.ColumnSpacing),
                ) {
                    Column(
                        Modifier
                            .width(MacSubjectDetailsDefaults.SidebarWidth)
                            .fillMaxHeight()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 4.dp, bottom = 32.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        content = sidebar,
                    )
                    Box(Modifier.weight(1f).fillMaxHeight()) {
                        content()
                    }
                }
            }
        }
    }
}

/**
 * 封面: 3:4、圆角 12、阴影 0 8 20 rgba(0,0,0,0.12). 有大图时点击打开图片查看器.
 */
@Composable
private fun MacSubjectCover(
    info: SubjectInfo?,
    onLoaded: (ImageBitmap) -> Unit,
    onClick: (() -> Unit)?,
) {
    val shape = RoundedCornerShape(12.dp)
    AsyncImage(
        info?.imageLarge,
        contentDescription = null,
        Modifier
            .fillMaxWidth()
            .aspectRatio(3f / 4f)
            .dropShadow(shape, Shadow(radius = 20.dp, color = Color.Black.copy(alpha = 0.12f), offset = DpOffset(0.dp, 8.dp)))
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        contentScale = ContentScale.Crop,
        onSuccess = { success -> success.bitmap?.let(onLoaded) },
    )
}

/**
 * 继续观看 (全宽, 高 36, 圆角 8, accentFill) 与操作行: 收藏状态 (tint 底, 高 32, 弹性宽, C15 菜单)、快取管理、写评价 (32×32 fill 方钮).
 * 未登录时收藏状态换成「登录后可收藏」, 不显示写评价.
 */
@Composable
private fun MacSubjectActions(
    uiState: SubjectDetailsUiState,
    isLoggedIn: Boolean,
    onPlay: (episodeId: Int) -> Unit,
    onSetCollectionType: (UnifiedCollectionType) -> Unit,
    onClickLogin: () -> Unit,
    onClickCache: () -> Unit,
    onClickWriteReview: () -> Unit,
) {
    val progressState = uiState.rememberSubjectProgressState()
    val strings = rememberSubjectStatusStrings()
    val episodeId = progressState.episodeIdToPlay
    AppleButton(
        { episodeId?.let(onPlay) },
        Modifier.fillMaxWidth().testTag(AppleSubjectDetailsTestTags.Play),
        style = if (progressState.buttonIsPrimary) AppleButtonStyle.Primary else AppleButtonStyle.Tinted,
        enabled = episodeId != null,
        height = AppleButtonDefaults.MacLargeHeight,
    ) {
        Icon(AppleIcons.playFilled(), contentDescription = null, Modifier.size(14.dp))
        Text(progressState.buttonText(strings), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (isLoggedIn) {
            CollectionTypeButton(
                uiState.collectionTypeEdit,
                onSetCollectionType,
                Modifier.weight(1f),
                height = MacSubjectDetailsDefaults.ActionHeight,
            )
        } else {
            AppleButton(
                onClickLogin,
                Modifier.weight(1f),
                style = AppleButtonStyle.Tinted,
                height = MacSubjectDetailsDefaults.ActionHeight,
            ) {
                Text(stringResource(Lang.subject_details_login_to_collect), maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
        MacSquareIconButton(
            onClickCache,
            AppleIcons.download(2f),
            stringResource(Lang.subject_details_manage_cache),
            Modifier.testTag(AppleSubjectDetailsTestTags.Download),
        )
        if (isLoggedIn) {
            MacSquareIconButton(
                onClickWriteReview,
                AppleIcons.pencil(),
                stringResource(Lang.subject_details_write_review),
                Modifier.testTag(MacSubjectDetailsTestTags.WriteReview),
            )
        }
    }
}

/**
 * 32×32 fill 底方钮, 圆角 8, 16 号 label 图标.
 */
@Composable
private fun MacSquareIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier
            .size(MacSubjectDetailsDefaults.ActionHeight)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription, Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurface)
    }
}

/**
 * 统计卡: card 圆角 12, 评分 / 排名 / 收藏三栏, 数值 17/700, 标签 11 label2. 登录后点评分打开评分编辑.
 */
@Composable
private fun MacStatsCard(
    info: SubjectInfo?,
    uiState: SubjectDetailsUiState,
    onClickRating: (() -> Unit)?,
) {
    val rating = info?.ratingInfo
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        MacStatCell(
            rating?.score?.takeIf { rating.scoreFloat > 0f } ?: "–",
            stringResource(Lang.subject_details_rating),
            Modifier
                .weight(1f)
                .then(if (onClickRating != null) Modifier.clickable(onClick = onClickRating) else Modifier)
                .testTag(AppleSubjectDetailsTestTags.Rating),
        )
        MacStatCell(
            rating?.rank?.takeIf { it > 0 }?.let { "#$it" } ?: "–",
            stringResource(Lang.subject_details_rank),
            Modifier.weight(1f),
        )
        MacStatCell(
            groupThousands(uiState.collectionStats.collect),
            stringResource(Lang.subject_details_stat_collected),
            Modifier.weight(1f),
        )
    }
}

@Composable
private fun MacStatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier.padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            value,
            style = AniTextStyles.macTitle.copy(fontFeatureSettings = "tnum"),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        Text(
            label,
            style = AniTextStyles.macCaption.copy(fontSize = 11.sp, lineHeight = 14.sp),
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
        )
    }
}

/**
 * 作品信息: 小节标题 13/600 + card 圆角 12 的定义列表 (行高 34, 左右内距 12, 键 64 宽 12 label2, 值 13).
 */
@Composable
private fun MacSubjectInfoSection(info: SubjectInfo, mainEpisodeCount: Int) {
    val rows = subjectInfoRows(info, mainEpisodeCount.takeIf { it > 0 })
    if (rows.isEmpty()) return
    val colorScheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            stringResource(Lang.subject_details_info),
            style = AniTextStyles.macCallout.copy(fontWeight = FontWeight.SemiBold),
            color = colorScheme.onSurface,
        )
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.surfaceContainerLow),
        ) {
            rows.forEachIndexed { index, (label, value) ->
                if (index > 0) {
                    AppleDivider(startIndent = 12.dp)
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .heightIn(min = 34.dp)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        label,
                        Modifier.width(64.dp).alignByBaseline(),
                        style = AniTextStyles.macCaption,
                        color = colorScheme.secondary,
                        maxLines = 1,
                    )
                    Text(
                        value,
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
 * 标题: 大标题 + 一行「原名 · 季度 · 话数 · 收藏 / 在看」13 label2. [uiState] 为 `null` (加载中) 时只有原名与季度.
 */
@Composable
private fun MacSubjectTitle(info: SubjectInfo?, uiState: SubjectDetailsUiState?) {
    val colorScheme = MaterialTheme.colorScheme
    val title = info?.preferredDisplayName(LocalSubjectAppearanceSettings.current.useOriginalTitle).orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            title,
            style = AniTextStyles.macLargeTitle,
            color = colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        val parts = buildList {
            info?.name?.takeIf { it.isNotBlank() && it != title }?.let { add(it) }
            if (info != null && info.airDate.isValid) add(renderSubjectSeason(info.airDate))
        }
        CompositionLocalProvider(LocalContentColor provides colorScheme.secondary) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(parts.joinToString(" · "), style = AniTextStyles.macCallout, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (uiState != null) {
                    if (parts.isNotEmpty()) Text(" · ", style = AniTextStyles.macCallout)
                    AiringLabel(
                        uiState.rememberAiringLabelState(),
                        style = AniTextStyles.macCallout,
                        progressColor = colorScheme.secondary,
                    )
                    Text(
                        " · " + stringResource(
                            Lang.subject_details_collection_summary,
                            groupThousands(uiState.collectionStats.collect),
                            groupThousands(uiState.collectionStats.doing),
                        ),
                        style = AniTextStyles.macCallout,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

/**
 * C4 分页: 详情 / 评价 N / 讨论.
 */
@Composable
private fun MacSubjectTabs(
    selected: Int,
    commentCount: Int?,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        SubjectDetailsTab.entries.forEachIndexed { index, tab ->
            PillTab(
                renderSubjectDetailsTab(tab),
                selected = selected == index,
                onClick = { onSelect(index) },
                Modifier.testTag(AppleSubjectDetailsTestTags.tab(index)),
                count = commentCount?.takeIf { tab == SubjectDetailsTab.COMMENTS && it > 0 }?.toString(),
            )
        }
    }
}

/**
 * 详情分页: 选集 (全部集数, 14 栏网格)、简介、标签、角色 (6 栏)、制作人员 (3 栏文字格)、关联条目.
 *
 * 选集按行放进列表, 集数很多时也只组合看得到的行.
 */
@Composable
private fun MacDetailsTabContent(
    state: SubjectDetailsState,
    info: SubjectInfo,
    onPlay: (episodeId: Int) -> Unit,
    onEpisodeLongClick: (EpisodeListItem) -> Unit,
    onClickTag: (Tag) -> Unit,
    onClickCache: () -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
) {
    val uiState by state.uiState.collectAsStateWithLifecycle()
    val episodes = uiState.episodeListUiState.mainEpisodes
    val currentEpisodeId = remember(episodes) { episodes.firstOrNull { !it.isDoneOrDropped }?.episodeId }
    val episodeRows = remember(episodes) { episodes.chunked(MacSubjectDetailsDefaults.EpisodeColumns) }

    val exposedCharacters = state.exposedCharactersPager.collectAsLazyPagingItemsWithLifecycle()
    val allCharacters = state.charactersPager.collectAsLazyPagingItemsWithLifecycle()
    val exposedStaff = state.exposedStaffPager.collectAsLazyPagingItemsWithLifecycle()
    val allStaff = state.staffPager.collectAsLazyPagingItemsWithLifecycle()
    val related = state.relatedSubjectsPager.collectAsLazyPagingItemsWithLifecycle()

    LazyColumn(modifier, state = listState, contentPadding = PaddingValues(bottom = 32.dp)) {
        // 剧集异步加载; item 恒定存在, 加载完成后才不会出现在锚点上方而滚不回去.
        item("episodes") {
            if (episodes.isNotEmpty()) {
                MacSectionHeader(stringResource(Lang.subject_details_episodes), Modifier.padding(bottom = 12.dp)) {
                    Text(
                        stringResource(
                            Lang.subject_details_watched_episodes,
                            episodes.count { it.isDoneOrDropped },
                            episodes.size,
                        ),
                        Modifier.padding(start = 8.dp).testTag(MacSubjectDetailsTestTags.WatchedEpisodes),
                        style = AniTextStyles.macCaption,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Spacer(Modifier.weight(1f))
                    AppleTextButton(onClickCache) {
                        Text(stringResource(Lang.subject_details_manage_cache))
                    }
                }
            }
        }
        items(episodeRows.size, key = { "episodes-$it" }) { rowIndex ->
            Row(
                Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val row = episodeRows[rowIndex]
                for (episode in row) {
                    EpisodeTile(
                        episodeTileLabel(episode.sort),
                        state = when {
                            episode.isDoneOrDropped -> EpisodeTileState.Watched
                            episode.episodeId == currentEpisodeId -> EpisodeTileState.Playing
                            else -> EpisodeTileState.Unwatched
                        },
                        onClick = { onPlay(episode.episodeId) },
                        Modifier
                            .weight(1f)
                            .height(EpisodeTileDefaults.MacGridHeight)
                            .testTag(AppleSubjectDetailsTestTags.episode(episode.episodeId)),
                        onLongClick = { onEpisodeLongClick(episode) },
                        cornerRadius = EpisodeTileDefaults.MacCornerRadius,
                    )
                }
                // 补齐末行空位, 保持方块等宽
                repeat(MacSubjectDetailsDefaults.EpisodeColumns - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }

        if (info.summary.isNotBlank()) {
            item("summary") {
                Column(Modifier.padding(top = MacSubjectDetailsDefaults.SectionSpacing), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MacSectionHeader(stringResource(Lang.subject_details_summary))
                    MacSummary(info.summary)
                }
            }
        }

        if (info.tags.isNotEmpty()) {
            item("tags") {
                SubjectTagsSection(info.tags, onClickTag, Modifier.padding(top = MacSubjectDetailsDefaults.SectionSpacing))
            }
        }

        if (exposedCharacters.itemCount > 0) {
            item("characters") {
                MacCharactersSection(
                    exposedCharacters.itemSnapshotList.items,
                    allCharacters,
                    uiState.totalCharactersCount,
                    Modifier.padding(top = MacSubjectDetailsDefaults.SectionSpacing),
                )
            }
        }

        if (exposedStaff.itemCount > 0) {
            item("staff") {
                MacStaffSection(
                    exposedStaff.itemSnapshotList.items,
                    allStaff,
                    uiState.totalStaffCount,
                    Modifier.padding(top = MacSubjectDetailsDefaults.SectionSpacing),
                )
            }
        }

        if (related.itemCount > 0) {
            item("related") {
                Box(Modifier.padding(top = MacSubjectDetailsDefaults.SectionSpacing)) {
                    RelatedSubjectsCompactSection(
                        state.subjectId,
                        related,
                        headerModifier = Modifier,
                        contentPadding = PaddingValues(0.dp),
                    )
                }
            }
        }
    }
}

/**
 * 区块标题行: 区块标题 17/22 W700, 其后可放说明与 accent 链接.
 */
@Composable
private fun MacSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
) {
    Row(modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(
            title,
            style = AniTextStyles.macTitle,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
        )
        trailing()
    }
}

/**
 * 简介: 最宽 640, 14 号 labelSoft, 默认 5 行; 溢出时末尾为 accent「顯示更多 / 顯示更少」.
 */
@Composable
private fun MacSummary(summary: String) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var hasOverflow by remember { mutableStateOf(false) }
    Column(Modifier.widthIn(max = 640.dp)) {
        Text(
            summary,
            style = AniTextStyles.macBody.copy(lineHeight = 22.sp),
            color = AniColors.labelSoft,
            maxLines = if (expanded) Int.MAX_VALUE else 5,
            overflow = TextOverflow.Ellipsis,
            onTextLayout = { if (!expanded) hasOverflow = it.hasVisualOverflow },
        )
        if (hasOverflow || expanded) {
            AppleTextButton({ expanded = !expanded }, Modifier.align(Alignment.End)) {
                Text(stringResource(if (expanded) Lang.subject_details_show_less else Lang.subject_details_show_more))
            }
        }
    }
}

/**
 * 角色: 6 栏, 间距 16; 56 圆形头像 + 角色名 13/600 + CV 12 label2, 居中. 标题行右侧「查看全部」打开全部角色.
 */
@Composable
private fun MacCharactersSection(
    characters: List<RelatedCharacterInfo>,
    allCharacters: LazyPagingItems<RelatedCharacterInfo>,
    totalCharactersCount: Int?,
    modifier: Modifier = Modifier,
) {
    var showAll by rememberSaveable { mutableStateOf(false) }
    val onClickCharacter = rememberPeopleClickHandler()
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MacSectionHeader(stringResource(Lang.subject_details_characters)) {
            Spacer(Modifier.weight(1f))
            AppleTextButton({ showAll = true }) {
                Text(stringResource(Lang.subject_details_view_all))
            }
        }
        for (row in characters.chunked(MacSubjectDetailsDefaults.CharacterColumns)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                for (character in row) {
                    MacCharacterCell(
                        character,
                        onClick = { onClickCharacter(PeoplePreviewTarget.Character(character.character.id)) },
                        Modifier.weight(1f),
                    )
                }
                repeat(MacSubjectDetailsDefaults.CharacterColumns - row.size) {
                    Spacer(Modifier.weight(1f))
                }
            }
        }
    }
    if (showAll) {
        ViewAllSheet(
            title = totalCharactersCount?.let { stringResource(Lang.subject_details_characters_with_count, it) }
                ?: stringResource(Lang.subject_details_characters),
            items = allCharacters,
            onDismissRequest = { showAll = false },
        ) {
            PersonCard(
                it,
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        showAll = false
                        onClickCharacter(PeoplePreviewTarget.Character(it.character.id))
                    },
            )
        }
    }
}

@Composable
private fun MacCharacterCell(
    info: RelatedCharacterInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cv = remember(info) { info.character.actors.firstOrNull()?.displayName }
    Column(
        modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        // 立绘顶部为脸部, 顶部对齐
        Box(Modifier.size(56.dp).clip(CircleShape)) {
            AvatarImage(
                info.character.imageMedium,
                Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
            )
        }
        Text(
            info.character.displayName,
            style = AniTextStyles.macCallout.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Text(
            cv ?: info.role.nameCn,
            style = AniTextStyles.macCaption,
            color = MaterialTheme.colorScheme.secondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * 制作人员: card 圆角 12 的 3 栏文字格 (内距 10/14, 职位 12 label2 + 姓名 13/600), 不放缩图. 标题行右侧「查看全部」.
 */
@Composable
private fun MacStaffSection(
    staff: List<RelatedPersonInfo>,
    allStaff: LazyPagingItems<RelatedPersonInfo>,
    totalStaffCount: Int?,
    modifier: Modifier = Modifier,
) {
    var showAll by rememberSaveable { mutableStateOf(false) }
    val onClickPerson = rememberPeopleClickHandler()
    val colorScheme = MaterialTheme.colorScheme
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        MacSectionHeader(stringResource(Lang.subject_details_staff)) {
            Spacer(Modifier.weight(1f))
            AppleTextButton({ showAll = true }) {
                Text(stringResource(Lang.subject_details_view_all))
            }
        }
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(colorScheme.surfaceContainerLow),
        ) {
            for (row in staff.take(MacSubjectDetailsDefaults.MaxStaff).chunked(MacSubjectDetailsDefaults.StaffColumns)) {
                Row(Modifier.fillMaxWidth()) {
                    for (person in row) {
                        Column(
                            Modifier
                                .weight(1f)
                                .clickable { onClickPerson(PeoplePreviewTarget.Person(person.personInfo.id)) }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                person.position.nameCn ?: "",
                                style = AniTextStyles.macCaption,
                                color = colorScheme.secondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Text(
                                person.personInfo.displayName,
                                style = AniTextStyles.macCallout.copy(fontWeight = FontWeight.SemiBold),
                                color = colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    repeat(MacSubjectDetailsDefaults.StaffColumns - row.size) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
    if (showAll) {
        ViewAllSheet(
            title = totalStaffCount?.let { stringResource(Lang.subject_details_staff_with_count, it) }
                ?: stringResource(Lang.subject_details_staff),
            items = allStaff,
            onDismissRequest = { showAll = false },
        ) {
            PersonCard(
                it,
                Modifier
                    .clip(MaterialTheme.shapes.small)
                    .clickable {
                        showAll = false
                        onClickPerson(PeoplePreviewTarget.Person(it.personInfo.id))
                    },
            )
        }
    }
}

internal object MacSubjectDetailsDefaults {
    val SidebarWidth = 240.dp
    val ColumnSpacing = 32.dp
    val ActionHeight = 32.dp
    val SectionSpacing = 28.dp
    const val EpisodeColumns = 14
    const val CharacterColumns = 6
    const val StaffColumns = 3

    /** 制作人员最多 3 行. */
    const val MaxStaff = 9
}

internal object MacSubjectDetailsTestTags {
    const val OpenInBangumi = "mac-subject-details-open-in-bangumi"
    const val WriteReview = "mac-subject-details-write-review"
    const val WatchedEpisodes = "mac-subject-details-watched-episodes"
}
