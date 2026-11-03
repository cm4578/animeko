/*
 * Copyright (C) 2026 OpenAni and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license, which can be found at the following link.
 *
 * https://github.com/open-ani/ani/blob/main/LICENSE
 */

package me.him188.ani.app.ui.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntryDecorator
import me.him188.ani.app.navigation.AniNavigator
import me.him188.ani.app.navigation.MainScreenPage
import me.him188.ani.app.navigation.NavRoutes
import me.him188.ani.app.navigation.SettingsTab
import me.him188.ani.app.navigation.getText
import me.him188.ani.app.ui.adaptive.navigation.AppleNavigationRail
import me.him188.ani.app.ui.adaptive.navigation.AppleNavigationRailItem
import me.him188.ani.app.ui.adaptive.navigation.FloatingTabBar
import me.him188.ani.app.ui.adaptive.navigation.FloatingTabBarDefaults
import me.him188.ani.app.ui.adaptive.navigation.FloatingTabBarItem
import me.him188.ani.app.ui.foundation.apple.AppleIcons
import me.him188.ani.app.ui.foundation.layout.AniWindowInsets
import me.him188.ani.app.ui.foundation.theme.LocalAppChromeOverlayInsets

/**
 * iOS 主分页的选中状态. 返回栈中每个 [NavRoutes.Main] 条目各自记住选中的分页.
 *
 * 以返回栈里的条目实例为键: 同一个 [NavRoutes.Main] 路由可能在栈中出现多次, 新压入的条目从它的
 * [NavRoutes.Main.initialPage] 开始. 推入页上的浮动导航栏读取最近一个 Main 条目的分页 (05 §8.5),
 * 这时该条目不在组合中, 所以状态放在 [androidx.navigation3.ui.NavDisplay] 外层.
 */
@Stable
internal class AppleMainPages(private val backStack: List<NavRoutes>) {
    private val pages = mutableStateListOf<Pair<NavRoutes.Main, MainScreenPage>>()

    fun pageOf(route: NavRoutes.Main): MainScreenPage =
        pages.lastOrNull { it.first === route }?.second ?: route.initialPage

    fun setPage(route: NavRoutes.Main, page: MainScreenPage) {
        // 顺便清掉已出栈的条目
        pages.removeAll { (main, _) -> main === route || backStack.none { it === main } }
        pages.add(route to page)
    }

    companion object {
        /**
         * 按条目在返回栈中的位置保存. 恢复时返回栈已先恢复, 位置对应的是同一个条目.
         */
        fun saver(backStack: List<NavRoutes>): Saver<AppleMainPages, List<String>> = Saver(
            save = { state ->
                state.pages.mapNotNull { (main, page) ->
                    val index = backStack.indexOfFirst { it === main }
                    if (index == -1) null else "$index:${page.name}"
                }
            },
            restore = { saved ->
                AppleMainPages(backStack).apply {
                    for (item in saved) {
                        val index = item.substringBefore(':').toIntOrNull() ?: continue
                        val main = backStack.getOrNull(index) as? NavRoutes.Main ?: continue
                        val page = MainScreenPage.entries.find { it.name == item.substringAfter(':') } ?: continue
                        pages.add(main to page)
                    }
                }
            },
        )
    }
}

@Composable
internal fun rememberAppleMainPages(backStack: List<NavRoutes>): AppleMainPages =
    rememberSaveable(backStack, saver = AppleMainPages.saver(backStack)) { AppleMainPages(backStack) }

/**
 * 页面与 iOS 导航栏 (C1) 的关系.
 */
internal enum class AppleTabBarMode {
    /** 全屏任务, 不显示导航栏. */
    Hidden,

    /** 内容延伸到浮动导航栏下方, 页面自己在底部留出 [LocalAppChromeOverlayInsets]. 用于 Apple 版页面. */
    Overlay,

    /** 内容止于浮动导航栏上方. 用于还没有 Apple 版的页面. */
    Inset,
}

/**
 * 02 C1 的显示规则: 全屏任务 (播放、搜索、资料源编辑器、登录/授权/扫码、合并收藏、编辑个人资料) 隐藏导航栏, 其余显示.
 */
internal fun NavRoutes.appleTabBarMode(): AppleTabBarMode = when (this) {
    is NavRoutes.Main,
    is NavRoutes.Schedule,
    is NavRoutes.SubjectDetail -> AppleTabBarMode.Overlay

    is NavRoutes.EpisodeDetail,
    is NavRoutes.SubjectSearch,
    is NavRoutes.EditMediaSource,
    NavRoutes.EmailLoginStart,
    NavRoutes.EmailLoginVerify,
    is NavRoutes.OAuthAuthorize,
    NavRoutes.QrLoginScan,
    is NavRoutes.QrLoginConfirm,
    NavRoutes.BangumiMerge -> AppleTabBarMode.Hidden

    // 帐号设置即编辑个人资料
    is NavRoutes.Settings -> if (tab == SettingsTab.PROFILE) AppleTabBarMode.Hidden else AppleTabBarMode.Inset

    else -> AppleTabBarMode.Inset
}

/**
 * iOS 的导航外壳: 把 C1 导航栏画在 [content] (`NavDisplay`) 外层, 主分页与推入页都显示 (05 G10).
 *
 * iPhone 是悬浮在内容上方的 [FloatingTabBar]; iPad 等宽屏是左侧的 [AppleNavigationRail] (05 G12).
 * 高亮的是来源分页, 即返回栈中最近一个 [NavRoutes.Main] 条目选中的分页 (05 §8.5).
 * 点其他分页: 弹回该 Main 条目再切换; 点当前分页: 弹回该 Main 条目, 即回到该分页的根页面.
 *
 * 浮动导航栏模式下, 提供 [LocalAppChromeOverlayInsets] 给 [AppleTabBarMode.Overlay] 的页面留出底部空白.
 */
@Composable
internal fun AppleNavigationShell(
    backStack: List<NavRoutes>,
    navigator: AniNavigator,
    mainPages: AppleMainPages,
    layoutType: NavigationSuiteType,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val mainIndex = backStack.indexOfLast { it is NavRoutes.Main }
    val main = backStack.getOrNull(mainIndex) as? NavRoutes.Main
    val visible = main != null && backStack.last().appleTabBarMode() != AppleTabBarMode.Hidden
    val selectedPage = main?.let { mainPages.pageOf(it) }

    fun onClickPage(page: MainScreenPage) {
        main ?: return
        mainPages.setPage(main, page)
        if (mainIndex != backStack.lastIndex) {
            navigator.popBackStack(main, inclusive = false)
        }
    }

    if (layoutType == NavigationSuiteType.NavigationBar) {
        CompositionLocalProvider(
            LocalAppChromeOverlayInsets provides WindowInsets(bottom = FloatingTabBarDefaults.ContentBottomSpace),
        ) {
            Box(modifier) {
                content()
                AnimatedVisibility(
                    visible,
                    Modifier.align(Alignment.BottomCenter),
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut(),
                ) {
                    FloatingTabBar {
                        for (page in MainScreenPage.visibleEntries) {
                            FloatingTabBarItem(
                                selected = page == selectedPage,
                                onClick = { onClickPage(page) },
                                icon = page.appleIcon(1.8f),
                                selectedIcon = page.appleIcon(2f),
                                label = page.getText(),
                            )
                        }
                    }
                }
            }
        }
    } else {
        Row(modifier) {
            AnimatedVisibility(
                visible,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {
                AppleNavigationRail {
                    for (page in MainScreenPage.visibleEntries) {
                        AppleNavigationRailItem(
                            selected = page == selectedPage,
                            onClick = { onClickPage(page) },
                            icon = page.appleIcon(1.8f),
                            selectedIcon = page.appleIcon(2f),
                            label = page.getText(),
                        )
                    }
                }
            }
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .then(
                        if (visible) {
                            Modifier.consumeWindowInsets(AniWindowInsets.forNavigationRail().only(WindowInsetsSides.Start))
                        } else {
                            Modifier
                        },
                    ),
            ) {
                content()
            }
        }
    }
}

/**
 * [AppleTabBarMode.Inset] 的页面在浮动导航栏上方结束, 不被导航栏遮住.
 *
 * `NavEntry` 不公开路由, 这里用 `contentKey` 找回: `entryProvider` 的条目默认以 `route.toString()` 为 `contentKey`.
 */
@Composable
internal fun rememberAppleTabBarInsetDecorator(
    backStack: List<NavRoutes>,
    layoutType: NavigationSuiteType,
): NavEntryDecorator<NavRoutes> = remember(backStack, layoutType) {
    NavEntryDecorator { entry ->
        val route = backStack.lastOrNull { it.toString() == entry.contentKey }
        val inset = layoutType == NavigationSuiteType.NavigationBar &&
                route?.appleTabBarMode() == AppleTabBarMode.Inset &&
                backStack.any { it is NavRoutes.Main }
        if (inset) {
            Box(
                Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom))
                    .padding(bottom = FloatingTabBarDefaults.Height + FloatingTabBarDefaults.BottomMargin),
            ) {
                entry.Content()
            }
        } else {
            entry.Content()
        }
    }
}

/**
 * Apple 版页面底部要留出的空白: 系统底部安全区, 加上浮动导航栏占用的 [LocalAppChromeOverlayInsets].
 */
@Composable
internal fun appleBottomInsets(): WindowInsets =
    WindowInsets.navigationBars.only(WindowInsetsSides.Bottom).add(LocalAppChromeOverlayInsets.current)

private fun MainScreenPage.appleIcon(strokeWidth: Float) = when (this) {
    MainScreenPage.Exploration -> AppleIcons.explore(strokeWidth)
    MainScreenPage.Collection -> AppleIcons.collection(strokeWidth)
    MainScreenPage.CacheManagement -> AppleIcons.download(strokeWidth)
    MainScreenPage.Profile -> AppleIcons.profile(strokeWidth)
}
